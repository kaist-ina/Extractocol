/*
package extractocol.backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import extractocol.Constants;
import extractocol.backend.request.helper.CFGSerializer;
import extractocol.backend.request.helper.SerializableCFG;
import extractocol.backend.request.helper.CFGSerializer.ICFG_CASE;
import extractocol.backend.response.SignatureBuilder_Response;
import extractocol.common.outputs.BackendOutput;
import extractocol.common.outputs.backendOutputHelper.ReqRespInfo;
import extractocol.common.outputs.backendOutputHelper.ResponseInfoList;

public class BackendTester_Response_Iter
{
	public static ArrayList<String> EPs = new ArrayList<String>();
	public static ArrayList<String> DPstmts = new ArrayList<String>();
	public static ArrayList<String> allMethods = new ArrayList<String>();
	
	public static boolean Backend_Forward(String ep, String DPStmt){
		try{
			
			//allMethods.add(ep);
			
			// Save DP method and DP Stmt
			BackendOutput.reqRespInfo.respie.setDPMethod(ep);
			BackendOutput.reqRespInfo.respie.setDPStmt(DPStmt);
			
			Constants.CurrentEP = ep; // EP
			Constants.CurrentEntryPoint = ep;
			Constants.CurrentParentMethod = ep; // DP
			//Constants.TaintFunctions = allMethods; // All Method
			Constants.DPStmt = DPStmt;
			Constants.foundDPStmt = false;
			Constants.methodVisitCount = 0;
			
			
			if (Constants.sCFG.getMethodOf(ep) == null)
			{
				System.out.println("EP not found");
				return false;
			} else
			{
				SignatureBuilder_Response be = new SignatureBuilder_Response();
				be.ep_methods = new JSONArray();
				be.responseEntry = new JSONObject();
				be.epstmts = new JSONArray();
				
				be.StartFingerprint();
				
				//Constants.PairInfo.Add_request_entry(new JSONArray(), be.responseEntry);
			}
			
			// BK We need to clear the list per loop (EP processing).
			// After processing an EP, in the list there could be some statements to which semantic model is applied.
			// The statements in the list should be cleared because they are not related to a signature that might be extracted while processing the next EP.
			//Constants.SemanticMethodAndStmt.clear();
			
			if(Constants.isForwardDebug)
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
			SerializableCFG dsCFG = CFGs.Deserialize(ICFG_CASE.FORWARD);
			Constants.sCFG_Forward = dsCFG;
			
			Constants.javaPath="../../tools/dex2jar-2.0/dex2jar-2.0/" + Constants.apkName + "-dex2jar/";
		}catch (Exception e)
		{
			e.printStackTrace();
		}
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
			}  for (String ep : EPs) 
			
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
		BackendOutput.reqRespInfo = new ReqRespInfo();
	}
	
	private static void KeepResponseInfoEntry(){
		if(BackendOutput.reqRespInfo.respie.getSignature() != null)
			ResponseInfoList.AddRespInfoEntry(BackendOutput.reqRespInfo.getResponseInfoEntry());
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
*/