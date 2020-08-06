package extractocol.common.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ObfuscationSolver.SolverCaller;
import extractocol.Constants;
import extractocol.backend.request.SignatureBuilder_Request;
import extractocol.backend.request.helper.SerializableCFG;
import extractocol.backend.response.helper.SearchMethodHelper;
import extractocol.backend.response.unitHandle.InvokeHandler;
import extractocol.backend.response.unitHandle.Unit_AssignStmt;
import extractocol.backend.response.unitHandle.Unit_IdentityStmt;
import extractocol.backend.response.unitHandle.Unit_InvokeStmt;
import extractocol.backend.response.unitHandle.Unit_ReturnStmt;
import extractocol.common.trackers.ImplicitCallEdgeTracker;
import extractocol.common.valueEntry.ValueEntryTracking;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.VirtualInvokeExpr;

public class InvokeHelper {
	static enum INVOKE_TYPE { STATIC, SPECIAL, VIRTUAL, INTERFACE, NONE };
	static List<String> alreadyInvokedClinitMethods = null;
	
	public static void InitInvokedClinitMethods(){
		if(alreadyInvokedClinitMethods != null)
			alreadyInvokedClinitMethods.clear();
		
		alreadyInvokedClinitMethods = new ArrayList<String>();
	}
	
	private static boolean isAlreadyInvokedClinitMethod(String method){ return alreadyInvokedClinitMethods.contains(method); }
	private static void AddAlreadyInvokedClinitMethod(String method){ alreadyInvokedClinitMethods.add(method); }
	
	public static Set<SootMethod> getSootMethodSet(ValueEntryTracking pb, Unit ut, SootMethod parentSm, boolean isBackward){
		Set<SootMethod> res;
		List<String> candidateCallees;
		
		res = SemanticEdgeConnector(pb, ut);
		if(res.size() > 0)
			return res;
		
		if(MethodFiltering(ut, isBackward))
			return new HashSet<SootMethod>();

		candidateCallees = Constants.sCFG.getCalleesAt(parentSm.getSignature(), ut.toString().replaceAll("_[0-9]+", ""));
		
		INVOKE_TYPE it = getInvokeType(ut);

		switch(it){
		case STATIC: res = StaticInvoke(ut, candidateCallees); break;
		case SPECIAL: res = SpecialInvoke(ut, candidateCallees); break;
		case VIRTUAL:
		case INTERFACE:
			res = VirtualInterfaceInvoke(pb, ut, parentSm, candidateCallees); break;
		default:
			res = new HashSet<SootMethod>();
		}
		
		return res;
	}
	
	private static Set<SootMethod> SemanticEdgeConnector(ValueEntryTracking pb, Unit ut) {
		InvokeExpr ie = getInvokeExpr(ut);
		String deobfusecatedMethod;
		
		// Currently, connects semantic edges only for rx
		if(!ie.getMethod().toString().contains("<rx.Observable:"))
			return new HashSet<SootMethod>();
		
		// deobfusecate the method name
		deobfusecatedMethod = methodDeobfuscation(ie.getMethod());
		
		return ImplicitCallEdgeTracker.SemanticEdge_Rx(pb, ie, deobfusecatedMethod);
	}
	
	
	public static String methodDeobfuscation(SootMethod m) {
		return SolverCaller.M(m);
	}
	
	
	/** Get Soot method set for init method and static invoke
	 * 
	 * @param ut Unit
	 * @param parentSm Soot method that contains 'ut'
	 * @param cfg Call flow graph
	 * @return Soot method set
	 */
	private static Set<SootMethod> StaticInvoke(Unit ut, List<String> candidateCallees)
	{
		Set<SootMethod> SootMethodSet = new HashSet<SootMethod>();
		
		if(candidateCallees != null){
			for(String candidate: candidateCallees){
				if(candidate.contains("clinit")){
					if(isAlreadyInvokedClinitMethod(candidate)) 
						continue;
					else 
						AddAlreadyInvokedClinitMethod(candidate);
				}
				
				SootMethodSet.add(Constants.sCFG.getMethodOf(candidate));
			}
		}
		
		return SootMethodSet;
	}
	
