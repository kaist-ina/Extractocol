package extractocol.backend.response.helper;

public class SemanticHelper {
	public static boolean isconst(String arg)
	{
		if (arg.indexOf("$") != -1)
			return false;
		else
			return true;
	}
}
