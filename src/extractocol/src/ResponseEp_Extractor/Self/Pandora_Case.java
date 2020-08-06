
package ResponseEp_Extractor.Self;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import ResponseEp_Extractor.EPCandidate;
import ResponseEp_Extractor.ExtractorUtils;
import extractocol.Constants;
import soot.Body;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;

public class Pandora_Case
{
	public static void main(String args[]) throws IOException
	{
		/*
		 * Arg0 : TargetClassName Arg1 : Sig Method
		 */
		final String TargetClass = "p.ee.x.jimple";
		final ArrayList<String> SigMethod = new ArrayList<String>();
		SigMethod.add("<p.ee.u: org.json.JSONObject d(java.lang.String,java.util.Hashtable,java.util.Hashtable,int)>");
		SigMethod.add("<p.ee.u: org.json.JSONObject c(java.lang.String,java.util.Hashtable,java.util.Hashtable,int)>");

		// Variables
		Constants.apkName = "pandora";
		SootClass sc = ExtractorUtils.readClass(TargetClass);
		final ArrayList<EPCandidate> EpList = new ArrayList<EPCandidate>();
		int count = 0;

		for (SootMethod sm : sc.getMethods())
		{
			final Body b = sm.getActiveBody();

			final PatchingChain<Unit> units = b.getUnits();
			final EPCandidate EPcan = new EPCandidate();

			// important to use snapshotIterator here
			for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();)
			{
				final Unit u = iter.next();
				u.apply(new AbstractStmtSwitch()
				{

					public void caseInvokeStmt(InvokeStmt stmt)
					{
						if (SigMethod.contains(stmt.getInvokeExpr().getMethodRef().getSignature()))
						{
							// Base Taint Case!
							EPcan.hasEPSig = false;
							EPcan.setEPandDPStmt(b.getMethod().getSignature(), stmt.toString());
							System.out.println("this Ep has SigMethod but, It's Base Taint Case");
						}
					}

					public void caseAssignStmt(AssignStmt stmt)
					{
						if (stmt.containsInvokeExpr())
							if (SigMethod.contains(stmt.getInvokeExpr().getMethodRef().getSignature()))
							{
								// Normal Case!
								EPcan.hasEPSig = true;
								EPcan.setEPSig(b.getMethod().getSignature());
								EPcan.setDPStmt(stmt.toString());
							}
					}

					public void caseReturnStmt(ReturnStmt stmt)
					{
						if (stmt.getOp().getType().toString().contains("JSONObject"))
						{
							EPcan.isreturnConsiderCase = true;
						}
					}
				});
			}


			if (EPcan.getEPSig() != null)
			{
				if (EPcan.isreturnConsiderCase)
					System.out.println("This EP return JSONObject.");
				
				System.out.println("EP[" + count++ + "] : " + EPcan.getEPSig());
				System.out.println("Dpstmt : " + EPcan.getDPStmt());
				System.out.println("");
			}
			
		}
		ExtractorUtils.WriteEPs(EpList);
	}
}
