package SignExtractor.ApplicationSpecific.pinterest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.gaurav.tree.NodeNotFoundException;
import com.gaurav.tree.Tree;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.Helper.FakeScene;
import Extractocol.BufferExtractor_Request.Helper.LinearSearchEntry;
import Extractocol.BufferExtractor_Request.Semantic.URL.UrlParser;
import emulator.EntrypointFinder;
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
import soot.jimple.Stmt;
import soot.jimple.infoflow.slice.AbstractSlice;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.source.ISourceSinkManager;
import soot.jimple.infoflow.util.SystemClassHandler;
import soot.jimple.parser.JimpleAST;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class pinterest_HeapHandler
{
	public static Hashtable<String, Set<String>> GlobalHeapTable = new Hashtable<String, Set<String>>();
	public static Set<String> isHeapObjectSearched = new HashSet<String>();
	
	
	// public static visit table
	
	public static void updateHeapTable(String SootValue, String value)
	{
		if(value == null || value.equals("") || value.equals("null"))
			return;
		value = value.replaceAll("\"", "");
		if(GlobalHeapTable.get(SootValue) == null) {
			GlobalHeapTable.put(SootValue, new HashSet<String>());
		}
		Set<String> stringSet = GlobalHeapTable.get(SootValue);
		stringSet.add(value);
	}

	public static void updateHeapTableWithSootValue(String leftSootValue, String rightSootValue)
	{
		if(GlobalHeapTable.get(leftSootValue) == null) {
			GlobalHeapTable.put(leftSootValue, new HashSet<String>());
		}
		if(!isHeapObjectSearched.contains(rightSootValue)) {
			linearSearch(rightSootValue);
			isHeapObjectSearched.add(rightSootValue);
		}
		Set<String> leftSet = GlobalHeapTable.get(leftSootValue);
		Set<String> rightSet = GlobalHeapTable.get(rightSootValue);
		leftSet.addAll(rightSet);
	}

	
	public static String getHeapObjectString(String SootValue)
	{
		
		if(!SootValue.equals("<com.pinterest.api.ApiHttpClient: java.lang.String baseApiUrl>"))
			return "(.*)";

		if(true)
			return "https://api.pinterest.com/v3/%s";
		
		String result = "";
		
		if(GlobalHeapTable.get(SootValue) == null) {
			GlobalHeapTable.put(SootValue, new HashSet<String>());
		}
		if(!isHeapObjectSearched.contains(SootValue)) {
			linearSearch(SootValue);
			isHeapObjectSearched.add(SootValue);
		}
		
		Set<String> stringSet = GlobalHeapTable.get(SootValue);
		if(stringSet == null)
			return result;

		// pinterest - only 1 heap
		// 20160107 hnamkoong
		for(String str : stringSet)
			return str;
					
		boolean orCheck = false;
		for(String str : stringSet) {
			if(str != null && !(str.equals(""))) {
				if(!result.equals("")) {
					result += " | ";
					orCheck = true;
				}
				result += str;
			}
		}
		if(orCheck)
			result = "(" + result + ")";
		if(result.equals("") || result.equals("null"))
			return "(.*)";
		
		return result;
	}
	
	// do linear search for SootValue
	// add only constant Strings to GlobalHeapTable
	private static void linearSearch(String SootValue)
	{
		if(!SootValue.contains("java.lang.String")) {
			// will only inspect method which returns string value
			return;
		}
		long start = System.currentTimeMillis();

		LinearSearchEntry LSE = findMethodfromHeapAllocStmt(SootValue);
		
		Set<String> stringSet = GlobalHeapTable.get(SootValue);
		
		for(Unit ut : LSE.constants) {
			AssignStmt as = (AssignStmt) ut;
			String cropLeftOp = CropVar(as.getLeftOp().toString());
			if(cropLeftOp.equals(SootValue) && !as.getRightOp().toString().equals("null")) {
				stringSet.add(as.getRightOp().toString().replaceAll("\"", ""));
			}
		}

		for(SootMethod sm : LSE.locals) {
//			UrlParser up = new UrlParser();
//
//			up.isForward = false;
//			Constants.targetheap=SootValue;
//			HashMap<String, ArrayList<BFNode>> BFTtable = new HashMap<String, ArrayList<BFNode>>();
//			up.onelevelheap(sm);
//			BFTtable.put("1", Constants.heapbft);
//			stringSet.add(up.GenRegex(null, BFTtable, "1"));
//			HeapObjectFinder HOF = new HeapObjectFinder(SootValue, sm);
//			stringSet.add(HOF.TargetC.toString());
			
//			AssignStmt as = (AssignStmt) ut;
//			String cropLeftOp = CropVar(as.getLeftOp().toString());
//			if(cropLeftOp.equals(SootValue)) {
//				if(as.getLeftOp().getType().equals("java.lang.Integer"))
//					stringSet.add("[0-9]*");
//				else
//					stringSet.add("(.*)");
//			}
		}
		long end = System.currentTimeMillis();
		System.out.format("\t\t Heap Search took %f - %s\n", (end - start) / 1000.0, SootValue);
	}
	

	//Linear Search for Constant String
	private static LinearSearchEntry findMethodfromHeapAllocStmt(String src)
	{
		LinearSearchEntry LSE = new LinearSearchEntry();
		if (Constants.serializationMode == true && Constants.fsc == null)
		{
			Constants.fsc = new FakeScene();
		}
		if (Constants.serializationMode == true)
		{
			for (SootClass sc : Constants.fsc.getClasses())
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
		for(int i=1; i<s.length; i++) {
			result += s[i];
		}
		return result;
	}
}

