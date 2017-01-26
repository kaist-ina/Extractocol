package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Static;

import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class replace extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		ArrayList<BFNode> dtr = spb.BFTtable.get(spb.ub.strDest);
		if (dtr != null)
			dtr = spb.ub.CopyList(spb.BFTtable.get(spb.sie.getArg(0).toString()));
	}
}
