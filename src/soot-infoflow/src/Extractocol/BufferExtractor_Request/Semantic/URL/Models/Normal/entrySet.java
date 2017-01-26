package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class entrySet extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals("<java.util.concurrent.ConcurrentHashMap: java.util.Set entrySet()>")
				|| spb.iie.getMethodRef().toString().equals("<java.util.Set: java.util.Iterator iterator()>")
				|| spb.iie.getMethodRef().toString().equals("<java.util.Iterator: java.lang.Object next()>"))
		{
			spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getBase().toString())));
		}
	}
}
