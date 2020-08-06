
package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;

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
