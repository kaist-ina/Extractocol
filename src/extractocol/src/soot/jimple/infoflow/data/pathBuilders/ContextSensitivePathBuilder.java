package soot.jimple.infoflow.data.pathBuilders;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import heros.solver.CountingThreadPoolExecutor;
import heros.solver.Pair;
import soot.jimple.Stmt;
import soot.jimple.infoflow.collect.ConcurrentIdentityHashMultiMap;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.AbstractionAtSink;
import soot.jimple.infoflow.data.SourceContext;
import soot.jimple.infoflow.data.SourceContextAndPath;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

/**
 * Class for reconstructing abstraction paths from sinks to source. This builder
 * is context-sensitive which makes it more precise than the
 * {@link ContextInsensitivePathBuilder}, but also a bit slower.
 * 
 * @author Steven Arzt
 */
public class ContextSensitivePathBuilder extends AbstractAbstractionPathBuilder {
	
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final InfoflowResults results = new InfoflowResults();
	private final CountingThreadPoolExecutor executor;

	protected ConcurrentIdentityHashMultiMap<Abstraction, SourceContextAndPath> pathCache =
			new ConcurrentIdentityHashMultiMap<>();
		
	/**
	 * Creates a new instance of the {@link ContextSensitivePathBuilder} class
	 * @param icfg The interprocedural control flow graph
	 * @param executor The executor in which to run the path reconstruction tasks
	 * @param reconstructPaths True if the exact propagation path between source
	 * and sink shall be reconstructed.
	 */
	public ContextSensitivePathBuilder(IInfoflowCFG icfg, CountingThreadPoolExecutor executor,
			boolean reconstructPaths) {
		super(icfg, reconstructPaths);
		this.executor = executor;
	}
	
	/**
	 * Task for tracking back the path from sink to source.
	 * 
	 * @author Steven Arzt
	 */
	private class SourceFindingTask implements Runnable {
		private final Abstraction abstraction;
		
		public SourceFindingTask(Abstraction abstraction) {
			this.abstraction = abstraction;
		}
		
		@Override
		public void run() {
			final Set<SourceContextAndPath> paths = pathCache.get(abstraction);
			final Abstraction pred = abstraction.getPredecessor();
			
			if (pred != null && paths != null) {
				for (SourceContextAndPath scap : paths) {
					// Process the predecessor
					if (processPredecessor(scap, pred))
						// Schedule the predecessor
						spawnSourceFindingTask(pred);
					
					// Process the predecessor's neighbors
					if (pred.getNeighbors() != null)
						for (Abstraction neighbor : pred.getNeighbors())
							if (processPredecessor(scap, neighbor))
								// Schedule the predecessor
								spawnSourceFindingTask(neighbor);
				}
			}
		}

		private boolean processPredecessor(SourceContextAndPath scap, Abstraction pred) {
			// Shortcut: If this a call-to-return node, we should not enter and
			// immediately leave again for performance reasons.
			if (pred.getCurrentStmt() != null
					&& pred.getCurrentStmt() == pred.getCorrespondingCallSite()) {
				SourceContextAndPath extendedScap = scap.extendPath(pred, reconstructPaths);
				if (extendedScap == null)
					return false;
				
				checkForSource(pred, extendedScap);
				return pathCache.put(pred, extendedScap);
			}
			
			// If we enter a method, we put it on the stack
			SourceContextAndPath extendedScap = scap.extendPath(pred, reconstructPaths);
			if (extendedScap == null)
				return false;
			
			// Do we process a method return?
			if (pred.getCurrentStmt() != null 
					&& pred.getCurrentStmt().containsInvokeExpr()) {
				// Pop the top item off the call stack. This gives us the item
				// and the new SCAP without the item we popped off.
				Pair<SourceContextAndPath, Stmt> pathAndItem =
						extendedScap.popTopCallStackItem();
				if (pathAndItem != null) {
					Stmt topCallStackItem = pathAndItem.getO2();
					// Make sure that we don't follow an unrealizable path
					if (topCallStackItem != pred.getCurrentStmt())
						return false;
					
					// We have returned from a function
					extendedScap = pathAndItem.getO1();
				}
			}
			
			// Add the new path
			checkForSource(pred, extendedScap);
			return pathCache.put(pred, extendedScap);
		}
		
	}
	
