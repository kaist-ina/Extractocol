package Extractocol.BufferExtractor_Request.HeapManagement;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapEntry.SOURCE_TYPE;
import Extractocol.BufferExtractor_Request.Semantic.URL.UrlBuilder;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;
import Extractocol.BufferExtractor_Response.Basics.ParameterBucket;
import Extractocol.Outputs.RequestInfoList;
import Extractocol.Outputs.ResponseInfoList;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;

public class ValueEntryTracking {
	// BK For tracking JSON key list of variable
	public HeapTable varTable;
	
	// For tracking a list of value entries for return variable
	public ArrayList<HeapEntry> returnValueEntryList;
	
	// For tracking a list of value entries for base variable
	public String baseOfCaller;
	
	public ValueEntryTracking(){
		varTable = new HeapTable();
		returnValueEntryList = new ArrayList<HeapEntry>();
		baseOfCaller = null;
	}
	
	public static void heapAndVarTracking(AssignStmt as, ValueEntryTracking pb, boolean isBackward){
		/** variables are tracked locally and heaps are tracked globally. **/
		// (1) when (<> or $r) = (<> or $r),  
		if((isLeftOpHeap(as) || isLeftOpVariable(as)) && (isRightOpHeap(as) || isRightOpVariable(as))){
			// case 1: <> = $r (Append the entry list of $r to that of <>)
			if(isLeftOpHeap(as) && isRightOpVariable(as))
				Constants.reqRespInfo.addValueEntryList(getLeftHeap(as), pb.varTable.getHeapEntryList(as.getRightOp().toString()));
			
			// case 2: $r = <>
			if(isLeftOpVariable(as) && isRightOpHeap(as))
				pb.varTable.OverwriteHeapEntryListTo(as.getLeftOp().toString(), Constants.reqRespInfo.getValueEntryListOf(getRightHeap(as)));
			
			// case 3: $r = $r
			if(isLeftOpVariable(as) && isRightOpVariable(as))
				pb.varTable.OverWriteHeapEntryListFromSrcToDest(as.getLeftOp().toString(), as.getRightOp().toString());
		}
		
		// (2) when right Op is constant (e.g., = constant)
		if(as.getRightOp() instanceof Constant){
			// case 1: <> = constant
			if(isLeftOpHeap(as))
				Constants.reqRespInfo.setConstantEntry(getLeftHeap(as), as.getRightOp().toString());
				
			// case 2: $r = constant
			if(isLeftOpVariable(as))
				pb.varTable.setConstantValue(as.getLeftOp().toString(), as.getRightOp().toString());
		}
	}
	
	/****************************************************************************/
	/***                        APIs for unit handling                        ***/
	/****************************************************************************/
	public void ParameterIdentityStmtHandler(IdentityStmt ds, String param){
		this.varTable.OverWriteHeapEntryListFromSrcToDest(ds.getLeftOp().toString(), param);
	}
	
	public void NewAssignmentHandler(AssignStmt as){
		this.varTable.addTypeEntry(as.getLeftOp().toString(), as.getRightOp().getType().toString());
	}
	
	public void ThisObjectHandler(IdentityStmt ds){
		String destVar = ds.getLeftOp().toString();
		
		// 1. relfect valueEntryList of base to the left Op
		ValueEntryTracking.overwriteValueEntryList(destVar, "@this", this);
		
		// 2. add THIS entry into the list of the left op
		this.varTable.addTypeEntry(destVar, ds.getLeftOp().getType().toString());
		//pb.varTable.addTypeEntry("@this", ds.getLeftOp().getType().toString());
		
		// 3. match base of caller with the variable of callee
		this.setBaseOfCaller(destVar);		
	}
	
	public void ReturnStmtHandler(ReturnStmt rs){
		String retOp = rs.getOp().toString();
		
		if(retOp == null) 
			return;
		
		if(rs.getOp() instanceof Constant)
			this.returnValueEntryList.add(new HeapEntry(SOURCE_TYPE.CONSTANT, retOp));
		else{
			ArrayList<HeapEntry> list = this.varTable.getHeapEntryList(retOp);
			if(list != null)
				this.returnValueEntryList.addAll(list);
		}
	}
	
	
	
