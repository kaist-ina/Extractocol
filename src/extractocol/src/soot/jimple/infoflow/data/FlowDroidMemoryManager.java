package soot.jimple.infoflow.data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.infoflow.solver.IMemoryManager;

/**
 * Memory manager implementation for FlowDroid
 * 
 * @author Steven Arzt
 *
 */
public class FlowDroidMemoryManager implements IMemoryManager<Abstraction> {
	
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
	 * Special class for encapsulating taint abstractions for a full equality
	 * check including those fields (predecessor, etc.) that are normally left
	 * out
	 * 
	 * @author Steven Arzt
	 *
	 */
	private class AbstractionCacheKey {
		
		private final Abstraction abs;
		
		public AbstractionCacheKey(Abstraction abs) {
			this.abs = abs;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * abs.hashCode();
			result = prime * result + ((abs.getPredecessor() == null) ? 0 : abs.getPredecessor().hashCode());
			result = prime * result + ((abs.getCurrentStmt() == null) ? 0 : abs.getCurrentStmt().hashCode());
			result = prime * result + ((abs.getCorrespondingCallSite() == null) ? 0 : abs.getCorrespondingCallSite().hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AbstractionCacheKey other = (AbstractionCacheKey) obj;
			
			if (!abs.equals(other.abs))
				return false;
			if (abs.getPredecessor() != other.abs.getPredecessor())
				return false;
			if (abs.getCurrentStmt() != other.abs.getCurrentStmt())
				return false;
			if (abs.getCorrespondingCallSite() != other.abs.getCorrespondingCallSite())
				return false;
			
			return true;
		}		
		
	}
	
	private ConcurrentMap<AccessPath, AccessPath> apCache = new ConcurrentHashMap<>();
	private ConcurrentHashMap<AbstractionCacheKey, Abstraction> absCache = new ConcurrentHashMap<>();
	private AtomicInteger reuseCounter = new AtomicInteger();
	
	private final boolean tracingEnabled;
	private final PathDataErasureMode erasePathData;
	private boolean useAbstractionCache = false;
	
	/**
	 * Supported modes that define which path tracking data shall be erased and
	 * which shall be kept
	 */
	public enum PathDataErasureMode {
		/**
		 * Keep all path tracking data.
		 */
		EraseNothing,
		/**
		 * Keep only those path tracking items that are necessary for context-
		 * sensitive path reconstruction.
		 */
		KeepOnlyContextData,
		/**
		 * Erase all path tracking data.
		 */
		EraseAll
	}
	
	/**
	 * Constructs a new instance of the AccessPathManager class
	 */
	public FlowDroidMemoryManager() {
		this(false, PathDataErasureMode.EraseNothing);
	}
	
	/**
	 * Constructs a new instance of the AccessPathManager class
	 * @param tracingEnabled True if performance tracing data shall be recorded
	 * @param erasePathData Specifies whether data for tracking paths (current
	 * statement, corresponding call site) shall be erased.
	 */	
	public FlowDroidMemoryManager(boolean tracingEnabled,
			PathDataErasureMode erasePathData) {
		this.tracingEnabled = tracingEnabled;
		this.erasePathData = erasePathData;
		
		logger.info("Initializing FlowDroid memory manager...");
		if (this.tracingEnabled)
			logger.info("FDMM: Tracing enabled. This may negatively affect performance.");
		if (this.erasePathData != PathDataErasureMode.EraseNothing)
			logger.info("FDMM: Path data erasure enabled");
	}
	
	/**
	 * Gets the cached equivalent of the given access path
	 * @param ap The access path for which to get the cached equivalent
	 * @return The cached equivalent of the given access path
	 */
	private AccessPath getCachedAccessPath(AccessPath ap) {
		AccessPath oldAP = apCache.putIfAbsent(ap, ap);
		if (oldAP == null)
			return ap;
		
		// We can re-use an old access path
		if (tracingEnabled && oldAP != ap)
			reuseCounter.incrementAndGet();
		return oldAP;
	}
	
