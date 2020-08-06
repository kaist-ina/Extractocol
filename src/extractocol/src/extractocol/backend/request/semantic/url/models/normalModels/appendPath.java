
package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;

public class appendPath extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString()
				.equals("<android.net.Uri$Builder: android.net.Uri$Builder appendPath(java.lang.String)>"))
		{
			/*ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());
			if (spb.BFTtable.get(spb.iie.getArg(0).toString()) != null)
			{
				list.addAll(spb.BFTtable.get(spb.iie.getArg(0).toString()));
			}
			spb.ub.TrackingReg = spb.iie.getBase().toString();*/
		}
	}
}
