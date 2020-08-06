package extractocol.frontend.output.basic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import soot.Unit;

@SuppressWarnings("serial")
public class DPContainer implements Serializable {
	String DPMethod;
	String DPStmt;
	int DPStmtHash;
	List<EPContainer> EPList = new ArrayList<EPContainer>();
	
	public DPContainer(String _dpMethod, String _dpStmt, int _dpStmtHash, String _ep, List<String> _taintMethodSet){
			this.DPMethod = _dpMethod;
			this.DPStmt = _dpStmt;
			this.DPStmtHash = _dpStmtHash;

			this.addEP(_ep, _taintMethodSet);
		}

	public String getDPMethod() { return this.DPMethod; }
	public String getDPStmt() { return this.DPStmt; }
	public int getDPStmtHash() { return this.DPStmtHash; }
	
	public List<EPContainer> getEPList() { return this.EPList; }

	public void addEP(String _EP, List<String> _taintMethodSet){
		EPContainer epc = new EPContainer(_EP, _taintMethodSet);
		this.EPList.add(epc);
	}
	
	public void clear() {
		this.DPMethod = null;
		this.DPStmt = null;
		if(this.EPList != null) {
			for(EPContainer e : this.EPList)
				e.clear();
		}
	}

	public boolean isSameWith(String _dpMethod, String _dpStmt, int _dpStmtHash){
		return this.DPMethod.equals(_dpMethod) && this.DPStmt.equals(_dpStmt) && (this.DPStmtHash == _dpStmtHash);
	}
}
