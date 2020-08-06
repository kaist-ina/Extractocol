package extractocol.common.valueEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import extractocol.Constants;
import extractocol.common.tools.Pair;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;
import extractocol.common.valueEntry.helper.General;
import extractocol.common.valueEntry.node.Array;
import extractocol.common.valueEntry.node.JSONKey;
import extractocol.common.valueEntry.node.Null;
import extractocol.common.valueEntry.unitHandle.Unit_AssignStmt;
import soot.Value;
import soot.jimple.Constant;

@SuppressWarnings("serial")
public class ValueEntryTable implements Serializable {
	private HashMap<String, ValueEntryList> Table = new HashMap<String, ValueEntryList>();
	
	public ValueEntryList getValueEntryList(String varORheap){ return this.Table.get(varORheap); }
	public ValueEntryList getValueEntryListDeep(String varORheap){
		if(this.getValueEntryList(varORheap) != null)
			return this.getValueEntryList(varORheap).getValueEntryListDeep();
		else
			return new ValueEntryList(null);
	}
	
	public void BringOriginalFromReferenceTable(){
		for(String varORheap: this.Table.keySet()){
			this.Table.put(varORheap, this.getValueEntryListDeep(varORheap));
		}
	}
	
	public HashMap<String, ValueEntryList> getValueEntryTable() { return this.Table; }
	public void setValueEntryTable(HashMap<String, ValueEntryList> _Table){ this.Table = _Table; }
	
	/** Get types of variable
	 * 
	 * @param varORheap variable or heap
	 * @return List of types
	 */
	public ArrayList<String> getTypeof(String varORheap){
		ValueEntryList vel = this.Table.get(varORheap);
		
		if(vel != null)
			return vel.getTypes();
		else
			return new ArrayList<String>();
	}
	
	
	/** Clear this table instance **/
	public void Clear(){
		makeEmpty();
		this.Table = null;
	}
	
	public void makeEmpty() {
		if(this.Table != null){
			for(String key : this.Table.keySet()){
				ValueEntryList vel = this.Table.get(key);
				vel.Clear();
				vel = null;
			}
			
			this.Table.clear();
		}
	}
	
	/** Clone this table instance **/
	public ValueEntryTable Clone(){
		ValueEntryTable newObject = new ValueEntryTable();
		HashMap<String, ValueEntryList> newTable = new HashMap<String, ValueEntryList>();
		
		for(String key: this.Table.keySet())
			newTable.put(key, this.Table.get(key).Clone());
		
		newObject.setValueEntryTable(newTable);
		return newObject;
	}
	
	/** Generate Regex of varORheap or return constant itself
	 * 
	 * @param varORheap variable, heap, or constant
	 * @return Regex of varORheap or constant itself
	 */
	public String GenRegex(String varHeapConstant){
		// Case 1: varORheap is constant (removing '"')
		if(!General.isOpVariable(varHeapConstant))
			return varHeapConstant.startsWith("\"") ? varHeapConstant.substring(0, varHeapConstant.length() - 1) : varHeapConstant;
			
		// Case 2: varORheap is variable or heap
		ValueEntryList vel = this.Table.get(varHeapConstant);
		if(vel == null) return ".*";
		
		return vel.GenRegex();
	}
	
	public String GenRegex(String varHeapConstant, boolean checkConstant){
		if(checkConstant)
			return GenRegex(varHeapConstant);
		
		ValueEntryList vel = this.Table.get(varHeapConstant);
		if(vel == null) return ".*";
		
		return vel.GenRegex();
	}
	
	public void setReferenceTable(ValueEntryTable refTable){
		for(String key : this.Table.keySet())
			this.Table.get(key).setReferenceTable(refTable);
	}
	
	public boolean isNull(String varORheap){
		return this.doesContainTypeOf(varORheap, SOURCE_TYPE.NULL);
	}
	
	public boolean doesContainTypeOf(String varORheap, SOURCE_TYPE type){
		ValueEntryList vel = this.Table.get(varORheap);
		
		if(vel == null)
			return false;
		
		return vel.doesContainTypeOf(type);
	}
	
	public void makeEmptyOf(String varORheap){
		ValueEntryList vel = this.Table.get(varORheap);
		if(vel == null) return;
		
		vel.makeEmptyOf();
	}
	
