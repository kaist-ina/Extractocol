package extractocol.common.valueEntry.node;

import java.io.Serializable;
import java.util.ArrayList;

import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;

@SuppressWarnings("serial")
public class List extends ValueEntry implements Serializable {
	ArrayList<String> list = new ArrayList<String>();
	
	public List() {}
	public List(String s) { this.list.add(s); }
	
	public void setList(ArrayList<String> _list) { this.list = _list; }
	public ArrayList<String> getList() { return this.list; }
	
	public SOURCE_TYPE getSourceType(){ return SOURCE_TYPE.LIST; }
	
	/** Clear this entry instance **/
	public void Clear(){ 
		if(this.list != null) 
			this.list.clear(); 
		this.list = null; 
	}
	
	public String GenRegex() {
		if(this.list.size() == 0)
			return null;
		
		return this.list.toString().substring(1, list.toString().length() - 1); 
	}
	
	/** Cloning this instance */
	@SuppressWarnings("unchecked")
	public List Clone(){
		List newObject = new List();
		newObject.setList((ArrayList<String>)this.list.clone());
		return newObject;
	}
	
	public void addEntry(String s){ if(!this.list.contains(s)) this.list.add(s); }
	public void setEntry(String s){ 
		if(this.list != null) 
			this.list.clear(); 
		else
			this.list = new ArrayList<String>();
		
		this.addEntry(s); 
	}
	
	public void addEntry(ValueEntry ve){
		if(ve.getSourceType() != this.getSourceType())
			return;
		
		ArrayList<String> src_list = ((List)ve).getList();
		if(src_list == null)
			return;
		
		for(String e: src_list)
			this.addEntry(e);
	}
	
	public void setEntry(ValueEntry ve){
		if(ve.getSourceType() != this.getSourceType())
			return;
		
		this.list.clear();
		this.addEntry(ve);
	}
}
