package extractocol.common.valueEntry.node;

import java.io.Serializable;
import java.util.ArrayList;

import extractocol.common.valueEntry.ValueEntry;

@SuppressWarnings("serial")
public class JSONKey extends ValueEntry implements Serializable {
	ArrayList<ArrayList<String>> JSONKeyList = new ArrayList<ArrayList<String>>();
	
	public JSONKey(String JSONKey) {
		if(JSONKey != null){
			ArrayList<String> newList = new ArrayList<String>();
			newList.add(JSONKey);
			this.JSONKeyList.add(newList);	
		}	
	}
	
	/** Get the list of JSON key lists
	 * @return List of JSON key lists
	 */
	public ArrayList<ArrayList<String>> getJSONKeyLists(){ return this.JSONKeyList;	}
	
	/** Get type */
	public SOURCE_TYPE getSourceType() { return SOURCE_TYPE.JSONKEY;}
	
	/** Clear this instance
	 */
	public void Clear(){
		if(this.JSONKeyList != null){
			for(int i = 0; i < this.JSONKeyList.size(); i++){
				ArrayList<String> list = this.JSONKeyList.get(i);
				list.clear();
				list = null;
			}
			
			this.JSONKeyList.clear();
			this.JSONKeyList = null;			
		}
	}
	
	/** Cloning this instance
	 */
	@SuppressWarnings("unchecked")
	public JSONKey Clone(){
		JSONKey newObject = new JSONKey(null);
		for(ArrayList<String> l : this.JSONKeyList)
			newObject.getJSONKeyLists().add((ArrayList<String>)l.clone());
		return newObject;
	}
	
	/** Generate Regex **/
	public String GenRegex(){ return null; }

	
	/**********************************************************************/
	/**                             Add & Set                            **/
	/**********************************************************************/
	/** Add JSON key after removing the existing list
	 * 
	 * @param JSONkey JSON key
	 */
	public void setEntry(String JSONkey){
		this.JSONKeyList = new ArrayList<ArrayList<String>>();
		this.addEntry(JSONkey);
	}
	
	/** Add JSON key in all of JSON lists
	 * 
	 * @param JSONkey JSON key
	 */
	public void addEntry(String JSONkey){
		if(this.JSONKeyList.size() == 0){
			ArrayList<String> newJSONKeyList = new ArrayList<String>();
			newJSONKeyList.add(JSONkey);
			this.JSONKeyList.add(newJSONKeyList);
		}else{
			for(ArrayList<String> JSONKeyList : this.JSONKeyList){
				JSONKeyList.add(JSONkey);
			}
		}
	}

	/** Add all of JSON key lists of 've' after removing the existing list
	 * 
	 * @param JSONkey JSON key
	 */
	public void setEntry(ValueEntry ve){
		if(ve.getSourceType() != this.getSourceType())
			return;
		
		this.JSONKeyList = new ArrayList<ArrayList<String>>();
		this.addEntry(ve);
	}

	/** Add all of JSON key lists of 've' (Except duplicated ones)
	 * 
	 * @param JSONkey JSON key
	 */
	public void addEntry(ValueEntry ve){
		if(ve.getSourceType() != this.getSourceType())
			return;
		
		for(ArrayList<String> JSONKeyList: ((JSONKey)ve).getJSONKeyLists())
			if(!doesContain(JSONKeyList, this.JSONKeyList))
				this.JSONKeyList.add(JSONKeyList);
	}
	
	/** Add all of JSON keys in keyList at the front of the existing lists
	 * 
	 * @param keyList JSON key list being inserted
	 */
	public void addKeyAtFront(java.util.List<String> keyList) {
		for(ArrayList<String> innerList : this.JSONKeyList)
			innerList.addAll(0, keyList);
	}
	
	public void addNewJSONKeyList(ArrayList<String> keyList) {
		this.JSONKeyList.add(keyList);
	}
	
	/** Check whether lists contains list
	 * 
	 * @param list JSON key list
	 * @param lists list of JSON key lists
	 * @return True if lists contains list
	 */
	private boolean doesContain(ArrayList<String> list, ArrayList<ArrayList<String>> lists){
		for(ArrayList<String> target : lists)
			if(list.containsAll(target))
				return true;
		
		return false;
	}
	
	/**********************************************************************/
	/**                         APIs for output                          **/
	/**********************************************************************/
	/** Output the JSONKey list as string array
	 * 
	 * @return String array of the JSONKey list
	 */
	public ArrayList<String> getJSONKeys(){
		ArrayList<String> output = new ArrayList<String>();
		for(ArrayList<String> entry: this.JSONKeyList)
			output.addAll(entry); // Is it correct? need to debug (BK)
			//output.add(entry.toString().replaceAll("[\\[\\] ]", ""));
		
		return output;
	}
	
	/** Get JSON Key list as String
	 *  (called at heap handler when generating signatures) 
	 * @return JSON Key List (String)
	 */
	public ArrayList<String> getJSONKeyString(){
		ArrayList<String> output = new ArrayList<String>();
		for(ArrayList<String> entry: this.JSONKeyList)
			output.add(entry.toString().replaceAll("[\\[\\] ]", ""));
		
		return output;
	}
}
