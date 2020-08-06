package CodeInjection;


/**
 * 1. package name: CodeInjection
 * 2. type name: DP_Pair.java
 * 3. writer: Hyun Ho
 * 4. description: Hold a pair of statement and method of a DP 
 */
public class DP_Pair {
	private String Stmt;
	private String Method;
	
	public DP_Pair(){this.Stmt = null; this.Method = null;}
	public DP_Pair(String st, String mth){this.Stmt = st; this.Method = mth;}

	public String getStmt(){ return this.Stmt; }
	public String getMethod(){ return this.Method;}
}
