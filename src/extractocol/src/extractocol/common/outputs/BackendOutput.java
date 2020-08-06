package extractocol.common.outputs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

import QueryConvertor.QueryConverter;
import extractocol.Constants;
import extractocol.common.helper.MultiThreadHelper;
import extractocol.common.outputs.backendOutputHelper.ReqRespInfo;
import extractocol.common.outputs.backendOutputHelper.ResponseInfoEntry.SEED_TYPE;
import extractocol.common.pairing.SemanticAppliedList;
import extractocol.common.retrofit.RetrofitHandle;
import extractocol.common.tools.Pair;
import extractocol.common.tools.highScaleLib.NonBlockingHashSet;
import extractocol.common.valueEntry.ValueEntryTable;
import extractocol.common.valueEntry.ValueEntryTracking;
import extractocol.frontend.basic.ExtractocolLogger;
import extractocol.frontend.output.basic.TaintResultEntry;

public class BackendOutput {
	public static NonBlockingHashSet<ReqRespInfo> ReqRespInfoList = new NonBlockingHashSet<ReqRespInfo>(); // Outputs of Backend
	private static ArrayList<ReqRespInfo> rriList = new ArrayList<ReqRespInfo>();
	private static ArrayList<ReqRespInfo> finalList = new ArrayList<ReqRespInfo>();
	public static int totEPCnt;
	private static int dedup_stride = 40000;
	
	public static void SaveResult(ReqRespInfo rri, int visitCnt)
	{
		ReqRespInfoList.add(rri);
		if(Constants.backendMultiThread)
			ExtractocolLogger.Log("(Backend) # results: " + ReqRespInfoList.size() + "/" + totEPCnt + String.format(" -- [Visit: %5d]", visitCnt) + " DP Stmt: " + rri.reqie.getDPStmt());
	}
	
	public static void clear() {
		if(ReqRespInfoList != null) {
			for(ReqRespInfo rri: ReqRespInfoList)
				rri.clear();
			
			// it will be used if the backend is being called progressively
			ReqRespInfoList = new NonBlockingHashSet<ReqRespInfo>(); 
		}
		
		if(finalList != null)
			finalList.clear();
		
		if(rriList != null)
			rriList.clear();
	}
	
	public static void addReqRespInfo(ReqRespInfo rri) { ReqRespInfoList.add(rri); }
	public static NonBlockingHashSet<ReqRespInfo> getReqRespInfoList() { return ReqRespInfoList; }
	public static ArrayList<ReqRespInfo> getFinalReqRespInfoList() { return finalList; }
	
	
	public static String getVolleyMethod(String m){
		switch(m){
		case "0": return "GET";
		case "1": return "POST";
		case "2": return "PUT";
		case "3": return "DELETE";
		default: return "Unknown";
		}
	}
	
	/** Extract query parameters from Url
	 *  (& set extracted queries into rri entry)
	 */
	public static void extractQuery() {
		String urlRefined;
		Map<String, String> queryMap;
		
		for(ReqRespInfo rri: ReqRespInfoList) {
			// 0. refine url 
			urlRefined = urlRefinement(rri);
			
			// 1. extract query
			queryMap = QueryConverter.convert(urlRefined); // (TODO: replace it with queryExtractor by JM) You need to enable
			
			if(queryMap == null)
				continue; 
			
			// 2. set query
			for(Entry<String, String> e: queryMap.entrySet()) {
				String key = e.getKey();
				String value = e.getValue();
				
				rri.addRequestQuery(key, value);
			}
		}
	}
	
	public static String urlRefinement(String url) {
		url = url.replace("?", "\\?");
		url = url.replace("$", "\\$");
		url = url.replace(".*", "####");
		url = url.replace(".", "\\.");
		url = url.replace("####", ".*");
		url = url.replace("{", "\\{");
		url = url.replace("}", "\\}");
		url = url.replace("[", "\\[");
		url = url.replace("]", "\\]");
		
		return url;
	}
	
	private static String urlRefinement(ReqRespInfo rri) {
		String url = rri.reqie.getSignature();
		url = urlRefinement(url);
		//rri.reqie.setSignature(url);
		return url;
	}
	
	private static Map<String, String> temp(String url) {
		return new HashMap<String, String>();
	}
	
	public static void Finalize() {
		for(ReqRespInfo rri: ReqRespInfoList) {
			rri.reqie.initHeader();
			rri.reqie.initBody();
			finalList.add(rri);
		}
	}
	
