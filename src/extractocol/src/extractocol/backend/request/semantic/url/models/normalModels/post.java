package extractocol.backend.request.semantic.url.models.normalModels;

import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.outputs.BackendOutput;

public class post extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		// Not used any more
		if (spb.iie.getMethodRef().getSignature().equals("<com.contextlogic.wish.http.WishHttpClient: void post(com.contextlogic.wish.http.WishHttpClient$RequestPool,java.lang.Object,java.lang.String,com.contextlogic.wish.http.HttpRequestParams,com.contextlogic.wish.http.HttpResponseHandler)>"))
		{
			/*spb.ub.TrackingReg = spb.iie.getArg(2).toString();
			spb.ub.printUrl(spb.CurrentPB, spb.BFTtable, spb.sm, spb.ut);*/
			// JM Wish Case (Temporarily)
			//spb.ub.printParam();
			
			// BK
			spb.CurrentPB.BT().RRI().AddHTTPMethod(spb.ub.isGet? "GET" : "POST");
			spb.CurrentPB.BT().RRI().SaveURI(spb.CurrentPB, spb.iie.getArg(2).toString());
		}
	}
}
