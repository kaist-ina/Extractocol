package soot.jimple.infoflow.problems;

import java.util.HashSet;
import java.util.Set;

import soot.jimple.infoflow.InfoflowManager;
import soot.jimple.infoflow.collect.MyConcurrentHashMap;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.AbstractionAtSink;
import soot.jimple.infoflow.solver.IMemoryManager;
import soot.jimple.infoflow.util.SystemClassHandler;

/**
 * Class for storing the results of the forward taint propagation
 * 
 * @author Steven Arzt
 *
 */
public class TaintPropagationResults {
	
	/**
	 * Handler interface that is invoked when new taint propagation results are
	 * added to the result object
	 */
	public interface OnTaintPropagationResultAdded {
		
		/**
		 * Called when a new abstraction has reached a sink statement
		 * @param abs The abstraction at the sink
		 * @return True if the data flow analysis shall continue, otherwise false
		 */
		public boolean onResultAvailable(AbstractionAtSink abs);
		
	}
	
	protected final InfoflowManager manager;
	protected final MyConcurrentHashMap<AbstractionAtSink, Abstraction> results =
			new MyConcurrentHashMap<AbstractionAtSink, Abstraction>();
	
	protected final Set<OnTaintPropagationResultAdded> resultAddedHandlers = new HashSet<>();

	/**
	 * Creates a new instance of the TaintPropagationResults class
	 * @param manager A reference to the manager class used during taint
	 * propagation
	 */
	public TaintPropagationResults(InfoflowManager manager) {
		this.manager = manager;
	}
	
	/**
	 * Adds a new result of the data flow analysis to the collection
	 * @param resultAbs The abstraction at the sink instruction
	 * @return True if the data flow analysis shall continue, otherwise false
	 */
	public boolean addResult(AbstractionAtSink resultAbs) {
		// Check whether we need to filter a result in a system package
		if (manager.getConfig().getIgnoreFlowsInSystemPackages() && SystemClassHandler.isClassInSystemPackage
				(manager.getICFG().getMethodOf(resultAbs.getSinkStmt()).getDeclaringClass().getName()))
			return true;
		
		// Construct the abstraction at the sink
		Abstraction abs = resultAbs.getAbstraction();
		abs = abs.deriveNewAbstraction(abs.getAccessPath(), resultAbs.getSinkStmt());
		abs.setCorrespondingCallSite(resultAbs.getSinkStmt());
		
		// Reduce the incoming abstraction
		IMemoryManager<Abstraction> memoryManager = manager.getForwardSolver().getMemoryManager();
		if (memoryManager != null) {
			abs = memoryManager.handleMemoryObject(abs);
			if (abs == null)
				return true;
		}
		
		// Record the result
		resultAbs = new AbstractionAtSink(abs, resultAbs.getSinkStmt());
		Abstraction newAbs = this.results.putIfAbsentElseGet
				(resultAbs, resultAbs.getAbstraction());
		if (newAbs != resultAbs.getAbstraction())
			newAbs.addNeighbor(resultAbs.getAbstraction());
		
		// Notify the handlers
		boolean continueAnalysis = true;
		for (OnTaintPropagationResultAdded handler : resultAddedHandlers)
			if (!handler.onResultAvailable(resultAbs))
				continueAnalysis = false;
		return continueAnalysis;
	}
	
	/**
	 * Checks whether this result object is empty
	 * @return True if this result object is empty, i.e., there are no results
	 * yet, otherwise false
	 * @return
	 */
	public boolean isEmpty() {
		return this.results.isEmpty();
	}
	
	/**
	 * Gets all results collected in this data object
	 * @return All data flow results collected in this object
	 */
	public Set<AbstractionAtSink> getResults() {
		return this.results.keySet();
	}
	
	/**
	 * Adds a new handler that is invoked when a new data flow result is added
	 * to this data object
	 * @param handler The handler implementation to add
	 */
	public void addResultAvailableHandler(OnTaintPropagationResultAdded handler) {
		this.resultAddedHandlers.add(handler);
	}

}
