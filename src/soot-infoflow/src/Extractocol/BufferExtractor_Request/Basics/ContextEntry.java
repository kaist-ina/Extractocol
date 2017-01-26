package Extractocol.BufferExtractor_Request.Basics;

import soot.SootMethod;

public class ContextEntry {
	private SootMethod inSm;
	private String VariableName;
	private String VariableType;

	public ContextEntry(SootMethod sm, String Vname, String Vtype) {
		setInSm(sm);
		setVariableType(Vtype);
		setVariableName(Vname);
	}

	public SootMethod getInSm() {
		return inSm;
	}

	public void setInSm(SootMethod inSm) {
		this.inSm = inSm;
	}

	public String getVariableName() {
		return VariableName;
	}

	public void setVariableName(String variableName) {
		VariableName = variableName;
	}

	public String getVariableType() {
		return VariableType;
	}

	public void setVariableType(String variableType) {
		VariableType = variableType;
	}

	public boolean equals(ContextEntry ce) {
		return (ce.getInSm().getSignature()
				.equals(this.getInSm().getSignature())
				&& ce.getVariableName().equals(this.getVariableName())
				&& ce.getVariableType().equals(this.getVariableType()));
	}
}
