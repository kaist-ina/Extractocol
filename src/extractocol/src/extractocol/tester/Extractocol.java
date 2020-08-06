package extractocol.tester;

import extractocol.Constants;
import extractocol.backend.response.helper.BlockTravelerHelper;
import extractocol.common.helper.MultiThreadHelper;
import extractocol.common.outputs.helper.StdOutputController;
import extractocol.common.trackers.ImplicitCallEdgeTracker;
import extractocol.common.trackers.IntentMapTracker;
import extractocol.frontend.basic.ExtractocolLogger;
import extractocol.frontend.helper.PropagateHelper;
import extractocol.frontend.output.basic.TaintResultEntry;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.Duration;

public class Extractocol {
	static boolean debug = true;
	static long totTime = 0;
	static boolean needIntentMap = false;
	
	public static void main(String[] args) {
		Instant s = Instant.now().truncatedTo(ChronoUnit.MICROS);
		Instant e = Instant.now().truncatedTo(ChronoUnit.MICROS);
		double tE = Duration.between(s, e).toNanos() / 1000000;
		
		// 0-1. Argument parsing 
		if(!ArgParsing(args))
			System.exit(0);
		
		// 0-2. initialization
		init();
		
		// 1. Implicit-Call-Edge Tracker
		Extractocol_ImplicitCallEdgeTracker();
		
		// 2. Retrofit analyzer (java parser & baseUrl tracking)
		if(Constants.getIsRetrofit())
			Extractocol_RetrofitAnalyzer();
		
		// 3. IntentMap builder
		if(needIntentMap)
			Extractocol_IntentMapBuilder();
		
		// 4. Frontend
		Extractocol_Frontend();
		
		// 5. Backend
		Extractocol_Backend();
		
		// 6. HeapHandling
		Extractocol_HeapHandling();

		ExtractocolLogger.Log("Extractocol Total Elapsed Time: " + secondToHMS(totTime));
		System.exit(0);
	}
	
	
	
	/********************************************************/
	/***                    Main Functions                ***/
	/********************************************************/
	private static void Extractocol_ImplicitCallEdgeTracker() {
		Long start, end;
		System.out.println("(1/6) Implicit-Call-Edge Tracker running ...");
		
		start = System.currentTimeMillis();
		if(!debug) StdOutputController.stop();
		ImplicitCallEdgeTracker.M(debug);
		if(!debug) StdOutputController.start();
		end = System.currentTimeMillis();
		
		BlockTravelerHelper.SerializeBackEdge();
		System.gc();
		totTime += (end-start)/1000;
		System.out.println("Done. Elapsed Time: " + secondToHMS((end-start)/1000));
	}
	
	private static void Extractocol_RetrofitAnalyzer() {
		long start, end;
		
		System.out.println("(2/6) Retrofit Analyzer running ...");
		
		start = System.currentTimeMillis();
		//if(!debug) StdOutputController.stop();
		RetrofitAnalyzer.main(getRetrofitArg());
		//if(!debug) StdOutputController.start();
		end = System.currentTimeMillis();
		
		BlockTravelerHelper.SerializeBackEdge();
		System.gc();
		totTime += (end-start)/1000;
		System.out.println("Done. Elapsed Time: " + secondToHMS((end-start)/1000));
	}
	
	private static void Extractocol_IntentMapBuilder() {
		long start, end;
		
		System.out.println("(3/6) Intent/Bundle Map Builder running ...");
		
		start = System.currentTimeMillis();
		//if(!debug) StdOutputController.stop();
		IntentMapTracker.M(debug);
		//if(!debug) StdOutputController.start();
		end = System.currentTimeMillis();
		
		BlockTravelerHelper.SerializeBackEdge();
		System.gc();
		totTime += (end-start)/1000;
		System.out.println("Done. Elapsed Time: " + secondToHMS((end-start)/1000));
	}
	
	private static void Extractocol_Frontend() {
		long start, end;
		
		System.out.println("(4/6) Frontend running ...");
		
		start = System.currentTimeMillis();
		if(!debug) StdOutputController.stop(); 
		Frontend.main(getFrontendArg());
		if(!debug) StdOutputController.start();
		end = System.currentTimeMillis();
		
		BlockTravelerHelper.SerializeBackEdge();
		System.gc();
		totTime += (end-start)/1000;
		System.out.println("Done. Elapsed Time: " + secondToHMS((end-start)/1000));
	}
	
