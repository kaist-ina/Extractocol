package Extractocol.BufferExtractor_Request.Semantic.Bluetooth.Models.Normal;
import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;
import soot.jimple.Constant;

public class getBytes extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		//Duong
		if (spb.iie.getMethodRef().toString().equals("<java.lang.String: byte[] getBytes()>"))
		{
			String base = spb.iie.getBase().toString();
			ArrayList<BFNode> list = spb.BFTtable.get(base);
			spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(list));
		}
	}
}
