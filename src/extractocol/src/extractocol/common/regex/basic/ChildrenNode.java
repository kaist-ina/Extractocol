package extractocol.common.regex.basic;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import extractocol.common.outputs.BackendOutput;
import extractocol.common.regex.RegexHandler;
import extractocol.tester.HeapHandling;

public class ChildrenNode {
	public static enum RELATION_TYPE {OR, AND} 
	private LinkedList<RegexNode> children = null;
	private LinkedList<RELATION_TYPE> relations = null;
	
	public ChildrenNode() {
		this.children = new LinkedList<RegexNode>();
		this.relations = new LinkedList<RELATION_TYPE>();
	}
	
	public void addChild(RegexNode child) { this.children.add(child); }
	public LinkedList<RegexNode> getChildren() { return this.children; }
	public LinkedList<RELATION_TYPE> getRelations() { return this.relations; }
	
	public void addChild(RegexNode child, boolean or) {
		this.addChild(child);
		
		if(or)
			this.relations.add(RELATION_TYPE.OR);
		else
			this.relations.add(RELATION_TYPE.AND);
	}
	
	public boolean hasSameTokenWith(RegexNode target) {
		for(RegexNode rn: this.children) {
			if(rn == target)
				continue;
			
			if(rn.getToken().equals(target.getToken()))
				return true;
		}
		
		return false;
	}
	
	public boolean hasChildRegexMatchingWith(RegexNode target) {
		for(RegexNode rn: this.children) {
			if(rn == target)
				continue;
			
			String tokenRefined = BackendOutput.urlRefinement(rn.getToken());
			if(target.getToken().matches(tokenRefined))
				return true;
		}
		return false;
	}
	
	public boolean doesItContainIndicator() {
		for(RegexNode rn: this.children) {
			if(rn.getToken().matches(HeapHandling.heapP))
				return true;
		}
		return false;
	}
	
	public boolean hasChild() {
		if(this.children == null)
			return false;
		
		return this.children.size() > 0;
	}
	
	public String toString() {
		String out = "";
		
		for(int i = 0; i < this.children.size(); i++) {
			if(i != this.children.size() - 1)
				out += this.children.get(i).toString() + ((this.relations.get(i) == RELATION_TYPE.OR)?"|":"");
			else
				out += this.children.get(i).toString();
		}
		
		return out;
	}
	
	public boolean doesContainGroup() {
		for(RegexNode rn : this.children)
			if(rn.getType() == RegexNode.NODE_TYPE.GROUP)
				return true;
		
		return false;
	}
}
