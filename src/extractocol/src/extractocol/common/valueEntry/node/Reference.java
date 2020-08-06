package extractocol.common.valueEntry.node;

import java.io.Serializable;
import java.util.ArrayList;

import extractocol.Constants;
import extractocol.common.MD5;
import extractocol.common.tools.Pair;
import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntryTable;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;

@SuppressWarnings("serial")
public class Reference extends ValueEntry implements Serializable {
	ArrayList<String> referList = new ArrayList<String>();
	ValueEntryTable referenceTable;
	
	public Reference(String MethodAndUnit, String type, ValueEntryTable refTable){
		this.referenceTable = refTable;
		
		if(MethodAndUnit!= null && type != null){
			String key = KeyGenerator(MethodAndUnit);
			this.referList.add(key);
			this.referenceTable.addTypeEntry(key, type, false);
			
			this.typeHandler(key, type);
		}
	}
	
	public void setReferenceTable(ValueEntryTable vet) { this.referenceTable = vet; }
	public ValueEntryTable getReferenceTable() { return this.referenceTable; }
	
	/** Get type **/
	public SOURCE_TYPE getSourceType() {return SOURCE_TYPE.REFERENCE; }
	
	/** Get list of MD5 hash keys used as key of the reference table **/
	public ArrayList<String> getReferList() { return this.referList; }
	
	/** Cloning this instance
	 */
	public Reference Clone(){
		Reference newObject = new Reference(null, null, this.referenceTable);
		newObject.getReferList().addAll(this.referList);
		return newObject;
	}

	/** Clear this instance
	 */
	public void Clear(){
		if(this.referList != null){
			this.referList.clear();
			this.referList = null;
		}
		
		this.referenceTable = null;
	}
	
	/** Generate MD5 hash key used for the key of the reference table
	 * 
	 * @param MethodAndUnit Unit + "####" + Method + "####" + Random number
	 * @return MD5 hash key
	 */
	private static String KeyGenerator(String MethodAndUnit){ return MD5.getMD5(MethodAndUnit); }
	
	/** Get types of variable (variable could have multiple types.)
	 * 
	 * @return List of types
	 */
	public ArrayList<String> getTypes(){
		ArrayList<String> types = new ArrayList<String>();

		for(String ref: this.referList){
			ValueEntryList velTemp = this.referenceTable.getValueEntryTable().get(ref);
			if(velTemp == null){
				System.out.println("error~!");
				continue;
			}
			ValueEntry typeEntry = this.referenceTable.getValueEntryTable().get(ref).getValueEntryOf(SOURCE_TYPE.TYPE);
			
			if(typeEntry != null)
				types.add(((Type)typeEntry).getType());
		}	
		
		return types; 
	}
	
	public ValueEntryList getValueEntryListFromReferenceTable(){
		ValueEntryList listFromReferTable = new ValueEntryList(null);
		
		for(String ref: this.referList){
			ValueEntryList vel = this.referenceTable.getValueEntryList(ref);
			
			if(vel != null)
				listFromReferTable.addValueEntryList(vel, false);			
		}
			
		
		return listFromReferTable;
	}
	
	/**********************************************************************/
	/**                             Add & Set                            **/
	/**********************************************************************/
	/** Add MD5 hash key after removing the existing list
	 * 
	 * @param MD5Hash MD5 hash key 
	 */
	public void setEntry(String MD5Hash){
		this.referList = new ArrayList<String>();
		this.referList.add(MD5Hash);
	}
	
	/** Add MD5 hash key (Except duplicated one)
	 * 
	 * @param MD5Hash MD5 hash key 
	 */
	public void addEntry(String MD5Hash){
		if(!this.referList.contains(MD5Hash))
			this.referList.add(MD5Hash);
	}
	
	/** Add all of MD5 hash keys in 've' after removing the existing list
	 * 
	 * @param ve Reference entry 
	 */
	public void setEntry(ValueEntry ve){
		if(ve.getSourceType() != this.getSourceType())
			return;
		
		this.referList = ((Reference)ve).getReferList();
	}
	
	/** Add all of MD5 hash keys in 've' (Except duplicated ones)
	 * 
	 * @param ve Reference entry 
	 */
	public void addEntry(ValueEntry ve){
		if(ve.getSourceType() != this.getSourceType())
			return;
		
		for(String referValue: ((Reference)ve).getReferList())
			this.addEntry(referValue);
	}
	
