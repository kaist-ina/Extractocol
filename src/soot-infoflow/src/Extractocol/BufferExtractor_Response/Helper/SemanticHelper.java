package Extractocol.BufferExtractor_Response.Helper;

public class SemanticHelper {
	public static boolean isconst(String arg)
	{
		if (arg.indexOf("$") != -1)
			return false;
		else
			return true;
	}
}
