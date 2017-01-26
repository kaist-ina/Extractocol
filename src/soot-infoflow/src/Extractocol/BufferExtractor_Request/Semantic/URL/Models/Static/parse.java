package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Static;

import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;
import soot.jimple.Constant;

public class parse extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.sie.getMethodRef().toString().equals("<android.net.Uri: android.net.Uri parse(java.lang.String)>"))
		{
			if (spb.sie.getArg(0) instanceof Constant)
			{
				ArrayList<BFNode> tr = new ArrayList<BFNode>();
				BFNode bfn = new BFNode();
				bfn.setStringForUrl(spb.sie.getArg(0).toString());
				tr.add(bfn);
				spb.BFTtable.put(spb.ub.strDest, tr);

			} else
			{
				spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(spb.BFTtable.get(spb.sie.getArg(0).toString())));
			}
			spb.ub.TrackingReg = spb.ub.strDest;
		}
	}
}
