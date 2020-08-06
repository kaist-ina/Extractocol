
package extractocol.backend.request.unitHandle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import extractocol.Constants;
import extractocol.backend.request.SignatureBuilder_Request;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.basics.ParameterBucket;
import extractocol.backend.request.heapManagement.HeapHandler;
import extractocol.backend.request.heapManagement.SourceforTaint;
import extractocol.backend.request.semantic.url.UrlBuilder;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.helper.AssignmentHelper;
import extractocol.common.outputs.BackendOutput;
import extractocol.common.trackers.tools.HeapToVEL;
import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntryTracking;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;
import extractocol.common.valueEntry.helper.General;
import extractocol.common.valueEntry.unitHandle.Unit_AssignStmt;
import extractocol.tester.HeapHandling;
import soot.Unit;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.shimple.PhiExpr;
import soot.jimple.NewExpr;

public class AssignStmtHandler extends AbstractUnitHandler
{

	private static void PhiExprProcessor(AssignStmt as, ParameterBucket pb, SignatureBuilder_Request sb)
	{
		PhiExpr pe = (PhiExpr) as.getRightOp();

		for (Value v : pe.getValues())
		{
			String phiVar = v.toString();

			// Left argument can be in the Phi-node. We should skip
			// this case.
			if (pb.strDest.equals(phiVar))
				continue;

			if (!sb.hasNode(pb.blockBFTtable, phiVar))
				pb.blockBFTtable.put(phiVar, new ArrayList<BFNode>());

			BFNode bfn = new BFNode();
			bfn.makePhinodeBfn(phiVar);
			ArrayList<BFNode> list = pb.blockBFTtable.get(pb.strDest);
			list.add(bfn);
		}
	}

	private static void IncludedInvokeExprProcessor(AssignStmt as, ParameterBucket pb, SignatureBuilder_Request sb, Unit ut)
	{
		if (as.getInvokeExpr() instanceof StaticInvokeExpr)
		{
			StaticInvokeExpr sie = (StaticInvokeExpr) as.getInvokeExpr();
			sb.DetermineJumpOtherMethod(SignatureBuilder_Request.Invoke_Type_Assign_Static, sie, pb.blockBFTtable, ut, pb);
		} else if (as.getInvokeExpr() instanceof VirtualInvokeExpr)
		{
			VirtualInvokeExpr vie = (VirtualInvokeExpr) as.getInvokeExpr();
			sb.DetermineJumpOtherMethod(SignatureBuilder_Request.Invoke_Type_Assign_Virtual, vie, pb.blockBFTtable, ut, pb);
		} else if (as.getInvokeExpr() instanceof InterfaceInvokeExpr)
		{
			InterfaceInvokeExpr ife = (InterfaceInvokeExpr) as.getInvokeExpr();
			sb.DetermineJumpOtherMethod(SignatureBuilder_Request.Invoke_Type_Assign_Interface, ife, pb.blockBFTtable, ut, pb);
		} else if (as.getInvokeExpr() instanceof SpecialInvokeExpr)
		{
			SpecialInvokeExpr sie = (SpecialInvokeExpr) as.getInvokeExpr();
			sb.DetermineJumpOtherMethod(SignatureBuilder_Request.Invoke_Type_Assign_Special, sie, pb.blockBFTtable, ut, pb);
		}
	}

