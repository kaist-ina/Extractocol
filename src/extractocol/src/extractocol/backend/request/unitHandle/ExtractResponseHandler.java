
package extractocol.backend.request.unitHandle;

import java.io.IOException;
import java.util.ArrayList;

import ResponseEp_Extractor.EPCandidate;
import ResponseEp_Extractor.ExtractorUtils;
import extractocol.Constants;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;

public class ExtractResponseHandler
{
	private static ArrayList<EPCandidate> EpList = new ArrayList<EPCandidate>();
	public static boolean ExtractedResponseEP()
	{
		if (EpList.size() > 0)
			return true;
		else
			return false;
	}
	public static void ExtractResponseEP(Unit ut)
	{
		if (ut.toString().contains("com.external.androidquery.a"))
			ForBeeframework(ut);
//		else if (ut.toString().contains(": rx.a get("))
//			ForRx(ut);
		else if (ut.toString().contains("<com.android.volley.RequestQueue: com.android.volley.Request add(com.android.volley.Request)>"))
			ForVolly(ut);
	}
	private static void ForVolly(Unit ut)
	{
				
	}
	private static void ForRx(Unit ut)
	{
		if (ut instanceof AssignStmt)
		{
			AssignStmt stmt = (AssignStmt) ut;
			
			if (stmt.getInvokeExpr().getMethodRef().getSignature().contains(": rx.a get("))
			{
				if (stmt.getInvokeExpr().getArgCount() == 3)
				{
					String classname = stmt.getInvokeExpr().getArg(2).toString().replaceAll("/", ".").replaceAll("class \"", "").replaceAll("\"", "") + "__JsonHelper";
					SootClass real = Scene.v().getSootClass(classname);
					for (SootMethod sm : real.getMethods())
					{
						if (sm.getName().equals("parseFromJson") && sm.getParameterType(0).toString().equals("com.fasterxml.jackson.core.JsonParser"))
						{
							EPCandidate EPcan = new EPCandidate();
							EPcan.setEPandDPStmt(sm.getSignature(), "$r0 := @parameter0: com.fasterxml.jackson.core.JsonParser");
							EpList.add(EPcan);
						}
					}
				}
//				else
//				{
//					EPCandidate EPcan = new EPCandidate();
//					EPcan.setEPandDPStmt(b.getMethod().getSignature(), "casd");
//					EpList.add(EPcan);
//				}
			}
		}
	}
	private static void ForBeeframework(Unit ut)
	{
		if (ut.toString().contains(": com.external.androidquery.a ajax(com.external.androidquery.b.d)>")
				|| ut.toString().contains(": com.external.androidquery.a ajaxProgress(com.external.androidquery.b.d)"))
		{
			if (ut instanceof InvokeStmt)
			{
				InvokeStmt is = (InvokeStmt) ut;
				SootClass thisClass = Scene.v().getSootClass(is.getInvokeExpr().getArg(0).getType().toString());
				if (thisClass.isConcrete())
				{
					for (SootClass sup : Scene.v().getActiveHierarchy().getSuperclassesOfIncluding(thisClass))
					{
						SootClass real = Scene.v().getSootClass(sup.getName());
						for (SootMethod sm : real.getMethods())
						{
							if (!sm.getSignature().startsWith("<com.BeeFramework.b.c") && sm.getName().equals("callback") && sm.getParameterCount() == 3)
							{
								if (sm.getParameterType(0).toString().equals("java.lang.String") && sm.getParameterType(1).toString().equals("org.json.JSONObject"))
								{
									EPCandidate epc = new EPCandidate();
									epc.setEPSig(sm.getSignature());
									epc.setDPStmt("$r2 := @parameter1: org.json.JSONObject");
									EpList.add(epc);
								}
							}
						}
					}
				}
			}
		}
	}
	public static void WrteEPs() throws IOException
	{
		ExtractorUtils.WriteEPs(EpList);
	}
}
