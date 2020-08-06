package extractocol.backend.request.semantic.url.models.staticModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.valueEntry.ValueEntryTable;
import soot.jimple.Constant;

public class valueOf extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		/*if (spb.sie.getMethodRef().getSignature().equals("<java.lang.Character: java.lang.Character valueOf(char)>"))
		{
			ArrayList<BFNode> list = spb.BFTtable.get(spb.ub.strDest);
			BFNode bfn = new BFNode();
			if (spb.sie.getArg(0) instanceof Constant)
				bfn.makeUrlBfn(String.valueOf(Character.toChars(Integer.parseInt(spb.sie.getArg(0).toString()))));
			list.add(bfn);
		}
		else if (spb.sie.getMethodRef().getSignature().equals("<java.lang.String: java.lang.String valueOf(int)>"))
		{
			ArrayList<BFNode> list = spb.BFTtable.get(spb.sie.getArg(0).toString());
			spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(list));
		}
		else if (spb.sie.getMethodRef().getSignature().equals("<java.lang.String: java.lang.String valueOf(java.lang.Object)>"))
		{
			
		}*/
		
		// Case 1: arg0 is constant
		if (spb.sie.getArg(0) instanceof Constant)
			spb.CurrentPB.varTable.setConstantValue(spb.ub.strDest, ValueEntryTable.getRefinedConstant(spb.sie.getArg(0).toString()), false);
		// Case 2: arg0 is variable
		else
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, spb.sie.getArg(0).toString(), false);
		//Constants.BFTResultAlreadyApplied = true;
	}
}
