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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import Extractocol.Debugger.ExtractocolLogger;
import Extractocol.common.UnzipAPK;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gaurav.tree.NodeNotFoundException;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.Backend_Worker;
import Extractocol.BufferExtractor_Request.Basics.EPcontainer;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapTreeNode;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapTreeNode.SourceforTaint;
import Extractocol.BufferExtractor_Request.Helper.CFGSerializer;
import Extractocol.BufferExtractor_Request.Helper.ResultPrintHandler;
import Extractocol.Outputs.RequestInfoEntry;
import Extractocol.Outputs.RequestInfoList;
import Extractocol.Pairing.BuildPairJson;
import Extractocol.Pairing.PairingHandler;
import emulator.EntrypointFinder;
import heros.solver.CountingThreadPoolExecutor;
import soot.CallbackCandidateFinder;
import soot.MethodOrMethodContext;
import soot.PackManager;
import soot.PatchingChain;
import soot.Printer;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.aliasing.FlowSensitiveAliasStrategy;
import soot.jimple.infoflow.aliasing.IAliasingStrategy;
import soot.jimple.infoflow.aliasing.PtsBasedAliasStrategy;
import soot.jimple.infoflow.cfg.BiDirICFGFactory;
import soot.jimple.infoflow.cfg.LibraryClassPatcher;
import soot.jimple.infoflow.config.IInfoflowConfig;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.data.FlowDroidMemoryManager;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory;
import soot.jimple.infoflow.data.pathBuilders.IPathBuilderFactory;
import soot.jimple.infoflow.entryPointCreators.IEntryPointCreator;
import soot.jimple.infoflow.handlers.PreAnalysisHandler;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler;
import soot.jimple.infoflow.handlers.TaintPropagationHandler;
import soot.jimple.infoflow.ipc.DefaultIPCManager;
import soot.jimple.infoflow.ipc.IIPCManager;
import soot.jimple.infoflow.pairing.ReconstructHttpTransaction;
import soot.jimple.infoflow.problems.BackwardTaintingProblem;
import soot.jimple.infoflow.problems.BackwardTaintingProblemForHeap;
import soot.jimple.infoflow.problems.BackwardsInfoflowProblem;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.slice.AbstractSlice;
import soot.jimple.infoflow.solver.IMemoryManager;
import soot.jimple.infoflow.solver.cfg.BackwardsInfoflowCFG;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.solver.cfg.InfoflowCFG;
import soot.jimple.infoflow.solver.fastSolver.InfoflowSolver;
import soot.jimple.infoflow.source.ISourceSinkManager;
import soot.jimple.infoflow.util.InterproceduralConstantValuePropagator;
import soot.jimple.infoflow.util.SootMethodRepresentationParser;
import soot.jimple.infoflow.util.SystemClassHandler;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.ConstantPropagatorAndFolder;
import soot.jimple.toolkits.scalar.DeadAssignmentEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.options.Options;
import soot.util.queue.QueueReader;

/**
 * main infoflow class which triggers the analysis and offers method to customize it.
 *
 */
