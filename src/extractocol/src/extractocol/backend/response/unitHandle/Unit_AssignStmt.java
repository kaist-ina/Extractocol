
package extractocol.backend.response.unitHandle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.heapManagement.HeapHandler;
import extractocol.backend.response.basics.BFNode_Response;
import extractocol.backend.response.basics.ParameterBucket;
import extractocol.backend.response.helper.TaintHelper;
import extractocol.backend.response.helper.UtilHelper;
import extractocol.common.outputs.backendOutputHelper.ResponseInfoList;
import extractocol.common.pairing.SemanticAppliedEntry;
import extractocol.common.valueEntry.ValueEntryTable;
import extractocol.common.valueEntry.ValueEntryTracking;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.shimple.PhiExpr;

public class Unit_AssignStmt {

	public static void handleUnit(ParameterBucket pb) {
		Unit ut = pb.ut;
		HashMap<String, ArrayList<BFNode_Response>> BFTtable = pb.BFTtable;
		SootMethod sm = pb.sm;

		AssignStmt as = (AssignStmt) ut;

		// array case
		if (as.getLeftOp().toString().contains("[") && as.getLeftOp().toString().contains("]"))
			pb.strDest = as.getLeftOp().toString().split("\\[")[0];
		else
			pb.strDest = as.getLeftOp().toString();

		// BK Keep the variable type (left side)
		/*if (pb.variable_type.get(strDest) == null)
			pb.variable_type.put(strDest, as.getLeftOp().getType().toString());*/
		
		/** variables are tracked locally and heaps are tracked globally. **/
		pb.AssignmentStmtHandler(as, pb.sm.toString());
		//ValueEntryTracking.heapAndVarTracking(as, static_pb, false);
		
		/*if (!UtilHelper.hasNode(BFTtable, strDest)) {
			BFTtable.put(strDest, new ArrayList<BFNode_Response>());
		}*/

/*		if (TaintHelper.isDpStmt(sm, ut)) {
			TaintHelper.seedGenerate(strDest, static_pb);
		}

		if (as.getRightOp() instanceof PhiExpr) {
			handlePhi();
		}*/

		if (as.containsInvokeExpr()) {
			handleAssignInvoke(pb);
		}

		/*else {
			handleAssignNotInvoke();
		}*/
	}
	
	/*private static void handlePhi() {
		Unit ut = static_pb.ut;
		HashMap<String, ArrayList<BFNode_Response>> BFTtable = static_pb.BFTtable;

		AssignStmt as = (AssignStmt) ut;
		PhiExpr pe = (PhiExpr) as.getRightOp();

		for (Value v : pe.getValues())
		{

			String phiVar = v.toString();
			// Left argument can be in the Phi-node. We should skip
			// this case.
			if (strDest.equals(phiVar)) {
				continue;
			}

			// Left argument is being tainted if one of the parameters is
			// tainted.
			TaintHelper.tryVariableTaint(strDest, phiVar, static_pb);

			// Add phiVar (left argument) into the BFTtable
			if (!UtilHelper.hasNode(BFTtable, phiVar)) {
				BFTtable.put(phiVar, new ArrayList<BFNode_Response>());
			}

			// Set the parameters

			// phi var is not being used for now.
			// 20160604 hnamkoong
			ArrayList<BFNode_Response> list_phivar = BFTtable.get(phiVar);
			ArrayList<BFNode_Response> list_dest = BFTtable.get(strDest);

			// prevent infinite loop during
			// TODO: we have to control it carefully. (I think we should rebuild
			// the code below.)
			// By Byungkwon

			boolean bool = true;

			static_pb.Cdp.addVertaxAndEdge(phiVar, strDest);
			if (static_pb.Cdp.HasCycle()) {
				bool = false;
			}

			if (bool) {
				BFNode_Response bfn = new BFNode_Response();
				bfn.makePhinodeBfn(strDest);
				list_phivar.add(bfn);
			}

//			boolean bool = true;
//			for (BFNode_Response bfn : list_dest)
//			{
//				if (bfn.type == BFNode_Response.VAR_TYPE.VAR_TYPE_PHINODE)
//				{
//					if (bfn.stringValue.equals(phiVar))
//					{
//						bool = false;
//					}
//				}
//			}
//
//			if (bool)
//			{
//				BFNode_Response bfn = new BFNode_Response();
//				bfn.makePhinodeBfn(strDest);
//				list_phivar.add(bfn);
//			}

		}
	}*/

