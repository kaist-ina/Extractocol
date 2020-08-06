package extractocol.frontend.basic;

import java.util.LinkedList;

import extractocol.frontend.output.basic.TaintResultEntry;

public class AbstractionExtension {
	private int ID;
	private TaintResultEntry tre;
	private boolean InitialSeed = false;
	
	public void setID(int _id) { this.ID = _id; }
	public int getID() { return this.ID; }
	
	public void setTRE(TaintResultEntry _tre) { this.tre = _tre; }
	public TaintResultEntry getTRE() { return this.tre; }

	public void setIsInitialSeed(boolean _isInitialSeed) { this.InitialSeed = _isInitialSeed; }
	public boolean IsInitialSeed() { return this.InitialSeed; }
	
	public void Clear() { this.tre.Clear(); }
	
}
