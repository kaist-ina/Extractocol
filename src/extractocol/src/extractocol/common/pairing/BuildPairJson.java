package extractocol.common.pairing;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BuildPairJson
{
	/*
	 * 
	 * 2016.07.03 Jeongmin Kim
	 * JSON Structure
	 * {DPs: [ DP_stmt : xx, DP_method : xx, Requests : [{URI:xx, EP_Method:xx, EP_stmts : [{xx}, {xx}]}], Responses : [{Body:xx, EP_Method : xx, EP_Stmts : [{xx}, {xx}, {xx}]}]]}
	 *  
	 */
	
	private JSONArray jsonDP = new JSONArray();
	public static int CurrentDpIdx = -1;
	
	public int getSize()
	{
		return jsonDP.size();
	}
	
	@SuppressWarnings("unchecked")
	public void Add_dp()
	{
		JSONObject SigEntry = new JSONObject();
		jsonDP.add(SigEntry);
		CurrentDpIdx ++;
	}
	
	public JSONObject getCurrentSigEntry()
	{
		return (JSONObject) jsonDP.get(CurrentDpIdx);
	}
	
	@SuppressWarnings("unchecked")
	public void Add_dp_stmt(JSONObject SigEntry, String stmt)
	{
		SigEntry.put("DP_Stmt", stmt);
	}
	
	@SuppressWarnings("unchecked")
	public void Add_dp_method(JSONObject SigEntry, String method)
	{
		SigEntry.put("DP_Method", method);
	}
	
	@SuppressWarnings("unchecked")
	public void Add_request_array(JSONObject SigEntry, JSONArray RequestorResponseEntries, boolean isRequest)
	{
		if (isRequest)
			SigEntry.put("Requests", RequestorResponseEntries);
		else
			SigEntry.put("Responses", RequestorResponseEntries);
	}
	
	@SuppressWarnings("unchecked")
	public void Add_request_entry(JSONArray RequestEntries, JSONObject RequestEntry)
	{
		RequestEntries.add(RequestEntry);
	}
	
	@SuppressWarnings("unchecked")
	public void Add_uri(JSONObject RequestEntry, String uriorbody, boolean isRequest)
	{
		if (isRequest)
			RequestEntry.put("URI", uriorbody);
		else
			RequestEntry.put("BODY", uriorbody);
	}
	
	@SuppressWarnings("unchecked")
	public void Add_ep_method(JSONObject RequestEntry, JSONArray EP_Methods)
	{
		RequestEntry.put("EP_Method", EP_Methods);
	}
	
	@SuppressWarnings("unchecked")
	public void Add_ep_stmts(JSONObject RequestEntry, JSONArray stmts)
	{
		RequestEntry.put("EP_Stmts", stmts);
	}
	
	@SuppressWarnings("unchecked")
	public void Add_ep_stmt(JSONArray stmts , String stmt)
	{
		stmts.add(stmt);
	}
	
	@SuppressWarnings("unchecked")
	public String jsonDPs()
	{
		JSONObject result = new JSONObject();
		
		ArrayList<JSONObject> toberemoved = new ArrayList<JSONObject> ();
		for (int i= 0 ; i < jsonDP.size(); i++ )
		{
			JSONObject entry = (JSONObject) jsonDP.get(i);
			if ( entry.get("Requests") == null && entry.get("Responses") == null )
				toberemoved.add(entry);
				
		}
		jsonDP.removeAll(toberemoved);
		result.put("DPs", jsonDP);
		return result.toJSONString();
	}
}
