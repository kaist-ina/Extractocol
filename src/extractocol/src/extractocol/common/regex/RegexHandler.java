package extractocol.common.regex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import extractocol.common.regex.basic.ChildrenNode;
import extractocol.common.regex.basic.RegexNode;
import extractocol.common.regex.basic.ChildrenNode.RELATION_TYPE;
import extractocol.common.regex.basic.RegexNode.NODE_TYPE;
import extractocol.tester.HeapHandling;

public class RegexHandler {
	//public static String indiP = "<[0-9]*>";
	//public static Pattern indiPattern = Pattern.compile(indiP);
	
	public static ArrayList<String> M(String regex){
		ArrayList<String> strList = new ArrayList<String>();
		
		RegexNode root = RegexToTree(regex);
		Refinement(root);
		
		for(RegexNode rn: root.getChildrenList()) {
			if(!rn.isElement())
				continue;
					
			strList.add(rn.getToken());
		}
		
		return strList;
	}
	
	public static String Refinement(String regex) {
		ArrayList<String> strList = M(regex);
		String out = "";
		
		for(int i = 0; i < strList.size(); i++) {
			if(i == strList.size() - 1)
				out += strList.get(i);
			else
				out += strList.get(i) + "|";
		}
			 
		return out;
	}
	
	public static ArrayList<String> RemoveQuery(ArrayList<String> regexList){
		ArrayList<String> result = new ArrayList<String>();
		for(String regex: regexList)
			result.add(regex.split("[\\?\\&]")[0]);
		return result;
	}
	
	private static RegexNode RegexToTree(String regex) {
		if(regex == null)
			return null;
		
		RegexNode root = new RegexNode(NODE_TYPE.ROOT);
		RegexNode curr = null;
		RegexNode parent = root;
		boolean or = false;
		boolean backSlash = false;
		
		for(int i = 0; i < regex.length(); i++) {
			switch(regex.charAt(i)) {
			case '(':
				if(backSlash) {
					if(curr == null) {
						curr = new RegexNode(NODE_TYPE.ELEMENT, parent);
						parent.addChild(curr, or);
					}
						
					curr.addToken(regex.charAt(i));
					
					or = false;
					backSlash = false;
				}else {
					curr = new RegexNode(NODE_TYPE.GROUP, parent);
					parent.addChild(curr, or);
					parent = curr;
					curr = null;
					or = false;
				}
				break;
				
			case ')':
				if(backSlash) {
					if(curr == null) {
						curr = new RegexNode(NODE_TYPE.ELEMENT, parent);
						parent.addChild(curr, or);
					}
						
					curr.addToken(regex.charAt(i));
					
					or = false;
					backSlash = false;
				}else {
					if(regex.charAt(i-1) == '|') {
						curr = new RegexNode(NODE_TYPE.ELEMENT, parent);
						parent.addChild(curr, or);
					}
					
					parent = parent.getParent();
					curr = null;
					or = false;
				}
				break;
				
			case '|':
				if(backSlash) {
					if(curr == null) {
						curr = new RegexNode(NODE_TYPE.ELEMENT, parent);
						parent.addChild(curr, or);
					}
						
					curr.addToken(regex.charAt(i));
					
					or = false;
					backSlash = false;
				}else {
					if(i == 0 || regex.charAt(i-1) == '|' || regex.charAt(i-1) == '(') {
						curr = new RegexNode(NODE_TYPE.ELEMENT, parent);
						parent.addChild(curr, or);
					}
					
					curr = null;
					or = true;
				}
				break;
				
			case '\\':
				backSlash = true;
				break;
				
			default:
				if(curr == null) {
					curr = new RegexNode(NODE_TYPE.ELEMENT, parent);
					parent.addChild(curr, or);
				}
					
				curr.addToken(regex.charAt(i));
				
				or = false;
				break;
				
			}
		}
		
		return root;
	}
	