	private static Set<SootMethod> SpecialInvoke(Unit ut, List<String> candidateCallees){
		Set<SootMethod> SootMethodSet = new HashSet<SootMethod>();
		
		if(candidateCallees != null){
			for(String candidate: candidateCallees){
				//if(ut.toString().contains("<init>"))
					SootMethodSet.add(Constants.sCFG.getMethodOf(candidate));
			}
		}
		
		return SootMethodSet;
	}
	
	/** Get Soot method set for virtual/interface/special invoke
	 * 
	 * @param pb Parameter bucket
	 * @param ut Unit
	 * @param parentSm Soot method that contains 'ut'
	 * @param cfg Call flow graph
	 * @return Soot method set
	 */
	private static Set<SootMethod> VirtualInterfaceInvoke(ValueEntryTracking pb, Unit ut, SootMethod parentSm, List<String> candidateCallees)
	{
		Set<SootMethod> SootMethodSet = new HashSet<SootMethod>();		
		
		// return empty list if there is no candidate callee
		if (candidateCallees == null)
			return SootMethodSet;
		
		for(String candidate: candidateCallees){
			if(candidate.contains("clinit")){
				System.out.println("There is clinit!!");
				break;
			}
		}
		
		// get the type of the base variable
		String base = getBase(ut);
		
		// return candidateCallees list if the base variable is null
		try {
			if (pb.varTable.isNull(base)) {
				for(String candidateCallee: candidateCallees)
					SootMethodSet.add(Constants.sCFG.getMethodOf(candidateCallee));
				return SootMethodSet;
			}
				
		}catch (Exception e)
		{
//			System.out.println("[Get type in InvokeHelper] Error Unit: " + ut.toString() + ", Method: " + parentSm);
			return new HashSet<>();
		}

		ArrayList<String> types = new ArrayList<String>();
		// Try to get type of the base variable
		try{
			types = pb.varTable.getTypeof(base);
		}catch (Exception e){
//			System.out.println("[Get type in InvokeHelper] Error Unit: " + ut.toString() + ", Method: " + parentSm);
//			e.printStackTrace();
			return new HashSet<>();
		}
		
		// Return all of the candidate callees if type of the base variable is unknown.
		if(types.size() == 0){
			for (String candidateCallee : candidateCallees)
				SootMethodSet.add(Constants.sCFG.getMethodOf(candidateCallee));
			return SootMethodSet;
		}
		
		// get super classes of the base variable
		for(String type: types){
			ArrayList<String> candidateClasses = new ArrayList<String>();
			List<String> superClasses = Constants.sCFG.getSuperclassOf(type);			
			
			// candidateClasses = its own type + super classes
			candidateClasses.add(type);
			if (superClasses != null)
				if (superClasses.size() > 0)
					candidateClasses.addAll(superClasses);
			
			// check whether one of the candidate classes is in the candidate callee list
			for (String candidateClass : candidateClasses)
			{
				boolean hasFound = false;
				for (String candidateCallee : candidateCallees)
				{
					if (candidateCallee.contains(candidateClass))
					{
						SootMethodSet.add(Constants.sCFG.getMethodOf(candidateCallee));
						hasFound = true;
						break;
					}
				}
				if (hasFound)
					break;
			}
		}
		
		// if it is failed to find candidate classes in the callee list
		if (SootMethodSet.size() == 0)
			// add all of the candidate callees
			for (String candidateCallee : candidateCallees)
				SootMethodSet.add(Constants.sCFG.getMethodOf(candidateCallee));
		
		return SootMethodSet;
	}
	
