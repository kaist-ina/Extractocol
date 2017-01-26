package SignExtractor.ApplicationSpecific.ted;

/* BufferExtractor Abstract class */
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import junit.framework.Assert;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.coffi.constant_element_value;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.util.SystemClassHandler;
//import soot.jimple.infoflow.solver.IInfoflowCFG;
import soot.jimple.parser.JimpleAST;
import soot.shimple.PhiExpr;
import soot.shimple.Shimple;
import soot.shimple.ShimpleBody;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;
import soot.util.Chain;
import soot.util.Cons;

import com.gaurav.tree.ArrayListTree;
import com.gaurav.tree.NodeNotFoundException;
import com.gaurav.tree.Tree;

import Extractocol.Constants;
import Extractocol.ExtractocolException;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Basics.EPcontainer;
import Extractocol.BufferExtractor_Request.Basics.SliceManager;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapHandler;
import Extractocol.BufferExtractor_Request.Helper.ExtractocolLoopFinder;
import Extractocol.BufferExtractor_Request.Semantic.URL.UrlParser;
import Extractocol.Pairing.PairingDPEntry;
import Extractocol.Pairing.PairingEPEntry;

//For Graph Visualization

public abstract class ted_BufferExtractor
{
	public List<String> methodlist;
	public boolean isForward = false;
	public boolean initFlag;

	
	private static LinkedList<String> BackwardRtnValueStack = new LinkedList<String>();
	private static LinkedList<String> ForwardRtnValueStack = new LinkedList<String>();
	private static LinkedList<String> ForwardRtnVariableStack = new LinkedList<String>();
	private static LinkedList<ArrayList<PairingEPEntry>> ForwardRtnPairingListStack = new LinkedList<ArrayList<PairingEPEntry>>();
	private static LinkedList<HashMap<String, String>> ForwardRtnParameterTableStack = new LinkedList<HashMap<String, String>>();
	
	// this needs to be used at JSONBUilder.java 20150921 hnamkoong
	protected static LinkedList<Set<String>> ForwardGsonTrackingVarStack = new LinkedList<Set<String>>(); 
	
	
	
	
//	private static String GlobalRtnValue = null;
//	private static String GlobalRtnVar = null;
//	private static ArrayList<PairingEPEntry> GlobalRtnPairingList = null;
//	private static HashMap<String, String> GlobalParameterTable = new HashMap<String, String>();
	
	

	private SootMethod sm;

	private HashMap<String, Integer[]> VisitTable = new HashMap<String, Integer[]>();

	//WOOMADE
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

	@SuppressWarnings("rawtypes")
	abstract public String GenRegex(HashMap<String, String> taintVariableMap, HashMap<String, ArrayList<BFNode>> BFTtable, String TrackingReg);

	abstract public String ExtractingExpr(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, String ParentMethod, SootMethod sootMethod,
			String string, HashMap<String, ArrayList<PairingEPEntry>> pairingInfoTable) throws NodeNotFoundException, ExtractocolException;

	abstract public void ExtractingExpr(StaticInvokeExpr sie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, String name, SootMethod methodOf, String string,
			HashMap<String, ArrayList<PairingEPEntry>> pairingInfoTable) throws ExtractocolException;

	abstract public boolean isContainDP(Block block);

	public void StartFingerprint(SliceManager SliceM, JimpleAST jast, boolean direction, int SMidx, IInfoflowCFG iCfg) throws Exception
	{
		// SootMethod sm = sc.getMethods().iterator().next();

		this.isForward = direction;
		SootMethod sm = SliceM.getStartMethod().get(SMidx);
		// if ()

		// // Parse url about each slices
		// UrlParser up = new UrlParser();
		// up.parseUrl(sc, sm, jast, direction, null, 0, false);
		// CreateHashMaps
		@SuppressWarnings("rawtypes")
		HashMap<String, ArrayList<BFNode>> BFTtable = new HashMap<String, ArrayList<BFNode>>();

		// System.out.println("Start function name : " + sm.getName());
		//		ExtractStmt(SliceM, sm, BFTtable, false, iCfg, null, null, null, null, null);
	}

