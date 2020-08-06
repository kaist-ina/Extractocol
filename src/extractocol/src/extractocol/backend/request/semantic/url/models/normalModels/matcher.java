package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;

public class matcher extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals("<java.util.regex.Pattern: java.util.regex.Matcher matcher(java.lang.CharSequence)>"))
		{
			// TODO: need to handle regex matcher 
			
			// BK temporary
			/*ArrayList<BFNode> list = spb.BFTtable.get(spb.strDst);
			list.addAll(spb.ub.CopyList(spb.BFTtable.get(spb.iie.getArg(0).toString())));*/
			
			// BK
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, spb.iie.getArg(0).toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		}
	}

}
