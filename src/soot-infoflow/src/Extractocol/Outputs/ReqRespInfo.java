package Extractocol.Outputs;

import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.HeapManagement.HeapEntry;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapTable;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapEntry.SOURCE_TYPE;

public class ReqRespInfo {
	public RequestInfoEntry reqie;
	public ResponseInfoEntry respie;
	
	// For tracking heap variables
	public HeapTable heapTable;
		
	/***********************************************************************************/
	/**********                          Basic APIs                           **********/
	/***********************************************************************************/
	public ReqRespInfo(){
		this.reqie = new RequestInfoEntry();
		this.respie = new ResponseInfoEntry();
		this.heapTable = new HeapTable();
	}
	
	public ReqRespInfo(RequestInfoEntry ReqIE, ResponseInfoEntry RespIE){
		this.reqie = ReqIE;
		this.respie = RespIE;
		this.heapTable = new HeapTable();
	}
	
	public RequestInfoEntry getRequestInfoEntry(){
		return this.reqie;
	}
	
	public ResponseInfoEntry getResponseInfoEntry(){
		return this.respie;
	}
	
	
	/***********************************************************************************/
	/**********                      APIs for Heap Table                      **********/
	/***********************************************************************************/
	public HeapTable getHeapTable(){
		return this.heapTable;
	}
	
	public void setHeapTable(HeapTable ht){
		this.heapTable = ht;
	}
	
	public void addJSONKey(String heap, String key){
		this.heapTable.addJSONKey(heap, key);
	}
	
	public void addJSONKeyList(String heap, ArrayList<String> keyList){
		this.heapTable.addJSONKeyList(heap, keyList);
	}
	
	public void addHeapEntry(String heap, String heapname){
		addHeapEntry(heap, heapname, SOURCE_TYPE.HEAP);
	}
	
	public void addConstantEntry(String heap, String constant){
		addHeapEntry(heap, constant, SOURCE_TYPE.CONSTANT);
	}
	
	public void setHeapEntry(String heap, String heapname){
		setHeapEntry(heap, heapname, SOURCE_TYPE.HEAP);
	}
	
	public void setConstantEntry(String heap, String constant){
		setHeapEntry(heap, constant, SOURCE_TYPE.CONSTANT);
	}
	
	public void addTypeEntry(String heap, String typeName){
		addHeapEntry(heap, typeName, SOURCE_TYPE.TYPE);
	}
	public void setTypeEntry(String heap, String typeName){
		setHeapEntry(heap, typeName, SOURCE_TYPE.TYPE);
	}
	
	public void addValueEntryList(String heap, ArrayList<HeapEntry> valueEntryList){
		this.heapTable.AddHeapEntryListTo(heap, valueEntryList);
	}
	
	public void setValueEntryList(String heap, ArrayList<HeapEntry> valueEntryList){
		this.heapTable.OverwriteHeapEntryListTo(heap, valueEntryList);
	}
	
	public ArrayList<HeapEntry> getValueEntryListOf(String heap){
		return this.heapTable.getHeapEntryList(heap);
	}
	
	public void addHeapEntry(String heap, String value, SOURCE_TYPE stype){
		this.heapTable.addValueEntrytoHeapTable(heap, value, stype);
	}
	
	public void setHeapEntry(String heap, String value, SOURCE_TYPE stype){
		this.heapTable.setValueEntrytoHeapTable(heap, value, stype);
	}
}
