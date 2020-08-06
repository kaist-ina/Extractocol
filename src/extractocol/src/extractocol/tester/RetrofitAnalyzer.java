package extractocol.tester;

import extractocol.Constants;
import extractocol.common.retrofit.RetrofitBaseURLTracker;
import extractocol.common.retrofit.RetrofitParse;
import extractocol.frontend.helper.PropagateHelper;
import extractocol.frontend.output.basic.TaintResultEntry;

public class RetrofitAnalyzer {
	static boolean debug = false;
	static boolean oldIsRetrofit;
	public static void main(String[] args) {
		// 0. Parse args
		if(!Argument_Parsing(args)){
			System.err.println("Argument is weird.");
			System.exit(0);
		}
		
		init();
		
		// 1. TODO:extract java file (is it possible?)
		System.out.println("Running...");
		
		// 2. extract base url of retrofit
		System.out.println("Getting base Url of retrofit ...");
		RetrofitBaseURLTracker.M(debug);
		System.out.println("Done.");
		
		// 2. analyze 
		System.out.println("Getting sub-Url & response format of retrofit ...");
		RetrofitParse.M();
		System.out.println("Done.");
		
		System.out.println("Retrofit Analyzer done.");
		
		postProcessing();
		
		if(Constants.isNotFullStack())
			System.exit(0);
	}
	
	private static void init() {
		oldIsRetrofit = Constants.setIsRetrofitFalse();
	}
	
	private static void postProcessing() {
		Constants.setIsRetrofit(oldIsRetrofit);
	}

	private static boolean Argument_Parsing(String[] args){
		Constants.setAPPName(args[0]);
		
		int k = 1;
		while (k < args.length)
		{
			// set whether logs are printed
			if (args[k].equalsIgnoreCase("--debug")) 
			{
				debug = true;
				k++;
				continue;
			}
			
			k++;
		}
		
		System.out.println("Retrofit Analyzer - Target App: " + Constants.getAPPName());
		
		return true;
	}
}
