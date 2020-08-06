package extractocol.backend.request.semantic.url.models.normalModels;

import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.outputs.BackendOutput;

public class open extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals("<android.content.res.AssetManager: java.io.InputStream open(java.lang.String)>"))
		{
			/*spb.ub.TrackingReg = spb.iie.getArg(0).toString();
			// Track = true;
			spb.ub.isGet = true;
			spb.ub.printUrl(spb.CurrentPB, spb.BFTtable, spb.sm, spb.ut);*/
			
			// BK
			spb.CurrentPB.BT().RRI().AddHTTPMethod(spb.ub.isGet? "GET" : "POST");
			spb.CurrentPB.BT().RRI().SaveURI(spb.CurrentPB, spb.iie.getArg(0).toString());
		}
	}
}
