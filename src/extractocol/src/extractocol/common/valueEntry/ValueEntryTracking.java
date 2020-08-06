package extractocol.common.valueEntry;

import java.util.ArrayList;
import java.util.List;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.backend.response.helper.BlockTravelerHelper;
import extractocol.common.helper.InvokeHelper;
import extractocol.common.trackers.ImplicitCallEdgeTracker;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;
import extractocol.common.valueEntry.helper.General;
import extractocol.common.valueEntry.unitHandle.Unit_AssignStmt;
import extractocol.common.valueEntry.unitHandle.Unit_IdentityStmt;
import extractocol.common.valueEntry.unitHandle.Unit_ReturnStmt;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.ClassConstant;
import soot.jimple.Constant;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;

public class ValueEntryTracking {
	
			
	public ValueEntryTable varTable; // For tracking a list of value entries for local variables that are assigned up to the current block
	
	public ValueEntryTable heapTable; // For tracking a list of value entries for heaps that are assigned up to the current block
	public ValueEntryTable FinalHeapTable;
	
	public ValueEntryTable referenceTable;
	public ValueEntryTable FinalReferenceTable;
	
	public ValueEntryTable[] varTableArray; // Array of local variable tables 
	public ValueEntryTable[] heapTableArray; // Array of heap tables
	public ValueEntryTable[] referenceTableArray; // Array of reference tables
	
	// For tracking a list of value entries for return variable
	public ValueEntryList returnValueEntryList;
	
	// For tracking a list of value entries for base variable
	public String baseOfCaller;
	
	public ValueEntryTracking(){
		this.varTable = new ValueEntryTable();
		this.referenceTable = new ValueEntryTable();
		this.FinalReferenceTable = new ValueEntryTable();
		this.heapTable = new ValueEntryTable();
		this.FinalHeapTable = new ValueEntryTable();
		this.returnValueEntryList = new ValueEntryList(null);
		this.baseOfCaller = null;
	}
	
	public void Clear(){
		this.varTable.Clear(); this.varTable = null;
		
		this.heapTable.Clear(); this.heapTable = null;
		this.FinalHeapTable.Clear(); this.FinalHeapTable = null;
		
		this.referenceTable.Clear(); this.referenceTable = null;
		this.FinalReferenceTable.Clear(); this.FinalReferenceTable = null;
		
		if(this.varTableArray != null){
			for(int i = 0; i < this.varTableArray.length; i++){
				if(this.varTableArray[i] != null){
					this.varTableArray[i].Clear();
					this.varTableArray[i] = null;
				}
			}
			this.varTableArray = null;
		}
		
		if(this.heapTableArray != null){
			for(int i = 0; i < this.heapTableArray.length; i++){
				if(this.heapTableArray[i] != null){
					this.heapTableArray[i].Clear();
					this.heapTableArray[i] = null;
				}
			}
			this.heapTableArray = null;
		}
		
		if(this.referenceTableArray != null){
			for(int i = 0; i < this.referenceTableArray.length; i++){
				if(this.referenceTableArray[i] != null){
					this.referenceTableArray[i].Clear();
					this.referenceTableArray[i] = null;
				}
			}
			this.referenceTableArray = null;
		}
		
		this.returnValueEntryList.Clear(); this.returnValueEntryList = null;
	}
	
	
	/****************************************************************************/
	/***                        APIs for table handling                       ***/
	/****************************************************************************/
	public void UpdateReferenceTable(){
		this.varTable.setReferenceTable(this.referenceTable);
		this.heapTable.setReferenceTable(this.referenceTable);
	}
	
	public void FinalizeStmtAnalysis(){
		MergeTables(this.FinalHeapTable, this.heapTable);
		MergeTables(this.FinalReferenceTable, this.referenceTable);
	}
	
	public void PreProcessingBeforeStmtAnalysis(List<Block> predBlockList, List<Block> wholeblockList){
		// 1. Merge var/heap/reference tables of predecessor blocks
		this.MergeTablesAndSet(predBlockList, wholeblockList);
		
		// 2. Set reference table
		this.UpdateReferenceTable();
	}
	
	public void PostProcessingAfterStmtAnalysis(Block currentBlock, BriefBlockGraph Bg, boolean[][] BackEdge, String currentSM){
		// BK keep result of heap/reference table into final heap/reference table.
		// The final heap/reference tables will be delivered to the caller's heap/reference table or global heap/reference table
		if(ValueEntryTracking.doesItNeedToMerge(currentBlock, Bg, BackEdge))
			this.FinalizeStmtAnalysis();
		
		// Save var/heap/reference tables
		this.SaveTables(currentBlock.getIndexInMethod());
				
		// BK Need to merge var/heap/refer tables of a block with successors connected with back edge
		//if (currentSM.toString().contains("ch.boye.httpclientandroidlib.HttpEntity toEntity()"))
		if (ValueEntryTracking.doesItHaveBackEdge(currentBlock, BackEdge))
			ValueEntryTracking.MergeValueEntryTablesForBackEdges(this, currentBlock, BackEdge, new ArrayList<Integer>());
	}
	
