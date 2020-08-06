package extractocol.backend.response.helper;

import extractocol.Constants;
import extractocol.backend.response.basics.ParameterBucket;
import soot.Unit;

public class DebugHelper {
	
	//public static int depthCount = 0;

	/*public static void printSpace() {
		for(int i=0; i<=depthCount; i++) {
			System.out.print("    ");
		}		
	}*/

	/*public static void printSM(ParameterBucket pb) {
		if(Constants.isDebug == false)
			return;
		DebugHelper.printSpace();
		System.out.println("[*** " + pb.sm.toString() + " ***]");
	}

	public static void printUnit(Unit ut) {
		if(Constants.isDebug == false)
			return;
		DebugHelper.printSpace();
		System.out.println(ut);
	}

	public static void printBFTTable(ParameterBucket pb) {
		if(Constants.isDebug == false)
			return;
		System.out.println("------------" + pb.sm.toString() + "------------");
		System.out.println("PRINT BFT");
		BFTTableHelper.printBFTTable(pb);
		System.out.println("PRINT BASE STACK");
		System.out.println(TaintHelper.GeneratedStringStack_BaseTaint.getLast());
	}

	public static void printBlockNumber(int blockIndex) {
		if(Constants.isDebug == false)
			return;
		System.out.println();
		DebugHelper.printSpace();
		System.out.println("---------Block Number " + blockIndex + "---------");
	}*/
}
