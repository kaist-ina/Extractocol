
package Extractocol.Tester;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.Helper.CFGSerializer;
import Extractocol.BufferExtractor_Request.Helper.FakeScene;
import Extractocol.BufferExtractor_Request.Helper.SerEPcontainer;
import Extractocol.BufferExtractor_Request.Helper.SerializableCFG;
import Extractocol.BufferExtractor_Response.SignatureBuilder_Response;
import Extractocol.Outputs.ReqRespInfo;
import Extractocol.Outputs.RequestInfoList;
import Extractocol.Outputs.ResponseInfoList;
import Extractocol.Pairing.SemanticAppliedList;

public class BackendTester_Response_Iter
{
	public static ArrayList<String> EPs = new ArrayList<String>();
	public static ArrayList<String> DPstmts = new ArrayList<String>();
	public static ArrayList<String> allMethods = new ArrayList<String>();
	
	public static boolean Backend_Forward(String ep, String DPStmt){
		try{
			
			allMethods.add(ep);
			
			// Save DP method and DP Stmt
			Constants.reqRespInfo.respie.setDPMethod(ep);
			Constants.reqRespInfo.respie.setDPStmt(DPStmt);
			
			Constants.CurrentEP = ep; // EP
			Constants.CurrentEntryPoint = ep;
			Constants.CurrentParentMethod = ep; // DP
			Constants.TaintFunctions = allMethods; // All Method
			Constants.DPStmt = DPStmt;
			Constants.foundDPStmt = false;
			Constants.methodVisitCount = 0;
			
			if(Constants.apkName.equals("flipboard"))
			{
				allMethods.add("<flipboard.json.JsonSerializationWrapper: java.lang.Object a(java.io.InputStream,java.lang.Class)>");
				Constants.CurrentParentMethod="<flipboard.json.JsonSerializationWrapper: java.lang.Object a(java.io.InputStream,java.lang.Class)>";
			}
			
			if(Constants.apkName.equals("linkedin"))
			{
				allMethods.add("<com.linkedin.android.client.MobileClient: java.lang.Object makeRequestAndUnmarshall(org.apache.http.client.methods.HttpUriRequest,java.lang.Class)>");
				allMethods.add("<com.linkedin.android.client.MobileClient: java.lang.Object makeRequestAndUnmarshall(org.apache.http.client.methods.HttpUriRequest,java.lang.Class,com.linkedin.android.client.MobileClient$JsonStreamParser,java.lang.StringBuilder)>");
				allMethods.add("<com.linkedin.android.client.MobileClient: java.lang.Object makeRequestAndUnmarshall(org.apache.http.impl.client.DefaultHttpClient,org.apache.http.client.methods.HttpUriRequest,java.lang.Class,com.linkedin.android.client.MobileClient$JsonStreamParser,java.lang.StringBuilder)>");
				allMethods.add("<com.linkedin.android.utils.JsonUtils: java.lang.Object objectFromJson(java.lang.String,java.lang.Class)>");
				allMethods.add("<com.linkedin.android.client.MobileClient: java.lang.Object makeRequestAndUnmarshall(org.apache.http.impl.client.DefaultHttpClient,org.apache.http.client.methods.HttpUriRequest,java.lang.Class,com.linkedin.android.client.MobileClient$JsonStreamParser,java.lang.StringBuilder)>");
				Constants.CurrentParentMethod="<com.linkedin.android.client.MobileClient: java.lang.Object makeRequestAndUnmarshall(org.apache.http.impl.client.DefaultHttpClient,org.apache.http.client.methods.HttpUriRequest,java.lang.Class,com.linkedin.android.client.MobileClient$JsonStreamParser,java.lang.StringBuilder)>";
				
				if(ep.equals("<com.linkedin.android.client.MobileClient: java.lang.String getUploadInfoToken()>"))
					return false;
			}
			
			
			//For Pairing
			Constants.PairInfo.Add_dp();
			Constants.PairInfo.Add_dp_method(Constants.PairInfo.getCurrentSigEntry(), ep);
			Constants.PairInfo.Add_dp_stmt(Constants.PairInfo.getCurrentSigEntry(), DPStmt);
			//ResponseInfoList.AddResponseInfo(null, new PairingInfoList(Constants.CurrentEP, Constants.DPStmt, null));
			
			// add request Array
			JSONArray responseEntries = new JSONArray();
			JSONArray ep_methods = new JSONArray();
			Constants.PairInfo.Add_request_array(Constants.PairInfo.getCurrentSigEntry(), responseEntries, false);
			JSONObject responseEntry = new JSONObject();
			JSONArray epstmts = new JSONArray();
			
			if (Constants.sCFG_Forward.getMethodOf(ep) == null)
			{
				System.out.println("EP not found");
				return false;
			} else
			{
				SignatureBuilder_Response be = new SignatureBuilder_Response();
				be.ep_methods = ep_methods;
				be.responseEntry = responseEntry;
				be.epstmts = epstmts;
				
				be.StartFingerprint();
				
				Constants.PairInfo.Add_request_entry(responseEntries, responseEntry);
			}
			
			// BK We need to clear the list per loop (EP processing).
			// After processing an EP, in the list there could be some statements to which semantic model is applied.
			// The statements in the list should be cleared because they are not related to a signature that might be extracted while processing the next EP.
			Constants.SemanticMethodAndStmt.clear();
			
			System.out.println("===================================================");
			
			return true;
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static void InitForward(){
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
	
	public static void InitForward_FaceScene(){
		// Load faceScene
		System.out.println("fake scene...");
		Constants.fsc = new FakeScene();
		System.out.println("fake scene done");			
	}
	
	private static void LoadDPInfo(){
		try{
			String EPPath = Constants.GetForwardEP_path();
			BufferedReader in = new BufferedReader(new FileReader(EPPath));
			String s;

			while ((s = in.readLine()) != null)
			{
				if(s.startsWith("%"))
					continue;
				
				String tok[] = s.split("####");
				EPs.add(tok[0]);
				DPstmts.add(tok[1]);
			}
			in.close();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		try
		{
			Constants.readDeobfuse(args[1]);
			Constants.readignorelibrary(args[1]);
			
			int k = 0;
			Constants.serIsForward = true;

 			while (k < args.length)
			{
				if (args[k].equalsIgnoreCase("--app"))
				{
					Constants.apkName = args[k + 1];
					k += 2;
				} else if (args[k].equalsIgnoreCase("--backward"))
				{
					Constants.serIsForward = false;
					k++;
				} else if (args[k].equalsIgnoreCase("--heapobject"))
				{
					Constants.heapobject = true;
					k += 2;
				}
				else if (args[k].equalsIgnoreCase("--taintFunctionOnly"))
				{
					Constants.searchMethodFilterUsingTaintFunction = true;
					k++;
				}else if (args[k].equalsIgnoreCase("--debug"))
				{
					Constants.isDebug = true;
					k++;
				}
			}
			
			System.out.println("App Name : " + Constants.apkName + "\n");
			InitForward();
			
			System.out.println("\n\n[Serialization Mode] Backend Start! -- Forward");

			// Load faceScene
			InitForward_FaceScene();

			// Read DPInfo
			LoadDPInfo();
			
			int idx = 0;
			System.out.println("Size : " + EPs.size());
			for (String ep : EPs)
			{
				System.out.println( ((idx) + 1) + "th EP");
				System.out.println("\tEP_Method: " + ep);
				System.out.println("\tDP_Stmt: " + DPstmts.get(idx));
				
				// Allocate an entry to save outputs of back-end-forward analysis
				NewResponseInfoEntry();
				
				// Start analysis
				Backend_Forward(ep, DPstmts.get(idx));
				
				// Save the result to the list
				KeepResponseInfoEntry(); 
				
				idx++;
			} /* for (String ep : EPs) */
			
			// Added by Byungkwon
			ResponseInfoList.Serialize(ResponseInfoList.lstResponseInfo);
			System.out.println("Finish serializing ResponseInfoList");

		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("error?");
		}

		System.out.println("exit!");
	}

	private static void NewResponseInfoEntry(){
		Constants.reqRespInfo = new ReqRespInfo();
	}
	
	private static void KeepResponseInfoEntry(){
		if(Constants.reqRespInfo.respie.getSignature() != null)
			ResponseInfoList.AddRespInfoEntry(Constants.reqRespInfo.getResponseInfoEntry());
	}

	
	public static void safePrint(String s)
	{
		synchronized (System.out)
		{
			System.out.print(s);
		}
	}

	public static void safePrintln(String s)
	{
		synchronized (System.out)
		{
			System.out.println(s);
		}
	}
}
