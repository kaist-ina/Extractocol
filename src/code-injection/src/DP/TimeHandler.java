package DP;

import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;

/**
 * 1. package name: DP
 * 2. type name: TimeDPHandler.java
 * 3. writer: Hyun Ho
 * 4. description: Insert time measuring code  
 */
public class TimeHandler extends DPHandler{
	public TimeHandler(Body b, PatchingChain<Unit> units, Unit u, Stmt stmt) {
		super(b, units, u, stmt);
	}
	public TimeHandler(DPHandler _Object)
	{
		super(_Object);
	}

	public void run(List<Local> logLocals)
	{
		System.out.println("Timehandler is called");

		// Locals to inject 
		Local throwable = AddLocals.Throwable(b);
		Local header_msg = AddLocals.String(b);
		Local time_msg = AddLocals.String(b);
		Local log_msg = AddLocals.String(b);
		Local space_msg = AddLocals.String(b);
		Local reqtime = AddLocals.Long(b);
		Local reqtime_prim = AddLocals.primLong(b);
		Local restime = AddLocals.Long(b);
		Local restime_prim = AddLocals.primLong(b);
		Local latency = AddLocals.Long(b);
		Local latency_prim = AddLocals.primLong(b);

		// SootMethod to use 
		SootMethod to_log = Scene.v().getSootClass("android.util.Log")
				.getMethod("int e(java.lang.String,java.lang.String,java.lang.Throwable)");
		SootMethod to_curr_time = Scene.v().getSootClass("java.lang.System")
				.getMethod("long currentTimeMillis()");
		SootMethod to_toString = Scene.v().getSootClass("java.lang.Long")
				.getMethod("java.lang.String toString()");						
		SootMethod to_longValue = Scene.v().getSootClass("java.lang.Long")
				.getMethod("long longValue()");
		SootMethod to_valueOf = Scene.v().getSootClass("java.lang.Long")
				.getMethod("java.lang.Long valueOf(long)");
		SootMethod to_concat = Scene.v().getSootClass("java.lang.String")
				.getMethod("java.lang.String concat(java.lang.String)");

		//Before execute
		units.insertBefore(Jimple.v().newAssignStmt(throwable, NullConstant.v()), u);
		units.insertBefore(Jimple.v().newAssignStmt(log_msg, StringConstant.v("")), u);
		units.insertBefore(Jimple.v().newAssignStmt(space_msg, StringConstant.v("   ")), u);
		units.insertBefore(Jimple.v().newAssignStmt(header_msg, StringConstant.v(Signature + " ")), u);
		units.insertBefore(Jimple.v().newAssignStmt(reqtime_prim, Jimple.v().newStaticInvokeExpr(to_curr_time.makeRef())), u);
		
		//After execute (reverser order)							
		units.insertAfter(Jimple.v().newInvokeStmt(
				Jimple.v().newStaticInvokeExpr(to_log.makeRef(), header_msg, log_msg, throwable)), u);
		
		for(Iterator<Local> iter = logLocals.iterator(); iter.hasNext();)
		{
			Local lc = iter.next();
			if(!lc.getType().toString().equals("java.lang.String"))
			{
				System.err.println("ERROR: ALL LOCALS IN LOGLOCALS SHOULD HAVE TYPE JAVA.LANG.STRING");
			}
			else
			{
				units.insertAfter(Jimple.v().newAssignStmt(log_msg, Jimple.v().newVirtualInvokeExpr(log_msg, to_concat.makeRef(), lc)), u);
				units.insertAfter(Jimple.v().newAssignStmt(log_msg, Jimple.v().newVirtualInvokeExpr(log_msg, to_concat.makeRef(), space_msg)), u);
			}
		}
		
		units.insertAfter(Jimple.v().newAssignStmt(log_msg, Jimple.v().newVirtualInvokeExpr(log_msg, to_concat.makeRef(), time_msg)), u);
		units.insertAfter(Jimple.v().newAssignStmt(time_msg, Jimple.v().newVirtualInvokeExpr(latency, to_toString.makeRef())), u);
		units.insertAfter(Jimple.v().newAssignStmt(latency, Jimple.v().newStaticInvokeExpr(to_valueOf.makeRef(), latency_prim)), u);
		units.insertAfter(Jimple.v().newAssignStmt(latency_prim, Jimple.v().newSubExpr(restime_prim, reqtime_prim)), u);
		units.insertAfter(Jimple.v().newAssignStmt(log_msg, Jimple.v().newVirtualInvokeExpr(log_msg, to_concat.makeRef(), space_msg)), u);
		units.insertAfter(Jimple.v().newAssignStmt(log_msg, Jimple.v().newVirtualInvokeExpr(log_msg, to_concat.makeRef(), time_msg)), u);
		units.insertAfter(Jimple.v().newAssignStmt(time_msg, Jimple.v().newVirtualInvokeExpr(restime, to_toString.makeRef())), u);
		units.insertAfter(Jimple.v().newAssignStmt(log_msg, Jimple.v().newVirtualInvokeExpr(log_msg, to_concat.makeRef(), space_msg)), u);
		units.insertAfter(Jimple.v().newAssignStmt(log_msg, Jimple.v().newVirtualInvokeExpr(log_msg, to_concat.makeRef(), time_msg)), u);		
		units.insertAfter(Jimple.v().newAssignStmt(time_msg, Jimple.v().newVirtualInvokeExpr(reqtime, to_toString.makeRef())), u);
		units.insertAfter(Jimple.v().newAssignStmt(reqtime, Jimple.v().newStaticInvokeExpr(to_valueOf.makeRef(), reqtime_prim)), u);
		units.insertAfter(Jimple.v().newAssignStmt(restime, Jimple.v().newStaticInvokeExpr(to_valueOf.makeRef(), restime_prim)), u);
		units.insertAfter(Jimple.v().newAssignStmt(restime_prim, Jimple.v().newStaticInvokeExpr(to_curr_time.makeRef())), u);
	}
}
