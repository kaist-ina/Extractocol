package extractocol.backend.request.semantic.url.models.normalModels;

import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;

public class getHost extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().getSignature().equals("<java.net.URI: java.lang.String getHost()>"))
		{
			// JM should be implemented
		}
	}
}
