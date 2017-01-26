
package ResponseEp_Extractor.Submit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.Helper.CFGSerializer;
import Extractocol.BufferExtractor_Request.Helper.SerializableCFG;
import Extractocol.BufferExtractor_Request.Helper.SerializableCFG.SerializableUnit;
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

public class geekandwish_Case
{

	public static void main(String args[]) throws IOException
	{
		/*
		 * Arg0 : TargetClassName Arg1 : Sig Method 5miles - BeeFramework
		 */
		final ArrayList<String> SigMethod = new ArrayList<String>();
		SigMethod.add(
				"<com.contextlogic.wish.api.core.WishApi: com.contextlogic.wish.api.core.WishApiRequest apiRequest(java.lang.String,com.contextlogic.wish.http.HttpRequestParams,com.contextlogic.wish.api.core.WishApiCallback)>");

		// Variables
		Constants.apkName = "wish";
		CFGSerializer CFGs = new CFGSerializer();
		final SerializableCFG dsCFG = CFGs.Deserialize(false);

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

						public void caseAssignStmt(AssignStmt stmt)
						{
							if (stmt.containsInvokeExpr())
								if (SigMethod.contains(stmt.getInvokeExpr().getMethodRef().getSignature()))
								{
									if (dsCFG.getCallersOf(b.getMethod().getSignature()) != null)
									{

										SerializableUnit su = dsCFG.getCallersOf(b.getMethod().getSignature()).get(0);
										SootMethod ParentMethod = Scene.v().getMethod(su.ParentMethod);

										for (Unit ut : ParentMethod.getActiveBody().getUnits())
										{
											if (ut.toString().equals(su.Ut))
											{
												/*
												 * Wish case $r7 = virtualinvoke
												 * $r6.<com.contextlogic.wish.
												 * api. core.WishApi:
												 * com.contextlogic.wish.api.
												 * core. WishApiRequest
												 * addToWishlist
												 * (java.util.ArrayList,java.
												 * lang.
												 * String,com.contextlogic.wish.
												 * api. core.WishApiCallback)>
												 * ($r1, $r2, $r5)
												 */
												AssignStmt as = (AssignStmt) ut;
												int callbackObjectAt = as.getInvokeExpr().getArgCount() - 1;
												SootClass thisClass = Scene.v()
														.getSootClass(as.getInvokeExpr().getArg(callbackObjectAt).getType().toString());
												if (thisClass.isConcrete())
												{
													EPcan.setSuperclasses(Scene.v().getActiveHierarchy().getSuperclassesOfIncluding(thisClass));
													EPcan.setEPSig(as.getInvokeExpr().getArg(callbackObjectAt).getType().toString());

													int count = 0;
													for (EPCandidate epc : EpList)
													{
														// System.out.println("EP
														// : " +
														// epc.getEPSig());
														// System.out.println("Sup
														// : " +
														// epc.getSuperclasses());
														for (SootClass sup : epc.getSuperclasses())
														{
															for (SootMethod sm : sup.getMethods())
															{
																// void
																// onSuccess(com.contextlogic.wish.api.core.WishApiRequest,com.contextlogic.wish.api.core.WishApiResponse)
																if (sm.getName().equals("onSuccess") && sm.getParameterCount() == 2)
																{
																	epc.setEPSig(sm.getSignature());
																	epc.setDPStmt("$r2 := @parameter1: com.contextlogic.wish.api.core.WishApiResponse");
																	// Extracting
																	// DPstmt
//																	for (Unit dpcandidate : sm.getActiveBody().getUnits())
//																	{
//																		if (dpcandidate instanceof AssignStmt)
//																		{
//																			AssignStmt DPas = (AssignStmt) dpcandidate;
//
//																			if (DPas.getLeftOp().getType().toString().equals("org.json.JSONObject"))
//																				epc.setDPStmt(dpcandidate.toString());
//																		}
//																	}
																}
															}
														}
													}

												}
											}
										}
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
		int count = 0;
		for (EPCandidate epc : EpList)
		{
			if (epc.getDPStmt() != null)
			{
				System.out.println("Found EP[" + count++ + "] : " + epc.getEPSig());
				System.out.println("DPStmt : " + epc.getDPStmt());
			}
		}
		
		ExtractorUtils.WriteEPs(EpList);
	}
}
