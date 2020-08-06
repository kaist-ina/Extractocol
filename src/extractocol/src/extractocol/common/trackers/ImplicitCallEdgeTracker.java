package extractocol.common.trackers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import extractocol.Constants;
import extractocol.common.helper.InvokeHelper;
import extractocol.common.tools.Pair2;
import extractocol.common.trackers.tools.ArgToVEL;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntryTable;
import extractocol.common.valueEntry.ValueEntryTracking;
import extractocol.frontend.basic.ExtractocolLogger;
import extractocol.frontend.basic.MyCallGraphBuilder;
import extractocol.frontend.helper.PropagateHelper;
import extractocol.frontend.output.basic.TaintResultEntry;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;

public class ImplicitCallEdgeTracker {
	enum METHOD_CASE {CASE0, CASE1, NONE}
	static boolean c;
	
	static Set<String> targetInvokeStmts = new HashSet<String>();
	static Map<Integer, Map<String, List<ValueEntryList>>> argTypeMap = null;
	
	static int oldMaxMainStream;
	static int oldMaxDepth;
	static boolean oldIsRetrofit;
	
	public static void M(boolean debug) {
		// 1. init
		init();
		
		// 2. Try to de-serialize the implicit call edge map
		if(!Deserialize()) {
			// 3. get result
			Map<Pair2<Unit, SootMethod>, List<ValueEntryList>> res = ArgToVEL.M(targetInvokeStmts, 2 /*maxMainStream*/, 1 /*maxDepth*/, debug);
			
			// 4. build intentMap
			processing(res);
		}
		
		// 5. post-processing
		postProcessing();
		
		if(Constants.isNotFullStack())
			System.exit(0);
	}
	
	private static void init() {
		// 1. set \
		targetInvokeStmts.add("<rx.Observable: rx.Observable flatMap(rx.functions.Func1)>");
		targetInvokeStmts.add("<rx.Observable: rx.Observable map(rx.functions.Func1)>");
		targetInvokeStmts.add("<rx.Observable: rx.Observable defer(rx.functions.Func0)>");
		
		// 4.
		c = Constants.setMonochromeOutput();
		oldIsRetrofit = Constants.setIsRetrofitFalse();
	}
	
	private static void processing(Map<Pair2<Unit, SootMethod>, List<ValueEntryList>> res) {
		argTypeMap = new HashMap<Integer, Map<String, List<ValueEntryList>>>();
		
		// 1. build argTypeMap
		for(Entry<Pair2<Unit, SootMethod>, List<ValueEntryList>> e: res.entrySet()) {
			Pair2<Unit, SootMethod> p = e.getKey();
			Unit ut = p.getE1();
			SootMethod sm = p.getE2();
			
			List<ValueEntryList> list = e.getValue();
			Integer hash = new Integer(MyCallGraphBuilder.getUnitHash(ut, sm));
			String u = ut.toString();
			
			List<ValueEntryList> newList = new ArrayList<ValueEntryList>();
			for(ValueEntryList vel: list)
				newList.add(vel.Clone());
			
			Map<String, List<ValueEntryList>> value = argTypeMap.get(hash);
			if(value == null)
				value = new HashMap<String, List<ValueEntryList>>();
				
			value.put(u, newList);
			
			argTypeMap.put(hash, value);
		}
		
		
		// 2. serialize argTypeMap
		Serialize();
	}
	
	private static void postProcessing() {
		// set CG to null in order to rebuild cg later.
		MyCallGraphBuilder.setCG(null);
		
		ArgToVEL.clear();
		Constants.setOutputColor(c);
		Constants.setIsRetrofit(oldIsRetrofit);
	}
	
	public static boolean isReady() { return argTypeMap != null; }
	
	public static List<SootMethod> find(Unit targetUnit, SootMethod sm) {
		// 1. get the corresponding 
		List<ValueEntryList> argVELs;
		Map<String, List<ValueEntryList>> res1 = argTypeMap.get(MyCallGraphBuilder.getUnitHash(targetUnit, sm));
		if(res1 == null)
			return null;
		
		argVELs = res1.get(targetUnit.toString());
		if(argVELs == null)
			return null;
		
		InvokeExpr ie = InvokeHelper.getInvokeExpr(targetUnit);
		if(ie == null)
			return null;
		
		// 2. de-obfuscate the method name
		String deObfuscated = InvokeHelper.methodDeobfuscation(ie.getMethod());
		
		// 3. handle each case
		switch(findTarget(deObfuscated)) {
		case CASE0: return case0(argVELs);
		case CASE1: return case1(argVELs);
		
		default:
			return null;
		}
	}
	
	private static METHOD_CASE findTarget(String method) {
		if(method.equals("<rx.Observable: rx.Observable flatMap(rx.functions.Func1)>")
				|| method.equals("<rx.Observable: rx.Observable map(rx.functions.Func1)>"))
			return METHOD_CASE.CASE1;
		
		if(method.equals("<rx.Observable: rx.Observable defer(rx.functions.Func0)>"))
			return METHOD_CASE.CASE0;
		
		return METHOD_CASE.NONE;
	}
	