	private static void handleAssignInvoke(ParameterBucket pb) {
		Unit ut = pb.ut;
		AssignStmt as = (AssignStmt) ut;

		// expr & static
		if (as.getInvokeExpr() instanceof StaticInvokeExpr) {
			StaticInvokeExpr sie = (StaticInvokeExpr) as.getInvokeExpr();
			pb.invokeType = InvokeHandler.Static_Assign;
			pb.ie = sie;
			InvokeHandler.handleInvoke(pb);
		}

		// expr & virtual
		else if (as.getInvokeExpr() instanceof VirtualInvokeExpr) {

			VirtualInvokeExpr vie = (VirtualInvokeExpr) as.getInvokeExpr();
			pb.invokeType = InvokeHandler.Virtual_Assign;
			pb.ie = vie;
			InvokeHandler.handleInvoke(pb);

		}

		// expr & interface
		else if (as.getInvokeExpr() instanceof InterfaceInvokeExpr) {
			InterfaceInvokeExpr ife = (InterfaceInvokeExpr) as.getInvokeExpr();
			pb.invokeType = InvokeHandler.Interface_Assign;
			pb.ie = ife;
			InvokeHandler.handleInvoke(pb);
		}

		// expr & special
		else if (as.getInvokeExpr() instanceof SpecialInvokeExpr) {
			SpecialInvokeExpr sie = (SpecialInvokeExpr) as.getInvokeExpr();
			pb.invokeType = InvokeHandler.Special_Assign;
			pb.ie = sie;
			InvokeHandler.handleInvoke(pb);
		}
	}

	/*private static void handleAssignNotInvoke() {
		Unit ut = static_pb.ut;
		HashMap<String, ArrayList<BFNode_Response>> BFTtable = static_pb.BFTtable;
		AssignStmt as = (AssignStmt) ut;

		String cropLeftOp = UtilHelper.CropVar(as.getLeftOp().toString());
		String cropRightOp = UtilHelper.CropVar(as.getRightOp().toString());

		// left op is heap object
		if (cropLeftOp.startsWith("<") && cropLeftOp.endsWith(">")) {
			// String LeftSootValue = cropLeftOp;
			// // if $v.<> = $v.<>
			// if (cropRightOp.startsWith("<") && cropRightOp.endsWith(">"))
			// {
			// }
			// // if $v.<> = constant
			// else if (as.getRightOp() instanceof Constant)
			// {
			// }
			// // if $v.<> = $v
			// else if (BFTtable.get(as.getRightOp().toString()) != null)
			// {
			// }
			// // if $v.<> = new or ...
			// // do nothing
			// else
			// {
			// }
		} else {
			String var = as.getLeftOp().toString();
			if (var.contains("[") && var.contains("]"))
				var = var.substring(0, var.indexOf("["));

			ArrayList<BFNode_Response> list = BFTtable.get(var);
			list.clear();

			// if $v = $v.<>
			if (cropRightOp.startsWith("<") && cropRightOp.endsWith(">")) {
				String rightBase = as.getRightOp().toString().split(".<")[0];
				TaintHelper.tryVariableTaint(var, rightBase, static_pb);
			}
			// if $v = constant
			else if (as.getRightOp() instanceof Constant) {
				if (static_pb.sm.getSignature().contains("__JsonHelper")) {
					BFNode_Response bfn = new BFNode_Response();
					bfn.makeJsonBfn(as.getRightOp().toString(), null, BFNode_Response.JSON_TYPE.JSON_TYPE_STRING);
					list.add(bfn);
				} else {
					BFNode_Response bfn = new BFNode_Response();
					bfn.makeStringBfn_Indirect(as.getRightOp().toString());
					list.add(bfn);
				}
			}
			// if $v = $v
			else if (BFTtable.get(as.getRightOp().toString()) != null) {
				String rightVar = as.getRightOp().toString();
				BFTtable.put(var, BFTtable.get(rightVar));

				TaintHelper.tryVariableTaint(var, rightVar, static_pb);
			}
			// if $v = new array -- woohyun 20160330
			else if (as.getRightOp().toString().contains("newarray")) {
			}
			// if $v = $v[]
			else if (as.getRightOp() instanceof ArrayRef && as.getRightOp().toString().contains("[")
					&& !as.toString().contains("instanceof")
					&& as.getRightOp().toString().split("[(].*[)]").length < 2) {
			}
			// if $v = (java.lang.xxx) $v
			else if (as.getRightOp().toString().split("[(].*[)]").length >= 2) {
				String rightVar = as.getRightOp().toString().split("[(].*[)]")[1].substring(1);
				BFTtable.put(var, BFTtable.get(rightVar));

				TaintHelper.tryVariableTaint(var, rightVar, static_pb);
			}
			// if $v = new 
			else if(as.getRightOp() instanceof NewExpr){
				// BK keep type name 
				//static_pb.NewAssignmentHandler(as);
			}
			// or ...
			else {
			}
		}
	}*/
}
