
package extractocol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;

import extractocol.backend.request.SignatureBuilder_Request;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.heapManagement.HeapHandler;
import extractocol.backend.request.helper.SerializableCFG;
import extractocol.backend.request.semantic.json.JSONBuilder;
import extractocol.backend.request.semantic.retrofit.retrofit_http_old;
import extractocol.backend.request.semantic.url.UrlBuilder;
import extractocol.common.debugger.DebugInfo;
import extractocol.common.pairing.BuildPairJson;
import extractocol.common.pairing.PairingDPEntry;
import extractocol.common.pairing.SemanticAppliedEntry;
import extractocol.common.trackers.tools.ArgToVEL;
import extractocol.common.valueEntry.ValueEntry;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Constant;
import soot.jimple.InstanceInvokeExpr;
import extractocol.frontend.output.basic.DPContainer;
import extractocol.frontend.slice.FlowsensitiveSlice;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.source.ISourceSinkManager;

public class Constants
{
	// The annotation /*boolean*/ means the default value when you push. (by BK)
	
	/***************************************/
	/***             Front-end           ***/
	/***************************************/
	/*(Debug)*/public static boolean printRawResult = false/*false*/; // Print Frontend result before merging
	/*(Debug)*/public static boolean printResultOfFrontend = false/*false*/; // Print final Frontend result
	
	public static int maxTRE = 100000; // limit of # TRE
	public static int maxFrontendRunningTime = 30; // timeout of frontend
	public static TimeUnit frontendTimeUnit = TimeUnit.MINUTES;
	public static int maxMergingTime = 5; // timeout of merging
	public static TimeUnit mergingTimeUnit = TimeUnit.HOURS; 
	
	public static int DPStride = 1/*1*/;
	public static boolean mergingMultiThread = true/*true*/;
	public static boolean SerializeJimpleBeforeTaintAnalysis = false/*false*/; // Generating Jimple before front-end taint analysis (JM) Caution: If this var is true, Extractocol will be terminated after jimplifying.
	public static boolean serializeJimpleShimple = true/*true*/; // Generate Jimple & Shimple files
	public static boolean coloredOutputFrontend = false/*false*/; // Print colorfully
	/***************************************/
	
	/***************************************/
	/***              Back-end           ***/
	/***************************************/
	/*(Debug)*/public static boolean isBackwardDebug = false/*false*/; // Print invoked methods when building request signatures
	/*(Debug)*/public static boolean isForwardDebug = false/*false*/; // Print invoked methods when building response signatures
	public static boolean printTaintMethods = false/*false*/; // Print taint methods when printing the result
	
	public static int maxMethodVisitCount = 2000;
	public static int maxRespVisitCnt = 20000; // Stopping response signature building when # of invoked methods exceeds this value
	public static int maxBackendRunningTime = 10; // timeout of backend
	public static TimeUnit backendTimeUnit = TimeUnit.HOURS; 

	public static boolean backendMultiThread = true/*true*/; // If you want to debug, it should be false.
	public static boolean stopwhenfindingDPStmt = true/*true*/; // Stopping request signature building when the DP stmt has been found
	public static boolean searchMethodFilterUsingTaintFunction = true/*true*/; // Invoking tainted methods only when building request signatures
	public static boolean invokeTaintedMethodOnlyForward = false/*false*/; // Invoking tainted methods only when building response signatures
	/***************************************/
	
	/***************************************/
	/***               Others            ***/
	/***************************************/
	public static boolean ArgToVEL_OneByOne = false/*false*/;
	/***************************************/
	
	
	/*************************************************************************/
	/***    For backend debugging (MUST be null or empty when pushing)     ***/
	/*************************************************************************/
	public static List<String> SpecificEPList =  Arrays.asList(
			/*"<com.contextlogic.wish.ui.fragment.filterfeed.FilterFeedFragment: void unwishProduct(com.contextlogic.wish.api.data.WishProduct)>",
			"<com.contextlogic.wish.ui.fragment.filterfeed.FilterFeedFragment: void wishForProduct(com.contextlogic.wish.api.data.WishProduct)>",
			"<com.contextlogic.wish.ui.fragment.filterfeed.FilterFeedFragment: void handleResume()>",
			
			"<com.contextlogic.wish.ui.fragment.productfeed.ProductFeedFragment: void handleScrollLoad(int,int,int)>",
			"<com.contextlogic.wish.ui.fragment.filterfeed.FilterFeedProductView: void handleLoadingProductCollections(java.util.ArrayList,boolean,int)>"*/
			);
	public static String SpecificEP= null;
	
