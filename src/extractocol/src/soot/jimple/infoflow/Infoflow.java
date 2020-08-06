/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 *
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
 * Bodden, and others.
 ******************************************************************************/
package soot.jimple.infoflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import extractocol.frontend.solver.MyBackwardsInfoflowSolver;
import extractocol.frontend.solver.MyInfoflowSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import heros.solver.CountingThreadPoolExecutor;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.infoflow.InfoflowConfiguration.CallgraphAlgorithm;
import soot.jimple.infoflow.InfoflowConfiguration.CodeEliminationMode;
import soot.jimple.infoflow.aliasing.FlowSensitiveAliasStrategy;
import soot.jimple.infoflow.aliasing.IAliasingStrategy;
import soot.jimple.infoflow.aliasing.PtsBasedAliasStrategy;
import soot.jimple.infoflow.cfg.BiDirICFGFactory;
import soot.jimple.infoflow.codeOptimization.DeadCodeEliminator;
import soot.jimple.infoflow.codeOptimization.ICodeOptimizer;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.AbstractionAtSink;
import soot.jimple.infoflow.data.AccessPathFactory;
import soot.jimple.infoflow.data.FlowDroidMemoryManager;
import soot.jimple.infoflow.data.FlowDroidMemoryManager.PathDataErasureMode;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory;
import soot.jimple.infoflow.data.pathBuilders.IAbstractionPathBuilder;
import soot.jimple.infoflow.data.pathBuilders.IAbstractionPathBuilder.OnPathBuilderResultAvailable;
import soot.jimple.infoflow.data.pathBuilders.IPathBuilderFactory;
import soot.jimple.infoflow.entryPointCreators.IEntryPointCreator;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler2;
import soot.jimple.infoflow.handlers.TaintPropagationHandler;
import soot.jimple.infoflow.problems.BackwardsInfoflowProblem;
import soot.jimple.infoflow.problems.InfoflowProblem;
import soot.jimple.infoflow.problems.TaintPropagationResults;
import soot.jimple.infoflow.problems.TaintPropagationResults.OnTaintPropagationResultAdded;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.IMemoryManager;
import soot.jimple.infoflow.solver.cfg.BackwardsInfoflowCFG;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.solver.fastSolver.BackwardsInfoflowSolver;
import soot.jimple.infoflow.solver.fastSolver.InfoflowSolver;
import soot.jimple.infoflow.source.ISourceSinkManager;
import soot.jimple.infoflow.util.SootMethodRepresentationParser;
import soot.jimple.infoflow.util.SystemClassHandler;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.options.Options;
/**
 * main infoflow class which triggers the analysis and offers method to customize it.
 *
 */
public class Infoflow extends AbstractInfoflow {

    private final Logger logger = LoggerFactory.getLogger(getClass());

	private InfoflowResults results = null;
    private IInfoflowCFG iCfg;

    private Set<ResultsAvailableHandler> onResultsAvailable = new HashSet<ResultsAvailableHandler>();
    private TaintPropagationHandler taintPropagationHandler = null;
    private TaintPropagationHandler backwardsPropagationHandler = null;

    private long maxMemoryConsumption = -1;

    private Set<Stmt> collectedSources = null;
    private Set<Stmt> collectedSinks = null;

	/**
	 * Creates a new instance of the InfoFlow class for analyzing plain Java code without any references to APKs or the Android SDK.
	 */
	public Infoflow() {
		super();
	}

	/**
	 * Creates a new instance of the Infoflow class for analyzing Android APK files.
	 * @param androidPath If forceAndroidJar is false, this is the base directory
	 * of the platform files in the Android SDK. If forceAndroidJar is true, this
	 * is the full path of a single android.jar file.
	 * @param forceAndroidJar True if a single platform JAR file shall be forced,
	 * false if Soot shall pick the appropriate platform version
	 */
	public Infoflow(String androidPath, boolean forceAndroidJar) {
		super(null, androidPath, forceAndroidJar);
		this.pathBuilderFactory = new DefaultPathBuilderFactory();
	}

