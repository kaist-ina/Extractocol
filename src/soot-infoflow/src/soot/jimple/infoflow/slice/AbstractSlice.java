package soot.jimple.infoflow.slice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.Scene;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class AbstractSlice {
	
	private Stmt demarcationStmt;
	private SootMethod demarcationMethod;
	public HashMap<SootMethod, HashSet<Stmt>> slices = new HashMap<SootMethod, HashSet<Stmt>>();
	
	public AbstractSlice(Stmt dpStmt, SootMethod dpMethod, Stmt sliceStmt, SootMethod sliceMethod) {
		demarcationStmt = dpStmt;
		demarcationMethod = dpMethod;
		
		// add first slice
		addSlice(sliceStmt, sliceMethod);
	}

	// has demarcation point?
	public boolean hasDemarcationPoint(Stmt dpStmt, SootMethod dpMethod) {
		return demarcationStmt.equals(dpStmt) && demarcationMethod.equals(dpMethod);
	}

	// add new slice
	public void addSlice(Stmt sliceStmt, SootMethod sliceMethod) {
		if (slices.containsKey(sliceMethod)) {
			HashSet<Stmt> slice = slices.get(sliceMethod);
			slice.add(sliceStmt);
		}
		else {
			HashSet<Stmt> slice = new HashSet<Stmt>();
			slice.add(sliceStmt);
			slices.put(sliceMethod, slice);
		}
	}
	
	// get sliced statements
	public HashSet<Stmt> getSlice(SootMethod slicedMethod) {
		if (slices.containsKey(slicedMethod))
			return slices.get(slicedMethod);
		else
			return null;
	}
	
	// Get potential entry points
	public List<SootMethod> getEPs() {
		List<SootMethod> eps = new ArrayList<SootMethod>();
		CallGraph cfg = Scene.v().getCallGraph();
		
		for (SootMethod callee : slices.keySet()) {
			
//			System.out.println("EP Candidates : " + callee.getSignature());
			
			Iterator<Edge> edgeInto = cfg.edgesInto(callee);
			
			//if ((!edgeInto.hasNext() && !callee.getName().equals("dummyMainMethod")))
				//eps.add(callee);
			//else if (!slices.containsKey(edgeInto.next().getSrc().method()) && !callee.getName().equals("dummyMainMethod") && cfg.edgesOutOf(callee) != null)
			//{
//				System.out.println("EP Candidates : " + callee.getSignature());
				eps.add(callee);
			//}
			
//			while (edgeInto.hasNext()) {
//				SootMethod caller = (SootMethod) edgeInto.next().getSrc();
//				if (!slices.keySet().contains(caller))
//					if (!eps.contains(callee))
//						eps.add(callee);
////				// dummyMainMethod can always be a potential entry point
////				if (caller.getName().equals("dummyMainMethod"))
////					if (!eps.contains(callee))
////						eps.add(callee);
//			}
		}
		return eps;
	}
	
	public HashMap<SootMethod, HashSet<Stmt>> getAllSlices() {
		return slices;
	}
	
	// Get sliced methods
	public Set<SootMethod> getMethods() {
		Set<SootMethod> methods = new HashSet<SootMethod>();
		
		for (SootMethod sm : slices.keySet()) {
			methods.add(sm);
		}
		
		return methods;
	}
	
	// Get demarcation point (Stmt)
	public Stmt getDpStmt() {
		return demarcationStmt;
	}
	
	// Get demarcation point (SootMethod)
	public SootMethod getDpMethod() {
		return demarcationMethod;
	}
}
