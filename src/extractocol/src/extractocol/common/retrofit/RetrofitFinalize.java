package extractocol.common.retrofit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import extractocol.common.outputs.BackendOutput;
import extractocol.common.outputs.backendOutputHelper.ReqRespInfo;
import extractocol.common.retrofit.struct.Param;
import extractocol.common.retrofit.struct.Transaction;
import extractocol.common.tools.Pair;
import extractocol.common.tools.highScaleLib.NonBlockingHashSet;
import extractocol.common.valueEntry.ValueEntryTable;
import extractocol.common.valueEntry.ValueEntryTracking;

public class RetrofitFinalize {
	/**
	 * Main function of RetrofitFinalizer Called at the end of the Backend
	 */
	public static void Request() {
		System.out.println("RetrofitFinalizer (request) running...");
		// 1. get base URL and set it into Transaction t (already set. see RetrofitAnalyzer.java)
		//setBaseURL();

		// 2. Finalize contents in Transactions
		FinalizeTrans();

		// 3. Copy content of transactions to ReqRespInfoList
		CopyTranContentToReqRespInfo(false);
		
		System.out.println("RetrofitFinalizer (request) done.");
	}
	
	public static void Response() {
		System.out.println("RetrofitFinalizer (response) running...");
		// Merge response heap table of transaction with that of rri 
		CopyTranContentToReqRespInfo(true);
		System.out.println("RetrofitFinalizer (response) done.");
	}
	
	private static void CopyTranContentToReqRespInfo(boolean isResponse) {
		// 1. get transactions from the Transaction map
		Collection<Transaction> tranMapValues = RetrofitHandle.TransactionMap().values();
		NonBlockingHashSet<ReqRespInfo> reqRespInfoList = BackendOutput.getReqRespInfoList();

		for(ReqRespInfo rri: reqRespInfoList) {
			// 1. get the corresponding
			Transaction tran = null;
			for(Transaction t: tranMapValues) {
				if(t.getDPMethod() == null)
					continue;
				
				if (rri.getRequestInfoEntry().getDPMethod().equals(t.getDPMethod())) {
					if(rri.getRequestInfoEntry().getDPStmt().contains(t.getInvokeStatement())){
						tran = t;
						break;	
					}
				}
			}
			
			if(tran == null)
				continue;
			
			if(!isResponse)	{
				// 2-1. handle url
				rri.getRequestInfoEntry().setSignature(tran.Request().getUrl());
				
				// 2-2. handle header
				rri.getRequestInfoEntry().setHeader(tran.Request().getHeaders());
				
				// 2-3. handle body
				rri.getRequestInfoEntry().setBody(tran.Request().getBody());
			}else {
				// 2-4. merge VET of tran with RRIs'
				ValueEntryTable destVET = rri.getHeapTable();
				ValueEntryTable srcVET = tran.Response().VET();
				ValueEntryTracking.MergeTables(destVET, srcVET);
				//rri.setHeapTable(tran.Response().VET());
			}
		}
	}

	private static void FinalizeTrans() {
		// 1. get transactions from the Transaction map
		Collection<Transaction> tranMapValues = RetrofitHandle.TransactionMap().values();

		// 2. handle transactions
		for (Transaction t : tranMapValues) {
			if(t.getDPMethod() == null)
				continue;
			
			// 2-1. handle '@Url' type parameters
			Set<String> fromUrlParam = null;
			urlLoop: for (Param p : t.getParams()) {
				switch (p.getRetrofitType()) {
				// becomes sub-url or whole url (e.g., @Url String paramString1)
				case URL:
					fromUrlParam = p.getResultSet();
					break urlLoop; /* '@url' will be included once in the param list. */
				default:
					break;
				}
			}

			// 2-2. finalize URL
			FinalizeURL(t, fromUrlParam);

			// 2-3. handle other types of parameters
			for (Param p : t.getParams()) {
				switch (p.getRetrofitType()) {
				// replace part of sub-url (e.g., @Path("roomId") UUID paramUUID)
				case PATH:
					HandleParam_PATH(p, t);
					break;

				// add query on sub-url (e.g., @Query("extra") List<String> paramList,
				// @Query("exclude_asap_info") boolean paramBoolean)
				case QUERY:
					HandleParam_QUERY(p, t);
					break;

				// add body parameter (e.g., @Field("duration") long paramLong)
				case FIELD:
					HandleParam_FIELD(p, t);
					break;

				// add query map on sub-url (e.g., @QueryMap Map<String, String> paramMap)
				case QUERY_MAP:
					HandleParam_QUERY_MAP(p, t);
					break;

				case PART: // Currently, we do not support multipart request
				default:
					break;
				}
			}
		}
	}

	private static void FinalizeURL(Transaction t, Set<String> fromUrlParam) {
		String baseUrl = t.Request().getBaseUrl();
		String subUrl;
		String Url = "";

		if (fromUrlParam == null) {
			subUrl = t.Request().getSubUrl();
			t.Request().setUrl(baseUrl + subUrl);
		} else {
			for (String urlFromParam : fromUrlParam) {
				if (urlFromParam.contains("http://"))
					Url = Url + urlFromParam + "|";
				else
					Url = Url + baseUrl + urlFromParam + "|";
			}

			Url = Url.substring(0, Url.length() - 1);
			t.Request().setUrl("(" + Url + ")");
		}
	}

	private static void HandleParam_PATH(Param p, Transaction t) {
		String key = "\\{" + p.getKeyword() + "\\}";
		String replacement = p.getFinalResultFromSet();

		String res = t.Request().getUrl().replaceAll(key, replacement);
		t.Request().setUrl(res);
	}

	private static void HandleParam_QUERY(Param p, Transaction t) {
		String url = t.Request().getUrl();
		String res = p.getFinalResultFromSet();
		if(res == null)
			return;

		if (url.contains("?"))
			url = url + "&";
		else
			url = url + "?";

		url = url + p.getKeyword() + "=" + res;

		t.Request().setUrl(url);
	}

	private static void HandleParam_FIELD(Param p, Transaction t) {
		String res = p.getFinalResultFromSet();
		t.Request().addBody(p.getKeyword(), res);
	}

	private static void HandleParam_QUERY_MAP(Param p, Transaction t) {
		String url = t.Request().getUrl();
		List<Pair> res = p.getResultMap();
		
		if(res == null)
			return;
		
		for (Pair e : res) {
			if (url.contains("?"))
				url = url + "&";
			else
				url = url + "?";

			url = url + e.getKey() + "=" + e.getValue();
		}

		t.Request().setUrl(url);
	}
}
