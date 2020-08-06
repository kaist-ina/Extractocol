
package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import soot.jimple.Constant;

public class appendEncodedPath extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString()
				.equals("<android.net.Uri$Builder: android.net.Uri$Builder appendEncodedPath(java.lang.String)>"))
		{
			/*ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());
			BFNode bfn = new BFNode();
			if (spb.iie.getArg(0) instanceof Constant)
				bfn.makeUrlBfn("/" + spb.iie.getArg(0).toString());
			else
				bfn.makeUrlBfn("/" + spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(0).toString()));
			list.add(bfn);*/
			
			// BK
			
			
		}
	}
}