	public static String SpecificDP = null; /*null*/ // "<com.cheerfulinc.flipagram.activity.comment.FlipagramCommentsActivity: void onCreate(android.os.Bundle)>";
	
	public static List<String> SpecificTaintMethodList =  Arrays.asList(
			/*"<com.mcdonalds.sdk.connectors.middleware.helpers.MWNutritionConnectorHelper: void getAllRecipesForCategory(java.lang.String,com.mcdonalds.sdk.AsyncListener)>",
			"<com.mcdonalds.sdk.connectors.middleware.request.MWNutritionGetCategoryDetailsRequest: void <init>(java.lang.String,java.lang.String)>",
			"<com.mcdonalds.sdk.services.network.GsonRequest: void <init>(android.content.Context,int,com.mcdonalds.sdk.services.network.RequestProvider,com.mcdonalds.sdk.AsyncListener)>",
			"<com.mcdonalds.sdk.services.network.RequestProvider: java.lang.String getURLString()>"*/
			);
	
	public static List<String> SkipEP = Arrays.asList();
	public static List<String> SkipDP = Arrays.asList();
	/***************************************/
	
	/***************************************/
	/***         For lazy semantic       ***/
	/***************************************/
	public static boolean flowsensitive = true;
	public static boolean LazySemantic = true;
	/***************************************/
	
	
	
	
	public enum FRONTEND_DIRECTION {BACKWARD, FORWARD}
	public static FRONTEND_DIRECTION fd = FRONTEND_DIRECTION.BACKWARD;
	
	public static void setFrontendForward() { fd = FRONTEND_DIRECTION.FORWARD; }
	public static void setFrontendBackward() { fd = FRONTEND_DIRECTION.BACKWARD; }
	public static boolean isFrontendBackward() { return fd == FRONTEND_DIRECTION.BACKWARD; }
	public static boolean isFrontendForward() { return !isFrontendBackward(); }
	
	//public static SignatureBuilder_Request SB_Request;
	public static Set<String> EPSet = new HashSet<String>(); // To avoid analysis of other EP in response signature building (Added by BK)
	//public static Stack<String> method_stack;
	public static Set<SootMethod> callflow;
	public static ISourceSinkManager sourcesSinks;
	public static IInfoflowCFG iCfg = null;
	public static Set<SootMethod> allmethods;
	public static Set<SootMethod> taintset;
	public static Map<Integer, int[]> line_deep = new HashMap<Integer, int[]>();
	public static int id = 0;
	public static HashMap<String, ArrayList<ValueEntry>> GlobalHeapTable; // Tracking values & JSON key list of heap & variable 
	public static String RunnableCalss;
	public static Scene globalScene;
	public static boolean isScene = false;
	/* from Constants 2 */
	public static boolean isLinear = true;
	public static String targetstring = "product_select";
	public static ArrayList<String> SM_what_you_want = new ArrayList<String>();
	public static Set<String> UrlRelatedObject = new HashSet<String>();
	public static String PairSourceSink = "SourceSink_";
	public static String PairSourceSinkNo = "300";
	// for Callgraph Analysis
	public static boolean isCallGraphAnalysis = true;
	// Search Method
	// Extractocol Model Flag
	public static boolean isDebug = false;
	
	public static boolean isDiffMode = false;
	public static boolean isPairing = false;
	public static boolean removeWrongEp = false;
	public static HashMap <String, DebugInfo> DebugInfoMap = null;
	public static int MaxTaintCount = 1;
	public static int TaintCount = 0;
	public static FlowsensitiveSlice currentbr;
	public static boolean SerilizeJimple = false;
	