	public String getBaseOfCaller(){
		return this.baseOfCaller; 
	}
	
	public void setBaseOfCaller(String variable){
		this.baseOfCaller = variable;
	}
	
	public static boolean isLeftOpVariable(AssignStmt as){
		return isOpVariable(as.getLeftOp().toString());
	}
	
	public static boolean isRightOpVariable(AssignStmt as){
		return isOpVariable(as.getRightOp().toString());
	}
	
	private static boolean isOpVariable(String op){
		Pattern var_pattern = Pattern.compile("(\\$|)[ridczb][0-9]{1,2}(|_[0-9]{1,2})");
		Matcher matcher = var_pattern.matcher(op);
		
		return matcher.matches();

		// Old version
		//boolean ret = false;
		//if(op.startsWith("$") && !op.contains("<") && !op.contains(" "))
		//	ret = true;
		//return ret; 
	}
	
	// Return heap name of op
	private static String getHeapName(String op){
		if(!isOpHeap(op)) return null;
		return CropVar(op);
	}
	
	// Return heap name of left op
	public static String getLeftHeap(AssignStmt as){
		return getHeapName(as.getLeftOp().toString());
	}
	
	// Return heap name of right op
	public static String getRightHeap(AssignStmt as){
		return getHeapName(as.getRightOp().toString());
	}

	private static String CropVar(String var)
	{
		if (var.startsWith("$"))
			return var.substring(var.indexOf(".") + 1);
		else
			return var;
	}
	
	private static boolean isOpHeap(String op){
		String cropVar = CropVar(op);
		
		if(cropVar.startsWith("<") && cropVar.endsWith(">"))
			return true;
		
		return false;
	}
	
	public static boolean isRightOpHeap(AssignStmt as){
		return isOpHeap(as.getRightOp().toString());
	}
	
	public static boolean isLeftOpHeap(AssignStmt as){
		return isOpHeap(as.getLeftOp().toString());
	}	
	
	// BK
	public static void deliverJSONKeyListofCallertoCallee(InvokeExpr ie, ValueEntryTracking pb, ValueEntryTracking new_pb){
		for (int i = 0; i < ie.getArgCount(); i++)
		{
			String arg = ie.getArg(i).toString();
			String parameter = "@parameter" + i;
			
			if(ie.getArg(i) instanceof Constant)
				new_pb.varTable.addConstantValue(parameter, arg);
			else
				new_pb.varTable.OverwriteHeapEntryListTo(parameter, pb.varTable.getHeapEntryList(arg));
		}
	}	
	
	// BK deliver valueEntryList of base value to callee method
	public static void deliverValueEntryListofBaseVariabletoCallee(String baseVar, ValueEntryTracking caller_pb, ValueEntryTracking callee_pb){
		callee_pb.varTable.OverwriteHeapEntryListTo("@this", caller_pb.varTable.getHeapEntryList(baseVar));
	}
	
	// BK deliver valueEntryList of source to that of destination
	public static void overwriteValueEntryList(String dest, String src, ValueEntryTracking pb){
		pb.varTable.OverWriteHeapEntryListFromSrcToDest(dest, src);
	}

	// BK reflect valueEntryList of return value to the destination variable
	public static void reflectValueEntryListofReturnVariable(String destVar, ValueEntryTracking caller_pb, ValueEntryTracking callee_pb){
		caller_pb.varTable.OverwriteHeapEntryListTo(destVar, callee_pb.returnValueEntryList);
	}
	
	// BK reflect valueEntryList of base value to the destination variable
	public static void reflectValueEntryListofBaseVariable(String baseVar, ValueEntryTracking caller_pb, ValueEntryTracking callee_pb){
		caller_pb.varTable.OverwriteHeapEntryListTo(baseVar, callee_pb.varTable.getHeapEntryList(callee_pb.getBaseOfCaller()));
	}
	
	
}
