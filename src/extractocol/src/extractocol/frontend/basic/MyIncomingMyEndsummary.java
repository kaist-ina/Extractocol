package extractocol.frontend.basic;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import extractocol.frontend.basic.pairs.Hexa;
import extractocol.frontend.basic.pairs.Triad;
import extractocol.frontend.output.basic.TaintResultEntry;
import heros.solver.Pair;
import soot.jimple.infoflow.collect.ConcurrentHashSet;
import soot.jimple.infoflow.collect.MyConcurrentHashMap;

/**
 * 
 * @author Byungkwon Choi
 *
 * @param <M> SootMethod
 * @param <N> Unit
 * @param <D> Abstraction
 * @param <R> TaintResultEntry
 */
public class MyIncomingMyEndsummary<M, N, D> {
	protected final D zeroValue;
	
	protected final MyConcurrentHashMap<Triad<M,D,TaintResultEntry>,Set<Pair<N,D>>> endSummary =
			new MyConcurrentHashMap<Triad<M,D,TaintResultEntry>, Set<Pair<N,D>>>();
	
	/*protected final MyConcurrentHashMap<Penta<M,D,N,TaintResultEntry>, TaintResultEntry> endSummaryTaintMethodSet =
			new MyConcurrentHashMap<Penta<M,D,N,TaintResultEntry>, TaintResultEntry>();*/
	
	protected final MyConcurrentHashMap<Pair<M,D>,MyConcurrentHashMap<TaintResultEntry,MyConcurrentHashMap<N,Map<D, D>>>> incoming =
			new MyConcurrentHashMap<Pair<M,D>,MyConcurrentHashMap<TaintResultEntry,MyConcurrentHashMap<N,Map<D, D>>>>();
	
	/*protected final MyConcurrentHashMap<Hexa<M,D,N,TaintResultEntry>, TaintResultEntry> incomingTaintMethodSet =
			new MyConcurrentHashMap<Hexa<M,D,N,TaintResultEntry>, TaintResultEntry>();*/

	
	
	public MyIncomingMyEndsummary(D z){
		this.zeroValue = z;
	}
	
	public Set<Pair<N, D>> endSummary(M m, D d3, TaintResultEntry tre) {
		Set<Pair<N, D>> map = endSummary.get(new Triad<M, D, TaintResultEntry>(m, d3, tre));
		return map;
	}

	public boolean addEndSummary(M m, D d1, N eP, D d2, TaintResultEntry tre) {
		if (d1 == zeroValue)
			return true;
		
		Set<Pair<N, D>> summaries = endSummary.putIfAbsentElseGet
				(new Triad<M, D, TaintResultEntry>(m, d1, tre), new ConcurrentHashSet<Pair<N, D>>());
		return summaries.add(new Pair<N, D>(eP, d2));
		//boolean existingRes = summaries.add(new Pair<N, D>(eP, d2));
		
		/*TaintResultEntry res = endSummaryTaintMethodSet.putIfAbsentElseGet(new Penta<M,D,N,TaintResultEntry>(m,d1,eP,d2,tre), tre);
		//return (res == tre);
		return tre.contains(res);*/
	}
	
	public Map<N, Map<D, D>> incoming(D d1, M m, TaintResultEntry tre) {
		MyConcurrentHashMap<TaintResultEntry, MyConcurrentHashMap<N, Map<D, D>>> map = incoming.get(new Pair<M,D>(m,d1));
		
		if(map != null){
			for(Entry<TaintResultEntry, MyConcurrentHashMap<N, Map<D, D>>> e : map.entrySet()){
				TaintResultEntry existingTRE = e.getKey();
				if(tre.contains(existingTRE))
					return e.getValue();
			}
		}
		
		return null;
		/*Map<N, Map<D, D>> map = incoming.get(new Triad<M, D, TaintResultEntry>(m, d1, tre));
		return map;*/
	}
	
	public boolean addIncoming(M m, D d3, N n, D d1, D d2, TaintResultEntry tre) {
		MyConcurrentHashMap<TaintResultEntry, MyConcurrentHashMap<N, Map<D, D>>> summaries = incoming.putIfAbsentElseGet
				(new Pair<M, D>(m, d3), new MyConcurrentHashMap<TaintResultEntry, MyConcurrentHashMap<N, Map<D, D>>>());
		MyConcurrentHashMap<N, Map<D, D>> set = summaries.putIfAbsentElseGet(tre, new MyConcurrentHashMap<N, Map<D, D>>());
		Map<D, D> setDeep = set.putIfAbsentElseGet(n, new ConcurrentHashMap<D,D>());
		return setDeep.put(d1, d2) == null;
		/*boolean existingRes = (set.put(d1, d2) == null);
		
		TaintResultEntry res = incomingTaintMethodSet.putIfAbsentElseGet(new Hexa<M,D,N,TaintResultEntry>(m,d3,n,d1,d2,tre), tre);
		//return (res == tre);
		return tre.contains(res);*/
	}
}
