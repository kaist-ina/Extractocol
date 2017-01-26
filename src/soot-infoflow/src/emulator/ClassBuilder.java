package emulator;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.Printer;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.VoidType;
import soot.javaToJimple.LocalGenerator;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.ConcreteRef;
import soot.jimple.Constant;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NewExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.util.BaseSelector;
import soot.jimple.infoflow.util.SystemClassHandler;
import soot.jimple.parser.JimpleAST;
import soot.jimple.parser.lexer.LexerException;
import soot.jimple.parser.parser.ParserException;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.Chain;

public class ClassBuilder {

	private IInfoflowCFG iCfg;
	private int class_count = 0;
	private String apkName;
	int count = 0;
	private HashSet<SootMethod> duplicated = new HashSet<SootMethod>();
	
	LinkedHashMap<Unit, LinkedHashSet<Unit>> retStmt;
	private HashSet<SootClass> sClasses = new HashSet<SootClass>();
	List<JimpleAST> jimpleAST = new ArrayList<JimpleAST>();
	List<String> entrypoints = new ArrayList<String>();
	LinkedList<HashMap<Unit, List<String>>> finalEPs = new LinkedList<HashMap<Unit, List<String>>>();
	private boolean backwardTainting;
	
	public ClassBuilder(LinkedHashMap<Unit, LinkedHashSet<Unit>> retStmt, IInfoflowCFG iCfg, boolean backwardTainting, String apkname) {
		this.retStmt = retStmt;
		this.iCfg = iCfg;
		this.backwardTainting = backwardTainting;
		
		if (apkname.contains("/"))
			this.apkName = apkname.substring(apkname.lastIndexOf("/") + 1);
		else
			this.apkName = apkname.substring(apkname.lastIndexOf("\\") + 1);
		
		sliceParser(this.retStmt);
	}
	
	/**
	 * Parse the sliced statements
	 * @param retStmt The sliced statements
	 */
	private void sliceParser(LinkedHashMap<Unit, LinkedHashSet<Unit>> retStmt) {
		LinkedHashMap<SootMethod, LinkedHashSet<Unit>> slicedStmts = new LinkedHashMap<SootMethod, LinkedHashSet<Unit>>();
		LinkedHashMap<SootMethod, LinkedHashSet<Unit>> bslicedStmts = new LinkedHashMap<SootMethod, LinkedHashSet<Unit>>();
		
		Iterator<Unit> it1 = retStmt.keySet().iterator();
		
		while (it1.hasNext()) {
			Unit oneClass = it1.next();
			
			for (Unit stmt : retStmt.get(oneClass)) {		
				if (slicedStmts.containsKey(iCfg.getMethodOf(stmt))) {
					LinkedHashSet<Unit> slice = slicedStmts.get(iCfg.getMethodOf(stmt));
					slice.add(stmt);
					slicedStmts.put(iCfg.getMethodOf(stmt), slice);
				}
				else {
					LinkedHashSet<Unit> slice = new LinkedHashSet<Unit>();
					slice.add(stmt);
					slicedStmts.put(iCfg.getMethodOf(stmt), slice);
				}
			}
			
			if (backwardTainting) {
				Iterator<SootMethod> it2 = slicedStmts.keySet().iterator();
				
				while (it2.hasNext()) {
					SootMethod bmethod = it2.next();
					List<Unit> units = new ArrayList<Unit>(slicedStmts.get(bmethod));
					Collections.reverse(units);
					
					LinkedHashSet<Unit> bslices = new LinkedHashSet<Unit>(units);
					
					bslicedStmts.put(bmethod, bslices);
				}

				createClass(bslicedStmts, oneClass);
			}
			else {
				createClass(slicedStmts, oneClass);
			}
			
			slicedStmts.clear();
			bslicedStmts.clear();
			entrypoints.clear();
		}
	}

