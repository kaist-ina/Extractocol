
package extractocol.common.outputs.backendOutputHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gaurav.tree.LinkedTree;
import com.gaurav.tree.Tree;

import extractocol.Constants;
import extractocol.backend.request.heapManagement.HeapTreeNode;
import extractocol.backend.request.semantic.url.UrlBuilder;
import extractocol.common.pairing.PairingDPEntry;
import extractocol.common.pairing.SemanticAppliedList;
import extractocol.common.tools.Pair;
import extractocol.common.valueEntry.PartofUrlStringTable;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntryTable;
import extractocol.common.valueEntry.node.Constant;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Arrays;

@SuppressWarnings("serial")
public class RequestInfoEntry extends CommonInfo implements Serializable {
	public Set<String> methods = new HashSet<String>();
	public List<String> taintMethods = null;
	//public Tree<HeapTreeNode> HeapTree;

	public List<Pair> Query;
	public List<Pair> Header;
	public List<Pair> Body;
	
	private ValueEntryTable _query;
	private ValueEntryTable _Header;
	private ValueEntryTable _Body;
	
	/*
	 * private static final List<String> app_header_field =
	 * Arrays.asList("Accept-Encoding"); private static final List<String>
	 * third_party_header_field = Arrays.asList(
	 * /////////////////////ch.boye.httpclientandroidlib///////////////////////
	 * "Content-Type", Wish-Static "Content-length", Dynamic "Cookie", Dynamic
	 * "host", Dynamic "User-Agent", Dynamic "Connection" Dynamic
	 * ////////////////////////////////////////////////////////////////////////
	 * );
	 */

	public RequestInfoEntry() {
		//this.HeapTree = new LinkedTree<HeapTreeNode>();
		this.Header = new ArrayList<Pair>();
		this.Body = new ArrayList<Pair>();
		this.Query = new ArrayList<Pair>();
		
		this._query  = new ValueEntryTable();
		this._Header = new ValueEntryTable();
		this._Body  = new ValueEntryTable();
	}

	public RequestInfoEntry(String _Url, String EP, String DP) {
		this();
		
		this.setSignature(_Url);
		this.setEPorSPMethod(EP);
		this.setDPMethod(DP);
	}
	
	public void clear() {
		if(this.methods != null)
			this.methods.clear();
		
		/*if(this.HeapTree != null)
			for(HeapTreeNode htn: this.HeapTree)
				htn.clear();*/
		
		if(this.Header != null)
			this.Header.clear();
		
		if(this.Body != null)
			this.Body.clear();
		
		if(this.Query != null)
			this.Query.clear();
		
		if(this._Header != null)
			this._Header.Clear();
		
		if(this._Body != null)
			this._Body.Clear();
		
		if(this._query != null)
			this._query.Clear();
	}
	
	private static void TableToList(List<Pair> dst, ValueEntryTable src) {
		for(Entry<String, ValueEntryList> e: src.getValueEntryTable().entrySet()) {
			String key = e.getKey();
			ValueEntryList vel = e.getValue();
			
			if(key.endsWith("[]")) {
				Constant ve = (Constant) vel.getValueEntryOf(SOURCE_TYPE.CONSTANT);
				if(ve.getConstantList() == null) {
					dst.add(new Pair(key, ".*"));
					continue;
				}
				
				for(String value : ve.getConstantList())
					dst.add(new Pair(key, value));
			}else
				dst.add(new Pair(key, vel.GenRegex()));
		}
	}
	
	/***********************************************/
	/***                  Query                  ***/
	/***********************************************/
	public void addQuery(String key, String value) {
		this._query.addConstantValue(key, value, false);
		/*if(this.Query == null)
			this.Query = new ArrayList<Pair>();
		
		Pair.addKeyValue(key, value, this.Query);*/
	}
	
	public void initQuery() {
		TableToList(this.Query, this._query);
		/*for(Entry<String, ValueEntryList> e: this._query.getValueEntryTable().entrySet()) {
			String key = e.getKey();
			ValueEntryList value = e.getValue();
			
			this.Query.add(new Pair(key, value.GenRegex()));
		}*/
	}
	
