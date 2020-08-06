
package extractocol.backend.request.semantic.url.models.staticModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.tools.Pair;
import soot.jimple.Constant;


public class addqueryparameters extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.sie.getMethodRef().toString().contains(
				"<oauth.signpost.OAuth: java.lang.String addQueryParameters(java.lang.String,java.lang.String[])>"))
		{
			/*BFNode arraybfn = spb.BFTtable.get(spb.sie.getArg(1).toString()).get(0);
			String url = spb.ub.GenRegex(null, spb.BFTtable, spb.sie.getArg(0).toString()) + "?";
			int index = 0;
			String query = "";
			while (true)
			{
				if (!BFNode.isArray(arraybfn) || index + 1 >= arraybfn.getSize())
					break;
				String key = arraybfn.getarraystring(index);
				String value = arraybfn.getarraystring(index + 1);
				if (index != 0)
					query += "&" + key + "=" + value;
				else
					query += key + "=" + value;
				index = index + 2;
			}
			url += query.replace("\"", "");
			BFNode bfn = new BFNode();
			bfn.makeUrlBfn(url);
			ArrayList<BFNode> list = new ArrayList<BFNode>();
			list.add(bfn);
			spb.BFTtable.put(spb.ub.strDest, list);*/
			
			
			// BK
			String arg0default;
			
			if(spb.sie.getArg(0) instanceof Constant)
				arg0default = spb.sie.getArg(0).toString().substring(1, spb.sie.getArg(0).toString().length() - 1);
			else
				arg0default = spb.CurrentPB.varTable.GenRegex(spb.sie.getArg(0).toString());
			
			String[] arg1 = spb.CurrentPB.varTable.getArray(spb.sie.getArg(1).toString());
			String[] arg0 = arg0default.split("\\|");
			
			if(arg1.length % 2 == 0){
				for(int j = 0; j < arg0.length; j++){
					for(int i = 0; i < arg1.length; i+=2){
						if(arg0[j].contains("?")) arg0[j] += "&";
						else arg0[j] += "?";
						
						arg0[j] += arg1[i] + "=" + arg1[i+1];
					}
				}
				
				spb.CurrentPB.varTable.setConstantValue(spb.ub.strDest, arg0, false);
				//Constants.BFTResultAlreadyApplied = true;
			}else
				System.out.println("BK length of arg1 is weird. (addQueryParameters)");
			
		}
		else if (spb.sie.getMethodRef().toString().contains(
					"<oauth.signpost.OAuth: java.lang.String addQueryParameters(java.lang.String,java.util.Map)>"))
		{
			/*String url = spb.ub.GenRegex(null, spb.BFTtable, spb.sie.getArg(0).toString()) + "?";
			ArrayList<BFNode> list = new ArrayList<BFNode>();
			BFNode parambfn = new BFNode();
			parambfn.makeUrlParamsBfn(spb.BFTtable.get(spb.sie.getArg(1).toString()));
			if (parambfn.getStringForUrl() != null)
				url = url + parambfn.getStringForUrl();
			parambfn.setStringForUrl(url);
			list.add(parambfn);
			spb.BFTtable.put(spb.ub.strDest, list);*/
			
			// BK
			String arg0default;
			
			if(spb.sie.getArg(0) instanceof Constant)
				arg0default = spb.sie.getArg(0).toString().substring(1, spb.sie.getArg(0).toString().length() - 1);
			else
				arg0default = spb.CurrentPB.varTable.GenRegex(spb.sie.getArg(0).toString());
			
			ArrayList<Pair> arg1 = spb.CurrentPB.varTable.getMap(spb.sie.getArg(1).toString());
			String[] arg0 = arg0default.split("\\|");
			
			for(int j = 0; j < arg0.length; j++){
				for(Pair p : arg1){
					if(arg0[j].contains("?")) arg0[j] += "&";
					else arg0[j] += "?";
					
					arg0[j] += p.getKey() + "=" + p.getValue();
				}
			}
			
			spb.CurrentPB.varTable.setConstantValue(spb.ub.strDest, arg0, false);
			//Constants.BFTResultAlreadyApplied = true;
		}
	}
}
