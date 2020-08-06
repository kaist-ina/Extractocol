package extractocol.common.valueEntry;

import java.io.Serializable;
import java.util.ArrayList;

import extractocol.common.tools.Pair;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;
import extractocol.common.valueEntry.node.*;
import extractocol.common.valueEntry.node.Class;

@SuppressWarnings("serial")
public class ValueEntryList implements Serializable {
	private ArrayList<ValueEntry> valueEntryList = new ArrayList<ValueEntry>();
	private String varType;
	
	public ValueEntryList() {
		this.varType = null;
	}
	
	public ValueEntryList(ValueEntry ve){
		if(ve != null)
			this.valueEntryList.add(ve);
	}
	
	public ArrayList<ValueEntry> getValueEntryList() { return this.valueEntryList; }
	public ValueEntryList getValueEntryListDeep(){
		ValueEntryList newList = new ValueEntryList(null);
		
		for(ValueEntry ve: this.valueEntryList){
			if(ve.getSourceType() != SOURCE_TYPE.REFERENCE)
				newList.addValueEntry(ve, false);
			else{
				ValueEntryList vel = ((Reference)ve).getValueEntryListFromReferenceTable();
				newList.addValueEntryList(vel, false);
			}
		}
		
		return newList;
	}
	
	public void setValueEntryList(ArrayList<ValueEntry> list) { this.valueEntryList = list; }
	
	/** Clear this list instance **/
	public void Clear(){
		if(this.valueEntryList != null){
			for(int i = 0; i < this.valueEntryList.size(); i++){
				ValueEntry ve = this.valueEntryList.get(i);
				ve.Clear();
				ve = null;
			}
			
			this.valueEntryList.clear();
			this.valueEntryList = null;
		}
	}
	
	/** Clone this list instance **/
	public ValueEntryList Clone(){
		ValueEntryList newObject = new ValueEntryList(null);
		ArrayList<ValueEntry> newList = new ArrayList<ValueEntry>();
		
		//if(this.valueEntryList!= null)
			for(ValueEntry ve : this.valueEntryList)
				newList.add(ve.Clone());
		
		newObject.setValueEntryList(newList);
		newObject.setVarType(this.getVarType());
		return newObject;
	}
	
	/** Get types of variable
	 * 
	 * @return List of types
	 */
	public ArrayList<String> getTypes(){
		ArrayList<String> ret = new ArrayList<String>();
		
		if(doesContainReference())
			ret.addAll(((Reference)this.getValueEntryOf(SOURCE_TYPE.REFERENCE)).getTypes());
		
		for(ValueEntry ve: this.valueEntryList) {
			if(ve.getSourceType() != SOURCE_TYPE.TYPE)
				continue;
			
			ret.add(((Type) ve).getType());
		}
		
		return ret;
	}
	
	public java.util.List<String> getCollectionTypeList(){
		ValueEntry ve = this.getValueEntryOf(SOURCE_TYPE.COLLECTIONTYPE);
		if(ve == null) return null;
		
		return ((CollectionType)ve).getTypeList();
	}
	
	public String getClassName() {
		ValueEntry ve = this.getValueEntryOf(SOURCE_TYPE.CLASS);
		if(ve == null) return null;
		
		return ((Class)ve).getClassName();
	}
	
	/** Set reference table of reference entry
	 * 
	 * @param refTable reference table
	 */
	public void setReferenceTable(ValueEntryTable refTable){
		Reference refVE = (Reference) this.getValueEntryOf(SOURCE_TYPE.REFERENCE);
		if(refVE != null)
			refVE.setReferenceTable(refTable);
		
		Array arrVE = (Array) this.getValueEntryOf(SOURCE_TYPE.ARRAY);
		if(arrVE != null) {
			for(int i = 0; i < arrVE.size(); i++)
				arrVE.getValue(i).setReferenceTable(refTable);
		}
		
	}
	
	public int size() { return this.valueEntryList.size(); }
	
