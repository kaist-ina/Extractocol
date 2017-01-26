/* Soot - a J*va Optimization Framework
 * Copyright (C) 2003 Ondrej Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package soot.jimple.toolkits.callgraph;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import soot.ArrayType;
import soot.Body;
import soot.CallbackCandidateFinder;
import soot.Context;
import soot.EntryPoints;
import soot.FastHierarchy;
import soot.G;
import soot.Kind;
import soot.Local;
import soot.MethodContext;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.PhaseOptions;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Transform;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.AssignStmt;
import soot.jimple.DynamicInvokeExpr;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.spark.pag.PAG;
import soot.jimple.toolkits.reflection.ReflectionTraceInfo;
import soot.options.CGOptions;
import soot.options.Options;
import soot.tagkit.StringTag;
import soot.tagkit.Tag;
import soot.toolkits.graph.BriefBlockGraph;
import soot.util.LargeNumberedMap;
import soot.util.NumberedString;
import soot.util.SmallNumberedMap;
import soot.util.queue.ChunkedQueue;
import soot.util.queue.QueueReader;

/**
 * Models the call graph.
 * 
 * @author Ondrej Lhotak
 */
public final class OnFlyCallGraphBuilder
{

	public class DefaultReflectionModel implements ReflectionModel
	{

		protected CGOptions options = new CGOptions(PhaseOptions.v().getPhaseOptions("cg"));

		protected HashSet<SootMethod> warnedAlready = new HashSet<SootMethod>();

		public void classForName(SootMethod source, Stmt s)
		{
			List<Local> stringConstants = methodToStringConstants.get(source);
			if (stringConstants == null)
				methodToStringConstants.put(source, stringConstants = new ArrayList<Local>());
			InvokeExpr ie = s.getInvokeExpr();
			Value className = ie.getArg(0);
			if (className instanceof StringConstant)
			{
				String cls = ((StringConstant) className).value;
				constantForName(cls, source, s);
			} else
			{
				Local constant = (Local) className;
				if (options.safe_forname())
				{
					for (SootMethod tgt : EntryPoints.v().clinits())
					{
						addEdge(source, s, tgt, Kind.CLINIT);
					}
				} else
				{
					for (SootClass cls : Scene.v().dynamicClasses())
					{
						for (SootMethod clinit : EntryPoints.v().clinitsOf(cls))
						{
							addEdge(source, s, clinit, Kind.CLINIT);
						}
					}
					VirtualCallSite site = new VirtualCallSite(s, source, null, null, Kind.CLINIT);
					List<VirtualCallSite> sites = stringConstToSites.get(constant);
					if (sites == null)
					{
						stringConstToSites.put(constant, sites = new ArrayList<VirtualCallSite>());
						stringConstants.add(constant);
					}
					sites.add(site);
				}
			}
		}

		public void classNewInstance(SootMethod source, Stmt s)
		{
			if (options.safe_newinstance())
			{
				for (SootMethod tgt : EntryPoints.v().inits())
				{
					addEdge(source, s, tgt, Kind.NEWINSTANCE);
				}
			} else
			{
				for (SootClass cls : Scene.v().dynamicClasses())
				{
					SootMethod sm = cls.getMethodUnsafe(sigInit);
					if (sm != null)
					{
						addEdge(source, s, sm, Kind.NEWINSTANCE);
					}
				}

				if (options.verbose())
				{
					G.v().out.println("Warning: Method " + source + " is reachable, and calls Class.newInstance;" + " graph will be incomplete!"
							+ " Use safe-newinstance option for a conservative result.");
				}
			}
		}

		public void contructorNewInstance(SootMethod source, Stmt s)
		{
			if (options.safe_newinstance())
			{
				for (SootMethod tgt : EntryPoints.v().allInits())
				{
					addEdge(source, s, tgt, Kind.NEWINSTANCE);
				}
			} else
			{
				for (SootClass cls : Scene.v().dynamicClasses())
				{
					for (SootMethod m : cls.getMethods())
					{
						if (m.getName().equals("<init>"))
						{
							addEdge(source, s, m, Kind.NEWINSTANCE);
						}
					}
				}
				if (options.verbose())
				{
					G.v().out.println("Warning: Method " + source + " is reachable, and calls Constructor.newInstance;" + " graph will be incomplete!"
							+ " Use safe-newinstance option for a conservative result.");
				}
			}
		}

		public void methodInvoke(SootMethod container, Stmt invokeStmt)
		{
			if (!warnedAlready(container))
			{
				if (options.verbose())
				{
					G.v().out.println("Warning: call to " + "java.lang.reflect.Method: invoke() from " + container + "; graph will be incomplete!");
				}
				markWarned(container);
			}
		}

		private void markWarned(SootMethod m)
		{
			warnedAlready.add(m);
		}

		private boolean warnedAlready(SootMethod m)
		{
			return warnedAlready.contains(m);
		}

	}

	public class TraceBasedReflectionModel implements ReflectionModel
	{

		class Guard
		{

			public Guard(SootMethod container, Stmt stmt, String message)
			{
				this.container = container;
				this.stmt = stmt;
				this.message = message;
			}

			final SootMethod container;

			final Stmt stmt;

			final String message;
		}

		protected Set<Guard> guards;

		protected ReflectionTraceInfo reflectionInfo;

		private boolean registeredTransformation = false;

		private TraceBasedReflectionModel()
		{
			guards = new HashSet<Guard>();

			String logFile = options.reflection_log();
			if (logFile == null)
			{
				throw new InternalError("Trace based refection model enabled but no trace file given!?");
			} else
			{
				reflectionInfo = new ReflectionTraceInfo(logFile);
			}
		}

		/**
		 * Adds an edge to all class initializers of all possible receivers of
		 * Class.forName() calls within source.
		 */
		public void classForName(SootMethod container, Stmt forNameInvokeStmt)
		{
			Set<String> classNames = reflectionInfo.classForNameClassNames(container);
			if (classNames == null || classNames.isEmpty())
			{
				registerGuard(container, forNameInvokeStmt, "Class.forName() call site; Soot did not expect this site to be reached");
			} else
			{
				for (String clsName : classNames)
				{
					constantForName(clsName, container, forNameInvokeStmt);
				}
			}
		}

		/**
		 * Adds an edge to the constructor of the target class from this call to
		 * {@link Class#newInstance()}.
		 */
		public void classNewInstance(SootMethod container, Stmt newInstanceInvokeStmt)
		{
			Set<String> classNames = reflectionInfo.classNewInstanceClassNames(container);
			if (classNames == null || classNames.isEmpty())
			{
				registerGuard(container, newInstanceInvokeStmt, "Class.newInstance() call site; Soot did not expect this site to be reached");
			} else
			{
				for (String clsName : classNames)
				{
					SootClass cls = Scene.v().getSootClass(clsName);
					SootMethod constructor = cls.getMethodUnsafe(sigInit);
					if (constructor != null)
					{
						addEdge(container, newInstanceInvokeStmt, constructor, Kind.REFL_CLASS_NEWINSTANCE);
					}
				}
			}
		}

