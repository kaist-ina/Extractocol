package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Helper.ExtractocolLoopFinder;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;
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
			String base = spb.iie.getBase().toString();
			spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(spb.BFTtable.get(base)));
		}
		else
		{
			if (spb.iie.getMethodRef().toString().contains("<roboguice.inject.Lazy: java.lang.Object get()>"))
			{
				spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getBase().toString())));
			}
			else
			{
				if (spb.ub.Rx5.contains(spb.iie.getMethodRef().toString()))
				{
					ArrayList<BFNode> list = new ArrayList<BFNode>();
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
					spb.ub.TrackingReg = spb.ub.strDest;
					spb.ub.printUrl(spb.BFTtable, spb.sm, spb.ut);
				}
			}
		}
	}
}
