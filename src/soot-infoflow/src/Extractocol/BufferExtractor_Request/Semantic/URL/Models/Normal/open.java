package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class open extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals("<android.content.res.AssetManager: java.io.InputStream open(java.lang.String)>"))
		{
			spb.ub.TrackingReg = spb.iie.getArg(0).toString();
			// Track = true;
			spb.ub.isGet = true;
			spb.ub.printUrl(spb.BFTtable, spb.sm, spb.ut);
		}
	}
}