		/**
		 * Adds a special edge of kind {@link Kind#REFL_CONSTR_NEWINSTANCE} to
		 * all possible target constructors of this call to
		 * {@link Constructor#newInstance(Object...)}. Those kinds of edges are
		 * treated specially in terms of how parameters are assigned, as
		 * parameters to the reflective call are passed into the argument array
		 * of {@link Constructor#newInstance(Object...)}.
		 * 
		 * @see PAG#addCallTarget(Edge)
		 */
		public void contructorNewInstance(SootMethod container, Stmt newInstanceInvokeStmt)
		{
			Set<String> constructorSignatures = reflectionInfo.constructorNewInstanceSignatures(container);
			if (constructorSignatures == null || constructorSignatures.isEmpty())
			{
				registerGuard(container, newInstanceInvokeStmt, "Constructor.newInstance(..) call site; Soot did not expect this site to be reached");
			} else
			{
				for (String constructorSignature : constructorSignatures)
				{
					SootMethod constructor = Scene.v().getMethod(constructorSignature);
					addEdge(container, newInstanceInvokeStmt, constructor, Kind.REFL_CONSTR_NEWINSTANCE);
				}
			}
		}

		/**
		 * Adds a special edge of kind {@link Kind#REFL_INVOKE} to all possible
		 * target methods of this call to
		 * {@link Method#invoke(Object, Object...)}. Those kinds of edges are
		 * treated specially in terms of how parameters are assigned, as
		 * parameters to the reflective call are passed into the argument array
		 * of {@link Method#invoke(Object, Object...)}.
		 * 
		 * @see PAG#addCallTarget(Edge)
		 */
		public void methodInvoke(SootMethod container, Stmt invokeStmt)
		{
			Set<String> methodSignatures = reflectionInfo.methodInvokeSignatures(container);
			if (methodSignatures == null || methodSignatures.isEmpty())
			{
				registerGuard(container, invokeStmt, "Method.invoke(..) call site; Soot did not expect this site to be reached");
			} else
			{
				for (String methodSignature : methodSignatures)
				{
					SootMethod method = Scene.v().getMethod(methodSignature);
					addEdge(container, invokeStmt, method, Kind.REFL_INVOKE);
				}
			}
		}

		private void registerGuard(SootMethod container, Stmt stmt, String string)
		{
			guards.add(new Guard(container, stmt, string));

			if (options.verbose())
			{
				G.v().out.println("Incomplete trace file: Class.forName() is called in method '" + container
						+ "' but trace contains no information about the receiver class of this call.");
				if (options.guards().equals("ignore"))
				{
					G.v().out.println("Guarding strategy is set to 'ignore'. Will ignore this problem.");
				} else if (options.guards().equals("print"))
				{
					G.v().out.println("Guarding strategy is set to 'print'. "
							+ "Program will print a stack trace if this location is reached during execution.");
				} else if (options.guards().equals("throw"))
				{
					G.v().out.println(
							"Guarding strategy is set to 'throw'. Program will throw an " + "Error if this location is reached during execution.");
				} else
				{
					throw new RuntimeException("Invalid value for phase option (guarding): " + options.guards());
				}
			}

			if (!registeredTransformation)
			{
				registeredTransformation = true;
				PackManager.v().getPack("wjap").add(new Transform("wjap.guards", new SceneTransformer()
				{

					@Override
					protected void internalTransform(String phaseName, Map<String, String> options)
					{
						for (Guard g : guards)
						{
							insertGuard(g);
						}
					}
				}));
				PhaseOptions.v().setPhaseOption("wjap.guards", "enabled");
			}
		}

		private void insertGuard(Guard guard)
		{
			if (options.guards().equals("ignore"))
				return;

			SootMethod container = guard.container;
			Stmt insertionPoint = guard.stmt;
			if (!container.hasActiveBody())
			{
				G.v().out.println("WARNING: Tried to insert guard into " + container + " but couldn't because method has no body.");
			} else
			{
				Body body = container.getActiveBody();

				// exc = new Error
				RefType runtimeExceptionType = RefType.v("java.lang.Error");
				NewExpr newExpr = Jimple.v().newNewExpr(runtimeExceptionType);
				LocalGenerator lg = new LocalGenerator(body);
				Local exceptionLocal = lg.generateLocal(runtimeExceptionType);
				AssignStmt assignStmt = Jimple.v().newAssignStmt(exceptionLocal, newExpr);
				body.getUnits().insertBefore(assignStmt, insertionPoint);

				// exc.<init>(message)
				SootMethodRef cref = runtimeExceptionType.getSootClass()
						.getMethod("<init>", Collections.<Type> singletonList(RefType.v("java.lang.String"))).makeRef();
				SpecialInvokeExpr constructorInvokeExpr = Jimple.v().newSpecialInvokeExpr(exceptionLocal, cref, StringConstant.v(guard.message));
				InvokeStmt initStmt = Jimple.v().newInvokeStmt(constructorInvokeExpr);
				body.getUnits().insertAfter(initStmt, assignStmt);

				if (options.guards().equals("print"))
				{
					// exc.printStackTrace();
					VirtualInvokeExpr printStackTraceExpr = Jimple.v().newVirtualInvokeExpr(exceptionLocal,
							Scene.v().getSootClass("java.lang.Throwable").getMethod("printStackTrace", Collections.<Type> emptyList()).makeRef());
					InvokeStmt printStackTraceStmt = Jimple.v().newInvokeStmt(printStackTraceExpr);
					body.getUnits().insertAfter(printStackTraceStmt, initStmt);
				} else if (options.guards().equals("throw"))
				{
					body.getUnits().insertAfter(Jimple.v().newThrowStmt(exceptionLocal), initStmt);
				} else
				{
					throw new RuntimeException("Invalid value for phase option (guarding): " + options.guards());
				}
			}
		}

	}

	/** context-insensitive stuff */
	private final CallGraph cicg = new CallGraph();

	private final HashSet<SootMethod> analyzedMethods = new HashSet<SootMethod>();

	private final LargeNumberedMap<Local, List<VirtualCallSite>> receiverToSites = new LargeNumberedMap<Local, List<VirtualCallSite>>(
			Scene.v().getLocalNumberer()); // Local -> List(VirtualCallSite)

	private final LargeNumberedMap<SootMethod, List<Local>> methodToReceivers = new LargeNumberedMap<SootMethod, List<Local>>(
			Scene.v().getMethodNumberer()); // SootMethod -> List(Local)

	public LargeNumberedMap<SootMethod, List<Local>> methodToReceivers()
	{
		return methodToReceivers;
	}

	private final SmallNumberedMap<List<VirtualCallSite>> stringConstToSites = new SmallNumberedMap<List<VirtualCallSite>>(
			Scene.v().getLocalNumberer()); // Local -> List(VirtualCallSite)

	private final LargeNumberedMap<SootMethod, List<Local>> methodToStringConstants = new LargeNumberedMap<SootMethod, List<Local>>(
			Scene.v().getMethodNumberer()); // SootMethod -> List(Local)