	private static List<SootMethod> case0(List<ValueEntryList> argVELs) {
		ValueEntryList vel = argVELs.get(0);
		List<String> types = vel.getTypes();
		List<SootMethod> ret = new ArrayList<SootMethod>();
		
		for(String type: types) {
			String m = "<" + type + ": java.lang.Object call()>";
			SootMethod sm = null;
			try {
				sm = Scene.v().getMethod(m);
			}catch (Exception e) {
				ExtractocolLogger.Warn("Not found corresponding sootMethod.");
				continue;
			}
			
			ret.add(sm);
		}
		
		return ret;
	}
	
	private static List<SootMethod> case1(List<ValueEntryList> argVELs) {
		ValueEntryList vel = argVELs.get(0);
		List<String> types = vel.getTypes();
		List<SootMethod> ret = new ArrayList<SootMethod>();
		
		for(String type: types) {
			String m = "<" + type + ": java.lang.Object call(java.lang.Object)>";
			SootMethod sm = null;
			try {
				sm = Scene.v().getMethod(m);
			}catch (Exception e) {
				ExtractocolLogger.Warn("Not found corresponding sootMethod.");
				continue;
			}
			
			ret.add(sm);
		}
		
		return ret;
	}
	
	public static Set<SootMethod> SemanticEdge_Rx(ValueEntryTracking pb, InvokeExpr ie, String deobfuscatedMethod){
		Set<SootMethod> res = new HashSet<SootMethod>();
		
		if(deobfuscatedMethod.equals("<rx.Observable: rx.Observable flatMap(rx.functions.Func1)>")
			|| deobfuscatedMethod.equals("<rx.Observable: rx.Observable map(rx.functions.Func1)>")) {
			// 1. get types of arg0
			List<String> arg0Types = pb.varTable.getTypeof(ie.getArg(0).toString());
			
			if(arg0Types != null && arg0Types.size() > 0) {
				for(String type: arg0Types) {
					// 2. get call method of the type
					String callee = "<" + type + ": java.lang.Object call(java.lang.Object)>";
					
					// 3. get SootMethod object for the call method
					res.add(Constants.sCFG.getMethodOf(callee));
				}
			}
		}
		else if(deobfuscatedMethod.equals("<rx.Observable: rx.Observable defer(rx.functions.Func0)>")) {
			List<String> arg0Types = pb.varTable.getTypeof(ie.getArg(0).toString());
			
			if(arg0Types != null && arg0Types.size() > 0) {
				for(String type: arg0Types) {
					// 2. get call method of the type
					String callee = "<" + type + ": java.lang.Object call()>";
					
					// 3. get SootMethod object for the call method
					res.add(Constants.sCFG.getMethodOf(callee));
				}
			}
		}
		
		return res;
	}
	
	/** Check whether the callee is one of Rx operators
	 * 
	 * @param deobfuscatedMethod method name
	 * @param ie invoke expression
	 * @param pb pb of caller
	 * @param new_pb pb of callee
	 * @return True if it is one of Rx operators
	 */
	public static boolean isRxOperator(String deobfuscatedMethod, InvokeExpr ie, ValueEntryTracking pb, ValueEntryTracking new_pb) {
		if(!targetInvokeStmts.contains(deobfuscatedMethod))
			return false;
		
		if(!(ie instanceof InstanceInvokeExpr))
			return false;
		
		if(deobfuscatedMethod.equals("<rx.Observable: rx.Observable flatMap(rx.functions.Func1)>")
				|| deobfuscatedMethod.equals("<rx.Observable: rx.Observable map(rx.functions.Func1)>")) {
			InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
			
			String base = iie.getBase().toString();
			ValueEntryList vel = pb.varTable.getValueEntryList(base);
			if(vel != null)
				new_pb.varTable.setValueEntryList("@parameter0", vel, false);
		}
		
		return true;
	}
	
	
	/** Save the heap results into file
	 * 
	 */
	public static void Serialize(){
		Kryo kryo = new Kryo();
		Output output;
		try
		{
			output = new Output(new FileOutputStream(getImplicitCallEdgePath()));
			kryo.writeClassAndObject(output, argTypeMap);
			output.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	/** Read the heap results from file
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static boolean Deserialize()
	{
		Kryo kryo = new Kryo();
		Input input;
		Map<Integer, Map<String, List<ValueEntryList>>> result = null;
		try
		{
			input = new Input(new FileInputStream(getImplicitCallEdgePath()));
			result = (Map<Integer, Map<String, List<ValueEntryList>>>) kryo.readClassAndObject(input);
			input.close();
			
			argTypeMap = result;
			ExtractocolLogger.Log("The implicit-call-edge table was successfully deserialized.");
			return true;
		}
		catch (FileNotFoundException e)
		{
			//e.printStackTrace();
			argTypeMap = null;
			return false;
		}
	}
	
	private static String getImplicitCallEdgePath()
	{
		File serializationDir = new File(Constants.serializationDirName);
		if (!serializationDir.exists())
		{
			serializationDir.mkdir();
		}
		
		return Constants.getImplicitCallEdgeOutputDirPath() + "/ImplicitCallEdge.ser";
	}
}