	/** Allocate heap or variable table array
	 * 
	 * @param size Array size
	 */
	public void AllocTableArrays(int size){
		this.varTableArray = new ValueEntryTable[size];
		this.heapTableArray = new ValueEntryTable[size];
		this.referenceTableArray = new ValueEntryTable[size];
	}

	/** Save variable and heap tables into the arrays
	 * 
	 * @param blockNum ID of block
	 */
	public void SaveTables(int blockNum){
		this.varTableArray[blockNum] = this.varTable;
		this.heapTableArray[blockNum] = this.heapTable;
		this.referenceTableArray[blockNum] = this.referenceTable;
	}
	
	/**
	 * 
	 * @param predBlockList
	 * @param wholeblockList
	 */
	public void MergeTablesAndSet(List<Block> predBlockList, List<Block> wholeblockList)
	{
		int[] targetBlockNums = new int[predBlockList.size()];
		
		for(int i = 0; i < predBlockList.size(); i++)
			targetBlockNums[i] = wholeblockList.indexOf(predBlockList.get(i));
		
		if(targetBlockNums.length > 0)
			MergeTablesAndSet_inner(targetBlockNums);
	}
	
	/** 
	 * 
	 * @param targetBlockNums
	 */
	private void MergeTablesAndSet_inner(int[] targetBlockNums){
		// 1. Merge varTable
		this.varTable = MergeTables(this.varTableArray, targetBlockNums);
		
		// 2. Merge heapTable
		this.heapTable = MergeTables(this.heapTableArray, targetBlockNums);
		
		// 3. Merge referenceTable
		this.referenceTable = MergeTables(this.referenceTableArray, targetBlockNums);
	}
	
	/** Merge heap(var) tables
	 * 
	 * @param tableMap Map of heap(var) tables (An index is a block number.)
	 * @param targetBlockNums ID of blocks whose tables will be merged
	 * @return A merged table
	 */
	private static ValueEntryTable MergeTables(ValueEntryTable[] tableMap, int[] targetBlockNums){
		ValueEntryTable resultTable = new ValueEntryTable();
		
		for(int targetBlockNum : targetBlockNums)
			MergeTables(resultTable, tableMap[targetBlockNum]);
		
		return resultTable;
	}
	
	/** Merge two tables into one
	 * 
	 * @param dest destination table
	 * @param source source table
	 */
	public static void MergeTables(ValueEntryTable dest, ValueEntryTable source){
		if(dest == null || source == null)
			return;
		
		for(String key : source.getValueEntryTable().keySet()){
			ValueEntryList vel = source.getValueEntryList(key);
			if(vel != null)
				dest.addValueEntryList(key, vel.Clone(), false);
		}
			
	}
	
	/** Check whether a block is the end/edge block
	 * 
	 * @param currentBlock current block
	 * @param Bg block graph
	 * @param BackEdge back edge table
	 * @return True if a block is the end/edge block
	 */
	public static boolean doesItNeedToMerge(Block currentBlock, BriefBlockGraph Bg, boolean[][] BackEdge){
		// 1. Need to merge if there is no successor
		if(currentBlock.getSuccs().size() == 0)
			return true;
		
		// 2. Need to merge if all of the successor edges are back-edge
		for(Block succBlock : currentBlock.getSuccs())
			if(!BlockTravelerHelper.isBackEdge(BackEdge, Bg.getBlocks().indexOf(currentBlock), Bg.getBlocks().indexOf(succBlock)))
				return false;
		
		return true;
	}
	
	/** Check whether at least a successor of a block is connected with back edge or not
	 * 
	 * @param currentBlock current block
	 * @param BackEdge back edge table
	 * @return True if at least a successor of a block is connected with back edge or not
	 */
	public static boolean doesItHaveBackEdge(Block currentBlock, boolean[][] BackEdge){
		for(Block succBlock : currentBlock.getSuccs())
			if(BlockTravelerHelper.isBackEdge(BackEdge, currentBlock.getIndexInMethod(), succBlock.getIndexInMethod()))
				return true;
		
		return false;
	}
	
