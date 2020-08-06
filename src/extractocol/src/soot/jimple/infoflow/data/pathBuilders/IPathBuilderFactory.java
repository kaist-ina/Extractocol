package soot.jimple.infoflow.data.pathBuilders;

import heros.solver.CountingThreadPoolExecutor;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;


/**
 * Common interface for all path builder factories
 * 
 * @author Steven Arzt
 */
public interface IPathBuilderFactory {
	
	/**
	 * Creates a new path builder. Use this overload to have the path builder
	 * manage its own executors.
	 * @param maxThreadNum The maximum number of threads to use
	 * @param icfg The interprocedural CFG to use
	 * @return The newly created path builder
	 */
	public IAbstractionPathBuilder createPathBuilder(int maxThreadNum, 
			IInfoflowCFG icfg);
	
	/**
	 * Creates a new path builder. Use this overload if you want the path builder
	 * to submit its tasks to an existing executor.
	 * @param executor The executor in which to run the path reconstruction
	 * tasks.
	 * @param icfg The interprocedural CFG to use
	 * @return The newly created path builder
	 */
	public IAbstractionPathBuilder createPathBuilder(
			CountingThreadPoolExecutor executor, IInfoflowCFG icfg);
	
	/**
	 * Gets whether the {@link IAbstractionPathBuilder} object created by this
	 * factory supports the reconstruction of the exact paths between source
	 * and sink.
	 * @return True if the {@link IAbstractionPathBuilder} object constructed
	 * by this factory gives the exact propagation path between source and sink,
	 * false if it only reports source-to-sink connections without paths.
	 */
	public boolean supportsPathReconstruction();
	
	/**
	 * Gets whether the {@link IAbstractionPathBuilder} object created by this
	 * factory supports context-sensitive path reconstruction.
	 * @return True if the {@link IAbstractionPathBuilder} object created by
	 * this factory supports context-sensitive path reconstruction, otherwise
	 * false.
	 */
	public boolean isContextSensitive();

}
