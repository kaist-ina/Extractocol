package extractocol.backend.request.semantic.url.models.staticModels;

import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.outputs.BackendOutput;

public class retrieveStream extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.sie.getMethodRef().toString().indexOf("java.io.InputStream retrieveStream(java.lang.String)>") != -1)
		{
			//spb.ub.printUrl(spb.CurrentPB, spb.BFTtable, spb.sm, spb.ut);
			
			// BK
			spb.CurrentPB.BT().RRI().AddHTTPMethod(spb.ub.isGet ? "GET" : "POST");
			spb.CurrentPB.BT().RRI().SaveURI(spb.CurrentPB, spb.ub.TrackingReg);
		}
	}
}
