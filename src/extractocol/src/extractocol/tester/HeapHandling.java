package extractocol.tester;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import QueryConvertor.QueryConverter;
import extractocol.Constants;
import extractocol.backend.request.heapManagement.SourceforTaint;
import extractocol.backend.request.helper.JimpleLoader;
import extractocol.common.outputs.BackendOutput;
import extractocol.common.outputs.backendOutputHelper.ReqRespInfo;
import extractocol.common.outputs.backendOutputHelper.RequestInfoEntry;
import extractocol.common.outputs.helper.BackendOutputHelper;
import extractocol.common.outputs.helper.HeapHandlingHelper;
import extractocol.common.outputs.helper.StdOutputController;
import extractocol.common.outputs.helper.HeapHandlingHelper.POSITION1;
import extractocol.common.regex.RegexHandler;
import extractocol.common.tools.Pair;
import extractocol.common.tools.highScaleLib.NonBlockingHashSet;
import extractocol.common.trackers.tools.General;
import extractocol.common.trackers.tools.HeapToVEL;
import extractocol.common.valueEntry.PartofUrlStringTable;
import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntryTable;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;
import extractocol.common.valueEntry.node.Constant;
import extractocol.common.valueEntry.node.Heap;
import extractocol.common.valueEntry.node.List;
import extractocol.frontend.MyTest;
import extractocol.frontend.helper.PropagateHelper;
import extractocol.frontend.output.basic.TaintResultEntry;
import soot.*;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.infoflow.android.TestApps.Test;
import soot.jimple.toolkits.callgraph.CallGraph;

/**
 * 
 * @author Byungkwon Choi
 *
 */
public class HeapHandling {
	static ArrayList<ReqRespInfo> reqRespInfoList;
	private static ArrayList<Pair> addedBody;
	private static Integer SeqNum = 0;
	
	public static String heapP = "<[a-z.,A-Z0-9,_ :$]*>";
	public static Pattern heapPattern = Pattern.compile(heapP);
	private static String GETLISTFROMANALYSIS = "#GetListFromAnalysis#";
	
	
	private static boolean debug = false; // Print all logs for frontend and backend
	private static boolean printRawOnly = false; // Print raw signature (without heap handling) if printRawOnly is true
	private static long start_handling, end_handling;
	
	private static ValueEntryList HeapValue;
	private static int oldMaxMethodVisitCount;
	private static int newMaxMethodVisitCount = 60; // for request sig. building of Backend (WARNING: There might be false negative.)
	private static int maxEPCnt = 30/*250*/; // limit of processing EP count when tracking heap object (WARNING: There might be false negative.)
	
	public static void main(String[] args) {
		// 0. Argument handling
		ArgumentProcessing(args);
		
		// 1. Initialization 
		if(!Initialization())
			return;
		
		// 2. Main processing
		for(int i = 0; i < reqRespInfoList.size(); i++){
			if(filtering(reqRespInfoList.get(i)))
				continue;
			
			//if(!reqRespInfoList.get(i).reqie.getSignature().contains("product/get")) // For debugging (by BK)
				//continue;
			
			System.out.println("[Request " + i + "]:");
			
			// 2-1. Handle heaps in URI signature
			HandleURI(i, reqRespInfoList.get(i).reqie);

			// 2-2. Handle heaps in Query signature
			HandleQuery(i, reqRespInfoList.get(i).reqie);
						
			// 2-3. Handle heaps in Header signature
			HandleHeader(i, reqRespInfoList.get(i).reqie);
			
			// 2-4. Handle heaps in Body signature
			HandleBody2(i, reqRespInfoList.get(i).reqie);
		}
		
		// 3. Print elapsed time
		PrintElapsedTime();
		
		// 4. Save the result
		SaveResultAndGenerateSignatures();
		
		// 5. Print the result in the console
		PrintResult(false);
		
		if(Constants.isNotFullStack())
			System.exit(0);
	}
	