	/*private static Set<SootMethod> VirtualInterfaceInvoke(ValueEntryTracking pb, Unit ut, SootMethod parentSm, List<String> candidateCallees)
	{
		Set<SootMethod> SootMethodSet = new HashSet<SootMethod>();		
		
		// return empty list if there is no candidate callee
		if (candidateCallees == null)
			return SootMethodSet;
		
		for(String candidate: candidateCallees){
			if(candidate.contains("clinit")){
				System.out.println("There is clinit!!");
				break;
			}
		}
		
		// get the type of the base variable
		String base = getBase(ut);
		
		// return empty list if the base variable is null
		if(pb.varTable.isNull(base))
			return SootMethodSet;
		
		ArrayList<String> types = new ArrayList<String>();
		try{
			types = pb.varTable.getTypeof(base);
		}catch (Exception e){
			System.out.println("Error Unit: " + ut.toString() + ", Method: " + parentSm);
			e.printStackTrace();
		}
		
		//String type = pb.varTable.getTypeof(base, pb.heapTable);
		if(types.size() == 0) types.add(getDeclaringClass(ut));
		
		// get super classes of the base variable
		for(String type: types){
			ArrayList<String> candidateClasses = new ArrayList<String>();
			List<String> superClasses = Constants.sCFG.getSuperclassOf(type);			
			
			// candidateClasses = its own type + super classes
			candidateClasses.add(type);
			if (superClasses != null)
				if (superClasses.size() > 0)
					candidateClasses.addAll(superClasses);
			
			boolean hasFound = false;
			
			// check whether one of the candidate classes is in the candidate callee list
			for (String cantidateClass : candidateClasses)
			{
				hasFound = false;
				for (String cantidateCallee : candidateCallees)
				{
					if (cantidateCallee.contains(cantidateClass))
					{
						SootMethodSet.add(Constants.sCFG.getMethodOf(cantidateCallee));
						hasFound = true;
						break;
					}
				}
				if (hasFound)
					break;
			}
			// if it is failed to find candidate classes in the callee list
			if (!hasFound)
				// add all of the candidate callees
				for (String cantidateCallee : candidateCallees)
				SootMethodSet.add(Constants.sCFG.getMethodOf(cantidateCallee));
		}
		
		return SootMethodSet;
	}*/
	
	
	/********************************************************************/
	/***                            Tools                             ***/
	/********************************************************************/
	private static boolean MethodFiltering(Unit ut, boolean isBackward){
		InvokeExpr ie = getInvokeExpr(ut);
		if(ie == null) return false;
		
		if(isBackward)
			return SignatureBuilder_Request.MethodFiltering(ie.getMethodRef().getSignature());
		else
			return SearchMethodHelper.MethodFiltering(ie.getMethodRef().getSignature());
	}
	
	private static INVOKE_TYPE getInvokeType(Unit ut){
		InvokeExpr ie = getInvokeExpr(ut);
		
		if(ie == null)
			return INVOKE_TYPE.NONE;
		
		if (ie instanceof StaticInvokeExpr)
			return INVOKE_TYPE.STATIC;
		else if (ie instanceof VirtualInvokeExpr)
			return INVOKE_TYPE.VIRTUAL;
		else if (ie instanceof InterfaceInvokeExpr)
			return INVOKE_TYPE.INTERFACE;
		else if (ie instanceof SpecialInvokeExpr)
			return INVOKE_TYPE.SPECIAL;
		
		return INVOKE_TYPE.NONE;
	}
	
	public static InvokeExpr getInvokeExpr(Unit ut){
		if (ut instanceof AssignStmt)
			return (InvokeExpr)((AssignStmt) ut).getRightOp();
		else if (ut instanceof InvokeStmt)
			return ((InvokeStmt) ut).getInvokeExpr();
		else
			return null;
	}
	
	public static InstanceInvokeExpr getInstanceInvokeExpr(Unit ut)
	{
		if (ut instanceof AssignStmt)
			if (((AssignStmt) ut).getInvokeExpr() instanceof InstanceInvokeExpr)
				return ((InstanceInvokeExpr) ((AssignStmt) ut).getInvokeExpr());
			else
				return null;
		else if (((InvokeStmt) ut).getInvokeExpr() instanceof InstanceInvokeExpr)
			return (InstanceInvokeExpr) ((InvokeStmt) ut).getInvokeExpr();
		else
			return null;
	}
	
	public static String getBase(Unit ut)
	{
		InstanceInvokeExpr iie = getInstanceInvokeExpr(ut);
		if (iie == null)
			return null;
		return iie.getBase().toString();
	}
	
	public static String getArg(Unit ut, int idx){
		if(idx < 0) return null;
		
		InvokeExpr ie = getInvokeExpr(ut);
		if(ie == null) return null;
		
		return ie.getArg(idx).toString();
	}

	public static String getDeclaringClass(Unit ut)
	{
		InstanceInvokeExpr iie = getInstanceInvokeExpr(ut);
		if (iie == null)
			return null;
		return iie.getMethodRef().declaringClass().toString();
	}
}
