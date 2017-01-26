package Extractocol.BufferExtractor_Request.Semantic.Bluetooth.Models.Normal;

import java.util.ArrayList;
import java.util.HashMap;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;
import soot.jimple.Constant;

public class write extends BaseModel {

	@Override
	public void applySemantic(SemanticParameterBucket spb) {
		//Duong
		if (spb.iie.getMethodRef().toString()
				.equals("<java.io.OutputStream: void write(byte[])>"))
		{
			String base = spb.iie.getBase().toString();
			ArrayList<BFNode> list = new ArrayList<>();
			String arg = spb.iie.getArg(0).toString();
			if (spb.iie.getArg(0) instanceof Constant)
			{
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(arg);
				list.add(bfn);
				
			}
			else
			{
				ArrayList<BFNode> al = spb.ub.CopyList(spb.BFTtable.get(arg));
				list.addAll(spb.ub.CopyList(spb.BFTtable.get(arg)));
			}
			
			spb.BFTtable.put(base, list);
			System.out.println("duong "+ spb.ub.printBTFromList(spb.BFTtable, base));
			System.out.println("duong "+ spb.ub.printUrlFromList(spb.BFTtable, base));
		}
	}
}

