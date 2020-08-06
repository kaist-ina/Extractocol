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
package extractocol.frontend.solver;

import heros.FlowFunction;
import heros.solver.CountingThreadPoolExecutor;
import heros.solver.Pair;
import heros.solver.PathEdge;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import extractocol.frontend.TransitionStrategy.ToBackwardTransition;
import extractocol.frontend.output.basic.TaintResultEntry;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.problems.AbstractInfoflowProblem;
import soot.jimple.infoflow.solver.IFollowReturnsPastSeedsHandler;
import soot.jimple.infoflow.solver.IInfoflowSolver;
import soot.jimple.infoflow.solver.functions.SolverCallFlowFunction;
import soot.jimple.infoflow.solver.functions.SolverCallToReturnFlowFunction;
import soot.jimple.infoflow.solver.functions.SolverNormalFlowFunction;
import soot.jimple.infoflow.solver.functions.SolverReturnFlowFunction;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
/**
 * We are subclassing the JimpleIFDSSolver because we need the same executor for both the forward and the backward analysis
 * Also we need to be able to insert edges containing new taint information
 * 
 */
public class MyInfoflowSolver extends MyIFDSSolver<Unit, Abstraction, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>>
		implements IInfoflowSolver {
	
	private IFollowReturnsPastSeedsHandler followReturnsPastSeedsHandler = null;
	private MyIFDSSolver<Unit, Abstraction, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> bSolver = null;
	public void setBackwardSolver(MyIFDSSolver<Unit, Abstraction, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> _bSolver) { this.bSolver = _bSolver; }
	
	@Override
	public void HandleTransitionNormal(Abstraction d1, Unit currUnit, Abstraction source, TaintResultEntry tre) {
		ToBackwardTransition.Normal(d1, currUnit, source, icfg, tre, bSolver);
	}
	
	@Override
	public void HandleTransitionCall(Abstraction d1, Unit currUnit, Abstraction source, TaintResultEntry tre) {
		ToBackwardTransition.Call(d1, currUnit, source, icfg, tre, bSolver);
	}
	
	public MyInfoflowSolver(AbstractInfoflowProblem problem, CountingThreadPoolExecutor executor) {
		super(problem);
		this.executor = executor;
		problem.setSolver(this);		
	}
	
	@Override
	protected CountingThreadPoolExecutor getExecutor() {
		return executor;
	}

	// The following function is never called in Frontend of Extractocol (by BK)
	@Override
	public boolean processEdge(PathEdge<Unit, Abstraction> edge){
		propagate(edge.factAtSource(), edge.getTarget(), edge.factAtTarget(), null, false, true, null);
		return true;
	}
	
	/**
	 * (Added by BK)
	 * @param edge
	 * @param tre
	 * @return
	 */
	public boolean processEdge(PathEdge<Unit, Abstraction> edge, TaintResultEntry tre){
		propagate(edge.factAtSource(), edge.getTarget(), edge.factAtTarget(), null, false, true, tre);
		return true;
	}
	
	@Override
	public void injectContext(IInfoflowSolver otherSolver, SootMethod callee,
			Abstraction d3, Unit callSite, Abstraction d2, Abstraction d1) {
		addIncoming(callee, d3, callSite, d1, d2);
	}
	
	@Override
	protected Set<Abstraction> computeReturnFlowFunction(
			FlowFunction<Abstraction> retFunction,
			Abstraction d1,
			Abstraction d2,
			Unit callSite,
			Collection<Abstraction> callerSideDs) {
		if (retFunction instanceof SolverReturnFlowFunction) {
			// Get the d1s at the start points of the caller
			return ((SolverReturnFlowFunction) retFunction).computeTargets(d2, d1, callerSideDs);
		}
		else
			return retFunction.computeTargets(d2);
	}

	@Override
	protected Set<Abstraction> computeNormalFlowFunction
			(FlowFunction<Abstraction> flowFunction, Abstraction d1, Abstraction d2) {
		if (flowFunction instanceof SolverNormalFlowFunction)
			return ((SolverNormalFlowFunction) flowFunction).computeTargets(d1, d2);
		else
			return flowFunction.computeTargets(d2);
	}

	@Override
	protected Set<Abstraction> computeCallToReturnFlowFunction
			(FlowFunction<Abstraction> flowFunction, Abstraction d1, Abstraction d2) {
		if (flowFunction instanceof SolverCallToReturnFlowFunction)
			return ((SolverCallToReturnFlowFunction) flowFunction).computeTargets(d1, d2);
		else
			return flowFunction.computeTargets(d2);		
	}

	@Override
	protected Set<Abstraction> computeCallFlowFunction
			(FlowFunction<Abstraction> flowFunction, Abstraction d1, Abstraction d2) {
		if (flowFunction instanceof SolverCallFlowFunction)
			return ((SolverCallFlowFunction) flowFunction).computeTargets(d1, d2);
		else
			return flowFunction.computeTargets(d2);		
	}
	
	@Override
	public void cleanup() {
		this.jumpFn.clear();
		this.incoming.clear();
		this.endSummary.clear();
	}
	
	@Override
	public Set<Pair<Unit, Abstraction>> endSummary(SootMethod m, Abstraction d3) {
		return super.endSummary(m, d3);
	}
	
	@Override
	protected void processExit(PathEdge<Unit, Abstraction> edge, boolean[] didTaint, TaintResultEntry tre) { // Modified by BK
		super.processExit(edge, didTaint, tre);
		
		if (followReturnsPastSeeds && followReturnsPastSeedsHandler != null) {
			final Abstraction d1 = edge.factAtSource();
			final Unit u = edge.getTarget();
			final Abstraction d2 = edge.factAtTarget();
			
			final SootMethod methodThatNeedsSummary = icfg.getMethodOf(u);
			final Map<Unit, Map<Abstraction, Abstraction>> inc = incoming(d1, methodThatNeedsSummary);
			
			if (inc == null || inc.isEmpty())
				followReturnsPastSeedsHandler.handleFollowReturnsPastSeeds(d1, u, d2);
		}
	}
	
	@Override
	public void setFollowReturnsPastSeedsHandler(IFollowReturnsPastSeedsHandler handler) {
		this.followReturnsPastSeedsHandler = handler;
	}
	
}