    public static String DrawGraph = null;
    private static boolean isFullStackRunning = false;
    
    public static boolean isFullStack() { return isFullStackRunning; }
    public static boolean isNotFullStack() { return !isFullStackRunning; }
    public static void setIsFullStack(boolean b) { isFullStackRunning = b; }
    
    public static int getMaxFrontendRunningTime() { return maxFrontendRunningTime; }
    public static TimeUnit getFrontendTimeUnit() { return frontendTimeUnit; }
    public static int getMaxBackendRunningTime() { return maxBackendRunningTime; }
    public static TimeUnit getBackendTimeUnit() { return backendTimeUnit; }
    

    public static void DebugSetup()
	{
		SM_what_you_want.add("com.hulu.thorn.services.i: void g()");
		SM_what_you_want.add("com.hulu.thorn.services.a: void a(com.hulu.thorn.services.i)");
		SM_what_you_want.add("com.hulu.thorn.services.b: java.lang.Object doInBackground(java.lang.Object[])");
		SM_what_you_want.add("com.hulu.thorn.services.b: java.lang.Void a(java.lang.Void[])");
		SM_what_you_want.add("com.hulu.thorn.services.d: java.lang.Object b()");
		SM_what_you_want.add("com.hulu.thorn.services.beacons.d: void run()");
		SM_what_you_want.add("com.hulu.thorn.util.v: com.hulu.thorn.services.e a(com.hulu.thorn.services.g)");
		SM_what_you_want.add("com.hulu.thorn.util.v: com.hulu.thorn.services.e a(com.hulu.thorn.services.g,int)");
		SM_what_you_want.add("com.hulu.thorn.services.g: java.net.HttpURLConnection k()");
	}
	// retrofit results
	public static ArrayList<retrofit_http_old> retrofits = new ArrayList<retrofit_http_old>();
	public static ArrayList<String> retrofit_ignorelist = new ArrayList<String>();
	public static boolean isRetrofitDP = false;
	public static boolean isRetrofit = false;
	public static boolean isRxJavaDP = false;
	// Rxjava DP
	public static int pathidx;
	public static ArrayList<String> path;
	public static ArrayList<String> retrofitDP = new ArrayList<String>();
	//public static boolean alreadyVisited = false;
	// before jump to sm
	//public static boolean BFTResultAlreadyApplied = false; // True if 'isSemantic' is true and it does not need to reflect BFT table output 
	public static boolean isCallee;
	public static Set<String> ResultUrls = new HashSet<String>();
	public static boolean Jimplify = true;
	//public static EPcontainer EPcont;
	//public static DPContainer DPCont;
	//public static String CurrentEP;
	//public static String DPStmt;
	public static String RealContextInthisObj = null;
	public static JSONBuilder jb;
	public static Set<String> robojuices = new HashSet<String>();
	public static ArrayList<String> stacktrace = new ArrayList<String>();
	// For specific EP

	
	public static String SpecificHeap = null;
	
	// For Visualization
	public static boolean GraphVisualization = true;
	// For Param
	public static boolean isFirstParam = true;
	// For serialization
	// setup
	public static boolean Serializing = true;
	public static boolean heapobject = false; // MUST be false as a default
	// backendTester on
	public static boolean serializationMode = false;
	public static boolean serIsForward;
	public static String serializationDirName = "../../SerializationFiles";
	public static String apkName;
	public static String VolleyUrlCandidate;
	public static String VolleyQueryCandidate = null;
	public static String CodeInjectDPsName = "DPs.txt";
	
	// line separator (line break)
	public static String LINE_SEPARATOR = System.getProperty("line.separator");
	
	public static boolean setColoredOutput() {
		boolean old = coloredOutputFrontend;
		coloredOutputFrontend = true;
		return old;
	}
	
	public static boolean setMonochromeOutput() {
		boolean old = coloredOutputFrontend;
		coloredOutputFrontend = false;
		return old;
	}
	
	public static void setOutputColor(boolean b) { coloredOutputFrontend = b; } 
	
