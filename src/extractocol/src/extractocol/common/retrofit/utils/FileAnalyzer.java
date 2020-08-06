package extractocol.common.retrofit.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import extractocol.common.retrofit.utils.FileAnalyzer.LineType;

public class FileAnalyzer {
	public enum LineType {PACKAGE, IMPORT, CLASS_START, CLASS_END, INNER_CLASS_START, INNER_CLASS_END, 
						  ANNOTATION, METHOD_PROTOTYPE, EMPTY, CLASS, INNER_CLASS, EXTENDS, IMPLEMENTS, 
						  MEMBER_VARIABLE, INNER_MEMBER_VARIABLE, COMMENT, NONE};
	public static String absolutePath = null;
	public static String appName = null;
	public static boolean debug = false;
	
	public static String getJavaFullPath(String packageName) {
		String str = packageName.replaceAll("\\.", "\\\\");
		if(absolutePath == null) setAbsolutePath();
		
		return absolutePath + "\\" + str + ".java";
	}
	
	public static void printLine(String line, int tabNum, int lineNum, String path) {
		if(debug) {
			LineType lt = lineTypeChecker(line);
			String tab = "";
			for(int i = 0; i < tabNum; i++)
				tab += "\t";
			System.out.println(String.format("%s%22s [%3d][%s]: %s", tab, lt, lineNum, getFileName(path), line));
		}
	}
	
	public static void setAppName(String aN) { appName = aN; }
	public static void setAbsolutePath() { absolutePath = "..\\..\\SerializationFiles\\" + appName + "\\java\\"; }
	public static String getAbsolutePath() { if(absolutePath == null) setAbsolutePath(); return absolutePath; }
	public static String lineRefinement(String line) { return line.replaceFirst("^ *", ""); }
	
	/** Check line type
	 * 
	 * @param line line string
	 * @return line type
	 */
	public static LineType lineTypeChecker(String line) {
		if(line.startsWith("package"))
			return LineType.PACKAGE;
		else if (line.startsWith("import"))
			return LineType.IMPORT;
		else if (isComment(line))
			return LineType.COMMENT;
		else if (line.equals("{"))
			return LineType.CLASS_START;
		else if (line.equals("}"))
			return LineType.CLASS_END;
		else if (isInnerClassStart(line))
			return LineType.INNER_CLASS_START;
		else if (isInnerClassEnd(line))
			return LineType.INNER_CLASS_END;
		else if (isRetrofitAnnatation(line))
			return LineType.ANNOTATION;
		else if (line.equals(""))
			return LineType.EMPTY;
		else if (line.startsWith("  extends"))
			return LineType.EXTENDS;
		else if (line.startsWith("  implements"))
			return LineType.IMPLEMENTS;
		else if (line.matches("^[^ ].*") && !line.endsWith(";") && !line.startsWith("@"))
			return LineType.CLASS;
		else if (isInnerClass(line))
			return LineType.INNER_CLASS;
		else if (isMethodPrototype(line)) 
			return LineType.METHOD_PROTOTYPE;
		else if (isMemberVariable(line))
			return LineType.MEMBER_VARIABLE;
		else if (isInnerMemberVariable(line))
			return LineType.INNER_MEMBER_VARIABLE;
		else
			return LineType.NONE;
	}
	
	public static boolean isInnerClassStart(String line) {
		if(line.matches("^(  |    )\\{"))
			return true;
		return false;
	}
	
	public static boolean isInnerClassEnd(String line) {
		if(line.matches("^(  |    )\\}"))
			return true;
		return false;
	}
	
	public static boolean isComment(String line) {
		if(line.contains("/*"))
			return true;
		if(line.contains("//"))
			return true;
		return false;
	}
	
	public static boolean isRetrofitAnnatation(String line) {
		if(!line.startsWith("  @") && !line.startsWith("    @"))
			return false;
		
		if(line.contains("@GET")
				|| line.contains("@POST")
				|| line.contains("@DELETE")
				|| line.contains("@PUT")
				|| line.contains("@PATCH"))
			return true;
		
		if(line.contains("@FormUrlEncoded")
				|| line.contains("@Headers")
				|| line.contains("@Multipart"))
			return true;
		
		if(line.contains("@SerializedName"))
			return true;
		
		return false;
	}
	
	public static String getFileName(String path) {
		String[] s = path.split("\\\\");
		return s[s.length - 1];
	}
	
	public static boolean doesFileExist(String path) {
		File f = new File(path);
		return f.exists();
	}
	
	public static boolean isMethodPrototype(String line) {
		if(!(line.matches("^  [^ ].+") && line.contains("(") && line.endsWith(";") && !line.contains("=")) // FilpagramService.java (Flipagram)
				&& !(line.matches("^    [^ ].+") && line.contains("(") && line.endsWith(";") && !line.startsWith("    return") && !line.contains("="))) // ConsumerApi.java (DoorDash)
			return false;
		
		// "param.close();" in AssetManager.java (Postmates)
		if(line.matches(".*\\..*\\([^\\(\\)]*\\)\\;"))
			return false;
		
		// "} while (localDateTime1.getMillis() < localDateTime2.getMillis());" in Customer.java (Postmates)
		if(line.matches(".*while.*\\(.*\\);"))
			return false;
		
		try {
		List<String> paramList = MethodPrototype.paramSplitter(lineRefinement(line));
		
		for(String p: paramList)
			if(!p.contains(" "))
				return false;
		}catch(Exception e) {
			System.err.println("Line: " + line);
			e.printStackTrace();
		}
		
		return true;
	}
	
	public static boolean isMemberVariable(String line) {
		if(line.matches("^  [^ ].+") 
			&& (line.contains("=") || (!line.contains("(") && !line.contains("=") && line.endsWith(";") && !line.contains("{") && !line.contains("}"))) 
			&& !line.contains(","))
			return true;
		
		return false;
	}
	
	public static boolean isInnerMemberVariable(String line) {
		if(line.contains("="))
			return false;
		
		if(line.matches("^.*this\\..*=.*;$"))
			return false;
		
		if(line.matches("^.*if.*\\(.*\\).*"))
			return false;
		
		if(line.matches("^.*(return|throw|break).*;"))
			return false;
		
		if(line.matches("^(    |      )[^ ].+") 
			&& (line.contains("=") || (!line.contains("(") && !line.contains("=") && line.endsWith(";") && !line.contains("{") && !line.contains("}"))) 
			&& !line.contains(","))
			return true;
		
		return false;
	}
	
	public static boolean isInnerClass(String line) {
		if((line.matches("^(  |    )[^ \\/].+")) && line.contains("class")
				&& !line.endsWith(";") && !line.startsWith("@") && !line.contains("("))
			return true;
		
		return false;
	}

	public static String HandleClass(String line) {
		String [] subStr = line.split(" ");
		return subStr[subStr.length - 1].split("<")[0];
	}
	
	/** PACKAGE line handler
	 * 
	 * @param line
	 */
	public static String HandlePackage(String line) {
		return line.split(" ")[1].split(";")[0];
	}
	
	/** IMPORT line handler
	 * 
	 * @param line
	 */
	public static void HandleImport(String line, List<String> importList) {
		importList.add(line.split(" ")[1].split(";")[0]);
	}
}
