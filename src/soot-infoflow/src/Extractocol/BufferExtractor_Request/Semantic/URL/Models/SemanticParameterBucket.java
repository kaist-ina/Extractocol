package Extractocol.BufferExtractor_Request.Semantic.URL.Models;

import java.util.ArrayList;
import java.util.HashMap;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.UrlBuilder;
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
	
	public SemanticParameterBucket(InstanceInvokeExpr _iie, HashMap<String, ArrayList<BFNode>> _BFTtable, String _strDst, UrlBuilder _ub, Unit _ut , SootMethod _sm, String _methodref)
	{
		iie = _iie;
		BFTtable = _BFTtable;
		strDst = _strDst;
		ub = _ub;
		ut = _ut;
		sm = _sm;
		methodref = _methodref;
	}
	
	public SemanticParameterBucket(StaticInvokeExpr _sie, HashMap<String, ArrayList<BFNode>> _BFTtable, String _strDst, UrlBuilder _ub, Unit _ut , SootMethod _sm, String _methodref)
	{
		sie = _sie;
		BFTtable = _BFTtable;
		strDst = _strDst;
		ub = _ub;
		ut = _ut;
		sm = _sm;
		methodref = _methodref;
	}
}
