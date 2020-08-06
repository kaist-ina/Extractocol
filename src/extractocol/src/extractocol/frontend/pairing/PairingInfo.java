package extractocol.frontend.pairing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PairingInfo {
	// Demarcation point statement
	private String dp_stmt;
	// Demarcation point method
	private String dp_method;
	
	// Request signature
	List<RequestInfo> requestInfos = new ArrayList<RequestInfo>();
	// Response signature
	List<ResponseInfo> responseInfos = new ArrayList<ResponseInfo>();;
	
	public PairingInfo(String stmt, String method) {
		this.dp_stmt = stmt;
		this.dp_method = method;
	}
	
	public String getDP_Stmt() {
		return this.dp_stmt;
	}
	
	public String getDP_Method() {
		return this.dp_method;
	}
	
	public List<RequestInfo> getRequestInfos() {
		return this.requestInfos;
	}
	
	public List<ResponseInfo> getResponseInfos() {
		return this.responseInfos;
	}
	
	public void setURISignature(String UriSig, String ep_stmt, String ep_method) {
		boolean hasSig = false;
		for (RequestInfo requestInfo : requestInfos) {
			if (requestInfo.getUriSig().equals(UriSig)) {
				hasSig = true;
				requestInfo.setEP_Stmt(ep_stmt);
			}
		}
		
		if (!hasSig) {
			RequestInfo newRequestInfo = new RequestInfo(UriSig, ep_method, ep_stmt);
			requestInfos.add(newRequestInfo);
		}
	}
	
	public void setBODYSignature(String BodySig, String ep_stmt, String ep_method) {
		boolean hasBody = false;
		for (ResponseInfo responseInfo : responseInfos) {
			if (responseInfo.getBodySig() != null && 
					responseInfo.getBodySig().equals(BodySig)) {
				responseInfo.setEP_Stmt(ep_stmt);
				hasBody = true;
			}
		}
		
		if (!hasBody) {
			ResponseInfo newReponseInfo = new ResponseInfo(BodySig, ep_method, ep_stmt);
			responseInfos.add(newReponseInfo);
		}
	}
	
	/*
	 *  Response signature class
	 */
	public class RequestInfo {
		// URI signature
		String uriSig;
		// Entry point method
		String ep_method;
		// Entry point stmt
		Set<String> ep_stmt = new HashSet<String>();
		
		public RequestInfo(String sig, String ep_m, String ep_s) {
			this.uriSig = sig;
			this.ep_method = ep_m;
			this.ep_stmt.add(ep_s);
		}

		public String getUriSig() {
			return this.uriSig;
		}
		
		public String getEP_Method() {
			return this.ep_method;
		}
		
		public Set<String> getEP_Stmt() {
			return this.ep_stmt;
		}
		
		public void setEP_Stmt(String ep_stmt) {
			this.ep_stmt.add(ep_stmt);
		}
	}
	
	/*
	 *  Request signature class
	 */
	public class ResponseInfo {
		// BODY signature
		String bodySig = null;
		// End point method
		String ep_method = null;
		// End point stmt
		Set<String> ep_stmt = new HashSet<String>();
		
		public ResponseInfo(String sig, String ep_m, String ep_s) {
			this.bodySig = sig;
			this.ep_method = ep_m;
			this.ep_stmt.add(ep_s);
		}

		public String getBodySig() {
			return this.bodySig;
		}
		
		public String getEP_Method() {
			return this.ep_method;
		}
		
		public Set<String> getEP_Stmt() {
			return this.ep_stmt;
		}
		
		public void setEP_Stmt(String ep_stmt) {
			this.ep_stmt.add(ep_stmt);
		}
	}
}
