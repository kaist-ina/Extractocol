package extractocol.common.valueEntry.unitHandle;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntryTracking;
import extractocol.common.valueEntry.helper.General;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;
import extractocol.common.valueEntry.node.Array;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.ClassConstant;
import soot.jimple.NewExpr;
import soot.shimple.PhiExpr;

public class Unit_AssignStmt {
	static Integer SEED = 0;
	static Integer INCRMENT = 7;
	static Integer MAX = 100;
	/*static String VAR_PATTERN = "(\\$|)[ridczb][0-9]{1,2}(_[0-9]{1,2}|)";
	static String ARRAY_PATTERN = "(\\$|)[ridczb][0-9]{1,2}(_[0-9]{1,2}|)\\[.*\\]";
	
	static Pattern var_pattern = Pattern.compile(VAR_PATTERN);
	static Pattern arr_pattern = Pattern.compile(ARRAY_PATTERN);*/
	
	public static void Handler(AssignStmt as, String method, ValueEntryTracking vet){
		// Case 1: new statement (e.g., $r1 = new java.lang.RuntimeException)
		if (as.getRightOp() instanceof NewExpr)
			NewStmtHandler(as, method, vet);
		// Case 2: newarray statement (e.g., $r0 = newarray (int)[1])
		else if(as.getRightOp().toString().contains("newarray"))
			NewArrayStmtHandler(as, vet);
		// Case 3: Phi node
		else if (as.getRightOp() instanceof PhiExpr)
			PhiStmtHandler(as, vet);
		// Case 4: set class statement (e.g., $v = class "className";)
		else if (as.getRightOp() instanceof ClassConstant)
			Var_ClassConstant(as, vet);
		// Case 5: other assignment statement
		else
			NormalAssignmentHandler(as, vet);
		
		SetVarType(as, vet);
	}
	
	
	
	private static void NewStmtHandler(AssignStmt as, String method, ValueEntryTracking vet){
		String methodAndUnit = as.toString() +  "####" + method + "####" + Random(); // methodAndUnit will be used for generating reference key
		vet.varTable.setReferenceValue(as.getLeftOp().toString(), methodAndUnit, as.getRightOp().getType().toString(), vet.referenceTable);
	}
	
	private static void NewArrayStmtHandler(AssignStmt as, ValueEntryTracking vet){
		String arraystring = as.getRightOp().toString();
		String number = arraystring.substring(arraystring.indexOf("[") + 1, arraystring.indexOf("]"));
		
		vet.varTable.AllocateArray(General.getLeftVariable(as), number, false);
	}
	
	/** Handle Phi node
	 * Ex) add value entry list of $i3_1 and i3_2 to that of $i3 when $i3 = Phi($i3_1 #0, $i3_2 #2)
	 * 
	 * @param as Assign statement
	 * @param vet Instance of value entry tracking 
	 */
	private static void PhiStmtHandler(AssignStmt as, ValueEntryTracking vet){
		String leftVar = as.getLeftOp().toString();
		PhiExpr phi = (PhiExpr) as.getRightOp();
		
		for(Value v : phi.getValues()){
			String rightVar = v.toString();
			
			if(!leftVar.equals(rightVar))
				vet.varTable.addValueEntryList(leftVar, rightVar, false);
		}
	}
	
