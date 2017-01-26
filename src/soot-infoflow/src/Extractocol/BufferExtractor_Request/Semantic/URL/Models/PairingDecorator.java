
package Extractocol.BufferExtractor_Request.Semantic.URL.Models;

import org.json.simple.JSONObject;

import Extractocol.Constants;
import Extractocol.Pairing.PairingDPEntry;
import Extractocol.Pairing.SemanticAppliedEntry;

public class PairingDecorator extends AbstractDecorator
{
	@SuppressWarnings("unchecked")
	public void applySemantic(SemanticParameterBucket spb)
	{
		//if (spb.iie != null) // What about sie? -- Byungkwon
		if (spb.iie != null)
		{
			// In order to track the statements to which a semantic model is applied.
			// This list will be cleared when the corresponding URL has been completely constructed. (You can see printUrl() in UrlBuilder.java.)
			// Added by Byungkwon
			if(!PairingDPEntry.isEntryIncluded(new SemanticAppliedEntry(spb.sm.getSignature(), spb.iie.getMethodRef().getSignature()), Constants.SemanticMethodAndStmt))
				Constants.SemanticMethodAndStmt.add(new SemanticAppliedEntry(spb.sm.getSignature(), spb.iie.getMethodRef().getSignature()));
			
			// The code below would be deleted soon.
			spb.ub.CurrentPb.ep_methods.add(spb.sm.getSignature());
			Constants.PairInfo.Add_ep_method(spb.ub.CurrentPb.requestEntry, spb.ub.CurrentPb.ep_methods);
			spb.ub.CurrentPb.epstmts.add(spb.iie.getMethodRef().getSignature());
			Constants.PairInfo.Add_ep_stmts(spb.ub.CurrentPb.requestEntry, spb.ub.CurrentPb.epstmts);
		}
		
		if (spb.sie != null)
		{
			// In order to track the statements to which a semantic model is applied.
			// This list will be cleared when the corresponding URL has been completely constructed. (You can see printUrl() in UrlBuilder.java.)
			// Added by Byungkwon
			if(!PairingDPEntry.isEntryIncluded(new SemanticAppliedEntry(spb.sm.getSignature(), spb.sie.getMethodRef().getSignature()), Constants.SemanticMethodAndStmt))
				Constants.SemanticMethodAndStmt.add(new SemanticAppliedEntry(spb.sm.getSignature(), spb.sie.getMethodRef().getSignature()));
		}
		
		// This should be located below the above code. -- Byungkwon
		super.applySemantic(spb);
	}
}
