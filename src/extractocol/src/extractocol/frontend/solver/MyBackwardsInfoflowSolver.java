package extractocol.frontend.solver;

import heros.FlowFunction;
import heros.solver.CountingThreadPoolExecutor;
import heros.solver.Pair;
import heros.solver.PathEdge;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import extractocol.frontend.TransitionStrategy.ToBackwardTransition;
import extractocol.frontend.TransitionStrategy.ToForwardTransition;
import extractocol.frontend.output.basic.TaintResultEntry;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.problems.AbstractInfoflowProblem;
import soot.jimple.infoflow.solver.IInfoflowSolver;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

public class MyBackwardsInfoflowSolver extends MyInfoflowSolver {
	private MyIFDSSolver<Unit, Abstraction, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> fSolver = null;
	public void setForwardSolver(MyIFDSSolver<Unit, Abstraction, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> _fSolver) { this.fSolver = _fSolver; }
	
	@Override
	public void HandleTransitionNormal(Abstraction d1, Unit currUnit, Abstraction source, TaintResultEntry tre) {
		if(this instanceof MyBackwardsInfoflowSolver)
			ToForwardTransition.Normal(d1, currUnit, source, icfg, tre, fSolver);
	}
	
	@Override
	public void HandleTransitionCall(Abstraction d1, Unit currUnit, Abstraction source, TaintResultEntry tre) {
		// Currently, there is no transition rule here.
	}
	
	public MyBackwardsInfoflowSolver(AbstractInfoflowProblem problem, CountingThreadPoolExecutor executor) {
		super(problem, executor);
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

	// is it used? by BK
	@Override
	public void injectContext(IInfoflowSolver otherSolver, SootMethod callee,
			Abstraction d3, Unit callSite, Abstraction d2, Abstraction d1) {
		if(!this.addIncoming(callee, d3, callSite, d1, d2)) {
			return;
		}
		
		Set<Pair<Unit, Abstraction>> endSumm = endSummary(callee, d3);		
		if (endSumm != null) {
			Collection<Unit> returnSiteNs = icfg.getReturnSitesOfCallAt(callSite);
			for(Pair<Unit, Abstraction> entry: endSumm) {
				Unit eP = entry.getO1();
				Abstraction d4 = entry.getO2();
				//for each return site
				for(Unit retSiteN: returnSiteNs) {
					//compute return-flow function
					FlowFunction<Abstraction> retFunction = flowFunctions.getReturnFlowFunction(callSite, callee, eP, retSiteN);
					//for each target value of the function
					for(Abstraction d5: computeReturnFlowFunction(retFunction, d3, d4, callSite, Collections.singleton(d1))) {
						if (memoryManager != null)
							d5 = memoryManager.handleGeneratedMemoryObject(d4, d5);
						
						// If we have not changed anything in the callee, we do not need the facts
						// from there. Even if we change something: If we don't need the concrete
						// path, we can skip the callee in the predecessor chain
						Abstraction d5p = d5;
						if (d5.equals(d2))
							d5p = d2;
						else if (setJumpPredecessors && d5p != d2) {
							d5p = d5p.clone();
							d5p.setPredecessor(d2);
						}
						propagate(d1, retSiteN, d5p, callSite, false, true, null);
					}
				}
			}
		}
	}
}
