package extractocol.backend.response.unitHandle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import extractocol.backend.request.basics.BFNode;
import extractocol.backend.response.basics.ParameterBucket;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;

public class Unit_InvokeStmt {

	public static void handleUnit(ParameterBucket pb) {
		
		Unit ut = pb.ut;
		
		InvokeStmt ivs = (InvokeStmt) ut;

		if (ivs.getInvokeExpr() instanceof StaticInvokeExpr)
		{
			StaticInvokeExpr sie = (StaticInvokeExpr) ivs.getInvokeExpr();
			pb.invokeType = InvokeHandler.Static_Not_Assign;
			pb.ie = sie;
			InvokeHandler.handleInvoke(pb);
		}

		else if (ivs.getInvokeExpr() instanceof VirtualInvokeExpr)
		{
			VirtualInvokeExpr vie = (VirtualInvokeExpr) ivs.getInvokeExpr();
			pb.invokeType = InvokeHandler.Virtual_Not_Assign;
			pb.ie = vie;
			InvokeHandler.handleInvoke(pb);
		}

		else if (ivs.getInvokeExpr() instanceof InterfaceInvokeExpr)
		{
			InterfaceInvokeExpr ife = (InterfaceInvokeExpr) ivs.getInvokeExpr();
			pb.invokeType = InvokeHandler.Interface_Not_Assign;
			pb.ie = ife;
			InvokeHandler.handleInvoke(pb);
		}

		else if (ivs.getInvokeExpr() instanceof SpecialInvokeExpr)
		{
			SpecialInvokeExpr sie = (SpecialInvokeExpr) ivs.getInvokeExpr();
			pb.invokeType = InvokeHandler.Special_Not_Assign;
			pb.ie = sie;
			InvokeHandler.handleInvoke(pb);
		}		
	}
}
