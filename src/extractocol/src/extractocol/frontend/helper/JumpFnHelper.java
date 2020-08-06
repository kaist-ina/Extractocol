package extractocol.frontend.helper;

import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import extractocol.frontend.output.basic.TaintResultEntry;
import soot.Unit;
import soot.jimple.infoflow.collect.MyConcurrentHashMap;

public class JumpFnHelper {
	static MyConcurrentHashMap<String, Integer> endSummary = new MyConcurrentHashMap<String, Integer>();
	static int _id = 0;
	static Lock l = new ReentrantLock();
	
	public static Integer getMethodID(String m){
		if(!endSummary.containsKey(m)){
			l.lock();
			try{
				endSummary.put(m, _id);
				_id++;
			}finally{
				l.unlock();
			}
		}
		return endSummary.get(m);
	}
	
	public static class SmallTRE{
		Integer currMethod;
		Integer DPMethod;
		String DPStmt;
		LinkedList<Integer> taintMethodSet = new LinkedList<Integer>();
		
		int hashCode;
		
		public SmallTRE(String _currMethod, TaintResultEntry _tre){
			this.currMethod = getMethodID(_currMethod);
			this.DPMethod = getMethodID(_tre.getDPMethod());
			this.DPStmt = _tre.getDPStmt();
			
			for(String tm : _tre.getTaintMethodSet())
				this.taintMethodSet.add(getMethodID(tm));
			
			final int prime = 31;
			int result = 1;
			result = prime * result + ((currMethod == null) ? 0 : currMethod.hashCode());
			result = prime * result + ((DPMethod == null) ? 0 : DPMethod.hashCode());
			result = prime * result + ((DPStmt == null) ? 0 : DPStmt.hashCode());
			result = prime * result + ((taintMethodSet == null) ? 0 : taintMethodSet.hashCode());
			
			this.hashCode = result;
		}
		
		public void clear(){
			this.currMethod = null;
			this.DPMethod = null;
			this.DPStmt = null;
			this.taintMethodSet.clear();
			this.taintMethodSet = null;
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
			
			SmallTRE other = (SmallTRE) obj;
			
			if (currMethod == null) {
				if (other.currMethod != null)
					return false;
			} else if (!currMethod.equals(other.currMethod))
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
			
			if (taintMethodSet == null) {
				if (other.taintMethodSet != null)
					return false;
			} else if (!taintMethodSet.equals(other.taintMethodSet))
				return false;
			
			return true;
		}
	}
}
