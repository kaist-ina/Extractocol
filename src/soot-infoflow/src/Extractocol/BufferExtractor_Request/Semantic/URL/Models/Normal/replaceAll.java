
package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;
import soot.jimple.Constant;

public class replaceAll extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		//Duong
		if (spb.iie.getMethodRef().toString()
				.equals("<java.lang.String: java.lang.String replaceAll(java.lang.String,java.lang.String)>"))
		{
			if (spb.iie.getArg(0) instanceof Constant && spb.iie.getArg(1) instanceof Constant)
			{
				String arg0 = spb.iie.getArg(0).toString();
				String arg1 = spb.iie.getArg(1).toString();
				String base = spb.iie.getBase().toString();
				String result = spb.ub.GenRegex(null, spb.BFTtable, base);
				result = result.replaceAll(arg0, arg1);
				ArrayList<BFNode> list = new ArrayList<BFNode>();
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(result);
				list.add(bfn);
				spb.BFTtable.put(spb.ub.strDest, list);
			}
			else if (spb.iie.getArg(0) instanceof Constant)
			{
				String arg0 = spb.iie.getArg(0).toString().replaceAll("\"", "");
				String arg1 = spb.iie.getArg(1).toString();
				String base = spb.iie.getBase().toString();
				String result = spb.ub.GenRegex(null, spb.BFTtable, base);
				arg1 = spb.ub.GenRegex(null, spb.BFTtable, arg1);
				result = result.replaceAll(arg0, arg1);
				ArrayList<BFNode> list = new ArrayList<BFNode>();
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(result);
				list.add(bfn);
				spb.BFTtable.put(spb.ub.strDest, list);
			}
		}
	}
}