	/** Merge var/heap/refer tables of a block with successors connected with back edge 
	 * 
	 * @param pb Parameter bucket
	 * @param currentBlock current block
	 * @param BackEdge back edge table
	 * @param blockNumStack stack of block numbers (to avoid infinite loop)
	 */
	public static void MergeValueEntryTablesForBackEdges(ValueEntryTracking pb, Block currentBlock, boolean[][] BackEdge, ArrayList<Integer> blockNumStack){
		if(blockNumStack.contains(currentBlock.getIndexInMethod()))
			return;
		
		blockNumStack.add(currentBlock.getIndexInMethod());
		
		for(Block succBlock : currentBlock.getSuccs()){
			int curIdx = currentBlock.getIndexInMethod();
			int succIdx = succBlock.getIndexInMethod();
			
			if(BlockTravelerHelper.isBackEdge(BackEdge, curIdx, succIdx))
			{
				MergeTables(pb.varTableArray[succIdx], pb.varTableArray[curIdx]);
				MergeTables(pb.heapTableArray[succIdx], pb.heapTableArray[curIdx]);
				MergeTables(pb.referenceTableArray[succIdx], pb.referenceTableArray[curIdx]);
				
				if(doesItHaveBackEdge(succBlock, BackEdge))
					MergeValueEntryTablesForBackEdges(pb, succBlock, BackEdge, blockNumStack);
			}
		}
	}
	
	/** Copy table from source to destination
	 * 
	 * @param dest destination table
	 * @param source source table
	 */
	@SuppressWarnings("unchecked")
	public static void CopyTable(ValueEntryTable dest, ValueEntryTable source){
		dest.Clear();
		dest.setValueEntryTable(source.Clone().getValueEntryTable());
	}
	
	/****************************************************************************/
	/***                        APIs for unit handling                        ***/
	/****************************************************************************/
	public void AssignmentStmtHandler(AssignStmt as, String method){ Unit_AssignStmt.Handler(as, method, this); }
	public void IdentityStmtHandler(IdentityStmt ds){ Unit_IdentityStmt.Handler(ds, this); }
	public void ReturnStmtHandler(Unit ut){	Unit_ReturnStmt.Handler(ut, this); }
	
	public String getBaseOfCaller(){ return this.baseOfCaller; }
	public void setBaseOfCaller(String variable){ this.baseOfCaller = variable; }
	
	/****************************************************************************/
	/***                       APIs for invoke statement                      ***/
	/****************************************************************************/
	/** Pre-processing for invoking
	 * 
	 * @param caller Parameter bucket of caller
	 * @param callee Parameter bucket of callee
	 * @param ie InvokeExpr
	 * @param isInstanceInvoke True, if the statement is not static invoke
	 */
	public static void PreprocessingForInvoke(ValueEntryTracking caller, ValueEntryTracking callee, InvokeExpr ie, SootMethod sm, boolean isInstanceInvoke){
		// Deliver Valuye Entry Lists of arguments in caller to parameters in callee
		deliverValueEntryListofCallertoCallee(ie, caller, callee, sm);
		
		// Deliver valueEntryList of base to callee method
		if (isInstanceInvoke)
			deliverValueEntryListofBaseVariabletoCallee(((InstanceInvokeExpr) ie).getBase().toString(), caller, callee);
		
		// Deliver heap/reference tables of the current block of the caller to the callee
		CopyTable(callee.heapTable, caller.heapTable);
		CopyTable(callee.referenceTable, caller.referenceTable);
	}
	
	/** Post-processing for invoking
	 * 
	 * @param caller Parameter bucket of caller
	 * @param callee Parameter bucket of callee
	 * @param ie InvokeExpr
	 * @param destVar destination variable
	 * @param isInstanceInvoke True, if the statement is not static invoke
	 */
	public static void PostprocessingForInvoke(ValueEntryTracking caller, ValueEntryTracking callee, InvokeExpr ie, String destVar, boolean isInstanceInvoke){
		// Reflect valueEntryList of return value to the destination variable
		if(destVar != null)
			reflectValueEntryListofReturnVariable(destVar, caller, callee);
		
		// Reflect valueEntryList of base variable back from callee to caller
		if (isInstanceInvoke)
			reflectValueEntryListofBaseVariable(((InstanceInvokeExpr) ie).getBase().toString(), caller, callee);
		
		// Reflect heap/reference tables of the callee to the caller
		CopyTable(caller.heapTable, callee.FinalHeapTable);
		CopyTable(caller.referenceTable, callee.FinalReferenceTable);
		
		// Update reference table
		caller.UpdateReferenceTable();
		
		// Relieve the callee's PB memory
		callee.Clear();
		callee = null;
	}
	
