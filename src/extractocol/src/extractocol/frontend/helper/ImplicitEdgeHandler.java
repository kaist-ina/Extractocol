package extractocol.frontend.helper;

import java.util.HashSet;
import java.util.Set;

import extractocol.common.helper.InvokeHelper;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;

public class ImplicitEdgeHandler {
	static Set<String> implicitEdges = null;
	
	public static boolean isImplicitEdge(Stmt s) {
		init();
		
		if(!s.containsInvokeExpr())
			return false;
		
		InvokeExpr ie = s.getInvokeExpr();
		
		return implicitEdges.contains(InvokeHelper.methodDeobfuscation(ie.getMethod()));
	}
	
	private static void init() {
		if(implicitEdges != null)
			return;
		
		implicitEdges = new HashSet<String>();
		implicitEdges.add("<rx.Observable: rx.Observable flatMap(rx.functions.Func1)>");
	}
}
