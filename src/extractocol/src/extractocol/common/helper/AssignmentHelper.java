package extractocol.common.helper;

import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;

public class AssignmentHelper {
	/** Check whether the variable is in base/args of the unit
	 * 
	 * @param var variable
	 * @param u unit
	 * @return True if the variable is in base/args of the unit, or false
	 */
	public static boolean isVarInBaseOrArgs(String var, AssignStmt as) {
		if(!as.containsInvokeExpr())
			return false;
		
		InvokeExpr ie = as.getInvokeExpr();
		
		// Check arguments
		for(Value arg: ie.getArgs()) {
			if(var.equals(arg.toString()))
				return true;
		}
		
		// Check base
		if(ie instanceof InstanceInvokeExpr) {
			InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
			if(var.equals(iie.getBase().toString()))
				return true;
		}
		
		return false;
	}
}
