package extractocol.backend.response.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import extractocol.backend.response.basics.BFNode_Response;
import extractocol.backend.response.basics.ParameterBucket;
import extractocol.backend.response.basics.BFNode_Response.VAR_TYPE;
import extractocol.backend.response.unitHandle.InvokeHandler;
import soot.Unit;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;

public class BFTTableHelper {

	
	public static void printBFTTable(ParameterBucket pb)
	{
		HashMap<String, ArrayList<BFNode_Response>> BFTtable = pb.BFTtable;
		for(String key : BFTtable.keySet()) {
			ArrayList<BFNode_Response> list = BFTtable.get(key);
			if(pb.taintVariableAndSeedPair.keySet().contains(key)) {
				System.out.print("[Seed-" + pb.taintVariableAndSeedPair.get(key) + "] " + key + " -> ");
			}
			else {
				System.out.print("[Tainted X] " + key + " -> ");
			}
			for (BFNode_Response bfn : list)
			{
				switch(bfn.type) {
				case VAR_TYPE_NONE :
					System.out.print("[NONE]");
					break;
				case VAR_TYPE_PHINODE :
					System.out.print("[PhiNode : " + bfn.stringValue + "]");
					break;
				case VAR_TYPE_STRING :
					System.out.print("[String : " + bfn.stringValue + "]");
					break;
				case VAR_TYPE_JSON :
					switch(bfn.jsonVtype) {
					case JSON_TYPE_NONE :
						break;
					case JSON_TYPE_STRING :
						System.out.print("[JSON : " + bfn.jsonKey + ": (.*)]");
						break;
					case JSON_TYPE_JSONOBJECT :
						System.out.print("[JSON : " + bfn.jsonKey + ": " + bfn.jsonValue + "]");
						break;
					case JSON_TYPE_JSONTREE :
						System.out.print("[JSONTree : " + bfn.treeLeft + "|" + bfn.treeMarker + "|" + bfn.treeRight + "]");
						break;
					}
					
					break;
				case VAR_TYPE_XML :
					
					break;
				}
			}
			System.out.println();
		}
	}
	
	public static String generateStringForVariable(String var, ParameterBucket pb) {
		
		HashMap<String, ArrayList<BFNode_Response>> BFTtable = pb.BFTtable;
		
		String result = generateStringForVariable_Recursive(BFTtable, var);
		
		// Add the most outside brackets if the current method is EP.
		// By Byungkwon
		if(TaintHelper.isEpMethod(pb.sm))
			result = "{" + result + "}";
		
		return result;
	}
	
