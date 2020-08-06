package extractocol.backend.request.semantic.url.models.staticModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.tools.Pair;
import extractocol.common.valueEntry.PartofUrlStringTable;
import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.node.PartofUrlString;
import soot.RefType;
import soot.jimple.Constant;
import soot.jimple.StringConstant;

public class format extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.sie.getMethodRef().toString()
				.equals("<org.apache.http.client.utils.URLEncodedUtils: java.lang.String format(java.util.List,java.lang.String)>"))
		{
			/*ArrayList<BFNode> tr = spb.BFTtable.get(spb.ub.strDest);

			tr.clear();

			String r = "";

			for (BFNode bfn : spb.BFTtable.get(spb.sie.getArg(0).toString()))
			{
				if (!bfn.getStringForUrl().equals(""))
					r += bfn.getStringForUrl() + "=(.*)&";
			}

			if (r.length() > 0)
			{
				BFNode bfn = new BFNode();
				bfn.setVtype("URLEncode");
				bfn.setStringForUrl(r.substring(0, r.length() - 1));
				tr.add(bfn);
			}
			spb.BFTtable.put(spb.ub.strDest, tr);*/


			// BK
			// Modified by jeongmin
			// For lazy semantic
			String httpParams = "";
			int index = 0;
			for ( Pair p : spb.CurrentPB.varTable.getValueEntryList(spb.sie.getArg(0).toString()).getMap()) {

				if (index == 0)
					httpParams += p.getKey() + "=" + p.getValue();
				else
					httpParams += "(?|&)" + p.getKey() + "=" + p.getValue();
				index ++;
			}
			spb.CurrentPB.varTable.setValueEntry(spb.ub.strDest, httpParams, ValueEntry.SOURCE_TYPE.CONSTANT, false);
			//Constants.BFTResultAlreadyApplied = true;

		} 
		else if (spb.sie.getMethodRef().getSignature().equals("<java.lang.String: java.lang.String format(java.util.Locale,java.lang.String,java.lang.Object[])>"))
		{
			if (spb.ub.strDest != null)
			{
				if (spb.sie.getArgCount() == 3)
				{
					/*String string = "";
					// arg0 is not constant
					if (!(spb.sie.getArg(1) instanceof StringConstant) || !(spb.sie.getArg(1) instanceof Constant))
					{
						System.out.println("not implemented for this case");
					} else
					{
						string = spb.sie.getArg(1).toString();
						string = string.replaceAll("%d|%s", "(.*)");
					}

					ArrayList<BFNode> list = new ArrayList<BFNode>();
					BFNode bfn = new BFNode();
					bfn.makeUrlBfn(string);
					list.add(bfn);
					spb.BFTtable.put(spb.ub.strDest, list);
					
					// BK
					spb.CurrentPB.varTable.addConstantValue(spb.ub.strDest, string, false);
					Constants.BFTResultAlreadyApplied = true;*/
				}
			}
		}
		else if (spb.sie.getMethodRef().toString().equals("<java.lang.String: java.lang.String format(java.lang.String,java.lang.Object[])>"))
		{
			if (spb.ub.strDest != null)
			{
				if (spb.sie.getArgCount() == 2)
				{
					// We don't apply format semantic model when all par ams include heap object.
//					if (spb.CurrentPB.varTable.getArray(spb.sie.getArg(0).toString())
//						System.out.println("[Extractocol] : RefType");

					// BK
					String arg0;
					Object[] arg1 = (Object[])spb.CurrentPB.varTable.getArray(spb.sie.getArg(1).toString());
					if(arg1 == null)
						return;
					
					if(spb.sie.getArg(0) instanceof Constant)
						arg0 = spb.sie.getArg(0).toString().substring(1, spb.sie.getArg(0).toString().length() - 1);
					else
						arg0 = spb.CurrentPB.varTable.GenRegex(spb.sie.getArg(0).toString());
					
					if( (arg0.split("\\%").length - 1) == arg1.length && !(arg0.contains("<") && arg0.contains((">")))){
						try{
							spb.CurrentPB.varTable.setConstantValue(spb.ub.strDest, String.format(arg0, arg1), false);
							//Constants.BFTResultAlreadyApplied = true;
						}catch(Exception e){
							; // It can reach here when arg0 contains others like %f, %d, etc not %s.
						}
					}
					else
					{
						if (arg0.contains("<") && arg0.contains((">"))) {
							// Added by Jeongmin
//							spb.CurrentPB.varTable.addHeapValue(spb.ub.strDest, arg0, false);
//							spb.CurrentPB.varTable.addValueEntryList(arg0, spb.CurrentPB.varTable.getValueEntryList(spb.sie.getArg(1).toString()) ,false);
							spb.CurrentPB.varTable.addConstantValueatFirst(spb.ub.strDest, arg0, false);
							spb.CurrentPB.varTable.addConstantValue(spb.ub.strDest, "Remain:" + spb.CurrentPB.BT().getReqEPMethod(), false);
							PartofUrlString pus = new PartofUrlString(spb.CurrentPB.varTable.getArray(spb.sie.getArg(1).toString()), spb.sie.getMethodRef().getSignature());
							PartofUrlStringTable.addRemainString(arg0, spb.CurrentPB.BT().getReqEPMethod(), pus);
							//Constants.BFTResultAlreadyApplied = true;
						}
					}
				}
			}
		}
	}

	private boolean hasHeap(String arg)
	{
		return arg.contains("<") && arg.contains(">");
	}
	
	private boolean isStringDouble(String s)
	{
		try
		{
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e)
		{
			return false;
		}
	}
}
