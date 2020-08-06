package soot.jimple.infoflow.data.pathBuilders;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import heros.solver.CountingThreadPoolExecutor;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.AbstractionAtSink;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

/**
 * Class for reconstructing abstraction paths from sinks to source
 * 
 * @author Steven Arzt
 */
public class ContextInsensitiveSourceFinder extends AbstractAbstractionPathBuilder {
	
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final InfoflowResults results = new InfoflowResults();
	private final CountingThreadPoolExecutor executor;
	
	private int lastTaskId = 0;
	private int numTasks = 0;
	
	/**
	 * Creates a new instance of the {@link ContextInsensitiveSourceFinder} class
	 * @param icfg The interprocedural control flow graph
	 * @param executor The executor in which to run the path reconstruction tasks
	 * @param maxThreadNum The maximum number of threads to use
	 */
	public ContextInsensitiveSourceFinder(IInfoflowCFG icfg, CountingThreadPoolExecutor executor) {
		super(icfg, false);
		this.executor = executor;
	}
	
	/**
	 * Task for only finding sources, not the paths towards them
	 * 
	 * @author Steven Arzt
	 */
	private class SourceFindingTask implements Runnable {
		private final int taskId;
		private final AbstractionAtSink flagAbs;
		private final List<Abstraction> abstractionQueue = new LinkedList<Abstraction>();
		
		public SourceFindingTask(int taskId, AbstractionAtSink flagAbs, Abstraction abstraction) {
			this.taskId = taskId;
			this.flagAbs = flagAbs;
			this.abstractionQueue.add(abstraction);
			abstraction.registerPathFlag(taskId, numTasks);
		}
		
		@Override
		public void run() {
			while (!abstractionQueue.isEmpty()) {
				Abstraction abstraction = abstractionQueue.remove(0);
				
				if (abstraction.getSourceContext() != null) {
					// Register the result
					results.addResult(flagAbs.getAbstraction().getAccessPath(),
							flagAbs.getSinkStmt(),
							abstraction.getSourceContext().getAccessPath(),
							abstraction.getSourceContext().getStmt(),
							abstraction.getSourceContext().getUserData(),
							null);
					
					// Sources may not have predecessors
					assert abstraction.getPredecessor() == null;
				}
				else
					if (abstraction.getPredecessor().registerPathFlag(taskId, numTasks))
						abstractionQueue.add(abstraction.getPredecessor());
				
				if (abstraction.getNeighbors() != null)
					for (Abstraction nb : abstraction.getNeighbors())
						if (nb.registerPathFlag(taskId, numTasks))
							abstractionQueue.add(nb);
			}
		}
	}
	
	@Override
	public void computeTaintPaths(final Set<AbstractionAtSink> res) {
		if (res.isEmpty())
			return;
		
		long beforePathTracking = System.nanoTime();
    	logger.info("Obtainted {} connections between sources and sinks", res.size());
    	
    	// Start the propagation tasks
    	int curResIdx = 0;
    	numTasks = res.size() + 1;
    	for (final AbstractionAtSink abs : res) {
    		logger.info("Building path " + ++curResIdx + " with task id " + lastTaskId);
    		executor.execute(new SourceFindingTask(lastTaskId++, abs, abs.getAbstraction()));
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
	
	@Override
	public InfoflowResults getResults() {
		return this.results;
	}

	@Override
	public void runIncrementalPathCompuation() {
		// not implemented
	}

}
