
package Extractocol;

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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.json.simple.JSONArray;

import com.gaurav.tree.Tree;
import Extractocol.Outputs.ReqRespInfo;
import Extractocol.Outputs.RequestInfoEntry;
import Extractocol.Outputs.ResponseInfoEntry;
import Extractocol.BufferExtractor_Request.SignatureBuilder_Request;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Basics.EPcontainer;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapEntry;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapHandler;
import Extractocol.BufferExtractor_Request.Helper.FakeScene;
import Extractocol.BufferExtractor_Request.Helper.SerializableCFG;
import Extractocol.BufferExtractor_Request.Semantic.JSON.JSONBuilder;
import Extractocol.BufferExtractor_Request.Semantic.Retrofit.retrofit_http_old;
import Extractocol.BufferExtractor_Request.Semantic.URL.UrlBuilder;
import Extractocol.Debugger.DebugInfo;
import Extractocol.Pairing.BuildPairJson;
import Extractocol.Pairing.PairingDPEntry;
import Extractocol.Pairing.SemanticAppliedEntry;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Constant;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.infoflow.BackwardInfoflow;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.source.ISourceSinkManager;

public class Constants
{
	public static ArrayList<ReqRespInfo> ReqRespInfoList = new ArrayList<ReqRespInfo>(); // Outputs of Backend
	public static ReqRespInfo reqRespInfo;
	
