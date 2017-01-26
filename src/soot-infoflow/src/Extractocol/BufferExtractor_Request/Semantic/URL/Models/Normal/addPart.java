package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;
import soot.jimple.Constant;

public class addPart extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		//JM Temporarily
		if (!spb.iie.toString().contains("<com.buzzfeed.android.util.MultipartPostRequest"))
		{
			if (spb.iie.getArgCount() > 2 && spb.iie.getArg(1) instanceof Constant)
			{
				ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(spb.iie.getArg(0).toString());
				list.add(bfn);
			}
			else
			{
				if (spb.iie.getArg(0) instanceof Constant)
				{
					ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());
					BFNode bfn = new BFNode();
					bfn.makeUrlBfn(spb.iie.getArg(0).toString());
					list.add(bfn);
				}
				else
				{
					// Tree tr = BFTtable.get(iie.getBase().toString());
					ArrayList<BFNode> list2 = spb.BFTtable.get(spb.iie.getArg(0).toString());
					spb.BFTtable.put(spb.iie.getBase().toString(), spb.ub.CopyList(list2));
				}
			}
		}
	}
}
