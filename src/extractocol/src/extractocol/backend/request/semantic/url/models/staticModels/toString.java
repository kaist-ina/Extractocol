package extractocol.backend.request.semantic.url.models.staticModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import soot.jimple.Constant;

public class toString extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		/*ArrayList<BFNode> list1 = new ArrayList<BFNode>();
		BFNode bfn1 = new BFNode();
		bfn1.makeUrlBfn(spb.ub.GenRegex(null, spb.BFTtable, spb.sie.getArg(0).toString()));
		list1.add(bfn1);
		spb.BFTtable.put(spb.ub.strDest, list1);	*/
		
		// Added by BK
		if(spb.sie.getArg(0) instanceof Constant)
			spb.CurrentPB.varTable.setConstantValue(spb.ub.strDest, spb.sie.getArg(0).toString(), false);
		else
			spb.CurrentPB.varTable.setConstantValue(spb.ub.strDest, spb.CurrentPB.varTable.GenRegex(spb.sie.getArg(0).toString()), false);
		
		//Constants.BFTResultAlreadyApplied = true;
	}
}
