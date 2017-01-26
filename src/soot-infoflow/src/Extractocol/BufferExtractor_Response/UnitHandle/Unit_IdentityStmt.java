package Extractocol.BufferExtractor_Response.UnitHandle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.IdentityStmt;
import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Basics.ContextEntry;
import Extractocol.BufferExtractor_Request.Basics.ContextTable;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapHandler;
import Extractocol.BufferExtractor_Response.Basics.BFNode_Response;
import Extractocol.BufferExtractor_Response.Basics.ParameterBucket;
import Extractocol.BufferExtractor_Response.Helper.TaintHelper;
import Extractocol.BufferExtractor_Response.Helper.UtilHelper;

public class Unit_IdentityStmt {

	public static ParameterBucket static_pb;
	public static String strDest = null;

	public static boolean isForward = false;

	public static void handleUnit(ParameterBucket pb) {
		static_pb = pb;
		
		Unit ut = static_pb.ut;
		HashMap<String, ArrayList<BFNode_Response>> BFTtable = static_pb.BFTtable;
		SootMethod sm = static_pb.sm;

		IdentityStmt ds = (IdentityStmt) ut;
		strDest = ds.getLeftOp().toString();
		ArrayList<BFNode_Response> list = new ArrayList<BFNode_Response>();

		if (TaintHelper.isDpStmt(sm, ut))
		{
			TaintHelper.seedGenerate(strDest, static_pb);
		}
		
		if (!UtilHelper.hasNode(BFTtable, strDest))
		{
			BFTtable.put(strDest, list);
		}

		if (ds.getRightOp().toString().indexOf("@parameter") != -1)
		{
			String param = ds.getRightOp().toString().split(":")[0];
			TaintHelper.tryVariableTaint(strDest, param, static_pb);
			
			// BK Put the constant value into the strDest if the constant value of the parameter exists. 
			if(static_pb.BFTtable.get(param) != null)
				static_pb.BFTtable.put(strDest, static_pb.BFTtable.get(param));
			
			// BK Update variable type if the corresponding parameter was Jackson_Key
			if(static_pb.variable_type.get(param) != null){
				if(static_pb.variable_type.get(param).equals("Jackson_Key")){
					static_pb.variable_type.put(strDest, "Jackson_Key");
				} else if (static_pb.variable_type.get(param).equals("Jackson_Parser"))
				{
					static_pb.variable_type.put(strDest, "Jackson_Parser");
				}
			}
			
			// BK set Value Entry list of parameter to destination variable
			static_pb.ParameterIdentityStmtHandler(ds, param);
		}
		
		if (ds.getRightOp().toString().indexOf("@this") != -1)
		{
			ThisObjectHandler(ds);
		}
		
		// Keep the variable type (left side)
		// By Byungkwon
		if (pb.variable_type.get(strDest) == null)
			pb.variable_type.put(strDest, ds.getLeftOp().getType().toString());
	}
	
	private static void ThisObjectHandler(IdentityStmt ds){
		String param = ds.getRightOp().toString().split(":")[0];
		TaintHelper.tryVariableTaint(strDest, param, static_pb);
		
		// For tracking the list of value entries
		static_pb.ThisObjectHandler(ds);
	}
}
