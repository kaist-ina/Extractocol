package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;

public class group extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals("<java.util.regex.Matcher: java.lang.String group(int)>"))
		{
			// TODO: need to handle regex group 
			
			// BK temporary
			/*ArrayList<BFNode> list = spb.BFTtable.get(spb.strDst);
			list.addAll(spb.ub.CopyList(spb.BFTtable.get(spb.iie.getBase().toString())));*/
			
			// BK
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, spb.iie.getBase().toString(), false);
		}
	}
}
