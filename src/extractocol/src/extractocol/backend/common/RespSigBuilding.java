package extractocol.backend.common;

import java.text.DecimalFormat;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import extractocol.Constants;
import extractocol.backend.response.SignatureBuilder_Response;
import extractocol.common.outputs.BackendOutput;

public class RespSigBuilding {
	static DecimalFormat runtimeFormat = new DecimalFormat("##.#");
	
	public static void Main(BackendThread bt) {
		// 1. Pre-Processing
		PreProcessing(bt);
		
		// 2. Main
		M(bt);
		
		// 3. Post-Processing
		PostProcessing(bt);
	}
	
	private static void PreProcessing(BackendThread bt) {
		bt.RRI().respie.setDPMethod(bt.getDPMethod());
		bt.RRI().respie.setDPStmt(bt.getDPStmt());
	}
	
	private static void M(BackendThread bt) {
		long start,end;
		start = System.currentTimeMillis();
		
		if (Constants.sCFG.getMethodOf(bt.getRespEPMethod()) == null) {
			System.out.println("EP not found");
			return;
		} 
		
		SignatureBuilder_Response be = new SignatureBuilder_Response();
		be.ep_methods = new JSONArray();
		be.responseEntry = new JSONObject();
		be.epstmts = new JSONArray();
		
		try {
			be.StartFingerprint(bt);
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		end = System.currentTimeMillis();
		
		bt.setRespBuildTime((int)(end-start)/1000);
	}
	
	private static void PostProcessing(BackendThread bt) {
		bt.setTotalMethodVisitCount(bt.getTotalMethodVisitCount() + bt.getMethodVisitCount());
		if(!bt.isMultiThread()) {
			if(bt.getMethodVisitCount() < Constants.maxRespVisitCnt)
				System.out
					.println("\t\t- [Response Sig. Building] Runtime : " + runtimeFormat.format(bt.getRespBuildTime()) + " seconds (Method Visit Count : " + bt.getMethodVisitCount() + ")");
			else
				System.out
					.println("\t\t- [Response Sig. Building] Runtime : " + runtimeFormat.format(bt.getRespBuildTime()) + " seconds (Method Visit Count : " + bt.getMethodVisitCount() + ")"
						 + " -> Stop analyzing forcibly. Reached to max count (" + Constants.maxRespVisitCnt + ")");
			System.out.println();
		}
	}
}
