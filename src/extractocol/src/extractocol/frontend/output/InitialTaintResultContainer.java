package extractocol.frontend.output;

import extractocol.Constants;
import extractocol.common.helper.MultiThreadHelper;
import extractocol.common.outputs.backendOutputHelper.ReqRespInfo;
import extractocol.common.outputs.helper.StdOutputController;
import extractocol.common.tools.highScaleLib.NonBlockingHashMap;
import extractocol.common.tools.highScaleLib.NonBlockingHashSet;
import extractocol.frontend.basic.ExtractocolLogger;
import extractocol.frontend.basic.ExtractocolLogger.MYCOLOR;
import extractocol.frontend.output.InitialTaintResultContainer.TAINT_METHOD_TYPE;
import extractocol.frontend.output.basic.TaintResultEntry;
import heros.solver.CountingThreadPoolExecutor;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class InitialTaintResultContainer {
	public static enum TAINT_METHOD_TYPE {LIST, SET}
	private static LinkedList<TaintResultEntry> seed = new LinkedList<TaintResultEntry>();
	
	//private static Queue<TaintResultEntry> rawResult = new ConcurrentLinkedQueue<TaintResultEntry>();
	public static NonBlockingHashSet<TaintResultEntry> rawResult = new NonBlockingHashSet<TaintResultEntry>();
	public static List<TaintResultEntry> rawResultList = new ArrayList<TaintResultEntry>();
	public static List<TaintResultEntry> finalList = new ArrayList<TaintResultEntry>();
	//private static LinkedList<TaintResultEntry> rawResult = new LinkedList<TaintResultEntry>();
	//private static NonBlockingHashMap<TaintResultEntry,Object> rawResult = new NonBlockingHashMap<TaintResultEntry, Object>();
	private static LinkedList<TaintResultEntry> entriesBeMerged = new LinkedList<TaintResultEntry>();
	//private static Set<TaintResultEntry> refinedResult = new HashSet<TaintResultEntry>();
	//private static ConcurrentHashMap<TaintResultEntry, Object> rawResult = new ConcurrentHashMap<TaintResultEntry, Object>();
	
	//private static List<AbstractionExtension> result = new ArrayList<AbstractionExtension>();
	
	//public static void addResult(AbstractionExtension ae) { result.add(ae); }
	//public static List<AbstractionExtension> getResult() { return result; }
	
	//private static Lock myLock = new ReentrantLock();
	//private static boolean didPrint = false;
	private static boolean colored = Constants.coloredOutputFrontend; // Printing outputs colorfully
	
	// A type of data structure for taint methods: List and Set
	// Both cases are logically working same. 
	// (1) List: Print result in detail (better for debugging), but slow
	// (2) Set: Fast processing, but little information
	public static TAINT_METHOD_TYPE tmType = TAINT_METHOD_TYPE.SET;
	private static int merge_stride = 40000;
	
	
	
	/** Move the refined result to the DP/EP container for serialization
	 * 
	 */
	public static void MoveResultToContainer(){
		ExtractocolLogger.Log("Moving the refined result to the container ... ");
		
		for(TaintResultEntry e : finalList){
			
			if(tmType == TAINT_METHOD_TYPE.SET) {
				List<String> list = new ArrayList<String>(e.getFinalTaintMethods());
				TaintResultContainer.addTaintResult(e.getDPMethod(), e.getDPStmt(), e.getDPStmtHash(), e.getEP(), list);
			}else if(tmType == TAINT_METHOD_TYPE.LIST) {
				TaintResultContainer.addTaintResult(e.getDPMethod(), e.getDPStmt(), e.getDPStmtHash(), e.getEP(), e.getTaintMethodSet());
			}
		}
	}
	
	/** Clear all of the sets and lists
	 * 
	 */
	public static void Clear(){
		// 1. Clear seed
		if(seed != null){
			for(TaintResultEntry e : seed)
				e.Clear();
			seed.clear();
		}
		
		// 2. Clear rawResult
		if(rawResult != null){
			for(TaintResultEntry e : rawResult)
				e.Clear();
			rawResult.clear();
		}
		
		if(rawResultList != null)
			rawResultList.clear();
		
		// 3. Clear entriesBeMerged
		if(entriesBeMerged != null){
			for(TaintResultEntry e : entriesBeMerged)
				e.Clear();
			entriesBeMerged.clear();
		}
		
		// 4. Clear final list
		if(finalList != null){
			for(TaintResultEntry e : finalList)
				e.Clear();
			finalList.clear();
		}
		
		// 5. cleanr main stream id map
		/*if(MainStreamIdMap != null)
			MainStreamIdMap.clear();*/
	}
	
	
	
	/** Add the raw result into the list
	 * 
	 * @param tre TaintResultEntry
	 */
	public static void addFinalResult(TaintResultEntry tre) { 
		rawResult.add(tre);
		if(rawResult.size() % 1000 == 0) ExtractocolLogger.Log("The size of raw result list is " + rawResult.size() 
																+ ". [P. cnt: " + tre.getPropagationCount() +"]"
																+ "[MainS len: " + tre.getMainStreamLength() + "]"
																+ "[EP var:"+ tre.FindEPVar() + "]"
																+ "[EP: " + tre.FindEP() +"]"
																+ "[TMS len: " + tre.getTaintMethodSet().size() + "]"
																+ "[TRE hash: " + tre.hashCode() + "]");	
	}
	
	/** Add the seed
	 * 
	 * @param tre TaintResultEntry
	 */
	public static void addSeed(TaintResultEntry tre) { seed.add(tre); }
	
	/** Get the corresponding seed 
	 * 
	 * @param ID 
	 * @return The corresponding TaintResultEntry 
	 */
	public static TaintResultEntry getSeed(int ID){
		for(TaintResultEntry tre: seed){
			if(tre.getID() == ID)
				return tre;
		}
		
		return null;
	}
	
	
	
	/******************************************************************************/
	/****                          Post Processing                             ****/
	/******************************************************************************/
	/** Refine the raw result */
	public static void ResultRefinement(){
		ExtractocolLogger.Log();
		ExtractocolLogger.Log("The size of raw result list: " + rawResult.size());
		
		//DeDuplication(); // de-duplication in the raw list (Not required anymore due to hashMap)
		FindingEP(); // Finding EP among the taint method set
		
		if(Constants.printRawResult)
			InitialTaintResultContainer.PrintRaw(); // for debugging (by BK)
		
		//procBeforeMerge();
		//Merge(); // Merging entries
		Merge2();
	}
	
	/** Find EP among the taint method set for all TRE entry */
	private static void FindingEP(){
		ExtractocolLogger.Log("Finding EP among the taint method set ... ");
		
		for(TaintResultEntry e: rawResult)
			e.setEP(e.FindEP());
	}
	
	/** Merge entries whose main stream is part of the other's main stream
	 *  (The main stream means a set of taint methods whose depth is 0.)
	 */
	private static void Merge(){
		ExtractocolLogger.LogNoLineBreak("Merging entries ... ");
		
		// Phase 0
		beforeMerging();
		
		// Phase 1
		if(Constants.mergingMultiThread)
			findEntriesBeingMerged_multithread();
		else
			findEntriesBeingMerged();
		
		// Phase 2
		//rawToRefined();
		
		// Phase 3
		if(Constants.mergingMultiThread)
			merging_Multithread();
		else
			merging();
		
		
	}
	
	private static void Merge2() {
		long start, end;
		
		ExtractocolLogger.LogNoLineBreak("Merging entries ... " + rawResult.size());
		
		// 1. raw set to list
		List<TaintResultEntry> treList = rawSetToList();
		
		// 2. main loop
		while(true) {
			start = System.currentTimeMillis();
			treList = merge_inner(treList);
			end = System.currentTimeMillis();
			System.out.print(String.format(" => %d(%ds)", treList.size(), (end-start)/1000));
			
			if(treList.size() < merge_stride)
				break;
			
			Collections.shuffle(treList);
		}
		
		// 3. Final deduplication
		start = System.currentTimeMillis();
		treList = merge_inner(treList);
		end = System.currentTimeMillis();
		System.out.print(String.format(" -> %d(%ds)", treList.size(), (end-start)/1000));
		
		// 4. Finalize
		finalize(treList);
		
		System.out.println();
		System.gc();
	}
	
	private static void finalize(List<TaintResultEntry> treList) {
		finalList.addAll(treList);
	}
	
	private static List<TaintResultEntry> merge_inner(List<TaintResultEntry> treList){
		if(treList.size() > merge_stride)
			return splitAndMerge(treList);
		else
			return merging(treList);
	}
	
	private static List<TaintResultEntry> splitAndMerge(List<TaintResultEntry> treList){
		long start, end;
		List<List<TaintResultEntry>> tot = new ArrayList<List<TaintResultEntry>>();
		List<TaintResultEntry> result = new ArrayList<TaintResultEntry>();
		List<TaintResultEntry> partialList = null;
		
		// 1. split
		for(int i = 0; i < treList.size(); i++) {
			if(i % merge_stride == 0) {
				partialList = new ArrayList<TaintResultEntry>();
				tot.add(partialList);
			}
			
			TaintResultEntry rri = treList.get(i);
			partialList.add(rri);
		}
		
		// 2. merge
		for(List<TaintResultEntry> pList : tot) {
			start = System.currentTimeMillis();
			List<TaintResultEntry> res = merging(pList);
			end = System.currentTimeMillis();
			
			result.addAll(res);
			System.out.print(String.format("-%d(%ds)", (pList.size()-res.size()), (end-start)/1000));
		}
		
		return result;
	}
	
	private static void clearMergerMergeeBits(List<TaintResultEntry> treList) {
		for(TaintResultEntry tre: treList)
			tre.clearMergerMergeeBit();
	}
	
	private static List<TaintResultEntry> merging(List<TaintResultEntry> origList){
		List<TaintResultEntry> refinedList = new ArrayList<TaintResultEntry>(origList);
		
		// 1. allocate bit array
		allocateBitsAndResetNeedToStay(origList);
		
		// 2.
		findEntriesBeingMerged_multithread(refinedList, origList);
		
		// 3. 
		merging_Multithread(refinedList, origList);
		
		// 4. clear merger/mergee bits
		clearMergerMergeeBits(origList);
		System.gc();
		
		return refinedList;
	}
	
	private static void allocateBitsAndResetNeedToStay(List<TaintResultEntry> treList) {
		int size = treList.size();
		for(TaintResultEntry tre: treList) {
			tre.allocMergerMergeeBit(size);
			tre.setNeedToStay(false);
		}
	}
	
	private static List<TaintResultEntry> rawSetToList(){
		List<TaintResultEntry> treList = new ArrayList<TaintResultEntry>();
		
		for(TaintResultEntry target : rawResult) {
			target.taintMethodlistToSet();
			treList.add(target);
		}
		
		return treList;
	}
	
	private static void beforeMerging() {
		long start, end;
		int rawResultSize = rawResult.size();
		start = System.currentTimeMillis();
		
		
		for(TaintResultEntry target : rawResult) {
			target.taintMethodlistToSet();
			target.allocMergerMergeeBit(rawResultSize);
			rawResultList.add(target);
		}
		
		end = System.currentTimeMillis();
		System.out.print("(" + (end-start)/1000 + "s, ");
	}
	
	private static void findEntriesBeingMerged() {
		/*long start, end;
		
		start = System.currentTimeMillis();
		for(Iterator<TaintResultEntry> iter = rawResult.iterator(); iter.hasNext();) {
			TaintResultEntry e = iter.next();
			boolean isAddedTo = false;
			
			//t1 = System.currentTimeMillis();
			for(TaintResultEntry target: rawResult){
				// 0. Avoid itself
				if(target == e)
					continue;
				
				// 1. Check whether its DP is same with the target
				if(!e.SameDPWith(target))
					continue;
				
				// 2. Check whether its EP is in the taint method set of the target
				if(!target.getTaintMethodSet().contains(e.getEP()))
					continue;
				
				// 3. Check whether the "main stream" is part of the target's
				if(!target.ContainMainStreamOf(e))
					continue;
				
				// 4. Then, add the target into the merging list of the entry e
				// e      : TRE being merged with target
				// target : TRE absorbing e
				e.addMerger(target);
				target.addMergee(e);
				isAddedTo = true;
				//break;
			}
			
			// 5. Remove it if the entry has been merged with others
			if(isAddedTo){
				entriesBeMerged.add(e);
				iter.remove();
			}
		}
		end = System.currentTimeMillis();
		System.out.print((end-start)/1000 + "s, ");*/
	}
	
	private static void findEntriesBeingMerged_multithread(List<TaintResultEntry> refinedList, List<TaintResultEntry> origList) {
		long start, end;
		
		/****************************************************************/
		/*** Phase 1: Find mergees/mergers bit                        ***/
		/****************************************************************/
		ThreadPoolExecutor executor = MultiThreadHelper.createExecutor();
		analyzingTREThread.init(origList);
		
		int numThread = MultiThreadHelper.getCoreNum();
		int stride = refinedList.size() / numThread;
		int cnt = 0;
		List<TaintResultEntry> listArr = new ArrayList<TaintResultEntry>();
		
		if(stride > 1) {
			for(TaintResultEntry entry: refinedList) {
				listArr.add(entry);
				cnt++;
				
				if(cnt % stride == 0) {
					executor.execute(new analyzingTREThread(listArr));
					listArr = new ArrayList<TaintResultEntry>();
				}else if(cnt == refinedList.size()) {
					executor.execute(new analyzingTREThread(listArr));
				}
			}
		}else {
			for(TaintResultEntry entry: refinedList)
				listArr.add(entry);
			executor.execute(new analyzingTREThread(listArr));
		}
		
		
		start = System.currentTimeMillis();
		MultiThreadHelper.awaitCompletion(executor, Constants.maxMergingTime, Constants.mergingTimeUnit, null, null);
		end = System.currentTimeMillis();
		
		//System.out.print("Find bits -- " + (end-start)/1000 + "s, ");
		/****************************************************************/
		List<TaintResultEntry> templist = new ArrayList<TaintResultEntry>(); // for debugging
		for(TaintResultEntry tre: refinedList) {
			if(tre.getFinalTaintMethods().contains("<com.mcdonalds.sdk.connectors.middleware.helpers.MWNutritionConnectorHelper: void getAllRecipesForCategory(java.lang.String,com.mcdonalds.sdk.AsyncListener)>"))
				templist.add(tre);
		}
		
		/****************************************************************/
		/*** Phase 2: Remove mergees from the rawResult               ***/
		/****************************************************************/
		start = System.currentTimeMillis();
		for(Iterator<TaintResultEntry> iter = refinedList.iterator(); iter.hasNext();) {
			TaintResultEntry e = iter.next();
			
			// can stay if it has no merger
			if(e.getMergerListSize() == 0) {
				e.setNeedToStay(true);
				continue;
			}
			
			// Count mergers whose mergee list contain me and needToStay is false 
			cnt = 0;
			int currIdx = origList.indexOf(e);
			for(int i = 0 ; i < origList.size(); i++) {
				if(!e.getMergerBit(i))
					continue;
				
				TaintResultEntry merger = origList.get(i);
				
				if(!merger.getMergerBit(currIdx) || merger.getNeedToStay())
					break;
				
				cnt++;
			}
			
			// 
			if(cnt == e.getMergerListSize()) {
				e.setNeedToStay(true);
				continue;
			}
				
			iter.remove();
		}
		end = System.currentTimeMillis();
		//System.out.print("Reduced to " + refinedList.size() + " -- " + (end-start)/1000 + "s, ");
		/****************************************************************/
	}
	
	private static void findEntriesBeingMerged_multithread() {
		long start, end;
		
		/****************************************************************/
		/*** Phase 1: Find mergees/mergers bit                        ***/
		/****************************************************************/
		ThreadPoolExecutor executor = MultiThreadHelper.createExecutor();
		analyzingTREThread.init(rawResultList);
		
		int numThread = MultiThreadHelper.getCoreNum();
		int stride = rawResult.size() / numThread;
		int cnt = 0;
		List<TaintResultEntry> listArr = new ArrayList<TaintResultEntry>();
		
		if(stride > 1) {
			for(TaintResultEntry entry: rawResult) {
				listArr.add(entry);
				cnt++;
				
				if(cnt % stride == 0) {
					executor.execute(new analyzingTREThread(listArr));
					listArr = new ArrayList<TaintResultEntry>();
				}else if(cnt == rawResult.size()) {
					executor.execute(new analyzingTREThread(listArr));
				}
			}
		}else {
			for(TaintResultEntry entry: rawResult)
				listArr.add(entry);
			executor.execute(new analyzingTREThread(listArr));
		}
		
		
		start = System.currentTimeMillis();
		MultiThreadHelper.awaitCompletion(executor, Constants.maxMergingTime, Constants.mergingTimeUnit, null, null);
		end = System.currentTimeMillis();
		
		System.out.print("Find bits -- " + (end-start)/1000 + "s, ");
		/****************************************************************/
		
		/****************************************************************/
		/*** Phase 2: Remove mergees from the rawResult               ***/
		/****************************************************************/
		start = System.currentTimeMillis();
		for(Iterator<TaintResultEntry> iter = rawResult.iterator(); iter.hasNext();) {
			TaintResultEntry e = iter.next();
			
			// can stay if it has no merger
			if(e.getMergerListSize() == 0) {
				e.setNeedToStay(true);
				continue;
			}
			
			// Count mergers whose mergee list contain me and needToStay is false 
			cnt = 0;
			int currIdx = rawResultList.indexOf(e);
			for(int i = 0 ; i < rawResultList.size(); i++) {
				if(!e.getMergerBit(i))
					continue;
				
				TaintResultEntry merger = rawResultList.get(i);
				
				if(!merger.getMergerBit(currIdx) || merger.getNeedToStay())
					break;
				
				cnt++;
			}
			
			// 
			if(cnt == e.getMergerListSize()) {
				e.setNeedToStay(true);
				continue;
			}
				
			entriesBeMerged.add(e);
			iter.remove();
		}
		end = System.currentTimeMillis();
		System.out.print("Reduced to " + rawResult.size() + " -- " + (end-start)/1000 + "s, ");
		/****************************************************************/
	}
	
	/*private static void rawToRefined() {
		//long start, end;
		//start = System.currentTimeMillis();
		for(TaintResultEntry target : rawResult){
			refinedResult.add(target);
		}
		//end = System.currentTimeMillis();
		//System.out.print((end-start)/1000 + "s,");
	}*/
	
	private static void merging() {
		/*long start, end;
		boolean oldStatus = StdOutputController.getCurrentStatus();
		
		StdOutputController.start();
		System.out.print(" Size of entriesBeMerged: " + entriesBeMerged.size() + ". ");
		StdOutputController.stopOrStart(oldStatus);
		
		System.out.print("Processing...");
		
		start = System.currentTimeMillis();
		for(int i = 0; i < entriesBeMerged.size(); i++){
			TaintResultEntry e = entriesBeMerged.get(i);
			
			for(TaintResultEntry target : e.getMergers())
				target.addTaintMethodSetOf(e);
			
			// for printing
			if(i != 0 && i % 500 == 0) {
				System.out.print(i + " ");
			}
		}
		end = System.currentTimeMillis();
		
		System.out.println(", " + (end-start)/1000 + "s, Reduced to " + rawResult.size() + ")");*/
	}
	
	private static void merging_Multithread(List<TaintResultEntry> refinedList, List<TaintResultEntry> origList) {
		long start, end;
		ThreadPoolExecutor executor = MultiThreadHelper.createExecutor();
		mergingThread.init(origList);
		
		//System.out.print(" Size of merger TREs: " + rawResult.size() + ". ");
		
		
		for(TaintResultEntry merger: refinedList) {
			if(merger.getMergeeListSize() > 0) {
				executor.execute(new mergingThread(merger));
			}
		}
		start = System.currentTimeMillis();
		MultiThreadHelper.awaitCompletion(executor, Constants.maxMergingTime, Constants.mergingTimeUnit, null, null);
		end = System.currentTimeMillis();
		//mergingThread.clear();
		//System.out.println( ((rawResult.size() > 1000)? "-- " : "") + (end-start)/1000 + "s, ");
		//System.gc();
		//System.out.println( "Merging -- " + (end-start)/1000 + "s)");
	}
	
	private static void merging_Multithread() {
		long start, end;
		ThreadPoolExecutor executor = MultiThreadHelper.createExecutor();
		mergingThread.init(rawResultList);
		
		//System.out.print(" Size of merger TREs: " + rawResult.size() + ". ");
		
		
		for(TaintResultEntry merger: rawResult) {
			if(merger.getMergeeListSize() > 0) {
				executor.execute(new mergingThread(merger));
			}
		}
		start = System.currentTimeMillis();
		MultiThreadHelper.awaitCompletion(executor, Constants.maxMergingTime, Constants.mergingTimeUnit, null, null);
		end = System.currentTimeMillis();
		//mergingThread.clear();
		//System.out.println( ((rawResult.size() > 1000)? "-- " : "") + (end-start)/1000 + "s, ");
		System.gc();
		System.out.println( "Merging -- " + (end-start)/1000 + "s)");
	}
	
	
	/******************************************************************************/
	/****                               Print                                  ****/
	/******************************************************************************/
	/** Print the result **/
	public static void Print(){
		int idx = 1;
		
		System.out.println("\n\n\n=============================================================\n\n\n");
		
		idx = 1;


		for(TaintResultEntry entry: finalList){
			
			ExtractocolLogger.coloredLog("\n(" + (idx++) + "/" + finalList.size() + ") Printing ...", MYCOLOR.BLACK, colored);
			ExtractocolLogger.coloredLog("+ DP Stmt : " + entry.getDPStmt(), MYCOLOR.BLACK, colored);
			ExtractocolLogger.coloredLog("+ DP SootMethod : " + entry.getDPMethod(), MYCOLOR.BLACK, colored);
			
			ExtractocolLogger.coloredLog("+ EntryPoint :", MYCOLOR.BLACK, colored);
			ExtractocolLogger.coloredLog("\t+ " + entry.getEP(), MYCOLOR.RED, colored);
			
			if(InitialTaintResultContainer.tmType == TAINT_METHOD_TYPE.LIST)
				printInner1(entry);
			else if(InitialTaintResultContainer.tmType == TAINT_METHOD_TYPE.SET)
				printInner2(entry);
			
			System.out.println();
		}
	}
	
	public static void PrintRaw(){
		int idx = 1;
		
		System.out.println("\n\n\n=============================================================\n\n\n");
		
		idx = 1;


		for(TaintResultEntry entry: rawResult){
			
			ExtractocolLogger.coloredLog("\n(" + (idx++) + "/" + rawResult.size() + ") Printing ...", MYCOLOR.BLACK, colored);
			ExtractocolLogger.coloredLog("+ DP Stmt : " + entry.getDPStmt(), MYCOLOR.BLACK, colored);
			ExtractocolLogger.coloredLog("+ DP SootMethod : " + entry.getDPMethod(), MYCOLOR.BLACK, colored);
			
			ExtractocolLogger.coloredLog("+ EntryPoint :", MYCOLOR.BLACK, colored);
			ExtractocolLogger.coloredLog("\t+ " + entry.getEP(), MYCOLOR.RED, colored);
			
			//if(InitialTaintResultContainer.tmType == TAINT_METHOD_TYPE.LIST)
				printInner1(entry);
			//else if(InitialTaintResultContainer.tmType == TAINT_METHOD_TYPE.SET)
				//printInner2(entry);
			
			System.out.println();
		}
	}
	
	private static void printInner1(TaintResultEntry entry) {
		if(entry.getTaintMethodSet() == null)
			return;
		
		ExtractocolLogger.coloredLog("+ Tainted Methods & Variable ("+entry.getTaintMethodSet().size()+") : ", MYCOLOR.BLACK, colored);
		boolean EPprinted = false;
		int mainStream = 0;
		
		for(int i = entry.getTaintMethodSet().size() - 1; i >= 0; i--){
			String tab = "\t";
			if(i == 0) tab = "DP\t";
			else if(entry.getIsMainStreamSet().get(i) && !EPprinted) { tab = "EP\t"; EPprinted = true; }
			
			String r = String.format("%s++ (%7s) (%6s)  ", tab, entry.getTaintVariableSet().get(i), entry.getPropaTypes().get(i));
			
			if(entry.getIsMainStreamSet().get(i)){
				String m = String.format("[M%2d]", mainStream++);
			
				ExtractocolLogger.coloredLog(m + r  + entry.getTaintMethodSet().get(i) + "     ---  PrevStmt:  " + entry.getPrevStmts().get(i), MYCOLOR.RED, colored);
			}else
				ExtractocolLogger.coloredLog(r + "  " + entry.getTaintMethodSet().get(i) + "     ---  PrevStmt:  " + entry.getPrevStmts().get(i), MYCOLOR.BLACK, colored);
		}
	}
	
	private static void printInner2(TaintResultEntry entry) {
		if(entry.getFinalTaintMethods() == null)
			return;
		
		ExtractocolLogger.coloredLog("+ Tainted Methods ("+entry.getFinalTaintMethods().size()+") : ", MYCOLOR.BLACK, colored);
		
		for(String method : entry.getFinalTaintMethods()) {
			ExtractocolLogger.coloredLog("\t++ " + method, MYCOLOR.BLACK, colored);
		}
	}
}



