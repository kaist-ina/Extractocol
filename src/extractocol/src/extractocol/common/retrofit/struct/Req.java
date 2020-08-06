package extractocol.common.retrofit.struct;

import java.util.ArrayList;
import java.util.List;

import extractocol.common.outputs.backendOutputHelper.RequestInfoEntry;
import extractocol.common.tools.Pair;

public class Req {
	public static enum HTTP_METHOD {GET, POST, PUT, DELETE, PATCH}
	
	HTTP_METHOD httpMethod;
	String baseUrl;
	String subUrl;
	String Url;
	List<Pair> headers;
	List<Pair> body = null;
	boolean FormUrlEncoded = false;
	
	public Req() {
		this.httpMethod = null;
		this.subUrl = null;
		this.baseUrl = null;
		this.headers = null;
	}
	
	public void clear() {
		if(this.headers != null) {
			this.headers.clear();
			this.headers = null;
		}
		
		if(this.body != null) {
			this.body.clear();
			this.body = null;
		}
	}
	
	public HTTP_METHOD getHttpMethod() { return this.httpMethod; }
	public void setHttpMethod(HTTP_METHOD hm) { this.httpMethod = hm; }
	
	public String getSubUrl() { return this.subUrl; }
	public void setSubUrl(String su) { this.subUrl = su;}
	
	public String getBaseUrl() { return this.baseUrl; }
	public void setBaseUrl(String su) { this.baseUrl = su;}
	
	public String getUrl() { return this.Url; }
	public void setUrl(String s) { this.Url = s; }
	
	public List<Pair> getHeaders() { return this.headers; }
	public void addHeader(String k, String v) {
		if(this.headers == null)
			this.headers = new ArrayList<Pair>();
		this.headers.add(new Pair(k, v)); 
	}
	
	public List<Pair> getBody() { return this.body; }
	public void addBody(String k, String v) {
		if(this.body == null)
			this.body = new ArrayList<Pair>();
		
		RequestInfoEntry.addBody(this.body, k, v);
	}
	
	public boolean getFormUrlEncoded() {return this.FormUrlEncoded; }
	public void setFormUrlEncoded(boolean fue) { this.FormUrlEncoded = fue; }

}
