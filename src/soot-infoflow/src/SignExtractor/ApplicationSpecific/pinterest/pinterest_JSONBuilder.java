/*package SignExtractor.ApplicationSpecific.pinterest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import soot.BooleanType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.PrimType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.parser.JimpleAST;
import soot.toolkits.graph.Block;
import SignExtractor.BFNode;
import SignExtractor.Constants;
import SignExtractor.EquvNode;
import SignExtractor.FakeScene;
import SignExtractor.PairingDPEntry;
import SignExtractor.PairingEPEntry;

import com.gaurav.tree.NodeNotFoundException;
import com.gaurav.tree.Tree;

public class pinterest_JSONBuilder extends pinterest_BufferExtractor
{
	private boolean isMulti;
	private String strDest;
	public static HashMap<String, Set<String>> MyCFG;
	public Set<String> VisitableForJacksonObject = new HashSet<String>();

	public pinterest_JSONBuilder()
	{
		methodlist = Arrays.asList("<init>", "accumulate", "setEntity", "setHeader", "toString", "append", "put", "getString", "has", "getJSONArray", "getJSONObject",
				"optString", "optBoolean", "optInt", "optLong", "optJSONObject", "getInt", "getLong", "fromJson", "optJSONArray", "execute", "putOpt", "putOnce", "opt",
				"getBoolean", "get", "readValue");

	}

	public class MethodIds
	{
		public static final int init = 0;
		public static final int accumulate = 1;
		public static final int setEntity = 2;
		public static final int setHeader = 3;
		public static final int toString = 4;
		public static final int append = 5;
		public static final int put = 6;
		public static final int getString = 7;
		public static final int has = 8;
		public static final int getJSONArray = 9;
		public static final int getJSONObject = 10;
		public static final int optString = 11;
		public static final int optBoolean = 12;
		public static final int optInt = 13;
		public static final int optLong = 14;
		public static final int optJSONObject = 15;
		public static final int getInt = 16;
		public static final int getLong = 17;
		public static final int fromJson = 18;
		public static final int optJSONArray = 19;
		public static final int execute = 20;
		public static final int putOpt = 21;
		public static final int putOnce = 22;
		public static final int opt = 23;
		public static final int getBoolean = 24;
		public static final int get = 25;
		public static final int readValue = 26;
	}

	public class Vtypes
	{
		public static final int vint = 0;
		public static final int vstring = 1;
		public static final int vjsonarray = 2;
		public static final int vconst = 3;
	}

	private void AddStatement(InstanceInvokeExpr iie, SootMethod sm)
	{

		// try {
		// if (isMulti)
		// {
		// if (!pi.iscomplete && !OriginDummyname.equals(sm.getName()))
		// {
		// // pi.Methodinfo.STMT.add(iie.getMethodRef().toString());
		// pi.Methodinfo.STMT.add(iie.getMethodRef().toString());
		// pi.Methodinfo.inMethodName.add(iiCfg.getMethodOf(sm.retrieveActiveBody().getUnits().getFirst()).toString());
		// pi.Methodinfo.myParent.add(sm.getName());
		// pi.getCallMethods().add(iiCfg.getMethodOf(sm.retrieveActiveBody().getUnits().getFirst()).toString());
		// }
		// } else {
		// if (!pi.iscomplete)
		// {
		// // pi.Methodinfo.STMT.add(iie.getMethodRef().toString());
		// pi.Methodinfo.STMT.add(iie.getMethodRef().toString());
		// pi.Methodinfo.inMethodName.add(iiCfg.getMethodOf(sm.retrieveActiveBody().getUnits().getFirst()).toString());
		// pi.Methodinfo.myParent.add(sm.getName());
		// pi.getCallMethods().add(iiCfg.getMethodOf(sm.retrieveActiveBody().getUnits().getFirst()).toString());
		// }
		// }
		// } catch (Exception e) {
		// System.out.println(sm.toString());
		// }
	}
	
	public void setIsForward(boolean f) {
		this.isForward = f;
	}

	public void start(SootClass sc, JimpleAST jast) throws Exception
	{
		this.methodlist = Arrays.asList("<init>", "accumulate", "setEntity", "setHeader", "toString", "append", "put", "getString", "has", "getJSONArray",
				"getJSONObject", "optString", "optBoolean", "optInt", "optJSONArray");

		// this.StartFingerprint(sc, jast);

		// jb.TrackingReg = "$r27";
		// jb.TrackingReg = "$r9";
		// jb.StartFingerprint("D:\\Github\\soot\\systests\\programslices\\OAuth2GetAccessToken.jimple");
		// jb.printBuffer(jb.hmBuffer, "$r9");
		// jb.TrackingReg = "$r9";
		// jb.StartFingerprint("D:\\Github\\soot\\systests\\programslices\\4.jimple");

	}

	@Override
	public boolean isContainDP(Block block)
	{
		System.out.println("error! is isContainDP not implemented. - Hun Namkoong 20150825");
		System.exit(1);
		return false;
	}

	private void GenRegx(HashMap<String, EquvNode> EQtable, String key)
	{
		EquvNode en = EQtable.get(key);
		boolean isExistPrev = false;

		if (en == null)
			return;

		HashMap<String, ArrayList<BFNode>> ENtable = en.getENtable();
		ArrayList<BFNode> al = null;
		int i = 1;

		// Receive message
		if (this.isForward)
		{
			StringBuilder sbBody = new StringBuilder();
			// for weather notification

			// ArrayList<String> mylist = new
			// ArrayList<>(pi.Methodinfo.myParent);
			// for radinator
			// if (mylist.size() >= 2)
			// {
			// String myMethod = mylist.get(mylist.size()-1);
			// String PrevMethod = mylist.get(mylist.size()-2);
			//
			// if (!MyCFG.get(myMethod).equals(MyCFG.get(PrevMethod)) &&
			// !MyCFG.get(myMethod).contains(PrevMethod)) {
			// pi.iscomplete = true;
			// PairingInfo newPi = new PairingInfo();
			// newPi.isreq = true;
			// newPi.Methodinfo.Method = null;
			//
			// int idx = 0;
			// int deleteidx = 0;
			//
			// for (; idx < pi.Methodinfo.inMethodName.size(); idx++)
			// {
			// if (pi.Methodinfo.inMethodName.get(idx).equals(myMethod))
			// {
			// break;
			// }
			// }
			// deleteidx = idx;
			// pi.Methodinfo.Method =
			// iiCfg.getMethodOf(sm.retrieveActiveBody().getUnits().getFirst()).toString();
			//
			// ArrayList<String> STMTlist = new ArrayList<>(pi.Methodinfo.STMT);
			// for (; idx < pi.Methodinfo.STMT.size(); idx++)
			// {
			// newPi.Methodinfo.inMethodName.add(pi.Methodinfo.inMethodName.get(idx));
			// newPi.Methodinfo.STMT.add(STMTlist.get(idx));
			// }
			//
			// newPi.isreq = pi.isreq;
			// newPi.Methodinfo.Method =
			// iiCfg.getMethodOf(this.globalsc.getMethodByName(
			// pi.Methodinfo.inMethodName.get(idx-1)).retrieveActiveBody().getUnits().getFirst()).toString();
			// this.Pairingdata.add(newPi);
			//
			//
			// for (int i1 = 0 ; i1 < deleteidx-1; i1++ ) {
			// pi.Methodinfo.STMT.remove(i1);
			// pi.Methodinfo.inMethodName.remove(i1);
			// i1-=1;
			// deleteidx-=1;
			// }
			// deleteidx-=1;
			// for (; deleteidx < pi.Methodinfo.STMT.size(); deleteidx++) {
			// if
			// (!pi.Methodinfo.inMethodName.get(deleteidx).equals(PrevMethod)) {
			// pi.Methodinfo.STMT.remove(deleteidx);
			// pi.Methodinfo.inMethodName.remove(deleteidx);
			// deleteidx-=1;
			// }
			// }
			//
			// this.pi = newPi;
			// }
			// }
			al = ENtable.get("String");
			if (!al.isEmpty())
			{
				sbBody.append("((");
				// System.out.print("((");
				for (BFNode bfn : al)
				{
					sbBody.append("(" + bfn.getKey() + ")");
					// System.out.print("(" + bfn.getKey() + ")");
					if (al.size() > i)
					{
						sbBody.append("|");
						// System.out.print("|");
					} else
					{
						sbBody.append("(.*))");
						sbBody.append("\\s*:\\s*(");
						// System.out.print(")");
						// System.out.print("\\s*:\\s*(");
					}
					i++;
				}
				// System.out.print("\"[~`!@\\[\\]{}#$%^&*()\\w\\h-.]*\")[,\\s]*)");
				// System.out.print("\"?(\\w|[^\",])*\"?)[,\\s]*)");
				sbBody.append("[^,s]*))");
				// System.out.print("\"?(\\w)*\"?)[,\\s]*)");
				isExistPrev = true;
			}
			// if ( !al.isEmpty() )

			i = 1;
			al = ENtable.get("Const");
			if (!al.isEmpty())
			{

				if (isExistPrev)
				{
					sbBody.append("|");
					// System.out.print("|");
				}

				for (BFNode bfn : al)
				{
					sbBody.append("(" + bfn.getKey() + "\\s*:\\s*" + bfn.getValue());
					// System.out.print("(" + bfn.getKey() + "\\s*:\\s*"
					// + bfn.getValue());
					// if ( al.size() > i )
					sbBody.append(",*");
					// System.out.print(",*");

					if (al.size() > i)
					{
						sbBody.append(")|");
						// System.out.print(")|");
					} else
					{
						sbBody.append(")");
						// System.out.print(")");
					}
					i++;
				}
			}

			i = 1;
			al = ENtable.get("int");
			if (!al.isEmpty())
			{

				if (isExistPrev)
				{
					sbBody.append("|");
					// System.out.print("|");
				}

				for (BFNode bfn : al)
				{
					sbBody.append("(" + bfn.getKey() + "\\s*:\\s*" + "[0-9]*");

					// System.out.print("(" + bfn.getKey() + "\\s*:\\s*"
					// + "[0-9]*");
					// if ( al.size() > i )
					// System.out.print(",*");
					sbBody.append(",*");

					if (al.size() > i)
					{
						sbBody.append(")|");
						// System.out.print(")|");
					} else
					{
						sbBody.append(")");
						// System.out.print(")");
					}
					i++;
				}
			}

			al = ENtable.get("JSONArray");
			if (!al.isEmpty())
			{

				for (BFNode bfn : al)
				{
					sbBody.append("|(" + bfn.getKey() + "\\s*:\\s*\\[");
					// System.out.print("|(" + bfn.getKey() + "\\s*:\\s*\\[");
					GenRegx(EQtable, bfn.getKey());
					sbBody.append("\\])");
					// System.out.print("\\])");
				}
				sbBody.append(",");
				// System.out.print(",");
			}
			// regex call after
			AddBody(sbBody.toString());
		}

		else if (!this.isForward)
		{
			StringBuilder sbBody = new StringBuilder();

			al = ENtable.get("String");
			if (!al.isEmpty())
			{
				sbBody.append("((");
				// System.out.print("((");
				for (BFNode bfn : al)
				{
					sbBody.append("(" + bfn.getKey() + ")");
					// System.out.print("(" + bfn.getKey() + ")");
					if (al.size() > i)
					{
						sbBody.append("|");
						// System.out.print("|");
					} else
					{
						sbBody.append("|(.*))");
						sbBody.append("\\s*:\\s*(");
						// System.out.print(")");
						// System.out.print("\\s*:\\s*(");
					}
					i++;
				}
				// System.out.print("\"[~`!@\\[\\]{}#$%^&*()\\w\\h-.]*\")[,\\s]*)");
				// System.out.print("\"?(\\w|[^\",])*\"?)[,\\s]*)");
				sbBody.append("[^,]*))");
				// System.out.print("\"?(\\w)*\"?)[,\\s]*)");
				isExistPrev = true;
			}
			// if ( !al.isEmpty() )

			i = 1;
			al = ENtable.get("Const");
			if (!al.isEmpty())
			{

				if (isExistPrev)
				{
					sbBody.append("|");
					// System.out.print("|");
				}

				for (BFNode bfn : al)
				{
					sbBody.append("(" + bfn.getKey() + "\\s*:\\s*" + bfn.getValue());
					// System.out.print("(" + bfn.getKey() + "\\s*:\\s*"
					// + bfn.getValue());
					// if ( al.size() > i )
					sbBody.append(",*");
					// System.out.print(",*");

					if (al.size() > i)
					{
						sbBody.append(")|");
						// System.out.print(")|");
					} else
					{
						sbBody.append(")");
						// System.out.print(")");
					}
					i++;
				}
			}

			i = 1;
			al = ENtable.get("int");
			if (!al.isEmpty())
			{

				if (isExistPrev)
				{
					sbBody.append("|");
					// System.out.print("|");
				}

				for (BFNode bfn : al)
				{
					sbBody.append("(" + bfn.getKey() + "\\s*:\\s*" + "[0-9]*");

					// System.out.print("(" + bfn.getKey() + "\\s*:\\s*"
					// + "[0-9]*");
					// if ( al.size() > i )
					// System.out.print(",*");
					sbBody.append(",*");

					if (al.size() > i)
					{
						sbBody.append(")|");
						// System.out.print(")|");
					} else
					{
						sbBody.append(")");
						// System.out.print(")");
					}
					i++;
				}
			}

			al = ENtable.get("JSONArray");
			if (!al.isEmpty())
			{

				for (BFNode bfn : al)
				{
					sbBody.append("|(" + bfn.getKey() + "\\s*:\\s*\\[");
					// System.out.print("|(" + bfn.getKey() + "\\s*:\\s*\\[");
					GenRegx(EQtable, bfn.getKey());
					sbBody.append("\\])");
					// System.out.print("\\])");
				}
				sbBody.append(",");
				// System.out.print(",");
			}
			// regex call after
			AddBody(sbBody.toString());
		}

		// else if (!this.isForward) {
		//
		// StringBuilder sbBody = new StringBuilder();
		// // Send message
		// sbBody.append("{?(");
		// // System.out.print("{?(");
		//
		// al = ENtable.get("String");
		// boolean chkNullval = false;
		// if (!al.isEmpty()) {
		// if (TrackingReg == key) {
		// for (BFNode bfn : al) {
		// if (bfn.getValue() != null) {
		// // System.out.print("(" + bfn.getKey() + "");
		// sbBody.append("(" + bfn.getKey() + "");
		// if (al.size() > i){
		// sbBody.append("|");
		// // System.out.print("|");
		// }
		// else {
		// sbBody.append(":");
		// // System.out.print(":");
		// }
		// } else {
		// chkNullval = true;
		// sbBody.append("(" + bfn.getKey() + ",?)");
		// sbBody.append("|");
		// // System.out.print("(" + bfn.getKey() + ",?)");
		// // System.out.print("|");
		// }
		// i++;
		// }
		// if (!chkNullval) {
		// // System.out.print("\"?(\\w|[^\",])*\"?,*)|");
		// sbBody.append("\"?(\\w)*\"?,*)|");
		// // System.out.print("\"?(\\w)*\"?,*)|");
		// } else {
		// sbBody.append("\"?(\\w)*\"?,*");
		// // System.out.print("\"?(\\w)*\"?,*");
		// }
		// } else {
		// sbBody.append(".*,?");
		// // System.out.print(".*,?");
		// int count = al.size();
		// int idx = 1;
		// sbBody.append(al.iterator().next().getKey());
		// sbBody.append(".*");
		// }
		// }
		// i = 1;
		// al = ENtable.get("Const");
		// for (BFNode bfn : al) {
		// sbBody.append("(" + bfn.getKey() + ":" + bfn.getValue());
		// sbBody.append(",*");
		// sbBody.append(")");
		// i++;
		// }
		//
		// i = 1;
		// al = ENtable.get("int");
		// if (!al.isEmpty()) {
		// for (BFNode bfn : al) {
		// sbBody.append("|(" + bfn.getKey() + ":" + "[0-9]*");
		// sbBody.append(",*");
		// sbBody.append(")");
		// i++;
		// }
		// }
		//
		// al = ENtable.get("JSONArray");
		// if (!al.isEmpty()) {
		// for (BFNode bfn : al) {
		//
		// sbBody.append("|(" + bfn.getKey() + ":\\[");
		// // System.out.print("|(" + bfn.getKey() + ":\\[");
		//
		// if (EQtable.get(bfn.getKey()) != null) {
		// GenRegx(EQtable, bfn.getKey());
		// } else {
		// sbBody.append("(\"?(\\w)*,?\"?)*");
		// // System.out.print("(\"?(\\w)*,?\"?)*");
		// }// System.out.print("(\"?(\\w|[^\",])*,?\"?)*");
		// sbBody.append("\\])");
		// // System.out.print("\\])");
		// }
		// }
		//
		// if (al.size() > 0){
		// sbBody.append(",?");
		// // System.out.print(",?");
		// }
		// sbBody.append(")}?");
		// // System.out.print(")}?");
		//
		// AddBody(sbBody.toString());
		// System.out.println(sbBody);
		// }
	}

	private void AddBody(String string)
	{
	}

	private void SetEqtable(String key, HashMap<String, EquvNode> EQtable, BFNode bfn)
	{
		EquvNode en = null;
		en = getEquvNode(key, EQtable);
		HashMap<String, ArrayList<BFNode>> ENtable = null;
		ENtable = en.getENtable();
		// System.out.println("Root!");

		// System.out.println("parent key : " + key);
		// System.out.println("bfn key : " + bfn.getKey());
		// System.out.println("chk Vtype : " + checkVtype(bfn));
		switch (checkVtype(bfn))
		{
			case Vtypes.vstring :
				ENtable.get("String").add(bfn);
				// System.out.println("String key : " + bfn.getKey());
				break;
			case Vtypes.vconst :
				ENtable.get("Const").add(bfn);
				// System.out.println("Const key : " + bfn.getKey());
				break;
			case Vtypes.vint :
				ENtable.get("int").add(bfn);
				// System.out.println("int key : " + bfn.getKey());
				break;
			case Vtypes.vjsonarray :
				ENtable.get("JSONArray").add(bfn);
				// System.out.println("JSONArray key : " + bfn.getKey());
				break;
			default :
				// System.out.println("Error!sss");
				break;
		}
	}

	// traversal Equivalent table
	private void printEqtable(HashMap<String, EquvNode> EQtable, String key)
	{
		EquvNode en = EQtable.get(key);
		HashMap<String, ArrayList<BFNode>> ENtable = en.getENtable();
		ArrayList<BFNode> al = null;

		System.out.println("key : " + key);
		al = ENtable.get("String");
		for (BFNode bfn : al)
		{
			System.out.println("String OR : " + bfn.getKey());
		}

		al = ENtable.get("Const");
		for (BFNode bfn : al)
		{
			System.out.println("Const OR : " + bfn.getKey());
		}

		al = ENtable.get("JSONArray");
		for (BFNode bfn : al)
		{
			System.out.println("JSONArray OR : " + bfn.getKey());
			printEqtable(EQtable, bfn.getKey());
		}

	}

	private EquvNode getEquvNode(String key, HashMap<String, EquvNode> EQtable)
	{
		if (EQtable.containsKey(key))
		{
			EquvNode qn = EQtable.get(key);
			return qn;
		} else
		{
			EquvNode qn = new EquvNode();
			EQtable.put(key, qn);
			return qn;
		}
	}

	private int checkVtype(BFNode bfn)
	{

		if (bfn.getVtype().indexOf("String") != -1)
		{
			if (bfn.getValue() == null)
				return Vtypes.vstring;
			else if (isconst(bfn.getValue()))
				return Vtypes.vconst;
		} else if (bfn.getVtype().indexOf("int") != -1)
			return Vtypes.vint;
		else if (bfn.getVtype().indexOf("JSONArray") != -1)
			return Vtypes.vjsonarray;

		return -1;
	}

	@Override
	public String GenRegex(HashMap<String, String> taintVariableMap, HashMap<String, ArrayList<BFNode>> BFTtable, String trackingReg)
	{
		Set<String> varSet = new HashSet<String>();
		Set<String> notRootVarSet = new HashSet<String>();

		for (String key : taintVariableMap.keySet())
		{
			if (taintVariableMap.get(key).equals(trackingReg))
			{
				varSet.add(key);
				ArrayList<BFNode> list = BFTtable.get(key);
				if (list != null)
				{
					for (BFNode bfn : list)
					{
						if (bfn.getKey() == null || bfn.getVtype() == null)
							continue;

						if (bfn.getVtype().equals("JSONObject"))
						{
							notRootVarSet.add(bfn.getValue());
						}
					}
				}
			}
		}

		varSet.removeAll(notRootVarSet);

		String result = "";
		for (String root : varSet)
		{
			String check = printResponse(BFTtable, root);
			if (!check.equals("{}") && !check.equals(""))
			{
				if (!result.equals(""))
					result += " / ";
				result += check;
			}

		}
		return result;
	}

	private boolean isContainPhiNode(ArrayList<BFNode> list)
	{
		if(list != null) {
			for(BFNode bfn : list) {
				if (bfn.isPhiNode())
					return true;
			}
		}
		return false;
	}
	
	private String printResponse(HashMap<String, ArrayList<BFNode>> BFTtable, String trackingReg)
	{
		String result = "";

		ArrayList<BFNode> list = BFTtable.get(trackingReg);
		if (list == null || list.size() == 0)
			return "";
		result += "{";
		
//		boolean isContainPhiNode = isContainPhiNode(list);
//		if (isContainPhiNode)
//		{
//			result += "(";
//		}
//		for (int i = 0; i < list.size(); i++)
//		{
//			BFNode phiBfn = (BFNode) list.get(i);
//			if (phiBfn.isPhiNode())
//			{
//				String ret = printResponse(BFTtable, phiBfn.getPhinodeVar());
//				if (!ret.equals("") && !ret.equals("()"))
//				{
//					if (!result.equals("("))
//						result += " | ";
//					result += ret;
//				}
//			}
//		}
//		if (isContainPhiNode)
//		{
//			result += ")";
//		}
		
		
		for (BFNode bfn : list)
		{
			if (bfn.getKey() == null || bfn.getVtype() == null)
				continue;
//
//			if (result.length() > 500)
//			{
//				System.out.println("too long! - error bug hnankoong");
//				return "";
//			}
			


			String str = "";
			switch (bfn.getVtype())
			{
				case "GenRegex" :
					str = bfn.getKey();
					break;
				case "java.lang.String" :
				case "String" :
					str = bfn.getKey() + ": (.*)";
					break;
				case "double" :
					str = bfn.getKey() + ": (.*)";
					break;
				case "java.lang.Integer" :
				case "int" :
					str = bfn.getKey() + ": ([0-9]*)";
					break;
				case "java.lang.Boolean" :
				case "boolean" :
					str = bfn.getKey() + ": (true|false)";
					break;
				case "JSONObject" :
					str = bfn.getKey() + ": ";
					if (BFTtable.get(bfn.getValue()).size() > 1)
						str += printResponse(BFTtable, bfn.getValue());
					else
						str += "(.*)";
					break;

				case "JSONArray" :
					str = bfn.getKey() + ": [JSONArray]";
					break;
				default :
					str = bfn.getKey() + ": ##" + bfn.getVtype() + "##";
					break;
			}
			if (!str.equals(""))
			{
				if (!result.equals("{"))
					result += ", ";
				result += str;
			}
		}
		result += "}";
		return result;
	}

	private void parsegSonClass(String Classname, Tree BFtree, Object parent)
	{
		SootClass gSonTarget = Scene.v().getSootClass(Classname);

		for (SootField sf : gSonTarget.getFields())
		{

			// System.out.println("sf type : " + sf.getType().toString());
			// System.out.println(sf.getType().toString().indexOf("String"));
			SootClass gInnerClass = null;
			if (sf.getType().toString().indexOf("String") == -1 && sf.getType().toString().indexOf("int") == -1 && sf.getType().toString().indexOf("double") == -1
					&& sf.getType().toString().indexOf("boolean") == -1 && sf.getType().toString().indexOf("Comparator") == -1)
			{
				// System.out.println("Inner Class : " + sf.getType());
				// System.out.println("sf : " + sf);
				// System.out.println(sf.getType());

				if (sf.getType().toString().indexOf("java.util.List") != -1)
				{
					// int end = sf.getSignature().toString().indexOf("List");
					String targetClass = Classname;
					targetClass = targetClass.replace("List", "");
					// System.out.println(targetClass);
					gInnerClass = Scene.v().getSootClass(targetClass);

					// if ( gInnerClass != null )
					// System.out.println("not null");
				} else
					gInnerClass = Scene.v().getSootClass(sf.getType().toString());
			}

			if (gInnerClass != null)
			{
				BFNode bfn = new BFNode();
				bfn.setKey("\"" + sf.getName().toString() + "\"");
				// System.out.println("class name : " + gInnerClass.getName());
				bfn.setVtype("String");
				BFtree.add(bfn);
				parsegSonClass(gInnerClass.getName(), BFtree, null);
			} else
			{
				BFNode bfn = new BFNode();
				bfn.setKey("\"" + sf.getName().toString() + "\"");
				bfn.setVtype(sf.getType().toString());
				if (parent != null)
					try
					{
						BFtree.add((BFNode) parent, bfn);
					} catch (NodeNotFoundException e)
					{
						e.printStackTrace();
					}
				else
					BFtree.add(bfn);
			}
		}

	}

	private void fromJsonfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sootMethod)
	{

		if (iie.getMethodRef().toString() == "<com.google.gson.Gson: java.lang.Object fromJson(java.io.Reader,java.lang.Class)>")
		{
			// System.out.println("!! gson used by this application.");
			// SootClass scccc =
			// Scene.v().getSootClass("com.radioreddit.android.api.StationStatus");
			// for (SootField sf : scccc.getFields()) {
			// System.out.println("\n"+sf);
			// System.out.println(sf.getName());
			// System.out.println(sf.getType());
			// }
			// System.out.print("class name : " +
			// iie.getArg(1).toString().substring(iie.getArg(1).toString().indexOf("\"")));

			String csName = iie.getArg(1).toString().substring(iie.getArg(1).toString().indexOf("\""));
			csName = csName.replaceAll("\"", "");
			csName = csName.replaceAll("/", ".");
			// System.out.print(csName);

			// SootClass gSonTarget = Scene.v().getSootClass(csName);

			// will do this later - 20150912 hnamkoong
			//			Tree BTTree = bFTtable.get(iie.getBase().toString());
			//			parsegSonClass(csName, BTTree, null);
			//			AddStatement(iie, sootMethod);
			// for ( SootField sf : gSonTarget.getFields()) {
			// SootClass gInnerClass =
			// Scene.v().getSootClass(sf.getType().toString());
			// if (gInnerClass != null)
			// {
			// System.out.println("Inner class : " + gInnerClass.getName());
			// for ( SootField isf : gInnerClass.getFields()) {
			// System.out.println(isf.getName());
			// System.out.println(isf.getType());
			// }
			// } else
			// {
			// BFNode bfn = new BFNode();
			// bfn.setKey(sf.getName().toString());
			// bfn.setVtype(sf.getType().toString());
			// BTTree.add(bfn);
			// }
			// }
		}
	}

	@SuppressWarnings("unused")
	private void MethodIdsfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sootMethod)
	{

		if (iie.getMethodRef().toString() == "<org.json.JSONArray: org.json.JSONObject getJSONObject(int)>")
		{
			// implement this later - 20150912 hnamkoong
			//			Tree BTTree = bFTtable.get(iie.getBase().toString());
			//			bFTtable.put(strDest, BTTree);
			AddStatement(iie, sootMethod);
			// System.out.println("Dest:" + strDest);
			// printTree(BTTree, "r10");
		} else if (iie.getMethodRef().toString() == "<org.json.JSONArray: org.json.JSONObject getJSONObject(String)>")
		{

		}
	}

	private void getJSONArrayfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sootMethod)
	{

		// implement this later - 20150912 hnamkoong
		if (iie.getMethodRef().toString() == "<org.json.JSONObject: org.json.JSONArray getJSONArray(java.lang.String)>"
				|| iie.getMethodRef().toString() == "<org.json.JSONObject: org.json.JSONArray optJSONArray(java.lang.String)>")
		{
			// this.TrackingReg = iie.getBase().toString();
			// System.out.println("strDest:" + strDest);
			// System.out.println(iie.toString());
			//			Tree BTTree = bFTtable.get(iie.getBase().toString());
			//			Tree BTTree2 = bFTtable.get(strDest);

			BFNode bfn = new BFNode();

			if (isconst(iie.getArg(0).toString()))
			{
				//				bfn.setKey(iie.getArg(0).toString());
				//				// System.out.println("stmt : " + iie.toString());
				//				bfn.setValue(null);
				//				bfn.setVtype("String");
				//				BTTree.add(bfn);
				//				BTTree2.add(bfn);
				//				AddStatement(iie, sootMethod);
			}
		}
	}

	private void setHeaderfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sootMethod)
	{

	}

	public void toStringfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sootMethod)
	{
		if (iie.getMethodRef().toString() == "<org.json.JSONObject: java.lang.String toString()>")
		{
			AddStatement(iie, sootMethod);
		}
	}

	public void setEntityfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut)
	{
	}

	public void StringEntityfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sootMethod)
	{
		if (iie.getMethodRef().toString() == "<org.apache.http.entity.StringEntity: void <init>(java.lang.String)>")
		{
			AddStatement(iie, sootMethod);
		}
	}

	public void hasfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sootMethod) throws NodeNotFoundException
	{
		if (iie.getMethodRef().toString() == "<org.json.JSONObject: boolean has(java.lang.String)>")
		{
//			System.out.println("<org.json.JSONObject: boolean has(java.lang.String)> Called!");
			AddStatement(iie, sootMethod);
		}
	}

	@SuppressWarnings("unchecked")
	private void getJSONObjectfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sootMethod)
			throws NodeNotFoundException
	{
		if (iie.getMethodRef().toString() == "<org.json.JSONObject: org.json.JSNObject opt(java.lang.String)>"
				|| iie.getMethodRef().toString() == "<org.json.JSONObject: org.json.JSONObject getJSONObject(java.lang.String)>"
				|| iie.getMethodRef().toString() == "<org.json.JSONObject: org.json.JSONObject optJSONObject(java.lang.String)>"
				|| iie.getMethodRef().toString().equals("<org.codehaus.jackson.JsonNode: org.codehaus.jackson.JsonNode get(java.lang.String)>"))
		{
			String arg0 = iie.getArg(0).toString();
			String base = iie.getBase().toString();

			if (isconst(arg0))
			{
				ArrayList<BFNode> list = BFTtable.get(base);
				if(list == null) {
					list = new ArrayList<BFNode>();
					BFTtable.put(base, list);
				}
				BFNode bfn = new BFNode();
				if (strDest == null) {
					bfn.makeResponseBfn(arg0, null, "String");
				}
				else {
					bfn.makeResponseBfn(arg0, strDest, "JSONObject");
				}
				list.add(bfn);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void getStringfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sootMethod) throws NodeNotFoundException
	{
		if (iie.getMethodRef().toString() == "<org.json.JSONObject: java.lang.String getString(java.lang.String)>"
				|| iie.getMethodRef().toString() == "<org.json.JSONObject: java.lang.String optString(java.lang.String,java.lang.String)>"
				|| iie.getMethodRef().toString() == "<org.json.JSONObject: java.lang.String optString(java.lang.String)>"
				|| iie.getMethodRef().toString() == "<org.json.JSONObject: boolean optBoolean(java.lang.String)>"
				|| iie.getMethodRef().toString() == "<org.json.JSONObject: boolean optBoolean(java.lang.String,boolean)>"
				|| iie.getMethodRef().toString() == "<org.json.JSONObject: int optInt(java.lang.String)>"
				|| iie.getMethodRef().toString() == "<org.json.JSONObject: int getInt(java.lang.String)>"
				|| iie.getMethodRef().toString() == "<org.json.JSONObject: long getLong(java.lang.String)>"
				|| iie.getMethodRef().toString() == "<org.json.JSONObject: int optInt(java.lang.String,int)>"
				|| iie.getMethodRef().toString() == "<org.json.JSONObject: long optLong(java.lang.String,long)>")
		{

			String arg0 = iie.getArg(0).toString();
			String base = iie.getBase().toString();
			String type = iie.getType().toString();

			if (isconst(arg0))
			{
				ArrayList<BFNode> list = BFTtable.get(base);
				if(list == null) {
					list = new ArrayList<BFNode>();
					BFTtable.put(base, list);
				}
				BFNode bfn = new BFNode();
				bfn.makeResponseBfn(arg0, null, iie.getArg(0).getType().toString());
				list.add(bfn);
			}
		} else if (iie.getMethodRef().toString() == "<org.json.JSONArray: org.json.JSONObject getJSONObject(int)>")
		{
		}
	}

//	@SuppressWarnings("unchecked")
//	public void printTree(Tree tr, String Treename)
//	{
//		System.out.println("===============================");
//		System.out.println("Tree " + Treename + " PreorderTraversal");
//		for (Iterator<BFNode> iter = tr.preOrderTraversal().iterator(); iter.hasNext();)
//		{
//			BFNode bfn = iter.next();
//			System.out.print(bfn.getKey() + ":" + bfn.getValue() + "\tVtype : " + bfn.getVtype());
//			try
//			{
//				System.out.print("\nThis : " + bfn);
//				System.out.println("\nParent : " + tr.parent(bfn));
//			} catch (NodeNotFoundException e)
//			{
//				e.printStackTrace();
//			}
//		}
//		System.out.println("Tree depth : " + tr.depth());
//	}

	@SuppressWarnings("unchecked")
	public void putfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sootMethod) throws NodeNotFoundException
	{

		if (iie.getMethodRef().toString() == "<org.json.JSONObject: org.json.JSONObject put(java.lang.String,java.lang.Object)>"
				|| iie.getMethodRef().toString() == "<org.json.JSONObject: org.json.JSONObject put(java.lang.String,int)>"
				|| iie.getMethodRef().toString() == "<org.json.JSONObject: org.json.JSONObject put(java.lang.String,long value)>"
				|| iie.getMethodRef().toString() == "<org.json.JSONObject: org.json.JSONObject put(java.lang.String,double value)>"
				|| iie.getMethodRef().toString() == "<org.json.JSONObject: org.json.JSONObject put(java.lang.String,boolean)>")
		{
			if (isconst(iie.getArg(1).toString()))
			{
				
//				Tree BTTree = BFTtable.get(iie.getBase().toString());
//				if(BTTree != null) {
//					BFNode bfn = new BFNode();
//					bfn.setKey(iie.getArg(0).toString());
//					bfn.setValue(iie.getArg(1).toString());
//					bfn.setVtype(iie.getArg(1).getType().toString());
//					BTTree.add(bfn); AddStatement(iie, sootMethod); //
//					printTree(BTTree,iie.getBase().toString());
//				}
				 
			} else
			{
//				  Tree BTTree = BFTtable.get(iie.getBase().toString());
//				  
//				  if (BTTree != null) {
//				  
//				  SymbolEntry se = SMtable.get(iie.getArg(1).toString());
//				  if(se != null) {
//					  if(iie.getArg(1).getType().toString().indexOf("JSONArray") == -1) {
//						  BFNode bfn = new BFNode();
//						  bfn.setKey(iie.getArg(0).toString());
//						  bfn.setValue(se.getValue()); bfn.setVtype(se.getType());
//						  BTTree.add(bfn); AddStatement(iie, sootMethod); //
//						  printTree(BTTree,iie.getBase().toString()); 
//					  } else { //
//						  printTree(BFTtable.get(iie.getArg(1).toString()),iie.getBase().toString()); BFNode parent = new BFNode();
//						  parent.setKey(iie.getArg(0).toString());
//						  parent.setValue(iie.getArg(1).toString());
//						  parent.setVtype(iie.getArg(1).getType().toString());
//						  BTTree.add(parent);
//				  
//	// Copy tree Tree SrcTree =
//						  BFTtable.get(iie.getArg(1).toString());
//						  if (SrcTree != null) {
//							  // must tree traversal! parent change
//							  copytree(BTTree, SrcTree, parent); AddStatement(iie, sootMethod); //
//							  printTree(BTTree,iie.getBase().toString());
//						  }
//					  }
//				  }
			}
		} else if (iie.getMethodRef().toString() == "<org.json.JSONArray: org.json.JSONArray put(java.lang.Object)>")
		{

//			if (iie.getArg(0).getType().toString().indexOf("JSONObject") != -1 || iie.getArg(0).getType().toString().indexOf("JSONArray") != -1)
//			{
//				
//				  Tree BTTree = BFTtable.get(iie.getBase().toString()); if
//				  (BTTree != null) { Tree SrcTree =
//				  BFTtable.get(iie.getArg(0).toString()); if (SrcTree != null)
//				 { copytree(BTTree, SrcTree, (BFNode) BTTree.root());
//				  AddStatement(iie, sootMethod); // printTree(BTTree,
//				  iie.getBase().toString()); } }
//				 
//			} else if (isconst(iie.getArg(0).toString()))
//			{
//				
//				  
//				  Tree BTTree = BFTtable.get(iie.getBase().toString()); if
//				  (BTTree != null) { BFNode bfn = new BFNode();
//				  
//				  bfn.setKey(iie.getArg(0).toString()); bfn.setValue(null);
//				  bfn.setVtype(iie.getArg(0).getType().toString());
//				  BTTree.add(bfn); BFTtable.put(iie.getBase().toString(),
//				  BTTree); AddStatement(iie, sootMethod); //
//				  printTree(BFTtable.
//				  get(iie.getBase().toString()),iie.getBase().toString()); }
//				 
//			}
		}
	}

	public void appendfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sootMethod)
	{
		if (iie.getMethodRef().toString() == "<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>")
		{
		}
	}

	public void initfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sootMethod)
	{
		if (iie.getMethodRef().toString() == "<org.apache.http.impl.client.DefaultHttpClient: void <init>()>")
		{
			// System.out.println("Init DefaultHttpClient");
		} else if (iie.getMethodRef().toString() == "<org.json.JSONObject: void <init>()>")
		{
			
			 * // System.out.println("Init JSONObject");
			 * BFTtable.remove(iie.getBase().toString());
			 * BFTtable.put(iie.getBase().toString(), new
			 * ArrayListTree<BFNode>());
			 * BFTtable.get(iie.getBase().toString()).add(new BFNode());
			 * AddStatement(iie, sootMethod); //
			 * System.out.println("remove complete");
			 

		} else if (iie.getMethodRef().toString() == "<org.json.JSONObject: void <init>(java.lang.String)>")
		{
			initFlag = true;
		} else if (iie.getMethodRef().toString() == "<java.lang.StringBuilder: void <init>()>")
		{
			// System.out.println("Init StringBuilder");
		} else if (iie.getMethodRef().toString() == "<org.apache.http.client.methods.HttpPost: void <init>(java.lang.String)>")
		{
			// System.out.println("Init HttpPost(String)");
		} else if (iie.getMethodRef().toString() == "<org.json.JSONArray: void <init>()>")
		{
			AddStatement(iie, sootMethod);
			// System.out.println("Init JSONArray");
		} else if (iie.getMethodRef().toString() == "<org.apache.http.entity.StringEntity: void <init>(java.lang.String)>")
		{
			// System.out.println("Init StringEntity(String)");
		} else if (iie.getMethodRef().toString() == "<org.apache.http.message.BasicHeader: void <init>(java.lang.String,java.lang.String)>")
		{
			// System.out.println("BasicHeader");
			AddStatement(iie, sootMethod);
		}
	}

	public boolean isconst(String arg)
	{
		if (arg.indexOf("$") != -1)
			return false;
		else
			return true;
	}

	@SuppressWarnings("unchecked")
	public void copytree(Tree DstTree, Tree SrcTree, BFNode parent)
	{
		try
		{
			DstTree.addAll(parent, SrcTree);

			for (Iterator<BFNode> iter = DstTree.children(parent).iterator(); iter.hasNext();)
			{
				BFNode bfn = iter.next();
				if (bfn.getKey() == null && bfn.getVtype() == null)
				{
					DstTree.remove(bfn);
				}
			}

		} catch (NodeNotFoundException e)
		{
			e.printStackTrace();
		}
		// printTree(DstTree, "combined tree");
	}

	@Override
	public String ExtractingExpr(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, String ParentMethod, SootMethod sootMethod, String strDst , HashMap<String, ArrayList<PairingEPEntry>> pairingInfoTable)
			throws NodeNotFoundException
	{

		strDest = strDst;
		String strMethod = iie.getMethodRef().name();
		List<Value> args = new ArrayList<Value>();
		Value larg = null;
		Value rarg = null;

		// Check method
		int chkMethod = methodlist.indexOf(strMethod);

		// loading args
		args = iie.getArgs();

		switch (chkMethod)
		{
			case MethodIds.init :
				initfuncHandler(iie, BFTtable, ut, sootMethod);
				break;
			case MethodIds.append :
				appendfuncHandler(iie, BFTtable, ut, sootMethod);
				break;
			case MethodIds.put :
				putfuncHandler(iie, BFTtable, ut, sootMethod);
				break;
			case MethodIds.getBoolean :
				getBooleanfuncHandler(iie, BFTtable, ut, sootMethod);
				break;
			case MethodIds.opt :
			case MethodIds.optString :
			case MethodIds.optBoolean :
			case MethodIds.optInt :
			case MethodIds.getInt :
			case MethodIds.getLong :
			case MethodIds.optLong :
			case MethodIds.getString :
				getStringfuncHandler(iie, BFTtable, ut, sootMethod);
				break;
			case MethodIds.get :
			case MethodIds.optJSONObject :
			case MethodIds.getJSONObject :
				getJSONObjectfuncHandler(iie, BFTtable, ut, sootMethod);
				break;
			case MethodIds.has :
				hasfuncHandler(iie, BFTtable, ut, sootMethod);
				break;
			case MethodIds.accumulate :
				break;
			case MethodIds.setEntity :
				break;
			case MethodIds.putOnce :
			case MethodIds.putOpt :
				putOptfuncHandler(iie, BFTtable, ut, sootMethod);
				break;
			case MethodIds.setHeader :
				setHeaderfuncHandler(iie, BFTtable, ut, sootMethod);
				break;
			case MethodIds.toString :
				toStringfuncHandler(iie, BFTtable, ut, sootMethod);
				break;
			case MethodIds.optJSONArray :
			case MethodIds.getJSONArray :
				getJSONArrayfuncHandler(iie, BFTtable, ut, sootMethod);
				break;
			case MethodIds.fromJson :
				fromJsonfuncHandler(iie, BFTtable, ut, sootMethod);
				break;
			case MethodIds.execute :
				if (iie.getMethodRef().toString()
						.equals("<org.apache.http.client.HttpClient: org.apache.http.HttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>"))
				{
				}
				break;
			
			 * case MethodIds.getJSONArray: //
			 * getJSONArrayfuncHandler(iie,SMtable,BFTtable, ut); break; case
			 * MethodIds.getJSONObject: // MethodIdsfuncHandler(iie, SMtable,
			 * BFTtable, ut); break;
			 
			case MethodIds.readValue :
				readValuefuncHandler(iie, BFTtable, ut, sootMethod);
				break;
			default :
				break;
		}
		if(iie.getMethodRef().toString().contains("<com.google.gson.stream")) {
			googleGsonHandler(iie, BFTtable, ut, sootMethod);
		} else if(iie.getMethodRef().toString().equals("<java.lang.String: boolean equals(java.lang.Object)>")) {
			// $z0 = virtualinvoke $r5.<java.lang.String: boolean equals(java.lang.Object)>("DateTime");
			
			String base = iie.getBase().toString();
			String arg0 = iie.getArg(0).toString();
			
			Set<String> GsonTrackingVar = ForwardGsonTrackingVarStack.getLast();
			if(GsonTrackingVar.contains(base)) {
				BFNode bfn = new BFNode();
				bfn.makeResponseBfn(arg0, null, "String");
				ArrayList<BFNode> list = BFTtable.get(base);
				list.add(bfn);
			}
		}
		
		return "";
	}

	private void googleGsonHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> bFTtable, Unit ut, SootMethod sm)
	{
		if(iie.getMethodRef().toString().equals("<com.google.gson.stream.JsonReader: java.lang.String nextName()>")) {
			Set<String> GsonTrackingVar = ForwardGsonTrackingVarStack.getLast();
			GsonTrackingVar.add(this.strDest);
		}
	}

	private String FixClassName(String Scname)
	{
		Scname = Scname.replaceAll("class ","");
		Scname = Scname.replaceAll("\"\\[L", "");
		Scname = Scname.replaceAll("\"", "");
		Scname = Scname.replaceAll(";", "");
		Scname = Scname.replaceAll("\\/", ".");
		return Scname;
	}

	private void RetreiveJacksonClass(FakeScene FakeS, SootClass sc, ArrayList<BFNode> JsonList, HashMap<String, ArrayList<BFNode>> bFTtable)
	{
		for (SootField sf : sc.getFields())
		{
			if (sf.getType() instanceof PrimType)
			{
				if (sf.getType() instanceof IntType)
				{
					BFNode bfn = new BFNode();
					bfn.makeResponseBfn(sf.getName(), null , sf.getType().toString());
					JsonList.add(bfn);
				} else if (sf.getType() instanceof DoubleType)
				{
					BFNode bfn = new BFNode();
					bfn.makeResponseBfn(sf.getName(), null , sf.getType().toString());
					JsonList.add(bfn);
				} else if (sf.getType() instanceof FloatType)
				{
					BFNode bfn = new BFNode();
					bfn.makeResponseBfn(sf.getName(), null , sf.getType().toString());
					JsonList.add(bfn);
				} else if (sf.getType() instanceof BooleanType)
				{
					BFNode bfn = new BFNode();
					bfn.makeResponseBfn(sf.getName(), null , sf.getType().toString());
					JsonList.add(bfn);
				} else
				{
//					System.out.println("sf type is : " + sf.getType().toString());
				}
			} else if (sf.getType().toString().equals("java.lang.String"))
			{
				BFNode bfn = new BFNode();
				bfn.makeResponseBfn(sf.getName(), null, sf.getType().toString());
				JsonList.add(bfn);
			} else if (sf.getType().toString().equals("java.lang.Boolean"))
			{
				BFNode bfn = new BFNode();
				bfn.makeResponseBfn(sf.getName(), null, sf.getType().toString());
				JsonList.add(bfn);
			} else
			// Not primitive type 
			{
				if (sf.getType().toString().contains("android") || sf.getType().toString().contains("java.lang") || sf.getType().toString().contains("java.util"))
					continue;
				
				ArrayList<BFNode> list = new ArrayList<BFNode>();
				if (VisitableForJacksonObject.contains(sf.getType().toString()))
				{
					BFNode bfn = new BFNode();
					bfn.makeResponseBfn(sf.getName(), ".*", "java.lang.String");
					JsonList.add(bfn);
				}
				else
				{
					VisitableForJacksonObject.add(sf.getType().toString());
					BFNode bfn = new BFNode();
					bfn.makeResponseBfn(sf.getName(), sf.getType().toString(), "JSONObject");
					JsonList.add(bfn);
					bFTtable.put(sf.getType().toString(), list);
//					System.out.println("sootClass : " + sf.getType());
					RetreiveJacksonClass(FakeS, FakeS.getSootClass(sf.getType().toString().replaceAll("\\[\\]", "")), list, bFTtable);
				}
			}
		}
	}

	private void readValuefuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> bFTtable, Unit ut, SootMethod sootMethod)
	{
		String Scname = FixClassName(iie.getArg(1).toString());
		if(Scname.equals("org.codehaus.jackson.JsonNode"))
			return;
		ArrayList<BFNode> list = new ArrayList<BFNode>();
		bFTtable.put(iie.getBase().toString(), list);
		//TODO FakeScene
		FakeScene FakeScene = new FakeScene();
//		VisitableForJacksonObject.add(iie.getBase().getType().toString());
		RetreiveJacksonClass(FakeScene, FakeScene.getSootClass(Scname), list, bFTtable);

		VisitableForJacksonObject.clear();
		
		
		String result = printResponse(bFTtable, iie.getBase().toString());
		if(!result.equals("")) {
//			System.out.println();
//			System.out.println("\t\t + SM : " + sootMethod.getSignature());
//			System.out.println("\t\t + readValue Class [" + iie.getArg(1) + "]");
//			System.out.println("\t\t[response] " + printResponse(bFTtable, iie.getBase().toString()));
//			System.out.println("\t\tEP_SM : " + sootMethod.toString());
//			System.out.println("\t\tEP_Stmt : " + ut.toString());
//			System.out.println();

			String key = Constants.EPcont.getDP().toString() + Constants.EPcont.getDPStmt().toString() + result;
			PairingDPEntry dpentry = Constants.pairingInfoContainer.get(key);
			if(dpentry == null) {
				dpentry = new PairingDPEntry(Constants.EPcont.getDP(), Constants.EPcont.getDPStmt(), result);
				Constants.pairingInfoContainer.put(key, dpentry);
			}
			dpentry.addEP(new PairingEPEntry(sootMethod.toString(), ut.toString()));
		}
	}

	private void getBooleanfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sootMethod)
	{

		if (iie.getMethodRef().toString().equals("<org.json.JSONObject: boolean getBoolean(java.lang.String)>"))
		{
			String arg = iie.getArg(0).toString();
			String base = iie.getBase().toString();

			
			 * if (isconst(arg)) { Tree BTTree = bFTtable.get(base); if (BTTree
			 * != null) { BFNode bfn = new BFNode();
			 * 
			 * bfn.setKey(arg); bfn.setValue(("true|false"));
			 * bfn.setVtype(iie.getType().toString()); BTTree.add(bfn);
			 * AddStatement(iie, sootMethod); //
			 * printTree(BTTree,iie.getBase().toString()); } }
			 
		}
	}

	private void putOptfuncHandler(InstanceInvokeExpr iie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sootMethod)
	{

		if (iie.getMethodRef().toString().equals("<org.json.JSONObject: org.json.JSONObject putOpt(java.lang.String, java.lang.Object)>")
				|| iie.getMethodRef().toString().equals("<org.json.JSONObject: org.json.JSONObject putOnce(java.lang.String, java.lang.Object)>")

		)
		{

			if (iie.getMethodRef().toString() == "<org.json.JSONObject: org.json.JSONObject put(java.lang.String,java.lang.Object)>"
					|| iie.getMethodRef().toString() == "<org.json.JSONObject: org.json.JSONObject put(java.lang.String,int)>"
					|| iie.getMethodRef().toString() == "<org.json.JSONObject: org.json.JSONObject put(java.lang.String,boolean)>")
			{
				if (isconst(iie.getArg(1).toString()))
				{
					
					 * Tree BTTree = BFTtable.get(iie.getBase().toString()); if
					 * (BTTree != null) { BFNode bfn = new BFNode();
					 * 
					 * bfn.setKey(iie.getArg(0).toString());
					 * bfn.setValue(iie.getArg(1).toString());
					 * bfn.setVtype(iie.getArg(1).getType().toString());
					 * BTTree.add(bfn); AddStatement(iie, sootMethod); //
					 * printTree(BTTree,iie.getBase().toString()); }
					 
				} else
				{
					
					 * Tree BTTree = BFTtable.get(iie.getBase().toString());
					 * 
					 * if (BTTree != null) {
					 * 
					 * SymbolEntry se = SMtable.get(iie.getArg(1).toString());
					 * if (se != null) { if
					 * (iie.getArg(1).getType().toString().indexOf("JSONArray")
					 * == -1) { BFNode bfn = new BFNode();
					 * bfn.setKey(iie.getArg(0).toString());
					 * bfn.setValue(se.getValue()); bfn.setVtype(se.getType());
					 * BTTree.add(bfn); AddStatement(iie, sootMethod); //
					 * printTree(BTTree,iie.getBase().toString()); } else { //
					 * printTree
					 * (BFTtable.get(iie.getArg(1).toString()),iie.getBase
					 * ().toString()); BFNode parent = new BFNode();
					 * parent.setKey(iie.getArg(0).toString());
					 * parent.setValue(iie.getArg(1).toString());
					 * parent.setVtype(iie.getArg(1).getType().toString());
					 * BTTree.add(parent);
					 * 
					 * // Copy tree Tree SrcTree =
					 * BFTtable.get(iie.getArg(1).toString()); if (SrcTree !=
					 * null) { // must tree traversal! parent change
					 * copytree(BTTree, SrcTree, parent); AddStatement(iie,
					 * sootMethod); //
					 * printTree(BTTree,iie.getBase().toString()); } } } }
					 
				}
			} else if (iie.getMethodRef().toString() == "<org.json.JSONArray: org.json.JSONArray put(java.lang.Object)>")
			{

				if (iie.getArg(0).getType().toString().indexOf("JSONObject") != -1 || iie.getArg(0).getType().toString().indexOf("JSONArray") != -1)
				{
					
					 * Tree BTTree = BFTtable.get(iie.getBase().toString()); if
					 * (BTTree != null) { Tree SrcTree =
					 * BFTtable.get(iie.getArg(0).toString()); if (SrcTree !=
					 * null) { copytree(BTTree, SrcTree, (BFNode)
					 * BTTree.root()); AddStatement(iie, sootMethod); //
					 * printTree(BTTree, iie.getBase().toString()); } }
					 
				} else if (isconst(iie.getArg(0).toString()))
				{
					
					 * Tree BTTree = BFTtable.get(iie.getBase().toString()); if
					 * (BTTree != null) { BFNode bfn = new BFNode();
					 * 
					 * bfn.setKey(iie.getArg(0).toString()); bfn.setValue(null);
					 * bfn.setVtype(iie.getArg(0).getType().toString());
					 * BTTree.add(bfn); BFTtable.put(iie.getBase().toString(),
					 * BTTree); AddStatement(iie, sootMethod); //
					 * printTree(BFTtable
					 * .get(iie.getBase().toString()),iie.getBase().toString());
					 * }
					 
				}
			}
		}
	}

	public boolean isMulti()
	{
		return isMulti;
	}

	public void setMulti(boolean isMulti)
	{
		this.isMulti = isMulti;
	}

	@Override
	public void ExtractingExpr(StaticInvokeExpr sie, HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, String name, SootMethod methodOf, String string, HashMap<String, ArrayList<PairingEPEntry>> pairingInfoTable)
	{

	}
}*/