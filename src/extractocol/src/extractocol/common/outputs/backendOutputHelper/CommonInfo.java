package extractocol.common.outputs.backendOutputHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.gaurav.tree.LinkedTree;
import com.gaurav.tree.Tree;

import extractocol.backend.request.heapManagement.HeapTreeNode;
import extractocol.common.pairing.SemanticAppliedList;
import extractocol.common.regex.RegexHandler;
import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntryTable;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;
import extractocol.frontend.output.basic.TaintResultEntry;

@SuppressWarnings("serial")
public class CommonInfo implements Serializable {
	// Signature (will be Request URI or Response Body signature)
	public String OrigSignature; // Signature by backend analysis
	public String wholeRegexSignature;
	public List<String> regexSignature; // Regex signature by heap handler 
	public List<String> proxySignature; // Proxy signature by heap handler
	
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
		this.OrigSignature = null;
		this.wholeRegexSignature = null;
		this.regexSignature = new ArrayList<String>();
		this.proxySignature = new ArrayList<String>();
		this.semanticAppliedList = new SemanticAppliedList();
		this.pairIDs = new ArrayList<Integer>();
		
		this.DPMethod = null;
		this.EPorSPMethod = null;
		this.DPStmt = null;
	}
	
	public void print_info() { System.out.println(OrigSignature); }
	
	public String getSignature(){ return this.OrigSignature; }
	public void setSignature(String signature){ this.OrigSignature = signature;	}
	
	public String getWholeRegexSignature() { return this.wholeRegexSignature; }
	public void initWholeRegexSignature() {
		if(this.regexSignature == null)
			return;
		
		String res = "";
		for(int i = 0; i < this.regexSignature.size(); i++) {
			String regexSig = this.regexSignature.get(i);
			if(i == this.regexSignature.size() - 1)
				res += regexSig;
			else
				res += regexSig + "|";
		}
		
		this.wholeRegexSignature = RegexHandler.Refinement(res);
	}
	
	public List<String> getRegexSignature(){ return this.regexSignature; }
	public void addRegexSignature(String Sig){ this.regexSignature.add(Sig); }
	
	public List<String> getProxySignature(){ return this.proxySignature; }
	public void addProxySignature(String proxySig){ this.proxySignature.add(proxySig); }
	
	public SemanticAppliedList getSemanticAppliedList(){ return this.semanticAppliedList; }
	public void setSemanticAppliedList(SemanticAppliedList list){ this.semanticAppliedList = list; }
	
	public ArrayList<Integer> getPairIDs(){ return this.pairIDs; }
	public void addPairIDs(Integer id){
		if(!this.pairIDs.contains(id))
			this.pairIDs.add(id);
	}
	
	public void setPairIDs(ArrayList<Integer> idList){
		if(idList == null) return;
		
		this.pairIDs.clear();
		this.pairIDs.addAll(idList);
	}
	
	public String getDPMethod(){ return this.DPMethod; }
	public void setDPMethod(String dpMethod){ this.DPMethod = dpMethod;	}
	
	public String getEPorSPMethod(){ return this.EPorSPMethod; }
	public void setEPorSPMethod(String EPorSP){ this.EPorSPMethod = EPorSP;	}
	
	public String getDPStmt(){ return this.DPStmt; }
	public void setDPStmt(String dpStmt){ this.DPStmt = dpStmt;	}
	
	public boolean CompareOrigSigWith(CommonInfo other){
		if (other == null)
			return false;
		
		if (OrigSignature == null) {
			if (other.OrigSignature != null)
				return false;
		} else if (other.OrigSignature == null) {
			if(this.OrigSignature != null)
				return false;
		}		
		else if (!OrigSignature.equals(other.OrigSignature))
			return false;
		
		return true;
	}
	
	public boolean isValid(){
		return this.OrigSignature != null;
	}
}
