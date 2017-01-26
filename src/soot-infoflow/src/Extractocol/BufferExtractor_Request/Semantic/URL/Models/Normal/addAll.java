package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class addAll extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals("<java.util.List: boolean addAll(java.util.Collection)>"))
		{
			ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getArg(0).toString());
			ArrayList<BFNode> dlist = spb.BFTtable.get(spb.iie.getBase().toString());
			for (BFNode bfn : list)
			{
				dlist.add(bfn);
			}
		}
	}
}
