package emulator;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.util.SystemClassHandler;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.Chain;

public class Emulator {
	
	private final SootMethod mainMethod;
	private final IInfoflowCFG iCfg;
	private Stack<LinkedList<Object>> stack = new Stack<LinkedList<Object>>();
	private LinkedList<List<Object>> heap = new LinkedList<List<Object>>();
	private Map<Type, Object> mapping = new HashMap<Type, Object>();
	private List<Unit> instructions = new ArrayList<Unit>();
	private List<Unit> subinstructions = new ArrayList<Unit>();
	private int pc;
	private int bp;
	private int variableCount;
	private boolean isExit = false;
	private boolean DEBUG = false;
	
	public Emulator(SootMethod main, IInfoflowCFG icfg) {
		this.mainMethod = main;
		this.iCfg = icfg;
		this.pc = 0;
		this.bp = 0;
		
		makeInstructions();
		int localvariableCount = initialzeStack(mainMethod);
		dispatcher();
				
	}
	
	// We consider all System Types (such as 'java.net.URL', 'int', 'boolean', ...) as a 'java.lang.String' type.
	private int initialzeStack(SootMethod method) {
		Chain<Local> locals = method.retrieveActiveBody().getLocals();
		
		for (Local local : locals) {
			Type type = local.getType();
			LinkedList<Object> entry1 = new LinkedList<Object>();
			
			if (isSystemType(type)) {
				entry1.add(local.toString());
				entry1.add("--");
			}
			else {
				List<Object> entry2 = new ArrayList<Object>();
				for (SootField field : Scene.v().getSootClass(local.getType().toString()).getFields()) {
					List<String> entry3 = new ArrayList<String>();
					entry3.add(field.toString());
					entry3.add("++");
					entry2.add(entry3);
				}
				
				entry1.add(local.toString());
				entry1.add(entry2);
			}
			
			stack.push(entry1);
		}
		
		return locals.size();
	}

	// Dispatch instruction (Unit)
	private void dispatcher() {	
		for (;;) {
			if (pc < instructions.size()) {
				
				if (DEBUG) {
					System.out.println("\n\n\n[*] PC -> "+instructions.get(pc));
				}
				
				executor(instructions.get(pc));
				
				if (DEBUG) {
					printStack();
					printHeap();
					printMapping();		
				}
				
				// Use only for debugging!
				//if (instructions.get(pc-1).toString().contains("<java.net.URL: void <init>(java.lang.String)>"))
					//System.exit(0);
				
				if (isExit)
					return;
			}
			else {
				break;
			}
			
			// Use only for debugging!
			//if (!iCfg.getMethodOf(instructions.get(pc)).equals(iCfg.getMethodOf(instructions.get(pc-1))))
			//	System.out.println();
		}
	}
	
	// Execute jimple statement
	private void executor(Unit unit) {
		Stmt stmt = (Stmt) unit;
		
		if (stmt instanceof IdentityStmt) {
			//System.out.println("\tIdentityStmt");
			executeIdentityStmt(stmt);
			pc++;
			return;
		}
		if (stmt.containsInvokeExpr()) {
			//System.out.println("\tInvokeExpr");
			executeInvokeExpr(stmt);				
			pc++;
			return;
		}
		if (stmt instanceof AssignStmt) {
			//System.out.println("\tAssignStmt");
			executeAssignStmt(stmt);
			pc++;
			return;
		}
		if (stmt instanceof ReturnStmt || stmt instanceof ReturnVoidStmt) {
			//System.out.println("\tReturnStmt");
			executeReturnStmt(stmt);
			return;
		}
		else {
			pc++;
		}
	}	
	
	// ReturnStmt handler
	private void executeReturnStmt(Stmt stmt) {
		if (stmt instanceof ReturnStmt) {
			ReturnStmt retStmt = (ReturnStmt) stmt;
			Value retValue = retStmt.getOp();
		}
		if (stmt instanceof ReturnVoidStmt) {
			
		}
		
		epilogue(stmt);
	}

