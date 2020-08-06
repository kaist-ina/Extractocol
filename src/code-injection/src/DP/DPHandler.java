package DP;

import java.util.List;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.Unit;
import soot.jimple.Stmt;

/**
 * 1. package name: DP
 * 2. type name: DPHandler.java
 * 3. writer: Hyun Ho
 * 4. description: Super class for implementing decorative pattern  
 */
public abstract class DPHandler {
	protected Body b;
	protected PatchingChain<Unit> units;
	protected Unit u;
	protected Stmt stmt;
	final protected static String Signature = "$EXTRACTOCOL$";
	
	public DPHandler(Body b, PatchingChain<Unit> units, Unit u, Stmt stmt)
	{
		this.b = b;
		this.units = units;
		this.u = u ;
		this.stmt = stmt;
	}
	
	public DPHandler(DPHandler _Object)
	{
		this.b = _Object.b;
		this.units = _Object.units;
		this.u = _Object.u ;
		this.stmt = _Object.stmt;
	}
	
	public abstract void run(List<Local> logLocals);
}
