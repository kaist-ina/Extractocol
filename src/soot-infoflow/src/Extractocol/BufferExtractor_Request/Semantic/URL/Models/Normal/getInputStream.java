package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class getInputStream extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().getSignature().equals("<java.net.HttpURLConnection: java.io.InputStream getInputStream()>"))
		{
			spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getBase().toString())));
			spb.ub.TrackingReg = spb.ub.strDest;
			spb.ub.printUrl(spb.BFTtable, spb.sm, spb.ut);
		}
	}
}