	private static boolean isExcludedHeaps(String heap) {
		return heap.startsWith("<rx.") ||
				heap.startsWith("<com.annimon.") ||
				heap.startsWith("<android.");
	}
	
	/*****************************************************/
	/***            APIs for heap handling             ***/
	/*****************************************************/
	/** Wrapper for handling heap in URI
	 * 
	 * @param succID ID of current info entry
	 * @param rif Info entry of request
	 */
	private static void HandleURI(int succID, RequestInfoEntry rif){
		String heapString = rif.getSignature();
		SeqNum = 0;

		System.out.println("\t[URI]");
		System.out.println("\t URI (Original): " + heapString);
		
		heapString = HandleHeap_1stPhase_URI(succID, heapString);

		// 1. get refined list
		ArrayList<String> refinedList = RegexHandler.M(heapString);
		
		
		// 2. set whole regex signature
		//ArrayList<String> forWholeRegex = new ArrayList<String>();
		//for(String refined: refinedList)
			//forWholeRegex.add(ReplaceHeapWithAsterisk(refined));
		
		
		// 3. separate query from urls & set
		for(String refined: refinedList) {
			//String urlRefined = BackendOutput.urlRefinement(refined);
			
			// 3-1. extract query
			//Map<String, String> queryMap = QueryConverter.convert(urlRefined); // (TODO: replace it with queryExtractor by JM) You need to enable
			Map<String, String> queryMap = QueryConverter.convert2(refined);
			
			if(queryMap == null)
				continue; 
			
			// 3-2. add query
			for(Entry<String, String> e: queryMap.entrySet()) {
				String key = e.getKey();
				String value = e.getValue();
				
				rif.addQuery(key, value);
			}
		}
		rif.initQuery();
		
		// 4. remove query from urls
		ArrayList<String> withoutQuery = RegexHandler.RemoveQuery(refinedList);
		

		// 5. set regex sig list
		for(String url: withoutQuery)
			rif.addRegexSignature(ReplaceHeapWithAsterisk(url));
		
		rif.initWholeRegexSignature();
		System.out.println("\t URI (Regex Signature w/o query): ");
		System.out.println("\t\t " + rif.getWholeRegexSignature());
		
		System.out.println("\t URI (Regex Sub-Signature): ");
		for(String regexStr: rif.getRegexSignature())
			System.out.println("\t\t " + regexStr);
		
		
		// 6. set proxy sig list
		for(String refined: withoutQuery) {
			heapString = HandleHeap_2ndPhase(succID, refined, POSITION1.URI);
			rif.addProxySignature(heapString);
		}
		
		System.out.println("\t URI (Proxy Sub-Signature): "); 
		for(String proxyStr: rif.getProxySignature())
			System.out.println("\t\t " + proxyStr);
		System.out.println();
		
		
		// 2-1-1. Apply lazy semantic model
		/*PartofUrlStringTable.deserialize();
		if (!PartofUrlStringTable.LazySemantic(rif)) {
			rif.setRegexSignature(ReplaceHeapWithAsterisk(heapString));
		}
		else
			heapString = rif.getRegexSignature();*/

		
	}
	
	/** Wrapper for handling heap in Query
	 * 
	 * @param succID ID of current info entry
	 * @param rif Info entry of request
	 */
	private static void HandleQuery(int succID, RequestInfoEntry rif){
		System.out.println("\t[Query]");
		if(rif.Query != null) {
			for(Pair p: rif.Query){
				System.out.println("\t (Key, Value): " + p.getKey() + ", " + p.getValue());
				HandleKey(succID, p, POSITION1.QUERY);
				HandleValue(succID, p, POSITION1.QUERY);
			}
		}
		System.out.println("");
	}
	
	/** Wrapper for handling heap in Header
	 * 
	 * @param succID ID of current info entry
	 * @param rif Info entry of request
	 */
	private static void HandleHeader(int succID, RequestInfoEntry rif){
		System.out.println("\t[Header]");
		if(rif.Header != null) {
			for(Pair p: rif.Header){
				System.out.println("\t (Key, Value): " + p.getKey() + ", " + p.getValue());
				HandleKey(succID, p, POSITION1.HEADER);
				HandleValue(succID, p, POSITION1.HEADER);
			}
		}
		System.out.println("");
	}
	
