package extractocol.backend.request.semantic.url.models.normalModels;

import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.outputs.BackendOutput;

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
			/*spb.ub.TrackingReg = spb.iie.getArg(0).toString();
			spb.ub.printUrl(spb.CurrentPB, spb.BFTtable, spb.sm, spb.ut);*/
			
			// BK 
			// TODO: need to track URI before meeting this statement
			spb.CurrentPB.BT().RRI().AddHTTPMethod(spb.ub.isGet ? "GET" : "POST");
			spb.CurrentPB.BT().RRI().SaveURI(spb.CurrentPB, spb.iie.getArg(0).toString());
		}
		else if (spb.iie.getMethodRef().toString().equals(("<org.apache.http.impl.client.DefaultHttpClient: org.apache.http.HttpResponse execute(org.apache.http.HttpHost,org.apache.http.HttpRequest)>")))
		{
			/*spb.ub.TrackingReg = spb.iie.getArg(1).toString();
			spb.ub.printUrl(spb.CurrentPB, spb.BFTtable, spb.sm, spb.ut);*/

			// BK
			// TODO: need to track URI before meeting this statement
			spb.CurrentPB.BT().RRI().AddHTTPMethod(spb.ub.isGet ? "GET" : "POST");
			spb.CurrentPB.BT().RRI().SaveURI(spb.CurrentPB, spb.iie.getArg(1).toString());
		}
	}
}
