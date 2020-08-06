package extractocol.common.trackers.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import extractocol.Constants;
import extractocol.backend.request.helper.JimpleLoader;
import extractocol.common.helper.InvokeHelper;
import extractocol.common.outputs.helper.StdOutputController;
import extractocol.common.tools.Pair2;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntryTracking;
import extractocol.frontend.MyTest;
import extractocol.frontend.basic.ExtractocolLogger;
import extractocol.frontend.basic.MyCallGraphBuilder;
import extractocol.frontend.helper.PropagateHelper;
import extractocol.frontend.output.basic.TaintResultEntry;
import extractocol.tester.Backend;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Constant;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;

public class ArgToVEL {
	//static Set<String> targetMethod;
	static List<Pair2<String, Integer>> UnitHash = new ArrayList<Pair2<String,Integer>>();
	static Pair2<String, Integer> target;
	//static int targetArgNum;
	static Map<Pair2<Unit, SootMethod>, List<ValueEntryList>> resultTable = null;
	//static ValueEntryList result;
	static boolean debug = true;
	static boolean isArgTracking = false;
	static boolean oneByone = Constants.ArgToVEL_OneByOne;
	
	static int oldMaxMainStream;
	static int oldMaxDepth;
	
	/** Main function of ArgToVEL
	 *  Must call clear() method after calling M() method
	 * 
	 * @param sootMethod list of methods
	 * @return result table
	 */
	public static Map<Pair2<Unit, SootMethod>, List<ValueEntryList>> M(Set<String> sootMethod, boolean _debug) {
		return M(sootMethod, 3, 2, debug);
	}
	
	/** Main function of ArgToVEL
	 *  Must call clear() method after calling M() method
	 *  
	 * @param sootMethod list of methods
	 * @param maxMainStream
	 * @param maxDepth
	 * @param _debug 
	 * @return
	 */
	public static Map<Pair2<Unit, SootMethod>, List<ValueEntryList>> M(Set<String> sootMethod, int maxMainStream, int maxDepth, boolean _debug) {
		// 1. initialization (set & init)
		init(sootMethod, maxMainStream, maxDepth, _debug);

		// 2. main loop
		if(oneByone) {
			for(Pair2<String,Integer> p: UnitHash) {
				target = p;
				
				// 2-1. Frontend
				if(callFrontend())
					callBackend(); // 2-2. Backend 
			}
		}else {
			// 2-1. Frontend
			if(callFrontend())
				callBackend(); // 2-2. Backend 
		}
		
		// 3. post-processing
		postProcessing();
		
		return resultTable;
	}
	
	public static boolean doesContainTargetMethod(Unit u, SootMethod m) {
		InvokeExpr ie = InvokeHelper.getInvokeExpr(u);
		
		if(ie == null)
			return false;
		
		SootMethod origSootMethod = ie.getMethod();
		String deObfuscated = InvokeHelper.methodDeobfuscation(origSootMethod);
		
		if(oneByone) {
			return target.getE1().equals(deObfuscated) && 
					target.getE2().equals(MyCallGraphBuilder.getUnitHash(u, m));
		}else {
			Pair2<String,Integer> currPair = new Pair2<String,Integer>(deObfuscated, MyCallGraphBuilder.getUnitHash(u, m));
			for(Pair2<String,Integer> p: UnitHash) {
				if(p.equals(currPair))
					return true;
			}
			return false;
		}
		
	}
	
	private static void init(Set<String> sm, int maxMainStream, int maxDepth, boolean _debug) {
		// load classes
		JimpleLoader jl = new JimpleLoader(Constants.getAndroidSDKPath(), Constants.getAPKpath(Constants.getAPPName() + ".apk"),Constants.getSourcesAndSinksPath());
				
		//targetMethod = sm;
		resultTable = new HashMap<Pair2<Unit, SootMethod>, List<ValueEntryList>>();
		isArgTracking = true;
		
		oldMaxMainStream = PropagateHelper.setMaxMainStream(maxMainStream);
		oldMaxDepth = TaintResultEntry.setMaxDepth(maxDepth);
		
		debug = _debug;
		
		getStmtHash(sm);
	}
	
