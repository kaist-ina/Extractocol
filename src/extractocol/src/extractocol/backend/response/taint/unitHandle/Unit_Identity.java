package extractocol.backend.response.taint.unitHandle;

import extractocol.backend.response.basics.ParameterBucket;
import extractocol.backend.response.taint.TaintingHelper;
import soot.Unit;
import soot.jimple.IdentityStmt;

public class Unit_Identity {
	public static void Handle(Unit ut, ParameterBucket pb) {
		IdentityStmt is = (IdentityStmt) ut;
		String leftOP = is.getLeftOp().toString();
		String rightOP = is.getRightOp().toString().split(":")[0];
		
		TaintingHelper.tryTainting(leftOP, rightOP, pb);
		
		if(rightOP.equals("@this")) 
			pb.setThisVariable(leftOP);
		
		//TaintingHelper.pointsTo(leftOP, rightOP, pb);
	}
}
