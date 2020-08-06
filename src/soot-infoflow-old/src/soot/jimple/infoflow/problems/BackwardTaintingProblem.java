/*******************************************************************************
 * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
 * Bodden, and others.
 ******************************************************************************/

package soot.jimple.infoflow.problems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import Extractocol.Constants;
import extractocol.frontend.AbstractSlice;
import extractocol.frontend.FlowsensitiveSlice;
import heros.FlowFunction;
import heros.FlowFunctions;
import heros.flowfunc.Identity;
import heros.flowfunc.KillAll;
import soot.ArrayType;
import soot.BooleanType;
import soot.IntType;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.LengthExpr;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.aliasing.Aliasing;
import soot.jimple.infoflow.aliasing.IAliasingStrategy;
import soot.jimple.infoflow.collect.MutableTwoElementSet;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.handlers.TaintPropagationHandler;
import soot.jimple.infoflow.handlers.TaintPropagationHandler.FlowFunctionType;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.solver.functions.SolverCallFlowFunction;
import soot.jimple.infoflow.solver.functions.SolverCallToReturnFlowFunction;
import soot.jimple.infoflow.solver.functions.SolverNormalFlowFunction;
import soot.jimple.infoflow.solver.functions.SolverReturnFlowFunction;
import soot.jimple.infoflow.source.ISourceSinkManager;
import soot.jimple.infoflow.source.SourceInfo;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import soot.jimple.infoflow.util.BaseSelector;
import soot.jimple.infoflow.util.SystemClassHandler;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

/**
 * class which contains the flow functions for the backwards solver. This is required for on-demand alias analysis.
 */
