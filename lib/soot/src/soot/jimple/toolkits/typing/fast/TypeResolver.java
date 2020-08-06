/* Soot - a J*va Optimization Framework
 * Copyright (C) 2008 Ben Bellamy 
 * 
 * All rights reserved.
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
package soot.jimple.toolkits.typing.fast;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import soot.ArrayType;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.IntType;
import soot.IntegerType;
import soot.Local;
import soot.PatchingChain;
import soot.RefType;
import soot.ShortType;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.DefinitionStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NegExpr;
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.typing.Util;
import soot.toolkits.scalar.LocalDefs;

/**
 * New Type Resolver by Ben Bellamy (see 'Efficient Local Type Inference'
 * at OOPSLA 08).
 *
 * Ben has tested this code, and verified that it provides a typing
 * that is at least as tight as the original algorithm (tighter in
 * 2914 methods out of 295598) on a number of benchmarks. These are:
 * abc-complete.jar, BlueJ, CSO (Scala code), Gant, Groovy, havoc.jar,
 * Java 3D, jEdit, Java Grande Forum, Jigsaw, Jython, Kawa, rt.jar,
 * Kawa, Scala and tools.jar.
 * The mean execution time improvement is around 10 times,
 * but for the longest methods (abc parser methods and havoc with
 * >9000 statements) the improvement is between 200 and 500 times.  
 * 
 * @author Ben Bellamy
 */
public class TypeResolver
{
	private JimpleBody jb;
	
	private final List<DefinitionStmt> assignments;
	private final HashMap<Local, BitSet> depends;
	
	public TypeResolver(JimpleBody jb)
	{
		this.jb = jb;

		this.assignments = new ArrayList<DefinitionStmt>();
		this.depends = new HashMap<Local, BitSet>();
		for ( Local v : this.jb.getLocals() )
			this.addLocal(v);
		this.initAssignments();
	}
	
	private void initAssignments()
	{
		for ( Unit stmt : this.jb.getUnits() )
			if ( stmt instanceof DefinitionStmt )
				this.initAssignment((DefinitionStmt)stmt);
	}
	
	private void initAssignment(DefinitionStmt ds)
	{
		Value lhs = ds.getLeftOp(), rhs = ds.getRightOp();
		if ( lhs instanceof Local || lhs instanceof ArrayRef)
		{
			int assignmentIdx = this.assignments.size();
			this.assignments.add(ds);
			
			if ( rhs instanceof Local )
				this.addDepend((Local)rhs, assignmentIdx);
			else if ( rhs instanceof BinopExpr )
			{
				BinopExpr be = (BinopExpr)rhs;
				Value lop = be.getOp1(), rop = be.getOp2();
				if ( lop instanceof Local )
					this.addDepend((Local)lop, assignmentIdx);
				if ( rop instanceof Local )
					this.addDepend((Local)rop, assignmentIdx);
			}
			else if ( rhs instanceof NegExpr )
			{
				Value op = ((NegExpr)rhs).getOp();
				if ( op instanceof Local )
					this.addDepend((Local)op, assignmentIdx);
			}
			else if ( rhs instanceof CastExpr ) {
				Value op = ((CastExpr)rhs).getOp();
				if ( op instanceof Local )
					this.addDepend((Local)op, assignmentIdx);
			}
			else if ( rhs instanceof ArrayRef )
				this.addDepend((Local)((ArrayRef)rhs).getBase(), assignmentIdx);
		}
	}
	
	private void addLocal(Local v)
	{
		this.depends.put(v, new BitSet());
	}
	
	private void addDepend(Local v, int stmtIndex)
	{
		this.depends.get(v).set(stmtIndex);
	}
	
	public void inferTypes()
	{		
		AugEvalFunction ef = new AugEvalFunction(this.jb);
		BytecodeHierarchy bh = new BytecodeHierarchy();
		Collection<Typing> sigma = this.applyAssignmentConstraints(
			new Typing(this.jb.getLocals()), ef, bh);
		
		// If there is nothing to type, we can quit
		if (sigma.isEmpty())
			return;
		
		int[] castCount = new int[1];
		Typing tg = this.minCasts(sigma, bh, castCount);
		if ( castCount[0] != 0 )
		{
			this.split_new();
			sigma = this.applyAssignmentConstraints(
				new Typing(this.jb.getLocals()), ef, bh);
			tg = this.minCasts(sigma, bh, castCount);
		}
		this.insertCasts(tg, bh, false);
		
		for ( Local v : this.jb.getLocals() )
		{
			Type t = tg.get(v);
			if ( t instanceof IntegerType )
			{
				t = IntType.v();
				tg.set(v, BottomType.v());
			}
			v.setType(t);
		}
		
		tg = this.typePromotion(tg);
		if ( tg  == null )
			// Use original soot algorithm for inserting casts
			soot.jimple.toolkits.typing.integer.TypeResolver.resolve(this.jb);
		else
			for ( Local v : this.jb.getLocals() )
				v.setType(tg.get(v));
	}
	
