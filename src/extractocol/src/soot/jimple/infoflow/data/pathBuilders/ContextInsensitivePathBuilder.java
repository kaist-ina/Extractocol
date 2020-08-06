package soot.jimple.infoflow.data.pathBuilders;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import heros.solver.CountingThreadPoolExecutor;
import soot.jimple.infoflow.collect.ConcurrentIdentityHashMultiMap;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.AbstractionAtSink;
import soot.jimple.infoflow.data.SourceContext;
import soot.jimple.infoflow.data.SourceContextAndPath;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

/**
 * Class for reconstructing abstraction paths from sinks to source. This builder
 * is context-insensitive which makes it faster, but also less precise than
 * {@link ContextSensitivePathBuilder}.
 * 
 * @author Steven Arzt
 */
public class ContextInsensitivePathBuilder extends AbstractAbstractionPathBuilder {
	
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final InfoflowResults results = new InfoflowResults();
	private final CountingThreadPoolExecutor executor;
	
	private ConcurrentIdentityHashMultiMap<Abstraction, SourceContextAndPath> pathCache =
			new ConcurrentIdentityHashMultiMap<>();
	
	/**
	 * Creates a new instance of the {@link ContextSensitivePathBuilder} class
	 * @param icfg The interprocedural control flow graph
	 * @param executor The executor in which to run the path reconstruction tasks
	 * @param reconstructPaths True if the exact propagation path between source
	 * and sink shall be reconstructed.
	 */
	public ContextInsensitivePathBuilder(IInfoflowCFG icfg, CountingThreadPoolExecutor executor,
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
			
			if (pred != null) {
				for (SourceContextAndPath scap : paths) {						
					// Process the predecessor
					if (processPredecessor(scap, pred))
						// Schedule the predecessor
						executor.execute(new SourceFindingTask(pred));
					
					// Process the predecessor's neighbors
					if (pred.getNeighbors() != null)
						for (Abstraction neighbor : pred.getNeighbors())
							if (processPredecessor(scap, neighbor))
								// Schedule the predecessor
								executor.execute(new SourceFindingTask(neighbor));
				}
			}
		}

		private boolean processPredecessor(SourceContextAndPath scap, Abstraction pred) {
			// Put the current statement on the list
			SourceContextAndPath extendedScap = scap.extendPath(pred, reconstructPaths);
			if (extendedScap == null)
				return false;
			
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
	private boolean checkForSource(Abstraction abs, SourceContextAndPath scap) {
		if (abs.getPredecessor() != null)
			return false;
		
		// If we have no predecessors, this must be a source
		assert abs.getSourceContext() != null;
		assert abs.getNeighbors() == null;
		
		// Register the source that we have found
		SourceContext sourceContext = abs.getSourceContext();
		results.addResult(scap.getAccessPath(),
				scap.getStmt(),
				sourceContext.getAccessPath(),
				sourceContext.getStmt(),
				sourceContext.getUserData(),
				scap.getAbstractionPath());
		return true;
	}
	
	@Override
	public void computeTaintPaths(final Set<AbstractionAtSink> res) {
		runSourceFindingTasks(res);
	}
	
	private void runSourceFindingTasks(final Set<AbstractionAtSink> res) {
		if (res.isEmpty())
			return;
		
		long beforePathTracking = System.nanoTime();
    	logger.info("Obtainted {} connections between sources and sinks", res.size());
    	
    	// Start the propagation tasks
    	int curResIdx = 0;
    	for (final AbstractionAtSink abs : res) {
    		logger.info("Building path " + ++curResIdx);
   			buildPathForAbstraction(abs);
   			
   			// Also build paths for the neighbors of our result abstraction
   			if (abs.getAbstraction().getNeighbors() != null)
   				for (Abstraction neighbor : abs.getAbstraction().getNeighbors()) {
   					AbstractionAtSink neighborAtSink = new AbstractionAtSink(neighbor,
   							abs.getSinkStmt());
   		   			buildPathForAbstraction(neighborAtSink);
   				}
    	}

    	try {
			executor.awaitCompletion();
		} catch (InterruptedException ex) {
			logger.error("Could not wait for path executor completion: {0}", ex.getMessage());
			ex.printStackTrace();
		}
    	
    	logger.info("Path processing took {} seconds in total",
    			(System.nanoTime() - beforePathTracking) / 1E9);
	}
	
	/**
	 * Builds the path for the given abstraction that reached a sink
	 * @param abs The abstraction that reached a sink
	 */
	private void buildPathForAbstraction(final AbstractionAtSink abs) {
		SourceContextAndPath scap = new SourceContextAndPath(
				abs.getAbstraction().getAccessPath(), abs.getSinkStmt());
		scap = scap.extendPath(abs.getAbstraction());
		if (pathCache.put(abs.getAbstraction(), scap))
			if (!checkForSource(abs.getAbstraction(), scap))
				executor.execute(new SourceFindingTask(abs.getAbstraction()));
	}
	
	@Override
	public InfoflowResults getResults() {
		return this.results;
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

}