	public String getVarType(String varORheap){
		ValueEntryList vel = this.Table.get(varORheap);
		if(vel == null)
			return null;
		
		return vel.getVarType();
	}
	
	public void setVarType(String varORheap, String type){
		ValueEntryList vel = this.Table.get(varORheap);
		if(vel != null)
			vel.setVarType(type);
	}
	
	/*****************************************************************************/
	/**                               Add & Set                                 **/
	/*****************************************************************************/
	public void setValueEntryList(String varORheap, ValueEntryList list, boolean toReference){
		ValueEntryList vel = this.Table.get(varORheap);
		if(vel == null) vel = new ValueEntryList(null);
			
		vel.setValueEntryList(list, toReference); 
		this.Table.put(varORheap, vel);
	}
	
	/** Add value entry list of srcVarORHeap to that of destVarOrHeap
	 * 
	 * @param destVarORheap destination var/heap
	 * @param srcVarORheap source var/heap
	 * @param toReference True if  
	 */
	public void addValueEntryList(String destVarORheap, String srcVarORheap, boolean toReference){ 
		ValueEntryList srcVel = this.Table.get(srcVarORheap);

		if(srcVel != null)
			this.addValueEntryList(destVarORheap, srcVel, toReference);
	}
	
	public void addValueEntryList(String varORheap, ValueEntryList list, boolean toReference){ 
		ValueEntryList vel = this.Table.get(varORheap);
		if(vel == null) vel = new ValueEntryList(null);
		
		vel.addValueEntryList(list, toReference);
		this.Table.put(varORheap, vel);
	}
	
	public void OverWriteValueEntryListFromSrcToDest(String dest, String src, boolean toReference){
		if(dest == null | src == null) return;
		if(dest.equals(src)) return;
		
		ValueEntryList vel = getValueEntryList(src);
		if(vel != null)
			this.setValueEntryList(dest, vel.Clone(), toReference); 
	}
	
	public void setValueEntry(String varORheap, String value, SOURCE_TYPE type, boolean toReference){
		ValueEntryList vel = this.Table.get(varORheap);
		if(vel == null) vel = new ValueEntryList(null);
		
		vel.setValueEntry(value, type, toReference);
		this.Table.put(varORheap, vel);
	}
	
	public void addValueEntry(String varORheap, String value, SOURCE_TYPE type, boolean toReference){
		ValueEntryList vel = this.Table.get(varORheap);
		if(vel == null) vel = new ValueEntryList(null);
			
		vel.addValueEntry(value, type, toReference);
		this.Table.put(varORheap, vel);
	}

	public void addValueEntryatFirst(String varORheap, String value, SOURCE_TYPE type, boolean toReference){
		ValueEntryList vel = new ValueEntryList(null);
		vel.addValueEntry(value, type, toReference);
		if (this.Table.get(varORheap) != null)
			vel.addValueEntryList(this.Table.get(varORheap), toReference);
		this.Table.put(varORheap, vel);
	}
	
	public void setValueEntry(String varORheap, String key, String value, SOURCE_TYPE type, boolean toReference){
		ValueEntryList vel = this.Table.get(varORheap);
		if(vel == null) vel = new ValueEntryList(null);
		
		vel.setValueEntry(key, value, type, toReference);
		this.Table.put(varORheap, vel);
	}
	
	public void setValueEntry(String varORheap, String key, ValueEntryList value, SOURCE_TYPE type, boolean toReference){
		ValueEntryList vel = this.Table.get(varORheap);
		if(vel == null) vel = new ValueEntryList(null);
		
		vel.setValueEntry(key, value, type, toReference);
		this.Table.put(varORheap, vel);
	}
	
	public void addValueEntry(String varORheap, String key, String value, SOURCE_TYPE type, boolean toReference){
		ValueEntryList vel = this.Table.get(varORheap);
		if(vel == null) vel = new ValueEntryList(null);
			
		vel.addValueEntry(key, value, type, toReference);
		this.Table.put(varORheap, vel);
	}
	
	public void addValueEntry(String varORheap, String key, ValueEntryList value, SOURCE_TYPE type, boolean toReference){
		ValueEntryList vel = this.Table.get(varORheap);
		if(vel == null) vel = new ValueEntryList(null);
			
		vel.addValueEntry(key, value, type, toReference);
		this.Table.put(varORheap, vel);
	}
	
