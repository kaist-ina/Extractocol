package extractocol.common.trackers.tools;

import java.util.List;

import extractocol.common.valueEntry.ValueEntryList;

public class HeapToString {
	public static String M(String heap, List<String> heapStack, boolean debug) {
		// 1. HeapToVEL
		ValueEntryList vel = HeapToVEL.M(heap, debug);
		
		// 2. VELToStr
		return VELToString.M(vel, heapStack, debug);
	}
}
