package extractocol.common.debugger;

import java.util.ArrayList;
import java.util.HashMap;

import extractocol.backend.request.basics.BFNode;

/**
 * @author Jeongmin Kim
 * @Date 2016. 10. 21.
 * @Version 1.00
 */
public class BFTTracker
{
	public String KeyUnit;
	public String ParentMethodSig;
	public ArrayList<HashMap<String, ArrayList<BFNode>>> BFTSnapshotList = new ArrayList<HashMap<String, ArrayList<BFNode>>> ();
	public int idx = 0;
	public BFTTracker(String ut, String signature)
	{
		KeyUnit = ut;
		ParentMethodSig = signature;
	}
	public boolean equals(String unit, String parentClsMtd2)
	{
		if (KeyUnit.equals(unit) && ParentMethodSig.equals(parentClsMtd2))
			return true;
		else
			return false;
	}
	
	public HashMap<String, ArrayList<BFNode>> getList()
	{
		return BFTSnapshotList.get(idx++);
	}
}
