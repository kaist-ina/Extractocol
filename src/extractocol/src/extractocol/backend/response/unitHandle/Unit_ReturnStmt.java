package extractocol.backend.response.unitHandle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import extractocol.backend.request.basics.BFNode;
import extractocol.backend.response.basics.BFNode_Response;
import extractocol.backend.response.basics.ParameterBucket;
import extractocol.backend.response.helper.TaintHelper;
import extractocol.common.valueEntry.ValueEntry;
import soot.Unit;
import soot.jimple.Constant;
import soot.jimple.ReturnStmt;

public class Unit_ReturnStmt {

	public static void handleUnit(ParameterBucket pb) {
		// Handle return variable/constant
		pb.ReturnStmtHandler(pb.ut);
	}
}


