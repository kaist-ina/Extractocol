package Extractocol.Pairing;


public class SemanticAppliedEntry {
	private String Method;
	private String Stmt;
	public SemanticAppliedEntry(String _EP_SM, String _EPStmt)
	{
		this.Method = _EP_SM;
		this.Stmt = _EPStmt;
	}
	public String getMethod()
	{
		return this.Method;
	}
	
	public void setMethod(String method)
	{
		this.Method = method;
	}
	
	public String getStmt()
	{
		return this.Stmt;
	}
	
	public void setStmt(String stmt)
	{
		this.Stmt = stmt;
	}
	
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean isSame(SemanticAppliedEntry sae) {
		if(this.Method.equals(sae.getMethod()) && this.Stmt.equals(sae.getStmt())) {
			return true;
		}
		return false;
	}
}