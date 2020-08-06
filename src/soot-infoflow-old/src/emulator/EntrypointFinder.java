
package emulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import com.gaurav.tree.ArrayListTree;
import com.gaurav.tree.NodeNotFoundException;
import com.gaurav.tree.Tree;

import soot.Local;
import soot.PatchingChain;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.util.SystemClassHandler;
import soot.util.Chain;

public class EntrypointFinder
{

	LinkedHashMap<Unit, List<Unit>> eps = new LinkedHashMap<Unit, List<Unit>>();

	HashSet<SootMethod> caller_m = new HashSet<SootMethod>();

	List<Unit> dPoints = new ArrayList<Unit>();

	IInfoflowCFG iCfg = null;

	int callerDepth = 0;

	public EntrypointFinder(List<Unit> dps, IInfoflowCFG icfg)
	{
		this.dPoints = dps;
		this.iCfg = icfg;

		run();
	}

	private void run()
	{

		for (Unit dp : dPoints)
		{

			SootMethod dpMethod = iCfg.getMethodOf(dp);

			HashSet<SootMethod> callers = new HashSet<SootMethod>();
			findCaller(callers, dpMethod);
			callers.add(dpMethod);

			List<Unit> dpoints = new ArrayList<Unit>();

			for (SootMethod sm : callers)
			{
				PatchingChain<Unit> units = sm.getActiveBody().getUnits();
				for (Unit unit : units)
				{
					Stmt stmt = (Stmt) unit;

					if (stmt.containsInvokeExpr())
					{
						InvokeExpr ie = stmt.getInvokeExpr();
						SootMethod iev = ie.getMethod();
						if (iev.toString().equals("<org.apache.http.client.methods.HttpGet: void <init>(java.lang.String)>")
								|| iev.toString().equals("<org.apache.http.client.methods.HttpPost: void <init>(java.lang.String)>")
								|| iev.toString().equals("<java.net.URL: void <init>(java.lang.String)>"))
						{
							dpoints.add(unit);
						}
					}
				}
			}

			eps.put(dp, dpoints);
		}
	}

	private void findCaller(HashSet<SootMethod> callers, SootMethod dpm)
	{

		Collection<Unit> units = iCfg.getCallersOf(dpm);

		if (!callers.contains(dpm))
		{
			callers.add(dpm);
			for (Unit unit : units)
			{
				// System.out.println("-> "+unit);
				// System.out.println("\t-> "+iCfg.getMethodOf(unit));
				findCaller(callers, iCfg.getMethodOf(unit));
			}
		}
		return;
	}

	// FIXIT I think that this module is not accurate.
	public List<SootMethod> findCallFlow(String demarcationpoint, String entrypoint) throws NodeNotFoundException
	{
		caller_m.clear();
		Tree<SootMethod> tree = new ArrayListTree<SootMethod>();
		tree.add(Scene.v().getMethod(demarcationpoint));
		generateTree(tree, Scene.v().getMethod(demarcationpoint), Scene.v().getMethod(entrypoint));

		// System.out.println("\n\n[*] Entry Point: "+entrypoint);
		List<SootMethod> CallFlow = new ArrayList<SootMethod>();
		// finalEPs.add(Scene.v().getMethod(entrypoint));
		CallFlow.addAll(caller_m);
		// printLeapToRoot(finalEPs, tree, Scene.v().getMethod(entrypoint));
		return CallFlow;
	}

	private void printFinalEP(List<SootMethod> finalEPs)
	{
		System.out.println("Final EPs : " + finalEPs.size());
		for (SootMethod sm : finalEPs)
		{
			System.out.println(sm.getSignature());
		}
	}

	private void printLeapToRoot(List<SootMethod> finalEPs, Tree<SootMethod> tree, SootMethod method) throws NodeNotFoundException
	{
		if (tree.parent(method) != null)
		{
			// System.out.println("\t"+tree.parent(method));
			finalEPs.add(tree.parent(method));
			printLeapToRoot(finalEPs, tree, tree.parent(method));
		}
	}

	@SuppressWarnings("deprecation")
	private void generateTree(Tree<SootMethod> tree, SootMethod callee, SootMethod entrypoint) throws NodeNotFoundException
	{
		Collection<Unit> callers = iCfg.getCallersOf(callee);
		if (!caller_m.contains(callee))
		{
			caller_m.add(callee);
			for (Unit caller : callers)
			{
				// if (iCfg.getMethodOf(caller).equals(entrypoint))
				// System.out.println("FOUND!!!");
				if (!(SystemClassHandler.isClassInSystemPackage(iCfg.getMethodOf(caller).getDeclaringClass().getName())))
				{
					tree.add(callee, iCfg.getMethodOf(caller));
					generateTree(tree, iCfg.getMethodOf(caller), entrypoint);
				}
			}
		} else
		{
			// System.out.println("================================================");
			// System.out.println("This method is contained : " +
			// callee.getSignature());
			// System.out.println("================================================");
		}
		return;
	}

	public LinkedHashMap<Unit, List<Unit>> getEPs()
	{
		return this.eps;
	}
}
