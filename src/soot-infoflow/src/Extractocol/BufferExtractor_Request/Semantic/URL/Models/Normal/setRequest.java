package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class setRequest extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals("<java.net.HttpURLConnection: void setRequestMethod(java.lang.String)>"))
		{
			if (spb.iie.getArg(0).toString().toLowerCase().replaceAll("\"", "").equals("post"))
			{
				spb.ub.isGet = false;
			} else
				spb.ub.isGet = true;
		}
	}
}
