package Extractocol.BufferExtractor_Request.Helper;

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import soot.Unit;

public class LinearSearchEntry {

	public LinearSearchEntry() {
		this.locals= new ArrayList<SootMethod>();
		this.constants= new ArrayList<Unit>();
	}
	public List<SootMethod> locals;
	public List<Unit> constants;
	
}
