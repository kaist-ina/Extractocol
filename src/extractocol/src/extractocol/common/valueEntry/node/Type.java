package extractocol.common.valueEntry.node;

import java.io.Serializable;

import extractocol.common.valueEntry.ValueEntry;

@SuppressWarnings("serial")
public class Type extends ValueEntry implements Serializable {
	String type = null;
	
	public Type(String _type) { this.type = _type; }
	
	/** Get type of this entry **/
	public SOURCE_TYPE getSourceType() { return SOURCE_TYPE.TYPE;}
	
	/** Set type of variable **/
	public void setType(String _type) { this.type = _type;}
	
	/** Get type of variable **/
	public String getType() {return this.type;}
	
	/** Clone this instance
	 */
	public Type Clone(){
		Type newObject = new Type(null);
		newObject.setType(this.getType());
		return newObject;
	}
	
	/** Clear this instance
	 */
	public void Clear(){
		this.type = null;
	}
	
	/** Generate Regex **/
	public String GenRegex(){ return null; }
	
	/** Not used **/
	public void setEntry(String value){}
	public void addEntry(String value){}
	
	public void setEntry(ValueEntry ve){}
	public void addEntry(ValueEntry ve){}
}