	public LargeNumberedMap<SootMethod, List<Local>> methodToStringConstants()
	{
		return methodToStringConstants;
	}

	private CGOptions options;

	private boolean appOnly;

	/** context-sensitive stuff */
	private ReachableMethods rm;

	private QueueReader<MethodOrMethodContext> worklist;

	private ContextManager cm;

	private final ChunkedQueue<SootMethod> targetsQueue = new ChunkedQueue<SootMethod>();

	private final QueueReader<SootMethod> targets = targetsQueue.reader();

	public OnFlyCallGraphBuilder(ContextManager cm, ReachableMethods rm)
	{
		this.cm = cm;
		this.rm = rm;
		worklist = rm.listener();
		options = new CGOptions(PhaseOptions.v().getPhaseOptions("cg"));
		if (!options.verbose())
		{
			G.v().out.println("[Call Graph] For information on where the call graph may be incomplete, use the verbose option to the cg phase.");
		}

		if (options.reflection_log() == null || options.reflection_log().length() == 0)
		{
			reflectionModel = new DefaultReflectionModel();
		} else
		{
			reflectionModel = new TraceBasedReflectionModel();
		}
	}

	public OnFlyCallGraphBuilder(ContextManager cm, ReachableMethods rm, boolean appOnly)
	{
		this(cm, rm);
		this.appOnly = appOnly;
	}

	public void processReachables()
	{
		while (true)
		{
			if (!worklist.hasNext())
			{
				rm.update();
				if (!worklist.hasNext())
					break;
			}
			MethodOrMethodContext momc = (MethodOrMethodContext) worklist.next();
			SootMethod m = momc.method();
			if (appOnly && !m.getDeclaringClass().isApplicationClass())
				continue;
			if (analyzedMethods.add(m))
				processNewMethod(m);
			processNewMethodContext(momc);
		}
	}

	public boolean wantTypes(Local receiver)
	{
		return receiverToSites.get(receiver) != null;
	}

	public void addType(Local receiver, Context srcContext, Type type, Context typeContext)
	{
		FastHierarchy fh = Scene.v().getOrMakeFastHierarchy();
		for (Iterator<VirtualCallSite> siteIt = receiverToSites.get(receiver).iterator(); siteIt.hasNext();)
		{
			final VirtualCallSite site = siteIt.next();
			if (site.kind() == Kind.THREAD && !fh.canStoreType(type, clRunnable))
				continue;
			if (site.kind() == Kind.EXECUTOR && !fh.canStoreType(type, clRunnable))
				continue;
			if (site.kind() == Kind.ASYNCTASK && !fh.canStoreType(type, clAsyncTask))
				continue;

			if (site.iie() instanceof SpecialInvokeExpr && site.kind != Kind.THREAD && site.kind != Kind.EXECUTOR && site.kind != Kind.ASYNCTASK)
			{
				SootMethod target = VirtualCalls.v().resolveSpecial((SpecialInvokeExpr) site.iie(), site.subSig(), site.container());
				// if the call target resides in a phantom class then "target"
				// will be null;
				// simply do not add the target in that case
				if (target != null)
				{
					targetsQueue.add(target);
				}
			} else
			{
				VirtualCalls.v().resolve(type, receiver.getType(), site.subSig(), site.container(), targetsQueue);
			}
			while (targets.hasNext())
			{
				SootMethod target = (SootMethod) targets.next();
				cm.addVirtualEdge(MethodContext.v(site.container(), srcContext), site.stmt(), target, site.kind(), typeContext);
			}
		}
	}

	public boolean wantStringConstants(Local stringConst)
	{
		return stringConstToSites.get(stringConst) != null;
	}

	public void addStringConstant(Local l, Context srcContext, String constant)
	{
		for (Iterator<VirtualCallSite> siteIt = (stringConstToSites.get(l)).iterator(); siteIt.hasNext();)
		{
			final VirtualCallSite site = siteIt.next();
			if (constant == null)
			{
				if (options.verbose())
				{
					G.v().out.println("Warning: Method " + site.container() + " is reachable, and calls Class.forName on a"
							+ " non-constant String; graph will be incomplete!" + " Use safe-forname option for a conservative result.");
				}
			} else
			{
				if (constant.length() > 0 && constant.charAt(0) == '[')
				{
					if (constant.length() > 1 && constant.charAt(1) == 'L' && constant.charAt(constant.length() - 1) == ';')
					{
						constant = constant.substring(2, constant.length() - 1);
					} else
						continue;
				}
				if (!Scene.v().containsClass(constant))
				{
					if (options.verbose())
					{
						G.v().out.println("Warning: Class " + constant + " is" + " a dynamic class, and you did not specify"
								+ " it as such; graph will be incomplete!");
					}
				} else
				{
					SootClass sootcls = Scene.v().getSootClass(constant);
					if (!sootcls.isApplicationClass())
					{
						sootcls.setLibraryClass();
					}
					for (SootMethod clinit : EntryPoints.v().clinitsOf(sootcls))
					{
						cm.addStaticEdge(MethodContext.v(site.container(), srcContext), site.stmt(), clinit, Kind.CLINIT);
					}
				}
			}
		}
	}

	/* End of public methods. */

	private void addVirtualCallSite(Stmt s, SootMethod m, Local receiver, InstanceInvokeExpr iie, NumberedString subSig, Kind kind)
	{
		List<VirtualCallSite> sites = (List<VirtualCallSite>) receiverToSites.get(receiver);
		if (sites == null)
		{
			receiverToSites.put(receiver, sites = new ArrayList<VirtualCallSite>());
			List<Local> receivers = (List<Local>) methodToReceivers.get(m);
			if (receivers == null)
				methodToReceivers.put(m, receivers = new ArrayList<Local>());
			receivers.add(receiver);
		}
		sites.add(new VirtualCallSite(s, m, iie, subSig, kind));
	}

	private void processNewMethod(SootMethod m)
	{
		if (m.isNative() || m.isPhantom())
		{
			return;
		}
		if (m.hasActiveBody())
		{
			Body b = m.retrieveActiveBody();
			getImplicitTargets(m);
			findReceivers(m, b);

			// added by zemisolsol
			// findAdditionalReceivers(m, b);
		}
	}

	public void processDPCandidates()
	{
		// JM start ICFG Module
		if (!AddReachableMethods.isCalled)
		{
			System.out.println("connect callback");
			ConnectCallbackForEachFunctions();
			ConnectCallbackForEachInterfaceInvoke();
			ConnectMissingInvoke();
			AddReachableMethods.isCalled = true;
		}
	}

