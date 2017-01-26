package Extractocol.BufferExtractor_Response.UnitHandle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import soot.Unit;
import soot.jimple.Constant;
import soot.jimple.ReturnStmt;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapEntry;
import Extractocol.BufferExtractor_Response.Basics.BFNode_Response;
import Extractocol.BufferExtractor_Response.Basics.ParameterBucket;
import Extractocol.BufferExtractor_Response.Helper.TaintHelper;

public class Unit_ReturnStmt {

	public static ParameterBucket static_pb;

	public static void handleUnit(ParameterBucket pb) {
		
		// init - set static parameter bucket
		static_pb = pb;
		
		Unit ut = static_pb.ut;

		ReturnStmt rs = (ReturnStmt) ut;

		if (!rs.getOp().toString().equals("null"))
			// Handle return variable/constant
			pb.ReturnStmtHandler(rs);
	}
}


