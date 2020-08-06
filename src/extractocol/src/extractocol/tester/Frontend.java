package extractocol.tester;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import extractocol.Constants;
import extractocol.backend.request.helper.JimpleLoader;
import extractocol.common.retrofit.utils.FileAnalyzer;
import extractocol.common.tools.Pair2;
import extractocol.frontend.*;
import extractocol.frontend.basic.ExtractocolLogger;
import extractocol.frontend.basic.MyCallGraphBuilder;
import extractocol.frontend.helper.PropagateHelper;
import extractocol.frontend.output.TaintResultContainer;
import extractocol.frontend.output.basic.DPContainer;
import extractocol.frontend.output.basic.TaintResultEntry;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;

public class Frontend {
	static String appName;
	static long start, end;
	static List<String> DPs = new ArrayList<String>();
	static List<Pair2<String, Integer>> DPandHASH = new ArrayList<Pair2<String,Integer>>();
	static List<DPContainer> dpListBackward = new ArrayList<DPContainer>();
	static List<DPContainer> dpListForward = new ArrayList<DPContainer>();
	static int epCnt = 0;
	static int startDPNum = 0; // Do not change
	static int endDPNum = 0; // Do not change
	static int pointOfDPs = 0; // Do not change
	static int targetStmtHash;
	
	public static void main(String[] args) {
		
		// 1. Argument parsing
		if(!Argument_Parsing(args)){
			System.err.println("Argument is weird.");
			System.exit(0);
		}
		
		// 2. Check whether we need to perform taint analysis
		// If the file already exists, we don't need to perform the taint analysis. (by BK)
		if (TaintResultContainer.doesBackwardDPListExist()) {
			ExtractocolLogger.Log("Not performing BACKWARD taint analysis. DPList.backward already exists.");
			return;
		}
				
		// 3. Init (empty the 'temp' directory)
		init();
		
		// 4. Open/Read the original DP file (retrofitDP.txt or [AppName].txt)
		readOriginalDPFile();
		
		//
		getStmtHash();
		
		// 5. Generate temp DP file containing partial DPs
		while(genTempDPFile()) {
			// 6. Run Front-end (Backward/Forward)
			if(!FileAnalyzer.doesFileExist(getCurrentDPListPath(true)))
				Run_Backward();
			//Run_Forward();
		}
		
		// 7. Merging results
		MergingResults();
		
		// 8. Finalization
		postProcessing();
		
		if(Constants.isNotFullStack())
			System.exit(0);
	}
	
	private static void init() {
		JimpleLoader jl = new JimpleLoader(Constants.getAndroidSDKPath(), Constants.getAPKpath(Constants.getAPPName() + ".apk"),Constants.getSourcesAndSinksPath());
	}
	
	private static void MergingResults() {
		// Get the temp directory
		String path = Constants.getPartialDPListDirPath();

		// 
		File tempDir = new File(path);
		for (File file : tempDir.listFiles()) {
			if(!file.getName().contains(".backward") &&	!file.getName().contains(".forward"))
				continue;
				
			List<DPContainer> res = TaintResultContainer.Deserialization(file.getPath());
			if(res == null)
				continue;
			
			if(file.getName().contains(".backward"))
				dpListBackward.addAll(res);
			else
				dpListForward.addAll(res);
		}
		
		if(dpListBackward.size() > 0) TaintResultContainer.Serialization(TaintResultContainer.getBackwardPath(), dpListBackward);
		if(dpListForward.size() > 0) TaintResultContainer.Serialization(TaintResultContainer.getForwardPath(), dpListForward);
	}
	
