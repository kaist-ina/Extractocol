package extractocol.backend.response.taint.unitHandle;

import extractocol.backend.response.basics.ParameterBucket;
import extractocol.backend.response.taint.TaintingHelper;
import extractocol.common.valueEntry.helper.General;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.NewExpr;
import soot.shimple.PhiExpr;


public class Unit_Assignment {
	public static void Handle(Unit ut, ParameterBucket pb) {
		AssignStmt as = (AssignStmt) ut;
		
		if (as.getRightOp() instanceof NewExpr) return;
		else if(as.getRightOp().toString().contains("newarray")) return;

		// Case 1: phi node
		if (as.getRightOp() instanceof PhiExpr)
			PhiStmtHandler(as, pb);
		
		// case 2: normal assignment (not containing invoke expr)
		else if(!as.containsInvokeExpr())
			NormalAssignmentHandler(as, pb);
	}

	/**
	 * 
	 * @param as
	 * @param pb
	 */
	private static void PhiStmtHandler(AssignStmt as, ParameterBucket pb) {
		String leftVar = as.getLeftOp().toString();
		PhiExpr phi = (PhiExpr) as.getRightOp();
		
		for(Value v : phi.getValues()){
			String phiVar = v.toString();
			
			TaintingHelper.tryTainting(leftVar, phiVar, pb);
			TaintingHelper.pointsTo(leftVar, phiVar, pb);
		}
	}

	private static void NormalAssignmentHandler(AssignStmt as, ParameterBucket pb) {
		// Case 1: $v = $v
		if (General.isLeftOpVariable(as) && General.isRightOpVariable(as))
			Var_Var(as, pb);
		// Case 2: $v = $v[]
		else if (General.isLeftOpVariable(as) && General.isRightOpArray(as))
			Var_Array(as, pb);
		// Case 3: $v = <>
		else if (General.isLeftOpVariable(as) && General.isRightOpHeap(as))
			Var_Heap(as, pb);
		// Case 5: $v[] = $v
		else if (General.isLeftOpArray(as) && General.isRightOpVariable(as))
			Array_Var(as, pb);
		// Case 7: <> = $v
		else if (General.isLeftOpHeap(as) && General.isRightOpVariable(as))
			Heap_Var(as, pb);
	}
	
	private static void Var_Var(AssignStmt as, ParameterBucket pb) {
		String left = General.getLeftVariable(as);
		String right = General.getRightVariable(as);
		
		TaintingHelper.tryTainting(left, right, pb);
		TaintingHelper.pointsTo(left, right, pb);
	}
	
	private static void Var_Array(AssignStmt as, ParameterBucket pb) {
		if(!(as.getRightOp() instanceof ArrayRef)) return;
		
		ArrayRef ar = (ArrayRef) as.getRightOp();
		String base = ar.getBase().toString();
		String left = General.getLeftVariable(as);
		
		TaintingHelper.tryTainting(left, base, pb);
		TaintingHelper.pointsTo(left, base, pb);
	}

	private static void Var_Heap(AssignStmt as, ParameterBucket pb) {
		String left = General.getLeftVariable(as);
		String right = General.getRightVariable(as);
		
		TaintingHelper.tryTainting(left, right, pb);
		//TaintingHelper.pointsTo(left, right, pb);
	}

	private static void Array_Var(AssignStmt as, ParameterBucket pb) {
		if (!(as.getLeftOp() instanceof ArrayRef))	return;
		
		ArrayRef ar = (ArrayRef) as.getLeftOp();
		String base = ar.getBase().toString();
		String right = General.getRightVariable(as);
		
		TaintingHelper.tryTainting(base, right, pb);
		TaintingHelper.pointsTo(base, right, pb);
	}

	private static void Heap_Var(AssignStmt as, ParameterBucket pb) {
		String left = General.getLeftVariable(as);
		String right = General.getRightVariable(as);
		
		TaintingHelper.tryTainting(left, right, pb);
		//TaintingHelper.pointsTo(left, right, pb);
	}
}