public class BackwardTaintingProblem extends AbstractInfoflowProblem
{
	private final Aliasing aliasing;
	private List<AbstractSlice> ass = new ArrayList<AbstractSlice>();
	private List<FlowsensitiveSlice> flowsensitive_results = new ArrayList<FlowsensitiveSlice>();
	private boolean DEBUG_ALL = true;
	private boolean DEBUG_NORMAL = false;
	private boolean DEBUG_CALL = false;
	private boolean DEBUG_RETURN = false;
	private boolean DEBUG_CALLTORETURN = false;
	public void setTaintWrapper(ITaintPropagationWrapper wrapper)
	{
		taintWrapper = wrapper;
	}
	public BackwardTaintingProblem(BiDiInterproceduralCFG<Unit, SootMethod> icfg, ISourceSinkManager sourceSinkManager, IAliasingStrategy aliasingStrategy)
	{
		super(icfg, sourceSinkManager);
		this.aliasing = new Aliasing(aliasingStrategy, (IInfoflowCFG) icfg);
		if (DEBUG_ALL)
		{
			DEBUG_NORMAL = DEBUG_CALL = DEBUG_RETURN = DEBUG_CALLTORETURN = true;
		}
	}
	private Collection<Abstraction> computeWrapperTaints(Abstraction d1, Stmt iStmt, Abstraction source)
	{
		assert inspectSources || source != getZeroValue();
		// If we don't have a taint wrapper, there's nothing we can do here
		if (taintWrapper == null)
			return Collections.emptySet();
		// Do not check taints that are not mentioned anywhere in the call
		if (!source.getAccessPath().isStaticFieldRef() && !source.getAccessPath().isEmpty())
		{
			boolean found = false;
			// The base object must be tainted
			if (iStmt.getInvokeExpr() instanceof InstanceInvokeExpr)
			{
				InstanceInvokeExpr iiExpr = (InstanceInvokeExpr) iStmt.getInvokeExpr();
				found = aliasing.mayAlias(iiExpr.getBase(), source.getAccessPath().getPlainValue());
			}
			if (iStmt instanceof AssignStmt)
			{
				Value value = ((AssignStmt) iStmt).getLeftOp();
				found |= aliasing.mayAlias(value, source.getAccessPath().getPlainValue());
			}
			// or one of the parameters must be tainted
			if (!found)
				for (int paramIdx = 0; paramIdx < iStmt.getInvokeExpr().getArgCount(); paramIdx++)
					if (aliasing.mayAlias(source.getAccessPath().getPlainValue(), iStmt.getInvokeExpr().getArg(paramIdx)))
					{
						found = true;
						break;
					}
			// If nothing is tainted, we don't have any taints to propagate
			if (!found)
				return Collections.emptySet();
		}
		Set<Abstraction> res = taintWrapper.getTaintsForMethod2(iStmt, d1, source);
		return res;
	}
	@Override
	public FlowFunctions<Unit, Abstraction, SootMethod> createFlowFunctionsFactory()
	{
		return new FlowFunctions<Unit, Abstraction, SootMethod>()
		{
			/**
			 * Notifies the outbound flow handlers, if any, about the computed result abstractions for the current flow function
			 * 
			 * @param d1
			 *            The abstraction at the beginning of the method
			 * @param stmt
			 *            The statement that has just been processed
			 * @param incoming
			 *            The incoming abstraction from which the outbound ones were computed
			 * @param outgoing
			 *            The outbound abstractions to be propagated on
			 * @param functionType
			 *            The type of flow function that was computed
			 * @return The outbound flow abstractions, potentially changed by the flow handlers
			 */
			private Set<Abstraction> notifyOutFlowHandlers(Unit stmt, Abstraction d1, Abstraction incoming, Set<Abstraction> outgoing, FlowFunctionType functionType)
			{
				// if (stmt.toString().contains("<flipboard.service.Flap: void
				// a()>"))
				// {
				// System.out.println("Found!!");
				// }
				// if(stmt.toString().contains("<me.lyft.android.infrastructure.api.BaseApi$2:
				// void
				// <init>(me.lyft.android.infrastructure.api.BaseApi,com.squareup.okhttp.Request$Builder,java.lang.Class)>"))
				// {
				// System.out.println("cadfasdfasdf");
				// }
				switch (functionType)
				{
					case NormalFlowFunction:
						if (DEBUG_NORMAL)
							System.out.println("res : " + outgoing);
					break;
					case CallToReturnFlowFunction:
						if (DEBUG_CALLTORETURN)
							System.out.println("res : " + outgoing);
					break;
					case CallFlowFunction:
						if (DEBUG_CALL)
							System.out.println("res : " + outgoing);
					break;
					case ReturnFlowFunction:
						if (DEBUG_RETURN)
							System.out.println("res : " + outgoing);
					break;
				}
				if (taintPropagationHandlers != null && outgoing != null && !outgoing.isEmpty())
					for (TaintPropagationHandler tp : taintPropagationHandlers)
						outgoing = tp.notifyFlowOut(stmt, d1, incoming, outgoing, interproceduralCFG(), functionType);
				if (!outgoing.isEmpty())
				{
					for (Abstraction abs : outgoing)
					{
						boolean found = false;
						if (abs != null && abs.getSourceContext() != null)
						{
							for (AbstractSlice as : ass)
							{
								if (as.hasDemarcationPoint(abs.getSourceContext().getStmt(), interproceduralCFG().getMethodOf(abs.getSourceContext().getStmt())))
								{
									if (abs.getCurrentStmt().containsInvokeExpr())
									{
										if (abs.getCurrentStmt().getInvokeExpr() instanceof InstanceInvokeExpr)
										{
											InstanceInvokeExpr iie = (InstanceInvokeExpr) abs.getCurrentStmt().getInvokeExpr();
											if (iie.getArgCount() == 0)
											{
												as.addSlice(abs.getCurrentStmt(), iie.getMethod());
											}
										}
									}
									as.addSlice(abs.getCurrentStmt(), interproceduralCFG().getMethodOf(abs.getCurrentStmt()));
									found = true;
									break;
								}
							}
							if (!found)
							{
								AbstractSlice newas = new AbstractSlice(abs.getSourceContext().getStmt(), interproceduralCFG().getMethodOf(abs.getSourceContext().getStmt()), abs.getCurrentStmt(),
										interproceduralCFG().getMethodOf(abs.getCurrentStmt()));
								ass.add(newas);
							}
						}
					}
					for (Abstraction abs : outgoing)
					{
						boolean hasTaintset = false;
						for (FlowsensitiveSlice br : flowsensitive_results)
						{
							if (br.equals(abs.gettaintmethods()))
							{
								hasTaintset = true;
							}
						}
						if (!hasTaintset && abs.gettaintmethods() != null && abs.gettaintmethods().size() > 0)
						{
							FlowsensitiveSlice br = new FlowsensitiveSlice(abs.gettaintmethods().getLast(), interproceduralCFG().getMethodOf(abs.getSourceContext().getStmt()),
									interproceduralCFG().getMethodOf(abs.getSourceContext().getStmt()).getSignature(), abs.getSourceContext().getStmt(), abs.gettaintmethods());
							flowsensitive_results.add(br);
						}
					}
				}
				return outgoing;
			}
			/**
			 * Computes the aliases for the given statement
			 * 
			 * @param def
			 *            The definition statement from which to extract the alias information
			 * @param leftValue
			 *            The left side of def. Passed in to allow for caching, no need to recompute this for every abstraction being processed.
			 * @param d1
			 *            The abstraction at the method's start node
			 * @param source
			 *            The source abstraction of the alias search from before the current statement
			 * @return The set of abstractions after the current statement
			 */
			private Set<Abstraction> computeAliases(final DefinitionStmt defStmt, Value leftValue, Abstraction d1, Abstraction source)
			{
				assert !source.getAccessPath().isEmpty();
				// A backward analysis looks for aliases of existing taints and
				// thus
				// cannot create new taints out of thin air
				if (source == getZeroValue())
					return Collections.emptySet();
				final Set<Abstraction> res = new MutableTwoElementSet<Abstraction>();
				// Check whether the left side of the assignment matches our
				// current taint abstraction
				final boolean leftSideMatches = baseMatches(leftValue, source);
				if (!leftSideMatches)
					res.add(source);
				else
				{
					// The left side is overwritten completely
					// If we have an assignment to the base local of the current
					// taint,
					// all taint propagations must be below that point, so this
					// is the
					// right point to turn around.
					// for (Unit u : interproceduralCFG().getPredsOf(defStmt))
					// fSolver.processEdge(new PathEdge<Unit, Abstraction>(d1,
					// u, source));
				}
				if (defStmt instanceof AssignStmt)
				{
					// Get the right side of the assignment
					final Value rightValue = BaseSelector.selectBase(defStmt.getRightOp(), false);
					// Is the left side overwritten completely?
					if (leftSideMatches)
					{
						// Termination shortcut: If the right side is a value we
						// do not track,
						// we can stop here.
						if (!(rightValue instanceof Local || rightValue instanceof FieldRef))
							return Collections.emptySet();
					}
					// If we assign a constant, there is no need to track the
					// right side
					// any further or do any forward propagation since constants
					// cannot
					// carry taint.
					if (rightValue instanceof Constant)
						return res;
					// We only process heap objects. Binary operations can only
					// be performed on primitive objects.
					if (rightValue instanceof BinopExpr)
						return res;
					// If we have a = x with the taint "x" being inactive,
					// we must not taint the left side. We can only taint
					// the left side if the tainted value is some "x.y".
					boolean aliasOverwritten = baseMatchesStrict(rightValue, source) && rightValue.getType() instanceof RefType && !source.dependsOnCutAP();
					if (!aliasOverwritten)
					{
						// If the tainted value 'b' is assigned to variable 'a'
						// and 'b'
						// is a heap object, we must also look for aliases of
						// 'a' upwards
						// from the current statement.
						Abstraction newLeftAbs = null;
						if (rightValue instanceof InstanceFieldRef)
						{
							InstanceFieldRef ref = (InstanceFieldRef) rightValue;
							if (source.getAccessPath().isInstanceFieldRef() && ref.getBase() == source.getAccessPath().getPlainValue() && source.getAccessPath().firstFieldMatches(ref.getField()))
							{
								newLeftAbs = source.deriveNewAbstraction(leftValue, true, defStmt, source.getAccessPath().getFirstFieldType());
							}
						}
						else if (enableStaticFields && rightValue instanceof StaticFieldRef)
						{
							StaticFieldRef ref = (StaticFieldRef) rightValue;
							if (source.getAccessPath().isStaticFieldRef() && source.getAccessPath().firstFieldMatches(ref.getField()))
							{
								newLeftAbs = source.deriveNewAbstraction(leftValue, true, defStmt, source.getAccessPath().getBaseType());
							}
						}
						else if (rightValue == source.getAccessPath().getPlainValue())
						{
							Type newType = source.getAccessPath().getBaseType();
							if (leftValue instanceof ArrayRef)
								newType = buildArrayOrAddDimension(newType);
							else if (defStmt.getRightOp() instanceof ArrayRef)
							{
								try
								{
									newType = ((ArrayType) newType).getElementType();
								}
								catch (Exception e)
								{
									// System.out.println("type conversion
									// error");
									return Collections.emptySet();
								}
							}
							// If this is an unrealizable typecast, drop the
							// abstraction
							if (defStmt.getRightOp() instanceof CastExpr)
							{
								CastExpr ce = (CastExpr) defStmt.getRightOp();
								if (!checkCast(source.getAccessPath(), ce.getCastType()))
									return Collections.emptySet();
								// If the cast was realizable, we can assume
								// that we had the
								// type to which we cast. Do not loosen types,
								// though.
								if (!Scene.v().getFastHierarchy().canStoreType(newType, ce.getCastType()))
									newType = ce.getCastType();
							}
							// Special type handling for certain operations
							else if (defStmt.getRightOp() instanceof LengthExpr)
							{
								assert source.getAccessPath().getBaseType() instanceof ArrayType;
								newLeftAbs = source.deriveNewAbstraction(new AccessPath(leftValue, null, IntType.v(), (Type[]) null, true), defStmt);
							}
							else if (defStmt.getRightOp() instanceof InstanceOfExpr)
								newLeftAbs = source.deriveNewAbstraction(new AccessPath(leftValue, null, BooleanType.v(), (Type[]) null, true), defStmt);
							if (newLeftAbs == null)
								newLeftAbs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(leftValue, newType, false), defStmt);
						}
						if (newLeftAbs != null)
						{
							res.add(newLeftAbs);
							// Inject the new alias into the forward solver
							// for (Unit u :
							// interproceduralCFG().getPredsOf(defStmt))
							// fSolver.processEdge(new PathEdge<Unit,
							// Abstraction>(d1, u, newLeftAbs));
						}
					}
					// If we have the tainted value on the left side of the
					// assignment,
					// we also have to look or aliases of the value on the right
					// side of
					// the assignment.
					if (rightValue instanceof Local || rightValue instanceof FieldRef)
					{
						boolean addRightValue = false;
						boolean cutFirstField = false;
						Type targetType = null;
						// if both are fields, we have to compare their
						// fieldName via equals and their bases via PTS
						if (leftValue instanceof InstanceFieldRef)
						{
							InstanceFieldRef leftRef = (InstanceFieldRef) leftValue;
							if (leftRef.getBase() == source.getAccessPath().getPlainValue())
							{
								if (source.getAccessPath().isInstanceFieldRef())
								{
									if (source.getAccessPath().firstFieldMatches(leftRef.getField()))
									{
										targetType = source.getAccessPath().getFirstFieldType();
										addRightValue = true;
										cutFirstField = true;
									}
								}
							}
							// indirect taint propagation:
							// if leftValue is local and source is instancefield
							// of this local:
						}
						else if (leftValue instanceof Local && source.getAccessPath().isInstanceFieldRef())
						{
							Local base = source.getAccessPath().getPlainValue(); // ?
							if (leftValue == base)
							{
								targetType = source.getAccessPath().getBaseType();
								addRightValue = true;
							}
						}
						else if (leftValue instanceof ArrayRef)
						{
							Local leftBase = (Local) ((ArrayRef) leftValue).getBase();
							if (leftBase == source.getAccessPath().getPlainValue())
							{
								addRightValue = true;
								targetType = source.getAccessPath().getBaseType();
								assert source.getAccessPath().getBaseType() instanceof ArrayType;
							}
							// generic case, is true for Locals, ArrayRefs that
							// are equal etc..
						}
						else if (leftValue == source.getAccessPath().getPlainValue())
						{
							addRightValue = true;
							targetType = source.getAccessPath().getBaseType();
							// Check for unrealizable casts. If a = (O) b and a
							// is tainted,
							// but incompatible to the type of b, this cast is
							// impossible
							if (defStmt.getRightOp() instanceof CastExpr)
							{
								CastExpr ce = (CastExpr) defStmt.getRightOp();
								if (!checkCast(source.getAccessPath(), ce.getOp().getType()))
									return Collections.emptySet();
							}
						}
						// if one of them is true -> add rightValue
						if (addRightValue)
						{
							if (targetType != null)
							{
								// Special handling for some operations
								if (defStmt.getRightOp() instanceof ArrayRef)
									targetType = buildArrayOrAddDimension(targetType);
								else if (leftValue instanceof ArrayRef)
								{
									assert source.getAccessPath().getBaseType() instanceof ArrayType;
									if (targetType instanceof ArrayType)
										targetType = ((ArrayType) targetType).getElementType();
									// If the types do not match, the right side
									// cannot be an alias
									if (!canCastType(rightValue.getType(), targetType))
										addRightValue = false;
									else
									{
										// If we have a type of
										// java.lang.Object, we try to tighten
										// it
										if (isObjectLikeType(targetType))
											targetType = rightValue.getType();
									}
								}
							}
							// Special type handling for certain operations
							if (defStmt.getRightOp() instanceof LengthExpr)
								targetType = null;
							// We do not need to handle casts. Casts only make
							// types more imprecise when going backwards.
							// If the right side's type is not compatible with
							// our current type,
							// this cannot be an alias
							if (addRightValue)
							{
								if (!canCastType(rightValue.getType(), targetType))
									addRightValue = false;
							}
							// Make sure to only track static fields if it has
							// been enabled
							if (addRightValue)
								if (!enableStaticFields && rightValue instanceof StaticFieldRef)
									addRightValue = false;
							if (addRightValue)
							{
								Abstraction newAbs = source.deriveNewAbstraction(rightValue, cutFirstField, defStmt, targetType);
								res.add(newAbs);
								// Inject the new alias into the forward solver
								// for (Unit u :
								// interproceduralCFG().getPredsOf(defStmt))
								// fSolver.processEdge(new PathEdge<Unit,
								// Abstraction>(d1, u, newAbs));
							}
						}
					}
				}
				else if (defStmt instanceof IdentityStmt)
					res.add(source);
				return res;
			}
			@Override
			public FlowFunction<Abstraction> getNormalFlowFunction(final Unit src, final Unit dest)
			{
				if (DEBUG_NORMAL)
				{
					System.out.println("NormalFlow : " + src.toString());
				}
				if (src instanceof DefinitionStmt)
				{
					final DefinitionStmt defStmt = (DefinitionStmt) src;
					final Value leftValue = BaseSelector.selectBase(defStmt.getLeftOp(), true);
					final DefinitionStmt destDefStmt = dest instanceof DefinitionStmt ? (DefinitionStmt) dest : null;
					final Value destLeftValue = destDefStmt == null ? null : BaseSelector.selectBase(destDefStmt.getLeftOp(), true);
					return new SolverNormalFlowFunction()
					{
						@Override
						public Set<Abstraction> computeTargets(Abstraction d1, Abstraction source)
						{
							Set<Abstraction> res = computeTargetsInternal(d1, source);
							return notifyOutFlowHandlers(src, d1, source, res, FlowFunctionType.CallFlowFunction);
						}
						public Set<Abstraction> computeTargetsInternal(Abstraction d1, Abstraction source)
						{
							if (DEBUG_NORMAL)
							{
								System.out.println("\n[*] NormalFlow" + "\nsrc : " + src + "\ndest : " + dest + "\nsource : " + source);
							}
							
							if (source == getZeroValue())
								return Collections.emptySet();
							assert source.isAbstractionActive() || flowSensitiveAliasing;
							Set<Abstraction> res = computeAliases(defStmt, leftValue, d1, source);
							if (destDefStmt != null && interproceduralCFG().isExitStmt(destDefStmt))
								for (Abstraction abs : res)
									computeAliases(destDefStmt, destLeftValue, d1, abs);
							return res;
						}
					};
				}
				return Identity.v();
			}
			@Override
			public FlowFunction<Abstraction> getCallFlowFunction(final Unit src, final SootMethod dest)
			{
				if (!dest.isConcrete())
					return KillAll.v();
				final Stmt stmt = (Stmt) src;
				final InvokeExpr ie = (stmt != null && stmt.containsInvokeExpr()) ? stmt.getInvokeExpr() : null;
				/**************************** EXTRACTOCOL *******************************/
				final String className = ie.getMethod().getDeclaringClass().getName();
				if (ignoreFlowsInSystemPackages && SystemClassHandler.isClassInSystemPackage(className))
					return KillAll.v();
				/***********************************************************************/
				final Value[] paramLocals = new Value[dest.getParameterCount()];
				for (int i = 0; i < dest.getParameterCount(); i++)
					paramLocals[i] = dest.getActiveBody().getParameterLocal(i);
				final boolean isSource = sourceSinkManager != null ? sourceSinkManager.getSourceInfo((Stmt) src, interproceduralCFG()) != null : false;
				// final boolean isSink = sourceSinkManager != null
				// ? sourceSinkManager.isSink(stmt,
				// interproceduralCFG(), null) : false;
				// This is not cached by Soot, so accesses are more expensive
				// than one might think
				final Local thisLocal = dest.isStatic() ? null : dest.getActiveBody().getThisLocal();
				// Android executor methods are handled specially.
				// getSubSignature()
				// is slow, so we try to avoid it whenever we can
				final boolean isExecutorExecute = isExecutorExecute(ie, dest);
				return new SolverCallFlowFunction()
				{
					@Override
					public Set<Abstraction> computeTargets(Abstraction d1, Abstraction source)
					{
						Set<Abstraction> res = computeTargetsInternal(d1, source);
						// if (!res.isEmpty())
						// for (Abstraction abs : res)
						// aliasingStratgy.injectCallingContext(abs, solver,
						// dest,
						// src, source, d1);
						return notifyOutFlowHandlers(stmt, d1, source, res, FlowFunctionType.CallFlowFunction);
					}
					public Set<Abstraction> computeTargetsInternal(Abstraction d1, Abstraction source)
					{
						if (DEBUG_CALL)
						{
							System.out.println("\n[*] CallFlow" + "\nsrc = " + src + "\ndest = " + dest + "\nsource = " + source);
						}
						// if (src.toString().contains("<com.contextlogic.wish.http.HttpRequestParams: ch.boye.httpclientandroidlib.HttpEntity toEntity()>"))
						// {
						// System.out.println("toEntity : " + source);
						// }
						if (source == getZeroValue())
							return Collections.emptySet();
						// assert source.isAbstractionActive() ||
						// flowSensitiveAliasing;
						// if we do not have to look into sources or sinks:
						if (!inspectSources && isSource)
							return Collections.emptySet();
						// if (!inspectSinks && isSink)
						// return Collections.emptySet();
						// Do not propagate in inactive taints that will be
						// activated there since they already came out of the
						// callee
						// if (isCallSiteActivatingTaint(stmt,
						// source.getActivationUnit()))
						// return Collections.emptySet();
						// taint is propagated in CallToReturnFunction, so we do
						// not
						// need any taint here if the taint wrapper is
						// exclusive:
						if (taintWrapper != null && taintWrapper.isExclusive(stmt, source))
							return Collections.emptySet();
						// Only propagate the taint if the target field is
						// actually read
						if (enableStaticFields && source.getAccessPath().isStaticFieldRef())
						{
							if (!interproceduralCFG().isStaticFieldRead(dest, source.getAccessPath().getFirstField()))
								return Collections.emptySet();
						}
						Set<Abstraction> res = new HashSet<Abstraction>();
						// if the returned value is tainted - taint values from
						// return statements
						if (src instanceof DefinitionStmt)
						{
							DefinitionStmt defnStmt = (DefinitionStmt) src;
							Value leftOp = defnStmt.getLeftOp();
							if (leftOp == source.getAccessPath().getPlainValue())
							{
								// look for returnStmts:
								for (Unit u : dest.getActiveBody().getUnits())
								{
									if (u instanceof ReturnStmt)
									{
										ReturnStmt rStmt = (ReturnStmt) u;
										if (rStmt.getOp() instanceof Local || rStmt.getOp() instanceof FieldRef)
										{
											Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(new AccessPath(rStmt.getOp(), true).getPlainValue(), null, false),
													(Stmt) src);
											copytaintmethods(abs, source.gettaintmethods(), dest.getSignature());
											res.add(abs);
										}
									}
								}
							}
						}
						// easy: static
						if (enableStaticFields && source.getAccessPath().isStaticFieldRef())
							res.add(source.deriveNewAbstraction(source.getAccessPath(), stmt));
						// checks: this/fields
						Value sourceBase = source.getAccessPath().getPlainValue();
						if (!isExecutorExecute && !source.getAccessPath().isStaticFieldRef() && !dest.isStatic())
						{
							InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) stmt.getInvokeExpr();
							if (iIExpr.getBase() == sourceBase && (hasCompatibleTypesForCall(source.getAccessPath(), dest.getDeclaringClass())))
							{
								boolean param = false;
								// check if it is not one of the params (then we
								// have already fixed it)
								for (int i = 0; i < dest.getParameterCount(); i++)
								{
									if (stmt.getInvokeExpr().getArg(i) == sourceBase)
									{
										param = true;
										break;
									}
								}
								if (!param)
								{
									Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(thisLocal), (Stmt) src);
									copytaintmethods(abs, source.gettaintmethods(), dest.getSignature());
									res.add(abs);
								}
							}
						}
						// Map the parameter values into the callee
						if (isExecutorExecute)
						{
							if (ie.getArg(0) == source.getAccessPath().getPlainValue())
							{
								Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(thisLocal), stmt);
								copytaintmethods(abs, source.gettaintmethods(), dest.getSignature());
								res.add(abs);
							}
						}
						return res;
					}
				};
			}
			@Override
			public FlowFunction<Abstraction> getReturnFlowFunction(final Unit callSite, final SootMethod callee, final Unit exitStmt, final Unit retSite)
			{
				// Get the call site
				if (callSite != null && !(callSite instanceof Stmt))
					return KillAll.v();
				final Value[] paramLocals = new Value[callee.getParameterCount()];
				for (int i = 0; i < callee.getParameterCount(); i++)
					paramLocals[i] = callee.getActiveBody().getParameterLocal(i);
				final Stmt stmt = (Stmt) callSite;
				final InvokeExpr ie = (stmt != null && stmt.containsInvokeExpr()) ? stmt.getInvokeExpr() : null;
				// This is not cached by Soot, so accesses are more expensive
				// than one might think
				final Local thisLocal = callee.isStatic() ? null : callee.getActiveBody().getThisLocal();
				// Android executor methods are handled specially.
				// getSubSignature()
				// is slow, so we try to avoid it whenever we can
				final boolean isExecutorExecute = isExecutorExecute(ie, callee);
				// Callsite
				// $r5 = staticinvoke <com.tophatter.network.NetworkUtils:
				// com.xtreme.network.NetworkResponse
				// a(com.xtreme.network.NetworkRequest)>($r4);
				// returnSite
				// $r4 =
				// $r0.<com.tophatter.network.NetworkUtils$TophatterNetworkRequest:
				// com.xtreme.network.NetworkRequest a>;
				// $r4 -> $r0 (Taint)
				final boolean HeapTaint = callSite != null ? HeapTaint(callSite, retSite) : false;
				return new SolverReturnFlowFunction()
				{
					@Override
					public Set<Abstraction> computeTargets(Abstraction source, Abstraction d1, Collection<Abstraction> callerD1s)
					{
						Set<Abstraction> res = computeTargetsInternal(source, d1, callerD1s);
						return notifyOutFlowHandlers(exitStmt, d1, source, res, FlowFunctionType.ReturnFlowFunction);
					}
					private Set<Abstraction> computeTargetsInternal(Abstraction source, Abstraction d1, Collection<Abstraction> callerD1s)
					{
						// if
						// (callSite.toString().contains("<com.xtreme.network.NetworkRequestLauncher:
						// com.xtreme.network.NetworkResponse
						// a(com.xtreme.network.NetworkRequest)>"))
						// {
						// System.out.println("hi");
						// }
						// if (callee != null && callSite != null)
						// {
						// if (callSite.toString().contains("<rx.Observable:
						// rx.Observable subscribeOn(rx.Scheduler)>")
						// || callee.toString().contains("<rx.Observable:
						// rx.Observable subscribeOn(rx.Scheduler)>"))
						// {
						// System.out.println("asdfasgasdf");
						// }
						// }
						if (DEBUG_RETURN)
						{
							System.out.println("\n[*] ReturnFlow" + "\ncallSite : " + callSite + "\ncallee : " + callee + "\nexitStmt : " + exitStmt + "\nretSite : " + retSite // + "\ncaller :
									+ "\nsource : " + source);
						}
						if (source == getZeroValue())
						{
							return Collections.emptySet();
						}
						// JM Taintrule 1
						else if (d1 == getZeroValue() && Constants.MaxTaintCount > Constants.TaintCount++)
						{
							if (callSite instanceof InvokeStmt)
							{
								InvokeStmt is = (InvokeStmt) callSite;
								if (is.getInvokeExpr() instanceof InstanceInvokeExpr)
								{
									InstanceInvokeExpr iie = (InstanceInvokeExpr) is.getInvokeExpr();
									if (exitStmt instanceof IdentityStmt)
									{
										InstanceInvokeExpr calleeiie = null;
										if (((InvokeStmt) callSite).getInvokeExpr() instanceof InstanceInvokeExpr)
										{
											calleeiie = (InstanceInvokeExpr) ((InvokeStmt) callSite).getInvokeExpr();
										}
										if (calleeiie != null && source.getAccessPath().getPlainValue() != null
												&& source.getAccessPath().getPlainValue().getType().equals(calleeiie.getBase().getType()))
										{
											Set<Abstraction> res = new HashSet<Abstraction>();
											Abstraction abs = source.deriveNewAbstraction(iie.getBase(), true, is, source.getAccessPath().getFirstFieldType());
											// JM appending heuristic callflow
											copytaintmethods(abs, flowsensitive_results.get(flowsensitive_results.size() - 1).getTaintmethods(),
													interproceduralCFG().getMethodOf(callSite).getSignature());
											res.add(abs);
											final Value sourceBase = source.getAccessPath().getPlainValue();
											Value originalCallArg = null;
											for (int i = 0; i < paramLocals.length; i++)
											{
												if (paramLocals[i] == sourceBase)
												{
													if (callSite instanceof Stmt)
													{
														originalCallArg = ie.getArg(i);
														// If this is a constant parameter, we can
														// safely ignore it
														if (!AccessPath.canContainValue(originalCallArg))
															continue;
														if (!checkCast(source.getAccessPath(), originalCallArg.getType()))
															continue;
														abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(originalCallArg), (Stmt) exitStmt);
														// JM appending heuristic callflow
														copytaintmethods(abs, flowsensitive_results.get(flowsensitive_results.size() - 1).getTaintmethods(),
																interproceduralCFG().getMethodOf(callSite).getSignature());
														res.add(abs);
														registerActivationCallSite(callSite, callee, abs);
													}
												}
											}
											if(source.getAccessPath().getFields() != null){
												if(source.getAccessPath().getFields().length > 0){
													Abstraction abs2 = source.deriveNewAbstraction(source.getAccessPath().getPlainValue(), false, (Stmt) exitStmt, source.getAccessPath().getBaseType());
													res.add(abs2);
												}
											}
											return res;
										}
									}
								}
							}
						}
						assert source.isAbstractionActive() || flowSensitiveAliasing;
						// If we have no caller, we have nowhere to propagate.
						// This
						// can happen when leaving the main method.
						if (callSite == null)
							return Collections.emptySet();
						// easy: static
						if (enableStaticFields && source.getAccessPath().isStaticFieldRef())
						{
							registerActivationCallSite(callSite, callee, source);
							return Collections.singleton(source);
						}
						final Value sourceBase = source.getAccessPath().getPlainValue();
						Set<Abstraction> res = new HashSet<Abstraction>();
						if(source.getAccessPath().getFields() != null){
							if(source.getAccessPath().getFields().length > 0){
								Abstraction abs2 = source.deriveNewAbstraction(source.getAccessPath().getPlainValue(), false, (Stmt) exitStmt, source.getAccessPath().getBaseType());
								res.add(abs2);
							}
						}
						// Since we return from the top of the callee into the
						// caller, return values cannot be propagated here. They
						// don't yet exist at the beginning of the callee.
						// if (isExecutorExecute) {
						// Map the "this" object to the first argument of the
						// call site
						if (source.getAccessPath().getPlainValue() == thisLocal && ie.getArgCount() > 0)
						{
							Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(ie.getArg(0)), (Stmt) exitStmt);
							// JM appending heuristic callflow
							if (callSite.toString().contains("java.util.concurrent.ThreadPoolExecutor: java.util.concurrent.Future submit(java.lang.Runnable)"))
							{
								copytaintmethods(abs, flowsensitive_results.get(flowsensitive_results.size() - 1).getTaintmethods(), interproceduralCFG().getMethodOf(callSite).getSignature());
							}
							else
								copytaintmethods(abs, source.gettaintmethods(), interproceduralCFG().getMethodOf(callSite).getSignature());
							res.add(abs);
							registerActivationCallSite(callSite, callee, abs);
						}
						if (HeapTaint)
						{
							AssignStmt as = (AssignStmt) retSite;
							Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(as.getRightOp()), (Stmt) retSite);
							copytaintmethods(abs, source.gettaintmethods(), interproceduralCFG().getMethodOf(callSite).getSignature());
							res.add(abs);
							// registerActivationCallSite(callSite, callee,
							// abs);
						}
						/*
						 * $r6 = staticinvoke <com.tophatter.network.NetworkUtils: com.xtreme.network.NetworkResponse b(com.xtreme.network.NetworkRequest,boolean,java.lang .String)>($r3, 1, $r2) -> virtualinvoke $r3.<com.xtreme.network.NetworkRequest: void a(java.lang.String,java.lang.String)>("bid[amount]", $r1) * $r3 and $r1 will be tainted
						 */
						if (callSite instanceof AssignStmt)
						{
							AssignStmt as = (AssignStmt) callSite;
							if (retSite instanceof InvokeStmt)
							{
								InvokeStmt is = (InvokeStmt) retSite;
								if (is.getInvokeExpr() instanceof InstanceInvokeExpr)
								{
									InstanceInvokeExpr iie = (InstanceInvokeExpr) is.getInvokeExpr();
									if (as.containsInvokeExpr())
									{
										if (as.getInvokeExpr() instanceof StaticInvokeExpr)
										{
											StaticInvokeExpr origin = (StaticInvokeExpr) as.getInvokeExpr();
											for (Value arg : origin.getArgs())
											{
												if (iie.getBase().toString().equals(arg.toString()))
												{
													if (iie.getArgCount() > 0)
													{
														// base arg will be tainted.
														Abstraction baseabs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(iie.getBase()), (Stmt) exitStmt);
														copytaintmethods(baseabs, source.gettaintmethods(), interproceduralCFG().getMethodOf(callSite).getSignature());
														res.add(baseabs);
														for (Value arg2 : iie.getArgs())
														{
															Abstraction argabs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(arg2), (Stmt) exitStmt);
															copytaintmethods(argabs, source.gettaintmethods(), interproceduralCFG().getMethodOf(callSite).getSignature());
															res.add(argabs);
														}
													}
												}
											}
										}
									}
								}
							}
						}
						// }
						// else {
						boolean parameterAliases = false;
						// check one of the call params are tainted (not if
						// simple type)
						Value originalCallArg = null;
						for (int i = 0; i < paramLocals.length; i++)
						{
							if (paramLocals[i] == sourceBase)
							{
								parameterAliases = true;
								if (callSite instanceof Stmt)
								{
									originalCallArg = ie.getArg(i);
									// If this is a constant parameter, we can
									// safely ignore it
									if (!AccessPath.canContainValue(originalCallArg))
										continue;
									if (!checkCast(source.getAccessPath(), originalCallArg.getType()))
										continue;
									/**************************** EXTRACTOCOL *******************************/
									// Primitive types and strings cannot have
									// aliases and thus
									// never need to be propagated back
									// if (source.getAccessPath().getBaseType()
									// instanceof PrimType)
									// continue;
									// if
									// (isStringType(source.getAccessPath().getBaseType()))
									// continue;
									/************************************************************************/
									if (ie instanceof InstanceInvokeExpr)
									{
										InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
										Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(iie.getBase()), (Stmt) exitStmt);
										copytaintmethods(abs, source.gettaintmethods(), interproceduralCFG().getMethodOf(callSite).getSignature());
										res.add(abs);
									}
									Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(originalCallArg), (Stmt) exitStmt);
									copytaintmethods(abs, source.gettaintmethods(), interproceduralCFG().getMethodOf(callSite).getSignature());
									res.add(abs);
									registerActivationCallSite(callSite, callee, abs);
								}
							}
							// }
							// Map the "this" local
							if (!callee.isStatic())
							{
								if (thisLocal == sourceBase && hasCompatibleTypesForCall(source.getAccessPath(), callee.getDeclaringClass()))
								{
									// check if it is not one of the params
									// (then we have already fixed it)
									if (!parameterAliases)
									{
										if (callSite instanceof Stmt)
										{
											Stmt stmt = (Stmt) callSite;
											if (stmt.getInvokeExpr() instanceof InstanceInvokeExpr)
											{
												InstanceInvokeExpr iIExpr = (InstanceInvokeExpr) stmt.getInvokeExpr();
												Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(iIExpr.getBase()), (Stmt) exitStmt);
												copytaintmethods(abs, source.gettaintmethods(), callee.getSignature());
												res.add(abs);
												registerActivationCallSite(callSite, callee, abs);
											}
										}
									}
								}
							}
						}
						for (Abstraction abs : res)
							if (abs != source)
								abs.setCorrespondingCallSite((Stmt) callSite);
						return res;
					}
				};
			}
			private boolean HeapTaint(Unit callSite, Unit retSite)
			{
				// JM Auto-generated method stub
				if (callSite instanceof AssignStmt)
				{
					AssignStmt as = (AssignStmt) callSite;
					if (retSite instanceof AssignStmt)
					{
						AssignStmt as2 = (AssignStmt) retSite;
						if (as.getRightOp() instanceof InvokeExpr)
						{
							InvokeExpr ie = as.getInvokeExpr();
							if (ie.getArgCount() > 0)
								if (as2.getLeftOp().toString().equals(ie.getArg(0).toString()))
									return true;
						}
					}
					else
						return false;
				}
				return false;
			}
			@Override
			public FlowFunction<Abstraction> getCallToReturnFlowFunction(final Unit call, final Unit returnSite)
			{
				final Stmt iStmt = (Stmt) call;
				final InvokeExpr invExpr = iStmt.getInvokeExpr();
				final Value[] callArgs = new Value[iStmt.getInvokeExpr().getArgCount()];
				for (int i = 0; i < iStmt.getInvokeExpr().getArgCount(); i++)
					callArgs[i] = iStmt.getInvokeExpr().getArg(i);
				final SootMethod callee = invExpr.getMethod();
				final DefinitionStmt defStmt = iStmt instanceof DefinitionStmt ? (DefinitionStmt) iStmt : null;
				final SourceInfo sourceInfo = sourceSinkManager != null ? sourceSinkManager.getSourceInfo(iStmt, interproceduralCFG()) : null;
				// System.out.println(call);
				// System.out.println(returnSite);
				return new SolverCallToReturnFlowFunction()
				{
					@Override
					public Set<Abstraction> computeTargets(Abstraction d1, Abstraction source)
					{
						Set<Abstraction> res = computeTargetsInternal(d1, source);
						return notifyOutFlowHandlers(call, d1, source, res, FlowFunctionType.CallToReturnFlowFunction);
					}
					public Set<Abstraction> computeTargetsInternal(Abstraction d1, Abstraction source)
					{
						if (DEBUG_CALLTORETURN)
						{
							System.out.println("\n[*] CallToReturnFlow" + "\ncall : " + call + "\nreturnSite : " + returnSite + "\nsource : " + source);
						}
						Set<Abstraction> res = new HashSet<Abstraction>();
						// if (source != getZeroValue() && source != null)
						// res.add(source.clone());
						// Sources can either be assignments like x =
						// getSecret() or
						// instance method calls like constructor invocations
						if (source == getZeroValue() && sourceInfo != null && !sourceInfo.getAccessPaths().isEmpty())
						{
							AccessPath targetAP = null;
							if (iStmt.getInvokeExpr() instanceof InstanceInvokeExpr)
							{
								// Taint the base object
								Value base = ((InstanceInvokeExpr) iStmt.getInvokeExpr()).getBase();
								targetAP = new AccessPath(base, true);
								final Abstraction abs = new Abstraction(targetAP, iStmt,
										// sourceInfo.getUserData(),
										callee, false, false);
								copytaintmethods(abs, source.gettaintmethods(), interproceduralCFG().getMethodOf(iStmt).getSignature());
								res.add(abs);
								for (Abstraction absRet : res)
									if (absRet != source)
										absRet.setCorrespondingCallSite(iStmt);
							}
							for (Value param : iStmt.getInvokeExpr().getArgs())
							{
								if (!(param instanceof Constant))
								{
									targetAP = new AccessPath(param, true);
									final Abstraction abs = new Abstraction(targetAP, iStmt,
											// sourceInfo.getUserData(),
											callee, false, false);
									copytaintmethods(abs, source.gettaintmethods(), null);
									res.add(abs);
								}
							}
							return res;
						}
						if (iStmt instanceof AssignStmt && iStmt.toString().contains("virtualinvoke $r1.<com.philips.lighting.hue.sdk.b.a.e: java.lang.StringBuffer a()>"))
						{
							if (source.toString().contains(((AssignStmt) iStmt).getLeftOp().toString()) && iStmt.getInvokeExpr() != null)
							{
								final Abstraction abs = source.deriveNewAbstraction(((InstanceInvokeExpr) iStmt.getInvokeExpr()).getBase(), true, defStmt, source.getAccessPath().getFirstFieldType());
								copytaintmethods(abs, source.gettaintmethods(), null);
								res.add(abs);
								return res;
							}
						}
						// JM $r10 = virtualinvoke $r9.<com.contextlogic.wish.http.HttpRequestParams: ch.boye.httpclientandroidlib.HttpEntity toEntity()>()
						// $10 was tainted -> we taint $r9 and local variables in toEntity method.
						else if (iStmt instanceof AssignStmt)
						{
							AssignStmt as = (AssignStmt) iStmt;
							if (as.getLeftOp().equals(source.getAccessPath().getPlainValue()))
							{
								if (as.containsInvokeExpr())
								{
									InvokeExpr ie = (InvokeExpr) as.getInvokeExpr();
									if (ie instanceof InstanceInvokeExpr && ie.getArgCount() == 0)
									{
										InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
										Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(new AccessPath(iie.getBase(), true).getPlainValue(), null, false),
												(Stmt) call);
										copytaintmethods(abs, source.gettaintmethods(), iie.getMethodRef().getSignature());
										res.add(abs);
										if (iie.getMethod().hasActiveBody())
										{
											for (Local loc : iie.getMethod().getActiveBody().getLocals())
											{
												Abstraction localbs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(new AccessPath(loc, true).getPlainValue(), null, false),
														(Stmt) call);
												copytaintmethods(localbs, source.gettaintmethods(), iie.getMethodRef().getSignature());
												res.add(abs);
											}
										}
										return res;
									}
									else if (ie instanceof StaticInvokeExpr)
									{
										//[Extractocol] : this case is not implemented.
									}
								}
							}
						}
						// JM specific taint rule for boye http library's method which makes HTTP header.
						if (call instanceof InvokeStmt)
						{
							InvokeStmt is = (InvokeStmt) call;
							if (is.getInvokeExpr().getMethodRef().getSignature()
									.equals("<ch.boye.httpclientandroidlib.client.methods.HttpPost: void setEntity(ch.boye.httpclientandroidlib.HttpEntity)>"))
							{
								if (is.getInvokeExpr() instanceof InstanceInvokeExpr)
								{
									InstanceInvokeExpr iie = (InstanceInvokeExpr) is.getInvokeExpr();
									AccessPath targetAP = new AccessPath(iie.getBase(), true);
									Abstraction abs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(targetAP.getPlainValue(), null, false), (Stmt) call);
									copytaintmethods(abs, source.gettaintmethods(), iie.getMethodRef().getSignature());
									res.add(abs);
									AccessPath targetAP2 = new AccessPath(iie.getArg(0), true);
									Abstraction abs2 = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(targetAP2.getPlainValue(), null, false), (Stmt) call);
									copytaintmethods(abs2, source.gettaintmethods(), iie.getMethodRef().getSignature());
									res.add(abs2);
									res.add(source.deriveNewAbstraction(source.getAccessPath(), (Stmt) call));
									return res;
								}
							}
						}
						if (iStmt.getInvokeExpr() instanceof InstanceInvokeExpr)
						{
							AccessPath targetAP = new AccessPath(((InstanceInvokeExpr) iStmt.getInvokeExpr()).getBase(), true);
							// For Volley Type
							if (Scene.v().getSootClass(((InstanceInvokeExpr) iStmt.getInvokeExpr()).getBase().getType().toString()) != null)
							{
								SootClass Parentsc = Scene.v().getSootClass(((InstanceInvokeExpr) iStmt.getInvokeExpr()).getBase().getType().toString());
								if (!Parentsc.isInterface())
								{
									List<SootClass> subcls = Scene.v().getActiveHierarchy().getSuperclassesOf(Parentsc);
									for (SootClass sc : subcls)
										if (sc.getName() == "com.android.volley.toolbox.StringRequest")
										{
											final Abstraction abs = source.deriveNewAbstraction(((InstanceInvokeExpr) iStmt.getInvokeExpr()).getBase(), true, iStmt,
													source.getAccessPath().getFirstFieldType());
											copytaintmethods(abs, source.gettaintmethods(), null);
											res.add(abs);
											return res;
										}
								}
							}
						}
						if (iStmt.getInvokeExpr() instanceof StaticInvokeExpr)
						{
							if (call.toString().contains("<rx.Observable: rx.Observable create(rx.Observable$OnSubscribe)>"))
							{
								/*
								 * $r4 = staticinvoke <rx.Observable: rx.Observable create(rx.Observable$OnSubscribe)>($r3) $r5 = staticinvoke <rx.schedulers.Schedulers: rx.Scheduler io()>() $r4 = virtualinvoke $r4.<rx.Observable: rx.Observable subscribeOn(rx.Scheduler)>($r5)
								 */
								AccessPath targetAP = null;
								Value arg = ((StaticInvokeExpr) iStmt.getInvokeExpr()).getArg(0);
								targetAP = new AccessPath(arg, true);
								final Abstraction abs = new Abstraction(targetAP, iStmt,
										// sourceInfo.getUserData(),
										callee, false, false);
								copytaintmethods(abs, source.gettaintmethods(), null);
								res.add(abs);
								System.out.println("[Extractocol : rx taint]" + " " + arg.getType());
								// ((AssignStmt)
								// returnSite).getInvokeExpr().getArg(0)
							}
						}
						// JM Taintrule 2
						else if (iStmt.getInvokeExpr() instanceof SpecialInvokeExpr && source.getAccessPath().getPlainValue() != null)
						{
							SpecialInvokeExpr sie = (SpecialInvokeExpr) iStmt.getInvokeExpr();
							if (source.getAccessPath().getPlainValue().equals(sie.getBase()))
							{
								for (Value local : sie.getArgs())
								{
									if (!(local instanceof NullConstant))
									{
										final Abstraction abs = source.deriveNewAbstraction(local, true, iStmt, source.getAccessPath().getFirstFieldType());
										copytaintmethods(abs, source.gettaintmethods(), sie.getMethodRef().getSignature());
										res.add(abs);
									}
								}
								return res;
							}
						}
						if (source == getZeroValue())
							return Collections.emptySet();
						assert source.isAbstractionActive() || flowSensitiveAliasing;
						/*
						 * ***************************************************** ********************************
						 */
						// Compute the taint wrapper taints
						Collection<Abstraction> wrapperTaints = computeWrapperTaints(d1, iStmt, source);
						if (wrapperTaints != null && !wrapperTaints.isEmpty())
						{
							// res.addAll(wrapperTaints);
							for (Abstraction abs : wrapperTaints)
							{
								LinkedList<String> taintmethods = new LinkedList<String>();
								taintmethods.add(interproceduralCFG().getMethodOf(call).getSignature());
								abs.settaintmethods(taintmethods);
								res.add(abs);
							}
							return res;
						}
						/*
						 * ***************************************************** ********************************
						 */
						/**************************** EXTRACTOCOL *******************************/
						// final String className =
						// callee.getDeclaringClass().getName();
						// if (ignoreFlowsInSystemPackages &&
						// SystemClassHandler.isClassInSystemPackage(className))
						// return Collections.singleton(source);
						/***********************************************************************/
						// If the callee does not read the given value, we also
						// need to pass it on
						// since we do not propagate it into the callee.
						if (enableStaticFields && source.getAccessPath().isStaticFieldRef())
							if (interproceduralCFG().isStaticFieldUsed(callee, source.getAccessPath().getFirstField()))
								return Collections.emptySet();
						// We may not pass on a taint if it is overwritten by
						// this call
						if (iStmt instanceof DefinitionStmt && ((DefinitionStmt) iStmt).getLeftOp() == source.getAccessPath().getPlainValue())
						{
							// if (iStmt.getInvokeExpr() instanceof
							// InstanceInvokeExpr) {
							// InstanceInvokeExpr iiExpr = (InstanceInvokeExpr)
							// iStmt.getInvokeExpr();
							// Abstraction abs = source.deriveNewAbstraction
							// (source.getAccessPath().copyWithNewValue(iiExpr.getBase()),
							// iStmt);
							// res.add(abs);
							// }
							// for (Value param :
							// iStmt.getInvokeExpr().getArgs()) {
							// if (!(param instanceof Constant)) {
							// Abstraction abs = source.deriveNewAbstraction
							// (source.getAccessPath().copyWithNewValue(param),
							// iStmt);
							// res.add(abs);
							// }
							// }
							//
							// return res;
							return Collections.emptySet();
						}
						if (iStmt.getInvokeExpr() instanceof InstanceInvokeExpr)
						{
							InstanceInvokeExpr iinv = (InstanceInvokeExpr) iStmt.getInvokeExpr();
							if (iinv.getBase() == source.getAccessPath().getPlainValue())
							{
								for (Value param : iStmt.getInvokeExpr().getArgs())
								{
									if (!(param instanceof Constant) && source.getCurrentStmt().containsInvokeExpr())
									{
										AccessPath targetAP = new AccessPath(param, true);
										final Abstraction abs = source.deriveNewAbstraction(param, true, source.getCurrentStmt(), source.getAccessPath().getFirstFieldType());
										copytaintmethods(abs, source.gettaintmethods(), null);
										res.add(abs);
									}
								}
								if (res.size() == 0)
									return Collections.singleton(source);
								else
									return res;
							}
						}
						// We do not pass taints on parameters over the
						// call-to-return edge
						for (int i = 0; i < callArgs.length; i++)
							if (callArgs[i] == source.getAccessPath().getPlainValue())
								return Collections.emptySet();
						return Collections.singleton(source);
					}
				};
			}
		};
	}
	/**
	 * Gets the results of the backward slice
	 * 
	 * @return Slice Abstraction
	 */
	public List<AbstractSlice> getResults()
	{
		return ass;
	}
	public List<FlowsensitiveSlice> getBackwardResults()
	{
		return flowsensitive_results;
	}
	/**
	 * Enable FlowFunction debugging
	 * 
	 * @param true
	 *            or false
	 */
	public void setDebuggingFlowFunction(boolean enable)
	{
		if (enable)
		{
			this.DEBUG_CALL = true;
			this.DEBUG_CALLTORETURN = true;
			this.DEBUG_NORMAL = true;
			this.DEBUG_RETURN = true;
		}
	}
	public void copytaintmethods(Abstraction abs, LinkedList<String> src, String thissm)
	{
		LinkedList<String> dst = null;
		if (src != null)
			dst = (LinkedList<String>) src.clone();
		else
			dst = new LinkedList<String>();
		if (thissm != null)
			dst.add(thissm);
		abs.settaintmethods(dst);
		// printtaintmethods(abs.gettaintmethods());
	}
	public void printtaintmethods(LinkedList<String> src)
	{
		System.out.print("taint method set : ");
		for (String tsm : src)
		{
			if (src.indexOf(tsm) == src.size() - 1)
				System.out.print(tsm + "\n");
			else
				System.out.print(tsm + "->");
		}
		System.out.println("");
	}
}
