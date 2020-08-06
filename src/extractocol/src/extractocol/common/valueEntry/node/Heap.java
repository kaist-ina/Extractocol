package extractocol.common.valueEntry.node;

import java.io.Serializable;
import java.util.ArrayList;

import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntryTable;

@SuppressWarnings("serial")
public class Heap extends ValueEntry implements Serializable {
	ArrayList<String> heapList = new ArrayList<String>();
	
	public Heap(String _heap) { if(_heap != null) this.heapList.add(_heap); }
	
	/** get the list of Heap names
	 * 
	 * @return list of Heap names
	 */
	public ArrayList<String> getHeapList() { return this.heapList; }
	
	/** get type
	 */
	public SOURCE_TYPE getSourceType() { return SOURCE_TYPE.HEAP;}
	
	/** Cloning this instance */
	public Heap Clone(){
		Heap newObject = new Heap(null);
		try {
			newObject.getHeapList().addAll(this.heapList);
		}catch (Exception e) {
			System.err.println("Heap cloing error!! (heapList is null.)");
			e.printStackTrace();
		}
			
		return newObject;
	}
	
	/** Clear this instance */
	public void Clear(){
		if(this.heapList != null){
			this.heapList.clear();
			this.heapList = null;
		}
	}
	
	/** Generate Regex **/
	public String GenRegex(){
		if(this.heapList.size() == 0)
			return null;
		
		String res = "";
		
		for(String heap: this.heapList)
			res = res + heap + "|";
		
		if(res.equals(""))
			return "";
		else
			return res.substring(0, res.length() - 1);
	}
	
	/**********************************************************************/
	/**                             Add & Set                            **/
	/**********************************************************************/
	/** Add Heap name after removing the existing list
	 * 
	 * @param value Heap name
	 */
	public void setEntry(String heap){
		this.heapList = new ArrayList<String>();
		this.heapList.add(heap);
	}
	
	/** Add Heap name (Except duplicated one)
	 * 
	 * @param value Heap name
	 */
	public void addEntry(String heap){
		if(!this.heapList.contains(heap))
			this.heapList.add(heap);
	}
	
	/** Add All Heap names of 've' after removing the existing list
	 * 
	 * @param value Heap name
	 */
	public void setEntry(ValueEntry ve){
		if(ve.getSourceType() != this.getSourceType())
			return;
		
		this.heapList = ((Heap)ve).getHeapList();
	}
	
	/** Add All Heap names of 've' (Except duplicated ones)
	 * 
	 * @param value Heap name
	 */
	public void addEntry(ValueEntry ve){
		if(ve.getSourceType() != this.getSourceType())
			return;
		
		for(String heap: ((Heap)ve).getHeapList())
			this.addEntry(heap);
	}
}
