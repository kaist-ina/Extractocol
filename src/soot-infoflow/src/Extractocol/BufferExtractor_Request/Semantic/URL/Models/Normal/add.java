
package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import java.util.ArrayList;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class add extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		String method = Constants.Deobfuse(spb.iie.getMethodRef().toString());

		if (method.equals("<java.util.List: boolean add(java.lang.Object)>"))
		{
			String arg = spb.iie.getArg(0).toString();
			String base = spb.iie.getBase().toString();
			spb.BFTtable.put(base, spb.ub.CopyList(spb.BFTtable.get(arg)));
		}
		else
		{
			if (method.equals("<com.android.volley.RequestQueue: com.android.volley.Request add(com.android.volley.Request)>"))
			{
				spb.ub.TrackingReg = spb.iie.getArg(0).toString();

				if (Constants.VolleyUrlCandidate != null && !Constants.VolleyUrlCandidate.equals(""))
				{
					ArrayList<BFNode> list = new ArrayList<BFNode>();
					BFNode bfn = new BFNode();
					bfn.makeUrlBfn(Constants.VolleyUrlCandidate);
					list.add(bfn);
					spb.BFTtable.put("Volley_result", list);
					spb.ub.TrackingReg = "Volley_result";
				}

				spb.ub.printUrl(spb.BFTtable, spb.sm, spb.ut);
			}
		}
	}
}