	public String getVarType() { return varType; }
	public void setVarType(String type) { this.varType = type; }
	
	
	/*****************************************************************************/
	/**                               Add & Set                                 **/
	/*****************************************************************************/
	/** (1/7) ValueEntryList **/
	public void addValueEntryList(ValueEntryList list, boolean toReference){
		if(list == null)
			return;

		if(PerformToReference(toReference))
			this.getRefernceEntry().addValueEntryList(list);
		else{
			if(list.getValueEntryList() != null)
				for (ValueEntry ve : list.getValueEntryList())
					this.addValueEntry(ve, false);

			this.setVarType(list.getVarType());
		}
	}
	
	@SuppressWarnings("unchecked")
	public void setValueEntryList(ValueEntryList list, boolean toReference){
		if(list == null)
			return;
		
		if(PerformToReference(toReference))
			this.getRefernceEntry().setValueEntryList(list);
		else{
			this.valueEntryList = new ArrayList<ValueEntry>();
			
			if(list.getValueEntryList() != null)
				for (ValueEntry ve : list.getValueEntryList())
					this.valueEntryList.add(ve.Clone());
			
			this.setVarType(list.getVarType());
		}
			 
	}
	
	
	/** (2/7) ValueEntry(ValueEntry ve) **/
	public void addValueEntry(ValueEntry ve, boolean toReference){
		if(ve == null)
			return;
		
		if(PerformToReference(toReference)){
			this.getRefernceEntry().addValueEntry(ve);
		}else{
			ValueEntry VE = this.getValueEntryOf(ve.getSourceType());
			
			if(VE == null)
			{
				this.valueEntryList.add(ve);
			}
			else
			{
				VE.addEntry(ve);
				
				/*switch(ve.getSourceType()){
				case JSONKEY: ((JSONKey)VE).addEntry(ve); break; 
				case CONSTANT: ((Constant)VE).addEntry(ve); break; 
				case HEAP: ((Heap)VE).addEntry(ve); break;
				case TYPE: ((Type)VE).addEntry(ve); break;
				case REFERENCE:  ((Reference)VE).addEntry(ve); break;
				case MAP:  ((Map)VE).addEntry(ve); break;
				case LIST: ((List)VE).addEntry(ve); break;
				case ARRAY: ((Array)VE).addEntry(ve); break;
				case STRBUILDER: ((StrBuilder)VE).addEntry(ve); break;
				default: break;
				}*/
			}
		}
		
	}
	
	public void setValueEntry(ValueEntry ve, boolean toReference){
		if(ve == null)
			return;
		
		if(PerformToReference(toReference)){
			this.getRefernceEntry().setValueEntry(ve);
		}else{
			this.makeEmptyOf();
			this.addValueEntry(ve, false);
		}
		
	}
	
	
	/** (3/7) ValueEntry(String value, SOURCE_TYPE type) **/
	public void addValueEntry(String value, SOURCE_TYPE type, boolean toReference){
		if(value == null || type == null)
			return;
		
		if(PerformToReference(toReference)){
			this.getRefernceEntry().addValueEntry(value, type);
		}else{
			ValueEntry VE = this.getValueEntryOf(type);
			
			if(VE != null)
			{
				switch(type){
				case JSONKEY: ((JSONKey)VE).addEntry(value); break; 
				case CONSTANT: ((Constant)VE).addEntry(value); break; 
				case HEAP: ((Heap)VE).addEntry(value); break;
				case TYPE: ((Type)VE).addEntry(value); break;
				case LIST: ((List)VE).addEntry(value); break;
				case STRBUILDER: ((StrBuilder)VE).addEntry(value); break;
				case COLLECTIONTYPE: ((CollectionType)VE).addEntry(value); break;
				case CLASS: ((Class)VE).addEntry(value); break;
				default: break;
				}
			}
			else 
			{
				switch(type){
				case JSONKEY: this.valueEntryList.add(new JSONKey(value)); break;
				case CONSTANT: this.valueEntryList.add(new Constant(value)); break;
				case HEAP: this.valueEntryList.add(new Heap(value)); break; 
				case TYPE: this.valueEntryList.add(new Type(value)); break;
				case LIST: this.valueEntryList.add(new List(value)); break;
				case ARRAY: this.valueEntryList.add(new Array(value/*index*/)); break;
				case STRBUILDER: this.valueEntryList.add(new StrBuilder(value)); break;
				case COLLECTIONTYPE: this.valueEntryList.add(new CollectionType(value)); break;
				case CLASS: this.valueEntryList.add(new Class(value)); break;
				default: break;
				}
			}
		}
	}	
	