	private static void NormalAssignmentHandler(AssignStmt as, ValueEntryTracking vet){
		// Case 1: $v = $v
		if(General.isLeftOpVariable(as) && General.isRightOpVariable(as)) 
			Var_Var(as, vet);
		// Case 2: $v = $v[]
		else if(General.isLeftOpVariable(as) && General.isRightOpArray(as)) 
			Var_Array(as, vet);
		// Case 3: $v = <>
		else if(General.isLeftOpVariable(as) && General.isRightOpHeap(as)) 
			Var_Heap(as, vet);
		// Case 4: $v = Constant
		else if(General.isLeftOpVariable(as) && General.isRightOpConstant(as)) 
			Var_Constant(as, vet);
		// Case 5: $v[] = $v
		else if(General.isLeftOpArray(as) && General.isRightOpVariable(as)) 
			Array_Var(as, vet);
		// Case 6: $v[] = Constant
		else if(General.isLeftOpArray(as) && General.isRightOpConstant(as)) 
			Array_Constant(as, vet);
		// Case 7: <> = $v
		else if(General.isLeftOpHeap(as) && General.isRightOpVariable(as)) 
			Heap_Var(as, vet);
		// Case 8: <> = Constant
		else if(General.isLeftOpHeap(as) && General.isRightOpConstant(as)) 
			Heap_Constant(as, vet);
	}
	
	private static void SetVarType(AssignStmt as, ValueEntryTracking vet){
		if(General.isLeftOpVariable(as))
			vet.varTable.setVarType(General.getLeftVariable(as), as.getLeftOp().getType().toString());
		else if(General.isLeftOpHeap(as))
			vet.heapTable.setVarType(General.getLeftHeap(as), as.getLeftOp().getType().toString());
	}
	
	
	/****************************************************************************/
	/***                         APIs for handling                            ***/
	/****************************************************************************/
	private static void Var_Var(AssignStmt as, ValueEntryTracking vet){
		vet.varTable.OverWriteValueEntryListFromSrcToDest(General.getLeftVariable(as), General.getRightVariable(as), false);
	}
	
	private static void Var_Array(AssignStmt as, ValueEntryTracking vet){
		vet.varTable.setValueEntryList(General.getLeftVariable(as), getValueOfArray(as.getRightOp(), vet), false);
	}
	
	private static void Var_Heap(AssignStmt as, ValueEntryTracking vet){
		ValueEntryList vel = vet.heapTable.getValueEntryList(General.getRightHeap(as));
		
		if(vel == null) // add heap entry if the heap has no entry.
			vet.varTable.setHeapValue(General.getLeftVariable(as), General.getRightHeap(as), false);
		else if(vel.size() == 0) 
			vet.varTable.setHeapValue(General.getLeftVariable(as), General.getRightHeap(as), false);
		else // add entry list of the heap if the heap has a entry list.
			vet.varTable.setValueEntryList(General.getLeftVariable(as), vel.Clone(), false);
	}
	
	private static void Var_Constant(AssignStmt as, ValueEntryTracking vet){
		if(General.getRightConstant(as).equals("null"))
			vet.varTable.setNullValue(General.getLeftVariable(as));
		else
			vet.varTable.setConstantValue(General.getLeftVariable(as), General.getRightConstant(as), false);
	}
	
	private static void Array_Var(AssignStmt as, ValueEntryTracking vet){
		ValueEntryList vel = getValueOfArray(as.getLeftOp(), vet);
		
		if(vel != null)
			vel.setValueEntryList(vet.varTable.getValueEntryList(General.getRightVariable(as)), false);
	}
	
	private static void Array_Constant(AssignStmt as, ValueEntryTracking vet){
		if(General.getRightConstant(as).equals("null"))
			return;
		
		ValueEntryList vel = getValueOfArray(as.getLeftOp(), vet);
		
		if(vel != null)
			vel.setValueEntry(General.getRightConstant(as), SOURCE_TYPE.CONSTANT, false);
	}
	
	private static void Heap_Var(AssignStmt as, ValueEntryTracking vet){
		ValueEntryList vel = vet.varTable.getValueEntryList(General.getRightVariable(as));
		if (vel != null)
			vet.heapTable.setValueEntryList(General.getLeftHeap(as), vel.Clone(), false);
	}
	
	private static void Heap_Constant(AssignStmt as, ValueEntryTracking vet){
		if(General.getRightConstant(as).equals("null"))
			vet.heapTable.setNullValue(General.getLeftHeap(as));
		else
			vet.heapTable.setConstantValue(General.getLeftHeap(as), General.getRightConstant(as), false);
	}
	
