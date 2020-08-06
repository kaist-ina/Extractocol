package extractocol.backend.request.basics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.SootMethod;
import soot.jimple.Stmt;

public class EPcontainer {
	private List<SootMethod> EPlist= new ArrayList<SootMethod>();
	private Set<SootMethod> MethodSet = new HashSet<SootMethod>();
	private List<Set<SootMethod>> callflow = new ArrayList<Set<SootMethod>>();
	private SootMethod DP;
	public String DemarcationInvoke;
	private Stmt dpStmt;
	
	public List<SootMethod> getEPlist() {
		return EPlist;
	}
	public List<Set<SootMethod>> getCallflow() {
		return callflow;
	}
	public void setCallflow(List<Set<SootMethod>> callflow) {
		this.callflow = callflow;
	}
	public void setEPlist(List<SootMethod> ePlist) {
		EPlist = ePlist;
	}
	public Set<SootMethod> getMethodSet() {
		return MethodSet;
	}
	public void setMethodSet(Set<SootMethod> methodSet) {
		MethodSet = methodSet;
	}
	public SootMethod getDP() {
		return DP;
	}
	public void setDP(SootMethod dP) {
		DP = dP;
	}
	public Stmt getDPStmt() {
		return dpStmt;
	}
	public void setDPStmt(Stmt dP) {
		dpStmt = dP;
	}
}

