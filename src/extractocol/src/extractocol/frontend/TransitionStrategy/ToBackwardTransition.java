package extractocol.frontend.TransitionStrategy;

import extractocol.common.helper.InvokeHelper;
import extractocol.frontend.helper.PropagateHelper.PROPA_TYPE;
import extractocol.frontend.output.basic.TaintResultEntry;
import extractocol.frontend.problem.AliasUtil;
import extractocol.frontend.solver.MyBackwardsInfoflowSolver;
import extractocol.frontend.solver.MyIFDSSolver;
import extractocol.frontend.solver.MyInfoflowSolver;
import heros.solver.PathEdge;
import soot.CallbackCandidateFinder;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.FieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

public class ToBackwardTransition {
	
	/** Handle normal for transition
	 * 
	 * @param d1
	 * @param currUnit
	 * @param source
	 * @param icfg
	 * @param tre
	 * @param bSolver
	 */
	public static void Normal(Abstraction d1, Unit currUnit, Abstraction source, BiDiInterproceduralCFG<Unit, SootMethod> icfg, TaintResultEntry tre, 
			MyIFDSSolver<Unit, Abstraction, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> bSolver) 
	{
		if(!(currUnit instanceof AssignStmt))
			return;
		
		if(NormalCase1(d1, (AssignStmt)currUnit, source, icfg, tre, (MyBackwardsInfoflowSolver)bSolver))
			return;
	}
	
	/** Handle call stmts for transition
	 * 
	 * @param d1
	 * @param currUnit
	 * @param source
	 * @param icfg
	 * @param tre
	 * @param bSolver
	 */
	public static void Call(Abstraction d1, Unit currUnit, Abstraction source, BiDiInterproceduralCFG<Unit, SootMethod> icfg, TaintResultEntry tre, 
			MyIFDSSolver<Unit, Abstraction, SootMethod, BiDiInterproceduralCFG<Unit, SootMethod>> bSolver) 
	{
		if(CallCase1(d1, currUnit, source, icfg, tre, (MyBackwardsInfoflowSolver)bSolver))
			return;
	}
	
	/** Normal Case 1
	 *  $r0.<heap> = $r5 and $r0 tainted, then propagate $r5 backward
	 */
	private static boolean NormalCase1(Abstraction d1, AssignStmt currUnit, Abstraction source, BiDiInterproceduralCFG<Unit, SootMethod> icfg, TaintResultEntry tre, MyBackwardsInfoflowSolver bSolver) {
		if (!AliasUtil.baseMatches(currUnit.getLeftOp(), source))
			return false;
		
		// check whether the right Op is instance of Local
		if(!(currUnit.getRightOp() instanceof Local))
			return false;
		
		Value rightValue = currUnit.getRightOp();
		Abstraction newRightAbs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(rightValue, rightValue.getType(), false), currUnit);
		
		for(Unit nextUnit: icfg.getPredsOf(currUnit)) {
			TaintResultEntry newTRE = tre.Clone();
			newTRE.setPropaType(PROPA_TYPE.TR_B2F);
			newTRE.setPrevStmt(currUnit.toString());
			
			bSolver.processEdge(new PathEdge<Unit, Abstraction>(d1, nextUnit, newRightAbs), newTRE);
		}
		
		return true;
	}
	
	/** Call Case 1
	 *  invoke $r3.<SystemAPI>($r1,...) and $r3 tainted, then propagate all args($r1,...) backward
	 */
	private static boolean CallCase1(Abstraction d1, Unit currUnit, Abstraction source, BiDiInterproceduralCFG<Unit, SootMethod> icfg, TaintResultEntry tre, MyBackwardsInfoflowSolver bSolver) {
		InstanceInvokeExpr iie = InvokeHelper.getInstanceInvokeExpr(currUnit);
		if(iie == null)
			return false;
		
		// check whether the base variable is tainted
		if(!iie.getBase().equals(source.getAccessPath().getPlainValue()))
			return false;
		
		// Check whether it is system API
		if(!CallbackCandidateFinder.isClassInSystemPackage(iie.getMethodRef().declaringClass().toString()))
			return false;
		
		// propagate arguments backward if not constant
		boolean propagate = false;
		for(Value arg: iie.getArgs()) {
			if(arg instanceof Constant)
				continue;
			
			for(Unit nextUnit: icfg.getPredsOf(currUnit)) {
				Abstraction newArgAbs = source.deriveNewAbstraction(source.getAccessPath().copyWithNewValue(arg, arg.getType(), false), (Stmt) currUnit);
				
				TaintResultEntry newTRE = tre.Clone();
				newTRE.setPropaType(PROPA_TYPE.TR_B2F);
				newTRE.setPrevStmt(currUnit.toString());
				
				bSolver.processEdge(new PathEdge<Unit, Abstraction>(d1, nextUnit, newArgAbs), newTRE);
				
				propagate = true;
			}
		}
		
		return propagate;
	}
}
