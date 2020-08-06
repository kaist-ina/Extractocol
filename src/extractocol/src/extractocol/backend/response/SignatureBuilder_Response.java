package extractocol.backend.response;

/* BufferExtractor Abstract class */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import extractocol.tester.Backend;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import extractocol.Constants;
import extractocol.backend.common.BackendThread;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.response.basics.BFNode_Response;
import extractocol.backend.response.basics.ParameterBucket;
import extractocol.backend.response.basics.BFNode_Response.JSON_TYPE;
import extractocol.backend.response.basics.BFNode_Response.VAR_TYPE;
import extractocol.backend.response.helper.BFTTableHelper;
import extractocol.backend.response.helper.BlockTravelerHelper;
import extractocol.backend.response.helper.DebugHelper;
import extractocol.backend.response.helper.TaintHelper;
import extractocol.backend.response.helper.UtilHelper;
import extractocol.backend.response.taint.TaintingHelper;
import extractocol.backend.response.unitHandle.Unit_AssignStmt;
import extractocol.backend.response.unitHandle.Unit_IdentityStmt;
import extractocol.backend.response.unitHandle.Unit_InvokeStmt;
import extractocol.backend.response.unitHandle.Unit_ReturnStmt;
import extractocol.common.outputs.BackendOutput;
import extractocol.common.outputs.backendOutputHelper.RequestInfoList;
import extractocol.common.outputs.backendOutputHelper.ResponseInfoList;
import extractocol.common.pairing.SemanticAppliedEntry;
import extractocol.common.pairing.SemanticAppliedList;
import extractocol.common.valueEntry.ValueEntryTracking;
import soot.Body;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.shimple.Shimple;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;

// For Graph Visualization

public class SignatureBuilder_Response {

	public SignatureBuilder_Response() {

	}

	private HashMap<String, Integer[]> VisitTable = new HashMap<String, Integer[]>();
	//For pairing
	public JSONArray ep_methods;
	public JSONObject responseEntry;
	public JSONArray epstmts;

	public void StartFingerprint(BackendThread _bt) throws Exception {

		ParameterBucket pb = new ParameterBucket(this, Constants.sCFG.getMethodOf(_bt.getRespEPMethod()), _bt);
		
		/*if (!Constants.sCFG.getMethodOf(Constants.CurrentEntryPoint).hasActiveBody())
			System.out.println("xxxx");
		
		TaintHelper.GeneratedStringStack_TaintedParameters.addLast(new HashMap<String, BFNode_Response>());
		TaintHelper.GeneratedStringStack_ReturnedSeeds.addLast(new HashMap<String, String>());
		TaintHelper.GeneratedStringStack_BaseTaint.addLast(new HashMap<String, String>());*/
		ExtractStmt(pb);
	}
	