	public static String getMultiDexName(String basePath, int i){
		File directory = new File(basePath + "/[MultiClasses]");
		if(!directory.exists())
			directory.mkdir();
		
		return "/[MultiClasses]/" + apkName + "_classes" + i + ".dex";
	}
	
	public static String getAndroidSDKPath(){
		return "../../AndroidSDK";
	}
	
	public static boolean getIsRetrofit() { return isRetrofit; }
	public static void setIsRetrofit(boolean b) { isRetrofit = b; }
	public static boolean setIsRetrofitFalse() {
		boolean old = isRetrofit;
		isRetrofit = false;
		return old;
	}
	
	public static String getOriginalDPFilePath() {
		if(isRetrofit)
			return getRetrofitDPPath();
		else
			return getSourcesAndSinksPath();
	}
	
	public static String getDPFilePath() {
		// Case 1: heap object/Arg tracking
		if(heapobject || ArgToVEL.isArgTracking())
			return getGeneralDPFilePath();
		// Case 2: Temp DP
		else
			return Constants.getTempDPPath();
	}
	
	public static String getSourcesAndSinksPath(){
		String path = "../../DemarcationPoint/" + apkName + ".txt";
		if(new File(path).exists())
			return path;
		else
			return getGeneralDPFilePath();
	}
	
	public static String getGeneralDPFilePath() {
		return "../../DemarcationPoint/[general].txt";
	}
	
	public static String jimplePath()
	{
		File serializationDir = new File(serializationDirName);
		if (!serializationDir.exists())
		{
			serializationDir.mkdir();
		}
		return serializationDirName + "/" + apkName + "/"
				+ "jimple"; /*
							 * you must not add / at the end of the path -
							 * hnamkoong 20150909
							 */
	}
	
	public static String getPartialDPListDirPath() {
		String path = getFrontendOutputDirPath() + "/PartialDPLists";
		
		File serializationDir = new File(path);
		if (!serializationDir.exists())
			serializationDir.mkdir();
		
		return path;
	}
	
	public static String getPartialRRIDirPath() {
		String path = getPermanentDirPath() + "/PartialRRI";
		
		File serializationDir = new File(path);
		if (!serializationDir.exists())
			serializationDir.mkdir();
		
		return path;
	}
	
	
	
	public static String getTempDirPath()
	{
		String path = getDataDirPath() + "/temp";
		
		File serializationDir = new File(path);
		if (!serializationDir.exists())
			serializationDir.mkdir();
		
		return path;
	}
	
	public static String getAppDirPath() {
		String path = serializationDirName + "/" + apkName;
		
		File serializationDir = new File(path);
		if (!serializationDir.exists())
			serializationDir.mkdir();
		
		return path;
	}
	
	public static String getDataDirPath() {
		String path = getAppDirPath() + "/data";
		
		File serializationDir = new File(path);
		if (!serializationDir.exists())
			serializationDir.mkdir();
		
		return path;
	}
	
	public static String RequestInfoPath()
	{
		return getBackendOutputDirPath() + "/RequestInfoList.ser";
	}
	
	public static String ResponseInfoPath()
	{
		File serializationDir = new File(serializationDirName);
		if (!serializationDir.exists())
		{
			serializationDir.mkdir();
		}
		
		return serializationDirName + "/" + apkName + "/" + "/ResponseInfoList.ser";
	}
	
	public static String shimplePath()
	{
		File serializationDir = new File(serializationDirName);
		if (!serializationDir.exists())
		{
			serializationDir.mkdir();
		}
		return serializationDirName + "/" + apkName + "/" + "shimple";
	}
	
	public static String CodeInjectPath()
	{
		String codeInjectPath = serializationDirName + "/" + apkName + "/" + "CodeInject";
		
		File serializationDir = new File(serializationDirName);
		if (!serializationDir.exists())
			serializationDir.mkdir();
		
		serializationDir = new File(codeInjectPath);
		if (!serializationDir.exists())
			serializationDir.mkdir();
		
		return codeInjectPath;
	}
	
	public static String getBasePath(){
		String path = serializationDirName + "/" + apkName;
		File serializationDir = new File(path);
		
		if (!serializationDir.exists())
			serializationDir.mkdir();
		
		return path;
	}
	
