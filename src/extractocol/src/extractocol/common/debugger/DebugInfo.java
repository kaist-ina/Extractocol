
package extractocol.common.debugger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import extractocol.backend.request.basics.BFNode;
import soot.SootMethod;
import soot.Unit;

public class DebugInfo
{
	ArrayList<BFTTracker> BFTList = new ArrayList<BFTTracker>();
	/**
	 * @author Jeongmin Kim
	 * @version 1.00
	 * @param ut
	 *            : target Unit
	 * @param sm
	 *            : sm that includes target unit.
	 */
	@SuppressWarnings("unchecked")
	public void AddUnitBFTPair(Unit ut, SootMethod sm, HashMap<String, ArrayList<BFNode>> bfttable)
	{
		BFTTracker tracker = null;
		tracker = hasBFTPair(ut.toString(), sm.getSignature());
		if (tracker == null)
		{
			BFTTracker trk = new BFTTracker(ut.toString(), sm.getSignature());
			trk.BFTSnapshotList.add(deepcopyBFT(bfttable));
			BFTList.add(trk);
		}
		else
		{
			tracker.BFTSnapshotList.add(deepcopyBFT(bfttable));
		}
	}
	/**
	 * @author Jeongmin Kim
	 * @version 1.00
	 * @param unit
	 *            : Target unit
	 * @param ParentClsMtd
	 *            : Class and Method that include target unit.
	 * @return
	 */
	public BFTTracker hasBFTPair(String unit, String ParentMethodSig)
	{
		for (BFTTracker tracker : BFTList)
		{
			if (tracker.equals(unit, ParentMethodSig))
				return tracker;
		}
		return null;
	}
	public HashMap<String, ArrayList<BFNode>> deepcopyBFT(HashMap<String, ArrayList<BFNode>> bfttable)
	{
		HashMap<String, ArrayList<BFNode>> result = new HashMap<String, ArrayList<BFNode>>();
		for (String key : bfttable.keySet())
		{
			result.put(key, CopyList(bfttable.get(key)));
		}
		return result;
	}
	private ArrayList<BFNode> CopyList(ArrayList<BFNode> src)
	{
		ArrayList<BFNode> tlist = new ArrayList<BFNode>();
		if (src == null)
			return null;
		for (BFNode bfn : src)
		{
			BFNode newbfn = new BFNode();
			newbfn.setKey(bfn.getKey());
			newbfn.setSootValue(bfn.getSootValue());
			newbfn.setStringForUrl(bfn.getStringForUrl());
			tlist.add(newbfn);
		}
		return tlist;
	}
	public boolean CheckDiff(Unit ut, SootMethod sm, HashMap<String, ArrayList<BFNode>> bfttable)
	{
		BFTTracker PreviousResult = hasBFTPair(ut.toString(), sm.getSignature());
		if (PreviousResult.BFTSnapshotList.size() > 1)
			return true;
		HashMap<String, ArrayList<BFNode>> src = PreviousResult.getList();
		if (CompareBFTs(src, bfttable, ut))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	private boolean CompareBFTs(HashMap<String, ArrayList<BFNode>> previous, HashMap<String, ArrayList<BFNode>> current, Unit ut)
	{
		if (!CompareKeyset(previous.keySet(), current.keySet()))
		{
			System.out.println("Keyset size is not same");
			return false;
		}
		for (String key : previous.keySet())
		{
			ArrayList<BFNode> prevlist = previous.get(key);
			ArrayList<BFNode> currentlist = current.get(key);
			if (prevlist == null && currentlist == null)
				continue;
			else
				if (!CompareBFNlist(prevlist, currentlist))
				{
					System.out.println("\nUnit : " + ut.toString());
					printList(prevlist, currentlist);
					System.exit(1);
					return false;
				}
		}
		return true;
	}
	private void printList(ArrayList<BFNode> prevlist, ArrayList<BFNode> currentlist)
	{
		System.out.println("Prev Result");
		for (BFNode bfn : prevlist)
		{
			System.out.print(" " + bfn.getStringForUrl());
		}
		System.out.println("\nCurrent Result");
		for (BFNode bfn : currentlist)
		{
			System.out.print(" " + bfn.getStringForUrl());
		}
	}
	private boolean CompareBFNlist(ArrayList<BFNode> prevlist, ArrayList<BFNode> currentlist)
	{
		if (prevlist.size() == 0 && currentlist.size() == 0)
			return true;
		if (prevlist.size() != currentlist.size())
		{
			System.out.println("size is not same");
		}
		for (int i = 0; i < prevlist.size(); i++)
		{
			if (prevlist.get(i).getSootValue() == null && prevlist.get(i).getStringForUrl() == null)
				continue;
			if (prevlist.get(i).getStringForUrl() == null)
			{
				if (!prevlist.get(i).getSootValue().equals(currentlist.get(i).getSootValue()))
					return false;
				else
					continue;
			}
			else
			{
				if (!prevlist.get(i).getStringForUrl().equals(currentlist.get(i).getStringForUrl()))
					return false;
				else
					continue;
			}
		}
		return true;
	}
	private boolean CompareKeyset(Set<String> keySet, Set<String> keySet2)
	{
		if (keySet.size() == 0 && keySet2.size() == 0)
			return true;
		String[] src = keySet.toArray(new String[keySet.size()]);
		Arrays.sort(src);
		String[] dst = keySet2.toArray(new String[keySet2.size()]);
		Arrays.sort(dst);
		for (int i = 0; i < keySet.size(); i++)
		{
			if (!src[i].equals(dst[i]))
				return false;
		}
		return true;
	}
	public void PrintBFT(HashMap<String, ArrayList<BFNode>> BFT)
	{
		for (String key : BFT.keySet())
		{
			System.out.print("Key : " + key);
			for (BFNode bfn : BFT.get(key))
			{
				System.out.print(" " + bfn.getStringForUrl());
			}
			System.out.println();
		}
	}
}
