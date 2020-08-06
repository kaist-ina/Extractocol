package extractocol.frontend.slice;

import java.util.LinkedList;

import soot.SootMethod;
import soot.jimple.Stmt;

public class FlowsensitiveSlice
{
	private String EP;
	private String DP;
	private LinkedList<String> taintmethods = new LinkedList<String> ();
	private SootMethod DPMethod;
	private Stmt DPStmt;
	
	public FlowsensitiveSlice( String _EP, SootMethod _DPMethod, String _DP, Stmt _DPStmt, LinkedList<String> _taintmethods)
	{
		EP = _EP;
		DP = _DP;
		taintmethods = _taintmethods;
		DPMethod = _DPMethod;
		DPStmt = _DPStmt;
	}
	
	public Stmt getDPStmt()
	{
		return DPStmt;
	}
	public SootMethod getDPMethod()
	{
		return DPMethod;
	}
	
	public String getEP()
	{
		return EP;
	}
	public void setEP(String eP)
	{
		EP = eP;
	}
	public String getDP()
	{
		return DP;
	}
	public void setDP(String dP)
	{
		DP = dP;
	}
	public LinkedList<String> getTaintmethods()
	{
		return taintmethods;
	}
	public void setTaintmethods(LinkedList<String> taintmethods)
	{
		this.taintmethods = taintmethods;
	}
	public boolean equals(LinkedList<String> src)
	{
		if (taintmethods == null || src == null)
			return false;
		
		if (taintmethods.size() != src.size())
			return false;
		else
		{
			for (int i = 0 ; i < taintmethods.size(); i++)
			{
				if (!src.get(i).equals(taintmethods.get(i)))
					return false;
			}
		}
		return true;
	}
}
