package extractocol.common.valueEntry.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.ClassConstant;
import soot.jimple.Constant;

public class General {
	static String VAR_PATTERN = "(\\$|)[ridczbl][0-9]{1,2}(_[0-9]{1,2}|)";
	static String ARRAY_PATTERN = "(\\$|)[ridczbl][0-9]{1,2}(_[0-9]{1,2}|)\\[.*\\]";
	
	static Pattern var_pattern = Pattern.compile(VAR_PATTERN);
	static Pattern arr_pattern = Pattern.compile(ARRAY_PATTERN);
	
	public static boolean isLeftOpVariable(AssignStmt as){ return isOpVariable(as.getLeftOp().toString()); }
	public static boolean isRightOpVariable(AssignStmt as){ return isOpVariable(as.getRightOp().toString()); }
	
	public static boolean isLeftOpHeap(AssignStmt as){ return isOpHeap(as.getLeftOp().toString()); }
	public static boolean isRightOpHeap(AssignStmt as){ return isOpHeap(as.getRightOp().toString()); }
	
	public static boolean isLeftOpArray(AssignStmt as){ return isOpArray(as.getLeftOp().toString()); }
	public static boolean isRightOpArray(AssignStmt as){ return isOpArray(as.getRightOp().toString()); }
	
	public static boolean isLeftOpConstant(AssignStmt as) { return isOpConstant(as.getLeftOp()); }
	public static boolean isRightOpConstant(AssignStmt as) { return isOpConstant(as.getRightOp()); }
	
	public static boolean isLeftOpClassConstant(AssignStmt as) { return isOpClassConstant(as.getLeftOp()); }
	public static boolean isRightOpClassConstant(AssignStmt as) { return isOpClassConstant(as.getRightOp()); }
	
	public static String getLeftHeap(AssignStmt as){ return getHeapName(as.getLeftOp().toString()); }
	public static String getRightHeap(AssignStmt as){ return getHeapName(as.getRightOp().toString()); }
	
	public static String getLeftVariable(AssignStmt as){ return getVariableName(as.getLeftOp().toString()); }
	public static String getRightVariable(AssignStmt as){ return getVariableName(as.getRightOp().toString()); }
	
	public static String getRightConstant(AssignStmt as){ return getConstantName(as.getRightOp().toString()); }
	public static String getLeftConstant(AssignStmt as){ return getConstantName(as.getLeftOp().toString()); }
	
	public static String getRightClassName(AssignStmt as){ return getClassName(as.getRightOp().toString()); }
	public static String getLeftClassName(AssignStmt as){ return getClassName(as.getLeftOp().toString()); }
	
	public static boolean isOpArray(String Op){ return arr_pattern.matcher(Op).find(); }
	public static boolean isOpConstant(Value Op){ return !(Op instanceof ClassConstant) && (Op instanceof Constant); }
	public static boolean isOpClassConstant(Value op) { return op instanceof ClassConstant; }
	
	public static boolean isOpVariable(String op){
		Matcher matcher = var_pattern.matcher(op);
		return matcher.find() && !op.contains("invoke") && !isOpHeap(op) && !isOpArray(op);
	}
	
	private static boolean isOpHeap(String op){
		String cropVar = CropVar(op);
		
		if(cropVar.startsWith("<") && cropVar.endsWith(">")) 
			return true;
		
		return false;
	}
	
	private static String CropVar(String var){
		if (var.startsWith("$")) return var.substring(var.indexOf(".") + 1);
		else return var;
	}
	
	public static String getClassName(String op){
		return op.split("\"")[1].replace('/', '.');
	}
	
	public static String getConstantName(String op){
		if(!op.contains("\""))
			return op;
		
		if(op.equals("\"\""))
			return "";
		
		return op.split("\"")[1];
	}
	
	// Return variable
	private static String getVariableName(String op){
		Matcher matcher = var_pattern.matcher(op);
		
		String var = null;
		while(matcher.find())
			var = matcher.group();
		
		return var;
	}
	
	// Return heap name of op
	private static String getHeapName(String op){
		if(!isOpHeap(op)) return null;
		return CropVar(op);
	}
}
