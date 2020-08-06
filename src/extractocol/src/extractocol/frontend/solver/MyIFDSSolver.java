/*******************************************************************************
 * Copyright (c) 2012 Eric Bodden.
 * Copyright (c) 2013 Tata Consultancy Services & Ecole Polytechnique de Montreal
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Eric Bodden - initial API and implementation
 *     Marc-Andre Laverdiere-Papineau - Fixed race condition
 *     Steven Arzt - Created FastSolver implementation
 ******************************************************************************/
package extractocol.frontend.solver;


import extractocol.Constants;
import extractocol.frontend.output.TaintResultContainer;
import extractocol.frontend.output.basic.TaintInfo;
import heros.DontSynchronize;
import heros.FlowFunction;
import heros.FlowFunctionCache;
import heros.FlowFunctions;
import heros.IFDSTabulationProblem;
import heros.SynchronizedBy;
import heros.ZeroedFlowFunctions;
import heros.solver.CountingThreadPoolExecutor;
import heros.solver.Pair;
import heros.solver.PathEdge;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.NullType;
import soot.jimple.Constant;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.collect.ConcurrentHashSet;
import soot.jimple.infoflow.collect.MyConcurrentHashMap;
import soot.jimple.infoflow.solver.IMemoryManager;
import soot.jimple.infoflow.solver.fastSolver.FastSolverLinkedNode;
import soot.jimple.infoflow.solver.fastSolver.JumpFunctions;
import soot.jimple.infoflow.solver.fastSolver.SetPoolExecutor;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import com.google.common.cache.CacheBuilder;

import extractocol.frontend.TransitionStrategy.ToForwardTransition;
import extractocol.frontend.basic.AbstractionExtension;
import extractocol.frontend.basic.ExtractocolLogger;
import extractocol.frontend.basic.MyIncomingMyEndsummary;
import extractocol.frontend.helper.PropagateHelper;
import extractocol.frontend.helper.PropagateHelper.MyEdgeEntry;
import extractocol.frontend.helper.PropagateHelper.PROPA_TYPE;
import extractocol.frontend.helper.StubMethodHandler;
import extractocol.frontend.output.InitialTaintResultContainer;
import extractocol.frontend.output.basic.TaintResultEntry;
import extractocol.frontend.problem.MyBackwardsInfoflowProblem;
import extractocol.frontend.problem.MyInfoflowProblem;
import soot.jimple.infoflow.data.Abstraction;

/**
 * A solver for an {@link IFDSTabulationProblem}. This solver is not based on the IDESolver
 * implementation in Heros for performance reasons.
 * 
 * @param <N> The type of nodes in the interprocedural control-flow graph. Typically {@link Unit}.
 * @param <D> The type of data-flow facts to be computed by the tabulation problem.
 * @param <M> The type of objects used to represent methods. Typically {@link SootMethod}.
 * @param <I> The type of inter-procedural control-flow graph being used.
 * @see IFDSTabulationProblem
 */