	public void setValueEntry(String varORheap, String key, String value, SOURCE_TYPE type, ValueEntryTable refTable, boolean toReference){
		ValueEntryList vel = this.Table.get(varORheap);
		if(vel == null) vel = new ValueEntryList(null);
		
		vel.setValueEntry(key, value, type, refTable, toReference);
		this.Table.put(varORheap, vel);
	}
	
	public void addValueEntry(String varORheap, String key, String value, SOURCE_TYPE type, ValueEntryTable refTable, boolean toReference){
		ValueEntryList vel = this.Table.get(varORheap);
		if(vel == null) vel = new ValueEntryList(null);
			
		vel.addValueEntry(key, value, type, refTable, toReference);
		this.Table.put(varORheap, vel);
	}
	
	public void addValueEntry(String varORheap, ValueEntry ve, boolean toReference){
		ValueEntryList vel = this.Table.get(varORheap);
		if(vel == null) vel = new ValueEntryList(null);
		
		vel.addValueEntry(ve, toReference);
		this.Table.put(varORheap, vel);
	}
	
	public void setValueEntry(String varORheap, ValueEntry ve, boolean toReference){
		ValueEntryList vel = this.Table.get(varORheap);
		if(vel == null) vel = new ValueEntryList(null);
		
		vel.setValueEntry(ve, toReference);
		this.Table.put(varORheap, vel);
	}
	
	public void clearValueEntryListOf(String varORheap) {
		ValueEntryList vel = this.Table.get(varORheap);
		if(vel == null)
			return;
		
		for(ValueEntry ve: vel.getValueEntryList())
			ve.Clear();
		
		vel.getValueEntryList().clear();
	}
	

	/****************************************************************************/
	/***                      APIs for Reference Entry                        ***/
	/****************************************************************************/
	/** Brief Adds heap name on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param heapName heap name (class variable)
	 */
	public void addReferenceValue(String varORheap, String MethodAndUnit, String type, ValueEntryTable refTable){
		addValueEntry(varORheap, MethodAndUnit, type, SOURCE_TYPE.REFERENCE, refTable, false);
	}
	
	/** Brief Sets heap name on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param heapName heap name (class variable)
	 */
	public void setReferenceValue(String varORheap, String MethodAndUnit, String type, ValueEntryTable refTable){
		setValueEntry(varORheap, MethodAndUnit, type, SOURCE_TYPE.REFERENCE, refTable, false);
	}
	
	/****************************************************************************/
	/***                       APIs for Heap Entry                            ***/
	/****************************************************************************/
	/** Brief Adds heap name on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param heapName heap name (class variable)
	 */
	public void addHeapValue(String varORheap, String heapName, boolean toReference){
		addValueEntry(varORheap, heapName, SOURCE_TYPE.HEAP, toReference);
	}
	
	/** Brief Sets heap name on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param heapName heap name (class variable)
	 */
	public void setHeapValue(String varORheap, String heapName, boolean toReference){
		setValueEntry(varORheap, heapName, SOURCE_TYPE.HEAP, toReference);
	}
	
	/****************************************************************************/
	/***                       APIs for Null Entry                            ***/
	/****************************************************************************/
	/** Add 'Null' entry to variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 */
	public void addNullValue(String varORheap){
		addValueEntry(varORheap, new Null(), false);
	}
	
	/** Set 'Null' entry to variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 */
	public void setNullValue(String varORheap){
		setValueEntry(varORheap, new Null(), false);
	}
	
	/****************************************************************************/
	/***                       APIs for List Entry                            ***/
	/****************************************************************************/
	/** Adds string on the list entry of variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param _string heap name (class variable)
	 */
	public void addListValue(String varORheap, String _string, boolean toReference){
		addValueEntry(varORheap, _string, SOURCE_TYPE.LIST, toReference);
	}
	
	/** sets string on the list entry of variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param _string heap name (class variable)
	 */
	public void setListValue(String varORheap, String _string, boolean toReference){
		setValueEntry(varORheap, _string, SOURCE_TYPE.LIST, toReference);
	}
	
