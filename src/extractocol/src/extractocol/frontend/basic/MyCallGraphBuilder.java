package extractocol.frontend.basic;

import java.util.*;

import extractocol.Constants;
import extractocol.common.trackers.ImplicitCallEdgeTracker;
import soot.Body;
import soot.CallbackCandidateFinder;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Unit;
import soot.Value;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.NumberedString;
import soot.jimple.*;


public class MyCallGraphBuilder {
	static CallGraph cg = null;
	static List<Edge> edgeList = new LinkedList<Edge>();
	static List<Edge> clinitEdgeList = new LinkedList<Edge>();
	static Set<String> excludedCalls = new HashSet<String>();
	
	public static CallGraph getCG() {return cg;}
	public static boolean hasCGBeenBuilt() { return cg != null; }
	
	public static CallGraph buildAndGet(){
		// Retrieve active body of all methods
		if(!CheckActiveBodies())
			retrieveActiveBodies();
		
		// Not need to build call graph again if it has been already constructed (case of Heap handling)
		if(cg != null)
			return cg;
		
		cg = new CallGraph();
		
		// Build the call graph
		buildCallGraph();
		
		return cg;
	}
	
	/** Build the call graph
	 * 
	 * YOU SHOULD USE getMethodRef() NOT getMethod() BECAUSE methodList OF SootClass CAN BE CHANGED IN getMethod().   
	 */
	private static void buildCallGraph(){
		ExtractocolLogger.Log("Building call graph ...");
		
		// Initialization
		init();
		
		for(SootClass sc: Scene.v().getClasses()){
			// Check whether this SootClass is phantom 
			if(sc.isPhantom())
				continue;
			
			for(Iterator<SootMethod> smIter = sc.getMethods().iterator(); smIter.hasNext();){
				SootMethod sm = null;
				
				try{
					sm = smIter.next();
				}catch (ConcurrentModificationException e){
					System.err.println("Warning: [ConcurrentModificationException] Class: " + sc.getName());
					break;
					
					//e.printStackTrace();
					//System.exit(0);
				}
				
				if(!sm.hasActiveBody())
					continue;
				
				for(Unit u : sm.getActiveBody().getUnits()){
					Stmt s = (Stmt) u;
					
					// Reset the edge/clinitEdge lists
					clearLists();
					
					// Check whether the statement contains invoke expression
					if(!s.containsInvokeExpr())
						continue;
					
					InvokeExpr ie = s.getInvokeExpr();
					
					// skip it if excluded
					if(isExcludedCall(ie))
						continue;
					
					if(!implicitCall(sm, s)){
						if(isSystemPackage(s))
							continue;
						
						//if(s.toString().contains("executeRequest("))
							//System.out.println("Found!");
						if(ie instanceof InterfaceInvokeExpr) // Handle interface invoke 
							InterfaceInvokeHandler(sm, s);
						else if (ie instanceof StaticInvokeExpr) // Handle static invoke 
							StaticInvokeHandler(sm, s); 
						else if (ie instanceof VirtualInvokeExpr) // Handle virtual invoke
							VirtualInvokeHandler(sm, s);
						else if (ie instanceof SpecialInvokeExpr) // Handle special invoke
							SpecialInvokeHandler(sm, s);
					}
					
					// Get the coressponding <clinit> method
					//clinitHandler(sm, s);
					
					// Add all the edges in edgeList/clinitEdgeList to the call graph
					AddEdgesToCallGraph();
				}
			}
		}
		
		ExtractocolLogger.Log("The call graph has been generated.");
	}
	
	private static void init() {
		excludedCalls.add("<rx.functions.Func1: java.lang.Object call(java.lang.Object)>");
	}
	
	private static boolean isExcludedCall(InvokeExpr ie) {
		String method = ie.getMethodRef().toString();
		return excludedCalls.contains(method);
	}
	
