package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import extractocol.Constants;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.tester.HeapHandling;

public class replace extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals("<java.lang.String: java.lang.String replace(java.lang.CharSequence,java.lang.CharSequence)>"))
		{
			//spb.BFTtable.put(spb.ub.strDest, spb.ub.CopyList(spb.BFTtable.get(spb.iie.getBase().toString())));
			
			// BK
			String base = spb.CurrentPB.varTable.GenRegex(spb.iie.getBase().toString());
			String arg0 = spb.CurrentPB.varTable.getFixedValueFromArg(spb.iie.getArg(0));
			String arg1 = spb.CurrentPB.varTable.getFixedValueFromArg(spb.iie.getArg(1));
			
			List<String> origHeaps = new ArrayList<String>();
			List<String> transformedHeaps = new ArrayList<String>();
			Matcher m = HeapHandling.heapPattern.matcher(base);
			
			while(m.find()){
				String heap = m.group();
				origHeaps.add(heap);
				
				heap = heap.replace(arg0, arg1);
				transformedHeaps.add(heap);
			}
			
			try {
				base = base.replace(arg0, arg1);
			} catch (Exception e) {
				System.err.println("[replace error] base: " + base + ", arg0: " + arg0 + ", arg1: " + arg1);
				e.printStackTrace();
				return;
			}
			
			for(int i = 0; i < origHeaps.size(); i ++)
				base = base.replace(transformedHeaps.get(i), origHeaps.get(i));
			
			spb.CurrentPB.varTable.setConstantValue(spb.ub.strDest, base, false);
			
			//spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, spb.iie.getBase().toString(), false);
			//Constants.BFTResultAlreadyApplied = true;
		}
	}
}