	public void setValueEntry(String value, SOURCE_TYPE type, boolean toReference){
		if(value == null || type == null)
			return;
		
		if(PerformToReference(toReference)){
			this.getRefernceEntry().setValueEntry(value, type);
		}else{
			this.makeEmptyOf();
			this.addValueEntry(value, type, false);
		}	
	}
	
	
	
	/** (4/7) ValueEntry(String key, String value, SOURCE_TYPE type) **/
	public void addValueEntry(String key, String value, SOURCE_TYPE type, boolean toReference){
	
		if(key == null || value == null || type == null)
			return;
		
		if(PerformToReference(toReference)){
			this.getRefernceEntry().addValueEntry(key, value, type);
		}else{
			ValueEntry VE = this.getValueEntryOf(type);
			
			if(VE != null)
			{
				switch(type){
				case MAP:  ((Map)VE).addEntry(key, value); break;
				case ARRAY: ((Array)VE).addEntry(key, value); break;
				default: break;
				}
			}
			else{
				switch(type){
				case MAP: this.valueEntryList.add(new Map(key, value)); break;
				case ARRAY: this.valueEntryList.add(new Array(key, value)); break;
				default: break;
				}
			}
		}
	}
	
	public void setValueEntry(String key, String value, SOURCE_TYPE type, boolean toReference){
		if(key == null || value == null || type == null)
			return;
		
		if(PerformToReference(toReference)){
			this.getRefernceEntry().setValueEntry(key, value, type);
		}else{
			this.makeEmptyOf();
			this.addValueEntry(key, value, type, false);
		}
	}	

	/** (5/7) ValueEntry(String key, String value, SOURCE_TYPE type, ValueEntryTable refTable) - Only for reference entry**/
	public void addValueEntry(String key, String value, SOURCE_TYPE type, ValueEntryTable refTable, boolean toReference){
	
		if(key == null || value == null || type == null)
			return;
		
		if(PerformToReference(toReference)){
			this.getRefernceEntry().addValueEntry(key, value, type);
		}else{
			ValueEntry VE = this.getValueEntryOf(type);
			
			if(VE != null)
			{
				switch(type){
				case REFERENCE:  ((Reference)VE).addEntry(value/*type*/); break;
				default: break;
				}
			}
			else{
				switch(type){
				case REFERENCE: this.valueEntryList.add(new Reference(key/*MethodAndUnit*/, value/*type*/, refTable/*reference Table*/)); break; 
				default: break;
				}
			}
		}
	}
	
	public void setValueEntry(String key, String value, SOURCE_TYPE type, ValueEntryTable refTable, boolean toReference){
		if(key == null || value == null || type == null)
			return;
		
		if(PerformToReference(toReference)){
			this.getRefernceEntry().setValueEntry(key, value, type);
		}else{
			this.makeEmptyOf();
			this.addValueEntry(key, value, type, refTable, false);
		}
	}
	
	/** (6/7) ValueEntry(String idx, ValueEntryList value, SOURCE_TYPE type) **/
	public void addValueEntry(String idx, ValueEntryList value, SOURCE_TYPE type, boolean toReference){
	
		if(idx == null || value == null || type == null)
			return;
		
		if(PerformToReference(toReference)){
			this.getRefernceEntry().addValueEntry(idx, value, type);
		}else{
			ValueEntry VE = this.getValueEntryOf(type);
			
			if(VE != null)
			{
				switch(type){
				case ARRAY:  ((Array)VE).addEntry(idx, value); break;
				default: break;
				}
			}
			else{
				switch(type){
				case ARRAY: this.valueEntryList.add(new Array(idx, value)); break; 
				default: break;
				}
			}
		}
	}
	/** (7/7) ValueEntry(String idx, ValueEntryList value, SOURCE_TYPE type) for Volley node **/
	public void addValueEntry(String url, int method, ValueEntryList value, SOURCE_TYPE type, boolean toReference){

	}
	
