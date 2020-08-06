package extractocol.frontend.output.basic;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.gaurav.tree.LinkedTree;
import com.gaurav.tree.NodeNotFoundException;
import com.gaurav.tree.Tree;
import extractocol.backend.request.heapManagement.HeapTreeNode;
import extractocol.common.tools.highScaleLib.NonBlockingHashSet;
import extractocol.frontend.basic.ExtractocolLogger;
import extractocol.frontend.basic.ExtractocolLogger.MYCOLOR;
import extractocol.frontend.helper.PropagateHelper.PROPA_TYPE;
import extractocol.frontend.output.InitialTaintResultContainer;
import extractocol.frontend.output.InitialTaintResultContainer.TAINT_METHOD_TYPE;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.toolkits.callgraph.Edge;

public class TaintResultEntry {
	private static int _ID = 0;
	static int MaxDepth = 3;
	
	private int ID;
	private int depth = 0;
	
	private String DPMethod; // Method containing Demarcation Point
	private String DPStmt; // Demarcation Point
	private int DPStmtHash = -1;
	
	private LinkedList<String> taintMethods = new LinkedList<String>();
	private LinkedList<String> taintVariable = new LinkedList<String>();
	private LinkedList<Boolean> isMainStream = new LinkedList<Boolean>();
	
	private LinkedList<PROPA_TYPE> propaTypes = new LinkedList<PROPA_TYPE>();
	private LinkedList<String> prevStmts = new LinkedList<String>();
	
	private PROPA_TYPE propaType = PROPA_TYPE.NONE;
	private String prevStmt = null;
	
	private Set<String> taintMethodsFinal; // This is for optimization.
	
	private String EP;
	private int propagationCnt = 0;
	private int MainStreamLength = 0;
	
	public PROPA_TYPE getPropaType() { return this.propaType; }
	public void setPropaType(PROPA_TYPE pt) { this.propaType = pt;}
	public void addPropaType(PROPA_TYPE pt) { this.propaTypes.add(pt); }
	public LinkedList<PROPA_TYPE> getPropaTypes() { return this.propaTypes; }
	
	public String getPrevStmt() { return this.prevStmt; }
	public void setPrevStmt(String s) { this.prevStmt = s; }
	public void addPrevStmt(String s) { this.prevStmts.add( (s!=null) ? s : "null"); }
	public LinkedList<String> getPrevStmts() { return this.prevStmts; }
	
	/** APIs for demarcation point **/
	public void setDPMethod(String _dp) { this.DPMethod = _dp; }
	public String getDPMethod() { return this.DPMethod; }
	
	public void setDPStmtHash(int hash) { this.DPStmtHash = hash; }
	public int getDPStmtHash() { return this.DPStmtHash; }
	
	public void setDPStmt(String _dpStmt) { this.DPStmt = _dpStmt; }
	public String getDPStmt() { return this.DPStmt; }
	
	public void setEP(String _ep) { this.EP = _ep; }
	public String getEP() { return this.EP; }
	
	public void setDepth(int d) { this.depth = d;}
	public void IncreaseDepth() { this.depth++; }
	public void DecreaseDepth() { if(this.depth > 0) this.depth--; }
	public int getDepth() { return this.depth; }
	public boolean doesReachMaxDepth() { return this.depth >= MaxDepth; }
	
	public static int getMaxDepth() { return MaxDepth; }
	public static int setMaxDepth(int maxD) {
		int oldMaxDepth = MaxDepth;
		//if(maxD > 0) maxD--;
		MaxDepth = maxD;
		return oldMaxDepth;
	}
	
	/** APIs for taint method set **/
	public void setTaintMethodSet(LinkedList<String> methodSet) { this.taintMethods = methodSet; }
	public void setPropagationCount(int cnt) { this.propagationCnt = cnt; }
	public int getPropagationCount() { return this.propagationCnt; }
	public void IncreasePropagationCount() { this.propagationCnt++; }
	public void setMainStreamLength(int msl) { this.MainStreamLength = msl; }
	public int getMainStreamLength() {
		int cnt = 0;
		for(boolean b: this.isMainStream)
			if(b) cnt++;
		
		return cnt;
	}
	public void IncreaseMainStreamLength() { this.MainStreamLength++; }