	/**
	 * Gets a cached equivalent abstraction for the given abstraction if we have
	 * one, otherwise returns null
	 * @param abs The abstraction for which to perform a cache lookup
	 * @return The cached abstraction equivalent to the given one of it exists,
	 * otherwise null
	 */
	private Abstraction getCachedAbstraction(Abstraction abs) {
		Abstraction oldAbs = absCache.putIfAbsent(new AbstractionCacheKey(abs), abs);
		if (oldAbs != null && oldAbs != abs)
			if (tracingEnabled)
				reuseCounter.incrementAndGet();
		return oldAbs;
	}
	
	/**
	 * Gets the number of access paths that have been re-used through caching
	 * @return The number of access paths that have been re-used through caching
	 */
	public int getReuseCount() {
		return this.reuseCounter.get();
	}

	@Override
	public Abstraction handleMemoryObject(Abstraction obj) {
		if (useAbstractionCache) {
			// We check for a cached version of the complete abstraction
			Abstraction cachedAbs = getCachedAbstraction(obj);
			if (cachedAbs != null)
				return cachedAbs;
		}
		
		// We check for a cached version of the access path
		AccessPath newAP = getCachedAccessPath(obj.getAccessPath());
		obj.setAccessPath(newAP);
		
		// Erase path data if requested
		if (erasePathData != PathDataErasureMode.EraseNothing) {
			Abstraction curAbs = obj;
			while (curAbs != null) {
				boolean doErase = erasePathData == PathDataErasureMode.EraseAll;
				if (erasePathData == PathDataErasureMode.KeepOnlyContextData
						&& curAbs.getCorrespondingCallSite() == curAbs.getCurrentStmt())
					doErase = true;
				if (erasePathData == PathDataErasureMode.KeepOnlyContextData
						&& curAbs.getCorrespondingCallSite() == null
						&& curAbs.getCurrentStmt() != null) {
					// Lock the abstraction and check again. This is to make sure that no
					// other thread has already erased the path data in the meantime and
					// we access null objects.
					synchronized (curAbs) {
						if (curAbs.getCorrespondingCallSite() == null
								&& curAbs.getCurrentStmt() != null
								&& !curAbs.getCurrentStmt().containsInvokeExpr()
								&& !(curAbs.getCurrentStmt() instanceof ReturnStmt)
								&& !(curAbs.getCurrentStmt() instanceof ReturnVoidStmt)) {
							doErase = true;
						}
					}
				}
				synchronized (curAbs) {
					if (doErase) {
						curAbs.setCurrentStmt(null);
						curAbs.setCorrespondingCallSite(null);
					}
				}
				curAbs = curAbs.getPredecessor();
			}
		}
		
		// If an intermediate statement does not change any taint state, skip it.
		// Note that we should not do this when we're reconstructing paths or we might
		// lose statements along the way.
		if (erasePathData != PathDataErasureMode.EraseNothing) {
			Abstraction pred = obj.getPredecessor();
			Abstraction curAbs = pred;
			while (curAbs != null && curAbs.getNeighbors() == null) {
				Abstraction predPred = curAbs.getPredecessor();
				if (predPred != null) {
					if (predPred.equals(obj)) {
						pred = predPred.getPredecessor();
						obj = predPred;
					}
				}
				curAbs = predPred;
			}
		}
		
		return obj;
	}

	@Override
	public Abstraction handleGeneratedMemoryObject(Abstraction input,
			Abstraction output) {
		// We we just pass the same object on, there is nothing to optimize
		if (input == output) {
			return output;
		}
		
		// If the flow function gave us a chain of abstractions, we can
		// compact it
		Abstraction pred = output.getPredecessor();
		if (pred != null && pred != input)
			output.setPredecessor(input);
		
		// If the abstraction didn't change at all, we can use the old one
		if (input.equals(output)) {
			if (output.getCurrentStmt() == null
					|| input.getCurrentStmt() == output.getCurrentStmt())
				return input;
			if (input.getCurrentStmt() == null) {
				synchronized (input) {
					if (input.getCurrentStmt() == null) {
						input.setCurrentStmt(output.getCurrentStmt());
						input.setCorrespondingCallSite(output.getCorrespondingCallSite());
						return input;
					}
				}
			}
		}
		
		return output;
	}
	
	/**
	 * Sets whether the memory manager shall use the abstraction cache
	 * @param useAbstractionCache True if the abstraction cache shall be used,
	 * otherwise false
	 */
	public void setUseAbstractionCache(boolean useAbstractionCache) {
		this.useAbstractionCache = useAbstractionCache;
	}

}
