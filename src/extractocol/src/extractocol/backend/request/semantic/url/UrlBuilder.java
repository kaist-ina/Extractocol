
package extractocol.backend.request.semantic.url;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.gaurav.tree.NodeNotFoundException;

import extractocol.Constants;
import extractocol.backend.request.SignatureBuilder_Request;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.basics.ParameterBucket;
import extractocol.backend.request.heapManagement.HeapHandler;
import extractocol.backend.request.semantic.retrofit.retrofit_http_old;
import extractocol.backend.request.semantic.url.models.AbstractDecorator;
import extractocol.backend.request.semantic.url.models.DebugDecorator;
import extractocol.backend.request.semantic.url.models.PairingDecorator;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.backend.request.semantic.url.models.normalModels.*;
import extractocol.backend.request.semantic.url.models.staticModels.addqueryparameters;
import extractocol.backend.request.semantic.url.models.staticModels.retrieveStream;
import extractocol.backend.request.semantic.url.models.staticModels.valueOf;
import extractocol.common.outputs.BackendOutput;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.StaticInvokeExpr;
import soot.toolkits.graph.Block;

public class UrlBuilder extends SignatureBuilder_Request {
	public String TrackingReg = "";
	public String strDest;
	public boolean isResource;
	private Set<String> printVisitSet;
	public boolean isGet;
	public int Volleymethodint = -1;
	public ParameterBucket CurrentPb;
	public List<String> Rx5 = Arrays.asList(
			"<com.thirdrock.repository.impl.AddressRepositoryImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
			"<com.thirdrock.repository.impl.FriendsRepositoryImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
			"<com.thirdrock.repository.impl.GoogleMapsApiImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
			"<com.thirdrock.repository.impl.ItemRepositoryImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
			"<com.thirdrock.repository.impl.MessageRepositoryImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
			"<com.thirdrock.repository.impl.OfferRepositoryImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
			"<com.thirdrock.repository.impl.ReviewRepositoryImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
			"<com.thirdrock.repository.impl.SystemRepositoryImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
			"<com.thirdrock.repository.impl.UserRepositoryImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
			"<com.thirdrock.framework.rest.AbsRestClient: rx.a post(java.lang.String,java.util.Map,com.thirdrock.framework.rest.HttpBodyParser)>",
			"<com.thirdrock.repository.impl.ItemRepositoryImpl: rx.a get(java.lang.String,java.util.Map,com.thirdrock.framework.rest.HttpBodyParser)>");
	
	public enum MethodNames {
		add, addall, addheader,	addpart, addqueryparameters, ajax, append, appendencodedpath, appendpath, appendqueryparameter,
		build, buildupon, create, encode, entryset, execute, format, fromjson, get, getactivenetworkinfo, getcontent, gethost,
		getinputstream, getjsonfrompost, getjsonfromurl, getscheme, getstring, geturi, group, init,	iterator, matcher, next,
		onpostexecute, open, openconnection, openstream, parse, path, post, put, replace, replaceall, replacefirst, retrievestream,
		retrievetoken, serverequest, setentity, setheader, setrequestmethod, split, substring, toentity, tolowercase, tostring,
		tourl, trim, url, valueof, write, getvalue, getkey, keyset, constructcollectiontype, convertvalue, frombody
		
		}

	
	/*public class MethodIds {
		public static final int init = 0;
		public static final int append = 1;
		public static final int setEntity = 2;
		public static final int setHeader = 3;
		public static final int getJSONFromPost = 4;
		public static final int getJSONFromUrl = 5;
		public static final int toString = 6;
		public static final int execute = 7;
		public static final int openStream = 8;
		public static final int format = 9;
		public static final int add = 10;
		public static final int put = 11;
		public static final int getActiveNetworkInfo = 12;
		public static final int fromJson = 13;
		public static final int retrieveStream = 14;
		public static final int onPostExecute = 15;
		public static final int getString = 16;
		public static final int parse = 17;
		public static final int appendQueryParameter = 18;
		public static final int addPart = 19;
		public static final int setRequestMethod = 20;
		public static final int write = 21;
		// Testing
		public static final int serveRequest = 22;
		public static final int retrieveToken = 23;
		public static final int openConnection = 24;
		public static final int open = 25;
		public static final int replace = 26;
		public static final int toLowerCase = 27;
		public static final int replaceFirst = 28;
		public static final int entrySet = 29;
		public static final int iterator = 30;
		public static final int next = 31;
		public static final int addAll = 32;
		public static final int trim = 33;
		public static final int getScheme = 34;
		public static final int getHost = 35;
		public static final int getInputStream = 36;
		public static final int buildUpon = 37;
		public static final int build = 38;
		public static final int appendEncodedPath = 39;
		public static final int appendPath = 40;
		public static final int valueOf = 41;
		public static final int split = 42;
		public static final int replaceAll = 43;
		public static final int ajax = 44;
		public static final int path = 45;
		// okhttp
		public static final int url = 46;
		public static final int post = 47;
		public static final int encode = 48;
		// hulu
		public static final int get = 49;
		public static final int addQueryParameters = 50;
		public static final int toURL = 51;
		public static final int getContent = 52;
		public static final int create = 53;
		public static final int getURI = 54;
		public static final int matcher = 55;
		public static final int group = 56;

		public static final int substring = 57;
		public static final int toEntity = 58;
		public static final int addHeader = 59;
	}*/

