package CodeInjection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import DP.*;
import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.util.Chain;


/**
 * 1. package name: CodeInjection
 * 2. type name: AppLogger.java
 * 3. writer: Hyun Ho
 * 4. description: Classify an DP which third party use it and add DPHandlers appropriately  
 */
public class AppLogger {
	
	private static AppLogger _Instance = new AppLogger();
	private static DPHandler _DPHandlers = null; 
	
	private AppLogger(){};
	
	public static AppLogger getInstance()
	{
		return _Instance;
	}
	
	public void HandleAssignStmt(Body b, PatchingChain<Unit> units, Unit u, AssignStmt stmt)
	{
		if(stmt.containsInvokeExpr())
		{
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			String DeclaringClass = invokeExpr.getMethod().getDeclaringClass().toString();

			//ch.boye library 
			if(DeclaringClass.startsWith("ch.boye"))
			{
				_DPHandlers = new ch_boye_Handler(new TimeHandler(b, units, u, stmt));
			}
		}
		
		_DPHandlers.run(new ArrayList<Local>());
	}

	
	public void HandleInvokeStmt(Body b, PatchingChain<Unit> units, Unit u, InvokeStmt stmt)
	{
	
	}
}