	/** Generate Regex **/
	public String GenRegex(){
		if(this.referList.size() == 0)
			return null;
		
		String res ="";
		
		for(String ref: this.referList){
			String val = this.referenceTable.getValueEntryTable().get(ref).GenRegex();
			
			if(val == null)
				continue;
			
			res = res + ValueEntryTable.AppendParenthesis(val) + "|";
		}
		
		if(res.equals(""))
			return res;
		else
			return res.substring(0, res.length() - 1);
	}
	/**********************************************************************/
	/**                               Tools                              **/
	/**********************************************************************/
	private void typeHandler(String key, String type) {
		if(type.equals("java.util.ArrayList"))
			this.referenceTable.addValueEntry(key, new List(), false);
		else if(type.equals("java.lang.StringBuilder"))
			this.referenceTable.addValueEntry(key, new StrBuilder(), false);
	}
	
	/**********************************************************************/
	/**                    APIs for the reference table                  **/
	/**********************************************************************/
	public void setValueEntryList(ValueEntryList list){
		for(String ref: this.referList)
			this.referenceTable.getValueEntryTable().get(ref).setValueEntryList(list, false); 
	}
	
	public void addValueEntryList(ValueEntryList list){ 
		for(String ref: this.referList)
			this.referenceTable.getValueEntryTable().get(ref).addValueEntryList(list, false); 
	}
	
	public void setValueEntry(String value, SOURCE_TYPE type){ 
		for(String ref: this.referList)
			this.referenceTable.getValueEntryTable().get(ref).setValueEntry(value, type, false);
	}
	public void addValueEntry(String value, SOURCE_TYPE type){ 
		for(String ref: this.referList)
			this.referenceTable.getValueEntryTable().get(ref).addValueEntry(value, type, false);
	}
	
	public void setValueEntry(String key, String value, SOURCE_TYPE type){ 
		for(String ref: this.referList)
			this.referenceTable.getValueEntryTable().get(ref).setValueEntry(key, value, type, false);
	}
	public void addValueEntry(String key, String value, SOURCE_TYPE type){ 
		for(String ref: this.referList)
			this.referenceTable.getValueEntryTable().get(ref).addValueEntry(key, value, type, false);
	}
	
	public void addValueEntry(String idx, ValueEntryList value, SOURCE_TYPE type){ 
		for(String ref: this.referList)
			this.referenceTable.getValueEntryTable().get(ref).addValueEntry(idx, value, type, false);
	}
	
	public void setValueEntry(String idx, ValueEntryList value, SOURCE_TYPE type){ 
		for(String ref: this.referList)
			this.referenceTable.getValueEntryTable().get(ref).setValueEntry(idx, value, type, false);
	}
	
	public void addValueEntry(ValueEntry ve){
		for(String ref: this.referList)
			this.referenceTable.getValueEntryTable().get(ref).addValueEntry(ve, false);
	}
	
	public void setValueEntry(ValueEntry ve){
		for(String ref: this.referList)
			this.referenceTable.getValueEntryTable().get(ref).setValueEntry(ve, false);
	}
	
	public boolean doesContainTypeOf(SOURCE_TYPE type){
		for(String ref: this.referList){
			if(this.referenceTable.getValueEntryTable().get(ref).getValueEntryOf(type) != null)
				return true;
		}
		return false;
	}
	
	public ArrayList<ArrayList<Pair>> getMapLists(){
		ArrayList<ArrayList<Pair>> mapListsFinal = new ArrayList<ArrayList<Pair>>();
		ArrayList<ArrayList<Pair>> mapLists;
		
		for(String ref: this.referList){
			ValueEntry mapEntry = this.referenceTable.getValueEntryTable().get(ref).getValueEntryOf(SOURCE_TYPE.MAP);
			if(mapEntry != null)
			{
				mapLists = ((Map)mapEntry).getMapLists();
				if(mapLists != null)
					mapListsFinal.addAll(mapLists);
			}
		}
		
		return mapListsFinal;
	}
	
	public java.util.List<String> getList(){
		java.util.List<String> listFinal = new ArrayList<String>();
		java.util.List<String> tmp;
		
		for(String ref: this.referList){
			ValueEntry ListEntry = this.referenceTable.getValueEntryTable().get(ref).getValueEntryOf(SOURCE_TYPE.LIST);
			if(ListEntry != null)
			{
				tmp = ((List)ListEntry).getList();
				if(tmp != null)
					listFinal.addAll(tmp);
			}
		}
		
		return listFinal;
	}
}
