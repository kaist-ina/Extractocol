
package extractocol.tester;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import extractocol.backend.request.helper.*;
import extractocol.common.outputs.helper.HtmlGraphDrawing;
import extractocol.common.valueEntry.PartofUrlStringTable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import extractocol.Constants;
import extractocol.backend.common.BackendThread;
import extractocol.backend.request.basics.ContextEntry;
import extractocol.backend.request.basics.ContextTable;
import extractocol.backend.request.basics.EPcontainer;
import extractocol.backend.request.basics.ParameterBucket;
import extractocol.backend.request.heapManagement.HeapHandler;
import extractocol.backend.request.heapManagement.HeapTreeNode;
import extractocol.backend.request.heapManagement.SourceFinder;
import extractocol.backend.request.helper.CFGSerializer.ICFG_CASE;
import extractocol.backend.request.semantic.json.JSONBuilder;
import extractocol.backend.request.semantic.retrofit.retrofit_http_old;
import extractocol.backend.request.semantic.url.UrlBuilder;
import extractocol.backend.request.unitHandle.ExtractResponseHandler;
import extractocol.backend.response.helper.DebugHelper;
import extractocol.common.debugger.DebugInfo;
import extractocol.common.helper.InvokeHelper;
import extractocol.common.helper.MultiThreadHelper;
import extractocol.common.outputs.BackendOutput;
import extractocol.common.outputs.backendOutputHelper.ReqRespInfo;
import extractocol.common.outputs.backendOutputHelper.ResponseInfoEntry.SEED_TYPE;
import extractocol.common.outputs.helper.BackendOutputHelper;
import extractocol.common.pairing.BuildPairJson;
import extractocol.common.retrofit.RetrofitFinalize;
import extractocol.common.retrofit.RetrofitHandle;
import extractocol.common.retrofit.utils.FileAnalyzer;
import extractocol.common.trackers.tools.ArgToVEL;
import extractocol.common.trackers.tools.HeapToVEL;
import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.frontend.MyBackwardInfoflow;
import extractocol.frontend.basic.BasicAPIs;
import extractocol.frontend.basic.ExtractocolLogger;
import extractocol.frontend.helper.PropagateHelper;
import extractocol.frontend.helper.StubMethodHandler;
import extractocol.frontend.output.TaintResultContainer;
import extractocol.frontend.output.basic.DPContainer;
import extractocol.frontend.output.basic.EPContainer;
import heros.solver.CountingThreadPoolExecutor;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;
import extractocol.frontend.basic.MyCallGraphBuilder;
import soot.Body;
import soot.Scene;
import soot.Unit;
import soot.jimple.Stmt;

public class Backend
{
	public static 	String Forward_EPMethod;
	public static String Forward_DPStmt;
	public static String CurrentParentMethod;
	public static String CurrentEntryPoint;
	public static HashSet<String> AnalyzedMethods = new HashSet<String>(); // list of methods that have been already analyzed.
	
	private static DPContainer DPC;
	private static EPContainer EPC;
	private static DebugInfo DebugInfo;
	private static DecimalFormat runtimeFormat = new DecimalFormat("##.#");
	
	private static int maxEP; // 
	private static int currentEPCnt;
	
	static ThreadPoolExecutor executor;
	
