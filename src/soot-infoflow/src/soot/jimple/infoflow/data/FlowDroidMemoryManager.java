package soot.jimple.infoflow.data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import soot.jimple.infoflow.solver.IMemoryManager;

/**
 * Memory manager implementation for FlowDroid
 * 
 * @author Steven Arzt
 *
 */
public class FlowDroidMemoryManager implements IMemoryManager<Abstraction> {
	
	private ConcurrentMap<AccessPath, AccessPath> apCache = new ConcurrentHashMap<>();
	private AtomicInteger reuseCounter = new AtomicInteger();
	
	private final boolean tracingEnabled;
	
	/**
	 * Constructs a new instance of the AccessPathManager class
	 */
	public FlowDroidMemoryManager() {
		this(false);
	}
	
	/**
	 * Constructs a new instance of the AccessPathManager class
	 * @param tracingEnabled True if performance tracing data shall be recorded
	 */	
	public FlowDroidMemoryManager(boolean tracingEnabled) {
		this.tracingEnabled = tracingEnabled;
	}
	
	/**
	 * Gets the cached equivalent of the given access path
	 * @param ap The access path for which to get the cached equivalent
	 * @return The cached equivalent of the given access path
	 */
	private AccessPath getCachedAccessPath(AccessPath ap) {
		AccessPath oldAP = apCache.putIfAbsent(ap, ap);
		if (oldAP == null) {
			if (tracingEnabled)
				reuseCounter.incrementAndGet();
			return ap;
		}
		return oldAP;
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
		// We check for a cached version of the access path
		AccessPath newAP = getCachedAccessPath(obj.getAccessPath());
		obj.setAccessPath(newAP);
		return obj;
	}

	@Override
	public Abstraction handleGeneratedMemoryObject(Abstraction input,
			Abstraction output) {
		// We we just pass the same object on, there is nothing to optimize
		if (input == output)
			return output;
		
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

}