public class
MyIFDSSolver<N,D extends FastSolverLinkedNode<D, N>,M,I extends BiDiInterproceduralCFG<N, M>> {
	
	public static CacheBuilder<Object, Object> DEFAULT_CACHE_BUILDER = CacheBuilder.newBuilder().concurrencyLevel
			(Runtime.getRuntime().availableProcessors()).initialCapacity(10000).softValues();
	
    protected static final Logger logger = LoggerFactory.getLogger(MyIFDSSolver.class);

    //enable with -Dorg.slf4j.simpleLogger.defaultLogLevel=trace
    public static final boolean DEBUG = logger.isDebugEnabled();

	protected CountingThreadPoolExecutor executor;
	
	@DontSynchronize("only used by single thread")
	protected int numThreads;
	
	@SynchronizedBy("thread safe data structure, consistent locking when used")
	protected final JumpFunctions<N,D> jumpFn;
	
	@SynchronizedBy("thread safe data structure, only modified internally")
	protected final I icfg;
	
	//stores summaries that were queried before they were computed
	//see CC 2010 paper by Naeem, Lhotak and Rodriguez
	@SynchronizedBy("consistent lock on 'incoming'")
	protected final MyConcurrentHashMap<Pair<M,D>,Set<Pair<N,D>>> endSummary =
			new MyConcurrentHashMap<Pair<M,D>, Set<Pair<N,D>>>();
	
	//edges going along calls
	//see CC 2010 paper by Naeem, Lhotak and Rodriguez
	@SynchronizedBy("consistent lock on field")
	protected final MyConcurrentHashMap<Pair<M,D>,MyConcurrentHashMap<N,Map<D, D>>> incoming =
			new MyConcurrentHashMap<Pair<M,D>,MyConcurrentHashMap<N,Map<D, D>>>();
	
	@DontSynchronize("stateless")
	protected final FlowFunctions<N, D, M> flowFunctions;
	
	@DontSynchronize("only used by single thread")
	protected final Map<N,Set<D>> initialSeeds;
	
	@DontSynchronize("benign races")
	public long propagationCount;
	
	@DontSynchronize("stateless")
	protected final D zeroValue;
	
	@DontSynchronize("readOnly")
	protected final FlowFunctionCache<N,D,M> ffCache; 
	
	@DontSynchronize("readOnly")
	protected final boolean followReturnsPastSeeds;
	
	@DontSynchronize("readOnly")
	protected boolean setJumpPredecessors = false;
	
	@DontSynchronize("readOnly")
	private boolean enableMergePointChecking = false;
	
	@DontSynchronize("readOnly")
	protected IMemoryManager<D> memoryManager = null;
	
	private MyIncomingMyEndsummary<M, N, D> mime; // Added by BK 
	
	private static Lock mLock = new ReentrantLock();
	private IFDSTabulationProblem<N,D,M,I> problem; // Added by BK
	
	/**
	 * Creates a solver for the given problem, which caches flow functions and edge functions.
	 * The solver must then be started by calling {@link #solve()}.
	 */
	public MyIFDSSolver(IFDSTabulationProblem<N,D,M,I> tabulationProblem) {
		this(tabulationProblem, DEFAULT_CACHE_BUILDER);
	}

	/**
	 * Creates a solver for the given problem, constructing caches with the
	 * given {@link CacheBuilder}. The solver must then be started by calling
	 * {@link #solve()}.
	 * @param tabulationProblem The tabulation problem to solve
	 * @param flowFunctionCacheBuilder A valid {@link CacheBuilder} or
					* <code>null</code> if no caching is to be used for flow functions.
	 */
	public MyIFDSSolver(IFDSTabulationProblem<N,D,M,I> tabulationProblem,
					@SuppressWarnings("rawtypes") CacheBuilder flowFunctionCacheBuilder) {
				if(logger.isDebugEnabled())
					flowFunctionCacheBuilder = flowFunctionCacheBuilder.recordStats();
				this.zeroValue = tabulationProblem.zeroValue();
				this.icfg = tabulationProblem.interproceduralCFG();
				FlowFunctions<N, D, M> flowFunctions = tabulationProblem.autoAddZero() ?
						new ZeroedFlowFunctions<N,D,M>(tabulationProblem.flowFunctions(), zeroValue) : tabulationProblem.flowFunctions();
				if(flowFunctionCacheBuilder!=null) {
					ffCache = new FlowFunctionCache<N,D,M>(flowFunctions, flowFunctionCacheBuilder);
					flowFunctions = ffCache;
				} else {
			ffCache = null;
		}
		this.flowFunctions = flowFunctions;
		this.initialSeeds = tabulationProblem.initialSeeds();
		this.jumpFn = new JumpFunctions<N,D>();
		this.followReturnsPastSeeds = tabulationProblem.followReturnsPastSeeds();
		this.numThreads = Math.max(1,tabulationProblem.numThreads());
		this.executor = getExecutor();
		
		this.mime = new MyIncomingMyEndsummary<M, N, D>(this.zeroValue); // Added by BK
		this.problem = tabulationProblem;
	}
	
	/**
	 * Runs the solver on the configured problem. This can take some time.
	 */
	public void solve() {		
		submitInitialSeeds();
		awaitCompletionComputeValuesAndShutdown();
	}

	/**
	 * Schedules the processing of initial seeds, initiating the analysis.
	 * Clients should only call this methods if performing synchronization on
	 * their own. Normally, {@link #solve()} should be called instead.
	 */
	protected void submitInitialSeeds() {
		for(Entry<N, Set<D>> seed: initialSeeds.entrySet()) {
			N startPoint = seed.getKey();
			for(D val: seed.getValue()) {
				TaintResultEntry tre = InitialTaintResultContainer.getSeed(((AbstractionExtension)val).getID()).Clone();
				tre.setPropaType(PROPA_TYPE.INIT);
				tre.setPrevStmt("[DP Stmt]");
				propagate(zeroValue, startPoint, val, null, false, tre);
			}
				
			jumpFn.addFunction(new PathEdge<N, D>(zeroValue, startPoint, zeroValue));
		}
	}

	/**
	 * Awaits the completion of the exploded super graph. When complete, computes result values,
	 * shuts down the executor and returns.
	 */
	protected void awaitCompletionComputeValuesAndShutdown() {
		{
			//run executor and await termination of tasks
			runExecutorAndAwaitCompletion();
		}
		if(logger.isDebugEnabled())
			printStats();

		// Some tasks could remain if this main stops waiting due to timeout. (by BK) 
		executor.shutdownNow();
		
		// Is the code below required? (by BK) 
		try {
			executor.awaitTermination(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			ExtractocolLogger.Warn("Failed to interrupt some tasks. (There might still exist zombie threads running.) The analysis has been finished anyway.");
			return;
		}
		
		ExtractocolLogger.Log("Backward analysis finished! (Current Time: " + getCurrentTime() +")");
		
		// Wait for the executor to be really gone
		while (!executor.isTerminated()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Runs execution, re-throwing exceptions that might be thrown during its execution.
	 */
	private void runExecutorAndAwaitCompletion() {
		try {
			ExtractocolLogger.Log("The analysis will be finished within " + Constants.getMaxFrontendRunningTime() + 
					" minutes. (Current Time: " + getCurrentTime() +")");
			executor.awaitCompletion(Constants.getMaxFrontendRunningTime(), Constants.getFrontendTimeUnit());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Throwable exception = executor.getException();
		if(exception!=null) {
			throw new RuntimeException("There were exceptions during IFDS analysis. Exiting.",exception);
		}
	}
	
	private static String getCurrentTime() {
		return (new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
	}

    /**
     * Dispatch the processing of a given edge. It may be executed in a different thread.
     * @param edge the edge to process
     */
    protected void scheduleEdgeProcessing(PathEdge<N,D> edge, TaintResultEntry prevTRE){
    	
    	// If the executor has been killed, there is little point
    	// in submitting new tasks
    	if (executor.isTerminating())
    		return;

    	// Stop analyzing when # of TRE exceeds a threshold (by JM)
		if(InitialTaintResultContainer.rawResult.size() > Constants.maxTRE){
			//System.out.println("# TRE exceeds the threshold! current rawResult size is: " + InitialTaintResultContainer.rawResult.size());
			//System.out.println("The queue size of executor is: " + executor.getQueue().size());
			//executor.shutdown();
			
			if(!executor.isShutdown()) {
				//executor.shutdownNow(); // It does not guarantee terminating all actively running threads. better to use shutdown().
				//ExtractocolLogger.Log("# TRE exceeds the threshold. The taint analysis will be forcely terminated.");
				
				executor.shutdown(); // It will not accept new tasks from now on.
				ExtractocolLogger.Log("From now on, new tasks will not be allowed. Remainings: " + executor.getQueue().size());
			}
			
			return;
		}
		
		try {
			executor.execute(new PathEdgeProcessingTask(edge, prevTRE));
		}catch(RejectedExecutionException e) {
			// execute() throws the RejectedExecutionException when it fails to execute.
			// The reason why it happens is the main thread calls executor.shutdownNow() in awaitCompletionComputeValuesAndShutdown().
			// Thus, we don't need to care about this exception. (by BK)
			return;
		}
    	
    	/*if(executor.getQueue().size() > 10000){
    		mLock.lock();
    		List<TaintResultEntry> treList = new LinkedList<TaintResultEntry>();
    		List<String> unitList = new LinkedList<String>();
    		List<String> varList = new LinkedList<String>();
    		int idx = 1;
    		int total = executor.getQueue().size();
    		for(Iterator<Runnable> iter = executor.getQueue().iterator(); iter.hasNext();){
    			PathEdgeProcessingTask pept = (PathEdgeProcessingTask) iter.next();

    			System.out.println("(" + (idx++) + "/" + total + ") Printing ...");
    			System.out.println("Var: " + ((Abstraction)pept.getEdge().factAtTarget()).getAccessPath().getPlainValue().toString());
    			System.out.println("Unit: " + pept.getEdge().getTarget().toString());
    			System.out.println("Method: " + icfg.getMethodOf(pept.getEdge().getTarget()).toString());
    			System.out.println("Prop. cnt: " + pept.getPrevTRE().getPropagationCount());
    			System.out.println("Main stream length: " + pept.getPrevTRE().getMainStreamLength());
    			pept.getPrevTRE().PrintInfo(false);
    		}
    		System.out.print("# of threads: " + executor.getQueue().size() + "\r"); // for debugging by BK
    		mLock.unlock();
    	}*/

    	//System.out.print("# of threads: " + executor.getQueue().size() + "\r"); // for debugging by BK
    	propagationCount++;
    }
    
    /*private boolean myTest(TaintResultEntry tre, N unit) {
    	String method = icfg.getMethodOf(unit).toString(); 
    	boolean b = method.contains("onCreate") 
				&& tre.containOnCreate() 
				&& !tre.getTaintMethodSet().contains(method)
				&& !method.contains("RxBaseActivity");
    	return b;
    }*/

	/**
	 * Lines 13-20 of the algorithm; processing a call site in the caller's context.
	 *
	 * For each possible callee, registers incoming call edges.
	 * Also propagates call-to-return flows and summarized callee flows within the caller.
	 *
	 * @param edge an edge whose target node resembles a method call
	 */
	@SuppressWarnings("unchecked")
	private void processCall(PathEdge<N,D> edge, boolean[] didTaint, TaintResultEntry tre) {
		final D d1 = edge.factAtSource();
		final N n = edge.getTarget(); // a call node; line 14...
		//final M method = icfg.getMethodOf(n); // for debugging by BK
		final D d2 = edge.factAtTarget();
		assert d2 != null;

		Collection<N> returnSiteNs = icfg.getReturnSitesOfCallAt(n);
		
		// For debugging (By JM)
		/*for (String method : tre.getTaintMethodSet())
		{
			if (method.contains("void a(java.lang.String,com.pinterest.api.remote.aa$i,java.lang.String)") && n.toString().contains("specialinvoke $r3.<com.pinterest.api.t: void <init>()>")
					&& d1.toString().contains("$r3"))
			{
				System.out.println("[Extractocol]: Found com.pinterest.api.t: void <init>");
			}
		}*/

		Collection<M> callees = icfg.getCalleesOfCallAt(n);

		// Unconditionally propagate base or arguments when (1) there is no callee or (2) it reaches the maximum depth (by BK)
		if(callees == null || callees.size() == 0 || tre.doesReachMaxDepth()){
			FlowFunction<D> function = flowFunctions.getCallFlowFunction(n, null /*this should be null (by BK)*/);

			Set<D> res = computeCallFlowFunction(function, d1, d2);

			for(D d3: res){
				for (N returnSiteN : returnSiteNs) {
					if (memoryManager != null)
						d3 = memoryManager.handleGeneratedMemoryObject(d2, d3);

					if (d3 != null){
						CheckWhetherSourceTainted(d3, d2, didTaint);
						/*if(myTest(tre, returnSiteN))
							System.out.println("Test");*/
						TaintResultEntry newTRE = tre.Clone();
						newTRE.setPropaType(PROPA_TYPE.CALL1);
						newTRE.setPrevStmt(n.toString());
						propagate(d1, returnSiteN, d3, null, false, newTRE);
					}
				}
			}
		}else{
			for(M sCalledProcN: callees) { //still line 14
				//if(tre.getTaintMethodSet().contains(sCalledProcN.toString()))
					//continue;

				if(!StubMethodHandler.IsStubMethod(sCalledProcN.toString())){ // Added by BK
					//compute the call-flow function
					FlowFunction<D> function = flowFunctions.getCallFlowFunction(n, sCalledProcN);

					Set<D> res = computeCallFlowFunction(function, d1, d2);

					Collection<N> startPointsOf = icfg.getStartPointsOf(sCalledProcN);
					//for each result node of the call-flow function
					for(D d3: res) {
						if (memoryManager != null)
							d3 = memoryManager.handleGeneratedMemoryObject(d2, d3);
						if (d3 == null)
							continue;

						//for each callee's start point(s)
						for(N sP: startPointsOf) {
							//create initial self-loop
							CheckWhetherSourceTainted(d3, d2, didTaint); // Added by BK
							//tre.IncreaseDepth(); // Added by BK
							/*if(myTest(tre, sP))
								System.out.println("Test");*/
							TaintResultEntry newTRE = tre.CloneAndIncreaseDepth();
							newTRE.setPropaType(PROPA_TYPE.CALL2);
							newTRE.setPrevStmt(n.toString());
							propagate(d3, sP, d3, n, false, true, newTRE); //line 15
						}

						//register the fact that <sp,d3> has an incoming edge from <n,d2>
						//line 15.1 of Naeem/Lhotak/Rodriguez
						//TaintResultEntry newTRE = tre.Clone();
						//newTRE.addTaintMethod(sCalledProcN.toString());
						if (!mime.addIncoming(sCalledProcN,d3,n,d1,d2, tre.Clone()))
							//if (!addIncoming(sCalledProcN,d3,n,d1,d2))
							continue;

						//line 15.2
						//Set<Pair<N, D>> endSumm = mime.endSummary(sCalledProcN, d3, tre); // Modified by BK
						Set<Pair<N, D>> endSumm = endSummary(sCalledProcN, d3); // Modified by BK

						//still line 15.2 of Naeem/Lhotak/Rodriguez
						//for each already-queried exit value <eP,d4> reachable from <sP,d3>,
						//create new caller-side jump functions to the return sites
						//because we have observed a potentially new incoming edge into <sP,d3>
						if (endSumm != null && !endSumm.isEmpty())
							for(Pair<N, D> entry: endSumm) {
								N eP = entry.getO1();
								D d4 = entry.getO2();
								//for each return site
								for(N retSiteN: returnSiteNs) {
									//compute return-flow function
									FlowFunction<D> retFunction = flowFunctions.getReturnFlowFunction(n, sCalledProcN, eP, retSiteN);
									//for each target value of the function
									for(D d5: computeReturnFlowFunction(retFunction, d3, d4, n, Collections.singleton(d1))) {
										if (memoryManager != null)
											d5 = memoryManager.handleGeneratedMemoryObject(d4, d5);

										// If we have not changed anything in the callee, we do not need the facts
										// from there. Even if we change something: If we don't need the concrete
										// path, we can skip the callee in the predecessor chain
										D d5p = d5;
										if (d5.equals(d2))
											d5p = d2;
										else if (setJumpPredecessors && d5p != d2) {
											d5p = d5p.clone();
											d5p.setPredecessor(d2);
										}
										//CheckWhetherSourceTainted(d5p, d2, didTaint); // Added by BK
										/*if(myTest(tre, retSiteN))
											System.out.println("Test");*/
										/*TaintResultEntry newTRE = tre.CloneAndAddTaintMethod(sCalledProcN.toString(),
																	"+" + ((Abstraction)d3).getAccessPath().getPlainValue().toString());*/
										TaintResultEntry newTRE = tre.Clone(); 
										newTRE.setPropaType(PROPA_TYPE.CALL3);
										newTRE.setPrevStmt(n.toString());
										newTRE.addTaintMethod(sCalledProcN.toString(),
												"+" + ((Abstraction)d3).getAccessPath().getPlainValue().toString(), false);
										
										propagate(d1, retSiteN, d5p, n, false, true, newTRE);
									}
								}
							}
					}
				}else{
					// A case where the callee is stub method
					FlowFunction<D> function = flowFunctions.getCallFlowFunction(n, null /*this should be null (by BK)*/);
					Set<D> res = computeCallFlowFunction(function, d1, d2);

					for(D d3: res){
						for (N returnSiteN : returnSiteNs) {
							if (memoryManager != null)
								d3 = memoryManager.handleGeneratedMemoryObject(d2, d3);

							if (d3 != null){
								CheckWhetherSourceTainted(d3, d2, didTaint);
								/*if(myTest(tre, returnSiteN))
									System.out.println("Test");*/
								TaintResultEntry newTRE = tre.Clone();
								newTRE.setPropaType(PROPA_TYPE.CALL4);
								newTRE.setPrevStmt(n.toString());
								propagate(d1, returnSiteN, d3, null, false, newTRE);
							}
						}
					}
				}
			}
		}


		//line 17-19 of Naeem/Lhotak/Rodriguez
		//process intra-procedural flows along call-to-return flow functions
		for (N returnSiteN : returnSiteNs) {
			FlowFunction<D> callToReturnFlowFunction = flowFunctions.getCallToReturnFlowFunction(n, returnSiteN);

			//Add jeongmin
			//Abstraction abs = (Abstraction) d1;
			//TaintResultEntry newTRE = tre.Clone();
			//newTRE.setPropaType(PROPA_TYPE.CALL5_1);
			//newTRE.setPrevStmt(n.toString());
			//abs.setTRE(newTRE);

			for(D d3: computeCallToReturnFlowFunction(callToReturnFlowFunction, d1, d2)) {
				if (memoryManager != null)
					d3 = memoryManager.handleGeneratedMemoryObject(d2, d3);
				if (d3 != null){
					CheckWhetherSourceTainted(d3, d2, didTaint); // Added by BK
					
					TaintResultEntry newTRE = tre.Clone();
					newTRE.setPropaType(PROPA_TYPE.CALL5);
					newTRE.setPrevStmt(n.toString());
					propagate(d1, returnSiteN, d3, n, false, newTRE);
				}
			}
		}
		
		// for transition (by BK)
		HandleTransitionCall((Abstraction)d1, (Unit)n, (Abstraction)d2, tre);
	}

	/**
	 * Computes the call flow function for the given call-site abstraction
	 * @param callFlowFunction The call flow function to compute
	 * @param d1 The abstraction at the current method's start node.
	 * @param d2 The abstraction at the call site
	 * @return The set of caller-side abstractions at the callee's start node
	 */
	protected Set<D> computeCallFlowFunction
			(FlowFunction<D> callFlowFunction, D d1, D d2) {
		return callFlowFunction.computeTargets(d2);
	}

	/**
	 * Computes the call-to-return flow function for the given call-site
	 * abstraction
	 * @param callToReturnFlowFunction The call-to-return flow function to
	 * compute
	 * @param d1 The abstraction at the current method's start node.
	 * @param d2 The abstraction at the call site
	 * @return The set of caller-side abstractions at the return site
	 */
	protected Set<D> computeCallToReturnFlowFunction
			(FlowFunction<D> callToReturnFlowFunction, D d1, D d2) {
		return callToReturnFlowFunction.computeTargets(d2);
	}

	/**
	 * Lines 21-32 of the algorithm.
	 *
	 * Stores callee-side summaries.
	 * Also, at the side of the caller, propagates intra-procedural flows to return sites
	 * using those newly computed summaries.
	 *
	 * @param edge an edge whose target node resembles a method exits
	 */
	protected void processExit(PathEdge<N,D> edge, boolean[] didTaint, TaintResultEntry tre) {
		final N n = edge.getTarget(); // an exit node; line 21...
		M methodThatNeedsSummary = icfg.getMethodOf(n);

		final D d1 = edge.factAtSource();
		final D d2 = edge.factAtTarget();

		//for each of the method's start points, determine incoming calls

		//line 21.1 of Naeem/Lhotak/Rodriguez
		//register end-summary
		//if (!mime.addEndSummary(methodThatNeedsSummary, d1, n, d2, tre.Clone())){
		if (!addEndSummary(methodThatNeedsSummary, d1, n, d2)){
			//if(tre.getDepth() == 0) ExtractocolLogger.Log("ProcessExit() was not operated even though depth equals to 0.");
			return;
		}
		Map<N,Map<D, D>> inc = mime.incoming(d1, methodThatNeedsSummary, tre);
		//Map<N,Map<D, D>> inc = incoming(d1, methodThatNeedsSummary);

		//for each incoming call edge already processed
		//(see processCall(..))
		if (inc != null)
			for (Entry<N,Map<D, D>> entry: inc.entrySet()) {
				//line 22
				N c = entry.getKey();
				Set<D> callerSideDs = entry.getValue().keySet();
				//for each return site
				for(N retSiteC: icfg.getReturnSitesOfCallAt(c)) {

					// for debugging by BK (should be removed!)
					/*if(icfg.getMethodOf(retSiteC).toString().contains("onCreate")) {
						System.out.println("Curr: " + methodThatNeedsSummary + " ---> " + icfg.getMethodOf(retSiteC));
					}*/
					
					if ( !tre.getTaintMethodSet().contains(icfg.getMethodOf(retSiteC).toString()))
					{
						System.out.println(icfg.getMethodOf(retSiteC).toString());
						continue;
					}

					//compute return-flow function
					FlowFunction<D> retFunction = flowFunctions.getReturnFlowFunction(c, methodThatNeedsSummary,n,retSiteC);
					Set<D> targets = computeReturnFlowFunction(retFunction, d1, d2, c, callerSideDs);
					//for each incoming-call value
					for(Entry<D, D> d1d2entry : entry.getValue().entrySet()) {
						final D d4 = d1d2entry.getKey();
						final D predVal = d1d2entry.getValue();

						for(D d5: targets) {
							if (memoryManager != null)
								d5 = memoryManager.handleGeneratedMemoryObject(d2, d5);
							if (d5 == null)
								continue;

							// If we have not changed anything in the callee, we do not need the facts
							// from there. Even if we change something: If we don't need the concrete
							// path, we can skip the callee in the predecessor chain
							D d5p = d5;
							if (d5.equals(predVal))
								d5p = predVal;
							else if (setJumpPredecessors && d5p != predVal) {
								d5p = d5p.clone();
								d5p.setPredecessor(predVal);

								// After cloning, the information for DP and taint method set will be removed.
								// So, we need to reset the information for newly-cloned d5p.
								//((AbstractionExtension)d5p).setAbstractionExtension((AbstractionExtension)d5); // Added by BK
							}
							// The fact here is that d5p derives from d5 and d5 derives from d2.
							// That means d5p derives from d2 and d2 tainted d5p.
							// Therefore, it does not have to set 'methodThatNeedsSummary' to EP for the source (d2).
							PropagateHelper.setPropagateOthers(didTaint); // Added by BK

							//CheckWhetherSourceTainted(d5p, d2, didTaint); // Added by BK
							//tre.DecreaseDepth(); // Added by BK
							/*if(myTest(tre, retSiteC))
								System.out.println("test");*/
							TaintResultEntry newTRE = tre.CloneAndDecreaseDepth();
							newTRE.setPropaType(PROPA_TYPE.EXIT1);
							newTRE.setPrevStmt(n.toString());
							propagate(d4, retSiteC, d5p, c, false, true, newTRE); // Augmented by BK
						}
					}
				}
			}

		//handling for unbalanced problems where we return out of a method with a fact for which we have no incoming flow
		//note: we propagate that way only values that originate from ZERO, as conditionally generated values should only
		//be propagated into callers that have an incoming edge for this condition
		if(followReturnsPastSeeds && d1 == zeroValue && (inc == null || inc.isEmpty())) {
			//System.out.println(methodThatNeedsSummary.toString());
			Collection<N> callers = icfg.getCallersOf(methodThatNeedsSummary);
			
			// currParamIdx is used when checking whether arg is constant. (by BK)
			SootMethod callee = (SootMethod)methodThatNeedsSummary;
			int currParamIdx = -1;
			for (int i = 0; i < callee.getParameterCount(); i++) {
				if(((Abstraction)d2).getAccessPath().getPlainValue() == callee.getActiveBody().getParameterLocal(i))
					currParamIdx = i;
			}
			
			for(N c: callers) {

				M callerMethod = icfg.getMethodOf(c);

				// Check whether the caller is already in the taint method set to avoid infinite loop (by BK)
				if(CheckLoop(tre, callerMethod.toString()))
					continue;

				Collection<N> retSiteCs = icfg.getReturnSitesOfCallAt(c);
				for(N retSiteC: retSiteCs) {
					FlowFunction<D> retFunction = flowFunctions.getReturnFlowFunction(c, methodThatNeedsSummary,n,retSiteC);
					Set<D> targets = computeReturnFlowFunction(retFunction, d1, d2, c, Collections.singleton(zeroValue));
					for(D d5: targets) {
						if (memoryManager != null)
							d5 = memoryManager.handleGeneratedMemoryObject(d2, d5);
						if (d5 != null){
							CheckWhetherSourceTainted(d5, d2, didTaint); // Added by BK
							//tre.DecreaseDepth(); // Added by BK
							//propagate(zeroValue, retSiteC, d5, c, true, callerMethod == methodThatNeedsSummary, tre.CloneAndDecreaseDepth());

//							TaintInfo root = new TaintInfo();
//							root.selfcount = 0;
//							root.obj = ((Abstraction)d5).getAccessPath().getPlainValue().toString();
//							root.abs = (Abstraction) d5;
//							root.unit =(Unit) n;
//							root.sm = (SootMethod) icfg.getMethodOf(n);
//							tre.TaintTree.put(root, new LinkedList<>());

							TaintResultEntry newTRE = tre.CloneDecreaseDepthIncreaseMainStreamLength();
							/*MyEdgeEntry mee = new MyEdgeEntry(d2.toString(), (Unit) n, icfg.getMethodOf(n).toString(), tre);
							System.out.println("V: " + "[callSz:" + callers.size() + "]"
												     + "[retSz:" + retSiteCs.size() + "]"
													 + "[tgtSz:" + targets.size() + "]"
													 + "("+ ((Abstraction)edge.factAtSource()).getAccessPath().getPlainValue().toString() + ")"
													 + ((Abstraction)edge.factAtTarget()).getAccessPath().getPlainValue().toString()
											   + "->"+ ((Abstraction)d5).getAccessPath().getPlainValue().toString()
											   + " (PC:"+newTRE.getPropagationCount() + ", tot:" + newTRE.getTaintMethodSet().size() + "-"+ newTRE.getTaintMethodSet().hashCode() + ")"
											   + "(Mee:"+ mee.hashCode() +")"
											   + ", M: " + ((SootMethod)methodThatNeedsSummary).getName() +"->"+ ((SootMethod)callerMethod).getSignature()
											   + ", C: " + c.hashCode());*/
							/*if(myTest(tre, retSiteC))
								System.out.println("test");*/
							newTRE.setPropaType(PROPA_TYPE.EXIT2);
							newTRE.setPrevStmt(n.toString());
							propagate(zeroValue, retSiteC, d5, c, true, callerMethod == methodThatNeedsSummary, newTRE);
						}

					}
				}
				
				// If the corresponding argument of caller is constant, Save cloned TRE with the caller. (by BK)
				final Stmt stmt = (Stmt) c;
				final InvokeExpr ie = (stmt != null && stmt.containsInvokeExpr())? stmt.getInvokeExpr() : null;
				
				if(ie == null)
					continue;
				
				// check whether the argument is constant (by BK)
				try {
					if(currParamIdx == -1 || !(ie.getArg(currParamIdx) instanceof Constant))
						continue;
				}catch(Exception e) {
					System.err.println("Invoke Expr: " + ie.toString() + ", currParamIdx: " + currParamIdx);
					continue;
				}
				
				
				// If it is true, clone TRE, add the caller method to the cloned TRE, and save the tre (by BK)
				TaintResultEntry newTRE = tre.CloneDecreaseDepthIncreaseMainStreamLength();
				newTRE.setPropaType(PROPA_TYPE.EXIT3);
				newTRE.setPrevStmt(n.toString());
				newTRE.addTaintMethod(callerMethod.toString(), "Const");
				SetEntryPoint(newTRE);
				newTRE.Clear();
			}
			//in cases where there are no callers, the return statement would normally not be processed at all;
			//this might be undesirable if the flow function has a side effect such as registering a taint;
			//instead we thus call the return flow function will a null caller
			if(callers.isEmpty()) {
				FlowFunction<D> retFunction = flowFunctions.getReturnFlowFunction(null, methodThatNeedsSummary,n,null);
				retFunction.computeTargets(d2);
				//didTaint[0] = true; // Added by BK (Why did I add this statement? :()
			}
		}
	}

	/**
	 * Computes the return flow function for the given set of caller-side
	 * abstractions.
	 * @param retFunction The return flow function to compute
	 * @param d1 The abstraction at the beginning of the callee
	 * @param d2 The abstraction at the exit node in the callee
	 * @param callSite The call site
	 * @param callerSideDs The abstractions at the call site
	 * @return The set of caller-side abstractions at the return site
	 */
	protected Set<D> computeReturnFlowFunction
			(FlowFunction<D> retFunction, D d1, D d2, N callSite, Collection<D> callerSideDs) {
		return retFunction.computeTargets(d2);
	}

	/**
	 * Lines 33-37 of the algorithm.
	 * Simply propagate normal, intra-procedural flows.
	 * @param edge
	 */
	private void processNormalFlow(PathEdge<N,D> edge, boolean[] didTaint, TaintResultEntry tre) {
		final D d1 = edge.factAtSource();
		final N n = edge.getTarget();
		final M method = icfg.getMethodOf(n); // for debugging (by BK)
		final D d2 = edge.factAtTarget();

		for (N m : icfg.getSuccsOf(n)) {
			FlowFunction<D> flowFunction = flowFunctions.getNormalFlowFunction(n,m);

			//Add Jeongmin
			//Abstraction abs = (Abstraction) d1;
			//abs.setTRE(tre.Clone());

			Set<D> res = computeNormalFlowFunction(flowFunction, d1, d2);
			for (D d3 : res) {
				if (memoryManager != null && d2 != d3)
					d3 = memoryManager.handleGeneratedMemoryObject(d2, d3);
				if (d3 != null){
					CheckWhetherSourceTainted(d3, d2, didTaint); // Added by BK
//					tre.AddTaintInfoNode((Abstraction) d2, (Abstraction) d3, (Unit) n, (SootMethod) method);
					
					TaintResultEntry newTRE = tre.Clone((Abstraction) d2, (Abstraction) d3, (Unit) n, (SootMethod) method);
					newTRE.setPropaType(PROPA_TYPE.NORMAL);
					newTRE.setPrevStmt(n.toString());
					
 					propagate(d1, m, d3, null, false, newTRE);
				}
			}
		}
		
		// for transition (by BK)
		HandleTransitionNormal((Abstraction)d1, (Unit)n, (Abstraction)d2, tre);
	}

	/**
	 * Computes the normal flow function for the given set of start and end
	 * abstractions.
	 * @param flowFunction The normal flow function to compute
	 * @param d1 The abstraction at the method's start node
	 * @param d2 The abstraction at the current node
	 * @return The set of abstractions at the successor node
	 */
	protected Set<D> computeNormalFlowFunction
			(FlowFunction<D> flowFunction, D d1, D d2) {
		return flowFunction.computeTargets(d2);
	}

	/**
	 * Propagates the flow further down the exploded super graph.
	 * @param sourceVal the source value of the propagated summary edge
	 * @param target the target statement
	 * @param targetVal the target value at the target statement
	 * @param relatedCallSite for call and return flows the related call statement, <code>null</code> otherwise
	 *        (this value is not used within this implementation but may be useful for subclasses of {@link MyIFDSSolver})
	 * @param isUnbalancedReturn <code>true</code> if this edge is propagating an unbalanced return
	 *        (this value is not used within this implementation but may be useful for subclasses of {@link MyIFDSSolver})
	 */
	protected void propagate(D sourceVal, N target, D targetVal,
		/* deliberately exposed to clients */ N relatedCallSite,
		/* deliberately exposed to clients */ boolean isUnbalancedReturn,
		/* containing DP & taint method set (by BK) */TaintResultEntry prevTRE) {
		propagate(sourceVal, target, targetVal, relatedCallSite, isUnbalancedReturn, false, prevTRE);
	}

	/**
	 * Propagates the flow further down the exploded super graph.
	 * @param sourceVal the source value of the propagated summary edge
	 * @param target the target statement
	 * @param targetVal the target value at the target statement
	 * @param relatedCallSite for call and return flows the related call statement, <code>null</code> otherwise
	 *        (this value is not used within this implementation but may be useful for subclasses of {@link MyIFDSSolver})
	 * @param isUnbalancedReturn <code>true</code> if this edge is propagating an unbalanced return
	 *        (this value is not used within this implementation but may be useful for subclasses of {@link MyIFDSSolver})
	 * @param forceRegister True if the jump function must always be registered with jumpFn .
	 * 		  This can happen when externally injecting edges that don't come out of this
	 * 		  solver.
	 */
	protected void propagate(D sourceVal, N target, D targetVal,
			/* deliberately exposed to clients */ N relatedCallSite,
			/* deliberately exposed to clients */ boolean isUnbalancedReturn,
			boolean forceRegister,
			TaintResultEntry prevTRE) {
		// Let the memory manager run
		if (memoryManager != null) {
			sourceVal = memoryManager.handleMemoryObject(sourceVal);
			targetVal = memoryManager.handleMemoryObject(targetVal);
			if (sourceVal == null || targetVal == null)
				return;
		}

		if(prevTRE == null)
			return;

		final PathEdge<N,D> edge = new PathEdge<N,D>(sourceVal, target, targetVal);

		//MyEdgeEntry mee = new MyEdgeEntry(targetVal.toString(), (Unit) target, icfg.getMethodOf(target).toString(), prevTRE/*prevTRE.Clone()*/);
		/*final String existingVal = (forceRegister || !enableMergePointChecking || isMergePoint(target)) ?
				PropagateHelper.addMyEdgeEntry(mee) : null;*/

		//List<N> tmp = icfg.getSuccsOf(target); // For debugging

		final BigInteger existingVal = (forceRegister || !enableMergePointChecking || isMergePoint(target)) ?
				PropagateHelper.addMyEdgeEntry(targetVal.toString(), (Unit) target, icfg.getMethodOf(target).toString(), prevTRE/*prevTRE.Clone()*/) : null;

		if(existingVal != null){
			//mee.clear();
			//mee = null;
			prevTRE.Clear();
		}else{
			scheduleEdgeProcessing(edge, prevTRE);
		}

		/*final D existingVal = (forceRegister || !enableMergePointChecking || isMergePoint(target)) ?
				jumpFn.addFunction(edge) : null;
		if (existingVal != null && ((AbstractionExtension)existingVal).getTRE().equals(prevTRE)) { // TODO: need to improve thread-safely (Augmented by BK)
			if (existingVal != targetVal) {
				existingVal.addNeighbor(targetVal);
			}

			//((AbstractionExtension)targetVal).Clear(); // Added by BK
			prevTRE.Clear(); // Added by BK
		}
		else {
			scheduleEdgeProcessing(edge, prevTRE);
		}*/
	}

	/**
	 * Gets whether the given unit is a merge point in the ICFG
	 * @param target The unit to check
	 * @return True if the given unit is a merge point in the ICFG, otherwise
	 * false
	 */
	private boolean isMergePoint(N target) {
		// Check whether there is more than one possibility to reach this unit
		List<N> preds = icfg.getPredsOf(target);
		int size = preds.size();
		if (size > 1)
			if (!icfg.getEndPointsOf(icfg.getMethodOf(target)).contains(target))
				return true;

		// Special case: If this is the first unit in the method, there is an
		// implicit second way (through method call)
		if (size == 1) {
			if (icfg.getStartPointsOf(icfg.getMethodOf(target)).contains(target))
				if (!icfg.getEndPointsOf(icfg.getMethodOf(target)).contains(target))
					return true;
		}

		return false;
	}

	protected Set<Pair<N, D>> endSummary(M m, D d3) {
		Set<Pair<N, D>> map = endSummary.get(new Pair<M, D>(m, d3));
		return map;
	}

	private boolean addEndSummary(M m, D d1, N eP, D d2) {
		if (d1 == zeroValue)
			return true;

		Pair<M, D> p1 = new Pair<M, D>(m, d1); // added by BK
		Pair<N, D> p2 = new Pair<N, D>(eP, d2); // added by BK

		Set<Pair<N, D>> summaries = endSummary.putIfAbsentElseGet
				(p1, new ConcurrentHashSet<Pair<N, D>>());
		return summaries.add(p2);
	}

	protected Map<N, Map<D, D>> incoming(D d1, M m) {
		Map<N, Map<D, D>> map = incoming.get(new Pair<M, D>(m, d1));
		return map;
	}

	protected boolean addIncoming(M m, D d3, N n, D d1, D d2) {
		MyConcurrentHashMap<N, Map<D, D>> summaries = incoming.putIfAbsentElseGet
				(new Pair<M, D>(m, d3), new MyConcurrentHashMap<N, Map<D, D>>());
		Map<D, D> set = summaries.putIfAbsentElseGet(n, new ConcurrentHashMap<D, D>());
		return set.put(d1, d2) == null;
	}

	/**
	 * Factory method for this solver's thread-pool executor.
	 */
	protected CountingThreadPoolExecutor getExecutor() {
		return new SetPoolExecutor(1, this.numThreads, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}

	/**
	 * Returns a String used to identify the output of this solver in debug mode.
	 * Subclasses can overwrite this string to distinguish the output from different solvers.
	 */
	protected String getDebugName() {
		return "FAST IFDS SOLVER";
	}

	public void printStats() {
		if(logger.isDebugEnabled()) {
			if(ffCache!=null)
				ffCache.printStats();
		} else {
			logger.info("No statistics were collected, as DEBUG is disabled.");
		}
	}

	private class PathEdgeProcessingTask implements Runnable {

		private final PathEdge<N,D> edge;
		private TaintResultEntry prevTRE; // Added by BK

		public PathEdgeProcessingTask(PathEdge<N,D> edge, TaintResultEntry tre) {
			this.edge = edge;
			this.prevTRE = tre;
		}

		public PathEdge<N,D> getEdge(){ return this.edge; }
		public TaintResultEntry getPrevTRE() { return this.prevTRE; }

		public void run() {
			//PropagateHelper.IncreaseCntAtomic(); // For debugging
			//System.out.print("# of threads: " + PropagateHelper.getCnt() + "\r");
			/*if(PropagateHelper.getCnt() > 1000)
				System.out.println("# of threads exceeds 1000.");
			else if (PropagateHelper.getCnt() > 100)
				System.out.println("# of threads exceeds 100.");*/

			TaintResultEntry tre = deriveNewTRE(edge, prevTRE);
			tre.IncreasePropagationCount();
			/*if(tre.getPropagationCount() > 500)
				System.out.println("[500]" + edge.getTarget() + ", " + tre.getInfoString());
			else if(tre.getPropagationCount() > 300)
				System.out.println("[300]" + edge.getTarget() + ", " + tre.getInfoString());*/
			//int didTaint = new Integer(0);
			boolean[] didTaint = PropagateHelper.deriveNewDidTaint(); // For passing-by-reference (Added by BK)
			//didTaint[0] = false; // Added by BK
			/*if(false){ // For debugging
				System.err.println("t,s,u,m: " + ((Abstraction)edge.factAtTarget()).getAccessPath().getPlainValue().toString() + ", "
												 + ((Abstraction)edge.factAtSource()).getAccessPath().getPlainValue().toString() + ", "
												 + edge.getTarget().toString() + ", "
												 + icfg.getMethodOf(edge.getTarget()).toString());
			}*/
			/*String m = icfg.getMethodOf(edge.getTarget()).toString();
			if(tre.getTaintMethodSet().getLast() != null){
				if(!tre.getTaintMethodSet().getLast().equals(m)){
					System.out.println(", V: " + edge.factAtTarget().toString() + "M: " + m);
				}
			}*/


			//if(tre.getPropagationCount() < 2){
			if(tre.getMainStreamLength() <= PropagateHelper.getMaxMainStream()){
				if(icfg.isCallStmt(edge.getTarget())) {
					processCall(edge, didTaint, tre);
				} else {
					//note that some statements, such as "throw" may be
					//both an exit statement and a "normal" statement
					if(icfg.isExitStmt(edge.getTarget()))
						processExit(edge, didTaint, tre);
					if(!icfg.getSuccsOf(edge.getTarget()).isEmpty())
						processNormalFlow(edge, didTaint, tre);
				}
			}

			/** Added by BK */
			if (!PropagateHelper.didPropagate(didTaint))
				SetEntryPoint(tre);

			// To avoid memory leak (by BK)
			tre.Clear(); // generated here
			prevTRE.Clear(); // generated at the previous thread
			//if(!PropagateHelper.didPropagateItself(didTaint))
			//	((AbstractionExtension) edge.factAtTarget()).Clear();

			//PropagateHelper.DecreaseCntAtomic(); // For debugging
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((edge == null) ? 0 : edge.hashCode());
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
			PathEdgeProcessingTask other = (PathEdgeProcessingTask) obj;
			if (edge == null) {
				if (other.edge != null)
					return false;
			} else if (!edge.equals(other.edge))
				return false;
			return true;
		}

	}

	/**
	 * Sets whether abstractions on method returns shall be connected to the
	 * respective call abstractions to shortcut paths.
	 * @param setJumpPredecessors True if return abstractions shall be connected
	 * to call abstractions as predecessors, otherwise false.
	 */
	public void setJumpPredecessors(boolean setJumpPredecessors) {
		this.setJumpPredecessors = setJumpPredecessors;
	}

	/**
	 * Sets whether only abstractions at merge points shall be recorded to jumpFn.
	 * @param enableMergePointChecking True if only abstractions at merge points
	 * shall be recorded to jumpFn, otherwise false.
	 */
	public void setEnableMergePointChecking(boolean enableMergePointChecking) {
		this.enableMergePointChecking = enableMergePointChecking;
	}

	/**
	 * Sets the memory manager that shall be used to manage the abstractions
	 * @param memoryManager The memory manager that shall be used to manage the
	 * abstractions
	 */
	public void setMemoryManager(IMemoryManager<D> memoryManager) {
		this.memoryManager = memoryManager;
	}

	/**
	 * Gets the memory manager used by this solver to reduce memory consumption
	 * @return The memory manager registered with this solver
	 */
	public IMemoryManager<D> getMemoryManager() {
		return this.memoryManager;
	}

	/** Added by BK */
	private void SetEntryPoint(TaintResultEntry tre) {
		//N currentStmt = edge.getTarget();
		//D source = edge.factAtTarget();
		//M method = icfg.getMethodOf(currentStmt);

		//AbstractionExtension abs = (AbstractionExtension)source;
		//abs.addTaintMethod(method.toString());

		/*System.out.println("+Result:");
		System.out.println("\t- Method: " + method.toString());
		System.out.println("\t- Stmt: " + currentStmt.toString());
		System.out.println("\t- Source: " + source.toString());*/
		//InitialTaintResultContainer.addResult(abs);
		InitialTaintResultContainer.addFinalResult(tre.Clone());
	}

	private void CheckWhetherSourceTainted(D dest, D source, boolean[] didTaint){
		if(dest.getPredecessor() == source)
			PropagateHelper.setPropagateOthers(didTaint);
		if(dest == source)
			PropagateHelper.setPropagateItself(didTaint);
		//if(didSourceTaint(dest, source))
			//didTaint[0] = true;
	}

	private boolean didSourceTaint(D dest, D source){
		return (dest.getPredecessor() == source)
				|| (dest == source);
	}

	private TaintResultEntry deriveNewTRE(PathEdge<N,D> edge, TaintResultEntry prevTRE){
		TaintResultEntry currTRE = new TaintResultEntry(prevTRE);
		currTRE.setPropaType(prevTRE.getPropaType());
		currTRE.setPrevStmt(prevTRE.getPrevStmt());
		//TaintResultEntry currTRE = new TaintResultEntry(prevTRE, prevTRE.getPropagationCount() + 1);

		N unit = edge.getTarget();
		M currMethod = icfg.getMethodOf(unit);
		//currTRE.addTaintMethod(currMethod.toString());

		currTRE.addTaintMethod(currMethod.toString(), ((Abstraction)edge.factAtTarget()).getAccessPath().getPlainValue().toString());

		return currTRE;
	}

	/** Check whether METHOD is contained in the taint method set of TRE
	 * 
	 * @param tre TaintResultEntry
	 * @param method Method
	 * @return True, if the METHOD is in the taint method set
	 */
	private boolean CheckLoop(TaintResultEntry tre, String method){
		return tre.getTaintMethodSet().contains(method);
	}
	
	public void HandleTransitionNormal(Abstraction d1, Unit currUnit, Abstraction source, TaintResultEntry tre) {}
	public void HandleTransitionCall(Abstraction d1, Unit currUnit, Abstraction source, TaintResultEntry tre) {}
}
