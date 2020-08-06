
package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.retrofit.retrofit_http_old;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.outputs.BackendOutput;

public class othercases extends BaseModel
{
	private static void okHttpHandler(SemanticParameterBucket spb)
	{
		String method = Constants.Deobfuse(spb.iie.getMethodRef().getSignature().toString());
		if (method.startsWith("<com.squareup.okhttp.Request$Builder: com.squareup.okhttp.Request$Builder ")
				&& spb.iie.getArgCount() == 1 && spb.iie.getArg(0).getType().toString().equals("java.lang.String"))
		{
			//spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getArg(0).toString())));
			
			//BK
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, spb.iie.getArg(0).toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		}
		else if (method.startsWith("<com.squareup.okhttp.Request$Builder: com.squareup.okhttp.Request")
					&& spb.iie.getArgCount() == 0)
		{
			//spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getBase().toString())));
			
			//BK
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, spb.iie.getBase().toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		} 
		else if (method.startsWith("<com.squareup.okhttp.OkHttpClient: com.squareup.okhttp.Call")
				&& spb.iie.getArgCount() == 1
				&& spb.iie.getArg(0).getType().toString().equals("com.squareup.okhttp.Request")) 
		{
			String arg0 = spb.iie.getArg(0).toString();
			/*spb.ub.TrackingReg = arg0;
			spb.ub.printUrl(spb.CurrentPB, spb.BFTtable, spb.sm, spb.ut);*/
			
			// BK
			spb.CurrentPB.BT().RRI().AddHTTPMethod(spb.ub.isGet? "GET" : "POST");
			spb.CurrentPB.BT().RRI().SaveURI(spb.CurrentPB, arg0);
		} 
		else if (spb.iie.getMethodRef().getSignature()
				.equals("<com.squareup.okhttp.OkHttpClient: java.net.HttpURLConnection open(java.net.URL)>")) 
		{
			/*spb.ub.TrackingReg = spb.ub.strDest;
			ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getArg(0).toString());
			spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(list));*/
			
			//BK
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, spb.iie.getArg(0).toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		} 
		else if (method.startsWith("<com.squareup.okhttp.Request$Builder: com.squareup.okhttp.Request$Builder")
				&& spb.iie.getArgCount() == 1
				&& spb.iie.getArg(0).getType().toString().contains("com.squareup.okhttp.")) 
		{
			/*spb.ub.TrackingReg = spb.ub.strDest;
			ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getArg(0).toString());
			spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(list));*/
			
			//BK
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, spb.iie.getArg(0).toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		} 
		else if (method.contains(
				"<com.squareup.okhttp.Request$Builder: com.squareup.okhttp.Request$Builder method(java.lang.String,com.squareup.okhttp.RequestBody)>")) 
		{
			/*String methodstring = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(0).toString());
			String url = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getBase().toString());
			String requestbody = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(1).toString());
			if (!requestbody.equals(""))
				url += requestbody;
			url = methodstring + url;
			BFNode bfn = new BFNode();
			bfn.makeUrlBfn(url);
			ArrayList<BFNode> list = new ArrayList<BFNode>();
			list.add(bfn);
			spb.BFTtable.put(spb.ub.strDest, list);*/
			
			// BK 
			String methodstring = spb.CurrentPB.varTable.GenRegex(spb.iie.getArg(0).toString());
			String url = spb.CurrentPB.varTable.GenRegex(spb.iie.getBase().toString());
			String requestbody = spb.CurrentPB.varTable.GenRegex(spb.iie.getArg(1).toString());
			if (!requestbody.equals(""))
				url += requestbody;
			url = methodstring + url;
			spb.CurrentPB.varTable.setConstantValue(spb.ub.strDest, url, false);
			//Constants.BFTResultAlreadyApplied = true;
		} 
		else if (method.contains("<com.squareup.okhttp.Request: com.squareup.okhttp.Request$Builder newBuilder()>")) 
		{
			//spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getBase().toString())));
			
			//BK
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, spb.iie.getBase().toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		}
	}
	
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		// JM for obfuscated OkhttpLib
		if (spb.methodref.toString().startsWith("<com.squareup.okhttp"))
		{
			okHttpHandler(spb);
		}
		// JM For Reftrofit
		/*retrofit_http_old Semantic = null;
		// retrofit cases
		// find retrofit class
		for (int i = 0; i < Constants.retrofits.size(); i++)
		{
			if (Constants.retrofits.get(i).methodref.equals(spb.iie.getMethodRef().toString()))
			{
				Semantic = Constants.retrofits.get(i);
				break;
			}
		}
		if (Semantic != null)
		{
			// suburl_param
			if (Semantic.paramset.size() > 0)
			{
				for (Integer key : Semantic.paramset.keySet())
				{
					String value = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(key).toString());
					String param = "\\{" + Semantic.paramset.get(key) + "\\}";
					Semantic.suburl = Semantic.suburl.replaceAll(param, value);
				}
			}
			String stringForURL = Semantic.suburl;
			String targetmap = "";
			String querymap = "";
			int numofquerys = 0;
			if (Semantic.querymap.size() > 0)
			{
				for (int i = 0; i < Semantic.paramarray.size(); i++)
				{
					if (Semantic.paramarray.get(i).equals(Semantic.querymap.get(0)))
						targetmap = spb.iie.getArg(i).toString();
				}
				ArrayList<BFNode> querymapbfns = spb.BFTtable.get(targetmap);
				for (int i = 0; i < querymapbfns.size(); i++)
				{
					BFNode targetbfn = querymapbfns.get(i);
					// targetbfn.getKey().replaceAll(""", "");
					if (targetbfn.getKey() == null || targetbfn.getValue() == null)
						continue;
					if (numofquerys == 0)
						querymap += "?";
					else
						querymap += "&";
					if (targetbfn.isLoop())
						querymap += "(" + targetbfn.getKey() + "=" + targetbfn.getValue() + ")*";
					else
						querymap += "(" + targetbfn.getKey() + "=" + targetbfn.getValue() + ")?";
					numofquerys++;
				}
			}
			if (numofquerys > 0)
				querymap = "(" + querymap + ")*";
			if (!Semantic.query.equals("?"))
				stringForURL += Semantic.query + querymap.replaceFirst("\\?", "");
			else
				stringForURL += querymap;
			// baseurl
			stringForURL = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getBase().toString()) + stringForURL;
			// http method
			stringForURL = "***" + Semantic.http_method + "***" + stringForURL;
			ArrayList<BFNode> list = new ArrayList<BFNode>();
			BFNode bfn = new BFNode();
			bfn.makeUrlBfn(stringForURL);
			list.add(bfn);
			spb.BFTtable.put(spb.ub.strDest, list);
			// retrofit DP
			if (Constants.retrofitDP.contains(spb.iie.getMethodRef().toString()))
				printUrlForretrofit(spb, spb.ub.strDest);
		}
		// retrofit ignore
		if (Constants.retrofit_ignorelist.contains(spb.iie.getMethodRef().toString()))
		{
			return;
		}
		ArrayList<String> rxdps = new ArrayList<String>();
		for (int i = 0; i < Constants.RxJavaDP.length; i++)
			rxdps.add(Constants.RxJavaDP[i]);
		// Rxjava DP
		if (rxdps.contains(spb.iie.getMethodRef().toString()))
		{
			printUrlForretrofit(spb, spb.iie.getBase().toString());
		}*/
	}
	

	/*public static void printUrlForretrofit(SemanticParameterBucket spb, String urlbfn)
	{
		String url = spb.ub.GenRegex(null, spb.BFTtable, urlbfn);
		if (url == null || url.length() < 6)
			return;
		if (url.equals("") || url.equals("(.*)"))
		{
			return;
		}
		String http_method = "";
		if (url.substring(0, 4).contains("***"))
		{
			http_method = url.split("\\*\\*\\*")[1];
			url = url.split("\\*\\*\\*")[0] + http_method + url.split("\\*\\*\\*")[2];
		}

		url = "url : " + url;
		System.out.println();
		System.out.println(url);
		System.out.println();

		StringBuilder sb = new StringBuilder();
		sb.append(url + "\n");
		sb.append("\tDP : " + Constants.DPCont.getDPMethod() + "\n");
		sb.append("\tEP : " + Constants.CurrentEP + "\n");
		sb.append("\tSM : " + spb.sm.toString() + "\n");
		sb.append("\tUnit : " + spb.ut.toString() + "\n");
		Constants.ResultUrls.add(sb.toString());

		Constants.ResultUrls.add(url);

		
		
		// BK
		String url2 = spb.CurrentPB.varTable.GenRegex(urlbfn);
		if (url2 == null || url2.length() < 6)
			return;
		if (url2.equals("") || url2.equals("(.*)"))
			return;
		
		String http_method2 = "";
		if (url2.substring(0, 4).contains("***"))
		{
			http_method2 = url2.split("\\*\\*\\*")[1];
			url2 = url2.split("\\*\\*\\*")[0] + http_method2 + url2.split("\\*\\*\\*")[2];
		}

		url2 = "url (with ValueEntryTable): " + url2;
		System.out.println();
		System.out.println(url2);
		System.out.println();

		StringBuilder sb2 = new StringBuilder();
		sb2.append(url2 + "\n");
		sb2.append("\tDP : " + Constants.DPCont.getDPMethod() + "\n");
		sb2.append("\tEP : " + Constants.CurrentEP + "\n");
		sb2.append("\tSM : " + spb.sm.toString() + "\n");
		sb2.append("\tUnit : " + spb.ut.toString() + "\n");
		Constants.ResultUrls.add(sb.toString());

		Constants.ResultUrls.add(url2);
		
		
		// interface invoke! this can't be allowed 20150921
		// many urls at one EP
		// Constants3.foundDPStmt = true;
	}*/

}
