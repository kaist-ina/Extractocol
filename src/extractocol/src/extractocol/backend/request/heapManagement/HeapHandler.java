
package extractocol.backend.request.heapManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.helper.LinearSearchEntry;
import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.JastAddJ.Variable;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.Stmt;
import soot.jimple.infoflow.util.SystemClassHandler;

public class HeapHandler
{

	public static Hashtable<String, Set<String>> GlobalHeapTable = new Hashtable<String, Set<String>>();

	public static Set<String> isHeapObjectSearched = new HashSet<String>();

	// for Volley Params
	public static HashMap<String, ArrayList<BFNode>> GlobalBFTtable = new HashMap<String, ArrayList<BFNode>>();

	public static ArrayList<String> OnceTaintValues = new ArrayList<String>();

	public static boolean requestUpdate = false;

	// This hashtable have a taint information in extractStmt method.
	public static Hashtable<String, String> OnceTaintTable = new Hashtable<String, String>();
	public static Hashtable<String,Hashtable<String, String>> RememberOnce = new Hashtable<String,Hashtable<String, String>>();

	// public static visit table

	public static void updateHeapTable(String SootValue, String value)
	{
		if (value == null || value.equals("") || value.equals("null"))
			return;
		value = value.replaceAll("\"", "");
		if (GlobalHeapTable.get(SootValue) == null)
		{
			GlobalHeapTable.put(SootValue, new HashSet<String>());
		}
		Set<String> stringSet = GlobalHeapTable.get(SootValue);
		stringSet.add(value);
	}

	// For Volley Request Params
	@SuppressWarnings("unchecked")
	public static void OnceTableUpdate(HashMap<String, ArrayList<BFNode>> bFTtable)
	{
		for (String key : OnceTaintTable.keySet())
		{
			String heapname = OnceTaintTable.get(key);

			if (heapname != null)
			{
				// GlobalBFTtable.put(heapname, bFTtable.get(key));
				ArrayList<BFNode> list = GlobalBFTtable.get(heapname) != null ? GlobalBFTtable.get(heapname) : new ArrayList<BFNode>();

				if (bFTtable.get(key) != null)
				{
					ArrayList<BFNode> srclist = (ArrayList<BFNode>) bFTtable.get(key).clone();

					for (BFNode bfn : srclist)
					{
						boolean isHas = false;
						for (BFNode src : list)
						{
							if (src.getStringForUrl() != null)
								if (src.getStringForUrl().equals(bfn.getStringForUrl()))
								{
									isHas = true;
								}
						}

						if (bfn.getStringForUrl() != null && !bfn.getStringForUrl().equals("(.*)") && !isHas)
						{

							BFNode newUrl = new BFNode();
							newUrl.setStringForUrl(bfn.getStringForUrl());
							list.add(newUrl);
						}
					}
					// GlobalBFTtable.put(heapname, CopyList(list));
				}
			}
		}
		return;
	}

	public static ArrayList<BFNode> CopyList(ArrayList<BFNode> src)
	{
		if (src == null)
			return null;
		ArrayList<BFNode> tlist = new ArrayList<BFNode>();

		for (BFNode bfn : src)
		{
			try
			{
				tlist.add((BFNode) bfn.clone());
			} catch (CloneNotSupportedException e)
			{
				e.printStackTrace();
			}
		}
		return tlist;
	}
	

	public static void updateHeapTableWithSootValue(String leftSootValue, String rightSootValue)
	{
		if (GlobalHeapTable.get(leftSootValue) == null)
		{
			GlobalHeapTable.put(leftSootValue, new HashSet<String>());
		}
		if (!isHeapObjectSearched.contains(rightSootValue))
		{
			linearSearch(rightSootValue);
			isHeapObjectSearched.add(rightSootValue);
		}
		Set<String> leftSet = GlobalHeapTable.get(leftSootValue);
		Set<String> rightSet = GlobalHeapTable.get(rightSootValue);
		leftSet.addAll(rightSet);
	}