	private HashMap<TaintInfo, LinkedList<TaintInfo>> TaintTree = new HashMap<>();
	
	private boolean needToStay = false;
	private static int bitSIZE = 32;
	private int[] mergeesBit = null;
	private int[] mergersBit = null;
	
	public void setMergeeBit(int i) { setBit(i, this.mergeesBit); }
	public boolean getMergeeBit(int i) { return getBit(i, this.mergeesBit); } 
	private void allocMergeeBit(int size) { this.mergeesBit = new int[size];}
	
	public void setMergerBit(int i) { setBit(i, this.mergersBit); }
	public boolean getMergerBit(int i) { return getBit(i, this.mergersBit); }
	private void allocMergerBit(int size) { this.mergersBit = new int[size];}
	
	public void allocMergerMergeeBit(int sizeOrig) {
		int size = (int) Math.ceil((double)sizeOrig/bitSIZE);
		this.allocMergeeBit(size);
		this.allocMergerBit(size);
		
		for(int i = 0; i < size; i++) {
			this.mergeesBit[i] = 0;
			this.mergersBit[i] = 0;
		}
	}
	
	public void clearMergerMergeeBit() {
		this.mergersBit = null;
		this.mergeesBit = null;
	}
	
	private static boolean getBit(int i, int[] bits) {
		// e.g., when i = 88, idx = 2 & offset = 24
		int idx = i / bitSIZE;
		int offset = i - (idx * bitSIZE);
		int mask = 1 << offset;
		
		return (bits[idx] & mask) != 0; 
	}
	
	private static void setBit(int i, int[] bits) {
		// e.g., when i = 88, idx = 2 & offset = 24
		int idx = i / bitSIZE;
		int offset = i - (idx * bitSIZE);
		int mask = 1 << offset;
		
		bits[idx] = bits[idx] | mask;
	}
	
	public int getMergerListSize() { return bitCount(mergersBit); }
	public int getMergeeListSize() { return bitCount(mergeesBit); }
	
	private static int bitCount(int[] bits) {
		int cnt = 0;
		for(int i = 0; i < bits.length; i++)
			cnt += Integer.bitCount(bits[i]);
		
		return cnt;
	}
	
	public void setNeedToStay(boolean b) { this.needToStay = b; }
	public boolean getNeedToStay() { return this.needToStay; }
	
	/*private BitSet mainStreamBitArray = new BitSet();
	public void setMainStreamBit(int idx) {	this.setMainStreamBit(idx, true); }
	public void setMainStreamBit(int idx, boolean b) { this.mainStreamBitArray.set(idx, b); }*/
	
	/*static final int bitArrSize = 50;
	static final int stepSize = 60;
	public long[] bitArr = new long[bitArrSize];
	static long[] bitArrForTest = new long[bitArrSize];
	public void setMainStreamBit(int idx) {
		int arrIdx = idx / stepSize;
		this.bitArr[arrIdx] |= 1L << (idx - arrIdx * stepSize); 
	}*/
	
	public void taintMethodlistToSet() {
		if(this.taintMethods == null)
			return;
		
		this.taintMethodsFinal = new HashSet<String>(this.taintMethods);
	}
	
	public Set<String> getFinalTaintMethods() { return this.taintMethodsFinal; }
	
	public boolean SameDPWith(TaintResultEntry other){
		return this.DPMethod.equals(other.DPMethod)
				&& this.DPStmt.equals(other.DPStmt)
				&& (this.DPStmtHash == other.DPStmtHash);
	}
	
