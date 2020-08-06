package extractocol.backend.request.semantic.url.models.normalModels;

import extractocol.Constants;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;

public class entrySet extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals("<java.util.concurrent.ConcurrentHashMap: java.util.Set entrySet()>")
				|| spb.iie.getMethodRef().toString().equals("<java.util.Set: java.util.Iterator iterator()>")
				|| spb.iie.getMethodRef().toString().equals("<java.util.Iterator: java.lang.Object next()>"))
		{
			//spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getBase().toString())));
			
			// BK
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, spb.iie.getBase().toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		}
	}
}