	private class CastInsertionUseVisitor implements IUseVisitor
	{
		private JimpleBody jb;
		private Typing tg;
		private IHierarchy h;
		
		private boolean countOnly;
		private int count;
		
		public CastInsertionUseVisitor(boolean countOnly, JimpleBody jb,
			Typing tg, IHierarchy h)
		{
			this.jb = jb;
			this.tg = tg;
			this.h = h;
			
			this.countOnly = countOnly;
			this.count = 0;
		}
		
		public Value visit(Value op, Type useType, Stmt stmt)
		{
			Type t = AugEvalFunction.eval_(this.tg, op, stmt, this.jb);
			
			if ( this.h.ancestor(useType, t) )
				return op;
			
			this.count++;
			
			if ( countOnly )
				return op;
			else
			{
				// If we're referencing an array of the base type java.lang.Object,
				// we also need to fix the type of the assignment's target variable.
				if (stmt.containsArrayRef()
						&& stmt.getArrayRef().getBase() == op
						&& stmt instanceof DefinitionStmt) {
					Type baseType = tg.get((Local) stmt.getArrayRef().getBase());
					DefinitionStmt defStmt = (DefinitionStmt) stmt;
					if (baseType instanceof RefType && defStmt.getLeftOp() instanceof Local) {
						RefType rt = (RefType) baseType;
						if (rt.getSootClass().getName().equals("java.lang.Object")
								|| rt.getSootClass().getName().equals("java.io.Serializable")
								|| rt.getSootClass().getName().equals("java.lang.Cloneable"))
							tg.set((Local) ((DefinitionStmt) stmt).getLeftOp(), ((ArrayType) useType).getElementType());
					}
				}
				
				Local vold;
				if ( !(op instanceof Local) )
				{
					/* By the time we have countOnly == false, all variables
					must by typed with concrete Jimple types, and never [0..1],
					[0..127] or [0..32767]. */
					vold = Jimple.v().newLocal("tmp", t);
					vold.setName("tmp$" + System.identityHashCode(vold));
					this.tg.set(vold, t);
					this.jb.getLocals().add(vold);
					Unit u = Util.findFirstNonIdentityUnit(jb, stmt);
					this.jb.getUnits().insertBefore(
						Jimple.v().newAssignStmt(vold, op), u);
				}
				else
					vold = (Local)op;
				
				Local vnew = Jimple.v().newLocal("tmp", useType);
				vnew.setName("tmp$" + System.identityHashCode(vnew));
				this.tg.set(vnew, useType);
				this.jb.getLocals().add(vnew);
				Unit u = Util.findFirstNonIdentityUnit(jb, stmt);
				this.jb.getUnits().insertBefore(
					Jimple.v().newAssignStmt(vnew,
					Jimple.v().newCastExpr(vold, useType)), u);
				return vnew;
			}
		}
		
		public int getCount() { return this.count; }
		
		public boolean finish() { return false; }
	}
	
	private class TypePromotionUseVisitor implements IUseVisitor
	{
		private JimpleBody jb;
		private Typing tg;
		
		public boolean fail;
		public boolean typingChanged;
		
		public TypePromotionUseVisitor(JimpleBody jb, Typing tg)
		{
			this.jb = jb;
			this.tg = tg;
			
			this.fail = false;
			this.typingChanged = false;
		}
		
		private Type promote(Type tlow, Type thigh)
		{
			if ( tlow instanceof Integer1Type )
			{
				if ( thigh instanceof IntType )
					return Integer127Type.v();
				else if ( thigh instanceof ShortType )
					return ByteType.v();
				else if ( thigh instanceof BooleanType
					|| thigh instanceof ByteType
					|| thigh instanceof CharType
					|| thigh instanceof Integer127Type
					|| thigh instanceof Integer32767Type )
					return thigh;
				else throw new RuntimeException();
			}
			else if ( tlow instanceof Integer127Type )
			{
				if ( thigh instanceof ShortType )
					return ByteType.v();
				else if ( thigh instanceof IntType )
					return Integer127Type.v();
				else if ( thigh instanceof ByteType
					|| thigh instanceof CharType
					|| thigh instanceof Integer32767Type )
					return thigh;
				else throw new RuntimeException();
			}
			else if ( tlow instanceof Integer32767Type )
			{
				if ( thigh instanceof IntType )
					return Integer32767Type.v();
				else if ( thigh instanceof ShortType
					|| thigh instanceof CharType )
					return thigh;
				else throw new RuntimeException();
			}
			else throw new RuntimeException();
		}
		