	// IdentityStmt handler
	private void executeIdentityStmt(Stmt stmt) {
		IdentityStmt iStmt = (IdentityStmt) stmt;
		Value leftValue = iStmt.getLeftOp();
		Value rightValue = iStmt.getRightOp();
		
		String identifier = rightValue.toString().substring(
				rightValue.toString().indexOf("@") + 1, rightValue.toString().indexOf(":"));
		
		if (identifier.contains("this")) {
			;
		}
		if (identifier.contains("parameter")) {
			int paramIndex = Integer.parseInt(identifier.substring(9));
			
			// We skip main method
			if (bp > 0) {
				updateStack(bp, leftValue, stack.get(bp-2-paramIndex).get(1).toString());
			}
		}		
	}

	// InvokeExpr handler
	private void executeInvokeExpr(Stmt stmt) {
		InvokeExpr ie = stmt.getInvokeExpr();
		SootMethod method = ie.getMethod();
		
		// Emulate predefined append() method
		if (method.toString().equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>")) {
			InstanceInvokeExpr inv = (InstanceInvokeExpr) ie;
			Value base = inv.getBase();
			Value param = inv.getArg(0);
			Value left = ((AssignStmt) stmt).getLeftOp();
			
			final String temp;
			if (param instanceof Constant) {
				temp = getLocalValueFromStack(bp, base)+param.toString();
				updateStack(bp, left, temp);
			}
			else {
				temp = getLocalValueFromStack(bp, base)+getLocalValueFromStack(bp, param);
				updateStack(bp, left, temp);
			}
			
			return;
		}
		// Emulate predefined toString() method
		if (method.toString().equals("<java.lang.StringBuilder: java.lang.String toString()>")) {
			InstanceInvokeExpr inv = (InstanceInvokeExpr) ie;
			Value base = inv.getBase();
			Value left = ((AssignStmt) stmt).getLeftOp();
			
			final String temp = getLocalValueFromStack(bp, base);
			updateStack(bp, left, temp);
			
			return;
		}
		if (method.toString().equals("<java.net.URL: void <init>(java.lang.String)>")) {
			List<Value> callArgs = ie.getArgs();
			System.out.println("\n\t URL: :"+getLocalValueFromStack(bp, callArgs.get(0)));
			isExit = true;
			return;
		}
		// Ignore system libraries
		if (isSystemType(method.getDeclaringClass().getType())) {
			return;
		}
		// If defined method,
		else {
			// Initialize method parameters
			List<Value> callArgs = ie.getArgs();

			// Push method parameters into the stack
			for (int i = callArgs.size()-1; i >= 0; i--) {
				LinkedList<Object> param = new LinkedList<Object>();
				param.add("parameter"+Integer.toString(i));
				param.add(getLocalValueFromStack(bp, callArgs.get(i)));
				
				stack.push(param);
			}

			// Save Return address
			LinkedList<Object> ret = new LinkedList<Object>();
			int retIndex;
			if (ie instanceof InterfaceInvokeExpr) {
				retIndex = pc + submakeInstructions(findCallee((Unit) stmt)) + 1;
			}
			else {
				retIndex = pc + submakeInstructions(ie.getMethod()) + 1;
			}
			ret.add("ret");
			ret.add(Integer.toString(retIndex));
			stack.push(ret);			
			
			prologue(ie, stmt);
		}
	}
	
	// We make instruction array according to ICFG's control flows.
	private int submakeInstructions(SootMethod sm) {
		subinstructions.clear();
		for (Unit unit : sm.retrieveActiveBody().getUnits()) {
			Stmt stmt = (Stmt) unit;
			subinstructions.add(unit);
			
			final InvokeExpr ie = (stmt != null && stmt.containsInvokeExpr()) ? stmt.getInvokeExpr() : null;
			if (ie != null) {
				if (!SystemClassHandler.isClassInSystemPackage(ie.getMethod().getDeclaringClass().getName())) {
					subcallInstractions(unit);
				}
			}
		}
		
		return subinstructions.size();
	}
	
