package extractocol.backend.request.semantic.url.models.normalModels;

import java.util.List;

import extractocol.Constants;
import extractocol.backend.request.semantic.url.models.BaseModel;
import extractocol.backend.request.semantic.url.models.SemanticParameterBucket;
import extractocol.common.outputs.BackendOutput;
import soot.toDex.instructions.SparseSwitchPayload;
import soot.jimple.Constant;

public class setHeader extends BaseModel {
	@Override
	public void applySemantic(SemanticParameterBucket spb) {
		String key = null;
		String value = null;
		
		/* ch.boye header semantic model */
		if (spb.iie.getMethodRef().getSignature().startsWith("<ch.boye")) {
			/* setHeader(String name, String value) */
			if (spb.iie.getMethodRef().getSubSignature().toString().equals("void setHeader(java.lang.String,java.lang.String)"))
			{
				/*if (spb.iie.getArg(0) instanceof Constant) {
					key = spb.iie.getArg(0).toString();
					key = key.replace("\"", "");
				} else {
					key = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(0).toString());
					if (key.isEmpty())
						key = "$BRTTABLE NOINFO$";
				}

				if (spb.iie.getArg(1) instanceof Constant) {
					value = spb.iie.getArg(1).toString();
					value = value.replace("\"", "");
				} else {
					value = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(1).toString());
					if (value.isEmpty())
						key = "$BRTTABLE NOINFO$";
				}
				BackendOutput.reqRespInfo.reqie.addHeader(key, value);*/
			}
			
			/* TODO setHeader(Header header) */
		}

		/* org.apach header semantic model */
		else if (spb.iie.getMethodRef().getSignature().startsWith("<org.apache")) {
			/* setHeader(String name, String value) */
			if (spb.iie.getMethodRef().getSubSignature().toString().equals("void setHeader(java.lang.String,java.lang.String)"))
			{
				/*if (spb.iie.getArg(0) instanceof Constant) {
					key = spb.iie.getArg(0).toString();
				} else {
					key = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(0).toString());
					if (key.isEmpty())
						key = "$BRTTABLE NOINFO$";
				}

				if (spb.iie.getArg(1) instanceof Constant) {
					value = spb.iie.getArg(1).toString();
				} else {
					value = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(1).toString());
					if (value.isEmpty())
						key = "$BRTTABLE NOINFO$";
				}

				BackendOutput.reqRespInfo.reqie.addHeader(key, value);*/
			}

			/* TODO setHeader(Header header) */
		}
	}
}
