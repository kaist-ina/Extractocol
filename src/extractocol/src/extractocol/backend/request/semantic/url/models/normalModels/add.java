
package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.outputs.BackendOutput;
import extractocol.common.valueEntry.ValueEntryList;
import soot.SootMethod;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;

public class add extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		String method = Constants.Deobfuse(spb.iie.getMethodRef().toString());

		if (method.equals("<java.util.List: boolean add(java.lang.Object)>"))
		{
			/*String arg = spb.iie.getArg(0).toString();
			String base = spb.iie.getBase().toString();
			spb.BFTtable.put(base, spb.ub.CopyList(spb.BFTtable.get(arg)));*/
			
			HandleValueEntry(spb);
		}
		else if (method.equals("<java.util.ArrayList: boolean add(java.lang.Object)>"))
		{
			HandleValueEntry(spb);
		}
		else if (method.equals("<com.android.volley.RequestQueue: com.android.volley.Request add(com.android.volley.Request)>"))
		{
			String arg0 = spb.iie.getArg(0).toString();
			ArrayList<String> types = spb.CurrentPB.varTable.getTypeof(arg0);
			String arg0Type = types.get(0);
			String respHandler = "<" + arg0Type + ": void deliverResponse(java.lang.Object)>";
			SootMethod respHandlerSM = Constants.sCFG.getMethodOf(respHandler); 
			
			if(respHandlerSM == null)
				return;
			
			String firstStmt = (respHandlerSM.hasActiveBody())?respHandlerSM.getActiveBody().getUnits().getFirst().toString() : null;
			
			if(firstStmt != null)
				spb.CurrentPB.BT().setRespEP(respHandler, firstStmt);
		}
	}
	
	private void HandleValueEntry(SemanticParameterBucket spb){
		String baseVar = spb.iie.getBase().toString();
		String arg = spb.iie.getArg(0).toString();
		
		ValueEntryList baseVel = spb.CurrentPB.varTable.getValueEntryList(baseVar);
		boolean flag = false; // TODO: need to clean up this method (remove flag variable)
		
		// case 1: when base variable is list
		if(baseVel != null){
			try{
				if (baseVel.doesContainTypeOf(SOURCE_TYPE.LIST)) {
					spb.CurrentPB.varTable.addListValue(baseVar, spb.CurrentPB.varTable.GenRegex(arg), true);
					flag = true;
				}
			}catch (Exception e){
				System.out.println("Error in Unit: " + spb.ut.toString() + ", Method: " + spb.CurrentPB.CurrentSM.toString());
				e.printStackTrace();
			}
		}
		
		// others
		if(!flag)
			spb.CurrentPB.varTable.addValueEntryList(baseVar, arg, false);
	}
}
