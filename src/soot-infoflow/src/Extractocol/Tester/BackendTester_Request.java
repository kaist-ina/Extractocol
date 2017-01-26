
package Extractocol.Tester;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.Basics.ContextEntry;
import Extractocol.BufferExtractor_Request.Basics.ContextTable;
import Extractocol.BufferExtractor_Request.Basics.EPcontainer;
import Extractocol.BufferExtractor_Request.Basics.ParameterBucket;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapHandler;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapTreeNode;
import Extractocol.BufferExtractor_Request.HeapManagement.SourceFinder;
import Extractocol.BufferExtractor_Request.Helper.CFGSerializer;
import Extractocol.BufferExtractor_Request.Helper.FakeScene;
import Extractocol.BufferExtractor_Request.Helper.ResultPrintHandler;
import Extractocol.BufferExtractor_Request.Helper.SerEPcontainer;
import Extractocol.BufferExtractor_Request.Helper.SerializableCFG;
import Extractocol.BufferExtractor_Request.Semantic.JSON.JSONBuilder;
import Extractocol.BufferExtractor_Request.Semantic.Retrofit.retrofit_http_old;
import Extractocol.BufferExtractor_Request.UnitHandle.ExtractResponseHandler;
import Extractocol.Debugger.DebugInfo;
import Extractocol.Outputs.RequestInfoEntry;
import Extractocol.Outputs.RequestInfoList;
import Extractocol.Outputs.ReqRespInfo;
import Extractocol.Outputs.ResponseInfoList;
import Extractocol.Pairing.BuildPairJson;
import Extractocol.Pairing.SemanticAppliedList;
import soot.Body;
import soot.Unit;
import soot.jimple.Stmt;

public class BackendTester_Request
{
	public static HashSet<String> VisitTable = new HashSet<String>();
	static HashMap<String, String> idMap;
	static int id;
	static boolean done;
	static LinkedList<String> path;
	static Integer[][] EdgeTable = new Integer[2000][2000];
	
	public static String CurrentParentMethod;
	public static SerEPcontainer serEPC;
	public static EPcontainer EPC;
	public static String CurrentEntryPoint;
	public static DebugInfo DebugInfo;
	
	public static void ArgProcess(String[] args){
		int k = 0;
		Constants.serIsForward = true;
		
		while (k < args.length)
		{
			if (args[k].equalsIgnoreCase("--app"))
			{
				Constants.apkName = args[k + 1];
				k += 2;
			}
			else
				if (args[k].equalsIgnoreCase("--backward"))
				{
					Constants.serIsForward = false;
					k++;
				}
				else
					if (args[k].equalsIgnoreCase("--heapobject"))
					{
						Constants.heapobject = true;
						Constants.serIsForward = false;
						k += 2;
					}
		}
	}
	
