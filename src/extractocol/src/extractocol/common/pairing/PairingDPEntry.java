package extractocol.common.pairing;

import java.util.ArrayList;

import soot.SootMethod;
import soot.jimple.Stmt;

public class PairingDPEntry
{
	private String DP;
	private String DPStmt;
	private String Signature;
	private ArrayList<SemanticAppliedEntry> epList;
	
	public PairingDPEntry(String _DP, String _DPStmt, String _Signature, ArrayList<SemanticAppliedEntry> eplist) {
		DP = _DP;
		DPStmt = _DPStmt;
		Signature = _Signature;
		
		epList = new ArrayList<SemanticAppliedEntry>();
		
		if(eplist != null)
			epList.addAll(eplist);
	}
	
	public ArrayList<SemanticAppliedEntry> getEpList()
	{
		return epList;
	}
	public String getDP()
	{
		return DP;
	}
	public String getDPStmt()
	{
		return DPStmt;
	}
	public void setDPStmt(String dPStmt)
	{
		DPStmt = dPStmt;
	}
	public void setDP(String dP)
	{
		DP = dP;
	}
	public void setSignature(String signature)
	{
		Signature = signature;
	}
	public void setEpList(ArrayList<SemanticAppliedEntry> epList)
	{
		this.epList = epList;
	}
	public String getSignature()
	{
		return Signature;
	}

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
	
	public void addEP(SemanticAppliedEntry newep)
	{
		boolean isNew = true;
		for(SemanticAppliedEntry ep : epList) {
			if(newep.isSame(ep))
				isNew = false;
		}
		if(isNew) {
			epList.add(newep);
		}
	}

	public void addEPEntries(ArrayList<SemanticAppliedEntry> list)
	{
		for(SemanticAppliedEntry newep : list) {
			boolean isNew = true;
			for(SemanticAppliedEntry ep : epList) {
				if(newep.isSame(ep))
					isNew = false;
			}
			if(isNew) {
				epList.add(newep);
			}
		}
	}
	
	public static boolean isEntryIncluded(SemanticAppliedEntry entry, ArrayList<SemanticAppliedEntry> list)
	{
		for(SemanticAppliedEntry ep : list)
			if(entry.isSame(ep))
				return true;
		
		return false;
	}
}