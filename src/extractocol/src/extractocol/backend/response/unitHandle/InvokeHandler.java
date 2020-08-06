
package extractocol.backend.response.unitHandle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import extractocol.Constants;
import extractocol.backend.request.SignatureBuilder_Request;
import extractocol.backend.response.basics.BFNode_Response;
import extractocol.backend.response.basics.ParameterBucket;
import extractocol.backend.response.helper.BFTTableHelper;
import extractocol.backend.response.helper.SearchMethodHelper;
import extractocol.backend.response.helper.TaintHelper;
import extractocol.backend.response.semantic.ResponseSemantic;
import extractocol.backend.response.taint.TaintingHelper;
import extractocol.backend.response.taint.unitHandle.Unit_Invoke;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntryTracking;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;

public class InvokeHandler {

	// temporary null
	// 20160603 hnamkoong
	public static List<String> SemanticModel = new ArrayList<String>();;

	public static final int Static_Assign = 1;

	public static final int Virtual_Assign = 2;

	public static final int Interface_Assign = 3;

	public static final int Special_Assign = 4;

	public static final int Static_Not_Assign = 5;

	public static final int Virtual_Not_Assign = 6;

	public static final int Interface_Not_Assign = 7;

	public static final int Special_Not_Assign = 8;

	public static boolean isAssignStmt(int invokeType) {
		if (Static_Assign <= invokeType && invokeType <= Special_Assign)
			return true;
		return false;
	}
	
	public static boolean isVirtualInvoke(int invokeType) {
		if (invokeType == Virtual_Assign || invokeType == Virtual_Not_Assign)
			return true;
		return false;
	}
	
	public static boolean isInterfaceInvoke(int invokeType) {
		if (invokeType == Interface_Assign || invokeType == Interface_Not_Assign)
			return true;
		return false;
	}

	public static boolean isInstanceInvoke(int invokeType) {
		if (invokeType == Static_Assign || invokeType == Static_Not_Assign)
			return false;
		return true;
	}

	public static boolean isStaticInvoke(int invokeType) {
		if (invokeType == Static_Assign || invokeType == Static_Not_Assign)
			return true;
		return false;
	}