	// When making instruction array ..
	private void subcallInstractions(Unit unit) {	
		SootMethod callee = findCallee(unit);
		
		if (callee != null) {
			for (Unit u : callee.retrieveActiveBody().getUnits()) {
				Stmt stmt = (Stmt) u;
				subinstructions.add(u);
				
				final InvokeExpr ie = (stmt != null && stmt.containsInvokeExpr()) ? stmt.getInvokeExpr() : null;
				if (ie != null) {
					if (!SystemClassHandler.isClassInSystemPackage(ie.getMethod().getDeclaringClass().getName())) {
						subcallInstractions(u);
					}
				}
			}
		}
	}

	// Emulate user defined method
	private void prologue(InvokeExpr ie, Stmt stmt) {
		// Store caller's stack frame pointer into the stack
		LinkedList<Object> sfp = new LinkedList<Object>();
		sfp.add("sfp");
		sfp.add(Integer.toString(bp));
		stack.push(sfp);
		
		// New base frame pointer
		bp = stack.size()-1;
		
		// Initialize local variables
		if (ie instanceof InterfaceInvokeExpr) {
			variableCount = initialzeStack(findCallee((Unit) stmt));
		}
		else
			variableCount = initialzeStack(ie.getMethod());
	}
	
	// Emulate user defined method
	private void epilogue(Stmt stmt) {
		// Release local variables
		for (int i = 0; i < variableCount; i++)
			stack.pop();
		// Restore saved frame pointer
		bp = Integer.parseInt(stack.pop().get(1).toString());
		
		// Restore saved PC
		pc = Integer.parseInt(stack.pop().get(1).toString());
		
		// Release method parameters
		for (int i = 0; i < iCfg.getMethodOf(stmt).getParameterCount(); i++)
			stack.pop();
	}

