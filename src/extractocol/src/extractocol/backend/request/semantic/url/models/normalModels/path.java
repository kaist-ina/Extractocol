package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;

public class path extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().getSignature().equals("<android.net.Uri$Builder: android.net.Uri$Builder path(java.lang.String)>"))
		{
			/*ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());

			// Adding path value to baselist
			if(spb.BFTtable.get(spb.iie.getArg(0).toString()) != null)
				list.addAll(spb.BFTtable.get(spb.iie.getArg(0).toString()));

			// Adding path value to strDest
			spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(list));*/
		}
	}
}
