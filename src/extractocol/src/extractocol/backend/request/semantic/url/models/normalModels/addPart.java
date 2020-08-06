package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import soot.jimple.Constant;

public class addPart extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		//JM Temporarily
		/*if (!spb.iie.toString().contains("<com.buzzfeed.android.util.MultipartPostRequest"))
		{
			if (spb.iie.getArgCount() > 2 && spb.iie.getArg(1) instanceof Constant)
			{
				ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(spb.iie.getArg(0).toString());
				list.add(bfn);
				
				// BK
				spb.CurrentPB.varTable.addConstantValue(spb.iie.getBase().toString(), spb.iie.getArg(0).toString(), false);
				Constants.BFTResultAlreadyApplied = true;
			}
			else if (spb.iie.getArg(0) instanceof Constant)
			{
				ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(spb.iie.getArg(0).toString());
				list.add(bfn);
				
				// BK
				spb.CurrentPB.varTable.addConstantValue(spb.iie.getBase().toString(), spb.iie.getArg(0).toString(), false);
				Constants.BFTResultAlreadyApplied = true;
			} 
			else 
			{
				// Tree tr = BFTtable.get(iie.getBase().toString());
				ArrayList<BFNode> list2 = spb.BFTtable.get(spb.iie.getArg(0).toString());
				spb.BFTtable.put(spb.iie.getBase().toString(), spb.ub.CopyList(list2));
				
				// BK
				spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.iie.getBase().toString(), spb.iie.getArg(0).toString(), false);
				Constants.BFTResultAlreadyApplied = true;
			}
		}*/
	}
}