	public static String getImplicitCallEdgeOutputDirPath() {
		return makeDir(getDataDirPath() + "/1.ImplicitCallEdge");
	}
	public static String getRetrofitOutputDirPath() {
		return makeDir(getDataDirPath() + "/2.Retrofit");
	}
	public static String getIntentMapOutputDirPath() {
		return makeDir(getDataDirPath() + "/3.IntentMap");
	}
	public static String getFrontendOutputDirPath() {
		return makeDir(getDataDirPath() + "/4.Frontend");
	}
	public static String getBackendOutputDirPath() {
		return makeDir(getDataDirPath() + "/5.Backend");
	}
	public static String getHeapHandlerOutputDirPath() {
		return makeDir(getDataDirPath() + "/6.HeapHandler");
	}
	
	public static String makeDir(String path) {
		File serializationDir = new File(path);
		if (!serializationDir.exists())
			serializationDir.mkdir();
		return path;
	}
	
	public static String getPermanentDirPath()
	{
		String path = getDataDirPath() + "/Permanent";
		
		File serializationDir = new File(path);
		if (!serializationDir.exists())
			serializationDir.mkdir();
		
		return path;
	}
	
	public static String getTempDPPath() {
		return getTempDirPath() + "/tempDP.txt";
	}

	public static String getPartofUrlStringPath(){
		return getDataDirPath() + "/" + "PartofUrlStrings.ser";
	}
	
	public static String getAPPName() { return apkName; }
	public static void setAPPName(String appName) { apkName = appName; }
	
	public static String CodeInjectDPsName()
	{
		return CodeInjectDPsName;
	}
	// backward serialization
	public static String SerPath()
	{
		return getFrontendOutputDirPath() + "/" + apkName + ".iCfg";
	}
	
	public static String getRetrofitDPPath() {
		return getRetrofitOutputDirPath() + "/retrofitDP.txt";
	}
	
	public static String getJavaDirPath() {
		return serializationDirName + "/" + apkName + "/java";
	}
	
	public static String getRetrofitTranMapFilePath() {
		return getRetrofitOutputDirPath() + "/retrofitTranMap.ser";
	}
	
	public static String DebugPath()
	{
		return serializationDirName + "/" + apkName + "/" + "debug.ser";
	}
	public static String EPContainorPath()
	{
		return serializationDirName + "/" + apkName + "/" + apkName + "-epc.txt";
	}
	public static String EPContainorPath_Heap()
	{
		return serializationDirName + "/" + apkName + "/" + apkName + "-epc-heap.txt";
	}
	public static String GraphPath()
	{
		return serializationDirName + "/" + apkName + "/" + apkName + "-graphs/";
	}
	// forward serialization
	public static String SerPath_forward()
	{
		return serializationDirName + "/" + apkName + "/" + apkName + "-forward.iCfg";
	}
	public static String SerPath_heap()
	{
		return serializationDirName + "/" + apkName + "/" + apkName + "-heap.iCfg";
	}
	public static String EPContainorPath_forward()
	{
		return serializationDirName + "/" + apkName + "/" + apkName + "-epc-forward.txt";
	}
	public static String UrlRelatedHeapPath()
	{
		return serializationDirName + "/" + apkName + "/" + apkName + "-heapobject.ser";
	}
	public static String getAPKpath(String APK)
	{
		return serializationDirName + "/" + APK;
	}
	public static String BFT_Visualize_path()
	{
		return serializationDirName + "/" + apkName + "/" + "BFT_Visual" + "/";
	}
	public static String GetForwardEP_path()
	{
		return serializationDirName + "/" + apkName + "/" + "ForwardEPs.txt";
	}
	// For Deserialization
	public static SerializableCFG sCFG;
	public static SerializableCFG sCFG_Forward;
	//public static String CurrentEntryPoint;
	//public static String CurrentParentMethod;
	//public static List<String> TaintFunctions;
	
	//This field is not used.
	public static ArrayList<String> CallFlow;
	
