package extractocol.common.outputs.backendOutputHelper;

import java.io.Serializable;

import extractocol.common.pairing.SemanticAppliedList;
import extractocol.common.valueEntry.ValueEntryList;

@SuppressWarnings("serial")
public class ResponseInfoEntry extends CommonInfo implements Serializable  
{
	public static enum SEED_TYPE {DEST, BASE, ARG};
	private SEED_TYPE seedType;
	private int argNum = 0;
	
	
	ValueEntryList startingVar = null;
	
	public ResponseInfoEntry(){}
	public ResponseInfoEntry(String _Body, SemanticAppliedList list)
	{
		this.setSignature(_Body);
		this.setSemanticAppliedList(list);
	}
	
	public void SetStartingVar(ValueEntryList vel){ this.startingVar = vel; }
	public ValueEntryList getStartingVar() { return this.startingVar; }
	public boolean isStartingVarNull(){ return this.startingVar == null; }
	
	public void setSeedType(SEED_TYPE type) { this.seedType = type; }
	public SEED_TYPE getSeedType() { return this.seedType; }
	
	public void SetArgNum(int num) { this.argNum = num; }
	public int getArgNum() { return this.argNum; }
	
	public void clear() {
		if(this.startingVar != null)
			this.startingVar.Clear();
	}
}
