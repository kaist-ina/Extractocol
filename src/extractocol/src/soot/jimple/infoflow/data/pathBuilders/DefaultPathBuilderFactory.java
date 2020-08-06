package soot.jimple.infoflow.data.pathBuilders;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import heros.solver.CountingThreadPoolExecutor;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

/**
 * Default factory class for abstraction path builders
 * 
 * @author Steven Arzt
 */
public class DefaultPathBuilderFactory implements IPathBuilderFactory {
	
	private final boolean reconstructPaths;
	
	/**
	 * Enumeration containing the supported path builders
	 */
	public enum PathBuilder {
		/**
		 * Simple context-insensitive, single-threaded, recursive approach to
		 * path reconstruction. Low overhead for small examples, but does not
		 * scale.
		 */
		Recursive,
		/**
		 * Highly precise context-sensitive path reconstruction approach. For
		 * a large number of paths or complex programs, it may be slow.
		 */
		ContextSensitive,
		/**
		 * A context-insensitive path reconstruction algorithm. It scales well,
		 * but may introduce false positives.
		 */
		ContextInsensitive,
		/**
		 * Very fast context-insensitive implementation that only finds
		 * source-to-sink connections, but no paths.
		 */
		ContextInsensitiveSourceFinder,
		/**
		 * An empty implementation that not reconstruct any paths and always
		 * returns an empty set. For internal use only.
		 */
		None
	}
	
	private final PathBuilder pathBuilder;
	
	/**
	 * Creates a new instance of the {@link DefaultPathBuilderFactory} class
	 */
	public DefaultPathBuilderFactory() {
		this(PathBuilder.ContextSensitive, false);
	}

	/**
	 * Creates a new instance of the {@link DefaultPathBuilderFactory} class
	 * @param builder The path building algorithm to use
	 * @param reconstructPaths Specifies whether the exact propagation paths
	 * between source and sink shall be reconstructed if supported by the chosen
	 * path building algorithm.
	 */
	public DefaultPathBuilderFactory(PathBuilder builder,
			boolean reconstructPaths) {
		this.pathBuilder = builder;
		this.reconstructPaths = reconstructPaths;
	}
	
	/**
	 * Creates a new executor object for spawning worker threads
	 * @param maxThreadNum The number of threads to use
	 * @return The generated executor
	 */
	private CountingThreadPoolExecutor createExecutor(int maxThreadNum) {
        int numThreads = Runtime.getRuntime().availableProcessors();
		return new CountingThreadPoolExecutor(maxThreadNum == -1 ? numThreads
				: Math.min(maxThreadNum, numThreads), Integer.MAX_VALUE, 30, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
	}
	
	@Override
	public IAbstractionPathBuilder createPathBuilder(int maxThreadNum,
			IInfoflowCFG icfg) {
		return createPathBuilder(createExecutor(maxThreadNum), icfg);
	}
	
	@Override
	public IAbstractionPathBuilder createPathBuilder(CountingThreadPoolExecutor executor,
			IInfoflowCFG icfg) {
		switch (pathBuilder) {
		case Recursive :
			return new RecursivePathBuilder(icfg, executor,
					reconstructPaths);
		case ContextSensitive :
			return new ContextSensitivePathBuilder(icfg, executor,
					reconstructPaths);
		case ContextInsensitive :
			return new ContextInsensitivePathBuilder(icfg, executor,
					reconstructPaths);
		case ContextInsensitiveSourceFinder :
			return new ContextInsensitiveSourceFinder(icfg, executor);
		case None:
			return new EmptyPathBuilder();
		}
		throw new RuntimeException("Unsupported path building algorithm");
	}

	@Override
	public boolean supportsPathReconstruction() {
		switch (pathBuilder) {
		case Recursive :
		case ContextSensitive :
		case ContextInsensitive :
			return reconstructPaths;
		case ContextInsensitiveSourceFinder :
		case None:
			return false;
		}
		throw new RuntimeException("Unsupported path building algorithm");
	}

	@Override
	public boolean isContextSensitive() {
		return pathBuilder == PathBuilder.ContextSensitive;
	}
	
}
