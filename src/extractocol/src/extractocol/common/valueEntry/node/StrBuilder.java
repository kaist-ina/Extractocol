package extractocol.common.valueEntry.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntryTable;

@SuppressWarnings("serial")
public class StrBuilder extends ValueEntry implements Serializable {
	ArrayList<StringBuilder> SBList = new ArrayList<StringBuilder>();
	Set<String> StrList;
	static int maxSize = 1000;
	
	public StrBuilder() { this.SBList.add(new StringBuilder()); }
	public StrBuilder(String str){
		if(str != null){
			StringBuilder newSB = new StringBuilder();
			newSB.append(str);
			this.SBList.add(newSB);
		}
	}
	
	public void setSBList(ArrayList<StringBuilder> _list) { this.SBList = _list; }
	public ArrayList<StringBuilder> getSBList() { return this.SBList; }
	
	/** Get type of this entry **/
	public SOURCE_TYPE getSourceType() { return SOURCE_TYPE.STRBUILDER; }
	
	/** Clear this entry instance **/
	public void Clear(){
		if(this.SBList != null){
			for(StringBuilder e : this.SBList){
				e.delete(0, e.length());
				e = null;
			}	
			
			this.SBList.clear();
			this.SBList  = null;
		}
		
		if(this.StrList != null){
			this.StrList.clear();
			this.StrList = null;
		}
	}
	
	public String GenRegex(){
		if(this.SBList.size() == 0)
			return null;
		
		String res = "";
		for(StringBuilder e: this.SBList)
			res += ValueEntryTable.AppendParenthesis(e.toString()) + "|";
		
		if(res.equals(""))
			return res;
		else
			return res.substring(0, res.length() - 1);
	}
	
	/** Clone this entry instance **/
	public ValueEntry Clone(){
		StrBuilder newObject = new StrBuilder(null);
		ArrayList<StringBuilder> newSBList = new ArrayList<StringBuilder>();
		
		for(StringBuilder curSB: this.getSBList())
			newSBList.add(new StringBuilder(curSB));
		
		newObject.setSBList(newSBList);
		return newObject;
	}
	
	public void addEntry(ValueEntry ve){
		if(ve.getSourceType() != this.getSourceType())
			return;
		
		if(((StrBuilder)ve).getSBList() != null){
			if(this.SBList.size() < maxSize)
			{
				// Build Hash-set for better search speed
				this.BuildHashSet();
				
				for(StringBuilder srcSB : ((StrBuilder)ve).getSBList())
					if(!this.isContained(srcSB))
						this.SBList.add(new StringBuilder(srcSB));
				
				// Clear the hash-set
				this.ClearHashSet();
			}
		}	
	}
	
	public void addEntry(String str){
		if(str != null && this.SBList != null){
			for(StringBuilder e: this.SBList)
				e.append(ValueEntryTable.AppendParenthesis(str));
		}
	}
	
	private boolean isContained(StringBuilder srcSB){
		return this.StrList.contains(srcSB.toString());
		/*for (StringBuilder destSB : this.SBList)
			if (srcSB.toString().equals(destSB.toString()))
				return true;
		
		return false;*/
	}
	
	private void BuildHashSet(){
		this.StrList = new HashSet<String>();
		
		for (StringBuilder SB : this.SBList)
			this.StrList.add(SB.toString());
	}
	
	private void ClearHashSet(){
		if(this.StrList != null){
			this.StrList.clear();
			this.StrList = null;
		}
	}
		
}