	public static void handleInvoke(ParameterBucket pb) {
		try {
			int invokeType = pb.invokeType;
			Unit ut = pb.ut;
			HashMap<String, ArrayList<BFNode_Response>> BFTtable = pb.BFTtable;
			SootMethod sm = pb.sm;
			InvokeExpr ie = pb.ie;

			AssignStmt as = null;

			if (isAssignStmt(invokeType)) {
				as = (AssignStmt) ut;
				pb.strDest = as.getLeftOp().toString();
			} else {
				pb.strDest = null;
			}

			/*if (isInstanceInvoke(invokeType)) {
				InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
				String base = iie.getBase().toString();
				if (strDest != null) {
					TaintHelper.tryVariableTaint(strDest, base, static_pb);

					// By Byungkwon
					if (iie.getMethodRef().returnType().toString().contains("JSON"))
						ParameterBucket.set_JSON_Parent(strDest, base, static_pb);
				}
				for (Value v : iie.getArgs()) {
					String arg = v.toString();
					TaintHelper.tryVariableTaint(base, arg, static_pb);
					if (strDest != null)
						TaintHelper.tryVariableTaint(strDest, arg, static_pb);
				}
			} else {
				StaticInvokeExpr sie = (StaticInvokeExpr) ie;
				for (Value v : ie.getArgs()) {
					String arg = v.toString();
					if (strDest != null)
						TaintHelper.tryVariableTaint(strDest, arg, static_pb);
				}
			}*/

			// Not need to invoke if we have semantic model of the invoke statement
			if (ResponseSemantic.process(pb)){
				//TaintingHelper.InvokeHandling(ut, static_pb); // need to taint the corresponding variables even though the semantic model exists (BK)
				return;
			}
				
			
			// Not need to invoke if the statement is DP statement
			/*if (TaintHelper.isDpStmt(sm, ut)){
				//TaintingHelper.InvokeHandling(ut, static_pb); // need to taint the corresponding variables even though it is DP stmt (BK)
				return;
			}*/

			Set<SootMethod> SootMethodSet = null;
			SootMethod[] sootMethodArray;


			// BK For Jackson JSON parser
			/*if (ut.toString().contains("com.fasterxml.jackson.core.JsonParser: java.lang.String")) {
				static_pb.variable_type.put(strDest, "Jackson_Key");
				
				AssignStmt JacksonAs = (AssignStmt) ut;
				VirtualInvokeExpr vie = (VirtualInvokeExpr) JacksonAs.getInvokeExpr();
				static_pb.variable_type.put(vie.getBase().toString(),"Jackson_Parser");
			}*/
						
			//SootMethodSet = SearchMethodHelper.SearchMethod(static_pb);
			SootMethodSet = SearchMethodHelper.SearchMethod_New(pb, Constants.sCFG);
			sootMethodArray = SootMethodSet.toArray(new SootMethod[SootMethodSet.size()]);

			// Need to taint the corresponding variables even though there is no callee candidates (BK)
			/*if(SootMethodSet.size() == 0)
				if(Constants.invokeTaintedMethodOnlyForward)
					TaintingHelper.InvokeHandling(ut, static_pb);*/

			//ValueEntryList dest = new ValueEntryList(null);

			// TODO: need to explore all sootMethod in sootMethodArray. Currently, only the first method is explored.
			for(SootMethod sootMethod: sootMethodArray){
				if(!sootMethod.hasActiveBody())
					continue;
				
				if (pb.BT().getMethodStack().contains(sootMethod.getSignature()))
					continue;
				
				pb.BT().getMethodStack().push(sootMethod.getSignature());
				
				ParameterBucket new_pb = new ParameterBucket(pb.be, sootMethod, pb.BT());
				
				// Check whether the callee is stub method and perform taint things (BK)
				/*if(Constants.invokeTaintedMethodOnlyForward)
					if(!TaintingHelper.InvokeHandling(ut, static_pb, new_pb))
						// Not need to invoke if the callee is stub
						continue;*/
					
				
				//TaintHelper.markTaintParametersAndSeedGenerate(static_pb, new_pb);
				
				// Pre-processing for invoking
				ValueEntryTracking.PreprocessingForInvoke(pb, new_pb, ie, sootMethod, isInstanceInvoke(invokeType));
				
				/*
				 * Extract Stmt!
				 */
				/*TaintHelper.GeneratedStringStack_TaintedParameters.addLast(new HashMap<String, BFNode_Response>());
				TaintHelper.GeneratedStringStack_ReturnedSeeds.addLast(new HashMap<String, String>());
				TaintHelper.GeneratedStringStack_BaseTaint.addLast(new HashMap<String, String>());*/
				
				new_pb.be.ep_methods = pb.be.ep_methods;
				new_pb.be.responseEntry = pb.be.responseEntry;
				new_pb.be.epstmts = pb.be.epstmts;

				pb.be.ExtractStmt(new_pb);
				pb.BT().getMethodStack().pop();
				
				/*
				 * Extract Stmt Done!
				 */
				
				
				// Taint dest variable if the return value of callee was tainted (BK)
				/*if(strDest != null && new_pb.isReturnVarTainted())
					TaintingHelper.Tainting(strDest, static_pb);*/
				
				// Taint base variable if @this of callee was tainted (BK)
				/*if(new_pb.isThisTainted())
					TaintingHelper.Tainting(((InstanceInvokeExpr)ie).getBase().toString(), static_pb);*/
				
				// Post-processing for invoking
				try{
					ValueEntryTracking.PostprocessingForInvoke(pb, new_pb, ie, pb.strDest, isInstanceInvoke(invokeType));
					new_pb.clear();
				}catch (Exception e){
					System.out.println("Error! Unit: " + pb.ut.toString() + ", Method: " + pb.sm.toString());
					e.printStackTrace();
				}
				
				
				
				/*BFTTableHelper.reflectTaintParametersToBFTTable(static_pb);
				BFTTableHelper.reflectBaseTaintToBFTTable(static_pb);
				
				TaintHelper.GeneratedStringStack_TaintedParameters.removeLast();
				TaintHelper.GeneratedStringStack_ReturnedSeeds.removeLast();
				TaintHelper.GeneratedStringStack_BaseTaint.removeLast();*/
			}

			//if(strDest != null)
				//pb.varTable.setValueEntryList(strDest, dest, false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
