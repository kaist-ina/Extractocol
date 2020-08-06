package extractocol.common.valueEntry.node;

import java.io.Serializable;

import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;

@SuppressWarnings("serial")
public class Class extends ValueEntry implements Serializable {
	String className = null;
	
	public Class(String _className) { this.className = _className; }
	
	/** Get type of this entry **/
	public SOURCE_TYPE getSourceType() { return SOURCE_TYPE.CLASS;}
	
	/** Set className of variable **/
	public void setClassName(String _className) { this.className = _className;}
	
	/** Get className **/
	public String getClassName() {return this.className;}
	
	/** Clone this instance
	 */
	public Class Clone(){
		Class newObject = new Class(null);
		newObject.setClassName(this.getClassName());
		return newObject;
	}
	
	/** Clear this instance
	 */
	public void Clear(){
		this.className = null;
	}
	
	/** Generate Regex **/
	public String GenRegex(){ return null; }
	
	/** Not used **/
	public void setEntry(String value){}
	public void addEntry(String value){}
	
	public void setEntry(ValueEntry ve){}
	public void addEntry(ValueEntry ve){}

}