	private void createClass(LinkedHashMap<SootMethod, LinkedHashSet<Unit>> slicedStmts_input, Unit dPoint) {

		LinkedHashMap<SootClass, List<SootMethod>> slicedClass = new LinkedHashMap<SootClass, List<SootMethod>>();
		
		// .apk Name
		String directory_apk = "./Slice_Output/"+this.apkName;
		File dir_apk = new File(directory_apk);
		if (dir_apk.exists()) {
			dir_apk.delete();
		}
		else {
			dir_apk.mkdir();
		}
		
		// Backward or Forward slicing
		String directory_direct;
		if (backwardTainting)
			directory_direct = directory_apk + "/backward";
		else
			directory_direct = directory_apk + "/forward";
		File dir_direct = new File(directory_direct);
		if (dir_direct.exists()) {
			dir_direct.delete();
		}
		else {
			dir_direct.mkdir();
		}
		
		// Slices
		String directory_slice = directory_direct + "/dPoint_"+String.valueOf(++count);
		// Make a directory
		File dir_slice = new File(directory_slice);
		if (dir_slice.exists()) {
			dir_slice.delete();
		}
		else {
			try{
				Files.createDirectories(dir_slice.toPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("\t"+directory_slice);
		
		LinkedHashMap<SootMethod, LinkedHashSet<Unit>> slicedStmts = slicedStmts_input;
		
		// Linear Search for heap
		HashSet<SootMethod> linearsearches = linearSearching(directory_slice);
		if (linearsearches.size() > 0) {
			for (SootMethod sm : linearsearches) {
				LinkedHashSet<Unit> units = new LinkedHashSet<Unit>();
				Body body = sm.retrieveActiveBody();
				PatchingChain<Unit> unitss = body.getUnits();
			
				for (Unit unit : unitss)
					units.add(unit);
				
				slicedStmts.put(sm, units);
			}
		}
		
		Iterator<SootMethod> it = slicedStmts.keySet().iterator();
		// Construct slicedClass that contains key as SootClass and value as List<SootMethod> respectively
		while (it.hasNext()) {
			SootMethod sm = it.next();
			SootClass sc = sm.getDeclaringClass();
			
			boolean isExist = slicedClass.containsKey(sc);
			if (isExist) {
				List<SootMethod> exist_sms = slicedClass.get(sc);
				exist_sms.add(sm);
				slicedClass.put(sc, exist_sms);
			}
			else {
				List<SootMethod> new_sms = new ArrayList<SootMethod>();
				new_sms.add(sm);
				slicedClass.put(sc, new_sms);
			}
		}
		
		for (SootClass key : slicedClass.keySet()) {
			List<SootMethod> values = slicedClass.get(key);
			String fileName = key.toString()+".jimple";
			
			System.out.print("\t\tGenerating Jimple Class ... \""+fileName+"\"\n");
			
			BufferedWriter out = null;
			try {
				if (!Files.exists(new File(directory_slice+"/"+fileName).toPath()))
					Files.createFile(new File(directory_slice+"/"+fileName).toPath());
				out = new BufferedWriter(new FileWriter(directory_slice+"/"+fileName));
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				out.write("public class "+key.toString()+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				out.write("{ "+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			for (SootMethod value : values) {
				try {
					out.newLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					out.write(value.retrieveActiveBody().toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					out.newLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			try {
				out.write("}"+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		makeEPs(makeCFG(slicedStmts, directory_slice), slicedStmts, directory_slice);
		
		HashMap<Unit, List<String>> tmp = new HashMap<Unit, List<String>>();
		tmp.put(dPoint, new ArrayList<String>(this.entrypoints));
		finalEPs.add(tmp);
		
		makeInterfaceInfo(slicedStmts, directory_slice);
		
		System.out.println();

	}
	
	// find all interfaces information.
	private void makeInterfaceInfo(
			LinkedHashMap<SootMethod, LinkedHashSet<Unit>> slicedStmts, String directory_slice) {
		List<SootClass> classes = new ArrayList<SootClass>();
		List<SootMethod> methods = new ArrayList<SootMethod>();
		
		Iterator<SootMethod> it = slicedStmts.keySet().iterator();
		while (it.hasNext()) {
			SootMethod method = it.next();
			methods.add(method);
			classes.add(method.getDeclaringClass());
		}
		
		String fileName = "/inteface.txt";
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(directory_slice+fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		Iterator<SootMethod> itt = slicedStmts.keySet().iterator();		
		while (itt.hasNext()) {
			SootMethod sm = itt.next();
			if (sm.hasActiveBody()) {
				Body body = sm.getActiveBody();
				PatchingChain<Unit> units = body.getUnits();
				for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
					final Unit unit = (Unit) iter.next();
					final Stmt stmt = (Stmt) unit;
					if (stmt.containsInvokeExpr()) {
						InvokeExpr inv = stmt.getInvokeExpr();
						SootMethod smm = inv.getMethod();
						if (smm.getDeclaringClass().isInterface()) {
							//System.out.println("==> "+iCfg.getCalleesOfCallAt(unit));
							// (1) using iCfg
							for (SootMethod smmm : iCfg.getCalleesOfCallAt(unit)) {
								if (classes.contains(smmm.getDeclaringClass())) {
									try {
										out.write(sm + " -> " + smm + " -> " + smmm + "\n");
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}
							
							// (2) using Scene.v()
							//for (SootClass sc : Scene.v().getActiveHierarchy().getDirectImplementersOf(smm.getDeclaringClass())) {
							//	if (classes.contains(sc)) {
							//		for (SootMethod m : Scene.v().getSootClass(sc.toString()).getMethods()) {
							//			if (smm.getName().equals(m.getName())) {
							//				//System.out.println("=> "+smm);
							//				//System.out.println("\t==> "+m);
							//			}
							//		}
							//	}
							//}
						}
					}
				}
			}
		}
		
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// find all entry point methods
	private void makeEPs(LinkedHashMap<String, List<String>> cfg, 
			LinkedHashMap<SootMethod, LinkedHashSet<Unit>> slicedStmts, String directory_slice) {
		Iterator<SootMethod> it = slicedStmts.keySet().iterator();
		SootMethod dPoint = it.next();
		
		HashSet<String> isExist = new HashSet<String>();
		findEPs(isExist, dPoint.toString(), cfg);
		
		// create entrypoints.txt
		String fileName = "/entrypoints.txt";
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(directory_slice+fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		for (String ep : isExist) {
			// find only reachable path: entrypoint to demarcation point.
			//List<SootMethod> e1 = new ArrayList<SootMethod>();
			//e1.add(Scene.v().getMethod(ep));
			//List<MethodOrMethodContext> eps = new ArrayList<MethodOrMethodContext>(e1);
			//ReachableMethods reachableMethods = new ReachableMethods(Scene.v().getCallGraph(), eps.iterator(), null);
			//reachableMethods.update();
			//
			//for (Iterator<MethodOrMethodContext> iter = reachableMethods.listener(); iter.hasNext();) {
			//      SootMethod method = iter.next().method();
			//      if (method.toString().contains(dPoint.toString())) {
			//    	  System.out.println("=> "+ep+"->"+method);
			//    	  try {
			//  				out.write(ep+"\n");
			//  			} catch (IOException e) {
			//  				e.printStackTrace();
			//  			}
			//      }
			//}
			
			try {
				out.write(ep+"\n");
				entrypoints.add(ep);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return;
	}

	private void findEPs(HashSet<String> isExist, String dPoint, LinkedHashMap<String, List<String>> cfg) {
		List<String> callers = cfg.get(dPoint);
		
		if (!isExist.contains(dPoint)) {
			isExist.add(dPoint);
			if (callers.size() > 0) {
				for (String caller : callers) {
					findEPs(isExist, caller, cfg);
				}
			}
		}
		
		return;
	}

	// Linear Searching..
	private HashSet<SootMethod> linearSearching(String directory_slice) {	
		List<String> heaps = new ArrayList<String>();
		HashSet<SootMethod> results = new HashSet<SootMethod>();
		
		File file = new File(directory_slice+"/heaps.txt");
		if (file.exists()) {
			try {
				FileInputStream fileInputStream = new FileInputStream(file);
				
				BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
				 
				String line = null;
				try {
					while ((line = br.readLine()) != null) {
						if (!line.contains("%"))
							heaps.add(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			 
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				// create heaps_methods.txt
				String fileName = "/heaps_methods.txt";
				BufferedWriter out = null;
				try {
					out = new BufferedWriter(new FileWriter(directory_slice+fileName));
				} catch (IOException e) {
					e.printStackTrace();
				}				
				
				// Start Linear Searching
				for (String constant : heaps) {
					List<SootMethod> foundMethod = findMethodfromHeapAllocStmt(constant);
					duplicated.clear();
					
					findAllHeaps(constant, foundMethod, 0, null, out, results);
				}
				
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
					
				//System.exit(0);
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		return results;
	}

	// Find all heaps' callers
	private void findAllHeaps(String constant, List<SootMethod> foundMethod, int depth, 
			SootMethod method, BufferedWriter out, HashSet<SootMethod> results) {
		if (depth > 5) {
			if (!duplicated.contains(method)) {
				try {
					out.write(constant+" -> "+method.toString()+"\n");
					//System.out.println(constant+" -> "+method.toString()+"\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			duplicated.add(method);
			return;
		}
			
		for (SootMethod sm : foundMethod) {
			results.add(sm);
			if (sm.getName().equals("<cinit>") || sm.isConstructor()) {
				List<SootMethod> callers1 = findInitializer(sm.toString());				
				if (callers1.isEmpty()) {
					if (!duplicated.contains(sm)) {
						try {
							out.write(constant+" -> "+sm.toString()+"\n");
							//System.out.println(constant+" -> "+method.toString()+"\n");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				else {
					//System.out.println("=> "+depth);
					findAllHeaps(constant, callers1, depth+1, sm, out, results);
				}
			}
			else {
				List<SootMethod> callers2 = findCaller(sm);
				if (callers2.isEmpty()) {
					// Here, write heaps_methods.txt
					//System.out.println("[*] StartPoint = "+sm);
				}
				else {
					//System.out.println("=> "+depth);
					findAllHeaps(constant, callers2, depth+1, sm, out, results);
				}
			}	
		}
	}

	public List<SootMethod> findMethodfromHeapAllocStmt(String src) {
		List<SootMethod> methods = new ArrayList<SootMethod>();
		for (SootClass sc : Scene.v().getClasses()) {
			//if (sc.toString().equals("com.squareup.picasso.BitmapHunter")) {
			//	for (SootMethod sm : sc.getMethods()) {
			//		if (sm.getName().equals("<init>")) {// && sm.hasActiveBody()) {
			//			System.out.println("*** "+sm);
			//			System.out.println(sm.retrieveActiveBody());
			//		}
			//	}
			//}
			
			for (SootMethod sm : sc.getMethods()) {
				if (SystemClassHandler.isClassInSystemPackage(sm.getDeclaringClass().getName()))
					continue;
				if (sm.getDeclaringClass().isPhantomClass())
					continue;
				if (sm.hasActiveBody() || sm.isConstructor()) {
					Body body = sm.retrieveActiveBody();
					PatchingChain<Unit> units = body.getUnits();
					for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
						final Unit unit = (Unit) iter.next();
						if (unit.toString().contains(src)) {
							Stmt stmt = (Stmt) unit;
							if (stmt instanceof AssignStmt) {
								if (((AssignStmt) stmt).getRightOp() instanceof Local
										|| ((AssignStmt) stmt).getRightOp() instanceof Constant) {
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
	
	// Find Caller given Callee
	public List<SootMethod> findCaller(SootMethod callee) {
		List<SootMethod> callers = new ArrayList<SootMethod>();
		
		CallGraph cg = Scene.v().getCallGraph();
		Iterator<Edge> edgeIterator = cg.edgesInto(callee);
		while (edgeIterator.hasNext()) {
			callers.add(edgeIterator.next().src());
		}
				
		/* aggressive mode
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
		*/
		
		return callers;
	}
	
	// Find Caller given Callee
	public List<SootMethod> findInitializer(String src) {
		List<SootMethod> methods = new ArrayList<SootMethod>();
		for (SootClass sc : Scene.v().getClasses()) {
			
			for (SootMethod sm : sc.getMethods()) {
				if (SystemClassHandler.isClassInSystemPackage(sm.getDeclaringClass().getName()))
					continue;
				if (sm.getDeclaringClass().isPhantomClass())
					continue;
				if (sm.hasActiveBody() || sm.isConstructor()) {
					Body body = sm.retrieveActiveBody();
					PatchingChain<Unit> units = body.getUnits();
					for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
						final Unit unit = (Unit) iter.next();
						if (unit.toString().contains(src)) {
							//Stmt stmt = (Stmt) unit;
							//if (stmt instanceof AssignStmt) {
							//	if (((AssignStmt) stmt).getRightOp() instanceof Local
							//			|| ((AssignStmt) stmt).getRightOp() instanceof Constant) {
									methods.add(sm);
							//	}
							//}
						}
					}
				}
			}
		}
				
		return methods;
	}
	
	private LinkedHashMap<String, List<String>> makeCFG(LinkedHashMap<SootMethod, LinkedHashSet<Unit>> slicedStmts, String directory) {
		Iterator<SootMethod> it = slicedStmts.keySet().iterator();
		List<SootMethod> temp = new ArrayList<SootMethod>();
		
		CallGraph cfg = Scene.v().getCallGraph();
		LinkedHashMap<String, List<String>> new_cfg = new LinkedHashMap<String, List<String>>();
		
		// extract only methods
		while (it.hasNext()) {
			SootMethod sm = it.next();
			temp.add(sm);
		}
		
		// delete duplicated elements
		List<SootMethod> methods = new ArrayList<SootMethod>(new HashSet<SootMethod>(temp));
		
		// for Backward
		if (backwardTainting) {
			for (SootMethod sm : methods) {
				Iterator<Edge> srcMethods = cfg.edgesInto(sm);
				List<String> temp2 = new ArrayList<String>();
				
				while(srcMethods.hasNext()) {
					Edge srcMethod = srcMethods.next();
					for (SootMethod m : methods) {
						if (m.equals(srcMethod.getSrc())) {
							if (!sm.equals(m)) {
								temp2.add(srcMethod.getSrc().toString());
								//System.out.println("SRC: "+srcMethod.getSrc()+" DST: "+sm);
							}
						}
					}
				}
				
				List<String> sources = new ArrayList<String>(new HashSet<String>(temp2));
				
				new_cfg.put(sm.toString(), sources);
			}
		}
		// for Forward
		else {
			for (SootMethod sm : methods) {
				Iterator<Edge> dstMethods = cfg.edgesOutOf(sm);
				List<String> temp3 = new ArrayList<String>();
				
				while(dstMethods.hasNext()) {
					Edge dstMethod = dstMethods.next();
					for (SootMethod m : methods) {
						if (m.equals(dstMethod.getTgt())) {
							if (!sm.equals(m)) {
								temp3.add(dstMethod.getSrc().toString());
								//System.out.println("SRC: "+sm+" DST: "+dstMethod.getTgt());
							}
						}
					}
				}
				
				List<String> sources = new ArrayList<String>(new HashSet<String>(temp3));
				
				new_cfg.put(sm.toString(), sources);
			}	
		}	
		
		// serialization
		try {       
			FileOutputStream f = new FileOutputStream(directory+"/"+"cfg.ser"); 
			ObjectOutput o = new ObjectOutputStream(f); 
			o.writeObject(new_cfg);
			o.close();
			f.close();
		}
		catch (IOException e) { }
		
		return new_cfg;
	}
	
	/**
	 * Create each classes
	 * @param slicedStmts The sliced statements
	 */
	private void createClass_backup(LinkedHashMap<SootMethod, LinkedHashSet<Unit>> slicedStmts) {
		SootClass sClass;
		SootMethod sMethod;
		LinkedHashMap<String, String> renamePair = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> originalMethod = new LinkedHashMap<String, String>();
		
		sClass = new SootClass("sliceMainClass_"+String.valueOf(++class_count), Modifier.PUBLIC);
		Scene.v().addClass(sClass);
		
		sMethod = new SootMethod("sliceMain", Arrays.asList(new Type[] {}), VoidType.v());
		sClass.addMethod(sMethod);
		JimpleBody body = Jimple.v().newBody(sMethod);
		sMethod.setActiveBody(body);
		Chain<Unit> units = body.getUnits();
		
		ArrayList<Unit> tmpMainList = new ArrayList<Unit>();
		
		Iterator<SootMethod> it = slicedStmts.keySet().iterator();
		
		int methodCount = 0;
		int duplicatedMethod = 0;
		while (it.hasNext()) {
			SootMethod method = it.next();
			originalMethod.put(method.getName(), method.toString());
			if (methodCount == 0) {
				
				sMethod.setName(method.getName());
				sMethod.setParameterTypes(method.getParameterTypes());
				
				List<Local> localVals = new ArrayList<Local>();
				ArrayList<Unit> stmtList = new ArrayList<Unit>();
				
				for (Unit unit : slicedStmts.get(method)) {
					localVals = createLocals(unit);
					
					for (Local local : localVals) {
						if (!isLocalContain(body.getLocals(), local))
							body.getLocals().add(local);
					}
					
					if (!isUnitContain(tmpMainList, unit) && !(unit instanceof IdentityStmt)) {
						tmpMainList.add(unit);
					}
				}
				
				/*
				// Find additional statements and add them into body
				ArrayList<Unit> additionalStmts = new ArrayList<Unit>();
				while (!tmpMainList.isEmpty()) {
					Unit temp = tmpMainList.get(0);
					if (!isUnitContain(additionalStmts, temp)) {
						additionalStmts.add(temp);
					}
					tmpMainList.remove(0);
					for (Unit u : findAdditionalStmt(temp, method)) {
						if (!isUnitContain(additionalStmts, u)) {
							tmpMainList.add(u);
						}
					}
				}
				
				for (Unit u : additionalStmts) {
					localVals = createLocals(u);
					for (Local local : localVals) {
						if (!isLocalContain(body.getLocals(), local))
							body.getLocals().add(local);
					}
					if (!isUnitContain(tmpMainList, u)) {
						tmpMainList.add(u);
					}
				}
				*/
				
				// For sorting
				SootMethod sm = Scene.v().getMethod(method.getSignature());
				for (Unit tmp : sm.retrieveActiveBody().getUnits()) {
					stmtList.add(tmp);
				}

				for (int i = 0; i < stmtList.size(); i++) {
					for (Unit unit : tmpMainList) {
						if (stmtList.get(i).equals(unit))
							units.add(unit);
					}
				}
				
				tmpMainList.clear();
				stmtList.clear();
			}
			else {				
				sMethod = new SootMethod(method.getName(),
						method.getParameterTypes(), method.getReturnType(), method.getModifiers());
				List<Local> localVals = new ArrayList<Local>();
				
				String renamedMethod = null;
				for (SootMethod sm : sClass.getMethods()) {
					if (sm.getName().equals(sMethod.getName())) {
						renamedMethod = new String(sMethod.getName()+"_renamed_"+String.valueOf(++duplicatedMethod));
						renamePair.put(method.toString(), renamedMethod);
					}
				}
				
				ArrayList<Unit> stmtList = new ArrayList<Unit>();
				ArrayList<Unit> tmpList = new ArrayList<Unit>();
				
				if (renamedMethod != null) {
					sMethod.setName(renamedMethod);
					// for eliminating dead function
					originalMethod.put(renamedMethod, method.toString());
				}
				sClass.addMethod(sMethod);
				originalMethod.put(method.getName(), method.toString());
				
				body = Jimple.v().newBody(sMethod);
				sMethod.setActiveBody(body);
				units = body.getUnits();
				
				for (Unit unit : slicedStmts.get(method)) {
					localVals = createLocals(unit);
						
					for (Local local : localVals) {
						if (!isLocalContain(body.getLocals(), local))
							body.getLocals().add(local);
					}
						
					if (!isUnitContain(tmpList, unit) && !(unit instanceof IdentityStmt)) {
						tmpList.add(unit);
					}
				}
					
				/*
				// Find additional statements and add them into body
				ArrayList<Unit> additionalStmts = new ArrayList<Unit>();
				while (!tmpList.isEmpty()) {
					//System.out.println("[*] #"+methodCount+" SUB: sizeof(tmpMainList) = "+tmpList.size());
					Unit temp = tmpList.get(0);
					if (!isUnitContain(additionalStmts, temp)) {
						additionalStmts.add(temp);
					}
					tmpList.remove(0);
					for (Unit u : findAdditionalStmt(temp, method)) {
						if (!isUnitContain(additionalStmts, u)) {
							tmpList.add(u);
						}
					}
						//System.out.println("[*] #"+methodCount+" SUB: sizeof(tmpMainList) = "+tmpList.size());
				}
					
				for (Unit u : additionalStmts) {
					localVals = createLocals(u);
					for (Local local : localVals) {
						if (!isLocalContain(body.getLocals(), local))
							body.getLocals().add(local);
					}
					if (!isUnitContain(tmpList, u)) {
						tmpList.add(u);
					}
				}
				*/
					
				// For sorting
				SootMethod sm = Scene.v().getMethod(method.getSignature());
				for (Unit tmp : sm.retrieveActiveBody().getUnits()) {
					stmtList.add(tmp);
				}
					
				for (int i = 0; i < stmtList.size(); i++) {
					for (Unit unit : tmpList) {
						if (stmtList.get(i).equals(unit))
							units.add(unit);
					}
				}
					
				stmtList.clear();
				tmpList.clear();
			}

			methodCount++;
		}

		// For IdentityStmt (We interest only parameters)
		for (SootMethod sm2 : sClass.getMethods()) {
			Body body2 = sm2.getActiveBody();
			LocalGenerator lg = new LocalGenerator(body2);
			Iterator<Type> paramIt = sm2.getParameterTypes().iterator();
			while (paramIt.hasNext()) {
	            Type sootType = paramIt.next();
	            Local paramLocal = lg.generateLocal(sootType);
	             
	            soot.jimple.ParameterRef paramRef = soot.jimple.Jimple.v().newParameterRef(sootType, 0);
	            soot.jimple.Stmt stmt = soot.jimple.Jimple.v().newIdentityStmt(paramLocal, paramRef);
	            body2.getUnits().addFirst(stmt);
	        }
		}
		
		// For method name renaming
		for (SootMethod slicedMethod : sClass.getMethods()) {
			Body slicedBody = slicedMethod.getActiveBody();
			PatchingChain<Unit> slicedUnits = slicedBody.getUnits();
			for (Iterator<Unit> iter = slicedUnits.snapshotIterator(); iter.hasNext();) {
				final Unit unit = (Unit) iter.next();
				final Stmt stmt = (Stmt) unit;
				if (stmt.containsInvokeExpr()) {
					String value = renamePair.get(stmt.getInvokeExpr().getMethod());
					if (value != null) {
						SootMethod oldMethod = stmt.getInvokeExpr().getMethod();
						SootMethod newMethod = null;
						for (SootMethod ss : sClass.getMethods()) {
							if (ss.getName().equals(renamePair.get(stmt.getInvokeExpr().getMethod()))) {
								newMethod = ss;
							}
						}
						
						InvokeExpr inv = stmt.getInvokeExpr();
						InvokeExpr newinv;
						Stmt newStmt;
						
						if (inv instanceof StaticInvokeExpr) {
							if (inv.getArgCount() > 0) {
								List<Value> callerArgs = inv.getArgs();
								newinv = Jimple.v().newStaticInvokeExpr(newMethod.makeRef(), callerArgs);
							} else {
								newinv = Jimple.v().newStaticInvokeExpr(newMethod.makeRef());
							}

							if (stmt instanceof AssignStmt)	
								newStmt	= Jimple.v().newAssignStmt(((AssignStmt) stmt).getLeftOp(), newinv);
							else
								newStmt = Jimple.v().newInvokeStmt(newinv);
							
							slicedUnits.insertAfter(newStmt,unit);
							slicedUnits.remove(unit);
							
							for (SootMethod sm : sClass.getMethods()) {
								if (sm.getName().equals(newStmt.getInvokeExpr().getMethod().getName())) {
									Edge newEdge = new Edge(slicedMethod, newStmt, sm);
									Scene.v().getCallGraph().addEdge(newEdge);
								}
							}
						}
						else {
							Local base = (Local) ((InstanceInvokeExpr) inv).getBase();
							
							if (inv.getArgCount() > 0) {
								List<Value> callerArgs = inv.getArgs();
								if (oldMethod.getDeclaringClass().isInterface()) {
									newinv = Jimple.v().newInterfaceInvokeExpr(base, newMethod.makeRef(), callerArgs);
								} else {
									newinv = Jimple.v().newVirtualInvokeExpr(base, newMethod.makeRef(), callerArgs);
								}
							} else {
								if (oldMethod.getDeclaringClass().isInterface()) {
									newinv = Jimple.v().newInterfaceInvokeExpr(base, newMethod.makeRef());
								} else {
									newinv = Jimple.v().newVirtualInvokeExpr(base, newMethod.makeRef());
								}
							}

							if (stmt instanceof AssignStmt)	
								newStmt	= Jimple.v().newAssignStmt(((AssignStmt) stmt).getLeftOp(), newinv);
							else
								newStmt = Jimple.v().newInvokeStmt(newinv);
							
							slicedUnits.insertAfter(newStmt,unit);
							slicedUnits.remove(unit);
							
							
							for (SootMethod sm : sClass.getMethods()) {
								if (sm.getName().equals(newStmt.getInvokeExpr().getMethod().getName())) {
									Edge newEdge = new Edge(slicedMethod, newStmt, sm);
									Scene.v().getCallGraph().addEdge(newEdge);
								}
							}
						}
					}
				}
			}
		}
		
		try{       
			FileOutputStream f = new FileOutputStream("renamePair_originalMethod.ser"); 
			ObjectOutput s = new ObjectOutputStream(f); 
			s.writeObject(renamePair); 
			s.writeObject(originalMethod); 
			s.close();
			f.close();
		}
		catch (IOException e) { }		

		this.sClasses.add(sClass);
	}
	
	/**
	 * Check whether the local is contained
	 * @param locals Body's all locals
	 * @param local A new local to be added
	 * @return hasLocal Return true if the local is existed in the locals
	 */
	private boolean isLocalContain(Chain<Local> locals, Local local) {
		Boolean hasLocal = false;
		Iterator<Local> i = locals.iterator();

		while (i.hasNext()) {
			Local u = i.next();
			if (u.toString().equals(local.toString())) {
				hasLocal = true;
			}
		}
		
		return hasLocal;
	}
	
	/**
	 * Check whether the unit is contained
	 * @param tmpList Body's all units
	 * @param local A new unit to be added
	 * @return hasUnit Return true if the unit is existed in the units
	 */
	private boolean isUnitContain(ArrayList<Unit> tmpList, Unit local) {
		Boolean hasUnit = false;
		Iterator<Unit> i = tmpList.iterator();

		while (i.hasNext()) {
			Unit u = i.next();
			if (u.toString().equals(local.toString())) {
				hasUnit = true;
			}
		}
		
		return hasUnit;
	}

	/**
	 * Create local variables
	 * @param unit The target statement
	 * @return localVals The extracted local variables
	 */
	private List<Local> createLocals(Unit unit) {
		List<Local> localVals = new ArrayList<Local>();
		
		if (((Stmt) unit).containsInvokeExpr()) {
			if (unit instanceof AssignStmt) {
				final AssignStmt assignStmt = (AssignStmt) unit;
				final Value left = assignStmt.getLeftOp();
				final Value leftValue = BaseSelector.selectBase(left, true);
				Local val;
				
				if (!(leftValue instanceof StaticFieldRef)) {
					val = Jimple.v().newLocal(leftValue.toString(), leftValue.getType());
					localVals.add(val);
				}
			}
			
			final InvokeExpr ie = ((Stmt) unit).getInvokeExpr();
			if (!(ie instanceof StaticInvokeExpr)) {
				final InstanceInvokeExpr vie = (InstanceInvokeExpr) ie;
				
				final Value[] callArgs = new Value[ie.getArgCount()];
				for (int i = 0; i < ie.getArgCount(); i++)
					callArgs[i] = ie.getArg(i);
				
				if (vie.getBase() != null) {
					Local val = Jimple.v().newLocal(vie.getBase().toString(), vie.getBase().getType());
					localVals.add(val);
				}
				
				for (Value arg : callArgs) {
					if (!(arg instanceof Constant)) {
						Local val = Jimple.v().newLocal(arg.toString(), arg.getType());
						localVals.add(val);
					}
				}
			}
			
			if (ie instanceof StaticInvokeExpr) {
				
				final Value[] callArgs = new Value[ie.getArgCount()];
				for (int i = 0; i < ie.getArgCount(); i++)
					callArgs[i] = ie.getArg(i);
				
				for (Value arg : callArgs) {
					if (!(arg instanceof Constant)) {
						Local val = Jimple.v().newLocal(arg.toString(), arg.getType());
						localVals.add(val);
					}
				}
			}
		} else if (unit instanceof ThrowStmt) {
			ThrowStmt throwStmt = (ThrowStmt) unit;
			
			Local val = Jimple.v().newLocal(throwStmt.getOp().toString(), throwStmt.getOp().getType());
			localVals.add(val);
		}
		else {
			if (unit instanceof AssignStmt) {
				
				final AssignStmt assignStmt = (AssignStmt) unit;
				final Value left = assignStmt.getLeftOp();
				final Value leftValue = BaseSelector.selectBase(left, true);
				final Value right = assignStmt.getRightOp();
				final Value[] rightVals = BaseSelector.selectBaseList(right, true);
				Local val;
				
				if (leftValue instanceof InstanceFieldRef) {
					InstanceFieldRef leftRef = (InstanceFieldRef) leftValue;
					
					val = Jimple.v().newLocal(leftRef.getBase().toString(), leftRef.getBase().getType());
					localVals.add(val);
				}
				if (leftValue instanceof ArrayRef) {
					val = Jimple.v().newLocal(((ArrayRef) leftValue).getBase().toString(), ((ArrayRef) leftValue).getBase().getType());
					localVals.add(val);
					if (((ArrayRef) leftValue).getIndex() instanceof Local) {
						val = Jimple.v().newLocal(((ArrayRef) leftValue).getIndex().toString(), ((ArrayRef) leftValue).getIndex().getType());
						localVals.add(val);
					}
				}
				else {
					if (!(leftValue instanceof StaticFieldRef)) {
						if (leftValue instanceof Local) {
							val = Jimple.v().newLocal(leftValue.toString(), leftValue.getType());
							localVals.add(val);
						}
					}
				}

				for (Value rightval : rightVals) {
					if (!(rightval instanceof Constant) 
							&& !(rightval instanceof StaticFieldRef) 
							&& !(rightval instanceof NewExpr)) {
						if (rightval instanceof FieldRef) {
							if (rightval instanceof InstanceFieldRef) {
								InstanceFieldRef rightRef = (InstanceFieldRef) rightval;
								val = Jimple.v().newLocal(rightRef.getBase().toString(), rightRef.getBase().getType());
								localVals.add(val);
							}
						}
						if (rightval instanceof ArrayRef) {
							val = Jimple.v().newLocal(((ArrayRef) rightval).getBase().toString(), ((ArrayRef) rightval).getBase().getType());
							localVals.add(val);
						}
						else {
							if (!(rightval instanceof ConcreteRef)) {
								val = Jimple.v().newLocal(rightval.toString(), rightval.getType());
								localVals.add(val);
							}
						}
					}
				}
			}
		}
		return localVals;
	}

	/**
	 * Create each classes
	 * @return sClasses The created dummyMainClasses
	 */
	public HashSet<SootClass> getDummyClass() {
		return this.sClasses;
	}

	/**
	 * Get JimpleASTs
	 * @return jimpleASTs The created JimpleASTs
	 */
	public List<JimpleAST> getJimpleAST() {
		for (SootClass sClass : sClasses) {
			StringWriter string = new StringWriter();
			PrintWriter writer = new PrintWriter(string);
			Printer printer = new Printer(null);
			
			printer.printTo(sClass, writer, true);
			byte[] barray = string.toString().getBytes(StandardCharsets.UTF_8);			
			
			InputStream is = new ByteArrayInputStream(barray);
			
			try {
				jimpleAST.add(new JimpleAST(is));
			} catch (ParserException e) {
				e.printStackTrace();
			} catch (LexerException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return this.jimpleAST;
	}
	
	/**
	 * Print class
	 * @return void
	 */
	private void printClass(SootClass sClass) {
		int classCount = 1;
		
		System.out.println("\n");
		System.out.println("[" + classCount + "/" + sClasses.size() + "] : "+sClass.getMethods().size()+" methods");
			
		PrintWriter writer = new PrintWriter(System.out, true);
		Printer printer = new Printer(null);
			
		printer.printTo(sClass, writer, true);
			
		classCount++;
	}
	
	/**
	 * Print all example_dymmy Classes
	 * @return void
	 */
	public void printAllDummyClasses() {
		
		
		int classCount = 1;
		PrintWriter writer = new PrintWriter(System.out, true);
		try {
			writer = new PrintWriter(new FileWriter("slices.txt"), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		for (SootClass sClass : sClasses) {
			int unitCount = 0;
			
			for (SootMethod sm : sClass.getMethods())
				//unitCount += sm.retrieveActiveBody().getUnits().size();
				unitCount += sm.getActiveBody().getUnits().size();
			
			System.out.println("\n");
			System.out.println("[" + classCount + "/" + sClasses.size() + "] : "+sClass.getMethods().size()+" methods, "+unitCount+" slices");
			
			Printer printer = new Printer(null);
			
			printer.printTo(sClass, writer, true);
			
			classCount++;
		}
	}

	public LinkedList<HashMap<Unit, List<String>>> getEntrypoints() {
		return this.finalEPs;
	}
}