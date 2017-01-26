
package ResponseEp_Extractor.Rx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.options.Options;

public class fivemiles_case2
{

	public static void main(String args[]) throws IOException
	{
		/*
		 * Arg0 : TargetClassName Arg1 : Sig Method
		 */
		final ArrayList<String> SigMethod = new ArrayList<String>();
		SigMethod.add(": rx.a get(java.lang.String,java.util.Map,java.lang.Class)>");
		SigMethod.add(
				"<com.thirdrock.repository.impl.ItemRepositoryImpl: rx.a get(java.lang.String,java.util.Map,com.thirdrock.framework.rest.HttpBodyParser)>");

		// Variables
		Constants.apkName = "5miles";
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
								for (String sig : SigMethod)
								{
									if (stmt.getInvokeExpr().getMethodRef().getSignature().endsWith(sig))
									{
										if (stmt.getInvokeExpr().getArgCount() == 3)
										{

											String classname = stmt.getInvokeExpr().getArg(2).toString().replaceAll("/", ".").replaceAll("class \"", "").replaceAll("\"","") + "__JsonHelper";
											SootClass sc = Scene.v().getSootClass(classname);

											for (SootMethod sm : sc.getMethods())
											{
												if (sm.getName().equals("parseFromJson")
														&& sm.getParameterType(0).toString().equals("com.fasterxml.jackson.core.JsonParser"))
													EPcan.setEPandDPStmt(sm.getSignature(),
															"$r0 := @parameter0: com.fasterxml.jackson.core.JsonParser");
											}
										}

										else
											EPcan.setEPandDPStmt(b.getMethod().getSignature(), "casd");
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
