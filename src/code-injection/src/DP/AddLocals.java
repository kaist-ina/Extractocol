package DP;

import soot.Body;
import soot.Local;
import soot.LongType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.jimple.Jimple;

/**
 * 1. package name: DP
 * 2. type name: AddLocals.java
 * 3. writer: Hyun Ho
 * 4. description: Assign an soot's local variable   
 */
public class AddLocals {
	private AddLocals()
	{
		throw new AssertionError();
	}

	static Integer index = new Integer(0); 

	public static Local Throwable(Body body)
	{
		if(Scene.v().containsClass("java.lang.System"))
			Scene.v().addBasicClass("java.lang.System", SootClass.SIGNATURES);
	
		Local tmpThrowable = Jimple.v().newLocal("tmpThrowable" + index.toString(), RefType.v("java.lang.Throwable"));
		body.getLocals().add(tmpThrowable);
		index++;
		return tmpThrowable;
	}

	public static Local String(Body body)
	{
		if(Scene.v().containsClass("java.lang.String"))
			Scene.v().addBasicClass("java.lang.String", SootClass.SIGNATURES);
		
		Local tmpString = Jimple.v().newLocal("tmpString" + index.toString(), RefType.v("java.lang.String"));
		body.getLocals().add(tmpString);
		index++;
		return tmpString;
	}

	public static Local Long(Body body)
	{
		if(Scene.v().containsClass("java.lang.Long"))
			Scene.v().addBasicClass("java.lang.Long", SootClass.SIGNATURES);
		
		Local tmpLong = Jimple.v().newLocal("tmpLong" + index.toString(), RefType.v("java.lang.Long"));
		body.getLocals().add(tmpLong);
		index++;
		return tmpLong;
	}
	
	public static Local primLong(Body body)
	{
		String Local_name = "tmpprimLong" + index.toString();
		Local tmpLong_prim = Jimple.v().newLocal(Local_name, LongType.v());
		body.getLocals().add(tmpLong_prim);
		index++;
		return tmpLong_prim;
	}

	public static Local URI(Body body)
	{
		if(Scene.v().containsClass("java.net.URI"))
			Scene.v().addBasicClass("java.net.URI", SootClass.SIGNATURES);
		
		Local tmpURI = Jimple.v().newLocal("tmpURI" + index.toString(), RefType.v("java.net.URI"));
		body.getLocals().add(tmpURI);
		index++;
		return tmpURI;
	}

	public static Local HttpUriRequest(Body body)
	{
		if(Scene.v().containsClass("ch.boye.httpclientandroidlib.client.methods.HttpUriRequest"))
			Scene.v().addBasicClass("ch.boye.httpclientandroidlib.client.methods.HttpUriRequest", SootClass.SIGNATURES);
		
		Local tmpHttpUriRequest = Jimple.v().newLocal("tmpHttpUriRequest" + index.toString(), RefType.v("ch.boye.httpclientandroidlib.client.methods.HttpUriRequest"));
		body.getLocals().add(tmpHttpUriRequest);
		index++;
		return tmpHttpUriRequest;
	}
}
