package extractocol.backend.request.heapManagement;

import java.io.Serializable;

import soot.Unit;

@SuppressWarnings("serial")
public class SourceforTaint implements Serializable 
{
	String SourceMethod;
	String SeedObject;
	Unit unit;
	
	public SourceforTaint(){
		this.SourceMethod = null;
		this.SeedObject = null;
		this.unit = null;
	}
	
	public SourceforTaint(String srcMethod, String seed, Unit u)
	{
		this.SourceMethod = srcMethod;
		this.SeedObject = seed;
		this.unit = u;
	}
	
	public void setSourceMethod(String srcMethod) { this.SourceMethod = srcMethod; }
	public void setSeedObject(String seed) { this.SeedObject = seed; }
	public void setUnit(Unit u) { this.unit = u; }
	
	public String getSourceMethod() { return SourceMethod; }
	public String getSeedObject() {	return SeedObject; }
	public Unit getUnit() { return unit;	}
}
