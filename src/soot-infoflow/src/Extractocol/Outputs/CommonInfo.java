package Extractocol.Outputs;

import java.util.ArrayList;

import com.gaurav.tree.LinkedTree;
import com.gaurav.tree.Tree;

import Extractocol.BufferExtractor_Request.HeapManagement.HeapEntry;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapTable;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapTreeNode;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapEntry.SOURCE_TYPE;
import Extractocol.Pairing.SemanticAppliedList;

public class CommonInfo {
	// Signature (will be Request URI or Response Body signature)
	public String Signature;
	
	// PE contains
	//  (1) statements to which a semantic model is applied, 
	//  (2) and the methods at which each statement is located.
	public SemanticAppliedList semanticAppliedList;
	
	// IDs of response pair
	public ArrayList<Integer> pairIDs;
	
	// Method that includes a demarcation point (DP) statement
	public String DPMethod;
	
	// Method which is Entry Point (EP) or Sink Point (SP)
	//  - EP is for request signature.
	//  - SP is for response signature.
	//  - A request signature can be identified using both EP method and DP method.
	//  - A response signature can be identified using both DP method and SP method.
	public String EPorSPMethod;
	
	// statement of demarcation point (DP)
	public String DPStmt;
	
	
	/**********/
	/** APIs **/
	/**********/
	public CommonInfo(){
		this.Signature = null;
		this.semanticAppliedList = new SemanticAppliedList();
		this.pairIDs = new ArrayList<Integer>();
		
		this.DPMethod = null;
		this.EPorSPMethod = null;
		this.DPStmt = null;
	}
	
	public String getSignature(){
		return this.Signature;
	}
	
	public void setSignature(String signature){
		this.Signature = signature;
	}
	
	public SemanticAppliedList getSemanticAppliedList(){
		return this.semanticAppliedList;
	}
	
	public void setSemanticAppliedList(SemanticAppliedList list){
		this.semanticAppliedList = list;
	}
	
	public ArrayList<Integer> getPairIDs(){
		return this.pairIDs;
	}
	
	public void addPairIDs(Integer id){
		if(!this.pairIDs.contains(id))
			this.pairIDs.add(id);
	}
	
	public void setPairIDs(ArrayList<Integer> idList){
		if(idList == null) return;
		
		this.pairIDs.clear();
		this.pairIDs.addAll(idList);
	}
	
	public String getDPMethod(){
		return this.DPMethod;
	}
	
	public void setDPMethod(String dpMethod){
		this.DPMethod = dpMethod;
	}
	
	public String getEPorSPMethod(){
		return this.EPorSPMethod;
	}
	
	public void setEPorSPMethod(String EPorSP){
		this.EPorSPMethod = EPorSP;
	}
	
	public String getDPStmt(){
		return this.DPStmt;
	}
	
	public void setDPStmt(String dpStmt){
		this.DPStmt = dpStmt;
	}
	
	
}