	private static void WhenLeftExprIsHeap(AssignStmt as, ParameterBucket pb, SignatureBuilder_Request sb, String cropLeftOp)
	{
		String LeftSootValue = cropLeftOp;

		// if $v.<> = constant
		if (as.getRightOp() instanceof Constant)
			HeapHandler.updateHeapTable(LeftSootValue, as.getRightOp().toString());
		// if $v.<> = $v
		else if (pb.blockBFTtable.get(as.getRightOp().toString()) != null)
		{
			String var = as.getRightOp().toString();

			// turn off heap object assign. Use only linear
			if (as.getRightOp().getType().toString().contains("Map") || as.getRightOp().getType().toString().contains("BasicNameValuePair"))
			{
				/*
				 * if $r3 != null goto label1; $r4 = new java.util.HashMap;
				 * specialinvoke $r4.<java.util.HashMap: void <init>()>();
				 * $r0.<com.external.androidquery.b.a: java.util.Map params> =
				 * $r4;
				 * 
				 * label1: $r3 = $r0.<com.external.androidquery.b.a:
				 * java.util.Map params>; interfaceinvoke $r3.<java.util.Map:
				 * java.lang.Object put(java.lang.Object,java.lang.Object)>(
				 * $r1, $r2);
				 */

				if (HeapHandler.GlobalBFTtable.containsKey(LeftSootValue))
				{
					if (HeapHandler.GlobalBFTtable.get(LeftSootValue).size() == 1 && pb.blockBFTtable.get(as.getRightOp().toString()).size() > 1)
						HeapHandler.GlobalBFTtable.put(LeftSootValue, pb.blockBFTtable.get(as.getRightOp().toString()));

				} else
					HeapHandler.GlobalBFTtable.put(LeftSootValue, pb.blockBFTtable.get(as.getRightOp().toString()));
			} else
				HeapHandler.updateHeapTable(LeftSootValue, sb.GenRegex(null, pb.blockBFTtable, var));
		}
		// if $v.<> = new or ...
		else
			HeapHandler.updateHeapTable(LeftSootValue, as.getLeftOp().getType().equals("java.lang.Integer") ? "[0-9]*" : "(.*)");
	}

	private static void WhenLeftOpIsArray(AssignStmt as, ParameterBucket pb, String cropRightOp, SignatureBuilder_Request sb, ArrayList<BFNode> list)
	{
		// Index: 0, Size: 0 - 1901 line
		if (list.size() == 0)
			return;

		String right = as.getRightOp().toString();
		ArrayRef arrayref = ((ArrayRef) as.getLeftOp());
		int number = 0;
		if (arrayref.getIndex() instanceof Constant)
			number = Integer.parseInt(arrayref.getIndex().toString());
		if (list.size() == 0)
			number = 3;
		BFNode bfn = null;
		bfn = list.get(0);

		// if $v[]= $v.<>
		// use heaphandler to find string from heap
		if (cropRightOp.startsWith("<") && cropRightOp.endsWith(">"))
		{
			String heapresult = HeapHandler.getHeapObjectString(bfn.getSootValue());
			bfn.makeUrlBfn(heapresult);
			list.add(bfn);
		}
		// if $v[]= constant
		else if (as.getRightOp() instanceof Constant)
		{
			bfn.setarray(number, as.getRightOp().toString());
		}
		// if $v[]= $v[]
		else if (as.getRightOp() instanceof ArrayRef && as.getRightOp().toString().contains("\\[") && as.getRightOp().toString().contains("\\]"))
		{

			ArrayRef arrayrefright = ((ArrayRef) as.getRightOp());

			String rightVar = arrayrefright.getBase().toString();
			String rightindex = arrayrefright.getIndex().toString();
			String leftindex = arrayref.getIndex().toString();

			// ex) $r1[$i1]= $r2[$i1]
			// if r2 has a const value on $r2[1]="hi"
			// We can assume anything can be a string
			// (.*) & $r2[1](hi) is in the array
			// BFTtable.get($r1).get(0).getStringForUrl=
			// "(.*)"
			// BFTtable.get($r1).get(0).getorcase = "hi"
			if (arrayref.getIndex() instanceof Constant == false && arrayrefright.getIndex() instanceof Constant == false)
			{
				bfn.initarray(Integer.valueOf(number));
				ArrayList<String> possiblestring = pb.blockBFTtable.get(rightVar).get(0).getpossiblestringarray();
				bfn.setArrayorcase(possiblestring);
				list.add(bfn);
			}
			// ex) $r1[$i1]= $r2[1]
			// We can assume anywhere -> r[1]("hi") is
			// in the array && if r1 has a orcase, add
			// it.
			// BFTtable.get($r1).get(0).getorcase = "hi"
			else if (arrayref.getIndex() instanceof Constant == false && arrayrefright.getIndex() instanceof Constant == true)
			{
				String[] fullarraystring = pb.blockBFTtable.get(rightVar).get(0).bfnTOarray();
				ArrayList<String> orarraylist = new ArrayList<String>();
				orarraylist.add(fullarraystring[Integer.valueOf(rightindex)]);
				// make or
				bfn.setArrayorcase(orarraylist);
				list.add(bfn);
			}
			// ex) $r1[1]= $r2[$i1]
			// We can assume anywhere ($i1) -> possible
			// string in r2 should be included.
			// BFTtable.get($r1).get(0).getStringforurl="###
			// ###(possible string)"
			else if (arrayref.getIndex() instanceof Constant == true && arrayrefright.getIndex() instanceof Constant == false)
			{
				String possiblestring = pb.blockBFTtable.get(rightVar).get(0).getpossiblestring();
				bfn.setarray(Integer.valueOf(leftindex), possiblestring);
				list.add(bfn);
				// ex) $r1[1]= $r2[2]
				// simple variable assignmnet
			} else if (arrayref.getIndex() instanceof Constant == true && arrayrefright.getIndex() instanceof Constant == true)
			{
				String[] fullarraystring = pb.blockBFTtable.get(rightVar).get(0).bfnTOarray();
				bfn.setarray(number, fullarraystring[Integer.valueOf(rightindex)]);
				list.add(bfn);
			} else
				System.out.println("ERROR when array assignment execption error");

		}
		// if $v[] = $v
		else
		{
			if(pb.blockBFTtable.get(right) != null)
			{
				if (pb.blockBFTtable.get(right).isEmpty())
				{
					BFNode bbfn = new BFNode();
					bbfn.makeUrlBfn(".*");
					pb.blockBFTtable.get(right).add(bbfn);
					// System.out.println("ERROR when A[]=b,
					// b is empty ");
				}

				String Rvalue = sb.GenRegex(null, pb.blockBFTtable, right);
				bfn.setarray(number, Rvalue);
			}
		}
	}

