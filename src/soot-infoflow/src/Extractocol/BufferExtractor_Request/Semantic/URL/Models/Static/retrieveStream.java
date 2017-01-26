package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Static;

import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class retrieveStream extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.sie.getMethodRef().toString().indexOf("java.io.InputStream retrieveStream(java.lang.String)>") != -1)
		{
			spb.ub.printUrl(spb.BFTtable, spb.sm, spb.ut);
		}
	}
}
