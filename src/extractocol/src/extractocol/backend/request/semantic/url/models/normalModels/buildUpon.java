package extractocol.backend.request.semantic.url.models.normalModels;

import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;

public class buildUpon extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().getSignature().equals("<android.net.Uri: android.net.Uri$Builder buildUpon()>"))
		{
			/*spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getBase().toString())));
			spb.ub.TrackingReg = spb.ub.strDest;*/
		}
	}
}
