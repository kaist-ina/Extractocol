
package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Static;

import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class addqueryparameters extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.sie.getMethodRef().toString().contains(
				"<oauth.signpost.OAuth: java.lang.String addQueryParameters(java.lang.String,java.lang.String[])>"))
		{
			BFNode arraybfn = spb.BFTtable.get(spb.sie.getArg(1).toString()).get(0);
			String url = spb.ub.GenRegex(null, spb.BFTtable, spb.sie.getArg(0).toString()) + "?";
			int index = 0;
			String query = "";
			while (true)
			{
				if (!BFNode.isArray(arraybfn) || index + 1 >= arraybfn.getSize())
					break;
				String key = arraybfn.getarraystring(index);
				String value = arraybfn.getarraystring(index + 1);
				if (index != 0)
					query += "&" + key + "=" + value;
				else
					query += key + "=" + value;
				index = index + 2;
			}
			url += query.replace("\"", "");
			BFNode bfn = new BFNode();
			bfn.makeUrlBfn(url);
			ArrayList<BFNode> list = new ArrayList<BFNode>();
			list.add(bfn);
			spb.BFTtable.put(spb.ub.strDest, list);
		}
		else
		{
			if (spb.sie.getMethodRef().toString().contains(
					"<oauth.signpost.OAuth: java.lang.String addQueryParameters(java.lang.String,java.util.Map)>"))
			{
				String url = spb.ub.GenRegex(null, spb.BFTtable, spb.sie.getArg(0).toString()) + "?";
				ArrayList<BFNode> list = new ArrayList<BFNode>();
				BFNode parambfn = new BFNode();
				parambfn.makeUrlParamsBfn(spb.BFTtable.get(spb.sie.getArg(1).toString()));
				if (parambfn.getStringForUrl() != null)
					url = url + parambfn.getStringForUrl();
				parambfn.setStringForUrl(url);
				list.add(parambfn);
				spb.BFTtable.put(spb.ub.strDest, list);
			}
		}
	}
}