	/* from Constants 3 */
	public static void log(Object line)
	{
		System.out.println(line);
	}
	public static String[] searchMethodFilter =
	{
			// basic android https
			"<org.apache.http.message.AbstractHttpMessage: void <init>()>",
			"<org.apache.http.client.methods.HttpRequestBase: void <init>()>",
			"<org.apache.http.client.methods.HttpPost: void <init>(java.lang.String)>",
			"<org.apache.http.client.methods.HttpGet: void <init>(java.lang.String)>",
			"<org.apache.http.client.HttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>",
			"<org.apache.http.impl.client.AbstractHttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>",
			"<org.apache.http.impl.client.DefaultHttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>",
			"<java.net.URL: void <init>(java.lang.String)>",
			"<org.codehaus.jackson.map.ObjectMapper: java.lang.Object readValue(java.io.InputStream,java.lang.Class)>" };
	public static HashMap<String, PairingDPEntry> pairingInfoContainer = new HashMap<String, PairingDPEntry>();
	//public static boolean foundDPStmt;
	public static Unit target;
	public static long starttime;
	public static boolean istimestart = false;
	//public static int methodVisitCount;
	public static int EPnum;
	public static int DPnum;
	public static ArrayList<JSONArray> DEBUG_json;
	 
	public static final boolean BFT_Visualize = false;

	//Pairing Information
	public static BuildPairJson PairInfo = new BuildPairJson();
	
	// The list 'SemanticMethodAndStmt' keeps statements to which a semantic model is applied.
	// The list also keeps the corresponding method.
	// The list can be used for both backward and forward analysis.
	// Added by Byungkwon
	public static ArrayList<SemanticAppliedEntry> SemanticMethodAndStmt = new ArrayList<SemanticAppliedEntry>();
	
