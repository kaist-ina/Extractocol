package Extractocol.Tester;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import Extractocol.BufferExtractor_Request.Helper.SerEPcontainer;
import Extractocol.Tester.BackendTester_Request;
import Extractocol.Tester.BackendTester_Response_Iter;


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
import Extractocol.BufferExtractor_Request.Helper.SerializableCFG;
import Extractocol.BufferExtractor_Request.Semantic.JSON.JSONBuilder;
import Extractocol.BufferExtractor_Request.Semantic.Retrofit.retrofit_http_old;
import Extractocol.BufferExtractor_Request.UnitHandle.ExtractResponseHandler;
import Extractocol.Debugger.DebugInfo;
import Extractocol.Outputs.*;
import Extractocol.Pairing.BuildPairJson;
import soot.Body;
import soot.Unit;
import soot.jimple.Stmt;

public class BackendTester {
	private static String Forward_EPMethod;
	private static String Forward_DPStmt;
	
	private static String CurrentParentMethod;
	private static SerEPcontainer serEPC;
	private static EPcontainer EPC;
	private static String CurrentEntryPoint;
	private static DebugInfo DebugInfo;
	
	public static void main(String[] args) {
		try{
			// Process argument
			ArgProcess(args);
			
			// Initialize
			List<SerEPcontainer> eplist = InitBackward();
			InitForward();
			
			System.out.println("\n\nBackend Start! ( Backward & Forward )\n");
			
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
					/**************************/
					/** 1. Backward analysis **/
					/**************************/
					CurrentEntryPoint = serEPC.getEPlist().get(j);
					
					if (SkipORSpecific())
						continue;
					
					NewReqRespInfoEntry(); // For saving outputs of back-end analysis
					setEPOfReqInfoEntry(CurrentEntryPoint);
					InitDebugInfo();
					InitGlobalVariables(i, j);
					ParameterBucket pb = InitPB();
					InitPairingInfo(pb); // For Pairing - setting DP
					
					System.out.println("\t" + (i + 1) + "-" + (j + 1) + "th EP : " + CurrentEntryPoint);
 
					// 1) Start backward analysis
					long start = System.currentTimeMillis();
					(new JSONBuilder()).StartAnalyze(pb);
					long end = System.currentTimeMillis();
					
					// 2) Print result
					System.out.println("\t\t- Backward Runtime : " + (end - start) / 1000.0 + " seconds");
					System.out.println("\t\t- Method Visit Count : " + Constants.methodVisitCount);
					
					// BK We need to clear the list per loop (EP processing).
					// After processing an EP, in the list there could be some statements to which semantic model is applied.
					// The statements in the list should be cleared because they are not related to a signature that might be extracted while processing the next EP.
					Constants.SemanticMethodAndStmt.clear();
					
					if (Constants.stopVisitMethod())
						System.out.println("\t stop visit method. Reached to max count " + Constants.maxMethodVisitCount);
					
					// JM Clearing ContextTable
					for (String key : ContextTable.Map.keySet())
						ContextTable.Map.put(key, new ArrayList<ContextEntry>());
					
					PostProcessDebugInfo();
					
					
					/*************************/
					/** 2. Forward analysis **/
					/*************************/
					// 1) build DP information
					BuildDP(CurrentParentMethod, serEPC.getDpStmt());
					
					// 2) start forward analysis
					start = System.currentTimeMillis();
					BackendTester_Response_Iter.Backend_Forward(Forward_EPMethod, Forward_DPStmt);
					end = System.currentTimeMillis();
					
					// 3) Print result
					System.out.println("\t\t- Forward Runtime : " + (end - start) / 1000.0 + " seconds");
					System.out.println("\t\t- Method Visit Count : " + Constants.methodVisitCount);
					System.out.println();

					
					/*************************/
					/**   3. Post-Process   **/
					/*************************/
					saveReqRespInfoEntry();
				}
			}			
			
			// Print url signatures
			ResultPrintHandler.urlResultPrint();
			
