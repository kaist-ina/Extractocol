
package Extractocol.BufferExtractor_Response.UnitHandle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Response.Basics.BFNode_Response;
import Extractocol.BufferExtractor_Response.Basics.ParameterBucket;
import Extractocol.BufferExtractor_Response.Helper.BFTTableHelper;
import Extractocol.BufferExtractor_Response.Helper.SearchMethodHelper;
import Extractocol.BufferExtractor_Response.Helper.TaintHelper;
import Extractocol.BufferExtractor_Response.Semantic.ResponseSemantic;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import Extractocol.BufferExtractor_Request.HeapManagement.ValueEntryTracking;

public class InvokeHandler {

	// temporary null
	// 20160603 hnamkoong
	public static List<String> SemanticModel = new ArrayList<String>();;

	public static ParameterBucket static_pb;

	public static String strDest = null;

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
			static_pb = pb;

			int invokeType = static_pb.invokeType;
			Unit ut = static_pb.ut;
			HashMap<String, ArrayList<BFNode_Response>> BFTtable = static_pb.BFTtable;
			SootMethod sm = static_pb.sm;
			InvokeExpr ie = static_pb.ie;

			AssignStmt as = null;

			strDest = null;
			if (isAssignStmt(invokeType)) {
				as = (AssignStmt) ut;
				strDest = as.getLeftOp().toString();
				static_pb.strDest = strDest;
			} else {
				static_pb.strDest = null;
			}

			if (isInstanceInvoke(invokeType)) {
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
			}

			if (ResponseSemantic.process(static_pb)) {
				return;
			}

			Set<SootMethod> SootMethodSet = null;
			SootMethod[] sootMethodArray;

			if (ut.toString().contains(
					"$r4 = staticinvoke <com.pinterest.activity.board.model.CollaboratorInvite: com.pinterest.activity.board.model.CollaboratorInvite make(com.pinterest.network.json.PinterestJsonObject,java.lang.String)>($r2, $r3)")) {
				@SuppressWarnings("unused")
				int a = 5;
			}

			// For Jackson JSON parser
			// By Byungkwon
			if (ut.toString().contains("com.fasterxml.jackson.core.JsonParser: java.lang.String")) {
				static_pb.variable_type.put(strDest, "Jackson_Key");
				
				AssignStmt JacksonAs = (AssignStmt) ut;
				VirtualInvokeExpr vie = (VirtualInvokeExpr) JacksonAs.getInvokeExpr();
				static_pb.variable_type.put(vie.getBase().toString(),"Jackson_Parser");
			}
						
			SootMethodSet = SearchMethodHelper.SearchMethod(static_pb);
			sootMethodArray = SootMethodSet.toArray(new SootMethod[SootMethodSet.size()]);

			if (sootMethodArray.length == 0)
				return;

			// Generate a new pb
			// We should refine SootMethodSet (removing clinit ~ )
//			int smIdx = 0;
//			
//			for (int i = 0; i < sootMethodArray.length; i++) {
//				System.out.println(sootMethodArray[i].getSignature());
//				if (sootMethodArray[i].getSignature().equals(pb.ie.getMethodRef().getSignature())) {
//					smIdx = i;
//					break;
//				}
//			}
			
			ParameterBucket new_pb;
			
			// TODO: need to explore all sootMethod in sootMethodArray. Currently, only the first method is explored.
			new_pb = new ParameterBucket(static_pb.be, sootMethodArray[0]);
			TaintHelper.markTaintParametersAndSeedGenerate(static_pb, new_pb);

			// BK deliver JSON key list of arguments in caller to parameters in callee
			ValueEntryTracking.deliverJSONKeyListofCallertoCallee(static_pb.ie, static_pb, new_pb);
			
			// deliver valueEntryList of base to callee method
			if (isInstanceInvoke(invokeType))
				ValueEntryTracking.deliverValueEntryListofBaseVariabletoCallee(((InstanceInvokeExpr) ie).getBase().toString(), static_pb, new_pb);
			
			/*
			 * Extract Stmt!
			 */
			TaintHelper.GeneratedStringStack_TaintedParameters.addLast(new HashMap<String, BFNode_Response>());
			TaintHelper.GeneratedStringStack_ReturnedSeeds.addLast(new HashMap<String, String>());
			TaintHelper.GeneratedStringStack_BaseTaint.addLast(new HashMap<String, String>());
			
			new_pb.be.ep_methods = pb.be.ep_methods;
			new_pb.be.responseEntry = pb.be.responseEntry;
			new_pb.be.epstmts = pb.be.epstmts;

			static_pb.be.ExtractStmt(new_pb);

			/*
			 * Extract Stmt Done!
			 */
			
			// BK reflect valueEntryList of return value to the destination variable
			ValueEntryTracking.reflectValueEntryListofReturnVariable(strDest, static_pb, new_pb);
			
			// reflect valueEntryList of base variable back from callee to caller
			if (!isStaticInvoke(invokeType)){
				InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
				ValueEntryTracking.reflectValueEntryListofBaseVariable(iie.getBase().toString(), pb, new_pb);
			}
			
			if (ut.toString().contains(
					"$r5 = staticinvoke <com.pinterest.activity.board.model.CollaboratorInvite: java.util.List makeAll(com.pinterest.network.json.PinterestJsonArray)>($r4)")) {
				@SuppressWarnings("unused")
				int a = 5;
			}

			// BK (Byungkwon Choi)
			// static_pb & strDest will be changed after ExtractStmt().
			// We need to turn them back.
			static_pb = pb;
			strDest = static_pb.strDest;

			BFTTableHelper.reflectTaintParametersToBFTTable(static_pb);
			BFTTableHelper.reflectBaseTaintToBFTTable(static_pb);

//			if(strDest != null) {
//				if(TaintHelper.GeneratedStringStack_ReturnedSeeds.getLast().size() > 0) {
//					String stringValue = "";
//					for(String key : TaintHelper.GeneratedStringStack_ReturnedSeeds.getLast().keySet()) {
//						stringValue += TaintHelper.GeneratedStringStack_ReturnedSeeds.getLast().get(key);
//					}
//					
//					// ??
//					BFNode_Response bfn = new BFNode_Response();
//					bfn.makeStringBfn_Direct(stringValue);
//					ArrayList<BFNode_Response> list = new ArrayList<BFNode_Response>();
//					BFTtable.put(strDest, list);
//					list.add(bfn);
//					
//					if(!static_pb.taintVariableAndSeedPair.keySet().contains(strDest)) {
//						TaintHelper.seedGenerate(strDest, static_pb);
//					}
//				}
//			}
			
			TaintHelper.GeneratedStringStack_TaintedParameters.removeLast();
			TaintHelper.GeneratedStringStack_ReturnedSeeds.removeLast();
			TaintHelper.GeneratedStringStack_BaseTaint.removeLast();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
