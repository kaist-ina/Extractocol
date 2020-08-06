
package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;

public class keySet extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals("<android.os.Bundle: java.util.Set keySet()>"))
		{
			//spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.iie.getBase().toString(), spb.iie.getArg(0).toString(), false);
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.strDst, spb.iie.getBase().toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		}
	}
}
