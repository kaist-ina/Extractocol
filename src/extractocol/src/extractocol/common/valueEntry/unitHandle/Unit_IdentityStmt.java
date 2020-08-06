package extractocol.common.valueEntry.unitHandle;

import extractocol.common.valueEntry.ValueEntryTracking;
import soot.jimple.IdentityStmt;

public class Unit_IdentityStmt  {
	public static void Handler(IdentityStmt ds, ValueEntryTracking vet){
		String param = ds.getRightOp().toString().split(":")[0];
		
		if (ds.toString().contains("@this")){
			ThisObjectHandler(ds, vet);
		
		}else if (ds.getRightOp().toString().indexOf("@parameter") != -1)
			ParameterIdentityStmtHandler(ds, param, vet);
	}
	
	private static void ParameterIdentityStmtHandler(IdentityStmt ds, String param, ValueEntryTracking vet){
		if(vet.varTable.getValueEntryList(param) == null && ds.getRightOp().toString().contains("["))
			vet.varTable.AllocateArray(ds.getLeftOp().toString(), "30", false);
		else
			vet.varTable.OverWriteValueEntryListFromSrcToDest(ds.getLeftOp().toString(), param, false);
	}
	
	private static void ThisObjectHandler(IdentityStmt ds, ValueEntryTracking vet){
		String destVar = ds.getLeftOp().toString();
		
		// 1. relfect valueEntryList of base to the left Op
		ValueEntryTracking.overwriteValueEntryList(destVar, "@this", vet);
		
		// 2. match base of caller with the variable of callee
		vet.setBaseOfCaller(destVar);
	}
}