	public ValueEntryTable getQueryTable() { return this._query; }
	

	/***********************************************/
	/***                  Header                 ***/
	/***********************************************/
	// set a value of a pair inside 'Header' list
	public void setHeader(String key, String value) {
		this._Header.makeEmpty();
		this._Header.addConstantValue(key, value, false);
	}

	public void addHeader(String key, String value) {
		this._Header.addConstantValue(key, value, false);
	}
	
	public void addHeader(List<Pair> l) {
		if(l == null)
			return;
		
		for(Pair p : l)
			this._Header.addConstantValue(p.getKey(), p.getValue(), false);
		/*if(this.Header == null)
			this.Header = new ArrayList<Pair>();
		
		Pair.addKeyValue(key, value, this.Header);*/
	}
	
	public void setHeader(List<Pair> l) {
		this._Header.makeEmpty();
		this.addHeader(l);
	}
	
	public void initHeader() {
		TableToList(this.Header, this._Header);
		/*for(Entry<String, ValueEntryList> e: this._Header.getValueEntryTable().entrySet()) {
			String key = e.getKey();
			ValueEntryList value = e.getValue();
			
			this.Header.add(new Pair(key, value.GenRegex()));
		}*/
	}
	
	public ValueEntryTable getHeaderTable() { return this._Header; }
	
	
	
	
	/***********************************************/
	/***                   Body                  ***/
	/***********************************************/
	public void setBody(List<Pair> _map){
		this._Body.makeEmpty();
		if(_map == null)
			return;
		
		for(Pair p : _map)
			this._Body.addConstantValue(p.getKey(), p.getValue(), false);
	}
	public void addBody(String key, String value) {
		this._Body.addConstantValue(key, value, false);
	}
	
	public void initBody() {
		TableToList(this.Body, this._Body);
	}
	
	public ValueEntryTable getBodyTable() { return this._Body; }
	
	public static void addBody(List<Pair> body, String key, String value) {
		if(body == null)
			return;
		
		if (body.size() > 0) {
			boolean existence = false;
			
			for(Pair entity: body) {
				if (entity.getKey().equals(key) && entity.getValue().equals(value)) {
					existence = true;
					break;
				}
			}
			
			if(!existence)
				body.add(new Pair(key, value));
		}
		else
			body.add(new Pair(key, value));
	}

	
	/***********************************************/
	/***         APIs for comparison             ***/
	/***********************************************/
	public boolean compareHeader(RequestInfoEntry other){ return compareOrigPair(this.Header, other.Header); }
	public boolean compareBody(RequestInfoEntry other){ return compareOrigPair(this.Body, other.Body); }
	
	public static boolean compareOrigPair(List<Pair> pl1, List<Pair> pl2){
		if(pl1 == null && pl2 != null) return false;
		if(pl1 != null && pl2 == null) return false;
		if(pl1 == null && pl2 == null) return true;
		
		if(pl1.size() != pl2.size())
			return false;
		
		for(Pair p1 : pl1){
			
			boolean exist = false;
			for(Pair p2 : pl2){
				if(!p2.getKey().equals(p1.getKey()))
					continue;
				
				if(!p2.getValue().equals(p1.getValue()))
					continue;
				
				exist = true;
				break;
			}
			if(!exist)
				return false;
		}
		
		return true;
	}
	
	public boolean compareTo(RequestInfoEntry other){
		if (other == null)
			return false;
		
		if(!super.CompareOrigSigWith(other))
			return false;
		
		/*if (Header == null) {
			if (other.Header != null)
				return false;
		} else if (!this.compareHeader(other))
			return false;
		
		if (Body == null) {
			if (other.Body != null)
				return false;
		} else if (!this.compareBody(other))
			return false;*/
		
		return true;
	}

