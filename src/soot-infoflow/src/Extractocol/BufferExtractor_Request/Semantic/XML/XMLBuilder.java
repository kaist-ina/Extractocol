package Extractocol.BufferExtractor_Request.Semantic.XML;
/*
package SignExtractor;

import java.awt.print.Printable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import SignExtractor.JSONBuilder.Vtypes;

import com.gaurav.tree.NodeNotFoundException;
import com.gaurav.tree.Tree;

import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.JastAddJ.ThisAccess;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.IntConstant;
import soot.jimple.StaticInvokeExpr;
import soot.toolkits.graph.Block;
import soot.util.Chain;

public class XMLBuilder extends BufferExtractor {
	private boolean isMulti;
	private String strDest;
	private StringBuilder sbBody = new StringBuilder();
	
	public class MethodIds {
		public static final int init = 0;
		public static final int toString = 1;
		public static final int getProperty = 2;
		public static final int split = 3;
		public static final int parseInt = 4;
		public static final int valueOf = 5;
		public static final int startTag = 6;
		public static final int endTag = 7;
		public static final int attribute = 8;
		public static final int text = 9;
		public static final int startDocument = 10;
		public static final int endDocument = 11;
		public static final int eqaulsIgnoreCase = 12;
		public static final int getValue = 13;
		public static final int compareTo = 14;
		public static final int getElementsByTagName = 15;
		public static final int getAttribute = 16;
		public static final int item = 17;
		public static final int getName = 18;
		public static final int equals = 19;
		public static final int getAttributeValue = 20;
		public static final int openStream = 21;
		public static final int getString = 22;
	}

	public class Vtypes {
		public static final int vint = 0;
		public static final int vstring = 1;
		public static final int vjsonarray = 2;
		public static final int vconst = 3;
	}

	public XMLBuilder() {
		methodlist = Arrays.asList("<init>", "toString", "getProperty",
				"split", "parseInt", "valueOf", "startTag", "endTag",
				"attribute", "text", "startDocument", "endDocument",
				"eqaulsIgnoreCase", "getValue", "compareTo" , "getElementsByTagName", "getAttribute", "item", "getName", "equals", "getAttributeValue", "openStream", "getString");
	}

	@SuppressWarnings("unchecked")
	public void printTree(Tree tr, String Treename)
			throws NodeNotFoundException {
		System.out.println("===============================");
		System.out.println("Tree " + Treename + " PreorderTraversal");
		for (Iterator<BFNode> iter = tr.preOrderTraversal().iterator(); iter
				.hasNext();) {
			BFNode bfn = iter.next();
			System.out.print(bfn.getKey() + ":" + bfn.getValue() + "\tVtype : "
					+ bfn.getVtype());

			ArrayList<AttrNode> atn;
			atn = bfn.getEquvAttrs().get("String");
			System.out.print("\nAttrs : ");
			for (AttrNode an : atn) {
				System.out.print(an.getKey() + " ");
			}
			System.out.println("");
		}
		System.out.println("Tree depth : " + tr.depth());
	}
	
	private void AddStatement(InstanceInvokeExpr iie, SootMethod sm) {
		
//		try {
//			if (isMulti)
//			{
//				if (!pi.iscomplete && !OriginDummyname.equals(sm.getName()))
//				{
//	//				pi.Methodinfo.STMT.add(iie.getMethodRef().toString());
//					pi.Methodinfo.STMT.add(iie.getMethodRef().toString());
//					pi.Methodinfo.inMethodName.add(iiCfg.getMethodOf(sm.retrieveActiveBody().getUnits().getFirst()).toString());
//					pi.Methodinfo.myParent.add(sm.getName());
//					pi.getCallMethods().add(iiCfg.getMethodOf(sm.retrieveActiveBody().getUnits().getFirst()).toString());
//				} 
//			} else {
//				if (!pi.iscomplete)
//				{
//	//				pi.Methodinfo.STMT.add(iie.getMethodRef().toString());
//					pi.Methodinfo.STMT.add(iie.getMethodRef().toString());
//					pi.Methodinfo.inMethodName.add(iiCfg.getMethodOf(sm.retrieveActiveBody().getUnits().getFirst()).toString());
//					pi.Methodinfo.myParent.add(sm.getName());
//					pi.getCallMethods().add(iiCfg.getMethodOf(sm.retrieveActiveBody().getUnits().getFirst()).toString());
//				} 
//			}
//		} catch (Exception e) {
//			System.out.println(sm.toString());
//		}
	}

	@Override
	public String GenRegex(HashMap<String, String> taintVariableMap, HashMap<String, ArrayList<BFNode>> BFTtable, String TrackingReg) {
		
		return null;
	}
	@Override
	public String ExtractingExpr(InstanceInvokeExpr iie,
			HashMap<String, SymbolEntry> SMtable,
			HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut,  String ParentMethod,
			SootMethod sm, String StrDst) throws NodeNotFoundException {
		

		strDest = StrDst;
		
		String strMethod = iie.getMethodRef().name();
		List<Value> args = new ArrayList<Value>();
		String strDest = iie.getBase().toString();
		Value larg = null;
		Value rarg = null;

		int chkMethod = methodlist.indexOf(strMethod);

		args = iie.getArgs();

		switch (chkMethod) {
		case MethodIds.init:
			// System.out.println(iie);
			break;
		case MethodIds.getAttributeValue:
			getAttributeValuefuncHandler(iie, SMtable, BFTtable, ut, sm);
			break;
		case MethodIds.attribute:
			// System.out.println(iie);
			attributefuncHandler(iie, SMtable, BFTtable, ut, sm);
			break;
		case MethodIds.endTag:
			// System.out.println(iie);
			endTagfuncHandler(iie, SMtable, BFTtable, ut, sm);
			break;
		case MethodIds.getProperty:
			// System.out.println(iie);
			break;
		case MethodIds.parseInt:
			// System.out.println(iie);
			break;
		case MethodIds.split:
			// System.out.println(iie);
			break;
		case MethodIds.startTag:
			// System.out.println(iie);
			startTagfuncHandler(iie, SMtable, BFTtable, ut, sm);
			break;
		case MethodIds.text:
			// System.out.println(iie);
			textfuncHandler(iie, SMtable, BFTtable, ut, sm);
			break;
		case MethodIds.toString:
			// System.out.println(iie);
			break;
		case MethodIds.valueOf:
			// System.out.println(iie);
			break;
		case MethodIds.endDocument:
			endDocumentfuncHandler(iie, SMtable, BFTtable, ut, sm);
			break;
		case MethodIds.startDocument:
			startDocumentfuncHandler(iie, SMtable, BFTtable, ut, sm);
			break;
		case MethodIds.eqaulsIgnoreCase:
			equalsIgnoreCasefuncHandler(iie, SMtable, BFTtable, ut, sm);
			break;
		case MethodIds.getValue:
			getValuefuncHandler(iie, SMtable, BFTtable, ut, sm);
			break;
		case MethodIds.compareTo:
			compareTofuncHandler(iie, SMtable, BFTtable, ut, sm);
			break;
		case MethodIds.getElementsByTagName:
			getElementsByTagNamefuncHandler(iie, SMtable, BFTtable, ut, sm);
			break;
		case MethodIds.getAttribute:
			getAttributefuncHandler(iie, SMtable, BFTtable, ut, sm);
			break;
		case MethodIds.item:
			itemfuncHandler(iie, SMtable, BFTtable, ut, sm);
			break;
		case MethodIds.getName:
			getNamefuncHandler(iie, SMtable, BFTtable, ut, sm);
			break;
		case MethodIds.equals:
			equalsfuncHandler(iie,SMtable,BFTtable, ut, sm);
			break;
		case MethodIds.openStream:
			if (iie.getMethodRef().toString().equals("<java.net.URL: java.io.InputStream openStream()>")) {
			}
			break;
		case MethodIds.getString:
			
			break;
		default:
			break;
		}
		return "";
	}

	private void getAttributeValuefuncHandler(InstanceInvokeExpr iie,
			HashMap<String, SymbolEntry> sMtable,
			HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm) {
		
		if (iie.getMethodRef().toString() == "<org.xmlpull.v1.XmlPullParser: java.lang.String getAttributeValue(java.lang.String,java.lang.String)>") {
			Tree BTTree = BFTtable.get(iie.getBase().toString());
			if (BTTree != null) {
				
				AddStatement(iie, sm);
				
				BFNode current = ((BFNode) BTTree.root()).getCurrent();

				String attrname = iie.getArg(1).toString();
				String attrvalue = "";
				ArrayList<AttrNode> atn;
				AttrNode an;

				int vtype = 0;

				vtype = Vtypes.vstring;

				switch (vtype) {
				case Vtypes.vstring:
					atn = current.getEquvAttrs().get("String");
					an = new AttrNode();
					an.setKey(attrname);
					an.setValue(attrvalue);
					atn.add(an);
					break;
				case Vtypes.vint:
					atn = current.getEquvAttrs().get("int");
					an = new AttrNode();
					an.setKey(attrname);
					an.setValue(attrvalue);
					atn.add(an);
					break;
				}
			}
		} //add!
	}

	private void equalsfuncHandler(InstanceInvokeExpr iie,
			HashMap<String, SymbolEntry> sMtable,
			HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm) throws NodeNotFoundException {
		
//		Tree BTTree = bFTtable.get(TrackingReg);
//		bFTtable.put(iie.getBase().toString(), BTTree);		
		Tree BTTree = bFTtable.get("");
		
			
		if (BTTree != null) {
			if (BTTree.size() == 1) {
				BFNode bfn = new BFNode();
				bfn.setKey("");
				bfn.setVtype("String");
				BTTree.add(bfn);
				((BFNode) BTTree.root()).setCurrent(bfn);
				AddStatement(iie, sm);
			}
		}
		
		if (iie.getMethodRef().toString() == "<java.lang.String: boolean equals(java.lang.Object)>") {
			BTTree = bFTtable.get("");
			if (BTTree != null) {
				BFNode bfn = new BFNode();
				
				if (isconst(iie.getArg(0).toString()))
				{
					bfn.setKey(iie.getArg(0).toString());
					bfn.setValue(iie.getArg(0).toString());
					bfn.setVtype("String");
					
					//we need determine when current point is change.
					BTTree.add(((BFNode) BTTree.root()).getCurrent(), bfn);
					((BFNode) BTTree.root()).setCurrent(bfn);
					AddStatement(iie, sm);
				}
			}
		}
	}

	private void getNamefuncHandler(InstanceInvokeExpr iie,
			HashMap<String, SymbolEntry> sMtable,
			HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm) {
		
		if (iie.getMethodRef().toString() == "<org.xmlpull.v1.XmlPullParser: java.lang.String getName()>") {
			AddStatement(iie, sm);
		}
	}

	private void itemfuncHandler(InstanceInvokeExpr iie,
			HashMap<String, SymbolEntry> sMtable,
			HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm) {
		
		if (iie.getMethodRef().toString() == "<org.w3c.dom.NodeList: org.w3c.dom.Node item(int)>") {
			AddStatement(iie, sm);
			Tree BTTree = bFTtable.get("");
			bFTtable.put(iie.getBase().toString(), BTTree);
		}
	}

	private void getAttributefuncHandler(InstanceInvokeExpr iie,
			HashMap<String, SymbolEntry> sMtable,
			HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm) {
		
		if (iie.getMethodRef().toString() == "<org.w3c.dom.Element: java.lang.String getAttribute(java.lang.String)>") {
			
			Tree BTTree = bFTtable.get("$r10");
			if (BTTree != null) {
				AddStatement(iie, sm);
				BFNode current = ((BFNode) BTTree.root()).getCurrent();

				String attrname = iie.getArg(0).toString();
				String attrvalue = "";
				ArrayList<AttrNode> atn;
				AttrNode an;
				
				atn = current.getEquvAttrs().get("String");
				an = new AttrNode();
				an.setKey(attrname);
				an.setValue(attrvalue);
				atn.add(an);
			}
		}
	}

	private void getElementsByTagNamefuncHandler(InstanceInvokeExpr iie,
			HashMap<String, SymbolEntry> sMtable,
			HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm) throws NodeNotFoundException {
		
		Tree BTTree = bFTtable.get("");
		bFTtable.put(iie.getBase().toString(), BTTree);		
		BTTree =	bFTtable.get(iie.getBase().toString());
		AddStatement(iie, sm);
		
		if (BTTree.size() == 1) {
			if (BTTree != null) {
				BFNode bfn = new BFNode();
				bfn.setKey("");
				bfn.setVtype("String");
				BTTree.add(bfn);
				((BFNode) BTTree.root()).setCurrent(bfn);
			}
		}
		
		
		if (iie.getMethodRef().toString() == "<org.w3c.dom.Document: org.w3c.dom.NodeList getElementsByTagName(java.lang.String)>") {
			BTTree = bFTtable.get(iie.getBase().toString());
			if (BTTree != null) {
				BFNode bfn = new BFNode();
				bfn.setKey(iie.getArg(0).toString());
				bfn.setValue(iie.getArg(0).toString());
				bfn.setVtype(iie.getArg(0).getType().toString());
				
				//we need determine when current point is change.
				BTTree.add(((BFNode) BTTree.root()).getCurrent(), bfn);
				((BFNode) BTTree.root()).setCurrent(bfn);
			}
		}
		else if (iie.getMethodRef().toString() == "<org.w3c.dom.Element: org.w3c.dom.NodeList getElementsByTagName(java.lang.String)>")
		{
			BTTree = bFTtable.get(iie.getBase().toString());
			if (BTTree != null) {
				BFNode bfn = new BFNode();
				bfn.setKey(iie.getArg(0).toString());
				bfn.setValue(iie.getArg(0).toString());
				bfn.setVtype(iie.getArg(0).getType().toString());

				BTTree.add(((BFNode) BTTree.root()).getCurrent(), bfn);
			}
		}
		
//		printTree(BTTree, "Tree");
	}

	private void compareTofuncHandler(InstanceInvokeExpr iie,
			HashMap<String, SymbolEntry> sMtable,
			HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm)
			throws NodeNotFoundException {
		
		if (iie.getMethodRef().toString() != "") {

			// 2015.09.11
//			Tree BTTree = bFTtable.get(this.XMLTreekey);
//			AddStatement(iie, sm);
//			if (BTTree != null) {
//				BFNode bfn = new BFNode();
//				if (Previouskey == iie.getArg(0).toString()) {
//					BTTree = bFTtable.get(this.XMLTreekey);
//					if (BTTree != null) {
//						BFNode current = ((BFNode) BTTree.root()).getCurrent();
//						if ((BFNode) (BTTree.parent(current)) != null) {
//							((BFNode) BTTree.root())
//									.setCurrent((BFNode) (BTTree
//											.parent(current)));
//						}
//					}
//				} else {
//					bfn.setKey(iie.getArg(0).toString());
//					BTTree.add(bfn);
//					((BFNode) BTTree.root()).setCurrent(bfn);
//				}
//			}
		}
	}

	private void getValuefuncHandler(InstanceInvokeExpr iie,
			HashMap<String, SymbolEntry> sMtable,
			HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm) {
		
		if (iie.getMethodRef().toString() != "") {
			//2015.09.11
//			Tree BTTree = bFTtable.get(this.XMLTreekey);
//			AddStatement(iie, sm);
//			if (BTTree != null) {
//				BFNode current = ((BFNode) BTTree.root()).getCurrent();
//
//				String attrname = iie.getArg(1).toString();
//				String attrvalue = "";
//				ArrayList<AttrNode> atn;
//				AttrNode an;
//
//				atn = current.getEquvAttrs().get("String");
//				an = new AttrNode();
//				an.setKey(attrname);
//				an.setValue(attrvalue);
//				atn.add(an);
//			}
		}
	}

	private void equalsIgnoreCasefuncHandler(InstanceInvokeExpr iie,
			HashMap<String, SymbolEntry> sMtable,
			HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm)
			throws NodeNotFoundException {
		
		if (iie.getMethodRef().toString() != "") {
			//2015.09.11
//			Tree BTTree = bFTtable.get(this.XMLTreekey);
//			AddStatement(iie, sm);
//			if (BTTree != null) {
//				BFNode bfn = new BFNode();
//				if (Previouskey == iie.getArg(0).toString()) {
//					BTTree = bFTtable.get(this.XMLTreekey);
//					if (BTTree != null) {
//						BFNode current = ((BFNode) BTTree.root()).getCurrent();
//						if ((BFNode) (BTTree.parent(current)) != null) {
//							((BFNode) BTTree.root())
//									.setCurrent((BFNode) (BTTree
//											.parent(current)));
//						}
//					}
//				} else {
//					bfn.setKey(iie.getArg(0).toString());
//					BTTree.add(bfn);
//					((BFNode) BTTree.root()).setCurrent(bfn);
//				}
//			}
		}
	}

	private void startDocumentfuncHandler(InstanceInvokeExpr iie,
			HashMap<String, SymbolEntry> sMtable,
			HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm) {
		
		if (iie.getMethodRef().toString() == "<org.xmlpull.v1.XmlSerializer: void startDocument(java.lang.String,java.lang.Boolean)>") {
			Tree BTTree = BFTtable.get(iie.getBase().toString());

			if (BTTree != null) {
				
				AddStatement(iie, sm);
				
				BFNode bfn = new BFNode();

				bfn.setKey(iie.getArg(0).toString());
				bfn.setVtype(iie.getArg(1).getType().toString());
				bfn.setValue(iie.getArg(1).toString());
				BTTree.add(bfn);
				((BFNode) BTTree.root()).setCurrent(bfn);
			}
		}
	}

	private void endDocumentfuncHandler(InstanceInvokeExpr iie,
			HashMap<String, SymbolEntry> sMtable,
			HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm) {
		
		
	}

	private void textfuncHandler(InstanceInvokeExpr iie,
			HashMap<String, SymbolEntry> sMtable,
			HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm) {
		

	}

	private void startTagfuncHandler(InstanceInvokeExpr iie,
			HashMap<String, SymbolEntry> sMtable,
			HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm)
			throws NodeNotFoundException {
		
		if (iie.getMethodRef().toString() == "<org.xmlpull.v1.XmlSerializer: org.xmlpull.v1.XmlSerializer startTag(java.lang.String,java.lang.String)>") {
			
			AddStatement(iie, sm);
			Tree BTTree = BFTtable.get(iie.getBase().toString());
			if (BTTree != null) {
				BFNode bfn = new BFNode();
				bfn.setKey(iie.getArg(1).toString());
				bfn.setValue(iie.getArg(1).toString());
				bfn.setVtype(iie.getArg(1).getType().toString());

				BTTree.add(((BFNode) BTTree.root()).getCurrent(), bfn);
				((BFNode) BTTree.root()).setCurrent(bfn);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void endTagfuncHandler(InstanceInvokeExpr iie,
			HashMap<String, SymbolEntry> sMtable,
			HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm)
			throws NodeNotFoundException {
		
		if (iie.getMethodRef().toString() == "<org.xmlpull.v1.XmlSerializer: org.xmlpull.v1.XmlSerializer endTag(java.lang.String,java.lang.String)>") {
			Tree BTTree = BFTtable.get(iie.getBase().toString());
			if (BTTree != null) {
				
				AddStatement(iie, sm);
				BFNode current = ((BFNode) BTTree.root()).getCurrent();

				if ((BFNode) (BTTree.parent(current)) != null) {
					((BFNode) BTTree.root()).setCurrent((BFNode) (BTTree
							.parent(current)));
					// System.out.println("This " + current.getValue() +
					// "Parent "
					// + ((BFNode) (BTTree.parent(current))).getValue());
				}
				// printTree(BTTree, "tree");
			}
		}
	}

	private void attributefuncHandler(InstanceInvokeExpr iie,
			HashMap<String, SymbolEntry> sMtable,
			HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, SootMethod sm) {
		

		if (iie.getMethodRef().toString() == "<org.xmlpull.v1.XmlSerializer: org.xmlpull.v1.XmlSerializer attribute(java.lang.String,java.lang.String,java.lang.String)>") {
			Tree BTTree = BFTtable.get(iie.getBase().toString());
			if (BTTree != null) {
				
				AddStatement(iie, sm);
				
				BFNode current = ((BFNode) BTTree.root()).getCurrent();

				String attrname = iie.getArg(1).toString();
				String attrvalue = iie.getArg(2).toString();
				ArrayList<AttrNode> atn;
				AttrNode an;

				int vtype = 0;

				if (iie.getArg(2).getType().toString().indexOf("String") != -1) {
					if (isconst(attrvalue))
						vtype = Vtypes.vconst;
					else
						vtype = Vtypes.vstring;
				} else if (iie.getArg(2).getType().toString().indexOf("int") != -1)
					vtype = Vtypes.vint;

				switch (vtype) {
				case Vtypes.vstring:
					atn = current.getEquvAttrs().get("String");
					an = new AttrNode();
					an.setKey(attrname);
					an.setValue(attrvalue);
					atn.add(an);
					break;
				case Vtypes.vint:
					atn = current.getEquvAttrs().get("int");
					an = new AttrNode();
					an.setKey(attrname);
					an.setValue(attrvalue);
					atn.add(an);
					break;
				}
			}
		}
	}

	@Override
	public void GetEquivalenttb(HashMap<String, ArrayList<BFNode>> BFTtable,
			HashMap<String, EquvNode> EQtable, String TrackingReg, SootMethod sm)
			throws NodeNotFoundException {
		
		Tree BTTree = BFTtable.get(TrackingReg);

		for (Iterator<BFNode> iter = BTTree.preOrderTraversal().iterator(); iter
				.hasNext();) {
			BFNode bfn = iter.next();
			if (bfn.getKey() == null)
				continue;
			// printTree(BTTree, "R10");
			@SuppressWarnings("unchecked")
			BFNode prt = (BFNode) BTTree.parent(bfn);

			if (prt.getKey() == null) {
				SetEqtable("root", EQtable, bfn);
			} else {
				SetEqtable(prt.getKey(), EQtable, bfn);
			}
		}

		GenRegx(EQtable, "root", BFTtable, null, 0);
		System.out.println(sbBody);
	}
	
	private void AddBody(String string) {
		
	}

	private void GenRegx(HashMap<String, EquvNode> EQtable, String key,
			HashMap<String, ArrayList<BFNode>> BFTtable, BFNode child, int ctab)
			throws NodeNotFoundException {
//		if (this.isForward) {
//			// 2015.09.11
////			if (this.XMLTreekey == "" || this.XMLTreekey == null) {
//				EquvNode en = EQtable.get(key);
//				if (en == null)
//					return;
//				HashMap<String, ArrayList<BFNode>> ENtable = en.getENtable();
//				ArrayList<BFNode> al = null;
//				Tree tree = null;
//				boolean last = false;
//				int count = 0;
//		
//				tree = BFTtable.get("");
//	
//				if (child == null) {
//					child = (BFNode) tree.root();
//					// System.out.println("<xml>");
//					last = true;
//				}
//	
//				for (Iterator<BFNode> iter = tree.children(child).iterator(); iter
//						.hasNext();) {
//					BFNode bfn = iter.next();
//	
//					for (int i = 0; i < ctab-1; i++) {
//						sbBody.append("\\t*");
////						System.out.print("(\\t*");
//					}
//	
//					if (last);
////						System.out.print("<\\?xml .*"
////								+ bfn.getKey().replaceAll("\"", ""));
//					else
//					{
//						sbBody.append("<" + bfn.getKey().replaceAll("\"", ""));
////						System.out.print("<" + bfn.getKey().replaceAll("\"", ""));
//					}
//	
//					for (AttrNode an : bfn.getEquvAttrs().get("String")) {
//						sbBody.append(" " + an.getKey().replaceAll("\"", "") + "=" + "\".*\"");
////						System.out.print(" " + an.getKey().replaceAll("\"", "") + "=" + "\".*\"");
//					}
//	
//					for (AttrNode an : bfn.getEquvAttrs().get("Const")) {
//						sbBody.append(" " + an.getKey().replaceAll("\"", "") + "=" + an.getValue());
////						System.out.print(" " + an.getKey().replaceAll("\"", "") + "=" + an.getValue());
//					}
//	
//					for (AttrNode an : bfn.getEquvAttrs().get("int")) {
//						sbBody.append(" " + an.getKey().replaceAll("\"", "")	+ "=" + an.getValue());
//						System.out.print(" " + an.getKey().replaceAll("\"", "")	+ "=" + an.getValue());
//					}
//					
//					if ( tree.children(bfn).size() != 0 && !last) {
//						sbBody.append(">\\n");
////						System.out.print(">\\n");
//					}
//					else if ( tree.children(bfn).size() == 0 && !last) {
//						sbBody.append(">.*");
////						System.out.print(">.*");
//					}
//					
//					
//					if (tree.children(bfn).size() != 0) {
//						ctab++;
//						GenRegx(EQtable, bfn.getKey(), BFTtable, bfn, ctab);
//						ctab--;
//					}
//	
////					for (int i = 0; i < ctab-1; i++) {
////						System.out.print("\\t");
////					}
//					if (!last)
//					{
//						if ( tree.children(bfn).size() != 0 )
//						{
//							sbBody.append("<\\/" + bfn.getKey().replaceAll("\"", "")	+ ">\\n");
////							System.out.print("<\\/" + bfn.getKey().replaceAll("\"", "")	+ ">\\n");
//						}
//						else
//						{
//							if (iter.hasNext()) {
//								sbBody.append("<\\/" + bfn.getKey().replaceAll("\"", "")	+ ">)|");
////								System.out.print("<\\/" + bfn.getKey().replaceAll("\"", "")	+ ">)|");
//							}
//							else {
//								sbBody.append("<\\/" + bfn.getKey().replaceAll("\"", "")	+ ">)\\n");
////								System.out.print("<\\/" + bfn.getKey().replaceAll("\"", "")	+ ">)\\n");
//							}
//						}
//					}
//				}
//	
//				if (last) {
//					;
////					System.out.print("<\\/xml>");
//				}
//				
//				AddBody(sbBody.toString());
////				System.out.println(sbBody);
//			} else {
//				
//				// other two xml parser
//				EquvNode en = EQtable.get(key);
//				if (en == null)
//					return;
//				HashMap<String, ArrayList<BFNode>> ENtable = en.getENtable();
//				ArrayList<BFNode> al = null;
//				Tree tree = null;
//				boolean last = false;
//				int count = 0;
////2015.09.11
////				tree = BFTtable.get(this.XMLTreekey);
//	
//				if (child == null) {
//					child = (BFNode) tree.root();
//					// System.out.println("<xml>");
//					last = true;
//				}
//				
//				for (Iterator<BFNode> iter = tree.children(child).iterator(); iter
//						.hasNext();) {
//					BFNode bfn = iter.next();
//	
//					if (last)
//						;
////						System.out.print("<?xml version='1.0' encoding='"
////								+ bfn.getKey().replaceAll("\"", "") + "'");
//					else
//						sbBody.append("(<" + bfn.getKey().replaceAll("\"", ""));
////						System.out.print("(<" + bfn.getKey().replaceAll("\"", ""));
//	
//					for (AttrNode an : bfn.getEquvAttrs().get("String")) {
//						sbBody.append(" " + an.getKey().replaceAll("\"", "")	+ "=" + "[a-zA-Z0-9]*");
////						System.out.print(" " + an.getKey().replaceAll("\"", "")	+ "=" + "[a-zA-Z0-9]*");
//					}
//	
//					for (AttrNode an : bfn.getEquvAttrs().get("Const")) {
//						sbBody.append(" " + an.getKey().replaceAll("\"", "")
//								+ "=" + an.getValue());
////						System.out.print(" " + an.getKey().replaceAll("\"", "")
////								+ "=" + an.getValue());
//					}
//	
//					for (AttrNode an : bfn.getEquvAttrs().get("int")) {
//						sbBody.append(" " + an.getKey().replaceAll("\"", "")
//								+ "=" + an.getValue());
////						System.out.print(" " + an.getKey().replaceAll("\"", "")
////								+ "=" + an.getValue());
//					}
//	
//					//onriginally println
//					System.out.print(">");
//					if (tree.children(child).size() > 1) {
//						sbBody.append("(\\n|\\t)*)*|");
////						System.out.print("(\\n|\\t)*)*|");
//					}
//					if (!last)
//					{
////						System.out.println("</" + bfn.getKey().replaceAll("\"", "") + ">");
//						sbBody.append("</" + bfn.getKey().replaceAll("\"", "") + ">");
//					}
//				}
//	
//				if (last) {
//					;
////					System.out.println("</xml>");
//				}
//				AddBody(sbBody.toString());
////				System.out.println(sbBody);
//			}
//		} else
//		{
//			if (this.XMLTreekey == "" || this.XMLTreekey == null) {
//				EquvNode en = EQtable.get(key);
//				if (en == null)
//					return;
//				HashMap<String, ArrayList<BFNode>> ENtable = en.getENtable();
//				ArrayList<BFNode> al = null;
//				Tree tree = null;
//				boolean last = false;
//				int count = 0;
//	
//				tree = BFTtable.get("");
//	
//				if (child == null) {
//					child = (BFNode) tree.root();
//					// System.out.println("<xml>");
//					last = true;
//				}
//	
//				for (Iterator<BFNode> iter = tree.children(child).iterator(); iter
//						.hasNext();) {
//					BFNode bfn = iter.next();
//	
////					for (int i = 0; i < ctab; i++) {
//						sbBody.append("\\t*");
////						System.out.print("\t");
////					}
//	
//					if (last) {
////						System.out.print("<?xml version='1.0' encoding='"
////								+ bfn.getKey().replaceAll("\"", "") + "'");
//						sbBody.append("<?xml version='1.0' encoding='"
//								+ bfn.getKey().replaceAll("\"", "") + "'");
//					}
//					else {
//						sbBody.append("<" + bfn.getKey().replaceAll("\"", ""));
////						System.out.print("<" + bfn.getKey().replaceAll("\"", ""));
//					}
//	
//					for (AttrNode an : bfn.getEquvAttrs().get("String")) {
//						sbBody.append(" " + an.getKey().replaceAll("\"", "")
//								+ "=" + "\".*\"");
////						System.out.print(" " + an.getKey().replaceAll("\"", "")
////								+ "=" + "\".*\"");
//					}
//	
//					for (AttrNode an : bfn.getEquvAttrs().get("Const")) {
//						sbBody.append(" " + an.getKey().replaceAll("\"", "")
//								+ "=" + an.getValue());
////						System.out.print(" " + an.getKey().replaceAll("\"", "")
////								+ "=" + an.getValue());
//					}
//	
//					for (AttrNode an : bfn.getEquvAttrs().get("int")) {
//						sbBody.append(" " + an.getKey().replaceAll("\"", "")
//								+ "=" + an.getValue());
////						System.out.print(" " + an.getKey().replaceAll("\"", "")
////								+ "=" + an.getValue());
//					}
//	
//					sbBody.append(">\\n");
////					System.out.println(">");
//	
//					if (tree.children(bfn).size() != 0) {
//						ctab++;
//						GenRegx(EQtable, bfn.getKey(), BFTtable, bfn, ctab);
//						ctab--;
//					}
//	
////					for (int i = 0; i < ctab; i++) {
//						sbBody.append("\\t*");
////						System.out.print("\t");
////					}
//					if (!last) {
////						System.out.println("</" + bfn.getKey().replaceAll("\"", "")	+ ">");
//						sbBody.append("</" + bfn.getKey().replaceAll("\"", "")	+ ">\\n");
//					}
//				}
//	
//				if (last) {
////					System.out.println("</xml>");
//					sbBody.append("</xml>");
//				}
////				System.out.print("\n" + sbBody);
//			} else {
//				// other two xml parser
//				EquvNode en = EQtable.get(key);
//				if (en == null)
//					return;
//				HashMap<String, ArrayList<BFNode>> ENtable = en.getENtable();
//				ArrayList<BFNode> al = null;
//				Tree tree = null;
//				boolean last = false;
//				int count = 0;
//	
//				tree = BFTtable.get(this.XMLTreekey);
//	
//				if (child == null) {
//					child = (BFNode) tree.root();
//					// System.out.println("<xml>");
//					last = true;
//				}
//				
//				for (Iterator<BFNode> iter = tree.children(child).iterator(); iter
//						.hasNext();) {
//					BFNode bfn = iter.next();
//	
//					if (last)
//						System.out.print("<?xml version='1.0' encoding='"
//								+ bfn.getKey().replaceAll("\"", "") + "'");
//					else
//						System.out.print("(<" + bfn.getKey().replaceAll("\"", ""));
//	
//					for (AttrNode an : bfn.getEquvAttrs().get("String")) {
//						System.out.print(" " + an.getKey().replaceAll("\"", "")
//								+ "=" + "[a-zA-Z0-9]*");
//					}
//	
//					for (AttrNode an : bfn.getEquvAttrs().get("Const")) {
//						System.out.print(" " + an.getKey().replaceAll("\"", "")
//								+ "=" + an.getValue());
//					}
//	
//					for (AttrNode an : bfn.getEquvAttrs().get("int")) {
//						System.out.print(" " + an.getKey().replaceAll("\"", "")
//								+ "=" + an.getValue());
//					}
//	
//					//onriginally println
//					System.out.print(">");
//					if (tree.children(child).size() > 1) {
//						System.out.print("(\\n|\\t)*)*|");
//					}
//					if (!last)
//						System.out.println("</" + bfn.getKey().replaceAll("\"", "")
//								+ ">");
//				}
//	
//				if (last) {
//					System.out.println("</xml>");
//				}
//			}
//		}
	}

	private EquvNode getEquvNode(String key, HashMap<String, EquvNode> EQtable) {
		if (EQtable.containsKey(key)) {
			EquvNode qn = EQtable.get(key);
			return qn;
		} else {
			EquvNode qn = new EquvNode();
			EQtable.put(key, qn);
			return qn;
		}
	}

	private int checkVtype(BFNode bfn) {

		if (bfn.getVtype().indexOf("String") != -1) {
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

	private void SetEqtable(String key, HashMap<String, EquvNode> EQtable,
			BFNode bfn) {
		EquvNode en = null;
		en = getEquvNode(key, EQtable);
		HashMap<String, ArrayList<BFNode>> ENtable = null;
		ENtable = en.getENtable();
		switch (checkVtype(bfn)) {
		case Vtypes.vstring:
			ENtable.get("String").add(bfn);
			// System.out.println("String value : " + bfn.getKey());
			break;
		case Vtypes.vconst:
			ENtable.get("Const").add(bfn);
			// System.out.println("Const value : " + bfn.getValue());
			// System.out.print("Attrs : ");
			// for ( AttrNode an : bfn.getEquvAttrs().get("String") ) {
			// System.out.print(an.getKey() + " ");
			// }
			// System.out.println("");
			break;
		case Vtypes.vint:
			ENtable.get("int").add(bfn);
			// System.out.println("int value : " + bfn.getKey());
			break;
		case Vtypes.vjsonarray:
			ENtable.get("JSONArray").add(bfn);
			// System.out.println("JSONArray key : " + bfn.getKey());
			break;
		default:
			// System.out.println("Error!");
			break;
		}
	}

	public boolean isconst(String arg) {
		if (arg.indexOf("$") != -1)
			return false;
		else
			return true;
	}

	public boolean isMulti() {
		return isMulti;
	}

	public void setMulti(boolean isMulti) {
		this.isMulti = isMulti;
	}

	@Override
	public void ExtractingExpr(StaticInvokeExpr sie,
			HashMap<String, SymbolEntry> sMtable,
			HashMap<String, ArrayList<BFNode>> BFTtable, Unit ut, String name,
			SootMethod methodOf, String string) {
		
		
	}

	@Override
	public boolean isContainDP(Block block)
	{
		// todo Auto-generated method stub
		return false;
	}

}
*/