	private void ConnectMissingInvoke()
	{
		for (SootClass sc : Scene.v().getClasses())
		{
			for (SootMethod sm : sc.getMethods())
			{
				if (sm.hasActiveBody())
					for (Unit ut : sm.getActiveBody().getUnits())
					{
						if (ut instanceof InvokeStmt)
						{
							InvokeStmt is = (InvokeStmt) ut;

							if (is.getInvokeExpr() instanceof StaticInvokeExpr || is.getInvokeExpr() instanceof VirtualInvokeExpr)
							{
								InvokeExpr sie = (InvokeExpr) is.getInvokeExpr();

								if (Scene.v().getMethod(sie.getMethod().getSignature()) != null)
								{

									Edge nEdge = new Edge(sm, ut, Scene.v().getMethod(sie.getMethod().getSignature()), Kind.STATIC);
									if (!cicg.edges.contains(nEdge))
										cicg.addEdge(nEdge);

								}
							} else if (is.getInvokeExpr() instanceof InterfaceInvokeExpr)
							{
								InterfaceInvokeExpr sie = (InterfaceInvokeExpr) is.getInvokeExpr();

								if (sie.getMethodRef().getSignature()
										.equals("<java.util.concurrent.ExecutorService: void execute(java.lang.Runnable)>"))
								{
									SootClass includerunClass = Scene.v().getSootClass(sie.getArg(0).getType().toString());
									String runFuncSig = "<" + sie.getArg(0).getType().toString() + ": void run()>";
									try
									{
										for (SootMethod runsm : includerunClass.getMethods())
										{
											if (runsm.getSignature().equals(runFuncSig))
											{
												Edge nEdge = new Edge(sm, ut, runsm, Kind.INTERFACE);
												cicg.addEdge(nEdge);
												Scene.v().setCallGraph(cicg);
												// System.out.println("Connect
												// Future submit");
											}
										}
									} catch (RuntimeException e)
									{
										// JM this is occurred when target
										// method is not exist in the class.
										continue;
									}
								}
							}
						} else if (ut instanceof AssignStmt)
						{
							AssignStmt as = (AssignStmt) ut;
							if (as.containsInvokeExpr())
							{
								if (as.getInvokeExpr() instanceof StaticInvokeExpr || as.getInvokeExpr() instanceof VirtualInvokeExpr)
								{
									InvokeExpr sie = (InvokeExpr) as.getInvokeExpr();

									// JM connecting
									// <java.util.concurrent.ThreadPoolExecutor:
									// java.util.concurrent.Future
									// submit(java.lang.Runnable)> edges.
									if (sie.getMethodRef().getSignature().equals(
											"<java.util.concurrent.ThreadPoolExecutor: java.util.concurrent.Future submit(java.lang.Runnable)>"))
									{
										SootClass includerunClass = Scene.v().getSootClass(sie.getArg(0).getType().toString());
										String runFuncSig = "<" + sie.getArg(0).getType().toString() + ": void run()>";
										try
										{
											for (SootMethod runsm : includerunClass.getMethods())
											{
												if (runsm.getSignature().equals(runFuncSig))
												{
													Edge nEdge = new Edge(sm, ut, runsm, Kind.VIRTUAL);
													cicg.addEdge(nEdge);
													Scene.v().setCallGraph(cicg);
													System.out.println("Connect Future submit");
												}
											}
										} catch (RuntimeException e)
										{
											// JM this is occurred when target
											// method is not exist in the class.
											continue;
										}
									}

									else if (sie.getMethod().getSignature().equals("<rx.Observable: rx.Observable subscribeOn(rx.Scheduler)>"))
									{

										/*
										 * $r4 = staticinvoke <rx.Observable:
										 * rx.Observable
										 * create(rx.Observable$OnSubscribe)>(
										 * $r3); $r5 = staticinvoke
										 * <rx.schedulers.Schedulers:
										 * rx.Scheduler io()>(); $r4 =
										 * virtualinvoke $r4.<rx.Observable:
										 * rx.Observable
										 * subscribeOn(rx.Scheduler)>($r5);
										 */

										VirtualInvokeExpr vie = (VirtualInvokeExpr) sie;
										
										if (vie.getBase().getType().Extype == null)
											return;

										if (!vie.getBase().getType().Extype.isEmpty())
										{
											System.out.println("[Extractocol rx connecting edge]");
											String TargetSig = "<" + vie.getBase().getType().Extype + ": void call(java.lang.Object)>";
											SootMethod rxCall = Scene.v().getMethod(TargetSig);
											if (rxCall != null)
											{
												Edge nEdge = new Edge(sm, ut, rxCall, Kind.VIRTUAL);
												cicg.addEdge(nEdge);
												Scene.v().setCallGraph(cicg);
											}
										}
									} else if (sie.getMethod().getSignature()
											.equals("<rx.Observable: rx.Observable create(rx.Observable$OnSubscribe)>"))
									{

										/*
										 * $r4 = staticinvoke <rx.Observable:
										 * rx.Observable
										 * create(rx.Observable$OnSubscribe)>(
										 * $r3); $r5 = staticinvoke
										 * <rx.schedulers.Schedulers:
										 * rx.Scheduler io()>(); $r4 =
										 * virtualinvoke $r4.<rx.Observable:
										 * rx.Observable
										 * subscribeOn(rx.Scheduler)>($r5);
										 */
										as.getLeftOp().getType().Extype = sie.getArg(0).getType().toString();
										// = sie.getArg(0).getType().toString();
									}

									else if (Scene.v().getMethod(sie.getMethod().getSignature()) != null && !sie.getMethod().isAbstract())
									{
										Edge nEdge = new Edge(sm, ut, Scene.v().getMethod(sie.getMethod().getSignature()), Kind.STATIC);
										if (!cicg.edges.contains(nEdge))
											cicg.addEdge(nEdge);
									}
									// JM When a object contains of virtual
									// invoke is extended by other object, we
									// should find right call edge!!!
									else if (Scene.v().getMethod(sie.getMethod().getSignature()) != null && sie.getMethod().isAbstract())
									{
										SootClass deClass = sie.getMethod().getDeclaringClass();

										if (!deClass.isInterface())
										{
											List<SootClass> subClasses = Scene.v().getActiveHierarchy().getSubclassesOf(deClass);
											for (SootClass sc2 : subClasses)
											{
												for (SootMethod src : sc2.getMethods())
												{
													SootMethod trg = sie.getMethod();

													if (src.getName().equals(trg.getName()) && src.getParameterCount() == trg.getParameterCount())
													{
														Edge nEdge = new Edge(sm, ut, src, Kind.INTERFACE);
														if (!cicg.edges.contains(nEdge))
															cicg.addEdge(nEdge);
													}
												}
											}
										} else
										{
											List<SootClass> subClasses = Scene.v().getActiveHierarchy().getImplementersOf(deClass);
											for (SootClass sc2 : subClasses)
											{
												for (SootMethod src : sc2.getMethods())
												{
													SootMethod trg = sie.getMethod();
													if (src.getName().equals(trg.getName()) && src.getParameterCount() == trg.getParameterCount())
													{
														Edge nEdge = new Edge(sm, ut, src, Kind.INTERFACE);
														if (!cicg.edges.contains(nEdge))
															cicg.addEdge(nEdge);
													}
												}
											}
										}
									}
								}
							}
						}
					}
			}
		}
		Scene.v().setCallGraph(cicg);
	}

