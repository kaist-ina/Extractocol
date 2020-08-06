package DP;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.LongType;
import soot.PackManager;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.StringConstant;
import soot.options.Options;

/**
 * 1. package name: DP
 * 2. type name: DPHandlerDecorator.java
 * 3. writer: Hyun Ho
 * 4. description: Abstract class for decorator classes
 */
public abstract class DPHandlerDecorator extends DPHandler {
	protected DPHandler dComponents_;
	
	public DPHandlerDecorator(DPHandler _Object)
	{
		super(_Object);
		this.dComponents_ = _Object;
	}

	//Decorators pass Locals to timehandler, then it prints out all the locals 
	public void run(List<Local> logLocals)
	{
		dComponents_.run(logLocals);
	}
}