	public class Vtypes {
		public static final int vint = 0;
		public static final int vstring = 1;
		public static final int vjsonarray = 2;
		public static final int vconst = 3;
	}
	
	public static void initSemanticModels() {
		methodlist = Arrays.asList("<init>", "append", "setEntity", "setHeader", "getJSONFromPost", "getJSONFromUrl",
				"toString", "execute", "openStream", "format", "add", "put", "getActiveNetworkInfo", "fromJson",
				"retrieveStream", "onPostExecute", "getString", "parse", "appendQueryParameter", "addPart",
				"setRequestMethod", "write", "serveRequest", "retrieveToken", "openConnection", "open", "replace",
				"toLowerCase", "replaceFirst", "entrySet", "iterator", "next", "addAll", "trim", "getScheme", "getHost",
				"getInputStream", "buildUpon", "build", "appendEncodedPath", "appendPath", "valueOf", "split",
				"replaceAll", "ajax", "path", "url", "post", "encode", "get", "addQueryParameters", "toURL",
				"getContent", "create", "getURI", "matcher", "group", "substring", "toEntity", "addHeader", "getString",
				"constructCollectionType", "convertValue", "fromBody");

		SemanticModel = Arrays.asList("<android.content.res.AssetManager: java.io.InputStream open(java.lang.String)>",
				"<java.net.URL: void <init>(java.lang.String)>",
				"<org.apache.http.client.methods.HttpGet: void <init>(java.lang.String)>",
				"<java.io.InputStream retrieveStream(java.lang.String)>",
				"<java.lang.String: java.lang.String toLowerCase(java.util.Locale)>",
				"<java.io.OutputStreamWriter: void write(java.lang.String)>",
				"<org.apache.http.impl.client.AbstractHttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>",
				"<org.apache.http.client.HttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>",
				"<org.apache.http.impl.client.DefaultHttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>",
				"<org.apache.http.client.utils.URLEncodedUtils: java.lang.String format(java.util.List,java.lang.String)>",
				"<java.lang.String: java.lang.String format(java.lang.String,java.lang.Object[])>",
				"<com.squareup.okhttp.OkHttpClient: java.net.HttpURLConnection open(java.net.URL)>",
				"<java.lang.String: java.lang.String replaceFirst(java.lang.String,java.lang.String)>",
				"<com.android.volley.RequestQueue: com.android.volley.Request add(com.android.volley.Request)>",
				"<com.squareup.okhttp.OkHttpClient: java.net.HttpURLConnection open(java.net.URL)>",
				"<android.net.Uri: android.net.Uri parse(java.lang.String)>",
				"<com.github.kevinsawicki.http.HttpRequest: com.github.kevinsawicki.http.HttpRequest get(java.lang.CharSequence)>",
				"<com.github.kevinsawicki.http.HttpRequest: com.github.kevinsawicki.http.HttpRequest post(java.lang.CharSequence)>",
				"<android.content.res.AssetManager: java.io.InputStream open(java.lang.String)>",
				"<java.net.HttpURLConnection: void setRequestMethod(java.lang.String)>",
				"<android.net.Uri$Builder: android.net.Uri$Builder appendQueryParameter(java.lang.String,java.lang.String)>",
				"<android.net.Uri: android.net.Uri parse(java.lang.String)>",
				"<org.json.JSONObject: org.json.JSONObject put(java.lang.String,java.lang.Object)>",
				"<org.json.JSONObject: org.json.JSONObject put(java.lang.String,int)>",
				"<org.json.JSONArray: org.json.JSONArray put(java.lang.Object)>",
				"<java.util.List: boolean add(java.lang.Object)>",
				"<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>",
				"<java.lang.StringBuffer: java.lang.StringBuffer append(java.lang.String)>",
				"<java.lang.StringBuilder: java.lang.StringBuilder append(int)>",
				"<java.lang.StringBuilder: java.lang.StringBuilder append(double)>",
				"<java.lang.StringBuilder: java.lang.StringBuilder append(int)>",
				"<org.apache.http.client.methods.HttpGet: void <init>(java.lang.String)>",
				"<org.apache.http.client.methods.HttpPost: void <init>(java.lang.String)>",
				"<org.apache.http.message.BasicNameValuePair: void <init>(java.lang.String,java.lang.String)>",
				"<org.apache.http.client.entity.UrlEncodedFormEntity: void <init>(java.util.List)>",
				"<java.lang.StringBuilder: void <init>()>", "<java.lang.StringBuilder: void <init>(java.lang.String)>",
				"<android.content.Intent: void <init>(java.lang.String,android.net.Uri)>",
				"<java.lang.String: java.lang.String format(java.lang.String,java.lang.Object[])>",
				"<java.util.concurrent.ConcurrentHashMap: java.lang.Object put(java.lang.Object,java.lang.Object)>",
				"<java.util.concurrent.ConcurrentHashMap: java.util.Set entrySet()>",
				"<java.util.Set: java.util.Iterator iterator()>", "<java.util.Iterator: java.lang.Object next()>",
				"<java.util.List: boolean addAll(java.util.Collection)>",
				"<java.lang.String: java.lang.String replace(java.lang.CharSequence,java.lang.CharSequence)",
				"<java.lang.String: java.lang.String trim()>", "<java.lang.StringBuilder: java.lang.String toString()>",
				"<java.net.URI: void <init>(java.lang.String)>", "<java.net.URI: java.lang.String getScheme()>",
				"<java.net.URI: java.lang.String getHost()>",
				"<com.squareup.okhttp.OkHttpClient: java.net.HttpURLConnection open(java.net.URL)>",
				"<java.net.HttpURLConnection: java.io.InputStream getInputStream()>",
				"<android.net.Uri: android.net.Uri$Builder buildUpon()>",
				"<android.net.Uri$Builder: android.net.Uri build()>",
				"<android.net.Uri$Builder: android.net.Uri$Builder appendEncodedPath(java.lang.String)>",
				"<android.net.Uri$Builder: android.net.Uri$Builder appendPath(java.lang.String)>",
				"<java.util.Map: java.lang.Object put(java.lang.Object,java.lang.Object)>",
				"<java.lang.Character: java.lang.Character valueOf(char)>",
				"<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.Object)>",
				"<java.lang.String: java.lang.String valueOf(java.lang.Object)>",
				"<java.lang.String: java.lang.String replaceAll(java.lang.String,java.lang.String)>",
				"<com.external.androidquery.a: com.external.androidquery.b ajax(com.external.androidquery.b.d)>",
				"<ch.boye.httpclientandroidlib.client.methods.HttpPost: void <init>(java.lang.String)>",
				"<ch.boye.httpclientandroidlib.client.HttpClient: ch.boye.httpclientandroidlib.HttpResponse execute(ch.boye.httpclientandroidlib.client.methods.HttpUriRequest,ch.boye.httpclientandroidlib.protocol.HttpContext)>",
				"<java.util.HashMap: java.lang.Object put(java.lang.Object,java.lang.Object)>",
				"<android.net.Uri$Builder: android.net.Uri$Builder path(java.lang.String)>",
				"<org.apache.http.client.HttpClient: java.lang.Object execute(org.apache.http.client.methods.HttpUriRequest,org.apache.http.client.ResponseHandler)>",
				"<android.net.Uri: java.lang.String toString()>",
				"<org.apache.http.client.methods.HttpPut: void <init>(java.lang.String)>",
				"<com.squareup.okhttp.Request$Builder: com.squareup.okhttp.Request$Builder url(java.lang.String)>",
				"<com.google.gson.Gson: java.lang.String toJson(java.lang.Object)>",
				"<com.squareup.okhttp.Request$Builder: com.squareup.okhttp.Request$Builder post(com.squareup.okhttp.RequestBody)>",
				"<java.net.URLEncoder: java.lang.String encode(java.lang.String,java.lang.String)>",
				"<java.lang.String: java.lang.String valueOf(double)>",
				"<java.lang.StringBuilder: java.lang.StringBuilder append(char)>",
				"<com.squareup.okhttp.Request$Builder: com.squareup.okhttp.Request build()>",
				"<java.lang.Long: java.lang.String toString(long)>",
				"<com.android.volley.Request: void <init>(int,java.lang.String,com.android.volley.Response$ErrorListener)>",
				"<com.android.volley.toolbox.StringRequest: void <init>(java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener,com.android.volley.Response$HeaderListener)>",
				"<java.lang.String: java.lang.String replace(java.lang.CharSequence,java.lang.CharSequence)>",
				"<com.android.volley.toolbox.StringRequest: void <init>(int,java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener,com.android.volley.Response$HeaderListener)>",
				"<com.android.volley.toolbox.ImageRequest: void <init>(java.lang.String,com.android.volley.Response$Listener,int,int,android.graphics.Bitmap$Config,com.android.volley.Response$ErrorListener)>",
				"<java.util.Map: java.lang.Object get(java.lang.Object)>",
				"<com.android.volley.toolbox.StringRequest: void <init>(int,java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener>",
				"<com.android.volley.toolbox.JsonObjectRequest: void <init>(int,java.lang.String,org.json.JSONObject,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener)>",
				"<com.android.volley.toolbox.StringRequest: void <init>(java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener>",
				"<com.android.volley.toolbox.JsonObjectRequest: void <init>(java.lang.String,org.json.JSONObject,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener)>",
				"<com.android.volley.toolbox.JsonRequest: void <init>(java.lang.String,java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener)>",
				"<com.android.volley.toolbox.JsonRequest: void <init>(int,java.lang.String,java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener)>",
				"<roboguice.inject.Lazy: java.lang.Object get()>",
				"<roboguice.util.Strings: boolean equalsIgnoreCase(java.lang.Object,java.lang.Object)>",
				"<roboguice.util.Ln: int d(java.lang.Object,java.lang.Object[])>",
				"<roboguice.util.Strings: boolean notEmpty(java.lang.Object)>",
				"<roboguice.util.Strings: java.lang.String toString(java.io.InputStream)>",
				"<roboguice.util.Strings: java.lang.String md5(java.lang.String)>",
				"<roboguice.util.Strings: java.lang.String toString(java.lang.Object)>",
				"<roboguice.util.Strings: boolean equals(java.lang.Object,java.lang.Object)>",
				"<roboguice.util.Strings: boolean isEmpty(java.lang.Object)>",
				"<oauth.signpost.OAuth: java.lang.String addQueryParameters(java.lang.String,java.lang.String[])>",
				"<oauth.signpost.OAuth: java.lang.String addQueryParameters(java.lang.String,java.util.Map)>",
				"<java.net.URI: java.net.URL toURL()>", "<java.net.URL: java.net.URLConnection openConnection()>",
				"<org.apache.http.client.HttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest,org.apache.http.protocol.HttpContext)>",
				"<java.net.URI: java.net.URI create(java.lang.String)>",
				"<java.net.URL: java.net.URLConnection openConnection(java.net.Proxy)>",
				"<com.linkedin.android.client.MobileClient: java.net.URI getURI(java.lang.String)>",
				"<java.lang.StringBuffer: java.lang.String toString()>",
				"<java.lang.Integer: java.lang.String toString(int)>",
				// 5miles
				"<com.thirdrock.repository.impl.AddressRepositoryImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
				"<com.thirdrock.repository.impl.FriendsRepositoryImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
				"<com.thirdrock.repository.impl.GoogleMapsApiImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
				"<com.thirdrock.repository.impl.ItemRepositoryImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
				"<com.thirdrock.repository.impl.MessageRepositoryImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
				"<com.thirdrock.repository.impl.OfferRepositoryImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
				"<com.thirdrock.repository.impl.ReviewRepositoryImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
				"<com.thirdrock.repository.impl.SystemRepositoryImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
				"<com.thirdrock.repository.impl.UserRepositoryImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
				"<com.thirdrock.repository.impl.ItemRepositoryImpl: rx.a get(java.lang.String,java.util.Map,com.thirdrock.framework.rest.HttpBodyParser)>",
				"<java.lang.String: java.lang.String format(java.util.Locale,java.lang.String,java.lang.Object[])>",
				"<android.net.http.AndroidHttpClient: java.lang.Object execute(org.apache.http.client.methods.HttpUriRequest,org.apache.http.client.ResponseHandler)>",
				"<org.json.JSONObject: java.lang.String toString()>",
				"<org.apache.http.entity.StringEntity: void <init>(java.lang.String,java.lang.String)>",
				"<org.apache.http.client.methods.HttpPost: void setEntity(org.apache.http.HttpEntity)>",
				// ,
				// "Pinterest Case Cover!!" ,
				"<com.pinterest.api.RequestParams: java.util.List b(java.lang.String,java.lang.Object)>",
				"<java.lang.String: java.lang.String valueOf(int)>",
				"<com.pinterest.e.c.d: java.lang.String a(java.lang.String,java.lang.String)>",

				// regex
				"<java.util.regex.Pattern: java.util.regex.Matcher matcher(java.lang.CharSequence)>",
				"<java.util.regex.Matcher: java.lang.String group(int)>",

				// string operations
				"<java.lang.String: java.lang.String substring(int)>",
				"<java.lang.String: java.lang.String substring(int,int)>",

				/////////////////////////////////////// header related
				/////////////////////////////////////// signatures/////////////////////////////////////
				"<ch.boye.httpclientandroidlib.client.methods.HttpRequestBase: void setHeader(java.lang.String,java.lang.String)>",
				//"<com.contextlogic.wish.http.HttpRequestParams: ch.boye.httpclientandroidlib.HttpEntity toEntity()>",
				"<ch.boye.httpclientandroidlib.HttpResponse: void addHeader(ch.boye.httpclientandroidlib.Header)>",
				
				// iteration
				"<java.util.Map$Entry: java.lang.Object getValue()>",
				"<java.util.Map$Entry: java.lang.Object getKey()>",
				"<ch.boye.httpclientandroidlib.message.BasicNameValuePair: void <init>(java.lang.String,java.lang.String)>",
				"<ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity: void <init>(java.util.List,java.lang.String)>",
				"<android.os.Bundle: java.util.Set keySet()>",
				"<android.os.Bundle: java.lang.Object get(java.lang.String)>",

				//Add Jeongmin
				"<org.apache.http.impl.client.DefaultHttpClient: org.apache.http.HttpResponse execute(org.apache.http.HttpHost,org.apache.http.HttpRequest)>",
				
				// body
				"<ch.boye.httpclientandroidlib.client.methods.HttpPost: void setEntity(ch.boye.httpclientandroidlib.HttpEntity)>",
				"<java.util.ArrayList: boolean add(java.lang.Object)>",
				"<org.apache.http.client.entity.UrlEncodedFormEntity: void <init>(java.util.List,java.lang.String)>",
				// for intent hanlding
				"<android.os.Bundle: java.lang.String getString(java.lang.String)>",
				
				// for handling sharedPreference
				"<android.content.SharedPreferences: java.lang.String getString(java.lang.String,java.lang.String)>",
				
				// for handling Jackson
				"<com.fasterxml.jackson.databind.type.TypeFactory: com.fasterxml.jackson.databind.type.CollectionType constructCollectionType(java.lang.Class,java.lang.Class)>",
				"<com.fasterxml.jackson.databind.ObjectMapper: java.lang.Object convertValue(java.lang.Object,com.fasterxml.jackson.databind.JavaType)>",
				"<com.fasterxml.jackson.databind.ObjectMapper: java.lang.Object convertValue(java.lang.Object,java.lang.Class)>",
				
				"<java.lang.Integer: java.lang.Integer valueOf(int)>",
				"<retrofit.converter.Converter: java.lang.Object fromBody(retrofit.mime.TypedInput,java.lang.reflect.Type>",
				"<java.lang.String: java.lang.String valueOf(long)>",
				"<java.lang.Boolean: java.lang.Boolean valueOf(boolean)>"


		/////////////////////////////////////////////////////////////////////////////////////////////////////

		// Wish temporarily
		// "<com.contextlogic.wish.http.WishHttpClient: void
		// post(com.contextlogic.wish.http.WishHttpClient$RequestPool,java.lang.Object,java.lang.String,com.contextlogic.wish.http.HttpRequestParams,com.contextlogic.wish.http.HttpResponseHandler)>"
		);
	}

