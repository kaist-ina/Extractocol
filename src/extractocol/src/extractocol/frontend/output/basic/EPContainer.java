package extractocol.frontend.output.basic;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class EPContainer implements Serializable {
	String EP;
	List<String> TaintMethodSet;
	
	EPContainer(String _EP, List<String> _taintMethodSet){
		this.EP = _EP;
		this.TaintMethodSet = _taintMethodSet;
	}
	
	public String getEP() { return this.EP; }
	public List<String> getTaintMethodSet() { return this.TaintMethodSet; }
	public void clear() {
		this.EP = null;
		if(this.TaintMethodSet != null)
			this.TaintMethodSet.clear();
	}
}
