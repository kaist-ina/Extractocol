package extractocol.common.valueEntry.node;

import java.io.Serializable;
import java.util.ArrayList;

import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntryTable;
import extractocol.tester.HeapHandling;

@SuppressWarnings("serial")
public class Constant extends ValueEntry implements Serializable {
	ArrayList<String> constantList = new ArrayList<String>();
	
	public Constant(String _constant){ if(_constant != null) this.constantList.add(_constant); }
	
	/** get type */
	public SOURCE_TYPE getSourceType() { return SOURCE_TYPE.CONSTANT;}
	
	/** get the list of constants
	 * 
	 * @return list of constants
	 */
	public ArrayList<String> getConstantList() { return this.constantList; }
	
	/** Cloning this instance
	 */
	public Constant Clone(){
		Constant newObject = new Constant(null);
		newObject.getConstantList().addAll(this.constantList);
		return newObject;
	}
	
	/** Clear this instance
	 */
	public void Clear(){
		if(this.constantList != null){
			this.constantList.clear();
			this.constantList = null;
		}
	}
	
	/** Generate Regex **/
	public String GenRegex(){
		if(this.constantList.size() == 0)
			return null;
		
		String res = "";
		
		for(String constant: this.constantList)
			res = res + ValueEntryTable.AppendParenthesis(constant) + "|";
			//res = res + constant + "|";
		
		if(res.equals(""))
			return "";
		else
			return res.substring(0, res.length() - 1);
	}
	
	/**********************************************************************/
	/**                             Add & Set                            **/
	/**********************************************************************/
	/** add 'value' into the list while removing the existing constants
	 * 
	 * @param value Constant value
	 */
	public void setEntry(String value){
		this.constantList = new ArrayList<String>();
		this.constantList.add(value);
	}
	
	/** add 'value' into the list (Except duplicated one)
	 * 
	 * @param value Constant value
	 */
	public void addEntry(String value){
		if(!this.constantList.contains(value))
			this.constantList.add(value);
	}
	
	/** add all constant values in ve while removing the existing constants
	 * 
	 * @param value Constant value
	 */
	public void setEntry(ValueEntry ve){
		if(ve.getSourceType() != this.getSourceType())
			return;
		
		this.constantList = ((Constant)ve).getConstantList();
	}
	
	/** add all constant values in ve (Except duplicated ones)
	 * 
	 * @param value Constant value
	 */
	public void addEntry(ValueEntry ve){
		if(ve.getSourceType() != this.getSourceType())
			return;
		
		for(String constant: ((Constant)ve).getConstantList())
			this.addEntry(constant);
	}
}
