
package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.valueEntry.ValueEntryTable;
import soot.jimple.Constant;

public class replaceFirst extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString()
				.equals("<java.lang.String: java.lang.String replaceFirst(java.lang.String,java.lang.String)>"))
		{
			if (spb.iie.getArg(0) instanceof Constant && spb.iie.getArg(1) instanceof Constant)
			{
				String arg0 = ValueEntryTable.getRefinedConstant(spb.iie.getArg(0).toString());
				String arg1 = ValueEntryTable.getRefinedConstant(spb.iie.getArg(1).toString());
				String base = spb.iie.getBase().toString();
				/*String result = spb.ub.GenRegex(null, spb.BFTtable, base);
				result = result.replaceFirst(arg0, arg1);
				ArrayList<BFNode> list = new ArrayList<BFNode>();
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(result);
				list.add(bfn);
				spb.BFTtable.put(spb.ub.strDest, list);*/
				
				// BK
				String result = spb.CurrentPB.varTable.GenRegex(base);
				result = result.replaceFirst(arg0, arg1);
				spb.CurrentPB.varTable.setConstantValue(spb.ub.strDest, result, false);
				//Constants.BFTResultAlreadyApplied = true;
			}
		}
	}
}