	private void ConnectCallbackForEachInterfaceInvoke()
	{
		CallGraph cg = Scene.v().getCallGraph();
		for (SootClass sc : Scene.v().getClasses())
		{
			// JM This's condition for some interfaceinvoke in specific classes.
			// sc.getName().equals("com.mobilemotion.dubsmash.services.impls.UserProviderImpl")
			// if (1)
			{
				for (SootMethod sm : sc.getMethods())
				{
					if (sm.hasActiveBody())
						for (Unit ut : sm.getActiveBody().getUnits())
						{
							if (ut instanceof AssignStmt)
							{
								AssignStmt as = (AssignStmt) ut;

								if (as.containsInvokeExpr())
								{
									if (as.getInvokeExpr() instanceof InterfaceInvokeExpr)
									{
										InterfaceInvokeExpr iie = (InterfaceInvokeExpr) as.getInvokeExpr();
										ConnectInterfaceExprEdge(iie, sm, ut);
									}
								}
							} else if (ut instanceof InvokeStmt)
							{
								InvokeStmt is = (InvokeStmt) ut;
								if (is.getInvokeExpr() instanceof InterfaceInvokeExpr)
								{
									InterfaceInvokeExpr iie = (InterfaceInvokeExpr) is.getInvokeExpr();
									ConnectInterfaceExprEdge(iie, sm, ut);
								}
							}
						}
				}
			}
		}
	}

	private void ConnectInterfaceExprEdge(InterfaceInvokeExpr Iie, SootMethod sm, Unit ut)
	{
		String baseClassname = Iie.getMethod().getDeclaringClass().toString();
		SootClass BaseClass = Scene.v().getSootClass(baseClassname);

		if (BaseClass.isInterface() && !isClassInSystemPackage(Iie.getMethod().getDeclaringClass().toString()))
		{
			List<SootClass> subclasses = null;

			try
			{
				subclasses = Scene.v().getActiveHierarchy().getImplementersOf(BaseClass);
			} catch (Exception e)
			{
				// FIXIT this is temporarily.
				return;
			}

			for (SootClass subsc : subclasses)
			{
				for (SootMethod sm2 : subsc.getMethods())
				{
					if (sm2.getSubSignature().equals(Iie.getMethod().getSubSignature()))
					{
						Edge nEdge = new Edge(sm, ut, sm2, Kind.INTERFACE);
						cicg.addEdge(nEdge);
						Scene.v().setCallGraph(cicg);
					}
				}
			}
		}

	}

