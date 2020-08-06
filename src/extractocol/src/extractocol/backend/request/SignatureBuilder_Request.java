
package extractocol.backend.request;

/* BufferExtractor Abstract class */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.gaurav.tree.NodeNotFoundException;
import com.gaurav.tree.Tree;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.basics.ContextEntry;
import extractocol.backend.request.basics.ContextTable;
import extractocol.backend.request.basics.ParameterBucket;
import extractocol.backend.request.heapManagement.HeapHandler;
import extractocol.backend.request.helper.ExtractocolLoopFinder;
import extractocol.backend.request.helper.SerializableCFG;
import extractocol.backend.request.semantic.url.UrlBuilder;
import extractocol.backend.request.unitHandle.AbstractUnitHandleDecorator;
import extractocol.backend.request.unitHandle.AssignStmtHandler;
import extractocol.backend.request.unitHandle.DebugDecorator;
import extractocol.backend.request.unitHandle.HeapTrackDecorator;
import extractocol.backend.request.unitHandle.IdentityStmtHandler;
import extractocol.backend.request.unitHandle.InvokeStmtHandler;
import extractocol.backend.request.unitHandle.ReturnStmtHandler;
import extractocol.backend.request.unitHandle.UnitType;
import extractocol.backend.response.helper.BlockTravelerHelper;
import extractocol.backend.response.helper.DebugHelper;
import extractocol.backend.response.semantic.ResponseSemantic;
import extractocol.backend.response.unitHandle.InvokeHandler;
import extractocol.common.helper.InvokeHelper;
import extractocol.common.outputs.BackendOutput;
import extractocol.common.pairing.SemanticAppliedEntry;
import extractocol.common.retrofit.RetrofitHandle;
import extractocol.common.trackers.tools.ArgToVEL;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntryTracking;
import extractocol.frontend.basic.MyCallGraphBuilder;
import extractocol.tester.Backend;
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
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;

