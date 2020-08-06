package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.List;

import extractocol.Constants;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.retrofit.struct.Transaction;
import extractocol.common.retrofit.utils.ResponseFileAnalyzer;
import extractocol.common.valueEntry.ValueEntryTracking;

public class convertValue extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals
				("<com.fasterxml.jackson.databind.ObjectMapper: java.lang.Object convertValue(java.lang.Object,com.fasterxml.jackson.databind.JavaType)>"))
		{
			// 1. get JSON key list from arg0
			List<String> keyList = spb.CurrentPB.varTable.getJSONKeyListOf(spb.iie.getArg(0).toString());
			
			// 2. get type list form CollectionType arg1
			List<String> typeList = spb.CurrentPB.varTable.getCollectionTypeList(spb.iie.getArg(1).toString());
			
			// 3. parse Java file of arg1 to extract JSON key list
			Transaction t = ResponseFileAnalyzer.Parser(typeList.get(1));
					
			// 4. add the JSON key list from arg0 into all JSON key entries of the extracted heap
			t.Response().VET().addJSONKeyListAtFront(keyList);
			
			// 5. merge heap table
			ValueEntryTracking.MergeTables(spb.CurrentPB.heapTable, t.Response().VET());
			
			// 6. clear
			t.clear();
		}
	}
}
