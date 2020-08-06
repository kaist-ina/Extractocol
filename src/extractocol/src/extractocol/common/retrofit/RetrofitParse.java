package extractocol.common.retrofit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import extractocol.common.retrofit.utils.FileAnalyzer;
import extractocol.common.retrofit.utils.JavaFileAnalyzer;
import extractocol.frontend.basic.ExtractocolLogger;
import extractocol.Constants;
import extractocol.common.retrofit.struct.Transaction;

public class RetrofitParse {
	/**
	 * 
	 * @return True if the input app uses retrofit
	 */
	public static boolean M() {
		// 0. Check whether the java files have been already analyzed
		if(alreadyDone()) {
			ExtractocolLogger.Log("The java files have been already analyzed.");
			return true;
		}
		
		// 1. parse java files
		List<Transaction> tranList = ParseJavaFiles();
		if(tranList == null || tranList.size() == 0)
			return false;
		
		// 2. generate DP file
		if(!GenDP(tranList))
			return false;
		
		// 3. set base url of retrofit 
		setBaseUrl(tranList);
		
		// 4. save the result
		if(!SaveTransactions(tranList))
			return false;
		
		return true;
	}
	
	private static boolean alreadyDone() {
		File f1 = new File(Constants.getRetrofitTranMapFilePath());
		File f2 = new File(Constants.getRetrofitDPPath());
		return f1.exists() && f2.exists();
	}
	
	private static boolean SaveTransactions(List<Transaction> tranList) {
		Map<String, Transaction> tranMap = new HashMap<String, Transaction>();
		
		// Convert the list to hash map
		for(Transaction t: tranList)
			tranMap.put(t.getInvokeStatement(), t);
		
		// Serialize the hash map
		return Transaction.Serialize(tranMap);
	}
	
	private static void setBaseUrl(List<Transaction> tranList) {
		String baseUrl = RetrofitBaseURLTracker.getRetrofitBaseURL();
		for(Transaction t: tranList)
			t.Request().setBaseUrl(baseUrl);
	}
	
	/** Generate the retrofit DP file
	 * 
	 * @param tranList Transaction list
	 * @return True if successfully generate
	 */
	private static boolean GenDP(List<Transaction> tranList) {
		// 0. get path
		String path = Constants.getRetrofitDPPath(); 
		
		// 1. open file
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		
		// 2. write DP info
		for(Transaction t: tranList) {
			pw.println(t.getInvokeStatement() + " -> _SOURCE_");
		}
		
		// 3. close the file
		pw.close();
		
		return true;
	}
	
	/** Parse all the java files from the root directory
	 * 
	 * @return Transaction list
	 */
	private static List<Transaction> ParseJavaFiles() {
		List<Transaction> tranList = null;
		
		// 1. set appName
		FileAnalyzer.setAppName(Constants.getAPPName());
		
		// 2. load all java files
		try {
			tranList = ParseJavaFiles_Inner(FileAnalyzer.getAbsolutePath(), true /*print log if true*/);
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		// 3. return the list
		return tranList;
	}
	
	/** Parse all the java files from the root directory
	 * 
	 * @param sDir path of directory
	 * @param debug True if you want to print log
	 * @return Transaction List
	 * @throws IOException
	 */
	private static List<Transaction> ParseJavaFiles_Inner(String sDir, boolean debug) throws IOException {
		List<Transaction> tranList = new ArrayList<Transaction>();
		List<Transaction> retList;

		File[] faFiles = new File(sDir).listFiles();
		for (File file : faFiles) {
			if (file.isDirectory()) {
				retList = ParseJavaFiles_Inner(file.getAbsolutePath(), debug);
				tranList.addAll(retList);
			}else {
				retList = JavaFileAnalyzer.parser(file.getAbsolutePath());
				if(debug && retList.size() > 0)
					System.out.println(String.format("%5s: %s", retList.size(), file.getAbsolutePath()));
				tranList.addAll(retList);
			}
		}
		
		return tranList;
	}
}
