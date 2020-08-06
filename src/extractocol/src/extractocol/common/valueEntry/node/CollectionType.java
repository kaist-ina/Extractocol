package extractocol.common.valueEntry.node;

import java.io.Serializable;
import java.util.ArrayList;

import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;

@SuppressWarnings("serial")
public class CollectionType extends ValueEntry implements Serializable {
	ArrayList<String> typeList = new ArrayList<String>();
	
	public CollectionType(String _type) { if(_type != null) this.typeList.add(_type); }
	
	/** Get type of this entry **/
	public SOURCE_TYPE getSourceType() { return SOURCE_TYPE.COLLECTIONTYPE; }
	public ArrayList<String> getTypeList() { return this.typeList; }
	
	/** Clear this entry instance **/
	public void Clear() {
		if(this.typeList!= null) {
			this.typeList.clear();
			this.typeList = null;
		}
	}
	
	public String GenRegex() {return null;} 
	
	/** Clone this entry instance **/
	public ValueEntry Clone() {
		CollectionType newObject = new CollectionType(null);
		newObject.getTypeList().addAll(this.typeList);
		return newObject;
	}
	
	public void setEntry(String _type){
		this.Clear();
		this.typeList = new ArrayList<String>();
		this.typeList.add(_type);
	}
	
	public void addEntry(String _type){
		if(!this.typeList.contains(_type))
			this.typeList.add(_type);
	}
	
	public void setEntry(ValueEntry ve){
		if(ve.getSourceType() != this.getSourceType())
			return;
		
		this.Clear();
		this.typeList = ((CollectionType)ve).getTypeList();
	}
	
	public void addEntry(ValueEntry ve){
		if(ve.getSourceType() != this.getSourceType())
			return;
		
		for(String _type: ((CollectionType)ve).getTypeList())
			this.addEntry(_type);
	}
}
