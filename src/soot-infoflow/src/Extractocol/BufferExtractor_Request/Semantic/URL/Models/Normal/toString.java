package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class toString extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString()
				.equals("<org.json.JSONObject: java.lang.String toString()>")){
			String str = spb.ub.printBTFromList(spb.BFTtable, spb.iie.getBase().toString());
			BFNode bfn = new BFNode();
			bfn.setStringForUrl(str);
			ArrayList<BFNode> al = new ArrayList<BFNode>();
			al.add(bfn);
			spb.BFTtable.put(spb.ub.strDest, al);
		}
		else{
			ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());
			spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(list));
		}
	}
}
