package extractocol.common.retrofit.struct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import extractocol.common.outputs.backendOutputHelper.RequestInfoEntry;
import extractocol.common.tools.Pair;

public class Param {
	public static enum PARAM_TYPE {PATH, URL, QUERY, FIELD, QUERY_MAP, BODY, PART, NONE}
	
	PARAM_TYPE retrofitType = PARAM_TYPE.NONE;
	String type = null;
	String keyword = null;
	boolean isNullable = false;
	
	Set<String> resultSet = null;
	List<Pair> resultMap = null;
	
	Semaphore lock = new Semaphore(1); // to prevent concurrent modification
	
	public void clear() {
		if(this.resultSet != null) {
			this.resultSet.clear();
			this.resultSet = null;
		}
		
		if(this.resultMap != null) {
			this.resultMap.clear();
			this.resultMap = null;
		}
		
		this.lock = null;
	}
	
	public PARAM_TYPE getRetrofitType() { return this.retrofitType; }
	public void setRetrofitType(PARAM_TYPE rt) { this.retrofitType = rt; }
	
	public String getType() { return this.type; }
	public void setType(String t) { this.type = t; }
	
	public String getKeyword() { return this.keyword; }
	public void setKeyword(String k) { this.keyword = k; }
	
	public boolean getIsNullable() {return this.isNullable; }
	public void setIsNullable(boolean b) { this.isNullable = b; }
	
	public Set<String> getResultSet() { return this.resultSet; }
	public void addResult(String r) {
		if(this.resultSet == null) 
			this.resultSet = new HashSet<String>();
		
		this.resultSet.add(r);
	}
	
	public void lock() { try { this.lock.acquire(); } catch (InterruptedException e) {} }
	public void unlock() { this.lock.release(); }
	
	public String getFinalResultFromSet() {
		if(this.resultSet == null)
			return "(.*)";
		
		String res = "";
		int i = 0;
		
		for(String s: this.resultSet) {
			if(i < this.resultSet.size() - 1)
				res = res + s + "|";
			else
				res = res + s;
			i++;
		}
		
		return "(" + res + ")";
	}
	
	public List<Pair> getResultMap() { return this.resultMap; }
	public void addResult(String k, String v) {
		if(this.resultMap == null)
			this.resultMap = new ArrayList<Pair>();
		
		RequestInfoEntry.addBody(this.resultMap, k, v);
	}
}
