
package ResponseEp_Extractor.Self;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import ResponseEp_Extractor.EPCandidate;
import ResponseEp_Extractor.ExtractorUtils;
import extractocol.Constants;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.options.Options;

public class Flipboard_Case
{
	public static void main(String args[]) throws IOException
	{
		/*
		 * Arg0 : TargetClassName Arg1 : Sig Method
		 */
		final ArrayList<String> SigMethod = new ArrayList<String>();
		SigMethod.add("<flipboard.json.JsonSerializationWrapper: java.lang.Object a(java.io.InputStream,java.lang.Class)>");
		// Variables
		Constants.apkName = "flipboard";
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
						public void caseAssignStmt(AssignStmt stmt)
						{
							if (stmt.containsInvokeExpr())
							{
								if (SigMethod.contains(stmt.getInvokeExpr().getMethodRef().getSignature()))
								{
									EPcan.setEPSig(b.getMethod().getSignature());
									EPcan.setDPStmt("$r0 := @parameter0: java.io.InputStream");
									/*
									 * $r3 = virtualinvoke $r2.<com.fasterxml.jackson.databind.ObjectMapper: com.fasterxml.jackson.databind.ObjectReader a(java.lang.Class)>($r1); $r4 = virtualinvoke $r3.<com.fasterxml.jackson.databind.ObjectReader: java.lang.Object a(java.io.InputStream)>($r0);
									 * 
									 */
								}
							}
						}
					});
				}
				if (EPcan.getEPSig() != null)
					EpList.add(EPcan);
			}
		}));
		soot.Main.main(args);
		ExtractorUtils.WriteEPs(EpList);
	}
}
