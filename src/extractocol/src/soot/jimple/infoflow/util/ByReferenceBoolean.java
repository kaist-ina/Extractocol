package soot.jimple.infoflow.util;

/**
 * Wrapper class for passing boolean values by reference
 * 
 * @author Steven Arzt
 *
 */
public class ByReferenceBoolean {
	
	public boolean value;
	
	public ByReferenceBoolean() {
		this.value = false;
	}
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}
	
}