	/** Wrapper for handling heap in Body
	 * 
	 * @param succID ID of current info entry
	 * @param rif Info entry of request
	 */
	private static void HandleBody(int succID, RequestInfoEntry rif){
		String heapString;
		addedBody = new ArrayList<Pair>();
		
		System.out.println("\t[Body]");
		
		if(rif.Body != null) {
			for(Iterator<Pair> iter = rif.Body.iterator(); iter.hasNext();){
				Pair p = iter.next();
				
				System.out.println("\t (Key, Value): " + p.getKey() + ", " + p.getValue());
				
				// 1. Handle Key
				HandleKey(succID, p, POSITION1.BODY);
				
				// 2. Handle Value
				// 2-1. Replace <heap> in value with constant value
				heapString = HandleHeap_1stPhase(succID, p.getValue(), 0, new ArrayList<String>(), p.getKey());
				
				if(heapString.contains(GETLISTFROMANALYSIS)){
					iter.remove();
				}else{
					p.setRegexSigValue(ReplaceHeapWithAsterisk(heapString));

					// 2-2. Replace <heap> in value with proxy sign <#>
					heapString = HandleHeap_2ndPhase(succID, heapString, POSITION1.BODY);
					p.setProxySigValue(heapString);
				}
			}
			
			for(Pair p: addedBody){
				System.out.println("\t (Key, Value): " + p.getKey() + ", " + p.getValue());
				// 1. Handle Key
				HandleKey(succID, p, POSITION1.BODY);
				
				// 2. Handle Value
				HandleValue(succID, p, POSITION1.BODY);
			}
			
			rif.Body.addAll(addedBody);
		}
		
		System.out.println();
	}
	
	private static void HandleBody2(int succID, RequestInfoEntry rif){
		String heapString;
		addedBody = new ArrayList<Pair>();
		
		System.out.println("\t[Body]");
		
		if(rif.Body == null) {
			System.out.println();
			return;
		}
		
		for(Iterator<Pair> iter = rif.Body.iterator(); iter.hasNext();){
			Pair p = iter.next();
			
			System.out.println("\t (Key, Value): " + p.getKey() + ", " + p.getValue());
			
			// 1. Handle Key
			HandleKey(succID, p, POSITION1.BODY);
			
			// 2. Handle Value
			// 2-1. Replace <heap> in value with constant value
			ValueEntryList v = new ValueEntryList();
			Matcher m = heapPattern.matcher(p.getValue());
			
			if(heapPattern.matcher(p.getValue()).find()) {
				while (m.find()){
					// 1. Extract heap
					String heap = m.group();
					ValueEntryList vel = new ValueEntryList();
					vel.addValueEntry(heap, SOURCE_TYPE.HEAP, false);
					vel = HandleHeap_1stPhase_VEL(succID, vel, 0, new ArrayList<String>());
					
					v.addValueEntryList(vel, false);
				}
			}else
				v.addValueEntry(p.getValue(), SOURCE_TYPE.CONSTANT, false);
			
			
			if(doesContainList(v)) {
				iter.remove();
				
				ValueEntry list = v.getValueEntryOf(SOURCE_TYPE.LIST);
				
				for(String s: ((List)list).getList())
					addedBody.add(new Pair(p.getKey(), s));
				
			}else {
				heapString = v.GenRegex();
				heapString = RegexHandler.Refinement(heapString);
				p.setRegexSigValue(ReplaceHeapWithAsterisk(heapString));

				// 2-2. Replace <heap> in value with proxy sign <#>
				heapString = HandleHeap_2ndPhase(succID, heapString, POSITION1.BODY);
				p.setProxySigValue(heapString);					
			}
			
			v.Clear();
		}
		
		for(Pair p: addedBody){
			System.out.println("\t (Key, Value): " + p.getKey() + ", " + p.getValue());
			// 1. Handle Key
			HandleKey(succID, p, POSITION1.BODY);
			
			// 2. Handle Value
			HandleValue(succID, p, POSITION1.BODY);
		}
		
		rif.Body.addAll(addedBody);
		
		System.out.println();
	}
	
