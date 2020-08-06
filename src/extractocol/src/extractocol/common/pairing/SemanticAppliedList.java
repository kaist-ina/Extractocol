package extractocol.common.pairing;

import java.io.Serializable;
import java.util.ArrayList;


@SuppressWarnings("serial")
public class SemanticAppliedList implements Serializable {
	private ArrayList<SemanticAppliedEntry> epList;

	public SemanticAppliedList(){
		this.epList = new ArrayList<SemanticAppliedEntry>(); 
	}
	public SemanticAppliedList(ArrayList<SemanticAppliedEntry> eplist) {
		this.epList = new ArrayList<SemanticAppliedEntry>();
		
		if(eplist != null)
			epList.addAll(eplist);
	}
	
	public ArrayList<SemanticAppliedEntry> getEpList()
	{
		return epList;
	}
	
	public void setEpList(ArrayList<SemanticAppliedEntry> epList)
	{
		if(epList == null)
			return;
		
		this.epList.clear();
		this.epList.addAll(epList);
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
