package extractocol.backend.common;

import java.text.DecimalFormat;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import extractocol.Constants;
import extractocol.backend.request.basics.ParameterBucket;
import extractocol.backend.request.semantic.json.JSONBuilder;
import extractocol.common.helper.InvokeHelper;

public class ReqSigBuilding {
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
		bt.RRI().setEP(bt.getReqEPMethod());
		bt.RRI().setDP(bt.getDPMethod());
		bt.RRI().setDPStmt(bt.getDPStmt());
	}
	
	private static void M(BackendThread bt) {
		long start, end;
		
		ParameterBucket pb = new ParameterBucket(Constants.sCFG.getMethodOf(bt.getReqEPMethod()));
		pb.ep_methods = new JSONArray();
		pb.requestEntries = new JSONArray();
		pb.requestEntry = new JSONObject();
		pb.epstmts = new JSONArray();
		pb.setBT(bt);
		
		InvokeHelper.InitInvokedClinitMethods(); // Initialize a list of <clinit> methods that are invoked already (To avoid invoking <clinit> more than once)

		// 1) Start backward analysis
		start = System.currentTimeMillis();
		try {
			(new JSONBuilder()).StartAnalyze(pb);
		}catch(Exception e) {
			e.printStackTrace();
		}
		end = System.currentTimeMillis();
		
		bt.setReqBuildTime((int)(end-start)/1000);
	}
	
	private static void PostProcessing(BackendThread bt) {
		bt.setTotalMethodVisitCount(bt.getTotalMethodVisitCount() + bt.getMethodVisitCount());
		if(!bt.isMultiThread()) {
			if (bt.getMethodVisitCount() < Constants.maxMethodVisitCount)
				System.out.println(
						"\t\t- [Request  Sig. Building] Runtime : " + runtimeFormat.format(bt.getReqBuildTime()) + " seconds (Method Visit Count : " + bt.getMethodVisitCount() + ")");
			else
				System.out.println("\t\t- [Request  Sig. Building] Runtime : " + runtimeFormat.format(bt.getReqBuildTime()) + " seconds (Method Visit Count : " + bt.getMethodVisitCount()
						+ ")" + " -> Stop analyzing forcibly. Reached to max count (" + Constants.maxMethodVisitCount + ")");
		}
	}
}
