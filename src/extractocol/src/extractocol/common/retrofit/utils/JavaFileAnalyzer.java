package extractocol.common.retrofit.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import extractocol.common.retrofit.struct.Transaction;
import extractocol.common.retrofit.utils.FileAnalyzer.LineType;

public class JavaFileAnalyzer extends FileAnalyzer{
	private static boolean tranCreated = false; // True if an object of the class 'Transaction' has been created and not closed yet
	private static Transaction currentTran;
	
	@SuppressWarnings("resource")
	public static List<Transaction> parser(String path) throws IOException {
		String package_name = null;
		String outermostClassName = null;
		String innerClassName = null;
		List<String> importList = new ArrayList<String>();
		List<Transaction> tranList = new ArrayList<Transaction>();
		BufferedReader in;
		String line;
		int lineNum = 1;
		tranCreated = false;
		
		// Check whether a file exists
		if (!doesFileExist(path))
			return tranList;
				
		// File open
		in = new BufferedReader(new FileReader(path));

		// Read the file line by line
		while ((line = in.readLine()) != null) {
			// Remove the first white space
			String lineRefined = lineRefinement(line);
			
			// print for debugging
			printLine(line, 0, lineNum++, path);
			
			// Check line type and handle line
			switch(lineTypeChecker(line)) {
			case PACKAGE: package_name = HandlePackage(lineRefined); break;
			case IMPORT: HandleImport(lineRefined, importList); break;
			case CLASS: outermostClassName = HandleClass(lineRefined); break;
			case INNER_CLASS: innerClassName = HandleClass(lineRefined); break;
			case CLASS_START: if(!CheckIfRetrofitInterface(importList)) {return tranList;} break;
			case CLASS_END: return tranList;
			case INNER_CLASS_END: innerClassName = null; break;
			case ANNOTATION: HandleRetrofitAnnotation(lineRefined); break;
			case METHOD_PROTOTYPE: HandleMethodPrototype(lineRefined, package_name, importList, outermostClassName, innerClassName, tranList); break;
			default: break;
			}
		}
		
		return tranList;
	}
	
	
	
	/** Check whether the importList contains retrofit HTTP method
	 * 
	 * @return True if the importList contains retrofit HTTP method
	 */
	private static boolean CheckIfRetrofitInterface(List<String> importList) {
		for(String e: importList) {
			if(!e.startsWith("retrofit"))
				continue;
			
			if(e.endsWith("GET")) return true;
			else if(e.endsWith("POST")) return true;
			else if(e.endsWith("PUT")) return true;
			else if(e.endsWith("DELETE")) return true;
			else if(e.endsWith("PATCH")) return true;
		}
		
		return false;
	}
	
	/** RETROFIT_ANNOTATION line handler
	 * 
	 * @param line
	 */
	private static void HandleRetrofitAnnotation(String line) {
		// 1. Create an object of the class 'Transaction' if possible
		if(!tranCreated) {
			currentTran = new Transaction();
			tranCreated = true;
		}
		
		// 2. Parse the line
		RetrofitAnnotation.Parser(currentTran, line);
	}
	
	/** METHOD_PROTOTYPE line handler
	 * 
	 * @param line
	 */
	private static void HandleMethodPrototype(String line, String package_name, List<String> importList, String outerMostClassName, String innerClassName, List<Transaction> tranList) throws IOException {
		// The line is method prototype but not for retrofit if tranCreated is false here
		if(!tranCreated)
			return;
		
		try {
			// 1-1. Split parameters from the line
			List<String> paramList = MethodPrototype.paramSplitter(line);
			
			// 1-2. Parse each parameter
			MethodPrototype.paramParser(currentTran, paramList, package_name, importList);
			
			// 2-1. Parse return type
			MethodPrototype.setReturnType(currentTran, line, package_name, importList);
			
			// 2-2. Parse response
			MethodPrototype.responseParser(currentTran, package_name + "." + outerMostClassName);
			
			// Generate invoke statement
			String methodName = MethodPrototype.getMethodName(line);
			currentTran.GenInvokeStatement(methodName, package_name, outerMostClassName, innerClassName);
		}catch (Exception e) {
			System.err.println("Line: " + line);
			e.printStackTrace();
		}
		
		// Finalize the transaction
		tranCreated = false;
		tranList.add(currentTran);
	}
}