	public void StartFingerprint(SliceManager SliceM, JimpleAST jast, boolean direction, ArrayList<String> path, LinkedHashMap<String, String> originalMethod, int SMidx,
			IInfoflowCFG iCfg, SootMethod sootMethod, Set<SootMethod> set) throws Exception
	{
		// SootMethod sm = sc.getMethods().iterator().next();
		boolean chkDummy = false;
		boolean chkDoIn = false;

		String StartTrackingfunc;
		// = path.get(0);

		// System.out.println("StartTrackingfunc : " + StartTrackingfunc);
		// System.out.println("Path size : " + path.size());

		SootMethod sm = null;
		// SearchdoStartfunc(sc, StartTrackingfunc);
		UrlParser up;
		// System.out.print("\nPath : " + path);

		// if ( sm.getName().indexOf("dummy") != -1)
		// chkDummy = true;

		// CreateHashMaps
		int idx = 0;
		try
		{
			sm = sootMethod;
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		// Parse url about each paths
		if (!isForward && !this.getClass().getName().equals("SignExtractor.HeapObjectFinder"))
		{
			// before separation - 20151216 hnamkoong
			// UrlParser -> Debug on/off
//			up = new UrlParser();
//			up.isResource = true;
//			up.parseUrl(SliceM, Constants.sCFG.getMethodOf(Constants.CurrentEntryPoint), jast, direction, path, 0, false, iCfg, set);
			
			Object obj = Class.forName("SignExtractor.ApplicationSpecific." + Constants.apkName + "." + Constants.apkName + "_UrlParser").newInstance();
			Class iClass = obj.getClass();
			Method parseUrl = null;
			for(Method m : iClass.getMethods()) {
//				System.out.println(m.getName());
//				System.out.println(m.getParameterTypes().length);
				if(m.getName().equals("parseUrl")) {
					parseUrl = m;
				}
			}
			parseUrl.invoke(obj, SliceM, Constants.sCFG.getMethodOf(Constants.CurrentEntryPoint), jast, direction, path, 0, false, iCfg, set);
		}

		if (isForward == true)
		{
			if (Constants.EPcont.getDPStmt() instanceof AssignStmt)
			{
				HashMap<String, ArrayList<BFNode>> BFTtable = new HashMap<String, ArrayList<BFNode>>();

				Set<String> taintParameterSet = new HashSet<String>();
				HashMap<String, String> taintVariableMap = new HashMap<String, String>();

				setSubTaintVariableMap(taintVariableMap, taintParameterSet);

				HashMap<String, PairingEPEntry> responsePairingInfo = new HashMap<String, PairingEPEntry>();

				ExtractStmt(SliceM, Constants.sCFG.getMethodOf(Constants.CurrentEntryPoint), BFTtable, false, iCfg, taintParameterSet, taintVariableMap, null, null,
						responsePairingInfo);
			}
		}
	}

	public void StartFingerprint(SliceManager SliceM, JimpleAST jast, boolean direction, ArrayList<String> path, LinkedHashMap<String, String> originalMethod, int SMidx,
			IInfoflowCFG iCfg, SootMethod sootMethod, Set<SootMethod> set, EPcontainer EPC) throws Exception
	{
		// SootMethod sm = sc.getMethods().iterator().next();
		boolean chkDummy = false;
		boolean chkDoIn = false;

		String StartTrackingfunc;
		// = path.get(0);

		// System.out.println("StartTrackingfunc : " + StartTrackingfunc);
		// System.out.println("Path size : " + path.size());

		SootMethod sm = null;
		// SearchdoStartfunc(sc, StartTrackingfunc);
		UrlParser up;
		// System.out.print("\nPath : " + path);

		// if ( sm.getName().indexOf("dummy") != -1)
		// chkDummy = true;

		// CreateHashMaps
		int idx = 0;
		try
		{
			sm = sootMethod;
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		// print url
		if (isForward == false)
		{
			// Parse url about each paths
			if (!isForward && !this.getClass().getName().equals("SignExtractor.HeapObjectFinder"))
			{
				// UrlParser -> Debug on/off
				up = new UrlParser();
				up.isResource = true;
				up.isForward = isForward;
				up.parseUrl(SliceM, sm, jast, direction, path, 0, false, iCfg, set);
			}
		}

		// print json response

		if (isForward == true)
		{
			if (EPC.getDPStmt() instanceof AssignStmt)
			{
				HashMap<String, ArrayList<BFNode>> BFTtable = new HashMap<String, ArrayList<BFNode>>();

				Set<String> taintParameterSet = new HashSet<String>();
				HashMap<String, String> taintVariableMap = new HashMap<String, String>();

				setSubTaintVariableMap(taintVariableMap, taintParameterSet);

				HashMap<String, PairingEPEntry> responsePairingInfo = new HashMap<String, PairingEPEntry>();
				ExtractStmt(SliceM, sm, BFTtable, false, iCfg, taintParameterSet, taintVariableMap, null, null, responsePairingInfo);
			}
		}
	}

	public Set<SootMethod> SearchMethod(SliceManager SliceM, String MethodSig, String Stmt, Unit ut, SootMethod parentSm, Stack<String> history)
	{
		int kkk = history.size();
		Unit JimpleUnit = ut;

		Set<SootMethod> SootMethodSet = new HashSet<SootMethod>();

		if (isForward)
		{
			// when slicing response, we use shimple
			// translate shimple unit into jimple unit 
			// 20150918 hnamkoong

			for (Unit inut : parentSm.getActiveBody().getUnits())
			{
				if (inut instanceof InvokeStmt && ut instanceof InvokeStmt)
				{
					InvokeStmt is = (InvokeStmt) inut;
					if (ut.toString().contains(is.getInvokeExpr().getMethodRef().getSignature()))
					{
						JimpleUnit = inut;
						break;
					}
				} else if (inut instanceof AssignStmt && ut instanceof AssignStmt)
				{
					AssignStmt as = (AssignStmt) inut;
					if (as.getRightOp() instanceof InvokeExpr)
					{
						InvokeExpr ie = (InvokeExpr) as.getRightOp();
						// System.out.println(ie);
						if (ie.getMethodRef() != null && ut.toString().contains(ie.getMethodRef().getSignature()))
						{
							JimpleUnit = inut;
							break;
						}
					}
				}
			}
		}

		if (Constants.serializationMode && Constants.serIsForward)
		{
			List<String> getCallees = Constants.sCFG.getCalleesAt(parentSm.getSignature(), JimpleUnit.toString());

			if (getCallees != null && getCallees.size() > 0)
			{
				for (String str : getCallees)
				{
					if (Constants.TaintFunctions.contains(str) && !VisitTable.containsKey(str))
					{
						sm = Constants.sCFG.getMethodOf(str);
						SootMethodSet.add(sm);
					}
				}
			}
			return methodFilter(SootMethodSet);

		}
		if (Constants.serializationMode && Constants.serIsForward == false)/*-?|first_review|Administrator|c0|*/
		{

			int k = 0;

			// TODO Jump Unit Detection Point
			List<String> getCallees = Constants.sCFG.getCalleesAt(parentSm.getSignature(), JimpleUnit.toString());

			if (getCallees == null)
				return SootMethodSet;

			//callflow set
			if (getCallees.size() > 1)
			{
				if (isForward == false)
					for (String itr : getCallees)
					{
//						if (Constants.CallFlow.contains(itr))
//						{
							sm = Constants.sCFG.getMethodOf(itr);
//						}
					}

				// same Signature
				if (sm == null)
				{
					for (String itr : getCallees)
					{
						if (itr.toString().contains(MethodSig))
						{
							sm = Constants.sCFG.getMethodOf(itr);
							if (k == 99)
								System.out.println("___________________ Same MethodSig in Searchmethod __________________ in SM: " + parentSm + " UT: " + ut);
							k = 99;
						}
					}

				}
				// TODO getbase jump
				if (sm == null)
				{
					String baseType = null;
					String Methodname = null;
					if (ut instanceof AssignStmt)
					{
						AssignStmt as = (AssignStmt) ut;
						if (as.containsInvokeExpr())
						{
							if (as.getInvokeExpr() instanceof SpecialInvokeExpr)
							{
								SpecialInvokeExpr sie = (SpecialInvokeExpr) as.getInvokeExpr();
								baseType = sie.getBase().getType().toString();
								Methodname = sie.getMethodRef().name();
							} else if (as.getInvokeExpr() instanceof StaticInvokeExpr)
							{
								StaticInvokeExpr sie = (StaticInvokeExpr) as.getInvokeExpr();
								k = -1;
								Methodname = sie.getMethodRef().name();
							} else if (as.getInvokeExpr() instanceof VirtualInvokeExpr)
							{
								VirtualInvokeExpr vie = (VirtualInvokeExpr) as.getInvokeExpr();
								baseType = vie.getBase().getType().toString();
								Methodname = vie.getMethodRef().name();
							} else if (as.getInvokeExpr() instanceof InterfaceInvokeExpr)
							{
								InterfaceInvokeExpr ife = (InterfaceInvokeExpr) as.getInvokeExpr();
								baseType = ife.getBase().getType().toString();
								Methodname = ife.getMethodRef().name();

							}
						} else
							System.out.println("___________________ UT is AssignSTMT BUT NO invoke in Searchmethod __________________ in SM: " + parentSm + " UT: " + ut);

					} else if (ut instanceof IdentityStmt)
					{
						System.out.println("___________________ UT has NO invoke in Searchmethod __________________ in SM: " + parentSm + " UT: " + ut);
					} else if (ut instanceof ReturnStmt)
					{
						System.out.println("___________________ UT has NO invoke in Searchmethod __________________ in SM: " + parentSm + " UT: " + ut);
					} else if (ut instanceof InvokeStmt)
					{
						InvokeStmt ivs = (InvokeStmt) ut;
						if (ivs.getInvokeExpr() instanceof SpecialInvokeExpr)
						{
							SpecialInvokeExpr sie = (SpecialInvokeExpr) ivs.getInvokeExpr();
							baseType = sie.getBase().getType().toString();
							Methodname = sie.getMethodRef().name();
						} else if (ivs.getInvokeExpr() instanceof VirtualInvokeExpr)
						{
							VirtualInvokeExpr vie = (VirtualInvokeExpr) ivs.getInvokeExpr();
							baseType = vie.getBase().getType().toString();
							Methodname = vie.getMethodRef().name();
						} else if (ivs.getInvokeExpr() instanceof InterfaceInvokeExpr)
						{
							InterfaceInvokeExpr ife = (InterfaceInvokeExpr) ivs.getInvokeExpr();
							baseType = ife.getBase().getType().toString();
							Methodname = ife.getMethodRef().name();

						} else if (ivs.getInvokeExpr() instanceof StaticInvokeExpr)
						{
							StaticInvokeExpr sie = (StaticInvokeExpr) ivs.getInvokeExpr();
							k = -1;
							Methodname = sie.getMethodRef().name();
						}

					}

					// find callee using base object
					for (String itr : getCallees)
					{
						if (baseType != null && Constants.sCFG.getMethodOf(itr).getDeclaringClass().toString().equals(baseType))
						{
							sm = Constants.sCFG.getMethodOf(itr);
							if (k == 88)
								System.out.println("___________________ Same baseType in Searchmethod __________________ in SM: " + parentSm + " UT: " + ut);
							k = 88;

						}
					}
					Stack<String> temp = new Stack<String>();
					Stack<String> clone = (Stack<String>) history.clone();
					temp = clone;
					// parent base
					if (sm == null)
					{
						for (int i = 0; i < history.size(); i++)
						{
							String baseobject = history.pop();
							for (String itr : getCallees)
							{

								if (baseobject != null && Constants.sCFG.getMethodOf(itr).getDeclaringClass().toString().contains(baseobject))
								{
									sm = Constants.sCFG.getMethodOf(itr);
									if (k == 55)
										System.out.println("___________________ mutiple baseobject in Searchmethod __________________ in SM: " + parentSm + " UT: " + ut);
									k = 55;
									break;
								}
								if (k == 55)
									break;
							}

						}
						history = temp;
					}
					// runnable
					if (sm == null && Constants.RunnableCalss != null)
					{
						for (String itr : getCallees)
						{
							if (Constants.sCFG.getMethodOf(itr).getSignature().toString().equals(Constants.RunnableCalss))
							{
								sm = Constants.sCFG.getMethodOf(itr);
								if (k == 66)
									System.out.println("___________________ multiple same runnable __________________ in SM: " + parentSm + " UT: " + ut);
								k = 66;

							}
						}
					}

					// static invoke
					if (baseType == null && sm == null)
					{
						assert k == -1;
						for (String itr : getCallees)
						{
							{
								if (Methodname != null && Constants.sCFG.getMethodOf(itr).getName().toString().equals(Methodname))
								{
									sm = Constants.sCFG.getMethodOf(itr);
									if (k == 77)
										System.out.println("___________________ Static invoke extend in Searchmethod __________________ in SM: " + parentSm + " UT: "
												+ ut);
									k = 77;

								}
							}
						}

						if (sm == null)
							System.out.println("___________________ SM is null in multiple callee case in Searchmethod __________________ in SM: " + parentSm + " UT: "
									+ ut);

					}

				}

			}

			//			// TODO WHATTODO
			//			if (getCallees == null)
			//				return SootMethodSet;
			//
			//			if (getCallees.size() > 1) {
			//				for (String str : getCallees) {
			//					if (Constants.CallFlow.contains(str)) {
			//						sm = Constants.sCFG.getMethodOf(str);
			//						break;
			//					}
			//				}
			//			}
			else
			{
				sm = Constants.sCFG.getMethodOf(getCallees.get(0));
			}

			if (sm != null && !Constants.TaintFunctions.contains(sm.getSignature()))
			{
				sm = null;
			}

			if (sm != null)
			{
				if (VisitTable.containsKey(sm.getSignature()))
					sm = null;
			}

			//			if (getCallees.size() > 1)
			//			{
			//				if (Constants.TaintFunctions.contains(getCallees.get(0)))
			//				{
			////					System.out.println("************************");
			////					System.out.println("This EP is not reachable");
			////					System.out.println("EP : " + getCallees + " Stmt : " + ut.toString());
			////					System.out.println("************************");
			//
			//					SootMethodSet.add(Constants.sCFG.getMethodOf(getCallees.get(0)));
			//				}
			//			}

			if (sm != null)
			{
				//				if (sm.getSignature().equals(Constants.CurrentParentMethod))
				//					System.out.println("We reached DP.");
				SootMethodSet.add(sm);
			}
			return methodFilter(SootMethodSet);
		}

		//Fully case
		int k = 0;
		Collection<SootMethod> sms = null;
		SootMethod sm = null;
		// woomade
		// if(ut.toString().contains("java.lang.Runnable") || sm==null)
		// {
		sms = Constants.iCfg.getCalleesOfCallAt(JimpleUnit);

		if (isForward == false || isForward == true)
		{
			if (sms.size() > 1)
			{
				if (isForward == false)
					for (SootMethod itr : sms)
					{
						if (itr.toString().contains("init"))
						{
							k = 1;
						}
						if (Constants.callflow.contains(itr))
						{
							sm = itr;
						}
					}

				// same Signature
				if (sm == null)
				{
					for (SootMethod itr : sms)
					{
						if (itr.toString().contains(MethodSig))
						{
							sm = itr;
							if (k == 99)
								System.out.println("___________________ Same MethodSig in Searchmethod __________________ in SM: " + parentSm + " UT: " + ut);
							k = 99;
						}
					}

				}
				// TODO getbase jump
				if (sm == null)
				{
					String baseType = null;
					String Methodname = null;
					if (ut instanceof AssignStmt)
					{
						AssignStmt as = (AssignStmt) ut;
						if (as.containsInvokeExpr())
						{
							if (as.getInvokeExpr() instanceof SpecialInvokeExpr)
							{
								SpecialInvokeExpr sie = (SpecialInvokeExpr) as.getInvokeExpr();
								baseType = sie.getBase().getType().toString();
								Methodname = sie.getMethod().getName();
							} else if (as.getInvokeExpr() instanceof StaticInvokeExpr)
							{
								StaticInvokeExpr sie = (StaticInvokeExpr) as.getInvokeExpr();
								k = -1;
								Methodname = sie.getMethod().getName();
							} else if (as.getInvokeExpr() instanceof VirtualInvokeExpr)
							{
								VirtualInvokeExpr vie = (VirtualInvokeExpr) as.getInvokeExpr();
								baseType = vie.getBase().getType().toString();
								Methodname = vie.getMethod().getName();
							} else if (as.getInvokeExpr() instanceof InterfaceInvokeExpr)
							{
								InterfaceInvokeExpr ife = (InterfaceInvokeExpr) as.getInvokeExpr();
								baseType = ife.getBase().getType().toString();
								Methodname = ife.getMethod().getName();

							}
						} else
							System.out.println("___________________ UT is AssignSTMT BUT NO invoke in Searchmethod __________________ in SM: " + parentSm + " UT: " + ut);

					} else if (ut instanceof IdentityStmt)
					{
						System.out.println("___________________ UT has NO invoke in Searchmethod __________________ in SM: " + parentSm + " UT: " + ut);
					} else if (ut instanceof ReturnStmt)
					{
						System.out.println("___________________ UT has NO invoke in Searchmethod __________________ in SM: " + parentSm + " UT: " + ut);
					} else if (ut instanceof InvokeStmt)
					{
						InvokeStmt ivs = (InvokeStmt) ut;
						if (ivs.getInvokeExpr() instanceof SpecialInvokeExpr)
						{
							SpecialInvokeExpr sie = (SpecialInvokeExpr) ivs.getInvokeExpr();
							baseType = sie.getBase().getType().toString();
							Methodname = sie.getMethod().getName();
						} else if (ivs.getInvokeExpr() instanceof VirtualInvokeExpr)
						{
							VirtualInvokeExpr vie = (VirtualInvokeExpr) ivs.getInvokeExpr();
							baseType = vie.getBase().getType().toString();
							Methodname = vie.getMethod().getName();
						} else if (ivs.getInvokeExpr() instanceof InterfaceInvokeExpr)
						{
							InterfaceInvokeExpr ife = (InterfaceInvokeExpr) ivs.getInvokeExpr();
							baseType = ife.getBase().getType().toString();
							Methodname = ife.getMethod().getName();

						} else if (ivs.getInvokeExpr() instanceof StaticInvokeExpr)
						{
							StaticInvokeExpr sie = (StaticInvokeExpr) ivs.getInvokeExpr();
							k = -1;
							Methodname = sie.getMethod().getName();
						}

					}

					// find callee using base object
					for (SootMethod itr : sms)
					{
						if (baseType != null && itr.getDeclaringClass().toString().equals(baseType))
						{
							sm = itr;
							if (k == 88)
								System.out.println("___________________ Same baseType in Searchmethod __________________ in SM: " + parentSm + " UT: " + ut);
							k = 88;

						}
					}
					Stack<String> temp = new Stack<String>();
					Stack<String> clone = (Stack<String>) history.clone();
					temp = clone;
					// parent base
					if (sm == null)
					{
						for (int i = 0; i < history.size(); i++)
						{
							String baseobject = history.pop();
							for (SootMethod itr : sms)
							{

								if (baseobject != null && itr.getDeclaringClass().toString().contains(baseobject))
								{
									sm = itr;
									if (k == 55)
										System.out.println("___________________ mutiple baseobject in Searchmethod __________________ in SM: " + parentSm + " UT: " + ut);
									k = 55;
									break;
								}
								if (k == 55)
									break;
							}

						}
						history = temp;
					}
					// runnable
					if (sm == null && Constants.RunnableCalss != null)
					{
						for (SootMethod itr : sms)
						{
							if (itr.getSignature().toString().equals(Constants.RunnableCalss))
							{
								sm = itr;
								if (k == 66)
									System.out.println("___________________ multiple same runnable __________________ in SM: " + parentSm + " UT: " + ut);
								k = 66;

							}
						}
					}

					// static invoke
					if (baseType == null && sm == null)
					{
						assert k == -1;
						for (SootMethod itr : sms)

						{
							if (Methodname != null && itr.getName().toString().equals(Methodname))
							{
								sm = itr;
								if (k == 77)
									System.out.println("___________________ Static invoke extend in Searchmethod __________________ in SM: " + parentSm + " UT: " + ut);
								k = 77;

							}
						}
					}

					if (sm == null)
						System.out.println("___________________ SM is null in multiple callee case in Searchmethod __________________ in SM: " + parentSm + " UT: " + ut);

				}

			} else
			{
				for (SootMethod itr : sms)
				{
					sm = itr;
				}
			}
			if (sm != null && !Constants.allmethods.contains(sm))
			{
				sm = null;
			}

			// If sm is included VisitTable.
			if (sm != null)
			{
				if (VisitTable.containsKey(sm.getSignature()))
					sm = null;
			}
			if (kkk != history.size())
				System.out.println("FUCK");

			if (sm != null)
				SootMethodSet.add(sm);
			return methodFilter(SootMethodSet);
		}
		// else
		// {
		// for (SootMethod ism : sms)
		// {
		// if (ism != null && Constants.allmethods.contains(ism) &&
		// !VisitTable.containsKey(ism.getSignature()))
		// {
		// SootMethodSet.add(ism);
		// }
		// }
		// return SootMethodSet;
		// }
		return methodFilter(SootMethodSet);
	}

	private Set<SootMethod> methodFilter(Set<SootMethod> sootMethodSet)
	{
		for (String filter : Constants.searchMethodFilter)
		{
			for (SootMethod sm : sootMethodSet)
			{
				if (sm.getSignature().equals(filter)
						|| sm.getSignature().startsWith("<android.")
						|| sm.getSignature().startsWith("<com.github.kevinsawicki.http.HttpRequest:")
						|| sm.getSignature().startsWith("<com.squareup.okhttp.OkHttpClient:"))
				{
					sootMethodSet.remove(sm);
					break;
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

	public void onelevelheap(SootMethod sm)
	{
		// SootMethod sm = sc.getMethods().iterator().next();
		boolean chkDummy = false;
		boolean chkDoIn = false;
		this.methodlist = Arrays.asList("<init>", "append", "setEntity", "setHeader", "getJSONFromPost", "getJSONFromUrl", "toString", "execute", "openStream", "format",
				"add", "put", "getActiveNetworkInfo", "fromJson", "retrieveStream", "onPostExecute", "getString", "parse", "appendQueryParameter", "addPart",
				"setRequestMethod", "write", "serveRequest", "retrieveToken", "openConnection", "open", "replace", "toLowerCase");

		UrlParser up;
		HashMap<String, ArrayList<BFNode>> BFTtable = new HashMap<String, ArrayList<BFNode>>();

		// UrlParser -> Debug on/off
		try
		{
			//			ExtractStmt(Constants.SM, sm, BFTtable, true, null, null, null, null, null);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// print json response

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

	private HashMap<String, ArrayList<BFNode>> MergeBFTtables(HashMap<String, HashMap<String, ArrayList<BFNode>>> BFTtableMap, Set<String> blockNumberSet)
	{
		// put everything into filter table
		HashMap<String, Set<String>> filterTable = new HashMap<String, Set<String>>();
		for (String blockNum : blockNumberSet)
		{
			HashMap<String, ArrayList<BFNode>> BFTtable = BFTtableMap.get(blockNum);
			for (String key : BFTtable.keySet())
			{
				if (filterTable.get(key) == null)
				{
					filterTable.put(key, new HashSet<String>());
				}
				filterTable.get(key).add(GenRegex(null, BFTtable, key));
			}
		}

		HashMap<String, ArrayList<BFNode>> mergedBFTtable = new HashMap<String, ArrayList<BFNode>>();
		for (String key : filterTable.keySet())
		{
			String result = "";
			boolean orCheck = false;
			for (String node : filterTable.get(key))
			{
				if (node != null && !node.equals(""))
				{
					if (!result.equals(""))
					{
						result += " | ";
						orCheck = true;
					}
					result += node;
				}
			}
			if (orCheck)
			{
				result = "(" + result + ")";
			}

			if (!result.equals(""))
			{
				ArrayList<BFNode> list = new ArrayList<BFNode>();
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(result);
				list.add(bfn);
				mergedBFTtable.put(key, list);
			} else
			{
				mergedBFTtable.put(key, new ArrayList<BFNode>());
			}
		}

		// handle reference problem 20150915
		HashMap<String, Set<String>> pairSetMap = new HashMap<String, Set<String>>();
		Set<String> allPair = new HashSet<String>();

		for (String blockNum : blockNumberSet)
		{
			HashMap<String, ArrayList<BFNode>> BFTtable = BFTtableMap.get(blockNum);
			Set<String> pairSet = new HashSet<String>();
			for (String key1 : BFTtable.keySet())
			{
				for (String key2 : BFTtable.keySet())
				{
					if(key1 == null) {
						continue;
					}
					if(key2 == null) {
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

	private HashMap<String, ArrayList<PairingEPEntry>> MergePairingInfoTables(HashMap<String, HashMap<String, ArrayList<PairingEPEntry>>> paringInfoTableMap,
			Set<String> blockNumberSet)
	{
		HashMap<String, ArrayList<PairingEPEntry>> mergedPairingInfotable = new HashMap<String, ArrayList<PairingEPEntry>>();

		for (String blockNum : blockNumberSet)
		{
			HashMap<String, ArrayList<PairingEPEntry>> pairingInfoTable = paringInfoTableMap.get(blockNum);
			for (String key : pairingInfoTable.keySet())
			{
				if (key.contains("@parameter"))
					continue;

				if (mergedPairingInfotable.get(key) == null)
				{
					mergedPairingInfotable.put(key, new ArrayList<PairingEPEntry>());
				}
				if (pairingInfoTable.get(key) != null)
					mergeList(mergedPairingInfotable.get(key), pairingInfoTable.get(key));
			}
		}

		return mergedPairingInfotable;
	}

	private void mergeList(ArrayList<PairingEPEntry> arrayList, ArrayList<PairingEPEntry> arrayList2)
	{
		for (PairingEPEntry newe : arrayList2)
		{
			boolean isNew = true;
			for (PairingEPEntry e : arrayList)
			{
				if (newe.isSame(e))
				{
					isNew = false;
					break;
				}
			}
			if (isNew)
			{
				arrayList.add(newe);
			}
		}
	}

	private DirectedGraph<String, DefaultEdge> createStringGraph(BriefBlockGraph bg)
	{
		DirectedGraph<String, DefaultEdge> dg = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		Set<String> EdgeSet = new HashSet<String>();

		for (Block b : bg.getBlocks())
		{
			String Current = String.valueOf(bg.getBlocks().indexOf(b));
			System.out.println("{ id: " + Current + ", label: '" + Current + "'},");
		}

		for (Block b : bg.getBlocks())
		{
			String Current = String.valueOf(bg.getBlocks().indexOf(b));
			// dg.addVertex( Current );

			// for (Block b2 : bg.getPredsOf(b)) {
			// String Pred = String.valueOf(bg.getBlocks().indexOf(b2));
			// // dg.addVertex( Pred );
			// // dg.addEdge( Pred , Current);
			//
			// EdgeSet.add("{from: " + Pred + ", to:" + Current
			// +", arrows:'to'},");
			// }

			for (Block b2 : bg.getSuccsOf(b))
			{
				String Succ = String.valueOf(bg.getBlocks().indexOf(b2));
				// dg.addVertex( Succ );
				// dg.addEdge( Current, Succ );

				EdgeSet.add("{from: " + Current + ", to:" + Succ + ", arrows:'to'},");
			}
		}

		for (String a : EdgeSet)
			System.out.println(a);

		return dg;
	}

	@SuppressWarnings({"unchecked", "unused", "rawtypes"})
	public void ExtractStmt(SliceManager SliceM, SootMethod sm, HashMap<String, ArrayList<BFNode>> BFTtable, boolean isUrlpath, IInfoflowCFG iCfg,
			Set<String> taintParameterSet, HashMap<String, String> taintVariableMap, Stack<String> historyofSM,
			HashMap<String, ArrayList<PairingEPEntry>> pairingInfoTable, HashMap<String, PairingEPEntry> responsePairingInfo) throws java.lang.Exception
	{
		try
		{
			BackwardRtnValueStack.addLast(null);
			ForwardRtnValueStack.addLast(null);
			ForwardRtnVariableStack.addLast(null);
			ForwardRtnPairingListStack.addLast(null);
			ForwardRtnParameterTableStack.addLast(new HashMap<String, String>());
			ForwardGsonTrackingVarStack.addLast(new HashSet<String>());

			if (Constants.foundDPStmt || Constants.stopVisitMethod())
				return;

			if (!sm.hasActiveBody())
			{
				return;
			}
			Stack<String> history = new Stack<String>();

			Constants.methodVisitCount++;
			if(Constants.methodVisitCount%30 == 0) {
				System.out.println("Method Count : " + Constants.methodVisitCount + "..");
			}

//			int line = 0;
//			int deep = 0;
			int id = Constants.id++;
//			int[] line_deep = new int[2];
//			line_deep[0] = line;
//			line_deep[1] = deep;
//
//			Constants.line_deep.put(id, line_deep);
//
			if (historyofSM != null)
				history = (Stack<String>) historyofSM.clone();
			//			if(Constants.taintset.contains(sm))
			//			Constants.taintset.remove(sm);

			Body body = sm.getActiveBody();

			//BackendTester.safePrintln("SootMethod : " + sm.getSignature());

			Value retArg = null;

			// todo setting Class private Variables for Widening

			Stack<Block> BlockStack = new Stack<Block>();

			Body b = sm.getActiveBody();
			try
			{
				// response parse needs to be shimple - 20150918 hnamkoong
				// to prevent BFTtable json object duplicate case 
				if (isForward)
				{
					b = Shimple.v().newBody(sm.getActiveBody());
				}
			} catch (Exception e)
			{
				System.out.println("simple body to jimple body fail");
				e.printStackTrace();
				return;
			}

			BriefBlockGraph Bg = new BriefBlockGraph(b);

			// todo Graph Creation
			// DirectedGraph<String, DefaultEdge> BgStringGraph =
			// TODO aMark] 001. Draw Graph
//			if(sm.getSignature().equals("<in.shick.ted.threads.DownloadThreadsTask: java.lang.Boolean doInBackground(java.lang.Void[])>")) {
//				System.out.println("----------------" + sm.getName() +"----------------");
//				createStringGraph(Bg);
//				System.out.println(Bg);
//				System.out.println("----------------------------------------------------");
//			}
			// Init Symboltables


//			if(sm.getSignature().equals("<com.espn.androidplayersdk.datamanager.FeedsManagerAPI: void getEventsForTopSport(java.lang.String,java.util.HashMap,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean)>")) {
//				System.out.println("111");
//			}else if(sm.getSignature().equals("<com.espn.androidplayersdk.datamanager.FeedsServerCom: void execTask(java.lang.String,int,int,com.espn.androidplayersdk.datamanager.FeedsCommListener,java.util.HashMap)>")) {
//				System.out.println("222");
//			}else if(sm.getSignature().equals("<com.espn.androidplayersdk.datamanager.FeedsServerCom$execTaskRunnable: void run()>")) {
//				System.out.println("333");
//			}else if(sm.getSignature().equals("<com.espn.androidplayersdk.datamanager.FeedsServerCom$execTaskRunnable: org.json.JSONObject httpGetJson(java.lang.String)>")) {
//				System.out.println("444");
//			}else if(sm.getSignature().equals("<com.espn.androidplayersdk.datamanager.FeedsUpdateListener: void update(org.json.JSONObject,java.lang.String,java.util.HashMap)>")) {
//				System.out.println("555");
//			}else if(sm.getSignature().equals("<com.espn.androidplayersdk.datamanager.GeneralParser: void parse(org.json.JSONObject,java.util.HashMap)>")) {
//				System.out.println("666");
//			}else if(sm.getSignature().equals("<com.espn.androidplayersdk.datamanager.TopSportParser: void parse(org.json.JSONObject,java.util.HashMap)>")) {
//				System.out.println("777");
//			}
//			else {
//				return;
//			}

			// TODO aMark] 001. start
//			if(sm.getSignature().contains("parse")) {
//			System.out.println();
//			for (int i = 0; i <= VisitTable.size(); i++)
//				System.out.print("\t");
//			System.out.print("start ( " + sm.getSignature() + " ) ------");
//			System.out.println();
//			}

//			if (sm.getSignature().equals("<com.dozuki.ifixit.util.api.ApiEndpoint$15: com.dozuki.ifixit.util.api.ApiEvent parse(java.lang.String)>"))
//			{
//				System.out.println("aa");
//			}

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
			if (isUrlpath)
			{ // when this is called from urlParser
				for (int i = 0; i < Bg.size(); i++)
				{
					isBlockContainDP[i] = this.isContainDP(Bg.getBlocks().get(i));
				}
			}

			VisitEntry[Bg.getBlocks().indexOf(bl)] = 1;
			VisitTable.put(sm.getSignature(), VisitEntry);

			HashMap<String, HashMap<String, ArrayList<BFNode>>> BFTtableMap = new HashMap<String, HashMap<String, ArrayList<BFNode>>>();
			HashMap<String, HashMap<String, ArrayList<PairingEPEntry>>> paringInfoTableMap = new HashMap<String, HashMap<String, ArrayList<PairingEPEntry>>>();

			CalcEdgeTable(EdgeEntry, Bg);
			CalcEdgeTable(original_EdgeTable, Bg);

			LinkedList<Block> BFSentry = new LinkedList<Block>();

			BFSentry.add(bl);
			

			if (isUrlpath)
			{ // when this is called from urlParser
				while (!BFSentry.isEmpty())
				{
					Block nextBlock = null;

					for (Object object : BFSentry)
					{
						Block block = (Block) object;
						int blockNumber = Bg.getBlocks().indexOf(block);
						if (isBlockContainDP[blockNumber] == false)
						{
							nextBlock = block;
						}
					}
					if (nextBlock == null)
						nextBlock = BFSentry.getLast();

					/*
					 * merge BFT tables
					 */

					int blockNumber = Bg.getBlocks().indexOf(nextBlock);
					//					if(sm.getSignature().equals("<com.pushio.manager.tasks.PushIORegisterTask: java.lang.Integer doInBackground(java.lang.Void[])>")) {
					//						if(blockNumber == 6) {
					//							blockNumber = blockNumber;
					//						}
					//					}
					HashMap<String, ArrayList<BFNode>> blockBFTtable;
					HashMap<String, ArrayList<PairingEPEntry>> blockPairingInfoTable;

					if (bl.equals(nextBlock))
					{ // seed BFTtable
						blockBFTtable = (HashMap<String, ArrayList<BFNode>>) BFTtable.clone();
						blockPairingInfoTable = (HashMap<String, ArrayList<PairingEPEntry>>) pairingInfoTable.clone();
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
						blockPairingInfoTable = MergePairingInfoTables(paringInfoTableMap, blockNumberSet);
					}

					BFTtableMap.put(String.valueOf(blockNumber), blockBFTtable);
					paringInfoTableMap.put(String.valueOf(blockNumber), blockPairingInfoTable);

					BlockGraphTraveler(Bg, nextBlock, SliceM, Shimple.v().newBody(sm.getActiveBody()).getMethod(), blockBFTtable, isUrlpath, iCfg, 0, BFSentry,
							VisitTable.get(sm.getSignature()), EdgeEntry, null, history, id, blockPairingInfoTable, null);
					BFSentry.remove(nextBlock);
				}
			}

			if (isForward)
			{
				while (!BFSentry.isEmpty())
				{
					Block nextBlock = BFSentry.getFirst();

					BlockGraphTraveler(Bg, nextBlock, SliceM, Shimple.v().newBody(sm.getActiveBody()).getMethod(), BFTtable, isUrlpath, iCfg, 0, BFSentry,
							VisitTable.get(sm.getSignature()), EdgeEntry, taintVariableMap, history, id, null, responsePairingInfo);
					BFSentry.remove(nextBlock);
				}

				Set<String> seedSet = new HashSet<String>(taintVariableMap.values());
				if (seedSet != null && seedSet.size() > 0)
				{
					for (String var : seedSet)
					{
						if (taintParameterSet.contains(var))
						{
							String result = GenRegex(taintVariableMap, BFTtable, var);
							if (result != null && result != "")
							{
								ForwardRtnParameterTableStack.getLast().put(var, result);
							}
						} else if (ForwardRtnVariableStack.getLast() != null && ForwardRtnVariableStack.getLast().equals(var))
						{
							ForwardRtnValueStack.removeLast();
							ForwardRtnValueStack.addLast(GenRegex(taintVariableMap, BFTtable, ForwardRtnVariableStack.getLast()));
						} else
						{
							String result = GenRegex(taintVariableMap, BFTtable, var);
							if (!result.equals(""))
							{
								System.out.println();
								System.out.println("\t\t" + sm.getSignature());
								System.out.println("\t\t[response] " + result);
								System.out.println("\t\tEP_SM : " + responsePairingInfo.get(var).getEP_SM());
								System.out.println("\t\tEP_Stmt : " + responsePairingInfo.get(var).getEPStmt());
								System.out.println();

								String key = Constants.EPcont.getDP().toString() + Constants.EPcont.getDPStmt().toString() + result;
								PairingDPEntry dpentry = Constants.pairingInfoContainer.get(key);
								if (dpentry == null)
								{
									dpentry = new PairingDPEntry(Constants.EPcont.getDP(), Constants.EPcont.getDPStmt(), result);
									Constants.pairingInfoContainer.put(key, dpentry);
								}
								dpentry.addEP(new PairingEPEntry(responsePairingInfo.get(var).getEP_SM(), responsePairingInfo.get(var).getEPStmt()));
							}
						}
					}
				}
			}
			//			if(sm.getSignature().equals("<in.shick.ted.mail.InboxListActivity$DownloadMessagesTask: java.lang.Void doInBackground(java.lang.Integer[])>")) {
			//				System.out.println("exit");
			//				System.exit(1);
			//			}

			VisitTable.remove(sm.getSignature().toString());

			// TODO aMark] 002. end
//			if(sm.getSignature().contains("parse")) {
//			System.out.println();
//			for (int i = 0; i <= VisitTable.size(); i++)
//				System.out.print("\t");
//			System.out.print("end");
//			System.out.println();
//			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void BlockGraphTraveler(BriefBlockGraph Bg, Block b, SliceManager SliceM, SootMethod sm, HashMap<String, ArrayList<BFNode>> BFTtable, boolean isUrlpath,
			IInfoflowCFG iCfg, int depth, LinkedList<Block> BFSentry, Integer[] VisitTableEntry, Integer[][] EdgeEntry, HashMap<String, String> taintVariableMap,
			Stack<String> history, int id, HashMap<String, ArrayList<PairingEPEntry>> pairingInfoTable, HashMap<String, PairingEPEntry> responsePairingInfo)
			throws Exception
	{

		Body body = Bg.getBody();

		// TODO aMark] 003. block and unit
//		if (sm.getSignature().equals("<com.espn.androidplayersdk.datamanager.TopSportParser: void parse(org.json.JSONObject,java.util.HashMap)>"))
//		{
//			System.out.println();
//			for (int i = 0; i <= VisitTable.size()-1; i++)
//				System.out.print("\t");
//			System.out.println("  block number : " + Bg.getBlocks().indexOf(b));
//		}
		for (Iterator<Unit> UnitintheBlock = b.iterator(); UnitintheBlock.hasNext();)
		{
			if (Constants.foundDPStmt || Constants.stopVisitMethod())
				return;
			Unit ut = UnitintheBlock.next();
//			if (sm.getSignature().equals("<com.espn.androidplayersdk.datamanager.GeneralParser: void parse(org.json.JSONObject,java.util.HashMap)>"))
//			{
//				for (int i = 0; i <= VisitTable.size()-1; i++)
//					System.out.print("\t");
//				System.out.println("    Unit : " + ut.toString());
//			}

//			if(sm.getSignature().contains("$r2 = specialinvoke $r0.<com.espn.androidplayersdk.datamanager.FeedsServerCom$execTaskRunnable: org.json.JSONObject httpGetJson(java.lang.String)>($r1)")) {
//				System.out.println("Unit : " + ut.toString());
//			}
				
//			if(ut.toString().contains("$r2 = specialinvoke $r0.<com.espn.androidplayersdk.datamanager.FeedsServerCom$execTaskRunnable: org.json.JSONObject httpGetJson(java.lang.String)>($r1)")) {
//				System.out.println("a");
//			}
				


			//			int line_deep []= Constants.line_deep.get(id);
			//
			//			line_deep[0]++;
			//
			//			// TODO WOOMADE
			//
			//			if(line_deep[0]==1)
			//			{
			//				nowdeep++;
			//				line_deep[1]=nowdeep;
			//			}
			//			if(nowdeep!=line_deep[1])
			//
			//				nowdeep--;
			//
			//			if(nowdeep==0)
			//				line_deep[1]=0;
			//
			//
			//			for (int i = 0; i < nowdeep; i++)
			//				BackendTester.safePrint("\t");
			//			BackendTester.safePrintln(line_deep[0] + ":	" + ut.toString());

			String strDest = null;
			initFlag = false;

			// TODO aMark] 01. ut instanceof AssignStmt
			if (ut instanceof AssignStmt)
			{
				AssignStmt as = (AssignStmt) ut;
				strDest = as.getLeftOp().toString();

				if (!hasNode(BFTtable, strDest))
				{
					BFTtable.put(strDest, new ArrayList<BFNode>());
				}

				if (isForward && isDpStmt(sm, ut))
				{
					putSeedInTaintVariableMap(strDest, taintVariableMap);
					responsePairingInfo.put(strDest, new PairingEPEntry(sm.toString(), ut.toString()));
//					System.out.println("found dp!!");
					
				}
				
				
				if (as.getRightOp() instanceof PhiExpr)
				{
					PhiExpr pe = (PhiExpr) as.getRightOp();

					for (Value v : pe.getValues())
					{
						String phiVar = v.toString();
						// Left argument can be in the Phi-node. We should skip
						// this case.
						if (strDest.equals(phiVar))
						{
							continue;
						}

						if (isForward)
						{
							forwardTaint(strDest, phiVar, taintVariableMap);
						}

						if (!hasNode(BFTtable, phiVar))
						{
							BFTtable.put(phiVar, new ArrayList<BFNode>());
						}

						BFNode bfn = new BFNode();
						bfn.makePhinodeBfn(phiVar);
						ArrayList<BFNode> list = BFTtable.get(strDest); 
						list.add(bfn);
					}
				}

				// TODO aMark] 02. ut instanceof AssignStmt & contain invoke
				// expr
				else if (as.containsInvokeExpr())
				{
					// expr & static
					if (as.getInvokeExpr() instanceof StaticInvokeExpr)
					{
						StaticInvokeExpr sie = (StaticInvokeExpr) as.getInvokeExpr();
						InvokeHandler(Invoke_Type_Assign_Static, sie, SliceM, BFTtable, iCfg, ut, sm, history, body, taintVariableMap, isUrlpath, pairingInfoTable,
								responsePairingInfo);
					}

					// expr & virtual
					else if (as.getInvokeExpr() instanceof VirtualInvokeExpr)
					{
						VirtualInvokeExpr vie = (VirtualInvokeExpr) as.getInvokeExpr();
						InvokeHandler(Invoke_Type_Assign_Virtual, vie, SliceM, BFTtable, iCfg, ut, sm, history, body, taintVariableMap, isUrlpath, pairingInfoTable,
								responsePairingInfo);

					}

					// expr & interface
					else if (as.getInvokeExpr() instanceof InterfaceInvokeExpr)
					{
						InterfaceInvokeExpr ife = (InterfaceInvokeExpr) as.getInvokeExpr();
						InvokeHandler(Invoke_Type_Assign_Interface, ife, SliceM, BFTtable, iCfg, ut, sm, history, body, taintVariableMap, isUrlpath, pairingInfoTable,
								responsePairingInfo);
					}

					// expr & special
					else if (as.getInvokeExpr() instanceof SpecialInvokeExpr)
					{
						SpecialInvokeExpr sie = (SpecialInvokeExpr) as.getInvokeExpr();
						InvokeHandler(Invoke_Type_Assign_Special, sie, SliceM, BFTtable, iCfg, ut, sm, history, body, taintVariableMap, isUrlpath, pairingInfoTable,
								responsePairingInfo);
					}

				}
				// TODO aMark] 03. ut instanceof AssignStmt & NOT contain invoke
				// expr
				else
				{
					if (isUrlpath)
					{
						String cropLeftOp = CropVar(as.getLeftOp().toString());
						String cropRightOp = CropVar(as.getRightOp().toString());

						// left op is heap object
						if (cropLeftOp.startsWith("<") && cropLeftOp.endsWith(">"))
						{
							String LeftSootValue = cropLeftOp;

							// if $v.<> = $v.<>
							if (cropRightOp.startsWith("<") && cropRightOp.endsWith(">"))
							{
								String RightSootValue = cropRightOp;
								// turn off heap object cycle.  Use only linear serach 20150920 hnamkoong
//								ted_HeapHandler.updateHeapTableWithSootValue(LeftSootValue, RightSootValue);
							}
							// if $v.<> = constant
							else if (as.getRightOp() instanceof Constant)
							{
//								ted_HeapHandler.updateHeapTable(LeftSootValue, as.getRightOp().toString());
							}
							// if $v.<> = $v
							else if (BFTtable.get(as.getRightOp().toString()) != null)
							{
								String var = as.getRightOp().toString();
								// turn off heap object assign. Use only linear serach 20150920 hnamkoong
								ted_HeapHandler.updateHeapTable(LeftSootValue, GenRegex(null, BFTtable, var));
							}
							// if $v.<> = new or ...
							// do nothing
							else
							{
								ted_HeapHandler.updateHeapTable(LeftSootValue, as.getLeftOp().getType().equals("java.lang.Integer") ? "[0-9]*" : "(.*)");
							}
						} else
						{
							String var = as.getLeftOp().toString();

							ArrayList<BFNode> list = BFTtable.get(var);
							if (list == null)
								list = new ArrayList<BFNode>();
							list.clear();

							ArrayList<PairingEPEntry> plist = pairingInfoTable.get(var);
							if (plist == null)
								plist = new ArrayList<PairingEPEntry>();
							plist.clear();

							// if $v = $v.<>
							if (cropRightOp.startsWith("<") && cropRightOp.endsWith(">"))
							{
								String RightSootValue = cropRightOp;
								BFNode bfn = new BFNode();
								bfn.makeHeapObjectBfn(RightSootValue);
								list.add(bfn);
							}
							// if $v = constant
							else if (as.getRightOp() instanceof Constant)
							{
								BFNode bfn = new BFNode();
								bfn.makeUrlBfn(as.getRightOp().toString());
								list.add(bfn);
							}
							// if $v = $v
							else if (BFTtable.get(as.getRightOp().toString()) != null)
							{
								String rightVar = as.getRightOp().toString();
								BFTtable.put(var, BFTtable.get(rightVar));
								pairingInfoTable.put(var, pairingInfoTable.get(rightVar));
							}
							// if $v = new or ...
							else
							{
								BFNode bfn = new BFNode();
								bfn.makeUrlBfn(as.getLeftOp().getType().equals("java.lang.Integer") ? "[0-9]*" : "(.*)");
								list.add(bfn);
							}
						}
					} else
					{
						if (isForward)
						{
							String[] split = as.getRightOp().toString().split(" ");
							String temp = split[split.length - 1];
							String var = temp.split(";")[0];
							
							forwardTaint(strDest.split("\\.")[0], var, taintVariableMap);
						}
					}
				}
			}

			// TODO aMark] 04. ut instanceof IdentityStmt
			else if (ut instanceof IdentityStmt)
			{

				IdentityStmt ds = (IdentityStmt) ut;
				strDest = ds.getLeftOp().toString();

				if (!hasNode(BFTtable, strDest))
				{
					ArrayList<BFNode> list = new ArrayList<BFNode>();
					BFNode bfn = new BFNode();
					bfn.makeUrlBfn(ds.getLeftOp().getType().equals("java.lang.Integer") ? "[0-9]*" : "(.*)");
					list.add(bfn);
					BFTtable.put(strDest, list);
				}

				if (ds.getRightOp().toString().indexOf("@parameter") != -1)
				{
					String param = ds.getRightOp().toString().split(":")[0];
					// when response slice, arg value is not our concern.
					if (isForward)
					{
						if (taintVariableMap.keySet().contains(param))
						{
							taintVariableMap.put(strDest, taintVariableMap.get(param));
						}
					} else
					{
						if (BFTtable.get(param) != null)
							BFTtable.put(strDest, BFTtable.get(param));
						if (pairingInfoTable.get(param) != null)
							pairingInfoTable.put(strDest, pairingInfoTable.get(param));
					}
				}
			}

			// TODO aMark] 05. ut instanceof ReturnStmt
			else if (ut instanceof ReturnStmt)
			{
				ReturnStmt rs = (ReturnStmt) ut;

				if (isUrlpath)
				{
					BFNode bfn = new BFNode();
					if (rs.getOp() instanceof Constant)
					{
						BackwardRtnValueStack.removeLast();
						BackwardRtnValueStack.addLast(rs.getOp().toString().replace("\"", ""));

					} else
					{
						String variable = rs.getOp().toString();
						BackwardRtnValueStack.removeLast();
						BackwardRtnValueStack.addLast(GenRegex(null, BFTtable, variable));
						ForwardRtnPairingListStack.removeLast();
						ForwardRtnPairingListStack.addLast(CopyPairingList(pairingInfoTable.get(variable)));
					}
				}

				if (isForward)
				{
					if(ut.toString().equals("return $r7_1")) {
						System.out.println("ab");
					}
					if (!rs.getOp().toString().equals("null") && !(rs.getOp() instanceof Constant))
					{
						String variable = rs.getOp().toString();
						if (taintVariableMap.get(variable) != null)
						{
							ForwardRtnVariableStack.removeLast();
							ForwardRtnVariableStack.addLast(taintVariableMap.get(variable));
						}
					}
				}
			}
			// TODO aMark] 06. ut instanceof InvokeStmt
			else if (ut instanceof InvokeStmt)
			{
				InvokeStmt ivs = (InvokeStmt) ut;

				if (ivs.getInvokeExpr() instanceof StaticInvokeExpr)
				{
					StaticInvokeExpr sie = (StaticInvokeExpr) ivs.getInvokeExpr();
					InvokeHandler(Invoke_Type_Not_Assign_Static, sie, SliceM, BFTtable, iCfg, ut, sm, history, body, taintVariableMap, isUrlpath, pairingInfoTable,
							responsePairingInfo);
				}

				else if (ivs.getInvokeExpr() instanceof VirtualInvokeExpr)
				{
					VirtualInvokeExpr vie = (VirtualInvokeExpr) ivs.getInvokeExpr();
					InvokeHandler(Invoke_Type_Not_Assign_Virtual, vie, SliceM, BFTtable, iCfg, ut, sm, history, body, taintVariableMap, isUrlpath, pairingInfoTable,
							responsePairingInfo);
				}

				else if (ivs.getInvokeExpr() instanceof InterfaceInvokeExpr)
				{
					InterfaceInvokeExpr ife = (InterfaceInvokeExpr) ivs.getInvokeExpr();
					InvokeHandler(Invoke_Type_Not_Assign_Interface, ife, SliceM, BFTtable, iCfg, ut, sm, history, body, taintVariableMap, isUrlpath, pairingInfoTable,
							responsePairingInfo);
				}

				else if (ivs.getInvokeExpr() instanceof SpecialInvokeExpr)
				{
					SpecialInvokeExpr sie = (SpecialInvokeExpr) ivs.getInvokeExpr();
					InvokeHandler(Invoke_Type_Not_Assign_Special, sie, SliceM, BFTtable, iCfg, ut, sm, history, body, taintVariableMap, isUrlpath, pairingInfoTable,
							responsePairingInfo);
				}
			}
		}

		// TODO aMark] 07. ut loop done
		// todo jump another Block
		for (Block tg : Bg.getSuccsOf(b))
		{
			EdgeEntry[Bg.getBlocks().indexOf(b)][Bg.getBlocks().indexOf(tg)] = 0;
		}

		for (Block b2 : Bg.getBlocks())
		{
			if (VisitTableEntry[Bg.getBlocks().indexOf(b2)] == 0)
			{
				if (BlockSelector(Bg.getBlocks().indexOf(b2), Bg, sm.getSignature(), EdgeEntry))
				{
					BFSentry.add(b2);
					VisitTableEntry[Bg.getBlocks().indexOf(b2)] = 1;
				}
			}
		}
	}

	private boolean isAssignStmt(int invokeType)
	{
		if (Invoke_Type_Assign_Static <= invokeType && invokeType <= Invoke_Type_Assign_Special)
			return true;
		return false;
	}

	private boolean isInstanceInvoke(int invokeType)
	{
		if (invokeType == Invoke_Type_Assign_Static || invokeType == Invoke_Type_Not_Assign_Static)
			return false;
		return true;

	}

	protected boolean isStaticInvoke(int invokeType)
	{
		if (invokeType == Invoke_Type_Assign_Static || invokeType == Invoke_Type_Not_Assign_Static)
			return true;
		return false;
	}

	private void InvokeHandler(int invokeType, InvokeExpr ie, SliceManager SliceM, HashMap<String, ArrayList<BFNode>> BFTtable, IInfoflowCFG iCfg, Unit ut,
			SootMethod sm, Stack<String> history, Body body, HashMap<String, String> taintVariableMap, boolean isUrlpath,
			HashMap<String, ArrayList<PairingEPEntry>> pairingInfoTable, HashMap<String, PairingEPEntry> responsePairingInfo)
	{
		try
		{
			AssignStmt as = null;
			String strDest = null;
			boolean isInterface = false;
			if (isAssignStmt(invokeType))
			{
				as = (AssignStmt) ut;
				strDest = as.getLeftOp().toString();
			}
			Set<SootMethod> SootMethodSet = null;
			SootMethod[] sootMethodArray;
			if (!isForward && (invokeType == Invoke_Type_Assign_Interface || invokeType == Invoke_Type_Not_Assign_Interface))
			{
				SootMethodSet = SearchMethod_interface(ut, sm);
				isInterface = true;

			} else
			{
				SootMethodSet = SearchMethod(SliceM, ie.getMethodRef().getSignature(), ut.toString(), ut, sm, history);
				// do bubble sort to unify call sequence
				// 20150919 hnamkoong

			}
			
			sootMethodArray = SootMethodSet.toArray(new SootMethod[SootMethodSet.size()]);
			for (int i = 0; i < sootMethodArray.length; i++)
			{
				for (int j = i + 1; j < sootMethodArray.length; j++)
				{
					if (sootMethodArray[i].toString().compareTo(sootMethodArray[j].toString()) > 0)
					{
						SootMethod temp = sootMethodArray[i];
						sootMethodArray[i] = sootMethodArray[j];
						sootMethodArray[j] = temp;
					}
				}
			}

			// 20150914 hnamkoong
			// $r4 = virtualinvoke $r4.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>($r5)
			// should clear list when there's no semantic or search method
			//			if(SootMethodSet == null || SootMethodSet.size() == 0) {
			//				if(strDest != null) {
			//					ArrayList<BFNode> list = new ArrayList<BFNode>();
			//					BFTtable.put(strDest, list);
			//					BFNode bfn = new BFNode();
			//					if(as.getLeftOp().getType().toString().equals("java.lang.Integer")) {
			//						bfn.makeUrlBfn("[0-9]*");
			//					}else {
			//						bfn.makeUrlBfn("(.*)");
			//					}
			//					list.add(bfn);
			//				}
			//			}

			// you need to distinguish this case
			// SootMethodSet.size() == 0 / append semantic
			// 		$r2 = virtualinvoke $r2.<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>("abc.com")
			// SootMethodSet.size() > 1 / interface invoke. multiple returns
			// 		$r2 = interfaceinvoke $r5.<com.dozuki.ifixit.util.api.ApiEndpoint$Endpoint: java.lang.String createUrl(java.lang.String)>($r2)
			// 20150919 hnamkoong

			String tempDest = "$TEMPDEST$";
			if (SootMethodSet.size() > 0)
			{
				if (strDest != null)
				{
					BFTtable.put(tempDest, new ArrayList<BFNode>());
					if (pairingInfoTable != null)
						pairingInfoTable.put(tempDest, pairingInfoTable.get(strDest));
				}
			}

			for (SootMethod subsm : sootMethodArray)
			{
				if (subsm != null && !subsm.getSignature().equals(sm.getSignature()))
				{
					HashMap<String, ArrayList<BFNode>> SubBFTtable = new HashMap<String, ArrayList<BFNode>>();
					HashMap<String, ArrayList<PairingEPEntry>> SubPairingInfoTable = new HashMap<String, ArrayList<PairingEPEntry>>();
					HashMap<String, PairingEPEntry> subResponsePairingInfo = new HashMap<String, PairingEPEntry>();
					CopyParam(ie, isUrlpath, SubBFTtable, BFTtable, body, sm, pairingInfoTable, SubPairingInfoTable);
					/*
					 * sub pairing EP Entry copy needed
					 */

					if (subsm.hasActiveBody())
					{
						Set<String> taintParameterSet = null;
						HashMap<String, String> subTaintVariableMap = null;
						if (isForward)
						{
							taintParameterSet = new HashSet<String>();
							setTaintParameter(ie, taintVariableMap, taintParameterSet);

							subTaintVariableMap = new HashMap<String, String>();
							setSubTaintVariableMap(subTaintVariableMap, taintParameterSet);
						}
						if (isInstanceInvoke(invokeType))
						{
							InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
							history.push(iie.getBase().getType().toString());
						}

						if(ut.toString().contains("interfaceinvoke $r3_1.<com.espn.androidplayersdk.datamanager.JSONParserInterface: void parse(org.json.JSONObject,java.util.HashMap)>($r1, $r2)")) {
							System.out.println("------------------------start------------------------------");
						}
						//history update
						ExtractStmt(SliceM, subsm, SubBFTtable, isUrlpath, iCfg, taintParameterSet, subTaintVariableMap, history, SubPairingInfoTable,
								subResponsePairingInfo);

						if(ut.toString().contains("interfaceinvoke $r3_1.<com.espn.androidplayersdk.datamanager.JSONParserInterface: void parse(org.json.JSONObject,java.util.HashMap)>($r1, $r2)")) {
							System.out.println("------------------------end------------------------------");
							System.out.println("@parameter0 : " + ForwardRtnParameterTableStack.getLast().get("@parameter0"));
						}

						if (isInstanceInvoke(invokeType))
						{
							history.pop();
						}

						if (isForward)
						{
							setTreeFromRetTable(BFTtable, ie, taintVariableMap);
							if (strDest != null)
							{
								taintReturnValue(strDest, BFTtable, taintVariableMap, sm, ut, responsePairingInfo);
							}
						}
						if (strDest != null)
						{
							handleGlobalRetTable(tempDest, BFTtable, taintVariableMap, isInterface, pairingInfoTable);
						}
						
						BackwardRtnValueStack.removeLast();
						ForwardRtnValueStack.removeLast();
						ForwardRtnVariableStack.removeLast();
						ForwardRtnPairingListStack.removeLast();
						ForwardRtnParameterTableStack.removeLast();
						ForwardGsonTrackingVarStack.removeLast();
						

					}

					if (invokeType == Invoke_Type_Not_Assign_Special)
						SliceM.setSpecialinvoke(false);
				}
			}
			if (SootMethodSet.size() > 0)
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

			if (isStaticInvoke(invokeType))
			{
				StaticInvokeExpr sie = (StaticInvokeExpr) ie;
				ExtractingExpr(sie, BFTtable, ut, sm.getName(), sm, strDest, pairingInfoTable);
			} else
			{
				InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
				ExtractingExpr(iie, BFTtable, ut, sm.getName(), sm, strDest, pairingInfoTable);
			}

			if (!isUrlpath)
			{
				if (isInstanceInvoke(invokeType))
				{
					InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
					String base = iie.getBase().toString();
					if (strDest != null)
					{
						forwardTaint(strDest, base, taintVariableMap);
					}
					for (Value v : iie.getArgs())
					{
						String arg = v.toString();
						forwardTaint(base, arg, taintVariableMap);
						if (strDest != null)
							forwardTaint(strDest, arg, taintVariableMap);

					}
				} else
				{
					StaticInvokeExpr sie = (StaticInvokeExpr) ie;
					for (Value v : ie.getArgs())
					{
						String arg = v.toString();
						if (strDest != null)
							forwardTaint(strDest, arg, taintVariableMap);

					}
				}
			}
		} catch (Exception e)
		{
			// FIXME Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Set<SootMethod> SearchMethod_interface(Unit ut, SootMethod parentSm)
	{
		Unit JimpleUnit = ut;

		Set<SootMethod> SootMethodSet = new HashSet<SootMethod>();

		if (Constants.serializationMode)
		{
			List<String> getCallees = Constants.sCFG.getCalleesAt(parentSm.getSignature(), JimpleUnit.toString());

			if (getCallees != null && getCallees.size() > 0)
			{
				for (String str : getCallees)
				{
					if (Constants.TaintFunctions.contains(str) && !VisitTable.containsKey(str))
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

	// response taint. (strDest) = (base).method(args)

	// TODO aMark] response taint 1. isDpStmt -> taint start
	private boolean isDpStmt(SootMethod sm, Unit ut)
	{
		String smSignature = Constants.EPcont.getDP().getSignature();
		String strDest = ((AssignStmt) Constants.EPcont.getDPStmt()).getLeftOp().toString();
		AssignStmt dpAs = ((AssignStmt) Constants.EPcont.getDPStmt());
		AssignStmt as = (AssignStmt) ut;
		if(as.getRightOp() instanceof InvokeExpr) {
			if (sm.getSignature().equals(smSignature) &&
					((InvokeExpr) dpAs.getRightOp()).getMethodRef().getSignature().equals(((InvokeExpr) as.getRightOp()).getMethodRef().getSignature()) &&
					as.getLeftOp().toString().split("_")[0].equals(strDest))
			{
				return true;
			}
		}

		return false;
	}

	// TODO aMark] response taint 2. putSeed
	private void putSeedInTaintVariableMap(String seed, HashMap<String, String> taintVariableMap)
	{
		taintVariableMap.put(seed, seed);
	}

	// TODO aMark] response taint 3. forwardTaint -> basic taint
	private void forwardTaint(String taintee, String tainter, HashMap<String, String> taintVariableMap)
	{
		if (taintVariableMap.keySet().contains(tainter))
			taintVariableMap.put(taintee, taintVariableMap.get(tainter));
	}

	// TODO aMark] response taint 4. passing parameter taint
	private void setTaintParameter(InvokeExpr ie, HashMap<String, String> taintVariableMap, Set<String> taintParameterSet)
	{
		for (int i = 0; i < ie.getArgCount(); i++)
		{
			String arg = ie.getArg(i).toString();
			if (taintVariableMap.containsKey(arg))
			{
				taintParameterSet.add("@parameter" + i);
			}
		}
	}

	// TODO aMark] response taint 5. taint ret value
	private void taintReturnValue(String dest, HashMap<String, ArrayList<BFNode>> BFTtable, HashMap<String, String> taintVariableMap, SootMethod sm, Unit ut,
			HashMap<String, PairingEPEntry> responsePairingInfo)
	{
		if (ForwardRtnVariableStack.getLast() != null)
		{
			putSeedInTaintVariableMap(dest, taintVariableMap);
			responsePairingInfo.put(dest, new PairingEPEntry(sm.toString(), ut.toString()));
		}
	}

	// TODO aMark] response z 6. set parameter value
	private void setTreeFromRetTable(HashMap<String, ArrayList<BFNode>> BFTtable, InvokeExpr ie, HashMap<String, String> taintVariableMap)
	{
		for (int i = 0; i < ie.getArgCount(); i++)
		{
			String arg = ie.getArg(i).toString();
			if (taintVariableMap.containsKey(arg))
			{
				String parameter = "@parameter" + i;
				String GenRegexValue = ForwardRtnParameterTableStack.getLast().get(parameter);
				ArrayList<BFNode> list = BFTtable.get(arg);
				BFNode bfn = new BFNode();
				bfn.makeResponseBfn(GenRegexValue, null, "GenRegex");
				list.add(bfn);
			}
		}
	}

	private void handleGlobalRetTable(String dest, HashMap<String, ArrayList<BFNode>> BFTtable, HashMap<String, String> taintVariableMap, boolean isInterface,
			HashMap<String, ArrayList<PairingEPEntry>> pairingInfoTable)
	{
		if (isInterface && !isForward)
		{
			if (BackwardRtnValueStack.getLast() != null)
			{

				ArrayList<BFNode> list = BFTtable.get(dest);

				if (list.size() == 0)
				{
					BFNode bfn = new BFNode();
					bfn.makeUrlBfn(BackwardRtnValueStack.getLast());
					list.add(bfn);
				} else
				{
					String pasturl = list.get(0).getStringForUrl();
					String newurl = pasturl + " | " + BackwardRtnValueStack.getLast();
					list.get(0).setStringForUrl(newurl);
				}
				if (ForwardRtnPairingListStack.getLast() != null)
				{
					if (pairingInfoTable.get(dest) == null)
						pairingInfoTable.put(dest, CopyPairingList(ForwardRtnPairingListStack.getLast()));
					else
					{
						for (PairingEPEntry e : ForwardRtnPairingListStack.getLast())
						{
							pairingInfoTable.get(dest).add(e);
						}
					}
				}
			}
		} else
		{
			if(!isForward) {
				if (BackwardRtnValueStack.getLast() != null) {
					BFNode bfn = new BFNode();
					bfn.makeUrlBfn(BackwardRtnValueStack.getLast());
					if (ForwardRtnPairingListStack.getLast() != null)
					{
						pairingInfoTable.put(dest, CopyPairingList(ForwardRtnPairingListStack.getLast()));
					}
					ArrayList<BFNode> list = new ArrayList<BFNode>();
					BFTtable.put(dest, list);
					list.add(bfn);
				}
			}else {
				if (ForwardRtnValueStack.getLast() != null) {
					BFNode bfn = new BFNode();
					bfn.makeResponseBfn(ForwardRtnValueStack.getLast(), null, "GenRegex");
					ArrayList<BFNode> list = new ArrayList<BFNode>();
					BFTtable.put(dest, list);
					list.add(bfn);
				}
			}
		}
	}

	private void setSubTaintVariableMap(HashMap<String, String> subTaintVariableMap, Set<String> taintParameterSet)
	{
		if (taintParameterSet != null)
		{
			for (String p : taintParameterSet)
			{
				putSeedInTaintVariableMap(p, subTaintVariableMap);
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

	private boolean isBackEdge(BriefBlockGraph Bg, String sig, int from, int to)
	{
		Body b = Bg.getBody();

		ExtractocolLoopFinder loopFinder = new ExtractocolLoopFinder();
		//Set<List<Stmt>> loops = loopFinder.getLoops(b);

		/*for (List<Stmt> lp : loops)
		{
			if (lp.size() < 2)
			{
				return false;
			}
			if (isBlockContainsStmt(Bg, from, lp.get(lp.size() - 2)) && isBlockContainsStmt(Bg, to, lp.get(lp.size() - 1)))
			{
				return true;
			}
		}*/
		return false;
	}

	private boolean BlockSelector(int bidx, BriefBlockGraph Bg, String sig, Integer[][] EdgeEntry)
	{
		for (int i = 0; i < Bg.getBlocks().size(); i++)
		{
			if (EdgeEntry[i][bidx] == 1 && !isBackEdge(Bg, sig, i, bidx))
			{
				return false;
			}
		}
		return true;
	}

	// WOOMADE
	private static String OPparsing(String s)
	{
		String k;
		if (s.contains("["))
		{
			int index = s.indexOf("[");
			k = s.substring(0, index);
		} else
		{
			k = s;
		}
		return k;
	}

	private void PrintTree(Tree bfTree)
	{

		if (bfTree == null)
			return;

		Iterator<BFNode> ibf = bfTree.iterator();
		for (BFNode bfn = ibf.next(); ibf.hasNext(); bfn = ibf.next())
		{
			System.out.print(bfn.getSootValue() != null ? bfn.getSootValue().toString() : bfn.getKey() != null ? bfn.getKey() : "" + " ");
		}
	}

	private String CropVar(String var)
	{
		if (var.startsWith("$"))
			return var.substring(var.indexOf(".") + 1);
		else
			return var;
	}

	private void CopyParam(InvokeExpr vie, boolean isUrlpath, HashMap<String, ArrayList<BFNode>> SubBFTtable, HashMap<String, ArrayList<BFNode>> BFTtable, Body body,
			SootMethod sm, HashMap<String, ArrayList<PairingEPEntry>> pairingInfoTable, HashMap<String, ArrayList<PairingEPEntry>> subPairingInfoTable)
	{
		int paramcount = 0;

		//runnable
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
			//			if (v.getType().toString().indexOf("JSON") != -1
			//					|| (isUrlpath && (v.getType().toString().indexOf("java.net.URL") != -1
			//					|| v.getType().toString().indexOf("StringBuilder") != -1
			//					|| v.getType().toString().contains("android.net.Uri")
			//					|| v.getType().toString().indexOf("String") != -1
			//					|| v.getType().toString().indexOf("java.lang.String[]") != -1)
			//					|| v.getType().toString().equals("java.lang.String[]")))
			//			{

			if (!isForward)
			{
				String stringForUrl;
				if (v instanceof Constant)
				{
					stringForUrl = v.toString();
				} else
				{
					stringForUrl = GenRegex(null, BFTtable, v.toString());
				}

				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(stringForUrl);
				ArrayList<BFNode> list = new ArrayList<BFNode>();
				list.add(bfn);
				SubBFTtable.put("@parameter" + paramcount, list);

				// pairing
				if (v instanceof Constant)
				{
				} else
				{
					ArrayList<PairingEPEntry> plist = pairingInfoTable.get(v.toString());
					subPairingInfoTable.put("@parameter" + paramcount, CopyPairingList(plist));
				}
			}

			paramcount++;
		}
	}

	/*
	 * public boolean isconst(String arg) { if (arg.getBytes()[0] == '"' || ('0'
	 * < arg.getBytes()[0] && arg.getBytes()[0] < '9')) { return true; } return
	 * false; }
	 */

	@SuppressWarnings("unchecked")
	public void printTree(Tree tr, String Treename) throws NodeNotFoundException
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

	@SuppressWarnings("unchecked")
	public void printHeap(Tree tr, String Treename) throws NodeNotFoundException, IOException
	{
		// System.out.println("===============================");
		// System.out.println("Tree " + Treename + " PreorderTraversal");

		//		FileWriter fw = new FileWriter(Constants.Path + "/heaps.txt", true);
		boolean hasHeap = false;
		// System.out.println("======================");

		for (Iterator<BFNode> iter = tr.preOrderTraversal().iterator(); iter.hasNext();)
		{
			BFNode bfn = iter.next();
			// System.out.println(bfn.getKey());
			if (bfn.getKey() == null)
			{
				if (bfn.getSootValue() != null)
				{
					// System.out.println(Treename + "->" +
					// bfn.getSootValue().toString());

					String sub = bfn.getSootValue().toString();
					if (sub.indexOf("<") > 0)
					{
						int start = sub.indexOf(".");
						sub = sub.substring(start + 1);
					}
					hasHeap = true;
					//					fw.write(sub + "\r\n");
				}
			}
		}

		// System.out.println("Tree depth : " + tr.depth());
		// System.out.println("======================");
	}

	@SuppressWarnings("unchecked")
	public void copytree(Tree DstTree, Tree SrcTree, BFNode parent)
	{
		try
		{
			DstTree.addAll(parent, SrcTree);

			for (Iterator<BFNode> iter = DstTree.children(parent).iterator(); iter.hasNext();)
			{
				BFNode bfn = iter.next();
				if (bfn.getKey() == null && bfn.getVtype() == null && bfn.getSootValue() == null)
				{
					DstTree.remove(bfn);
				}
			}

		} catch (NodeNotFoundException e)
		{
			e.printStackTrace();
		}
		// printTree(DstTree, "combined tree");
	}

	//	public Value GetHeap(Tree tr)
	//	{
	//		// System.out.println("===============================");
	//		// System.out.println("Tree " + Treename + " PreorderTraversal");
	//		for (Iterator<BFNode> iter = tr.preOrderTraversal().iterator(); iter.hasNext();)
	//		{
	//			BFNode bfn = iter.next();
	//			if (bfn.getKey() == null)
	//			{
	//				if (bfn.getSootValue() != null)
	//					return bfn.getSootValue();
	//			}
	//		}
	//		return null;
	//	}

	// ---------------------------------------------------WOOHYUN ADDED
	public static boolean isStringDouble(String s)
	{
		try
		{
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e)
		{
			return false;
		}
	}

	// ---------------------------------------------------WOOHYUN ADDED

	static void callException() throws ExtractocolException
	{
		throw new ExtractocolException();
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return tlist;
	}

	protected ArrayList<PairingEPEntry> CopyPairingList(ArrayList<PairingEPEntry> plist)
	{
		if (plist == null)
			return null;
		ArrayList<PairingEPEntry> tlist = new ArrayList<PairingEPEntry>();

		for (PairingEPEntry e : plist)
		{
			tlist.add(new PairingEPEntry(e.getEP_SM(), e.getEPStmt()));
		}
		return tlist;
	}
}