	/*public static boolean stopVisitMethod()
	{
		if (methodVisitCount > maxMethodVisitCount)
			return true;
		
		return false;
	}*/
	public static HashMap<String, String> deobfuseMap;
	public static Set<String> ignorelibrary;
	public static void readignorelibrary(String apkname)
	{
		ignorelibrary = new HashSet<String>();
		Constants.serializationMode = true;
		File file = new File("./deobfuse/ignorelibrary/" + apkname + ".txt");
		BufferedReader in;
		if (file.isFile())
		{
			try
			{
				in = new BufferedReader(new FileReader(file));
				String s;
				try
				{
					while ((s = in.readLine()) != null)
					{
						ignorelibrary.add(s);
					}
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				in.close();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}
	public static String[] WellknownLibrarys =
	{ "<com.squareup.okhttp.", "<com.android.volley", "<com.github.kevinsawicki.", "<org.apache.http.",
			"<ch.boye.httpclientandroidlib.", "<java.net." };
	
	@SuppressWarnings("resource")
	public static void readDeobfuse(String apkname)
	{
		deobfuseMap = new HashMap<String, String>();
		Constants.serializationMode = true;
		File file = new File("./deobfuse/" + apkname + ".txt");
		BufferedReader in;
		if (file.isFile())
		{
			String s;
			try
			{
				in = new BufferedReader(new FileReader(file));
				while ((s = in.readLine()) != null)
				{
					String r[] = s.split("=====");
					if (r.length != 2)
						throw new IOException();
					deobfuseMap.put(r[0], r[1]);
				}
				in.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	public static String Deobfuse(String string)
	{
		if (deobfuseMap.containsKey(string))
		{
			return deobfuseMap.get(string);
		}
		else
		{
			return string;
		}
	}
	public static String getMethodname(String method)
	{
		String sig[] = method.split(" ");
		if (sig.length != 3)
		{
			System.out.println("Getmethodname error");
			System.exit(-1);
		}
		return sig[2].split("\\(")[0];
	}
	public static Set<String> UncoveredCandidate = new HashSet<String>();
	public static boolean isWellknownLibrary(String methodref)
	{
		for (String library : WellknownLibrarys)
		{
			if (methodref.startsWith(library))
			{
				for (String ignore : ignorelibrary)
				{
					if (methodref.startsWith(ignore))
						return false;
				}
				return true;
			}
		}
		return false;
	}
	public static void VolleyinitHandler(String method, int url, int query, HashMap<String, ArrayList<BFNode>> BFTtable,
			InstanceInvokeExpr iie, UrlBuilder urlparser)
	{
		String methodstring;
		if (method.equals("0"))
		{
			methodstring = "GET ";
		}
		else
			if (method.equals("1"))
			{
				methodstring = "POST ";
			}
			else
				if (method.equals("2"))
				{
					methodstring = "PUT ";
				}
				else
					if (method.equals("3"))
					{
						methodstring = "Delete ";
					}
					else
					{
						methodstring = "Unkownmethod ";
					}
		String urlstring = "(.*)";
		String querystring = "";
		if (url >= 0)
		{
			if (iie.getArg(url) instanceof Constant)
			{
				urlstring = iie.getArg(url).toString();
			}
			else
			{
				urlstring = urlparser.GenRegex(null, BFTtable, iie.getArg(url).toString());
			}
			urlstring = methodstring + urlstring.replaceAll("\"", "");
		}
		if (query >= 0)
		{
			if (iie.getArg(query) instanceof Constant)
			{
				querystring = iie.getArg(query).toString();
			}
			else
			{
				querystring = urlparser.GenRegex(null, BFTtable, iie.getArg(query).toString());
			}
			if (querystring.equals("null"))
				querystring = "";
		}
		if (Constants.VolleyUrlCandidate != null) {
			if (Constants.VolleyUrlCandidate.equals(""))
				Constants.VolleyUrlCandidate = urlstring;
			else
				Constants.VolleyUrlCandidate += "|" + urlstring;
		}else
			Constants.VolleyUrlCandidate = urlstring;
		
		if (querystring != null)
		{
			Constants.VolleyQueryCandidate = querystring;
		}
		if (Constants.VolleyQueryCandidate != null)
			Constants.VolleyUrlCandidate += Constants.VolleyQueryCandidate;
		Constants.VolleyQueryCandidate = null;
	}
	public static boolean Jsonstart = false;
	public static int Jsonnumber = 1000;
	public static int segmentation = 0;
	public static int nowJsonnumber = 0;
	public static String target_string;
	public static String javaPath;
	public static String classparam = "";
	public static DebugInfo DebugInfo;
	
	

	public static boolean CheckJsonStart(HashMap<String, ArrayList<BFNode>> bFTtable,
			SignatureBuilder_Request bufferExtractor)
	{
		if (Jsonstart == true && nowJsonnumber < Jsonnumber)
		{
			nowJsonnumber++;
			return true;
		}
		else
			if (Jsonstart == true && nowJsonnumber == Jsonnumber)
			{
				nowJsonnumber = 0;
				segmentation++;
				return true;
			}
		for (String keys : bFTtable.keySet())
		{
			String regex = bufferExtractor.GenRegex(null, bFTtable, keys);
			if (regex.contains(target_string))
			{
				Jsonstart = true;
				return true;
			}
		}
		return false;
	}
	public static void readheapresult(String apkname)
	{
		deobfuseMap = new HashMap<String, String>();
		Constants.serializationMode = true;
		for (int num = 0; num < 10; num++)
		{
			File file = new File("./heapresult/" + apkname + "/" + num + ".txt");
			BufferedReader in;
			if (file.isFile())
			{
				try
				{
					in = new BufferedReader(new FileReader(file));
					String s;
					try
					{
						int line = 0;
						String key = "";
						String value = "";
						while ((s = in.readLine()) != null)
						{
							if (line == 0)
								key = s;
							if (line == 1)
								value = s;
							line++;
						}
						HeapHandler.updateHeapTable(key, value.substring(0, value.length() - 1));
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					in.close();
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
	}
	public static String getReqPath()
	{
		return serializationDirName + "/" + apkName + "/" + "ReqPair.json";
	}
	
	public static String getResPath()
	{
		return serializationDirName + "/" + apkName + "/" + "ResPair.json";
	}
	
	public static void WriteFile(String path, String contents)
	{
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(path));
			out.write(contents);
			out.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static String getObfsPath() {
		return Constants.serializationDirName + "\\obfstable.ser";
	}
}
