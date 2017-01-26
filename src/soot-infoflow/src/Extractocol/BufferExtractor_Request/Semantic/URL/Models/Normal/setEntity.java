package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import java.util.List;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class setEntity extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		spb.ub.TrackingReg = spb.iie.getBase().toString();
		
		if (spb.iie.getMethodRef().getSignature().equals("<org.apache.http.client.methods.HttpPost: void setEntity(org.apache.http.HttpEntity)>"))
		{
			List<BFNode> dstlist = spb.BFTtable.get(spb.iie.getBase().toString());
			List<BFNode> srclist = spb.BFTtable.get(spb.iie.getArg(0).toString());
			
			dstlist.add(srclist.get(0));
		}
	}
}