	/** Replace <heap> in Pair p with constant value or proxy sign <#> 
	 * 
	 * @param succID ID of current info entry
	 * @param p Pair including key & value of header or body
	 * @param pos POSITION1 (BODY or HEADER)
	 */
	private static void HandleKey(int succID, Pair p, POSITION1 pos){
		String heapString;
				
		// 1. Handle Key
		// 1-1. Replace <heap> in key with constant value
		//heapString = HandleHeap_1stPhase(succID, p.getKey(), 0, new ArrayList<String>());
		heapString = ReplaceHeapToString(succID, p.getKey());
		p.setRegexSigKey(ReplaceHeapWithAsterisk(heapString));
		
		// 1-2. Replace <heap> in key with proxy sign <#>
		heapString = HandleHeap_2ndPhase(succID, heapString, pos);
		p.setProxySigKey(heapString);
	}
	
	/** Replace <heap> in Pair p with constant value or proxy sign <#> 
	 * 
	 * @param succID ID of current info entry
	 * @param p Pair including key & value of header or body
	 * @param pos POSITION1 (BODY or HEADER)
	 */
	private static void HandleValue(int succID, Pair p, POSITION1 pos){
		String heapString;
		
		// 2. Handle Value
		// 2-1. Replace <heap> in value with constant value
		//heapString = HandleHeap_1stPhase(succID, p.getValue(), 0, new ArrayList<String>());
		heapString = ReplaceHeapToString(succID, p.getValue());
		p.setRegexSigValue(ReplaceHeapWithAsterisk(heapString));
		
		// 2-2. Replace <heap> in value with proxy sign <#>
		heapString = HandleHeap_2ndPhase(succID, heapString, pos);
		p.setProxySigValue(heapString);
	}
	
	private static String ReplaceHeapToString(int succID, String stringWithHeap){
		Matcher m = heapPattern.matcher(stringWithHeap);
		
		while (m.find()){
			// 1. Extract heap
			String heap = m.group();
			
			ValueEntryList vel = new ValueEntryList();
			vel.addValueEntry(heap, SOURCE_TYPE.HEAP, false);
			vel = HandleHeap_1stPhase_VEL(succID, vel, 0, new ArrayList<String>());
			stringWithHeap = stringWithHeap.replace(heap, ResultRefinery(vel.GenRegex()));
			vel.Clear();
		}
		
		stringWithHeap = RegexHandler.Refinement(stringWithHeap);
		return stringWithHeap;
	}
	
	/** Finalize heap tracking and generate proxy signatures
	 * 
	 * @param succID ID of successor
	 * @param HeapString URI that might include heaps
	 * @return URI whose heaps are replaced to specific value
	 */
	private static String HandleHeap_2ndPhase(int succID, String HeapString, POSITION1 pos){
		Matcher m = heapPattern.matcher(HeapString);
		
		while (m.find()){
			// 1. Extract heap
			String heap = m.group();
			
			// 2. Check whether the heap is from other responses
			if(GenerateSignature(succID, heap, SeqNum, pos)){
				// 2-1. replace heap to <SeqNum> and increase SeqNum
				HeapString = HeapString.replaceFirst(Pattern.quote(heap), "<"+ SeqNum.toString() +">");
				
				// 2-2. Increase the sequence number
				SeqNum ++;
			}
		}
		
		return HeapString;
	}
	
	/** Replace heaps in URI signature to specific value
	 * 
	 * @param succID ID of successor
	 * @param HeapString URI that might include heaps
	 * @param depth level of heap
	 * @return URI whose heaps are replaced to specific value
	 */
	private static String HandleHeap_1stPhase(int succID, String HeapString, int depth, ArrayList<String> heapStack){
		return HandleHeap_1stPhase(succID, HeapString, depth, heapStack, null);
	}
	