public abstract class SignatureBuilder_Request
{
	public static List<String> SemanticModel;
	public static List<String> methodlist;
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
			String strDst, ParameterBucket pb) throws NodeNotFoundException;
	abstract public void AnalyzeExpression(StaticInvokeExpr sie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod currentMethod,
			String strDst, ParameterBucket pb);
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
	
	public static boolean MethodFiltering(String MethodSig)
	{
		return MethodSig.startsWith("<com.squareup.okhttp") || MethodSig.startsWith("<com.fasterxml.jackson") || MethodSig.startsWith("<com.snappydb.DBFactory")
				|| MethodSig.startsWith("<com.esotericsoftware.kryo.util") || MethodSig.startsWith("<java.net.URI") || MethodSig.startsWith("<com.skyhookwireless.wps._sdkob")
				|| MethodSig.startsWith("<java.lang.") || SemanticModel.contains(MethodSig) || MethodSig.startsWith("<org.codehaus.jackson") || MethodSig.startsWith("<okio.ByteString")
				|| MethodSig.startsWith("<com.google.inject.Key") || MethodSig.startsWith("<com.b.a") || MethodSig.startsWith("<org.bouncycastle") || MethodSig.startsWith("<com.google.android.gms.analytics")
				|| MethodSig.startsWith("<org.apache.http") || MethodSig.startsWith("<ch.boye") || MethodSig.startsWith("<com.annimon.stream.function") /*temporal (by BK)*/;
	}
	
	/*public Set<SootMethod> getSootMethodSet(int InvokeType, ParameterBucket pb, InvokeExpr ie, Unit ut, SootMethod parentSm, SerializableCFG cfg)
	{
		String MethodSig = ie.getMethodRef().getSignature();
		
		// 0. Filter methods
		if (MethodFiltering(MethodSig))
			return new HashSet<SootMethod>();
		
		// 1. Handle <init> method & staticInvoke
		if (MethodSig.contains("<init>") || isStaticInvoke(InvokeType))
			return InvokeHelper.getSootMethodSetForInitMethodORStaticInvoke(ut, parentSm, cfg);
		
		// 2. Handle Virtual-invoke, Interface-invoke, or Special-invoke
		return InvokeHelper.getSootMethodSetForVirtualInterfaceSpecialInvoke(pb, ut, parentSm, cfg);
	}*/
	
	public Set<SootMethod> SearchMethod_New(ParameterBucket pb, Unit ut, SootMethod parentSm)
	{
		Set<SootMethod> methodSet = InvokeHelper.getSootMethodSet(pb, ut, parentSm, true);
		
		if (Constants.searchMethodFilterUsingTaintFunction){
			for(Iterator<SootMethod> iter = methodSet.iterator(); iter.hasNext();){
				String sm = iter.next().toString();
				if(!sm.toString().contains("clinit") && !pb.BT().getTaintMethodSet().contains(sm.toString()) 
						&& !sm.toString().contains("<com.tumblr.network.TumblrAPI:") && !sm.toString().contains("<com.tumblr.network.request"))
					//if(!sm.toString().contains("<init>")) // For debugging (added by Byungkwon)
					iter.remove();
			}
		}
		
		return methodSet;
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
	/*
	 * MethodName : CreateBlockGraphAndInitEdgeTables Creator : Jeongmin Kim and
	 * Kunghun Nam Description : Build BlockGraph for a SootMethod and
	 * Initialize Edgetables for considering control blocks.
	 */
	public void CreateBlockGraphAndInitEdgeTables(ParameterBucket pb) throws java.lang.Exception
	{
		try
		{
			if (pb.BT().hasFoundDPStmt() || pb.BT().stopVisitMethod())
			{
				pb.FinalizeStmtAnalysis();
				return;
			}
			
			if (pb.CurrentSM == null)
			{
				if (Constants.isDebug)
					System.out.println("Current Sm is null!");
				pb.FinalizeStmtAnalysis();
				return;
			}
			
			if (!pb.CurrentSM.hasActiveBody())
			{
				pb.FinalizeStmtAnalysis();
				return;
			}
			
			
			pb.BT().increaseMethodVisitCount();
			pb.BT().increaseDepthCount();
			
			Body b = pb.CurrentSM.getActiveBody();
			BriefBlockGraph Bg = new BriefBlockGraph(b);
			
			pb.BackEdge = BlockTravelerHelper.GetBackEdgeTable(pb.CurrentSM.toString(), Bg);
			
			if (Constants.isBackwardDebug && !pb.BT().isMultiThread())
			{
				System.out.print("\t\t");
				for (int i = 0; i < pb.BT().getDepthCount(); i++)
					System.out.print("  ");
				System.out.println("+ Dep(" + String.format("%2d", pb.BT().getDepthCount()) + "), Cnt(" + pb.BT().getMethodVisitCount() + "), Method: " + pb.CurrentSM.toString());
			}
			
			// BK Allocate varTable and heapTable as the size of block graph
			pb.AllocTableArrays(Bg.size());
				
			List<Block> orderedBlockList = BlockTravelerHelper.BlockAlignerWrapper(Bg.getHeads().get(0));
			
			for(Block nextBlock: orderedBlockList)
			{
				// Build block BFT table
				
				// Add BlockGrpahTraveler Param to ParameterBucket Class
				pb.Bg = Bg;
				pb.nextBlock = nextBlock;
				
				try{
				pb.PreProcessingBeforeStmtAnalysis(nextBlock.getPreds(), Bg.getBlocks());
				}catch (Exception e){
					System.out.println("Error in Method: " + pb.CurrentSM.toString() + ", block number: " + nextBlock.getIndexInMethod());
					e.printStackTrace();
				}
				
				AnalyzeEachStmts(pb);
				
				try{
				pb.PostProcessingAfterStmtAnalysis(nextBlock, Bg, pb.BackEdge, pb.CurrentSM.toString());
				} catch (Exception e){
					System.out.println("Error in Method: " + pb.CurrentSM.toString() + ", block number: " + nextBlock.getIndexInMethod() + ", EP: " + Backend.CurrentEntryPoint + ", DP: " + Backend.CurrentParentMethod);
					e.printStackTrace();
				}
			}
			
			//VisitTable.remove(pb.CurrentSM.getSignature().toString());
			//HeapHandler.RememberOnce.remove(pb.CurrentSM.toString());
			
			
			// BK Save the heap value result
			if(pb.CurrentSM.toString().equals(pb.BT().getReqEPMethod())){
				ValueEntryTracking.CopyTable(pb.BT().RRI().heapTable, pb.FinalHeapTable);
				ValueEntryTracking.CopyTable(pb.BT().RRI().ReferenceTable, pb.FinalReferenceTable);
			}
				
			pb.BT().decreaseDepthCount();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	private void getAbstractUnitHandlerDecorator(AbstractUnitHandleDecorator auhd, int HandlerId){
		switch (HandlerId)
		{
			case 0: // Assign Stmt
				auhd.SetTheUnitHandler(new AssignStmtHandler());
			break;
			case 1: // Invoke Stmt
				auhd.SetTheUnitHandler(new InvokeStmtHandler());
			break;
			case 2: // Return Stmt
				auhd.SetTheUnitHandler(new ReturnStmtHandler());
			break;
			case 3: // Identity Stmt
				auhd.SetTheUnitHandler(new IdentityStmtHandler());
			break;
		}
	}
	
	private AbstractUnitHandleDecorator getAbstractUnitHandlerDecorator(Unit ut){
		int HandlerId = SelectHandler(ut);
		AbstractUnitHandleDecorator auhd;
		if (!Constants.isDebug)	auhd = new HeapTrackDecorator();
		else auhd = new DebugDecorator();
		
		getAbstractUnitHandlerDecorator(auhd, HandlerId);
		
		return auhd;
	}
	
	/*
	 * MethodName : AnalyzeEachStmts Creator : Jeongmin Kim and Kunghun Nam Description : Analyze Each Stmts and Jump to other method.
	 */
	private void AnalyzeEachStmts(ParameterBucket pb) throws Exception
	{
		for (Iterator<Unit> UnitintheBlock = pb.nextBlock.iterator(); UnitintheBlock.hasNext();)
		{
			if (pb.BT().hasFoundDPStmt() || pb.BT().stopVisitMethod()){
				// Must keep the heap tracking result even though the analysis has been done due to the method visiting count
				pb.FinalizeStmtAnalysis();
				return;
			}
			
			Unit ut = UnitintheBlock.next();
			
			if(Constants.stopwhenfindingDPStmt)
				CheckDPStmt(ut, pb);
			
			// Analyze Each Units
			AbstractUnitHandleDecorator auhd = getAbstractUnitHandlerDecorator(ut); 
			auhd.HandleUnit(ut, pb, this);
		}
	}
	
	private void CheckDPStmt(Unit u, ParameterBucket pb){
		String ut = u.toString(); 
		String method = pb.CurrentSM.toString();
		
		if(Constants.heapobject)
			return;
		
		if(!method.equals(pb.BT().getDPMethod()))
			return;

		if(!ut.equals(pb.BT().getDPStmt()))
			return;
		
		if(MyCallGraphBuilder.getUnitHash(u, pb.CurrentSM) != pb.BT().DPC().getDPStmtHash())
			return; 
		
		
		pb.BT().setFoundDPStmt(true);
		
		// reaching here means the target statement has found.
		// The ValueEntryList of the target arg of this stmt into the result. (bk BK)
		if(ArgToVEL.isArgTracking())
			ArgToVEL.SaveResult(u, pb.CurrentSM, pb);
	}
	
	
	private int SelectHandler(Unit ut)
	{
		if (ut instanceof AssignStmt)
			return UnitType.AssignStmt;
		else if (ut instanceof InvokeStmt)
			return UnitType.InvokeStmt;
		else if (ut instanceof ReturnStmt || ut instanceof ReturnVoidStmt)
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
						te = new ContextEntry(Constants.sCFG.getMethodOf(ie.getMethodRef().getSignature()), param.toString(), arg.getType().toString());
						if (KeyintheMap != null)
							ContextTable.add(KeyintheMap, te);
					}
				}
				else if (ie instanceof StaticInvokeExpr)
				{
					StaticInvokeExpr sie = (StaticInvokeExpr) ie;
					int paramcount = 0;
					for (Value arg : sie.getArgs())
					{
						String param = "@parameter" + paramcount++;
						te = new ContextEntry(parentSm, arg.toString(), arg.getType().toString());
						String KeyintheMap = ContextTable.findkey(te);
						te = new ContextEntry(Constants.sCFG.getMethodOf(ie.getMethodRef().getSignature()), param.toString(), arg.getType().toString());
						if (KeyintheMap != null)
							ContextTable.add(KeyintheMap, te);
					}
				}
			}
		}
		else
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
				}
				else
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
	protected boolean isInterfaceInvoke(int invokeType)
	{
		if (invokeType == Invoke_Type_Assign_Interface || invokeType == Invoke_Type_Not_Assign_Interface)
			return true;
		return false;
	}
	public void DetermineJumpOtherMethod(int invokeType, InvokeExpr ie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, ParameterBucket pb)
	{
		try
		{
			SootMethod sm = pb.CurrentSM;
			AssignStmt as = null;
			String strDest = null;
			
			if (isAssignStmt(invokeType))
			{
				as = (AssignStmt) ut;
				strDest = as.getLeftOp().toString();
			}
			
			Set<SootMethod> SootMethodSet = null;
			SootMethod[] sootMethodArray;
			
			/********************************/
			/**    BK new search Method    **/
			/********************************/
			SootMethodSet = SearchMethod_New(pb, ut, sm);
			/********************************/
			
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
			
			boolean isSemantic = false;
			//Constants.BFTResultAlreadyApplied = false;
			boolean robo = false;
			
			if (SemanticModel.contains(Constants.Deobfuse(ie.getMethodRef().toString())) || ie.getMethodRef().toString().contains("<com.squareup.okhttp.")
					|| ie.getMethodRef().toString().contains("<ch.boye.httpclientandroidlib") || ie.getMethodRef().toString().contains("<java.net."))
				isSemantic = true;
			
			if (isSemantic)
			{
				if (robo == false)
				{
					if (isStaticInvoke(invokeType))
					{
						StaticInvokeExpr sie = (StaticInvokeExpr) ie;
						AnalyzeExpression(sie, BFTtable, ut, sm, strDest, pb);
					}
					else
					{
						InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
						AnalyzeExpression(iie, BFTtable, ut, sm, strDest, pb);
					}
				}
			}
			else if (!ResponseSemantic.JSON_Semantic(pb, null, pb.strDest, ie, isStaticInvoke(invokeType), false)
					&& !RetrofitHandle.M(ut, pb, sm.toString(), pb.BT()) /* Added by BK*/)
			{
				for (SootMethod subsm : sootMethodArray)
				{
					if(!subsm.hasActiveBody())
						continue;
					
					if (pb.BT().getMethodStack().contains(subsm.getSignature()))
						continue;
					
					pb.BT().getMethodStack().push(subsm.getSignature());

					// create new parameterBucket object
					ParameterBucket newpb = new ParameterBucket(subsm);
					newpb.setBT(pb.BT());

					// Pre-processing for invoking
					ValueEntryTracking.PreprocessingForInvoke(pb, newpb, ie, subsm, InvokeHandler.isInstanceInvoke(invokeType));

					/***********************************/
					/** Start invoking **/
					/***********************************/
					CreateBlockGraphAndInitEdgeTables(newpb);
					pb.BT().getMethodStack().pop();

					// Post-processing for invoking
					ValueEntryTracking.PostprocessingForInvoke(pb, newpb, ie, strDest, InvokeHandler.isInstanceInvoke(invokeType));
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean hasNode(HashMap<String, ArrayList<BFNode>> bufttable, String Key)
	{
		return bufttable.containsKey(Key);
	}
	
	public String CropVar(String var)
	{
		if (var.startsWith("$"))
			return var.substring(var.indexOf(".") + 1);
		else
			return var;
	}
	/*
	 * public boolean isconst(String arg) { if (arg.getBytes()[0] == '"' || ('0' < arg.getBytes()[0] && arg.getBytes()[0] < '9')) { return true; } return false; }
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
			}
			catch (NodeNotFoundException e)
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
			}
			catch (CloneNotSupportedException e)
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
			}
			catch (CloneNotSupportedException e)
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