	// For debugging
	private static boolean isSkip(Stmt s){
		return s.toString().contains("void setup(com.contextlogic.wish.api.data.WishProduct,int,com.contextlogic.wish.ui.fragment.product.tabbeddetails.TabbedProductDetailsFragment)")
				|| s.toString().contains("void collectPayment(com.contextlogic.wish.ui.fragment.cartmodal.paymentprocessor.google.GoogleWalletPaymentCollector$SuccessListener,com.contextlogic.wish.ui.fragment.cartmodal.paymentprocessor.google.GoogleWalletPaymentCollector$FailureListener)>")
				|| s.toString().contains("void handleResume()>")
				|| s.toString().contains("void onSessionStateChange(com.facebook.SessionState,java.lang.Exception)>")
				|| s.toString().contains("void checkout(com.contextlogic.wish.ui.fragment.cartmodal.paymentprocessor.CartPaymentProcessor$SuccessListener,com.contextlogic.wish.ui.fragment.cartmodal.paymentprocessor.CartPaymentProcessor$FailureListener)>")
				//|| s.toString().contains("void save(com.contextlogic.wish.ui.fragment.cartmodal.ui.billing.vault.CartPaymentVaultProcessor$SaveListener,android.os.Bundle)>")
				//|| s.toString().contains("void performNetworkRequest()>")
				//|| s.toString().contains("void setupHeaderViews(android.view.View)>")
				//|| s.toString().contains("void prepareForRecycle()>")
				//|| s.toString().contains("void initializeUi(android.view.View)>")
				
				|| s.toString().contains("void onSuccess(org.json.JSONObject)>")
				|| s.toString().contains("void onSuccess(android.graphics.Bitmap)>")
				|| s.toString().contains("void onSuccess(java.lang.String)>")
				|| s.toString().contains("void onSuccess(com.stripe.android.model.Token)>")
				|| s.toString().contains("void onFailure(java.lang.Throwable,android.graphics.Bitmap)>")
				|| s.toString().contains("void onFailure(java.lang.Throwable,org.json.JSONObject)>");
	}
	
	public static boolean needToRetrieveActiveBodies() {
		return !Scene.v().doneResolving() || !MyCallGraphBuilder.CheckActiveBodies();
	}
	
	/** Retrieve the active body of all methods
	 */
	public static void retrieveActiveBodies(){
		ExtractocolLogger.Log("Retrieving active bodies ...");
		
		for(SootClass sc: Scene.v().getClasses()){
			// Check whether this SootClass is phantom 
			if(sc.isPhantom())
				continue;

			for(SootMethod sm : sc.getMethods()){
				try {
					// Get active body
					retrieveActiveBody(sm);
				} catch (Exception e) {
					ExtractocolLogger.Warn("Err_Method: " + sm.toString());
					//e.printStackTrace();
					continue;
				}
			}
		}
		
		ExtractocolLogger.Log("All active bodies have been retrieved.");
	}
	
	/** Retrieve the active body of the SootMethod when the body exists
	 * 
	 * @param m target SootMethod
	 */
	private static void retrieveActiveBody(SootMethod m){
		if (!m.isConcrete()) return;
		if (m.isAbstract()) return;
		if (m.isNative()) return;
		if (m.isPhantom()) return;
		
		// Retrieve the active body
		if (!m.hasActiveBody())
			m.retrieveActiveBody();
	}
	
	/** Check whether all methods have active body
	 * 
	 * @return True, if all method have active body.
	 */
	public static boolean CheckActiveBodies(){
		//ExtractocolLogger.Log("Checking whether all methods have active body ...");
		
		//int cnt = 0;
		for(SootClass sc: Scene.v().getClasses()){
			//cnt++;
			
			// Check whether this SootClass is phantom 
			if(sc.isPhantom())
				continue;

			for(SootMethod sm : sc.getMethods()){
				// Get active body
				if(!checkActiveBody(sm)){
					//ExtractocolLogger.Warn("There is no active body: " + sm.toString());
					return false;
				}
			}
		}
		
		//ExtractocolLogger.Log("All methods have active body. (# of classes: " + cnt + ")");
		return true;
	}
	
	/** Check whether the method has active body
	 * 
	 * @param m SootMethod
	 * @return True if it has active body
	 */
	private static boolean checkActiveBody(SootMethod m){
		if (!m.isConcrete()) return true;
		if (m.isAbstract()) return true;
		if (m.isNative()) return true;
		if (m.isPhantom()) return true;
		
		return m.hasActiveBody();
	}
	