	public boolean ContainMainStreamOf(TaintResultEntry other){
		/*for(int i = 0; i < bitArrSize; i++) {
			boolean res = other.bitArr[i] == (this.bitArr[i] & other.bitArr[i]);
			if(!res)
				return false;
			bitArrForTest[i] = other.bitArr[i] ^ (this.bitArr[i] & other.bitArr[i]);
			bitArrForTest[0] |= bitArrForTest[i];
		}
		return true;*/
		//return bitArrForTest[0] == 0;
		
		// 1. check bit array first
		
		/*BitSet res = (BitSet) this.mainStreamBitArray.clone();
		res.and(other.mainStreamBitArray);
		
		return res.equals(other.mainStreamBitArray);*/
		
		/*if(!res.equals(other.mainStreamBitArray)) {
			res.clear();
			return false;
		}
		//res.clear();
		return true;*/
		
		// 2. Check exactly
		for(int i = 0; i < other.taintMethods.size(); i++){
			if(!other.isMainStream.get(i))
				continue;
			
			if(!this.taintMethods.contains(other.taintMethods.get(i)))
				return false;
			
			if(!this.isMainStream.get(this.taintMethods.indexOf(other.taintMethods.get(i))))
				return false;
		}
		
		return true;
	}
	
	public void addTaintMethodSetOf(TaintResultEntry other){
		// Version 1
		/*for(int i = 0; i < other.taintMethods.size(); i++){
			String target = other.taintMethods.get(i);
			
			if(this.taintMethods.contains(target))
				continue;
			
			int idx = this.IndexOfItsCaller(target);
			
			if(idx == -1)
				continue;
			
			this.taintMethods.add(idx+1, target);
			this.taintVariable.add(idx+1, other.taintVariable.get(i));
			this.isMainStream.add(idx+1, other.isMainStream.get(i));
		}*/
		
		// Version 2
		if(InitialTaintResultContainer.tmType == TAINT_METHOD_TYPE.LIST) {
			Set<String> tMethods = new HashSet<String>(this.taintMethods);
			String[] tVar = other.taintVariable.toArray(new String[other.taintVariable.size()]);
			Boolean[] isMain = other.isMainStream.toArray(new Boolean[other.isMainStream.size()]);
			
			for(int i = 0; i < other.taintMethods.size(); i++){
				String target = other.taintMethods.get(i);
			
				//if(other.isMainStream.get(i))
				if(isMain[i])
					continue;
				
				
				//if(this.taintMethods.contains(target))
				if(tMethods.contains(target))
					continue;
				
				tMethods.add(target);
				this.taintMethods.add(target);
				
				//this.taintVariable.add(other.taintVariable.get(i));
				//this.isMainStream.add(other.isMainStream.get(i));
				
				this.taintVariable.add(tVar[i]);
				this.isMainStream.add(isMain[i]);
			}
			
			tMethods.clear();
		}else if(InitialTaintResultContainer.tmType == TAINT_METHOD_TYPE.SET) {
			// Version 3
			this.taintMethodsFinal.addAll(other.taintMethodsFinal);
		}
	}
	
	public boolean addTaintMethod(String method, String variable){
		/*if(!this.taintMethods.contains(method)){
			this.taintMethods.add(method);
			this.taintVariable.add(variable);
			this.isMainStream.add(depth == 0);
			return true;
		}
		
		return false;*/
		return addTaintMethod(method, variable, depth == 0);
	}
	
	public boolean addTaintMethod(String method, String variable, Boolean isMainStream){
		if(!this.taintMethods.contains(method)){
			this.taintMethods.add(method);
			this.taintVariable.add(variable);
			this.isMainStream.add(isMainStream);
			
			this.propaTypes.add(this.propaType);
			if(this.prevStmt != null)
				this.addPrevStmt(this.prevStmt);
			else {
				StackTraceElement[] i = new Throwable().getStackTrace();
				String s = "null [";
				s = s + i[1].getMethodName() + " - ";
				s = s + i[2].getMethodName() + " - ";
				s = s + i[3].getMethodName() + " - ... ]";
				this.addPrevStmt(s);
			}
				
			return true;
		}
		
		return false;
	}
	
