package extractocol.common.valueEntry.node;

import java.io.Serializable;

import extractocol.common.valueEntry.ValueEntry;

@SuppressWarnings("serial")
public class Null extends ValueEntry implements Serializable {
	/** Get type of this entry **/
	public SOURCE_TYPE getSourceType() { return SOURCE_TYPE.NULL; }
	
	/** Clear this entry instance **/
	public void Clear() {}
	
	public String GenRegex() { return null; }
	
	/** Clone this entry instance **/
	public ValueEntry Clone() { return new Null(); }
	
	public void addEntry(ValueEntry ve) {}
}
