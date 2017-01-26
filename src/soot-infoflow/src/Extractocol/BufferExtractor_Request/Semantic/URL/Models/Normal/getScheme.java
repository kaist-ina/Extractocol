
package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class getScheme extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().getSignature().equals("<java.net.URI: java.lang.String getScheme()>"))
		{
			String base = spb.iie.getBase().toString();
			String url = spb.ub.GenRegex(null, spb.BFTtable, base);
			String scheme = "";
			if (url.startsWith("http:"))
			{
				scheme = "http";
			}
			else
			{
				if (url.startsWith("https:"))
				{
					scheme = "https";
				}
				else
				{
					scheme = "http";
				}
			}
			BFNode bfn = new BFNode();
			bfn.makeUrlBfn(scheme);
			ArrayList<BFNode> list = new ArrayList<BFNode>();
			list.add(bfn);
			spb.BFTtable.put(spb.ub.strDest, list);
		}
	}
}