	private static void NormalAssignStmtProcessor(AssignStmt as, ParameterBucket pb, SignatureBuilder_Request sb)
	{
		String cropLeftOp = sb.CropVar(as.getLeftOp().toString());
		String cropRightOp = sb.CropVar(as.getRightOp().toString());

		// left op is heap object
		String var = as.getLeftOp().toString();
		if (var.contains("[") && var.contains("]"))
			var = var.substring(0, var.indexOf("["));

		// jeongmin value
		boolean isSubclassed = false;

		ArrayList<BFNode> list = pb.blockBFTtable.get(var);

		if (list == null)
			list = new ArrayList<BFNode>();

		// reference error
		// ex) 1: v1 = v2 -> BFTtable.get(v1).get(0) ==
		// BFTtable.get(v2).get(0)
		// 2: v2 = v3 -> BFTtable.get(v1).get(0) == null
		// in line 2, BFTtable.get(v2) is cleared. So that
		// v1 is also cleared.
		// My solution is making a clone of original bfn.
		// woohyun 20160420

		// before reset list, find same pointer values <-
		// this code here
		// after assigning, the pointer values re assign.
		// woohyun 20160423

		boolean leftrightsame = false;
		ArrayList<BFNode> leftoparraylist = pb.blockBFTtable.get(as.getLeftOp().toString());
		ArrayList<String> samepointerasleft = new ArrayList<String>();
		Iterator<String> keys = pb.blockBFTtable.keySet().iterator();
		while (keys.hasNext())
		{
			String key = keys.next();
			if (key.equals(as.getLeftOp().toString()))
				continue;
			if (pb.blockBFTtable.get(key) == leftoparraylist)
				samepointerasleft.add(key);
		}
		// clear when it is not an array -- 2016 0404
		if (list.size() != 0 && !BFNode.isArray(list.get(0)) && !leftrightsame)
			list.clear();
		
		//AsignStmt Cases
		if (cropLeftOp.startsWith("<") && cropLeftOp.endsWith(">"))
			WhenLeftExprIsHeap(as, pb, sb, cropLeftOp);
		// if $v[]=... -- woohyun 20160330
		else if (as.getLeftOp() instanceof ArrayRef && as.getLeftOp().toString().contains("["))
			WhenLeftOpIsArray(as, pb, cropRightOp, sb, list);

		else if (cropRightOp.startsWith("<") && cropRightOp.endsWith(">"))
			WhenRightOpIsHeap(as, cropRightOp, isSubclassed, pb, list, sb);

		else if (as.getRightOp().toString().contains("newarray"))
			WhenNewArrayInit(as, list);

		else if (as.getRightOp() instanceof ArrayRef && as.getRightOp().toString().contains("[") && !as.toString().contains("instanceof")
				&& as.getRightOp().toString().split("[(].*[)]").length < 2)
			WhenRightOpIsArray(as, pb, list);

		else if (as.getRightOp().toString().split("[(].*[)]").length >= 2)
			WhenRightOpIsTypeCasted(as, pb, var, list);

		else
			WhenOtherCases(as, pb, var, list);

		// reference error
		// ex) 1: v1 = v2 -> BFTtable.get(v1).get(0) ==
		// BFTtable.get(v2).get(0)
		// 2: v2 = v3 -> BFTtable.get(v1).get(0) == null
		// in line 2, BFTtable.get(v2) is cleared. So that
		// v1 is also cleared.
		// My solution is making a clone of original bfn.
		// woohyun 20160420

		// before reset list, find same pointer values
		// after assigning, the pointer values re assign. <-
		// this code here
		// woohyun 20160423
		for (int i = 0; i < samepointerasleft.size(); i++)
		{
			pb.blockBFTtable.put(samepointerasleft.get(i), pb.blockBFTtable.get(as.getLeftOp().toString()));
		}
	}

