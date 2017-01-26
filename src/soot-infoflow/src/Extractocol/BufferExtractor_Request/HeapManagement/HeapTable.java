package Extractocol.BufferExtractor_Request.HeapManagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import Extractocol.BufferExtractor_Request.HeapManagement.HeapEntry.SOURCE_TYPE;

public class HeapTable {
	private HashMap<String, ArrayList<HeapEntry>> HeapTable = new HashMap<String, ArrayList<HeapEntry>>();
	
	/** Brief The methods below are for heap management.
	 * 
	 * @param varORheap name of variable or heap
	 */
	public ArrayList<HeapEntry> getHeapEntryList(String varORheap){
		return this.HeapTable.get(varORheap);
	}
	
	/** Brief Return HeapTable */
	public HashMap<String, ArrayList<HeapEntry>> getHeapTable(){
		return this.HeapTable;
	}
	
	/** Brief Return type of varORheap
	 * 
	 * @param varORheap variable or heap
	 * 
	 * @return type name of the parameter
	 */
	public ArrayList<String> getTypeof(String varORheap){
		ArrayList<String> typeList = new ArrayList<String>();
		ArrayList<HeapEntry> list = this.HeapTable.get(varORheap);
		
		if(list == null)
			return null;
		
		for(HeapEntry he : list)
			if(he.getSourceType() == SOURCE_TYPE.TYPE)
				typeList.add(he.getValue());
				//return he.getValue();
		
		return typeList;
	}
	
	/** Brief add an entry into list
	 * 
	 * @param list list of HeapEntry (HeapEntry includes (i) source(value) type (JSON, constant, heap) and (ii) value
	 * @param value value will include JSON key list, constant value, or heap name
	 * @param stype source(value) type
	 */
	private void addEntry(ArrayList<HeapEntry> list, String value, SOURCE_TYPE stype){
		for(HeapEntry he : list)
			if(he.getSourceType() == stype && he.getValue().equals(value))
				return;
		
		list.add(new HeapEntry(stype, value));
	}
	
	/** Brief add an entry on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param value value will include JSON key list, constant value, or heap name
	 * @param stype source(value) type
	 */
	public void addValueEntrytoHeapTable(String varORheap, String value, SOURCE_TYPE stype){
		if(varORheap == null || value == null) 
			return;
		
		ArrayList<HeapEntry> list = this.HeapTable.get(varORheap);
		if(list == null) list = new ArrayList<HeapEntry>();
		
		addEntry(list, value, stype);
		
		this.HeapTable.put(varORheap, list);
	}
	
	/** Brief remove the existing list and add new entry for variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param value value will include JSON key list, constant value, or heap name
	 * @param stype source(value) type
	 */
	public void setValueEntrytoHeapTable(String varORheap, String value, SOURCE_TYPE stype){
		if(varORheap == null || value == null) 
			return;
		
		// 1. Remove the original list 
		makeEmptyOf(varORheap);
		
		// 2. add new item
		addValueEntrytoHeapTable(varORheap, value, stype);
	}
	
	public void makeEmptyOf(String varORheap){
		if(varORheap == null) 
			return;
		
		this.HeapTable.put(varORheap, null);
	}
	
	public boolean isEntryListNull(String varORheap){
		return (this.HeapTable.get(varORheap) == null);
	}
	
	/** Brief Up to 1 of JSON entry exists in the entry list of variable or heap
	 *
	 * @param varORheap name of variable or heap
	 *
	 * @return index of JSON entry or -1 when JSON entry does not exist.
	 */
	public int indexOfJSONEntry(String varORheap){
		for(HeapEntry he: this.HeapTable.get(varORheap))
			if(he.getSourceType() == SOURCE_TYPE.JSON)
				return this.HeapTable.get(varORheap).indexOf(he);
		
		return -1;
	}
	
	/** Brief Add JSON key. It adds new JSON entry if there is no JSON entry, or it appends key to the existing JSON entry if more than one of JSON entry exists
	 * 
	 * @param varORheap name of variable or heap
	 * @param key a JSON key
	 */
	public void addJSONKey(String varORheap, String key){
		int idx;
		
		// case 1: if the entry list is null or include no JSON entry
		if( isEntryListNull(varORheap) || ((idx = indexOfJSONEntry(varORheap)) < 0) )
			addValueEntrytoHeapTable(varORheap, key, SOURCE_TYPE.JSON);
		
		// case 2: A JSON entry exists.
		else{
			// 1. load existing JSON key list (String)
			String val = this.HeapTable.get(varORheap).get(idx).getValue();
			// 2. transform String into List
			ArrayList<String> list = new ArrayList<String>(Arrays.asList(val.split(",")));
			
			// 3. add new key
			list.add(key);
			
			// 4. save new JSON key list
			this.HeapTable.get(varORheap).get(idx).setValue(String.join(",", list));
		}
	}
	
