
package extractocol.backend.response.semantic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.json.simple.JSONArray;

import extractocol.Constants;
import extractocol.backend.request.SignatureBuilder_Request;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.retrofit.retrofit_http;
import extractocol.backend.response.SignatureBuilder_Response;
import extractocol.backend.response.basics.BFNode_Response;
import extractocol.backend.response.basics.ParameterBucket;
import extractocol.backend.response.helper.BFTTableHelper;
import extractocol.backend.response.helper.SemanticHelper;
import extractocol.backend.response.helper.TaintHelper;
import extractocol.backend.response.unitHandle.InvokeHandler;
import extractocol.common.outputs.backendOutputHelper.ResponseInfoList;
import extractocol.common.pairing.PairingDPEntry;
import extractocol.common.pairing.SemanticAppliedEntry;
import extractocol.common.pairing.SemanticAppliedList;
import extractocol.common.retrofit.struct.Transaction;
import extractocol.common.retrofit.utils.ResponseFileAnalyzer;
import extractocol.common.valueEntry.ValueEntryTracking;
import extractocol.common.valueEntry.helper.General;
import soot.jimple.Constant;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.StaticInvokeExpr;

public class ResponseSemantic
{
	@SuppressWarnings("unchecked")
	public static boolean process(ParameterBucket pb)
	{
		if (String_Semantic(pb)) {
			//postProcess();
			return true;
		}
		else if (JSON_Semantic(pb, pb.BFTtable, pb.strDest, pb.ie, InvokeHandler.isStaticInvoke(pb.invokeType), true)) {
			//postProcess();
			return true;
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	/*private static void postProcess() {
		ParameterBucket pb = static_pb;
		
		// For Pairing Information
		if (pb.be.responseEntry != null) {
			// BK Add the unit information if this unit does not include JSON parser using
			// class such as readValue
			if (!isJSONwithCLASS)
				if (!PairingDPEntry.isEntryIncluded(
						new SemanticAppliedEntry(pb.sm.getSignature(), pb.ie.getMethodRef().getSignature()),
						Constants.SemanticMethodAndStmt))
					Constants.SemanticMethodAndStmt
							.add(new SemanticAppliedEntry(pb.sm.getSignature(), pb.ie.getMethodRef().getSignature()));

			pb.be.ep_methods.add(pb.sm.getSignature());
			Constants.PairInfo.Add_ep_method(pb.be.responseEntry, pb.be.ep_methods);
			pb.be.epstmts.add(pb.ie.getMethodRef().getSignature());
			Constants.PairInfo.Add_ep_stmts(pb.be.responseEntry, pb.be.epstmts);
		}
	}*/
	
	private static boolean String_Semantic(ParameterBucket pb)
	{
		// Check whether there exist semantic model for the invoke statement
		if(!SignatureBuilder_Request.SemanticModel.contains(pb.ie.getMethod().toString()))
			return false;
		
		return New_Semantics(pb);
	}
	
	private static boolean New_Semantics(ParameterBucket pb) {
		String method = pb.ie.getMethod().toString();
		if(method.equals("<com.fasterxml.jackson.databind.ObjectMapper: java.lang.Object convertValue(java.lang.Object,java.lang.Class)>")) {
			
			if(!(pb.ie instanceof InstanceInvokeExpr))
				return false;
			
			InstanceInvokeExpr iie = (InstanceInvokeExpr) pb.ie;
			
			// 1. get JSON key list from arg0
			List<String> keyList = pb.varTable.getJSONKeyListOf(iie.getArg(0).toString());

			// 2. get type list form CollectionType arg1
			String className = pb.varTable.getClassNameOf(iie.getArg(1).toString());

			// 3. parse Java file of arg1 to extract JSON key list
			Transaction t = ResponseFileAnalyzer.Parser(className);

			// 4. add the JSON key list from arg0 into all JSON key entries of the extracted
			// heap
			t.Response().VET().addJSONKeyListAtFront(keyList);

			// 5. merge heap table
			ValueEntryTracking.MergeTables(pb.heapTable, t.Response().VET());

			// 6. clear
			t.clear();

			return true;
		}
		else if (method.equals("<com.fasterxml.jackson.databind.ObjectMapper: java.lang.Object convertValue(java.lang.Object,com.fasterxml.jackson.databind.JavaType)>")) {
			if(!(pb.ie instanceof InstanceInvokeExpr))
				return false;
			
			InstanceInvokeExpr iie = (InstanceInvokeExpr) pb.ie;
			
			// 1. get JSON key list from arg0
			List<String> keyList = pb.varTable.getJSONKeyListOf(iie.getArg(0).toString());

			// 2. get type list form CollectionType arg1
			List<String> typeList = pb.varTable.getCollectionTypeList(iie.getArg(1).toString());

			// 3. parse Java file of arg1 to extract JSON key list
			Transaction t = ResponseFileAnalyzer.Parser(typeList.get(1));

			// 4. add the JSON key list from arg0 into all JSON key entries of the extracted
			// heap
			t.Response().VET().addJSONKeyListAtFront(keyList);

			// 5. merge heap table
			ValueEntryTracking.MergeTables(pb.heapTable, t.Response().VET());

			// 6. clear
			t.clear();

			return true;
		}
		else if (method.equals("<com.fasterxml.jackson.databind.type.TypeFactory: com.fasterxml.jackson.databind.type.CollectionType constructCollectionType(java.lang.Class,java.lang.Class)>")) {
			if(!(pb.ie instanceof InstanceInvokeExpr))
				return false;
			
			InstanceInvokeExpr iie = (InstanceInvokeExpr) pb.ie;
			// 1. get args and dest
			String arg0 = iie.getArg(0).toString();
			String arg1 = iie.getArg(1).toString();
			String dest = pb.strDest;

			// 2. get class name
			String class0 = pb.varTable.getClassNameOf(arg0);
			String class1 = pb.varTable.getClassNameOf(arg1);

			// 3. add the class name into CollectionType entry
			pb.varTable.addCollectionTypeEntry(dest, class0, false);
			pb.varTable.addCollectionTypeEntry(dest, class1, false);
			return true;
		}
		
		return false;
	}
	
	public static boolean JSON_Semantic(ValueEntryTracking pb, HashMap<String, ArrayList<BFNode_Response>> BFTtable, String strDest, InvokeExpr ie, boolean isStaticInvoke, boolean isForward)
	{
		if (!isStaticInvoke)
		{
			InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
			String methodref = Constants.Deobfuse(iie.getMethodRef().getSignature().toString());
			if (iie.getMethodRef().toString().equals("<org.json.JSONObject: org.json.JSNObject opt(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: org.json.JSONObject getJSONObject(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: org.json.JSONObject optJSONObject(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.codehaus.jackson.JsonNode: org.codehaus.jackson.JsonNode get(java.lang.String)>"))
			{
				makeBFNode(0, iie, strDest, BFTtable, pb, isForward);
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
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: org.json.JSONArray optJSONArray(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: double optDouble(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONArray: java.lang.String getString(int)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONArray: org.json.JSONObject optJSONObject(int)>")
					|| iie.getMethodRef().toString().equals("<org.json.JSONObject: double getDouble(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<com.fasterxml.jackson.databind.JsonNode: com.fasterxml.jackson.databind.JsonNode get(java.lang.String)>"))
			{
				makeBFNode(0, iie, strDest, BFTtable, pb, isForward);
				return true;
			}
			if (iie.getMethodRef().toString().equals("<org.json.JSONArray: org.json.JSONObject getJSONObject(int)>"))
			{
				String base = iie.getBase().toString();
				
				/*if(isForward)
					BFTtable.put(strDest, BFTtable.get(base));*/
				
				// BK add JSON key list of base
				pb.varTable.OverwriteJSONKeyListFromTo(strDest, base);
			}
			if (iie.getMethodRef().toString().equals("<com.pinterest.network.json.PinterestJsonObject: com.pinterest.network.json.PinterestJsonObject b(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<com.pinterest.network.json.PinterestJsonObject: java.lang.String a(java.lang.String,java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<com.pinterest.network.json.PinterestJsonArray: com.pinterest.network.json.PinterestJsonObject d(int)>")
					|| iie.getMethodRef().toString().equals("<com.pinterest.network.json.PinterestJsonObject: com.pinterest.network.json.PinterestJsonArray d(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<com.pinterest.network.json.PinterestJsonObject: boolean e(java.lang.String)>")
					|| iie.getMethodRef().toString().equals("<com.pinterest.network.json.PinterestJsonObject: int a(java.lang.String,int)>")
					|| iie.getMethodRef().toString().equals("<com.pinterest.e.c.d: java.lang.String a(java.lang.String,java.lang.String)>"))
			{
				 makeBFNode(0, iie, strDest, BFTtable, pb, isForward);
				 return true;
			}
			if (iie.getMethodRef().toString().equals("<com.google.gson.JsonObject: com.google.gson.JsonElement get(java.lang.String)>"))
			{
				makeBFNode(0, iie, strDest, BFTtable, pb, isForward);
				return true;
			}
			if (iie.getMethodRef().toString().equals("<org.json.JSONObject: void <init>(java.lang.String)>"))
			{
				if(isForward){
					// String -> Json Init
					/*TaintHelper.seedGenerate(iie.getArg(0).toString(), static_pb);
					ParameterBucket.add_seed(iie.getArg(0).toString(), static_pb);
					TaintHelper.tryVariableTaint(iie.getBase().toString(), iie.getArg(0).toString(), static_pb);*/
					return true;
				}
			}
			if (iie.getMethodRef().toString().equals("<com.google.gson.Gson: java.lang.Object fromJson(java.lang.String,java.lang.Class)>")
					|| iie.getMethodRef().toString().equals("<com.fasterxml.jackson.databind.ObjectMapper: java.lang.Object readValue(java.io.InputStream,java.lang.Class)>")
					|| methodref.equals("<com.fasterxml.jackson.databind.ObjectMapper: java.lang.Object readValue(java.lang.String,java.lang.Class)>")
					|| methodref.equals("<com.google.gson.Gson: java.lang.Object fromJson(com.google.gson.JsonElement,java.lang.Class)>")
					|| methodref.equals("<org.codehaus.jackson.map.ObjectMapper: java.lang.Object readValue(java.io.InputStream,java.lang.Class)>")
					)
			{
				/*if(isForward){
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
					
					// BK Keep the result in ResponseInfoList (Temporarily commented by Byungkwon (20170209))
					//keepResult(result, static_pb.sm.toString());
					
					BFNode_Response bfn = new BFNode_Response();
					bfn.makeJSONBfn(result);
					list.add(bfn);
					return true;
				}*/
				return true;
			}
			if (methodref.equals("<com.fasterxml.jackson.databind.ObjectMapper: com.fasterxml.jackson.databind.ObjectReader reader(java.lang.Class)>"))
			{
				/*String arg0 = iie.getArg(0).toString();
				if (iie.getArg(0) instanceof Constant)
					Constants.classparam = arg0;*/
				return true;
			}
			if (methodref.equals("<com.fasterxml.jackson.databind.ObjectReader: java.lang.Object readValue(java.io.InputStream)>"))
			{
				/*if(isForward){
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
					
					// BK Keep the result in ResponseInfoList (Temporarily commented by Byungkwon (20170209))
					//keepResult(result, static_pb.sm.toString());
					
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
				}*/
				return true;
			}
			
			// Temporarily commented by Byungkwon (20170209)
			/*if (iie.getMethodRef().toString().equals("<java.lang.String: boolean equals(java.lang.Object)>"))
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
			}*/
		}
		return false;
	}
	
	// BK for semantic model of JSON with class
	/*private static void keepResult(String res, String CurrentMethod){
		// 1. Keep EP information in the EP list
		if(!PairingDPEntry.isEntryIncluded(new SemanticAppliedEntry(static_pb.sm.getSignature(), static_pb.ie.getMethodRef().getSignature()), Constants.SemanticMethodAndStmt))
			Constants.SemanticMethodAndStmt.add(new SemanticAppliedEntry(static_pb.sm.getSignature(), static_pb.ie.getMethodRef().getSignature()));
		
		// 2. Keep the result with the EP list
		SignatureBuilder_Response.keepSignatureInResponseInfoList(res, CurrentMethod);
		
		// 3. It is for avoiding redundant addition in list 'Constants.SemanticMethodAndStmt'.
		isJSONwithCLASS = true;
	}*/

	// Brief
	//
	// @param arg_index the index number of the argument
	//
	// By Byungkwon
	private static void makeBFNode(int arg_index, InstanceInvokeExpr iie, String strDest, HashMap<String, ArrayList<BFNode_Response>> BFTtable, ValueEntryTracking pb, boolean isForward)
	{
		String arg0 = iie.getArg(arg_index).toString();
		String base = iie.getBase().toString();

		/*if(isForward){
			// 1. Extract strDest
			if (InvokeHandler.isAssignStmt(static_pb.invokeType))
				strDest = static_pb.strDest;
			else
				strDest = null;

			// TODO: What if arg0 is not constant but variable or heap?
			String arg0String = arg0;
			
			if (!SemanticHelper.isconst(arg0))
			{
				arg0String = BFTTableHelper.generateStringForVariable(arg0, pb);
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
		}*/
		
		// BK Add JSON keys (it will be used to track JSON key hierarchy.)
		// 1. add JSON key list of base
		pb.varTable.OverwriteJSONKeyListFromTo(strDest, base);
		
		// 2. add JSON key (argument)
		// 2-1. Case 1: when arg is constant
		if(iie.getArg(arg_index) instanceof Constant)
			pb.varTable.addJSONKey(strDest, General.getConstantName(arg0), true); // Remove double quotation marks (e.g., "key" -> key)
		else
			pb.varTable.addJSONKey(strDest, pb.varTable.GenRegex(arg0), false);
	}
}
