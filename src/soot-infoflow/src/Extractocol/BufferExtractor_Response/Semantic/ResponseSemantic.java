
package Extractocol.BufferExtractor_Response.Semantic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.json.simple.JSONArray;

import soot.jimple.Constant;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.Retrofit.retrofit_http;
import Extractocol.BufferExtractor_Response.SignatureBuilder_Response;
import Extractocol.BufferExtractor_Response.Basics.BFNode_Response;
import Extractocol.BufferExtractor_Response.Basics.ParameterBucket;
import Extractocol.BufferExtractor_Response.Helper.BFTTableHelper;
import Extractocol.BufferExtractor_Response.Helper.SemanticHelper;
import Extractocol.BufferExtractor_Response.Helper.TaintHelper;
import Extractocol.BufferExtractor_Response.UnitHandle.InvokeHandler;
import Extractocol.Outputs.ResponseInfoList;
import Extractocol.Pairing.PairingDPEntry;
import Extractocol.Pairing.SemanticAppliedList;
import Extractocol.Pairing.SemanticAppliedEntry;

public class ResponseSemantic
{
	public static ParameterBucket static_pb;
	private static boolean isJSONwithCLASS = false;
	@SuppressWarnings("unchecked")
	public static boolean process(ParameterBucket pb)
	{
		static_pb = pb;
		// BK isJSONwithCLASS is used for checking whether the semantic model of JSON with class (readValue, etc) is applied in JSON_Semantic().
		isJSONwithCLASS = false;
		if (String_Semantic() || JSON_Semantic())
		{
			// For Pairing Information
			if (pb.be.responseEntry != null)
			{
				// BK Add the unit information if this unit does not include JSON parser using class such as readValue
				if(!isJSONwithCLASS)
					if(!PairingDPEntry.isEntryIncluded(new SemanticAppliedEntry(pb.sm.getSignature(), pb.ie.getMethodRef().getSignature()), Constants.SemanticMethodAndStmt))
						Constants.SemanticMethodAndStmt.add(new SemanticAppliedEntry(pb.sm.getSignature(), pb.ie.getMethodRef().getSignature()));
				
				pb.be.ep_methods.add(pb.sm.getSignature());
				Constants.PairInfo.Add_ep_method(pb.be.responseEntry, pb.be.ep_methods);
				pb.be.epstmts.add(pb.ie.getMethodRef().getSignature());
				Constants.PairInfo.Add_ep_stmts(pb.be.responseEntry, pb.be.epstmts);
			}
			return true;
		}
		return false;
	}
	private static boolean String_Semantic()
	{
		if (InvokeHandler.isStaticInvoke(static_pb.invokeType))
		{
			StaticInvokeExpr sie = (StaticInvokeExpr) static_pb.ie;
		}
		else
		{
			InstanceInvokeExpr iie = (InstanceInvokeExpr) static_pb.ie;
		}
		return false;
	}
	private static boolean JSON_Semantic()
	{
		HashMap<String, ArrayList<BFNode_Response>> BFTtable = static_pb.BFTtable;
		if (InvokeHandler.isStaticInvoke(static_pb.invokeType))
		{
			StaticInvokeExpr sie = (StaticInvokeExpr) static_pb.ie;
		}
		else
		{
			InstanceInvokeExpr iie = (InstanceInvokeExpr) static_pb.ie;
			String methodref = Constants.Deobfuse(iie.getMethodRef().getSignature().toString());
			if (iie.getMethodRef().toString().equals("<org.json.JSONObject: org.json.JSNObject opt(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: org.json.JSONObject getJSONObject(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: org.json.JSONObject optJSONObject(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.codehaus.jackson.JsonNode: org.codehaus.jackson.JsonNode get(java.lang.String)>"))
			{
				makeBFNode(0, iie);
				return true;
			}
			if (iie.getMethodRef().toString().equals("<org.json.JSONObject: java.lang.String getString(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: java.lang.String optString(java.lang.String,java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: java.lang.String optString(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: boolean optBoolean(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: boolean optBoolean(java.lang.String,boolean)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: int optInt(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: int getInt(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: long getLong(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: int optInt(java.lang.String,int)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: long optLong(java.lang.String,long)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: long optLong(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: org.json.JSONArray getJSONArray(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: boolean getBoolean(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONArray: org.json.JSONObject getJSONObject(int)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: org.json.JSONArray optJSONArray(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: double optDouble(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONArray: java.lang.String getString(int)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONArray: org.json.JSONObject optJSONObject(int)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: double getDouble(java.lang.String)>"))
			{
				makeBFNode(0, iie);
				return true;
			}
			if (iie.getMethodRef().toString().equals("<org.json.JSONArray: org.json.JSONObject getJSONObject(int)>"))
			{
				String base = iie.getBase().toString();
				BFTtable.put(static_pb.strDest, BFTtable.get(base));
			}
			if (iie.getMethodRef().toString().equals("<com.pinterest.network.json.PinterestJsonObject: com.pinterest.network.json.PinterestJsonObject b(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<com.pinterest.network.json.PinterestJsonObject: java.lang.String a(java.lang.String,java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<com.pinterest.network.json.PinterestJsonArray: com.pinterest.network.json.PinterestJsonObject d(int)>")
					|| iie.getMethodRef().toString().equals("<com.pinterest.network.json.PinterestJsonObject: com.pinterest.network.json.PinterestJsonArray d(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<com.pinterest.network.json.PinterestJsonObject: boolean e(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<com.pinterest.network.json.PinterestJsonObject: int a(java.lang.String,int)>"))
			{
				// makeBFNode(0, iie);
				// return true;
			}
			if (iie.getMethodRef().toString().equals("<com.google.gson.JsonObject: com.google.gson.JsonElement get(java.lang.String)>"))
			{
				makeBFNode(0, iie);
				return true;
			}
			if (iie.getMethodRef().toString().equals("<org.json.JSONObject: void <init>(java.lang.String)>"))
			{
				// String -> Json Init
				TaintHelper.seedGenerate(iie.getArg(0).toString(), static_pb);
				ParameterBucket.add_seed(iie.getArg(0).toString(), static_pb);
				TaintHelper.tryVariableTaint(iie.getBase().toString(), iie.getArg(0).toString(), static_pb);
				return true;
			}
			if (iie.getMethodRef().toString().equals("<com.google.gson.Gson: java.lang.Object fromJson(java.lang.String,java.lang.Class)>")
					|| iie.getMethodRef().toString().equals("<com.fasterxml.jackson.databind.ObjectMapper: java.lang.Object readValue(java.io.InputStream,java.lang.Class)>")
					|| methodref.equals("<com.fasterxml.jackson.databind.ObjectMapper: java.lang.Object readValue(java.lang.String,java.lang.Class)>")
					|| methodref.equals("<com.google.gson.Gson: java.lang.Object fromJson(com.google.gson.JsonElement,java.lang.Class)>")
					|| methodref.equals("<org.codehaus.jackson.map.ObjectMapper: java.lang.Object readValue(java.io.InputStream,java.lang.Class)>")
					)
			{
				String arg0 = iie.getArg(0).toString();
				String arg1 = iie.getArg(1).toString();
				String base = iie.getBase().toString();
				String namespace = "";
				String result = "";
				if (iie.getArg(1) instanceof Constant)
					namespace = arg1;
				else
					if (BFTtable.get(arg1) != null && BFTtable.get(arg1).size() > 0 && BFTtable.get(arg1).get(0) != null && BFTtable.get(arg1).get(0).stringValue != null
							&& !BFTtable.get(arg1).get(0).stringValue.equals(""))
						namespace = BFTtable.get(arg1).get(0).stringValue;
					else
						namespace = Constants.classparam;
				namespace = namespace.replace("class ", "").replaceAll("\"", "").replaceAll("/", ".");
				if (namespace.contains("Alert401Response"))
					return true;
				try
				{
					result = retrofit_http.fromjson(Constants.javaPath, namespace, new HashSet<String>(), "");
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				ArrayList<BFNode_Response> list = BFTtable.get(arg0);
				if (list == null)
				{
					list = new ArrayList<BFNode_Response>();
					BFTtable.put(arg0, list);
				}
				
				if(Constants.isDebug){
					System.out.println("CLASS : " + namespace);
					System.out.println("fromjson result : " + result);
				}
				
				// BK Keep the result in ResponseInfoList
				keepResult(result, static_pb.sm.toString());
				
				BFNode_Response bfn = new BFNode_Response();
				bfn.makeJSONBfn(result);
				list.add(bfn);
				return true;
			}
			if (methodref.equals("<com.fasterxml.jackson.databind.ObjectMapper: com.fasterxml.jackson.databind.ObjectReader reader(java.lang.Class)>"))
			{
				String arg0 = iie.getArg(0).toString();
				if (iie.getArg(0) instanceof Constant)
					Constants.classparam = arg0;
				return true;
			}
			if (methodref.equals("<com.fasterxml.jackson.databind.ObjectReader: java.lang.Object readValue(java.io.InputStream)>"))
			{
				String arg0 = iie.getArg(0).toString();
				String namespace = "";
				String result = "";
				namespace = Constants.classparam;
				if (namespace.contains("Alert401Response"))
					return true;
				namespace = namespace.replace("class ", "").replaceAll("\"", "").replaceAll("/", ".");
				try
				{
					result = retrofit_http.fromjson(Constants.javaPath, namespace, new HashSet<String>(), "");
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				if(Constants.isDebug){
					System.out.println("CLASS : " + namespace);
					System.out.println("fromjson result : " + result);
				}
				
				// BK Keep the result in ResponseInfoList
				keepResult(result, static_pb.sm.toString());
				
				ArrayList<BFNode_Response> list = BFTtable.get(arg0);
				if (list == null)
				{
					list = new ArrayList<BFNode_Response>();
					BFTtable.put(arg0, list);
				}
				BFNode_Response bfn = new BFNode_Response();
				bfn.makeJSONBfn(result);
				list.add(bfn);
				return true;
			}
			// By Byungkwon
			if (iie.getMethodRef().toString().equals("<java.lang.String: boolean equals(java.lang.Object)>"))
			{
				String arg0 = iie.getArg(0).toString();
				// Adopt this sementic model only when the argument type is
				// "Jackson_Key"
				if (static_pb.variable_type != null)
					if (static_pb.variable_type.get(arg0) != null)
						if (static_pb.variable_type.get(arg0).equals("Jackson_Key"))
						{
							String base = iie.getBase().toString(); // key
							String arg0String;
							String strDest;
							String JacksonParser = "";
							for (String key : static_pb.variable_type.keySet())
							{
								if (static_pb.variable_type.get(key).equals("Jackson_Parser"))
								{
									JacksonParser = key;
									break;
								}
							}
							// 1. Extract strDest
							if (InvokeHandler.isAssignStmt(static_pb.invokeType))
							{
								strDest = static_pb.strDest;
							}
							else
							{
								strDest = null;
							}
							ArrayList<BFNode_Response> list = BFTtable.get(JacksonParser);
							if (list == null)
							{
								list = new ArrayList<BFNode_Response>();
								BFTtable.put(JacksonParser, list);
							}
							BFNode_Response bfn = new BFNode_Response();
							// Set parent
							ParameterBucket.set_JSON_Parent(strDest, base, static_pb);
							ParameterBucket.set_JSON_Parent(base, arg0, static_pb);
							// Extract string from the base
							arg0String = BFTTableHelper.generateStringForVariable(base, static_pb);
							// Add the BFNode
							// TODO : We have to change the first argument
							// (arg0String.split ...).
							bfn.makeJsonBfn_Direct(arg0String.split(":")[0], "(.*)", BFNode_Response.JSON_TYPE.JSON_TYPE_STRING);
							list.add(bfn);
							return true;
						}
			}
		}
		return false;
	}
	
	// BK for semantic model of JSON with class
	private static void keepResult(String res, String CurrentMethod){
		// 1. Keep EP information in the EP list
		if(!PairingDPEntry.isEntryIncluded(new SemanticAppliedEntry(static_pb.sm.getSignature(), static_pb.ie.getMethodRef().getSignature()), Constants.SemanticMethodAndStmt))
			Constants.SemanticMethodAndStmt.add(new SemanticAppliedEntry(static_pb.sm.getSignature(), static_pb.ie.getMethodRef().getSignature()));
		
		// 2. Keep the result with the EP list
		SignatureBuilder_Response.keepSignatureInResponseInfoList(res, CurrentMethod);
		
		// 3. It is for avoiding redundant addition in list 'Constants.SemanticMethodAndStmt'.
		isJSONwithCLASS = true;
	}

	// Brief
	//
	// @param arg_index the index number of the argument
	//
	// By Byungkwon
	private static void makeBFNode(int arg_index, InstanceInvokeExpr iie)
	{
		HashMap<String, ArrayList<BFNode_Response>> BFTtable = static_pb.BFTtable;
		String strDest;
		
		// 1. Extract strDest
		if (InvokeHandler.isAssignStmt(static_pb.invokeType))
			strDest = static_pb.strDest;
		else
			strDest = null;

		// TODO: What if arg0 is not constant but variable or heap?
		String arg0 = iie.getArg(arg_index).toString();
		String base = iie.getBase().toString();
		String arg0String = arg0;
		
		if (!SemanticHelper.isconst(arg0))
		{
			arg0String = BFTTableHelper.generateStringForVariable(arg0, static_pb);
			// Add regex if the arg has no content in the BFTtable
			if (arg0String.equals(""))
				if (iie.getArg(arg_index).getType().toString().contains("String"))
					arg0String = "(.*)";
				else
					if (iie.getArg(arg_index).getType().toString().contains("int"))
						arg0String = "([0-9]+)";
					else
						if (iie.getArg(arg_index).getType().toString().contains("int"))
							arg0String = "([0-9]+)";
		}
		
		ArrayList<BFNode_Response> list = BFTtable.get(base);
		
		if (list == null)
		{
			list = new ArrayList<BFNode_Response>();
			BFTtable.put(base, list);
		}
		
		BFNode_Response bfn = new BFNode_Response();
		
		// Set parent
		ParameterBucket.set_JSON_Parent(strDest, base, static_pb);
		
		// Check the type of return value
		if (iie.getMethodRef().returnType().toString().toLowerCase().contains("json"))
			bfn.makeJsonBfn_Indirect(arg0String, strDest, BFNode_Response.JSON_TYPE.JSON_TYPE_JSONOBJECT); // indirect
		else
			bfn.makeJsonBfn_Direct(arg0String, strDest, BFNode_Response.JSON_TYPE.JSON_TYPE_STRING); // direct
		
		list.add(bfn);
		
		// BK Add JSON keys (it will be used to track JSON key hierarchy.)
		// 1. add JSON key list of base
		static_pb.varTable.setJSONKeyList(strDest, static_pb.varTable.getJSONKeyList(base));
		
		// 2. add JSON key (argument)
		// TODO: What if arg0 is variable?
		if(iie.getArg(arg_index) instanceof Constant)
			static_pb.varTable.addJSONKey(strDest, arg0);
	}
}