	private static String HandleHeap_1stPhase_URI(int succID, String UrIWithHeap){
		Matcher m = heapPattern.matcher(UrIWithHeap);
		
		while (m.find()){
			// 1. Extract heap
			String heap = m.group();
						
			ValueEntryList vel = new ValueEntryList();
			vel.addValueEntry(heap, SOURCE_TYPE.HEAP, false);
			vel = HandleHeap_1stPhase_VEL(succID, vel, 0, new ArrayList<String>());
			
			UrIWithHeap = UrIWithHeap.replace(heap, ResultRefinery(vel.GenRegex()));
			vel.Clear();
		}
		
		return UrIWithHeap;
	}
	
	/** Get value assigned to the heap in Jimple code
	 * 
	 * @param heap Name of heap
	 * @return Candidate string value of the heap
	 */
	private static String getAssignedValue(String heap) {
		String result = "";
		HeapValue = getAssignedValueEntryList(heap);
		
		if(HeapValue.doesContainTypeOf(SOURCE_TYPE.LIST)){
			result = GETLISTFROMANALYSIS;
		}else{
			result = HeapValue.GenRegex();
		}
		
		return result.equals("") ? ".*" : result;
	}
	
	private static ValueEntryList getAssignedValueEntryList(String heap) {
		if(!debug) StdOutputController.stop();
		ValueEntryList res = HeapToVEL.M(heap, debug);
		if(!debug) StdOutputController.start();
		
		return res;
	}
	
	/** Generate proxy signature(s) 
	 * 
	 * @param succID ID of successor
	 * @param heap name of heap
	 * @param SeqNum sequence number
	 * @return true if, and only if the heap is from other response(s)
	 */
	private static boolean GenerateSignature(int succID, String heap, Integer seqNum, POSITION1 pos){
		// A proxy signature would be generated in TrackDependencyAndGenerateSignature().
		return HeapHandlingHelper.TrackDependencyAndGenerateSignature(succID, pos, "<"+ seqNum.toString() +">", heap, reqRespInfoList);
	}
	
	/*****************************************************/
	/***        APIs for heap handling (body)          ***/
	/*****************************************************/
	/** Replace heaps in URI signature to specific value
	 * 
	 * @param succID ID of successor
	 * @param HeapString URI that might include heaps
	 * @param depth level of heap
	 * @return URI whose heaps are replaced to specific value
	 */
	private static String HandleHeap_1stPhase(int succID, String HeapString, int depth, ArrayList<String> heapStack, String bodyKey){
		Matcher m = heapPattern.matcher(HeapString);
		
		String heap;
		String res;
		
		while (m.find()){
			// 1. Extract heap
			heap = m.group();

			PrintTab(depth); System.out.print("Heap: " + heap); 
			
			// 2. Check whether the heap has been already analyzed
			if(heapStack.contains(heap)){
				res = ".*"; // the result will be '.*' if the heap exists in the heap stack
			}else{
				// 4. Check whether the heap has been analyzed before
				if(!isHeapAnalyzed(heap))
				{
					// 4-1. Check whether the heap is from other responses
						if(isFromOtherResponse_URI(succID, heap))
						{
							// Print Result
							System.out.println(" => From other transaction(s)");
							continue;
						}
						// 4-2. Need to analyze source code to get the value of the heap
						else
						{
							// Get value assigned to the heap from code
							res = getAssignedValue(heap);
						}
				}else{
					// 5. Check whether the saved result contains 'List' entry
					if(getHeapResult(heap).doesContainTypeOf(SOURCE_TYPE.LIST)){
						// Take it from the saved result if it contains 'List' entry
						res = GETLISTFROMANALYSIS;
						HeapValue = getHeapResult(heap);
					}
					else
						res = getHeapResult(heap).GenRegex(); // Generate regex of the saved result if it does not
				}
				
				// 6. Check whether the result contains 'List' entry
				if(res.equals(GETLISTFROMANALYSIS)){
					if(bodyKey != null){
						// 6-1. Save the result
						SaveHeapResult(heap, HeapValue.Clone());
						
						// 6-2. Take the list from the result
						ValueEntry list = HeapValue.getValueEntryOf(SOURCE_TYPE.LIST);
						
						// 6-3. Add pair entries 
						for(String s: ((List)list).getList())
							addedBody.add(new Pair(bodyKey, s));
					}else{
						res = HeapValue.GenRegex();
					}
				}
				
				// 7. Check whether the value contains heap
				if(doesContainHeap(res)){
					// Added by Jeongmin Tracking HeapString
					PartofUrlStringTable.addHeapConstantList(heap, res);

					// Save the result first
					SaveHeapResult(heap, res);
					
					// Print the result
					System.out.println(" => " + res);
					
					// Add the heap into the heap stack if it does not exist in the stack
					heapStack.add(heap);
					
					// if it does, call HandleHeapInURI() recursively
					res = HandleHeap_1stPhase(succID, res, depth + 1, heapStack);
					
					// Remove it from the stack
					heapStack.remove(heap);
					
					PrintTab(depth);
				}
				
				// 8. Save the result
				if(!res.equals(GETLISTFROMANALYSIS))
					SaveHeapResult(heap, res);
					
			}
				
			// 9. replace heap to res
			HeapString = HeapString.replace(heap, ResultRefinery(res));

			// 10. Print result
			System.out.println(" => " + res);
			
		}
		
		return HeapString;
	}
	
