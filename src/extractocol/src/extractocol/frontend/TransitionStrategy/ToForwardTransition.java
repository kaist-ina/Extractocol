package extractocol.frontend.TransitionStrategy;

import extractocol.frontend.helper.PropagateHelper.PROPA_TYPE;
import extractocol.frontend.output.basic.TaintResultEntry;
import extractocol.frontend.problem.AliasUtil;
import extractocol.frontend.solver.MyIFDSSolver;
import extractocol.frontend.solver.MyInfoflowSolver;
import heros.solver.PathEdge;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

public class ToForwardTransition {
	public static void Normal(Abstraction d1, Unit currUnit, Abstraction source, BiDiInterproceduralCFG<Unit, SootMethod> icfg/*Backward's*/, TaintResultEntry tre, 
			MyIFDSSolver<Unit, Abstraction, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> fSolver) {
		if(!(currUnit instanceof AssignStmt))
			return;
		
		if(NomalCase1(d1, (AssignStmt)currUnit, source, icfg, tre, (MyInfoflowSolver)fSolver))
			return;
		
		if(NomalCase2(d1, (AssignStmt)currUnit, source, icfg, tre, (MyInfoflowSolver)fSolver))
			return;
	}
	
	/** Normal Case 1 
	 *  $v1 = $v0.<heap> and $v0 tainted, then propagate $v1 forward 
	 */
	private static boolean NomalCase1(Abstraction d1, AssignStmt currUnit, Abstraction source, BiDiInterproceduralCFG<Unit, SootMethod> icfg, TaintResultEntry tre, MyInfoflowSolver fSolver) {
		if (!AliasUtil.baseMatches(currUnit.getRightOp(), source))
			return false;
		
		Value leftValue = currUnit.getLeftOp();
		Abstraction newLeftAbs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(leftValue, leftValue.getType(), false), currUnit);
		
		for(Unit nextUnit: icfg.getPredsOf(currUnit)) {
			TaintResultEntry newTRE = tre.Clone();
			newTRE.setPropaType(PROPA_TYPE.TR_F2B);
			newTRE.setPrevStmt(currUnit.toString());
			
			fSolver.processEdge(new PathEdge<Unit, Abstraction>(d1, nextUnit, newLeftAbs), newTRE);
		}
		
		return true;
	}
	
	/** Normal Case 2 
	 *  $v1 = $v0 and $v1 tainted, then propagate $v0 forward 
	 */
	private static boolean NomalCase2(Abstraction d1, AssignStmt currUnit, Abstraction source, BiDiInterproceduralCFG<Unit, SootMethod> icfg, TaintResultEntry tre, MyInfoflowSolver fSolver) {
		if(!currUnit.getLeftOp().equals(source.getAccessPath().getPlainValue()))
			return false;
		
		// check whether the right Op is instance of Local
		if(!(currUnit.getRightOp() instanceof Local))
			return false;
		
		Value rightValue = currUnit.getRightOp();
		Abstraction newRightAbs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(rightValue, rightValue.getType(), false), currUnit);
		
		for(Unit nextUnit: icfg.getPredsOf(currUnit)) {
			TaintResultEntry newTRE = tre.Clone();
			newTRE.setPropaType(PROPA_TYPE.TR_F2B);
			newTRE.setPrevStmt(currUnit.toString());
			
			fSolver.processEdge(new PathEdge<Unit, Abstraction>(d1, nextUnit, newRightAbs), newTRE);
		}
		
		return true;
	}
}
