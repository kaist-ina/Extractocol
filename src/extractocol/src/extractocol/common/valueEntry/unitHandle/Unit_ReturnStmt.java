package extractocol.common.valueEntry.unitHandle;

import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntryTable;
import extractocol.common.valueEntry.ValueEntryTracking;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;
import soot.Unit;
import soot.jimple.Constant;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;

public class Unit_ReturnStmt {
	public static void Handler(Unit ut, ValueEntryTracking vet){
		if(ut instanceof ReturnVoidStmt)
			return;
		
		ReturnStmt rs = (ReturnStmt) ut;
		
		String retOp = rs.getOp().toString();
				
		if(retOp == null) 
			return;
		
		if(retOp.toString().equals("null"))
			return;
		
		if(rs.getOp() instanceof Constant)
			vet.returnValueEntryList.addValueEntry(ValueEntryTable.getRefinedConstant(retOp), SOURCE_TYPE.CONSTANT, false);
		else{
			ValueEntryList vel = vet.varTable.getValueEntryList(retOp);
			if(vel != null)
				vet.returnValueEntryList.addValueEntryList(vel.Clone(), false);
		}
			
		
	}
}
