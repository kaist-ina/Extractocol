package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.List;

import extractocol.Constants;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.retrofit.struct.Transaction;
import extractocol.common.retrofit.utils.ResponseFileAnalyzer;
import extractocol.common.valueEntry.ValueEntryTracking;

public class fromBody  extends BaseModel{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals
				("<retrofit.converter.Converter: java.lang.Object fromBody(retrofit.mime.TypedInput,java.lang.reflect.Type>"))
		{
			// 1. get type list form CollectionType arg1
			List<String> typeList = spb.CurrentPB.varTable.getCollectionTypeList(spb.iie.getArg(1).toString());
			
			// 2. parse Java file of arg1 to extract JSON key list
			Transaction t = ResponseFileAnalyzer.Parser(typeList.get(1));
					
			// 3. merge heap table
			ValueEntryTracking.MergeTables(spb.CurrentPB.heapTable, t.Response().VET());
			
			// 4. clear
			t.clear();
		}
	}
}
