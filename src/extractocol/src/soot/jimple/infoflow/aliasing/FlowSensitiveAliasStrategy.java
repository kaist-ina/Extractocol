package soot.jimple.infoflow.aliasing;

import extractocol.frontend.helper.PropagateHelper.PROPA_TYPE;
import extractocol.frontend.output.basic.TaintResultEntry;
import extractocol.frontend.solver.MyInfoflowSolver;
import heros.solver.PathEdge;

import java.util.List;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.solver.IInfoflowSolver;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

/**
 * A fully flow-sensitive aliasing strategy
 * 
 * @author Steven Arzt
 */
public class FlowSensitiveAliasStrategy extends AbstractBulkAliasStrategy {
	
	private final MyInfoflowSolver bSolver;
	
	public FlowSensitiveAliasStrategy(IInfoflowCFG cfg, MyInfoflowSolver backwardsSolver) {
		super(cfg);
		this.bSolver = backwardsSolver;
	}

	@Override
	public void computeAliasTaints
			(final Abstraction d1, final Stmt src,
			final Value targetValue, Set<Abstraction> taintSet,
			SootMethod method, Abstraction newAbs) {
		// Start the backwards solver
		Abstraction bwAbs = newAbs.deriveInactiveAbstraction(src);
		for (Unit predUnit : interproceduralCFG().getSuccsOf(src)) {
			/*TaintResultEntry tre = d1.getTRE().Clone();
			tre.setPrevStmt(predUnit.toString());
        	tre.setPropaType(PROPA_TYPE.TR_F2B);
        	
        	if(!methodContained(tre, predUnit)) {
        		System.out.println("TEST!!");
        	}*/
        	
			bSolver.processEdge(new PathEdge<Unit, Abstraction>(d1,
					predUnit, bwAbs));
		}
			
	}
	
	private boolean methodContained(TaintResultEntry tre, Unit u) {
		List<String> taintMethods = tre.getTaintMethodSet();
		String nextMethod = interproceduralCFG().getMethodOf(u).toString();
		
		return taintMethods.contains(nextMethod);
	}
	
	@Override
	public void injectCallingContext(Abstraction d3, IInfoflowSolver fSolver,
			SootMethod callee, Unit callSite, Abstraction source, Abstraction d1) {
		bSolver.injectContext(fSolver, callee, d3, callSite, source, d1);
	}

	@Override
	public boolean isFlowSensitive() {
		return true;
	}

	@Override
	public boolean requiresAnalysisOnReturn() {
		return false;
	}
	
}