	/** Brief Return JSON key list as ArrayList
	 * 
	 * @param varORheap name of variable or heap
	 */
	public ArrayList<String> getJSONKeyList(String varORheap){
		int idx;
		
		if( isEntryListNull(varORheap) || ((idx = indexOfJSONEntry(varORheap)) < 0) )
			return null;
		
		String val = this.HeapTable.get(varORheap).get(idx).getValue();
		return new ArrayList<String>(Arrays.asList(val.split(",")));
	}
	
	/** Brief Set JSON key list to a variable
	 * 
	 * @param varORheap name of variable or heap
	 * @param keyList a list of JSON keys
	 */
	public void setJSONKeyList(String varORheap, ArrayList<String> keyList){
		int idx;
		
		if(keyList == null)
			return;
		
		if( isEntryListNull(varORheap) || ((idx = indexOfJSONEntry(varORheap)) < 0) ){
			addJSONKeyList(varORheap, keyList);
			return;
		}
		
		this.HeapTable.get(varORheap).get(idx).setValue(String.join(",", keyList));
	}
	
	/** Brief Overwrite JSON key list of source to that of destination
	 * 
	 * @param dst destination variable/heap name
	 * @param src source variable/heap name
	 */
	public void overwriteJSONKeyList(String dst, String src){
		setJSONKeyList(dst, getJSONKeyList(src));
	}			
	
	/** Brief Add JSON key list. It adds new JSON entry if there is no JSON entry, or it appends keys in the list to the existing JSON entry if more than one of JSON entry exists
	 * 
	 * @param varORheap name of variable or heap
	 * @param keyList a list of JSON keys
	 */
	public void addJSONKeyList(String varORheap, ArrayList<String> keyList){
		if(keyList != null)
			for(String key: keyList)
				addJSONKey(varORheap, key);
	}
	
	/** Brief Adds new JSON entry
	 * 
	 * @param varORheap name of variable or heap
	 * @param keyList a list of JSON keys
	 */
	public void addNewJSONKeyList(String varORheap, ArrayList<String> keyList){
		if(keyList != null)
			addValueEntrytoHeapTable(varORheap, String.join(",", keyList), SOURCE_TYPE.JSON);
	}
	
	/** Brief Adds constant value on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param value constant value (e.g., "string", 1, etc)
	 */
	public void addConstantValue(String varORheap, String value){
		addValueEntrytoHeapTable(varORheap, value, SOURCE_TYPE.CONSTANT);
	}
	
	/** Brief Adds heap name on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param heapName heap name (class variable)
	 */
	public void addHeapValue(String varORheap, String heapName){
		addValueEntrytoHeapTable(varORheap, heapName, SOURCE_TYPE.HEAP);
	}
	
	/** Brief Adds heap name on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param thisName 
	 */
	public void addTypeEntry(String varORheap, String typeName){
		addValueEntrytoHeapTable(varORheap, typeName, SOURCE_TYPE.TYPE);
	}
	
	/** Brief Sets constant value on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param value constant value (e.g., "string", 1, etc)
	 */
	public void setConstantValue(String varORheap, String value){
		setValueEntrytoHeapTable(varORheap, value, SOURCE_TYPE.CONSTANT);
	}
	
	/** Brief Sets heap name on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param heapName heap name (class variable)
	 */
	public void setHeapValue(String varORheap, String heapName){
		setValueEntrytoHeapTable(varORheap, heapName, SOURCE_TYPE.HEAP);
	}
	
	/** Brief Sets heap name on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param thisName 
	 */
	public void setTypeEntry(String varORheap, String typeName){
		setValueEntrytoHeapTable(varORheap, typeName, SOURCE_TYPE.TYPE);
	}
	
	/** add the heapEntryList on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param heapEntryList a list of entries whose type will be JSON, constant, or heap
	 */
	public void AddHeapEntryListTo(String varORheap, ArrayList<HeapEntry> heapEntryList){
		if(varORheap == null || heapEntryList == null) 
			return;
		
		ArrayList<HeapEntry> list = this.HeapTable.get(varORheap);
		if(list == null) list = new ArrayList<HeapEntry>();
		
		for(HeapEntry he: heapEntryList)
			addEntry(list, he.getValue(),he.getSourceType());
		
		this.HeapTable.put(varORheap, list);
	}
	
	/** Overwrite the heapEntryList on variable/heap
	 * 
	 * @param varORheap name of variable or heap
	 * @param heapEntryList a list of entries whose type will be JSON, constant, or heap
	 */
	public void OverwriteHeapEntryListTo(String varORheap, ArrayList<HeapEntry> heapEntryList){
		if(varORheap == null || heapEntryList == null) 
			return;
		
		if(heapEntryList.size() == 0)
			return;
		
		ArrayList<HeapEntry> list = new ArrayList<HeapEntry>();
		
		list.addAll(heapEntryList);
		
		this.HeapTable.put(varORheap, list);
	}
	
	/** Overwrite the heapEntryList from src to dest
	 * 
	 * @param dest destination variable/heap name
	 * @param src source variable/heap name
	 */
	public void OverWriteHeapEntryListFromSrcToDest(String dest, String src){
		OverwriteHeapEntryListTo(dest, getHeapEntryList(src));
	}
}
