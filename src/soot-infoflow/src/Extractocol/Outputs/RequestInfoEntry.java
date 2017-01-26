
package Extractocol.Outputs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gaurav.tree.LinkedTree;
import com.gaurav.tree.Tree;

import Extractocol.BufferExtractor_Request.HeapManagement.HeapTable;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapTreeNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.UrlBuilder;

import java.util.ArrayList;
import java.util.List;

import Extractocol.Pairing.PairingDPEntry;
import Extractocol.Pairing.SemanticAppliedList;


public class RequestInfoEntry extends CommonInfo
{
	public Tree<HeapTreeNode> HeapTree;
	
	public RequestInfoEntry(){
		this.HeapTree = new LinkedTree<HeapTreeNode>();
	}
	
	public RequestInfoEntry(String _Url, String EP, String DP)
	{
		this.setSignature(_Url);
		this.setEPorSPMethod(EP);
		this.setDPMethod(DP);
		
		this.HeapTree = new LinkedTree<HeapTreeNode>();
	}
	
	public void addHeapTreeRoot()
	{
		HeapTreeNode newHeapNode = new HeapTreeNode();
		if (HeapTree.root() == null)
		{
			SplitEachHeaps(newHeapNode);
			HeapTree.add(newHeapNode);
		}
	}
	
	public Tree<HeapTreeNode> getHeapTree()
	{
		return HeapTree;
	}
	
	private void SplitEachHeaps(HeapTreeNode newHeapNode)
	{
		if (getSignature() != null)
			if (UrlBuilder.hasHeapObject(getSignature())) {
				Pattern p = Pattern.compile("<[a-z.,A-Z0-9,_ :]*>");
				Matcher m = p.matcher(getSignature());
				while (m.find()) {
					newHeapNode.listURL.add(m.group());
				}
			}
	}
	
	public boolean equals(String Url)
	{
		if (getSignature().equals(Url))
			return true;
		else
			return false;
	}
}