	/*public boolean containOnCreate() {
		for(String m: this.taintMethods)
			if(m.contains("FlipagramCommentsActivity: void onCreate(android.os.Bundle)"))
				return true;
	
		return false;
	}*/
	
	public LinkedList<String> getTaintMethodSet() { return this.taintMethods; }
	public LinkedList<String> getTaintVariableSet() { return this.taintVariable; }
	public LinkedList<Boolean> getIsMainStreamSet() { return this.isMainStream; }
	
	public void Clear() {
		this.DPMethod = null;
		this.DPStmt = null;
		this.DPStmtHash = 0;
		if(this.taintMethods!= null){ this.taintMethods.clear(); this.taintMethods = null; }
		if(this.taintVariable!= null){ this.taintVariable.clear(); this.taintVariable = null; }
		if(this.isMainStream!= null){ this.isMainStream.clear(); this.isMainStream = null; }
		
		if(this.propaTypes!= null){ this.propaTypes.clear(); this.propaTypes = null; }
		if(this.prevStmts!= null){ this.prevStmts.clear(); this.prevStmts = null; }
		if(this.taintMethodsFinal != null) { this.taintMethodsFinal.clear(); this.taintMethodsFinal = null; }
		
		this.prevStmt = null;
		this.EP = null;
		
		if(this.TaintTree != null) { this.TaintTree.clear(); this.TaintTree = null; }
		
		/*if(this.mergees != null) { this.mergees.clear(); this.mergees = null; }
		if(this.mergers != null) { this.mergers.clear(); this.mergers = null; }*/
		
		this.mergeesBit = null;
		this.mergersBit = null;
	} 
	
	public int getID() { return this.ID; }
	
	public TaintResultEntry Clone() {
		return new TaintResultEntry(this);
	}
	
	public TaintResultEntry CloneAndCutTaintMethodSetUntil(int idx){
		TaintResultEntry newTRE = this.Clone();
		List<String> newTaintMethodSet = newTRE.getTaintMethodSet().subList(0, idx);
		
		newTRE.setTaintMethodSet(new LinkedList<String>(newTaintMethodSet));
		return newTRE;
	}
	
	public TaintResultEntry CloneDecreaseDepthIncreaseMainStreamLength() {
		TaintResultEntry newTRE = new TaintResultEntry(this);
		newTRE.DecreaseDepth();
		newTRE.IncreaseMainStreamLength();
		return newTRE;
	}
	
	public TaintResultEntry CloneAndDecreaseDepth() {
		TaintResultEntry newTRE = new TaintResultEntry(this);
		newTRE.DecreaseDepth();
		return newTRE;
	}
	
	public TaintResultEntry CloneAndIncreaseDepth() {
		TaintResultEntry newTRE = new TaintResultEntry(this);
		newTRE.IncreaseDepth();
		return newTRE;
	}
	
	public TaintResultEntry CloneAndAddTaintMethod(String method, String variable) {
		TaintResultEntry newTRE = new TaintResultEntry(this);
		newTRE.addTaintMethod(method, variable, false);
		return newTRE;
	}
	
	public TaintResultEntry(String _dpMethod, String _dpStmt, int _dpStmtHash){
		this.ID = _ID++;
		this.DPMethod = _dpMethod;
		this.DPStmt = _dpStmt;
		this.DPStmtHash = _dpStmtHash; 
		
		this.propagationCnt = 0;
		/*for(int i = 0; i < bitArrSize; i++)
			this.bitArr[i] = 0b0;*/
	}
	
