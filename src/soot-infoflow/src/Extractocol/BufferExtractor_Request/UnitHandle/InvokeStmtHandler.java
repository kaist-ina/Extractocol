package Extractocol.BufferExtractor_Request.UnitHandle;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.SignatureBuilder_Request;
import Extractocol.BufferExtractor_Request.Basics.ParameterBucket;
import soot.Unit;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;



public class InvokeStmtHandler extends AbstractUnitHandler
{
	public void HandleUnit(Unit ut, ParameterBucket pb, SignatureBuilder_Request sb)
	{
		InvokeStmt ivs = (InvokeStmt) ut;

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
		
		// Prevent the current value of isSemantic from being used in other statements
		Constants.isSemantic = false;
	}

}
