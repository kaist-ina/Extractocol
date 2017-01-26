
package Extractocol.BufferExtractor_Request;

/* BufferExtractor Abstract class */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.gaurav.tree.NodeNotFoundException;
import com.gaurav.tree.Tree;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Basics.ContextEntry;
import Extractocol.BufferExtractor_Request.Basics.ContextTable;
import Extractocol.BufferExtractor_Request.Basics.ParameterBucket;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapHandler;
import Extractocol.BufferExtractor_Request.HeapManagement.ValueEntryTracking;
import Extractocol.BufferExtractor_Request.Helper.ExtractocolLoopFinder;
import Extractocol.BufferExtractor_Request.Semantic.URL.UrlBuilder;
import Extractocol.BufferExtractor_Request.UnitHandle.AbstractUnitHandleDecorator;
import Extractocol.BufferExtractor_Request.UnitHandle.AssignStmtHandler;
import Extractocol.BufferExtractor_Request.UnitHandle.DebugDecorator;
import Extractocol.BufferExtractor_Request.UnitHandle.HeapTrackDecorator;
import Extractocol.BufferExtractor_Request.UnitHandle.IdentityStmtHandler;
import Extractocol.BufferExtractor_Request.UnitHandle.InvokeStmtHandler;
import Extractocol.BufferExtractor_Request.UnitHandle.ReturnStmtHandler;
import Extractocol.BufferExtractor_Request.UnitHandle.UnitType;
import Extractocol.Pairing.SemanticAppliedEntry;
import soot.Body;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;
import Extractocol.BufferExtractor_Response.UnitHandle.InvokeHandler;

public abstract class SignatureBuilder_Request
{

	public static List<String> SemanticModel;

	public List<String> methodlist;

	public static LinkedList<Object> BackwardRtnValueStack = new LinkedList<Object>();

	public HashMap<String, Integer[]> VisitTable = new HashMap<String, Integer[]>();

	// WOOMADE
	int nowdeep = 0;

	// hnamkoong - invoke type
	public static final int Invoke_Type_Assign_Static = 1;

	public static final int Invoke_Type_Assign_Virtual = 2;

	public static final int Invoke_Type_Assign_Interface = 3;

	public static final int Invoke_Type_Assign_Special = 4;

	public static final int Invoke_Type_Not_Assign_Static = 5;

	public static final int Invoke_Type_Not_Assign_Virtual = 6;

	public static final int Invoke_Type_Not_Assign_Interface = 7;

	public static final int Invoke_Type_Not_Assign_Special = 8;

	abstract public String GenRegex(HashMap<String, String> taintVariableMap, HashMap<String, ArrayList<BFNode>> BFTtable, String TrackingReg);

	abstract public String AnalyzeExpression(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod currentMethod,
			String strDst) throws NodeNotFoundException;

	abstract public void AnalyzeExpression(StaticInvokeExpr sie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod currentMethod,
			String strDst);

	abstract public boolean isContainDP(Block block);

	public void StartAnalyze(ParameterBucket pb) throws Exception
	{
		UrlBuilder up;
		up = new UrlBuilder();
		// Parse url about each paths
		if (!this.getClass().getName().equals("SignExtractor.HeapObjectFinder"))
		{
			up.parseUrl(pb);
		}
	}
	
	private InstanceInvokeExpr getInstanceInvokeExpr(Unit ut){
		if(ut instanceof AssignStmt)
			if( ((AssignStmt) ut).getInvokeExpr() instanceof InstanceInvokeExpr)
				return ((InstanceInvokeExpr) ((AssignStmt) ut).getInvokeExpr());
			else
				return null;
		else
			if(((InvokeStmt) ut).getInvokeExpr() instanceof InstanceInvokeExpr)
				return (InstanceInvokeExpr) ((InvokeStmt) ut).getInvokeExpr();
			else
				return null;
	}
	
	public String getBase(Unit ut){
		InstanceInvokeExpr iie = getInstanceInvokeExpr(ut);
		if(iie == null) 
			return null;
		
		return iie.getBase().toString();
	}

	public String getDeclaringClass(Unit ut){
		InstanceInvokeExpr iie = getInstanceInvokeExpr(ut);
		if(iie == null) 
			return null;
		
		return iie.getMethodRef().declaringClass().toString();
	}
	
	public String getMethodName(Unit ut){
		InstanceInvokeExpr iie = getInstanceInvokeExpr(ut);
		if(iie == null) 
			return null;
		
		return iie.getMethodRef().name().toString();
	}
	
