package extractocol.backend.request.semantic.url.models.normalModels;

import extractocol.Constants;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.trackers.IntentMapTracker;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.helper.General;
import soot.Value;
import soot.jimple.Constant;

public class getString extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals("<android.os.Bundle: java.lang.String getString(java.lang.String)>"))
		{
			String key;
			String val;
			String arg = spb.iie.getArg(0).toString();
			
			// 1. get key
			if(spb.iie.getArg(0) instanceof Constant)
				key = General.getConstantName(arg);
			else
				key = spb.CurrentPB.varTable.GenRegex(arg);
			
			// 2. get value
			if(key.equals(".*"))
				val = ".*";
			else
				val = IntentMapTracker.getIntentValue(key);
			
			// 3. processing
			spb.CurrentPB.varTable.setConstantValue(spb.ub.strDest, val, false);
		}
		
		else if (spb.iie.getMethodRef().toString().equals("<android.content.SharedPreferences: java.lang.String getString(java.lang.String,java.lang.String)>")) {
			// get
			String res;
			Value arg1 = spb.iie.getArg(1);
			if (arg1.toString().equals("null"))
				res = ".*";
			else if (arg1 instanceof Constant)
				res = General.getConstantName(spb.iie.getArg(1).toString());
			else {
				ValueEntryList vel = spb.CurrentPB.varTable.getValueEntryList(arg1.toString());
				if (vel != null)
					res = vel.GenRegex();
				else
					res = ".*";
			}
				
			// set
			spb.CurrentPB.varTable.setConstantValue(spb.ub.strDest, res, false);
		}
	}
}
