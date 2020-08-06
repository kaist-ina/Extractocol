package extractocol.backend.request.semantic.url.models.normalModels;

import extractocol.Constants;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;

public class constructCollectionType extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString().equals
				("<com.fasterxml.jackson.databind.type.TypeFactory: com.fasterxml.jackson.databind.type.CollectionType constructCollectionType(java.lang.Class,java.lang.Class)>"))
		{
			// 1. get args and dest
			String arg0 = spb.iie.getArg(0).toString();
			String arg1 = spb.iie.getArg(1).toString();
			String dest = spb.ub.strDest;
			
			// 2. get class name
			String class0 = spb.CurrentPB.varTable.getClassNameOf(arg0);
			String class1 = spb.CurrentPB.varTable.getClassNameOf(arg1);
			
			// 3. add the class name into CollectionType entry
			spb.CurrentPB.varTable.addCollectionTypeEntry(dest, class0, false);
			spb.CurrentPB.varTable.addCollectionTypeEntry(dest, class1, false);
		}
	}
}
