package ResponseEp_Extractor;

import java.util.ArrayList;
import java.util.List;

import soot.SootClass;

public class EPCandidate
{
	private String EPSig;
	private String  DPStmt;
	public boolean hasDPStmt;
	public boolean hasEPSig;
	public boolean isreturnConsiderCase;
	private List<SootClass> superclasses;
	
	public EPCandidate()
	{
		hasDPStmt = false;
		hasEPSig = false;
		isreturnConsiderCase = false;
	}
	
	public EPCandidate (String _EPSig, String _DPStmt)
	{
		setEPSig(_EPSig);
		setDPStmt(_DPStmt);
	}

	public String getEPSig()
	{
		return EPSig;
	}
	
	public void setEPandDPStmt(String _EPSig, String _DPStmt)
	{
		setEPSig(_EPSig);
		setDPStmt(_DPStmt);
	}

	public void setEPSig(String ePSig)
	{
		EPSig = ePSig;
	}

	public String getDPStmt()
	{
		return DPStmt;
	}

	public void setDPStmt(String dPStmt)
	{
		DPStmt = dPStmt;
	}

	public List<SootClass> getSuperclasses()
	{
		return superclasses;
	}

	public void setSuperclasses(List<SootClass> superclasses)
	{
		this.superclasses = superclasses;
	}
}