	public Set<SootMethod> SearchMethod(int InvokeType, ParameterBucket pb, String MethodSig, Unit ut, SootMethod parentSm, HashMap<String, ArrayList<BFNode>> bFTtable)
	{
		Unit JimpleUnit = ut;
		Constants.searchmethodnum = Constants.searchmethodnum + 1;

		Set<SootMethod> SootMethodSet = new HashSet<SootMethod>();

		if (MethodSig.startsWith("<com.squareup.okhttp") || MethodSig.startsWith("<com.fasterxml.jackson")
				|| MethodSig.startsWith("<com.snappydb.DBFactory") || MethodSig.startsWith("<com.esotericsoftware.kryo.util")
				|| MethodSig.startsWith("<java.net.URI") || MethodSig.startsWith("<com.skyhookwireless.wps._sdkob")
				|| MethodSig.startsWith("<java.lang.") || SemanticModel.contains(MethodSig) || MethodSig.startsWith("<org.codehaus.jackson")
				|| MethodSig.startsWith("<okio.ByteString") || MethodSig.startsWith("<com.google.inject.Key") || MethodSig.startsWith("<com.b.a")
				|| MethodSig.startsWith("<org.bouncycastle"))
			return methodFilter(SootMethodSet);

		List<String> getCallees = null;
		
		getCallees = Constants.sCFG.getCalleesAt(parentSm.getSignature(), JimpleUnit.toString());

		if (getCallees == null)
			return SootMethodSet;

		for (String sig : getCallees)
		{
			if (Constants.searchMethodFilterUsingTaintFunction){
				if(Constants.TaintFunctions.contains(sig)){
					SootMethodSet.add(Constants.sCFG.getMethodOf(sig));
				}
			}else{
				SootMethodSet.add(Constants.sCFG.getMethodOf(sig));
			}
		}
		
		// BK Filters unnecessary invoking
		if(!isStaticInvoke(InvokeType)){
			// get the base variable of the statement (e.g., '$r0' is the base variable when $r1 = virtualinvoke $r0.<myClass: void myMethod()>)
			String base = getBase(ut);
			
			// get the declaringClass of the statement (e.g., 'myClass' is the declaring class when $r1 = virtualinvoke $r0.<myClass: void myMethod()>)
			String declaringClass = getDeclaringClass(ut);
			
			// get the list of types of the base variable
			ArrayList<String> typeNames = pb.varTable.getTypeof(base);
			
			if(typeNames != null)
				if(typeNames.size() > 0){
					boolean hasFound = false;
					
					// Check whether the declaring class is included in the type list
					for(String typeName : typeNames){
						if(declaringClass.equals(typeName)){
							hasFound = true;
							break;
						}
					}
					
					// If it is not, we can invoke only the specific method. (only one specified in the jimple code)
					if(!hasFound){
						String target = null;
						
						// Find the specific method
						for(String callee: getCallees){
							if(callee.contains(declaringClass)){
								target = callee;
								break;
							}
						}
						
						// Remove the other candidate methods except the specific one
						if(target != null){
							if (Constants.searchMethodFilterUsingTaintFunction){
								if(Constants.TaintFunctions.contains(target)){
									SootMethodSet.clear();
									SootMethodSet.add(Constants.sCFG.getMethodOf(target));
								}
							}else{
								SootMethodSet.clear();
								SootMethodSet.add(Constants.sCFG.getMethodOf(target));
							}
						}
					}
				}
		}
		
		// BK Do not need to consider superclasses when the method is <init> (superclass's <init> has been already included in the jimple code.)
		if(!isStaticInvoke(InvokeType))
			if(getMethodName(ut).equals("<init>"))
				return SootMethodSet;

		getCallees = Constants.sCFG.getCalleesAt(parentSm.getSignature(), JimpleUnit.toString());
		if (getCallees != null && getCallees.size() > 0)
		{
			for (String str : getCallees)
			{
				if (ut instanceof InvokeStmt)
				{
					InvokeStmt is = (InvokeStmt) ut;
					if (is.getInvokeExpr() instanceof InstanceInvokeExpr)
					{
						InstanceInvokeExpr iie = (InstanceInvokeExpr) is.getInvokeExpr();

						List<String> supclsses = Constants.sCFG.getSuperclassOf(iie.getMethodRef().declaringClass().toString());
						if (supclsses != null)
						{
							for (String superclss : supclsses)
							{
								int namespaceidx = iie.getMethodRef().getSignature().indexOf(":");

								String sig = "<" + superclss + ": " + iie.getMethodRef().getSignature().substring(namespaceidx + 2);
								if (Constants.sCFG.getMethodOf(sig) != null)
									SootMethodSet.add(Constants.sCFG.getMethodOf(sig));
							}
						}
					}
				} else if (ut instanceof AssignStmt)
				{
					AssignStmt as = (AssignStmt) ut;
					if (as.getInvokeExpr() instanceof InstanceInvokeExpr)
					{
						InstanceInvokeExpr iie = (InstanceInvokeExpr) as.getInvokeExpr();
						try
						{
							if (bFTtable.get(iie.getBase().toString()).size() > 0)
							{
								if (bFTtable.get(iie.getBase().toString()).get(0).getExtendedType() != null)
								{
									String ExtendedType = bFTtable.get(iie.getBase().toString()).get(0).getExtendedType();
									if (ExtendedType != null)
									{
										if (str.contains(ExtendedType))
										{
											SootMethodSet.add(Constants.sCFG.getMethodOf(str));
											return methodFilter(SootMethodSet);
										}
									}
								}
								// JM When this method is extended.
								else if (!SuperClassOnlyObject(iie))
								{
									List<String> supclsses = Constants.sCFG.getSuperclassOf(iie.getMethodRef().declaringClass().toString());

									for (String superclss : supclsses)
									{
										int namespaceidx = iie.getMethodRef().getSignature().indexOf(":");

										String sig = "<" + superclss + ": " + iie.getMethodRef().getSignature().substring(namespaceidx + 2);
										if (Constants.sCFG.getMethodOf(sig) != null)
											SootMethodSet.add(Constants.sCFG.getMethodOf(sig));
									}

									if (SootMethodSet.size() == 0)
									{
										if (Constants.sCFG.getMethodOf(iie.getMethodRef().getSignature()) != null)
											SootMethodSet.add(Constants.sCFG.getMethodOf(iie.getMethodRef().getSignature()));
										else
											SootMethodSet.add(Constants.sCFG.getMethodOf(str));
									}

									return methodFilter(SootMethodSet);
								}
								else if (SuperClassOnlyObject(iie))
								{
									//Check exist Subclasses;
									List<String> subclasses = Constants.sCFG.getSubclassOf(iie.getMethodRef().declaringClass().toString());
									for (String subclass : subclasses)
									{
										int namespaceidx = iie.getMethodRef().getSignature().indexOf(":");

										String sig = "<" + subclass + ": " + iie.getMethodRef().getSignature().substring(namespaceidx + 2);
										if (Constants.sCFG.getMethodOf(sig) != null)
											SootMethodSet.add(Constants.sCFG.getMethodOf(sig));
									}
								}
							}
						} catch (Exception e)
						{
							// JM extendedTyp is null!!
							// System.out.println("ExtendedType is null");
						}
						// SootMethodSet.add(sm);
					}
					// static invoke
					else if (as.getInvokeExpr() instanceof StaticInvokeExpr)
					{

						StaticInvokeExpr iie = (StaticInvokeExpr) as.getInvokeExpr();
						try
						{
							// JM When this method is extended.
							if (SootMethodSet.size() == 0)
							{
								if (Constants.sCFG.getMethodOf(iie.getMethodRef().getSignature()) != null)
									SootMethodSet.add(Constants.sCFG.getMethodOf(iie.getMethodRef().getSignature()));
								else
									SootMethodSet.add(Constants.sCFG.getMethodOf(str));
							}

							return methodFilter(SootMethodSet);
						} catch (Exception e)
						{
							// JM extendedTyp is null!!
							// System.out.println("ExtendedType is null");
							continue;
						}
					}
				}
			}
		} else if (getCallees == null)
		{
			// We should extended method
			if (ut instanceof AssignStmt)
			{
				AssignStmt as = (AssignStmt) ut;
				if (Constants.sCFG.getMethodOf(as.getInvokeExpr().getMethodRef().getSignature()) != null)
				{
					SootMethodSet.add(Constants.sCFG.getMethodOf(as.getInvokeExpr().getMethodRef().getSignature()));
					return methodFilter(SootMethodSet);
				}
			}
		}
		
		
				
		return SootMethodSet;
	}
	
	private boolean SuperClassOnlyObject(InstanceInvokeExpr iie)
	{
		if (Constants.sCFG.getSuperclassOf(iie.getMethodRef().declaringClass().toString()).size() == 1 && Constants.sCFG.getSuperclassOf(iie.getMethodRef().declaringClass().toString()).get(0).equals("java.lang.Object"))
			return true;
		else
			return false;
	}

	private Set<SootMethod> methodFilter(Set<SootMethod> sootMethodSet)
	{
		for (String filter : Constants.searchMethodFilter)
		{
			for (SootMethod sm : sootMethodSet)
			{
				if (sm != null)
				{
					if (sm.getSignature().equals(filter) || sm.getSignature().startsWith("<android.")
							|| sm.getSignature().startsWith("<com.github.kevinsawicki.http.HttpRequest:"))
					{
						sootMethodSet.remove(sm);
						break;
					}
				}
			}
		}
		return sootMethodSet;
	}

	public SootMethod SearchdoStartfunc(SootClass sc, String target)
	{
		for (SootMethod sm : sc.getMethods())
		{
			if (sm.getName().equals(target))
			{
				return sm;
			}
		}
		return sc.getMethods().iterator().next();
	}