	// Deliver valurEntryList of arguments to callee
	private static void deliverValueEntryListofCallertoCallee(InvokeExpr ie, ValueEntryTracking pb, ValueEntryTracking new_pb, SootMethod sm){
		// Check whether the callee method (sm) is different with ie.getMethod()
		// Some edges are semantic-modeled. (e.g., <rx: observable map(Func1)> -> Func1.call(object))
		if(isSemanticEdge(ie, pb, new_pb, sm))
			return;
		
		for (int i = 0; i < ie.getArgCount(); i++)
		{
			String arg = ie.getArg(i).toString();
			String parameter = "@parameter" + i;
			
			// Case 1: when arg is constant
			if(ie.getArg(i) instanceof Constant){
				// Case 1-1: when arg is null
				if(ie.getArg(i).toString().equals("null"))
					new_pb.varTable.setNullValue(parameter);
				// Case 1-2: when arg is ClassConstant 
				else if(ie.getArg(i) instanceof ClassConstant)
					new_pb.varTable.setClassEntry(parameter, General.getClassName(arg), false);
				// Case 1-3: other constants
				else
					new_pb.varTable.addConstantValue(parameter, ValueEntryTable.getRefinedConstant(arg), false);
			// Case 2: when arg is not constant (variable, ...)
			}else{
				ValueEntryList vel = pb.varTable.getValueEntryList(arg);
				if(vel != null)
					new_pb.varTable.setValueEntryList(parameter, vel.Clone(), false);
			}
		}
	}
	
	/** Check whether sm is semantic-modeled and handle parameter delivery case by case
	 * 
	 * @param ie invoke expression
	 * @param pb pb of caller
	 * @param new_pb pb of callee
	 * @param sm callee
	 * @return True if sm is semantic-modeled
	 */
	private static boolean isSemanticEdge(InvokeExpr ie, ValueEntryTracking pb, ValueEntryTracking new_pb, SootMethod sm) {
		// compare parameter types to check whether sm is semantic-modeled method
		if(ie.getMethod().getParameterTypes().toString().equals(sm.getParameterTypes().toString()))
			return false;
		
		// need to deobfuscate the method name
		String deobfuscated = InvokeHelper.methodDeobfuscation(ie.getMethod());
		
		// Check whether it is one of Rx operators 
		return ImplicitCallEdgeTracker.isRxOperator(deobfuscated, ie, pb, new_pb);
	}
	
	
	// BK deliver valueEntryList of base value to callee method
	private static void deliverValueEntryListofBaseVariabletoCallee(String baseVar, ValueEntryTracking caller_pb, ValueEntryTracking callee_pb){
		ValueEntryList vel = caller_pb.varTable.getValueEntryList(baseVar);
		if(vel != null)
			callee_pb.varTable.setValueEntryList("@this", vel.Clone(), false);
	}
	
	// BK deliver valueEntryList of source to that of destination
	public static void overwriteValueEntryList(String dest, String src, ValueEntryTracking pb){
		pb.varTable.OverWriteValueEntryListFromSrcToDest(dest, src, false);
	}

	// BK reflect valueEntryList of return value to the destination variable
	private static void reflectValueEntryListofReturnVariable(String destVar, ValueEntryTracking caller_pb, ValueEntryTracking callee_pb){
		caller_pb.varTable.setValueEntryList(destVar, callee_pb.returnValueEntryList.Clone(), false);
	}
	
	// BK reflect valueEntryList of base value to the destination variable
	private static void reflectValueEntryListofBaseVariable(String baseVar, ValueEntryTracking caller_pb, ValueEntryTracking callee_pb){
		ValueEntryList velBaseCaller = caller_pb.varTable.getValueEntryList(baseVar);
		ValueEntryList velBaseCallee = callee_pb.varTable.getValueEntryList(callee_pb.getBaseOfCaller());
		
		if(velBaseCaller == null) 
			velBaseCaller = new ValueEntryList(null);
		
		if(velBaseCallee != null)
			velBaseCaller.setValueEntryList(velBaseCallee.Clone(), false);
		
		caller_pb.varTable.getValueEntryTable().put(baseVar, velBaseCaller);
	}

	// JM String to Map
	public static void ToMap(String base, String src, SemanticParameterBucket spb)
	{
		// ControlBlockGroup : ()
		// Key:Value###Key:Value|x:y###y:x
		try {

			String groups[] = src.split("\\)");
			for (String group : groups) {
				String pairs[] = group.split("###");
				for (String pair : pairs) {
					String key = pair.split(":")[0];
					String value = pair.split(":")[1];
					spb.CurrentPB.varTable.addValueEntry(base, key, value, SOURCE_TYPE.MAP, true);
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("[Extractocol ToMap] : " + e.getMessage());
		}
	}
}
