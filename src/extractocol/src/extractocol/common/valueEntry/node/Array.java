package extractocol.common.valueEntry.node;

import java.io.Serializable;
import java.util.Arrays;

import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntryList;

@SuppressWarnings("serial")
public class Array extends ValueEntry implements Serializable {
	static int defaultSize = 30;
	ValueEntryList[] array;
	
	/** Make new array instance while allocating array as size
	 * 
	 * @param size Array size
	 */
	public Array(int size){
		if(size < 0) size = defaultSize;
		
		InitArray(size);
	}
	
	/** Make new array instance while allocating array as size
	 * 
	 * @param size Array size
	 */
	public Array(String size){
		int sz;
		
		try{
			sz = Integer.parseInt(size);
		}catch (Exception e){
			sz = defaultSize;
		}
		
		InitArray(sz);
	}
	
	/** Make new array instance while adding value(constant) at index
	 * 
	 * @param index Array index
	 * @param value constant value
	 */
	public Array(String index, String value){
		try{
			int idx = Integer.parseInt(index);
			InitArray(defaultSize);
			
			this.array[idx].addValueEntry(value, SOURCE_TYPE.CONSTANT, false);
		}catch (Exception e){
			return;
		}
	}
	
	/** Make new array instance while adding ValueEntryList at index
	 * 
	 * @param index Array index
	 * @param vel ValueEntryList
	 */
	public Array(String index, ValueEntryList vel){
		try{
			int idx = Integer.parseInt(index);
			InitArray(defaultSize);
			
			this.array[idx] = vel;
		}catch (Exception e){
			return;
		}
	}
	
	/** Allocate & initialize array
	 * 
	 * @param size Array size
	 */
	private void InitArray(int size){
		this.array = new ValueEntryList[size];
		for(int i = 0; i < size; i ++)
			this.array[i] = new ValueEntryList(null);
	}
	
	/** Get type of this entry **/
	public SOURCE_TYPE getSourceType() { return SOURCE_TYPE.ARRAY; }
	
	public ValueEntryList[] getArray() { return this.array; }
	public void setArray(ValueEntryList[] arr) { this.array = arr; }
	
	public int size() { return this.array.length; }
	
	/** Clear this entry instance **/
	public void Clear() {
		if(this.array != null){
			for(int i = 0; i < this.size(); i++){
				this.array[i].Clear();
				this.array[i] = null;
			}
			this.array = null;
		}
	}
	
	public String GenRegex() { return null; }
	
	/** Clone this entry instance **/
	public Array Clone() {
		Array newObject = new Array(this.array.length);
		ValueEntryList[] newList = new ValueEntryList[this.array.length];
		
		for(int i = 0; i < this.array.length; i++)
			newList[i] = this.array[i].Clone();
			
		newObject.setArray(newList);
		return newObject;
	}
	
	/** Add ValueEntryList at index (e.g., ValueEntryList is for $v2 where $v1[index] = $v2)
	 * 
	 * @param index Array index
	 * @param value ValueEntryList
	 */
	public void addEntry(String index, ValueEntryList value) {
		try{
			int idx = Integer.parseInt(index);
			if(idx >= this.array.length) return;
			
			this.array[idx] = value.Clone();
		} catch (Exception e){
			return;
		}
	}
	
	/** Add constant at index (e.g., $v1[index] = constant)
	 * 
	 * @param index Array index
	 * @param value constant value
	 */
	public void addEntry(String index, String value) {
		try{
			int idx = Integer.parseInt(index);
			if(idx >= this.array.length) return;
			
			if(this.array[idx] == null) this.array[idx] = new ValueEntryList(null);
			
			this.array[idx].addValueEntry(value, SOURCE_TYPE.CONSTANT, false);
		}catch (Exception e){
			return;
		}
	}
	
	/** Get ValueEntryList of index
	 * 
	 * @param index Array index
	 * @return ValueEntryList of the index
	 */
	public ValueEntryList getValue(String index) {
		try{
			int idx = Integer.parseInt(index);
			return getValue(idx);
		}catch (Exception e){
			return null;
		}
	}
	
	public ValueEntryList getValue(int idx) {
		if(idx >= this.array.length) return null;
		
		return this.array[idx];
	}
	
	/** Overwrite array of ve into this entry
	 * 
	 * @param ve 'Array' entry
	 */
	public void addEntry(ValueEntry ve) {
		if(ve.getSourceType() != this.getSourceType())
			return;
		
		Array arr = (Array) ve;
		if(arr.size() > this.size())
			ResizeArray(arr.size());
		
		for(int i = 0; i < arr.size(); i++){
			if(this.array[i] != null){
				this.array[i].addValueEntryList(arr.getArray()[i].Clone(), false);
			}
		}
	}
	
	private void ResizeArray(int newSize){
		int oldSize = this.size();
		this.array = Arrays.copyOf(this.array, newSize);
		
		for(int i = oldSize; i < newSize; i++)
			this.array[i] = new ValueEntryList(null);
	}
	
	public void setArrayEntry(String[] strArray){
		if(strArray == null)
			return;
		
		this.Clear();
		InitArray(strArray.length);
		for(int i = 0; i < strArray.length; i++)
			this.array[i].setValueEntry(strArray[i], SOURCE_TYPE.CONSTANT, false);
	}
	
}