	public static void deduplication(){
		int orig_size = ReqRespInfoList.size();
		int new_size;
		
		System.out.print("Deduplication of results ... ");
		
		for(Iterator<ReqRespInfo> iter = ReqRespInfoList.iterator(); iter.hasNext();){
			ReqRespInfo entry = iter.next();
			
			// To handle non-processed entry
			if(entry.getRequestInfoEntry().getSignature() == null
				|| entry.getRequestInfoEntry().getSignature().equals(RetrofitHandle.getRetrofitSignature())) 
			{
				entry.clear();
				iter.remove();
				continue;
			}
			
			boolean found = false;
			for(ReqRespInfo rri : ReqRespInfoList){
				if(entry == rri)
					continue;
				
				// Comparison for URI
				if(entry.reqie.compareTo(rri.reqie)){
					// (1) Header
					ValueEntryTracking.MergeTables(rri.reqie.getHeaderTable(), entry.reqie.getHeaderTable());
					
					// (2) Body
					ValueEntryTracking.MergeTables(rri.reqie.getBodyTable(), entry.reqie.getBodyTable());
					
					// (3) merge its heapTable & referenceTable with entry's.
					ValueEntryTracking.MergeTables(rri.getHeapTable(), entry.getHeapTable()/*cloned inside*/);
					ValueEntryTracking.MergeTables(rri.getReferenceTable(), entry.getReferenceTable()/*cloned inside*/);
				
					found = true;
				}
			}
			
			if(found) {
				entry.clear();
				iter.remove();
			}
		}
		
		new_size = ReqRespInfoList.size();
		
		System.out.println("Done. (" + orig_size + "->" + new_size + ")");
	}
	
	public static void deduplication_multiThreading(){
		System.out.print("Deduplication of results ... ");
		
		// 1. hashSet to List
		SetToList();
		
		// 2. find merger/mergee
		FindBits();
		
		// 3. exclude mergees from the list
		excludeMergees();
		
		// 4. merging
		merging();
	}
	
	public static void deduplication_new() {
		long start, end;
		System.out.print("Deduplication of results ... " + ReqRespInfoList.size());
		
		// 1. Transform raw set to list
		ArrayList<ReqRespInfo> rriList = rawSetToList();
		
		// 2. main loop
		while(true) {
			start = System.currentTimeMillis();
			rriList = deduplication_inner(rriList);
			end = System.currentTimeMillis();
			System.out.print(String.format(" --> %d (%ds)", rriList.size(), (end-start)/1000));
			
			if(rriList.size() < dedup_stride)
				break;
			
			Collections.shuffle(rriList);
		}
		
		// 3. Final deduplication
		start = System.currentTimeMillis();
		rriList = deduplication_inner(rriList);
		end = System.currentTimeMillis();
		System.out.print(String.format(" --> %d (%ds)", rriList.size(), (end-start)/1000));
		
		// 4. Finalize
		finalize(rriList);
		
		System.out.println();
	}
	
	private static void finalize(ArrayList<ReqRespInfo> rriList) {
		for(ReqRespInfo rri: rriList) {
			rri.reqie.initHeader();
			rri.reqie.initBody();
			rri.clearBits();
			finalList.add(rri);
		}
	}
	
	private static ArrayList<ReqRespInfo> deduplication_inner(ArrayList<ReqRespInfo> rriList) {
		if (rriList.size() > dedup_stride)
			return splitAndMerge(rriList);
		else
			return merge(rriList);
	}
	
	private static ArrayList<ReqRespInfo> splitAndMerge(ArrayList<ReqRespInfo> rriList){
		ArrayList<ArrayList<ReqRespInfo>> tot = new ArrayList<ArrayList<ReqRespInfo>>();
		ArrayList<ReqRespInfo> result = new ArrayList<ReqRespInfo>();
		ArrayList<ReqRespInfo> partialList = null;
		
		// 1. split
		for(int i = 0; i < rriList.size(); i++) {
			if(i % dedup_stride == 0) {
				partialList = new ArrayList<ReqRespInfo>();
				tot.add(partialList);
			}
			
			ReqRespInfo rri = rriList.get(i);
			partialList.add(rri);
		}
		
		// 2. merge
		for(ArrayList<ReqRespInfo> pList : tot) {
			result.addAll(merge(pList));
			System.gc();
		}
		
		return result;
	}
	
	private static void clearMergerMergeeBits(ArrayList<ReqRespInfo> rriList) {
		for(ReqRespInfo rri: rriList)
			rri.clearMergerMergeeBit();
	}
	
