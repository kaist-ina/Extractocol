package extractocol.common.outputs.backendOutputHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import extractocol.common.outputs.BackendOutput;
import extractocol.common.tools.Pair;
import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntryTable;
import extractocol.common.valueEntry.ValueEntryTracking;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;

@SuppressWarnings("serial")
public class ReqRespInfo implements Serializable {
	public RequestInfoEntry reqie;
	public ResponseInfoEntry respie;
	
	// For tracking heap variables
	public ValueEntryTable heapTable = new ValueEntryTable();
	
	// For handling memory allocation
	public ValueEntryTable ReferenceTable = new ValueEntryTable();
	
	public boolean isResponseSigBuildingRequired = true; // Basically, RSB is required. For some cases of retrofit, it is not required.
	
	private boolean needToStay = false;
	private static int bitSIZE = 32;
	private int[] mergeesBit = null;
	private int[] mergersBit = null;
	
	public void setMergeeBit(int i) { setBit(i, this.mergeesBit); }
	public boolean getMergeeBit(int i) { return getBit(i, this.mergeesBit); } 
	private void allocMergeeBit(int size) { this.mergeesBit = new int[size];}
	
	public void setMergerBit(int i) { setBit(i, this.mergersBit); }
	public boolean getMergerBit(int i) { return getBit(i, this.mergersBit); }
	private void allocMergerBit(int size) { this.mergersBit = new int[size];}
	
	public void allocMergerMergeeBit(int sizeOrig) {
		int size = (int) Math.ceil((double)sizeOrig/bitSIZE);
		this.allocMergeeBit(size);
		this.allocMergerBit(size);
		
		for(int i = 0; i < size; i++) {
			this.mergeesBit[i] = 0;
			this.mergersBit[i] = 0;
		}
	}
	
	public void clearMergerMergeeBit() {
		this.mergersBit = null;
		this.mergeesBit = null;
	}
	
	private static boolean getBit(int i, int[] bits) {
		// e.g., when i = 88, idx = 2 & offset = 24
		int idx = i / bitSIZE;
		int offset = i - (idx * bitSIZE);
		int mask = 1 << offset;
		
		return (bits[idx] & mask) != 0; 
	}
	
	private static void setBit(int i, int[] bits) {
		// e.g., when i = 88, idx = 2 & offset = 24
		int idx = i / bitSIZE;
		int offset = i - (idx * bitSIZE);
		int mask = 1 << offset;
		
		bits[idx] = bits[idx] | mask;
	}
	
	public int getMergerListSize() { return bitCount(mergersBit); }
	public int getMergeeListSize() { return bitCount(mergeesBit); }
	
	private static int bitCount(int[] bits) {
		int cnt = 0;
		for(int i = 0; i < bits.length; i++)
			cnt += Integer.bitCount(bits[i]);
		
		return cnt;
	}
	
	public void setNeedToStay(boolean b) { this.needToStay = b; }
	public boolean getNeedToStay() { return this.needToStay; }
	
	/***********************************************************************************/
	/**********                          Basic APIs                           **********/
	/***********************************************************************************/
	public ReqRespInfo(){
		this.reqie = new RequestInfoEntry();
		this.respie = new ResponseInfoEntry();
	}
	
	public ReqRespInfo(RequestInfoEntry ReqIE, ResponseInfoEntry RespIE){
		this.reqie = ReqIE;
		this.respie = RespIE;
	}
	
	public void clear() {
		if(this.reqie != null)
			reqie.clear();
		
		if(this.respie != null)
			respie.clear();
		
		if(this.heapTable != null)
			this.heapTable.Clear();
		
		if(this.ReferenceTable != null)
			this.ReferenceTable.Clear();
		
		this.mergeesBit = null;
		this.mergersBit = null;
	}
	
	public void clearBits() {
		this.mergeesBit = null;
		this.mergersBit = null;
	}
	
	public RequestInfoEntry getRequestInfoEntry(){ return this.reqie; }
	public ResponseInfoEntry getResponseInfoEntry(){ return this.respie; }
	
	public boolean getIsRSBRequired() { return this.isResponseSigBuildingRequired; }
	public void setIsRSBRequired(boolean b) { this.isResponseSigBuildingRequired = b; }
	
	/***********************************************************************************/
	/**********                       APIs for request                        **********/
	/***********************************************************************************/
	public void addRequestHeader(String key, String value){ this.reqie.addHeader(key, value); }
	public void SetRequestHeader(List<Pair> l) { this.reqie.setHeader(l); }
	
	public void setRequestBody(ArrayList<Pair> map){ this.reqie.setBody(map); }
	public void addRequestBody(String key, String value) { this.reqie.addBody(key, value); }
	
	public void addRequestQuery(String key, String value) { this.reqie.addQuery(key, value); }
	
	public void AddHTTPMethod(String method){ this.reqie.methods.add(method); }
	
	/***********************************************************************************/
	/**********                      APIs for Heap Table                      **********/
	/***********************************************************************************/
	public ValueEntryTable getHeapTable(){ return this.heapTable; }
	public void setHeapTable(ValueEntryTable ht){ this.heapTable = ht; }
	
	public ValueEntryTable getReferenceTable(){ return this.ReferenceTable; }
	public void setReferenceTable(ValueEntryTable rt){ this.ReferenceTable = rt; }
	
//	public void addJSONKey(String heap, String key){ this.heapTable.addJSONKey(heap, key); }
//	
//	public void addHeapEntry(String heap, String heapname){ this.heapTable.addValueEntry(heap, heapname, SOURCE_TYPE.HEAP); }
//	public void setHeapEntry(String heap, String heapname){	this.heapTable.setValueEntry(heap, heapname, SOURCE_TYPE.HEAP);	}
//	
	public void addConstantEntry(String heap, String constant){ this.heapTable.addValueEntry(heap, constant, SOURCE_TYPE.CONSTANT, false);	}
	public void setConstantEntry(String heap, String constant){	this.heapTable.setValueEntry(heap, constant, SOURCE_TYPE.CONSTANT, false);	}
//	
//	public void addTypeEntry(String heap, String typeName){	this.heapTable.addValueEntry(heap, typeName, SOURCE_TYPE.TYPE);	}
//	public void setTypeEntry(String heap, String typeName){	this.heapTable.setValueEntry(heap, typeName, SOURCE_TYPE.TYPE);	}
//	
//	public void addValueEntryList(String heap, ValueEntryList valueEntryList){ this.heapTable.addValueEntryList(heap, valueEntryList); }
//	public void setValueEntryList(String heap, ValueEntryList valueEntryList){ this.heapTable.setValueEntryList(heap, valueEntryList); }
	
	public ValueEntryList getValueEntryListOf(String heap){ return this.heapTable.getValueEntryList(heap); }
	
	public void SaveURI(String url) { reqie.setSignature(url); }
	public void SaveURI(ValueEntryTracking vet, String trackingVar){
		String uri = vet.varTable.GenRegex(trackingVar);
		
		SaveURI(uri);
	}
	
	public void SaveResult(int visitCnt) { BackendOutput.SaveResult(this, visitCnt); }
	public void setEP(String EP) { this.reqie.setEPorSPMethod(EP); }
	
	//Added JM
	public String getEP() {return this.reqie.getEPorSPMethod();}
	public void setDP(String DP) { this.reqie.setDPMethod(DP); }
	public void setDPStmt(String DPStmt) { this.reqie.setDPStmt(DPStmt); }
	public boolean doesDPStmtContain(String sootMethod) { return this.reqie.getDPStmt().contains(sootMethod); }
	
}
