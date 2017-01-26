package Extractocol.BufferExtractor_Request.Semantic.Bluetooth.Models.Static;

import java.util.ArrayList;
import java.util.HashMap;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;

public class sendRequest extends BaseModel {

	@Override
	public void applySemantic(SemanticParameterBucket spb) {
		if (spb.sie.getMethodRef().toString()
				.equals("<com.logitech.harmonyhub.sdk.imp.BluetoothManager: boolean sendRequest(java.lang.String)>"))
		{
			System.out.println("SendRequest!! : " + printUrlFromList(spb.BFTtable, spb.sie.getArgs().get(0).toString()));
		}
	}
	
	public String printUrlFromList(HashMap<String, ArrayList<BFNode>> BFTtable, String trackingReg)
	{
		String result = "";
		ArrayList<BFNode> list = BFTtable.get(trackingReg);
		if (list == null)
		{
			return "";
		}
		for (BFNode bfn : list)
		{
			if (bfn.isPhiNode())
			{
				continue;
			}
			if (bfn.getVtype() != null)
			{
				if (bfn.getVtype().contains("HashMap"))
				{
					if (result.indexOf("?") == -1)
						result += "?" + bfn.getStringForUrl() + "=(.*)";
					else
						result += "&" + bfn.getStringForUrl() + "=(.*)";
				}
				else
				{
					if (bfn.getVtype().equals("NameValuePair"))
					{
						result += bfn.getStringForUrl();
					}
					else
					{
						if (bfn.getVtype().equals("URLEncode"))
						{
							result += bfn.getStringForUrl();
						}
						else
						{
							if (bfn.getVtype().equals("org.json.JSONObject") && bfn.getKey() != null)
							{
								result += "(?|&)" + bfn.getKey() + "=" + bfn.getValue();
							}
						}
					}
				}
			}
			else
			{
				if (bfn.getVtype() != null && bfn.getVtype().contains("array"))
				{
					result += bfn.getpossiblestring();
				}
				else
				{
					if (bfn.isHeapObject())
					{
//						result += HeapHandler.getHeapObjectString(bfn.getSootValue());
					}
					else
					{
						if (bfn.getStringForUrl() == null)
						{
							// System.out.println("bfn url string is null -
							// 20150914
							// hnamkoong");
							result += "(.*)";
						}
						else
						{
							if (bfn.getStringForUrl() != null)
							{
								String urlString = bfn.getStringForUrl();
								if (!urlString.equals("\"\""))
								{
									result += alignHttpString(urlString).replaceAll("\"", "").replaceAll("null", "");
								}
							}
						}
					}
				}
			}
		}
		return result;
	}
	
	public static String alignHttpString(String httpurl)
	{
		if (httpurl.indexOf("/") != -1)
		{
			httpurl = httpurl.replaceAll("\\/", "\\/");
		}
		if (httpurl.indexOf("?") != -1)
		{
			httpurl = httpurl.replaceAll("\\?", "\\?");
		}
		return httpurl;
	}
	

}