	/** Clear the edge/clinitEdge lists
	 */
	private static void clearLists(){
		edgeList.clear();
		clinitEdgeList.clear();
	}

	/** Handle implicit call flow
	 * 
	 * @param srcSM current method
	 * @param s current statement
	 * @return True if s has implicit call flow
	 */
	private static boolean implicitCall(SootMethod srcSM, Unit u){
		Stmt s = (Stmt) u;
		InvokeExpr ie = s.getInvokeExpr();
		
		final NumberedString sigRun = Scene.v().getSubSigNumberer().findOrAdd( "void run()" );
		final NumberedString sigFutureSubmit = Scene.v().getSubSigNumberer().findOrAdd( "java.util.concurrent.Future submit(java.lang.Runnable)" );

		if (ie instanceof InstanceInvokeExpr) {
			InstanceInvokeExpr iie = (InstanceInvokeExpr) ie;
			NumberedString subSig =	iie.getMethodRef().getSubSignature();
			
			if(subSig == sigFutureSubmit){
				// Get the first argument
				Value runnable = iie.getArg(0);
				
				// Get the class(type) of the argument
				SootClass sc = Scene.v().getSootClass(runnable.getType().toString());
				
				// Get 'void run()' method of the class(type) of the first argument
				SootMethod targetSM = sc.getMethodUnsafe(sigRun);
				
				// Add it to the edge
				if(isValidMethod(targetSM))
					edgeList.add(new Edge(srcSM, s, targetSM));
				else if(!sc.isInterface()){
					// Get the super-classes when the method of the declaring class does not exist
					List<SootClass> superClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(sc);
					
					for(SootClass superClass : superClasses){
						targetSM = superClass.getMethodUnsafe(sigRun);
						
						// Check whether the super class contains the corresponding method
						if(!isValidMethod(targetSM))
							continue;
						
						// Add the corresponding method and exit when it exist
						edgeList.add(new Edge(srcSM, s, targetSM));
						break;
					}
				}
				
				return true;
			}
		}
		
		// Handle rx.operators (e.g., flatmap, ...)
		if(ImplicitCallEdgeTracker.isReady()) {
			List<SootMethod> res = ImplicitCallEdgeTracker.find(u, srcSM);
			if(res == null || res.size() == 0)
				return false;
			
			for(SootMethod targetSM: res)
				edgeList.add(new Edge(srcSM, s, targetSM));
			
			return true;
		}
		
		return false;
	}
	
	public static int getUnitHash(Unit u, SootMethod sm) {
		Unit predU = sm.getActiveBody().getUnits().getPredOf(u);
		Unit succU = sm.getActiveBody().getUnits().getSuccOf(u);
		
		int pred = (predU != null)? predU.toString().hashCode() : 0;
		int curr = u.toString().hashCode();
		int succ = (succU != null)? succU.toString().hashCode() : 0;
		
		return pred+curr+succ;
	}
	
	public static boolean isSystemPackage(Stmt s){
		InvokeExpr ie = s.getInvokeExpr();
		return CallbackCandidateFinder.isClassInSystemPackage
				(ie.getMethodRef().declaringClass().toString());
	}
	
	/** Handle interface invoke
	 * 
	 * @param srcSM current method
	 * @param s current statement
	 */
	private static void InterfaceInvokeHandler(SootMethod srcSM, Stmt s){
		InvokeExpr ie = s.getInvokeExpr();
		SootClass sc = ie.getMethodRef().declaringClass();
		
		if(!sc.isInterface())// CallbackCandidateFinder.isClassInSystemPackage(ie.getMethod().getDeclaringClass().toString()))
			return;
		
		// Get implementers of the declaring class
		List<SootClass> impls = Scene.v().getActiveHierarchy().getImplementersOf(sc);
		for(SootClass impl : impls){
			SootMethod targetSM = impl.getMethodUnsafe(ie.getMethodRef().getSubSignature());
			
			// Check whether the implementer contains the corresponding method
			if(!isValidMethod(targetSM))
				continue;
			
			// Add the method of the implementer when it exists
			edgeList.add(new Edge(srcSM, s, targetSM));
		}
	}