	@SuppressWarnings("unchecked")
	public TaintResultEntry(TaintResultEntry tre){
		this.ID = tre.ID;
		this.depth = tre.depth;
		this.DPMethod = tre.DPMethod;
		this.DPStmt = tre.DPStmt;
		this.DPStmtHash = tre.DPStmtHash;
		
		this.taintMethods = (LinkedList<String>)tre.taintMethods.clone();
		this.taintVariable = (LinkedList<String>)tre.taintVariable.clone();
		this.isMainStream = (LinkedList<Boolean>) tre.isMainStream.clone();
		this.propaTypes = (LinkedList<PROPA_TYPE>) tre.propaTypes.clone();
		this.prevStmts = (LinkedList<String>)tre.prevStmts.clone();
		
		this.propagationCnt = tre.propagationCnt;
		this.MainStreamLength = tre.MainStreamLength;
		this.TaintTree = new HashMap<>(tre.TaintTree);
		//this.bitArr = tre.bitArr.clone();
	}
	
	/** Check whether it is same with ae
	 * 
	 * @param ae AbstractionExtension
	 * @return True if it is same with ae
	 */
	@Override
	public boolean equals(Object obj){
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		TaintResultEntry other = (TaintResultEntry) obj;
		
		if (DPMethod == null) {
			if (other.DPMethod != null)
				return false;
		} else if (!DPMethod.equals(other.DPMethod))
			return false;
		
		if (DPStmt == null) {
			if (other.DPStmt != null)
				return false;
		} else if (!DPStmt.equals(other.DPStmt))
			return false;
		
		if (DPStmtHash == -1) {
			if (other.DPStmtHash != -1)
				return false;
		} else if (DPStmtHash != other.DPStmtHash)
			return false;
		
		if (taintMethods == null) {
			if (other.taintMethods != null)
				return false;
		} else if (!taintMethods.equals(other.taintMethods))
			return false;
		
		return true;
	}
	