class mergingThread implements Runnable{
	static List<TaintResultEntry> rawResultList;
	TaintResultEntry merger;
	
	public mergingThread(TaintResultEntry _merger) {
		this.merger = _merger;
	}
	
	public static void init(List<TaintResultEntry> _rawResultList) {
		rawResultList = _rawResultList;
	}
	
	public void run() {
		for(int i = 0 ; i < rawResultList.size(); i++) {
			if(!merger.getMergeeBit(i))
				continue;
			
			TaintResultEntry mergee = rawResultList.get(i);
			this.merger.addTaintMethodSetOf(mergee);
		}
	}
}

class analyzingTREThread implements Runnable {
	static List<TaintResultEntry> rawResultList;
	List<TaintResultEntry> currTREList;
	
	public analyzingTREThread(List<TaintResultEntry> _currTREList) {
		this.currTREList = _currTREList;
	}
	
	public static void init(List<TaintResultEntry> _rawResultList) {
		rawResultList = _rawResultList;
	}
	
	public void run() {
		for(TaintResultEntry currTRE: this.currTREList) {
			int currIdx = rawResultList.indexOf(currTRE);
			
			for(int i = 0; i < rawResultList.size(); i++) {
				TaintResultEntry target = rawResultList.get(i);
				
				// 0. Avoid itself
				if(target == currTRE)
					continue;
				
				// 1. Check whether its DP is same with the target
				if(!currTRE.SameDPWith(target))
					continue;
				
				// 2. Check whether its EP is in the taint method set of the target
				if(!target.getTaintMethodSet().contains(currTRE.getEP()))
					continue;
				
				// 3. Check whether the "main stream" is part of the target's
				if(!target.ContainMainStreamOf(currTRE))
					continue;
				
				// 4. Then, add the target into the merging list of the entry e
				// currTRE : TRE being merged with target
				// target  : TRE absorbing currTRE
				currTRE.setMergerBit(i);
				target.setMergeeBit(currIdx);
			}
		}
	}
}

/*class bitToSetThread implements Runnable {
	static List<TaintResultEntry> rawResultList;
	List<TaintResultEntry> currTREList;
	
	public bitToSetThread(List<TaintResultEntry> _currTREList) {
		this.currTREList = _currTREList;
	}
	
	public static void init(List<TaintResultEntry> _rawResultList) {
		rawResultList = _rawResultList;
	}
	
	public void run() {
		for(TaintResultEntry currTRE: this.currTREList) {
			for (int i = 0; i < rawResultList.size(); i++) {
				if (currTRE.getMergeeBit(i))
					currTRE.addMergee(rawResultList.get(i));

				if (currTRE.getMergerBit(i))
					currTRE.addMerger(rawResultList.get(i));
			}
		}
	}
}*/


