package Extractocol.BufferExtractor_Response.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Response.Basics.BFNode_Response;
import Extractocol.BufferExtractor_Response.Basics.BFNode_Response.VAR_TYPE;
import Extractocol.BufferExtractor_Response.Basics.ParameterBucket;
import Extractocol.BufferExtractor_Response.UnitHandle.InvokeHandler;
import Extractocol.Pairing.SemanticAppliedEntry;

public class TaintHelper {

	
	public static LinkedList<HashMap<String, BFNode_Response>> GeneratedStringStack_TaintedParameters = new LinkedList<HashMap<String, BFNode_Response>>();
	public static LinkedList<HashMap<String, String>> GeneratedStringStack_ReturnedSeeds = new LinkedList<HashMap<String, String>>();
	public static LinkedList<HashMap<String, String>> GeneratedStringStack_BaseTaint = new LinkedList<HashMap<String, String>>();
	
	public static boolean isEpMethod(SootMethod sm)
	{
		// TODO 1. need to access current EP.
		//      2. compare the current EP with the current method.
		//      3. return true if a match is found.
		return false;
	}
	
	public static boolean isDpStmt(SootMethod sm, Unit ut)
	{
		if(Constants.CurrentParentMethod.equals(sm.getSignature()) && Constants.DPStmt.equals(ut.toString())) {
			return true;
		}
		return false;
	}
	
	public static void seedGenerate(String seed, ParameterBucket pb)
	{
		pb.taintVariableAndSeedPair.put(seed, seed);
		
		// If key is same with the corresponding value in the hashmap named 'json_child_parent',
		// it means that the key (variable) has no parent.
		// By Byungkwon 
		pb.json_child_parent.put(seed, seed);
		ParameterBucket.add_seed(seed, pb);
	}

	public static String getSeedForVariable(String variable, ParameterBucket pb)
	{
		return pb.taintVariableAndSeedPair.get(variable);
	}


	public static void tryVariableTaint(String taintee, String tainter, ParameterBucket pb)
	{
		HashMap<String, String> taintVariableAndSeedPair = pb.taintVariableAndSeedPair;

		// check if tainter is in taint variable
		if (taintVariableAndSeedPair.keySet().contains(tainter)) { 
			String seed = getSeedForVariable(tainter, pb);
			taintVariableAndSeedPair.put(taintee, seed);
			
			// For tacking parent of variable
			// By Byungkwon
			ParameterBucket.set_JSON_Parent(taintee, tainter, pb);
		}
	}
	
	

	public static void markTaintParametersAndSeedGenerate(ParameterBucket pb, ParameterBucket new_pb) {
		InvokeExpr ie = pb.ie;
		HashMap<String, String> taintVariableAndSeedPair = pb.taintVariableAndSeedPair;

		Set<String> new_taintParameters = new_pb.taintParameters;

		for (int i = 0; i < ie.getArgCount(); i++)
		{
			String arg = ie.getArg(i).toString();
			String parameter = "@parameter" + i;
			
			// By Byungkwon
			new_pb.BFTtable.put(parameter, new ArrayList<BFNode_Response>());
			
			// Check whether the argument is tainted
			if (taintVariableAndSeedPair.containsKey(arg))
			{
				new_taintParameters.add(parameter);
				
				TaintHelper.seedGenerate(parameter, new_pb);
				
				ParameterBucket.add_seed(parameter, new_pb);
			}
			
			//Class parameter passing
			if(ie.getArg(i) instanceof Constant && arg.startsWith("class") && !ie.toString().contains("Alert401Response"))
				Constants.classparam=arg;
			// if the argument 
			// By Byungkwon
			if(pb.variable_type != null){
				if (pb.variable_type.get(arg) != null) {
					if (pb.variable_type.get(arg).toString().equals("Jackson_Key")) {
						new_pb.variable_type.put(parameter, "Jackson_Key");
					}
					else if (pb.variable_type.get(arg).toString().equals("Jackson_Parser"))
					{
						new_pb.variable_type.put(parameter, "Jackson_Parser");
					}
				}
			}
			
			// Make BFNode in new_pb's BFT table if the argument is constant
			// By Byungkwon
			if (ie.getArg(i).getType().toString().contains("String")){
				if(arg.startsWith("\"") && arg.endsWith("\"")){
					ArrayList<BFNode_Response> list = new_pb.BFTtable.get(parameter);
					BFNode_Response bfn = new BFNode_Response();
					if(!arg.equals("\"\""))
						arg = arg.split("\"")[1];
					else
						arg = "";
					bfn.makeStringBfn_Direct(arg);
					list.add(bfn);
					
					new_pb.BFTtable.put(parameter, list);
				}
			}
			//Class parameter passing
			//By woohyun
			if (arg.startsWith("class ")){
				ArrayList<BFNode_Response> list = new ArrayList<BFNode_Response>();
				BFNode_Response bfn = new BFNode_Response();
				if(!arg.equals("\"\""))
					arg = arg.split("\"")[1];
				else
					arg = "";
				bfn.makeStringBfn_Direct(arg);
				list.add(bfn);
				
				new_pb.BFTtable.put(parameter, list);
			}
		}
		
		if(!InvokeHandler.isStaticInvoke(pb.invokeType)) {
			InstanceInvokeExpr iie = (InstanceInvokeExpr) pb.ie;
			String base = iie.getBase().toString();
			if (taintVariableAndSeedPair.containsKey(base)) {
				TaintHelper.seedGenerate("@this", new_pb);
			}
		}
	}
	
	public static boolean isJSON(String var, ParameterBucket pb) {
		ArrayList<BFNode_Response> list;
		
		if(pb == null || var == null)
			return false;
		
		list = pb.BFTtable.get(var);
		
		if(list == null)
			return false;
		
		if(list.size() <= 0)
			return false;
		
		// It is enough to check the first entry of the BFNode list,
		// because all BFNodes have same variable type.
		if(list.get(0).type == VAR_TYPE.VAR_TYPE_JSON)
			return true;
				
		return false;
	}
	
	public static boolean doesRootsetIncludeJSONNode(String seed, ParameterBucket pb) {
		Set<String> rootSet = BFTTableHelper.getRootVarSetFromSeed(seed, pb);
		
		for (String var : rootSet)
			if(isJSON(var, pb))
				return true;
		
		return false;
	}

	public static String generateStringForSeed(String seed, ParameterBucket pb) {
		
		Set<String> rootSet = BFTTableHelper.getRootVarSetFromSeed(seed, pb);
		
		String result = "";
		
		for (String root : rootSet)
		{
			String check = BFTTableHelper.generateStringForVariable(root, pb);
			if (!check.equals("{}") && !check.equals(""))
			{
				if (!result.equals(""))
					result += " / ";
				result += check;
			}

		}
		return result;
	}
}
