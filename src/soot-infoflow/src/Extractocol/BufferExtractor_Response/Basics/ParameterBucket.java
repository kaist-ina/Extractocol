package Extractocol.BufferExtractor_Response.Basics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;
import Extractocol.BufferExtractor_Request.Semantic.JSON.JSONBuilder;
import Extractocol.BufferExtractor_Response.SignatureBuilder_Response;
import Extractocol.BufferExtractor_Response.Helper.CycleDetectionforPhinodes;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapEntry;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapTable;
import Extractocol.BufferExtractor_Request.HeapManagement.ValueEntryTracking;

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
	
	// BK For tracking JSON key list of variable
	//public HeapTable varTable;
	
	//public ArrayList<HeapEntry> returnValueEntryList;
	//public ArrayList<HeapEntry> baseValueEntryList;
	
	// Initialization
	public ParameterBucket(SignatureBuilder_Response be, SootMethod sm){
		this.be = be;
		this.sm = sm;
		this.BFTtable = new HashMap<String, ArrayList<BFNode_Response>>();
		this.taintVariableAndSeedPair = new HashMap<String, String>();
		this.taintParameters = new HashSet<String>();
		this.json_child_parent = new HashMap<String, String>();
		this.seed = new ArrayList<String>();
		this.Cdp = new CycleDetectionforPhinodes();
		this.variable_type = new HashMap<String, String>();
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