	private static ValueEntryList HandleHeap_1stPhase_VEL(int succID, ValueEntryList vel, int depth, ArrayList<String> heapStack){
		
		Heap heapEntry = (Heap) vel.getValueEntryOf(SOURCE_TYPE.HEAP);
		ValueEntryList tempVel = new ValueEntryList();
		boolean heapInString = false;
		
		for(Iterator<String> iter = heapEntry.getHeapList().iterator(); iter.hasNext();) {
			String heap = iter.next();
			ValueEntryList res = null;
			
			PrintTab(depth); System.out.print("Heap: " + heap); 
			
			// 2. Check whether the heap has been already analyzed or the heap is excluded
			if(heapStack.contains(heap) || isExcludedHeaps(heap)){
				res = new ValueEntryList();
				res.addValueEntry(".*", SOURCE_TYPE.CONSTANT, false); // the result will be '.*' if the heap exists in the heap stack
			}else{
				// 4. Check whether the heap has been analyzed before
				if(!isHeapAnalyzed(heap))
				{
					// 4-1. Check whether the heap is from other responses
					if(isFromOtherResponse_URI(succID, heap))
					{
						// Print Result
						System.out.println(" => From other transaction(s)");
						continue;
					}
					// 4-2. Need to analyze source code to get the value of the heap
					else
					{
						// Get value assigned to the heap from code
						res = getAssignedValueEntryList(heap);
					}
				}else{
					res = getHeapResult(heap).Clone();
				}
				
				// 7. Check whether the value contains heap
				if(doesContainHeap(res)){
					// Added by Jeongmin Tracking HeapString
					PartofUrlStringTable.addHeapConstantList(heap, res.GenRegex());

					// Print the result
					System.out.println(" => " + res.GenRegex());
					
					// Add the heap into the heap stack if it does not exist in the stack
					heapStack.add(heap);
					
					// if it does, call HandleHeapInURI() recursively
					res = HandleHeap_1stPhase_VEL(succID, res, depth + 1, heapStack);
					
					// Remove it from the stack
					heapStack.remove(heap);
					
					
				}
				
				// Handle constant value entry
				if(doesContainConstants(res)){
					Constant constEntry = (Constant) res.getValueEntryOf(SOURCE_TYPE.CONSTANT);
					ArrayList<String> newConst = new ArrayList<String>();
					
					for(Iterator<String> iter2 = constEntry.getConstantList().iterator(); iter2.hasNext();) {
						String c = iter2.next();
						Matcher m = heapPattern.matcher(c);
						
						while (m.find()){
							String h = m.group();
							
							// Add the heap into the heap stack if it does not exist in the stack
							heapStack.add(h);
							
							// if it does, call HandleHeapInURI() recursively
							ValueEntryList v = new ValueEntryList();
							v.addValueEntry(h, SOURCE_TYPE.HEAP, false);
							v = HandleHeap_1stPhase_VEL(succID, v, depth + 1, heapStack);
							
							// Remove it from the stack
							heapStack.remove(h);
							
							c = c.replace(h, v.GenRegex());
							v.Clear();
							heapInString = true;
						}
						
						newConst.add(c);
						iter2.remove();
					}
					
					for(String nC: newConst)
						constEntry.addEntry(nC);
					newConst.clear();
				}
				
				
			}
			
			if(doesContainHeap(res) || heapInString)
				PrintTab(depth);
				
			// 9. replace heap to res
			iter.remove();
			tempVel.addValueEntryList(res.Clone(), false);

			// 10. Print result
			if(doesContainList(res))
				System.out.println(" => ## List ##");
			else
				System.out.println(" => " + res.GenRegex());
			
			res.Clear();
		}
		
		vel.addValueEntryList(tempVel.Clone(), false);
		tempVel.Clear();
		return vel;
	}
	
	
	
