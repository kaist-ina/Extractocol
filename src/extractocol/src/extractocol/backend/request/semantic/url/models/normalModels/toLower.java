package extractocol.backend.request.semantic.url.models.normalModels;

import extractocol.Constants;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;

public class toLower extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().getSignature().equals("<java.lang.String: java.lang.String toLowerCase(java.util.Locale)>"))
		{
			//spb.BFTtable.put(spb.strDst, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getBase().toString())));
			
			// BK
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, spb.iie.getBase().toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		}
	}
}