	/****************************************************************************/
	/***                       APIs for Array Entry                           ***/
	/****************************************************************************/
	/** Allocate new array
	 * 
	 * @param varORheap variable or heap
	 * @param size array size
	 * @param toReference True if, and only if, you want to add this entry into the reference table
	 */
	public void AllocateArray(String varORheap, String size, boolean toReference){
		addValueEntry(varORheap, size, SOURCE_TYPE.ARRAY, toReference);
	}
	
	/** Insert constant value at the index
	 * 
	 * @param varORheap variable or heap
	 * @param idx array index
	 * @param value constant value
	 * @param toReference True if, and only if, you want to add this entry into the reference table
	 */
	public void addArrayValue(String varORheap, String idx, String value, boolean toReference){
		addValueEntry(varORheap, idx, value, SOURCE_TYPE.ARRAY, toReference);
	}
	
	/** Insert ValueEntryList at the index
	 * 
	 * @param varORheap variable or heap
	 * @param idx array index
	 * @param vel ValueEntryList
	 * @param toReference True if, and only if, you want to add this entry into the reference table
	 */
	public void addArrayValue(String varORheap, String idx, ValueEntryList vel, boolean toReference){
		addValueEntry(varORheap, idx, vel, SOURCE_TYPE.ARRAY, toReference);
	}
	
	/** Get string array from 'Array' entry
	 * 
	 * @param variable variable name
	 * @return string array of the variable
	 */
	public String[] getArray(String variable){
		ValueEntryList vel = this.getValueEntryList(variable);
		
		if(vel != null){
			Array arrEntry = (Array) vel.getValueEntryOf(SOURCE_TYPE.ARRAY);
			
			if(arrEntry != null){
				ValueEntryList[] array = arrEntry.getArray();
				
				if(array != null){
					String[] result = new String[array.length];
					
					for(int i = 0; i < array.length; i++)	
						result[i] = array[i].GenRegex();
					
					return result;
				}
			}
		}
		
		return null;
	}

	/** Set string array at varORheap
	 * 
	 * @param varORheap variable or heap
	 * @param str string array
	 */
	public void setArrayValue(String varORheap, String[] str){
		ValueEntryList vel = this.Table.get(varORheap);
		if(vel == null)
			return;
		
		Array arr = (Array) vel.getValueEntryOf(SOURCE_TYPE.ARRAY);
		if(arr == null)
			return;
		
		arr.setArrayEntry(str);
	}
	
	/*public void setArrayValue(String varORheap, String idx, ValueEntryList vel, boolean toReference){
		setValueEntry(varORheap, idx, vel, SOURCE_TYPE.ARRAY, toReference);
	}*/
	
	
	
	/****************************************************************************/
	/***                     APIs for Constant Entry                          ***/
	/****************************************************************************/
	/** Brief Adds constant value on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param value constant value (e.g., "string", 1, etc)
	 */
	public void addConstantValue(String varORheap, String value, boolean toReference){
		addValueEntry(varORheap, value, SOURCE_TYPE.CONSTANT, toReference);
	}

	public void addConstantValueatFirst(String varORheap, String value, boolean toReference){
		addValueEntryatFirst(varORheap, value, SOURCE_TYPE.CONSTANT, toReference);
	}
	
	public void addConstantValue(String varORheap, String[] valueList, boolean toReference){
		for(String value: valueList)
			addValueEntry(varORheap, value, SOURCE_TYPE.CONSTANT, toReference);
	}
	
	/** Brief Sets constant value on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param value constant value (e.g., "string", 1, etc)
	 */
	public void setConstantValue(String varORheap, String value, boolean toReference){
		setValueEntry(varORheap, value, SOURCE_TYPE.CONSTANT, toReference);
	}
	
	public void setConstantValue(String varORheap, String[] valueList, boolean toReference){
		if(valueList == null)
			return;
		
		setValueEntry(varORheap, valueList[0], SOURCE_TYPE.CONSTANT, toReference);
		
		for(int i = 1; i < valueList.length; i++)
			addValueEntry(varORheap, valueList[i], SOURCE_TYPE.CONSTANT, toReference);
	}
	
