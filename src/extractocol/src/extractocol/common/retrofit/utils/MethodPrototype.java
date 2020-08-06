package extractocol.common.retrofit.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import extractocol.common.retrofit.struct.Param;
import extractocol.common.retrofit.struct.Transaction;
import extractocol.common.retrofit.struct.Param.PARAM_TYPE;

public class MethodPrototype {
	public static List<String> paramSplitter(String line){
		// 1. Get the parameter part
		String subStr = line.split("\\(", 2)[1];
		subStr = subStr.substring(0, subStr.length() - 2);
		
		if(subStr.equals(""))
			return new ArrayList<String>();
		
		// 2. Add all parameters into the list
		return parseParameterString(subStr);
	}
	
	private static List<String> parseParameterString(String paramString){
		String[] subStr = paramString.split(", ");
		if(!paramString.contains("<"))
			return Arrays.asList(subStr);
		
		List<String> params = new ArrayList<String>();
		for(int i = 0; i < subStr.length; i++) {
			if(subStr[i].contains("<") && !subStr[i].contains(">")) {
				params.add(subStr[i] + ", " + subStr[i+1]);
				i++;
			}else
				params.add(subStr[i]);
		}
		
		return params;
	}
	
	public static String getMethodName(String line) {
		String[] res = line.split(" ");
		for(int i = 0; i < res.length; i++) {
			if(res[i].contains("("))
				return res[i].split("\\(")[0];
		}
		
		return null;
	}
	
	public static void paramParser(Transaction currentTran, List<String> paramList, String package_name, List<String> importList) {
		// e.g.) @Path("flipagramId") String paramString
		// e.g.) @Body Map<String, Object> paramMap
		// e.g.) @Nullable @Query("afterCursor") String paramString2
		// e.g.) @NonNull @QueryMap Map<String, Object> paramMap
		
		for(String param: paramList) {
			Param p = new Param();
			String paramType = null;
			String[] paramSub;
			
			currentTran.addParam(p);
			
			// 1. param type
			if(param.contains(", ")) {
				paramSub = param.split(" ");
				paramType = paramSub[paramSub.length - 3].split("<")[0];
			}else {
				paramSub = param.split(" ");
				paramType = paramSub[paramSub.length - 2].split("<")[0];
			}
				
			paramType = ResponseFileAnalyzer.typeAugmentation(paramType, package_name, importList);
			p.setType(paramType);
						
			// 2. Retrofit type
			if(param.contains("@Path")) p.setRetrofitType(PARAM_TYPE.PATH);
			else if(param.contains("@QueryMap")) { p.setRetrofitType(PARAM_TYPE.QUERY_MAP); continue; }
			else if(param.contains("@Query")) p.setRetrofitType(PARAM_TYPE.QUERY);
			else if(param.contains("@Field")) p.setRetrofitType(PARAM_TYPE.FIELD);
			else if(param.contains("@Body")) { p.setRetrofitType(PARAM_TYPE.BODY); continue; }
			else if(param.contains("@Url")) {p.setRetrofitType(PARAM_TYPE.URL); continue; }
			else if(param.contains("@Part")) {p.setRetrofitType(PARAM_TYPE.PART); }
			
			// 3. keyword
			if(param.contains("\""))
				p.setKeyword(param.split("\"")[1]);
			
			// 4. check nullable-ness
			if(param.contains("@Nullable")) 
				p.setIsNullable(true);
			
			if(param.contains("Callback<")) {
				String cType = param.split(" ")[0];
				String cTypeInnerMost = ResponseFileAnalyzer.typeAugmentation(getInnerMost(cType), package_name, importList);
				
				currentTran.Response().setCallBackType(cTypeInnerMost);
			}
		}
	}
	
	public static void setReturnType(Transaction currentTran, String line, String package_name, List<String> importList) {
		// ex) Observable<ApiResponse> 
		// ex) Single<Response<Promo>>
		// ex) Single<Response<List<Promo>>>
		String retType = returnTypeParser(line);
		String outerMost = getOuterMost(retType);
		String innerMost = getInnerMost(retType);
		
		if(outerMost != null) outerMost = ResponseFileAnalyzer.typeAugmentation(outerMost, package_name, importList);
		if(innerMost != null) innerMost = ResponseFileAnalyzer.typeAugmentation(innerMost, package_name, importList);
		
		currentTran.Response().setRetTypeOutermost(outerMost);
		currentTran.Response().setRetTypeInnermost(innerMost);
	}
	
	private static String returnTypeParser(String line) {
		String[] subStr = line.split("\\(", 2)[0].split(" ");
		return subStr[subStr.length - 2];
	}
	
	/** Get outermost type from retType
	 *  e.g.) 'Observable' from 'Observable<ApiResponse>'
	 *  e.g.) 'Single' from 'Single<Response<List<Promo>>>'
	 * 
	 * @param retType whole return type
	 * @return outermost type
	 */
	private static String getOuterMost(String retType) {
		return retType.split("<")[0];
	}
	
	/** Get innermost type from retType
	 *  e.g.) 'ApiResponse' from 'Observable<ApiResponse>'
	 *  e.g.) 'Promo' from 'Single<Response<List<Promo>>>'
	 * 
	 * @param retType whole return type
	 * @return innermost type
	 */
	private static String getInnerMost(String retType) {
		Pattern pat = Pattern.compile("<[^<^>]+>");
		Matcher mat = pat.matcher(retType);
		
		if(!mat.find())
			return null;
		
		return mat.group().split("<")[1].split(">")[0];
	}
	
	public static void responseParser(Transaction currentTran, String currentClass) throws IOException {
		// 0. Type check
		String type = currentTran.Response().hasRetTypeInnermost()? currentTran.Response().getRetTypeInnermost() : currentTran.Response().getRetTypeOutermost();
		boolean isRetTypeVoid = ResponseFileAnalyzer.isVoid(type);
		if(isRetTypeVoid && !currentTran.Response().hasCallBackType())
			return;
		
		// 1. get path
		String path = (!isRetTypeVoid)? FileAnalyzer.getJavaFullPath(type)
				: FileAnalyzer.getJavaFullPath(currentTran.Response().getCallBackType());
		
		// 2. parse response 
		if(FileAnalyzer.doesFileExist(path))
			ResponseFileAnalyzer.Parser(path, currentTran, new ArrayList<String>(), new Stack<String>(), new ArrayList<String>());
		else {
			String[] s = (!isRetTypeVoid)? type.split("\\.") : currentTran.Response().getCallBackType().split("\\$");
			String innerClass = s[s.length - 1];
			path = FileAnalyzer.getJavaFullPath((!isRetTypeVoid)? currentClass : s[0]);
			InnerClassAnalyzer.Parser(path, innerClass, currentTran, new ArrayList<String>(), new Stack<String>(), new ArrayList<String>());
		}
			
	}
}