	/**
	 * Checks whether the given abstraction is a source. If so, a result entry
	 * is created.
	 * @param abs The abstraction to check
	 * @param scap The path leading up to the current abstraction
	 * @return True if the current abstraction is a source, otherwise false
	 */
	protected boolean checkForSource(Abstraction abs, SourceContextAndPath scap) {
		if (abs.getPredecessor() != null)
			return false;
		
		// If we have no predecessors, this must be a source
		assert abs.getSourceContext() != null;
		assert abs.getNeighbors() == null;
		
		// Register the source that we have found
		SourceContext sourceContext = abs.getSourceContext();
		Pair<ResultSourceInfo, ResultSinkInfo> newResult = results.addResult(
				scap.getAccessPath(),
				scap.getStmt(),
				sourceContext.getAccessPath(),
				sourceContext.getStmt(),
				sourceContext.getUserData(),
				scap.getAbstractionPath());
		
		// Notify our handlers
		if (resultAvailableHandlers != null)
			for (OnPathBuilderResultAvailable handler : resultAvailableHandlers)
				handler.onResultAvailable(newResult.getO1(), newResult.getO2());
		
		return true;
	}
	
	@Override
	public void computeTaintPaths(final Set<AbstractionAtSink> res) {
		logger.info("Context-sensitive path reconstructor started");
		runSourceFindingTasks(res);
	}
	
	private void runSourceFindingTasks(final Set<AbstractionAtSink> res) {
		if (res.isEmpty())
			return;
		
    	logger.info("Obtainted {} connections between {} sources and sinks", res.size());
    	
    	// Start the propagation tasks
    	for (final AbstractionAtSink abs : res) {
   			buildPathForAbstraction(abs);
   			
   			// Also build paths for the neighbors of our result abstraction
   			if (abs.getAbstraction().getNeighbors() != null)
   				for (Abstraction neighbor : abs.getAbstraction().getNeighbors()) {
   					AbstractionAtSink neighborAtSink = new AbstractionAtSink(neighbor,
   							abs.getSinkStmt());
   		   			buildPathForAbstraction(neighborAtSink);
   				}
    	}
	}
	
	@Override
	public void runIncrementalPathCompuation() {
		for (Abstraction abs : pathCache.keySet())
			for (SourceContextAndPath scap : pathCache.get(abs)) {
				if (abs.getNeighbors() != null && abs.getNeighbors().size() != scap.getNeighborCounter()) {
					// This is a path for which we have to process the new neighbors
					scap.setNeighborCounter(abs.getNeighbors().size());
					
					for (Abstraction neighbor : abs.getNeighbors())
						buildPathForAbstraction(new AbstractionAtSink(neighbor, scap.getStmt()));
				}
			}
	}
	
	/**
	 * Builds the path for the given abstraction that reached a sink
	 * @param abs The abstraction that reached a sink
	 */
	protected void buildPathForAbstraction(final AbstractionAtSink abs) {
		SourceContextAndPath scap = new SourceContextAndPath(
				abs.getAbstraction().getAccessPath(), abs.getSinkStmt());
		scap = scap.extendPath(abs.getAbstraction());
		
		if (pathCache.put(abs.getAbstraction(), scap))
			if (!checkForSource(abs.getAbstraction(), scap))
				spawnSourceFindingTask(abs.getAbstraction());
	}
	
	/**
	 * Schedules a new propagation task for execution
	 * @param abs The abstraction for which to schedule a propagation task
	 */
	protected void spawnSourceFindingTask(Abstraction abs) {
		executor.execute(new SourceFindingTask(abs));
	}
	
	@Override
	public InfoflowResults getResults() {
		return this.results;
	}

}