	// By Byungkwon
	private static String generateStringForVariable_Recursive(HashMap<String, ArrayList<BFNode_Response>> BFTtable, String var)
	{
		String str = "";
		String result = "";
		
		ArrayList<BFNode_Response> list = BFTtable.get(var);
		ArrayList<String> output = new ArrayList<String>();
		
		if (list == null || list.size() == 0)
			return "";
		
		for (BFNode_Response bfn : list)
		{
			switch(bfn.type) {
			case VAR_TYPE_NONE :
				str += "ERROR1";
				break;
			case VAR_TYPE_PHINODE :
				str += generateStringForVariable_Recursive(BFTtable, bfn.stringValue);
				break;
			case VAR_TYPE_STRING :
				str += bfn.stringValue;
				break;
			case VAR_TYPE_JSON :
				switch(bfn.jsonVtype) {
				case JSON_TYPE_NONE :
					str += "ERROR2";
					break;
				case JSON_TYPE_FINAL :
					str += bfn.stringValue;
					break;
				case JSON_TYPE_STRING :
					if(bfn.value_type == BFNode_Response.VALUE_TYPE.VALUE_TYPE_INDIRECT){
						str += bfn.jsonKey + ": ";
						str += generateStringForVariable_Recursive(BFTtable, bfn.jsonValue);
					}else
						str += bfn.jsonKey + ": (.*)";
					break;
				case JSON_TYPE_JSONOBJECT :
					str += bfn.jsonKey + ": {";
					if (BFTtable.get(bfn.jsonValue).size() > 0)
						str += generateStringForVariable_Recursive(BFTtable, bfn.jsonValue);
					else
						str += "(.*)";
					str += "}";						
					break;
				case JSON_TYPE_JSONTREE :
					str += bfn.treeLeft;
					if(bfn.treeMarker == null) {
						str += "(.*) ";
					}
					else {
						String temp = generateStringForVariable_Recursive(BFTtable, bfn.treeMarker);
						if(temp != null && temp != "") {
							str += ", " + temp;
						}
					}
					str += bfn.treeRight;
					break;
				}
				break;
			case VAR_TYPE_XML :
				
				break;
			}
				
			output.add(str);
			str = "";
		}

		// compact list (remove ""'s)
		ArrayList<String> compact = new ArrayList<String>();
		for (int i = 0; i < output.size(); i++) {
			String o = output.get(i);
			if (!o.equals("")) 
				compact.add(o);
		}
		
		// Add comma if this node is not the last node.
		for (int i = 0; i < compact.size(); i++) {
			String o = compact.get(i);
			result += o;
			if(i < compact.size() - 1){
				result += ", ";
			}
		}	
		return result;
	}
	
// Original
//	private static String generateStringForVariable_Recursive(HashMap<String, ArrayList<BFNode_Response>> BFTtable, String var)
//	{
//		String result = "";
//		
//		ArrayList<BFNode_Response> list = BFTtable.get(var);
//		if (list == null || list.size() == 0)
//			return "(.*)";
//		
//		if (list.get(0).type !=  BFNode_Response.VAR_TYPE.VAR_TYPE_PHINODE)
//			result += "{";
//		
//		for (BFNode_Response bfn : list)
//		{
//			
//			String str = "";
//			switch(bfn.type) {
//			case VAR_TYPE_NONE :
//				str += "ERROR1";
//				break;
//			case VAR_TYPE_PHINODE :
//				if (BFTtable.get(bfn.phinodeVar).size() > 0)
//					str += generateStringForVariable_Recursive(BFTtable, bfn.phinodeVar);
//				else
//					str += "(.*)";
//				break;
//			case VAR_TYPE_STRING :
//				str += bfn.stringValue;
//				break;
//			case VAR_TYPE_JSON :
//				switch(bfn.jsonVtype) {
//				case JSON_TYPE_NONE :
//					str += "ERROR2";
//					break;
//				case JSON_TYPE_STRING :
//					str += bfn.jsonKey + ": (.*)";
//					break;
//				case JSON_TYPE_JSONOBJECT :
//					str += bfn.jsonKey + ": ";
//					if (BFTtable.get(bfn.jsonValue).size() > 0)
//						str += generateStringForVariable_Recursive(BFTtable, bfn.jsonValue);
//					else
//						str += "(.*)";
//
//					break;
//				}
//				break;
//			case VAR_TYPE_XML :
//				
//				break;
//			}
//			
//			if (!str.equals(""))
//			{
//				if (!result.equals("{"))
//					if (list.get(0).type !=  BFNode_Response.VAR_TYPE.VAR_TYPE_PHINODE)
//						result += ", ";
//				result += str;
//			}
//		}
//		
//		if (list.get(0).type !=  BFNode_Response.VAR_TYPE.VAR_TYPE_PHINODE)
//			result += "}";
//		
//		return result;
//	}