public class BackwardInfoflow extends AbstractInfoflow
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	public static ArrayList<String> arrEntry = new ArrayList<String>();
	private static int accessPathLength = 5;
	private static boolean useRecursiveAccessPaths = true;
	private static boolean pathAgnosticResults = true;
	private static boolean oneResultPerAccessPath = false;
	private static boolean mergeNeighbors = false;
	private InfoflowResults results = null;
	private IPathBuilderFactory pathBuilderFactory;
	private final String androidPath;
	private final boolean forceAndroidJar;
	private IInfoflowConfig sootConfig;
	private IIPCManager ipcManager = new DefaultIPCManager(new ArrayList<String>());
	private IInfoflowCFG iCfg;
	private Set<ResultsAvailableHandler> onResultsAvailable = new HashSet<ResultsAvailableHandler>();
	private Set<TaintPropagationHandler> taintPropagationHandlers = new HashSet<TaintPropagationHandler>();
	private long maxMemoryConsumption = -1;
	/**
	 * Creates a new instance of the InfoFlow class for analyzing plain Java code without any references to APKs or the Android SDK.
	 */
	public BackwardInfoflow()
	{
		this.androidPath = "";
		this.forceAndroidJar = false;
		this.pathBuilderFactory = new DefaultPathBuilderFactory();
	}
	/**
	 * Creates a new instance of the Infoflow class for analyzing Android APK files.
	 * 
	 * @param androidPath
	 *            If forceAndroidJar is false, this is the base directory of the platform files in the Android SDK. If forceAndroidJar is true, this is the full path of a single android.jar file.
	 * @param forceAndroidJar
	 *            True if a single platform JAR file shall be forced, false if Soot shall pick the appropriate platform version
	 */
	public BackwardInfoflow(String androidPath, boolean forceAndroidJar)
	{
		super();
		this.androidPath = androidPath;
		this.forceAndroidJar = forceAndroidJar;
		this.pathBuilderFactory = new DefaultPathBuilderFactory();
	}
	/**
	 * Creates a new instance of the Infoflow class for analyzing Android APK files.
	 * 
	 * @param androidPath
	 *            If forceAndroidJar is false, this is the base directory of the platform files in the Android SDK. If forceAndroidJar is true, this is the full path of a single android.jar file.
	 * @param forceAndroidJar
	 *            True if a single platform JAR file shall be forced, false if Soot shall pick the appropriate platform version
	 * @param icfgFactory
	 *            The interprocedural CFG to be used by the InfoFlowProblem
	 * @param pathBuilderFactory
	 *            The factory class for constructing a path builder algorithm
	 */
	public BackwardInfoflow(String androidPath, boolean forceAndroidJar, BiDirICFGFactory icfgFactory, IPathBuilderFactory pathBuilderFactory)
	{
		super(icfgFactory);
		this.androidPath = androidPath;
		this.forceAndroidJar = forceAndroidJar;
		this.pathBuilderFactory = pathBuilderFactory;
	}
	public void setSootConfig(IInfoflowConfig config)
	{
		sootConfig = config;
	}
	/**
	 * Initializes Soot.
	 * 
	 * @param appPath
	 *            The application path containing the analysis client
	 * @param libPath
	 *            The Soot classpath containing the libraries
	 * @param classes
	 *            The set of classes that shall be checked for data flow analysis seeds. All sources in these classes are used as seeds.
	 * @param sourcesSinks
	 *            The manager object for identifying sources and sinks
	 */
	private void initializeSoot(String appPath, String libPath, Collection<String> classes)
	{
		initializeSoot(appPath, libPath, classes, "");
	}
	/** Set multiple dex files for Soot initialization
	 *
	 * @author Byungkwon Choi
	 * @param appPath Path of the app APK file
	 * @return True if it loads and sets multiple dex files successfully
	 */
	private boolean LoadMultiDexFiles(String appPath){
		if (appPath == null)
			return false;

		List<String> processDirs = new LinkedList<String>();
		processDirs.add(appPath);
		String basicPath = appPath.substring(0, appPath.lastIndexOf("/"));

		// Unzip the APK file to extract multiple dex files
		try	{
			UnzipAPK.UnzipAPK(appPath, basicPath);
		} catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}

		// Set the extracted dex files for Soot initialization
		for (int i = 2; i < 50; i++)
		{
			String dexPath = basicPath + Constants.getMultiDexName(basicPath, i);

			if (new File(dexPath).exists()){
				ExtractocolLogger.Info("Read additional dex file: " + dexPath);
				processDirs.add(dexPath);
			}else
				break;
		}

		if(processDirs.size() > 0)
			Options.v().set_process_dir(processDirs);

		return true;
	}

	/**
	 * Initializes Soot.
	 * 
	 * @param appPath
	 *            The application path containing the analysis client
	 * @param libPath
	 *            The Soot classpath containing the libraries
	 * @param classes
	 *            The set of classes that shall be checked for data flow analysis seeds. All sources in these classes are used as seeds. If a non-empty extra seed is given, this one is used too.
	 */
	private void initializeSoot(String appPath, String libPath, Collection<String> classes, String extraSeed)
	{
		// reset Soot:
		logger.info("Resetting Soot...");
		soot.G.reset();
		Options.v().set_no_bodies_for_excluded(true);
		Options.v().set_allow_phantom_refs(true);
		if (logger.isDebugEnabled())
			Options.v().set_output_format(Options.output_format_jimple);
		else
			Options.v().set_output_format(Options.output_format_none);
		// We only need to distinguish between application and library classes
		// if we use the OnTheFly ICFG
		if (callgraphAlgorithm == CallgraphAlgorithm.OnDemand)
		{
			Options.v().set_soot_classpath(libPath);
			if (appPath != null)
			{
				List<String> processDirs = new LinkedList<String>();
				for (String ap : appPath.split(File.pathSeparator))
					processDirs.add(ap);
				Options.v().set_process_dir(processDirs);
			}
		}
		else
		{
			Options.v().set_soot_classpath(libPath);
//			if (appPath != null)
//			{
//				List<String> processDirs = new LinkedList<String>();
//				processDirs.add(appPath);
//				String path2 = appPath.substring(0, appPath.lastIndexOf("\\"));
//				for (int i = 2; i < 11; i++)
//				{
//					processDirs.add(path2 + "/classes" + i + ".dex");
//				}
//				Options.v().set_process_dir(processDirs);
//			}
			if(!LoadMultiDexFiles(appPath))
				return;
		}
		// Configure the callgraph algorithm
		switch (callgraphAlgorithm)
		{
			case AutomaticSelection:
				// If we analyze a distinct entry point which is not static,
				// SPARK fails due to the missing allocation site and we fall
				// back to CHA.
				if (extraSeed == null || extraSeed.isEmpty())
				{
					Options.v().setPhaseOption("cg.spark", "on");
					Options.v().setPhaseOption("cg.spark", "string-constants:true");
					Options.v().setPhaseOption("cg.spark", "pre-jimplify:true");
				}
				else
					Options.v().setPhaseOption("cg.cha", "on");
			break;
			case CHA:
				Options.v().setPhaseOption("cg.cha", "on");
			break;
			case RTA:
				Options.v().setPhaseOption("cg.spark", "on");
				Options.v().setPhaseOption("cg.spark", "rta:true");
				Options.v().setPhaseOption("cg.spark", "string-constants:true");
			break;
			case VTA:
				Options.v().setPhaseOption("cg.spark", "on");
				Options.v().setPhaseOption("cg.spark", "vta:true");
				Options.v().setPhaseOption("cg.spark", "string-constants:true");
			break;
			case SPARK:
				Options.v().setPhaseOption("cg.spark", "on");
				Options.v().setPhaseOption("cg.spark", "string-constants:true");
				if (this.aliasingAlgorithm == AliasingAlgorithm.FlowSensitive)
				{
					// Options.v().setPhaseOption("cg.spark",
					// "types-for-sites:true");
				}
			break;
			case OnDemand:
			// nothing to set here
			break;
			default:
				throw new RuntimeException("Invalid callgraph algorithm");
		}
		// Specify additional options required for the callgraph
		if (callgraphAlgorithm != CallgraphAlgorithm.OnDemand)
		{
			Options.v().set_whole_program(true);
			Options.v().setPhaseOption("cg", "trim-clinit:false");
		}
		// do not merge variables (causes problems with PointsToSets)
		Options.v().setPhaseOption("jb.ulp", "off");
		if (!this.androidPath.isEmpty())
		{
			Options.v().set_src_prec(Options.src_prec_apk);
			if (this.forceAndroidJar)
				soot.options.Options.v().set_force_android_jar(this.androidPath);
			else
				soot.options.Options.v().set_android_jars(this.androidPath);
		}
		else
			Options.v().set_src_prec(Options.src_prec_java);
		// at the end of setting: load user settings:
		if (sootConfig != null)
			sootConfig.setSootOptions(Options.v());
		// load all entryPoint classes with their bodies
		for (String className : classes)
			Scene.v().addBasicClass(className, SootClass.BODIES);
		Scene.v().loadNecessaryClasses();
		logger.info("Basic class loading done.");
		boolean hasClasses = false;
		for (String className : classes)
		{
			SootClass c = Scene.v().forceResolve(className, SootClass.BODIES);
			if (c != null)
			{
				c.setApplicationClass();
				if (!c.isPhantomClass() && !c.isPhantom())
					hasClasses = true;
			}
		}
		if (!hasClasses)
		{
			logger.error("Only phantom classes loaded, skipping analysis...");
			return;
		}
	}
	/**
	 * Appends two elements to build a classpath
	 * 
	 * @param appPath
	 *            The first entry of the classpath
	 * @param libPath
	 *            The second entry of the classpath
	 * @return The concatenated classpath
	 */
	private String appendClasspath(String appPath, String libPath)
	{
		String s = (appPath != null && !appPath.isEmpty()) ? appPath : "";
		if (libPath != null && !libPath.isEmpty())
		{
			if (!s.isEmpty())
				s += File.pathSeparator;
			s += libPath;
		}
		return s;
	}
	@Override
	public void computeInfoflow(String appPath, String libPath, IEntryPointCreator entryPointCreator, ISourceSinkManager sourcesSinks)
	{
		if (sourcesSinks == null)
		{
			logger.error("Sources are empty!");
			return;
		}
		initializeSoot(appPath, libPath, entryPointCreator.getRequiredClasses());
		// entryPoints are the entryPoints required by Soot to calculate Graph -
		// if there is no main method,
		// we have to create a new main method and use it as entryPoint and
		// store our real entryPoints
		Scene.v().setEntryPoints(Collections.singletonList(entryPointCreator.createDummyMain()));
		// Run the analysis
		runAnalysis(sourcesSinks, null);
	}
	@Override
	public void computeInfoflow(String appPath, String libPath, String entryPoint, ISourceSinkManager sourcesSinks)
	{
		if (sourcesSinks == null)
		{
			logger.error("Sources are empty!");
			return;
		}
		long start = System.currentTimeMillis();
		initializeSoot(appPath, libPath, SootMethodRepresentationParser.v().parseClassNames(Collections.singletonList(entryPoint), false).keySet(), entryPoint);
		if (!Scene.v().containsMethod(entryPoint))
		{
			logger.error("Entry point not found: " + entryPoint);
			return;
		}
		SootMethod ep = Scene.v().getMethod(entryPoint);
		if (ep.isConcrete())
			ep.retrieveActiveBody();
		else
		{
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
		long end = System.currentTimeMillis();
		System.out.println("실행 시간 : " + (end - start) / 1000.0);
	}
	/**
	 * Conducts a taint analysis on an already initialized callgraph
	 * 
	 * @param sourcesSinks
	 *            The sources and sinks to be used
	 */
	protected void runAnalysis(final ISourceSinkManager sourcesSinks)
	{
		runAnalysis(sourcesSinks, null);
	}
	/**
	 * Conducts a taint analysis on an already initialized callgraph
	 * 
	 * @param sourcesSinks
	 *            The sources and sinks to be used
	 * @param additionalSeeds
	 *            Additional seeds at which to create A ZERO fact even if they are not sources
	 */
	private void runAnalysis(final ISourceSinkManager sourcesSinks, final Set<String> additionalSeeds)
	{
		//Finding Edge
		
		
		
		
		long start = System.currentTimeMillis();
		maxMemoryConsumption = -1;
		ipcManager.updateJimpleForICC();
		// Some configuration options do not really make sense in combination
		if (enableStaticFields && accessPathLength == 0)
			throw new RuntimeException("Static field tracking must be disabled " + "if the access path length is zero");
		if (accessPathLength < 0)
			throw new RuntimeException("The access path length may not be negative");
		// Clear the base registrations from previous runs
		AccessPath.clearBaseRegister();
		// Run the preprocessors
		for (PreAnalysisHandler tr : preProcessors)
			tr.onBeforeCallgraphConstruction();
		// Patch the system libraries we need for callgraph construction
		LibraryClassPatcher patcher = new LibraryClassPatcher();
		patcher.patchLibraries();
		// We explicitly select the packs we want to run for performance
		// reasons. Do not re-run the callgraph algorithm if the host
		// application already provides us with a CG.
		if (callgraphAlgorithm != CallgraphAlgorithm.OnDemand && !Scene.v().hasCallGraph())
		{
			PackManager.v().getPack("wjpp").apply();
			PackManager.v().getPack("cg").apply();
		}
		// JM this's temporary.
		// if (Constants.isCallGraphAnalysis)
		// {
		// ExtractWishPairInfo();
		// System.exit(1);
		// }
		// Run the preprocessors
		for (PreAnalysisHandler tr : preProcessors)
			tr.onAfterCallgraphConstruction();
		// Perform constant propagation and remove dead code
		if (codeEliminationMode != CodeEliminationMode.NoCodeElimination)
		{
			long currentMillis = System.nanoTime();
			// eliminateDeadCode(sourcesSinks);
			logger.info("Dead code elimination took " + (System.nanoTime() - currentMillis) / 1E9 + " seconds");
		}
		if (callgraphAlgorithm != CallgraphAlgorithm.OnDemand)
			logger.info("Callgraph has {} edges", Scene.v().getCallGraph().size());
		iCfg = icfgFactory.buildBiDirICFG(callgraphAlgorithm, enableExceptions);
		// int numThreads = Runtime.getRuntime().availableProcessors();
		int numThreads = 1;
		CountingThreadPoolExecutor executor = createExecutor(numThreads);
		// Initialize the memory manager
		IMemoryManager<Abstraction> memoryManager = new FlowDroidMemoryManager();
		BackwardsInfoflowProblem backProblem;
		InfoflowSolver backSolver;
		final IAliasingStrategy aliasingStrategy;
		switch (aliasingAlgorithm)
		{
			case FlowSensitive:
				backProblem = new BackwardsInfoflowProblem(new BackwardsInfoflowCFG(iCfg), sourcesSinks);
				// need to set this before creating the zero abstraction
				backProblem.setFlowSensitiveAliasing(flowSensitiveAliasing);
				backSolver = new InfoflowSolver(backProblem, executor);
				backSolver.setMemoryManager(memoryManager);
				backSolver.setJumpPredecessors(!pathBuilderFactory.supportsPathReconstruction());
				// backSolver.setEnableMergePointChecking(true);
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
		BackwardTaintingProblem backwardProblem = new BackwardTaintingProblem(new BackwardsInfoflowCFG(iCfg), sourcesSinks, aliasingStrategy);
		// need to set this before creating the zero abstraction
		backwardProblem.setFlowSensitiveAliasing(flowSensitiveAliasing);
		if (backProblem != null)
			backwardProblem.setZeroValue(backProblem.createZeroValue());
		// Set the options
		InfoflowSolver backwardSolver = new InfoflowSolver(backwardProblem, executor);
		aliasingStrategy.setForwardSolver(backwardSolver);
		backwardSolver.setMemoryManager(memoryManager);
		backwardSolver.setJumpPredecessors(!pathBuilderFactory.supportsPathReconstruction());
		backwardProblem.setInspectSources(inspectSources);
		backwardProblem.setInspectSinks(inspectSinks);
		backwardProblem.setEnableImplicitFlows(enableImplicitFlows);
		backwardProblem.setEnableStaticFieldTracking(enableStaticFields);
		backwardProblem.setEnableExceptionTracking(enableExceptions);
		for (TaintPropagationHandler tp : taintPropagationHandlers)
			backwardProblem.addTaintPropagationHandler(tp);
		backwardProblem.setTaintWrapper(taintWrapper);
		if (nativeCallHandler != null)
			backwardProblem.setNativeCallHandler(nativeCallHandler);
		backwardProblem.setStopAfterFirstFlow(stopAfterFirstFlow);
		backwardProblem.setEnableTypeChecking(enableTypeChecking);
		backwardProblem.setIgnoreFlowsInSystemPackages(ignoreFlowsInSystemPackages);
		// backwardProblem.setDebuggingFlowFunction(true);
		if (backProblem != null)
		{
			backProblem.setForwardSolver((InfoflowSolver) backwardSolver);
			backProblem.setTaintWrapper(taintWrapper);
			backProblem.setEnableStaticFieldTracking(enableStaticFields);
			backProblem.setEnableExceptionTracking(enableExceptions);
			for (TaintPropagationHandler tp : taintPropagationHandlers)
				backProblem.addTaintPropagationHandler(tp);
			backProblem.setTaintWrapper(taintWrapper);
			if (nativeCallHandler != null)
				backProblem.setNativeCallHandler(nativeCallHandler);
			backProblem.setActivationUnitsToCallSites(backwardProblem);
			backProblem.setEnableTypeChecking(enableTypeChecking);
			backProblem.setIgnoreFlowsInSystemPackages(ignoreFlowsInSystemPackages);
			backProblem.setInspectSources(inspectSources);
			backProblem.setInspectSinks(inspectSinks);
		}
		// Print our configuration
		if (!enableStaticFields)
			logger.warn("Static field tracking is disabled, results may be incomplete");
		if (!flowSensitiveAliasing || !aliasingStrategy.isFlowSensitive())
			logger.warn("Using flow-insensitive alias tracking, results may be imprecise");
		if (enableImplicitFlows)
			logger.info("Implicit flow tracking is enabled");
		else
			logger.info("Implicit flow tracking is NOT enabled");
		if (enableExceptions)
			logger.info("Exceptional flow tracking is enabled");
		else
			logger.info("Exceptional flow tracking is NOT enabled");
		logger.info("Running with a maximum access path length of {}", getAccessPathLength());
		if (pathAgnosticResults)
			logger.info("Using path-agnostic result collection");
		else
			logger.info("Using path-sensitive result collection");
		if (useRecursiveAccessPaths)
			logger.info("Recursive access path shortening is enabled");
		else
			logger.info("Recursive access path shortening is NOT enabled");
		// We have to look through the complete program to find sources
		// which are then taken as seeds.
		int sinkCount = 0;
		logger.info("Looking for sources and sinks...");
	
		// JM Edge Finding Test
		for (SootMethod sm : getMethodsForSeeds(iCfg))
		{
			// System.out.println("Seed : " + sm.getSignature());
			sinkCount += scanMethodForSourcesSinks(sourcesSinks, backwardProblem, sm);
		}
		// System.out.println("================================================");
		// We optionally also allow additional seeds to be specified
		if (additionalSeeds != null)
			for (String meth : additionalSeeds)
			{
				SootMethod m = Scene.v().getMethod(meth);
				if (!m.hasActiveBody())
				{
					logger.warn("Seed method {} has no active body", m);
					continue;
				}
				backwardProblem.addInitialSeeds(m.getActiveBody().getUnits().getFirst(), Collections.singleton(backwardProblem.zeroValue()));
			}
		
		// Report on the sources and sinks we have found
		if (!backwardProblem.hasInitialSeeds())
		{
			logger.error("No sources found, aborting analysis");
			// return;
		}
		if (sinkCount == 0)
		{
			logger.error("No sinks found, aborting analysis");
			// return;
		}

		//JM refactoring.
//		Map<Unit, Set<Abstraction>>  sources = backwardProblem.getInitialSeeds();
//		ArrayList<Unit> removecandidates = new ArrayList<Unit>();
//		
//		for (Unit key :  sources.keySet())
//		{
////			System.out.println(key);
//			
//			if (!key.toString().equals("$r3 = virtualinvoke $r4.<com.philips.lighting.hue.sdk.b.a.d: java.lang.String b(java.lang.String,java.lang.String)>($r7, $r3)"))
//				removecandidates.add(key);
//		}
//		
//		for (Unit u : removecandidates)
//			sources.remove(u);
		
		
		logger.info("Source lookup done, found {} sources and {} sinks.", backwardProblem.getInitialSeeds().size(), sinkCount);
		// Initialize the taint wrapper if we have one
		if (taintWrapper != null)
			taintWrapper.initialize(new InfoflowManager(backwardSolver, iCfg));
		backwardSolver.solve();
		maxMemoryConsumption = Math.max(maxMemoryConsumption, getUsedMemory());
		/**************************
		 * EXTRACTOCOL (Pairing)
		 **************************/
		/*
		 * List<AbstractSlice> asss = backwardProblem.getResults(); ReconstructHttpTransaction reconHttpTransacs = new ReconstructHttpTransaction(asss, true); //reconHttpTransacs.addPairingInfo(dp_stmt, dp_method, uri_or_body_signature, ep_stmt, ep_method);
		 * 
		 * // #1 reconHttpTransacs.addPairingInfo( "$r3 = virtualinvoke $r2.<java.net.URL: java.net.URLConnection openConnection()>()" , "<com.ted.android.core.data.helper.FeedHelper: java.io.InputStream getInputStream(java.lang.String)>" , "GET (https://app-api.ted.com)(/v1/playlists.json?fields=playlist_items,suggestions,attribution_data," + "duration_in_seconds,image,guest_curated,original_image&limit=50&offset=.....)" , "<java.lang.StringBuilder: void <init>()>", "<com.ted.android.core.data.helper.PlaylistHelper: void loadPlaylists(java.lang.String)>" );
		 * 
		 * // #2 reconHttpTransacs.addPairingInfo( "$r3 = virtualinvoke $r2.<java.net.URL: java.net.URLConnection openConnection()>()" , "<com.ted.android.core.data.helper.FeedHelper: java.io.InputStream getInputStream(java.lang.String)>" , "GET (https://app-api.ted.com)(/v1/video_prerolls/)((0-9)*)(.json?api-key=)(.*)" , "<java.lang.StringBuilder: void <init>()>", "<com.ted.android.core.data.helper.VideoPrerollHelper: com.ted.android.core.data.model.VideoPreroll loadVideoPreroll(int)>" );
		 * 
		 * // #3 reconHttpTransacs.addPairingInfo( "$r3 = virtualinvoke $r2.<java.net.URL: java.net.URLConnection openConnection()>()" , "<com.ted.android.core.data.helper.FeedHelper: java.io.InputStream getInputStream(java.lang.String)>" , "GET (https://app-api.ted.com)(/v1/talk_catalogs/android_v1.json?api-key=)()(&limit=)([0-9]*)(&offset=)([0-9]*)" + "(&externals=false&fields=duration_in_seconds&filter=)(.*)(:%3E)", "<java.lang.StringBuilder: void <init>()>", "<com.ted.android.data.helper.TalkHelper: com.ted.android.core.data.model.Talk updateStreamUrls(com.ted.android.core.data.model.Talk)>" );
		 * 
		 * //reconHttpTransacs.addPairingInfo("Demarcation Statement2", "Demarcation Method2", // "zemisolsol_signature_body", "kkkkk.", "zemisolsolsol_end_method"); reconHttpTransacs.print(); reconHttpTransacs.write(); reconHttpTransacs.makeSourcesAndSinks(); System.exit(0);
		 */
		/***************************************************************************/
		/**************************
		 * EXTRACTOCOL (Slicing)
		 **************************/
		/*
		 * List<AbstractSlice> assDebug = backwardProblem.getResults(); int assDebugCount = 1;
		 * 
		 * System.out.println(); for (AbstractSlice as : assDebug) { System.out.println ("\n("+assDebugCount+"/"+assDebug.size()+ ") Printing.."); System.out.println("+ DP Stmt : "+as.getDpStmt()); System.out.println("+ DP SootMethod : "+as.getDpMethod()); System.out.println("+ EP candidates ("+as.getEPs().size()+") :");
		 * 
		 * for (SootMethod sm : as.getEPs()) System.out.println("\t- "+sm);
		 * 
		 * System.out.println("+ all Methods : "); for (SootMethod sm : as.getMethods()) { System.out.println("\t- "+sm); }
		 * 
		 * System.out.println("+ all Slices : "); for (SootMethod sm1 : as.getAllSlices().keySet()) { System.out.println("\t- "+sm1); HashSet<Stmt> stmts = as.getAllSlices().get(sm1); for (Stmt stmt : stmts) { System.out.println("\t\t- "+stmt); } } assDebugCount++; } System.out.println(); System.exit(0);
		 */
		/***************************************************************************/
		/****************************** EXTRACTOCOL ******************************/
		long end = System.currentTimeMillis();
		System.out.println("실행 시간 : " + (end - start) / 1000.0);
		List<AbstractSlice> ass = backwardProblem.getResults();
		// WOOMADE
		List<EPcontainer> Epcontainerlist = new ArrayList<EPcontainer>();
		List<Unit> dPoints = new ArrayList<Unit>();
		int assCount = 1;
		System.out.println();
		for (AbstractSlice as : ass)
		{
			EPcontainer epcontainer = new EPcontainer();
			System.out.println("\n(" + assCount + "/" + ass.size() + ") Printing..");
			System.out.println("+ DP Stmt : " + as.getDpStmt());
			System.out.println("+ DP SootMethod : " + as.getDpMethod());
			dPoints.add(as.getDpStmt());
			epcontainer.setDP(as.getDpMethod());
			epcontainer.setDPStmt(as.getDpStmt());
			epcontainer.DemarcationInvoke = as.getDpStmt().getInvokeExpr().getMethod().getSubSignature();
			// System.out.println("+ EPs (" + as.getEPs().size() + ") :");
			List<String> Epointlist = new ArrayList<String>();
			List<SootMethod> listSM = new ArrayList<SootMethod>();
			Set<SootMethod> setSM = new HashSet<SootMethod>();
			// RemovingOverlapedEp(as.getEPs());
			for (SootMethod sm : as.getEPs())
			{
				Epointlist.add(sm.toString());
			}
			assCount++;
			//
			Hashtable<String, Set<SootMethod>> RefinedListcallflows = new Hashtable<String, Set<SootMethod>>();
			// print
			EntrypointFinder ep = new EntrypointFinder(dPoints, iCfg);
			for (String epoint : Epointlist)
			{
				Set<SootMethod> callflowset = new HashSet<SootMethod>();
				List<SootMethod> callflows = new ArrayList<SootMethod>();
				// if (epoint.contains("<com.tinder.managers.bp: void run()>"))
				// {
				// try
				// {
				// callflows = ep.findCallFlow(as.getDpMethod().toString(),
				// epoint);
				// } catch (NodeNotFoundException e)
				// {
				// e.printStackTrace();
				// }
				//
				// for (SootMethod callflow : callflows)
				// callflowset.add(callflow);
				//
				// try
				// {
				// BufferedWriter file = new BufferedWriter(new
				// FileWriter("callflow.txt", true));
				// file.newLine();
				// file.newLine();
				// file.newLine();
				// file.write(epoint +
				// "-------------------------------------------------------------");
				//
				// for (SootMethod sootmethods :
				// CallbackCandidateFinder.refineCallFlowForEachEp(Scene.v().getMethod(epoint),
				// as.getDpMethod(),
				// callflowset))
				// {
				// file.write(sootmethods.toString());
				// file.newLine();
				//
				// }
				//
				// file.close();
				//
				// } catch (IOException e)
				// {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				//
				// }
				RefinedListcallflows.put(epoint, new HashSet<SootMethod>(CallbackCandidateFinder.refineCallFlowForEachEp(Scene.v().getMethod(epoint), as.getDpMethod(), callflowset)));
			}
			Set<String> removedEps = new HashSet<String>();
			List<Set<SootMethod>> removedCFs = new ArrayList<Set<SootMethod>>();
			for (String epoint : Epointlist)
			{
				for (Set<SootMethod> set : RefinedListcallflows.values())
				{
					SootMethod sm = Scene.v().getMethod(epoint);
					Iterator<Edge> itrEdges = Scene.v().getCallGraph().edgesInto(sm);
					while (itrEdges.hasNext())
					{
						Edge ed = itrEdges.next();
						if (set.contains(ed.getSrc()))
						{
							removedEps.add(sm.getSignature());
						}
					}
				}
			}
			// Removing Duplicated EPs
			Epointlist.removeAll(removedEps);
			for (String a : removedEps)
				RefinedListcallflows.remove(a);
			System.out.println("+ EPs (" + Epointlist.size() + ") :");
			List<SootMethod> FinalEps = new ArrayList<SootMethod>();
			for (String epoint : Epointlist)
			{
				System.out.println("\t+" + epoint);
				if (Scene.v().getMethod(epoint) != null)
					FinalEps.add(Scene.v().getMethod(epoint));
			}
			epcontainer.setEPlist(FinalEps);
			System.out.println("+ Taint Methods : ");
			System.out.println("\t++ " + as.getDpStmt().getInvokeExpr().getMethod());
			setSM.add(as.getDpStmt().getInvokeExpr().getMethod());
			for (SootMethod sm : as.getMethods())
			{
				System.out.println("\t++ " + sm);
				setSM.add(sm);
			}
			if (Constants.GraphVisualization)
			{
				System.out.println("Graph Visualizing for this DP...");
				for (String epoint : Epointlist)
				{
					Set<SootMethod> callflowset = RefinedListcallflows.get(epoint);
					CallbackCandidateFinder.PrintEPtoDP(Scene.v().getMethod(epoint), as.getDpMethod(), callflowset, Constants.GraphPath());
				}
			}
			List<Set<SootMethod>> FinalCf = new ArrayList<Set<SootMethod>>();
			if (Constants.removeWrongEp)
				RemoveWrongEp(dPoints, Epointlist, as, epcontainer, setSM, FinalCf);
			else
				noRemoveWrongEp(dPoints, Epointlist, as, epcontainer, setSM, FinalCf);
			epcontainer.setMethodSet(setSM);
			epcontainer.setCallflow(FinalCf);
			Epcontainerlist.add(epcontainer);
		}
		// System.out.println();
		// System.exit(1);
		/*************************************************************************/
		// Unit test = null;
		// for (SootClass sc : Scene.v().getClasses()) {
		// for (SootMethod sm : sc.getMethods()) {
		//
		// if (sm.hasActiveBody() ) {
		// for (Unit unit : sm.retrieveActiveBody().getUnits())
		// if
		// (unit.toString().contains("$r0.<in.shick.diode.threads.ThreadsListActivity:
		// java.lang.String mSortByUrl> = $r1"))
		// {
		// test = unit;
		// }
		//
		// }
		// }
		// }
		//
		//
		// System.out.println("[*] Linear Search Results...");
		// List<AbstractSlice> linearResults = linearSearchForHeap(sourcesSinks,
		// test);
		//
		//
		// for (AbstractSlice linearAss : linearResults) {
		// List<SootMethod> linearEPs = linearAss.getEPs();
		// for (SootMethod ep : linearEPs) {
		// Collection<Unit> callers = iCfg.getCallersOf(ep);
		// for (Unit callerUnit : callers) {
		// Stmt callerStmt = (Stmt) callerUnit;
		// List<Value> args = callerStmt.getInvokeExpr().getArgs();
		// boolean hasConstantUri = false;
		// for (Value arg : args) {
		// if (arg instanceof Constant) {
		// hasConstantUri = true;
		// break;
		// }
		// }
		//
		// if (hasConstantUri) {
		// System.out.println("+ "+iCfg.getMethodOf(callerStmt));
		// System.out.println("\t- "+callerUnit);
		// }
		// else {
		// System.out.println("+ "+ep);
		// for (Stmt stmt : linearAss.getAllSlices().get(ep))
		// System.out.println("\t- "+stmt);
		// }
		// }
		// }
		// }
		/*************************************************************************/
		Constants.iCfg = iCfg;
		Constants.sourcesSinks = sourcesSinks;
		Constants.backendinfoflow = this;
		// iCfg.getCalleesOfCallAt(arg0)(sootm);
		// List<Unit> tgt =
		// CallbackCandidateFinder.SearchInvokeStmt("<org.wikipedia.ApiTask:
		// org.mediawiki.api.json.RequestBuilder
		// buildRequest(org.mediawiki.api.json.Api)>");
		// CallbackCandidateFinder.PrintEdges("<org.wikipedia.ApiTask:
		// java.lang.Object processResult(org.mediawiki.api.json.ApiResult)>");
		// CallbackCandidateFinder.PrintEdges("<org.wikipedia.ApiTask:
		// org.mediawiki.api.json.RequestBuilder
		// buildRequest(org.mediawiki.api.json.Api)>");
		// CallbackCandidateFinder.PrintEdges("<org.wikipedia.ApiTask:
		// java.lang.Object performTask()>");
		// TODO Start Backend!!
		if (Constants.Serializing)
		{
			try
			{
				System.out.println("Backward Serializing....");
				String upperDirPath = "";
				String splitPath[] = Constants.jimplePath().split("/");
				for (int i = 0; i < splitPath.length - 1; i++)
				{
					upperDirPath += splitPath[i] + "/";
				}
				File outputFile = new File(upperDirPath);
				outputFile.mkdir();
				CFGSerializer CFGs = new CFGSerializer(iCfg, Epcontainerlist);
				CFGs.Serialize(false);
				CFGs.SerializeEPC(false);
				System.out.println("Finished Serialization");
				if (Constants.Jimplify)
					// Write Jimple files
					System.out.println("write jimple start....");
				for (SootClass sc : Scene.v().getClasses())
				{
					try
					{
						WriteSootClass(sc);
					}
					catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println("Finish write jimple");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			System.out.println("exit");
			System.exit(1);
		}
		
		System.out.println("exit");
		System.exit(1);
		
		Backend_Worker bw = new Backend_Worker();
		try
		{
			bw.Start_BackEnd(Epcontainerlist, false);
			System.out.println("Backend Runtime : " + (end - start) / 1000.0);
			System.out.println("SIZE " + Scene.v().getCallGraph().size());
			ResultPrintHandler.urlResultPrint();
			List<AbstractSlice> asss = backwardProblem.getResults();
			ReconstructHttpTransaction reconHttpTransacs = new ReconstructHttpTransaction(asss, true);
			PairingHandler.urlPairingInfoHandler(reconHttpTransacs);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		// Not really nice, but sometimes Heros returns before all
		// executor tasks are actually done. This way, we give it a
		// chance to terminate gracefully before moving on.
		int terminateTries = 0;
		while (terminateTries < 10)
		{
			if (executor.getActiveCount() != 0 || !executor.isTerminated())
			{
				terminateTries++;
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e)
				{
					logger.error("Could not wait for executor termination", e);
				}
			}
			else
				break;
		}
		if (executor.getActiveCount() != 0 || !executor.isTerminated())
			logger.error("Executor did not terminate gracefully");
		// Print taint wrapper statistics
		if (taintWrapper != null)
		{
			logger.info("Taint wrapper hits: " + taintWrapper.getWrapperHits());
			logger.info("Taint wrapper misses: " + taintWrapper.getWrapperMisses());
		}
		logger.info("IFDS problem with {} backward edges solved. ", backwardSolver.propagationCount);
		// Force a cleanup. Everything we need is reachable through the
		// results set, the other abstractions can be killed now.
		maxMemoryConsumption = Math.max(maxMemoryConsumption, getUsedMemory());
		backwardSolver.cleanup();
		if (backSolver != null)
		{
			backSolver.cleanup();
			backSolver = null;
			backProblem = null;
		}
		backwardSolver = null;
		backwardProblem = null;
		Runtime.getRuntime().gc();
		for (ResultsAvailableHandler handler : onResultsAvailable)
			handler.onResultsAvailable(iCfg, results);
		if (logger.isDebugEnabled())
			PackManager.v().writeOutput();
		maxMemoryConsumption = Math.max(maxMemoryConsumption, getUsedMemory());
		System.out.println("Maximum memory consumption: " + maxMemoryConsumption / 1E6 + " MB");
	}
	
	public void noRemoveWrongEp(List<Unit> dPoints, List<String> Epointlist, AbstractSlice as, EPcontainer epcontainer, Set<SootMethod> setSM, List<Set<SootMethod>> FinalCf)
	{
		Hashtable<String, Set<SootMethod>> RefinedListcallflows = new Hashtable<String, Set<SootMethod>>();
		// print
		EntrypointFinder ep = new EntrypointFinder(dPoints, iCfg);
		for (String epoint : Epointlist)
		{
			Set<SootMethod> callflowset = new HashSet<SootMethod>();
			List<SootMethod> callflows = new ArrayList<SootMethod>();
			RefinedListcallflows.put(epoint, new HashSet<SootMethod>(CallbackCandidateFinder.refineCallFlowForEachEp(Scene.v().getMethod(epoint), as.getDpMethod(), callflowset)));
		}
		Set<String> removedEps = new HashSet<String>();
		List<Set<SootMethod>> removedCFs = new ArrayList<Set<SootMethod>>();
		for (String epoint : Epointlist)
		{
			for (Set<SootMethod> set : RefinedListcallflows.values())
			{
				SootMethod sm = Scene.v().getMethod(epoint);
				Iterator<Edge> itrEdges = Scene.v().getCallGraph().edgesInto(sm);
				while (itrEdges.hasNext())
				{
					Edge ed = itrEdges.next();
					if (set.contains(ed.getSrc()))
					{
						removedEps.add(sm.getSignature());
					}
				}
			}
		}
		// Removing Duplicated EPs
		Epointlist.removeAll(removedEps);
		for (String a : removedEps)
			RefinedListcallflows.remove(a);
		System.out.println("+ EPs (" + Epointlist.size() + ") :");
		List<SootMethod> FinalEps = new ArrayList<SootMethod>();
		for (String epoint : Epointlist)
		{
			System.out.println("\t+" + epoint);
			if (Scene.v().getMethod(epoint) != null)
				FinalEps.add(Scene.v().getMethod(epoint));
		}
		epcontainer.setEPlist(FinalEps);
		System.out.println("+ Taint Methods : ");
		System.out.println("\t++ " + as.getDpStmt().getInvokeExpr().getMethod());
		setSM.add(as.getDpStmt().getInvokeExpr().getMethod());
		for (SootMethod sm : as.getMethods())
		{
			System.out.println("\t++ " + sm);
			setSM.add(sm);
		}
		if (Constants.GraphVisualization)
		{
			System.out.println("Graph Visualizing for this DP...");
			for (String epoint : Epointlist)
			{
				Set<SootMethod> callflowset = RefinedListcallflows.get(epoint);
				CallbackCandidateFinder.PrintEPtoDP(Scene.v().getMethod(epoint), as.getDpMethod(), callflowset, Constants.GraphPath());
			}
		}
		for (String ep1 : Epointlist)
			FinalCf.add(RefinedListcallflows.get(ep1));
	}
	public void RemoveWrongEp(List<Unit> dPoints, List<String> Epointlist, AbstractSlice as, EPcontainer epcontainer, Set<SootMethod> setSM, List<Set<SootMethod>> finalCf)
	{
		Hashtable<String, Set<SootMethod>> RefinedListcallflows = new Hashtable<String, Set<SootMethod>>();
		// print
		EntrypointFinder ep = new EntrypointFinder(dPoints, iCfg);
		for (String epoint : Epointlist)
		{
			Set<SootMethod> callflowset = new HashSet<SootMethod>();
			List<SootMethod> callflows = new ArrayList<SootMethod>();
			try
			{
				callflows = ep.findCallFlow(as.getDpMethod().toString(), epoint);
			}
			catch (NodeNotFoundException e)
			{
				e.printStackTrace();
			}
			for (SootMethod callflow : callflows)
				callflowset.add(callflow);
			RefinedListcallflows.put(epoint, new HashSet<SootMethod>(CallbackCandidateFinder.refineCallFlowForEachEp(Scene.v().getMethod(epoint), as.getDpMethod(), callflowset)));
		}
		Set<String> removedEps = new HashSet<String>();
		List<Set<SootMethod>> removedCFs = new ArrayList<Set<SootMethod>>();
		for (String epoint : Epointlist)
		{
			for (Set<SootMethod> set : RefinedListcallflows.values())
			{
				SootMethod sm = Scene.v().getMethod(epoint);
				Iterator<Edge> itrEdges = Scene.v().getCallGraph().edgesInto(sm);
				while (itrEdges.hasNext())
				{
					Edge ed = itrEdges.next();
					if (set.contains(ed.getSrc()))
					{
						removedEps.add(sm.getSignature());
					}
				}
			}
		}
		// Removing Duplicated EPs
		Epointlist.removeAll(removedEps);
		for (String a : removedEps)
			RefinedListcallflows.remove(a);
		List<SootMethod> FinalEps = new ArrayList<SootMethod>();
		for (String epoint : Epointlist)
		{
			if (Scene.v().getMethod(epoint) != null && RefinedListcallflows.get(epoint).contains(as.getDpMethod()))
				FinalEps.add(Scene.v().getMethod(epoint));
		}
		epcontainer.setEPlist(FinalEps);
		System.out.println("+ EPs (" + FinalEps.size() + ") :");
		removedEps.clear();
		// JM remove wrong Eps
		for (String finalsm : Epointlist)
		{
			if (!FinalEps.contains(Scene.v().getMethod(finalsm)))
			{
				removedEps.add(finalsm);
			}
			else
			{
				System.out.println("\t+" + finalsm);
			}
		}
		Epointlist.removeAll(removedEps);
		System.out.println("+ Taint Methods : ");
		System.out.println("\t++ " + as.getDpStmt().getInvokeExpr().getMethod());
		setSM.add(as.getDpStmt().getInvokeExpr().getMethod());
		for (SootMethod sm : as.getMethods())
		{
			System.out.println("\t++ " + sm);
			setSM.add(sm);
		}
		if (Constants.GraphVisualization)
		{
			System.out.println("Graph Visualizing for this DP...");
			for (String epoint : Epointlist)
			{
				Set<SootMethod> callflowset = RefinedListcallflows.get(epoint);
				CallbackCandidateFinder.PrintEPtoDP(Scene.v().getMethod(epoint), as.getDpMethod(), callflowset, Constants.GraphPath());
			}
		}
		for (String ep1 : Epointlist)
			finalCf.add(RefinedListcallflows.get(ep1));
	}
	public List<AbstractSlice> linearSearchForHeap(final ISourceSinkManager sourcesSinks, final Unit additionalSeeds)
	{
		maxMemoryConsumption = -1;
		ipcManager.updateJimpleForICC();
		// Clear the base registrations from previous runs
		AccessPath.clearBaseRegister();
		// Run the preprocessors
		for (PreAnalysisHandler tr : preProcessors)
			tr.onBeforeCallgraphConstruction();
		// Patch the system libraries we need for callgraph construction
		LibraryClassPatcher patcher = new LibraryClassPatcher();
		patcher.patchLibraries();
		// We explicitly select the packs we want to run for performance
		// reasons. Do not re-run the callgraph algorithm if the host
		// application already provides us with a CG.
		if (callgraphAlgorithm != CallgraphAlgorithm.OnDemand && !Scene.v().hasCallGraph())
		{
			PackManager.v().getPack("wjpp").apply();
			PackManager.v().getPack("cg").apply();
		}
		// Run the preprocessors
		for (PreAnalysisHandler tr : preProcessors)
			tr.onAfterCallgraphConstruction();
		iCfg = icfgFactory.buildBiDirICFG(callgraphAlgorithm, enableExceptions);
		// int numThreads = Runtime.getRuntime().availableProcessors();
		int numThreads = 1;
		CountingThreadPoolExecutor executor = createExecutor(numThreads);
		// Initialize the memory manager
		IMemoryManager<Abstraction> memoryManager = new FlowDroidMemoryManager();
		final IAliasingStrategy aliasingStrategy;
		aliasingStrategy = new PtsBasedAliasStrategy(iCfg);
		BackwardTaintingProblemForHeap backwardProblem = new BackwardTaintingProblemForHeap(new BackwardsInfoflowCFG(iCfg), sourcesSinks, aliasingStrategy, additionalSeeds);
		// need to set this before creating the zero abstraction
		backwardProblem.setFlowSensitiveAliasing(flowSensitiveAliasing);
		// Set the options
		InfoflowSolver backwardSolver = new InfoflowSolver(backwardProblem, executor);
		aliasingStrategy.setForwardSolver(backwardSolver);
		backwardSolver.setMemoryManager(memoryManager);
		backwardSolver.setJumpPredecessors(!pathBuilderFactory.supportsPathReconstruction());
		backwardProblem.setInspectSources(inspectSources);
		backwardProblem.setInspectSinks(inspectSinks);
		backwardProblem.setEnableImplicitFlows(enableImplicitFlows);
		backwardProblem.setEnableStaticFieldTracking(enableStaticFields);
		backwardProblem.setEnableExceptionTracking(enableExceptions);
		for (TaintPropagationHandler tp : taintPropagationHandlers)
			backwardProblem.addTaintPropagationHandler(tp);
		backwardProblem.setTaintWrapper(taintWrapper);
		if (nativeCallHandler != null)
			backwardProblem.setNativeCallHandler(nativeCallHandler);
		backwardProblem.setStopAfterFirstFlow(stopAfterFirstFlow);
		backwardProblem.setEnableTypeChecking(enableTypeChecking);
		backwardProblem.setIgnoreFlowsInSystemPackages(ignoreFlowsInSystemPackages);
		// backwardProblem.setDebuggingFlowFunction(true);
		// We have to look through the complete program to find sources
		// which are then taken as seeds.
		backwardProblem.getInitialSeeds().clear();
		// We optionally also allow additional seeds to be specified
		if (additionalSeeds != null)
			backwardProblem.addInitialSeeds(additionalSeeds, Collections.singleton(backwardProblem.zeroValue()));
		System.out.println("[*] LinearSearching at " + additionalSeeds);
		// Initialize the taint wrapper if we have one
		if (taintWrapper != null)
			taintWrapper.initialize(new InfoflowManager(backwardSolver, iCfg));
		backwardSolver.solve();
		/********************
		 * EXTRACTOCOL (Linear Searching)
		 ********************/
		/*
		 * Use below codes only for debugging List<AbstractSlice> ass = backwardProblem.getResults(); int assCount = 1; System.out.println(); for (AbstractSlice as : ass) { System.out.println("\n("+assCount+"/"+ass.size()+") Printing.."); System.out.println("+ DP Stmt : "+as.getDpStmt()); System.out.println("+ DP SootMethod : "+as.getDpMethod());
		 * 
		 * System.out.println("+ EP candidates ("+as.getEPs().size()+") :"); for (SootMethod sm : as.getEPs()) System.out.println("\t- "+sm);
		 * 
		 * System.out.println("+ all Methods : "); for (SootMethod sm : as.getMethods()) System.out.println("\t- "+sm);
		 * 
		 * System.out.println("+ all Slices : "); for (SootMethod sm : as.getAllSlices().keySet()) { System.out.println("\t- "+sm); HashSet<Stmt> stmts = as.getAllSlices().get(sm); for (Stmt stmt : stmts) System.out.println("\t\t- "+stmt); }
		 * 
		 * assCount++; } System.out.println();
		 */
		/*************************************************************************/
		// Not really nice, but sometimes Heros returns before all
		// executor tasks are actually done. This way, we give it a
		// chance to terminate gracefully before moving on.
		int terminateTries = 0;
		while (terminateTries < 10)
		{
			if (executor.getActiveCount() != 0 || !executor.isTerminated())
			{
				terminateTries++;
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e)
				{
					logger.error("Could not wait for executor termination", e);
				}
			}
			else
				break;
		}
		if (executor.getActiveCount() != 0 || !executor.isTerminated())
			logger.error("Executor did not terminate gracefully");
		System.out.println("[*] for linear searching, IFDS problem with " + backwardSolver.propagationCount + " backward edges solved. ");
		System.out.println();
		// Force a cleanup. Everything we need is reachable through the
		// results set, the other abstractions can be killed now.
		backwardSolver.cleanup();
		Runtime.getRuntime().gc();
		for (ResultsAvailableHandler handler : onResultsAvailable)
			handler.onResultsAvailable(iCfg, results);
		return backwardProblem.getResults();
	}
	/**
	 * Gets the memory used by FlowDroid at the moment
	 * 
	 * @return FlowDroid's current memory consumption in bytes
	 */
	private long getUsedMemory()
	{
		Runtime runtime = Runtime.getRuntime();
		return runtime.totalMemory() - runtime.freeMemory();
	}
	/**
	 * Performs an interprocedural dead-code elimination on all application classes
	 * 
	 * @param sourcesSinks
	 *            The SourceSinkManager to make sure that sources remain intact during constant propagation
	 */
	private void eliminateDeadCode(ISourceSinkManager sourcesSinks)
	{
		// Perform an intra-procedural constant propagation to prepare for the
		// inter-procedural one
		for (QueueReader<MethodOrMethodContext> rdr = Scene.v().getReachableMethods().listener(); rdr.hasNext();)
		{
			MethodOrMethodContext sm = rdr.next();
			if (sm.method() == null || !sm.method().hasActiveBody())
				continue;
			// Exclude the dummy main method
			if (Scene.v().getEntryPoints().contains(sm.method()))
				continue;
			List<Unit> callSites = getCallsInMethod(sm.method());
			ConstantPropagatorAndFolder.v().transform(sm.method().getActiveBody());
			DeadAssignmentEliminator.v().transform(sm.method().getActiveBody());
			// Remove the dead callgraph edges
			List<Unit> newCallSites = getCallsInMethod(sm.method());
			if (callSites != null)
				for (Unit u : callSites)
					if (newCallSites == null || !newCallSites.contains(u))
					{
						Scene.v().getCallGraph().removeAllEdgesOutOf(u);
					}
		}
		// Perform an inter-procedural constant propagation and code cleanup
		InterproceduralConstantValuePropagator ipcvp = new InterproceduralConstantValuePropagator(new InfoflowCFG(), Scene.v().getEntryPoints(), sourcesSinks, taintWrapper);
		ipcvp.setRemoveSideEffectFreeMethods(codeEliminationMode == CodeEliminationMode.RemoveSideEffectFreeCode && !enableImplicitFlows);
		ipcvp.transform();
		// Get rid of all dead code
		for (QueueReader<MethodOrMethodContext> rdr = Scene.v().getReachableMethods().listener(); rdr.hasNext();)
		{
			MethodOrMethodContext sm = rdr.next();
			if (sm.method() == null || !sm.method().hasActiveBody())
				continue;
			if (SystemClassHandler.isClassInSystemPackage(sm.method().getDeclaringClass().getName()))
				continue;
			ConditionalBranchFolder.v().transform(sm.method().getActiveBody());
			// Delete all dead code. We need to be careful and patch the cfg so
			// that it does not retain edges for call statements we have deleted
			List<Unit> callSites = getCallsInMethod(sm.method());
			UnreachableCodeEliminator.v().transform(sm.method().getActiveBody());
			List<Unit> newCallSites = getCallsInMethod(sm.method());
			if (callSites != null)
				for (Unit u : callSites)
					if (newCallSites == null || !newCallSites.contains(u))
					{
						Scene.v().getCallGraph().removeAllEdgesOutOf(u);
					}
		}
	}
	/**
	 * Gets a list of all units that invoke other methods in the given method
	 * 
	 * @param method
	 *            The method from which to get all invocations
	 * @return The list of units calling other methods in the given method if there is at least one such unit. Otherwise null.
	 */
	private List<Unit> getCallsInMethod(SootMethod method)
	{
		List<Unit> callSites = null;
		for (Unit u : method.getActiveBody().getUnits())
			if (((Stmt) u).containsInvokeExpr())
			{
				if (callSites == null)
					callSites = new ArrayList<Unit>();
				callSites.add(u);
			}
		return callSites;
	}
	/**
	 * Creates a new executor object for spawning worker threads
	 * 
	 * @param numThreads
	 *            The number of threads to use
	 * @return The generated executor
	 */
	private CountingThreadPoolExecutor createExecutor(int numThreads)
	{
		return new CountingThreadPoolExecutor(maxThreadNum == -1 ? numThreads : Math.min(maxThreadNum, numThreads), Integer.MAX_VALUE, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}
	private Collection<SootMethod> getMethodsForSeeds(IInfoflowCFG icfg)
	{
		List<SootMethod> seeds = new LinkedList<SootMethod>();
		// If we have a callgraph, we retrieve the reachable methods. Otherwise,
		// we have no choice but take all application methods as an
		// approximation
		if (Scene.v().hasCallGraph())
		{
			List<MethodOrMethodContext> eps = new ArrayList<MethodOrMethodContext>(Scene.v().getEntryPoints());
			ReachableMethods reachableMethods = new ReachableMethods(Scene.v().getCallGraph(), eps.iterator(), null);
			// JM this Loop is temporary. by Jeongmin
			// JM All of Source and sink add to seed set.
			for (SootClass sc : Scene.v().getClasses())
			{
				// if (sc.isConcrete()) {
				for (SootMethod sm : sc.getMethods())
				{
					if (sm.isConcrete())
					{
						seeds.add(sm);
					}
				}
				// }
			}
			reachableMethods.update();
			// FIXME I think that this loop assign seed methods which are all of
			// reachable methods.
			// for (Iterator<MethodOrMethodContext> iter =
			// reachableMethods.listener(); iter.hasNext();) {
			// SootMethod sm = iter.next().method();
			// seeds.add(sm);
			// }
			//
			// reachableMethods.update();
		}
		else
		{
			long beforeSeedMethods = System.nanoTime();
			Set<SootMethod> doneSet = new HashSet<SootMethod>();
			// for (SootMethod sm : Scene.v().getEntryPoints())
			// getMethodsForSeedsIncremental(sm, doneSet, seeds, icfg);
			for (SootClass sc : Scene.v().getClasses())
			{
				if (sc.isConcrete())
				{
					for (SootMethod sm : sc.getMethods())
					{
						if (sm.isConcrete())
						{
							seeds.add(sm);
						}
					}
				}
			}
			logger.info("Collecting seed methods took {} seconds", (System.nanoTime() - beforeSeedMethods) / 1E9);
		}
		return seeds;
	}
	private void getMethodsForSeedsIncremental(SootMethod sm, Set<SootMethod> doneSet, List<SootMethod> seeds, IInfoflowCFG icfg)
	{
		assert Scene.v().hasFastHierarchy();
		if (!sm.isConcrete() || !sm.getDeclaringClass().isApplicationClass() || !doneSet.add(sm))
			return;
		seeds.add(sm);
		for (Unit u : sm.retrieveActiveBody().getUnits())
		{
			Stmt stmt = (Stmt) u;
			if (stmt.containsInvokeExpr())
				for (SootMethod callee : icfg.getCalleesOfCallAt(stmt))
					getMethodsForSeedsIncremental(callee, doneSet, seeds, icfg);
		}
	}
	/**
	 * Scans the given method for sources and sinks contained in it. Sinks are just counted, sources are added to the InfoflowProblem as seeds.
	 * 
	 * @param sourcesSinks
	 *            The SourceSinkManager to be used for identifying sources and sinks
	 * @param backwardProblem
	 *            The InfoflowProblem in which to register the sources as seeds
	 * @param m
	 *            The method to scan for sources and sinks
	 * @return The number of sinks found in this method
	 */
	private int scanMethodForSourcesSinks(final ISourceSinkManager sourcesSinks, BackwardTaintingProblem backwardProblem, SootMethod m)
	{
		int sinkCount = 0;
		if (m.hasActiveBody())
		{
			// Check whether this is a system class we need to ignore
			final String className = m.getDeclaringClass().getName();
			if (ignoreFlowsInSystemPackages && SystemClassHandler.isClassInSystemPackage(className))
				return sinkCount;
			// Look for a source in the method. Also look for sinks. If we
			// have no sink in the program, we don't need to perform any
			// analysis
			PatchingChain<Unit> units = m.getActiveBody().getUnits();
			for (Unit u : units)
			{
				Stmt s = (Stmt) u;
				if (!s.containsInvokeExpr())
					continue;
				if (sourcesSinks.getSourceInfo(s, iCfg) != null)
				{
					// if (iCfg.getMethodOf(u).toString().equals(""
					// + "<com.ted.android.core.data.helper.FeedHelper:
					// java.io.InputStream getInputStream(java.lang.String)>"))
					// {
					backwardProblem.addInitialSeeds(u, Collections.singleton(backwardProblem.zeroValue()));
					logger.debug("Source found: {}", u);
					// }
				}
				if (sourcesSinks.isSink(s, iCfg, null))
				{
					logger.debug("Sink found: {}", u);
					sinkCount++;
				}
			}
		}
		return sinkCount;
	}
	@Override
	public InfoflowResults getResults()
	{
		return results;
	}
	@Override
	public boolean isResultAvailable()
	{
		if (results == null)
		{
			return false;
		}
		return true;
	}
	public static int getAccessPathLength()
	{
		return accessPathLength;
	}
	/**
	 * Sets the maximum depth of the access paths. All paths will be truncated if they exceed the given size.
	 * 
	 * @param accessPathLength
	 *            the maximum value of an access path. If it gets longer than this value, it is truncated and all following fields are assumed as tainted (which is imprecise but gains performance) Default value is 5.
	 */
	public static void setAccessPathLength(int accessPathLength)
	{
		BackwardInfoflow.accessPathLength = accessPathLength;
	}
	/**
	 * Sets whether results (source-to-sink connections) that only differ in their propagation paths shall be merged into a single result or not.
	 * 
	 * @param pathAgnosticResults
	 *            True if two results shall be regarded as equal if they connect the same source and sink, even if their propagation paths differ, otherwise false
	 */
	public static void setPathAgnosticResults(boolean pathAgnosticResults)
	{
		BackwardInfoflow.pathAgnosticResults = pathAgnosticResults;
	}
	/**
	 * Gets whether results (source-to-sink connections) that only differ in their propagation paths shall be merged into a single result or not.
	 * 
	 * @return True if two results shall be regarded as equal if they connect the same source and sink, even if their propagation paths differ, otherwise false
	 */
	public static boolean getPathAgnosticResults()
	{
		return BackwardInfoflow.pathAgnosticResults;
	}
	/**
	 * Gets whether recursive access paths shall be reduced, e.g. whether we shall propagate a.[next].data instead of a.next.next.data.
	 * 
	 * @return True if recursive access paths shall be reduced, otherwise false
	 */
	public static boolean getUseRecursiveAccessPaths()
	{
		return useRecursiveAccessPaths;
	}
	/**
	 * Sets whether recursive access paths shall be reduced, e.g. whether we shall propagate a.[next].data instead of a.next.next.data.
	 * 
	 * @param useRecursiveAccessPaths
	 *            True if recursive access paths shall be reduced, otherwise false
	 */
	public static void setUseRecursiveAccessPaths(boolean useRecursiveAccessPaths)
	{
		BackwardInfoflow.useRecursiveAccessPaths = useRecursiveAccessPaths;
	}
	/**
	 * Gets whether different results shall be reported if they only differ in the access path the reached the sink or left the source
	 * 
	 * @return True if results shall also be distinguished based on access paths
	 */
	public static boolean getOneResultPerAccessPath()
	{
		return oneResultPerAccessPath;
	}
	/**
	 * Gets whether different results shall be reported if they only differ in the access path the reached the sink or left the source
	 * 
	 * @param oneResultPerAP
	 *            True if results shall also be distinguished based on access paths
	 */
	public static void setOneResultPerAccessPath(boolean oneResultPerAP)
	{
		oneResultPerAccessPath = oneResultPerAP;
	}
	/**
	 * Adds a handler that is called when information flow results are available
	 * 
	 * @param handler
	 *            The handler to add
	 */
	public void addResultsAvailableHandler(ResultsAvailableHandler handler)
	{
		this.onResultsAvailable.add(handler);
	}
	/**
	 * Gets whether neighbors at the same statement shall be merged into a single abstraction
	 * 
	 * @return True if equivalent neighbor shall be merged, otherwise false
	 */
	public static boolean getMergeNeighbors()
	{
		return mergeNeighbors;
	}
	/**
	 * Sets whether neighbors at the same statement shall be merged into a single abstraction
	 * 
	 * @param value
	 *            True if equivalent neighbor shall be merged, otherwise false
	 */
	public static void setMergeNeighbors(boolean value)
	{
		mergeNeighbors = value;
	}
	/**
	 * Adds a handler which is invoked whenever a taint is propagated
	 * 
	 * @param handler
	 *            The handler to be invoked when propagating taints
	 */
	public void addTaintPropagationHandler(TaintPropagationHandler handler)
	{
		this.taintPropagationHandlers.add(handler);
	}
	/**
	 * Removes a handler that is called when information flow results are available
	 * 
	 * @param handler
	 *            The handler to remove
	 */
	public void removeResultsAvailableHandler(ResultsAvailableHandler handler)
	{
		onResultsAvailable.remove(handler);
	}
	@Override
	public void setIPCManager(IIPCManager ipcManager)
	{
		this.ipcManager = ipcManager;
	}
	/**
	 * Gets the maximum memory consumption during the last analysis run
	 * 
	 * @return The maximum memory consumption during the last analysis run if available, otherwise -1
	 */
	public long getMaxMemoryConsumption()
	{
		return this.maxMemoryConsumption;
	}
	/**
	 * Sets the path builder factory to be used in subsequent data flow analyses
	 * 
	 * @param factory
	 *            The path bilder factory to use for constructing path reconstruction algorithms
	 */
	public void setPathBuilderFactory(IPathBuilderFactory factory)
	{
		this.pathBuilderFactory = factory;
	}
	private void WriteSootClass(SootClass sc) throws IOException
	{
		File outputFile = new File(Constants.jimplePath());
		outputFile.mkdir();
		String fileName = Constants.jimplePath() + "/" + sc.getName() + ".jimple";
		try
		{
			OutputStream streamOut = new FileOutputStream(fileName);
			PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
			Printer.v().printTo(sc, writerOut, true);
			writerOut.flush();
			streamOut.close();
		}
		catch (Exception e)
		{}
	}
	
	private boolean getSources(BuildPairJson PairInfo, JSONObject responseEntry, SootClass onSuccessIncludeClass)
	{
		// JM Auto-generated method stub
		SootMethod onSuccessSm = null;
		for (SootMethod sm : onSuccessIncludeClass.getMethods())
		{
			if (sm.getName().equals("onSuccess"))
			{
				onSuccessSm = sm;
				break;
			}
		}
		SootClass targetClass = null;
		if (!onSuccessSm.hasActiveBody())
			return false;
		for (Unit ut : onSuccessSm.getActiveBody().getUnits())
		{
			if (ut.toString().contains("com.contextlogic.wish.api.data") && ut instanceof InvokeStmt)
			{
				InvokeStmt is = (InvokeStmt) ut;
				targetClass = is.getInvokeExpr().getMethod().getDeclaringClass();
				break;
			}
		}
		if (targetClass == null)
		{
			return false;
		}
		else
		{
			JSONArray stmts = new JSONArray();
			PairInfo.Add_ep_stmts(responseEntry, stmts);
			for (SootMethod sm : targetClass.getMethods())
			{
				if (sm.getName().startsWith("get"))
				{
					JSONObject stmt = new JSONObject();
					stmt.put("stmt", sm.getSignature());
					stmts.add(stmt);
				}
			}
		}
		return true;
	}
	private String FindResponseEp(SootMethod sm, String Callsig)
	{
		for (Unit ut : sm.getActiveBody().getUnits())
		{
			if (ut instanceof AssignStmt)
			{
				AssignStmt as = (AssignStmt) ut;
				if (as.containsInvokeExpr())
					if (as.getInvokeExpr().toString().contains(Callsig))
					{
						SootClass sc = Scene.v().getSootClass(as.getInvokeExpr().getArg(as.getInvokeExpr().getArgCount() - 1).getType().toString());
						for (SootMethod reshandler : sc.getMethods())
						{
							if (reshandler.getName().equals("onSuccess"))
								return reshandler.getSignature();
						}
					}
			}
		}
		return null;
	}
	private String UrlBuild(SootMethod sm)
	{
		for (Unit ut : sm.getActiveBody().getUnits())
		{
			if (ut instanceof AssignStmt)
			{
				if (ut.toString().contains("apiRequest"))
				{
					AssignStmt as = (AssignStmt) ut;
					return as.getInvokeExpr().getArg(0).toString().replaceAll("\"", "");
				}
			}
		}
		return null;
	}
	public static void ParsingEPs(ArrayList<String> EPs, ArrayList<String> DPstmts)
	{
		String EPPath = Constants.GetForwardEP_path();
		BufferedReader in = null;
		try
		{
			in = new BufferedReader(new FileReader(EPPath));
		}
		catch (FileNotFoundException e)
		{
			// JM Auto-generated catch block
			e.printStackTrace();
		}
		String s;
		try
		{
			while ((s = in.readLine()) != null)
			{
				String tok[] = s.split("####");
				EPs.add(tok[0].substring(1, tok[0].indexOf(":")));
				DPstmts.add(tok[1]);
			}
		}
		catch (IOException e1)
		{
			// JM Auto-generated catch block
			e1.printStackTrace();
		}
		try
		{
			in.close();
		}
		catch (IOException e)
		{
			// JM Auto-generated catch block
			e.printStackTrace();
		}
		if (EPs.size() == 0)
		{
			System.out.println("EP Parsing fail");
			System.exit(1);
		}
	}
}