	/** Handle static invoke
	 * 
	 * @param srcSM current method
	 * @param s current statement
	 */
	private static void StaticInvokeHandler(SootMethod srcSM, Stmt s) {
		InvokeExpr ie = s.getInvokeExpr();
		SootClass sc = ie.getMethodRef().declaringClass();
		SootMethod targetSM = sc.getMethodUnsafe(ie.getMethodRef().getSubSignature());
		
		if(isValidMethod(targetSM))
			// Add the method of the declaring class when it exists
			edgeList.add(new Edge(srcSM, s, targetSM));
		else{
			// Get the super-classes when the method of the declaring class does not exist
			List<SootClass> superClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(sc);
			
			for(SootClass superClass : superClasses){
				targetSM = superClass.getMethodUnsafe(ie.getMethodRef().getSubSignature());
				
				// Check whether the super class contains the corresponding method
				if(!isValidMethod(targetSM))
					continue;
				
				// Add the corresponding method and exit when it exist
				edgeList.add(new Edge(srcSM, s, targetSM));
				break;
			}
		}
	}

	/** Handle virtual invoke. (Need to consider both of sub-classes and super-classes)
	 * 
	 * @param srcSM current method
	 * @param s current statement
	 */
	private static void VirtualInvokeHandler(SootMethod srcSM, Stmt s) {
		InvokeExpr ie = s.getInvokeExpr();
		SootClass sc = ie.getMethodRef().declaringClass();
		SootMethod targetSM = sc.getMethodUnsafe(ie.getMethodRef().getSubSignature()); 
		
		// Add the method of the declaring class when it exists
		if(isValidMethod(targetSM))
			edgeList.add(new Edge(srcSM, s, targetSM));
		
		// Get the sub-classes of the declaring class
		List<SootClass> subClasses = Scene.v().getActiveHierarchy().getSubclassesOf(sc);
		for(SootClass subClass : subClasses){
			targetSM = subClass.getMethodUnsafe(ie.getMethodRef().getSubSignature());
			if(!isValidMethod(targetSM))
				continue;
			
			// Add the corresponding method of the sub-class when it exists
			edgeList.add(new Edge(srcSM, s, targetSM));
		}
		
		// Get the super-classes of the declaring class
		List<SootClass> superClasses = Scene.v().getActiveHierarchy().getSuperclassesOf(sc);
		for (SootClass superClass : superClasses) {
			targetSM = superClass.getMethodUnsafe(ie.getMethodRef().getSubSignature());
			if (!isValidMethod(targetSM))
				continue;

			// Add the corresponding method of the sub-class when it exists
			edgeList.add(new Edge(srcSM, s, targetSM));
		}
	}

	/** Handle special invoke
	 * 
	 * @param srcSM current method
	 * @param s current statement
	 */
	private static void SpecialInvokeHandler(SootMethod srcSM, Stmt s) {
		InvokeExpr ie = s.getInvokeExpr();
		
		// Add the method when it exists
		if(Scene.v().containsMethod(ie.getMethodRef().getSignature()))
			if(isValidMethod(ie.getMethod()))
				edgeList.add(new Edge(srcSM, s, ie.getMethod()));
	}
	
	/** Add corresponding clinit method
	 * 
	 * @param srcSM current method
	 * @param s current statement
	 */
	private static void clinitHandler(SootMethod srcSM, Stmt s){
		final NumberedString sigClinit = Scene.v().getSubSigNumberer().findOrAdd( "void <clinit>()" );
		
		for(Edge edge: edgeList){
			SootClass sc = edge.getTgt().method().getDeclaringClass();
			SootMethod targetSM = sc.getMethodUnsafe(sigClinit);
			
			// if the <clinit> exists in the class,
			if(isValidMethod(targetSM))
				// Add it to clinitEdgeList
				clinitEdgeList.add(new Edge(srcSM, s, targetSM));
		}
	}
	
	/** Add edges to the call graph
	 */
	private static void AddEdgesToCallGraph(){
		for(Edge e: clinitEdgeList)
			cg.addEdge(e);
		
		for(Edge e: edgeList)
			cg.addEdge(e);
	}
	
	private static boolean isValidMethod(SootMethod m){
		return (m != null) && m.hasActiveBody();
	}
	
	public static void setCG(CallGraph _cg) { cg = _cg; }
}
