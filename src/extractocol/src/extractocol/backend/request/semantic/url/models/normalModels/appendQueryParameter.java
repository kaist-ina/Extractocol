
package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import soot.jimple.Constant;

public class appendQueryParameter extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals(
				"<android.net.Uri$Builder: android.net.Uri$Builder appendQueryParameter(java.lang.String,java.lang.String)>"))
		{
			/*if (!(spb.iie.getArg(1) instanceof Constant) && spb.BFTtable.get(spb.iie.getArg(1).toString()) != null
					&& spb.BFTtable.get(spb.iie.getArg(1).toString()).size() > 0
					&& spb.BFTtable.get(spb.iie.getArg(1).toString()).get(0).getSootValue() != null
					&& spb.BFTtable.get(spb.iie.getArg(1).toString()).get(0).getSootValue().contains("Map"))
			{
				ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());
				list.addAll(spb.ub.CopyList(spb.BFTtable.get(spb.iie.getArg(1).toString())));
			}
			else
			{
				ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());
				BFNode bfn = new BFNode();
				String param = "?";
				if (Constants.isFirstParam)
					Constants.isFirstParam = false;
				else
					if (!Constants.isFirstParam)
						param = "&";
				String key = spb.iie.getArg(0).toString();
				String value = "";
				if (spb.iie.getArg(1) instanceof Constant)
				{
					value = spb.iie.getArg(1).toString().replaceAll("\"", "");
				}
				else
				{
					value = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(1).toString());
				}
				bfn.setStringForUrl(param + key + "=" + (value != "" ? value : "(.*)"));
				list.add(bfn);
			}*/
		}
	}
}
