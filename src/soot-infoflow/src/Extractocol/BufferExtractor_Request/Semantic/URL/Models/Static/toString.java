package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Static;

import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class toString extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		ArrayList<BFNode> list1 = new ArrayList<BFNode>();
		BFNode bfn1 = new BFNode();
		bfn1.makeUrlBfn(spb.ub.GenRegex(null, spb.BFTtable, spb.sie.getArg(0).toString()));
		list1.add(bfn1);
		spb.BFTtable.put(spb.ub.strDest, list1);		
	}
}
