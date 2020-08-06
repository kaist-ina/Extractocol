package extractocol.common.trackers.tools;

import java.util.List;
import java.util.regex.Matcher;

import extractocol.common.valueEntry.ValueEntryList;
import extractocol.tester.HeapHandling;

public class VELToString {
	public static String M(ValueEntryList vel, List<String> heapStack, boolean debug) {
		return StrWithHeapToStr(vel.GenRegex(), heapStack, debug);
	}
	
	public static String StrWithHeapToStr(String res, List<String> heapStack, boolean debug) {
		String heapRes;
		
		Matcher m = HeapHandling.heapPattern.matcher(res);
		
		while (m.find()){
			// 1. Extract heap
			String heap = m.group();
			
			if(heapStack.contains(heap))
				heapRes = ".*";
			else {
				heapStack.add(heap);
				heapRes = HeapToString.M(heap, heapStack, debug);
				heapStack.remove(heap);
			}
			
			if(HeapHandling.doesContainHeap(heapRes))
				heapRes = StrWithHeapToStr(heapRes, heapStack, debug);
			
			res = res.replaceFirst(HeapHandling.heapP, heapRes);
		}
		
		return res;
	}
}