	private static void getStmtHash(Set<String> smSet) {
		// find the target and calculate hash
		for(String smtgt: smSet) {
			for (SootClass sc : Scene.v().getClasses()) {
				
				// Check whether this SootClass is phantom 
				if(sc.isPhantom())
					continue;
				
				try {
					for (SootMethod sm : sc.getMethods()) {
						if (!sm.hasActiveBody())
							continue;
						
						for(Unit u: sm.getActiveBody().getUnits()) {
							Stmt s = (Stmt) u;
							if(!s.containsInvokeExpr())
								continue;
							
							SootMethod invokeTarget = s.getInvokeExpr().getMethod();
							String deObfuscated = InvokeHelper.methodDeobfuscation(invokeTarget);
							
							if(!deObfuscated.equals(smtgt))
								continue;
							
							int hash = MyCallGraphBuilder.getUnitHash(u, sm);
							UnitHash.add(new Pair2<String,Integer>(smtgt, hash));
							ExtractocolLogger.Log("Unit: " + u.toString() + " [Hash:" + hash + "] in " + sm.toString());
						}
					}
				}catch(Exception e) {
					System.err.println("SC: " + sc.toString());
				}
			}
		}
	}
	
	private static void postProcessing() {
		isArgTracking = false;
		PropagateHelper.setMaxMainStream(oldMaxMainStream);
		TaintResultEntry.setMaxDepth(oldMaxDepth);
	}
	
	public static void clear() {
		if(resultTable == null)
			return;
		
		for(Entry<Pair2<Unit, SootMethod>, List<ValueEntryList>> e: resultTable.entrySet()) {
			Pair2<Unit, SootMethod> p = e.getKey();
			List<ValueEntryList> l = e.getValue();
			
			p.clear();
			
			for(ValueEntryList vel : l)
				vel.Clear();
			
			l.clear();
		}
		
		resultTable.clear();
		resultTable = null;
	}
	
	private static boolean callFrontend() {
		try{
			System.out.println("Frontend running ...");
			if(!debug) StdOutputController.stop(); 
			MyTest.main(getFrontendArgument().split(" "));
			if(!debug) StdOutputController.start();
			System.out.println("Frontend finished.");
			
			// After a call graph has been built once, cgBuilt will be changed to True and a call graph will not be constructed again.
			//setCGBuilt(true);
		}catch(IOException e){
			e.printStackTrace();
			System.out.println("Frontend has been failed due to IOException.");
			return false;
		}catch(InterruptedException e){
			e.printStackTrace();
			System.out.println("Frontend has been failed due to InterruptedException.");
			return false;
		}
		
		return true;
	}
	
	private static void callBackend() {
		System.out.println("Backend running ...");
		if(!debug) StdOutputController.stop();
		Backend.main(getBackendArgument().split(" "));
		if(!debug) StdOutputController.start();
		System.out.println("Backend finished.");
	}
	
	private static String getBackendArgument(){
		return "--app " + Constants.apkName; 
	}
	
	private static String getFrontendArgument(){
		return Constants.apkName + ".apk "
				+ Constants.getAndroidSDKPath()
				+ " --noexceptions"	+ " --nostatic"	+ " --aplength 0"
				+ " --aliasflowins"	+ " --layoutmode none"	+ " --nocallbacks"
				+ " --repeatcount 1";
	}
	
	public static boolean isArgTracking() { return isArgTracking; }
	//public static String getTargetMethod() { return targetMethod; }
	//public static int getTargetArgNum() { return targetArgNum; }
	
	public static void SaveResult(Unit u, SootMethod sm, ValueEntryTracking vet) {
		// 1. get invokeExpr
		InvokeExpr ie = InvokeHelper.getInvokeExpr(u);
		
		if(ie == null)
			return;
		
		// 2. allocate new VEL list
		Pair2<Unit, SootMethod> key = new Pair2<Unit, SootMethod>(u, sm);
		List<ValueEntryList> newValueList = resultTable.get(key); 
		if(newValueList == null) {
			newValueList = new ArrayList<ValueEntryList>();
			for(int i = 0; i < ie.getArgs().size(); i++)
				newValueList.add(new ValueEntryList());
			resultTable.put(key, newValueList);
		}
		
		// 3. get the target argument
		for(int i = 0; i < ie.getArgs().size(); i++) {
			String argStr;
			ValueEntryList tgtRes;
			
			Value v = ie.getArg(i);
			ValueEntryList currList = newValueList.get(i);
			
			// Case 1: when arg is instance of Constant
			if(v instanceof Constant) {
				argStr = extractocol.common.valueEntry.helper.General.getConstantName(v.toString());
				currList.addValueEntry(argStr, SOURCE_TYPE.CONSTANT, false);
			}
			// Case 2: when arg is not instance of Constant
			else {
				argStr = v.toString();
				try {
					tgtRes = vet.varTable.getValueEntryListDeep(argStr);
					currList.addValueEntryList(tgtRes.Clone(), false);
				}catch(Exception e) {
					ExtractocolLogger.Warn("Unit: " + u + ", SM: " + sm);
					e.printStackTrace();
				}
				
			}
		}
	}
}
