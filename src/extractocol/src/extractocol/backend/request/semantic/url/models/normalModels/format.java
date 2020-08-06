
package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;

public class format extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef()
				.toString() == "<org.apache.http.client.utils.URLEncodedUtils: java.lang.String format(java.util.List,java.lang.String)>")
		{
			spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getArg(0).toString())));
			
			// BK
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, spb.iie.getArg(0).toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		}
		// BK does it really exist? java.lang.String.format is always staticInvoke, isn't it?
		else if (spb.iie.getMethodRef().toString().equals("<java.lang.String: java.lang.String format(java.lang.String,java.lang.Object[])>"))
		{
			/*ArrayList<BFNode> tr = spb.BFTtable.get(spb.ub.strDest);
			if (tr != null)
			{
				BFNode bfn = new BFNode();
				String arg0 = spb.iie.getArg(0).toString();
				bfn.makeUrlBfn(arg0);
			}*/
			
			//BK
			spb.CurrentPB.varTable.setConstantValue(spb.ub.strDest, spb.iie.getArg(0).toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		}
			
	}
}
