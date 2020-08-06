package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;

public class parse extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals("<android.net.Uri: android.net.Uri parse(java.lang.String)>"))
		{
			/*ArrayList<BFNode> tr = spb.BFTtable.get(spb.ub.strDest);
			BFNode bfn = new BFNode();
			bfn.makeUrlBfn(spb.iie.getArg(0).toString());
			tr.add(bfn);
			spb.ub.TrackingReg = spb.ub.strDest;*/
		}
	}
}
