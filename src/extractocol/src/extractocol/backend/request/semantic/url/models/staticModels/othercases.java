package extractocol.backend.request.semantic.url.models.staticModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.outputs.BackendOutput;
import soot.jimple.Constant;

public class othercases extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		/*if (spb.sie.getMethodRef().toString().contains("<com.github.kevinsawicki.http.HttpRequest"))
		{
			kevinsawickiHttpHandler(spb);
		}

		if (spb.sie.getMethodRef().toString().contains("<android.net.Uri"))
		{
			androidNetUriHandler(spb);
		}*/

		if (spb.sie.getMethodRef().toString().startsWith("<java.lang.String: java.lang.String valueOf("))
		{
			String arg0 = spb.sie.getArg(0).toString();
			/*ArrayList<BFNode> list = spb.BFTtable.get(arg0);
			spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(list));*/
			
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, arg0, false);
			//Constants.BFTResultAlreadyApplied = true;
		}
	}
	
	private static void androidNetUriHandler(SemanticParameterBucket spb)
	{
		// BK it is not used any more.
		// same as toString
		if (spb.sie.getMethodRef().toString().equals("<android.net.Uri: android.net.Uri parse(java.lang.String)>"))
		{
			if (spb.ub.strDest != null)
			{
				if (spb.sie.getArg(0) instanceof Constant)
				{
					ArrayList<BFNode> list = new ArrayList<BFNode>();
					BFNode bfn = new BFNode();
					bfn.makeUrlBfn(spb.sie.getArg(0).toString());
					list.add(bfn);
					spb.BFTtable.put(spb.ub.strDest, list);
				} else
				{
					String arg0 = spb.sie.getArg(0).toString();

					ArrayList<BFNode> list = spb.BFTtable.get(arg0);
					spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(list));
				}
			}
		}
	}
	
	private static void kevinsawickiHttpHandler(SemanticParameterBucket spb)
	{
		boolean isSemantic = false;
		if (spb.sie.getMethodRef().toString()
				.equals("<com.github.kevinsawicki.http.HttpRequest: com.github.kevinsawicki.http.HttpRequest get(java.lang.CharSequence)>"))
		{
			spb.ub.isGet = true;
			isSemantic = true;
		} else if (spb.sie.getMethodRef().toString()
				.equals("<com.github.kevinsawicki.http.HttpRequest: com.github.kevinsawicki.http.HttpRequest post(java.lang.CharSequence)>"))
		{
			spb.ub.isGet = false;
			isSemantic = true;
		}

		if (isSemantic)
		{
			String arg0 = spb.sie.getArg(0).toString();
			spb.ub.TrackingReg = arg0;
			spb.ub.printUrl(spb.CurrentPB, spb.BFTtable, spb.sm, spb.ut);
			
			// BK
			spb.CurrentPB.BT().RRI().AddHTTPMethod(spb.ub.isGet ? "GET" : "POST");
			spb.CurrentPB.BT().RRI().SaveURI(spb.CurrentPB, arg0);
		}
	}
}