	/***********************************************/
	/***           APIs for HeapTree             ***/
	/***********************************************/
	/*public void addHeapTreeRoot() {
		HeapTreeNode newHeapNode = new HeapTreeNode();
		if (HeapTree.root() == null) {
			SplitEachHeaps(newHeapNode);
			HeapTree.add(newHeapNode);
		}
	}

	public Tree<HeapTreeNode> getHeapTree() {
		return HeapTree;
	}

	private void SplitEachHeaps(HeapTreeNode newHeapNode) {
		if (getSignature() != null)
			if (UrlBuilder.hasHeapObject(getSignature())) {
				Pattern p = Pattern.compile("<[a-z.,A-Z0-9,_ :]*>");
				Matcher m = p.matcher(getSignature());
				while (m.find()) {
					newHeapNode.listURL.add(m.group());
				}
			}
	}*/

	
	/***********************************************/
	/***                  Others                 ***/
	/***********************************************/
	public boolean equals(String Url) {
		if (getSignature().equals(Url))
			return true;
		else
			return false;
	}
	
	public String getMethod(){
		return this.methods.toString().substring(1, this.methods.toString().length() - 1).replaceAll(" ", "");
	}
	
	

	public void print_info(int id, boolean isProxySig, boolean printTaintMethods) {
		System.out.println("\n\n--------------------------------- [" + id + "] Reqeust information---------------------------------");
		System.out.println(" - EP: " + super.EPorSPMethod);
		System.out.println(" - DP Stmt: " + super.DPStmt);
		System.out.println(" - DP Method: " + super.DPMethod);
		if(printTaintMethods) {
			System.out.println(" - Taint Methods: ");
			for(String m: this.taintMethods)
				System.out.println("\t" + m);
		}
		System.out.println();

		// 1. Print URI
		System.out.println("1. URI (Orig Sig.) : " + getMethod()+ " " + super.OrigSignature);
		if(isProxySig){
			System.out.println("   URI (Regex Sig. w/o qeury): " + getMethod()+ " " + super.wholeRegexSignature);
			for(String sig: super.regexSignature)
				System.out.println("   URI (Regex Sub-Sig.): " + getMethod()+ " " + sig);
			for(String sig: super.proxySignature)
				System.out.println("   URI (Proxy Sig.): " + getMethod()+ " " + sig);
		}
		System.out.println();

		// 2. Print Query
		System.out.println("2. Query Signature:");
		if (this.Query != null) {
			for (Pair p : this.Query)
				System.out.println("\t Original - (key, value): " + p.getKey() + ", " + p.getValue());
			if (isProxySig) {
				System.out.println();
				for (Pair p : this.Query)
					System.out.println("\t RegexSig - (key, value): " + p.getRegexSigKey() + ", " + p.getRegexSigValue());
				System.out.println();
				for (Pair p : this.Query)
					System.out.println("\t ProxySig - (key, value): " + p.getProxySigKey() + ", " + p.getProxySigValue());
			}
		}
		System.out.println("\n");
		
		// 3. Print Header
		System.out.println("3. Header Signature:");
		if(this.Header != null) {
			for(Pair p: this.Header)
				System.out.println("\t Original - (key, value): " + p.getKey() + ", " + p.getValue());
			if(isProxySig){
				System.out.println();
				for(Pair p: this.Header)
					System.out.println("\t RegexSig - (key, value): " + p.getRegexSigKey() + ", " + p.getRegexSigValue());
				System.out.println();	
				for(Pair p: this.Header)
					System.out.println("\t ProxySig - (key, value): " + p.getProxySigKey() + ", " + p.getProxySigValue());
			}	
		}
		System.out.println("\n");
		
		
		// 4. Print Body
		System.out.println("4. Body Signature:");
		if(this.Body != null) {
			for(Pair p: this.Body)
				System.out.println("\t Original - (key, value): " + p.getKey() + ", " + p.getValue());
			if(isProxySig){
				System.out.println();
				for(Pair p: this.Body)
					System.out.println("\t RegexSig - (key, value): " + p.getRegexSigKey() + ", " + p.getRegexSigValue());
				System.out.println();
				for(Pair p: this.Body)
					System.out.println("\t ProxySig - (key, value): " + p.getProxySigKey() + ", " + p.getProxySigValue());
			}	
		}
	}
}
