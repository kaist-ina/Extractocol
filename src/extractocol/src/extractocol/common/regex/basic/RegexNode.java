package extractocol.common.regex.basic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import extractocol.common.regex.basic.ChildrenNode.RELATION_TYPE;



public class RegexNode {
	public static enum NODE_TYPE {ROOT, GROUP, ELEMENT}
	
	private NODE_TYPE type = null;
	private ChildrenNode children = new ChildrenNode();
	private RegexNode parent = null;
	private StringBuilder token = new StringBuilder();

	public RegexNode(NODE_TYPE nt, RegexNode _parent) {
		this.type = nt;
		this.parent = _parent;
	}
	
	public RegexNode(NODE_TYPE nt) {
		this.type = nt;
		this.parent = this;
	}

	public void removeChild(RegexNode child) {
		int idx = this.children.getChildren().indexOf(child);
		
		if(idx == -1)
			return;
		
		this.children.getChildren().remove(child);
		this.children.getRelations().remove(idx);
	}
	
	public void addChild(RegexNode child, boolean or) {
		if(this.children == null)
			this.children = new ChildrenNode();
		
		if(this.hasChild())
			this.children.addChild(child, or);
		else
			this.children.addChild(child);
	}
	
	public void addElement(String str, boolean or) {
		RegexNode elem = new RegexNode(NODE_TYPE.ELEMENT, this);
		elem.addToken(str);
		
		this.addChild(elem, or);
	}
	
	public boolean hasSameTokenChildWith(RegexNode target) {
		return this.children.hasSameTokenWith(target);
	}
	
	public boolean hasChildRegexMatchingWith(RegexNode target) {
		return this.children.hasChildRegexMatchingWith(target);
	}
	
	public boolean doesItContainIndicator() {
		if(this.children == null)
			return false;
		
		return this.children.doesItContainIndicator();
	}
	
	public boolean hasChild() {
		if(this.children == null)
			return false;
		
		return this.children.hasChild();
	}
	
	public RegexNode getParent() { return this.parent; }
	public void addToken(char c) { this.token.append(c); }
	public void addToken(String s) { this.token.append(s); }
	public String getToken() { return this.token.toString(); }
	public NODE_TYPE getType() { return this.type; }
	public LinkedList<RegexNode> getChildrenList() { return this.children.getChildren(); }
	public LinkedList<RELATION_TYPE> getRelationList() { return this.children.getRelations(); }
	
	public ChildrenNode getChildren() { return this.children; }
	public boolean isGroup() { return this.type == NODE_TYPE.GROUP; }
	public boolean isElement() { return this.type == NODE_TYPE.ELEMENT; }
	
	public String toString() { 
		return this.type.toString().charAt(0) + 
				(this.token.toString().equals("")? "" : "(" + this.token.toString() + ")"); 
	}
	
	public boolean doesContainGroup() {
		if(this.children == null)
			return false;
		return this.children.doesContainGroup();
	}
	
	public static void Merge(RegexNode front, RegexNode rear) {
		// One of them must be GROUP type.
		if(!front.isGroup() && !rear.isGroup())
			return;
		
		LinkedList<RegexNode> frontList = new LinkedList<RegexNode>();
		LinkedList<RegexNode> rearList = new LinkedList<RegexNode>();
		RegexNode src = null, dst = null;
		
		if(front.isGroup() && rear.isElement()) {
			dst = front;
			src = rear;
		}else if(front.isElement() && rear.isGroup()) {
			dst = rear;
			src = front;
		}else if(front.isGroup() && rear.isGroup()) {
			dst = rear;
			src = front;
		}
		
		
		// (It supposes in group node all children are ELEMENT and all relations are OR.)
		if(front.isGroup())
			frontList.addAll(front.getChildrenList());
		else if (front.isElement())
			frontList.add(front);
		
		if(rear.isGroup())
			rearList.addAll(rear.getChildrenList());
		else if (rear.isElement())
			rearList.add(rear);

		
		// Add
		if(frontList.size() == 0 && rearList.size() > 0) {
			for(RegexNode rNode: rearList) {
				String rStr = rNode.getToken();
				dst.addElement(rStr, true);
			}
		}else if (frontList.size() > 0 && rearList.size() == 0) {
			for(RegexNode fNode: frontList) {
				String fStr = fNode.getToken();
				dst.addElement(fStr, true);
			}
		}else {
			for(RegexNode fNode: frontList) {
				for(RegexNode rNode: rearList) {
					String fStr = fNode.getToken();
					String rStr = rNode.getToken();
					
					dst.addElement(fStr + rStr, true);
				}
			}
		}
		
		
		// Remove
		if(front.isGroup() && rear.isElement()) {
			// remove frontList from front
			for(Iterator<RegexNode> iter = frontList.iterator(); iter.hasNext();) {
				RegexNode removal = iter.next();
				
				int idx = front.getChildrenList().indexOf(removal);
				front.getChildrenList().remove(idx);
				front.getRelationList().remove(idx);
				iter.remove();
			}
			
		}else if(rear.isGroup()) {
			// remove rearList from rear
			for(Iterator<RegexNode> iter = rearList.iterator(); iter.hasNext();) {
				RegexNode removal = iter.next();
				
				int idx = rear.getChildrenList().indexOf(removal);
				rear.getChildrenList().remove(idx);
				rear.getRelationList().remove(idx);
				iter.remove();
			}
		}
	}
}


