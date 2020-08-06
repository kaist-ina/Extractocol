package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.helper.ExtractocolLoopFinder;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.outputs.BackendOutput;
import soot.jimple.Constant;
import soot.jimple.Stmt;

public class get extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		ExtractocolLoopFinder loopFinder = new ExtractocolLoopFinder();
		Set<List<Stmt>> loops = loopFinder.getLoops(spb.sm.getActiveBody());

		boolean isLoop = false;

		Iterator<List<Stmt>> iter = loops.iterator();
		while (iter.hasNext())
		{
			List<Stmt> loopstmt = iter.next();
			for (Stmt stmt : loopstmt)
			{
				if (stmt.toString().contains(spb.iie.toString()))
					isLoop = true;
			}
		}

		if (spb.iie.getMethodRef().getSignature().equals("<java.util.Map: java.lang.Object get(java.lang.Object)>") && isLoop)
		{
			/*String base = spb.iie.getBase().toString();
			spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(spb.BFTtable.get(base)));*/
			
			// BK
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.iie.getBase().toString(), spb.iie.getArg(0).toString(), false);
		}
		else if (spb.iie.getMethodRef().toString().contains("<roboguice.inject.Lazy: java.lang.Object get()>"))
		{
			//spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getBase().toString())));
			
			// BK
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, spb.iie.getBase().toString(), false);
		}
		else if(spb.iie.getMethodRef().toString().contains("<android.os.Bundle: java.lang.Object get(java.lang.String)>"))
		{
			// BK
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.iie.getBase().toString(), spb.iie.getArg(0).toString(), false);
		}
		else if (spb.ub.Rx5.contains(spb.iie.getMethodRef().toString()))
		{
			/*ArrayList<BFNode> list = new ArrayList<BFNode>();
			BFNode bfn = new BFNode();
			String arg0 = "";
			//JM Temporarily.
			if (spb.iie.getArg(0) instanceof Constant)
			{
				arg0 = "https://api.5milesapp.com/api/v2" + spb.iie.getArg(0).toString();
			}
			else
			{
				arg0 = "https://api.5milesapp.com/api/v2" + spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(0).toString());
			}

			bfn.makeUrlBfn(arg0);
			list.add(bfn);

			spb.BFTtable.put(spb.ub.strDest, list);
			
			
			// BK
			if (spb.iie.getArg(0) instanceof Constant)
				arg0 = "https://api.5milesapp.com/api/v2" + spb.iie.getArg(0).toString();
			else
				arg0 = "https://api.5milesapp.com/api/v2" + spb.CurrentPB.varTable.GenRegex(spb.iie.getArg(0).toString());
			spb.CurrentPB.varTable.setConstantValue(spb.ub.strDest, arg0, false);
			
			spb.ub.TrackingReg = spb.ub.strDest;
			spb.ub.printUrl(spb.CurrentPB, spb.BFTtable, spb.sm, spb.ut);*/
			
			// BK
			spb.CurrentPB.BT().RRI().AddHTTPMethod(spb.ub.isGet ? "GET" : "POST");
			spb.CurrentPB.BT().RRI().SaveURI(spb.CurrentPB, spb.ub.strDest);
		}
	}	
}
