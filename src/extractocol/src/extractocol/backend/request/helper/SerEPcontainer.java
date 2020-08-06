
package extractocol.backend.request.helper;

import java.util.ArrayList;
import java.util.List;

import extractocol.backend.request.basics.EPcontainer;

public class SerEPcontainer
{
	private List<String> EPlist = new ArrayList<String>();
	private String DP;
	private String dpStmt;
	public SerEPcontainer(EPcontainer EPC)
	{
		if (EPC.getDP() != null)
		{
			DP = EPC.getDP().toString();
			if (EPC.getDPStmt() != null)
			{
				dpStmt = EPC.getDPStmt().toString();
			}
		}
		for (int j = 0; j < EPC.getEPlist().size(); j++)
		{
			String EPString = EPC.getEPlist().get(j).toString();
			EPlist.add(EPString);
		}
	}
	public List<String> getEPlist()
	{
		return EPlist;
	}
	public void setEPlist(List<String> ePlist)
	{
		EPlist = ePlist;
	}
	public String getDpStmt()
	{
		return dpStmt;
	}
	public void setDpStmt(String dpStmt)
	{
		this.dpStmt = dpStmt;
	}
	public String getDP()
	{
		return DP;
	}
	public void setDP(String dP)
	{
		DP = dP;
	}
}