	public static String getHeapObjectString(String SootValue)
	{
		if (SootValue.equals("<com.mobilemotion.dubsmash.utils.EndpointProvider: java.lang.String mTagBaseURL>"))
			return "https://tag-staging.dubsmash.com";
//		else if (SootValue.equals("<com.buzzfeed.android.data.AppData: java.lang.String BUZZFEED_CSRF_URL>"))
//			return "http://(www.buzzfeed.com|dev.buzzfeed.com|stage.buzzfeed.com|stage02.buzzfeed.com|qa01.buzzfeed.com|qa02.buzzfeed.com|qa03.buzzfeed.com|qa04.buzzfeed.com|qa05.buzzfeed.com)/bf2";
//		else if (SootValue.equals("<com.buzzfeed.android.data.AppData: java.lang.String BUZZFEED_URL>"))
//			return "http://((www.buzzfeed.com|dev.buzzfeed.com|stage.buzzfeed.com|stage02.buzzfeed.com|qa01.buzzfeed.com|qa02.buzzfeed.com|qa03.buzzfeed.com|qa04.buzzfeed.com|qa05.buzzfeed.com)|"
//					+ "(www.buzzfeed.com|dev.buzzfeed.com|stage.buzzfeed.com|stage02.buzzfeed.com|qa01.buzzfeed.com|qa02.buzzfeed.com|qa03.buzzfeed.com|qa04.buzzfeed.com|qa05.buzzfeed.com)/bf2)";
//		else if (SootValue.equals("<com.buzzfeed.spicerack.data.constant.Environment: java.lang.String BUZZ_URL>"))
//			return "(http://buzzfeed.com)|(http://stage.buzzfeed.com)|(http://qa01.buzzfeed.com)|(http://buzzfeed.com)|(http://dev.buzzfeed.com/bf2)";
//		else if (SootValue.equals("<com.buzzfeed.android.data.AppData: java.lang.String BUZZFEED_IMAGE_CONTRIBUTE_URL>"))
//			return "https://"
//					+ "((www.buzzfeed.com|dev.buzzfeed.com|stage.buzzfeed.com|stage02.buzzfeed.com|qa01.buzzfeed.com|qa02.buzzfeed.com|qa03.buzzfeed.com|qa04.buzzfeed.com|qa05.buzzfeed.com) | (www.buzzfeed.com|dev.buzzfeed.com|stage.buzzfeed.com|stage02.buzzfeed.com|qa01.buzzfeed.com|qa02.buzzfeed.com|qa03.buzzfeed.com|qa04.buzzfeed.com|qa05.buzzfeed.com)/bf2)"
////					+ "/bfcgi/_edit_photo_editor_image";
//		else if (SootValue.equals("<com.pinterest.api.ApiHttpClient: java.lang.String baseApiUrl>"))
//			return "https://api.pinterest.com/v3/%s";
//		else if (SootValue.equals("<com.pinterest.api.ApiHttpClient: java.lang.String baseLoggingApiUrl>"))
//			return "https://trk.pinterest.com/v3/%s";
//		else if (SootValue.equals("<com.pinterest.api.ApiHttpClient: java.lang.String baseLoggingApiUrlV4>"))
//			return "https://trk.pinterest.com/v4/%s";
		// else if (!SootValue
		// .equals("<com.pinterest.api.ApiHttpClient: java.lang.String
		// baseApiUrl>"))
		// return "(.*)";
//		else if (SootValue.equals("<com.insthub.fivemiles.Protocol.a: java.lang.String USER_LIKES>"))
//			return "/api/(v1|v2)/user_likes/";
		else if (HeapHandler.GlobalHeapTable.containsKey(SootValue))
		{
			String ret = "(";
			for (String a : HeapHandler.GlobalHeapTable.get(SootValue))
			{
				ret += a + "|";
			}
			ret = ret.substring(0, ret.length() - 1) + ")";

			//JM 4000
//			if(ret.length()>4000)
//				ret="(.*)";
//			System.out.println(SootValue + "=>" + ret);
			return ret;
		} else
			return SootValue;

		// JM 1 heap find code.
		// String result = "";
		//
		// if(GlobalHeapTable.get(SootValue) == null) {
		// GlobalHeapTable.put(SootValue, new HashSet<String>());
		// }
		// if(!isHeapObjectSearched.contains(SootValue)) {
		// linearSearch(SootValue);
		// isHeapObjectSearched.add(SootValue);
		// }
		//
		// Set<String> stringSet = GlobalHeapTable.get(SootValue);
		// if(stringSet == null)
		// return result;
		//
		// // pinterest - only 1 heap
		// // 20160107 hnamkoong
		// for(String str : stringSet)
		// return str;
		//
		// boolean orCheck = false;
		// for(String str : stringSet) {
		// if(str != null && !(str.equals(""))) {
		// if(!result.equals("")) {
		// result += " | ";
		// orCheck = true;
		// }
		// result += str;
		// }
		// }
		// if(orCheck)
		// result = "(" + result + ")";
		// if(result.equals("") || result.equals("null"))
		// return "(.*)";
		//
		// return result;

	}

