
package ResponseEp_Extractor.Volley;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import soot.jimple.SpecialInvokeExpr;
import soot.options.Options;

public class BuzzFeed_Case
{

	public static void main(String args[]) throws IOException
	{
		/*
		 * Arg0 : TargetClassName Arg1 : Sig Method
		 */
		final ArrayList<String> SigMethod = new ArrayList<String>();
		final ArrayList<String> ApiList = new ArrayList<String>();
	

		// Variables
		Constants.apkName = "buzzfeed";
		/*
		 * Start Soot Main
		 */
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_allow_phantom_refs(true);

		final ArrayList<EPCandidate> EpList = new ArrayList<EPCandidate>();

		PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer()
		{
			boolean isCheck = false;
			
			protected void FoundApis()
			{
				SootClass sc = Scene.v().getSootClass("com.buzzfeed.android.util.BuzzApiClient");
				
				for (SootMethod sm : sc.getMethods())
				{
					for (Unit ut : sm.getActiveBody().getUnits())
					{
						if (ut.toString().contains("<com.android.volley.RequestQueue: com.android.volley.Request add(com.android.volley.Request)>"))
							ApiList.add(sm.getSignature());
					}
				}
			}
			
			@Override
			protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options)
			{
				if (!isCheck)
				{
					FoundApis();
					isCheck = true;
				}
				
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
							if (ApiList.contains(stmt.getInvokeExpr().getMethodRef().getSignature()))
							{
								for (Value local : b.getMethod().getActiveBody().getLocals())
								{
									SootClass declaredClass = Scene.v().getSootClass(local.getType().toString());
									if (declaredClass.implementsInterface("com.android.volley.Response$Listener"))
									{
										System.out.println(b.getMethod().getSignature());
										System.out.println("Volley Origin Listner Found!!" + " " + local.toString());
										SootClass VolleyListner = Scene.v().getSootClass(local.getType().toString());
										
										for (SootMethod sm : VolleyListner.getMethods())
										{
											if (sm.getName().equals("onResponse"))
												if (sm.getParameterType(0).toString().equals("java.lang.Object"))
												{
													EPcan.setEPSig(sm.getSignature());
													EPcan.setDPStmt("$r1 := @parameter0: java.lang.Object");
												}
										}
									}
								}
							}
						}

						public void caseAssignStmt(AssignStmt stmt)
						{
							
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
