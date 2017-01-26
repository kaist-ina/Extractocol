
package Extractocol.BufferExtractor_Request.UnitHandle;

import java.util.ArrayList;
import java.util.List;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.SignatureBuilder_Request;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Basics.ParameterBucket;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapHandler;
import soot.Unit;
import soot.jimple.Constant;
import soot.jimple.ReturnStmt;

public class ReturnStmtHandler extends AbstractUnitHandler
{

	public void HandleUnit(Unit ut, ParameterBucket pb, SignatureBuilder_Request sb)
	{

		ReturnStmt rs = (ReturnStmt) ut;

		if (rs.getOp() instanceof Constant)
		{
			SignatureBuilder_Request.BackwardRtnValueStack.removeLast();
			SignatureBuilder_Request.BackwardRtnValueStack.addLast(rs.getOp().toString().replace("\"", ""));

		} else
		{
			String variable = rs.getOp().toString();
			if (SignatureBuilder_Request.BackwardRtnValueStack.getLast() != null)
			{
				String rtn = (String) SignatureBuilder_Request.BackwardRtnValueStack.getLast();
				if (sb.isHashMap(pb.blockBFTtable, variable))
				{
					SignatureBuilder_Request.BackwardRtnValueStack.removeLast();
					SignatureBuilder_Request.BackwardRtnValueStack.addLast(pb.blockBFTtable.get(variable));
				} else
				{
					rtn = "(" + rtn + "|" + sb.GenRegex(null, pb.blockBFTtable, variable) + ")";
					SignatureBuilder_Request.BackwardRtnValueStack.removeLast();
					SignatureBuilder_Request.BackwardRtnValueStack.addLast(rtn);
				}

			} else
			{
				SignatureBuilder_Request.BackwardRtnValueStack.removeLast();
				// JM If variable name is in rtnvaluestack, we
				// should
				// preserve that BFTree.
				if (sb.isHashMap(pb.blockBFTtable, variable))
					SignatureBuilder_Request.BackwardRtnValueStack.addLast(pb.blockBFTtable.get(variable));
				else
					SignatureBuilder_Request.BackwardRtnValueStack.addLast(sb.GenRegex(null, pb.blockBFTtable, variable));
			}
		}

		// For Volley
		List<String> superclasses = Constants.sCFG.getSuperclassOf(rs.getOp().getType().toString());
		if (superclasses != null)
			if (superclasses.contains("com.android.volley.Request") && Constants.VolleyUrlCandidate != null)
			{
				BFNode volleyurl = new BFNode();
				volleyurl.makeUrlBfn(Constants.VolleyUrlCandidate);
				Constants.VolleyUrlCandidate = null;
				ArrayList<BFNode> a = new ArrayList<BFNode>();
				a.add(volleyurl);
				pb.blockBFTtable.put(rs.getOp().toString(), sb.CopyList(a));
				SignatureBuilder_Request.BackwardRtnValueStack.addLast(pb.blockBFTtable.get(rs.getOp().toString()));
				// ContextTable.Map.clear();
			}
		HeapHandler.OnceTableClear();
	}

}
