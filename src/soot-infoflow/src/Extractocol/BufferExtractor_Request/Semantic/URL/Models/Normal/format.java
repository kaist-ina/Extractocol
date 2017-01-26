
package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class format extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef()
				.toString() == "<org.apache.http.client.utils.URLEncodedUtils: java.lang.String format(java.util.List,java.lang.String)>")
		{
			spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getArg(0).toString())));
		}
		else
		{
			if (spb.iie.getMethodRef().toString()
					.equals("<java.lang.String: java.lang.String format(java.lang.String,java.lang.Object[])>"))
			{
				ArrayList<BFNode> tr = spb.BFTtable.get(spb.ub.strDest);
				if (tr != null)
				{
					BFNode bfn = new BFNode();
					String arg0 = spb.iie.getArg(0).toString();
					bfn.makeUrlBfn(arg0);
				}
			}
		}
	}
}
