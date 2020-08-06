package extractocol.backend.response.basics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import extractocol.backend.common.BackendThread;
import extractocol.backend.request.semantic.json.JSONBuilder;
import extractocol.backend.response.SignatureBuilder_Response;
import extractocol.backend.response.helper.CycleDetectionforPhinodes;
import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntryTable;
import extractocol.common.valueEntry.ValueEntryTracking;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;

public class ParameterBucket extends ValueEntryTracking
{
	// There is parameter set cycle
	
	// From Start of ExtractStmt 
	public SignatureBuilder_Response be;
	public SootMethod sm;
	public HashMap<String, ArrayList<BFNode_Response>> BFTtable;

	// taint variables and parameters
	public HashMap<String, String> taintVariableAndSeedPair;
	public Set<String> taintParameters;
	
	// From Block Graph Traveler
	public Unit ut;

	// Invoke Handler
	public int invokeType;
	public InvokeExpr ie;
	
	//CycleDetector
	public CycleDetectionforPhinodes Cdp;
	
	// Semantic
	public String strDest;	
	
	// For tracking JSON tree hierarchy
	// Code by Byungkwon
	public ArrayList<String> seed; // name of seed variable
	public HashMap<String, String> json_child_parent; // key: name of child variable, value: name parent variable
	
	// BK For tracking variable type 
	public HashMap<String, String> variable_type;
	
	// BK back-edge
	public boolean[][] BackEdge;
	
	// For tracking tainted variables (BK)
	private HashMap<String, Set<String>> pointsToAnalysis = new HashMap<String, Set<String>>();
	private Set<String> taintedVariables = new HashSet<String>();
	private boolean returnVarTainted = false;
	private boolean thisTainted = false;
	private String thisVariable;
	
	BackendThread bt;
	
	public BackendThread BT() { return this.bt; }
	public void setBT(BackendThread _bt) { this.bt = _bt; } 
	
	public Set<String> getPointSource(String source) { return this.pointsToAnalysis.get(source); }
	
	public void addPTA(String target, String source){
		if(target.equals(source))
			return;
		
		if(this.pointsToAnalysis.containsKey(source)){
			Set<String> tgtSet = this.pointsToAnalysis.get(source);
			tgtSet.add(target);
		}else{
			Set<String> tgtSet = new HashSet<String>();
			if(target != null) tgtSet.add(target);
			this.pointsToAnalysis.put(source, tgtSet);
		}
	}
	
	public Set<String> getTaintedVariables() { return this.taintedVariables; }
	public void addTaintedVariable(String var){ this.taintedVariables.add(var); }
	public boolean isVarTainted(String var) { return this.taintedVariables.contains(var); }
	
	public void setReturnVarTainted(boolean b) { this.returnVarTainted = b; }
	public boolean isReturnVarTainted() { return this.returnVarTainted; }
	
	public void setThisTainted(boolean b) { this.thisTainted = b; }
	public boolean isThisTainted() { return this.thisTainted; }
	
	public void setThisVariable(String var) { this.thisVariable = var; }
	public String getThisVariable() { return this.thisVariable; }
	public boolean isThisVariable(String var) { return var.equals(this.thisVariable); }
	
	// Initialization
	public ParameterBucket(SignatureBuilder_Response be, SootMethod sm, BackendThread _bt){
		this.be = be;
		this.sm = sm;
		this.BFTtable = new HashMap<String, ArrayList<BFNode_Response>>();
		this.taintVariableAndSeedPair = new HashMap<String, String>();
		this.taintParameters = new HashSet<String>();
		this.json_child_parent = new HashMap<String, String>();
		this.seed = new ArrayList<String>();
		this.Cdp = new CycleDetectionforPhinodes();
		this.variable_type = new HashMap<String, String>();
		
		this.bt = _bt;
	}
	
	public void clear(){
		for(Set<String> srcSet : this.pointsToAnalysis.values())
			srcSet.clear();
		
		this.pointsToAnalysis.clear();
		this.taintedVariables.clear();
		this.variable_type.clear();
		this.json_child_parent.clear();
		this.seed.clear();
		this.taintVariableAndSeedPair.clear();
		
		//for(ArrayList<BFNode_Response> BFNodes : this.BFTtable.values())
			//BFNodes.clear();
		
		//this.BFTtable.clear();
	}
	
	// TODO: find correct position for this function
	public static void set_JSON_Parent(String child, String parent, ParameterBucket pb){
		if(pb.json_child_parent != null){
			pb.json_child_parent.put(child, parent);
		}
	}
	
	public static void add_seed(String seed, ParameterBucket pb){
		if(pb.seed != null){
			pb.seed.add(seed);
		}
	}
}