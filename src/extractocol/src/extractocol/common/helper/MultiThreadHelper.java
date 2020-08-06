package extractocol.common.helper;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import extractocol.frontend.basic.ExtractocolLogger;
import heros.solver.CountingThreadPoolExecutor;

public class MultiThreadHelper {
	public static ThreadPoolExecutor createExecutor(int numThreads, int keepAliveTime, TimeUnit keepAliveTimeUnit) {
		return new ThreadPoolExecutor(numThreads, Integer.MAX_VALUE, keepAliveTime, keepAliveTimeUnit, new LinkedBlockingQueue<Runnable>());
	}
	
	public static ThreadPoolExecutor createExecutor() {
		return createExecutor(getCoreNum(), 30, TimeUnit.SECONDS);
	}
	
	public static ThreadPoolExecutor createExecutor(int keepAliveTime, TimeUnit keepAliveTimeUnit) {
		return createExecutor(getCoreNum(), keepAliveTime, keepAliveTimeUnit);
	}
	
	public static int getCoreNum() {
		return Runtime.getRuntime().availableProcessors();
	}
	
	public static void awaitCompletion(ThreadPoolExecutor executor, int waitingTime, TimeUnit tu, String startMsg, String endMsg) {
		if(startMsg != null)
			ExtractocolLogger.Log(startMsg);
		
		executor.shutdown();
		
		try {
			executor.awaitTermination(waitingTime, tu);
		} catch (InterruptedException e) {
			e.printStackTrace();
			ExtractocolLogger.Warn(	"Failed to interrupt some tasks. The analysis has been finished anyway.");
		}
		
		if(endMsg != null)
			ExtractocolLogger.Log(endMsg);
		
		executor.getQueue().clear();
	}
	
	/*public static void awaitCompletion(ThreadPoolExecutor executor, int waitingTime, TimeUnit tu, String startMsg, String endMsg) {
		{
			runExecutorAndAwaitCompletion(executor, waitingTime, tu, startMsg);
		}
		
		// Some tasks could remain if this main stops waiting due to timeout. (by BK) 
		executor.shutdownNow();

		// Is the code below required? (by BK)
		try {
			executor.awaitTermination(100, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			ExtractocolLogger.Warn(	"Failed to interrupt some tasks. The analysis has been finished anyway.");
			return;
		}

		// Wait for the executor to be really gone
		while (!executor.isTerminated()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if(endMsg != null)
			ExtractocolLogger.Log(endMsg);
	}
	
	private static void runExecutorAndAwaitCompletion(ThreadPoolExecutor executor, int waitingTime, TimeUnit tu, String startMsg) {
		try {
			if(startMsg != null)
				ExtractocolLogger.Log(startMsg);
			
			executor.awaitCompletion(waitingTime, tu);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Throwable exception = executor.getException();
		if(exception!=null) {
			throw new RuntimeException("There were exceptions during Backend analysis. Exiting.", exception);
		}
	}*/
}
