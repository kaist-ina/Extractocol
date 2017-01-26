
package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.Basics.Arraysolver;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;
import soot.jimple.Constant;

public class split extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals("<java.lang.String: java.lang.String[] split(java.lang.String)>"))
		{
			if (spb.iie.getArg(0) instanceof Constant)
			{
				String base = spb.BFTtable.get(spb.iie.getBase().toString()).get(0).getStringForUrl();
				String arg0 = spb.iie.getArg(0).toString();
				if (arg0.contains("\""))
					arg0 = arg0.replace("\"", "");
				if (arg0.contains("\\\\"))
					arg0 = arg0.replace("\\\\", "\\");
				ArrayList<BFNode> list = new ArrayList<BFNode>();
				// if we meet heap case, we should find heap first before do
				// split.
				// however, the heap handler is not implemented so that I
				// consider if the base is heapobject, the result of split is .*
				// woohyun 20160419
				if (spb.BFTtable.get(spb.iie.getBase().toString()).get(0).getSootValue() != null && base == null)
				{
					BFNode bfn = new BFNode();
					bfn.makeUrlBfn(".*");
					list.add(bfn);
				}
				else
				{
					String[] returnarray = Arraysolver.splitcustom(base, arg0);
					BFNode bfn = new BFNode();
					bfn.initarray(returnarray.length);
					for (int i = 0; i < returnarray.length; i++)
						bfn.setarray(i, returnarray[i]);
					list.add(bfn);
				}
				spb.BFTtable.put(spb.ub.strDest, list);
			}
			else
			{
				System.out.println("ERROR in ut : " + spb.ut.toString() + "arg0 is not a constant");
			}
		}
	}
}
