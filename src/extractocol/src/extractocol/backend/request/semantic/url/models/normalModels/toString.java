package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;

public class toString extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		/*ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());
		spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(list));*/
		
		// Added by BK
		//spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, spb.iie.getBase().toString(), false);
		spb.CurrentPB.varTable.setConstantValue(spb.ub.strDest, spb.CurrentPB.varTable.GenRegex(spb.iie.getBase().toString()), false);
		//Constants.BFTResultAlreadyApplied = true;
	}
}
