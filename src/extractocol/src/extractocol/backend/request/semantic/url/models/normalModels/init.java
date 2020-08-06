
package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;
import java.util.List;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.outputs.BackendOutput;
import extractocol.common.tools.Pair;
import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntryTable;
import extractocol.common.valueEntry.ValueEntryTracking;
import extractocol.common.valueEntry.node.Map;
import extractocol.common.valueEntry.node.Reference;
import soot.jimple.Constant;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeStmt;

public class init extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		// JM please refactoring this.
		// this stmt exists for pinterest
		String method = Constants.Deobfuse(spb.iie.getMethodRef().getSignature().toString());
		if (method.equals("<java.net.URL: void <init>(java.lang.String)>")
				|| method.toString().equals("<ch.boye.httpclientandroidlib.client.methods.HttpPost: void <init>(java.lang.String)>")
				|| method.equals("<org.apache.http.client.methods.HttpPut: void <init>(java.lang.String)>"))
		{
			String base = spb.iie.getBase().toString();
			String arg = spb.iie.getArg(0).toString();
			/*BFNode bfn = new BFNode();
			if (spb.iie.getArg(0) instanceof Constant)
			{
				bfn.makeUrlBfn(arg);
				spb.BFTtable.put(base, new ArrayList<BFNode>());
				spb.BFTtable.get(base).add(bfn);
			}
			else
			{
				ArrayList<BFNode> argList = spb.BFTtable.get(arg);
				spb.BFTtable.put(base, spb.ub.CopyList(argList));
			}
			spb.ub.TrackingReg = base;*/
			
			// BK
			if (spb.iie.getArg(0) instanceof Constant)
				spb.CurrentPB.varTable.setConstantValue(base, ValueEntryTable.getRefinedConstant(arg), false);
			else
				spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(base, arg, false);
			
			//Constants.BFTResultAlreadyApplied = true;
			
			
			//spb.ub.printUrl(spb.CurrentPB, spb.BFTtable, spb.sm, spb.ut);
			
			// BK
			spb.CurrentPB.BT().RRI().AddHTTPMethod(spb.ub.isGet? "GET" : "POST");
			spb.CurrentPB.BT().RRI().SaveURI(spb.CurrentPB, base);
			
			// JM Wish Case (Temporarily)
			//spb.ub.printParam();
		}

		else if (method.equals("<org.apache.http.client.entity.UrlEncodedFormEntity: void <init>(java.util.List,java.lang.String)>"))
		{
			spb.CurrentPB.BT().RRI().setRequestBody(spb.CurrentPB.varTable.getValueEntryList(spb.iie.getArg(0).toString()).getMap());
		}
		
		else if (method.equals("<org.apache.http.entity.StringEntity: void <init>(java.lang.String,java.lang.String)>"))
		{
			/*List<BFNode> list = spb.BFTtable.get(spb.iie.getArg(0).toString());
			String params = " Params : ";
			for (BFNode bfn : list)
			{
				if (bfn.getKey() != null && bfn.getValue() != null)
				{
					params += bfn.getKey() + "=" + bfn.getValue() + " ";
				}
			}
			
			BFNode newbfn = new BFNode();
			newbfn.setStringForUrl(params);
			
			ArrayList<BFNode> newlist = new ArrayList<BFNode> ();
			newlist.add(newbfn);
			
			spb.BFTtable.put(spb.iie.getBase().toString(), (ArrayList<BFNode>) newlist);*/
			
			// BK
			ValueEntryTracking.ToMap(spb.iie.getBase().toString(), spb.CurrentPB.varTable.getValueEntryList(spb.iie.getArg(0).toString()).GenRegex(), spb);
			ArrayList<Pair> map = spb.CurrentPB.varTable.getMap(spb.iie.getBase().toString());
			spb.CurrentPB.BT().RRI().setRequestBody(map);
			//Constants.BFTResultAlreadyApplied = true;

//			ArrayList<Pair> map = spb.CurrentPB.varTable.getMap(spb.iie.getArg(0).toString());
//			BackendOutput.reqRespInfo.setRequestBody(map);
		}
		//JM ?????? refactoring!!!!
		// For Volley Init
		else if (method.equals("<com.android.volley.Request: void <init>(int,java.lang.String,com.android.volley.Response$ErrorListener)>") || 
				 method.equals("<com.android.volley.toolbox.StringRequest: void <init>(int,java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener,com.android.volley.Response$HeaderListener)>") ||
				 method.equals("<com.android.volley.toolbox.StringRequest: void <init>(int,java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener>") ||
			// TODO hardcoding
				 method.equals("<com.pinterest.api.MultipartRequest: void <init>(int,java.lang.String,com.pinterest.api.RequestParams,java.util.Map,com.pinterest.api.BaseApiResponseHandler)>") ||
				 method.equals("<com.android.volley.toolbox.StringRequest: void <init>(int,java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener>"))
		{
			String methodstring;
			/*if (spb.iie.getArg(0) instanceof Constant)
			{
				methodstring = spb.iie.getArg(0).toString();
			}
			else
			{
				methodstring = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(0).toString());
			}
			Constants.VolleyinitHandler(methodstring, 1, -1, spb.BFTtable, spb.iie, spb.ub);*/
			
			/* BK */
			methodstring = BackendOutput.getVolleyMethod(spb.CurrentPB.varTable.GenRegex(spb.iie.getArg(0).toString()));
			
			spb.CurrentPB.BT().RRI().AddHTTPMethod(methodstring);
			spb.CurrentPB.BT().RRI().SaveURI(spb.CurrentPB, spb.iie.getArg(1).toString());
		}
		else if (method.equals("<com.android.volley.toolbox.StringRequest: void <init>(java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener,com.android.volley.Response$HeaderListener)>") ||
				 method.equals("<com.android.volley.toolbox.ImageRequest: void <init>(java.lang.String,com.android.volley.Response$Listener,int,int,android.graphics.Bitmap$Config,com.android.volley.Response$ErrorListener)>"))
		{
			//Constants.VolleyinitHandler("0", 0, -1, spb.BFTtable, spb.iie, spb.ub);
			
			/* BK */
			spb.CurrentPB.BT().RRI().AddHTTPMethod("GET");
			spb.CurrentPB.BT().RRI().SaveURI(spb.CurrentPB, spb.iie.getArg(0).toString());
		}
		else if (method.equals("<com.android.volley.toolbox.JsonObjectRequest: void <init>(int,java.lang.String,org.json.JSONObject,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener)>") ||
				 method.equals("<com.android.volley.toolbox.JsonRequest: void <init>(int,java.lang.String,java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener)>"))
		{
			String methodstring;
			/*if (spb.iie.getArg(0) instanceof Constant)
			{
				methodstring = spb.iie.getArg(0).toString();
			}
			else
			{
				methodstring = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(0).toString());
			}
			Constants.VolleyinitHandler(methodstring, 1, 2, spb.BFTtable, spb.iie, spb.ub);*/
			
			/* BK */
			methodstring = BackendOutput.getVolleyMethod(spb.CurrentPB.varTable.GenRegex(spb.iie.getArg(0).toString()));
			
			spb.CurrentPB.BT().RRI().AddHTTPMethod(methodstring);
			spb.CurrentPB.BT().RRI().SaveURI(spb.CurrentPB, spb.iie.getArg(1).toString());
		}
		else if (method.equals("<com.android.volley.toolbox.JsonObjectRequest: void <init>(java.lang.String,org.json.JSONObject,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener)>") ||
				 method.equals("<com.android.volley.toolbox.JsonRequest: void <init>(java.lang.String,java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener)>"))
		{
			//Constants.VolleyinitHandler("0", 0, 1, spb.BFTtable, spb.iie, spb.ub);
			
			/* BK */
			spb.CurrentPB.BT().RRI().AddHTTPMethod("GET");
			spb.CurrentPB.BT().RRI().SaveURI(spb.CurrentPB, spb.iie.getArg(0).toString());
		}
		else if (method.equals("<org.apache.http.client.methods.HttpGet: void <init>(java.lang.String)>") || 
				 method.equals("<org.apache.http.client.methods.HttpPost: void <init>(java.lang.String)>") ||
				 method.equals("<org.apache.http.client.methods.HttpGet: void <init>(java.net.URI)>") ||
				 method.equals("<org.apache.http.client.methods.HttpPost: void <init>(java.net.URI)>"))
		{
			if (method.equals("<org.apache.http.client.methods.HttpGet: void <init>(java.lang.String)>")
					|| method.equals("<org.apache.http.client.methods.HttpGet: void <init>(java.net.URI)>"))
				spb.ub.isGet = true;
			else
				spb.ub.isGet = false;
			String base = spb.iie.getBase().toString();
			String arg = spb.iie.getArg(0).toString();
			/*BFNode bfn = new BFNode();
			if (spb.iie.getArg(0) instanceof Constant)
			{
				bfn.makeUrlBfn(arg);
				spb.BFTtable.put(base, new ArrayList<BFNode>());
				spb.BFTtable.get(base).add(bfn);
			}
			else
			{
				ArrayList<BFNode> argList = spb.BFTtable.get(arg);
				spb.ub.CopyListReference(argList, spb.BFTtable.get(base));
				// BFTtable.put(base, CopyList(argList));
				// init handle needs to be done
			}
			spb.ub.TrackingReg = base;*/
			
			//BK
			if (spb.iie.getArg(0) instanceof Constant)
				spb.CurrentPB.varTable.setConstantValue(base, ValueEntryTable.getRefinedConstant(arg), false);
			else
				spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(base, arg, false);
			
			//Constants.BFTResultAlreadyApplied = true;
			
			// this.printUrl(BFTtable, sm, ut);
			// Track =true;
		}
		else if (method.equals("<org.apache.http.message.BasicNameValuePair: void <init>(java.lang.String,java.lang.String)>"))
		{
			/*ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getArg(0).toString());
			if (list != null)
			{
				for (BFNode bfn : list)
					bfn.setVtype("BasicNameValuePair");
				spb.BFTtable.put(spb.iie.getBase().toString(), list);
			}*/
			
			// BK
			spb.CurrentPB.varTable.setVarType(spb.iie.getArg(0).toString(), "BasicNameValuePair");
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.iie.getBase().toString(), spb.iie.getArg(0).toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		}
		else if (method.equals("<org.apache.http.client.entity.UrlEncodedFormEntity: void <init>(java.util.List)>"))
		{
			/*spb.BFTtable.put(spb.iie.getBase().toString(),
					spb.ub.CopyList(spb.BFTtable.get(spb.iie.getArg(0).toString())));*/
			
			// BK
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.iie.getBase().toString(), spb.iie.getArg(0).toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		}
		else if (spb.iie.getMethodRef().toString().equals("<java.lang.StringBuilder: void <init>()>"))
		{
			/*String base = spb.iie.getBase().toString();
			spb.BFTtable.put(base, new ArrayList<BFNode>());*/
		}
		else if (method.equals("<java.lang.StringBuilder: void <init>(java.lang.String)>"))
		{
			/*String base = spb.iie.getBase().toString();
			if (spb.iie.getArg(0) instanceof Constant)
			{
				ArrayList<BFNode> list = spb.BFTtable.get(base);
				list.clear();
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(spb.iie.getArg(0).toString().toString().replaceAll("\"", ""));
				list.add(bfn);
			}
			else
			{
				ArrayList<BFNode> list = spb.BFTtable.get(base);
				list.clear();
				ArrayList<BFNode> copyList = spb.ub.CopyList(spb.BFTtable.get(spb.iie.getArg(0).toString()));
				for (BFNode bfn : copyList)
				{
					list.add(bfn);
				}
			}*/
			
			// BK
			spb.CurrentPB.varTable.AppendStrToStringBuilder(spb.iie.getBase().toString(), spb.iie.getArg(0));
			//Constants.BFTResultAlreadyApplied = true;
		}	
		else if (method.equals("<android.content.Intent: void <init>(java.lang.String,android.net.Uri)>"))
		{
			if (spb.iie.getArg(0).toString().contains("android.intent.action.VIEW"))
			{
				/*spb.ub.TrackingReg = spb.iie.getArg(1).toString();
				spb.ub.printUrl(spb.CurrentPB, spb.BFTtable, spb.sm, spb.ut);*/
				
				// BK
				spb.CurrentPB.BT().RRI().AddHTTPMethod(spb.ub.isGet ? "GET" : "POST");
				spb.CurrentPB.BT().RRI().SaveURI(spb.CurrentPB, spb.iie.getArg(1).toString());
			}
			// wrong semantic point -
			// 20150914 hnamkoong
			// specialinvoke
			// $r8.<android.content.Intent:
			// void
			// <init>(java.lang.String,android.net.Uri)>("android.intent.action.VIEW",
			// $r4) //
			// TrackingReg =
			// iie.getArg(1).toString();
			// this.printUrl(BFTtable,
			// sm, ut);
		}
		else if (method.equals("<java.net.URI: void <init>(java.lang.String)>"))
		{
			String base = spb.iie.getBase().toString();
			/*spb.BFTtable.put(base, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getArg(0).toString())));*/
			
			// BK
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(base, spb.iie.getArg(0).toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		}
		else if (method.equals("<ch.boye.httpclientandroidlib.message.BasicNameValuePair: void <init>(java.lang.String,java.lang.String)>"))
		{
			// BK
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.iie.getBase().toString(), spb.iie.getArg(0).toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		}
		else if (method.equals("<ch.boye.httpclientandroidlib.client.entity.UrlEncodedFormEntity: void <init>(java.util.List,java.lang.String)>"))
		{
			// BK
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.iie.getBase().toString(), spb.iie.getArg(0).toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
			
			// header
			/*BackendOutput.reqRespInfo.reqie.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			BackendOutput.reqRespInfo.reqie.addHeader("host", "");
			BackendOutput.reqRespInfo.reqie.addHeader("Connection", "");
			BackendOutput.reqRespInfo.reqie.addHeader("User-Agent", "");
			BackendOutput.reqRespInfo.reqie.addHeader("Cookie", "");*/
		}
	}
}