	// AssignStmt handler
	private void executeAssignStmt(Stmt stmt) {
		AssignStmt assignStmt = (AssignStmt) stmt;
		Value rightValue = assignStmt.getRightOp();
		Value leftValue = assignStmt.getLeftOp();
		
		if (!isSystemType(leftValue.getType()) && rightValue.toString().contains("new")) {
			for (int i = bp; i < stack.size(); i++) {
				LinkedList<Object> temp = stack.get(i);
				if (temp.contains(leftValue.toString())) {
					heap.add(temp);
					mapping.put(leftValue.getType(), stack.get(i).get(0));
				}
			}
		}
		
		if (rightValue instanceof StaticFieldRef 
				&& rightValue.getType().toString().equals("java.lang.String")) {
			List<Value> results = findMethodfromHeapAllocStmt(rightValue.toString());
			
			for (Value linearsearchValue : results) {
				if (linearsearchValue instanceof Constant) {
					updateStack(bp, leftValue, linearsearchValue.toString());
					return;
				}
			}
			
			// Store into the Stack
			updateStack(bp, leftValue, rightValue.toString());
		}
		if (rightValue instanceof FieldRef
				&& rightValue instanceof InstanceFieldRef) {
			Value base = ((InstanceFieldRef) rightValue).getBase();
			SootField field = ((InstanceFieldRef) rightValue).getField();
			
			String value = null;
			if (iCfg.getMethodOf(stmt).retrieveActiveBody().getThisLocal().equals(base)) {
				value = getBaseFieldValueFromHeap(base.getType(), field);
			}
			else {
				value = getBaseFieldValueFromStack(bp, base, field);
			}
 
			if (value != null) {
				updateStack(bp, leftValue, value);
			}
		}
		if (leftValue instanceof FieldRef
				&& leftValue instanceof InstanceFieldRef) {
			Value base = ((InstanceFieldRef) leftValue).getBase();
			SootField field = ((InstanceFieldRef) leftValue).getField();
			
			String value = getLocalValueFromStack(bp, rightValue);
			if (value != null) {
				updateStack(bp, base, field, value);
			}
			
			if (iCfg.getMethodOf(stmt).retrieveActiveBody().getThisLocal().equals(base)) {
				if (mapping.get(base.getType()) != null) {
					String target = mapping.get(base.getType()).toString();
					updateHeap(target, field, value);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private String getBaseFieldValueFromHeap(Type type, SootField field) {
		if (mapping.get(type)!= null) {
			for (int i = 0; i < heap.size(); i++) {
				List<Object> temp = heap.get(i);
				if (temp.contains(mapping.get(type))) {
					List<Object> temp1 = (List<Object>) temp.get(1);
					for (Object object : temp1) {
						List<String> temp2 = (List<String>) object;
						if (temp2.contains(field.toString())) {
							return temp2.get(1);
						}
					}
				}
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private void updateHeap(String type, SootField field, String value) {
		for (int i = 0; i < heap.size(); i++) {
			List<Object> temp = heap.get(i);
			if (temp.contains(type)) {
				List<Object> temp1 = (List<Object>) temp.get(1);
				for (Object object : temp1) {
					List<String> temp2 = (List<String>) object;
					if (temp2.contains(field.toString())) {
						temp2.set(1, value);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void updateStack(int bp, Value base, SootField field, String value) {
		for (int i = bp; i < stack.size(); i++) {
			LinkedList<Object> temp = stack.get(i);
			if (temp.contains(base.toString())) {
				List<Object> temp1 = (List<Object>) temp.get(1);
				for (Object object : temp1) {
					List<String> temp2 = (List<String>) object;
					if (temp2.contains(field.toString())) {
						temp2.set(1, value);
					}
				}
			}
		}
	}

	// Find field from stack
	@SuppressWarnings("unchecked")
	private String getBaseFieldValueFromStack(int bp, Value base, SootField field) {
		for (int i = bp; i < stack.size(); i++) {
			LinkedList<Object> temp = stack.get(i);
			if (temp.contains(base.toString())) {
				List<Object> temp1 = (List<Object>) temp.get(1);
				for (Object object : temp1) {
					List<String> temp2 = (List<String>) object;
					if (temp2.contains(field.toString()))
						return temp2.get(1).toString();
				}
			}
		}
		
		return null;
	}
	
	// Find value from stack
	private String getLocalValueFromStack(int bp, Value base) {
		for (int i = bp; i < stack.size(); i++) {
			LinkedList<Object> temp = stack.get(i);
			if (temp.contains(base.toString())) {
				return temp.get(1).toString();
			}
		}
		
		return null;
	}

	// Update variable in Stack
	private void updateStack(int bp, Value key, String value) {
		for (int i = bp; i < stack.size(); i++) {
			LinkedList<Object> temp = stack.get(i);
			if (temp.contains(key.toString())) {
				LinkedList<Object> temp2 = new LinkedList<Object>();
				temp2.add(key.toString());
				temp2.add(value);
				stack.set(i, temp2);
			}
				
		}
	}

	// We make instruction array according to ICFG's control flows.
	private void makeInstructions() {		
		for (Unit unit : mainMethod.retrieveActiveBody().getUnits()) {
			Stmt stmt = (Stmt) unit;
			
			instructions.add(unit);
			//System.out.println("\t"+stmt);
			
			final InvokeExpr ie = (stmt != null && stmt.containsInvokeExpr()) ? stmt.getInvokeExpr() : null;
			if (ie != null) {
				if (!SystemClassHandler.isClassInSystemPackage(ie.getMethod().getDeclaringClass().getName())) {
					callInstractions(unit);
				}
			}
		}
	}
	
	// When making instruction array ..
	private void callInstractions(Unit unit) {	
		SootMethod callee = findCallee(unit);
		
		if (callee != null) {
			for (Unit u : callee.retrieveActiveBody().getUnits()) {
				Stmt stmt = (Stmt) u;
				instructions.add(u);
				
				final InvokeExpr ie = (stmt != null && stmt.containsInvokeExpr()) ? stmt.getInvokeExpr() : null;
				if (ie != null) {
					if (!SystemClassHandler.isClassInSystemPackage(ie.getMethod().getDeclaringClass().getName())) {
						callInstractions(u);
					}
				}
			}
		}
	}
	
	// Find callee (only one target - ignore <cinit> and <init>)
	private SootMethod findCallee(Unit unit) {
		List<SootMethod> callees = new ArrayList<SootMethod>();
		String methodName = ((Stmt) unit).getInvokeExpr().getMethod().getName();
		
		CallGraph cg = Scene.v().getCallGraph();
		Iterator<Edge> edgeIterator = cg.edgesOutOf(unit);
		while (edgeIterator.hasNext()) {
			callees.add(edgeIterator.next().tgt());
		}
		
		// We may find all callees including <cinit> or <init>, but we return callee except for them.
		for (SootMethod sm : callees) {
			if (sm.getName().equals(methodName))
				return sm;
		}
		
		return null;
	}
	
	// Linear Search for Constant String
	private List<Value> findMethodfromHeapAllocStmt(String src) {
		List<Value> strings = new ArrayList<Value>();
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
							Stmt stmt = (Stmt) unit;
							if (stmt instanceof AssignStmt) {
								if (((AssignStmt) stmt).getRightOp() instanceof Local
										|| ((AssignStmt) stmt).getRightOp() instanceof Constant) {
									strings.add(((AssignStmt) stmt).getRightOp());
								}
							}
						}
					}
				}
			}
		}
				
		return strings;
	}
	
	// Print current stack
	private void printStack() {
		System.out.println("\n\n");
		System.out.println("========== STACK ==========");
		for (int i = 0; i<stack.size(); i++)
			System.out.println(stack.get(i));
		System.out.println("---------------------------");
		System.out.println("PC: "+pc+", BP: "+bp+", SP: "+(stack.size()-1));
		System.out.println("===========================");
		System.out.println("\n");
	}
	
	// Print current heap
	private void printHeap() {
		System.out.println("========== HEAP ==========");
		for (int i = 0; i<heap.size(); i++)
			System.out.println(heap.get(i));
		System.out.println("===========================");
		System.out.println("\n");
	}
	
	// Print current current heap mapping table
	private void printMapping() {
		System.out.println("======== MAPPING =========");
		for (Type key : mapping.keySet())
			System.out.println("["+key+", "+mapping.get(key)+"]");
		System.out.println("===========================");
		System.out.println("\n");
	}
	
	// Return if type is System Type.
	private boolean isSystemType(Type type) {
		String typeName = type.toString();
		return typeName.startsWith("android.")
				|| typeName.startsWith("java.")
				|| typeName.startsWith("sun.")
				|| typeName.startsWith("org.")
				|| typeName.startsWith("javax.")
				|| typeName.equals("boolean")
				|| typeName.equals("int")
				|| typeName.equals("double")
				|| typeName.equals("byte")
				|| typeName.equals("char")
				|| typeName.equals("float")
				|| typeName.equals("long")
				|| typeName.equals("short");
	}
	
	// Emulate user defined method
	private void executeUserdefinedMethod(InvokeExpr ie, Stmt stmt) {
		// Store caller's stack frame pointer into the stack
		LinkedList<Object> sfp = new LinkedList<Object>();
		sfp.add("sfp");
		sfp.add(Integer.toString(bp));
		stack.push(sfp);
		
		// New base frame pointer
		bp = stack.size()-1;
		
		// Initialize local variables
		if (ie instanceof InterfaceInvokeExpr) {
			variableCount = initialzeStack(findCallee((Unit) stmt));
		}
		else
			variableCount = initialzeStack(ie.getMethod());
		
		// Release local variables
		for (int i = 0; i < variableCount; i++)
			stack.pop();
		
		// Restore saved frame pointer
		bp = Integer.parseInt(stack.pop().get(1).toString());
	}
}