	/**
	 * Creates a new instance of the Infoflow class for analyzing Android APK files.
	 * @param androidPath If forceAndroidJar is false, this is the base directory
	 * of the platform files in the Android SDK. If forceAndroidJar is true, this
	 * is the full path of a single android.jar file.
	 * @param forceAndroidJar True if a single platform JAR file shall be forced,
	 * false if Soot shall pick the appropriate platform version
	 * @param icfgFactory The interprocedural CFG to be used by the InfoFlowProblem
	 * @param pathBuilderFactory The factory class for constructing a path builder
	 * algorithm
	 */
	public Infoflow(String androidPath, boolean forceAndroidJar, BiDirICFGFactory icfgFactory,
			IPathBuilderFactory pathBuilderFactory) {
		super(icfgFactory, androidPath, forceAndroidJar);
		this.pathBuilderFactory = pathBuilderFactory == null ? new DefaultPathBuilderFactory()
				: pathBuilderFactory;
	}

	@Override
	public void computeInfoflow(String appPath, String libPath,
			IEntryPointCreator entryPointCreator,
			ISourceSinkManager sourcesSinks) {
		if (sourcesSinks == null) {
			logger.error("Sources are empty!");
			return;
		}

		initializeSoot(appPath, libPath, entryPointCreator.getRequiredClasses());

		// entryPoints are the entryPoints required by Soot to calculate Graph - if there is no main method,
		// we have to create a new main method and use it as entryPoint and store our real entryPoints
		Scene.v().setEntryPoints(Collections.singletonList(entryPointCreator.createDummyMain()));

		// Run the analysis
        runAnalysis(sourcesSinks, null);
	}

	@Override
	public void computeInfoflow(String appPath, String libPath, String entryPoint,
			ISourceSinkManager sourcesSinks) {
		if (sourcesSinks == null) {
			logger.error("Sources are empty!");
			return;
		}

		initializeSoot(appPath, libPath,
				SootMethodRepresentationParser.v().parseClassNames
					(Collections.singletonList(entryPoint), false).keySet(), entryPoint);

		if (!Scene.v().containsMethod(entryPoint)){
			logger.error("Entry point not found: " + entryPoint);
			return;
		}
		SootMethod ep = Scene.v().getMethod(entryPoint);
		if (ep.isConcrete())
			ep.retrieveActiveBody();
		else {
			logger.debug("Skipping non-concrete method " + ep);
			return;
		}
		Scene.v().setEntryPoints(Collections.singletonList(ep));
		Options.v().set_main_class(ep.getDeclaringClass().getName());

		// Compute the additional seeds if they are specified
		Set<String> seeds = Collections.emptySet();
		if (entryPoint != null && !entryPoint.isEmpty())
			seeds = Collections.singleton(entryPoint);
		ipcManager.updateJimpleForICC();

		// Run the analysis
        runAnalysis(sourcesSinks, seeds);
	}

	/**
	 * Conducts a taint analysis on an already initialized callgraph
	 * @param sourcesSinks The sources and sinks to be used
	 */
	protected void runAnalysis(final ISourceSinkManager sourcesSinks) {
		runAnalysis(sourcesSinks, null);
	}

