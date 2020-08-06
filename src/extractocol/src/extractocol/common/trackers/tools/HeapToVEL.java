package extractocol.common.trackers.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import extractocol.Constants;
import extractocol.backend.request.heapManagement.SourceforTaint;
import extractocol.backend.request.helper.JimpleLoader;
import extractocol.common.outputs.helper.HeapHandlingHelper;
import extractocol.common.outputs.helper.StdOutputController;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntryTable;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;
import extractocol.frontend.MyTest;
import extractocol.frontend.basic.MyCallGraphBuilder;
import extractocol.frontend.helper.PropagateHelper;
import extractocol.frontend.output.basic.TaintResultEntry;
import extractocol.tester.Backend;
import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.toolkits.callgraph.CallGraph;

public class HeapToVEL {
	private static boolean debug = true;
	
	public static ArrayList<SourceforTaint> lstTaintSourceInfo = new ArrayList<SourceforTaint>();
	public static String targetHeap = null;
	public static ValueEntryList HeapValue;
	
	public static boolean doesFindHeapinCode;
	public static boolean needToRunFrontBackend;
	
	// cgBuilt will be changed TRUE when a call graph has been built and all methods retrieve active body.
	
	public static ValueEntryList M(String heap, boolean _debug) {
		ValueEntryList result;
		boolean frontSuccess = true;
		long start, end;
		
		// 0. Check whether the heap has been already analyzed
		if(General.isHeapAlreadyAnalyzed(heap))
			return General.getValueEntryList(heap);
		
		// 1. Initialization
		Initialization(heap, _debug);
		
		// 2. Locate assignment of heap in Jimple code
		start = System.currentTimeMillis();
		if(!debug) StdOutputController.stop();
		result = SourceFinder(heap);
		if(!debug) StdOutputController.start();
		System.out.print(" [(S)");
		
		if(needToRunFrontBackend){
			// 3. Find entry points (EPs) by running Front-end
			try{
				if(!debug) StdOutputController.stop(); 
				MyTest.main(getFrontendArgument().split(" "));
				if(!debug) StdOutputController.start();
				System.out.print("(F)");
				
				// After a call graph has been built once, cgBuilt will be changed to True and a call graph will not be constructed again.
				//setCGBuilt(true);
			}catch(IOException e){
				e.printStackTrace();
				System.out.println("Frontend has been failed due to IOException.");
				frontSuccess = false;
			}catch(InterruptedException e){
				e.printStackTrace();
				System.out.println("Frontend has been failed due to InterruptedException.");
				frontSuccess = false;
			}
			
			// 4. Find candidate values of the heap running Back-end (Backward)
			if(frontSuccess){
				if(!debug) StdOutputController.stop();
				Backend.main(getBackendArgument().split(" "));
				if(!debug) StdOutputController.start();
				System.out.print("(B)");
				
				result.addValueEntryList(HeapValue, false);
			}
		}
		
		end = System.currentTimeMillis();
		System.out.print(", " + (end-start)/1000 + "s]");
		
		// 5. Save the result
		General.SaveHeapResult(heap, result.Clone());
		
		// 6. post processing
		postProcessing();
		
		return result;
	}
	
	private static void postProcessing() {
		Constants.heapobject = false;
	}
	
	private static void Initialization(String heap, boolean _debug) {
		debug = _debug;
		
		if(MyCallGraphBuilder.needToRetrieveActiveBodies()) {
			if(!debug) StdOutputController.stop();
			JimpleLoader jl = new JimpleLoader(Constants.getAndroidSDKPath(), Constants.getAPKpath(Constants.apkName + ".apk"),Constants.getSourcesAndSinksPath());
			if(!debug) StdOutputController.start();
		}
		
		// set maxMainStream & maxDepth for frontend (empirically determined) 
		PropagateHelper.setMaxMainStream(4);
		TaintResultEntry.setMaxDepth(2);
		
		EmptyHeapSource(); // Empty HeapSource.txt
		lstTaintSourceInfo.clear(); // Empty the list of heap sources
		targetHeap = heap;
		
		General.DeserializeHeapResults();
		
		Constants.heapobject = true;
	}
	
	/** Empty HeapSource.txt **/
	private static void EmptyHeapSource(){
		HeapHandlingHelper.MakeFileEmpty(getHeapObjectSourcePath());
	}
	
