package extractocol.common.retrofit.utils;

import extractocol.common.retrofit.struct.Transaction;
import extractocol.common.retrofit.struct.Req.HTTP_METHOD;

public class RetrofitAnnotation {
	public static void Parser(Transaction currentTran, String line) {
		if(line.contains("@FormUrlEncoded"))
			currentTran.Request().setFormUrlEncoded(true);
		else if(line.contains("@Headers"))
			HandleHeaders(currentTran, line);
		else if(line.contains("@SerializedName"))
			return;
		else if(line.contains("@Multipart")) // Support not multi-part request currently
			return;
		else
			HandleMethod(currentTran, line);
	}
	
	private static void HandleHeaders(Transaction currentTran, String line) {
		String[] hStr = line.split("\"")[1].split(":");
		currentTran.Request().addHeader(hStr[0], hStr[1].replaceFirst("^ *", ""));
	}
	
	private static void HandleMethod(Transaction currentTran, String line) {
		// 1. Check Http method
		if(line.contains("@GET")) currentTran.Request().setHttpMethod(HTTP_METHOD.GET);
		else if(line.contains("@POST")) currentTran.Request().setHttpMethod(HTTP_METHOD.POST);
		else if(line.contains("@PUT")) currentTran.Request().setHttpMethod(HTTP_METHOD.PUT);
		else if(line.contains("@DELETE")) currentTran.Request().setHttpMethod(HTTP_METHOD.DELETE);
		else if(line.contains("@PATCH")) currentTran.Request().setHttpMethod(HTTP_METHOD.PATCH);
		else System.err.println("New HTTP Method detected: " + line);
		
		// 2. Get sub-url
		if(!line.contains("(")) return;
		currentTran.Request().setSubUrl(line.split("\"")[1]);
	}
}
