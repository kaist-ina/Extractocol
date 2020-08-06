
package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.heapManagement.HeapHandler;
import extractocol.backend.request.helper.ExtractocolLoopFinder;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.valueEntry.ValueEntryTracking;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;
import soot.Value;
import soot.jimple.Constant;
import soot.jimple.Stmt;

public class put extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString()
				.equals("<org.json.JSONObject: org.json.JSONObject put(java.lang.String,java.lang.Object)>"))
		{
			/*ArrayList<BFNode> tr = spb.BFTtable.get(spb.iie.getBase().toString());
			if (spb.iie.getArg(1) instanceof Constant)
			{
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(".*");
				bfn.setKey(spb.iie.getArg(0).toString());
				bfn.setValue(spb.iie.getArg(1).toString());
				bfn.setVtype("org.json.JSONObject");
				tr.add(bfn);
			}
			else
			{
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(".*");
				bfn.setKey(spb.iie.getArg(0).toString());
				bfn.setValue(".*");
				bfn.setVtype("org.json.JSONObject");
				tr.add(bfn);
			}*/

			// Added by BK
			HandleValueEntry(spb);
		}
		else if (spb.iie.getMethodRef().toString()
				.equals("<org.json.JSONObject: org.json.JSONObject put(java.lang.String,int)>"))
		{
			/*ArrayList<BFNode> tr = spb.BFTtable.get(spb.iie.getBase().toString());
			BFNode bfn = new BFNode();
			bfn.makeUrlBfn(spb.iie.getArg(0).toString());
			tr.add(bfn);*/

			// Added by BK
			HandleValueEntry(spb);
		}	
		else if (spb.iie.getMethodRef().toString()
				.equals("<org.json.JSONArray: org.json.JSONArray put(java.lang.Object)>"))
		{
			/*ArrayList<BFNode> tr = spb.BFTtable.get(spb.iie.getBase().toString());
			BFNode bfn = new BFNode();
			if (spb.iie.getArg(0) instanceof Constant)
			{
				bfn.makeUrlBfn(spb.iie.getArg(0).toString());
				tr.add(bfn);
			}*/
		}	
		else if (spb.iie.getMethodRef().toString().equals(
				"<java.util.concurrent.ConcurrentHashMap: java.lang.Object put(java.lang.Object,java.lang.Object)>"))
		{
			/*ArrayList<BFNode> tr = spb.BFTtable.get(spb.iie.getBase().toString());
			if (spb.iie.getArg(1) instanceof Constant)
			{
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(spb.iie.getArg(0).toString());
				tr.add(bfn);
			}
			else
			{
				BFNode bfn = new BFNode();
				String r = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(0).toString());
				bfn.makeUrlBfn(r);
				bfn.setVtype("HashMap");
				boolean isHas = false;
				for (BFNode src : tr)
				{
					if (src.getStringForUrl() != null)
						if (src.getStringForUrl().equals(bfn.getStringForUrl()))
						{
							isHas = true;
						}
				}
				if (!isHas)
					tr.add(bfn);
				HeapHandler.requestUpdate = true;
			}*/
			
			// Added by BK
			HandleValueEntry(spb);
		}		
		else if (spb.iie.getMethodRef().getSignature()
				.equals("<java.util.Map: java.lang.Object put(java.lang.Object,java.lang.Object)>"))
		{
			/*ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());
			BFNode bfn = new BFNode();
			if (list.size() > 0)
				list.get(0).setVtype("Map");
			else
				bfn.setVtype("Map");
			// Loop case
			ExtractocolLoopFinder loopFinder = new ExtractocolLoopFinder();
			Set<List<Stmt>> loops = loopFinder.getLoops(spb.sm.getActiveBody());
			Iterator<List<Stmt>> iter = loops.iterator();
			while (iter.hasNext())
			{
				List<Stmt> loopstmt = iter.next();
				for (Stmt stmt : loopstmt)
				{
					if (stmt.toString().contains(spb.iie.toString()))
						bfn.setLoop(true);
				}
			}
			if (spb.iie.getArg(0) instanceof Constant)
			{
				bfn.setKey(spb.iie.getArg(0).toString().replaceAll("\"", ""));
				if (spb.iie.getArg(1) instanceof Constant)
					bfn.setValue(spb.iie.getArg(1).toString().replaceAll("\"", ""));
				else
					bfn.setValue("(.*)");
				list.add(bfn);
			}
			else
			{
				bfn.setKey("(.*)");
				if (spb.iie.getArg(1) instanceof Constant)
					bfn.setValue(spb.iie.getArg(1).toString().replaceAll("\"", ""));
				else
					bfn.setValue("(.*)");
				list.add(bfn);
			}*/
			
			// Added by BK
			HandleValueEntry(spb);
		} 
		else if (spb.iie.getMethodRef().getSignature()
				.equals("<java.util.HashMap: java.lang.Object put(java.lang.Object,java.lang.Object)>")) 
		{
			// Added by BK
			HandleValueEntry(spb);
						
			/*ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());
			if(list == null)
				return;
			
			BFNode bfn = new BFNode();
			if (list.size() > 0)
				list.get(0).setVtype("HashMap");
			else
				bfn.setVtype("HashMap");
			
			// Loop case
			ExtractocolLoopFinder loopFinder = new ExtractocolLoopFinder();
			Set<List<Stmt>> loops = loopFinder.getLoops(spb.sm.getActiveBody());
			Iterator<List<Stmt>> iter = loops.iterator();
			
			while (iter.hasNext()) {
				List<Stmt> loopstmt = iter.next();
				for (Stmt stmt : loopstmt) {
					if (stmt.toString().contains(spb.iie.toString()))
						bfn.setLoop(true);
				}
			}
			
			if (spb.iie.getArg(0) instanceof Constant) {
				bfn.setKey(spb.iie.getArg(0).toString().replaceAll("\"", ""));
				if (spb.iie.getArg(1) instanceof Constant)
					bfn.setValue(spb.iie.getArg(1).toString().replaceAll("\"", ""));
				else
					bfn.setValue("(.*)");
				list.add(bfn);
			} else {
				bfn.setKey("(.*)");
				if (spb.iie.getArg(1) instanceof Constant)
					bfn.setValue(spb.iie.getArg(1).toString().replaceAll("\"", ""));
				else
					bfn.setValue("(.*)");
				list.add(bfn);
			}*/
		}
	}

	
	
	/** Add pair(key,value) into baseVar of ValueEntryTable
	 * 
	 * @param spb
	 */
	private void HandleValueEntry(SemanticParameterBucket spb){
		if(spb.iie.getArgCount() != 2)
			return;

		// Case 1: Copy map list when arg0 has map entry
		if(hasMapEntryInArg0(spb)){
			spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.iie.getBase().toString(), spb.iie.getArg(0).toString(), false);
			
		}
		// Case 2: others
		else
		{
			String baseVar = spb.iie.getBase().toString();
			//String key = getValue(spb.CurrentPB, spb.iie.getArg(0)).replaceAll("\"", "");
			//String value = getValue(spb.CurrentPB, spb.iie.getArg(1)).replaceAll("\"", "");
			spb.CurrentPB.varTable.addMapValue(baseVar, spb.iie.getArg(0), spb.iie.getArg(1), true);
		}
	}
	
	/** Check whether arg0 contains Map entry
	 * 
	 * @param spb
	 * @return True if arg0 contains Map entry
	 */
	private boolean hasMapEntryInArg0(SemanticParameterBucket spb){
		return spb.CurrentPB.varTable.doesContainTypeOf(spb.iie.getArg(0).toString(), SOURCE_TYPE.MAP);
	}
	
	/** Generate constant value of 'arg'
	 * 
	 * @param pb ParameterBucket
	 * @param arg argument
	 * @return constant value of 'arg'
	 */
	private String getValue(ValueEntryTracking pb, Value arg){
		if(arg instanceof Constant)
			return arg.toString().replaceAll("\"", "");
		else
			return pb.varTable.GenRegex(arg.toString());
	}
}