	public boolean linearSearch(Set<String> MethodList, String Cmname)
	{
		if (MethodList.contains(Cmname))
			return true;
		else
			return false;
	}

	private void CalcEdgeTable(Integer[][] edgeTable, BriefBlockGraph bg2)
	{
		for (Block b : bg2)
		{
			for (Block outedge : bg2.getSuccsOf(b))
			{
				edgeTable[bg2.getBlocks().indexOf(b)][bg2.getBlocks().indexOf(outedge)] = 1;
			}
		}
	}

	private Integer[][] ManipulateBackEdge(Integer[][] edgeTable, BriefBlockGraph bg)
	{
		Integer[][] rtn = new Integer[bg.getBlocks().size()][bg.getBlocks().size()];

		for (int i = 0; i < bg.size(); i++)
		{
			for (int j = 0; j < bg.size(); j++)
			{
				rtn[i][j] = 0;
			}
		}

		for (int i = 0; i < bg.getBlocks().size(); i++)
		{
			for (int j = 0; j < bg.getBlocks().size(); j++)
			{
				if (isBackEdge(bg, i, j))
				{

					for (int k = 0; k < bg.getBlocks().size(); k++)
					{
						if (edgeTable[j][k] == 1 && k != i)
						{
							edgeTable[i][k] = 1;
							rtn[i][k] = 1;
							break;
						}
					}
					edgeTable[i][j] = 0;
				}
			}
		}
		return rtn;
	}

	private HashMap<String, ArrayList<BFNode>> MergeBFTtables(HashMap<String, HashMap<String, ArrayList<BFNode>>> BFTtableMap,
			Set<String> blockNumberSet)
	{

		ArrayList<String> HashMapKey = new ArrayList<String>();
		ArrayList<HashMap<String, ArrayList<BFNode>>> BFTarray = new ArrayList<HashMap<String, ArrayList<BFNode>>>();
		HashMap<String, Set<BFNode>> filterTable = new HashMap<String, Set<BFNode>>();
		HashMap<String, ArrayList<BFNode>> mergedBFTtable = new HashMap<String, ArrayList<BFNode>>();

		int BFTarraynum = 0;
		// put everything into filter table
		for (String blockNum : blockNumberSet)
		{

			HashMap<String, ArrayList<BFNode>> BFTtable = BFTtableMap.get(blockNum);
			BFTarray.add(BFTtable);

			String[] Keyset = BFTtable.keySet().toArray(new String[BFTtable.keySet().size()]);
			Arrays.sort(Keyset);
			for (String key : Keyset)
			{
				if (filterTable.get(key) == null)
				{
					filterTable.put(key, new HashSet<BFNode>());
				}

				if (BFTtable.get(key) != null)
				{

					if (BFTtable.get(key).size() == 0)
					{
						BFNode bbfn = new BFNode();
						bbfn.makeUrlBfn("");
						filterTable.get(key).add(bbfn);
					}

					if (BFTtable.get(key).size() > 0)
					{
						if (BFTtable.get(key).get(0).hasVtype())
							if (BFTtable.get(key).get(0).getVtype().equals("HashMap")
									|| BFTtable.get(key).get(0).getVtype().equals("BasicNameValuePair")
									|| BFTtable.get(key).get(0).getVtype().equals("Map")
									|| BFTtable.get(key).get(0).getVtype().equals("oncetainttable"))

							{
								HashMapKey.add(key);
								mergedBFTtable.put(key, BFTtable.get(key));
								continue;
							}

						// ERROR CAN BE OCCURED HERE
						// woohyun version
						BFNode tempbfn = BFTtable.get(key).get(0);
						// tempbfn.setStringForUrl(GenRegex(null, BFTtable,
						// key));
						tempbfn.blocknum = BFTarraynum;

						filterTable.get(key).add(tempbfn);

					}
				}
			}
			BFTarraynum++;
		}

		String[] Keyset = filterTable.keySet().toArray(new String[filterTable.keySet().size()]);
		Arrays.sort(Keyset);
		
		for (String key : Keyset)
		{
			if (!HashMapKey.contains(key))
			{
				String result = "";
				ArrayList<String> orarray = new ArrayList<String>();
				Set<String> orset = new HashSet<String>();
				boolean isArraycase = false;

				// 1,2,3,4 -> ArrayList<String>
				// 4,5,6 -> ArrayList<String>
				// ------
				// 1|4,2|5,3|6,4 -> String []
				ArrayList<ArrayList<String>> arraystring = new ArrayList<ArrayList<String>>();

				HashSet<String> nodeSet = new HashSet<String>();

				int size = 0;
				int i = 0;
				for (BFNode bfnnode : filterTable.get(key))
				{
					if (BFNode.isArray(bfnnode))
					{

						isArraycase = true;

						// invalid array
						if (bfnnode.getSize() <= 0)
						{
							// donothing
						} else
						{
							// orcase add
							for (String orstring : bfnnode.getArrayorcase())
								orset.add(orstring);

							String[] currentarray = bfnnode.getStringForUrl().split("###");
							if (size < currentarray.length - 1)
								size = currentarray.length - 1;
							arraystring.add(new ArrayList<String>());
							ArrayList<String> targetarray = arraystring.get(i++);

							for (int j = 1; j < currentarray.length; j++)
							{
								targetarray.add(currentarray[j]);
							}
						}

					} else
					{
						String node = GenRegex(null, BFTarray.get(bfnnode.blocknum), key);
						// if(node.length()>2000)
						// node="(.*)";

						if (node != null && !node.equals(""))
						{
							nodeSet.add(node);
						}
					}

				}

				if (nodeSet.size() == 0)
				{
					mergedBFTtable.put(key, new ArrayList<BFNode>());
				} else
				{
					int nodenum = 0;
					
					String[] SortedNodes = nodeSet.toArray(new String[nodeSet.size()]);
					Arrays.sort(SortedNodes);
					
					for (String node : SortedNodes)
					{
						nodenum++;
						if (nodenum == 1)
							result += "(" + node + ")";
						else
							result += "|" + "(" + node + ")";
					}
					if (nodenum == 1)
					{
						result = result.substring(1, result.length() - 1);
					}

					ArrayList<BFNode> list = new ArrayList<BFNode>();
					BFNode bfn = new BFNode();
					bfn.makeUrlBfn(result);
					list.add(bfn);
					mergedBFTtable.put(key, list);

				}

				// arraycase cover --woohyun 20160415
				// horizantal
				if (isArraycase)
				{
					for (int l = 0; l < size; l++)
					{
						Set<String> verticalset = new HashSet<String>();
						// get vertical or
						for (int k = 0; k < arraystring.size(); k++)
						{
							if (arraystring.get(k).size() > l)
								verticalset.add(arraystring.get(k).get(l));
						}

						String verticalresult = "";
						boolean isIterate = false;
						for (String iterator : verticalset)
						{
							verticalresult += iterator + "|";
							isIterate = true;
						}

						if (!isIterate)
							verticalresult = "(" + verticalresult.substring(0, verticalresult.length() - 1) + ")";
						else
							verticalresult = verticalresult.substring(0, verticalresult.length() - 1);

						result += "###" + verticalresult;

					}

					for (String or : orset)
						orarray.add(or);

					ArrayList<BFNode> list = new ArrayList<BFNode>();
					BFNode bfn = new BFNode();

					// empty array
					if (arraystring.size() == 0)
					{
						bfn.setrandomarray();
					}

					else
					{
						bfn.initarray(size);
						bfn.setStringForUrl(result);
						bfn.setArrayorcase(orarray);
					}
					
					
					list.add(bfn);
					mergedBFTtable.put(key, list);
				}
			}
		}

		// handle reference problem 20150915
		HashMap<String, Set<String>> pairSetMap = new HashMap<String, Set<String>>();
		Set<String> allPair = new HashSet<String>();

		for (String blockNum : blockNumberSet)
		{
			HashMap<String, ArrayList<BFNode>> BFTtable = BFTtableMap.get(blockNum);
			Set<String> pairSet = new HashSet<String>();
			
			String[] Keyset1 = BFTtable.keySet().toArray(new String[BFTtable.keySet().size()]);
			Arrays.sort(Keyset1);
			
			String[] Keyset2 = BFTtable.keySet().toArray(new String[BFTtable.keySet().size()]);
			Arrays.sort(Keyset2);
			
			for (String key1 : Keyset1)
			{
				for (String key2 : Keyset2)
				{
					if (key1 == null)
					{
						continue;
					}
					if (key2 == null)
					{
						continue;
					}
					if (!key1.equals(key2))
					{
						if (BFTtable.get(key1) == BFTtable.get(key2))
						{
							pairSet.add(key1 + "," + key2);
							allPair.add(key1 + "," + key2);
						}
					}
				}
			}
			pairSetMap.put(blockNum, pairSet);
		}

		for (String pair : allPair)
		{
			String key1 = pair.split(",")[0];
			String key2 = pair.split(",")[1];
			boolean sameReference = true;
			for (String blockNum : blockNumberSet)
			{

				HashMap<String, ArrayList<BFNode>> BFTtable = BFTtableMap.get(blockNum);
				Set<String> pairSet = pairSetMap.get(blockNum);
				if (!BFTtable.containsKey(key1) && !BFTtable.containsKey(key2))
				{
					continue;
				}
				if (pairSet.contains(pair))
				{
					continue;
				}
				sameReference = false;
			}
			if (sameReference)
			{
				mergedBFTtable.put(key1, mergedBFTtable.get(key2));
			}
		}
		return mergedBFTtable;
	}

