package Extractocol.BufferExtractor_Response;

/* BufferExtractor Abstract class */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.shimple.Shimple;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;
import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Response.Basics.BFNode_Response;
import Extractocol.BufferExtractor_Response.Basics.BFNode_Response.JSON_TYPE;
import Extractocol.BufferExtractor_Response.Basics.BFNode_Response.VAR_TYPE;
import Extractocol.BufferExtractor_Response.Basics.ParameterBucket;
import Extractocol.BufferExtractor_Response.Helper.BFTTableHelper;
import Extractocol.BufferExtractor_Response.Helper.BlockTravelerHelper;
import Extractocol.BufferExtractor_Response.Helper.DebugHelper;
import Extractocol.BufferExtractor_Response.Helper.TaintHelper;
import Extractocol.BufferExtractor_Response.Helper.UtilHelper;
import Extractocol.BufferExtractor_Response.UnitHandle.Unit_AssignStmt;
import Extractocol.BufferExtractor_Response.UnitHandle.Unit_IdentityStmt;
import Extractocol.BufferExtractor_Response.UnitHandle.Unit_InvokeStmt;
import Extractocol.BufferExtractor_Response.UnitHandle.Unit_ReturnStmt;
import Extractocol.Outputs.RequestInfoList;
import Extractocol.Outputs.ResponseInfoList;
import Extractocol.Pairing.SemanticAppliedList;
import Extractocol.Pairing.SemanticAppliedEntry;

// For Graph Visualization

public class SignatureBuilder_Response {

	public SignatureBuilder_Response() {

	}

	private HashMap<String, Integer[]> VisitTable = new HashMap<String, Integer[]>();
	//For pairing
	public JSONArray ep_methods;
	public JSONObject responseEntry;
	public JSONArray epstmts;

	public void StartFingerprint() throws Exception {

		ParameterBucket pb = new ParameterBucket(this, Constants.sCFG_Forward.getMethodOf(Constants.CurrentEntryPoint));
		TaintHelper.GeneratedStringStack_TaintedParameters.addLast(new HashMap<String, BFNode_Response>());
		TaintHelper.GeneratedStringStack_ReturnedSeeds.addLast(new HashMap<String, String>());
		TaintHelper.GeneratedStringStack_BaseTaint.addLast(new HashMap<String, String>());
		ExtractStmt(pb);
	}

