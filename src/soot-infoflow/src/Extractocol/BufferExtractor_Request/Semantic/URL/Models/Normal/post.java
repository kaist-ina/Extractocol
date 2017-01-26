package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class post extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().getSignature().equals("<com.contextlogic.wish.http.WishHttpClient: void post(com.contextlogic.wish.http.WishHttpClient$RequestPool,java.lang.Object,java.lang.String,com.contextlogic.wish.http.HttpRequestParams,com.contextlogic.wish.http.HttpResponseHandler)>"))
		{
			spb.ub.TrackingReg = spb.iie.getArg(2).toString();
			spb.ub.printUrl(spb.BFTtable, spb.sm, spb.ut);
			// JM Wish Case (Temporarily)
			spb.ub.printParam();
		}
	}
}