	public static List<SerEPcontainer> InitBackward() throws Exception 
	{
		try {
			Constants.readDeobfuse(Constants.apkName);
			Constants.readignorelibrary(Constants.apkName);
			
			if (Constants.isRetrofit)
			{
				File[] files = new File("D:/new_extractocol/tools/dex2jar-2.0/dex2jar-2.0/" + Constants.apkName + "-dex2jar.src/com").listFiles();
				ArrayList<String> result = retrofit_http_old.retrofit_file_finder(files, "com.offerup");
				// retrofit method loading
				for (String retrofit_files : result)
					Constants.retrofits.addAll(retrofit_http_old.retrofitparser(retrofit_files));
				for (int i = 0; i < Constants.retrofits.size(); i++)
				{
					retrofit_http_old tt = Constants.retrofits.get(i);
					System.out.println("SUB = " + tt.suburl);
					System.out.println("Method = " + tt.methodref);
					System.out.println("query = " + tt.query);
					System.out.println("querymap = " + tt.querymap);
					System.out.println();
				}
				Constants.retrofit_ignorelist.add("<rx.android.observables.AndroidObservable: rx.Observable a(android.app.Activity,rx.Observable)>");
				System.out.println("retrofit Done");
			}
			
			System.out.println("App Name : " + Constants.apkName + "\n");
			System.out.println("Loading Call Flow Graph (Backward)...");
			CFGSerializer CFGs = new CFGSerializer();
			Constants.sCFG = CFGs.Deserialize(false);

			Constants.fsc = new FakeScene();
			if (Constants.isDiffMode)
				DebugInfoDeserialize();
			else
				Constants.DebugInfoMap = new HashMap<String, DebugInfo>();

			return CFGs.DeserializeEPC(false);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean SkipORSpecific(){
		if (Constants.SpecificEP != null)
			if (!CurrentEntryPoint.contains(Constants.SpecificEP))
				return true;
		if (!(Constants.SkipEP.isEmpty()))
			if (Constants.SkipEP.contains(CurrentEntryPoint.toString()))
				return true;
		if (Constants.SpecificDP != null)
			if (!CurrentParentMethod.equals(Constants.SpecificDP))
				return true;
		if (!(Constants.SkipDP.isEmpty()))
			if (Constants.SkipDP.contains(CurrentParentMethod.toString()))
				return true;
		
		return false;
	}
	
	public static ParameterBucket InitPB(){
		ParameterBucket pb = new ParameterBucket(Constants.sCFG.getMethodOf(CurrentEntryPoint));
		pb.ep_methods = new JSONArray();
		pb.requestEntries = new JSONArray();
		pb.requestEntry = new JSONObject();
		pb.epstmts = new JSONArray();
		
		return pb;
	}
	
	public static void InitPairingInfo(ParameterBucket pb){
		Constants.PairInfo.Add_dp();
		Constants.PairInfo.Add_dp_method(Constants.PairInfo.getCurrentSigEntry(), serEPC.getDP());
		Constants.PairInfo.Add_dp_stmt(Constants.PairInfo.getCurrentSigEntry(), serEPC.getDpStmt());
		Constants.PairInfo.Add_request_array(Constants.PairInfo.getCurrentSigEntry(), pb.requestEntries, true);
		Constants.PairInfo.Add_request_entry(pb.requestEntries, pb.requestEntry);
		
		//RequestInfoList.AddRequestInfo(null, new PairingInfoList(Constants.CurrentEP, Constants.CurrentParentMethod, null));
	}
	
	public static boolean InitEPC(){
		// EPC setting
		EPC = new EPcontainer();
		EPC.setDP(Constants.sCFG.getMethodOf(CurrentParentMethod));
		
		if (EPC.getDP() == null)
			return false;
		
		Body b = EPC.getDP().getActiveBody();
		for (Unit ut : b.getUnits())
		{
			if (ut.toString().equals(serEPC.getDpStmt()))
			{
				Stmt st = (Stmt) ut;
				EPC.setDPStmt(st);
				break;
			}
		}
		
		if (EPC.getDPStmt() == null)
		{
			System.out.println("Can not find DP Stmt!");
			return false;
		}
		
		return true;
	}
	
	public static void InitDebugInfo(){
		DebugInfo = null; // Set up debuginfo
		
		if (Constants.isDiffMode)
			Constants.DebugInfo = Constants.DebugInfoMap.get(CurrentEntryPoint);
		else
		{
			DebugInfo = new DebugInfo();
			Constants.DebugInfo = DebugInfo;
		}
	}
	
	public static void PostProcessDebugInfo(){
		if (Constants.isDebug && !Constants.isDiffMode)
		{
			Constants.DebugInfoMap.put(CurrentEntryPoint, DebugInfo);
			DebugInfo = new DebugInfo();
			Constants.DebugInfo = DebugInfo;
		}
	}
	
	public static void InitGlobalVariables(int idxDP, int idxEP){
		Constants.CurrentParentMethod = CurrentParentMethod;
		Constants.TaintFunctions = Constants.sCFG.GetTaintFunctions(CurrentParentMethod, serEPC.getDpStmt());
		Constants.EPcont = EPC;
		
		HeapHandler.GlobalBFTtable.clear();
		HeapHandler.GlobalHeapTable.clear();
		Constants.readheapresult(Constants.apkName);
		Constants.DebugSetup();
		
		Constants.VolleyUrlCandidate = "";
		Constants.method_stack = new Stack<String>(); // BK Method stack for avoiding recursive function call
		Constants.CallFlow = Constants.sCFG.GetCallFlows(CurrentEntryPoint);
		Constants.CurrentEntryPoint = CurrentEntryPoint;
		Constants.CurrentEP = CurrentEntryPoint;
		Constants.foundDPStmt = false;
		Constants.methodVisitCount = 0;
		Constants.EPnum = idxEP + 1;
		Constants.DPnum = idxDP + 1;
		Constants.segmentation = 0;
	}
	
	public static void NewRequestInfoEntry(){
		Constants.reqRespInfo = new ReqRespInfo();
	}
	
	public static void setEPOfReqInfoEntry(String EP){
		Constants.reqRespInfo.reqie.setEPorSPMethod(EP);
	}
	
	public static void KeepReqInfoEntry(){
		if(Constants.reqRespInfo.reqie.getSignature() != null)
			RequestInfoList.AddReqInfoEntry(Constants.reqRespInfo.getRequestInfoEntry());
	}
	
	public static void main(String[] args) throws Exception
	{
		try
		{
			// Process argument
			ArgProcess(args);
			
			// Initialize
			List<SerEPcontainer> eplist = InitBackward();
			System.out.println("\n\n[Serialization Mode] Backend Start! -- Backward");
			
			// Start
			for (int i = 0; i < eplist.size(); i++)
			{
				serEPC = eplist.get(i);
				CurrentParentMethod = serEPC.getDP();
				System.out.println((i + 1) + "th DP : " + CurrentParentMethod);
				
				// EPC setting
				if (!InitEPC())
					continue;
				
				for (int j = 0; j < serEPC.getEPlist().size(); j++)
				{
					CurrentEntryPoint = serEPC.getEPlist().get(j);
					System.out.println("\t" + (i + 1) + "-" + (j + 1) + "th EP : " + CurrentEntryPoint);
					
					InitDebugInfo();
					
					if (SkipORSpecific())
						continue;
					
					long start = System.currentTimeMillis();
					InitGlobalVariables(i, j);
					
					ParameterBucket pb = InitPB();
					
					// For Pairing - setting DP
					InitPairingInfo(pb);
					
					// Allocate an entry to save output of backward analysis
					NewRequestInfoEntry();
					setEPOfReqInfoEntry(CurrentEntryPoint);

					// Start analysis
					JSONBuilder jb = new JSONBuilder();
					jb.StartAnalyze(pb);
					
					// Save the output of backward analysis
					KeepReqInfoEntry();
					
					// BK We need to clear the list per loop (EP processing).
					// After processing an EP, in the list there could be some statements to which semantic model is applied.
					// The statements in the list should be cleared because they are not related to a signature that might be extracted while processing the next EP.
					Constants.SemanticMethodAndStmt.clear();
					
					// DEBUG
					if (Constants.BFT_Visualize)
					{
						// BufferedWriter file = new BufferedWriter(new
						// FileWriter("D:/new_extractocol/debuging Module/" +
						// Constants.apkName + "_"
						// + Constants.DPnum + "_" + Constants.EPnum + ".json",
						// true))
						// file.write(Constants.DEBUG_json.toJSONString());
						// file.close();
						int segment = 0;
						for (JSONArray segmentJsonarray : Constants.DEBUG_json)
						{
							StringBuilder GraphString = new StringBuilder("digraph structs { " + System.getProperty("line.separator") + " rankdir=\"LR\";");
							for (int jsonidx = 1; jsonidx < segmentJsonarray.size(); jsonidx++)
							{
								JSONObject job = (JSONObject) segmentJsonarray.get(jsonidx);
								if (job.toJSONString().equals("{}"))
									continue;
								@SuppressWarnings("unchecked")
								Set<String> keys = job.keySet();
								String Unitkey = "";
								String method = (String) job.get("sm");
								int blockno = (int) job.get("block");
								for (String a : keys)
								{
									if (!a.equals("sm") && !a.equals("block"))
									{
										Unitkey = a;
									}
								}
								// JM Add Gv record
								GraphString.append("node [shape=record];");
								GraphString.append(System.getProperty("line.separator"));
								addUnitLabel(GraphString, jsonidx, method, blockno, Unitkey);
								JSONArray unitforEntries = (JSONArray) job.get(Unitkey);
								int accumidx = 0;
								for (int bfnidx = 0; bfnidx < unitforEntries.size(); bfnidx++)
								{
									JSONObject unitEntity = (JSONObject) unitforEntries.get(bfnidx);
									String vName = (String) unitEntity.get("key");
									JSONArray bfns = (JSONArray) unitEntity.get("bfns");
									if (bfns.size() == 0)
										break;
									for (int bfnidx2 = 0; bfnidx2 < bfns.size(); bfnidx2++)
									{
										accumidx++;
										if (bfnidx2 == 0)
										{
											addBfnLabel(GraphString, (JSONObject) bfns.get(bfnidx2), jsonidx + "_" + accumidx, vName);
											GraphString.append("struct" + jsonidx + " -> " + "struct" + jsonidx + "_" + accumidx);
											GraphString.append(System.getProperty("line.separator"));
										}
										else
										{
											addBfnLabel(GraphString, (JSONObject) bfns.get(bfnidx2), jsonidx + "_" + accumidx, vName);
											int previdx = accumidx - 1;
											GraphString.append("struct" + jsonidx + "_" + previdx + " -> " + "struct" + jsonidx + "_" + accumidx);
											GraphString.append(System.getProperty("line.separator"));
										}
									}
								}
							}
							GraphString.append("}");
							File targetDir = new File(Constants.BFT_Visualize_path());
							if (!targetDir.exists())
							{
								targetDir.mkdirs();
							}
							BufferedWriter file = new BufferedWriter(new FileWriter(Constants.BFT_Visualize_path() + Constants.DPnum + "_" + Constants.EPnum + "_" + segment++ + ".txt"));
							file.write(GraphString.toString());
							file.close();
						}
					}
					VisitTable.clear();
					
					long end = System.currentTimeMillis();
					
					if (Constants.stopVisitMethod())
						System.out.println("\t stop visit method. Reached to max count " + Constants.maxMethodVisitCount);
					
					// JM Clearing ContextTable
					for (String key : ContextTable.Map.keySet())
						ContextTable.Map.put(key, new ArrayList<ContextEntry>());
					
					System.out.println("\t" + (i + 1) + "-" + (j + 1) + "th EP : " + "Runtime : " + (end - start) / 1000.0);
					System.out.println("\tMethod Visit Count : " + Constants.methodVisitCount);
					System.out.println();
					
					PostProcessDebugInfo();
					
					
					
				} /* for (int j = 0; j < serEPC.getEPlist().size(); j++) */
				if (Constants.isDebug && !Constants.isDiffMode)
					DebugInfoSerialize(Constants.DebugInfoMap);
			} /* for (int i = 0; i < eplist.size(); i++) */
			
			// BK Serializing RequestInfoList
			RequestInfoList.Serialize(RequestInfoList.lstRequestInfo);
			
			// JM Serializing RequestInfoList
			if (!Constants.heapobject)
			{
//				System.out.println("Source Finding...");
//				SourceFinder sf = new SourceFinder();
//				sf.FindSource();
//				if (Constants.isDebug)
//				{
//					System.out.println("Print HeapTree");
//					sf.PrintHeapResult();
//				}
//				System.out.println("Serialize RequestInfo");
//				RequestInfoList.Serialize(RequestInfoList.lstRequestInfo);
//				ReplaceHeapSearchResultWithSignatures();
			}
			else
			{
//				 SourceFinder sf = new SourceFinder();
//				 sf.PrintHeapResult(RequestInfoList.lstRequestInfo);
			}
			ResultPrintHandler.urlResultPrint();
			if (Constants.isDebug)
			{
				System.out.println("Count of Url : " + Constants.ResultUrls.size());
				System.out.println("Count of RequestInfoList : " + RequestInfoList.lstRequestInfo.size());
			}
			if (ExtractResponseHandler.ExtractedResponseEP())
			{
				System.out.println("Extracting response handler");
				ExtractResponseHandler.WrteEPs();
			}
			// Print Pair
			if (!Constants.heapobject)
			{
				System.out.println("Write Pairing Information");
				Constants.WriteFile(Constants.getReqPath(),Constants.PairInfo.jsonDPs());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (Constants.isDebug)
		{
			for (String ss : Constants.UncoveredCandidate)
				System.out.println("UNCOVERED Unique set : " + ss);
			for (String robo : Constants.robojuices)
				System.out.println("ROBO : " + robo);
		}
		System.out.println("exit!");
	}
	private static void ReplaceHeapSearchResultWithSignatures()
	{
		for (RequestInfoEntry Rie : RequestInfoList.lstRequestInfo)
		{
			if (Constants.ResultUrls.contains(Rie.getSignature()))
			{
				String resultUrl = Rie.getSignature();
				Constants.ResultUrls.remove(Rie.getSignature());
				for (HeapTreeNode Htn : Rie.getHeapTree().inOrderTraversal())
				{
					if (Htn.getStrResult() != null)
						resultUrl = resultUrl.replaceAll(Htn.getstrThisHeap(), Htn.getStrResult());
				}
				Constants.ResultUrls.add(resultUrl);
			}
		}
	}
	private static void addBfnLabel(StringBuilder graphString, JSONObject bfn, String idx, String vName)
	{
		// JM Auto-generated method stub
		String SootValue = (String) bfn.get("SootValue");
		String stringForUrl = (String) bfn.get("stringForUrl");
		if (SootValue != null)
			SootValue = SootValue.replaceAll("<", "#").replaceAll(">", "#").replaceAll("\\\"", "\\\\\"");
		if (stringForUrl != null)
		{
			stringForUrl = stringForUrl.replaceAll("<", "#").replaceAll(">", "#").replaceAll("\\\\", "BACKSLASH").replaceAll("\\\"", "\\\\\"");
			if (stringForUrl.equals(
					"###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)###(.*)"))
				stringForUrl = "UNKOWN ARRAY";
		}
		graphString.append("struct" + idx + " [shape=record, label=\"<f0> " + vName + "|<f1> " + SootValue + "|<f2> " + stringForUrl + "\"]");
		graphString.append(System.getProperty("line.separator"));
	}
	private static void addUnitLabel(StringBuilder graphString, int jsonidx, String method, int blockno, String unit)
	{
		if (unit.contains("lookupswitch"))
			unit = "lookupswitch";
		// JM Auto-generated method stub
		graphString.append("struct" + jsonidx + " [shape=record, label=\"" + unit.replaceAll("<", "").replaceAll(">", "").replaceAll("\\\"", "\\\\\"") + "|{<f0> "
				+ method.replaceAll("<", "").replaceAll(">", "") + " | <f1> blockno : " + blockno + "}\"]");
		graphString.append(System.getProperty("line.separator"));
	}
	public static void DebugInfoSerialize(HashMap<String, DebugInfo> DiMap) throws FileNotFoundException
	{
		String path = Constants.DebugPath();
		Kryo kryo = new Kryo();
		Output output = new Output(new FileOutputStream(path));
		kryo.writeObject(output, DiMap);
		output.close();
	}
	public static void DebugInfoDeserialize() throws FileNotFoundException
	{
		String path = Constants.DebugPath();
		Kryo kryo = new Kryo();
		Input input = new Input(new FileInputStream(path));
		@SuppressWarnings("unchecked")
		HashMap<String, DebugInfo> DiMap = kryo.readObject(input, HashMap.class);
		input.close();
		Constants.DebugInfoMap = (HashMap<String, DebugInfo>) DiMap.clone();
	}
}
