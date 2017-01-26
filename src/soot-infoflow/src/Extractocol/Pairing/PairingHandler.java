package Extractocol.Pairing;

import java.util.HashMap;
import java.util.List;

import Extractocol.Constants;
import soot.jimple.infoflow.pairing.ReconstructHttpTransaction;
import soot.jimple.infoflow.slice.AbstractSlice;

public class PairingHandler
{
	public static void responsePairingInfoHandler(ReconstructHttpTransaction reconHttpTransacs)
	{
		for(String key : Constants.pairingInfoContainer.keySet()) {
			PairingDPEntry dp = Constants.pairingInfoContainer.get(key);
			if(dp.getEpList().size() > 0) {
				for(SemanticAppliedEntry e : dp.getEpList()) {
					reconHttpTransacs.addPairingInfo(dp.getDPStmt().toString(), dp.getDP().toString(), dp.getSignature(), e.getMethod(), e.getStmt());
				}
			}
			else {
				reconHttpTransacs.addPairingInfo(dp.getDPStmt().toString(), dp.getDP().toString(), dp.getSignature(), dp.getDPStmt().toString(), dp.getDP().toString());
			}
		}
		
		reconHttpTransacs.print();
		reconHttpTransacs.write();
		reconHttpTransacs.makeSourcesAndSinks();		
	}
	
	public static void urlPairingInfoHandler(ReconstructHttpTransaction reconHttpTransacs)
	{
		HashMap<String, Integer> EPSetCount = new HashMap<String, Integer>();

		for(String key : Constants.pairingInfoContainer.keySet()) {
			PairingDPEntry dp = Constants.pairingInfoContainer.get(key);
			for(SemanticAppliedEntry e : dp.getEpList()) {
				String epkey = e.getMethod() + " " + e.getStmt();
				if(EPSetCount.get(epkey) == null) {
					EPSetCount.put(epkey, new Integer(1));
				}
				else {
					EPSetCount.put(epkey, new Integer(2));
				}
			}
		}
		for(String key : Constants.pairingInfoContainer.keySet()) {
			PairingDPEntry dp = Constants.pairingInfoContainer.get(key);
			for(String epkey : EPSetCount.keySet()) {
				Integer i = EPSetCount.get(epkey);
				if(i == 1)
					continue;
				
				for(SemanticAppliedEntry e : dp.getEpList()) {
					if(epkey.equals(e.getMethod() + " " + e.getStmt())) {
						dp.getEpList().remove(e);
						break;
					}
				}
			}
		}
		for(String key : Constants.pairingInfoContainer.keySet()) {
			PairingDPEntry dp = Constants.pairingInfoContainer.get(key);
			if(dp.getEpList().size() > 0) {
				for(SemanticAppliedEntry e : dp.getEpList()) {
					reconHttpTransacs.addPairingInfo(dp.getDPStmt().toString(), dp.getDP().toString(), dp.getSignature(), e.getMethod(), e.getStmt());
				}
			}
			else {
				reconHttpTransacs.addPairingInfo(dp.getDPStmt().toString(), dp.getDP().toString(), dp.getSignature(), dp.getDPStmt().toString(), dp.getDP().toString());
			}
		}
		
		reconHttpTransacs.print();
		reconHttpTransacs.write();
		reconHttpTransacs.makeSourcesAndSinks();
	}
}