	/****************************************************************************/
	/***                          APIs for Map Entry                          ***/
	/****************************************************************************/
	/** Brief Adds constant value on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param value constant value (e.g., "string", 1, etc)
	 */
	public void addMapValue(String varORheap, Value key, Value value, boolean toReference){
		addValueEntry(varORheap, getFixedValueFromArg(key), getFixedValueFromArg(value), SOURCE_TYPE.MAP, toReference);
	}
	
	/** Brief Sets constant value on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param value constant value (e.g., "string", 1, etc)
	 */
	public void setMapValue(String varORheap, String key, String value, boolean toReference){
		setValueEntry(varORheap, key, value, SOURCE_TYPE.MAP, toReference);
	}
	
	/** Get Map list of varORheap
	 * 
	 * @param varORheap variable or heap
	 * @return List of Maps
	 */
	public ArrayList<Pair> getMap(String varORheap){
		ValueEntryList vel = this.getValueEntryList(varORheap);
		
		if(vel == null)
			return new ArrayList<Pair>();
		
		return vel.getMap();
	}
	
	/** Get list of varORheap
	 * 
	 * @param varORheap variable or heap
	 * @return string list
	 */
	public java.util.List<String> getList(String varORheap){
		ValueEntryList vel = this.getValueEntryList(varORheap);
		
		if(vel == null)
			return new ArrayList<String>();
		
		return vel.getList();
	}
	
	/****************************************************************************/
	/***                   APIs for stringbuilder Entry                       ***/
	/****************************************************************************/
	/** Append _string to stringbuilder entry
	 * 
	 * @param varORheap variable or heap
	 * @param _string string
	 */
	/*public void AppendStrToStringBuilder(String varORheap, String _string){
		addValueEntry(varORheap, _string, SOURCE_TYPE.STRBUILDER, true);
	}*/
	
	/** Append _string to stringbuilder entry
	 * 
	 * @param varORheap variable or heap
	 * @param _string string
	 */
	public void AppendStrToStringBuilder(String varORheap, Value val){
		String _string;
		if(val instanceof Constant)
			_string = getRefinedConstant(val.toString());
		else
			_string = this.GenRegex(val.toString());
		
		addValueEntry(varORheap, _string, SOURCE_TYPE.STRBUILDER, true);
	}


	/****************************************************************************/
	/***                        APIs for Type Entry                           ***/
	/****************************************************************************/
	/** Brief Sets heap name on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param thisName 
	 */
	public void setTypeEntry(String varORheap, String typeName, boolean toReference){
		setValueEntry(varORheap, typeName, SOURCE_TYPE.TYPE, toReference);
	}
	
	/** Brief Adds heap name on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param thisName 
	 */
	public void addTypeEntry(String varORheap, String typeName, boolean toReference){
		addValueEntry(varORheap, typeName, SOURCE_TYPE.TYPE, toReference);
	}
	
	/****************************************************************************/
	/***                        APIs for Class Entry                          ***/
	/****************************************************************************/
	public void setClassEntry(String varORheap, String className, boolean toReference){
		setValueEntry(varORheap, className, SOURCE_TYPE.CLASS, toReference);
	}
	
	public String getClassNameOf(String varORheap) {
		ValueEntryList vel = this.Table.get(varORheap);
		
		if(vel != null)
			return vel.getClassName();
		else
			return null;
	}
	
	
	/****************************************************************************/
	/***                  APIs for CollectionType Entry                       ***/
	/****************************************************************************/
	public void addCollectionTypeEntry(String varORheap, String typeName, boolean toReference){
		addValueEntry(varORheap, typeName, SOURCE_TYPE.COLLECTIONTYPE, toReference);
	}
	
	public java.util.List<String> getCollectionTypeList(String varORheap){
		ValueEntryList vel = this.Table.get(varORheap);
		
		if(vel != null)
			return vel.getCollectionTypeList();
		else
			return new ArrayList<String>();
	}
	
	
	/****************************************************************************/
	/***                        APIs for JSON Entry                           ***/
	/****************************************************************************/
	/** Add JSON key. It adds new JSON entry if there is no JSON entry, or it appends key to the existing JSON entry if more than one of JSON entry exists
	 * 
	 * @param varORheap name of variable or heap
	 * @param key a JSON key
	 */
	public void addJSONKey(String varORheap, String key, boolean toReference){
		addValueEntry(varORheap, key, SOURCE_TYPE.JSONKEY, toReference);
	}
	
