package extractocol.common.retrofit.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import extractocol.common.retrofit.struct.Transaction;

public class ResponseFileAnalyzer extends FileAnalyzer{
	enum VAR_TYPE {PRIMITIVE, NON_PRIMITIVE, SPECIAL}
	
	public static Transaction Parser(String packageName) {
		Transaction t = new Transaction();
		
		// 1. get path
		String fullPath = getJavaFullPath(packageName);
		
		// 2. processing
		try {
			Parser(fullPath, t, new ArrayList<String>(), new Stack<String>(), new ArrayList<String>());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return t;
	}
	
	@SuppressWarnings("resource")
	public static void Parser(String path, Transaction currentTran, List<String> JSON_Key, Stack<String> packageStack, List<String> extendedClassList) throws IOException {
		List<String> importList = new ArrayList<String>();
		
		BufferedReader in;
		String line;
		int lineNum = 1;
		String annotation = null;
		String className = null;
		String package_name = null;
		
		// Check whether a file exists
		if(!doesFileExist(path))
			return;
		
		// to avoid stack overflow
		if(packageStack.contains(path))
			return;
		
		packageStack.push(path);
		
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
			case ANNOTATION: annotation = HandleAnnotation(lineRefined); break;
			case CLASS: className = HandleClass(lineRefined); break;
			case EXTENDS: HandleExtends(lineRefined, package_name, currentTran, JSON_Key, packageStack, className, extendedClassList); break;
			case MEMBER_VARIABLE: 
				HandleMemberVariable(lineRefined, annotation, package_name, className, currentTran, JSON_Key, importList, packageStack, extendedClassList); 
				annotation = null; 
				break;
			case CLASS_END: break mainLoop;
			default: break;
			}
		}
		