	public void setValueEntry(String idx, ValueEntryList value, SOURCE_TYPE type, boolean toReference){
		if(idx == null || value == null || type == null)
			return;
		
		if(PerformToReference(toReference)){
			this.getRefernceEntry().setValueEntry(idx, value, type);
		}else{
			this.makeEmptyOf();
			this.addValueEntry(idx, value, type, false);
		}
	}
	
	/*****************************************************************************/
	/**                           APIs for outputs                              **/
	/*****************************************************************************/
	public java.util.List<String> getList(){
		java.util.List<String> listFinal = new ArrayList<String>();
		java.util.List<String> tmp;
		
		ValueEntry listEntry = getValueEntryOf(SOURCE_TYPE.LIST);
		if(listEntry != null) {
			tmp = ((List)listEntry).getList();
			if(tmp != null)
				listFinal.addAll(tmp);
		}
		
		ValueEntry refEntry = getValueEntryOf(SOURCE_TYPE.REFERENCE);
		if(refEntry != null) {
			tmp = ((Reference)refEntry).getList();
			if(tmp != null)
				listFinal.addAll(tmp);
		}
		
		return listFinal;
	}
	
	public ArrayList<Pair> getMap(){
		ArrayList<Pair> mapListsFinal = new ArrayList<Pair>();
		ArrayList<ArrayList<Pair>> mapLists;
		
		ValueEntry mapEntry = getValueEntryOf(SOURCE_TYPE.MAP);
		
		if(mapEntry != null){
			mapLists = ((Map)mapEntry).getMapLists();
			if(mapLists != null){
				for(ArrayList<Pair> mapFromEntry: mapLists)
					mapListsFinal.addAll(mapFromEntry);	
			}
		}
			
			
		ValueEntry refEntry = getValueEntryOf(SOURCE_TYPE.REFERENCE);
		if(refEntry != null){
			mapLists = ((Reference)refEntry).getMapLists();
			if(mapLists != null){
				for(ArrayList<Pair> mapFromEntry: mapLists)
					mapListsFinal.addAll(mapFromEntry);	
			}
		}
		
		return mapListsFinal;
	}
	
	/** Generate Regex **/
	public String GenRegex(){
		String res = "";
		
		for(ValueEntry ve : this.valueEntryList){
			String veRes = ve.GenRegex();
			if(veRes == null)
				continue; 
			
			res = res + ValueEntryTable.AppendParenthesis(veRes) + "|";
			//res = res + veRes + "|";
		}
		
		if(res.equals(""))
			return ".*";
		else
			return res.substring(0, res.length() - 1);
	}
	
	public ArrayList<String> getJSONKeyList(){
		ValueEntry entry = getValueEntryOf(SOURCE_TYPE.JSONKEY);
		
		if(entry == null)
			return new ArrayList<String>();
		else {
			return ((JSONKey)entry).getJSONKeys();
		}
	}
	
	/**********************************************************************/
	/**                               Tools                              **/
	/**********************************************************************/
	private boolean PerformToReference(boolean toReference){ return toReference && this.doesContainReference(); }
	private Reference getRefernceEntry() { return (Reference)this.getValueEntryOf(SOURCE_TYPE.REFERENCE); }
	private boolean doesContainReference(){ return getRefernceEntry() != null; }
	
	public void makeEmptyOf(){
		ArrayList<ValueEntry> newList = new ArrayList<ValueEntry>();
		
		for(ValueEntry ve: this.valueEntryList)
			if(ve.getSourceType() == SOURCE_TYPE.TYPE)
				newList.add(ve);
		
		this.setValueEntryList(newList);
	}
	
	public boolean doesContainTypeOf(SOURCE_TYPE type){
		Reference r = this.getRefernceEntry();
		
		if(r != null){
			if(r.doesContainTypeOf(type))
				return true;
		}else{
			if(this.getValueEntryOf(type) != null)
				return true;
		}
		
		return false;
	}
	
	public ValueEntry getValueEntryOf(SOURCE_TYPE type){
		for(ValueEntry ve: this.valueEntryList)
			if(ve.getSourceType() == type)
				return ve;
		
		return null;
	}
}
