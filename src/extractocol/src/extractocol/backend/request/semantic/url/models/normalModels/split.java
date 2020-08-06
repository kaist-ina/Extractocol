
package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.Arraysolver;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import soot.jimple.Constant;

public class split extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals("<java.lang.String: java.lang.String[] split(java.lang.String)>"))
		{
			/*if (spb.iie.getArg(0) instanceof Constant)
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
			}*/
			
			// BK
			String arg0;
			String baseString = spb.CurrentPB.varTable.GenRegex(spb.iie.getBase().toString());
			if (spb.iie.getArg(0) instanceof Constant)
				arg0 = spb.iie.getArg(0).toString().substring(1, spb.iie.getArg(0).toString().length() - 1);
			else
				arg0 = spb.CurrentPB.varTable.GenRegex(spb.iie.getArg(0).toString());
			
			String[] res = baseString.split(arg0);
			spb.CurrentPB.varTable.setArrayValue(spb.ub.strDest, res);
		}
	}
}