	private static void WhenNewArrayInit(AssignStmt as, ArrayList<BFNode> list)
	{
		// if $v = new array -- woohyun 20160330
		BFNode bfn = new BFNode();

		// A[number]<=== find number
		String arraystring = as.getRightOp().toString();
		String number = arraystring.substring(arraystring.indexOf("[") + 1, arraystring.indexOf("]"));

		// ex) r4= newarray [$i1]
		if (number.contains("i") || number.toString().isEmpty())
		{
			// if array is dynamic, I assume that size
			// is 30
			number = "30";
		}

		bfn.initarray(Integer.valueOf(number));
		list.clear();
		list.add(bfn);
	}

	// if $v = new or ... $v = $v
	private static void WhenOtherCases(AssignStmt as, ParameterBucket pb, String var, ArrayList<BFNode> list)
	{
		if (as.getRightOp().toString().contains("(com.android.volley.Request)"))
		{
			String rightVar = as.getRightOp().toString().split("\\) ")[1];
			pb.blockBFTtable.put(var, pb.blockBFTtable.get(rightVar));
		}
		// $v = (java.lang.String) v1
		else if (as.getRightOp().toString().startsWith("(java.lang.String)"))
		{
			String rightop = as.getRightOp().toString().substring(as.getRightOp().toString().indexOf(")") + 2);
			pb.blockBFTtable.put(as.getLeftOp().toString(), pb.blockBFTtable.get(rightop));
		} else if (pb.blockBFTtable.get(as.getRightOp().toString()) != null)
		{
			// if $v = $v
			String rightVar = as.getRightOp().toString();
			pb.blockBFTtable.put(var, pb.blockBFTtable.get(rightVar));
		} else if (as.getRightOp() instanceof Constant)
		{
			BFNode bfn = new BFNode();
			bfn.makeUrlBfn(as.getRightOp().toString());
			list.add(bfn);
		} else
		{
			BFNode bfn = new BFNode();
			bfn.setVtype(as.getLeftOp().getType().toString());
			// bfn.makeUrlBfn(as.getLeftOp().getType().equals("java.lang.Integer")
			// ? "[0-9]*" : "(.*)");
			list.add(bfn);
		}
	}

