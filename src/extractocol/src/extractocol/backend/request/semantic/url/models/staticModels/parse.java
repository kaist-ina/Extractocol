package extractocol.backend.request.semantic.url.models.staticModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import soot.jimple.Constant;

public class parse extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.sie.getMethodRef().toString().equals("<android.net.Uri: android.net.Uri parse(java.lang.String)>"))
		{
			/*if (spb.sie.getArg(0) instanceof Constant)
			{
				ArrayList<BFNode> tr = new ArrayList<BFNode>();
				BFNode bfn = new BFNode();
				bfn.setStringForUrl(spb.sie.getArg(0).toString());
				tr.add(bfn);
				spb.BFTtable.put(spb.ub.strDest, tr);

			} else
			{
				spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(spb.BFTtable.get(spb.sie.getArg(0).toString())));
			}
			
			spb.ub.TrackingReg = spb.ub.strDest;*/
			
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, spb.sie.getArg(0).toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		}
	}
}