	@SuppressWarnings("unchecked")
	public void addNewJSONKeyList(String varORheap, java.util.List<String> JSONKeyList, boolean toReference) {
		ValueEntryList vel = this.Table.get(varORheap);
		ValueEntry e;
		if((vel == null) || ((e = vel.getValueEntryOf(SOURCE_TYPE.JSONKEY)) == null)) {
			for(String key: JSONKeyList)
				addJSONKey(varORheap, key, toReference);		
		}else {
			((JSONKey)e).addNewJSONKeyList((ArrayList)JSONKeyList);
		}
	}
	
	/** Set JSON key list to a variable or heap
	 * 
	 * @param dst destination variable/heap name
	 * @param src source variable/heap name
	 */
	public void OverwriteJSONKeyListFromTo(String dst, String src){
		ValueEntryList velSrc = this.Table.get(src);
		ValueEntry srcJSONEntry;
		if(velSrc == null) return;
		if((srcJSONEntry = velSrc.getValueEntryOf(SOURCE_TYPE.JSONKEY)) == null) srcJSONEntry = new JSONKey(null);
		
		ValueEntryList velDst = this.Table.get(dst);
		if(velDst == null) velDst = new ValueEntryList(null);
		
		velDst.setValueEntry(srcJSONEntry.Clone(), false);
		this.Table.put(dst, velDst);
	}
	
	public java.util.List<String> getJSONKeyListOf(String varORheap){
		ValueEntryList vel = this.Table.get(varORheap);
		
		if(vel == null)
			return new ArrayList<String>();
		else
			return vel.getJSONKeyList();
	}
	
	public void addJSONKeyListAtFront(java.util.List<String> JSONKeyList) {
		for(ValueEntryList vel : this.Table.values()) {
			ValueEntry e = vel.getValueEntryOf(SOURCE_TYPE.JSONKEY);
			
			if(e == null)
				continue;
			
			((JSONKey)e).addKeyAtFront(JSONKeyList);
		}
	}
	
	
	
	
	/****************************************************************************/
	/***                                Tools                                 ***/
	/****************************************************************************/
	
	public String getFixedValueFromArg(Value arg){
		if(arg instanceof Constant)
			return getRefinedConstant(arg.toString());
		else
			return this.GenRegex(arg.toString());
	}
	
	/** Append parenthesis to the string
	 *  Ex1: 0|1|-1 -> (0|1|-1)
	 *  Ex2: (0|1|-1) -> (0|1|-1)
	 *  Ex3: (0|1)|(-1|-2) -> ((0|1)|(-1|-2))
	 *  Ex4: w=(0|1|-1) -> w=(0|1|-1)
	 *  Ex5: w=(0|1|-1)|s=0 -> (w=(0|1|-1)|s=0)
	 *  Ex6: w=(0|1|-1)|s=(0|1) -> (w=(0|1|-1)|s=(0|1))
	 *  Ex7: w=(0|1|-1)&s=(0|1) -> w=(0|1|-1)&s=(0|1)
	 *  Ex8: \|T\|(.*|.*44.*)\|GCM -> \|T\|(.*|.*44.*)\|GCM
	 * 
	 * @param str input string
	 * @return parenthesis-appended string
	 */
	public static String AppendParenthesis(String str){
		int open = 0;
		int orNum = 0;
		boolean backslash = false;
		
		for(int i = 0; i < str.length(); i ++){
			if(str.charAt(i) == '\\'){
				if(i+1 < str.length())
					if(str.charAt(i+1) == '|')
						backslash = true;
			}else if(str.charAt(i) == '(') 
				open++;
			else if(str.charAt(i) == ')') 
				open--;
			else if(str.charAt(i) == '|' && open == 0 && !backslash) {
				orNum++; 
				backslash = false;
			} 
		}
		
		return (orNum > 0) ? "(" + str + ")" : str;
	}
	
	/**
	 * 
	 * @param _string
	 * @return
	 */
	public static String getRefinedConstant(String _string){
		if(_string.startsWith("\"") && _string.endsWith("\""))
			return _string.substring(1, _string.length() - 1).replaceAll("\\|", "\\\\|");
		else
			return _string;
	}
}
