package extractocol.frontend.helper;

import extractocol.common.MD5;
import extractocol.frontend.helper.JumpFnHelper.SmallTRE;
import extractocol.frontend.helper.PropagateHelper.MyEdgeEntry;
import extractocol.frontend.output.basic.TaintResultEntry;
import soot.Unit;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PropagateHelper {
	public enum PROPA_TYPE {INIT, CALL1, CALL2, CALL3, CALL4, CALL5, EXIT1, EXIT2, EXIT3, NORMAL, TR_B2F, TR_F2B, NONE, CALL5_1}
	//static ConcurrentHashMap<MyEdgeEntry, String> jumpFn = new ConcurrentHashMap<MyEdgeEntry, String>();
	static ConcurrentHashMap<MyEdgeEntry, BigInteger> jumpFn = new ConcurrentHashMap<MyEdgeEntry, BigInteger>();
	final static int mapSize = 100;
	//static ConcurrentHashMap<MyEdgeEntry, BigInteger>[] jumpFn = new ConcurrentHashMap[mapSize];
	
	private static Lock myLock = new ReentrantLock();
	private static int cnt = 0;
	
	private static int maxMainStream = 7;
	
	public static int getMaxMainStream(){ return maxMainStream; }
	public static int setMaxMainStream(int maxMS) {
		int oldMaxMainStream = maxMainStream;
		//if(maxMS > 0) maxMS--;
		maxMainStream = maxMS; 
		return oldMaxMainStream;
	}
	
	public static void clearJumpFn() { if(jumpFn != null) jumpFn.clear(); }
	
	public static void Initialize(){
		/*for(int i = 0; i < mapSize; i++)
			jumpFn[i] = new ConcurrentHashMap<MyEdgeEntry, BigInteger>();*/
	}
	
	public static void IncreaseCntAtomic(){
		myLock.lock();
		cnt++;
		myLock.unlock();
	}
	
	public static void DecreaseCntAtomic(){
		myLock.lock();
		cnt--;
		myLock.unlock();
	}
	
	public static int getCnt(){ return cnt;	}
	
	public static BigInteger addMyEdgeEntry(String targetVal, Unit targetUnit, String method, TaintResultEntry prevTRE){
		MyEdgeEntry mee = new MyEdgeEntry(targetVal, targetUnit, method, prevTRE);
		BigInteger existingVal = jumpFn.putIfAbsent(mee, mee.bi1);
		
		if(existingVal != null)
			mee.clear();
		
		return existingVal;
		
		/*int tmsSize = prevTRE.getTaintMethodSet().size();
		
		if(tmsSize == 0)
			return null;
		
		MyEdgeEntry mee = new MyEdgeEntry(targetVal, targetUnit, method, prevTRE);
		BigInteger existingVal = jumpFn[tmsSize].putIfAbsent(mee, mee.bi1);
		
		if(existingVal != null){
			mee.clear();
			return existingVal;
		}
		
		for(int i = 1; i < tmsSize; i++){
			TaintResultEntry subTRE = prevTRE.CloneAndCutTaintMethodSetUntil(i);
			mee = new MyEdgeEntry(targetVal, targetUnit, method, subTRE);
			jumpFn[tmsSize].putIfAbsent(mee, mee.bi1);
		}
		
		return null;*/
	}
	
	public static String genString1(String _targetVal, Unit _unit, String _method, TaintResultEntry _tre){
		String str1 = "";
		
		str1 += _targetVal + _unit.toString() + _unit.hashCode() + _method;
		str1 += _tre.getDPMethod() + _tre.getDPStmt() + _tre.getDPStmtHash();
		str1 += _tre.getTaintMethodSet().hashCode();
		/*for(String tm: _tre.getTaintMethodSet())
			str1 += tm;*/
		
		return str1;
	}
	
	public static String genString2(String _targetVal, Unit _unit, String _method, TaintResultEntry _tre){
		String str2 = "";
		
		for(String tm: _tre.getTaintMethodSet())
			str2 += tm;
		str2 += _tre.getDPMethod() + _tre.getDPStmt() + _tre.getDPStmtHash();
		str2 += _targetVal + _unit.toString() + _unit.hashCode() + _method;
		
		return str2;
	}
	
	public static int genHashCode(BigInteger bi1, BigInteger bi2){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bi1 == null) ? 0 : bi1.hashCode());
		result = prime * result + ((bi2 == null) ? 0 : bi2.hashCode());
		
		return result;
	}
	
	public static int genHashCode(BigInteger bi1){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bi1 == null) ? 0 : bi1.hashCode());
		
		return result;
	}
	
	public static class MyEdgeEntry{
		BigInteger bi1;
		//BigInteger bi2;
		
		int hashCode;
		
		public MyEdgeEntry(String _targetVal, Unit _unit, String _method, TaintResultEntry _tre){
			String str1 = genString1(_targetVal, _unit, _method, _tre);
			//String str2 = genString2(_targetVal, _unit, _method, _tre);
			
			this.bi1 = new BigInteger(MD5.getMD5(str1), 16);
			//this.bi2 = new BigInteger(MD5.getMD5(str2), 16);
			
			//this.hashCode = genHashCode(this.bi1, this.bi2);
			this.hashCode = genHashCode(this.bi1);
		}
		
		public void clear(){
			this.bi1 = null;
			//this.bi2 = null;
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			MyEdgeEntry other = (MyEdgeEntry) obj;
			
			if (bi1 == null) {
				if (other.bi1 != null)
					return false;
			} else if (!bi1.equals(other.bi1))
				return false;
			
			/*if (bi2 == null) {
				if (other.bi2 != null)
					return false;
			} else if (!bi2.equals(other.bi2))
				return false;*/
			
			return true;
		}
	}
	
	/*public static int getJumpFnSize(){
		return jumpFn.size();
	}
	
	public static ConcurrentHashMap<MyEdgeEntry, BigInteger> getJumpFn(){
		return jumpFn;
	}*/
	
	/*
	 public static String addMyEdgeEntry(MyEdgeEntry mee){
		return jumpFn.putIfAbsent(mee, mee.targetVal);
	 }
	  
	 public static class MyEdgeEntry{
		String targetVal;
		Unit unit;
		SmallTRE sTRE;
		
		int hashCode;
		
		public MyEdgeEntry(String _targetVal, Unit _unit, String _method, TaintResultEntry _tre){
			this.targetVal = _targetVal;
			this.unit = _unit;
			this.sTRE = new SmallTRE(_method, _tre);
			
			final int prime = 31;
			int result = 1;
			result = prime * result + ((targetVal == null) ? 0 : targetVal.hashCode());
			result = prime * result + ((unit == null) ? 0 : unit.hashCode());
			result = prime * result + ((sTRE == null) ? 0 : sTRE.hashCode());
			
			this.hashCode = result;
		}
		
		public void clear(){
			this.targetVal = null;
			this.unit = null;
			this.sTRE.clear();
			this.sTRE = null;
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			MyEdgeEntry other = (MyEdgeEntry) obj;
			
			if (targetVal == null) {
				if (other.targetVal != null)
					return false;
			} else if (!targetVal.equals(other.targetVal))
				return false;
			
			if (unit == null) {
				if (other.unit != null)
					return false;
			} else if (!unit.equals(other.unit))
				return false;
			
			if (sTRE == null) {
				if (other.sTRE != null)
					return false;
			} else if (!sTRE.equals(other.sTRE))
				return false;
			
			return true;
		}
	}*/
	
	/*public static class MyEdgeEntry{
		String targetVal;
		Unit unit;
		String Method;
		TaintResultEntry tre;
		int hashCode;
		
		public MyEdgeEntry(String _targetVal, Unit _unit, String _method, TaintResultEntry _tre){
			this.targetVal = _targetVal;
			this.unit = _unit;
			this.Method = _method;
			this.tre = _tre;
			
			final int prime = 31;
			int result = 1;
			result = prime * result + ((targetVal == null) ? 0 : targetVal.hashCode());
			result = prime * result + ((unit == null) ? 0 : unit.hashCode());
			result = prime * result + ((Method == null) ? 0 : Method.hashCode());
			result = prime * result + ((tre == null) ? 0 : tre.hashCode());
			
			this.hashCode = result;
		}
		
		public void clear(){
			this.targetVal = null;
			this.unit = null;
			this.Method = null;
			this.tre.Clear();
			this.tre = null;
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			
			MyEdgeEntry other = (MyEdgeEntry) obj;
			
			if (targetVal == null) {
				if (other.targetVal != null)
					return false;
			} else if (!targetVal.equals(other.targetVal))
				return false;
			
			if (unit == null) {
				if (other.unit != null)
					return false;
			} else if (!unit.equals(other.unit))
				return false;
			
			if (Method == null) {
				if (other.Method != null)
					return false;
			} else if (!Method.equals(other.Method))
				return false;
			
			if (tre == null) {
				if (other.tre != null)
					return false;
			} else if (!tre.equals(other.tre))
				return false;
			
			return true;
		}
	}*/
	
	
	/********************************************************************************/
	/**            APIs for checking whether a source propagates or not            **/
	/********************************************************************************/
	public static boolean[] deriveNewDidTaint(){
		boolean[] newDidTaint = new boolean[2];
		newDidTaint[0] = false;
		newDidTaint[1] = false;
		
		return newDidTaint;
	}
	
	public static void setPropagateOthers(boolean[] didTaint){ didTaint[0] = true; }
	public static void setPropagateItself(boolean[] didTaint){ didTaint[1] = true; }
	
	public static boolean didPropagateOthers(boolean[] didTaint){ return didTaint[0]; }
	public static boolean didPropagateItself(boolean[] didTaint){ return didTaint[1]; }
	
	public static boolean didPropagate(boolean[] didTaint){
		return didPropagateOthers(didTaint) || didPropagateItself(didTaint);
	}
}
