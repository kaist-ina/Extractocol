
package Extractocol.BufferExtractor_Request.Semantic.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.gaurav.tree.NodeNotFoundException;
import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.SignatureBuilder_Request;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Basics.ParameterBucket;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapHandler;
import Extractocol.BufferExtractor_Request.Semantic.Retrofit.retrofit_http_old;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.AbstractDecorator;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.DebugDecorator;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.PairingDecorator;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.add;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.addAll;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.addPart;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.ajax;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.append;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.appendEncodedPath;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.appendPath;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.appendQueryParameter;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.build;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.buildUpon;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.entrySet;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.execute;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.format;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.get;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.getHost;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.getInputStream;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.getScheme;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.getURI;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.init;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.open;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.othercases;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.parse;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.path;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.post;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.put;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.replace;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.replaceAll;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.replaceFirst;
//import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.setEntity;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.setRequest;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.split;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.toLower;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.toString;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal.trim;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Static.addqueryparameters;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Static.retrieveStream;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.Static.valueOf;
import Extractocol.BufferExtractor_Request.Semantic.Bluetooth.Models.Normal.getBytes;
import Extractocol.BufferExtractor_Request.Semantic.Bluetooth.Models.Normal.write;
import Extractocol.Outputs.RequestInfoList;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.StaticInvokeExpr;
import soot.toolkits.graph.Block;
import Extractocol.Pairing.PairingDPEntry;
import Extractocol.Pairing.SemanticAppliedList;

