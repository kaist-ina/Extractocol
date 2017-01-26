
package Extractocol.BufferExtractor_Request.Helper;

import Extractocol.Constants;
import Extractocol.Pairing.PairingDPEntry;
import Extractocol.Pairing.SemanticAppliedEntry;

public class ResultPrintHandler
{

	public static void urlResultPrint()
	{
		System.out.println("---------------------------------url information---------------------------------");
		System.out.println("Url Signature : " + Constants.ResultUrls.size());
		int idx = 0;
		for (String result : Constants.ResultUrls)
		{
			System.out.println("URL[" + idx++ + "] : " + result);
		}
//
//		System.out.println("---------------------------------unique set---------------------------------");
//		for (String result : Constants.ResultUrls)
//		{
//			System.out.println(result);
//		}
	}

	public static void responseResultPrint()
	{

		System.out.println("---------------------------------response information---------------------------------");
		for (String key : Constants.pairingInfoContainer.keySet())
		{
			PairingDPEntry dp = Constants.pairingInfoContainer.get(key);
			System.out.println("+ DP     : " + dp.getDP().toString());
			System.out.println("+ DPStmt : " + dp.getDPStmt().toString());
			System.out.println("+ sig : " + dp.getSignature());
			for (SemanticAppliedEntry ep : dp.getEpList())
			{
				System.out.println();
				System.out.println("\t+ EP :     " + ep.getMethod());
				System.out.println("\t+ EPStmt : " + ep.getStmt());
				System.out.println();
			}
		}

//		System.out.println("---------------------------------unique set---------------------------------");
//		for (String key : Constants.pairingInfoContainer.keySet())
//		{
//			PairingDPEntry dp = Constants.pairingInfoContainer.get(key);
//			System.out.println("+ sig : " + dp.getSignature());
//		}
	}
}