	public void ExtractStmt(ParameterBucket pb) throws java.lang.Exception {
		try {
			SootMethod sm = pb.sm;

			if (sm==null ||!sm.hasActiveBody())
			{
				return;
			}

			Body b = sm.getActiveBody();

			try {
				// response parse needs to be shimple - 20150918 hnamkoong
				// to prevent BFTtable json object duplicate case
				b = Shimple.v().newBody(sm.getActiveBody());
			} catch (Exception e) {
				System.out.println("simple body to jimple body fail");
				e.printStackTrace();
				return;
			}

			BriefBlockGraph Bg = new BriefBlockGraph(b);
			DebugHelper.depthCount++;
			
			if(Constants.isForwardDebug){
				System.out.print("\t\t");
				for(int i = 0; i < DebugHelper.depthCount; i++)
					System.out.print("  ");
				
				System.out.println("+ Dep(" + String.format("%2d", DebugHelper.depthCount) + "), Method: " + pb.sm.toString());
			}
			
			DebugHelper.printSM(pb);
			
			Block bl = Bg.getHeads().get(0);
			Integer[] VisitEntry = new Integer[Bg.size()];
			Integer[][] original_EdgeTable = new Integer[Bg.size()][Bg.size()];
			Integer[][] EdgeEntry = new Integer[Bg.size()][Bg.size()];

			Arrays.fill(VisitEntry, 0);
			for (int i = 0; i < Bg.size(); i++) {
				for (int j = 0; j < Bg.size(); j++) {
					original_EdgeTable[i][j] = 0;
					EdgeEntry[i][j] = 0;
				}
			}

			VisitEntry[Bg.getBlocks().indexOf(bl)] = 1;
			if (VisitTable.containsKey(sm.getSignature())) {
				Constants.alreadyVisited = true;
				return;
			}

			VisitTable.put(sm.getSignature(), VisitEntry);

			BlockTravelerHelper.CalcEdgeTable(EdgeEntry, Bg);
			BlockTravelerHelper.CalcEdgeTable(original_EdgeTable, Bg);

			// System.out.println("ManipulateBackEdge...");
			Integer[][] maniEdge = BlockTravelerHelper.ManipulateBackEdge(original_EdgeTable, Bg);
			BlockTravelerHelper.ManipulateBackEdge(EdgeEntry, Bg);
			// System.out.println("ManipulateBackEdge done");

			LinkedList<Block> BFSentry = new LinkedList<Block>();

			BFSentry.add(bl);

			// 20160603 hnamkoong - use sootmethod as below for some reason?
			// Shimple.v().newBody(sm.getActiveBody()).getMethod()
			while (!BFSentry.isEmpty()) {
				Block nextBlock = BFSentry.getFirst();
				DebugHelper.printBlockNumber(Bg.getBlocks().indexOf(nextBlock));

				BlockGraphTraveler(Bg, nextBlock, BFSentry, VisitTable.get(sm.getSignature()), EdgeEntry, maniEdge, pb);
				BFSentry.remove(nextBlock);
			}

			VisitTable.remove(sm.getSignature().toString());

			HashMap<String, String> taintVariableAndSeedPair = pb.taintVariableAndSeedPair;
			Set<String> taintParameters = pb.taintParameters;

			if(sm.toString().contains("<com.pinterest.activity.board.model.CollaboratorInviteFeed: void <init>(com.pinterest.network.json.PinterestJsonObject,java.lang.String,com.pinterest.api.model.Board,boolean)>")) {
				int a =1;
			}
			
			
			Set<String> seedSet = new HashSet<String>(taintVariableAndSeedPair.values());
			if (seedSet != null && seedSet.size() > 0) {
				for (String seed : seedSet) {
					boolean flag = true;
					
//					if (TaintHelper.GeneratedStringStack_ReturnedSeeds.getLast().containsKey(seed)) {
//						flag = false;
//						String result = TaintHelper.generateStringForSeed(seed, pb);
//						if (result != null && result != "") {
//							TaintHelper.GeneratedStringStack_ReturnedSeeds.getLast().put(seed, result);
//						}
//					}
					
					if(seed.equals("@this")) {
						flag = false;
						String result = TaintHelper.generateStringForSeed(seed, pb);
						if (result != null && result != "") {
							TaintHelper.GeneratedStringStack_BaseTaint.getLast().put(seed, result);
						}
					}
					
					else if (taintParameters.contains(seed)) {
						flag = false;
						String result = TaintHelper.generateStringForSeed(seed, pb);
						if (result != null && result != "") {
							if(TaintHelper.doesRootsetIncludeJSONNode(seed, pb))
								// Case 1: when the tainted parameter includes JSON object
								TaintHelper.GeneratedStringStack_TaintedParameters.getLast().put(seed, new BFNode_Response(result, VAR_TYPE.VAR_TYPE_JSON, JSON_TYPE.JSON_TYPE_FINAL) );
							else
								// Case 2: when the tainted parameter includes NO JSON object
								TaintHelper.GeneratedStringStack_TaintedParameters.getLast().put(seed, new BFNode_Response(result, VAR_TYPE.VAR_TYPE_STRING));
						}
					}
					
					// Force printing Jackson variable
					// By Byungkwon
					for (String tp : taintParameters){
						
						if(pb.variable_type.get(tp) != null){
							// Check whether the parent of the jackson variable is the current seed
							if(pb.variable_type.get(tp).equals("Jackson_Key")){
								// Print string generated if it is true
								if(taintVariableAndSeedPair.get(tp).toString().equals(seed)){
									flag = true;
									break;
								}
							}
						}
					}
					
					if(flag || pb.sm.toString().equals(Constants.CurrentEntryPoint)) {
						System.out.println("\tSeed : " + seed);
						System.out.println("\tRootSet : " + BFTTableHelper.getRootVarSetFromSeed(seed, pb));
						String result = TaintHelper.generateStringForSeed(seed, pb);
						if (!result.equals("")) {
							System.out.println();
							//System.out.println("\t\t" + sm.getSignature());
							System.out.println("\t\t[response] " + result);
							System.out.println();
							Constants.PairInfo.Add_uri(pb.be.responseEntry, result, false);
							
							// Save the result
							keepSignatureInResponseInfoList(result, pb.sm.toString());
							
							//Constants.reqRespInfo.respie.EPorSPMethod();
						}
					}
				}
			}
			
			// BFTTable print - debug
			DebugHelper.printBFTTable(pb);
			
			DebugHelper.depthCount--;

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// BK Keep a signature in the ResponseInfoList
	public static void keepSignatureInResponseInfoList(String result, String CurrentMethod){
		Constants.reqRespInfo.respie.setSignature(result);
		
		Constants.reqRespInfo.respie.setEPorSPMethod(CurrentMethod);
		Constants.reqRespInfo.respie.setSemanticAppliedList(new SemanticAppliedList(Constants.SemanticMethodAndStmt));
		
		//ResponseInfoList.AddPairingInfo(result, new SemanticAppliedList(Constants.SemanticMethodAndStmt), Constants.CurrentEP, Constants.DPStmt);
		Constants.SemanticMethodAndStmt.clear();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void BlockGraphTraveler(BriefBlockGraph Bg, Block b, LinkedList<Block> BFSentry, Integer[] VisitTableEntry,
			Integer[][] EdgeEntry, Integer[][] maniEdge, ParameterBucket pb) throws Exception {
		
		SootMethod sm = pb.sm;
		for (Iterator<Unit> UnitintheBlock = b.iterator(); UnitintheBlock.hasNext();) {

			Unit ut = UnitintheBlock.next();

			DebugHelper.printUnit(ut);

			pb.ut = ut;
			
			if(pb.ut.toString().contains("objectFromJson")) {
				@SuppressWarnings("unused")
				int a =5;
			}

			if (ut instanceof AssignStmt) {
				Unit_AssignStmt.handleUnit(pb);
			} else if (ut instanceof IdentityStmt) {
				Unit_IdentityStmt.handleUnit(pb);
			} else if (ut instanceof ReturnStmt) {
				Unit_ReturnStmt.handleUnit(pb);
			} else if (ut instanceof InvokeStmt) {
				Unit_InvokeStmt.handleUnit(pb);
			}
		}

 		for (Block tg : Bg.getSuccsOf(b)) {
			EdgeEntry[Bg.getBlocks().indexOf(b)][Bg.getBlocks().indexOf(tg)] = 0;
		}

		for (Block b2 : Bg.getBlocks()) {
			if (VisitTableEntry[Bg.getBlocks().indexOf(b2)] == 0) {

				if (BlockTravelerHelper.BlockSelector(Bg.getBlocks().indexOf(b2), Bg, sm.getSignature(), EdgeEntry,
						maniEdge)) {
					BFSentry.add(b2);
					VisitTableEntry[Bg.getBlocks().indexOf(b2)] = 1;
				}
			}
		}
	}
}