	// do linear search for SootValue
	// add only constant Strings to GlobalHeapTable
	private static void linearSearch(String SootValue)
	{
		if (!SootValue.contains("java.lang.String"))
		{
			// will only inspect method which returns string value
			return;
		}
		long start = System.currentTimeMillis();

		LinearSearchEntry LSE = findMethodfromHeapAllocStmt(SootValue);

		Set<String> stringSet = GlobalHeapTable.get(SootValue);

		for (Unit ut : LSE.constants)
		{
			AssignStmt as = (AssignStmt) ut;
			String cropLeftOp = CropVar(as.getLeftOp().toString());
			if (cropLeftOp.equals(SootValue) && !as.getRightOp().toString().equals("null"))
			{
				stringSet.add(as.getRightOp().toString().replaceAll("\"", ""));
			}
		}

		for (SootMethod sm : LSE.locals)
		{
			// UrlParser up = new UrlParser();
			//
			// up.isForward = false;
			// Constants.targetheap=SootValue;
			// HashMap<String, ArrayList<BFNode>> BFTtable = new HashMap<String,
			// ArrayList<BFNode>>();
			// up.onelevelheap(sm);
			// BFTtable.put("1", Constants.heapbft);
			// stringSet.add(up.GenRegex(null, BFTtable, "1"));
			// HeapObjectFinder HOF = new HeapObjectFinder(SootValue, sm);
			// stringSet.add(HOF.TargetC.toString());

			// AssignStmt as = (AssignStmt) ut;
			// String cropLeftOp = CropVar(as.getLeftOp().toString());
			// if(cropLeftOp.equals(SootValue)) {
			// if(as.getLeftOp().getType().equals("java.lang.Integer"))
			// stringSet.add("[0-9]*");
			// else
			// stringSet.add("(.*)");
			// }
		}
		long end = System.currentTimeMillis();
		System.out.format("\t\t Heap Search took %f - %s\n", (end - start) / 1000.0, SootValue);
	}

	// Linear Search for Constant String
	private static LinearSearchEntry findMethodfromHeapAllocStmt(String src)
	{
		LinearSearchEntry LSE = new LinearSearchEntry();
		if (Constants.serializationMode == true)
		{
			for (SootClass sc : Scene.v().getClasses())
			{
				for (SootMethod sm : sc.getMethods())
				{
					if (SystemClassHandler.isClassInSystemPackage(sm.getDeclaringClass().getName()))
						continue;
					if (sm.getDeclaringClass().isPhantomClass())
						continue;
					if (sm.hasActiveBody())
					{
						Body body = sm.retrieveActiveBody();
						PatchingChain<Unit> units = body.getUnits();
						for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();)
						{
							final Unit unit = (Unit) iter.next();
							if (unit.toString().contains(src))
							{
								Stmt stmt = (Stmt) unit;
								if (stmt instanceof AssignStmt && ((AssignStmt) stmt).getLeftOp().toString().contains(src))
								{
									if (((AssignStmt) stmt).getRightOp() instanceof Constant)
									{
										LSE.constants.add(unit);
									} else if (((AssignStmt) stmt).getRightOp() instanceof Variable && sm.toString().contains("init"))
									{

									} else if (((AssignStmt) stmt).getRightOp() instanceof Local)
									{
										LSE.locals.add(sm);
									}
								}
							}
						}
					}
				}
			}
		} else
			for (SootClass sc : Scene.v().getClasses())
			{
				for (SootMethod sm : sc.getMethods())
				{
					if (SystemClassHandler.isClassInSystemPackage(sm.getDeclaringClass().getName()))
						continue;
					if (sm.getDeclaringClass().isPhantomClass())
						continue;
					if (sm.hasActiveBody())
					{
						Body body = sm.retrieveActiveBody();
						PatchingChain<Unit> units = body.getUnits();
						for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();)
						{
							final Unit unit = (Unit) iter.next();
							if (unit.toString().contains(src))
							{
								Stmt stmt = (Stmt) unit;
								if (stmt instanceof AssignStmt && ((AssignStmt) stmt).getLeftOp().toString().contains(src))
								{
									if (((AssignStmt) stmt).getRightOp() instanceof Constant)
									{
										LSE.constants.add(unit);
									} else if (((AssignStmt) stmt).getRightOp() instanceof Variable && sm.toString().contains("init"))
									{

									} else if (((AssignStmt) stmt).getRightOp() instanceof Local)
									{
										LSE.locals.add(sm);
									}
								}
							}
						}
					}
				}
			}
		return LSE;
	}

	private static String CropVar(String var)
	{
		String s[] = var.split("<");
		String result = "<";
		for (int i = 1; i < s.length; i++)
		{
			result += s[i];
		}
		return result;
	}

	public static void OnceTableClear()
	{
		OnceTaintTable.clear();
	}
}