	// if $v = (java.lang.xxx) $v
	private static void WhenRightOpIsTypeCasted(AssignStmt as, ParameterBucket pb, String var, ArrayList<BFNode> list)
	{
		String rightVar = as.getRightOp().toString().split("[(].*[)]")[1].substring(1);

		// if $vL = (java.lang.xxx []) $vR && vR !=
		// array
		if (as.getRightOp().toString().contains("[")) {
			if (pb.blockBFTtable.get(rightVar) != null) {
				if (!BFNode.isArray(pb.blockBFTtable.get(rightVar).get(0))) {
					BFNode bfn = new BFNode();
					bfn.setrandomarray();
					list.add(bfn);
					return;
				}

			}
		}
		
		if (pb.blockBFTtable.get(rightVar) == null) {
			ArrayList<BFNode> arraylist = new ArrayList<BFNode>();
			BFNode bfn = new BFNode();
			bfn.makeUrlBfn(".*");
			arraylist.add(bfn);
			pb.blockBFTtable.put(var, pb.blockBFTtable.get(rightVar));
		} else
			pb.blockBFTtable.put(var, pb.blockBFTtable.get(rightVar));
	}

	// if $v = $v[]
	private static void WhenRightOpIsArray(AssignStmt as, ParameterBucket pb, ArrayList<BFNode> list)
	{
		ArrayRef arrayrefright = ((ArrayRef) as.getRightOp());

		String rightVar = arrayrefright.getBase().toString();
		String rightindex = arrayrefright.getIndex().toString();

		// if $v = $v[$i1]
		if (arrayrefright.getIndex() instanceof Constant == false)
		{
			if (pb.blockBFTtable.get(rightVar).size() == 0)
			{
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(".*");
				list.add(bfn);
			} else
			{
				String possiblestring = pb.blockBFTtable.get(rightVar).get(0).getpossiblestring();
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(possiblestring);
				list.add(bfn);
			}
		}
		// if $v = $v[1]
		else if (arrayrefright.getIndex() instanceof Constant == true && !as.toString().contains("(")
				&& !BFNode.isArray(pb.blockBFTtable.get(as.getRightOp().toString().split("\\[")[0]).get(0)))
		{

			BFNode bfn = new BFNode();
			String[] fullarraystring = pb.blockBFTtable.get(rightVar).get(0).bfnTOarray();
			if (fullarraystring.length > Integer.valueOf(rightindex))
				bfn.makeUrlBfn(fullarraystring[Integer.valueOf(rightindex)]);
			else
				bfn.makeUrlBfn(".*");

		}
	}