		public Value visit(Value op, Type useType, Stmt stmt)
		{
			if ( this.finish() )
				return op;
			
			Type t = AugEvalFunction.eval_(this.tg, op, stmt, this.jb);
			
			if ( !AugHierarchy.ancestor_(useType, t) )
				this.fail = true;
			else if ( op instanceof Local &&
				(t instanceof Integer1Type
				|| t instanceof Integer127Type
				|| t instanceof Integer32767Type) )
			{
				Local v = (Local)op;
				if ( !typesEqual(t, useType) )
				{
					Type t_ = this.promote(t, useType);
					if ( !typesEqual(t, t_) )
					{
						this.tg.set(v, t_);
						this.typingChanged = true;
					}
				}
			}
			
			return op;
		}
		
		public boolean finish() { return this.typingChanged || this.fail; }
	}
	
	private Typing typePromotion(Typing tg)
	{
		boolean conversionDone;
		do {
			AugEvalFunction ef = new AugEvalFunction(this.jb);
			AugHierarchy h = new AugHierarchy();
			UseChecker uc = new UseChecker(this.jb);
			TypePromotionUseVisitor uv = new TypePromotionUseVisitor(jb, tg);
			do
			{
				Collection<Typing> sigma
					= this.applyAssignmentConstraints(tg, ef, h);
				if ( sigma.isEmpty() )
					return null;
				tg = sigma.iterator().next();
				uv.typingChanged = false;
				uc.check(tg, uv);
				if ( uv.fail )
					return null;
			} while ( uv.typingChanged );

			conversionDone = false;
			for ( Local v : this.jb.getLocals() )
			{
				Type t = tg.get(v);
				if ( t instanceof Integer1Type )
				{
					tg.set(v, BooleanType.v());
					conversionDone = true;
				}
				else if ( t instanceof Integer127Type )
				{
					tg.set(v, ByteType.v());
					conversionDone = true;
				}
				else if ( t instanceof Integer32767Type )
				{
					tg.set(v, ShortType.v());
					conversionDone = true;
				}
			}
		} while (conversionDone);
		
		return tg;
	}
	
	private int insertCasts(Typing tg, IHierarchy h, boolean countOnly)
	{
		UseChecker uc = new UseChecker(this.jb);
		CastInsertionUseVisitor uv
			= new CastInsertionUseVisitor(countOnly, this.jb, tg, h);
		uc.check(tg, uv);
		return uv.getCount();
	}
	
	private Typing minCasts(Collection<Typing> sigma, IHierarchy h, int[] count)
	{
		Typing r = null;
		count[0] = -1;
		boolean setR = false;
		for ( Typing tg : sigma )
		{
			int n = this.insertCasts(tg, h, true);
			if ( count[0] == -1 || n < count[0] )
			{
				count[0] = n;
				r = tg;
				setR = true;
			}
		}
		if (setR)
			return r;
		else
			return null;
	}
	
