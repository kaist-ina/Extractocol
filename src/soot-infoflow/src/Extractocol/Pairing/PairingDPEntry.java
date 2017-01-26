package Extractocol.Pairing;

import java.util.ArrayList;

import soot.SootMethod;
import soot.jimple.Stmt;

public class PairingDPEntry
{
	private SootMethod DP;
	private Stmt DPStmt;
	private String Signature;
	private ArrayList<SemanticAppliedEntry> epList;
	
	public PairingDPEntry(SootMethod _DP, Stmt _DPStmt, String _Signature, ArrayList<SemanticAppliedEntry> eplist) {
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
	public SootMethod getDP()
	{
		return DP;
	}
	public Stmt getDPStmt()
	{
		return DPStmt;
	}
	public void setDPStmt(Stmt dPStmt)
	{
		DPStmt = dPStmt;
	}
	public void setDP(SootMethod dP)
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