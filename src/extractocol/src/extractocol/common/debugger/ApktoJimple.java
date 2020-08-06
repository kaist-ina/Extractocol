package extractocol.common.debugger;

import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.options.Options;
public class ApktoJimple
{
	public static void main(String[] args)
	{
		//prefer Android APK files// -src-prec apk
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_allow_phantom_refs(true);

		//output as APK, too//-f J
		//		Options.v().set_output_format(Options.out);

		PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer()
		{
			@Override
			protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options)
			{
//				final PatchingChain<Unit> units = b.getUnits();
//				
//				//important to use snapshotIterator here
//				for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
//					final Unit u = iter.next();
//					u.apply(new AbstractStmtSwitch() {
//						
//						public void caseInvokeStmt(InvokeStmt stmt) {
//							InvokeExpr invokeExpr = stmt.getInvokeExpr();
//							if(invokeExpr.getMethod().getName().equals("call")) {
//								System.out.println(u.toString());
//							}
//						}
//					});
//				}
//				SootMethod sm = Scene.v().getMethod("<com.zillow.android.webservices.volley.ClaimedHomesVolleyRequest: void <init>(com.zillow.android.webservices.volley.ClaimedHomesVolleyRequest$ClaimedHomesAction,com.zillow.android.webservices.volley.ClaimedHomesVolleyRequest$ClaimedHomesListener)>");
//				System.out.println(Scene.v().getCallGraph().edgesInto(sm).next().getSrc());
//				for (Unit u : sm.getActiveBody().getUnits())
//				{
//					if (u.toString().contains("<org.wordpress.android.ui.comments.CommentActions$1: void start()>"))
//					{
//						Iterator<soot.jimple.toolkits.callgraph.Edge> a = Scene.v().getCallGraph().edgesOutOf(u);
//						while (a.hasNext())
//						{
//							System.out.println(a.next().tgt().getSignature());
//						}
//					}
//				}

			}
		}));

		// resolve the PrintStream and System soot-classes
		//		Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
		//        Scene.v().addBasicClass("java.lang.System",SootClass.SIGNATURES);

		soot.Main.main(args);
	}
}
