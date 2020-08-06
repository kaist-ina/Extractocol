
package extractocol.backend.request.unitHandle;

import java.util.ArrayList;
import java.util.List;

import extractocol.Constants;
import extractocol.backend.request.SignatureBuilder_Request;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.basics.ParameterBucket;
import extractocol.backend.request.heapManagement.HeapHandler;
import extractocol.common.valueEntry.ValueEntryTracking;
import soot.Unit;
import soot.jimple.Constant;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;

public class ReturnStmtHandler extends AbstractUnitHandler
{

	public void HandleUnit(Unit ut, ParameterBucket pb, SignatureBuilder_Request sb)
	{
		// Handle return variable/constant
		pb.ReturnStmtHandler(ut);
	}
}
