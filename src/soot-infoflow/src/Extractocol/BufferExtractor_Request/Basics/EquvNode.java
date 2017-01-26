package Extractocol.BufferExtractor_Request.Basics;

import java.util.ArrayList;
import java.util.HashMap;

public class EquvNode {
	private String Parent;
	private HashMap<String, ArrayList<BFNode>> ENtable;
	
	public EquvNode() {
		ENtable = new HashMap<String, ArrayList<BFNode>>();
		ENtable.put("String",  new ArrayList<BFNode>());
		ENtable.put("int",  new ArrayList<BFNode>());
		ENtable.put("Const",  new ArrayList<BFNode>());
		ENtable.put("JSONArray",  new ArrayList<BFNode>());
		ENtable.put("double",  new ArrayList<BFNode>());
	}
	
	public String getParent() {
		return Parent;
	}
	public void setParent(String parent) {
		Parent = parent;
	}
	public HashMap<String, ArrayList<BFNode>> getENtable() {
		return ENtable;
	}
	public void setENtable(HashMap<String, ArrayList<BFNode>> bFtable) {
		ENtable = bFtable;
	}
	
}