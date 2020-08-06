
package ResponseEp_Extractor.Rx;

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
import soot.options.Options;

public class Rx_Case
{

	public static void main(String args[]) throws IOException
	{
		/*
		 * Arg0 : TargetClassName Arg1 : Sig Method
		 */
		final ArrayList<String> SigMethod = new ArrayList<String>();
		SigMethod.add(
				"<com.abtnprojects.ambatana.data.mapper.entity.ResponseMapper: java.lang.Object getBodyAs(retrofit.client.Response,java.lang.Class)>");

		// Variables
		Constants.apkName = "letgo";
		final ArrayList<SootMethod> ApiList = new ArrayList<SootMethod>();

		/*
		 * Start Soot Main
		 * 
		 * First, we should extract restapi list in the
		 * com.abtnprojects.ambatana.data.datasource.network.RestApi class.
		 * 
		 */
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_allow_phantom_refs(true);

		final ArrayList<EPCandidate> EpList = new ArrayList<EPCandidate>();

		PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer()
		{

			boolean hasApis = false;

			@Override
			protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options)
			{

				if (!hasApis)
				{
					SootClass sc = Scene.v().getSootClass("com.abtnprojects.ambatana.data.datasource.network.RestApi");
					ApiList.addAll(sc.getMethods());

					System.out.println("API List : " + ApiList);
					hasApis = true;
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

						}

						public void caseAssignStmt(AssignStmt stmt)
						{
							if (stmt.containsInvokeExpr())
							{
								if (ApiList.contains(stmt.getInvokeExpr().getMethod()))
								{
									// System.out.println("Found Rest API : " +
									// stmt.toString());

									// All Local Variables
									// this method extend rx.functions.Func1 ?
									SootClass rxClass = Scene.v().getSootClass("rx.functions.Func1");
									for (Value lc : b.getMethod().getActiveBody().getLocals())
									{
										SootClass thisclass = Scene.v().getSootClass(lc.getType().toString());

										if (!thisclass.isConcrete())
											continue;

										for (SootMethod sm : thisclass.getMethods())
										{
											if (sm.getName().equals("call"))
											{
												if (sm.getParameterType(0).toString().equals("retrofit.client.Response"))
												{
													for (Unit ut : sm.getActiveBody().getUnits())
													{
														if (ut instanceof AssignStmt)
														{
															AssignStmt as = (AssignStmt) ut;

															if (as.containsInvokeExpr())
																if (SigMethod.contains(as.getInvokeExpr().getMethodRef().getSignature()))
																{
																	EPCandidate EPCan = new EPCandidate();
																	EPCan.setDPStmt("$r1 := @parameter0: retrofit.client.Response");
																	EPCan.setEPSig(sm.getSignature());
																	EpList.add(EPCan);
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
			 System.out.println("DPStmt : " + epc.getDPStmt());
			 System.out.println("");
		}	

		ExtractorUtils.WriteEPs(EpList);
	}
}