	private Collection<Typing> applyAssignmentConstraints(Typing tg,
		IEvalFunction ef, IHierarchy h)
	{
		final int numAssignments = this.assignments.size();
		
		LinkedList<Typing> sigma = new LinkedList<Typing>(),
			r = new LinkedList<Typing>();
		if (numAssignments == 0)
			return sigma;
		
		HashMap<Typing, BitSet> worklists = new HashMap<Typing, BitSet>();
		
		sigma.add(tg);
		BitSet wl = new BitSet(numAssignments - 1);
		wl.set(0, numAssignments);
		worklists.put(tg, wl);
		
		while ( !sigma.isEmpty() )
		{
			tg = sigma.element();
			wl = worklists.get(tg);
			if ( wl.isEmpty() )
			{
				r.add(tg);
				sigma.remove();
				worklists.remove(tg);
			}
			else
			{
				// Get the next definition statement
				int defIdx = wl.nextSetBit(0);
				wl.clear(defIdx);
				DefinitionStmt stmt = this.assignments.get(defIdx);
				
				Value lhs = stmt.getLeftOp(), rhs = stmt.getRightOp();
				
				Local v;
				if ( lhs instanceof Local )
					v = (Local)lhs;
				else
					v = (Local)((ArrayRef)lhs).getBase();
				
				Type told = tg.get(v);
				
				Collection<Type> eval = new ArrayList<Type>(ef.eval(tg, rhs, stmt));
				
				boolean isFirstType = true;
				for ( Type t_ : eval )
				{
					if ( lhs instanceof ArrayRef )
					{
						/* We only need to consider array references on the LHS
						of assignments where there is supertyping between array
						types, which is only for arrays of reference types and
						multidimensional arrays. */
						if ( !(t_ instanceof RefType
							|| t_ instanceof ArrayType) )
						{
							continue;
						}
							
						t_ = t_.makeArrayType();
					}
					
					// Special handling for exception objects with phantom types
					final Collection<Type> lcas;
					if (!typesEqual(told, t_)
							&& told instanceof RefType && t_ instanceof RefType
							&& (
									((RefType) told).getSootClass().isPhantom()
									|| ((RefType) t_).getSootClass().isPhantom())
							&& (stmt.getRightOp() instanceof CaughtExceptionRef))
						lcas = Collections.<Type>singleton(RefType.v("java.lang.Throwable"));
					else
						lcas = h.lcas(told, t_);

					for ( Type t : lcas ) {
						if ( ! typesEqual(t, told) )
						{
							Typing tg_;
							BitSet wl_;
							if ( /*(eval.size() == 1 && lcas.size() == 1) ||*/ isFirstType )
							{
								// The types agree, we have a type we can directly use
								tg_ = tg;
								wl_ = wl;
							}
							else
							{
								// The types do not agree, add all supertype candidates
								tg_ = new Typing(tg);
								wl_ = new BitSet(numAssignments - 1);
								wl_.or(wl);
								sigma.add(tg_);
								worklists.put(tg_, wl_);
							}
							tg_.set(v, t);
							
							BitSet dependsV = this.depends.get(v);
							if (dependsV != null)
								wl_.or(dependsV);
						}
						isFirstType = false;
					}
				}//end for
			}
		}
		
		Typing.minimize(r, h);
		return r;
	}
	
	// The ArrayType.equals method seems odd in Soot 2.2.5
	public static boolean typesEqual(Type a, Type b)
	{
		if ( a instanceof ArrayType && b instanceof ArrayType )
		{
			ArrayType a_ = (ArrayType)a, b_ = (ArrayType)b;
			return a_.numDimensions == b_.numDimensions &&
				a_.baseType.equals(b_.baseType);
		}
			
		return a.equals(b);
	}
	
	/* Taken from the soot.jimple.toolkits.typing.TypeResolver class of Soot
	version 2.2.5. */
	private void split_new()
	{		
		LocalDefs defs = LocalDefs.Factory.newLocalDefs(jb);
		PatchingChain<Unit> units = this.jb.getUnits();
		Stmt[] stmts = new Stmt[units.size()];
		
		units.toArray(stmts);
		
		for ( Stmt stmt : stmts )
		{
			if ( stmt instanceof InvokeStmt )
			{
				InvokeStmt invoke = (InvokeStmt)stmt;
				
				if ( invoke.getInvokeExpr() instanceof SpecialInvokeExpr )
				{
					SpecialInvokeExpr special
						= (SpecialInvokeExpr)invoke.getInvokeExpr();
					
					if ( special.getMethodRef().name().equals("<init>") )
					{
						List<Unit> deflist = defs.getDefsOfAt(
							(Local)special.getBase(), invoke);
						
						while ( deflist.size() == 1 )
						{
							Stmt stmt2 = (Stmt)deflist.get(0);
							
							if ( stmt2 instanceof AssignStmt )
							{
								AssignStmt assign = (AssignStmt)stmt2;
								
								if ( assign.getRightOp() instanceof Local )
								{
									deflist = defs.getDefsOfAt(
										(Local)assign.getRightOp(), assign);
									continue;
								}
								else if ( assign.getRightOp()
									instanceof NewExpr )
								{
									Local newlocal = Jimple.v().newLocal(
										"tmp", null);
									newlocal.setName("tmp$" + System.identityHashCode(newlocal));
									this.jb.getLocals().add(newlocal);
									
									special.setBase(newlocal);
									
									DefinitionStmt assignStmt
										= Jimple.v().newAssignStmt(
										assign.getLeftOp(), newlocal);
									Unit u = Util.findLastIdentityUnit(jb, assign);
									units.insertAfter(assignStmt, u);
									assign.setLeftOp(newlocal);
									
									this.addLocal(newlocal);
									this.initAssignment(assignStmt);
								}
							}
							break;
						}
					}
				}
			}
		}
	}
}
