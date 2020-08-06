package extractocol.common.retrofit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import extractocol.Constants;
import extractocol.backend.common.BackendThread;
import extractocol.common.helper.InvokeHelper;
import extractocol.common.outputs.BackendOutput;
import extractocol.common.outputs.backendOutputHelper.ReqRespInfo;
import extractocol.common.retrofit.struct.Param;
import extractocol.common.retrofit.struct.Req.HTTP_METHOD;
import extractocol.common.retrofit.struct.Transaction;
import extractocol.common.tools.Pair;
import extractocol.common.valueEntry.ValueEntryTracking;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;

public class RetrofitHandle {
	private static Map<String, Transaction> tranMap = null;
	private static String RetrofitSignature = "##Retrofit##";
	
	public static Map<String, Transaction> TransactionMap() {return tranMap; }
	public static void TransactionMapLoad(){ tranMap = Transaction.Deserialize(); }
	public static Transaction getTransaction(String sootMethod) { return tranMap.get(sootMethod); }
	public static String getRetrofitSignature() { return RetrofitSignature; }
	
	public static boolean M(Unit ut, ValueEntryTracking vet, String DPmethod, BackendThread bt) {
		if(!Constants.getIsRetrofit() || tranMap == null)
			return false;
		
		// 1. Check whether ut is instance of InvokeStmt
		InvokeExpr ie = InvokeHelper.getInvokeExpr(ut);
		if(ie == null)
			return false;
		
		// 2. Check whether sootMethod is one of retrofit DPs
		Transaction t = getTransaction(ie.getMethod().toString());
		if(t == null || !bt.getDPStmt().contains(t.getInvokeStatement())) 
			return false;
		
		// 3. Get arg list
		List<String> argList = getArgList(ie);
		
		// 4. Handle request & Set
		RequestHandler(t, argList, vet, DPmethod, bt);
		
		// 5. Handle response
		ResponseHandler(t, vet);
		
		return true;
	}
	
	private static void ResponseHandler(Transaction t, ValueEntryTracking vet) {
		//if(t.Response().getIsRespSigBuildingRequired())
			ValueEntryTracking.MergeTables(vet.heapTable, t.Response().VET());
	}
	
	public static void RequestHandler(Transaction t, List<String> argList, ValueEntryTracking vet, String DPmethod, BackendThread bt) {
		// 1. URL, Body handling
		Handle_URL_Body(t, argList, vet);
		
		// 2. Header handling
		Handle_Header(t, bt);
		
		// 3. HTTP method & DP handling
		Handle_Others(t, DPmethod, bt);
	}
	
	private static void Handle_Others(Transaction t, String DPmethod, BackendThread bt) {
		// 1. Set HTTP method
		bt.RRI().AddHTTPMethod(t.Request().getHttpMethod().toString());
		
		// 2. Set DP method
		t.setDPMethod(DPmethod);
		
		// 3. Set retrofit signature
		bt.RRI().SaveURI(getRetrofitSignature());
		
		// 4. set whether RSB (Response sig. building) is required
		bt.RRI().setIsRSBRequired(t.Response().getIsRespSigBuildingRequired());
	}
	
	private static void Handle_Header(Transaction t, BackendThread bt) {
		// Do I have to clone? (I don't think so.) by BK
		bt.RRI().SetRequestHeader(t.Request().getHeaders());
	}
	
	private static void Handle_URL_Body(Transaction t, List<String> argList, ValueEntryTracking vet) {
		List<Param> retrofitParamList = t.getParams();
		// 1. Handle parameters
		for(int i = 0; i < retrofitParamList.size(); i++) {
			String arg = argList.get(i);
			Param p = retrofitParamList.get(i);
			
			if(arg.equals("null")) continue;
			
			switch(p.getRetrofitType()) {
			// replace part of sub-url (e.g., @Path("roomId") UUID paramUUID)
			case PATH: HandleParam_PATH(p, t, arg, vet); break;
			
			// becomes sub-url or whole url (e.g., @Url String paramString1)
			case URL: HandleParam_URL(p, t, arg, vet); break;
			
			// add query on sub-url (e.g., @Query("extra") List<String> paramList, @Query("exclude_asap_info") boolean paramBoolean)
			case QUERY: HandleParam_QUERY(p, t, arg, vet); break;
				
			// add body parameter (e.g., @Field("duration") long paramLong)
			case FIELD: HandleParam_FIELD(p, arg, vet); break;
				
			// add query map on sub-url (e.g., @QueryMap Map<String, String> paramMap)
			case QUERY_MAP: HandleParam_QUERY_MAP(p, t, arg, vet); break;
				
			// add body parameter (e.g., @Body Map paramMap, @Body List<OrderOption> paramList, @Body JsonNode paramJsonNode)
			case BODY: /*need to support Gson. Future work*/ break;
			
			default: break;
			}
		}
		
	}
	
	private static void HandleParam_PATH(Param p, Transaction t, String arg, ValueEntryTracking vet) {
		//String key = "\\{" + p.getKeyword() + "\\}";
		String replacement = getReplacement(arg, vet, true);
		
		p.lock();
		p.addResult(replacement);
		p.unlock();
	}
	
	private static void HandleParam_URL(Param p, Transaction t, String arg, ValueEntryTracking vet) {
		String replacement = getReplacement(arg, vet, false);
		
		p.lock();
		p.addResult(replacement);
		p.unlock();
	}
	
	private static void HandleParam_QUERY(Param p, Transaction t, String arg, ValueEntryTracking vet) {
		List<String> list = null;
		
		if(p.getType().equals("java.util.List")) {
			list = vet.varTable.getList(arg);
		}else {
			list = new ArrayList<String>();
			list.add(getReplacement(arg, vet, false));
		}
		
		p.lock();
		for(String e: list)
			p.addResult(e);
		p.unlock();
	}
	
	private static void HandleParam_FIELD(Param p, String arg, ValueEntryTracking vet) {
		String argString = getReplacement(arg, vet, false);
		
		p.lock();
		p.addResult(argString);
		p.unlock();
	}
	
	private static void HandleParam_QUERY_MAP(Param p, Transaction t, String arg, ValueEntryTracking vet) {
		List<Pair> map = vet.varTable.getMap(arg);
		
		p.lock();
		for(Pair e: map)
			p.addResult(e.getKey(), e.getValue());
		p.unlock();
	}
	
	private static void HandleParam_BODY(Param p, String arg, ValueEntryTracking vet) {
		// Not implemented
		// In retrofit, '@Body' type means JSON body. (basically using GSON)
		// The current version of Extractocol does not support JSON body of POST request.
		// We currently do not consider this case. 
	}
	
	public static void ResponseHandler() {
		
	}
	
	/***********/
	/** Tools **/
	/***********/
	
	private static String getReplacement(String arg, ValueEntryTracking vet, boolean needBackSlush) {
		String replacement = vet.varTable.GenRegex(arg);
		if(needBackSlush)
			return replacement.replace("$", "\\$");
		else
			return replacement;
	}
	
	private static List<String> getArgList(InvokeExpr ie){
		List<String> argList = new ArrayList<String>();
		
		for(Value v: ie.getArgs())
			argList.add(v.toString());
		
		return argList;
	}
}
