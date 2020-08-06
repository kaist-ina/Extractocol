package extractocol.backend.response.taint;

import java.util.HashSet;
import java.util.Set;

import extractocol.backend.response.basics.ParameterBucket;
import extractocol.backend.response.taint.unitHandle.*;
import extractocol.common.helper.InvokeHelper;
import extractocol.common.outputs.BackendOutput;
import soot.Unit;
import soot.jimple.*;

public class TaintingHelper {
	
	/** Handle assign/identity/return statement for taint analysis
	 * 
	 * @param ut assign/identity/return statement
	 * @param pb parameter bucket
	 */
	public static void UnitHandling(Unit ut, ParameterBucket pb){
		if (ut instanceof AssignStmt) {
			Unit_Assignment.Handle(ut, pb);
		} else if (ut instanceof IdentityStmt) {
			Unit_Identity.Handle(ut, pb);
		} else if (ut instanceof ReturnStmt) {
			Unit_Return.Handle(ut, pb);
		}
	}
	
	/** Handle DP statement for taint analysis 
	 * 
	 * @param ut DP statement
	 * @param pb parameter bucket 
	 */
	/*public static void DPHandling(Unit ut, ParameterBucket pb){
		int argIdx = BackendOutput.reqRespInfo.respie.getArgNum();
		
		switch(BackendOutput.reqRespInfo.respie.getSeedType()){
		case DEST:
			String left = "";
			if (ut instanceof AssignStmt)
				left = ((AssignStmt)ut).getLeftOp().toString();
			else if (ut instanceof DefinitionStmt)
				left = ((DefinitionStmt)ut).getLeftOp().toString();
			Tainting(left, pb); 
			break;
		case BASE: Tainting(InvokeHelper.getBase(ut), pb); break;
		case ARG: Tainting(InvokeHelper.getArg(ut, argIdx), pb); break;
		default: return;
		}
	}*/
	
	/** Handle invoke statement for taint analysis
	 * 
	 * @param ut
	 * @param caller_pb
	 * @param callee_pb
	 * @return True if the callee method is not stub method.
	 */
	public static boolean InvokeHandling(Unit ut, ParameterBucket caller_pb, ParameterBucket callee_pb){
		return Unit_Invoke.Handle(ut, caller_pb, callee_pb);
	}
	
	/** Taint the corresponding variables (dest and/or base) 
	 * 
	 * @param ut invoke statement
	 * @param pb parameter bucket
	 */
	public static void InvokeHandling(Unit ut, ParameterBucket pb){
		Unit_Invoke.Handle(ut, pb);
	}
	
	/** Try to taint taintee if tainter variable is tainted
	 * 
	 * @param taintee variable that might be being tainted
	 * @param tainter variable that might be tainted already
	 * @param pb parameter bucket
	 * @return True, if tainter has been tainted
	 */
	public static boolean tryTainting(String taintee, String tainter, ParameterBucket pb){
		if(taintee == null || tainter == null) return false;
		
		if(pb.isVarTainted(tainter)){
			TaintingHelper.Tainting(taintee, pb);
			return true;
		}
		
		return false;
	}
	
	/** Register target with the corresponding source
	 * 
	 * @param target left operator of unit
	 * @param source right operator of unit
	 * @param pb parameter bucket
	 */
	public static void pointsTo(String target, String source, ParameterBucket pb){
		pb.addPTA(target, source);
	}
	
	/** Taint target and variables that point to target
	 * 
	 * @param target target variable
	 * @param pb parameter bucket
	 */
	public static void Tainting(String target, ParameterBucket pb){
		Tainting_Inner(target, pb);
		
		try{
			taintSource(target, pb, new HashSet<String>());
		}catch(StackOverflowError e){
			/** The code below is for debugging. */
			System.err.println("[StackOverFlowError] ut: " + pb.ut.toString() + ", sm: " + pb.sm.toString() + " " + pb.getTaintedVariables().toString());
			System.exit(0);
		}
	}
	
	/** Taint target and check whether it is THIS variable
	 * 
	 * @param target target variable
	 * @param pb parameter bucket
	 */
	private static void Tainting_Inner(String target, ParameterBucket pb){
		pb.addTaintedVariable(target);
		
		if(pb.isThisVariable(target))
			pb.setThisTainted(true);
	}
	
	/** Taint variables that point to source recursively
	 *  (substitute for backward taint analysis)
	 * 
	 * @param source source variable
	 * @param pb parameter bucket
	 * @param varStack variable stack to avoid stack overflow 
	 */
	private static void taintSource(String source, ParameterBucket pb, Set<String> varStack){
		Set<String> tgtSet = pb.getPointSource(source);
		if(tgtSet == null) return;
		
		// To avoid stack over flow due to loop
		varStack.add(source);
		
		for(String target : tgtSet){
			Tainting_Inner(target, pb);
			
			if(varStack.contains(target))
				continue;
			
			taintSource(target, pb, new HashSet<String>(varStack));
		}
	}
}