	private static ArrayList<ReqRespInfo> merge(ArrayList<ReqRespInfo> origList){
		ArrayList<ReqRespInfo> refinedList = new ArrayList<ReqRespInfo>(origList);
		
		// 1. allocate bits
		allocateBitsAndResetNeedToStay(origList);
		
		// 2. find merger/mergee
		findBits(refinedList, origList);
		
		// 3. exclude mergees from the list
		excludeMergees(refinedList, origList);
		
		// 4. merging
		merging(refinedList, origList);
		
		// 5. clear merger/mergee bits
		clearMergerMergeeBits(origList);
		System.gc();
		
		return refinedList;
	}
	
	private static void allocateBitsAndResetNeedToStay(ArrayList<ReqRespInfo> rriList) {
		int size = rriList.size();
		for(ReqRespInfo rri: rriList) {
			rri.allocMergerMergeeBit(size);
			rri.setNeedToStay(false);
		}
	}

	private static void findBits(ArrayList<ReqRespInfo> refinedList, ArrayList<ReqRespInfo> origList) {
		ThreadPoolExecutor executor = MultiThreadHelper.createExecutor();
		analyzingThread.init(origList);
		
		for(Iterator<ReqRespInfo> iter = refinedList.iterator(); iter.hasNext();) {
			ReqRespInfo entry = iter.next();
			
			// To handle non-processed entry
			if(entry.getRequestInfoEntry().getSignature() == null
				|| entry.getRequestInfoEntry().getSignature().equals(RetrofitHandle.getRetrofitSignature())) 
			{
				entry.clear();
				iter.remove();
				continue;
			}
			
			executor.execute(new analyzingThread(entry));
		}
		
		//start = System.currentTimeMillis();
		MultiThreadHelper.awaitCompletion(executor, Constants.maxMergingTime, Constants.mergingTimeUnit, null, null);
		//end = System.currentTimeMillis();
		System.gc();
		//System.out.print( "(Find mergers -- " + (end-start)/1000 + "s, ");
		//System.out.print("(F)");
	}
	
