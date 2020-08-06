package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.List;

import extractocol.Constants;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.outputs.BackendOutput;
import soot.toDex.instructions.SparseSwitchPayload;

public class toEntity extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		//wish application's specific semantic model 
		if (spb.iie.getMethodRef().getSignature().equals("<com.contextlogic.wish.http.HttpRequestParams: ch.boye.httpclientandroidlib.HttpEntity toEntity()>"))
		{
			/*boolean success = BackendOutput.reqRespInfo.reqie.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
						
			if(!success)
			{
				//private static final List<String> third_party_header_field" should be updated
			}*/
		}
	}
}
