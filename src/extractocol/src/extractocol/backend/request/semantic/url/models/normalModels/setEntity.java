package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;
import java.util.List;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.outputs.BackendOutput;
import extractocol.common.tools.Pair;

public class setEntity extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		spb.ub.TrackingReg = spb.iie.getBase().toString();
		
		if (spb.iie.getMethodRef().getSignature().equals("<org.apache.http.client.methods.HttpPost: void setEntity(org.apache.http.HttpEntity)>"))
		{
			/*List<BFNode> dstlist = spb.BFTtable.get(spb.iie.getBase().toString());
			List<BFNode> srclist = spb.BFTtable.get(spb.iie.getArg(0).toString());
			
			dstlist.add(srclist.get(0));*/
			
			// BK
			// Add request body (map)
			ArrayList<Pair> map = spb.CurrentPB.varTable.getMap(spb.iie.getArg(0).toString());
			spb.CurrentPB.BT().RRI().setRequestBody(map);
			//Constants.BFTResultAlreadyApplied = true;
		}
		
		if(spb.iie.getMethodRef().getSignature().equals("<ch.boye.httpclientandroidlib.client.methods.HttpPost: void setEntity(ch.boye.httpclientandroidlib.HttpEntity)>"))
		{
			// Add request body (map) 
			ArrayList<Pair> map = spb.CurrentPB.varTable.getMap(spb.iie.getArg(0).toString());
			spb.CurrentPB.BT().RRI().setRequestBody(map);
			//Constants.BFTResultAlreadyApplied = true;
		}
	}
}
