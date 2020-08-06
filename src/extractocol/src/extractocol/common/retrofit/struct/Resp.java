package extractocol.common.retrofit.struct;

import extractocol.common.valueEntry.ValueEntryTable;

public class Resp {
	boolean isRespSigBuildingRequired;
	String retType_outermost;
	String retType_innermost;
	String callbackType;
	ValueEntryTable vet;
	
	public Resp() {
		this.retType_innermost = null;
		this.retType_outermost = null;
		this.callbackType = null;
		this.isRespSigBuildingRequired = false;
		this.vet = new ValueEntryTable();
	}
	
	public void clear() {
		if(this.vet != null) {
			this.vet.Clear();
			this.vet = null;
		}
	}
	
	public String getRetTypeOutermost() { return this.retType_outermost; }
	public void setRetTypeOutermost(String t) {this.retType_outermost = t;}
	
	public String getRetTypeInnermost() { return this.retType_innermost; }
	public void setRetTypeInnermost(String t) {this.retType_innermost = t;}
	public boolean hasRetTypeInnermost() { return this.retType_innermost != null;}
	
	public String getCallBackType() { return this.callbackType; }
	public void setCallBackType(String t) {this.callbackType = t;}
	public boolean hasCallBackType() { return this.callbackType != null; }
	
	public boolean getIsRespSigBuildingRequired() { return this.isRespSigBuildingRequired; }
	public void setIsRespSigBuildingRequired(boolean b) { this.isRespSigBuildingRequired = b; }
	
	public ValueEntryTable VET() { return this.vet; }
	
}
