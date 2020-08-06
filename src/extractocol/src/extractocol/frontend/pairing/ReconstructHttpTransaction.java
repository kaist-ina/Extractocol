
package extractocol.frontend.pairing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import extractocol.Constants;
import extractocol.common.outputs.backendOutputHelper.RequestInfoList;
import extractocol.common.outputs.backendOutputHelper.ResponseInfoList;
import extractocol.frontend.slice.AbstractSlice;

public class ReconstructHttpTransaction
{
	public static String pairingSourcesAndSinksFile = "SourcesAndSinksPairing.txt";
	private boolean backward = false;
	List<PairingInfo> pairingInfos = new ArrayList<PairingInfo>();
	private String pairingFile = "Pairing.json";
	public ReconstructHttpTransaction()
	{
		// BK Load pairing information
		//load_ReqRespInfo();
		
		/*
		// We must load response and request pair information.
		File fp = new File(Constants.getReqPath());
		// If "Pairing.json" file exists, we load and parse it.
		if (fp.exists() && fp.isFile())
		{
			load();
		}
		// If "Pairing.json" file dosen't exist, exit.
		else
		{
			System.out.println("No Request Pairing.json file!");
		}
		fp = new File(Constants.getResPath());
		// If "Pairing.json" file exists, we load and parse it.
		if (fp.exists() && fp.isFile())
		{
			load();
		}
		// If "Pairing.json" file dosen't exist, exit.
		else
		{
			System.out.println("No Response Pairing.json file!");
		}
		*/
	}
	public ReconstructHttpTransaction(List<AbstractSlice> ass, boolean backward)
	{
		this.backward = backward;
		File fp = new File(pairingFile);
		// If "Pairing.json" file exists, we have to load and parse it.
		if (fp.exists() && fp.isFile())
		{
			load();
		}
		// If "Pairing.json" file dosen't exist, we have to create a new file.
		else
		{
			newPairing(ass);
		}
	}
	/*
	 * If "Pairing.json" doesn't exist, we initialize Pairing.json contents.
	 */
	@SuppressWarnings("unchecked")
	private void newPairing(List<AbstractSlice> ass)
	{
		JSONObject obj = new JSONObject();
		JSONArray dps = new JSONArray();
		for (AbstractSlice as : ass)
		{
			JSONObject dp = new JSONObject();
			dp.put("DP_Stmt", as.getDpStmt().toString());
			dp.put("DP_Method", as.getDpMethod().toString());
			dp.put("Requests", new JSONArray());
			dp.put("Responses", new JSONArray());
			dps.add(dp);
			PairingInfo pInfo = new PairingInfo(as.getDpStmt().toString(), as.getDpMethod().toString());
			pairingInfos.add(pInfo);
		}
		obj.put("DPs", dps);
		try
		{
			FileWriter file = new FileWriter(pairingFile);
			file.write(obj.toJSONString());
			file.flush();
			file.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	// Load request and response information from file.
	// Added by Byungkwon
	private void load_ReqRespInfo()
	{
		// 1. Load info from file
		//RequestInfoList.LoadFromFile();
		//ResponseInfoList.LoadFromFile();
		
		// 
	}
	
	/*
	 * If "Pairing.json" exists, we load Pairing.json contents.
	 */
	private void load()
	{
		JSONParser parser = new JSONParser();
		boolean loadDebug = false;
		try
		{
			Object obj = parser.parse(new FileReader(pairingFile));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray dps = (JSONArray) jsonObject.get("DPs");
			for (int i = 0; i < dps.size(); i++)
			{
				if (loadDebug)
					System.out.println("\n<DP#" + (i + 1) + ">");
				JSONObject dp = (JSONObject) dps.get(i);
				String dp_stmt = (String) dp.get("DP_Stmt");
				if (loadDebug)
					System.out.println("DP_Stmt: " + dp_stmt);
				String dp_method = (String) dp.get("DP_Method");
				if (loadDebug)
					System.out.println("DP_Method: " + dp_method);
				// Initial case
				addPairingInfo(dp_stmt, dp_method);
				// Extract request items
				if (dp.get("Requests") != null)
				{
					JSONArray requests = (JSONArray) dp.get("Requests");
					for (int j = 0; j < requests.size(); j++)
					{
						if (loadDebug)
							System.out.println("Request#" + (j + 1));
						JSONObject request = (JSONObject) requests.get(j);
						String uri = (String) request.get("URI");
						if (loadDebug)
							System.out.println("\tURI: " + uri);
						JSONArray ep_methods = (JSONArray) request.get("EP_Method");
						for (int f = 0; f < ep_methods.size(); f++)
						{
							String ep_method = (String) ep_methods.get(f);
							if (loadDebug)
								System.out.println("\tEP_Method: " + ep_methods);
							JSONArray ep_stmts = (JSONArray) request.get("EP_Stmts");
							if (loadDebug)
								System.out.println("\tEP_Stmts: ");
							String stmt = (String) ep_stmts.get(f);
							if (loadDebug)
								System.out.println("\t\tStmt: " + stmt);
							addPairingInfo(dp_stmt, dp_method, uri, stmt, ep_method, true);
						}
					}
				}
				else if (dp.get("Responses") != null)
				{
					// Extract response items
					JSONArray responses = (JSONArray) dp.get("Responses");
					for (int j = 0; j < responses.size(); j++)
					{
						if (loadDebug)
							System.out.println("Response#" + (j + 1));
						JSONObject response = (JSONObject) responses.get(j);
						String body = (String) response.get("BODY");
						if (loadDebug)
							System.out.println("\tBODY: " + body);
						JSONArray ep_methods = (JSONArray) response.get("EP_Method");
						// When Extractocol backward analyzes this ep, it has not instructions that cannot be applied to semantic model.
						// So this loop is to be skipped.
						if (response.get("EP_Method") == null)
							continue;
						for (int f = 0; f < ep_methods.size(); f++)
						{
							String ep_method = (String) ep_methods.get(f);
							if (loadDebug)
								System.out.println("\tEP_Method: " + ep_method);
							JSONArray ep_stmts = (JSONArray) response.get("EP_Stmts");
							String stmt = (String) ep_stmts.get(f);
							if (loadDebug)
								System.out.println("\tEP_Stmts: " + stmt);
							addPairingInfo(dp_stmt, dp_method, body, stmt, ep_method, false);
						}
					}
				}
				else
					System.out.println("[Extractocol Error] : Pairing Data is wrong");
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
	}
	// First, we need to load DP information
	public void addPairingInfo(String dp_stmt, String dp_method)
	{
		boolean hasExist = false;
		for (PairingInfo pInfo : pairingInfos)
		{
			if (pInfo.getDP_Stmt().equals(dp_stmt) && pInfo.getDP_Method().equals(dp_method))
			{
				hasExist = true;
			}
		}
		if (!hasExist)
		{
			PairingInfo newInfo = new PairingInfo(dp_stmt, dp_method);
			pairingInfos.add(newInfo);
		}
	}
	// addPairingInfo() wrapper
	public void addPairingInfo(String dp_stmt, String dp_method, String signature, String ep_stmt, String ep_method)
	{
		if (this.backward)
			addPairingInfo(dp_stmt, dp_method, signature, ep_stmt, ep_method, true);
		else
			addPairingInfo(dp_stmt, dp_method, signature, ep_stmt, ep_method, false);
	}
	
	// Add pairing information.
	public void addPairingInfo(String dp_stmt, String dp_method, String signature, String ep_stmt, String ep_method, boolean request)
	{
		boolean hasExist = false;
		for (PairingInfo pInfo : pairingInfos)
		{
			if (pInfo.getDP_Stmt().equals(dp_stmt) && pInfo.getDP_Method().equals(dp_method))
			{
				hasExist = true;
				update(pInfo, signature, ep_stmt, ep_method, request);
			}
		}
		if (!hasExist)
		{
			PairingInfo newInfo = new PairingInfo(dp_stmt, dp_method);
			update(newInfo, signature, ep_stmt, ep_method, request);
			pairingInfos.add(newInfo);
		}
	}
	// Update URI/BODY signature class.
	private void update(PairingInfo pInfo, String signature, String ep_stmt, String ep_method, boolean request)
	{
		if (request)
			pInfo.setURISignature(signature, ep_stmt, ep_method);
		else
			pInfo.setBODYSignature(signature, ep_stmt, ep_method);
	}
	// Print stored pairing information in memory.
	public void print()
	{
		for (int i = 0; i < pairingInfos.size(); i++)
		{
			System.out.println("\n<DP#" + (i + 1) + ">");
			PairingInfo pInfo = pairingInfos.get(i);
			System.out.println("DP_Stmt: " + pInfo.getDP_Stmt());
			System.out.println("DP_Method: " + pInfo.getDP_Method());
			System.out.println("Requests: ");
			for (int j = 0; j < pInfo.getRequestInfos().size(); j++)
			{
				System.out.println("\tURI: " + pInfo.getRequestInfos().get(j).getUriSig());
				System.out.println("\tEP_Method: " + pInfo.getRequestInfos().get(j).getEP_Method());
				System.out.println("\tEP_Stmts: ");
				for (String stmt : pInfo.getRequestInfos().get(j).getEP_Stmt())
				{
					System.out.println("\t\tStmt: " + stmt);
				}
			}
			System.out.println("Reponses: ");
			for (int j = 0; j < pInfo.getResponseInfos().size(); j++)
			{
				System.out.println("\tBODY: " + pInfo.getResponseInfos().get(j).getBodySig());
				System.out.println("\tEP_Method: " + pInfo.getResponseInfos().get(j).getEP_Method());
				System.out.println("\tEP_Stmts: ");
				for (String stmt : pInfo.getResponseInfos().get(j).getEP_Stmt())
				{
					System.out.println("\t\tStmt: " + stmt);
				}
			}
		}
		System.out.println();
	}
	// Write pairing information into the "Pairing.json" file.
	@SuppressWarnings("unchecked")
	public void write()
	{
		JSONObject obj = new JSONObject();
		JSONArray dps = new JSONArray();
		for (int i = 0; i < pairingInfos.size(); i++)
		{
			JSONObject dp = new JSONObject();
			PairingInfo pInfo = pairingInfos.get(i);
			dp.put("DP_Stmt", pInfo.getDP_Stmt());
			dp.put("DP_Method", pInfo.getDP_Method());
			JSONArray requests = new JSONArray();
			for (int j = 0; j < pInfo.getRequestInfos().size(); j++)
			{
				JSONObject request = new JSONObject();
				request.put("URI", pInfo.getRequestInfos().get(j).getUriSig());
				request.put("EP_Method", pInfo.getRequestInfos().get(j).getEP_Method());
				JSONArray stmts = new JSONArray();
				for (String s : pInfo.getRequestInfos().get(j).getEP_Stmt())
				{
					JSONObject stmt = new JSONObject();
					stmt.put("Stmt", s);
					stmts.add(stmt);
				}
				request.put("EP_Stmts", stmts);
				requests.add(request);
			}
			dp.put("Requests", requests);
			JSONArray responses = new JSONArray();
			for (int j = 0; j < pInfo.getResponseInfos().size(); j++)
			{
				JSONObject response = new JSONObject();
				response.put("BODY", pInfo.getResponseInfos().get(j).getBodySig());
				response.put("EP_Method", pInfo.getResponseInfos().get(j).getEP_Method());
				JSONArray stmts = new JSONArray();
				for (String s : pInfo.getResponseInfos().get(j).getEP_Stmt())
				{
					JSONObject stmt = new JSONObject();
					stmt.put("Stmt", s);
					stmts.add(stmt);
				}
				response.put("EP_Stmts", stmts);
				responses.add(response);
			}
			dp.put("Responses", responses);
			dps.add(dp);
		}
		obj.put("DPs", dps);
		try
		{
			FileWriter file = new FileWriter(pairingFile);
			file.write(obj.toJSONString());
			file.flush();
			file.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	// Make a SourcesAndSinksPairing.txt file from pairing information.
	/*public static void makeSourcesAndSinks2()
	{
		try
		{
			FileWriter file = new FileWriter(pairingSourcesAndSinksFile);
			
			//file.write("<java.lang.StringBuilder: void <init>(java.lang.String)> -> _SOURCE_\n");
			//file.write("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)> -> _SOURCE_\n");
			//file.write("<java.lang.StringBuilder: java.lang.String toString()> -> _SOURCE_\n");
			//file.write("<org.codehaus.jackson.map.ObjectMapper: java.lang.Object readValue(java.io.InputStream,java.lang.Class)> -> _SINK_\n");
			
			// BK commented instantly
			for(int i = 0; i < RequestInfoList.lstRequestInfo.size(); i++)
				for(int j = 0; j < RequestInfoList.lstRequestInfo.get(i).getSemanticAppliedList().getEpList().size(); j++){
					file.write(RequestInfoList.lstRequestInfo.get(i).getSemanticAppliedList().getEpList().get(j).getStmt() + " -> _SOURCE_");
					
					// line breaker
					file.write(System.lineSeparator());
				}
			
			for(int i = 0; i < ResponseInfoList.lstResponseInfo.size(); i++)
				for(int j = 0; j < ResponseInfoList.lstResponseInfo.get(i).getSemanticAppliedList().getEpList().size(); j++){
					file.write(ResponseInfoList.lstResponseInfo.get(i).getSemanticAppliedList().getEpList().get(j).getStmt() + " -> _SINK_");
					
					// line breaker
					file.write(System.lineSeparator());
				}
			
			file.flush();
			file.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}*/
	
	// Make a SourcesAndSinksPairing.txt file.
	public void makeSourcesAndSinks()
	{
		try
		{
			FileWriter file = new FileWriter(pairingSourcesAndSinksFile);
			Set<String> sources = new HashSet<String>();
			Set<String> sinks = new HashSet<String>();
			for (int i = 0; i < pairingInfos.size(); i++)
			{
				PairingInfo pInfo = pairingInfos.get(i);
				for (int j = 0; j < pInfo.getRequestInfos().size(); j++)
				{
					for (String stmt : pInfo.getRequestInfos().get(j).getEP_Stmt())
					{
						sources.add(stmt + " -> _SOURCE_\n");
					}
				}
				for (int j = 0; j < pInfo.getResponseInfos().size(); j++)
				{
					for (String stmt : pInfo.getResponseInfos().get(j).getEP_Stmt())
					{
						sinks.add(stmt + " -> _SINK_\n");
					}
				}
			}
			for (String source : sources)
				file.write(source);
			for (String sink : sinks)
				file.write(sink);
			file.flush();
			file.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	// Get PairingInfo.
	public List<PairingInfo> getPairingInfos()
	{
		return this.pairingInfos;
	}
}