	private static void readOriginalDPFile() {
		String path = Constants.getOriginalDPFilePath();
		String line;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			while((line = br.readLine()) != null) {
				if(line.startsWith("%") || line.equals(""))
					continue;
				
				DPs.add(line);
			}
				
			br.close();
		}catch (IOException e) {
			System.err.println(e);
		}
	}
	
	private static void getStmtHash() {
		ExtractocolLogger.Log("Calculating hash value for target stmt ...");
		
		for(String dpSig: DPs) {
			String dp = dpSig.split(" \\-\\> \\_SOURCE\\_")[0];
			
			for (SootClass sc : Scene.v().getClasses()) {
				for (SootMethod sm : sc.getMethods()) {
					if (!sm.hasActiveBody())
						continue;
					
					for(Unit u: sm.getActiveBody().getUnits()) {
						Stmt s = (Stmt) u;
						if(!s.containsInvokeExpr())
							continue;
						
						if(!s.toString().contains(dp))
							continue;
						
						int hash = MyCallGraphBuilder.getUnitHash(u, sm);
						DPandHASH.add(new Pair2<String,Integer>(dpSig, hash));
						ExtractocolLogger.Log(u.toString() + " [Hash: " + hash + "] in " + sm.toString());
					}
				}
			}
		}
		
		ExtractocolLogger.Log("Done.");
	}
	
	private static boolean genTempDPFile() {
		// Check whether all DPs have been processed
		if(pointOfDPs == DPandHASH.size())
			return false;

		// 1. open file
		String path = Constants.getTempDPPath();
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}

		// 2. write DP info
		Pair2<String,Integer> p = DPandHASH.get(pointOfDPs++);
		pw.println(p.getE1());
		targetStmtHash = p.getE2();
		ExtractocolLogger.Log(pointOfDPs + "th DP: " + p.getE1() + " [Hash: " + p.getE2() + "]");
		
		// 3. close the file
		pw.close();
		
		return true;
	}
	
	public static void setStride(int s) { Constants.DPStride = s; }
	public static int getCurrentStmtHash() { return targetStmtHash; }
	
	public static String getCurrentDPListPath(boolean isBackward) {
		return Constants.getPartialDPListDirPath() + "/DPList_" + pointOfDPs + (isBackward? ".backward" : ".forward");
	}
	
	private static boolean Argument_Parsing(String[] args){
		//if(args.length != 1)
			//return false;
		
		start = System.currentTimeMillis();
		appName = args[0];
		Constants.setAPPName(appName);
		
		int k = 1;
		Constants.serIsForward = true;
		Constants.heapobject = false;
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
				k ++;
				continue;
			}
			k++;
		}
		
		return true;
	}
	
	private static void postProcessing() {
		// Print result
		if(Constants.printResultOfFrontend) {
			if(dpListBackward.size() > 0) epCnt += TaintResultContainer.Print(dpListBackward);
			if(dpListForward.size() > 0) epCnt += TaintResultContainer.Print(dpListForward);
		}
		
		
		// Delete temp DP file
		new File(Constants.getTempDPPath()).delete();
		
		for(DPContainer dpc: dpListBackward)
			dpc.clear();
		
		for(DPContainer dpc: dpListForward)
			dpc.clear();
		
		dpListBackward.clear();
		dpListForward.clear();
		DPs.clear();
		
		dpListBackward = null;
		dpListForward = null;
		
		// Print elapsed time
		Print_Elapsed_Time();
	}
	
	private static void Run_Backward(){
		System.out.println(appName + " - Frontend Backward");
		System.out.println("================================================\n\n\n");
		
		try{
			//Test.main(getBackwardArgument().split(" "));
			MyTest.main(getBackwardArgument().split(" "));
		}catch(IOException e){
			e.printStackTrace();
			System.out.println("Backward Analysis has been failed due to IOException.");
		}catch(InterruptedException e){
			e.printStackTrace();
			System.out.println("Backward Analysis has been failed due to InterruptedException.");
		}
	}
	
	private static void Run_Forward(){
		System.out.println("\n\n\n\n");
		System.out.println(appName + " - Frontend Forward");
		System.out.println("================================================\n\n\n");
		
		try{
			//Test.main(getForwardArgument().split(" "));
			MyTest.main(getForwardArgument().split(" "));
		}catch(IOException e){
			e.printStackTrace();
			System.out.println("Forward Analysis has been failed due to IOException.");
		}catch(InterruptedException e){
			e.printStackTrace();
			System.out.println("Forward Analysis has been failed due to InterruptedException.");
		}
	}
	
	private static void Print_Elapsed_Time(){
		end = System.currentTimeMillis();
		System.out.println("\n\n\n");
		System.out.println("=====================================================================");
		if(Constants.printResultOfFrontend)
			System.out.println("Total elapsed time: " + Backend.getTimeString((end-start)/1000.0) + "s (Total EP Count: " + epCnt + ")\n\n");
		else
			System.out.println("Total elapsed time: " + Backend.getTimeString((end-start)/1000.0) + "s\n\n");
	}
	
	private static String getBackwardArgument(){ return getBaseArgument() + " --backward"; }
	private static String getForwardArgument(){ return getBaseArgument() + " --forward"; }
	
	private static String getBaseArgument(){
		return appName + ".apk "
				+ Constants.getAndroidSDKPath()
				+ " --noexceptions"	+ " --nostatic"	+ " --aplength 0" /* aplength should be 0. (by BK)*/
				+ " --aliasflowins"	+ " --layoutmode none"	+ " --nocallbacks" + " --repeatcount 1";
	}
}