	/*
	 * MethodName : CreateBlockGraphAndInitEdgeTables Creator : Jeongmin Kim and
	 * Kunghun Nam Description : Build BlockGraph for a SootMethod and
	 * Initialize Edgetables for considering control blocks.
	 */
	public void CreateBlockGraphAndInitEdgeTables(ParameterBucket pb) throws java.lang.Exception
	{
		try
		{
			BackwardRtnValueStack.addLast(null);
			if (pb.CurrentSM == null)
				Constants.stacktrace.add(null);
			else
				Constants.stacktrace.add(pb.CurrentSM.toString());
			
			if (Constants.foundDPStmt || Constants.stopVisitMethod())
				return;

			if (pb.CurrentSM == null)
			{
				if (Constants.isDebug)
					System.out.println("Current Sm is null!");
				return;
			}
			if (!pb.CurrentSM.hasActiveBody())
			{
				return;
			}

			Constants.methodVisitCount++;
			if (Constants.methodVisitCount % 30 == 0)
			{
				if (Constants.isDebug)
					System.out.println("Method Count : " + Constants.methodVisitCount + "..");
			}

			Body b = pb.CurrentSM.getActiveBody();
			HeapHandler.RememberOnce.put(pb.CurrentSM.toString(), new Hashtable<String, String>());

			BriefBlockGraph Bg = new BriefBlockGraph(b);
			Block bl = Bg.getHeads().get(0);
			Integer[] VisitEntry = new Integer[Bg.size()];
			Integer[][] original_EdgeTable = new Integer[Bg.size()][Bg.size()];
			Integer[][] EdgeEntry = new Integer[Bg.size()][Bg.size()];

			Arrays.fill(VisitEntry, 0);
			for (int i = 0; i < Bg.size(); i++)
			{
				for (int j = 0; j < Bg.size(); j++)
				{
					original_EdgeTable[i][j] = 0;
					EdgeEntry[i][j] = 0;
				}
			}

			boolean[] isBlockContainDP = new boolean[Bg.size()];
			for (int i = 0; i < Bg.size(); i++)
			{
				isBlockContainDP[i] = this.isContainDP(Bg.getBlocks().get(i));
			}

			VisitEntry[Bg.getBlocks().indexOf(bl)] = 1;
			if (VisitTable.containsKey(pb.CurrentSM.getSignature()))
			{
				Constants.alreadyVisited = true;
				return;
			}
			VisitTable.put(pb.CurrentSM.getSignature(), VisitEntry);

			HashMap<String, HashMap<String, ArrayList<BFNode>>> BFTtableMap = new HashMap<String, HashMap<String, ArrayList<BFNode>>>();
			CalcEdgeTable(EdgeEntry, Bg);
			CalcEdgeTable(original_EdgeTable, Bg);

			Integer[][] maniEdge = ManipulateBackEdge(original_EdgeTable, Bg);
			ManipulateBackEdge(EdgeEntry, Bg);

			LinkedList<Block> BFSentry = new LinkedList<Block>();
			BFSentry.add(bl);

			while (!BFSentry.isEmpty())
			{
				Block nextBlock = null;

				for (Block block : BFSentry)
				{
					int blockNumber = Bg.getBlocks().indexOf(block);

					if (!isManipulatedEdge(maniEdge, blockNumber, Bg.size()) && BFSentry.size() > 1)
					{
						nextBlock = block;
						break;
					} else if (isBlockContainDP[blockNumber] == false)
					{
						nextBlock = block;
					}
				}
				if (nextBlock == null)
					nextBlock = BFSentry.getLast();

				int blockNumber = Bg.getBlocks().indexOf(nextBlock);

				HashMap<String, ArrayList<BFNode>> blockBFTtable;

				if (bl.equals(nextBlock))
				{
					blockBFTtable = new HashMap<String, ArrayList<BFNode>>(pb.BFTtable);
				} else
				{
					Set<String> blockNumberSet = new HashSet<String>();
					for (Block block : Bg.getBlocks())
					{
						int checkBlockNumber = Bg.getBlocks().indexOf(block);
						if (original_EdgeTable[checkBlockNumber][blockNumber] == 1)
						{
							String blockNumString = String.valueOf(checkBlockNumber);
							HashMap<String, ArrayList<BFNode>> fromBFTtable = BFTtableMap.get(blockNumString);
							if (fromBFTtable != null)
							{
								blockNumberSet.add(blockNumString);
							}
						}
					}
					blockBFTtable = MergeBFTtables(BFTtableMap, blockNumberSet);
				}

				BFTtableMap.put(String.valueOf(blockNumber), blockBFTtable);

				// add BlockGrpahTraveler Param to ParameterBucket Class

				pb.BFSentry = BFSentry;
				pb.BFTtableMap = BFTtableMap;
				pb.Bg = Bg;
				pb.nextBlock = nextBlock;
				pb.EdgeEntry = EdgeEntry;
				pb.maniEdge = maniEdge;
				pb.blockBFTtable = blockBFTtable;
				pb.VisitEntry = VisitEntry;
				
				AnalyzeEachStmts(pb);
				BFSentry.remove(nextBlock);
			}

			VisitTable.remove(pb.CurrentSM.getSignature().toString());
			HeapHandler.RememberOnce.remove(pb.CurrentSM.toString());

			// stack trace
			if (pb.CurrentSM.toString().equals(Constants.stacktrace.get(Constants.stacktrace.size() - 1)))
				Constants.stacktrace.remove(Constants.stacktrace.size() - 1);
			else
			{
				while (!pb.CurrentSM.toString().equals(Constants.stacktrace.get(Constants.stacktrace.size() - 1)))
					Constants.stacktrace.remove(Constants.stacktrace.size() - 1);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private boolean isManipulatedEdge(Integer[][] maniEdge, int blockNumber, int bgsize)
	{
		for (int i = 0; i < bgsize; i++)
		{
			if (maniEdge[i][blockNumber] == 1)
				return true;
		}
		return false;
	}

	/*
	 * MethodName : AnalyzeEachStmts Creator : Jeongmin Kim and Kunghun Nam
	 * Description : Analyze Each Stmts and Jump to other method.
	 */
	private void AnalyzeEachStmts(ParameterBucket pb) throws Exception
	{
		Body body = pb.Bg.getBody();
		
		for (Iterator<Unit> UnitintheBlock = pb.nextBlock.iterator(); UnitintheBlock.hasNext();)
		{
			if (Constants.foundDPStmt || Constants.stopVisitMethod())
				return;

			Unit ut = UnitintheBlock.next();
			ArrayList<String> oncetaintkeys = new ArrayList<String>();
			if (pb.blockBFTtable.get(null) != null)
				pb.blockBFTtable.remove(null);

			for (String bftkeys : pb.blockBFTtable.keySet())
			{
				if (bftkeys.startsWith("oncetaint_"))
					oncetaintkeys.add(bftkeys.split("oncetaint_")[1]);
			}

			// remember oncetable
			for (String oncekeys : oncetaintkeys)
			{
				HeapHandler.RememberOnce.get(pb.CurrentSM.toString()).put(oncekeys, GenRegex(null, pb.blockBFTtable, oncekeys));
				if (ut instanceof AssignStmt)
				{
					AssignStmt as = (AssignStmt) ut;
					if (as.getLeftOp().toString().equals(oncekeys) && !as.getRightOp().toString().contains(oncekeys))
					{
						HeapHandler.RememberOnce.get(pb.CurrentSM.toString()).remove(oncekeys);
						if (pb.blockBFTtable.get("oncetaint_" + oncekeys) != null)
							pb.blockBFTtable.remove("oncetaint_" + oncekeys);
					}
				}
			}

			// Analyze Each Units
			int HandlerId = SelectHandler(ut);
			AbstractUnitHandleDecorator auhd;
			if (!Constants.isDebug)
				auhd = new HeapTrackDecorator();
			else
				auhd = new DebugDecorator();
			switch (HandlerId)
			{
				case 0:
					auhd.SetTheUnitHandler(new AssignStmtHandler());
					break;
				case 1:
					auhd.SetTheUnitHandler(new InvokeStmtHandler());
					break;
				case 2:
					auhd.SetTheUnitHandler(new ReturnStmtHandler());
					break;
				case 3:
					auhd.SetTheUnitHandler(new IdentityStmtHandler());
					break;
			}
			
			auhd.HandleUnit(ut, pb, this);

			// JM In this point, handling iteration.
			if (pb.CurrentSM.getName().equals("getUrlWithQueryString"))
				MergingIteration(body, ut, pb.BFTtableMap, pb.Bg);

			// JM In this point, Extractocol handle GlobalBFTTable.
 			if (HeapHandler.requestUpdate)
			{
				HeapHandler.OnceTableUpdate(pb.blockBFTtable);
				HeapHandler.requestUpdate = false;
			}

			// JM Please Refactoring - compare rememberonce
			for (String oncekeys : HeapHandler.RememberOnce.get(pb.CurrentSM.toString()).keySet())
			{
				if (!HeapHandler.RememberOnce.get(pb.CurrentSM.toString()).get(oncekeys).equals(GenRegex(null, pb.blockBFTtable, oncekeys))
						&& !GenRegex(null, pb.blockBFTtable, oncekeys).equals(""))
					HeapHandler.GlobalBFTtable.put(HeapHandler.OnceTaintTable.get(oncekeys), pb.blockBFTtable.get(oncekeys));
			}
		}
		FinishAnalyzingBlockAndSelectNextBlock(pb);
	}

	private void FinishAnalyzingBlockAndSelectNextBlock(ParameterBucket pb)
	{
		for (Block tg : pb.Bg.getSuccsOf(pb.nextBlock))
		{
			pb.EdgeEntry[pb.Bg.getBlocks().indexOf(pb.nextBlock)][pb.Bg.getBlocks().indexOf(tg)] = 0;
		}

		for (Block b2 : pb.Bg.getBlocks())
		{
			if (pb.VisitEntry[pb.Bg.getBlocks().indexOf(b2)] == 0)
			{
				if (BlockSelector(pb.Bg.getBlocks().indexOf(b2), pb.Bg, pb.EdgeEntry, pb.maniEdge))
				{
					pb.BFSentry.add(b2);
					pb.VisitEntry[pb.Bg.getBlocks().indexOf(b2)] = 1;
				}
			}
		}
	}

	private int SelectHandler(Unit ut)
	{
		if (ut instanceof AssignStmt)
			return UnitType.AssignStmt;
		else if (ut instanceof InvokeStmt)
			return UnitType.InvokeStmt;
		else if (ut instanceof ReturnStmt)
			return UnitType.ReturnStmt;
		else if (ut instanceof IdentityStmt)
			return UnitType.IdentityStmt;
		else
			return -1;
	}

	public void AddContextTable(SootMethod parentSm, AssignStmt as)
	{
		// JM Auto-generated method stub

		ContextEntry te = null;

		if (as.containsInvokeExpr() && !(as.getInvokeExpr() instanceof InterfaceInvokeExpr))	
		{
			InvokeExpr ie = as.getInvokeExpr();
			SootMethod InvokedSm = Constants.sCFG.getMethodOf(ie.getMethodRef().getSignature());

			if (InvokedSm == null || !InvokedSm.hasActiveBody())
				return;

			Type Rt = InvokedSm.getReturnType();

			if (Rt.equals(as.getLeftOp().getType()))
			{
				ContextTable.addKey(Rt.toString());

				te = new ContextEntry(parentSm, as.getLeftOp().toString(), as.getLeftOp().getType().toString());
				ContextTable.add(Rt.toString(), te);
			}

			// Method Args
			if (InvokedSm.getParameterCount() > 0)
			{
				if (ie instanceof InstanceInvokeExpr)
				{
					InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
					int paramcount = 0;
					for (Value arg : iie.getArgs())
					{
						String param = "@parameter" + paramcount++;
						te = new ContextEntry(parentSm, arg.toString(), arg.getType().toString());
						String KeyintheMap = ContextTable.findkey(te);
						te = new ContextEntry(Constants.sCFG.getMethodOf(ie.getMethodRef().getSignature()), param.toString(),
								arg.getType().toString());
						if (KeyintheMap != null)
							ContextTable.add(KeyintheMap, te);
					}
				} else if (ie instanceof StaticInvokeExpr)
				{
					StaticInvokeExpr sie = (StaticInvokeExpr) ie;
					int paramcount = 0;
					for (Value arg : sie.getArgs())
					{
						String param = "@parameter" + paramcount++;
						te = new ContextEntry(parentSm, arg.toString(), arg.getType().toString());
						String KeyintheMap = ContextTable.findkey(te);
						te = new ContextEntry(Constants.sCFG.getMethodOf(ie.getMethodRef().getSignature()), param.toString(),
								arg.getType().toString());
						if (KeyintheMap != null)
							ContextTable.add(KeyintheMap, te);
					}
				}
			}
		} else
		{
			if (!(as.getRightOp() instanceof Constant))
			{
				te = new ContextEntry(parentSm, as.getRightOp().toString(), as.getRightOp().getType().toString());
				String TaintKey = ContextTable.findkey(te);

				if (TaintKey != null)
				{
					// whether rightop is removing or not.
					te = new ContextEntry(parentSm, as.getLeftOp().toString(), as.getLeftOp().getType().toString());
					String LTaintKey = ContextTable.findkey(te);

					if (LTaintKey != null)
						ContextTable.remove(LTaintKey, te);

					ContextTable.add(TaintKey, te);
				} else
				{
					if (as.getRightOp().toString().startsWith("new "))
					{
						String RightOp = as.getRightOp().toString().substring(as.getRightOp().toString().indexOf(" ") + 1);
						ContextTable.addKey(RightOp);
						te = new ContextEntry(parentSm, as.getLeftOp().toString(), as.getLeftOp().getType().toString());
						ContextTable.add(RightOp, te);
					}
				}
			}
		}
	}

	public boolean isHashMap(HashMap<String, ArrayList<BFNode>> BFTtable, String key)
	{
		if (BFTtable.get(key) != null)
			if (BFTtable.get(key).size() > 0)
				if (BFTtable.get(key).get(0).hasVtype())
					if (BFTtable.get(key).get(0).getVtype().contains("HashMap") || BFTtable.get(key).get(0).getVtype().contains("BasicNameValuePair"))
						return true;

		return false;

	}

	private void MergingIteration(Body body, Unit ut, HashMap<String, HashMap<String, ArrayList<BFNode>>> bFTtableMap, BriefBlockGraph bg)
	{
		ExtractocolLoopFinder loopFinder = new ExtractocolLoopFinder();
		Set<List<Stmt>> loops = loopFinder.getLoops(body);
		int LastBlock, FirstBlock;

		for (List<Stmt> lp : loops)
		{
			// It's last stmt in this block
			if (lp.get(lp.size() - 2).equals(ut))
			{
				FirstBlock = getBlockNum(bg, lp.get(0));
				bFTtableMap.get(String.valueOf(FirstBlock));
				LastBlock = getBlockNum(bg, ut);
				bFTtableMap.get(String.valueOf(LastBlock));
			}
		}
	}

	private int getBlockNum(BriefBlockGraph Bg, Unit ut)
	{
		for (Block b : Bg.getBlocks())
		{

			Iterator<Unit> IterUnit = b.iterator();

			while (IterUnit.hasNext())
			{
				Unit in = IterUnit.next();
				if (in.equals(ut))
				{
					return Bg.getBlocks().indexOf(b);
				}
			}
		}
		return -1;
	}

	private boolean isAssignStmt(int invokeType)
	{
		if (Invoke_Type_Assign_Static <= invokeType && invokeType <= Invoke_Type_Assign_Special)
			return true;
		return false;
	}

	protected boolean isStaticInvoke(int invokeType)
	{
		if (invokeType == Invoke_Type_Assign_Static || invokeType == Invoke_Type_Not_Assign_Static)
			return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	public void DetermineJumpOtherMethod(int invokeType, InvokeExpr ie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, ParameterBucket pb)
	{
		try
		{
			SootMethod sm = pb.CurrentSM;
			
			AssignStmt as = null;
			String strDest = null;
			boolean isInterface = false;

			if (isAssignStmt(invokeType))
			{
				as = (AssignStmt) ut;
				strDest = as.getLeftOp().toString();
			}

			ConsiderateContext(ut, sm);

			Set<SootMethod> SootMethodSet = null;
			SootMethod[] sootMethodArray;
			if (invokeType == Invoke_Type_Assign_Interface || invokeType == Invoke_Type_Not_Assign_Interface)
			{
				SootMethodSet = SearchMethod_interface(ut, sm);
				isInterface = true;
			} else
			{
				SootMethodSet = SearchMethod(invokeType, pb, ie.getMethodRef().getSignature(), ut, sm, BFTtable);
			}

			sootMethodArray = SootMethodSet.toArray(new SootMethod[SootMethodSet.size()]);
			for (int i = 0; i < sootMethodArray.length; i++)
			{
				for (int j = i + 1; j < sootMethodArray.length; j++)
				{
					if (sootMethodArray[i] == null || sootMethodArray[j] == null)
						continue;

					if (sootMethodArray[i].toString().compareTo(sootMethodArray[j].toString()) > 0)
					{
						SootMethod temp = sootMethodArray[i];
						sootMethodArray[i] = sootMethodArray[j];
						sootMethodArray[j] = temp;
					}
				}
			}

			String tempDest = "$TEMPDEST$";
			if (SootMethodSet.size() > 0)
			{
				if (strDest != null)
				{
					BFTtable.put(tempDest, new ArrayList<BFNode>());
				}
			}

			Constants.isSemantic = false;
			boolean robo = false;

			// if method is inhereted by parents and it is also semantic model,
			// change methodref
			for (SootMethod method : SootMethodSet)
			{
				if (SemanticModel.contains(method.toString()) || method.toString().contains("<com.squareup.okhttp."))
				{
					Constants.deobfuseMap.put(ie.getMethodRef().toString(), method.toString());
					Constants.isSemantic = true;
				}
			}

			if (SemanticModel.contains(Constants.Deobfuse(ie.getMethodRef().toString()))
					|| ie.getMethodRef().toString().contains("<com.squareup.okhttp.")
					|| ie.getMethodRef().toString().contains("<ch.boye.httpclientandroidlib") || ie.getMethodRef().toString().contains("<java.net."))
				Constants.isSemantic = true;

   			if (Constants.isSemantic)
			{
				if (robo == false)
				{
					if (isStaticInvoke(invokeType))
					{
						StaticInvokeExpr sie = (StaticInvokeExpr) ie;
						AnalyzeExpression(sie, BFTtable, ut, sm, strDest);
					} else
					{
						InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
						AnalyzeExpression(iie, BFTtable, ut, sm, strDest);
					}
				}
			}

			else
			{
 				if (Constants.isDebug && Constants.isWellknownLibrary(ie.getMethodRef().toString()))
				{
					System.out.println("Semantic Model uncovered : " + ie.getMethodRef().toString());
					Constants.UncoveredCandidate.add(ie.getMethodRef().toString());
				}

				int initIndex = 0;
				for (SootMethod nowtemp : sootMethodArray)
				{
					if (nowtemp.toString().contains("<clinit>"))
					{
						SootMethod tempsm = sootMethodArray[0];
						sootMethodArray[0] = nowtemp;
						sootMethodArray[initIndex] = tempsm;
					}

					initIndex++;
				}

				for (SootMethod subsm : sootMethodArray)
				{
					if (subsm != null && !subsm.getSignature().equals(sm.getSignature()))
					{
						HashMap<String, ArrayList<BFNode>> SubBFTtable = new HashMap<String, ArrayList<BFNode>>();
						CopyParam(ie, SubBFTtable, BFTtable);

						if (subsm.hasActiveBody())
						{
							// history update
							if (!isContainStack(subsm.getSignature()))
							{
								Constants.method_stack.push(subsm.getSignature());
								/*
								 * create new parameterBucket object
								 */
								ParameterBucket newpb = new ParameterBucket(subsm);
								newpb.BFTtable = SubBFTtable;
								
								// BK deliver JSON key list of arguments in caller to parameters in callee
								ValueEntryTracking.deliverJSONKeyListofCallertoCallee(ie, pb, newpb);
								
								// deliver valueEntryList of base to callee method
								if (InvokeHandler.isInstanceInvoke(invokeType))
									ValueEntryTracking.deliverValueEntryListofBaseVariabletoCallee(((InstanceInvokeExpr) ie).getBase().toString(), pb, newpb);
								
								CreateBlockGraphAndInitEdgeTables(newpb);
								Constants.method_stack.pop();
								
								// BK reflect valueEntryList of return value to the destination variable
								ValueEntryTracking.reflectValueEntryListofReturnVariable(strDest, pb, newpb);
								
								// reflect valueEntryList of base variable back from callee to caller
								if (!isStaticInvoke(invokeType)){
									InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
									ValueEntryTracking.reflectValueEntryListofBaseVariable(iie.getBase().toString(), pb, newpb);
								}
								
								
								if (!Constants.alreadyVisited)
								{
									if (strDest != null && !(BackwardRtnValueStack.getLast() instanceof ArrayList))
									{
										handleGlobalRetTable(tempDest, BFTtable, isInterface);
									} else if (strDest != null && (BackwardRtnValueStack.getLast() instanceof ArrayList))
									{
										BFTtable.put(tempDest, (ArrayList<BFNode>) BackwardRtnValueStack.getLast());
									}
								}
								
								BackwardRtnValueStack.removeLast();
								Constants.alreadyVisited = false;
							}
						}
					}

					if (SootMethodSet.size() > 0)
					{

						// When Invoke Call has been included in semantic
						// model, we
						// should jump this condition block.
						SootMethod targetsm = SootMethodSet.iterator().next();
						if (targetsm != null)
						{
							String TargetSig = targetsm.getSignature();
							if (!SemanticModel.contains(TargetSig))
							{
								if (strDest != null)
								{
									BFTtable.put(strDest, BFTtable.get(tempDest));

									if (isInterface)
									{
										if (BFTtable.get(strDest).size() > 0)
										{
											BFTtable.get(strDest).get(0).setStringForUrl("(" + BFTtable.get(strDest).get(0).getStringForUrl() + ")");
										}
									}
								}
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

	private boolean isContainStack(String signature)
	{
		int stacksize = Constants.method_stack.size();

		for (int i = 0; i < stacksize; i++)
		{
			if (Constants.method_stack.get(i).equals(signature))
				return true;
		}
		return false;
	}

	private void ConsiderateContext(Unit ut, SootMethod sm)
	{
		// JM Auto-generated method stub
		if (ut instanceof AssignStmt)
		{
			AssignStmt as = (AssignStmt) ut;

			if (as.containsInvokeExpr())
			{
				InvokeExpr ie = as.getInvokeExpr();

				if (ie instanceof InstanceInvokeExpr)
				{
					String type = getContextforIIE((InstanceInvokeExpr) ie, sm);

					Constants.RealContextInthisObj = type;
				}
			}

		} else if (ut instanceof InvokeStmt)
		{
			InvokeStmt is = (InvokeStmt) ut;

			if (is.getInvokeExpr() instanceof InstanceInvokeExpr)
			{
				String type = getContextforIIE((InstanceInvokeExpr) is.getInvokeExpr(), sm);
				Constants.RealContextInthisObj = type;
			}
		}
	}

	private String getContextforIIE(InstanceInvokeExpr iie, SootMethod fromSm)
	{
		ContextEntry ce = new ContextEntry(fromSm, iie.getBase().toString(), iie.getBase().getType().toString());
		String key = ContextTable.findkey(ce);
		return key;
	}

	private Set<SootMethod> SearchMethod_interface(Unit ut, SootMethod parentSm)
	{
		Unit JimpleUnit = ut;
		SootMethod sm;

		Set<SootMethod> SootMethodSet = new HashSet<SootMethod>();

		if (ut.toString().contains("<ch.qos.logback.classic") || ut.toString().contains("<org.slf4j.Logger"))
			return methodFilter(SootMethodSet);

		if (Constants.serializationMode)
		{

			List<String> getCallees = Constants.sCFG.getCalleesAt(parentSm.getSignature(), JimpleUnit.toString());

			if (getCallees != null && getCallees.size() > 0)
			{
				for (String str : getCallees)
				{
					// Constants.TaintFunctions.contains(str) &&
					if (!VisitTable.containsKey(str))
					{
						sm = Constants.sCFG.getMethodOf(str);
						SootMethodSet.add(sm);
					}
				}
			}
			return methodFilter(SootMethodSet);
		} else
		{

			Collection<SootMethod> sms = Constants.iCfg.getCalleesOfCallAt(JimpleUnit);
			for (SootMethod itr : sms)
				SootMethodSet.add(itr);

			return methodFilter(SootMethodSet);
		}
	}

	public boolean hasNode(HashMap<String, ArrayList<BFNode>> bufttable, String Key)
	{
		return bufttable.containsKey(Key);
	}

	private void handleGlobalRetTable(String dest, HashMap<String, ArrayList<BFNode>> BFTtable, boolean isInterface)
	{
		if (isInterface)
		{
			if (BackwardRtnValueStack.getLast() != null)
			{

				ArrayList<BFNode> list = BFTtable.get(dest);

				if (list.size() == 0)
				{
					BFNode bfn = new BFNode();
					if (BackwardRtnValueStack.getLast() instanceof String)
						bfn.makeUrlBfn((String) BackwardRtnValueStack.getLast());
					list.add(bfn);
				} else
				{
					String pasturl = list.get(0).getStringForUrl();
					String newurl = pasturl + " | " + BackwardRtnValueStack.getLast();
					list.get(0).setStringForUrl(newurl);
				}
			}
		} else
		{
			if (BackwardRtnValueStack.getLast() != null)
			{

				BFNode bfn = new BFNode();
				if (BackwardRtnValueStack.getLast() instanceof String)
					bfn.makeUrlBfn((String) BackwardRtnValueStack.getLast());
				ArrayList<BFNode> list = new ArrayList<BFNode>();
				BFTtable.put(dest, list);
				list.add(bfn);
			}
		}
	}

	private boolean isBlockContainsStmt(BriefBlockGraph Bg, int index, Stmt stmt)
	{

		for (Iterator<Unit> iter = Bg.getBlocks().get(index).iterator(); iter.hasNext();)
		{
			Unit ut = iter.next();
			if (ut.toString().equals(stmt.toString()))
			{
				return true;
			}
		}
		return false;
	}

	private boolean isBackEdge(BriefBlockGraph Bg, int from, int to)
	{
		Body b = Bg.getBody();

		ExtractocolLoopFinder loopFinder = new ExtractocolLoopFinder();
		Set<List<Stmt>> loops = loopFinder.getLoops(b);

		for (List<Stmt> lp : loops)
		{
			if (lp.size() < 2)
			{
				return false;
			}
			if (isBlockContainsStmt(Bg, from, lp.get(lp.size() - 2)) && isBlockContainsStmt(Bg, to, lp.get(lp.size() - 1)))
			{
				return true;
			}
		}
		return false;
	}

	private boolean BlockSelector(int bidx, BriefBlockGraph Bg, Integer[][] EdgeEntry, Integer[][] maniEdge)
	{
		for (int i = 0; i < Bg.getBlocks().size(); i++)
		{
			if (EdgeEntry[i][bidx] == 1 && maniEdge[i][bidx] == 1)
				return true;
			else if (EdgeEntry[i][bidx] == 1 && (!isBackEdge(Bg, i, bidx)))
			{
				return false;
			}
		}
		return true;
	}

	public String CropVar(String var)
	{
		if (var.startsWith("$"))
			return var.substring(var.indexOf(".") + 1);
		else
			return var;
	}

	private void CopyParam(InvokeExpr vie, HashMap<String, ArrayList<BFNode>> SubBFTtable, HashMap<String, ArrayList<BFNode>> BFTtable)
	{
		int paramcount = 0;

		// runnable
		int start = vie.getMethodRef().toString().indexOf("(");
		int end = vie.getMethodRef().toString().indexOf(")");
		String parametertype = vie.getMethodRef().toString().substring(start + 1, end);
		String[] parameters = parametertype.split(",");
		for (int i = 0; i < parameters.length; i++)
		{
			String targetParam = parameters[i];
			if (targetParam.contains("java.lang.Runnable"))
				Constants.RunnableCalss = vie.getArg(i).getType().toString();
		}

		for (Value v : vie.getArgs())
		{
			String stringForUrl = null;
			BFNode bfn = new BFNode();

			// constant
			if (v instanceof Constant)
			{
				stringForUrl = v.toString();
				bfn.makeUrlBfn(stringForUrl);
			}

			// if array copy bfn array -- woohyun 20160331
			else if (BFTtable.get(v.toString()) != null && BFTtable.get(v.toString()).size() >= 1
					&& BFNode.isArray(BFTtable.get(v.toString()).get(0)))
			{
				BFNode targetbfn = BFTtable.get(v.toString()).get(0);

				// array init
				bfn.initarray(targetbfn.getSize());

				// stringforurl copy
				bfn.makeUrlBfn(targetbfn.getStringForUrl());

				// orcase copy
				if (targetbfn.getArrayorcase() != null)
					bfn.setArrayorcase(new ArrayList<String>(targetbfn.getArrayorcase()));
			}
			// normal varialbe
			else
			{
				// variable
				stringForUrl = GenRegex(null, BFTtable, v.toString());

				bfn = new BFNode();
				bfn.makeUrlBfn(stringForUrl);
			}

			// JM for extendedType
			if (BFTtable.get(v.toString()) != null)
				if (BFTtable.get(v.toString()).size() > 0)
					bfn.setExtendedType(BFTtable.get(v.toString()).get(0).getExtendedType());

			ArrayList<BFNode> list = new ArrayList<BFNode>();
			list.add(bfn);

			if (BFTtable.get(v.toString()) != null)
			{
				if (BFTtable.get(v.toString()).size() > 0)
				{
					if (BFTtable.get(v.toString()).get(0).hasVtype())
					{
						if (BFTtable.get(v.toString()).get(0).getVtype().contains("HashMap")
								|| BFTtable.get(v.toString()).get(0).getVtype().contains("BasicNameValuePair"))
						{
							SubBFTtable.put("@parameter" + paramcount, BFTtable.get(v.toString()));
						}
					}
				}
			}

			if (!SubBFTtable.keySet().contains("@parameter" + paramcount))
				SubBFTtable.put("@parameter" + paramcount, list);

			paramcount++;
		}
	}

	/*
	 * public boolean isconst(String arg) { if (arg.getBytes()[0] == '"' || ('0'
	 * < arg.getBytes()[0] && arg.getBytes()[0] < '9')) { return true; } return
	 * false; }
	 */

	public void printTree(Tree<BFNode> tr, String Treename) throws NodeNotFoundException
	{
		System.out.println("===============================");
		System.out.println("Tree " + Treename + " PreorderTraversal");
		for (Iterator<BFNode> iter = tr.preOrderTraversal().iterator(); iter.hasNext();)
		{
			BFNode bfn = iter.next();
			System.out.print(bfn.getKey() + ":" + bfn.getValue() + "\tVtype : " + bfn.getVtype());
			try
			{
				System.out.print("\nThis : " + bfn);
				System.out.println("\nParent : " + tr.parent(bfn));
			} catch (NodeNotFoundException e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("Tree depth : " + tr.depth());
	}

	public void printHeap(Tree<BFNode> tr) throws NodeNotFoundException, IOException
	{
		for (Iterator<BFNode> iter = tr.preOrderTraversal().iterator(); iter.hasNext();)
		{
			BFNode bfn = iter.next();
			if (bfn.getKey() == null)
			{
				if (bfn.getSootValue() != null)
				{
					String sub = bfn.getSootValue().toString();
					if (sub.indexOf("<") > 0)
					{
						int start = sub.indexOf(".");
						sub = sub.substring(start + 1);
					}
				}
			}
		}
	}

	public ArrayList<BFNode> CopyList(ArrayList<BFNode> src)
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

	public void CopyListReference(ArrayList<BFNode> src, ArrayList<BFNode> arraybfn)
	{

		arraybfn.clear();

		for (BFNode bfn : src)
		{
			try
			{
				arraybfn.add((BFNode) bfn.clone());
			} catch (CloneNotSupportedException e)
			{
				e.printStackTrace();
			}
		}
	}

	protected ArrayList<SemanticAppliedEntry> CopyPairingList(ArrayList<SemanticAppliedEntry> plist)
	{
		if (plist == null)
			return null;
		ArrayList<SemanticAppliedEntry> tlist = new ArrayList<SemanticAppliedEntry>();

		for (SemanticAppliedEntry e : plist)
		{
			tlist.add(new SemanticAppliedEntry(e.getMethod(), e.getStmt()));
		}
		return tlist;
	}
}
