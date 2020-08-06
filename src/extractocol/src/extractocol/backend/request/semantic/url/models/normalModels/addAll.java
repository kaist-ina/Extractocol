package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;

public class addAll extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals("<java.util.List: boolean addAll(java.util.Collection)>"))
		{
			/*ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getArg(0).toString());
			ArrayList<BFNode> dlist = spb.BFTtable.get(spb.iie.getBase().toString());
			for (BFNode bfn : list)
			{
				dlist.add(bfn);
			}*/
			
			// BK
			String s = spb.CurrentPB.varTable.GenRegex(spb.iie.getArg(0).toString());
			spb.CurrentPB.varTable.addListValue(spb.iie.getBase().toString(), s, true);
			
			//spb.CurrentPB.varTable.addValueEntryList(spb.iie.getBase().toString(), spb.iie.getArg(0).toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		}
	}
}
