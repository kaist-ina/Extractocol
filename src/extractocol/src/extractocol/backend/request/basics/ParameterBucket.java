
package extractocol.backend.request.basics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import extractocol.backend.common.BackendThread;
import extractocol.common.valueEntry.ValueEntryTracking;
import soot.SootMethod;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;

public class ParameterBucket extends ValueEntryTracking
{
	/*
	 * Parseurl Method
	 */
	public SootMethod CurrentSM;
	/*
	 * ExtractStmt Method - it's name will be changed.
	 */
	public HashMap<String, ArrayList<BFNode>> BFTtable;
	/*
	 * BlockTraveler Params
	 */
	public HashMap<String, ArrayList<BFNode>> blockBFTtable;
	public BriefBlockGraph Bg;
	public Block nextBlock;
	public LinkedList<Block> BFSentry;
	public Integer[][] EdgeEntry;
	public Integer[][] maniEdge;
	public HashMap<String, HashMap<String, ArrayList<BFNode>>> BFTtableMap;
	public Integer[] VisitEntry;
	/*
	 * UnitHandler
	 */
	public String strDest;
	public JSONArray ep_methods;
	public JSONArray requestEntries;
	public JSONObject requestEntry;
	public JSONArray epstmts;
	
	public boolean[][] BackEdge;
	
	BackendThread bt;
	
	public BackendThread BT() { return this.bt; }
	public void setBT(BackendThread _bt) { this.bt = _bt; }
	
	public ParameterBucket(SootMethod _Sm)
	{
		CurrentSM = _Sm;
	}
}
