package extractocol.backend.response.taint.unitHandle;

import extractocol.backend.response.basics.ParameterBucket;
import soot.Unit;
import soot.jimple.ReturnStmt;

public class Unit_Return {
	public static void Handle(Unit ut, ParameterBucket pb) {
		ReturnStmt rs = (ReturnStmt) ut;
		
		if(pb.isVarTainted(rs.getOp().toString()))
			pb.setReturnVarTainted(true);
	}
}
