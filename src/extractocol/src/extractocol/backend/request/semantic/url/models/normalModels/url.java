package extractocol.backend.request.semantic.url.models.normalModels;

import extractocol.Constants;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;

/**
 * Created by Administrator on 2017-07-18.
 */
public class url extends BaseModel {
    @Override
    public void applySemantic(SemanticParameterBucket spb) {
        if (spb.iie.getMethodRef().toString().equals("<com.BeeFramework.b.c: java.lang.Object url(java.lang.String)>"))
        {
//            System.out.println("[Beeframework Url] : " + spb.CurrentPB.varTable.getValueEntryList(spb.iie.getArg(0).toString()).GenRegex());
            spb.CurrentPB.varTable.addValueEntryList(spb.iie.getBase().toString(),  spb.iie.getArg(0).toString(), false);
            spb.CurrentPB.varTable.OverWriteValueEntryListFromSrcToDest(spb.ub.strDest, spb.iie.getBase().toString(), false);
            //Constants.BFTResultAlreadyApplied = true;
        }
    }
}
