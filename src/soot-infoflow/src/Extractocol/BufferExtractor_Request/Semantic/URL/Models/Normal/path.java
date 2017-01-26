package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class path extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().getSignature().equals("<android.net.Uri$Builder: android.net.Uri$Builder path(java.lang.String)>"))
		{
			ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());

			// Adding path value to baselist
			list.addAll(spb.BFTtable.get(spb.iie.getArg(0).toString()));

			// Adding path value to strDest
			spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(list));
		}
	}
}