	private static void Refinement(RegexNode root) {
		List<RegexNode> targets = new ArrayList<RegexNode>();
		/** 0. Initial refinement **/
		for(RegexNode rn: root.getChildrenList()) {
			if(rn.getType() != NODE_TYPE.GROUP)
				continue;
			
			if(!rn.doesItContainIndicator())
				continue;
			
			for(Iterator<RegexNode> iter = rn.getChildrenList().iterator(); iter.hasNext();) {
				RegexNode child = iter.next();
				int idx = rn.getChildrenList().indexOf(child);
				
				Matcher m = HeapHandling.heapPattern.matcher(child.getToken());
				if(!m.find()) {
					if(rn.getChildrenList().getLast() == child)
						rn.getRelationList().removeLast();
					else
						rn.getRelationList().remove(idx);
					iter.remove();
				}
			}
		}
		
		/** 1. Handle groups **/
		for(int i = 0; i < root.getChildrenList().size(); i++) {
			RegexNode node = root.getChildrenList().get(i);
			
			if(node.getType() != NODE_TYPE.GROUP)
				continue;
		
			// (1) call refinement() recursively if childNode contains group node(s)
			if(node.doesContainGroup())
				Refinement(node);
			
			// (2) get children of group node and put them to child list of this node at this position if both front and rear relations of this group node are OR
			// rear relation
			if( (i != root.getChildrenList().size() - 1) && (root.getRelationList().get(i) != RELATION_TYPE.OR) ) 
				continue;
			
			// front relation
			if(i != 0 && (root.getRelationList().get(i-1) != RELATION_TYPE.OR))
				continue;
					
			targets.add(node);
		}
		 
		for(RegexNode target: targets) {
			int currIdx = root.getChildrenList().indexOf(target);
			root.getChildrenList().addAll(currIdx, target.getChildrenList());
			root.getRelationList().addAll(currIdx, target.getRelationList());
			
			root.getChildrenList().remove(target);
		}
		
		targets.clear();
		
		
		/** 2. Handle AND relation **/
		for(Iterator<RELATION_TYPE> iter = root.getRelationList().iterator(); iter.hasNext();) {
			RELATION_TYPE currRT = iter.next();
			int idx = root.getRelationList().indexOf(currRT);
			
			if(currRT == RELATION_TYPE.OR)
				continue;
			
			RegexNode front = root.getChildrenList().get(idx);
			RegexNode rear  = root.getChildrenList().get(idx + 1);
			
			RegexNode.Merge(front, rear);
			if(rear.isElement()) {
				root.getChildrenList().remove(rear);
			}else if(rear.isGroup()) {
				root.getChildrenList().remove(front);
			}
			
			iter.remove();
		}
		
		/** 3. Take out the children of the group node if it has a group node **/
		if(root.doesContainGroup()) {
			// If reach here, only a single group left
			for(int i = 0; i < root.getChildrenList().size(); i++) {
				RegexNode node = root.getChildrenList().get(i);
				
				if(!node.isGroup())
					continue;
				
				targets.add(node);
			}
			 
			for(RegexNode target: targets) {
				int currIdx = root.getChildrenList().indexOf(target);
				root.getChildrenList().addAll(currIdx, target.getChildrenList());
				root.getRelationList().addAll(currIdx, target.getRelationList());
				
				root.getChildrenList().remove(target);
			}
			
			targets.clear();
			
			/*RegexNode g = root.getChildrenList().get(0);
			root.getChildrenList().addAll(g.getChildrenList());
			root.getRelationList().addAll(g.getRelationList());
			
			root.getChildrenList().remove(g);*/
		}
		
		/** 4. Perform De-duplication & Remove unnecessary nodes **/
		boolean hasIndicator = root.doesItContainIndicator();
		for(Iterator<RegexNode> iter = root.getChildrenList().iterator(); iter.hasNext();) {
			RegexNode child = iter.next();
			int idx = root.getChildrenList().indexOf(child);
			
			// Remove unnecessary nodes
			if(hasIndicator) {
				Matcher m = HeapHandling.heapPattern.matcher(child.getToken());
				if(!m.find()) {
					if(root.getChildrenList().getLast() == child)
						root.getRelationList().removeLast();
					else
						root.getRelationList().remove(idx);
					iter.remove();
					continue;
				}
			}
			
			// Perform de-duplication
			if(root.hasSameTokenChildWith(child)) {
				if(root.getChildrenList().getLast() == child)
					root.getRelationList().removeLast();
				else
					root.getRelationList().remove(idx);
				iter.remove();
			}
		}
		
		/** 5. Final de-duplication **/
		// All the remaining nodes are ELEMENT and all the relations are OR here.
		for(Iterator<RegexNode> iter = root.getChildrenList().iterator(); iter.hasNext();) {
			RegexNode child = iter.next();
			int idx = root.getChildrenList().indexOf(child);
			
			// Perform final de-duplication
			if(root.hasChildRegexMatchingWith(child)) {
				if(root.getChildrenList().getLast() == child)
					root.getRelationList().removeLast();
				else
					root.getRelationList().remove(idx);
				iter.remove();
			}
		}
	}
}