	/**
	 * Conducts a taint analysis on an already initialized callgraph
	 * @param sourcesSinks The sources and sinks to be used
	 * @param additionalSeeds Additional seeds at which to create A ZERO fact
	 * even if they are not sources
	 */
	private void runAnalysis(final ISourceSinkManager sourcesSinks, final Set<String> additionalSeeds) {
		// Clear the data from previous runs
		maxMemoryConsumption = -1;
		results = null;

		// Some configuration options do not really make sense in combination
		if (config.getEnableStaticFieldTracking()
				&& InfoflowConfiguration.getAccessPathLength() == 0)
			throw new RuntimeException("Static field tracking must be disabled "
					+ "if the access path length is zero");
		if (InfoflowConfiguration.getAccessPathLength() < 0)
			throw new RuntimeException("The access path length may not be negative");

		// Clear the base registrations from previous runs
		AccessPathFactory.v().clearBaseRegister();

		// Build the callgraph
		long beforeCallgraph = System.nanoTime();
		constructCallgraph();
		logger.info("Callgraph construction took " + (System.nanoTime() - beforeCallgraph) / 1E9
				+ " seconds");

        // Perform constant propagation and remove dead code
        if (config.getCodeEliminationMode() != CodeEliminationMode.NoCodeElimination) {
			long currentMillis = System.nanoTime();
			eliminateDeadCode(sourcesSinks);
			logger.info("Dead code elimination took " + (System.nanoTime() - currentMillis) / 1E9
					+ " seconds");
        }

        if (config.getCallgraphAlgorithm() != CallgraphAlgorithm.OnDemand)
        	logger.info("Callgraph has {} edges", Scene.v().getCallGraph().size());

		if (!config.isTaintAnalysisEnabled()) {
			return;
		}
		logger.info("Starting Taint Analysis");
        iCfg = icfgFactory.buildBiDirICFG(config.getCallgraphAlgorithm(),
        		config.getEnableExceptionTracking());

        int numThreads = Runtime.getRuntime().availableProcessors();
		CountingThreadPoolExecutor executor = createExecutor(numThreads);

		// Initialize the memory manager
		PathDataErasureMode erasureMode = PathDataErasureMode.EraseAll;
		if (pathBuilderFactory.isContextSensitive())
			erasureMode = PathDataErasureMode.KeepOnlyContextData;
		if (pathBuilderFactory.supportsPathReconstruction())
			erasureMode = PathDataErasureMode.EraseNothing;
		IMemoryManager<Abstraction> memoryManager = new FlowDroidMemoryManager(false,
				erasureMode);

		// Initialize the data flow manager
		InfoflowManager manager = new InfoflowManager(config, null, iCfg, sourcesSinks,
				taintWrapper, hierarchy);

		BackwardsInfoflowProblem backProblem = null;
		InfoflowManager backwardsManager = null;
		MyInfoflowSolver backSolver = null;
		final IAliasingStrategy aliasingStrategy;
		switch (getConfig().getAliasingAlgorithm()) {
			case FlowSensitive:
				backwardsManager = new InfoflowManager(config, null,
						new BackwardsInfoflowCFG(iCfg), sourcesSinks, taintWrapper, hierarchy);
				backProblem = new BackwardsInfoflowProblem(backwardsManager);
				backSolver = new MyBackwardsInfoflowSolver(backProblem, executor);
				backSolver.setMemoryManager(memoryManager);
				backSolver.setJumpPredecessors(!pathBuilderFactory.supportsPathReconstruction());
//				backSolver.setEnableMergePointChecking(true);

				aliasingStrategy = new FlowSensitiveAliasStrategy(iCfg, backSolver);
				break;
			case PtsBased:
				backProblem = null;
				backSolver = null;
				aliasingStrategy = new PtsBasedAliasStrategy(iCfg);
				break;
			default:
				throw new RuntimeException("Unsupported aliasing algorithm");
		}

		// Get the zero fact
		Abstraction zeroValue = backProblem != null
				? backProblem.createZeroValue() : null;
		InfoflowProblem forwardProblem  = new InfoflowProblem(manager,
				aliasingStrategy, zeroValue);

		// Set the options
		InfoflowSolver forwardSolver = new InfoflowSolver(forwardProblem, executor);
		aliasingStrategy.setForwardSolver(forwardSolver);
		manager.setForwardSolver(forwardSolver);
		if (backwardsManager != null)
			backwardsManager.setForwardSolver(forwardSolver);

		forwardSolver.setMemoryManager(memoryManager);
		forwardSolver.setJumpPredecessors(!pathBuilderFactory.supportsPathReconstruction());
//		forwardSolver.setEnableMergePointChecking(true);

		forwardProblem.setTaintPropagationHandler(taintPropagationHandler);
		forwardProblem.setTaintWrapper(taintWrapper);
		if (nativeCallHandler != null)
			forwardProblem.setNativeCallHandler(nativeCallHandler);

		if (backProblem != null) {
			backProblem.setForwardSolver(forwardSolver);
			backProblem.setTaintPropagationHandler(backwardsPropagationHandler);
			backProblem.setTaintWrapper(taintWrapper);
			if (nativeCallHandler != null)
				backProblem.setNativeCallHandler(nativeCallHandler);
			backProblem.setActivationUnitsToCallSites(forwardProblem);
		}

		// Print our configuration
		config.printSummary();
		if (config.getFlowSensitiveAliasing() && !aliasingStrategy.isFlowSensitive())
			logger.warn("Trying to use a flow-sensitive aliasing with an "
					+ "aliasing strategy that does not support this feature");

		// We have to look through the complete program to find sources
		// which are then taken as seeds.
		int sinkCount = 0;
        logger.info("Looking for sources and sinks...");

        for (SootMethod sm : getMethodsForSeeds(iCfg))
			sinkCount += scanMethodForSourcesSinks(sourcesSinks, forwardProblem, sm);

		// We optionally also allow additional seeds to be specified
		if (additionalSeeds != null)
			for (String meth : additionalSeeds) {
				SootMethod m = Scene.v().getMethod(meth);
				if (!m.hasActiveBody()) {
					logger.warn("Seed method {} has no active body", m);
					continue;
				}
				forwardProblem.addInitialSeeds(m.getActiveBody().getUnits().getFirst(),
						Collections.singleton(forwardProblem.zeroValue()));
			}

		// Report on the sources and sinks we have found
		if (!forwardProblem.hasInitialSeeds()) {
			logger.error("No sources found, aborting analysis");
			return;
		}
		if (sinkCount == 0) {
			logger.error("No sinks found, aborting analysis");
			return;
		}
		logger.info("Source lookup done, found {} sources and {} sinks.", forwardProblem.getInitialSeeds().size(),
				sinkCount);

		// Initialize the taint wrapper if we have one
		if (taintWrapper != null)
			taintWrapper.initialize(manager);
		if (nativeCallHandler != null)
			nativeCallHandler.initialize(manager);
		
		// Register the handler for interim results
		TaintPropagationResults propagationResults = forwardProblem.getResults();
		final CountingThreadPoolExecutor resultExecutor = createExecutor(numThreads);
		final IAbstractionPathBuilder builder = pathBuilderFactory.createPathBuilder(
				resultExecutor, iCfg);
		
		if (config.getIncrementalResultReporting()) {
			// Create the path builder
			this.results = new InfoflowResults();
			propagationResults.addResultAvailableHandler(new OnTaintPropagationResultAdded() {
				
				@Override
				public boolean onResultAvailable(AbstractionAtSink abs) {
					builder.addResultAvailableHandler(new OnPathBuilderResultAvailable() {
						
						@Override
						public void onResultAvailable(ResultSourceInfo source, ResultSinkInfo sink) {
							// Notify our external handlers
							for (ResultsAvailableHandler handler : onResultsAvailable) {
								if (handler instanceof ResultsAvailableHandler2) {
									ResultsAvailableHandler2 handler2 = (ResultsAvailableHandler2) handler;
									handler2.onSingleResultAvailable(source, sink);
								}
							}
					   		results.addResult(sink, source);
						}
						
					});
					
					// Compute the result paths
			   		builder.computeTaintPaths(Collections.singleton(abs));
					return true;
				}
				
			});
		}

		forwardSolver.solve();
		maxMemoryConsumption = Math.max(maxMemoryConsumption, getUsedMemory());
		
		// Not really nice, but sometimes Heros returns before all
		// executor tasks are actually done. This way, we give it a
		// chance to terminate gracefully before moving on.
		int terminateTries = 0;
		while (terminateTries < 10) {
			if (executor.getActiveCount() != 0 || !executor.isTerminated()) {
				terminateTries++;
				try {
					Thread.sleep(500);
				}
				catch (InterruptedException e) {
					logger.error("Could not wait for executor termination", e);
				}
			}
			else
				break;
		}
		if (executor.getActiveCount() != 0 || !executor.isTerminated())
			logger.error("Executor did not terminate gracefully");
		
		// Print taint wrapper statistics
		if (taintWrapper != null) {
			logger.info("Taint wrapper hits: " + taintWrapper.getWrapperHits());
			logger.info("Taint wrapper misses: " + taintWrapper.getWrapperMisses());
		}

		Set<AbstractionAtSink> res = propagationResults.getResults();

		// We need to prune access paths that are entailed by another one
		for (Iterator<AbstractionAtSink> absAtSinkIt = res.iterator(); absAtSinkIt.hasNext(); ) {
			AbstractionAtSink curAbs = absAtSinkIt.next();
			for (AbstractionAtSink checkAbs : res)
				if (checkAbs != curAbs
						&& checkAbs.getSinkStmt() == curAbs.getSinkStmt()
						&& checkAbs.getAbstraction().isImplicit() == curAbs.getAbstraction().isImplicit()
						&& checkAbs.getAbstraction().getSourceContext() == curAbs.getAbstraction().getSourceContext())
					if (checkAbs.getAbstraction().getAccessPath().entails(
							curAbs.getAbstraction().getAccessPath())) {
						absAtSinkIt.remove();
						break;
					}
		}

		logger.info("IFDS problem with {} forward and {} backward edges solved, "
				+ "processing {} results...", forwardSolver.propagationCount,
				backSolver == null ? 0 : backSolver.propagationCount,
				res == null ? 0 : res.size());

		// Force a cleanup. Everything we need is reachable through the
		// results set, the other abstractions can be killed now.
		maxMemoryConsumption = Math.max(maxMemoryConsumption, getUsedMemory());
		forwardSolver.cleanup();
		if (backSolver != null) {
			backSolver.cleanup();
			backSolver = null;
			backProblem = null;
		}
		forwardSolver = null;
		forwardProblem = null;
		Runtime.getRuntime().gc();

		if (!config.getIncrementalResultReporting()) {
	   		builder.computeTaintPaths(res);
	   		if (this.results == null)
	   			this.results = builder.getResults();
	   		else
	   			this.results.addAll(builder.getResults());
		}
		
		// Wait for the path builders to terminate
		try {
			resultExecutor.awaitCompletion();
		} catch (InterruptedException e) {
			logger.error("Could not wait for executor termination", e);
		}

		if (config.getIncrementalResultReporting()) {
			// After the last intermediate result has been computed, we need to
			// re-process those abstractions that received new neighbors in the
			// meantime
			builder.runIncrementalPathCompuation();
			
			try {
				resultExecutor.awaitCompletion();
			} catch (InterruptedException e) {
				logger.error("Could not wait for executor termination", e);
			}			
		}
		resultExecutor.shutdown();

		if (results == null || results.getResults().isEmpty())
			logger.warn("No results found.");
		else for (ResultSinkInfo sink : results.getResults().keySet()) {
			logger.info("The sink {} in method {} was called with values from the following sources:",
                    sink, iCfg.getMethodOf(sink.getSink()).getSignature() );
			for (ResultSourceInfo source : results.getResults().get(sink)) {
				logger.info("- {} in method {}",source, iCfg.getMethodOf(source.getSource()).getSignature());
				if (source.getPath() != null) {
					logger.info("\ton Path: ");
					for (Unit p : source.getPath()) {
						logger.info("\t -> " + iCfg.getMethodOf(p));
						logger.info("\t\t -> " + p);
					}
				}
			}
		}

		for (ResultsAvailableHandler handler : onResultsAvailable)
			handler.onResultsAvailable(iCfg, results);

		if (config.getWriteOutputFiles())
			PackManager.v().writeOutput();

		maxMemoryConsumption = Math.max(maxMemoryConsumption, getUsedMemory());
		System.out.println("Maximum memory consumption: " + maxMemoryConsumption / 1E6 + " MB");
	}

