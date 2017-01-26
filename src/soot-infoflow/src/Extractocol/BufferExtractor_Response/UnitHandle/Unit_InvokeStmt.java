package Extractocol.BufferExtractor_Response.UnitHandle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Response.Basics.ParameterBucket;

public class Unit_InvokeStmt {

	public static ParameterBucket static_pb;

	public static void handleUnit(ParameterBucket pb) {
		
		static_pb = pb;
		
		Unit ut = static_pb.ut;
		
		InvokeStmt ivs = (InvokeStmt) ut;

		if (ivs.getInvokeExpr() instanceof StaticInvokeExpr)
		{
			StaticInvokeExpr sie = (StaticInvokeExpr) ivs.getInvokeExpr();
			static_pb.invokeType = InvokeHandler.Static_Not_Assign;
			static_pb.ie = sie;
			InvokeHandler.handleInvoke(static_pb);
		}

		else if (ivs.getInvokeExpr() instanceof VirtualInvokeExpr)
		{
			VirtualInvokeExpr vie = (VirtualInvokeExpr) ivs.getInvokeExpr();
			static_pb.invokeType = InvokeHandler.Virtual_Not_Assign;
			static_pb.ie = vie;
			InvokeHandler.handleInvoke(static_pb);
		}

		else if (ivs.getInvokeExpr() instanceof InterfaceInvokeExpr)
		{
			InterfaceInvokeExpr ife = (InterfaceInvokeExpr) ivs.getInvokeExpr();
			static_pb.invokeType = InvokeHandler.Interface_Not_Assign;
			static_pb.ie = ife;
			InvokeHandler.handleInvoke(static_pb);
		}

		else if (ivs.getInvokeExpr() instanceof SpecialInvokeExpr)
		{
			SpecialInvokeExpr sie = (SpecialInvokeExpr) ivs.getInvokeExpr();
			static_pb.invokeType = InvokeHandler.Special_Not_Assign;
			static_pb.ie = sie;
			InvokeHandler.handleInvoke(static_pb);
		}		
	}
}
