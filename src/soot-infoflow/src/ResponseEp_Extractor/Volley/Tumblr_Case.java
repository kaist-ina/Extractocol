
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
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.options.Options;

public class Tumblr_Case
{

	public static void main(String args[]) throws IOException
	{
		/*
		 * Arg0 : TargetClassName Arg1 : Sig Method
		 */
		final ArrayList<String> SigMethod = new ArrayList<String>();
		SigMethod.add(
				"<com.tumblr.network.VolleyRequestAssembler: com.tumblr.network.request.JsonOAuthRequest assemble(com.tumblr.network.request.ApiRequest,com.android.volley.Response$Listener,com.android.volley.Response$ErrorListener)>");

		// Variables
		Constants.apkName = "tumblr";
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

						}

						public void caseAssignStmt(AssignStmt stmt)
						{
							if (stmt.containsInvokeExpr())
								if (SigMethod.contains(stmt.getInvokeExpr().getMethodRef().getSignature()))
								{
									SootClass thisClass = Scene.v().getSootClass(stmt.getInvokeExpr().getArg(1).getType().toString());
									if (thisClass.isConcrete())
									{
										EPcan.setSuperclasses(Scene.v().getActiveHierarchy().getSuperclassesOfIncluding(thisClass));
										EPcan.setEPSig(stmt.getInvokeExpr().getArg(1).getType().toString());
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
//			System.out.println("EP : " + epc.getEPSig());
//			System.out.println("Sup : " + epc.getSuperclasses());
			for (SootClass sup : epc.getSuperclasses())
			{
				for (SootMethod sm : sup.getMethods())
				{
					if (sm.getName().equals("onResponse") && sm.getParameterType(0).toString().equals("org.json.JSONObject"))
					{
						epc.setEPSig(sm.getSignature());
						epc.setDPStmt("$r1 := @parameter0: org.json.JSONObject");
						System.out.println("Found EP[" + count++ + "] : " + sm.getSignature());
					}
				}
			}
		}
		
		ExtractorUtils.WriteEPs(EpList);
	}
}