	/** Find methods that contain the assign statement which contains heap within the left Op 
	 * 
	 * @param heap Name of the target heap
	 * @return Constant values assigned to the target heap. (Example format: "Const1"|"Const2")
	 */
	private static ValueEntryList SourceFinder(String heap){
		HashSet<String> result = new HashSet<String>();
		doesFindHeapinCode = false;
		needToRunFrontBackend = false;
		
		Collection<SootClass> AllClasses = Scene.v().getClasses();
		
		for (SootClass sc : AllClasses)
			for (final SootMethod sm : sc.getMethods())
				if (sm.hasActiveBody())
					result.addAll(FindHeapInUnit(sm.getActiveBody(), heap));
		
		return HashsetToValueEntryList(result);
	}
	
	private static ValueEntryList HashsetToValueEntryList(HashSet<String> input){
		ValueEntryList output = new ValueEntryList(null);
		for(String s : input)
			output.addValueEntry(s, SOURCE_TYPE.CONSTANT, false);
		
		return output;
	}
	
	/**
	 * 
	 * @param b Method body
	 * @param heap Name of target heap
	 * @return Constant values assigned to the target heap. (Example format: "Const1"|"Const2"|)
	 */
	private static HashSet<String> FindHeapInUnit(final Body b, final String heap) 
	{
		final HashSet<String> result = new HashSet<String>();
		final PatchingChain<Unit> units = b.getUnits();
		
		for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();)
		{
			final Unit u = iter.next();
			
			u.apply(new AbstractStmtSwitch()
			{
				public void caseAssignStmt(AssignStmt as) 
				{
					try{
						final FileWriter fw = new FileWriter(new File(getHeapObjectSourcePath()), true);
						
						// Check whether the unit contains the heap
						if (doesLeftOpContainHeap(as, heap)){
							// case 1: when the right Op is constant
							if (as.getRightOp() instanceof soot.jimple.Constant) 
							{
								if(!as.getRightOp().toString().equals("null")){
									result.add(as.getRightOp().toString().replaceAll("\"", ""));
									doesFindHeapinCode = true;
								}
							}
							// case 2: others (variable or invoke statement)
							else 
							{
								fw.write(b.getMethod().getSignature() + " -> _SOURCE_");
								fw.write(Constants.LINE_SEPARATOR);
								
								// Add the source information for the target heap
								addSourcesforTaint(b.getMethod().getSignature(), as.getRightOp().toString(), (Unit)as);
								needToRunFrontBackend = true;
							}
						}
						
						fw.flush();
						fw.close();
						
					}catch(IOException e){
						e.printStackTrace();
					}
				}
			});
		}
		
		return result;
	}
	
	/** Add the source information for the target heap
	 * 
	 * @param _methodsig A signature of the method that contains _unit
	 * @param _seedobject Right Op of _unit
	 * @param _unit The assignment statement that contains the target heap within the left Op
	 */
	private static void addSourcesforTaint(String _methodsig, String _seedobject, Unit _unit)
	{
		SourceforTaint newsource = new SourceforTaint(_methodsig, _seedobject, _unit);
		lstTaintSourceInfo.add(newsource);
	}
	
	/** Check whether the unit contains the heap
	 * 
	 * @param as assignment statement
	 * @param heap name of heap
	 * @return true if, and only if the assignment statement contains the heap in the left Op.
	 */
	private static boolean doesLeftOpContainHeap(AssignStmt as, String heap){
		return as.getLeftOp().toString().contains(heap);
	}
	
	private static String getHeapObjectSourcePath(){
		File serializationDir = new File(Constants.serializationDirName);
		if (!serializationDir.exists())
		{
			serializationDir.mkdir();
		}
		
		return Constants.getTempDirPath() + "/" + "HeapSource.txt";
	}	
	
	private static String getBackendArgument(){
		return "--app " + Constants.apkName 
				+ " --heapobject";
	}
	
	private static String getFrontendArgument(){
		return Constants.apkName + ".apk "
				+ Constants.getAndroidSDKPath()
				+ " --noexceptions"	+ " --nostatic"	+ " --aplength 0"
				+ " --aliasflowins"	+ " --layoutmode none"	+ " --nocallbacks"
				+ " --heapobject" + " --repeatcount 1";
	}
}
