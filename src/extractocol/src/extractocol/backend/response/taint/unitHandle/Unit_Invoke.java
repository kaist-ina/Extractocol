package extractocol.backend.response.taint.unitHandle;

import extractocol.backend.response.basics.ParameterBucket;
import extractocol.backend.response.taint.TaintingHelper;
import extractocol.common.helper.InvokeHelper;
import extractocol.frontend.helper.StubMethodHandler;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;

public class Unit_Invoke {
	
	/** Handling invoke statement for taint analysis
	 * 
	 * @param ut
	 * @param caller_pb
	 * @param callee_pb
	 * @return True if the callee method is not stub method.
	 */
	public static boolean Handle(Unit ut, ParameterBucket caller_pb, ParameterBucket callee_pb) {
		// 1. Check whether the callee method is stub or not
		if (StubMethodHandler.IsStubMethod(callee_pb.sm.toString())) {
			TaintingInGeneral(ut, caller_pb);
			
			return false;
		} else {
			// 2-2.
			InvokeExpr ie = InvokeHelper.getInvokeExpr(ut); 
			
			return deliverBaseTaint(InvokeHelper.getBase(ut), caller_pb, callee_pb) || deliverArgTaint(ie, caller_pb, callee_pb);
		}
	}
	
	public static void Handle(Unit ut, ParameterBucket pb){
		TaintingInGeneral(ut, pb);
	}
	
	public static void TaintingInGeneral(Unit ut, ParameterBucket pb){
		// 2-1.
		InvokeExpr ie = InvokeHelper.getInvokeExpr(ut);
		if(ie == null) return;

		String base = getBaseVariable(ie);
		String dest = getDestVariable(ut);

		// 3. Check whether base is tainted
		if(!TaintingHelper.tryTainting(dest, base, pb)) // tainting destination variable
			// 4. Check whether one of args is tainted
			if (isArgTainted(ie, pb)) {
				if (base != null) TaintingHelper.Tainting(base, pb); // tainting base variable
				if (dest != null) TaintingHelper.Tainting(dest, pb); // tainting destination variable
			}
	}
	
	/** Get destination variable name from unit
	 * 
	 * @param ut unit 
	 * @return Destination variable name if it exists
	 */
	private static String getDestVariable(Unit ut){
		if(!(ut instanceof AssignStmt))
			return null;
			
		return ((AssignStmt) ut).getLeftOp().toString();
	}
	
	/** Get the base variable from the invoke statement
	 * 
	 * @param is invoke statement
	 * @return variable name if is is instance invoke expr, or null
	 */
	private static String getBaseVariable(InvokeExpr ie){
		if (!(ie instanceof InstanceInvokeExpr))
			return null;
		
		return ((InstanceInvokeExpr) ie).getBase().toString();
	}

	/** Check whether if the base variable of invoke statment is tainted
	 * 
	 * @param is invoke statement
	 * @param pb parameter bucket
	 * @return True if the base exists and is tainted
	 */
	/*private static boolean isBaseTainted(InvokeStmt is, ParameterBucket pb) {
		if (!(is.getInvokeExpr() instanceof InstanceInvokeExpr))
			return false;

		InstanceInvokeExpr iie = (InstanceInvokeExpr) is.getInvokeExpr();
		return pb.isVarTainted(iie.getBase().toString());
	}*/
	
	/** Check whether one of args is tainted
	 * 
	 * @param is invoke statement
	 * @param pb parameter bucket
	 * @return True if one of args is tainted
	 */
	private static boolean isArgTainted(InvokeExpr ie, ParameterBucket pb) {
		for(Value arg : ie.getArgs()){
			if(pb.isVarTainted(arg.toString()))
				return true;
		}
		
		return false;
	}
	
	/** Try to taint 'this' of callee if the base of invoke expr is tainted in caller
	 * 
	 * @param is invoke statement
	 * @param caller_pb caller parameter bucket
	 * @param callee_pb callee parameter bucket
	 */
	private static boolean deliverBaseTaint(String base, ParameterBucket caller_pb, ParameterBucket callee_pb){
		if(base == null)
			return false;
		
		if(!caller_pb.isVarTainted(base))
			return false;
		
		callee_pb.addTaintedVariable("@this");
		return true;
	}
	
	/** Try to taint parameters of callee if the corresponding arg is tainted in caller
	 * 
	 * @param is invoke statement
	 * @param caller_pb caller parameter bucket
	 * @param callee_pb callee parameter bucket
	 */
	private static boolean deliverArgTaint(InvokeExpr ie, ParameterBucket caller_pb, ParameterBucket callee_pb){
		boolean hasDelivered = false;
		
		for(int i = 0; i < ie.getArgs().size(); i++){
			String arg = ie.getArg(i).toString();
			
			if(caller_pb.isVarTainted(arg.toString())){
				callee_pb.addTaintedVariable("@parameter" + i);
				hasDelivered = true;
			}
		}
		
		return hasDelivered;
	}
}
