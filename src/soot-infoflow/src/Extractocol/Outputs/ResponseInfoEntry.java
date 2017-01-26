package Extractocol.Outputs;

import Extractocol.Pairing.SemanticAppliedList;

public class ResponseInfoEntry extends CommonInfo 
{
	public ResponseInfoEntry(){
		//
	}
	
	public ResponseInfoEntry(String _Body, SemanticAppliedList list)
	{
		this.setSignature(_Body);
		this.setSemanticAppliedList(list);
	}
}