	private static void Var_ClassConstant(AssignStmt as, ValueEntryTracking vet){
		if(!General.isRightOpClassConstant(as))
			return;
		
		vet.varTable.setClassEntry(General.getLeftVariable(as), General.getRightClassName(as), false);
	}

	/****************************************************************************/
	/***                           Tools for array                            ***/
	/****************************************************************************/
	/** Get ValueEntryList of the array at the index (e.g., ValueEntryList is for $v0[$i0])
	 * 
	 * @param op Op of assignment statement
	 * @param vet ValueEntryTracking (containing VarTable, HeapTable, and RefTable)
	 * @return ValueEntryList of the array at the index
	 */
	private static ValueEntryList getValueOfArray(Value op, ValueEntryTracking vet){
		if(!(op instanceof ArrayRef))
			return null;
		
		// Get base variable of the Op
		String rVar = ((ArrayRef)op).getBase().toString();
	
		// Bring ValueEntryList of the base variable
		ValueEntryList vel = vet.varTable.getValueEntryList(rVar);
		if(vel != null){
			Array arr = (Array) vel.getValueEntryOf(SOURCE_TYPE.ARRAY);
			
			// Check whether the base variable contains 'Array' entry
			if(arr != null){
				ArrayRef ar = (ArrayRef) op;
				
				// Case 1: index is constant (e.g., $v0[3])
				if(ar.getIndex() instanceof Constant){
					return arr.getValue(ar.getIndex().toString());
				}
				else{
					ValueEntryList velOfIdx = vet.varTable.getValueEntryList(ar.getIndex().toString());
					if(velOfIdx != null){
						return arr.getValue(velOfIdx.GenRegex());
					}
				}
			}
		}
		
		return null;
	}
	
	/****************************************************************************/
	/***                                Tools                                 ***/
	/****************************************************************************/
	private static String Random(){ return ((Integer)((SEED = SEED + INCRMENT) % MAX)).toString(); }
	
	/*public static boolean isLeftOpVariable(AssignStmt as){ return isOpVariable(as.getLeftOp().toString()); }
	public static boolean isRightOpVariable(AssignStmt as){ return isOpVariable(as.getRightOp().toString()); }
	
	public static boolean isLeftOpHeap(AssignStmt as){ return isOpHeap(as.getLeftOp().toString()); }
	public static boolean isRightOpHeap(AssignStmt as){ return isOpHeap(as.getRightOp().toString()); }
	
	public static boolean isLeftOpArray(AssignStmt as){ return isOpArray(as.getLeftOp().toString()); }
	public static boolean isRightOpArray(AssignStmt as){ return isOpArray(as.getRightOp().toString()); }
	
	public static boolean isLeftOpConstant(AssignStmt as) { return isOpConstant(as.getLeftOp()); }
	public static boolean isRightOpConstant(AssignStmt as) { return isOpConstant(as.getRightOp()); }
	
	public static String getLeftHeap(AssignStmt as){ return getHeapName(as.getLeftOp().toString()); }
	public static String getRightHeap(AssignStmt as){ return getHeapName(as.getRightOp().toString()); }
	
	public static String getLeftVariable(AssignStmt as){ return getVariableName(as.getLeftOp().toString()); }
	public static String getRightVariable(AssignStmt as){ return getVariableName(as.getRightOp().toString()); }
	
	public static String getRightConstant(AssignStmt as){ return getConstantName(as.getRightOp().toString()); }
	public static String getLeftConstant(AssignStmt as){ return getConstantName(as.getLeftOp().toString()); }
	
	public static boolean isOpArray(String Op){ return arr_pattern.matcher(Op).find(); }
	public static boolean isOpConstant(Value Op){ return Op instanceof Constant; }
	
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
	
	private static String getConstantName(String op){
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
	}*/
}
