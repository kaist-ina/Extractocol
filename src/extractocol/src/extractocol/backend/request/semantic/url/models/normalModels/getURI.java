
package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.outputs.BackendOutput;

public class getURI extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		// JM Temporarily
		if (spb.iie.getMethodRef().getSignature()
				.equals("<com.linkedin.android.client.MobileClient: java.net.URI getURI(java.lang.String)>"))
		{
			/*String url = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(0).toString());
			ArrayList<BFNode> list = new ArrayList<BFNode>();
			BFNode bfn = new BFNode();
			bfn.makeUrlBfn(url);
			list.add(bfn);
			spb.BFTtable.put(spb.ub.strDest, list);
			spb.ub.TrackingReg = spb.ub.strDest;*/
			
			// BK
			String url = spb.CurrentPB.varTable.GenRegex(spb.iie.getArg(0).toString());
			spb.CurrentPB.varTable.setConstantValue(spb.ub.strDest, url, false);
			//Constants.BFTResultAlreadyApplied = true;
			
			//spb.ub.printUrl(spb.CurrentPB, spb.BFTtable, spb.sm, spb.ut);
			
			// BK
			spb.CurrentPB.BT().RRI().AddHTTPMethod(spb.ub.isGet ? "GET" : "POST");
			spb.CurrentPB.BT().RRI().SaveURI(spb.CurrentPB, spb.iie.getArg(0).toString());
		}
	}
}
