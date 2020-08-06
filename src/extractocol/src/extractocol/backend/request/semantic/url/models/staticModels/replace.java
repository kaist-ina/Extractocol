package extractocol.backend.request.semantic.url.models.staticModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;

public class replace extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		/*ArrayList<BFNode> dtr = spb.BFTtable.get(spb.ub.strDest);
		if (dtr != null)
			dtr = spb.ub.CopyList(spb.BFTtable.get(spb.sie.getArg(0).toString()));*/
		
		spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, spb.sie.getArg(0).toString() , false);
		//Constants.BFTResultAlreadyApplied = true;
	}
}