	private static void excludeMergees(ArrayList<ReqRespInfo> refinedList, ArrayList<ReqRespInfo> origList) {
		long start, end;
		int cnt = 0;
		
		start = System.currentTimeMillis();
		for(Iterator<ReqRespInfo> iter = refinedList.iterator(); iter.hasNext();) {
			ReqRespInfo e = iter.next();
			
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
				
				ReqRespInfo merger = origList.get(i);
				
				if(!merger.getMergerBit(currIdx) || merger.getNeedToStay())
					break;
				
				cnt++;
			}
			
			if(cnt == e.getMergerListSize()) {
				e.setNeedToStay(true);
				continue;
			}
				
			iter.remove();
		}
		end = System.currentTimeMillis();
		//System.out.print("Reduced to " + refinedList.size() + " -- " + (end-start)/1000 + "s, ");
		//System.out.print("(E)");
	}
	
	private static void merging(ArrayList<ReqRespInfo> refinedList, ArrayList<ReqRespInfo> origList) {
		long start, end;
		ThreadPoolExecutor executor = MultiThreadHelper.createExecutor();
		mergingThread.init(origList);
		
		for(ReqRespInfo entry: refinedList) {
			executor.execute(new mergingThread(entry));
		}
		
		start = System.currentTimeMillis();
		MultiThreadHelper.awaitCompletion(executor, Constants.maxMergingTime, Constants.mergingTimeUnit, null, null);
		end = System.currentTimeMillis();
		System.gc();
		//System.out.println( "Merging -- " + (end-start)/1000 + "s)");
		//System.out.print("(M)");
	}
	
	private static ArrayList<ReqRespInfo> rawSetToList(){
		ArrayList<ReqRespInfo> rriList = new ArrayList<ReqRespInfo>();
		for(ReqRespInfo rri: ReqRespInfoList) 
			rriList.add(rri);
		return rriList;
	}
	
	private static void SetToList() {
		int totSize = ReqRespInfoList.size();
		for(ReqRespInfo rri: ReqRespInfoList) {
			rri.allocMergerMergeeBit(totSize);
			rriList.add(rri);
		}
	}
	
	private static void FindBits() {
		long start, end;
		ThreadPoolExecutor executor = MultiThreadHelper.createExecutor();
		analyzingThread.init(rriList);
		
		for(Iterator<ReqRespInfo> iter = ReqRespInfoList.iterator(); iter.hasNext();) {
			ReqRespInfo entry = iter.next();
			
			// To handle non-processed entry
			if(entry.getRequestInfoEntry().getSignature() == null
				|| entry.getRequestInfoEntry().getSignature().equals(RetrofitHandle.getRetrofitSignature())) 
			{
				entry.clear();
				iter.remove();
				continue;
			}
			
			executor.execute(new analyzingThread(entry));
		}
		
		start = System.currentTimeMillis();
		MultiThreadHelper.awaitCompletion(executor, Constants.maxMergingTime, Constants.mergingTimeUnit, null, null);
		end = System.currentTimeMillis();
		System.gc();
		System.out.print( "(Find mergers -- " + (end-start)/1000 + "s, ");
	}
	
	
	
	private static void excludeMergees() {
		long start, end;
		int cnt = 0;
		
		start = System.currentTimeMillis();
		for(Iterator<ReqRespInfo> iter = ReqRespInfoList.iterator(); iter.hasNext();) {
			ReqRespInfo e = iter.next();
			
			// can stay if it has no merger
			if(e.getMergerListSize() == 0) {
				e.setNeedToStay(true);
				continue;
			}
			
			// Count mergers whose mergee list contain me and needToStay is false 
			cnt = 0;
			int currIdx = rriList.indexOf(e);
			for(int i = 0 ; i < rriList.size(); i++) {
				if(!e.getMergerBit(i))
					continue;
				
				ReqRespInfo merger = rriList.get(i);
				
				if(!merger.getMergerBit(currIdx) || merger.getNeedToStay())
					break;
				
				cnt++;
			}
			
			if(cnt == e.getMergerListSize()) {
				e.setNeedToStay(true);
				continue;
			}
				
			iter.remove();
		}
		end = System.currentTimeMillis();
		System.out.print("Reduced to " + ReqRespInfoList.size() + " -- " + (end-start)/1000 + "s, ");
	}
	
	private static void merging() {
		long start, end;
		ThreadPoolExecutor executor = MultiThreadHelper.createExecutor();
		mergingThread.init(rriList);
		
		for(ReqRespInfo entry: ReqRespInfoList) {
			executor.execute(new mergingThread(entry));
		}
		
		start = System.currentTimeMillis();
		MultiThreadHelper.awaitCompletion(executor, Constants.maxMergingTime, Constants.mergingTimeUnit, null, null);
		end = System.currentTimeMillis();
		System.gc();
		System.out.println( "Merging -- " + (end-start)/1000 + "s)");
	}
}

class mergingThread implements Runnable{
	static List<ReqRespInfo> rawResultList;
	ReqRespInfo merger;
	
	public mergingThread(ReqRespInfo _merger) {
		this.merger = _merger;
	}
	
	public static void init(List<ReqRespInfo> _rawResultList) {
		rawResultList = _rawResultList;
	}
	
	public void run() {
		for(int i = 0 ; i < rawResultList.size(); i++) {
			if(!merger.getMergeeBit(i))
				continue;
			
			ReqRespInfo mergee = rawResultList.get(i);
			
			// (1) Header
			ValueEntryTracking.MergeTables(merger.reqie.getHeaderTable(), mergee.reqie.getHeaderTable());
			
			// (2) Body
			ValueEntryTracking.MergeTables(merger.reqie.getBodyTable(), mergee.reqie.getBodyTable());
			
			// (3) merge its heapTable & referenceTable with entry's.
			ValueEntryTracking.MergeTables(merger.getHeapTable(), mergee.getHeapTable()/*cloned inside*/);
			ValueEntryTracking.MergeTables(merger.getReferenceTable(), mergee.getReferenceTable()/*cloned inside*/);
			
		}
	}
}

class analyzingThread implements Runnable{
	static List<ReqRespInfo> rawResultList;
	ReqRespInfo currRRI;
	
	public analyzingThread(ReqRespInfo _merger) {
		this.currRRI = _merger;
	}
	
	public static void init(List<ReqRespInfo> _rawResultList) {
		rawResultList = _rawResultList;
	}
	
	public void run() {
		int currIdx = rawResultList.indexOf(this.currRRI);
		
		for(int i = 0 ; i < rawResultList.size(); i++) {
			ReqRespInfo rri = rawResultList.get(i);
			
			if(this.currRRI == rri)
				continue;
			
			if(!this.currRRI.reqie.compareTo(rri.reqie))
				continue;

			this.currRRI.setMergerBit(i);
			rri.setMergeeBit(currIdx);
		}
	}
}
