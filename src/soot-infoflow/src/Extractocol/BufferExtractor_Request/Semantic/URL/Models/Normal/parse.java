package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class parse extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals("<android.net.Uri: android.net.Uri parse(java.lang.String)>"))
		{
			ArrayList<BFNode> tr = spb.BFTtable.get(spb.ub.strDest);
			BFNode bfn = new BFNode();
			bfn.makeUrlBfn(spb.iie.getArg(0).toString());
			tr.add(bfn);
			spb.ub.TrackingReg = spb.ub.strDest;
		}
	}
}