	private static void Extractocol_Backend() {
		long start, end;
		
		System.out.println("(5/6) Backend running ...");
		
		start = System.currentTimeMillis();
		if(!debug) StdOutputController.stop();
		Backend.main(getBackendArg());
		if(!debug) StdOutputController.start();
		end = System.currentTimeMillis();
		
		BlockTravelerHelper.SerializeBackEdge();
		System.gc();
		totTime += (end-start)/1000;
		System.out.println("Done. Elapsed Time: " + secondToHMS((end-start)/1000));
	}
	
	private static void Extractocol_HeapHandling() {
		long start, end;
		
		System.out.println("(6/6) HeapHandler running ...");
		
		start = System.currentTimeMillis();
		//if(!debug) StdOutputController.stop();
		HeapHandling.main(getHeapHandlingArg());
		//if(!debug) StdOutputController.start();
		end = System.currentTimeMillis();
		
		BlockTravelerHelper.SerializeBackEdge();
		totTime += (end-start)/1000;
		System.out.println("Done. Elapsed Time: " + secondToHMS((end-start)/1000));
	}
	
	
	
	/********************************************************/
	/***                    Get arguments                 ***/
	/********************************************************/
	private static String[] getRetrofitArg() { return splitter(Constants.getAPPName() + (debug? " --debug" : ""));	}
	private static String[] getFrontendArg() { return splitter(Constants.getAPPName()); }
	private static String[] getBackendArg() { return splitter("--app " + Constants.getAPPName()); }
	private static String[] getHeapHandlingArg() { return splitter("--app " + Constants.getAPPName()); }
	private static String[] splitter(String args) { return args.split(" "); }
	
	
	
	/********************************************************/
	/***                       Others                     ***/
	/********************************************************/
	private static String secondToHMS(long t) {
		String res;
		
		int hour = (int) t/3600;
		int remainder = (int) t - hour * 3600;
		int min = remainder / 60;
		int sec = remainder - min * 60;
		
		if(hour > 0)
			res = String.format("%dh %dm %ds", hour, min, sec);
		else if (min > 0)
			res = String.format("%dm %ds", min, sec);
		else
			res = String.format("%ds", sec);
		
		return res;
	}
	
	private static void init() {
		ExtractocolLogger.Log("Extractocol is running... (App Name: " + Constants.getAPPName() + ", # Processors: " + MultiThreadHelper.getCoreNum() + ")\n");
		Constants.setIsFullStack(true);
		BlockTravelerHelper.DeserializeBackEdge();
	}
	
	private static boolean ArgParsing(String[] args){
		Constants.setAPPName(args[0]);
		
		int k = 1;
		while (k < args.length)
		{
			// Set maximum main stream length
			if (args[k].equalsIgnoreCase("--maxms"))
			{
				PropagateHelper.setMaxMainStream(Integer.parseInt(args[k + 1]));
				k += 2;
				continue;
			}
			
			// Set maximum depth
			else if (args[k].equalsIgnoreCase("--maxdepth"))
			{
				TaintResultEntry.setMaxDepth(Integer.parseInt(args[k + 1]));
				k += 2;
				continue;
			}
			
			// set whether it uses retrofit
			else if (args[k].equalsIgnoreCase("--retrofit")) 
			{
				Constants.setIsRetrofit(true);
				k++;
				continue;
			}
			
			// set whether it needs intentMap
			else if (args[k].equalsIgnoreCase("--intentmap")) 
			{
				needIntentMap = true;
				k++;
				continue;
			}
			
			// set whether logs are printed
			else if (args[k].equalsIgnoreCase("--debug")) 
			{
				debug = true;
				k++;
				continue;
			}
			
			// Set maximum depth
			else if (args[k].equalsIgnoreCase("--dpstride")) 
			{
				Frontend.setStride(Integer.parseInt(args[k + 1]));
				k += 2;
				continue;
			}
			
			k++;
		}
		
		return true;
	}

}
