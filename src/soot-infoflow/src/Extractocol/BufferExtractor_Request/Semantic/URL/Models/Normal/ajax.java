package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import java.util.ArrayList;
import java.util.List;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapHandler;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class ajax extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().getSignature()
				.equals("<com.external.androidquery.a: com.external.androidquery.b ajax(com.external.androidquery.b.d)>"))
		{
			spb.ub.TrackingReg = spb.iie.getArg(0).toString();
			// Concat Base Namespace + url and params.
			// String urlKey = "<" + iie.getArg(0).getType().toString() + ":
			// " + "java.lang.String url>";
			String urlKey = "<com.external.androidquery.b.a: java.lang.String url>";
			String paramKey = "<" + spb.iie.getArg(0).getType().toString() + ": java.util.Map params>";
			String paramString = "";

			// JM param is extended
			if (!HeapHandler.GlobalBFTtable.containsKey(paramKey))
			{
				List<String> sups = Constants.sCFG.getSuperclassOf(spb.iie.getArg(0).getType().toString());

				for (String a : sups)
				{
					if (HeapHandler.GlobalBFTtable.containsKey("<" + a + ": java.util.Map params>"))
					{
						paramKey = "<" + a + ": java.util.Map params>";
						break;
					}
				}
			}

			// JM url is extended
			if (!HeapHandler.GlobalHeapTable.containsKey(urlKey))
			{
				List<String> sups = Constants.sCFG.getSuperclassOf(spb.iie.getArg(0).getType().toString());

				for (String a : sups)
				{
					if (HeapHandler.GlobalHeapTable.containsKey("<" + a + ": " + "java.lang.String url>"))
					{
						urlKey = "<" + a + ": java.lang.String url>";
						break;
					}
				}
			}

			// bFTtable.put(iie.getArg(0).toString(), bFTtable.get(url));

			if (HeapHandler.GlobalBFTtable.containsKey(paramKey))
			{
				for (BFNode bfn : HeapHandler.GlobalBFTtable.get(paramKey))
				{
					if (bfn.getStringForUrl() != null)
					{
						if (paramString.length() == 0)
							paramString = "?" + bfn.getStringForUrl().replaceAll("\"", "");
						else
							paramString += "&" + bfn.getStringForUrl().replaceAll("\"", "");
					}
				}
			}

			ArrayList<BFNode> list = new ArrayList<BFNode>();

			BFNode bfn = new BFNode();
			bfn.setSootValue(urlKey);
			bfn.setHeapObject(true);
			list.add(bfn);

			if (paramString.length() > 0)
			{
				bfn = new BFNode();
				bfn.makeUrlBfn(paramString);
				list.add(bfn);
			}
			spb.BFTtable.put(spb.ub.TrackingReg, list);
			spb.ub.printUrl(spb.BFTtable, spb.sm, spb.ut);
		}
	}
}