	public static Stack<String> method_stack;
	public static Set<SootMethod> callflow;
	public static ISourceSinkManager sourcesSinks;
	public static BackwardInfoflow backendinfoflow;
	public static IInfoflowCFG iCfg = null;
	public static Set<SootMethod> allmethods;
	public static Set<SootMethod> taintset;
	public static Map<Integer, int[]> line_deep = new HashMap<Integer, int[]>();
	public static int id = 0;
	public static HashMap<String, ArrayList<HeapEntry>> GlobalHeapTable; // Tracking values & JSON key list of heap & variable 
	public static String RunnableCalss;
	public static Scene globalScene;
	public static boolean isScene = false;
	public static FakeScene fsc = null;
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
	public static boolean searchMethodFilterUsingTaintFunction = false;
	// Extractocol Model Flag
	public static boolean isDebug = false;
	public static boolean isForwardDebug = true;
	public static boolean isDiffMode = false;
	public static boolean isPairing = false;
	public static boolean removeWrongEp = false;
	public static HashMap <String, DebugInfo> DebugInfoMap = null;
	
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
		// SM_what_you_want.add("com.xtreme.network.NetworkRequestExecutor:
		// com.xtreme.network.NetworkResponse
		// a(com.xtreme.network.NetworkRequest)");
		// SM_what_you_want.add(
		// "com.xtreme.network.NetworkRequestExecutor:
		// com.xtreme.network.NetworkResponse
		// a(com.xtreme.network.NetworkRequest,org.apache.http.client.HttpClient)");
	}
	// retrofit results
	public static ArrayList<retrofit_http_old> retrofits = new ArrayList<retrofit_http_old>();
	public static ArrayList<String> retrofit_ignorelist = new ArrayList<String>();
	public static boolean isRetrofitDP = false;
	public static boolean isRetrofit = false;
	public static boolean isRxJavaDP = false;
	public static int searchmethodnum = 0;
	// Rxjava DP
	public static String[] RxJavaDP =
	{ "<rx.Observable: rx.Observable b(rx.Scheduler)>", "<rx.Observable: rx.Observable a(rx.Scheduler)>",
			"<rx.Observable: rx.Observable map(rx.functions.Func1)>" };
	public static ArrayList<String> retrofitDP = new ArrayList<String>();
	public static boolean alreadyVisited = false;
	// before jump to sm
	public static boolean isSemantic = false;
	public static boolean isCallee;
	public static int pathidx;
	public static ArrayList<String> path;
	public static Set<String> ResultUrls = new HashSet<String>();
	public static boolean Jimplify = true;
	public static EPcontainer EPcont;
	public static String CurrentEP;
	public static String DPStmt;
	public static String RealContextInthisObj = null;
	public static JSONBuilder jb;
	public static Set<String> robojuices = new HashSet<String>();
	public static ArrayList<String> stacktrace = new ArrayList<String>();
	// For specific EP

	//public static String SpecificEP = "<com.thirdrock.fivemiles.main.listing.ListItemViewModel: void parseFromItem(com.thirdrock.domain.Item)>";
	public static String SpecificEP = "<com.lgallardo.qbittorrentclient.MainActivity$qBittorrentTask: java.lang.Object doInBackground(java.lang.Object[])>";
	//public static String SpecificEP = "<com.logitech.harmonyhub.sdk.imp.BluetoothManager: boolean sendRequest(java.lang.String)>";
	//public static String SpecificEP = null;
	//public static String SpecificEP = "<com.contextlogic.wish.ui.fragment.product.tabbeddetails.TabbedProductDetailsFragment: void loadProduct()>";

	//<com.ted.android.view.fragment.TalkDetailFragment$14: void run()>
	//<com.linkedin.android.client.MobileClient: com.linkedin.android.model.JobsV2 getAppliedJobsV2(boolean,long,int)>
	//"<com.buzzfeed.android.util.BuzzApiClient: void loginToBuzzFeedWithAccount(java.lang.String,java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener)>"
	
	public static String SpecificHeap = null;
	public static List<String> SkipEP = Arrays.asList(
	// "<com.audible.application.ShopStore: void
	// onCreateVirtual(android.os.Bundle)>",
	// "<com.audible.application.ShopStore: void
	// onNewIntent(android.content.Intent)>
	);
	//public static String SpecificDP = null;
	//public static String SpecificDP   = "<com.logitech.harmonyhub.sdk.imp.BluetoothManager: boolean sendRequest(java.lang.String)>";
	//public static String SpecificDP = "<szelok.app.twister.DataModel$GetSpamPostsTask: java.util.TreeMap doInBackground(java.lang.Void[])>";
	public static String SpecificDP = null;
	//public static String SpecificDP = "<com.insthub.fivemiles.a.o: void fillProfile(java.lang.String,java.lang.String,java.lang.String)>";
	public static List<String> SkipDP = Arrays.asList(
	);
	// For Visualization
	public static boolean GraphVisualization = true;
	// For Param
	public static boolean isFirstParam = true;
	// For serialization
	// setup
	public static boolean Serializing = true;
	public static boolean heapobject = false;
	// backendTester on
	public static boolean serializationMode = false;
	public static boolean serIsForward;
	public static String serializationDirName = "../../SerializationFiles";
	public static String apkName;
	public static String VolleyUrlCandidate;
	public static String VolleyQueryCandidate = null;
	
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
	public static String RequestInfoPath()
	{
		File serializationDir = new File(serializationDirName);
		if (!serializationDir.exists())
		{
			serializationDir.mkdir();
		}
		return serializationDirName + "/" + apkName + "/" + "/RequestInfoList.ser"; /*
											 * you must not add / at the end of
											 * the path - hnamkoong 20150909
											 */
	}
	public static String getMultiDexName(String basePath, int i){
		File directory = new File(basePath + "/[MultiClasses]");
		if(!directory.exists())
			directory.mkdir();

		return "/[MultiClasses]/" + apkName + "_classes" + i + ".dex";
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
	// backward serialization
	public static String SerPath()
	{
		return serializationDirName + "/" + apkName + "/" + apkName + ".iCfg";
	}
	
	public static String DebugPath()
	{
		return serializationDirName + "/" + apkName + "/" + "debug.ser";
	}
	public static String EPContainorPath()
	{
		return serializationDirName + "/" + apkName + "/" + apkName + "-epc.txt";
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
	public static String EPContainorPath_forward()
	{
		return serializationDirName + "/" + apkName + "/" + apkName + "-epc-forward.txt";
	}
	public static String UrlRelatedHeapPath()
	{
		return serializationDirName + "/" + apkName + "/" + apkName + "-heapobject.ser";
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
	public static String CurrentEntryPoint;
	public static String CurrentParentMethod;
	public static ArrayList<String> CallFlow;
	public static ArrayList<String> TaintFunctions;
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
	public static boolean foundDPStmt;
	public static Unit target;
	public static long starttime;
	public static boolean istimestart = false;
	public static int methodVisitCount;
	public static int EPnum;
	public static int DPnum;
	public static ArrayList<JSONArray> DEBUG_json;
	public final static int maxMethodVisitCount = 3000;
	public static final boolean BFT_Visualize = false;

	//Pairing Information
	public static BuildPairJson PairInfo = new BuildPairJson();
	
	// The list 'SemanticMethodAndStmt' keeps statements to which a semantic model is applied.
	// The list also keeps the corresponding method.
	// The list can be used for both backward and forward analysis.
	// Added by Byungkwon
	public static ArrayList<SemanticAppliedEntry> SemanticMethodAndStmt = new ArrayList<SemanticAppliedEntry>();
	
	public static boolean stopVisitMethod()
	{
		if (methodVisitCount > maxMethodVisitCount)
		{
			return true;
		}
		return false;
	}
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				in.close();
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
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
		if (Constants.VolleyUrlCandidate.equals(""))
			Constants.VolleyUrlCandidate = urlstring;
		else
			Constants.VolleyUrlCandidate += "|" + urlstring;
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					in.close();
				}
				catch (IOException e1)
				{
					// TODO Auto-generated catch block
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
}
