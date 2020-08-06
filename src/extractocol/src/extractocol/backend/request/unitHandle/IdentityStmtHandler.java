
package extractocol.backend.request.unitHandle;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.SignatureBuilder_Request;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.basics.ContextEntry;
import extractocol.backend.request.basics.ContextTable;
import extractocol.backend.request.basics.ParameterBucket;
import extractocol.backend.request.heapManagement.HeapHandler;
import extractocol.common.valueEntry.ValueEntryTracking;
import soot.Unit;
import soot.jimple.IdentityStmt;

public class IdentityStmtHandler extends AbstractUnitHandler
{
	public void HandleUnit(Unit ut, ParameterBucket pb, SignatureBuilder_Request sb)
	{
		IdentityStmt ds = (IdentityStmt) ut;
		pb.IdentityStmtHandler(ds);
	}

	private static void ParamVarHandler(ParameterBucket pb, IdentityStmt ds, SignatureBuilder_Request sb, ArrayList<BFNode> list)
	{
		String param = ds.getRightOp().toString().split(":")[0];
		String type = ds.getRightOp().toString().split(":")[1];
		// when response slice, arg value is not our concern.
		if (pb.blockBFTtable.get(param) != null)
		{
			ArrayList<BFNode> src = pb.blockBFTtable.get(param);
			pb.blockBFTtable.put(pb.strDest, sb.CopyList(src));
			
			// JM Todo pinterest hard code Volley problem.
			if (type.contains("com.pinterest.api.RequestParams") && pb.CurrentSM.toString().contains(
					"<com.pinterest.api.MultipartRequest: void <init>(int,java.lang.String,com.pinterest.api.RequestParams,java.util.Map,com.pinterest.api.BaseApiResponseHandler)>"))

			{
				ArrayList<BFNode> params = HeapHandler.GlobalBFTtable
						.get("<com.pinterest.api.RequestParams: java.util.concurrent.ConcurrentHashMap a>");

				BFNode parambfn = new BFNode();

				// request param to map
				for (BFNode bfn : params)
				{
					bfn.setKey(bfn.getStringForUrl());
					bfn.setValue("(.*)");
				}

				parambfn.makeUrlParamsBfn(params);
				Constants.VolleyQueryCandidate = parambfn.getStringForUrl();
			} else if (type.contains("String") && pb.CurrentSM.toString().contains(
					"<com.android.volley.a.i: void <init>(int,java.lang.String,java.lang.String,com.android.volley.i$b,com.android.volley.i$a)>"))
			{
				Constants.VolleyQueryCandidate = sb.GenRegex(null, pb.blockBFTtable, param);
			} else if (type.contains("org.json.JSONObject") && pb.CurrentSM.toString().contains(
					"<com.tinder.a.h: void <init>(int,java.lang.String,org.json.JSONObject,com.android.volley.i$b,com.android.volley.i$a,java.lang.String)>"))
			{
				Constants.VolleyQueryCandidate = sb.GenRegex(null, pb.blockBFTtable, param);
			}

		} else
		{
			BFNode bfn = new BFNode();
			if (type.contains("["))
			{
				list.clear();
				bfn.initarray(30);
			} else
				bfn.makeUrlBfn(ds.getLeftOp().getType().equals("java.lang.Integer") ? "[0-9]*" : "(.*)");

			list.add(bfn);
			pb.blockBFTtable.put(pb.strDest, list);
		}
		
		// BK deliver valueEntryList of parameter to the destination variable
		//pb.ParameterIdentityStmtHandler(ds, param);
	}

	private static void ThisObjectHandler(ParameterBucket pb, IdentityStmt ds, ArrayList<BFNode> list)
	{
		ContextEntry ce = new ContextEntry(pb.CurrentSM, pb.strDest, ds.getLeftOp().getType().toString());
		ContextTable.add(Constants.RealContextInthisObj, ce);
		BFNode bfn = new BFNode();
		bfn.setExtendedType(Constants.RealContextInthisObj);
		list.add(bfn);
		pb.blockBFTtable.put(pb.strDest, list);
		
		
	}
}