	/**
	 * Gets the memory used by FlowDroid at the moment
	 * @return FlowDroid's current memory consumption in bytes
	 */
	private long getUsedMemory() {
		Runtime runtime = Runtime.getRuntime();
		return runtime.totalMemory() - runtime.freeMemory();
	}

	/**
	 * Runs all code optimizers
	 * @param sourcesSinks The SourceSinkManager
	 */
	private void eliminateDeadCode(ISourceSinkManager sourcesSinks) {
		ICodeOptimizer dce = new DeadCodeEliminator();
		dce.initialize(config);
		dce.run(iCfg, Scene.v().getEntryPoints(), sourcesSinks, taintWrapper);
	}

	/**
	 * Creates a new executor object for spawning worker threads
	 * @param numThreads The number of threads to use
	 * @return The generated executor
	 */
	private CountingThreadPoolExecutor createExecutor(int numThreads) {
		return new CountingThreadPoolExecutor
				(config.getMaxThreadNum() == -1 ? numThreads
						: Math.min(config.getMaxThreadNum(), numThreads),
				Integer.MAX_VALUE, 30, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>());
	}
	
	private Collection<SootMethod> getMethodsForSeeds(IInfoflowCFG icfg) {
		List<SootMethod> seeds = new LinkedList<SootMethod>();
		// If we have a callgraph, we retrieve the reachable methods. Otherwise,
		// we have no choice but take all application methods as an approximation
		if (Scene.v().hasCallGraph()) {
			List<MethodOrMethodContext> eps = new ArrayList<MethodOrMethodContext>(Scene.v().getEntryPoints());
			ReachableMethods reachableMethods = new ReachableMethods(Scene.v().getCallGraph(), eps.iterator(), null);
			reachableMethods.update();
			for (Iterator<MethodOrMethodContext> iter = reachableMethods.listener(); iter.hasNext();)
				seeds.add(iter.next().method());
		}
		else {
			long beforeSeedMethods = System.nanoTime();
			Set<SootMethod> doneSet = new HashSet<SootMethod>();
			for (SootMethod sm : Scene.v().getEntryPoints())
				getMethodsForSeedsIncremental(sm, doneSet, seeds, icfg);
			logger.info("Collecting seed methods took {} seconds", (System.nanoTime() - beforeSeedMethods) / 1E9);
		}
		return seeds;
	}

