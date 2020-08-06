package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;
import java.util.List;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.heapManagement.HeapHandler;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.outputs.BackendOutput;
import extractocol.common.tools.Pair;

public class ajax extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().getSignature()
				.equals("<com.external.androidquery.a: com.external.androidquery.b ajax(com.external.androidquery.b.d)>")||
				spb.iie.getMethodRef().getSignature().equals("<com.insthub.fivemiles.a.be: com.external.androidquery.a ajax(com.external.androidquery.b.d)>"))
		{
			/*spb.ub.TrackingReg = spb.iie.getArg(0).toString();
			// Concat Base Namespace + url and params.
			// String urlKey = "<" + iie.getArg(0).getType().toString() + ":
			// " + "java.lang.String url>";
			String urlKey = "<com.external.androidquery.b.a: java.lang.String url>";
			String paramKey = "<" + spb.iie.getArg(0).getType().toString() + ": java.util.Map params>";
			String paramString = "";

			// JM param is extended
			if (!HeapHandler.GlobalBFTtable.containsKey(paramKey))
			{
				List<String> sups = Constants.sCFG.getSuperclassOf(spb.iie.getArg(0).getType().toString());

				for (String a : sups)
				{
					if (HeapHandler.GlobalBFTtable.containsKey("<" + a + ": java.util.Map params>"))
					{
						paramKey = "<" + a + ": java.util.Map params>";
						break;
					}
				}
			}

			// JM url is extended
			if (!HeapHandler.GlobalHeapTable.containsKey(urlKey))
			{
				List<String> sups = Constants.sCFG.getSuperclassOf(spb.iie.getArg(0).getType().toString());

				for (String a : sups)
				{
					if (HeapHandler.GlobalHeapTable.containsKey("<" + a + ": " + "java.lang.String url>"))
					{
						urlKey = "<" + a + ": java.lang.String url>";
						break;
					}
				}
			}

			// bFTtable.put(iie.getArg(0).toString(), bFTtable.get(url));

			if (HeapHandler.GlobalBFTtable.containsKey(paramKey))
			{
				for (BFNode bfn : HeapHandler.GlobalBFTtable.get(paramKey))
				{
					if (bfn.getStringForUrl() != null)
					{
						if (paramString.length() == 0)
							paramString = "?" + bfn.getStringForUrl().replaceAll("\"", "");
						else
							paramString += "&" + bfn.getStringForUrl().replaceAll("\"", "");
					}
				}
			}

			ArrayList<BFNode> list = new ArrayList<BFNode>();

			BFNode bfn = new BFNode();
			bfn.setSootValue(urlKey);
			bfn.setHeapObject(true);
			list.add(bfn);

			if (paramString.length() > 0)
			{
				bfn = new BFNode();
				bfn.makeUrlBfn(paramString);
				list.add(bfn);
			}
			spb.BFTtable.put(spb.ub.TrackingReg, list);
			
			// BK
//			ajaxHandler(spb);
						
			spb.ub.printUrl(spb.CurrentPB, spb.BFTtable, spb.sm, spb.ut);*/
			
			// BK
			spb.CurrentPB.BT().RRI().AddHTTPMethod(spb.ub.isGet ? "GET" : "POST");
			spb.CurrentPB.BT().RRI().SaveURI(spb.CurrentPB, spb.CurrentPB.varTable.getValueEntryList(spb.iie.getArg(0).toString()).GenRegex());
		}
	}
	
	private static void ajaxHandler(SemanticParameterBucket spb)
	{
		String urlKey = "<com.external.androidquery.b.a: java.lang.String url>";
		String paramKey = "<" + spb.iie.getArg(0).getType().toString() + ": java.util.Map params>";
		String paramString = "";

		if(!spb.CurrentPB.heapTable.getValueEntryTable().containsKey(paramKey))
		{
			List<String> sups = Constants.sCFG.getSuperclassOf(spb.iie.getArg(0).getType().toString());

			for (String a : sups)
			{
				if(spb.CurrentPB.heapTable.getValueEntryTable().containsKey("<" + a + ": java.util.Map params>"))
				{
					paramKey = "<" + a + ": java.util.Map params>";
					break;
				}
			}
		}

		if(!spb.CurrentPB.heapTable.getValueEntryTable().containsKey(urlKey))
		{
			List<String> sups = Constants.sCFG.getSuperclassOf(spb.iie.getArg(0).getType().toString());

			for (String a : sups)
			{
				if(spb.CurrentPB.heapTable.getValueEntryTable().containsKey("<" + a + ": " + "java.lang.String url>"))
				{
					urlKey = "<" + a + ": java.lang.String url>";
					break;
				}
			}
		}

		if(spb.CurrentPB.heapTable.getValueEntryTable().containsKey(paramKey))
		{
			ArrayList<Pair> map = spb.CurrentPB.heapTable.getMap(paramKey);
			for(Pair p: map){
				if(paramString.contains("?")) paramString += "&";
				else paramString += "?";
				
				paramString += p.getKey() + "=" + p.getValue();
			}
		}

		spb.CurrentPB.varTable.setConstantValue(spb.ub.TrackingReg, urlKey + paramString, false);
	}
}