	public static void main(String[] args)
	{
		try
		{
			long analysis_start, analysis_end;
			analysis_start = System.currentTimeMillis();
			
			// Process argument
			ArgProcess(args);
			
			// Check whether the Backend is required to be running
			if(!BackendOutputHelper.needToRunBackend() && !Constants.heapobject && !ArgToVEL.isArgTracking()) {
				ExtractocolLogger.Log("Not performing Backend. ReqRespInfoList.ser already exists.");
				return;
			}

			// JimpleLoader
			// BK: Need not to retrieve active bodies again if it has been done before 
			// cgBuilt will be true when the heap handler is being performed and the frontend has been called at least once. 
			// cgBuilt is set to false as default. You don't need to care about this when running backend independently not through the heap handler.
			if(MyCallGraphBuilder.needToRetrieveActiveBodies()){
				JimpleLoader jl = new JimpleLoader(Constants.getAndroidSDKPath(), Constants.getAPKpath(args[1] + ".apk"),Constants.getSourcesAndSinksPath());
			}
			
			// Initialize
			BackendOutput.clear();
			executor = MultiThreadHelper.createExecutor(15, TimeUnit.MINUTES);
			
			long start = System.currentTimeMillis();
			List<DPContainer> dplist = InitBackward();
			if(dplist == null) {
				ExtractocolLogger.Log("Nothing to do in Backend.");
				return;
			}
			
			StubMethodHandler.readStubMapFromFile();
			long end = System.currentTimeMillis();
			
			System.out.println("Loading time: " + (end - start) / 1000 + " second");
			System.out.println("\n\nBackend Start! ( Backward & Forward )");
			System.out.println();
			
			countTotEP(dplist);

			//Added by jeongmin for drawing block graph.
			if (Constants.DrawGraph != null) {
				HtmlGraphDrawing.DrawingBlockGraph(Scene.v().getMethod(Constants.DrawGraph));
				System.exit(1);
			}
			
			// Main function
			if(Constants.backendMultiThread)
				multiThread(dplist);
			else
				singleThread(dplist);
			
			analysis_end = System.currentTimeMillis();
			System.out.println("\n** Total analysis time: " + getTimeString((analysis_end - analysis_start) / 1000.0) + "\n");
			
			if (Constants.heapobject)
			{
				getHeapValue();
				BackendOutput.clear();
				return;
			}
			
			// not need to save the other result when tracking argument
			if(ArgToVEL.isArgTracking()) {
				BackendOutput.clear();
				return;
			}
			
			// Finalize retrofit entries (for request)
			if(Constants.getIsRetrofit())
				RetrofitFinalize.Request();
			
			// De-duplicate the reqRespInfo entries
			//BackendOutput.deduplication();
			//BackendOutput.deduplication_multiThreading();
			BackendOutput.deduplication_new();
			//BackendOutput.Finalize();
			
			// Print url signatures
			ResultPrintHandler.urlResultPrint();
			ResultPrintHandler.requestResultPrint();
			
			// Save the result into file
			BackendOutputHelper.SerializeBackendOutputs(BackendOutput.getFinalReqRespInfoList());
			BackendOutput.clear();

			PartofUrlStringTable.serialize();

			if(Constants.isNotFullStack())
				System.exit(0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void singleThread(List<DPContainer> dplist) {
		for (int i = 0; i < dplist.size(); i++)
		{
			DPC = dplist.get(i);
			CurrentParentMethod = DPC.getDPMethod();
			System.out.println("\n" + (i + 1) + "th DP : " + CurrentParentMethod);
			System.out.println("\tDP Stmt: " + DPC.getDPStmt() + "\n");
			
			if (SkipORSpecificDP(DPC))
				continue;
			
			for (int j = 0; j < DPC.getEPList().size(); j++)
			{
				/*******************************************************/
				/** 1. Backward analysis (Request signature building) **/
				/*******************************************************/
				EPC = DPC.getEPList().get(j);
				CurrentEntryPoint = EPC.getEP();
				System.out.println("\t" + (i + 1) + "/" + dplist.size() + " DP - " + (j + 1) + "/" + DPC.getEPList().size() + " EP : " + CurrentEntryPoint);
				
				if (SkipORSpecificEP(EPC))
					continue;
				
				if(!doesContainSpecificTaintMethod(EPC))
					continue;
				
				increaseEPCnt();
				if(Constants.heapobject)
					if(doesReachMaxEP())
						break;
				
				BackendThread bt = new BackendThread(currentEPCnt, DPC, EPC, false);
				bt.run();
			}
		}
	}
	
	private static void multiThread(List<DPContainer> dplist) {
		int cnt = 0;
		for (int i = 0; i < dplist.size(); i++)
		{
			DPC = dplist.get(i);
			
			if (SkipORSpecificDP(DPC))
				continue;
			
			for (int j = 0; j < DPC.getEPList().size(); j++)
			{
				/*******************************************************/
				/** 1. Backward analysis (Request signature building) **/
				/*******************************************************/
				EPC = DPC.getEPList().get(j);
				
				if (SkipORSpecificEP(EPC))
					continue;
				
				if(!doesContainSpecificTaintMethod(EPC))
					continue;
				
				increaseEPCnt();
				if(Constants.heapobject)
					if(doesReachMaxEP())
						break;
				
				executor.execute(new BackendThread(currentEPCnt, DPC, EPC, true));
				cnt++;
			}
		}
		
		ExtractocolLogger.Log("Thread #: " + cnt);
		MultiThreadHelper.awaitCompletion(executor, Constants.getMaxBackendRunningTime(), Constants.getBackendTimeUnit(), 
				"The Backend will be finished within " + Constants.getMaxBackendRunningTime() +	" " + Constants.getBackendTimeUnit() + ".", 
				"Backend finished!");
	}
	
	
	/****************************************************************************/
	/***                    APIs for heap value tracking                      ***/
	/****************************************************************************/
	private static void getHeapValue()
	{
		HeapToVEL.HeapValue = new ValueEntryList(null);
		for (ReqRespInfo rri : BackendOutput.ReqRespInfoList)
		{
			ValueEntryList vel = rri.heapTable.getValueEntryListDeep(HeapToVEL.targetHeap);
			if (vel != null)
				HeapToVEL.HeapValue.addValueEntryList(vel.Clone(), false);
		}
	}
	
	/****************************************************************************/
	/***                        APIs for initialization                       ***/
	/****************************************************************************/
	private static void ArgProcess(String[] args)
	{
		int k = 0;
		Constants.serIsForward = true;
		Constants.heapobject = false;
		while (k < args.length)
		{
			if (args[k].equalsIgnoreCase("--app"))
			{
				Constants.apkName = args[k + 1];
				FileAnalyzer.setAppName(args[k + 1]);
				k += 2;
				continue;
			}
			else if (args[k].equalsIgnoreCase("--backward"))
			{
				Constants.serIsForward = false;
				k++;
				continue;
			}
			else if (args[k].equalsIgnoreCase("--heapobject"))
			{
				Constants.heapobject = true;
				k += 2;
				continue;
			}
			else if (args[k].equalsIgnoreCase("--maxms"))
			{
				PropagateHelper.setMaxMainStream(Integer.parseInt(args[k + 1]));
				k += 2;
				continue;
			}
			else if (args[k].equalsIgnoreCase("--retrofit"))
			{
				Constants.setIsRetrofit(true);
				k++;
				continue;
			}
			k++;
		}
	}
	
	private static List<DPContainer> InitBackward() throws Exception
	{
		try
		{
			Constants.readDeobfuse(Constants.apkName);
			Constants.readignorelibrary(Constants.apkName);
			
			if(Constants.getIsRetrofit())
				RetrofitHandle.TransactionMapLoad();
			
			System.out.println("App Name : " + Constants.apkName + "\n");
			System.out.println("Loading Call Flow Graph ...");
			CFGSerializer CFGs = new CFGSerializer();
			Constants.sCFG = CFGs.Deserialize(ICFG_CASE.BACKWARD); // Not need to distinguish cfg between heap/backward analysis
			
			if (Constants.isDiffMode)
				DebugInfoDeserialize();
			else
				Constants.DebugInfoMap = new HashMap<String, DebugInfo>();
			
			// create SB_request object
			//Constants.SB_Request = new JSONBuilder();
			
			// Initialize currentEPCnt to zero
			currentEPCnt = 0;
			
			// Initialize semantic models
			UrlBuilder.initSemanticModels();
			
			if (Constants.heapobject)
				return TaintResultContainer.Deserialization(ICFG_CASE.HEAP);
			else if(ArgToVEL.isArgTracking())
				return TaintResultContainer.Deserialization(ICFG_CASE.ARG);
			else
				return TaintResultContainer.Deserialization(ICFG_CASE.BACKWARD);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
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
	/*** APIs for backward processing ***/
	/****************************************************************************/
	private static boolean SkipORSpecificDP(DPContainer DPC)
	{
		if (Constants.heapobject)
			return false;
		if (Constants.SpecificDP != null)
			if (!DPC.getDPMethod().equals(Constants.SpecificDP))
				return true;
		if (!(Constants.SkipDP.isEmpty()))
			if (Constants.SkipDP.contains(DPC.getDPMethod()))
				return true;
		return false;
	}
	
	private static boolean SkipORSpecificEP(EPContainer EPC)
	{
		// Skip the EP when the EP has been already analyzed before within the same DP
		//if (AnalyzedMethods.contains(CurrentEntryPoint))
			//return true;
		if(Constants.heapobject)
			return false;
		
		if (Constants.SpecificEP != null)
			if (!EPC.getEP().contains(Constants.SpecificEP))
				return true;
		if (Constants.SpecificEPList != null)
			if (Constants.SpecificEPList.size() > 0)
				if (!Constants.SpecificEPList.contains(EPC.getEP()))
					return true;
		if (!(Constants.SkipEP.isEmpty()))
			if (Constants.SkipEP.contains(EPC.getEP()))
				return true;
		return false;
	}
	
	private static boolean doesContainSpecificTaintMethod(EPContainer EPC) {
		if(Constants.SpecificTaintMethodList == null ||
				Constants.SpecificTaintMethodList.size() == 0)
			return true;
		else {
			for(String method: Constants.SpecificTaintMethodList) {
				if(!EPC.getTaintMethodSet().contains(method))
					return false;
			}
			return true;
		}
	}
	
	
	/****************************************************************************/
	/*** APIs for forward processing ***/
	/****************************************************************************/
	public static void SetForwardEP(String EPMethod){ Forward_EPMethod = EPMethod; }
	public static void SetForwardEPStmt(String EPStmt) { Forward_DPStmt = EPStmt; }
	
	public static void InitForwardEP(String EPmethod, String EPstmt){
		Forward_EPMethod = EPmethod;
		Forward_DPStmt = EPstmt;
		
		// TODO: need to handle various type according to DP stmt (BK)
		//BackendOutput.setSeedType(SEED_TYPE.DEST);
		
		//Forward_EPMethod = "<com.android.volley.toolbox.Volley: com.android.volley.RequestQueue newRequestQueue(android.content.Context,com.android.volley.toolbox.HttpStack)>";
		
		//Forward_EPMethod = "<com.android.volley.toolbox.PinterestJsonObjectRequest: void deliverResponse(com.pinterest.e.c.d)>";
		//Forward_DPStmt = "$r1 := @parameter0: com.pinterest.e.c.d";
	}
	
	/****************************************************************************/
	/***                                   Etc                                ***/
	/****************************************************************************/
	public static String getTimeString(double t)
	{
		if (t < 60)
			return t + " seconds";
		else if (t < 3600)
		{
			int m = ((int) t / 60);
			int s = (int) t - (m * 60);
			return m + "m " + s + "s (" + runtimeFormat.format(t) + " sec)";
		}
		else
		{
			int h = ((int) t / 3600);
			int m = ((int) t - (h * 3600)) / 60;
			int s = (int) t - (h * 3600) - (m * 60);
			return h + "h " + m + "m " + s + "s (" + runtimeFormat.format(t) + " sec)";
		}
	}
	
	public static void setMaxEPInitCurrEPCnt(int m) { maxEP = m; currentEPCnt = 0; }
	//public static int getMaxEP() { return maxEP; }
	public static boolean doesReachMaxEP() { return maxEP <= currentEPCnt;}
	public static void increaseEPCnt() { currentEPCnt++; }
	
	/****************************************************************************/
	/***                        APIs for initialization                       ***/
	/****************************************************************************/
	private static void countTotEP(List<DPContainer> dplist) {
		int cnt = 0;
		
		for (DPContainer dpc: dplist)
			cnt += dpc.getEPList().size();
		
		BackendOutput.totEPCnt = cnt;
		ExtractocolLogger.Log("Total # EPs: " + cnt);
	}
}
