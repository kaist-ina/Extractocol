
package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class getURI extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		// JM Temporarily
		if (spb.iie.getMethodRef().getSignature()
				.equals("<com.linkedin.android.client.MobileClient: java.net.URI getURI(java.lang.String)>"))
		{
			String url = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(0).toString());
			ArrayList<BFNode> list = new ArrayList<BFNode>();
			BFNode bfn = new BFNode();
			bfn.makeUrlBfn(url);
			list.add(bfn);
			spb.BFTtable.put(spb.ub.strDest, list);
			spb.ub.TrackingReg = spb.ub.strDest;
			spb.ub.printUrl(spb.BFTtable, spb.sm, spb.ut);
		}
	}
}
