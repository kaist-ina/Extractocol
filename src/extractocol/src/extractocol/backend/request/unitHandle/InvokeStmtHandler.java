package extractocol.backend.request.unitHandle;

import extractocol.Constants;
import extractocol.backend.request.SignatureBuilder_Request;
import extractocol.backend.request.basics.ParameterBucket;
import extractocol.tester.Backend;
import soot.*;
import soot.jimple.*;

import java.util.List;


public class InvokeStmtHandler extends AbstractUnitHandler
{
	private boolean extendingVolleyRequest(SootClass sc)
	{
		for (SootClass sub : Scene.v().getActiveHierarchy().getSuperclassesOfIncluding(sc))
		{
			if (sub.getName().contains("volley.Request"))
				return true;
		}
		return false;
	}

	public void HandleUnit(Unit ut, ParameterBucket pb, SignatureBuilder_Request sb)
	{
		InvokeStmt ivs = (InvokeStmt) ut;

		if (ivs.toString().contains("<com.pinterest.api.e$3: void <init>(java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener,com.android.volley.Response$HeaderListener,boolean,java.lang.String,java.util.Map)>"))
		{
			System.out.println("[Extractocol - Volley Response Handler]: " + pb.varTable.getValueEntryList(ivs.getInvokeExpr().getArg(1).toString()).getVarType());
			if (extendingVolleyRequest(ivs.getInvokeExpr().getMethod().getDeclaringClass()))
			{
				Value target = getVolleyResponse(ivs.getInvokeExpr().getArgs());
				getResponseInit(Scene.v().getSootClass(pb.varTable.getValueEntryList(target.toString()).getTypes().get(0)), pb);
			}
		}

		if (ivs.getInvokeExpr() instanceof StaticInvokeExpr)
		{
			StaticInvokeExpr sie = (StaticInvokeExpr) ivs.getInvokeExpr();
			sb.DetermineJumpOtherMethod(SignatureBuilder_Request.Invoke_Type_Not_Assign_Static, sie, pb.blockBFTtable, ut, pb);
		}

		else if (ivs.getInvokeExpr() instanceof VirtualInvokeExpr)
		{
			VirtualInvokeExpr vie = (VirtualInvokeExpr) ivs.getInvokeExpr();
			sb.DetermineJumpOtherMethod(SignatureBuilder_Request.Invoke_Type_Not_Assign_Virtual, vie, pb.blockBFTtable, ut, pb);
		}

		else if (ivs.getInvokeExpr() instanceof InterfaceInvokeExpr)
		{
			InterfaceInvokeExpr ife = (InterfaceInvokeExpr) ivs.getInvokeExpr();
			sb.DetermineJumpOtherMethod(SignatureBuilder_Request.Invoke_Type_Not_Assign_Interface, ife, pb.blockBFTtable, ut, pb);
		}

		else if (ivs.getInvokeExpr() instanceof SpecialInvokeExpr)
		{
			SpecialInvokeExpr sie = (SpecialInvokeExpr) ivs.getInvokeExpr();
			sb.DetermineJumpOtherMethod(SignatureBuilder_Request.Invoke_Type_Not_Assign_Special, sie, pb.blockBFTtable, ut, pb);
		}
	}

	private Value getVolleyResponse(List<Value> args) {
		for (Value arg : args)
		{
			for (SootClass subs : Scene.v().getSootClass(arg.getType().toString()).getInterfaces())
			if (subs.getName().equals("com.android.volley.Response$Listener"))
				return arg;
		}
		return null;
	}

	private void getResponseInit(SootClass declaringClass, ParameterBucket pb) {
		for (SootClass sub : Scene.v().getActiveHierarchy().getSuperclassesOfIncluding(declaringClass))
		{
			for (SootMethod sm : sub.getMethods())
			{
				if (sm.getSignature().contains("void onResponse(java.lang.Object)"))
				{

					for (Unit ut : sm.getActiveBody().getUnits()) {
						if (ut.toString().equals("$r1 := @parameter0: java.lang.Object")) {
							pb.BT().setFoundDPStmt(true);
							pb.BT().setRespEP(sm.getSignature(), ut.toString());
							return;
						}
					}
				}
			}
		}
	}

}
