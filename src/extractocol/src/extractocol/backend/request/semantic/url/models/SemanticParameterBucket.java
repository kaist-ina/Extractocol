package extractocol.backend.request.semantic.url.models;

import java.util.ArrayList;
import java.util.HashMap;

import extractocol.backend.request.basics.BFNode;
import extractocol.backend.request.basics.ParameterBucket;
import extractocol.backend.request.semantic.url.UrlBuilder;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.StaticInvokeExpr;

public class SemanticParameterBucket
{
	public InstanceInvokeExpr iie;
	public HashMap<String, ArrayList<BFNode>> BFTtable;
	public String strDst;
	public UrlBuilder ub;
	public Unit ut;
	public SootMethod sm;
	public String methodref;
	public StaticInvokeExpr sie;
	public ParameterBucket CurrentPB;
	
	public SemanticParameterBucket(InstanceInvokeExpr _iie, HashMap<String, ArrayList<BFNode>> _BFTtable, String _strDst, UrlBuilder _ub, Unit _ut , SootMethod _sm, String _methodref, ParameterBucket pb)
	{
		iie = _iie;
		BFTtable = _BFTtable;
		strDst = _strDst;
		ub = _ub;
		ut = _ut;
		sm = _sm;
		methodref = _methodref;
		CurrentPB = pb;
	}
	
	public SemanticParameterBucket(StaticInvokeExpr _sie, HashMap<String, ArrayList<BFNode>> _BFTtable, String _strDst, UrlBuilder _ub, Unit _ut , SootMethod _sm, String _methodref, ParameterBucket pb)
	{
		sie = _sie;
		BFTtable = _BFTtable;
		strDst = _strDst;
		ub = _ub;
		ut = _ut;
		sm = _sm;
		methodref = _methodref;
		CurrentPB = pb;
	}
}
