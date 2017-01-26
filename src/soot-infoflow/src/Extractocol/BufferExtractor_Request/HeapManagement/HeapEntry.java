package Extractocol.BufferExtractor_Request.HeapManagement;

/* Brief  
 * By BK
 */
public class HeapEntry {
	public static enum SOURCE_TYPE {JSON, CONSTANT, HEAP, TYPE, NONE};
	
	private SOURCE_TYPE stype;
	
	// 'value' will include:
	//  1. JSON key hierarchy (e.g., "data,products,commerce_info,id")
	//  2. constant value
	//  3. heap name (e.g., <com.contextLogic.data: java.Lang.String id>
	//  4. this name (e.g., $r0 will has 'com.class.name' if stmt is "$r0 := @this: com.class.name")
	private String value;
	
	public HeapEntry(){
		this.stype = SOURCE_TYPE.NONE; 
		this.value = null;
	}
	
	public HeapEntry(SOURCE_TYPE st){
		this.stype = st; 
		this.value = null;
	}
	
	public HeapEntry(SOURCE_TYPE st, String v){
		this.stype = st; 
		this.value = v;
	}
	
	public void setValue(String v){
		this.value = v;
	}
	
	public String getValue(){
		return this.value;
	}
	
	public void setSourceType(SOURCE_TYPE s){
		this.stype = s;
	}
	
	public SOURCE_TYPE getSourceType(){
		return this.stype;
	}
	
	public boolean isJSON(){
		return (this.stype == SOURCE_TYPE.JSON);
	}
	
	public boolean isConstant(){
		return (this.stype == SOURCE_TYPE.CONSTANT);
	}
	
	public boolean isHeap(){
		return (this.stype == SOURCE_TYPE.HEAP);
	}
}