	private void getMethodsForSeedsIncremental(SootMethod sm,
			Set<SootMethod> doneSet, List<SootMethod> seeds, IInfoflowCFG icfg) {
		assert Scene.v().hasFastHierarchy();
		if (!sm.isConcrete() || !sm.getDeclaringClass().isApplicationClass() || !doneSet.add(sm))
			return;
		seeds.add(sm);
		for (Unit u : sm.retrieveActiveBody().getUnits()) {
			Stmt stmt = (Stmt) u;
			if (stmt.containsInvokeExpr())
				for (SootMethod callee : icfg.getCalleesOfCallAt(stmt))
					getMethodsForSeedsIncremental(callee, doneSet, seeds, icfg);
		}
	}

	/**
	 * Scans the given method for sources and sinks contained in it. Sinks are
	 * just counted, sources are added to the InfoflowProblem as seeds.
	 * @param sourcesSinks The SourceSinkManager to be used for identifying
	 * sources and sinks
	 * @param forwardProblem The InfoflowProblem in which to register the
	 * sources as seeds
	 * @param m The method to scan for sources and sinks
	 * @return The number of sinks found in this method
	 */
	private int scanMethodForSourcesSinks(
			final ISourceSinkManager sourcesSinks,
			InfoflowProblem forwardProblem,
			SootMethod m) {
		if (getConfig().getLogSourcesAndSinks() && collectedSources == null) {
			collectedSources = new HashSet<>();
			collectedSinks = new HashSet<>();
		}
		
		int sinkCount = 0;
		if (m.hasActiveBody()) {
			// Check whether this is a system class we need to ignore
			final String className = m.getDeclaringClass().getName();
			if (config.getIgnoreFlowsInSystemPackages()
					&& SystemClassHandler.isClassInSystemPackage(className))
				return sinkCount;

			// Look for a source in the method. Also look for sinks. If we
			// have no sink in the program, we don't need to perform any
			// analysis
			PatchingChain<Unit> units = m.getActiveBody().getUnits();
			for (Unit u : units) {
				Stmt s = (Stmt) u;
				if (sourcesSinks.getSourceInfo(s, iCfg) != null) {
					forwardProblem.addInitialSeeds(u, Collections.singleton(forwardProblem.zeroValue()));
					if (getConfig().getLogSourcesAndSinks())
						collectedSources.add(s);
					logger.debug("Source found: {}", u);
				}
				if (sourcesSinks.isSink(s, iCfg, null)) {
					sinkCount++;
					if (getConfig().getLogSourcesAndSinks())
						collectedSinks.add(s);
					logger.debug("Sink found: {}", u);
				}
			}

		}
		return sinkCount;
	}

