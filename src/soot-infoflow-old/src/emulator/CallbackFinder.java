package emulator;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gaurav.tree.NodeNotFoundException;
import com.gaurav.tree.Tree;

import soot.Body;
import soot.Local;
import soot.MethodOrMethodContext;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.InvokeExpr;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.problems.BackwardTaintingProblem;
import soot.jimple.infoflow.util.SystemClassHandler;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.util.Chain;

public class CallbackFinder {
	
	boolean hasAsyncTask = false;
	int count = 0;
	
	/**
	 * Find AsyncTask methods and add them into the CFG
	 * (1) onPreExecute
	 * (2) doInBackground
	 * (3) onPostExecute
	 */
	public void findAsyncTaskLifecycle() {
		// CallGraph callgraph = Scene.v().getCallGraph();
		hasAsyncTask = false;
		for (SootClass sc : Scene.v().getClasses()) {
			for (SootMethod sm : sc.getMethods()) {
				if (sm.hasActiveBody()) {
					Body body = sm.getActiveBody();
					PatchingChain<Unit> units = body.getUnits();
					for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
						final Unit unit = (Unit) iter.next();
						final Stmt stmt = (Stmt) unit;
						if (stmt.containsInvokeExpr()) {
							InvokeExpr inv = stmt.getInvokeExpr();
							if (!(inv instanceof StaticInvokeExpr)) {
								// we retrieve "android.os.AsyncTask execute()"
								if (inv.getType().toString().equals("android.os.AsyncTask")
										&& inv.getMethod().getName().equals("execute")) {
									//System.out.println("\n\n---> "+stmt);
									SootMethod onPreExecute = null;
									SootMethod doInBackground = null;
									SootMethod onPostExecute = null;
									
									for (SootMethod smm : Scene.v().getSootClass(
											((InstanceInvokeExpr) inv).getBase().getType().toString()).getMethods()) {
										String mName = smm.getName();
										/*
										if (mName.equals("onPreExecute")
												&& !(smm.toString().contains("java.lang.Object"))) {
											onPreExecute = smm;
										}
										if (mName.equals("doInBackground")
												&& !(smm.toString().contains("java.lang.Object"))) {
											doInBackground = smm;
										}
										if (mName.equals("onPostExecute")
												&& !(smm.toString().contains("java.lang.Object"))) {
											onPostExecute = smm;
										}
										*/
										
										if (mName.equals("onPreExecute")) {
											onPreExecute = smm;
										}
										if (mName.equals("doInBackground")) {
											doInBackground = smm;
										}
										if (mName.equals("onPostExecute")) {
											onPostExecute = smm;
										}
									}

									Local base = (Local) ((InstanceInvokeExpr) inv).getBase();
									Local retValue = null;
									InvokeExpr expr;
									Stmt onPreExecuteStmt = null;
									Stmt doInBackgroundStmt = null;
									Stmt onPostExecuteStmt = null;
									// Edge onPreExecuteEdge = null;
									// Edge doInBackgroundEdge = null;
									// Edge onPostExecuteEdge = null;
									Type retType = null;

									// make sure that AsyncTask methods follow
									// the order of method call
									// for onPostExecute()
									if (onPreExecute != null) {
										if (onPreExecute.getDeclaringClass().isInterface()) {
											expr = Jimple.v().newInterfaceInvokeExpr(base, onPreExecute.makeRef());
										} else {
											expr = Jimple.v().newVirtualInvokeExpr(base, onPreExecute.makeRef());
										}

										onPreExecuteStmt = Jimple.v().newInvokeStmt(expr);

										// add new Edge into the CFG
										// onPreExecuteEdge = new Edge(sm,
										// onPreExecuteStmt, onPreExecute);

										// onPreExecute.setActiveBody(onPreExecute.retrieveActiveBody());
									}
									// for doInBackground()
									if (doInBackground != null) {
										if (inv.getArgCount() > 0) {
											List<Value> callerArgs = inv.getArgs();
											if (doInBackground.getDeclaringClass().isInterface()) {
												expr = Jimple.v().newInterfaceInvokeExpr(base, doInBackground.makeRef(), callerArgs);
											} else {
												expr = Jimple.v().newVirtualInvokeExpr(base, doInBackground.makeRef(), callerArgs);
											}
										} else {
											if (doInBackground.getDeclaringClass().isInterface()) {
												expr = Jimple.v().newInterfaceInvokeExpr(base, doInBackground.makeRef());
											} else {
												expr = Jimple.v().newVirtualInvokeExpr(base, doInBackground.makeRef());
											}
										}

										retType = doInBackground.getReturnType();
										if (retType == VoidType.v()) {
											doInBackgroundStmt = Jimple.v().newInvokeStmt(expr);
										} else {
											LocalGenerator lg = new LocalGenerator(body);
											retValue = lg.generateLocal(retType);
											doInBackgroundStmt = Jimple.v().newAssignStmt(retValue, expr);
										}

										// add new Edge into the CFG
										// doInBackgroundEdge = new Edge(sm,
										// doInBackgroundStmt, doInBackground);
									}
									// for onPreExecute()
									if (onPostExecute != null) {
										// if (!(retType == VoidType.v())) {
										if (retValue != null) {
											if (onPostExecute.getDeclaringClass().isInterface()) {
												expr = Jimple.v().newInterfaceInvokeExpr(base, onPostExecute.makeRef(), retValue);
											} else {
												expr = Jimple.v().newVirtualInvokeExpr(base, onPostExecute.makeRef(), retValue);
											}
										} else {
											if (onPostExecute.getDeclaringClass().isInterface()) {
												expr = Jimple.v().newInterfaceInvokeExpr(base, onPostExecute.makeRef());
											} else {
												expr = Jimple.v().newVirtualInvokeExpr(base, onPostExecute.makeRef());
											}
										}

										// onPostExecute.setActiveBody(onPostExecute.retrieveActiveBody());

										onPostExecuteStmt = Jimple.v().newInvokeStmt(expr);

										// add new Edge into the CFG
										// onPostExecuteEdge = new Edge(sm,
										// onPostExecuteStmt, onPostExecute);

										// onPostExecute.setActiveBody(onPostExecute.retrieveActiveBody());
									}

									// for onPostExecute()
									if (onPostExecuteStmt != null) {
										units.insertAfter(onPostExecuteStmt, unit);
										// callgraph.addEdge(onPostExecuteEdge);
									}
									// for doInBackground()
									if (doInBackgroundStmt != null) {
										units.insertAfter(doInBackgroundStmt, unit);
										// callgraph.addEdge(doInBackgroundEdge);
									}
									// for onPreExecute()
									if (onPreExecuteStmt != null) {
										units.insertAfter(onPreExecuteStmt, unit);
										// callgraph.addEdge(onPreExecuteEdge);
									}

									// remove execute method
									units.remove(unit);
									
									hasAsyncTask = true;
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void findThreadStart() {
		for (SootClass sc : Scene.v().getClasses()) {
			for (SootMethod sm : sc.getMethods()) {
				if (sm.hasActiveBody()) {
					Body body = sm.getActiveBody();
					PatchingChain<Unit> units = body.getUnits();
					for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
						final Unit unit = (Unit) iter.next();
						final Stmt stmt = (Stmt) unit;
						if (stmt.containsInvokeExpr()) {
							InvokeExpr inv = stmt.getInvokeExpr();
							if (!(inv instanceof StaticInvokeExpr)) {
								SootMethod invMethod = inv.getMethod();
								if (invMethod.toString().equals("<java.lang.Thread: void start()>")) {
									//System.out.println("\n"+invMethod);
									Local base = null;
									
									// Find run() class
									for (Unit u : units) {
										Stmt s = (Stmt) u;
										if (s.containsInvokeExpr()) {
											InvokeExpr ie = s.getInvokeExpr();
											if (!(inv instanceof StaticInvokeExpr)) {
												SootMethod threadInit = ie.getMethod();
												if (threadInit.toString().equals("<java.lang.Thread: void <init>(java.lang.Runnable)>")) {
													//System.out.println("\t"+u);
													if (ie.getArgCount() >= 1) {
														base = (Local) ie.getArg(0);
														//System.out.println("\t\tType: "+base.getType());
													}
												}
											}
										}
									}
									
									if (base != null) {
										// Find run() method
										SootMethod runMethod = null;
										for (SootMethod rm : Scene.v().getSootClass(base.getType().toString()).getMethods()) {
											if (rm.getName().equals("run")) {
												runMethod = rm;
												//System.out.println("\t\t\tMethod: "+runMethod);
											}
										}
										
										// Add run() statement
										Stmt threadRun = null;
										InvokeExpr expr;
										if (runMethod != null) {
											if (runMethod.getDeclaringClass().isInterface()) {
												expr = Jimple.v().newInterfaceInvokeExpr(base, runMethod.makeRef());
											} else {
												expr = Jimple.v().newVirtualInvokeExpr(base, runMethod.makeRef());
											}

											threadRun = Jimple.v().newInvokeStmt(expr);
										}
										
										if (threadRun != null) {
											units.insertAfter(threadRun, unit);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void AndroidHttpClient_ResponseHandler() {
		// CallGraph callgraph = Scene.v().getCallGraph();
		for (SootClass sc : Scene.v().getClasses()) {
			for (SootMethod sm : sc.getMethods()) {
				if (sm.hasActiveBody()) {
					Body body = sm.getActiveBody();
					PatchingChain<Unit> units = body.getUnits();
					for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
						final Unit unit = (Unit) iter.next();
						final Stmt stmt = (Stmt) unit;
						if (stmt.containsInvokeExpr()) {
							InvokeExpr inv = stmt.getInvokeExpr();
							if (!(inv instanceof StaticInvokeExpr)) {
								// we retrieve "<android.net.http.AndroidHttpClient: java.lang.Object execute(org.apache.http.client.methods.HttpUriRequest,org.apache.http.client.ResponseHandler)>"
								if (inv.getMethod().getName().equals("execute")
										&& inv.toString().contains("android.net.http.AndroidHttpClient")
										&& inv.getArgCount() == 2) {									
									SootMethod ResponseHandler = null;
									for (SootMethod smm : Scene.v().getSootClass(inv.getArg(1).getType().toString()).getMethods()) {
										String mName = smm.getName();
										if (mName.equals("handleResponse")
												&& !(smm.toString().contains("java.lang.Object"))) {
											ResponseHandler = smm;
										}
									}
									
									Stmt ResponseHandlerStmt = null;
									InvokeExpr expr;
									Local base = (Local) ((InstanceInvokeExpr) inv).getArg(1);
									if (ResponseHandler != null) {
										if (ResponseHandler.getDeclaringClass().isInterface()) {
											expr = Jimple.v().newInterfaceInvokeExpr(base, ResponseHandler.makeRef(), inv.getArg(1));
										} else {
											expr = Jimple.v().newVirtualInvokeExpr(base, ResponseHandler.makeRef(), inv.getArg(1));
										}

										ResponseHandlerStmt = Jimple.v().newInvokeStmt(expr);
									}
									
									// for onPostExecute()
									if (ResponseHandlerStmt != null) {
										units.insertAfter(ResponseHandlerStmt, unit);
									}
									
									//units.remove(unit);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Update CFG
	 */
	public void update() {
		// Update CallGraph
		CallGraphBuilder cgBuilder = new CallGraphBuilder(DumbPointerAnalysis.v());
		cgBuilder.build();
		CallGraph callGraph = Scene.v().getCallGraph();
		Scene.v().setCallGraph(callGraph);

		// Recreate the exception throw analysis
		Scene.v().getDefaultThrowAnalysis();

		// Invalidate the list of reachable methods. It will automatically be recreated
		// on the next call to "getReachableMethods".
		Scene.v().getReachableMethods();

		// Update the class hierarchy
		Scene.v().getActiveHierarchy();

		List<MethodOrMethodContext> eps = new ArrayList<MethodOrMethodContext>();
		eps.addAll(Scene.v().getEntryPoints());
		ReachableMethods reachables = new ReachableMethods(callGraph, eps.iterator());
		reachables.update();
	}

	public boolean isAsyncTask() {
		if (hasAsyncTask)
			return true;
		else
			return false;
	}
		
	/*
	 * Linear Search for finding heaps and constants
	 */
	@SuppressWarnings("resource")
	public HashSet<SootMethod> linearSearchReturn(boolean backwardTainting, BackwardTaintingProblem backwardTaintingProblem) {
		
		BufferedReader br = null;
		if (backwardTainting) {
			try {
				br = new BufferedReader(new FileReader("SourcesAndSinks_for_backward.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				br = new BufferedReader(new FileReader("SourcesAndSinks.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		String line;
		List<String> dps = new ArrayList<String>();
		try {
			while ((line = br.readLine()) != null) {
				if (line.startsWith("%"))
					continue;
				if (line.length() < 1 || line == null)
					continue;
				String dp = line.split(" -> ")[0];
				if (dp.length() > 1) {
					dps.add(dp);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		HashSet<SootMethod> methods = new HashSet<SootMethod>();
		Set<SootMethod> startpointSet = new HashSet<SootMethod>();
		for (SootClass sc : Scene.v().getClasses()) {
			for (SootMethod sm : sc.getMethods()) {
				//if (sm.method() == null || sm.getSource() == null)
				//	continue;
				if (!sm.hasActiveBody())
					continue;
				if (SystemClassHandler.isClassInSystemPackage(sm.method()
						.getDeclaringClass().getName()))
					continue;
				
				Body body = sm.retrieveActiveBody();
				PatchingChain<Unit> units = body.getUnits();
				for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
					final Unit unit = (Unit) iter.next();
					for (String dp : dps) {
						if (unit.toString().contains(dp)) {
							methods.add(sm);
							if (!startpointSet.contains(sm)) {
								//backwardTaintingProblem.addInitialSeeds(unit, 
								//		Collections.singleton(backwardTaintingProblem.zeroValue()));
								//System.out.println("[INFO] Source Found : \n" + 
								//		"\tUNIT: " + unit +
								//		"\n\tMETHOD: " + sm);
								startpointSet.add(sm);
							}
						}
					}
				}
			}
		}
		
		return methods;
	}
	
	/*
	 * Linear Search for finding heaps and constants
	 */
	public void linearSearch(boolean backwardTainting) {
		
		BufferedReader br = null;
		if (backwardTainting) {
			try {
				br = new BufferedReader(new FileReader("SourcesAndSinks_for_backward.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				br = new BufferedReader(new FileReader("SourcesAndSinks.txt"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		String line;
		List<String> dps = new ArrayList<String>();
		try {
			while ((line = br.readLine()) != null) {
				if (line.startsWith("%"))
					continue;
				if (line.length() < 1 || line == null)
					continue;
				String dp = line.split(" -> ")[0];
				if (dp.length() > 1) {
					dps.add(dp);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		HashSet<SootMethod> methods = new HashSet<SootMethod>();
		for (SootClass sc : Scene.v().getClasses()) {
			for (SootMethod sm : sc.getMethods()) {
				//if (sm.method() == null || sm.getSource() == null)
				//	continue;
				if (!sm.hasActiveBody())
					continue;
				if (SystemClassHandler.isClassInSystemPackage(sm.method()
						.getDeclaringClass().getName()))
					continue;
				
				Body body = sm.retrieveActiveBody();
				PatchingChain<Unit> units = body.getUnits();
				for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
					final Unit unit = (Unit) iter.next();
					for (String dp : dps) {
						if (unit.toString().contains(dp)) {
							//System.out.println("[*] dp : "+dp);
							//System.out.println("\t[-] getSource : "+sm.getSource());
							//System.out.println("\t[-] method : "+sm.toString());
							//System.out.println("\t[-] unit : "+unit.toString());
							methods.add(sm);
						}
					}
				}
			}
		}
		
		List<SootMethod> seeds = new LinkedList<SootMethod>();
		if (Scene.v().hasCallGraph()) {
			List<MethodOrMethodContext> eps = new ArrayList<MethodOrMethodContext>(
					Scene.v().getEntryPoints());
			ReachableMethods reachableMethods = new ReachableMethods(Scene.v()
					.getCallGraph(), eps.iterator(), null);
			reachableMethods.update();
			for (Iterator<MethodOrMethodContext> iter = reachableMethods
					.listener(); iter.hasNext();)
				seeds.add(iter.next().method());
		}
		
		SootMethod dummyMain = Scene.v().getMethod("<dummyMainClass: void dummyMainMethod()>");
		Body dummyBody = dummyMain.getActiveBody();
		PatchingChain<Unit> units = dummyBody.getUnits();
		LocalGenerator gen = null;
		
		for (SootMethod sm : methods) {
			
			final InvokeExpr invokeExpr;
			List<Value> args = new LinkedList<Value>();
			SootClass currentClass = Scene.v().getSootClass(sm.getDeclaringClass().toString());
			Chain<Local> locals = dummyBody.getLocals();
			Local classLocal = locals.getLast();

			if(sm.getParameterCount()>0){
				
				if(sm.isStatic())
					invokeExpr = Jimple.v().newStaticInvokeExpr(sm.makeRef(), args);
				else {
					if (sm.isConstructor())
						invokeExpr = Jimple.v().newSpecialInvokeExpr(classLocal, sm.makeRef(), args);
					else
						invokeExpr = Jimple.v().newVirtualInvokeExpr(classLocal, sm.makeRef(), args);
				}
			}else{
				if(sm.isStatic()){
					invokeExpr = Jimple.v().newStaticInvokeExpr(sm.makeRef());
				}else{
					if (sm.isConstructor())
						invokeExpr = Jimple.v().newSpecialInvokeExpr(classLocal, sm.makeRef());
					else
						invokeExpr = Jimple.v().newVirtualInvokeExpr(classLocal, sm.makeRef());
				}
			}
			 
			Stmt stmt = Jimple.v().newInvokeStmt(invokeExpr);

			dummyBody.getUnits().add(stmt);
			
			// Clean up
			for (Object val : args)
				if (val instanceof Local && ((Value) val).getType() instanceof RefType)
					dummyBody.getUnits().add(Jimple.v().newAssignStmt((Value) val, NullConstant.v()));
		}
	}
	
	// Generate Tree recursively
	@SuppressWarnings("deprecation")
	public void generateTree(Tree<SootMethod> tree, SootMethod callee) throws NodeNotFoundException {
			
		List<SootMethod> callers = findCaller(callee);
		if (callers.size() > 0) {
			for (SootMethod caller : callers) {
				tree.add(callee, caller);
				generateTree(tree, caller);
			}
		}
			
		return;
	}
	
	// Find Method given Unit
	public List<SootMethod> findMethod(String src) {
			
		List<SootMethod> methods = new ArrayList<SootMethod>();
		for (SootClass sc : Scene.v().getClasses()) {
			for (SootMethod sm : sc.getMethods()) {
				if (sm.hasActiveBody()) {
					Body body = sm.retrieveActiveBody();
					PatchingChain<Unit> units = body.getUnits();
					for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
						final Unit unit = (Unit) iter.next();
						final Stmt stmt = (Stmt) unit;
						if (stmt.containsInvokeExpr()) {
							InvokeExpr inv = stmt.getInvokeExpr();
							if (!(inv instanceof StaticInvokeExpr)) {
								if (inv.getMethod().toString().equals(src)) {
									methods.add(sm);
								}
							}
						}
					}
				}
			}
		}
			
		return methods;
	}
	
	// Find Method given Unit
	public List<SootMethod> findMethodfromHeapAllocStmt(String src) {
				
		List<SootMethod> methods = new ArrayList<SootMethod>();
		for (SootClass sc : Scene.v().getClasses()) {
			for (SootMethod sm : sc.getMethods()) {
				if (sm.hasActiveBody()) {
					Body body = sm.retrieveActiveBody();
					PatchingChain<Unit> units = body.getUnits();
					for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
						final Unit unit = (Unit) iter.next();
						if (unit.toString().contains(src))
							methods.add(sm);
					}
				}
			}
		}
				
		return methods;
	}
		
	// Find Caller given Callee
	public List<SootMethod> findCaller(SootMethod callee) {
			
		List<SootMethod> callers = new ArrayList<SootMethod>();
			
		for (SootClass sc : Scene.v().getClasses()) {
			for (SootMethod sm : sc.getMethods()) {
				if (sm.hasActiveBody()) {
					Body body = sm.getActiveBody();
					PatchingChain<Unit> units = body.getUnits();
					for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
						final Unit unit = (Unit) iter.next();
						final Stmt stmt = (Stmt) unit;
						if (stmt.containsInvokeExpr()) {
							InvokeExpr inv = stmt.getInvokeExpr();
							if (inv.getMethod().toString().equals(callee.toString())) {
								callers.add(sm);
							}
						}
					}
				}
			}
		}
			
		return callers;
	}

	// Slice Builder : make sliceClass
	private void sliceBuilder(List<SootMethod> path, String dPoint) throws IOException {
		//Collections.reverse(path);
		List<String> sClass = new ArrayList<String>();
		
		for (SootMethod sm : path) {
			String oneMethod = "";
			String methodClass = "";
			String smClass = sm.getDeclaringClass().toString();
			String methodBody = sm.retrieveActiveBody().toString();
			methodClass = sm.getDeclaringClass().toString().replace(".", "_");
			String renamedMethod = methodClass+"__"+sm.getName();
			String[] parts = methodBody.split("\n");
			
			for (int i = 0; i < parts.length; i++) {
				if (i == 0) {
					String newMethod = parts[i].replace(sm.getName(), renamedMethod);
					oneMethod = oneMethod + parts[i] + "\n";
				}
				else {
					oneMethod = oneMethod + parts[i] + "\n";
				}
			}

			sClass.add(oneMethod);
		}
		
		String fileName = "";
		Pattern pattern = Pattern.compile(": .+? (.+?)\\(");
		Matcher matcher = pattern.matcher(dPoint);
		if (matcher.find()) {
		    fileName = matcher.group(1);
		}
		
		String finalFileName = fileName+"_"+String.valueOf(++count);
		System.out.print("\tGenerating slices... \""+finalFileName+".jimple"+"\"");
		BufferedWriter out = new BufferedWriter(new FileWriter("./Slice_Output/"+finalFileName+".jimple"));
		out.write("public class "+finalFileName+"\n");
		out.write("{ "+"\n");
		for (String sm : sClass) {
			out.newLine();
			out.write(sm);
		}
		out.write("}"+"\n");
		out.close();
		System.out.println();
	}
}