			// Serialize the output array into a file.
			BackendOutputHelper.SerializeBackendOutputs();
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/****************************************************************************/
	/***                        APIs for initialization                       ***/
	/****************************************************************************/
	private static void ArgProcess(String[] args){
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
	
	private static List<SerEPcontainer> InitBackward() throws Exception 
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
	
	private static void InitForward(){
		try{
			System.out.println("Loading Call Flow Graph (Forward)...");
			CFGSerializer CFGs = new CFGSerializer();
			SerializableCFG dsCFG = CFGs.Deserialize(true);
			Constants.sCFG_Forward = dsCFG;
			
			Constants.javaPath="../../tools/dex2jar-2.0/dex2jar-2.0/" + Constants.apkName + "-dex2jar/";
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static boolean InitEPC(){
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
	
	private static void InitPairingInfo(ParameterBucket pb){
		Constants.PairInfo.Add_dp();
		Constants.PairInfo.Add_dp_method(Constants.PairInfo.getCurrentSigEntry(), serEPC.getDP());
		Constants.PairInfo.Add_dp_stmt(Constants.PairInfo.getCurrentSigEntry(), serEPC.getDpStmt());
		Constants.PairInfo.Add_request_array(Constants.PairInfo.getCurrentSigEntry(), pb.requestEntries, true);
		Constants.PairInfo.Add_request_entry(pb.requestEntries, pb.requestEntry);
	}
	
	private static void InitDebugInfo(){
		DebugInfo = null; // Set up debuginfo
		
		if (Constants.isDiffMode)
			Constants.DebugInfo = Constants.DebugInfoMap.get(CurrentEntryPoint);
		else
		{
			DebugInfo = new DebugInfo();
			Constants.DebugInfo = DebugInfo;
		}
	}
	
	private static void InitGlobalVariables(int idxDP, int idxEP){
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
	
	private static ParameterBucket InitPB(){
		ParameterBucket pb = new ParameterBucket(Constants.sCFG.getMethodOf(CurrentEntryPoint));
		pb.ep_methods = new JSONArray();
		pb.requestEntries = new JSONArray();
		pb.requestEntry = new JSONObject();
		pb.epstmts = new JSONArray();
		
		return pb;
	}
	
	private static void setEPOfReqInfoEntry(String EP){
		Constants.reqRespInfo.reqie.setEPorSPMethod(EP);
	}
	
	private static void NewReqRespInfoEntry(){
		Constants.reqRespInfo = new ReqRespInfo();
	}
	
	@SuppressWarnings("unchecked")
	private static void DebugInfoDeserialize() throws FileNotFoundException
	{
		String path = Constants.DebugPath();
		Kryo kryo = new Kryo();
		Input input = new Input(new FileInputStream(path));
		HashMap<String, DebugInfo> DiMap = kryo.readObject(input, HashMap.class);
		input.close();
		Constants.DebugInfoMap = (HashMap<String, DebugInfo>) DiMap.clone();
	}
	
	
	
	
	/****************************************************************************/
	/***                     APIs for backward processing                     ***/
	/****************************************************************************/
	
	private static boolean SkipORSpecific(){
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
	
	private static void PostProcessDebugInfo(){
		if (Constants.isDebug && !Constants.isDiffMode)
		{
			Constants.DebugInfoMap.put(CurrentEntryPoint, DebugInfo);
			DebugInfo = new DebugInfo();
			Constants.DebugInfo = DebugInfo;
		}
	}
	
	
	
	/****************************************************************************/
	/***                      APIs for forward processing                     ***/
	/****************************************************************************/
	private static void BuildDP(String DPmethod, String DPstmt){
		//Forward_EPMethod = DPmethod;
		//Forward_DPStmt = DPstmt;
		
		Forward_EPMethod = "<com.contextlogic.wish.http.HttpRequest: void executeRequest()>";
		Forward_DPStmt = "$r9 = interfaceinvoke $r7.<ch.boye.httpclientandroidlib.client.HttpClient: ch.boye.httpclientandroidlib.HttpResponse execute(ch.boye.httpclientandroidlib.client.methods.HttpUriRequest,ch.boye.httpclientandroidlib.protocol.HttpContext)>($r3, $r8)";
	}
	
	
	
	/****************************************************************************/
	/***                      APIs for post-processing                     ***/
	/****************************************************************************/
	private static void saveReqRespInfoEntry(){
		Constants.ReqRespInfoList.add(Constants.reqRespInfo);
	}
	
	
	
	
}