	@Override
	public InfoflowResults getResults() {
		return results;
	}

	@Override
	public boolean isResultAvailable() {
		if (results == null) {
			return false;
		}
		return true;
	}

	/**
	 * Adds a handler that is called when information flow results are available
	 * @param handler The handler to add
	 */
	public void addResultsAvailableHandler(ResultsAvailableHandler handler) {
		this.onResultsAvailable.add(handler);
	}

	/**
	 * Sets a handler which is invoked whenever a taint is propagated
	 * @param handler The handler to be invoked when propagating taints
	 */
	public void setTaintPropagationHandler(TaintPropagationHandler handler) {
		this.taintPropagationHandler = handler;
	}

	/**
	 * Sets a handler which is invoked whenever an alias is propagated backwards
	 * @param handler The handler to be invoked when propagating aliases
	 */
	public void setBackwardsPropagationHandler(TaintPropagationHandler handler) {
		this.backwardsPropagationHandler = handler;
	}

	/**
	 * Removes a handler that is called when information flow results are available
	 * @param handler The handler to remove
	 */
	public void removeResultsAvailableHandler(ResultsAvailableHandler handler) {
		onResultsAvailable.remove(handler);
	}

	/**
	 * Gets the maximum memory consumption during the last analysis run
	 * @return The maximum memory consumption during the last analysis run if
	 * available, otherwise -1
	 */
	public long getMaxMemoryConsumption() {
		return this.maxMemoryConsumption;
	}

	/**
	 * Gets the concrete set of sources that have been collected in preparation
	 * for the taint analysis. This method will return null if source and sink
	 * logging has not been enabled (see InfoflowConfiguration.
	 * setLogSourcesAndSinks()),
	 * @return The set of sources collected for taint analysis
	 */
	public Set<Stmt> getCollectedSources() {
		return this.collectedSources;
	}

	/**
	 * Gets the concrete set of sinks that have been collected in preparation
	 * for the taint analysis. This method will return null if source and sink
	 * logging has not been enabled (see InfoflowConfiguration.
	 * setLogSourcesAndSinks()),
	 * @return The set of sinks collected for taint analysis
	 */
	public Set<Stmt> getCollectedSinks() {
		return this.collectedSinks;
	}

}