	/*****************************************************/
	/***                  Helper APIs                  ***/
	/*****************************************************/
	/** Check whether the heap is from other responses
	 * 
	 * @param succID ID of successor
	 * @param heap name of heap
	 * @param SeqNum sequence number
	 * @return true if, and only if the heap is from other response(s)
	 */
	private static boolean isFromOtherResponse_URI(int succID, String heap){
		return HeapHandlingHelper.doesHeapComeFromOtherTransactions(succID, heap, reqRespInfoList);
	}
	
	private static void PrintTab(int depth){
		for(int i = 0; i < depth + 2; i++)
			System.out.print("\t");
	}
	
	/** Check whether the value contains heap
	 * 
	 * @param heap
	 * @return True if, and only if, the string contains heap 
	 */
	public static boolean doesContainHeap(String _string){
		Matcher m = heapPattern.matcher(_string);
		
		return m.find();
	}
	
	public static boolean doesContainHeap(ValueEntryList vel){
		return vel.getValueEntryOf(SOURCE_TYPE.HEAP) != null;
	}
	
	public static boolean doesContainList(ValueEntryList vel){
		return vel.getValueEntryOf(SOURCE_TYPE.LIST) != null;
	}
	
	public static boolean doesContainConstants(ValueEntryList vel){
		return vel.getValueEntryOf(SOURCE_TYPE.CONSTANT) != null;
	}

	/** Replace <heap> with '(.*)'
	 * 
	 * @param input string with <heap>
	 * @return String replaced <heap> with '(.*)'
	 */
	private static String ReplaceHeapWithAsterisk(String input){
		return input.replaceAll(heapP, "(.*)");
	}
	
	private static String ResultRefinery(String result){
		if (result.equals(""))
			return ".*";
		
		return ValueEntryTable.AppendParenthesis(result);
		/*if(result.contains("|"))
			return "(" + result + ")";
		else if (result.equals(""))
			return ".*";
		return result;*/
	}
	
