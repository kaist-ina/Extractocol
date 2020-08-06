package soot.jimple.toolkits.callgraph;

import java.util.ArrayList;

import soot.SootMethod;
import soot.Unit;

public class AddReachableMethods
{
	public static ArrayList<SootMethod> AddReachable = new ArrayList<SootMethod>();
	public static Unit targetUt;
	public static boolean isCalled = false;
	public static CallGraph cg;
}