	private static void WhenRightOpIsHeap(AssignStmt as, String cropRightOp, boolean isSubclassed, ParameterBucket pb, ArrayList<BFNode> list,
			SignatureBuilder_Request sb)
	{
		// JM If thisRightOp is in the globalHeap table,
		// We should store it to BFTtable.
		String RightSootValue = cropRightOp;

		if (HeapHandler.GlobalHeapTable.get(RightSootValue) != null)
		{
			isSubclassed = true;
			BFNode bfn = new BFNode();
			bfn.makeUrlBfn(HeapHandler.GlobalHeapTable.get(RightSootValue));
			// HeapHandler.GlobalHeapTable.clear();
			list.add(bfn);
		} else
		{
			String ParentClass = RightSootValue.substring(1, RightSootValue.lastIndexOf(":"));
			List<String> superclasses = Constants.sCFG.getSuperclassOf(ParentClass);
			if (superclasses != null)
			{
				for (String a : superclasses)
				{
					String heapName = "<" + a + RightSootValue.substring(RightSootValue.indexOf(":"));
					if (HeapHandler.GlobalHeapTable.containsKey(heapName))
					{
						isSubclassed = true;
						BFNode bfn = new BFNode();
						bfn.makeUrlBfn(HeapHandler.GlobalHeapTable.get(heapName));
						// HeapHandler.GlobalHeapTable.clear();
						list.add(bfn);
					}
				}
			}
		}

		if (!isSubclassed)
		{
			BFNode bfn = new BFNode();
			bfn.makeHeapObjectBfn(RightSootValue);
			list.add(bfn);
		}

		if (HeapHandler.GlobalBFTtable.containsKey(RightSootValue))
			pb.blockBFTtable.put(as.getLeftOp().toString(), HeapHandler.GlobalBFTtable.get(RightSootValue));

		if (HeapHandler.GlobalBFTtable.containsKey(RightSootValue) && HeapHandler.OnceTaintTable.isEmpty())
		{
			list = HeapHandler.GlobalBFTtable.get(RightSootValue);
			pb.blockBFTtable.put(as.getLeftOp().toString(), sb.CopyList(list));
		}

		HeapHandler.OnceTaintTable.put(as.getLeftOp().toString(), RightSootValue);
		String oncetaintkey = "oncetaint_" + as.getLeftOp().toString();
		if (pb.blockBFTtable.get(oncetaintkey) == null)
		{
			BFNode oncetaintbfn = new BFNode();
			oncetaintbfn.setVtype("oncetainttable");
			oncetaintbfn.setStringForUrl(RightSootValue);
			ArrayList<BFNode> oncetaintlist = new ArrayList<BFNode>();
			oncetaintlist.add(oncetaintbfn);
			pb.blockBFTtable.put(oncetaintkey, oncetaintlist);
		}
	}


	@Override
	public void HandleUnit(Unit ut, ParameterBucket pb, SignatureBuilder_Request sb)
	{
		AssignStmt as = (AssignStmt) ut;
		
		// array case
		if (as.getLeftOp().toString().contains("[") && as.getLeftOp().toString().contains("]"))
			pb.strDest = as.getLeftOp().toString().split("\\[")[0];
		else
			pb.strDest = as.getLeftOp().toString();

		if (as.containsInvokeExpr())
			IncludedInvokeExprProcessor(as, pb, sb, ut);
		
		/** variables are tracked locally and heaps are tracked globally. **/
		pb.AssignmentStmtHandler(as, pb.CurrentSM.toString());
	}
	
	/**
	 * 
	 * @param ut
	 * @param pb
	 * @param sb
	 */
	/*private void TrackingHeapObject(Unit ut, ParameterBucket pb, SignatureBuilder_Request sb){
		AssignStmt as = (AssignStmt) ut;
		
		for(SourceforTaint sft: HeapToVEL.lstTaintSourceInfo){
			if(ut.toString().equals(sft.getUnit())){
				//UrlBuilder ub = (UrlBuilder) sb;
				//String resultUrl = ub.printUrlFromList(pb.blockBFTtable, as.getRightOp().toString());
				String resultUrl = getValues(pb, as.getRightOp().toString());
				BackendOutput.reqRespInfo.addConstantEntry(HeapToVEL.targetHeap, resultUrl);
			}
		}
	}*/
	
	private String getValues(ParameterBucket pb, String var){
		String result = "";
		
		ValueEntryList list = pb.varTable.getValueEntryList(var);
		
		if (list != null)
			result = list.GenRegex();
		
//			for (ValueEntry he : list.getValueEntryList())
//				if (he.getSourceType() == SOURCE_TYPE.CONSTANT || he.getSourceType() == SOURCE_TYPE.HEAP)
//					result += he.getValue() + "|";
		
//		if(!result.equals(""))
//			return result.substring(0, result.length() - 1);
//		else
		
		return result;
	}
}
