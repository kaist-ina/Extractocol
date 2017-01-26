package soot.jimple.infoflow.cfg;

import java.util.ArrayList;
import java.util.List;

import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.Kind;

/**
 * Class for finding java.lang.Runnable edges so that we get complete callgraphs
 * 
 * @author zemisolsol at KAIST
 *
 */
public class ThreadEdgeAnalyzer {

	public void threadRunnables() {
		
		// from all classes
		for (SootClass sc : Scene.v().getClasses()) {
			for (SootMethod sm : sc.getMethods()) {
				if (sm.hasActiveBody()) {
					for (Unit unit : sm.retrieveActiveBody().getUnits()) {
						Stmt stmt = (Stmt) unit;
						if (stmt.containsInvokeExpr() && 
								isThreadCall(stmt.getInvokeExpr().getMethod().getSignature())) {
							
							// If the given call statement has edges, we ignore this statement.
							if (Scene.v().getCallGraph().edgesOutOf(unit).hasNext())
								continue;
							
							// else, find corresponding edge
							Edge newEdge = findEdge(sm, stmt, stmt.getInvokeExpr().getArg(0));
							// and, add edge
							if (newEdge != null) {
								Scene.v().getCallGraph().addEdge(newEdge);
								
								System.out.println("[*] add (unit) : "+unit);
								System.out.println("    add (method) : "+sm);
								System.out.println("\t[+] callee : "+newEdge.getTgt());
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Find a target method and make a new edge
	 * @param src Source method
	 * @param srcUnit Source statement
	 * @param param Runnable parameter
	 * @return
	 */
	private Edge findEdge(SootMethod src, Stmt srcUnit, Value param) {
		
		// find callee
		SootMethod target = Scene.v().getSootClass(param.getType().toString()).getMethodByName("run");
		
		Edge edge = null;
		if (target != null)
			edge = new Edge(src, srcUnit, target, Kind.EXECUTOR);
		
		if (edge != null)
			return edge;
		else
			return null;
	}

	public void update() {
		// Update CallGraph
		CallGraphBuilder cgBuilder = new CallGraphBuilder(DumbPointerAnalysis.v());
		cgBuilder.build();
		CallGraph callGraph = Scene.v().getCallGraph();
		Scene.v().setCallGraph(callGraph);
		
		// Recreate the exception throw analysis
		Scene.v().getDefaultThrowAnalysis();
		
		// Invalidate the list of reachable methods. It will automatically be recreated
		// on the next call to "getReachableMethods".
		Scene.v().getReachableMethods();
		
		// Update the class hierarchy
		Scene.v().getActiveHierarchy();
		
		List<MethodOrMethodContext> eps = new ArrayList<MethodOrMethodContext>();
		eps.addAll(Scene.v().getEntryPoints());
		ReachableMethods reachables = new ReachableMethods(callGraph, eps.iterator());
		reachables.update();
	}
	
	/**
	 * Checks whether the given invocation belongs to a thread call
	 * @param callStmt The call statement name to check
	 * @return True if the given call statement belongs to a thread call,
	 * otherwise false
	 */
	public static boolean isThreadCall(String callStmt) {
		return callStmt.equals("<java.util.concurrent.ExecutorService: void execute(java.lang.Runnable)>")
				|| callStmt.equals("<java.util.concurrent.ExecutorService: java.util.concurrent.Future submit(java.lang.Runnable)>");
	}
}
