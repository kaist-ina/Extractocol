package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class build extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().getSignature().equals("<android.net.Uri$Builder: android.net.Uri build()>"))
		{
			spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getBase().toString())));
			spb.ub.TrackingReg = spb.ub.strDest;
		}
	}
}