	public static Set<String> getRootVarSetFromSeed(String seed, ParameterBucket pb) {
		
		HashMap<String, String> taintVariableAndSeedPair = pb.taintVariableAndSeedPair;
		HashMap<String, ArrayList<BFNode_Response>> BFTtable = pb.BFTtable;
		
		Set<String> varSet = new HashSet<String>();
		Set<String> notRootVarSet = new HashSet<String>();


		for (String key : taintVariableAndSeedPair.keySet())
		{
			if (TaintHelper.getSeedForVariable(key, pb).equals(seed))
			{
				varSet.add(key);
				ArrayList<BFNode_Response> list = BFTtable.get(key);
				if (list != null)
				{
					for (BFNode_Response bfn : list)
					{
						if(bfn.type == BFNode_Response.VAR_TYPE.VAR_TYPE_JSON && bfn.jsonVtype == BFNode_Response.JSON_TYPE.JSON_TYPE_JSONOBJECT)
							notRootVarSet.add(bfn.jsonValue);
						if(bfn.type == BFNode_Response.VAR_TYPE.VAR_TYPE_JSON && bfn.jsonVtype == BFNode_Response.JSON_TYPE.JSON_TYPE_JSONTREE && bfn.treeMarker != null)
							notRootVarSet.add(bfn.treeMarker);
						if(bfn.type == BFNode_Response.VAR_TYPE.VAR_TYPE_PHINODE)
							notRootVarSet.add(bfn.stringValue);
					}
				}
			}
		}
		
		varSet.removeAll(notRootVarSet);

	    // Remove all but JSON objects
		for (String s: varSet) {
			// remove s if s is a parameter.
			// It is because parameter is just seed and the other variable is tainted by the parameter if the parameter was tainted.
			if(s.contains("@parameter0")){
				notRootVarSet.add(s);
				break;
			}
			
			// getLocals() give you Jimple variables while varSet contains Shimple variables
			for ( Value l : pb.sm.getActiveBody().getLocals())
			{
				// TODO: add more semantics
				if(!l.getType().toString().toLowerCase().contains("json")) {
					String[] split= s.split("_");
					// removing $r0, $r0_1, etc.
					if (split.length>=1 && split[0].equals(l.toString())) {
						notRootVarSet.add(s);
						
						// BK The variable s should be included in the root set if its type is JSON.
						if(TaintHelper.isJSON(s, pb))
							notRootVarSet.remove(s);
						
						break;
					}
				}
			}
			
			
						
						
					
		}
		
//		// Remove a variable from root set if the parent of a variable is not seed 
//		// By Byungkwon
//		for (String v: varSet) {
//			if(pb.json_child_parent.get(v) != null){
//				boolean parent_is_seed = false; 
//				
//				for(String s: pb.seed){
//					// check whether its parent is one of the seeds
//					if(pb.json_child_parent.get(v).toString().equals(s)){
//						parent_is_seed = true;
//						break;
//					}
//				}
//				
//				// remove the variable from root set if the parent does not exist in the seed set.
//				if(!parent_is_seed)
//					notRootVarSet.add(v);
//			}
//		}
//		
//		varSet.removeAll(notRootVarSet);
		
		varSet.removeAll(notRootVarSet);
		return varSet;
	}

	public static void reflectTaintParametersToBFTTable(ParameterBucket pb) {
		
		InvokeExpr ie = pb.ie;
		HashMap<String, String> taintVariableAndSeedPair = pb.taintVariableAndSeedPair;
		HashMap<String, ArrayList<BFNode_Response>> BFTtable = pb.BFTtable;
		
		for (int i = 0; i < ie.getArgCount(); i++)
		{
			String arg = ie.getArg(i).toString();
			if (taintVariableAndSeedPair.containsKey(arg))
			{
				String parameter = "@parameter" + i;
				BFNode_Response res = TaintHelper.GeneratedStringStack_TaintedParameters.getLast().get(parameter);
				if(res != null){
					ArrayList<BFNode_Response> list = BFTtable.get(arg);
					list.add(res);
				}
			}
		}
	}

	public static void reflectBaseTaintToBFTTable(ParameterBucket pb) {

		HashMap<String, ArrayList<BFNode_Response>> BFTtable = pb.BFTtable;
		Unit ut = pb.ut;
		String strDest = pb.strDest;
		int invokeType = pb.invokeType;

		if (InvokeHandler.isInstanceInvoke(invokeType))
		{
			InstanceInvokeExpr iie = (InstanceInvokeExpr) pb.ie;
			String base = iie.getBase().toString();

			if(TaintHelper.GeneratedStringStack_BaseTaint.getLast().containsKey("@this")) {
				String stringValue = TaintHelper.GeneratedStringStack_BaseTaint.getLast().get("@this");
				if(strDest == null) {
					ArrayList<BFNode_Response> list = BFTtable.get(base);
					BFNode_Response bfn = new BFNode_Response();
					bfn.makeStringBfn_Direct(stringValue);
					list.add(bfn);
				}
				else {
					ArrayList<BFNode_Response> list = BFTtable.get(base);
					BFNode_Response bfn = new BFNode_Response();
					stringValue = stringValue.substring(0, stringValue.length()-1); 
					bfn.makeJsonTreeBfn(stringValue, strDest, "}");
					list.add(bfn);
				}
			}
		}
	}
}