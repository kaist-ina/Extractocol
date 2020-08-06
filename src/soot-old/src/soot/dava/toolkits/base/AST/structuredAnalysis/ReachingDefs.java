/* Soot - a J*va Optimization Framework
 * Copyright (C) 2005 Nomair A. Naeem
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

/**
 * Maintained by: Nomair A. Naeem
 */

package soot.dava.toolkits.base.AST.structuredAnalysis;

import soot.*;
import java.util.*;
import soot.jimple.*;
//import soot.dava.internal.javaRep.*;
import soot.dava.internal.AST.*;
//import soot.dava.internal.SET.*;
import soot.dava.toolkits.base.AST.traversals.*;

/**
 * CHANGE LOG: * November 21st Added support for implicit breaks and continues
 * Tested code for reaching defs within switch/try/if/while/for
 *
 * * November 22nd Refactored code to make structure flow analysis framework
 * handle breaks and returns.
 *
 * * November 24th newInitialFlow ERROR............initialFlow should be the set
 * of all defs........since there needs to exist SOME path
 */

/*
 * Reaching Defs Step 1: Set of definitions (a definition is a Stmt within a
 * StatementSequenceNode) Step 2: A definition d: x = ... reaches a point p in
 * the program if there exists a path from p such that there is no other
 * definition of x between d and p. Step 3: Forward Analysis Step 4: Union Step
 * 5: d: x = expr kill = { all existing defs of x}
 * 
 * gen = (d)
 * 
 * Step 6: newInitialFlow: No definitions reach (safe) (Catch bodies) //November
 * 24th.........changing above to be the universal set of all definitions
 * 
 * 
 * out(start) = {} since there has been no definition so far
 * 
 * out(Si) not needed for structured flow analysis
 */

public class ReachingDefs extends StructuredAnalysis {
	Object toAnalyze;

	public ReachingDefs(Object analyze) {
		super();
		toAnalyze = analyze;
		DavaFlowSet temp = (DavaFlowSet) process(analyze, new DavaFlowSet());
	}

	@Override
	public DavaFlowSet emptyFlowSet() {
		return new DavaFlowSet();
	}

	/*
	 * Initial flow into catch statements is empty meaning no definition reaches
	 */
	@Override
	public DavaFlowSet newInitialFlow() {
		DavaFlowSet initial = new DavaFlowSet();
		// find all definitions in the program
		AllDefinitionsFinder defFinder = new AllDefinitionsFinder();
		((ASTNode) toAnalyze).apply(defFinder);
		List<DefinitionStmt> allDefs = defFinder.getAllDefs();
		// all defs is the list of all augmented stmts which contains
		// DefinitionStmts
		Iterator<DefinitionStmt> defIt = allDefs.iterator();
		while (defIt.hasNext())
			initial.add(defIt.next());

		// initial is not the universal set of all definitions
		return initial;
	}

	/*
	 * Using union
	 */
	public void setMergeType() {
		MERGETYPE = UNION;
	}

	@Override
	public DavaFlowSet cloneFlowSet(DavaFlowSet flowSet) {
		return ((DavaFlowSet) flowSet).clone();
	}

	/*
	 * In the case of reachingDefs the evaluation of a condition has no effect
	 * on the reachingDefs
	 */
	@Override
	public DavaFlowSet processUnaryBinaryCondition(ASTUnaryBinaryCondition cond, DavaFlowSet inSet) {
		return inSet;
	}

	/*
	 * In the case of reachingDefs the use of a local has no effect on
	 * reachingDefs
	 */
	@Override
	public DavaFlowSet processSynchronizedLocal(Local local, DavaFlowSet inSet) {
		return inSet;
	}

	/*
	 * In the case of reachingDefs a value has no effect on reachingDefs
	 */
	@Override
	public DavaFlowSet processSwitchKey(Value key, DavaFlowSet inSet) {
		return inSet;
	}

	/*
	 * This method internally invoked by the process method decides which
	 * Statement specialized method to call
	 */
	@Override
	public DavaFlowSet processStatement(Stmt s, DavaFlowSet inSet) {		
		/*
		 * If this path will not be taken return no path straightaway
		 */
		if (inSet == NOPATH) {
			return inSet;
		}

		if (s instanceof DefinitionStmt) {
			DavaFlowSet toReturn = cloneFlowSet(inSet);
			// d:x = expr
			// gen is x
			// kill is all previous defs of x

			Value leftOp = ((DefinitionStmt) s).getLeftOp();

			if (leftOp instanceof Local) {
				// KILL any reaching defs of leftOp
				kill(toReturn, (Local) leftOp);
				// GEN
				gen(toReturn, (DefinitionStmt) s);
				return toReturn;
			}// leftop is a local
		}
		return inSet;
	}

	public void gen(DavaFlowSet in, DefinitionStmt s) {
		// System.out.println("Adding Definition Stmt: "+s);
		in.add(s);
	}

	public void kill(DavaFlowSet in, Local redefined) {
		String redefinedLocalName = redefined.getName();

		// kill any previous localpairs which have the redefined Local in the
		// left i.e. previous definitions
		for (Iterator listIt = in.iterator(); listIt.hasNext(); ) {
			DefinitionStmt tempStmt = (DefinitionStmt) listIt.next();
			Value leftOp = tempStmt.getLeftOp();
			if (leftOp instanceof Local) {
				String storedLocalName = ((Local) leftOp).getName();
				if (redefinedLocalName.compareTo(storedLocalName) == 0) {
					// need to kill this from the list
					// System.out.println("Killing "+tempStmt);
					listIt.remove();
				}
			}
		}
	}

	public List<DefinitionStmt> getReachingDefs(Local local, Object node) {
		ArrayList<DefinitionStmt> toReturn = new ArrayList<DefinitionStmt>();

		// get the reaching defs of this node
		DavaFlowSet beforeSet = null;
		/*
		 * If this object is some sort of loop while, for dowhile, unconditional
		 * then return after set
		 */
		if (node instanceof ASTWhileNode || node instanceof ASTDoWhileNode || node instanceof ASTUnconditionalLoopNode
				|| node instanceof ASTForLoopNode)
			beforeSet = getAfterSet(node);
		else
			beforeSet = getBeforeSet(node);

		if (beforeSet == null) {
			throw new RuntimeException("Could not get reaching defs of node");
		}

		// find all reachingdefs matching this local
		for (Object temp : beforeSet) {
			// checking each def to see if it is a def of local
			if (!(temp instanceof DefinitionStmt))
				throw new RuntimeException("Not an instanceof DefinitionStmt" + temp);
			DefinitionStmt stmt = (DefinitionStmt) temp;
			Value leftOp = stmt.getLeftOp();
			if (leftOp.toString().compareTo(local.toString()) == 0) {
				toReturn.add(stmt);
			}
		}
		return toReturn;
	}

	public void reachingDefsToString(Object node) {
		ArrayList toReturn = new ArrayList();

		// get the reaching defs of this node
		DavaFlowSet beforeSet = null;
		/*
		 * If this object is some sort of loop while, for dowhile, unconditional
		 * then return after set
		 */
		if (node instanceof ASTWhileNode || node instanceof ASTDoWhileNode || node instanceof ASTUnconditionalLoopNode
				|| node instanceof ASTForLoopNode)
			beforeSet = getAfterSet(node);
		else
			beforeSet = getBeforeSet(node);

		if (beforeSet == null) {
			throw new RuntimeException("Could not get reaching defs of node");
		}

		// find all reachingdefs matching this local
		for (Object o : beforeSet) {
			System.out.println("Reaching def:" + o);
		}
	}
}