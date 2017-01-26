package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class execute extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString()
				.equals("<org.apache.http.impl.client.AbstractHttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>")
				|| spb.iie.getMethodRef().toString()
						.equals("<org.apache.http.client.HttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>")
				|| spb.iie.getMethodRef().toString()
						.equals("<org.apache.http.impl.client.DefaultHttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>")
				|| spb.iie.getMethodRef().toString().equals(
						"<org.apache.http.client.HttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest,org.apache.http.protocol.HttpContext)>")
				|| spb.iie.getMethodRef().toString().equals("<android.net.http.AndroidHttpClient: java.lang.Object execute(org.apache.http.client.methods.HttpUriRequest,org.apache.http.client.ResponseHandler)>")
				)
		{
			spb.ub.TrackingReg = spb.iie.getArg(0).toString();
			spb.ub.printUrl(spb.BFTtable, spb.sm, spb.ut);
		}
	}
}
