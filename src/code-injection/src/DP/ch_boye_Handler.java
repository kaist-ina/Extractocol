package DP;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.StringConstant;
import soot.util.Chain;

/**
 * 1. package name: DP
 * 2. type name: ch_boye_Handler.java
 * 3. writer: Hyun Ho
 * 4. description: Insert url printing code of ch_boye DP methods
 */
public class ch_boye_Handler extends DPHandlerDecorator{
	public ch_boye_Handler(DPHandler _Object)
	{
		super(_Object);
	}
	
	public void run(List<Local> logLocals)
	{
		System.out.println("ch_boye is called");
		
		if(stmt.containsInvokeExpr())
		{
			InvokeExpr invokeExpr = stmt.getInvokeExpr();
			
			if(invokeExpr.getMethod().getSignature().equals("<ch.boye.httpclientandroidlib.client.HttpClient: ch.boye.httpclientandroidlib.HttpResponse execute(ch.boye.httpclientandroidlib.client.methods.HttpUriRequest,ch.boye.httpclientandroidlib.protocol.HttpContext)>"))
			{
				if(Scene.v().containsClass("ch.boye.httpclientandroidlib.client.methods.HttpRequestBase"));
					Scene.v().addBasicClass("ch.boye.httpclientandroidlib.client.methods.HttpRequestBase", SootClass.SIGNATURES);
					
				Local HttpUriRequest = null;
				Local URI_obj = AddLocals.URI(b);
				Local URI_string = AddLocals.String(b);

				SootMethod HttpUriRequest_getURI = Scene.v().getSootClass("ch.boye.httpclientandroidlib.client.methods.HttpRequestBase")
						.getMethod("java.net.URI getURI()");
				SootMethod URI_toString = Scene.v().getSootClass("java.net.URI")
						.getMethod("java.lang.String toString()");
				
				Chain<Local> Locals = b.getLocals();
				for (Iterator<Local> iter = Locals.snapshotIterator(); iter.hasNext();)
				{
					final Local u = iter.next();

					if (u.getName().equals("$r3") && u.getType().toString().equals("ch.boye.httpclientandroidlib.client.methods.HttpRequestBase"))
						HttpUriRequest = u;
				}
				
				units.insertBefore(Jimple.v().newAssignStmt(URI_obj, Jimple.v().newVirtualInvokeExpr(HttpUriRequest, HttpUriRequest_getURI.makeRef())), u);
				units.insertBefore(Jimple.v().newAssignStmt(URI_string, Jimple.v().newVirtualInvokeExpr(URI_obj, URI_toString.makeRef())), u);
				
				logLocals.add(URI_string);

				// check that we did not mess up the Jimple
				b.validate();
			}
				
		}
		
		super.run(logLocals);
	}

}