	private void ConnectCallbackForEachFunctions()
	{
		CallGraph cg = Scene.v().getCallGraph();
		List<SootMethod> Results = new ArrayList<SootMethod>();

		for (SootClass sc : Scene.v().getClasses())
		{
			/*
			 * JM the condition for App specific Dubsmash:sc.getName().equals(
			 * "com.mobilemotion.dubsmash.services.UserProvider") iheartradio :
			 * sc.getName().equals(
			 * "com.clearchannel.iheartradio.http.adapter.IHttpExecutor")
			 * wikipedia : org.wikipedia.ApiTask
			 */
			
			if (!isClassInSystemPackage(sc.getName()) && (sc.getName().equals("com.contextlogic.wish.http.HttpResponseHandler") || (sc.getName().equals("com.contextlogic.wish.http.JsonHttpResponseHandler"))))
			{
				List<SootClass> subScs = new ArrayList<SootClass>();
				Unit targetInvokeUt = null;
				SootMethod targetSm = null;
				
				for (SootMethod sm : sc.getMethods())
				{
					if (sm.isAbstract())
					{
						targetSm = sm;
						Kind thisKind = null;
						if (!sc.isInterface())
						{
							subScs = Scene.v().getActiveHierarchy().getSubclassesOf(sc);
							thisKind = Kind.VIRTUAL;
						} else
						{
							subScs = Scene.v().getActiveHierarchy().getImplementersOf(sc);
							thisKind = Kind.INTERFACE;
						}

						if (subScs.size() > 0)
						{
							for (SootClass sc2 : subScs)
							{
								for (SootMethod sm2 : sc2.getMethods())
								{
									if (sm2.getSubSignature().equals(targetSm.getSubSignature()))
									{
										// Results will be removed.
										Results.add(sm2);
										List<Unit> targetInvokeStmt = CallbackCandidateFinder.SearchAllInvokeStmt(targetSm.getSignature());
										if (targetInvokeStmt.size() == 0)
											continue;
										targetInvokeUt = targetInvokeStmt.get(0);
										// targetInvokeStmt.get(0) will be
										// changed.

										Scene.v().getMethod(sm2.getSignature());

										Edge nEdge = new Edge(targetSm, targetInvokeStmt.get(0), sm2, thisKind);
										cicg.addEdge(nEdge);

										if (sm2.hasActiveBody())
										{
											InnerEdgeConnector(sm2);
											AddReachableMethods.AddReachable.add(sm2);
										}
										// findReceivers(sm2,
										// sm2.getActiveBody());
									}
								}
							}
						}
					}
					else
					{
						Kind thisKind = null;
						subScs = Scene.v().getActiveHierarchy().getSubclassesOf(sc);
						thisKind = Kind.VIRTUAL;
						targetSm = sm;
						if (subScs.size() > 0)
						{
							for (SootClass sc2 : subScs)
							{
								for (SootMethod sm2 : sc2.getMethods())
								{
									if (sm2.getSubSignature().equals(targetSm.getSubSignature()))
									{
										// Results will be removed.
										Results.add(sm2);
										List<Unit> targetInvokeStmt = CallbackCandidateFinder.SearchAllInvokeStmt(targetSm.getSignature());
										if (targetInvokeStmt.size() == 0)
											continue;
										targetInvokeUt = targetInvokeStmt.get(0);
										// targetInvokeStmt.get(0) will be
										// changed.

										Scene.v().getMethod(sm2.getSignature());

										Edge nEdge = new Edge(targetSm, targetInvokeStmt.get(0), sm2, thisKind);
										cicg.addEdge(nEdge);

										if (sm2.hasActiveBody())
										{
											InnerEdgeConnector(sm2);
											AddReachableMethods.AddReachable.add(sm2);
										}
										// findReceivers(sm2,
										// sm2.getActiveBody());
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void ConnectVirtualEdge(SootMethod srcsm, SootMethod tgtsm, Local base, Body b, Unit point, boolean isBefore)
	{
		InvokeStmt IvkUnit = Jimple.v()
				.newInvokeStmt(Jimple.v().newVirtualInvokeExpr(base, tgtsm.makeRef(), CreateLocalBase(tgtsm.getParameterType(0), b)));
		BriefBlockGraph Bg = new BriefBlockGraph(b);
		if (!isBefore)
			Bg.getBlocks().get(0).insertAfter(IvkUnit, point);
		else
			Bg.getBlocks().get(0).insertBefore(IvkUnit, point);

		cm.addVirtualEdge(srcsm, IvkUnit, tgtsm, Kind.VOLLEY_RESPONSE, null);
		addEdge(srcsm, IvkUnit, tgtsm);
		srcsm.setActiveBody(Bg.getBody());
	}

	private Local CreateLocalBase(Type type, Body b)
	{
		Local base;
		LocalGenerator lg = new LocalGenerator(b);
		base = lg.generateLocal(type);
		return base;
	}

	private Local MatchArg(String arg, Body b)
	{
		for (Local lc : b.getLocals())
			if (lc.toString().equals(arg))
				return lc;
		return null;
	}

	private void ConnectVolleyAdd(SootMethod src, Body b, Unit u)
	{
		System.out.println("Volley Add");
		SootClass MyClass = src.getDeclaringClass();
		InvokeStmt vis = (InvokeStmt) u;
		VirtualInvokeExpr vie = (VirtualInvokeExpr) vis.getInvokeExpr();

		for (SootMethod insm : MyClass.getMethods())
		{
			if (insm.getSubSignature().equals("com.android.volley.Response parseNetworkResponse(com.android.volley.NetworkResponse)"))
			{
				SootMethod tgtsm = insm;
				ConnectVirtualEdge(src, tgtsm, MatchArg(vie.getArg(0).toString(), b), b, u, false);
				return;
			}
		}

		// Searching parseNetworkResponse function in subclasses of this Method.
		SootClass ClassofArg = Scene.v().getSootClass(vie.getArg(0).getType().toString());
		List<SootClass> subClasses = Scene.v().getActiveHierarchy().getSubclassesOfIncluding(ClassofArg);

		for (SootClass subsc : subClasses)
		{
			for (SootMethod insm : subsc.getMethods())
			{
				if (insm.getSubSignature().equals("com.android.volley.Response parseNetworkResponse(com.android.volley.NetworkResponse)"))
				{
					SootMethod tgtsm = insm;
					ConnectVirtualEdge(src, tgtsm, MatchArg(vie.getArg(0).toString(), b), b, u, false);
					return;
				}
			}
		}
	}

	private void findReceivers(SootMethod m, Body b)
	{
		for (final Unit u : b.getUnits())
		{
			final Stmt s = (Stmt) u;
			if (s.containsInvokeExpr())
			{
				InvokeExpr ie = s.getInvokeExpr();

				if (ie instanceof InstanceInvokeExpr)
				{
					// added by zemisolsol. from here
					SootMethod tgt1 = ie.getMethod();
					if (tgt1 != null && !isClassInSystemPackage(tgt1.getDeclaringClass().toString())
							&& !Scene.v().getCallGraph().edgesOutOf(u).hasNext() && tgt1.hasActiveBody())
						addEdge(m, s, tgt1);

					InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
					Local receiver = (Local) iie.getBase();
					NumberedString subSig = iie.getMethodRef().getSubSignature();
					addVirtualCallSite(s, m, receiver, iie, subSig, Edge.ieToKind(iie));
					if (subSig == sigStart)
					{
						addVirtualCallSite(s, m, receiver, iie, sigRun, Kind.THREAD);
					} else if (subSig == sigExecutorExecute || subSig == sigExecutorSubmit || subSig == sigHandlerPost
							|| subSig == sigHandlerPostAtFrontOfQueue || subSig == sigHandlerPostAtTime || subSig == sigHandlerPostAtTimeWithToken
							|| subSig == sigHandlerPostDelayed)
					{
						if (iie.getArgCount() > 0)
						{
							Value runnable = iie.getArg(0);
							if (runnable instanceof Local)
							{
								// added by zemisolsol. from here
								if (runnable.getType().toString().equals("java.util.concurrent.FutureTask"))
								{
									System.out.println("[EXTRACTOCOL] Processing.. " + runnable.getType().toString());
									futureTask(s, m, runnable, iie, Kind.EXECUTOR);
								} else
								{
									// to here
									addVirtualCallSite(s, m, (Local) runnable, iie, sigRun, Kind.EXECUTOR);
								}
							}
						}
					} else if (subSig == sigExecute)
					{
						addVirtualCallSite(s, m, receiver, iie, sigDoInBackground, Kind.ASYNCTASK);
					}
				} else if (ie instanceof DynamicInvokeExpr)
				{
					System.out.println("Virtual invoke : " + ie.toString());

					if (options.verbose())
						G.v().out.println("WARNING: InvokeDynamic to " + ie + " not resolved during call-graph construction.");
				} else
				{
					SootMethod tgt = ie.getMethod();
					if (tgt != null)
					{
						addEdge(m, s, tgt);
						String signature = tgt.getSignature();
						if (signature.equals("<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedAction)>")
								|| signature
										.equals("<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedExceptionAction)>")
								|| signature
										.equals("<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedAction,java.security.AccessControlContext)>")
								|| signature.equals(
										"<java.security.AccessController: java.lang.Object doPrivileged(java.security.PrivilegedExceptionAction,java.security.AccessControlContext)>"))
						{

							Local receiver = (Local) ie.getArg(0);
							addVirtualCallSite(s, m, receiver, null, sigObjRun, Kind.PRIVILEGED);
						}
					} else
					{
						if (!Options.v().ignore_resolution_errors())
						{
							throw new InternalError("Unresolved target " + ie.getMethod() + ". Resolution error should have occured earlier.");
						}
					}
				}
			}
		}
	}

	public SootClass HasClass(String ClassName)
	{
		SootClass calleeClass = null;
		if (!isClassInSystemPackage(ClassName))
		{
			for (SootClass cc : Scene.v().getClasses())
			{
				if (cc.toString().equals(ClassName))
				{
					calleeClass = cc;
				}
			}
		}
		return calleeClass;
	}

	public void InnerEdgeConnector(SootMethod m)/*-?|first_review|Administrator|c3|*/
	{
		Queue<SootMethod> smQueue = new LinkedList<SootMethod>();
		smQueue.add(m);
		// JM connecting all of missing edge in this loop.
		while (!smQueue.isEmpty())
		{
			for (final Unit u : smQueue.poll().getActiveBody().getUnits())
			{
				final Stmt s = (Stmt) u;
				if (s.containsInvokeExpr() && !cicg.edgesOutOf(u).hasNext())
				{
					InvokeExpr ie = s.getInvokeExpr();
					if (ie instanceof VirtualInvokeExpr)
					{
						InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
						Edge nEdge = new Edge(m, u, iie.getMethod(), Kind.VIRTUAL);
						cicg.addEdge(nEdge);

						if (iie.getMethod().hasActiveBody())
							smQueue.add(iie.getMethod());

					} else if (ie instanceof InterfaceInvokeExpr)
					{
						InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
						SootClass MyClass = HasClass(iie.getBase().getType().toString());
						if (MyClass != null && MyClass.isInterface())
						{
							List<SootClass> subClasses = null;
							try
							{
								subClasses = Scene.v().getActiveHierarchy().getImplementersOf(MyClass);
							} catch (Exception e)
							{
								continue;
							}

							for (SootClass subsc : subClasses)
							{
								for (SootMethod MethodOfSubClass : subsc.getMethods())
								{
									if (iie.getMethod().getSubSignature().equals(MethodOfSubClass.getSubSignature())
											&& MethodOfSubClass.hasActiveBody())
									{
										Edge nEdge = new Edge(m, u, MethodOfSubClass, Kind.INTERFACE);
										cicg.addEdge(nEdge);
										if (MethodOfSubClass.hasActiveBody())
											smQueue.add(MethodOfSubClass);
									}
								}
							}
						}
					} else if (ie instanceof SpecialInvokeExpr)
					{
						InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
						Edge nEdge = new Edge(m, u, iie.getMethod(), Kind.VIRTUAL);
						cicg.addEdge(nEdge);

						// JM Set ActiveCallgraph
						AddReachableMethods.cg = cicg;

						if (iie.getMethod().hasActiveBody())
							smQueue.add(iie.getMethod());
					}
				} else if (s instanceof InvokeStmt)
				{
					InvokeStmt is = (InvokeStmt) s;
					// System.out.println(is);
				}
			}
		}
	}/*-|first_review|Administrator|c3|?*/

	// added by zemisolsol
	public void findAdditionalReceivers(SootMethod m, Body b)
	{
		for (final Unit u : b.getUnits())
		{
			final Stmt s = (Stmt) u;
			if (s.containsInvokeExpr() && !Scene.v().getCallGraph().edgesOutOf(u).hasNext())
			{
				InvokeExpr ie = s.getInvokeExpr();

				if ((ie instanceof InstanceInvokeExpr) && (ie instanceof VirtualInvokeExpr))
				{
					InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
					Value base = iie.getBase();
					String callee = iie.getMethod().getName();
					int callerCount = iie.getArgCount();

					boolean hasClass = false;
					SootClass calleeClass = null;
					if (!isClassInSystemPackage(base.getType().toString()))
					{
						for (SootClass cc : Scene.v().getClasses())
						{
							if (cc.toString().equals(base.getType().toString()))
							{
								hasClass = true;
								calleeClass = cc;
							}
						}
					}

					if (hasClass && !calleeClass.isInterface())
					{
						List<SootClass> subClasses = Scene.v().getActiveHierarchy()
								.getSubclassesOf(Scene.v().getSootClass(base.getType().toString()));
						for (SootClass sc2 : subClasses)
						{
							for (SootMethod sm2 : sc2.getMethods())
							{
								if (sm2.getName().equals(callee) && callerCount == sm2.getParameterCount()
										&& m.getReturnType().equals(sm2.getReturnType()))
								{
									boolean hasType = true;
									for (int i = 0; i < callerCount; i++)
									{
										if (!iie.getArg(i).getType().equals(sm2.getParameterType(i)))
										{
											hasType = false;
										}
									}

									if (hasType)
									{
										// if
										// (sm2.getName().equals("doInBackground")
										// ||
										// sm2.getName().equals("performTask"))
										// {
										// System.out.println("[EXTRACTOCOL]
										// Processing.. " + sm2.getSignature());
										// System.out.println("\nFOUND!!");
										// System.out.println("caller: "+m);
										// System.out.println("caller unit:
										// "+u);
										// System.out.println("callee: "+sm2);
										if (sm2.hasActiveBody())
										{
											NumberedString addHandler = Scene.v().getSubSigNumberer().findOrAdd(sm2.getSubSignature());
											addEdge(m, s, sm2);
											addVirtualCallSite(s, m, (Local) base, iie, addHandler, Edge.ieToKind(ie));
											// InnerEdgeConnector(sm2,
											// sm2.getActiveBody());
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	// added by zemisolsol
	public static boolean isClassInSystemPackage(String className)
	{
		return className.startsWith("android.") || className.startsWith("java.") || className.startsWith("sun.")
				|| className.startsWith("org.codehaus.jackson.") || className.startsWith("org.jsoup.") || className.startsWith("com.google.gson.");
	}

	// added by zemisolsol
	public void futureTask(Stmt s, SootMethod m, Value param, InstanceInvokeExpr iie, Kind kind)
	{
		// From here
		List<SootClass> subClasses = Scene.v().getActiveHierarchy().getSubclassesOf(Scene.v().getSootClass(param.getType().toString()));
		for (SootClass sc : subClasses)
		{
			for (SootMethod sm : sc.getMethods())
			{
				if (sm.hasActiveBody())
				{
					for (Unit unit : sm.getActiveBody().getUnits())
					{
						if (unit.toString().contains("<java.util.concurrent.FutureTask: void <init>(java.util.concurrent.Callable)>"))
						{
							Stmt stmt = (Stmt) unit;
							Value para = stmt.getInvokeExpr().getArg(0);
							if (stmt.getInvokeExpr().getArgs().contains(para))
							{
								for (SootClass sc2 : Scene.v().getClasses())
								{
									for (SootMethod sm2 : sc2.getMethods())
									{
										if (sm2.hasActiveBody())
										{
											for (Unit unit2 : sm2.getActiveBody().getUnits())
											{
												Stmt stmt2 = (Stmt) unit2;
												if (stmt2.containsInvokeExpr())
												{
													if (stmt2.getInvokeExpr().getMethod().equals(sm))
													{
														List<Value> params = stmt2.getInvokeExpr().getArgs();
														for (Value para2 : params)
														{
															SootClass sc3 = Scene.v().getSootClass(para2.getType().toString());
															try
															{
																List<SootClass> subClasses2 = Scene.v().getActiveHierarchy().getSubclassesOf(sc3);
																for (SootClass sc4 : subClasses2)
																{
																	for (SootMethod sm3 : sc4.getMethods())
																	{
																		if (sm3.getName().equals("call"))
																		{
																			// System.out.println("FOUND");
																			// System.out.println("\tClass:
																			// "+sc4);
																			// System.out.println("\tMethod:
																			// "+sm3);
																			addEdge(m, s, sm3, Kind.EXECUTOR);
																			addVirtualCallSite(s, m, (Local) param, iie, sigCall, Kind.EXECUTOR);
																		}
																	}
																}
															} catch (Exception e)
															{
																;
															}
														}
													}
												}
											}
										}
									}
								}
							} else
							{
								// addVirtualCallsite()
							}
						}
					}
				}
			}
		}
		// To here
	}

	ReflectionModel reflectionModel;

	private void getImplicitTargets(SootMethod source)
	{
		final SootClass scl = source.getDeclaringClass();
		if (source.isNative() || source.isPhantom())
			return;
		if (source.getSubSignature().indexOf("<init>") >= 0)
		{
			handleInit(source, scl);
		}
		Body b = source.retrieveActiveBody();
		for (Iterator<Unit> sIt = b.getUnits().iterator(); sIt.hasNext();)
		{
			final Stmt s = (Stmt) sIt.next();
			if (s.containsInvokeExpr())
			{
				InvokeExpr ie = s.getInvokeExpr();
				final String methRefSig = ie.getMethodRef().getSignature();
				if (methRefSig.equals("<java.lang.reflect.Method: java.lang.Object invoke(java.lang.Object,java.lang.Object[])>"))
				{
					reflectionModel.methodInvoke(source, s);
				} else if (methRefSig.equals("<java.lang.Class: java.lang.Object newInstance()>"))
				{
					reflectionModel.classNewInstance(source, s);
				} else if (methRefSig.equals("<java.lang.reflect.Constructor: java.lang.Object newInstance(java.lang.Object[])>"))
				{
					reflectionModel.contructorNewInstance(source, s);
				}
				if (ie.getMethodRef().getSubSignature() == sigForName)
				{
					reflectionModel.classForName(source, s);
				}
				if (ie instanceof StaticInvokeExpr)
				{
					SootClass cl = ie.getMethodRef().declaringClass();
					for (SootMethod clinit : EntryPoints.v().clinitsOf(cl))
					{
						addEdge(source, s, clinit, Kind.CLINIT);
					}
				}
			}
			if (s.containsFieldRef())
			{
				FieldRef fr = s.getFieldRef();
				if (fr instanceof StaticFieldRef)
				{
					SootClass cl = fr.getFieldRef().declaringClass();
					for (SootMethod clinit : EntryPoints.v().clinitsOf(cl))
					{
						addEdge(source, s, clinit, Kind.CLINIT);
					}
				}
			}
			if (s instanceof AssignStmt)
			{
				Value rhs = ((AssignStmt) s).getRightOp();
				if (rhs instanceof NewExpr)
				{
					NewExpr r = (NewExpr) rhs;
					SootClass cl = r.getBaseType().getSootClass();
					for (SootMethod clinit : EntryPoints.v().clinitsOf(cl))
					{
						addEdge(source, s, clinit, Kind.CLINIT);
					}
				} else if (rhs instanceof NewArrayExpr || rhs instanceof NewMultiArrayExpr)
				{
					Type t = rhs.getType();
					if (t instanceof ArrayType)
						t = ((ArrayType) t).baseType;
					if (t instanceof RefType)
					{
						SootClass cl = ((RefType) t).getSootClass();
						for (SootMethod clinit : EntryPoints.v().clinitsOf(cl))
						{
							addEdge(source, s, clinit, Kind.CLINIT);
						}
					}
				}
			}
		}
	}

	private void processNewMethodContext(MethodOrMethodContext momc)
	{
		SootMethod m = momc.method();
		Iterator<Edge> it = cicg.edgesOutOf(m);
		while (it.hasNext())
		{
			Edge e = (Edge) it.next();
			cm.addStaticEdge(momc, e.srcUnit(), e.tgt(), e.kind());
		}
	}

	private void handleInit(SootMethod source, final SootClass scl)
	{
		addEdge(source, null, scl, sigFinalize, Kind.FINALIZE);
	}

	private void constantForName(String cls, SootMethod src, Stmt srcUnit)
	{
		if (cls.length() > 0 && cls.charAt(0) == '[')
		{
			if (cls.length() > 1 && cls.charAt(1) == 'L' && cls.charAt(cls.length() - 1) == ';')
			{
				cls = cls.substring(2, cls.length() - 1);
				constantForName(cls, src, srcUnit);
			}
		} else
		{
			if (!Scene.v().containsClass(cls))
			{
				if (options.verbose())
				{
					G.v().out.println(
							"Warning: Class " + cls + " is" + " a dynamic class, and you did not specify" + " it as such; graph will be incomplete!");
				}
			} else
			{
				SootClass sootcls = Scene.v().getSootClass(cls);
				if (!sootcls.isPhantomClass())
				{
					if (!sootcls.isApplicationClass())
					{
						sootcls.setLibraryClass();
					}
					for (SootMethod clinit : EntryPoints.v().clinitsOf(sootcls))
					{
						addEdge(src, srcUnit, clinit, Kind.CLINIT);
					}
				}

			}
		}
	}

	private void addEdge(SootMethod src, Stmt stmt, SootMethod tgt, Kind kind)
	{
		cicg.addEdge(new Edge(src, stmt, tgt, kind));
	}

	private void addEdge(SootMethod src, Stmt stmt, SootClass cls, NumberedString methodSubSig, Kind kind)
	{
		SootMethod sm = cls.getMethodUnsafe(methodSubSig);
		if (sm != null)
		{
			addEdge(src, stmt, sm, kind);
		}
	}

	private void addEdge(SootMethod src, Stmt stmt, SootMethod tgt)
	{
		InvokeExpr ie = stmt.getInvokeExpr();
		addEdge(src, stmt, tgt, Edge.ieToKind(ie));
	}

	protected final NumberedString sigFinalize = Scene.v().getSubSigNumberer().findOrAdd("void finalize()");

	protected final NumberedString sigInit = Scene.v().getSubSigNumberer().findOrAdd("void <init>()");

	protected final NumberedString sigStart = Scene.v().getSubSigNumberer().findOrAdd("void start()");

	protected final NumberedString sigRun = Scene.v().getSubSigNumberer().findOrAdd("void run()");

	// added by zemisolsol
	protected final NumberedString sigCall = Scene.v().getSubSigNumberer().findOrAdd("java.lang.Object call()");

	protected final NumberedString sigExecutorSubmit = Scene.v().getSubSigNumberer()
			.findOrAdd("java.util.concurrent.Future submit(java.lang.Runnable)");

	protected final NumberedString sigExecute = Scene.v().getSubSigNumberer().findOrAdd("android.os.AsyncTask execute(java.lang.Object[])");

	protected final NumberedString sigExecutorExecute = Scene.v().getSubSigNumberer().findOrAdd("void execute(java.lang.Runnable)");

	protected final NumberedString sigHandlerPost = Scene.v().getSubSigNumberer().findOrAdd("boolean post(java.lang.Runnable)");

	protected final NumberedString sigHandlerPostAtFrontOfQueue = Scene.v().getSubSigNumberer()
			.findOrAdd("boolean postAtFrontOfQueue(java.lang.Runnable)");

	protected final NumberedString sigHandlerPostAtTime = Scene.v().getSubSigNumberer().findOrAdd("boolean postAtTime(java.lang.Runnable,long)");

	protected final NumberedString sigHandlerPostAtTimeWithToken = Scene.v().getSubSigNumberer()
			.findOrAdd("boolean postAtTime(java.lang.Runnable,java.lang.Object,long)");

	protected final NumberedString sigHandlerPostDelayed = Scene.v().getSubSigNumberer().findOrAdd("boolean postDelayed(java.lang.Runnable,long)");

	protected final NumberedString sigObjRun = Scene.v().getSubSigNumberer().findOrAdd("java.lang.Object run()");

	protected final NumberedString sigDoInBackground = Scene.v().getSubSigNumberer().findOrAdd("java.lang.Object doInBackground(java.lang.Object[])");

	protected final NumberedString sigForName = Scene.v().getSubSigNumberer().findOrAdd("java.lang.Class forName(java.lang.String)");

	protected final NumberedString sigVolleyInit = Scene.v().getSubSigNumberer()
			.findOrAdd("void <init>(int,java.lang.String,com.android.volley.Response$ErrorListener)");

	protected final NumberedString sigVolleyAdd = Scene.v().getSubSigNumberer()
			.findOrAdd("com.android.volley.Request add(com.android.volley.Request)");

	protected final RefType clRunnable = RefType.v("java.lang.Runnable");

	protected final RefType clAsyncTask = RefType.v("android.os.AsyncTask");
}