	public void ExtractStmt(ParameterBucket pb) throws java.lang.Exception {
		try {
			SootMethod sm = pb.sm;
			Body b = null;
			
			// Return if the method does not have active body
			if (sm==null ||!sm.hasActiveBody())
			{	
				pb.FinalizeStmtAnalysis();
				return;
			}
			
			// Return if there exist a loop during method traveling
			/*if (VisitTable.containsKey(sm.getSignature())) {
				Constants.alreadyVisited = true;
				pb.FinalizeStmtAnalysis();
				return;
			}*/
			
			// Try to get shimple body
			try {
				// response parse needs to be shimple - 20150918 hnamkoong
				// to prevent BFTtable json object duplicate case
				b = Shimple.v().newBody(sm.getActiveBody());
			} catch (Exception e) {
				System.out.println("simple body to jimple body fail: " + sm.getSignature());
				e.printStackTrace();
				pb.FinalizeStmtAnalysis();
				return;
			}
			
			// Return if the method visit count exceeds the maximum value
			if(pb.BT().getMethodVisitCount() >= Constants.maxRespVisitCnt){
				pb.FinalizeStmtAnalysis();
				return;
			}
			
			pb.BT().increaseMethodVisitCount();
			pb.BT().increaseDepthCount();
			BriefBlockGraph Bg = new BriefBlockGraph(b);
			
			// Print for debugging
			if(Constants.isForwardDebug && !pb.BT().isMultiThread()){
				System.out.print("\t\t");
				for(int i = 0; i < pb.BT().getDepthCount(); i++)
					System.out.print("  ");
				
				if(!Constants.invokeTaintedMethodOnlyForward)
					System.out.println("+ Dep(" + String.format("%2d", pb.BT().getDepthCount()) + "), Cnt(" + pb.BT().getMethodVisitCount() + ")  " + pb.sm.toString());
				else
					System.out.println("+ Dep(" + String.format("%2d", pb.BT().getDepthCount()) + "), Cnt(" + pb.BT().getMethodVisitCount() + ")  " + pb.sm.toString() + " " + pb.getTaintedVariables().toString());
			}
			
			// BK copy the global heap table to the local heap table
			if(pb.sm.toString().equals(pb.BT().getRespEPMethod())){
				ValueEntryTracking.CopyTable(pb.heapTable, pb.BT().RRI().heapTable);
				ValueEntryTracking.CopyTable(pb.referenceTable, pb.BT().RRI().ReferenceTable);
			}

			//VisitTable.put(sm.getSignature(), new Integer[Bg.size()]);

			pb.BackEdge = BlockTravelerHelper.GetBackEdgeTable(pb.sm.toString(), Bg);
			
			// BK Allocate varTable and heapTable as the size of block graph
			pb.AllocTableArrays(Bg.size());

			// 20160603 hnamkoong - use sootmethod as below for some reason?
			// Shimple.v().newBody(sm.getActiveBody()).getMethod()
			
			// Get an ordered block list
			List<Block> orderedBlockList = BlockTravelerHelper.BlockAlignerWrapper(Bg.getHeads().get(0));

			for(Block nextBlock: orderedBlockList)
			{
				//DebugHelper.printBlockNumber(Bg.getBlocks().indexOf(nextBlock));
				
				pb.PreProcessingBeforeStmtAnalysis(nextBlock.getPreds(), Bg.getBlocks());
				
				BlockGraphTraveler(nextBlock, pb);
				
				pb.PostProcessingAfterStmtAnalysis(nextBlock, Bg, pb.BackEdge, pb.sm.toString());
			}

			//VisitTable.remove(sm.getSignature().toString());
			pb.BT().decreaseDepthCount();
			
			// BK copy the global heap table to the local heap table
			if (pb.sm.toString().equals(pb.BT().getRespEPMethod())){
				ValueEntryTracking.CopyTable(pb.BT().RRI().heapTable, pb.FinalHeapTable);
				ValueEntryTracking.CopyTable(pb.BT().RRI().ReferenceTable, pb.FinalReferenceTable);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// BK Keep a signature in the ResponseInfoList
	/*public static void keepSignatureInResponseInfoList(String result, String CurrentMethod){
		BackendOutput.reqRespInfo.respie.setSignature(result);
		
		BackendOutput.reqRespInfo.respie.setEPorSPMethod(CurrentMethod);
		BackendOutput.reqRespInfo.respie.setSemanticAppliedList(new SemanticAppliedList(Constants.SemanticMethodAndStmt));
		
		//ResponseInfoList.AddPairingInfo(result, new SemanticAppliedList(Constants.SemanticMethodAndStmt), Constants.CurrentEP, Constants.DPStmt);
		Constants.SemanticMethodAndStmt.clear();
	}*/

	private void BlockGraphTraveler(Block b, ParameterBucket pb) throws Exception {
		
		//SootMethod sm = pb.sm;
		for (Iterator<Unit> UnitintheBlock = b.iterator(); UnitintheBlock.hasNext();) {

			Unit ut = UnitintheBlock.next();

			//DebugHelper.printUnit(ut);

			pb.ut = ut;
			
			if(!pb.BT().hasFoundDPStmt()){
				CheckDPStmt(ut.toString(), pb.sm.toString(), pb);
				continue;
				/*else{
					TaintingHelper.DPHandling(ut, pb);
					continue;
				}*/
			}
			
			//if(Constants.invokeTaintedMethodOnlyForward)
				//TaintingHelper.UnitHandling(ut, pb); // for taint analysis
			
			if (ut instanceof AssignStmt) {
				Unit_AssignStmt.handleUnit(pb);
			} else if (ut instanceof IdentityStmt) {
				Unit_IdentityStmt.handleUnit(pb);
			} else if (ut instanceof ReturnStmt || ut instanceof ReturnVoidStmt) {
				Unit_ReturnStmt.handleUnit(pb);
			} else if (ut instanceof InvokeStmt) {
				Unit_InvokeStmt.handleUnit(pb);
			}
		}

 		/*for (Block tg : Bg.getSuccsOf(b)) {
			EdgeEntry[Bg.getBlocks().indexOf(b)][Bg.getBlocks().indexOf(tg)] = 0;
		}

		for (Block b2 : Bg.getBlocks()) {
			if (VisitTableEntry[Bg.getBlocks().indexOf(b2)] == 0) {

				if (BlockTravelerHelper.BlockSelector(Bg.getBlocks().indexOf(b2), Bg, sm.getSignature(), EdgeEntry,
						maniEdge, pb.BackEdge)) {
					BFSentry.add(b2);
					VisitTableEntry[Bg.getBlocks().indexOf(b2)] = 1;
				}
			}
		}*/
 	}
	
	private boolean CheckDPStmt(String ut, String method, ParameterBucket pb){
		if(!method.equals(pb.BT().getRespEPMethod()))
			return false;
		
		if(!ut.replaceAll("_[0-9]+", "").equals(pb.BT().getRespEPStmt()))
			return false;
		
		pb.BT().setFoundDPStmt(true);
		return true;
	}
	
	/*private boolean hasFoundDP(){
		return Constants.foundDPStmt;
	}*/
}
