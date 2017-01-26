
package ResponseEp_Extractor.Volley;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import Extractocol.Constants;
import ResponseEp_Extractor.EPCandidate;
import ResponseEp_Extractor.ExtractorUtils;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.PatchingChain;
import soot.Transform;
import soot.Unit;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.options.Options;

public class Pinterest_Case2
{

	public static void main(String args[]) throws IOException
	{
		/*
		 * Arg0 : TargetClassName Arg1 : Sig Method
		 */
		final String MethodSig = "";

		// Variables
		Constants.apkName = "pinterest";
		/*
		 * Start Soot Main
		 */
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_allow_phantom_refs(true);

		final ArrayList<EPCandidate> EpList = new ArrayList<EPCandidate>();

		PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer()
		{

			@Override
			protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options)
			{

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
							if (b.getMethod().getName().equals("onSuccess") && stmt.getInvokeExpr().getMethodRef().getSignature()
									.contains("make(com.pinterest.network.json.PinterestJsonObject)"))
							{
								EPcan.setDPStmt("$r1 := @parameter0: com.pinterest.network.json.PinterestJsonObject");
								EPcan.setEPSig(b.getMethod().getSignature());
							}
						}

						public void caseAssignStmt(AssignStmt stmt)
						{
							if (stmt.containsInvokeExpr())
							{
								if (b.getMethod().getName().equals("onSuccess") && stmt.getInvokeExpr().getMethodRef().getSignature()
										.contains("make(com.pinterest.network.json.PinterestJsonObject)"))
								{
									EPcan.setDPStmt("$r1 := @parameter0: com.pinterest.network.json.PinterestJsonObject");
									EPcan.setEPSig(b.getMethod().getSignature());
								}
							}
						}

						public void caseReturnStmt(ReturnStmt stmt)
						{

						}
					});
				}

				if (EPcan.getEPSig() != null)
					EpList.add(EPcan);
			}
		}));

		soot.Main.main(args);
		int count = 0;
		for (EPCandidate epc : EpList)
		{
			System.out.println("EP[" + count++ + "] : " + epc.getEPSig());
			System.out.println("DPstmt : " + epc.getDPStmt());
		}

		ExtractorUtils.WriteEPs(EpList);
	}
}
