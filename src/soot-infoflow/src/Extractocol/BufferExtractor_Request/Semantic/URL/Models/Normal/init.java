
package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;
import soot.jimple.Constant;

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
			BFNode bfn = new BFNode();
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
			spb.ub.TrackingReg = base;
			spb.ub.printUrl(spb.BFTtable, spb.sm, spb.ut);
			// JM Wish Case (Temporarily)
			spb.ub.printParam();
		}
		
		else if (method.equals("<org.apache.http.entity.StringEntity: void <init>(java.lang.String,java.lang.String)>"))
		{
			List<BFNode> list = spb.BFTtable.get(spb.iie.getArg(0).toString());
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
			
			spb.BFTtable.put(spb.iie.getBase().toString(), (ArrayList<BFNode>) newlist);
			
		}
		//JM ?????? refactoring!!!!
		// For Volley Init
		else
		{
			if (method.equals("<com.android.volley.Request: void <init>(int,java.lang.String,com.android.volley.Response$ErrorListener)>") || method
					.equals("<com.android.volley.toolbox.StringRequest: void <init>(int,java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener,com.android.volley.Response$HeaderListener)>")
			// TODO hardcoding
					|| method
							.equals("<com.pinterest.api.MultipartRequest: void <init>(int,java.lang.String,com.pinterest.api.RequestParams,java.util.Map,com.pinterest.api.BaseApiResponseHandler)>")
					|| method.equals(
							"<com.android.volley.toolbox.StringRequest: void <init>(int,java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener>"))
			{
				String methodstring;
				if (spb.iie.getArg(0) instanceof Constant)
				{
					methodstring = spb.iie.getArg(0).toString();
				}
				else
				{
					methodstring = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(0).toString());
				}
				Constants.VolleyinitHandler(methodstring, 1, -1, spb.BFTtable, spb.iie, spb.ub);
			}
			else
			{
				if (method
						.equals("<com.android.volley.toolbox.StringRequest: void <init>(java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener,com.android.volley.Response$HeaderListener)>")
						|| method
								.equals("<com.android.volley.toolbox.StringRequest: void <init>(int,java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener>")
						|| method.equals(
								"<com.android.volley.toolbox.ImageRequest: void <init>(java.lang.String,com.android.volley.Response$Listener,int,int,android.graphics.Bitmap$Config,com.android.volley.Response$ErrorListener)>"))
				{
					Constants.VolleyinitHandler("0", 0, -1, spb.BFTtable, spb.iie, spb.ub);
				}
				else
				{
					if (method
							.equals("<com.android.volley.toolbox.JsonObjectRequest: void <init>(int,java.lang.String,org.json.JSONObject,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener)>")
							|| method.equals(
									"<com.android.volley.toolbox.JsonRequest: void <init>(int,java.lang.String,java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener)>"))
					{
						String methodstring;
						if (spb.iie.getArg(0) instanceof Constant)
						{
							methodstring = spb.iie.getArg(0).toString();
						}
						else
						{
							methodstring = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(0).toString());
						}
						Constants.VolleyinitHandler(methodstring, 1, 2, spb.BFTtable, spb.iie, spb.ub);
					}
					else
					{
						if (method
								.equals("<com.android.volley.toolbox.JsonObjectRequest: void <init>(java.lang.String,org.json.JSONObject,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener)>")
								|| method.equals(
										"<com.android.volley.toolbox.JsonRequest: void <init>(java.lang.String,java.lang.String,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener)>"))
						{
							Constants.VolleyinitHandler("0", 0, 1, spb.BFTtable, spb.iie, spb.ub);
						}
						else
						{
							if (method.equals("<org.apache.http.client.methods.HttpGet: void <init>(java.lang.String)>")
									|| method.equals("<org.apache.http.client.methods.HttpPost: void <init>(java.lang.String)>")
									|| method.equals("<org.apache.http.client.methods.HttpGet: void <init>(java.net.URI)>")
									|| method.equals("<org.apache.http.client.methods.HttpPost: void <init>(java.net.URI)>"))
							{
								if (method.equals("<org.apache.http.client.methods.HttpGet: void <init>(java.lang.String)>")
										|| method.equals("<org.apache.http.client.methods.HttpGet: void <init>(java.net.URI)>"))
									spb.ub.isGet = true;
								else
									spb.ub.isGet = false;
								String base = spb.iie.getBase().toString();
								String arg = spb.iie.getArg(0).toString();
								BFNode bfn = new BFNode();
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
								spb.ub.TrackingReg = base;
								// this.printUrl(BFTtable, sm, ut);
								// Track =true;
							}
							else
							{
								if (method.equals("<org.apache.http.message.BasicNameValuePair: void <init>(java.lang.String,java.lang.String)>"))
								{
									ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getArg(0).toString());
									if (list != null)
									{
										for (BFNode bfn : list)
											bfn.setVtype("BasicNameValuePair");
										spb.BFTtable.put(spb.iie.getBase().toString(), list);
									}
								}
								else
								{
									if (method.equals("<org.apache.http.client.entity.UrlEncodedFormEntity: void <init>(java.util.List)>"))
									{
										spb.BFTtable.put(spb.iie.getBase().toString(),
												spb.ub.CopyList(spb.BFTtable.get(spb.iie.getArg(0).toString())));
									}
									else
									{
										if (spb.iie.getMethodRef().toString().equals("<java.lang.StringBuilder: void <init>()>"))
										{
											String base = spb.iie.getBase().toString();
											spb.BFTtable.put(base, new ArrayList<BFNode>());
										}
										else
										{
											if (method.equals("<java.lang.StringBuilder: void <init>(java.lang.String)>"))
											{
												String base = spb.iie.getBase().toString();
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
												}
											}
											else
											{
												if (method.equals("<android.content.Intent: void <init>(java.lang.String,android.net.Uri)>"))
												{
													if (spb.iie.getArg(0).toString().contains("android.intent.action.VIEW"))
													{
														spb.ub.TrackingReg = spb.iie.getArg(1).toString();
														spb.ub.printUrl(spb.BFTtable, spb.sm, spb.ut);
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
												else
												{
													if (method.equals("<java.net.URI: void <init>(java.lang.String)>"))
													{
														String base = spb.iie.getBase().toString();
														spb.BFTtable.put(base, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getArg(0).toString())));
													}
													//Duong
													else if (method.equals("<org.json.JSONObject: void <init>(java.lang.String)>")){
														String base = spb.iie.getBase().toString();
														//spb.BFTtable.put(base, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getArg(0).toString())));
														String arg0 = spb.iie.getArg(0).toString();
														String input = spb.ub.GenRegex(null, spb.BFTtable, arg0);
														ArrayList<String> al = splitInputNode(input);
														ArrayList<BFNode> tr = new ArrayList<>();
														for (String s : al){
															try {
																JSONObject newjs = new JSONObject(s);
																ArrayList<BFNode> bfnl = createBfnListFromJson(newjs);
																if (!bfnl.isEmpty()){
																	bfnl.get(0).setStartJson(true);
																	bfnl.get(bfnl.size()-1).setEndJson(true);
																	tr.addAll(bfnl);
																}
															} catch (JSONException e) {
																// TODO Auto-generated catch block
																System.out.println("Can't create JSONObject from the given string");
																//e.printStackTrace();
															}
														}
														if (!tr.isEmpty())
															spb.BFTtable.put(base, tr);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	//Duong
	//split input (node)|(node)|...|(node) into a list of node
	private ArrayList<String> splitInputNode(String str){
		String[] arr = str.split("\\|");
		for (int i = 0; i < arr.length; i++){
			arr[i] = arr[i].replace(")","").replace("(","");
		}
		return new ArrayList<String>(Arrays.asList(arr));
	}
	//Duong
	@SuppressWarnings("unchecked")
	private ArrayList<BFNode> createBfnListFromJson(JSONObject js){
		ArrayList<BFNode> al = new ArrayList<BFNode>();
		Iterator<String> it = js.keys();
		while (it.hasNext()){
			String k = it.next();
			if (!k.equals(".*")){
				BFNode bfn = new BFNode();
				bfn.setKey(k);
				try {
					Object v = js.get(k);
					bfn.setValue(v.toString());
					bfn.setVtype("org.json.JSONObject");
					al.add(bfn);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("Cant create this bfn from this json");
				}
			}
		}
		return al;
	}
}