	public boolean contains(TaintResultEntry other){
		if (this == other)
			return true;
		if (other == null)
			return false;
		
		if (DPMethod == null) {
			if (other.DPMethod != null)
				return false;
		} else if (!DPMethod.equals(other.DPMethod))
			return false;
		
		if (DPStmt == null) {
			if (other.DPStmt != null)
				return false;
		} else if (!DPStmt.equals(other.DPStmt))
			return false;
		
		if (DPStmtHash == -1) {
			if (other.DPStmtHash != -1)
				return false;
		} else if (DPStmtHash != other.DPStmtHash)
			return false;
		
		return this.taintMethods.containsAll(other.taintMethods);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.DPMethod == null) ? 0 : this.DPMethod.hashCode());
		result = prime * result + ((this.DPStmt == null) ? 0 : this.DPStmt.hashCode());
		result = prime * result + (this.DPStmtHash);
		result = prime * result + ((this.taintMethods == null) ? 0 : this.taintMethods.hashCode());

		return result;
	}
	
	public void PrintInfo(boolean colored){
		System.out.println("+ DP Stmt : " + this.getDPStmt());
		System.out.println("+ DP SootMethod : " + this.getDPMethod());
		
		System.out.println("+ EntryPoint :");
		ExtractocolLogger.coloredLog("\t+ " + this.getEP(), MYCOLOR.RED, colored);
		
		System.out.println("+ Tainted Methods & Variable ("+this.getTaintMethodSet().size()+") : ");
		for(int i = 0; i < this.getTaintMethodSet().size(); i++){
			String r = String.format("\t++ (%5s)  ", this.getTaintVariableSet().get(i));
			if(this.getIsMainStreamSet().get(i))
				ExtractocolLogger.coloredLog(r + this.getTaintMethodSet().get(i), MYCOLOR.RED, colored);
			else
				System.out.println(r + "  " + this.getTaintMethodSet().get(i));
		}
	}
	
	public String FindEP() /*throws Exception*/ {
		/**
		 * @Author : Jeongmin
		 * @Date : 2017.03.03
		 * Refine taintmethods set.
		 * When the last entity in taintmethods list has no explicit call flow to another taintMethods,
		 * EP is last - 1 entity.
		 */
		// Phase 1 : Obtaining out-edges of entity in the reverse order.
		/*for (Iterator<String> it = taintMethods.descendingIterator(); it.hasNext();) {
			String entity = it.next();
			SootMethod lastSm = Scene.v().getMethod(entity);

			Iterator<Edge> itrEdge = Scene.v().getCallGraph().edgesOutOf(lastSm);

			// Phase 2: Extracting EP candidate.
			while (itrEdge.hasNext()) {
				Edge ed = itrEdge.next();

				// If last entity has any out-edges to another taint methods, It's a EP candidate.
				if (!entity.equals(ed.tgt().getSignature()) && taintMethods.contains(ed.tgt().getSignature())) {
					return entity;
				}
			}
		}*/
		
		// The last main stream method is EP.
		for(int i = taintMethods.size() - 1; i >= 0; i--)
			// Find the last entry of main stream methods
			if(isMainStream.get(i))
				return taintMethods.get(i);

		// If last entity has not any out-edges to another methods, return null;
		//throw new Exception("Taintmethod set may be wrong. It has no EP candidate.");
		return null;
	}
	
	public String getInfoString(){
		return "[Prop. cnt: " + this.getPropagationCount() +"]"
			 + "[Main stream length: " + this.getMainStreamLength() + "]"
			 + "[EP var:"+ this.FindEPVar() + "]"
			 + "[EP: " + this.FindEP() +"]";	
	}
	
	public String FindEPVar(){
		for(int i = taintVariable.size() - 1; i >= 0; i--)
			// Find the last entry of main stream methods
			if(isMainStream.get(i))
				return taintVariable.get(i);

		return null;
	}
	
	private int IndexOfItsCaller(String target){
		for(Iterator<String> it = taintMethods.iterator(); it.hasNext();){
			String entity = it.next();
			
			SootMethod targetSm = Scene.v().getMethod(entity);
			Iterator<Edge> itrEdge = Scene.v().getCallGraph().edgesOutOf(targetSm);
			
			while (itrEdge.hasNext()) {
				Edge ed = itrEdge.next();

				if (target.contains(ed.tgt().getSignature())) {
					return taintMethods.indexOf(entity); 
				}
			}
		}
		
		return -1;
	}

	public void addTaintInfoNode(Abstraction _prev, Abstraction _curt, Unit _unit, SootMethod _sm)
	{
		if (_prev.equals(_curt))
		{
			TaintInfo self = new TaintInfo();
			self.obj = _prev.getAccessPath().getPlainValue().toString();
			self.abs = _prev;
			self.sm = _sm;
			self.selfcount = 0;
			self.unit = _unit;

			if (searchNode(self) == null)
			{
				TaintTree.put(self, new LinkedList<>());
			}
			else
			{
				searchNode(self).selfcount++;
			}
		}
		else
		{
			TaintInfo parent = new TaintInfo();
			parent.obj = _prev.getAccessPath().getPlainValue().toString();
			parent.abs = _prev;
			parent.sm = _sm;
			parent.selfcount = 0;
			parent.unit = _unit;

			TaintInfo child = new TaintInfo();
			child.unit = _unit;
			child.abs = _curt;
			child.obj = _curt.getAccessPath().getPlainValue().toString();
			child.selfcount = 0;

			if (searchNode(parent) != null)
			{
				LinkedList<TaintInfo> result = TaintTree.get(searchNode(parent));
				result.add(child);
			}
			else {
				LinkedList<TaintInfo> result = new LinkedList<>();
				result.add(child);
				TaintTree.put(parent, result);
			}
		}
	}

	public TaintInfo searchNode(TaintInfo x)
	{
		for (TaintInfo ti : TaintTree.keySet())
		{
			if (ti == null)
				System.out.println("TI is null.");

			if (ti.obj.equals(x.obj) && ti.sm.equals(x.sm)) {
				return ti;
			}
		}
		return null;
	}

	public TaintResultEntry Clone(Abstraction d2, Abstraction d3, Unit n, SootMethod method) {
		TaintResultEntry newTre = new TaintResultEntry(this);
		newTre.addTaintInfoNode(d2, d3, n, method);
		return newTre;
	}
}