package SignExtractor.ApplicationSpecific.ted;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.JastAddJ.Variable;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.slice.AbstractSlice;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.source.ISourceSinkManager;
import soot.jimple.infoflow.util.SystemClassHandler;
import soot.jimple.parser.JimpleAST;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.toolkits.graph.Block;

import com.gaurav.tree.ArrayListTree;
import com.gaurav.tree.NodeNotFoundException;
import com.gaurav.tree.Tree;
import com.sun.corba.se.spi.ior.MakeImmutable;

import Extractocol.Constants;
import Extractocol.ExtractocolException;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Basics.SliceManager;
import Extractocol.Pairing.PairingDPEntry;
import Extractocol.Pairing.PairingEPEntry;
import emulator.EntrypointFinder;

public class ted_UrlParser extends ted_BufferExtractor
{
	private String TrackingReg = "";
	private String BodyTrackingReg = "";
	private String strDest;
	public boolean isResource;
	private SliceManager _SliceManager;
	private Set<String> printVisitSet;
	public boolean isGet;
	HashMap<String, ArrayList<PairingEPEntry>> pairingInfoTable;

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
	}

	public class Vtypes
	{
		public static final int vint = 0;
		public static final int vstring = 1;
		public static final int vjsonarray = 2;
		public static final int vconst = 3;
	}

	public ted_UrlParser()
	{
	}

	public void parseUrl(SliceManager SliceM, SootMethod sm, JimpleAST jast, boolean direction, ArrayList<String> path, int pathidx, boolean isCallee, IInfoflowCFG iCfg,
			Set<SootMethod> set)
	{
		HashMap<String, ArrayList<BFNode>> BFTtable = new HashMap<String, ArrayList<BFNode>>();
		
		HashMap<String, ArrayList<PairingEPEntry>> pairingInfoTable = new HashMap<String, ArrayList<PairingEPEntry>>();
		
		
		_SliceManager = SliceM;

		this.methodlist = Arrays.asList("<init>", "append", "setEntity", "setHeader", "getJSONFromPost", "getJSONFromUrl", "toString", "execute", "openStream", "format",
				"add", "put", "getActiveNetworkInfo", "fromJson", "retrieveStream", "onPostExecute", "getString", "parse", "appendQueryParameter", "addPart",
				"setRequestMethod", "write", "serveRequest", "retrieveToken", "openConnection", "open", "replace", "toLowerCase");
		try
		{
			Constants.path = path;
			Constants.pathidx = pathidx;
			Constants.isCallee = isCallee;
			Constants.iCfg = iCfg;
			ExtractStmt(SliceM, sm, BFTtable, true, iCfg, null, null, null, pairingInfoTable, null);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private String alignHttpString(String httpurl)
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
			} else if (ut instanceof InvokeStmt)
			{
				invokeExpr = ((InvokeStmt) ut).getInvokeExpr();
			}

			if (invokeExpr != null)
			{
				String strMethod = invokeExpr.getMethodRef().name();
				int chkMethod = methodlist.indexOf(strMethod);

				switch (chkMethod)
				{
					case MethodIds.getJSONFromPost :
					case MethodIds.getJSONFromUrl :
					case MethodIds.fromJson :
					case MethodIds.execute :
					case MethodIds.openStream :
						return true;
					case MethodIds.open :
						if (invokeExpr.getMethodRef().toString().equals("<android.content.res.AssetManager: java.io.InputStream open(java.lang.String)>"))
							return true;
						break;
					case MethodIds.init :
						if (invokeExpr.getMethodRef().toString().equals("<java.net.URL: void <init>(java.lang.String)>"))
							return true;
						if (invokeExpr.getMethodRef().toString().equals("<org.apache.http.client.methods.HttpGet: void <init>(java.lang.String)>"))
							return true;
						break;
					case MethodIds.toLowerCase :
						if (invokeExpr.getMethodRef().toString().indexOf("java.io.InputStream retrieveStream(java.lang.String)>") != -1)
							return true;
						break;
					default :
						break;
				}
			}
		}
		return false;
	}

	@SuppressWarnings({"unchecked", "unused", "rawtypes"})
	public void printUrl(HashMap<String, ArrayList<BFNode>> BFTtable, SootMethod sm, Unit ut) throws ExtractocolException
	{
		System.out.println(ut);
		System.out.println("come");
		String url = GenRegex(null, BFTtable, TrackingReg);
		if(url.equals("") || url.equals("(.*)")) {
			return;
		}
		url = (isGet ? "GET " : "POST ") + url;

		if(sm.toString().equals(Constants.EPcont.getDP().toString()) && ut.toString().equals(Constants.EPcont.getDPStmt().toString())) {

			// for paring - 20150916 hnamkoong
			String key = Constants.EPcont.getDP().toString() + Constants.EPcont.getDPStmt().toString() + url;
			PairingDPEntry dpentry = Constants.pairingInfoContainer.get(key);
			if(dpentry == null) {
				dpentry = new PairingDPEntry(Constants.EPcont.getDP(), Constants.EPcont.getDPStmt(), url);
				Constants.pairingInfoContainer.put(key, dpentry);
			}
			if(pairingInfoTable.get(TrackingReg) != null)
				dpentry.addEPEntries(pairingInfoTable.get(TrackingReg));

			url = "url : " + url;
			System.out.println();
			System.out.println(url);
			System.out.println();

			StringBuilder sb = new StringBuilder();
			sb.append(url + "\n");
			sb.append("\tDP : " + Constants.EPcont.getDP().getSignature() + "\n");
			sb.append("\tEP : " + Constants.CurrentEP + "\n");
			sb.append("\tSM : " + sm.toString() + "\n");
			sb.append("\tUnit : " + ut.toString() + "\n");
			Constants.ResultUrls.add(sb.toString());

			Constants.ResultUrls.add(url);
			
			// interface invoke! this can't be allowed 20150921
			// many urls at one EP
//			Constants3.foundDPStmt = true;

			
		}
		else {
			url = "url : " + url;
			System.out.println("\t" + ut);
			System.out.println("(DP and DPStmt not match) " + url);
			System.out.println();
		}
	}

	@Override
	public String GenRegex(HashMap<String, String> taintVariableMap, HashMap<String, ArrayList<BFNode>> BFTtable, String TrackingReg)
	{
		//WOOWILLFIX
		//ReplaceHeapObject(BTTree);
		printVisitSet = new HashSet<String>();
		return printUrlFromList(BFTtable, TrackingReg);
	}

	private String printUrlFromList(HashMap<String, ArrayList<BFNode>> BFTtable, String trackingReg)
	{
		String result = "";
		
		String ret;
		printVisitSet.add(trackingReg);

		ArrayList<BFNode> list = BFTtable.get(trackingReg);
		if(list == null) {
			return "";
		}
		for(BFNode bfn : list)
		{
			if (bfn.isPhiNode())
			{
				continue;
			}

			if(bfn.isHeapObject()) {
//				result += "(.*)";
//				result += HeapHandler.getHeapObjectString(bfn.getSootValue());
			}
			else if(bfn.getStringForUrl() == null) {
				System.out.println("bfn url string is null - 20150914 hnamkoong");
				result += "(.*)";
			}else if (bfn.getStringForUrl() != null)
			{
				String urlString = bfn.getStringForUrl();
				if (!urlString.equals("\"\""))
				{
					result += alignHttpString(urlString).replaceAll("\"", "");

//					this is for debug purpose
//					result += "(" + alignHttpString(urlString).replaceAll("\"", "") + ")";
				}
			}
		}
		

		return result;
	}

	private void AddBody(String string)
	{
		// if (pi.Siginfo.Body.indexOf(string) == -1)
	}

	/* print HTTP Body. comment out 20150912 hnamkoong
	public void printBody(@SuppressWarnings("rawtypes") HashMap<String, Tree> BFTtable)
	{
		if (!BodyTrackingReg.equals(""))
		{
			// System.out.println("TrackingReg : " + TrackingReg);
			try
			{
				Tree BTTree = BFTtable.get(BodyTrackingReg);
				if (BTTree != null)
				{
					System.out.print("\n\trequest body : ");
					for (Iterator<BFNode> iter = BTTree.preOrderTraversal().iterator(); iter.hasNext();)
					{
						BFNode bfn = iter.next();
						if (bfn.getKey() != null)
						{
							if (!bfn.getKey().equals("\"\""))
							{
								// System.out.println(bfn.getKey() + ":" +
								// bfn.getValue());
								if (bfn.getValue() != null)
								{
									String a;
									if (!isGet)
										a = bfn.getKey().replaceAll("[&]", "");
									else
										a = bfn.getKey();
									AddBody(a + "=" + bfn.getValue().replace("\"", "") + " ");
								} else if (!bfn.getKey().equals(".*"))
								{
									String a;
									if (!isGet)
										a = bfn.getKey().replaceAll("[&]", "");
									else
										a = bfn.getKey();
									AddBody(a + "=" + ".*" + " ");
								}
							}
						}
					}
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	*/

	private void getStringfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm)
			throws IOException
	{
		//For parsing preference XML. 20150911

		//		if (iie.getMethodRef().toString().equals("<android.content.Context: java.lang.String getString(int)>"))
		//		{
		//			if (iie.getArg(0) instanceof IntConstant)
		//			{
		//				@SuppressWarnings("unused")
		//				PreferenceParser Pp = new PreferenceParser(this.Apkname, iie.getArg(0).toString());
		//				Pp.DecompileXML();
		//				String value = Pp.SearchConstantString();
		//
		//				@SuppressWarnings("rawtypes")
		//				Tree tr = BFTtable.get(strDest);
		//				if (tr != null)
		//				{
		//					BFNode bfn = new BFNode();
		//					bfn.setKey(value);
		//					bfn.setValue("");
		//					bfn.setVtype("String");
		//					tr.add(bfn);
		//				}
		//			} else
		//			{
		//
		//				if (sMtable.containsKey(iie.getArg(0).toString()))
		//				{
		//					String Key = sMtable.get(iie.getArg(0).toString()).getValue();
		//					if (Key != null)
		//					{
		//						PreferenceParser Pp = new PreferenceParser(this.Apkname, Key);
		//						Pp.DecompileXML();
		//						String value = Pp.SearchConstantString();
		//
		//						@SuppressWarnings("rawtypes")
		//						Tree tr = BFTtable.get(strDest);
		//						if (tr != null)
		//						{
		//							BFNode bfn = new BFNode();
		//							bfn.setKey(value);
		//							bfn.setValue("");
		//							bfn.setVtype("String");
		//							tr.add(bfn);
		//						}
		//					}
		//				}
		//			}
		//		}
	}

	@SuppressWarnings({"rawtypes", "unused"})
	@Override
	public String ExtractingExpr(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, String ParentMethod,
			SootMethod sm, String strDst, HashMap<String, ArrayList<PairingEPEntry>> _pairingInfoTable)
					throws NodeNotFoundException, ExtractocolException
	{
		//TODO ------------- semantic model

		pairingInfoTable = _pairingInfoTable;
		String strMethod = iie.getMethodRef().name();
		List<Value> args = new ArrayList<Value>();
		Value larg = null;
		Value rarg = null;

		strDest = strDst;

		// Check method
		int chkMethod = methodlist.indexOf(strMethod);

		args = iie.getArgs();

		switch (chkMethod)
		{
			case MethodIds.toLowerCase :
				if (iie.getMethodRef().getSignature().equals("<java.lang.String: java.lang.String toLowerCase(java.util.Locale)>"))
				{
					BFTtable.put(strDst, CopyList(BFTtable.get(iie.getBase().toString())));
				}
				break;
			case MethodIds.setRequestMethod :
				setRequestMethodfuncHandler(iie, BFTtable, ut, sm);
				break;

			case MethodIds.add :
				addfuncHandler(iie, BFTtable, ut, sm);
				break;
			case MethodIds.init :
				initfuncHandler(iie, BFTtable, ut, sm);
				break;
			case MethodIds.append :
				appendfuncHandler(iie, BFTtable, ut, sm);
				break;
			case MethodIds.setEntity :
				AddStatement(iie, sm);
				TrackingReg = iie.getBase().toString();
				return "";
			case MethodIds.addPart :
				addPartfuncHandler(iie, BFTtable, ut, sm);
				break;
			case MethodIds.write :
				if (iie.getMethodRef().toString().equals("<java.io.OutputStreamWriter: void write(java.lang.String)>"))
				{
					BodyTrackingReg = iie.getArg(0).toString();
				}
				break;
			case MethodIds.getJSONFromPost :
//				if (iie.getArg(0) instanceof Constant)
//				{
//					TrackingReg = "";
//					AddStatement(iie, sm);
//				} else
//				{
//					TrackingReg = iie.getArg(0).toString();
//					AddStatement(iie, sm);
//				}
//				this.printUrl(BFTtable, sm, ut);
				return "";
			case MethodIds.getActiveNetworkInfo :
				AddStatement(iie, sm);
				break;
			case MethodIds.onPostExecute :
				break;
			case MethodIds.getJSONFromUrl :
				// strUrl = iie.getArg(0).toString();
//				if (iie.getArg(0)  instanceof Constant)
//				{
//					TrackingReg = "";
//					AddStatement(iie, sm);
//				} else
//				{
//					TrackingReg = iie.getArg(0).toString();
//					AddStatement(iie, sm);
//				}
//				this.printUrl(BFTtable, sm, ut);
				return "";
			case MethodIds.getString :
				try
				{
					getStringfuncHandler(iie, BFTtable, ut, sm);
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				break;
			case MethodIds.fromJson :
//				if (iie.getMethodRef().toString().equals(""))
//				{
//					if (iie.getArg(0) instanceof Constant)
//					{
//						TrackingReg = "";
//						AddStatement(iie, sm);
//					}
//					this.printUrl(BFTtable, sm, ut);
//				}
				break;
			case MethodIds.toString :
			{
				ArrayList<BFNode> list = BFTtable.get(iie.getBase().toString());
				BFTtable.put(this.strDest, CopyList(list));
				
				ArrayList<PairingEPEntry> plist = pairingInfoTable.get(iie.getBase().toString());
				pairingInfoTable.put(this.strDest, CopyPairingList(plist));
				
				AddStatement(iie, sm);
			}
				break;
			case MethodIds.parse :
				parsefuncHandler(iie, BFTtable, ut, sm);
				break;
			case MethodIds.execute :
				if(iie.getMethodRef().toString().equals("<org.apache.http.impl.client.AbstractHttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>") ||
						iie.getMethodRef().toString().equals("<org.apache.http.client.HttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>") ||
						iie.getMethodRef().toString().equals("<org.apache.http.impl.client.DefaultHttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>")) {
					AddStatement(iie, sm);
					TrackingReg = iie.getArg(0).toString();
					this.printUrl(BFTtable, sm, ut);
				}
				break;
			case MethodIds.openStream :
				AddStatement(iie, sm);
				this.printUrl(BFTtable, sm, ut);
				break;
			case MethodIds.format :
				if (iie.getMethodRef().toString() == "<org.apache.http.client.utils.URLEncodedUtils: java.lang.String format(java.util.List,java.lang.String)>")
				{
					ArrayList<BFNode> tr = BFTtable.get(strDest);
					BFTtable.put(strDest, CopyList(BFTtable.get(iie.getArg(0).toString())));
					AddStatement(iie, sm);
				} else if (iie.getMethodRef().toString().equals("<java.lang.String: java.lang.String format(java.lang.String,java.lang.Object[])>"))
				{
					ArrayList<BFNode> tr = BFTtable.get(strDest);
					if (tr != null)
					{
						BFNode bfn = new BFNode();
						String arg0 = iie.getArg(0).toString();
						bfn.makeUrlBfn(arg0);
						System.out.println("Format Function!!");
					}
					
				}
				break;
			case MethodIds.put :
				putfuncHandler(iie, BFTtable, ut, sm);
				break;
			case MethodIds.appendQueryParameter :
				appendQueryParameter(iie, BFTtable, ut, sm);
				break;
			case MethodIds.openConnection :
//				openConnectionfuncHandler(iie, BFTtable, ut, sm);
				TrackingReg = iie.getBase().toString();
				this.printUrl(BFTtable, sm, ut);
				break;
			case MethodIds.open :
				openfuncHandler(iie, BFTtable, ut, sm);
				break;
			case MethodIds.retrieveToken :
				break;
			default :
				break;
		}
		if(iie.getMethodRef().toString().contains("<com.squareup.okhttp.OkHttpClient")) {
			okHttpHandler(iie, BFTtable, ut, sm);
		}
		return "";
	}

	private void okHttpHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm) throws ExtractocolException
	{
		if(iie.getMethodRef().toString().equals("<com.squareup.okhttp.OkHttpClient: java.net.HttpURLConnection open(java.net.URL)>")) {
			String arg0 = iie.getArg(0).toString();
			TrackingReg = arg0;
			this.printUrl(BFTtable, sm, ut);
		}
	}
	private void androidNetUriHandler(StaticInvokeExpr sie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm)
	{
		// same as toString
		if(sie.getMethodRef().toString().equals("<android.net.Uri: android.net.Uri parse(java.lang.String)>")) {
			if(this.strDest != null) {
				if(sie.getArg(0) instanceof Constant) {
					ArrayList<BFNode> list = new ArrayList<BFNode>();
					BFNode bfn = new BFNode();
					bfn.makeUrlBfn(sie.getArg(0).toString());
					list.add(bfn);
					BFTtable.put(this.strDest, list);
				}
				else {
					String arg0 = sie.getArg(0).toString();

					ArrayList<BFNode> list = BFTtable.get(arg0);
					BFTtable.put(this.strDest, CopyList(list));

					ArrayList<PairingEPEntry> plist = pairingInfoTable.get(arg0);
					pairingInfoTable.put(this.strDest, CopyPairingList(plist));
				}
			}
		}
	}


	private void kevinsawickiHttpHandler(StaticInvokeExpr sie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm) throws ExtractocolException
	{
		boolean isSemantic = false;
		if(sie.getMethodRef().toString().equals("<com.github.kevinsawicki.http.HttpRequest: com.github.kevinsawicki.http.HttpRequest get(java.lang.CharSequence)>"))
		{
			isGet = true;
			isSemantic = true;
		}
		else if(sie.getMethodRef().toString().equals("<com.github.kevinsawicki.http.HttpRequest: com.github.kevinsawicki.http.HttpRequest post(java.lang.CharSequence)>"))
		{
			isGet = false;
			isSemantic = true;
		}
		
		if(isSemantic) {
			String arg0 = sie.getArg(0).toString();
			TrackingReg = arg0;
			this.printUrl(BFTtable, sm, ut);
		}
	}

	private void openfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm)
	{

		if (iie.getMethodRef().toString().equals("<android.content.res.AssetManager: java.io.InputStream open(java.lang.String)>"))
		{
			TrackingReg = iie.getArg(0).toString();
			// Track = true;
			AddStatement(iie, sm);
			isGet = true;
			try
			{
				this.printUrl(BFTtable, sm, ut);
			} catch (ExtractocolException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void openConnectionfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm)
			throws NodeNotFoundException
	{
	}

	private void setRequestMethodfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm)
	{
		if (iie.getMethodRef().toString().equals("<java.net.HttpURLConnection: void setRequestMethod(java.lang.String)>"))
		{
			if (iie.getArg(0).toString().toLowerCase().replaceAll("\"", "").equals("post"))
			{
				isGet = false;
			} else
				isGet = true;
		}
	}

	private void addPartfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm)
	{
		this.BodyTrackingReg = iie.getBase().toString();
//		if (iie.getArg(1) instanceof Constant )
//		{
//			ArrayList<BFNode> list = BFTtable.get(iie.getBase().toString());
//			BFNode bfn = new BFNode();
//			bfn.makeUrlBfn(iie.getArg(0).toString());
//			list.add(bfn);
//			AddStatement(iie, sm);
//		} else
//		{
			if (iie.getArg(0) instanceof Constant)
			{
				ArrayList<BFNode> list = BFTtable.get(iie.getBase().toString());
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(iie.getArg(0).toString());
				list.add(bfn);
				AddStatement(iie, sm);
			} else
			{
				// Tree tr = BFTtable.get(iie.getBase().toString());
				ArrayList<BFNode> list2 = BFTtable.get(iie.getArg(0).toString());
				BFTtable.put(iie.getBase().toString(), CopyList(list2));
				AddStatement(iie, sm);
			}
//		}
	}

	private void appendQueryParameter(InstanceInvokeExpr iie,HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm)
	{

		if (iie.getMethodRef().toString().equals("<android.net.Uri$Builder: android.net.Uri$Builder appendQueryParameter(java.lang.String,java.lang.String)>"))
		{
			// System.out.println("appendQueryParameter : " + iie.toString());
			// System.out.println("body tracking iie : " + BodyTrackingReg);
//			if (iie.getArg(1)  instanceof Constant )
//			{
//
//				ArrayList<BFNode> tr = BFTtable.get(TrackingReg);
//				if (tr == null)
//					return;
//				BFNode bfn = new BFNode();
//				bfn.makeUrlBfn("(&|\\?)" + iie.getArg(0).toString());
//				tr.add(bfn);
//				AddStatement(iie, sm);
//			} else
//			{
//				if (iie.getArg(0) instanceof Constant )
//				{
//					ArrayList<BFNode> tr = BFTtable.get(TrackingReg);
//					if (tr == null)
//						return;
//					BFNode bfn = new BFNode();
//					bfn.makeUrlBfn("(&|\\?)" + iie.getArg(0).toString());
//					tr.add(bfn);
//					AddStatement(iie, sm);
//				} else
//				{
//					// Tree tr = BFTtable.get(iie.getBase().toString());
//					ArrayList<BFNode> tr2 = BFTtable.get(TrackingReg);
//					BFTtable.put(iie.getBase().toString(), CopyList(tr2));
//					AddStatement(iie, sm);
//				}
//			}
		}
	}

	private void parsefuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm)
	{
		if (iie.getMethodRef().toString().equals("<android.net.Uri: android.net.Uri parse(java.lang.String)>"))
		{
			ArrayList<BFNode> tr = BFTtable.get(strDest);
			BFNode bfn = new BFNode();
			bfn.makeUrlBfn(iie.getArg(0).toString());
			tr.add(bfn);
			AddStatement(iie, sm);
			TrackingReg = strDest;
		}
	}

	private void parsefuncHandler(StaticInvokeExpr sie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm)
	{

		if (sie.getMethodRef().toString().equals("<android.net.Uri: android.net.Uri parse(java.lang.String)>"))
		{
			if(sie.getArg(0) instanceof Constant)
			{
				ArrayList<BFNode> tr = new ArrayList<BFNode>();
				BFNode bfn = new BFNode();
				bfn.setStringForUrl(sie.getArg(0).toString());
				tr.add(bfn);
				BFTtable.put(strDest, tr);
				
			}
			else
			{
				BFTtable.put(strDest, CopyList(BFTtable.get(sie.getArg(0).toString())));
				AddStatement(sie, sm);
			}
			TrackingReg = strDest;
		}
	}

	private void putfuncHandler(InstanceInvokeExpr iie,  HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm)
	{

		if (iie.getMethodRef().toString() == "<org.json.JSONObject: org.json.JSONObject put(java.lang.String,java.lang.Object)>")
		{
			ArrayList<BFNode> tr = BFTtable.get(iie.getBase().toString());
			// System.out.println("Tree variable : " +
			// iie.getBase().toString());
			if (iie.getArg(1)  instanceof Constant )
			{
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(iie.getArg(0).toString());
				tr.add(bfn);
				AddStatement(iie, sm);
			} else
			{
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(iie.getArg(0).toString());
				tr.add(bfn);
				AddStatement(iie, sm);
			}

		} else if (iie.getMethodRef().toString() == "<org.json.JSONObject: org.json.JSONObject put(java.lang.String,int)>")
		{
			ArrayList<BFNode> tr = BFTtable.get(iie.getBase().toString());
			BFNode bfn = new BFNode();
			bfn.makeUrlBfn(iie.getArg(0).toString());
			tr.add(bfn);
			AddStatement(iie, sm);
		} else if (iie.getMethodRef().toString() == "<org.json.JSONArray: org.json.JSONArray put(java.lang.Object)>")
		{
			ArrayList<BFNode> tr = BFTtable.get(iie.getBase().toString());
			BFNode bfn = new BFNode();
			if (iie.getArg(0)  instanceof Constant )
			{
				bfn.makeUrlBfn(iie.getArg(0).toString());
				tr.add(bfn);
				AddStatement(iie, sm);
			}
		}
	}

	private void addfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm)
	{
		if (iie.getMethodRef().toString() == "<java.util.List: boolean add(java.lang.Object)>")
		{
			String arg = iie.getArg(0).toString();
			String base = iie.getBase().toString();
			BFTtable.put(base, CopyList(BFTtable.get(arg)));
			AddStatement(iie, sm);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void appendfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm)
	{
		if (iie.getMethodRef().toString().equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>")
				|| iie.getMethodRef().toString().equals("<java.lang.StringBuffer: java.lang.StringBuffer append(java.lang.String)>"))
		{
			String base = iie.getBase().toString();
			
			ArrayList<BFNode> list = BFTtable.get(base);
			if (iie.getArg(0) instanceof Constant)
			{
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(iie.getArg(0).toString());
				list.add(bfn);
			} else
			{
				String arg = iie.getArg(0).toString();
				BFNode bfn = new BFNode();
				String r = GenRegex(null, BFTtable, arg);
				bfn.makeUrlBfn(r);
				list.add(bfn);
				
				// pairing
				ArrayList<PairingEPEntry> plist = pairingInfoTable.get(base);
				if(pairingInfoTable.get(arg) != null) {
					for(PairingEPEntry e : pairingInfoTable.get(arg)) {
						plist.add(e);
					}
				}
			}

			if(strDest != null) {
				BFTtable.put(strDest, BFTtable.get(base));
				pairingInfoTable.put(strDest, pairingInfoTable.get(base));
				
			}
		} else if (iie.getMethodRef().toString().equals("<java.lang.StringBuilder: java.lang.StringBuilder append(int)>")
				|| iie.getMethodRef().toString().equals("<java.lang.StringBuilder: java.lang.StringBuilder append(double)>"))
		{
			String base = iie.getBase().toString();
			
			ArrayList<BFNode> list = BFTtable.get(base);
			if (iie.getArg(0) instanceof Constant)
			{
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(iie.getArg(0).toString());
				list.add(bfn);
			} else
			{
				String arg = iie.getArg(0).toString();
				BFNode bfn = new BFNode();
				String r = GenRegex(null, BFTtable, arg);
				if(r.equals("")) {
					if (iie.getMethodRef().toString().equals("<java.lang.StringBuilder: java.lang.StringBuilder append(int)>"))
						r = "[0-9]*";
					else
						r = "(.*)";
				}
				bfn.makeUrlBfn(r);
				list.add(bfn);
			}

			if(strDest != null) {
				BFTtable.put(strDest, BFTtable.get(base));
				pairingInfoTable.put(strDest, pairingInfoTable.get(base));
			}
		}
	}

	private void initfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm) throws ExtractocolException
	{

		if (iie.getMethodRef().getSignature().toString().equals("<java.net.URL: void <init>(java.lang.String)>"))
		{
			String base = iie.getBase().toString();
			String arg = iie.getArg(0).toString();
			
			BFNode bfn = new BFNode();
			if (iie.getArg(0) instanceof Constant )
			{
				bfn.makeUrlBfn(arg);
				BFTtable.put(base, new ArrayList<BFNode>());
				BFTtable.get(base).add(bfn);
			} else
			{
				ArrayList<BFNode> argList = BFTtable.get(arg);
				BFTtable.put(base, CopyList(argList));
			}
			TrackingReg = base;
//			this.printUrl(BFTtable, sm, ut);

		} else if (iie.getMethodRef().getSignature().toString().equals("<org.apache.http.client.methods.HttpGet: void <init>(java.lang.String)>") ||
				iie.getMethodRef().getSignature().toString().equals("<org.apache.http.client.methods.HttpPost: void <init>(java.lang.String)>"))
		{
			
			if(iie.getMethodRef().getSignature().toString().equals("<org.apache.http.client.methods.HttpGet: void <init>(java.lang.String)>"))
				isGet = true;
			else
				isGet = false;
			
			AddStatement(iie, sm);

			String base = iie.getBase().toString();
			String arg = iie.getArg(0).toString();
			
			BFNode bfn = new BFNode();
			if (iie.getArg(0) instanceof Constant )
			{
				bfn.makeUrlBfn(arg);
				BFTtable.put(base, new ArrayList<BFNode>());
				BFTtable.get(base).add(bfn);
				// init handle needs to be done
				
				pairingInfoTable.put(base, new ArrayList<PairingEPEntry>());
				pairingInfoTable.get(base).add(new PairingEPEntry(sm.toString(), ut.toString()));
				
			} else
			{
				ArrayList<BFNode> argList = BFTtable.get(arg);
				BFTtable.put(base, CopyList(argList));
				// init handle needs to be done
				pairingInfoTable.put(base, CopyPairingList(pairingInfoTable.get(arg)));
			}
			TrackingReg = base;
//			this.printUrl(BFTtable, sm, ut);
			// Track =true;
		}

		else if (iie.getMethodRef().getSignature().toString().equals("<org.apache.http.message.BasicNameValuePair: void <init>(java.lang.String,java.lang.String)>"))
		{
			/* will do request body later - 20150912 hnamkoong
			// System.out.println("BasicNameValuePair : " + iie.toString());
			this.BodyTrackingReg = iie.getBase().toString();
			// System.out.println("body tracking iie : " + BodyTrackingReg);
			if (iie.getArg(1)  instanceof Constant )
			{
				ArrayList<BFNode> tr = BFTtable.get(iie.getBase().toString());
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(iie.getArg(0).toString().replaceAll("\"", ""));
				tr.add(bfn);
				AddStatement(iie, sm);
			} else
			{
				if (iie.getArg(0)  instanceof Constant )
				{
					ArrayList<BFNode> tr = BFTtable.get(iie.getBase().toString());
					BFNode bfn = new BFNode();
					bfn.makeUrlBfn(iie.getArg(0).toString().replaceAll("\"", ""));
					tr.add(bfn);
					AddStatement(iie, sm);
				} else
				{
					ArrayList<BFNode> tr2 = BFTtable.get(iie.getArg(0).toString());
					BFTtable.put(iie.getBase().toString(), CopyList(tr2));
					AddStatement(iie, sm);
				}
			}
			*/
		} else if (iie.getMethodRef().getSignature().toString().equals("<org.apache.http.client.entity.UrlEncodedFormEntity: void <init>(java.util.List)>"))
		{
			BFTtable.put(iie.getBase().toString(), CopyList(BFTtable.get(iie.getArg(0).toString())));
			AddStatement(iie, sm);
		} else if (iie.getMethodRef().toString().equals("<java.lang.StringBuilder: void <init>()>"))
		{
			String base = iie.getBase().toString();
			BFTtable.put(base, new ArrayList<BFNode>());
		} else if (iie.getMethodRef().getSignature().toString().equals("<java.lang.StringBuilder: void <init>(java.lang.String)>"))
		{
			String base = iie.getBase().toString();
			if (iie.getArg(0)  instanceof Constant)
			{
				ArrayList<BFNode> list = BFTtable.get(base);
				list.clear();
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(iie.getArg(0).toString().toString().replaceAll("\"", ""));
				list.add(bfn);

				// for pairing
				ArrayList<PairingEPEntry> plist = pairingInfoTable.get(base);
				if(plist == null) {
					plist = new ArrayList<PairingEPEntry>();
					pairingInfoTable.put(base, plist);
				}
				plist.clear();
				PairingEPEntry pep = new PairingEPEntry(sm.toString(), ut.toString());
				plist.add(pep);
			} else
			{
				ArrayList<BFNode> list = BFTtable.get(base);
				list.clear();
				ArrayList<BFNode> copyList = CopyList(BFTtable.get(iie.getArg(0).toString()));
				for(BFNode bfn : copyList) {
					list.add(bfn);
				}
				
				// for pairing
				ArrayList<PairingEPEntry> plist = pairingInfoTable.get(base);
				if(plist == null) {
					plist = new ArrayList<PairingEPEntry>();
					pairingInfoTable.put(base, plist);
				}
				plist.clear();
				PairingEPEntry pep = new PairingEPEntry(sm.toString(), ut.toString());
				plist.add(pep);
				
				if(pairingInfoTable.get(iie.getArg(0).toString()) != null) {
					for(PairingEPEntry e : pairingInfoTable.get(iie.getArg(0).toString())) {
						plist.add(e);
					}
				}
			}
		}
		else if(iie.getMethodRef().toString().equals("<android.content.Intent: void <init>(java.lang.String,android.net.Uri)>"))
		{
		
		// wrong semantic point - 20150914 hnamkoong
		// specialinvoke $r8.<android.content.Intent: void <init>(java.lang.String,android.net.Uri)>("android.intent.action.VIEW", $r4) //
			
//			TrackingReg = iie.getArg(1).toString();
//			this.printUrl(BFTtable, sm, ut);
		}
	}

	private void AddStatement(InvokeExpr iie, SootMethod sm)
	{
		//		if (!isMulti)
		//		{
		//			if (!pi.iscomplete)
		//			{
		//				// pi.addSTMT(iie.getMethodRef().toString());
		//				pi.getCallMethods().add(originalMethod.get(sm.getName()));
		//				pi.addSTMT(iie.getMethodRef().toString());
		//				pi.Methodinfo.inMethodName.add(originalMethod.get(sm.getName()));
		//			}
		//		} else
		//		{
		//
		//			// && !OriginDummyname.equals(sm.getName()) for radio-reddit remove
		//			if (!pi.iscomplete)
		//			{
		//				// pi.addSTMT(iie.getMethodRef().toString());
		//				pi.getCallMethods().add(originalMethod.get(sm.getName()));
		//				pi.addSTMT(iie.getMethodRef().toString());
		//				pi.Methodinfo.inMethodName.add(originalMethod.get(sm.getName()));
		//			}
		//		}
	}

	@Override
	public void ExtractingExpr(StaticInvokeExpr sie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, String name, SootMethod sm,
			String strDest, HashMap<String, ArrayList<PairingEPEntry>> _pairingInfoTable) throws ExtractocolException
	{
		pairingInfoTable = _pairingInfoTable;
		this.strDest = strDest;
		String strMethod = sie.getMethodRef().name();

		// Check method
		int chkMethod = methodlist.indexOf(strMethod);

		switch (chkMethod)
		{
			case MethodIds.retrieveStream :
				if (sie.getMethodRef().toString().indexOf("java.io.InputStream retrieveStream(java.lang.String)>") != -1)
				{
					this.printUrl(BFTtable, sm, ut);
				}
				break;
			case MethodIds.parse :
				parsefuncHandler(sie, BFTtable, ut, sm);
				break;
			case MethodIds.replace :
				ArrayList<BFNode> dtr = BFTtable.get(strDest);
				if (dtr != null)
				{
					dtr = CopyList(BFTtable.get(sie.getArg(0).toString()));
				}
				break;
			case MethodIds.format :
				// System.out.println(sie);
				if(sie.getMethodRef().toString().equals("<java.lang.String: java.lang.String format(java.util.Locale,java.lang.String,java.lang.Object[])>")) {
					if(strDest != null) {
						if(sie.getArgCount() == 3) {
							String arg1 = sie.getArg(1).toString();
							String arg2 = sie.getArg(2).toString();

							String[] var = new String[50];
							for(int i=0; i<50; i++) {
								String TrackingReg = arg1 + "[" + i + "]";
								String result = GenRegex(null, BFTtable, TrackingReg);
								if(result.equals(""))
									result = "(.*)";
								var[i] = result;
							}
							ArrayList<BFNode> list = new ArrayList<BFNode>();
							BFNode bfn = new BFNode();
							bfn.makeUrlBfn(String.format(arg1, var));
							list.add(bfn);
							BFTtable.put(strDest, list);
						}
					}
	
				}
				if (sie.getMethodRef().toString().equals("<java.lang.String: java.lang.String format(java.lang.String,java.lang.Object[])>")) {
					if(strDest != null) {
						if(sie.getArgCount() == 2) {
							String arg0 = sie.getArg(0).toString();
							String arg1 = sie.getArg(1).toString();

							String[] var = new String[50];
							for(int i=0; i<50; i++) {
								String TrackingReg = arg1 + "[" + i + "]";
								String result = GenRegex(null, BFTtable, TrackingReg);
								if(result.equals(""))
									result = "(.*)";
								var[i] = result;
							}
							ArrayList<BFNode> list = new ArrayList<BFNode>();
							BFNode bfn = new BFNode();
							bfn.makeUrlBfn(String.format(arg0, var));
							list.add(bfn);
							BFTtable.put(strDest, list);
						}
					}
//(					sie.get
//					System.out.println("aa");
				}
				
				
				
//				boolean format_3arg = false;
//				if (sie.getMethodRef().toString().equals("<java.lang.String: java.lang.String format(java.util.Locale,java.lang.String,java.lang.Object[])>"))
//					format_3arg = true;
//
//				if (sie.getMethodRef().toString().equals("<java.lang.String: java.lang.String format(java.lang.String,java.lang.Object[])>") || format_3arg)
//				{
//					ArrayList<BFNode> tr = BFTtable.get(strDest);
//					if (tr == null)
//					{
//						BFTtable.put(strDest, new ArrayList<BFNode>());
//						BFTtable.get(strDest).add(new BFNode());
//						tr = BFTtable.get(strDest);
//					}
//					ArrayList<BFNode> arg1 = null;
//					ArrayList<BFNode> arg2 = null;
//					if (format_3arg)
//					{
//						if (BFTtable.get(sie.getArg(1).toString()) != null)
//							arg1 = BFTtable.get(sie.getArg(1).toString());
//						else
//						{
//							BFNode bfn = new BFNode();
//							bfn.makeUrlBfn(sie.getArg(1).toString());
//							ArrayList<BFNode> dst = new ArrayList<BFNode>();
//							dst.add(new BFNode());
//							dst.add(bfn);
//							arg1 = dst;
//
//						}
//						arg2 = BFTtable.get(sie.getArg(2).toString());
//					} else
//					{
//						//IF arg0 constant
//						if(sie.getArg(0) instanceof Constant)
//						{
//							ArrayList<BFNode> list= new ArrayList<BFNode>();
//							BFNode bfn = new BFNode();
//							bfn.makeUrlBfn(sie.getArg(0).toString());
//							list.add(bfn);
//							arg1=list;
//						}
//						else
//						arg1 = BFTtable.get(sie.getArg(0).toString());
//						//IF arg0 constant
//						if(sie.getArg(1) instanceof Constant)
//						{
//							ArrayList<BFNode> list= new ArrayList<BFNode>();
//							BFNode bfn = new BFNode();
//							bfn.makeUrlBfn(sie.getArg(1).toString());
//							list.add(bfn);
//							arg2=list;
//						}
//						arg2 = BFTtable.get(sie.getArg(1).toString());
//					}
//					if (arg2 == null)
//						return;
//
//					if (arg1 != null)
//					{
//						for (Iterator<BFNode> iter = arg1.iterator(); iter.hasNext();)
//						{
//							BFNode bfn = iter.next();
//							if (bfn.getStringForUrl() != null && !bfn.getStringForUrl().equals(""))
//							{
//								String target = bfn.getStringForUrl();
//								bfn.makeUrlBfn(VirtualFormatFunction(target));
//							}
//						}
//					}
//
//					BFTtable.put(strDest, CopyList(arg1));
//				}
				break;
		}

		if(sie.getMethodRef().toString().contains("<com.github.kevinsawicki.http.HttpRequest")) {
			kevinsawickiHttpHandler(sie, BFTtable, ut, sm);
		}
		
		if(sie.getMethodRef().toString().contains("<android.net.Uri")) {
			androidNetUriHandler(sie, BFTtable, ut, sm);
		}
		
		if(sie.getMethodRef().toString().equals("<java.lang.String: java.lang.String valueOf(java.lang.Object)>")) {
			String arg0 = sie.getArg(0).toString();

			ArrayList<BFNode> list = BFTtable.get(arg0);
			BFTtable.put(this.strDest, CopyList(list));
		}
	}


	public String VirtualFormatFunction(String target)
	{

		target = target.replaceAll("%[0-9]*d", "[0-9]*");
		target = target.replaceAll("%[0-9]*s", ".*");

		return target;
	}
}
 