public class UrlBuilder extends SignatureBuilder_Request
{
	public String TrackingReg = "";
	public String strDest;
	public boolean isResource;
	private Set<String> printVisitSet;
	public boolean isGet;
	public int Volleymethodint = -1;
	public ParameterBucket CurrentPb;
	public List<String> Rx5 = Arrays.asList("<com.thirdrock.repository.impl.AddressRepositoryImpl: rx.a get(java.lang.String,java.util.Map,java.lang.Class)>",
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
	public class MethodIds
	{
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
		//harmony
		//public static final int sendRequest = 56;
		//public static final int request = 57;
		public static final int getBytes = 55;
	}
	public class Vtypes
	{
		public static final int vint = 0;
		public static final int vstring = 1;
		public static final int vjsonarray = 2;
		public static final int vconst = 3;
	}
	public void parseUrl(ParameterBucket pb)
	{
		//It provide SemanticDecorator with access permission of pb
		CurrentPb = pb;
		
		HashMap<String, ArrayList<BFNode>> BFTtable = new HashMap<String, ArrayList<BFNode>>();
		methodlist = Arrays.asList("<init>", "append", "setEntity", "setHeader", "getJSONFromPost", "getJSONFromUrl", "toString", "execute", "openStream", "format", "add", "put",
				"getActiveNetworkInfo", "fromJson", "retrieveStream", "onPostExecute", "getString", "parse", "appendQueryParameter", "addPart", "setRequestMethod", "write", "serveRequest",
				"retrieveToken", "openConnection", "open", "replace", "toLowerCase", "replaceFirst", "entrySet", "iterator", "next", "addAll", "trim", "getScheme", "getHost", "getInputStream",
				"buildUpon", "build", "appendEncodedPath", "appendPath", "valueOf", "split", "replaceAll", "ajax", "path", "url", "post", "encode", "get", "addQueryParameters", "toURL", "getContent",
				"create", "getURI", "getBytes");
		SemanticModel = Arrays.asList("<android.content.res.AssetManager: java.io.InputStream open(java.lang.String)>", "<java.net.URL: void <init>(java.lang.String)>",
				"<org.apache.http.client.methods.HttpGet: void <init>(java.lang.String)>", "<java.io.InputStream retrieveStream(java.lang.String)>",
				"<java.lang.String: java.lang.String toLowerCase(java.util.Locale)>", "<java.io.OutputStreamWriter: void write(java.lang.String)>",
				"<org.apache.http.impl.client.AbstractHttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>",
				"<org.apache.http.client.HttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>",
				"<org.apache.http.impl.client.DefaultHttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>",
				"<org.apache.http.client.utils.URLEncodedUtils: java.lang.String format(java.util.List,java.lang.String)>",
				"<java.lang.String: java.lang.String format(java.lang.String,java.lang.Object[])>", "<com.squareup.okhttp.OkHttpClient: java.net.HttpURLConnection open(java.net.URL)>",
				"<java.lang.String: java.lang.String replaceFirst(java.lang.String,java.lang.String)>", "<com.android.volley.RequestQueue: com.android.volley.Request add(com.android.volley.Request)>",
				"<com.squareup.okhttp.OkHttpClient: java.net.HttpURLConnection open(java.net.URL)>", "<android.net.Uri: android.net.Uri parse(java.lang.String)>",
				"<com.github.kevinsawicki.http.HttpRequest: com.github.kevinsawicki.http.HttpRequest get(java.lang.CharSequence)>",
				"<com.github.kevinsawicki.http.HttpRequest: com.github.kevinsawicki.http.HttpRequest post(java.lang.CharSequence)>",
				"<android.content.res.AssetManager: java.io.InputStream open(java.lang.String)>", "<java.net.HttpURLConnection: void setRequestMethod(java.lang.String)>",
				"<android.net.Uri$Builder: android.net.Uri$Builder appendQueryParameter(java.lang.String,java.lang.String)>", "<android.net.Uri: android.net.Uri parse(java.lang.String)>",
				"<org.json.JSONObject: org.json.JSONObject put(java.lang.String,java.lang.Object)>", "<org.json.JSONObject: org.json.JSONObject put(java.lang.String,int)>",
				"<org.json.JSONArray: org.json.JSONArray put(java.lang.Object)>", "<java.util.List: boolean add(java.lang.Object)>",
				"<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>", "<java.lang.StringBuffer: java.lang.StringBuffer append(java.lang.String)>",
				"<java.lang.StringBuilder: java.lang.StringBuilder append(int)>", "<java.lang.StringBuilder: java.lang.StringBuilder append(double)>",
				"<java.lang.StringBuilder: java.lang.StringBuilder append(int)>", "<org.apache.http.client.methods.HttpGet: void <init>(java.lang.String)>",
				"<org.apache.http.client.methods.HttpPost: void <init>(java.lang.String)>", "<org.apache.http.message.BasicNameValuePair: void <init>(java.lang.String,java.lang.String)>",
				"<org.apache.http.client.entity.UrlEncodedFormEntity: void <init>(java.util.List)>", "<java.lang.StringBuilder: void <init>()>",
				"<java.lang.StringBuilder: void <init>(java.lang.String)>", "<android.content.Intent: void <init>(java.lang.String,android.net.Uri)>",
				"<java.lang.String: java.lang.String format(java.lang.String,java.lang.Object[])>", "<java.util.concurrent.ConcurrentHashMap: java.lang.Object put(java.lang.Object,java.lang.Object)>",
				"<java.util.concurrent.ConcurrentHashMap: java.util.Set entrySet()>", "<java.util.Set: java.util.Iterator iterator()>", "<java.util.Iterator: java.lang.Object next()>",
				"<java.util.List: boolean addAll(java.util.Collection)>", "<java.lang.String: java.lang.String replace(java.lang.CharSequence,java.lang.CharSequence)",
				"<java.lang.String: java.lang.String trim()>", "<java.lang.StringBuilder: java.lang.String toString()>", "<java.net.URI: void <init>(java.lang.String)>",
				"<java.net.URI: java.lang.String getScheme()>", "<java.net.URI: java.lang.String getHost()>", "<com.squareup.okhttp.OkHttpClient: java.net.HttpURLConnection open(java.net.URL)>",
				"<java.net.HttpURLConnection: java.io.InputStream getInputStream()>", "<android.net.Uri: android.net.Uri$Builder buildUpon()>", "<android.net.Uri$Builder: android.net.Uri build()>",
				"<android.net.Uri$Builder: android.net.Uri$Builder appendEncodedPath(java.lang.String)>", "<android.net.Uri$Builder: android.net.Uri$Builder appendPath(java.lang.String)>",
				"<java.util.Map: java.lang.Object put(java.lang.Object,java.lang.Object)>", "<java.lang.Character: java.lang.Character valueOf(char)>",
				"<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.Object)>", "<java.lang.String: java.lang.String valueOf(java.lang.Object)>",
				"<java.lang.String: java.lang.String replaceAll(java.lang.String,java.lang.String)>", "<com.external.androidquery.a: com.external.androidquery.b ajax(com.external.androidquery.b.d)>",
				"<ch.boye.httpclientandroidlib.client.methods.HttpPost: void <init>(java.lang.String)>",
				"<ch.boye.httpclientandroidlib.client.HttpClient: ch.boye.httpclientandroidlib.HttpResponse execute(ch.boye.httpclientandroidlib.client.methods.HttpUriRequest,ch.boye.httpclientandroidlib.protocol.HttpContext)>",
				"<java.util.HashMap: java.lang.Object put(java.lang.Object,java.lang.Object)>", "<android.net.Uri$Builder: android.net.Uri$Builder path(java.lang.String)>",
				"<org.apache.http.client.HttpClient: java.lang.Object execute(org.apache.http.client.methods.HttpUriRequest,org.apache.http.client.ResponseHandler)>",
				"<android.net.Uri: java.lang.String toString()>", "<org.apache.http.client.methods.HttpPut: void <init>(java.lang.String)>",
				"<com.squareup.okhttp.Request$Builder: com.squareup.okhttp.Request$Builder url(java.lang.String)>", "<com.google.gson.Gson: java.lang.String toJson(java.lang.Object)>",
				"<com.squareup.okhttp.Request$Builder: com.squareup.okhttp.Request$Builder post(com.squareup.okhttp.RequestBody)>",
				"<java.net.URLEncoder: java.lang.String encode(java.lang.String,java.lang.String)>", "<java.lang.String: java.lang.String valueOf(double)>",
				"<java.lang.StringBuilder: java.lang.StringBuilder append(char)>", "<com.squareup.okhttp.Request$Builder: com.squareup.okhttp.Request build()>",
				"<java.lang.Long: java.lang.String toString(long)>", "<com.android.volley.Request: void <init>(int,java.lang.String,com.android.volley.Response$ErrorListener)>",
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
				"<roboguice.inject.Lazy: java.lang.Object get()>", "<roboguice.util.Strings: boolean equalsIgnoreCase(java.lang.Object,java.lang.Object)>",
				"<roboguice.util.Ln: int d(java.lang.Object,java.lang.Object[])>", "<roboguice.util.Strings: boolean notEmpty(java.lang.Object)>",
				"<roboguice.util.Strings: java.lang.String toString(java.io.InputStream)>", "<roboguice.util.Strings: java.lang.String md5(java.lang.String)>",
				"<roboguice.util.Strings: java.lang.String toString(java.lang.Object)>", "<roboguice.util.Strings: boolean equals(java.lang.Object,java.lang.Object)>",
				"<roboguice.util.Strings: boolean isEmpty(java.lang.Object)>", "<oauth.signpost.OAuth: java.lang.String addQueryParameters(java.lang.String,java.lang.String[])>",
				"<oauth.signpost.OAuth: java.lang.String addQueryParameters(java.lang.String,java.util.Map)>", "<java.net.URI: java.net.URL toURL()>",
				"<java.net.URL: java.net.URLConnection openConnection()>",
				"<org.apache.http.client.HttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest,org.apache.http.protocol.HttpContext)>",
				"<java.net.URI: java.net.URI create(java.lang.String)>", "<java.net.URL: java.net.URLConnection openConnection(java.net.Proxy)>",
				"<com.linkedin.android.client.MobileClient: java.net.URI getURI(java.lang.String)>", "<java.lang.StringBuffer: java.lang.String toString()>",
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
				// TODO SEMANTICARRAY
				// ,
				// "Pinterest Case Cover!!" ,
				"<com.pinterest.api.RequestParams: java.util.List b(java.lang.String,java.lang.Object)>",
				// Harmony
				//"<com.logitech.harmonyhub.sdk.imp.BluetoothManager: boolean sendRequest(java.lang.String)>",
				"<java.io.OutputStream: void write(byte[])>",
				"<org.json.JSONObject: void <init>(java.lang.String)>",
				"<org.json.JSONObject: java.lang.String toString()>",
				"<java.lang.String: byte[] getBytes()>"
				//"<com.logitech.harmonyhub.sdk.imp.BluetoothManager: org.json.JSONObject request(java.lang.String)>",
				//"<com.logitech.harmonyhub.sdk.imp.BluetoothManager: org.json.JSONObject request(java.lang.String,long)>"
				//Wish temporarily
				//"<com.contextlogic.wish.http.WishHttpClient: void post(com.contextlogic.wish.http.WishHttpClient$RequestPool,java.lang.Object,java.lang.String,com.contextlogic.wish.http.HttpRequestParams,com.contextlogic.wish.http.HttpResponseHandler)>"
				);
		List<String> retrofitList = new ArrayList<String>();
		for (int i = 0; i < Constants.retrofits.size(); i++)
		{
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
		SemanticModel = retrofitList;
		try
		{
			pb.BFTtable = BFTtable;
			CreateBlockGraphAndInitEdgeTables(pb);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public static String alignHttpString(String httpurl)
	{
		if (httpurl.indexOf("/") != -1)
		{
			httpurl = httpurl.replaceAll("\\/", "\\/");
		}
		if (httpurl.indexOf("?") != -1)
		{
			httpurl = httpurl.replaceAll("\\?", "\\?");
		}
		return httpurl;
	}
	@Override
	public boolean isContainDP(Block block)
	{
		for (Iterator<Unit> iter = block.iterator(); iter.hasNext();)
		{
			Unit ut = iter.next();
			InvokeExpr invokeExpr = null;
			if (ut instanceof AssignStmt)
			{
				AssignStmt _Assignstmt = (AssignStmt) ut;
				if (_Assignstmt.containsInvokeExpr())
				{
					invokeExpr = _Assignstmt.getInvokeExpr();
				}
			}
			else
				if (ut instanceof InvokeStmt)
				{
					invokeExpr = ((InvokeStmt) ut).getInvokeExpr();
				}
			if (invokeExpr != null)
			{
				String strMethod = invokeExpr.getMethodRef().name();
				int chkMethod = methodlist.indexOf(strMethod);
				switch (chkMethod)
				{
					case MethodIds.getJSONFromPost:
					case MethodIds.getJSONFromUrl:
					case MethodIds.fromJson:
					case MethodIds.execute:
					case MethodIds.openStream:
						return true;
					case MethodIds.open:
						if (invokeExpr.getMethodRef().toString().equals("<android.content.res.AssetManager: java.io.InputStream open(java.lang.String)>"))
							return true;
					break;
					case MethodIds.init:
						if (invokeExpr.getMethodRef().toString().equals("<java.net.URL: void <init>(java.lang.String)>"))
							return true;
						if (invokeExpr.getMethodRef().toString().equals("<org.apache.http.client.methods.HttpGet: void <init>(java.lang.String)>"))
							return true;
					break;
					case MethodIds.toLowerCase:
						if (invokeExpr.getMethodRef().toString().indexOf("<java.io.InputStream retrieveStream(java.lang.String)>") != -1)
							return true;
					break;
					default:
					break;
				}
			}
		}
		return false;
	}
	public void printUrl(HashMap<String, ArrayList<BFNode>> BFTtable, SootMethod sm, Unit ut)
	{
		String url = GenRegex(null, BFTtable, TrackingReg);
		Constants.VolleyUrlCandidate = "";
		if (url.equals("") || url.equals("(.*)"))
		{
			return;
		}
		if (!TrackingReg.equals("Volley_result"))
			url = (isGet ? "GET " : "POST ") + url;
		
		
			
		if (sm.toString().equals(Constants.EPcont.getDP().toString()) && ut.toString().equals(Constants.EPcont.getDPStmt().toString()))
		{
			// System.out.println();
			// System.out.println(removeBlankwithoutHeapObject(url));
			// System.out.println();
			StringBuilder sb = new StringBuilder();
			if (Constants.isDebug)
			{
				sb.append(url + "\n");
				sb.append("\tDP : " + Constants.EPcont.getDP().getSignature() + "\n");
				sb.append("\tEP : " + Constants.CurrentEP + "\n");
				sb.append("\tSM : " + sm.toString() + "\n");
				sb.append("\tUnit : " + ut.toString() + "\n");
				Constants.ResultUrls.add(sb.toString());
			}
			Constants.ResultUrls.add(removeBlankwithoutHeapObject(url));
			Constants.PairInfo.Add_uri(CurrentPb.requestEntry, removeBlankwithoutHeapObject(url), true);
		}
		else
		{
			// System.out.println("\t" + ut);
			// JM (DP and DPStmt not match)
			// System.out.println(removeBlankwithoutHeapObject(url));
			Constants.ResultUrls.add(removeBlankwithoutHeapObject(url));
			Constants.PairInfo.Add_uri(CurrentPb.requestEntry, removeBlankwithoutHeapObject(url), true);
		}
		
		//if (hasHeapObject(url)) // Modified by Byungkwon
		//RequestInfoList.AddRequestInfo(url, new PairingInfoList(Constants.EPcont.getDP().toString(), Constants.EPcont.getDPStmt().toString(), Constants.SemanticMethodAndStmt));
		//RequestInfoList.AddPairingInfo(url, pairingInfoList, Constants.EPcont.get, Constants.EPcont.getDP().toString());
		Constants.reqRespInfo.reqie.setSignature(url);
		Constants.reqRespInfo.reqie.setDPMethod(Constants.EPcont.getDP().toString());
		Constants.reqRespInfo.reqie.setDPStmt(Constants.EPcont.getDPStmt().toString());
		Constants.reqRespInfo.reqie.setSemanticAppliedList(new SemanticAppliedList(Constants.SemanticMethodAndStmt));
		
		if (Constants.isDebug){
			System.out.println("\t" + url);
			System.out.println("\t\t" + "# of statements to which semantic models are applied: " + Constants.SemanticMethodAndStmt.size());
		}
		Constants.SemanticMethodAndStmt.clear();
	}
	public static boolean hasHeapObject(String url)
	{
		if (url.contains("<") && url.contains(">"))
			return true;
		else
			return false;
	}
	private String removeBlankwithoutHeapObject(String url)
	{
		if (hasHeapObject(url))
		{
			int heapidx1 = url.indexOf("<");
			int heapidx2 = url.indexOf(">");
			String heapString = url.substring(heapidx1, heapidx2 + 1);
			Constants.UrlRelatedObject.add(heapString);
		}
		return url;
	}
	@Override
	public String GenRegex(HashMap<String, String> taintVariableMap, HashMap<String, ArrayList<BFNode>> BFTtable, String TrackingReg)
	{
		// WOOWILLFIX
		// ReplaceHeapObject(BTTree);
		printVisitSet = new HashSet<String>();
		return printUrlFromList(BFTtable, TrackingReg);
	}
	
	// Duong
	public String printBTFromList(HashMap<String, ArrayList<BFNode>> BFTtable, String trackingReg){
		String result = "";
		printVisitSet.add(trackingReg);
		ArrayList<BFNode> list = BFTtable.get(trackingReg);
		if (list == null)
		{
			return "";
		}
		for (BFNode bfn : list)
		{
			if (bfn.getVtype() != null)
			{
				if (bfn.getVtype().equals("org.json.JSONObject") && bfn.getKey() != null)
				{
					if (bfn.isStartJson() && bfn.isEndJson())
						result += "{" + bfn.getKey() + ":" + bfn.getValue() + "}";
					else if (bfn.isStartJson())
						result += "{" + bfn.getKey() + ":" + bfn.getValue() + ", ";
					else if (bfn.isEndJson())
						result += bfn.getKey() + ":" + bfn.getValue() + "}" + "(?|&)";
					else
						result += bfn.getKey() + ":" + bfn.getValue() + ", ";
				}
			}
			else{
				result += bfn.getStringForUrl();
			}
		}
		return result;
	}
	public String printUrlFromList(HashMap<String, ArrayList<BFNode>> BFTtable, String trackingReg)
	{
		String result = "";
		printVisitSet.add(trackingReg);
		ArrayList<BFNode> list = BFTtable.get(trackingReg);
		if (list == null)
		{
			return "";
		}
		for (BFNode bfn : list)
		{
			if (bfn.isPhiNode())
			{
				continue;
			}
			if (bfn.getVtype() != null)
			{
				if (bfn.getVtype().contains("HashMap"))
				{
					if (result.indexOf("?") == -1)
						result += "?" + bfn.getStringForUrl() + "=(.*)";
					else
						result += "&" + bfn.getStringForUrl() + "=(.*)";
				}
				else
				{
					if (bfn.getVtype().equals("NameValuePair"))
					{
						result += bfn.getStringForUrl();
					}
					else
					{
						if (bfn.getVtype().equals("URLEncode"))
						{
							result += bfn.getStringForUrl();
						}
						else
						{
							if (bfn.getVtype().equals("org.json.JSONObject") && bfn.getKey() != null)
							{
								result += "(?|&)" + bfn.getKey() + "=" + bfn.getValue();
							}
						}
					}
				}
			}
			else
			{
				if (bfn.getVtype() != null && bfn.getVtype().contains("array"))
				{
					result += bfn.getpossiblestring();
				}
				else
				{
					if (bfn.isHeapObject())
					{
						result += HeapHandler.getHeapObjectString(bfn.getSootValue());
					}
					else
					{
						if (bfn.getStringForUrl() == null)
						{
							// System.out.println("bfn url string is null -
							// 20150914
							// hnamkoong");
							result += "(.*)";
						}
						else
						{
							if (bfn.getStringForUrl() != null)
							{
								String urlString = bfn.getStringForUrl();
								if (!urlString.equals("\"\""))
								{
									//Duong
									//result += alignHttpString(urlString).replaceAll("\"", "").replaceAll("null", "");
									result += alignHttpString(urlString).replaceAll("null", "");
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
	public String AnalyzeExpression(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm, String strDst) throws NodeNotFoundException
	{
		String strMethod = iie.getMethodRef().name();
		strDest = strDst;
		String methodref = Constants.Deobfuse(iie.getMethodRef().getSignature().toString());
		strMethod = Constants.getMethodname(methodref);
		// Check method
		int chkMethod = methodlist.indexOf(strMethod);
		SemanticParameterBucket spb = new SemanticParameterBucket(iie, BFTtable, strDst, this, ut, sm, methodref);
		AbstractDecorator SemanticDecorator = new DebugDecorator();
		if (Constants.isDebug)
			SemanticDecorator = new DebugDecorator();
		else
			if (Constants.isPairing)
				SemanticDecorator = new PairingDecorator();
			else
				SemanticDecorator = new PairingDecorator();
		switch (chkMethod)
		{
			case MethodIds.toLowerCase:
				SemanticDecorator.setBaseModel(new toLower());
			break;
			case MethodIds.setRequestMethod:
				SemanticDecorator.setBaseModel(new setRequest());
			break;
			case MethodIds.add:
				SemanticDecorator.setBaseModel(new add());
			break;
			case MethodIds.init:
				SemanticDecorator.setBaseModel(new init());
			break;
			case MethodIds.getScheme:
				SemanticDecorator.setBaseModel(new getScheme());
			break;
			case MethodIds.appendEncodedPath:
				SemanticDecorator.setBaseModel(new appendEncodedPath());
			break;
			case MethodIds.appendPath:
				SemanticDecorator.setBaseModel(new appendPath());
			case MethodIds.append:
				SemanticDecorator.setBaseModel(new append());
			break;
			case MethodIds.setEntity:
				//SemanticDecorator.setBaseModel(new setEntity());
				break;
			case MethodIds.addPart:
				SemanticDecorator.setBaseModel(new addPart());
			break;
			case MethodIds.toURL:
			case MethodIds.getContent:
			case MethodIds.toString:
				SemanticDecorator.setBaseModel(new toString());
			break;
			case MethodIds.parse:
				SemanticDecorator.setBaseModel(new parse());
			break;
			case MethodIds.build:
				SemanticDecorator.setBaseModel(new build());
			break;
			case MethodIds.execute:
				SemanticDecorator.setBaseModel(new execute());
				break;
			case MethodIds.openStream:
				this.isGet = true;
				this.printUrl(BFTtable, sm, ut);
			break;
			case MethodIds.getHost:
				SemanticDecorator.setBaseModel(new getHost());
			break;
			case MethodIds.buildUpon:
				SemanticDecorator.setBaseModel(new buildUpon());
			break;
			case MethodIds.getURI:
				SemanticDecorator.setBaseModel(new getURI());
			break;
			case MethodIds.format:
				SemanticDecorator.setBaseModel(new format());
			break;
			case MethodIds.put:
				SemanticDecorator.setBaseModel(new put());
			break;
			case MethodIds.appendQueryParameter:
				SemanticDecorator.setBaseModel(new appendQueryParameter());
			break;
			case MethodIds.openConnection:
				TrackingReg = iie.getBase().toString();
				//JM we need to track setRequestMethod("POST") or ("GET")
				this.isGet = true;
				this.printUrl(BFTtable, sm, ut);
			break;
			case MethodIds.open:
				SemanticDecorator.setBaseModel(new open());
			break;
			case MethodIds.replace:
				SemanticDecorator.setBaseModel(new replace());
			break;
			case MethodIds.replaceFirst:
				SemanticDecorator.setBaseModel(new replaceFirst());
			break;
			case MethodIds.replaceAll:
				SemanticDecorator.setBaseModel(new replaceAll());
			break;
			case MethodIds.iterator:
			case MethodIds.entrySet:
			case MethodIds.next:
				SemanticDecorator.setBaseModel(new entrySet());
			break;
			case MethodIds.addAll:
				SemanticDecorator.setBaseModel(new addAll());
			break;
			case MethodIds.trim:
				SemanticDecorator.setBaseModel(new trim());
			break;
			case MethodIds.split:
				SemanticDecorator.setBaseModel(new split());
			break;
			case MethodIds.getInputStream:
				SemanticDecorator.setBaseModel(new getInputStream());
			break;
			case MethodIds.path:
				SemanticDecorator.setBaseModel(new path());
			break;
			case MethodIds.url:
				BFTtable.put(strDest, CopyList(BFTtable.get(iie.getArg(0).toString())));
			break;
			case MethodIds.post:
				SemanticDecorator.setBaseModel(new post());
			break;
			case MethodIds.get:
				SemanticDecorator.setBaseModel(new get());
			break;
			case MethodIds.ajax:
				SemanticDecorator.setBaseModel(new ajax());
			break;
			case MethodIds.write:
				this.printUrl(BFTtable, sm, ut);
				SemanticDecorator.setBaseModel(new write());
			break;
			case MethodIds.getBytes:
				SemanticDecorator.setBaseModel(new getBytes());
			break;
			default:
				SemanticDecorator.setBaseModel(new othercases());
			break;
		}
		//System.out.println(">>>strMethod " + strMethod);
		SemanticDecorator.applySemantic(spb);
		return "";
	}
	public void printParam()
	{
		ArrayList<BFNode> list = HeapHandler.GlobalBFTtable.get("<com.contextlogic.wish.http.HttpRequestParams: java.util.concurrent.ConcurrentHashMap requestParams>");
		if (list == null)
			return;
		System.out.print("Params : ");
		for (BFNode bfn : list)
		{
			if (bfn.getSootValue() == null)
				System.out.print(bfn.getStringForUrl() + "=(.*)");
		}
		System.out.println("");
	}
	@Override
	public void AnalyzeExpression(StaticInvokeExpr sie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm, String strDest)
	{
		this.strDest = strDest;
		String strMethod;
		String methodref = Constants.Deobfuse(sie.getMethodRef().getSignature().toString());
		strMethod = Constants.getMethodname(methodref);
		// Check method
		int chkMethod = methodlist.indexOf(strMethod);
		if (chkMethod >= 0)
			Constants.isSemantic = true;
		SemanticParameterBucket spb = new SemanticParameterBucket(sie, BFTtable, null, this, ut, sm, methodref);
		AbstractDecorator SemanticDecorator = new DebugDecorator();
		if (Constants.isDebug)
			SemanticDecorator = new DebugDecorator();
		else
			if (Constants.isPairing)
				SemanticDecorator = new PairingDecorator();
			else
				SemanticDecorator = new PairingDecorator();
		switch (chkMethod)
		{
			case MethodIds.retrieveStream:
				SemanticDecorator.setBaseModel(new retrieveStream());
			break;
			case MethodIds.parse:
				SemanticDecorator.setBaseModel(new Extractocol.BufferExtractor_Request.Semantic.URL.Models.Static.parse());
			break;
			case MethodIds.replace:
				SemanticDecorator.setBaseModel(new Extractocol.BufferExtractor_Request.Semantic.URL.Models.Static.replace());
			break;
			case MethodIds.valueOf:
				SemanticDecorator.setBaseModel(new valueOf());
			break;
			case MethodIds.toString:
				SemanticDecorator.setBaseModel(new Extractocol.BufferExtractor_Request.Semantic.URL.Models.Static.toString());
			break;
			case MethodIds.format:
				SemanticDecorator.setBaseModel(new Extractocol.BufferExtractor_Request.Semantic.URL.Models.Static.format());
			break;
			case MethodIds.create:
			case MethodIds.encode:
				BFTtable.put(strDest, CopyList(BFTtable.get(sie.getArg(0).toString())));
			break;
			case MethodIds.addQueryParameters:
				SemanticDecorator.setBaseModel(new addqueryparameters());
			break;
			/*case MethodIds.sendRequest:
				SemanticDecorator.setBaseModel(new sendRequest());
				System.out.println(">>>>>>>>>>>>>>>>>>>>SEND REQUEST strDest:" + strDest);
				break;*/
			/*case MethodIds.request:
				SemanticDecorator.setBaseModel(new request());
				System.out.println(">>>>>>>>>>>>>>>>>>>>strDest:" + strDest);
				break;*/
			default:
				//System.out.println(">>>>>>>>>default strMethod" + strMethod);
				SemanticDecorator.setBaseModel(new Extractocol.BufferExtractor_Request.Semantic.URL.Models.Static.othercases());
			break;
		}
		System.out.println(">>>strMethod " + strMethod + ">>>chkMethod " + chkMethod);
		SemanticDecorator.applySemantic(spb);
	}
	public String VirtualFormatFunction(String target)
	{
		target = target.replaceAll("%[0-9]*d", "[0-9]*");
		target = target.replaceAll("%[0-9]*s", ".*");
		return target;
	}
	String replaceLast(String string, String substring, String replacement)
	{
	  int index = string.lastIndexOf(substring);
	  if (index == -1)
	    return string;
	  return string.substring(0, index) + replacement
	          + string.substring(index+substring.length());
	}
}