	/*****************************************************/
	/***                   Basic APIs                  ***/
	/*****************************************************/
	private static boolean Initialization(){
		start_handling = System.currentTimeMillis();
		
		// 0. Print Info
		System.out.println("[Heap Handling]");
		System.out.println("App name: " + Constants.apkName + "\n");
		System.out.println("Initializing...");
		
		// 1. Load Back-end output
		reqRespInfoList = BackendOutputHelper.DeserializeBackendOutputs();
		if(reqRespInfoList == null)
			return false;
		
		if(printRawOnly){
			PrintResult(true);
			return false;
		}
		
		// 3. Ready for dependency trackingtant
		HeapHandlingHelper.Initialization(reqRespInfoList);
		
		// 4. Read heap result from file
		General.DeserializeHeapResults();
		
		// 5. Setup the maximum main stream length & maximum depth for the propagation in frontend 
		//PropagateHelper.setMaxMainStream(5);
		//TaintResultEntry.setMaxDepth(5);
		
		// 6. set the corresponding variables for debugging
		//Constants.printResultOfFrontend = debug;
		//Constants.isBackwardDebug = debug;
		//Constants.isForwardDebug = debug;
		
		oldMaxMethodVisitCount = Constants.maxMethodVisitCount;
		Constants.maxMethodVisitCount = newMaxMethodVisitCount;
		
		Backend.setMaxEPInitCurrEPCnt(maxEPCnt); // Backend will process EPs up to maxEPCnt when tracking heap object.
		
		System.out.println("\nStart heap handling!");
		System.out.println("===============================================");
		return true;
	}
	
	/** save the result
	 * 
	 */
	private static void SaveResultAndGenerateSignatures(){
		// 1. Save signatures into file
		//BackendOutputHelper.SerializeBackendOutputs(reqRespInfoList);
		
		// 2. Save dependency graph into file
		HeapHandlingHelper.GenerageSignatures(reqRespInfoList);
	}
	
	/** Print the final result
	 * 
	 */
	private static void PrintResult(boolean printOrig){
		// 1. Print URIs only
		int order = 0;
		for(ReqRespInfo rri : reqRespInfoList){
			if(rri.reqie.getSignature() != null)
				if(printOrig)
					System.out.println((order++) + " - [ID:"+reqRespInfoList.indexOf(rri)+"]: " + rri.reqie.getSignature());
				else
					System.out.println((order++) + " - [ID:"+reqRespInfoList.indexOf(rri)+"]: " + rri.reqie.getRegexSignature());
		}
		System.out.println("-----------------------------------------------------------------------------------------------");
		System.out.println("Total: " + order);
		System.out.println();
			
		// 2. Detail information
		for(Iterator<ReqRespInfo> iter = reqRespInfoList.iterator(); iter.hasNext();){
			ReqRespInfo rri = iter.next(); 
			if(rri.reqie.getSignature() != null)
				rri.reqie.print_info(reqRespInfoList.indexOf(rri), true, Constants.printTaintMethods);
		}
		
		// 2. post processing
		Constants.maxMethodVisitCount = oldMaxMethodVisitCount; 
	}
	
	/** Process argument
	 * 
	 * @param args list of arguments
	 */
	private static void ArgumentProcessing(String[] args){
		int k = 0;
		
		while (k < args.length)
		{
			if (args[k].equalsIgnoreCase("--app"))
			{
				Constants.apkName = args[k + 1];
				k += 2;
			}
			else if (args[k].equalsIgnoreCase("--printRawOnly"))
			{
				printRawOnly = true;
				k ++;
			}else
				k++;
		}
	}
	
	
	/*****************************************************/
	/***        APIs for finding heap in code          ***/
	/*****************************************************/
	private static void SaveHeapResult(String Heap, String res){ General.SaveHeapResult(Heap, res); }
	private static void SaveHeapResult(String Heap, ValueEntryList res){ General.SaveHeapResult(Heap, res); }
	private static ValueEntryList getHeapResult(String heap) { return General.getValueEntryList(heap); }
	private static boolean isHeapAnalyzed(String heap) { return getHeapResult(heap) != null; }
	private static boolean filtering(ReqRespInfo rri){ return rri.reqie.getSignature() == null;	}
	
	private static void PrintElapsedTime(){
		end_handling = System.currentTimeMillis();
		System.out.println("=====================================================================");
		System.out.println("Total elapsed time: " + Backend.getTimeString((end_handling-start_handling)/1000.0) + "\n\n");
	}
	
	
}
