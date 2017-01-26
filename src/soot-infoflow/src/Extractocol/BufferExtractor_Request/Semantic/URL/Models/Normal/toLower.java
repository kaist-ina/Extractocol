package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class toLower extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().getSignature().equals("<java.lang.String: java.lang.String toLowerCase(java.util.Locale)>"))
		{
			spb.BFTtable.put(spb.strDst, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getBase().toString())));
		}
	}
}
