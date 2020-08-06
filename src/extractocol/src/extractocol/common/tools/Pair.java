package extractocol.common.tools;

import java.io.Serializable;
import java.util.List;

@SuppressWarnings("serial")
public class Pair implements Serializable {
	// key & value are original signature.
	// e.g., value = (60)|(<heap>)
	String origKey;
	String origValue;
	
	// regexKey & regexValue are regex signature.
	// e.g., value = (60)|(.*)
	String regexSigKey;
	String regexSigValue;
	
	// proxySigKey & proxySigValue are signature for prefetching in proxy.
	// e.g., value = (60)|(<1>)
	String proxySigKey;
	String proxySigValue;
	
	public Pair(){ this.origKey = null; this.origValue = null; }
	public Pair(String ok, String ov) {this.origKey = ok; this.origValue = ov;}
	public Pair(String ok, String ov, String rk, String rv, String pk, String pv)
	{ this.origKey = ok; this.origValue = ov; this.regexSigKey = rk; this.regexSigValue = rv; this.proxySigKey = pk; this.proxySigValue = pv;}
	
	public String getKey(){ return this.origKey; }
	public String getValue(){ return this.origValue; }
	public void setKey(String k){ this.origKey = k; }
	public void setValue(String val){ this.origValue = val;	}
	
	public String getRegexSigKey(){ return this.regexSigKey; }
	public String getRegexSigValue(){ return this.regexSigValue; }
	public void setRegexSigKey(String regexKey) { this.regexSigKey = regexKey; }
	public void setRegexSigValue(String regexValue) { this.regexSigValue = regexValue; }
	
	public String getProxySigKey(){ return this.proxySigKey; }
	public String getProxySigValue(){ return this.proxySigValue; }
	public void setProxySigKey(String proxyKey) { this.proxySigKey = proxyKey; }
	public void setProxySigValue(String proxyValue) { this.proxySigValue = proxyValue; }
	
	public Pair Clone(){ return new Pair(this.origKey, this.origValue, this.regexSigKey, this.regexSigValue, this.proxySigKey, this.proxySigValue); }
	
	public static void Merge(List<Pair> dst, List<Pair> src) {
		for(Pair p: src)
			addKeyValue(p.getKey(), p.getValue(), dst);
	}
	
	public static void addKeyValue(String key, String value, List<Pair> pairList) {
		// 1. try to find entry with same key
		Pair tgt = null;
		for(Pair p: pairList) {
			if(p.getKey().equals(key)) {
				tgt = p;
				break;
			}
		}
		
		// 2. add value
		if(tgt == null)
			pairList.add(new Pair(key, value));
		else {
			String val = tgt.getValue();
			val += "|" + value;
			tgt.setValue(val);
		}
	}
}
