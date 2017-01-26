package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Static;

import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;
import soot.jimple.Constant;

public class valueOf extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.sie.getMethodRef().getSignature().equals("<java.lang.Character: java.lang.Character valueOf(char)>"))
		{
			ArrayList<BFNode> list = spb.BFTtable.get(spb.ub.strDest);
			BFNode bfn = new BFNode();
			if (spb.sie.getArg(0) instanceof Constant)
				bfn.makeUrlBfn(String.valueOf(Character.toChars(Integer.parseInt(spb.sie.getArg(0).toString()))));
			list.add(bfn);
		}
	}
}
