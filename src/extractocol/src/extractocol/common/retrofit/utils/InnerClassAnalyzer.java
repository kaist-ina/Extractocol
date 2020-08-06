package extractocol.common.retrofit.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import extractocol.common.retrofit.struct.Transaction;
import extractocol.common.retrofit.utils.ResponseFileAnalyzer;

public class InnerClassAnalyzer extends FileAnalyzer {
	
	@SuppressWarnings("resource")
	public static void Parser(String path, String targetInnerClass, Transaction currentTran, List<String> JSON_Key, 
			Stack<String> packageStack, List<String> extendedClassList) throws IOException {
		List<String> importList = new ArrayList<String>();
		
		BufferedReader in;
		String line;
		int lineNum = 1;
		String annotation = null;
		String className = null;
		String package_name = null;
		boolean foundInnerClass = false;
		
		// Check whether a file exists
		if(!doesFileExist(path))
			return;
		
		// to avoid stack overflow
		if(packageStack.contains(path+targetInnerClass))
			return;
		
		packageStack.push(path+targetInnerClass);
		
		// File open
		in = new BufferedReader(new FileReader(path));

		// Read the file line by line
		mainLoop:
		while ((line = in.readLine()) != null) {
			// Remove the first white space
			String lineRefined = lineRefinement(line);
			
			// print for debugging
			printLine(line, packageStack.size(), lineNum++, path);
			
			// Check line type and handle line
			switch(lineTypeChecker(line)) {
			case PACKAGE: package_name = HandlePackage(lineRefined); break;
			case IMPORT: HandleImport(lineRefined, importList); break;
			case ANNOTATION: annotation = ResponseFileAnalyzer.HandleAnnotation(lineRefined); break;
			case CLASS: className = HandleClass(lineRefined); break;
			case INNER_CLASS: if(HandleClass(lineRefined).equals(targetInnerClass)) { foundInnerClass = true; } break;
			case INNER_MEMBER_VARIABLE:
				if(foundInnerClass)
					ResponseFileAnalyzer.HandleMemberVariable(lineRefined, annotation, package_name, className+"$"+targetInnerClass, 
																currentTran, JSON_Key, importList, packageStack, extendedClassList);
			case MEMBER_VARIABLE:
				annotation = null; 
				break;
			case INNER_CLASS_END: if(foundInnerClass) break mainLoop;
			default: break;
			}
		}
		
		packageStack.pop();
		return;
	}
}
