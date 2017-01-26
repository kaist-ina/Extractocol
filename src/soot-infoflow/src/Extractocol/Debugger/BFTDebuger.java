package Extractocol.Debugger;

import java.util.ArrayList;
import java.util.HashMap;

import Extractocol.BufferExtractor_Request.Basics.BFNode;

public class BFTDebuger {
	static void getUrlofBFN( HashMap<String, ArrayList<BFNode>> BFTtable , String key )
	{
		ArrayList<BFNode> arrBFNList = BFTtable.get(key);
		
		if (arrBFNList == null)
				return;
		
		for (int i = 0 ; i < arrBFNList.size() ; i++)
		{
			BFNode bfn = arrBFNList.get(i);		
//			System.out.println("idx of BFN List : " + i );
			System.out.println("Url : " + bfn.getStringForUrl());
		}
	}
}