	public void parseUrl(ParameterBucket pb) {
		// It provide SemanticDecorator with access permission of pb
		CurrentPb = pb;

		/*List<String> retrofitList = new ArrayList<String>();
		for (int i = 0; i < Constants.retrofits.size(); i++) {
			retrofitList.add(Constants.retrofits.get(i).methodref);
			retrofit_http_old retrofits = Constants.retrofits.get(i);
			if (!retrofits.methodref.contains("Observable"))
				Constants.retrofitDP.add(retrofits.methodref);
		}
		for (int i = 0; i < Constants.retrofit_ignorelist.size(); i++)
			retrofitList.add(Constants.retrofit_ignorelist.get(i));
		for (int i = 0; i < Constants.RxJavaDP.length; i++)
			retrofitList.add(Constants.RxJavaDP[i]);
		for (int i = 0; i < SemanticModel.size(); i++)
			retrofitList.add(SemanticModel.get(i));
		SemanticModel = retrofitList;*/
		
		try {
			pb.BFTtable = new HashMap<String, ArrayList<BFNode>>();
			CreateBlockGraphAndInitEdgeTables(pb);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String alignHttpString(String httpurl) {
		if (httpurl.indexOf("/") != -1) {
			httpurl = httpurl.replaceAll("\\/", "\\/");
		}
		if (httpurl.indexOf("?") != -1) {
			httpurl = httpurl.replaceAll("\\?", "\\?");
		}
		return httpurl;
	}

	@Override
	public boolean isContainDP(Block block) {
		for (Iterator<Unit> iter = block.iterator(); iter.hasNext();) {
			Unit ut = iter.next();
			InvokeExpr invokeExpr = null;
			if (ut instanceof AssignStmt) {
				AssignStmt _Assignstmt = (AssignStmt) ut;
				if (_Assignstmt.containsInvokeExpr()) {
					invokeExpr = _Assignstmt.getInvokeExpr();
				}
			} else if (ut instanceof InvokeStmt) {
				invokeExpr = ((InvokeStmt) ut).getInvokeExpr();
			}
			if (invokeExpr != null) {
				String strMethod = invokeExpr.getMethodRef().name().toLowerCase().replaceAll("<|>", "");
				//int chkMethod = methodlist.indexOf(strMethod);
				try{
					switch (MethodNames.valueOf(strMethod)) {
					case getjsonfrompost:
					case getjsonfromurl:
					case fromjson:
					case execute:
					case openstream:
						return true;
					case open:
						if (invokeExpr.getMethodRef().toString()
								.equals("<android.content.res.AssetManager: java.io.InputStream open(java.lang.String)>"))
							return true;
						break;
					case init:
						if (invokeExpr.getMethodRef().toString().equals("<java.net.URL: void <init>(java.lang.String)>"))
							return true;
						if (invokeExpr.getMethodRef().toString()
								.equals("<org.apache.http.client.methods.HttpGet: void <init>(java.lang.String)>"))
							return true;
						break;
					case tolowercase:
						if (invokeExpr.getMethodRef().toString()
								.indexOf("<java.io.InputStream retrieveStream(java.lang.String)>") != -1)
							return true;
						break;
					default:
						break;
					}
				}catch (Exception e){
					;
				}
			}
		}
		return false;
	}

	public void printUrl(ParameterBucket pb, HashMap<String, ArrayList<BFNode>> BFTtable, SootMethod sm, Unit ut) {
		String url = GenRegex(null, BFTtable, TrackingReg);
		Constants.VolleyUrlCandidate = "";
		if (url.equals("") || url.equals("(.*)")) {
			return;
		}
		if (!TrackingReg.equals("Volley_result"))
			url = (isGet ? "GET " : "POST ") + url;

		if (sm.toString().equals(pb.BT().getDPMethod())
				&& ut.toString().equals(pb.BT().getDPStmt())) {
			// System.out.println();
			// System.out.println(removeBlankwithoutHeapObject(url));
			// System.out.println();
			StringBuilder sb = new StringBuilder();
			if (Constants.isDebug) {
				sb.append(url + "\n");
				sb.append("\tDP : " + pb.BT().getDPMethod() + "\n");
				sb.append("\tEP : " + pb.BT().getReqEPMethod() + "\n");
				sb.append("\tSM : " + sm.toString() + "\n");
				sb.append("\tUnit : " + ut.toString() + "\n");
				Constants.ResultUrls.add(sb.toString());
			}
			Constants.ResultUrls.add(removeBlankwithoutHeapObject(url));
			//Constants.PairInfo.Add_uri(CurrentPb.requestEntry, removeBlankwithoutHeapObject(url), true);
		} else {
			// System.out.println("\t" + ut);
			// JM (DP and DPStmt not match)
			// System.out.println(removeBlankwithoutHeapObject(url));
			Constants.ResultUrls.add(removeBlankwithoutHeapObject(url));
			//Constants.PairInfo.Add_uri(CurrentPb.requestEntry, removeBlankwithoutHeapObject(url), true);
		}
	}

	public static boolean hasHeapObject(String url) {
		if (url.contains("<") && url.contains(">"))
			return true;
		else
			return false;
	}

	private String removeBlankwithoutHeapObject(String url) {
		if (hasHeapObject(url)) {
			int heapidx1 = url.indexOf("<");
			int heapidx2 = url.indexOf(">");
			String heapString = url.substring(heapidx1, heapidx2 + 1);
			Constants.UrlRelatedObject.add(heapString);
		}
		return url;
	}

	private static ArrayList<BFNode> RemoveRedundantNodes(ArrayList<BFNode> inputList) {
		ArrayList<BFNode> outList = new ArrayList<BFNode>();

		for (int i = 0; i < inputList.size(); i++) {
			boolean doesExist = false;
			BFNode bfn1 = inputList.get(i);

			for (int j = 0; j < i; j++) {
				if (bfn1.isSameWith(inputList.get(j))) {
					doesExist = true;
					break;
				}
			}

			if (!doesExist)
				outList.add(bfn1);
		}

		return outList;
	}

	@Override
	public String GenRegex(HashMap<String, String> taintVariableMap, HashMap<String, ArrayList<BFNode>> BFTtable,
			String TrackingReg) {
		// WOOWILLFIX
		// ReplaceHeapObject(BTTree);
		printVisitSet = new HashSet<String>();
		return printUrlFromList(BFTtable, TrackingReg);
	}

	public String printUrlFromList(HashMap<String, ArrayList<BFNode>> BFTtable, String trackingReg) {
		String result = "";
		printVisitSet.add(trackingReg);
		ArrayList<BFNode> oldList = BFTtable.get(trackingReg);

		if (oldList == null)
			return "";

		// remove redundant nodes
		ArrayList<BFNode> list = RemoveRedundantNodes(oldList);

		if (list.size() == 0)
			return "";

		for (BFNode bfn : list) {
			if (bfn.isPhiNode()) {
				continue;
			}
			if (bfn.getVtype() != null) {
				if (bfn.getVtype().contains("HashMap")) {
					if (result.indexOf("?") == -1)
						result += "?" + bfn.getStringForUrl() + "=(.*)";
					else
						result += "&" + bfn.getStringForUrl() + "=(.*)";
				} else {
					if (bfn.getVtype().equals("NameValuePair")) {
						result += bfn.getStringForUrl();
					} else {
						if (bfn.getVtype().equals("URLEncode")) {
							result += bfn.getStringForUrl();
						} else {
							if (bfn.getVtype().equals("org.json.JSONObject") && bfn.getKey() != null) {
								result += "(?|&)" + bfn.getKey() + "=" + bfn.getValue();
							}
						}
					}
				}
			} else {
				if (bfn.getVtype() != null && bfn.getVtype().contains("array")) {
					result += bfn.getpossiblestring();
				} else {
					if (bfn.isHeapObject()) {
						result += HeapHandler.getHeapObjectString(bfn.getSootValue());
					} else {
						if (bfn.getStringForUrl() == null) {
							// System.out.println("bfn url string is null -
							// 20150914
							// hnamkoong");
							result += "(.*)";
						} else {
							if (bfn.getStringForUrl() != null) {
								String urlString = bfn.getStringForUrl();
								if (!urlString.equals("\"\"")) {
//									System.out.println("[Extractocol Url]: " + urlString);
//									result += alignHttpString(urlString).replaceAll("\"", "").replaceAll("null", "");
								}
							}
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	public String AnalyzeExpression(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut,
			SootMethod sm, String strDst, ParameterBucket pb) throws NodeNotFoundException {
		String strMethod = iie.getMethodRef().name();
		String methodref = Constants.Deobfuse(iie.getMethodRef().getSignature().toString());

		this.strDest = strDst;

		strMethod = Constants.getMethodname(methodref).toLowerCase().replaceAll("<|>", "");
		// Check method
		//int chkMethod = methodlist.indexOf(strMethod);
		SemanticParameterBucket spb = new SemanticParameterBucket(iie, BFTtable, strDst, this, ut, sm, methodref, pb);
		AbstractDecorator SemanticDecorator = new DebugDecorator();
		if (Constants.isDebug)
			SemanticDecorator = new DebugDecorator();
		else
			SemanticDecorator = new PairingDecorator();
		
		try{
			switch (MethodNames.valueOf(strMethod)) {
			case tolowercase:
				SemanticDecorator.setBaseModel(new toLower());
				break;
			case setrequestmethod:
				SemanticDecorator.setBaseModel(new setRequest());
				break;
			case add:
				SemanticDecorator.setBaseModel(new add());
				break;
			case init:
				SemanticDecorator.setBaseModel(new init());
				break;
			case getscheme:
				SemanticDecorator.setBaseModel(new getScheme());
				break;
			case appendencodedpath:
				SemanticDecorator.setBaseModel(new appendEncodedPath());
				break;
			case appendpath:
				SemanticDecorator.setBaseModel(new appendPath());
			case append:
				SemanticDecorator.setBaseModel(new append());
				break;
			case setentity:
				SemanticDecorator.setBaseModel(new setEntity());
				break;
			case addpart:
				SemanticDecorator.setBaseModel(new addPart());
				break;
			case tourl:
				break;
			case getcontent:
				break;
			case tostring:
				SemanticDecorator.setBaseModel(new toString());
				break;
			case parse:
				SemanticDecorator.setBaseModel(new parse());
				break;
			case build:
				SemanticDecorator.setBaseModel(new build());
				break;
			case execute:
				SemanticDecorator.setBaseModel(new execute());
				break;
			case openstream:
				this.isGet = true;
				this.printUrl(pb, BFTtable, sm, ut);
				pb.BT().RRI().AddHTTPMethod("GET");
				pb.BT().RRI().SaveURI(pb, TrackingReg);
				break;
			case gethost:
				SemanticDecorator.setBaseModel(new getHost());
				break;
			case buildupon:
				SemanticDecorator.setBaseModel(new buildUpon());
				break;
			case geturi:
				SemanticDecorator.setBaseModel(new getURI());
				break;
			case format:
				SemanticDecorator.setBaseModel(new format());
				break;
			case put:
				SemanticDecorator.setBaseModel(new put());
				break;
			case appendqueryparameter:
				SemanticDecorator.setBaseModel(new appendQueryParameter());
				break;
			case openconnection:
				TrackingReg = iie.getBase().toString();
				// JM we need to track setRequestMethod("POST") or ("GET")
				this.isGet = true;
				this.printUrl(pb, BFTtable, sm, ut);
				pb.BT().RRI().AddHTTPMethod("GET");
				pb.BT().RRI().SaveURI(pb, TrackingReg);
				break;
			case open:
				SemanticDecorator.setBaseModel(new open());
				break;
			case replace:
				SemanticDecorator.setBaseModel(new replace());
				break;
			case replacefirst:
				SemanticDecorator.setBaseModel(new replaceFirst());
				break;
			case iterator:
			case entryset:
			case next:
				SemanticDecorator.setBaseModel(new entrySet());
				break;
			case addall:
				SemanticDecorator.setBaseModel(new addAll());
				break;
			case trim:
				SemanticDecorator.setBaseModel(new trim());
				break;
			case split:
				SemanticDecorator.setBaseModel(new split());
				break;
			case getinputstream:
				SemanticDecorator.setBaseModel(new getInputStream());
				break;
			case path:
				SemanticDecorator.setBaseModel(new path());
				break;
			case url:
				SemanticDecorator.setBaseModel(new url());
				break;
			case post:
				SemanticDecorator.setBaseModel(new post());
				break;
			case get:
				SemanticDecorator.setBaseModel(new get());
				break;
			case ajax:
				SemanticDecorator.setBaseModel(new ajax());
				break;
			case matcher:
				SemanticDecorator.setBaseModel(new matcher());
				break;
			case group:
				SemanticDecorator.setBaseModel(new group());
				break;
			case substring:
				SemanticDecorator.setBaseModel(new substring());
				break;
			case setheader:
				SemanticDecorator.setBaseModel(new setHeader());
				break;
			case toentity:
				SemanticDecorator.setBaseModel(new toEntity());
				break;
			case addheader:
				SemanticDecorator.setBaseModel(new addHeader());
				break;
			case getvalue:
				SemanticDecorator.setBaseModel(new getValue());
				break;
			case getkey:
				SemanticDecorator.setBaseModel(new getKey());
				break;
			case keyset:
				SemanticDecorator.setBaseModel(new keySet());
				break;
			case getstring:
				SemanticDecorator.setBaseModel(new getString()); 
				break;
			case constructcollectiontype:
				SemanticDecorator.setBaseModel(new constructCollectionType());
				break;
			case convertvalue:
				SemanticDecorator.setBaseModel(new convertValue());
				break;
			case frombody: 
				SemanticDecorator.setBaseModel(new fromBody());
				break;
			default:
				SemanticDecorator.setBaseModel(new othercases());
				break;
			}
		}catch (Exception e){
			SemanticDecorator.setBaseModel(new othercases());
		}
		
		SemanticDecorator.applySemantic(spb);
		return "";
	}

	public void printParam() {
		ArrayList<BFNode> list = HeapHandler.GlobalBFTtable.get(
				"<com.contextlogic.wish.http.HttpRequestParams: java.util.concurrent.ConcurrentHashMap requestParams>");
		if (list == null)
			return;
		
		/*System.out.print("Params : ");
		for (BFNode bfn : list) {
			if (bfn.getSootValue() == null)
				System.out.print(bfn.getStringForUrl() + "=(.*)");
		}
		System.out.println("");*/
	}

	@Override
	public void AnalyzeExpression(StaticInvokeExpr sie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut,
			SootMethod sm, String strDest, ParameterBucket pb) {
		this.strDest = strDest;
		String strMethod;
		String methodref = Constants.Deobfuse(sie.getMethodRef().getSignature().toString());
		strMethod = Constants.getMethodname(methodref).toLowerCase().toLowerCase().replaceAll("<|>", "");
		// Check method
		//int chkMethod = methodlist.indexOf(strMethod);
		//if (chkMethod >= 0)
		
		SemanticParameterBucket spb = new SemanticParameterBucket(sie, BFTtable, null, this, ut, sm, methodref, pb);
		AbstractDecorator SemanticDecorator = new DebugDecorator();
		if (Constants.isDebug)
			SemanticDecorator = new DebugDecorator();
		else if (Constants.isPairing)
			SemanticDecorator = new PairingDecorator();
		else
			SemanticDecorator = new PairingDecorator();
		
		try{
			switch (MethodNames.valueOf(strMethod)) {
			case retrievestream:
				SemanticDecorator.setBaseModel(new retrieveStream());
				break;
			case parse:
				SemanticDecorator.setBaseModel(new extractocol.backend.request.semantic.url.models.staticModels.parse());
				break;
			case replace:
				SemanticDecorator
						.setBaseModel(new extractocol.backend.request.semantic.url.models.staticModels.replace());
				break;
			case valueof:
				SemanticDecorator.setBaseModel(new valueOf());
				break;
			case tostring:
				SemanticDecorator
						.setBaseModel(new extractocol.backend.request.semantic.url.models.staticModels.toString());
				break;
			case format:
				SemanticDecorator.setBaseModel(new extractocol.backend.request.semantic.url.models.staticModels.format());
				break;
			case create:
			case encode:
				BFTtable.put(strDest, CopyList(BFTtable.get(sie.getArg(0).toString())));
				break;
			case addqueryparameters:
				SemanticDecorator.setBaseModel(new addqueryparameters());
				break;
			default:
				SemanticDecorator
						.setBaseModel(new extractocol.backend.request.semantic.url.models.staticModels.othercases());
				break;
			}
		}catch (Exception e){
			SemanticDecorator.setBaseModel(new extractocol.backend.request.semantic.url.models.staticModels.othercases());
		}
		
		SemanticDecorator.applySemantic(spb);
	}

	public String VirtualFormatFunction(String target) {
		target = target.replaceAll("%[0-9]*d", "[0-9]*");
		target = target.replaceAll("%[0-9]*s", ".*");
		return target;
	}
}