		packageStack.pop();
		return;
	}
	
	public static String HandleAnnotation(String line) {
		if(!line.startsWith("@SerializedName"))
			return null;
		
		return line.split("\"")[1];
	}
	
	
	public static void HandleExtends(String line, String package_name, Transaction currentTran, List<String> JSON_Key,
			Stack<String> packageStack, String currClass, List<String> extendedClassList) throws IOException {
		// Get extends class name
		String extendsClass = line.split(" ")[1];
		
		// Get file path
		String filePath = getJavaFullPath(package_name + "." + extendsClass);
		
		// call parse() recursively
		currClass = package_name + "." + currClass;
		extendedClassList.add(currClass);
		Parser(filePath, currentTran, JSON_Key, packageStack, extendedClassList);
		extendedClassList.remove(currClass);
	}
	
	public static void HandleMemberVariable(String line, String annotation, String package_name, String class_name, Transaction currentTran, 
			List<String> JSON_Key_List, List<String> importList, Stack<String> packageStack, List<String> extendedClassList) throws IOException {
		String innerClass;
		
		// 0. Check whether it is static
		if(line.matches("^static ") || line.contains(" static "))
			return;
		
		// 1. Put the heap value as a key with the JSON key list as a value into the VET
		// 1-1. get class name
		String currClassFullName = getClassFullName(package_name, class_name);
		
		// 1-2. get variable type/name
		String varName = getVarTypeVarName(line, false);
		String varType = getVarTypeVarName(line, true);
		String varTypeAugmented = typeAugmentation(varType, package_name, importList);
		
		String JSONKey;
		if(annotation != null) { JSONKey = annotation; }
		else JSONKey = varName;
		
		// 1-3. get heap name
		String heap = getHeap(currClassFullName, varTypeAugmented, varName);
		
		// 1-4. get new JSON key list & add JSON key into the new list
		List<String> newJSON_Key_List = new ArrayList<String>(JSON_Key_List);
		newJSON_Key_List.add(JSONKey);
		
		// 1-5. put the heap value as a key with the JSON key list as a value into the VET
		currentTran.Response().VET().addNewJSONKeyList(heap, newJSON_Key_List, false);
		
		// put the heap value for the upperclass (extended by the current class)
		for(String extendedClass: extendedClassList) {
			String upperClassHeap = getHeap(extendedClass, varTypeAugmented, varName);
			currentTran.Response().VET().addNewJSONKeyList(upperClassHeap, newJSON_Key_List, false);
		}
			
		/*for(String key: newJSON_Key_List)
			currentTran.Response().VET().addJSONKey(heap, key, false);*/
		
		// 2. Parsing type class if the variable type is not a primitive type
		// 2-1. refine type name
		if (varTypeAugmented.equals("java.util.List") ||
				varTypeAugmented.equals("java.util.ArrayList") )
			varType = varType.split("<")[1].split(">")[0];
		else
			varType = varType.replaceAll("\\[\\]", "");
		
		// 2-2. check whether it is not a primitive type
		switch(isPrimitive(varType)) {
		case SPECIAL: currentTran.Response().setIsRespSigBuildingRequired(true);
		case PRIMITIVE: return;
		case NON_PRIMITIVE: break;
		}
		
		// 2-3. check whether the type is imported
		String res = isTypeInList(varType, importList);
		
		// 2-4. get full class name
		String fullName = null;
		if(res != null) fullName = res;
		else fullName = package_name + "." + varType;
		
		// 2-5. Avoid infinite recursive invocation
		if(fullName.equals(currClassFullName))
			return;
		
		// 2-5. get file path of the target class
		String filePath = getJavaFullPath(fullName);
		
		// 2-6. call parse() recursively
		if(doesFileExist(filePath))
			Parser(filePath, currentTran, newJSON_Key_List, packageStack, new ArrayList<String>());
		else if (varType.contains("$"))	{
			filePath = getJavaFullPath(varType.split("\\$")[0]);
			innerClass = varType.split("\\$")[1];
			
			InnerClassAnalyzer.Parser(filePath, innerClass, currentTran, newJSON_Key_List, packageStack, new ArrayList<String>());
		}else if(varType.contains(".")) {
			res = isTypeInList(varType.split("\\.")[0], importList);
			if(res != null)
				filePath = getJavaFullPath(res);
			else
				filePath = getJavaFullPath(getClassFullName(package_name, varType.split("\\.")[0]));
			innerClass = varType.split("\\.")[1];
			
			InnerClassAnalyzer.Parser(filePath, innerClass, currentTran, newJSON_Key_List, packageStack, new ArrayList<String>());
		}else if (res == null /*inner class if res == null (e.g., type is CancelOrderConfirmDialog in cancelOrderConfirmDialog.java - postmates)*/) {
			filePath = getJavaFullPath(currClassFullName.split("\\$")[0]);
			innerClass = varType;
			
			InnerClassAnalyzer.Parser(filePath, innerClass, currentTran, newJSON_Key_List, packageStack, new ArrayList<String>());
		}
	}
	
	private static String getClassFullName(String package_name, String class_name) {
		return package_name + "." + class_name;
	}

	/** Get variable type or name
	 * 
	 * @param line statement of member variable
	 * @param isVarType True, if you want to get variable type.
	 * @return variable type or name
	 */
	private static String getVarTypeVarName(String line, boolean isVarType) {
		String[] strSub;
		
		// Case 1: containing '='
		if(line.contains("=")) {
			strSub = line.split("=")[0].split(" ");
		}
		// Case 2: not containing '='
		else {
			strSub = line.split(" ");
			strSub[strSub.length - 1] = strSub[strSub.length - 1].split(";")[0]; // remove ';' at the end of the string
		}
		
		if(isVarType) return strSub[strSub.length - 2];
		else return strSub[strSub.length - 1].split("<")[0];
	}
	
	private static String getHeap(String classFull, String type, String varName) {
		return "<" + classFull + ": " + type + " " + varName + ">";
	}
	
	private static VAR_TYPE isPrimitive(String varType) {
		// 0. refine the varType
		varType = varType.split("<")[0]; // split("<") is for some types (e.g., Response<?> in HttpException of Flipagram)
		
		// 1. check whether it is a special case ( meaning the response sig. building is required )
		if(isSpecialType(varType))
			return VAR_TYPE.SPECIAL;
		
		// 2. check whether it is primitive
		else if(isPrimitiveType_inner(varType))
			return VAR_TYPE.PRIMITIVE;
		
		// 3. others non primitive
		else
			return VAR_TYPE.NON_PRIMITIVE;
	}
	
	private static boolean isSpecialType(String varType) {
		varType = varType.replaceAll("\\[\\]", "").split("<")[0]; // need to remove '[]' (e.g., short[], Integer[], ...)
		
		return varType.equals("JsonNode");
	}
	
	private static boolean isPrimitiveType_inner(String varType) {
		varType = varType.replaceAll("\\[\\]", "").split("<")[0]; // need to remove '[]' (e.g., short[], Integer[], ...)
		
		return isPrimitiveType(varType) 
				|| isPrimitiveType_Upper(varType)
				|| isPrimitiveType_Others(varType);
	}
	
	private static boolean isPrimitiveType(String varType) {
		varType = varType.replaceAll("\\[\\]", "").split("<")[0]; // need to remove '[]' (e.g., short[], Integer[], ...)
		
		return varType.equals("short")
				|| varType.equals("byte")
				|| varType.equals("long")
				|| varType.equals("float")
				|| varType.equals("int")
				|| varType.equals("double")
				|| varType.equals("boolean")
				|| varType.equals("char");
	}
	
	private static boolean isPrimitiveType_Upper(String varType) {
		varType = varType.replaceAll("\\[\\]", "").split("<")[0]; // need to remove '[]' (e.g., short[], Integer[], ...)
		
		return varType.equals("String")
				|| varType.equals("Short")
				|| varType.equals("Byte")
				|| varType.equals("Long")
				|| varType.equals("Float")
				|| varType.equals("Integer")
				|| varType.equals("Double")
				|| varType.equals("Boolean");
	}
	
	private static boolean isPrimitiveType_Others(String varType) {
		varType = varType.replaceAll("\\[\\]", "").split("<")[0]; // need to remove '[]' (e.g., short[], Integer[], ...)
		
		return varType.equals("Date")
				|| varType.equals("Uri")
				|| varType.equals("BigDecimal");
	}
	
	/** get full class package name if exist
	 * 
	 * @param type type name
	 * @param list import list
	 * @return the full class package name if it exists
	 */
	private static String isTypeInList(String type, List<String> list) {
		for(String e: list) {
			if (e.endsWith("." + type))
				return e;
		}
		
		return null;
	}
	
	public static String typeAugmentation(String varType, String package_name, List<String> importList) {
		varType = varType.split("<")[0]; // split("<") is for some types (e.g., List<Asset> in RichTest of Flipagram, Response<?> in HttpException of Flipagram, ...)
		
		// 0. Check if it is 'void' or 'object'
		if(isVoid(varType))
			return varType;
		
		// 1. Check whether the type exists in the import list
		for(String e: importList) {
			if(e.endsWith("." + varType))
				if(!varType.contains("."))
					return e;
				else
					// a case for varType with '.' 
					// (e.g., @Path("duration") AccuDuration.DailyForecastDuration paramDailyForecastDuration in AccuDailyForecastAPI.java)
					return e.replace(varType, varType.replace(".", "$")); 
		}
		
		// 2. Check whether type is primitive
		if(isPrimitiveType(varType))
			return varType;
		
		if(isPrimitiveType_Upper(varType))
			return "java.lang." + varType;

		// 3. others
		return package_name + "." + varType;
	}
	
	public static boolean isVoid(String varType) {
		if(varType == null)
			return true;
		
		if(varType.equalsIgnoreCase("void")
				|| varType.equalsIgnoreCase("object"))
			return true;
		
		return false;
	}
	
	/*public static void test(String b, int i) {
		String a = b + i;
		System.out.println("a: " + a);
		
		if( i < 5)
			test(a, ++i);
		
		System.out.println("a: " + a);
	}*/
}
