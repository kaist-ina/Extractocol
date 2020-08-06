package extractocol.backend.response.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import extractocol.Constants;
import extractocol.backend.request.basics.BFNode;
import extractocol.backend.response.basics.BFNode_Response;
import extractocol.common.pairing.SemanticAppliedEntry;
import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.Constant;
import soot.jimple.InvokeExpr;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;

public class UtilHelper {
	public static boolean isForward = false;

	
	public static void CopyParam(InvokeExpr vie, HashMap<String, ArrayList<BFNode_Response>> SubBFTtable, HashMap<String, ArrayList<BFNode_Response>> BFTtable)
	{
		int paramcount = 0;

		// runnable
		int start = vie.getMethodRef().toString().indexOf("(");
		int end = vie.getMethodRef().toString().indexOf(")");
		String parametertype = vie.getMethodRef().toString().substring(start + 1, end);
		String[] parameters = parametertype.split(",");
		for (int i = 0; i < parameters.length; i++)
		{
			String targetParam = parameters[i];
			if (targetParam.contains("java.lang.Runnable"))
				Constants.RunnableCalss = vie.getArg(i).getType().toString();
		}

		for (Value v : vie.getArgs())
		{
			String stringForUrl = null;
			BFNode bfn = new BFNode();
			if (!isForward)
			{

				// constant
				if (v instanceof Constant)
				{
					stringForUrl = v.toString();
					bfn.makeUrlBfn(stringForUrl);
				}
//
//				// if array copy bfn array -- woohyun 20160331
//				else if (BFTtable.get(v.toString()) != null && BFTtable.get(v.toString()).size() >= 1
//						&& BFNode.isArray(BFTtable.get(v.toString()).get(0)))
//				{
//					BFNode targetbfn = BFTtable.get(v.toString()).get(0);
//
//					// array init
//					bfn.initarray(targetbfn.getSize());
//
//					// stringforurl copy
//					bfn.makeUrlBfn(targetbfn.getStringForUrl());
//
//					// orcase copy
//					if (targetbfn.getArrayorcase() != null)
//						bfn.setArrayorcase((ArrayList<String>) targetbfn.getArrayorcase().clone());
//
//				}

				// normal varialbe
				else
				{
					// variable
//					stringForUrl = GenRegex(null, BFTtable, v.toString());

					bfn = new BFNode();
					bfn.makeUrlBfn(stringForUrl);
				}

				// JM for extendedType
//				if (BFTtable.get(v.toString()) != null)
//					if (BFTtable.get(v.toString()).size() > 0)
//						bfn.setExtendedType(BFTtable.get(v.toString()).get(0).getExtendedType());

				ArrayList<BFNode> list = new ArrayList<BFNode>();
				list.add(bfn);

				if (BFTtable.get(v.toString()) != null)
				{
					if (BFTtable.get(v.toString()).size() > 0)
					{
//						if (BFTtable.get(v.toString()).get(0).hasVtype())
//						{
//							if (BFTtable.get(v.toString()).get(0).getVtype().contains("HashMap")
//									|| BFTtable.get(v.toString()).get(0).getVtype().contains("BasicNameValuePair"))
//							{
//								SubBFTtable.put("@parameter" + paramcount, BFTtable.get(v.toString()));
//							}
//						}
					}
				}

//				if (!SubBFTtable.keySet().contains("@parameter" + paramcount))
//					SubBFTtable.put("@parameter" + paramcount, list);
//				paramcount++;
			}
		}
	}
	
	public void CopyListReference(ArrayList<BFNode> src, ArrayList<BFNode> arraybfn)
	{

		arraybfn.clear();

		for (BFNode bfn : src)
		{
			try
			{
				arraybfn.add((BFNode) bfn.clone());
			} catch (CloneNotSupportedException e)
			{
				e.printStackTrace();
			}
		}
	}

	protected ArrayList<SemanticAppliedEntry> CopyPairingList(ArrayList<SemanticAppliedEntry> plist)
	{
		if (plist == null)
			return null;
		ArrayList<SemanticAppliedEntry> tlist = new ArrayList<SemanticAppliedEntry>();

		for (SemanticAppliedEntry e : plist)
		{
			tlist.add(new SemanticAppliedEntry(e.getMethod(), e.getStmt()));
		}
		return tlist;
	}

	public static boolean isHashMap(HashMap<String, ArrayList<BFNode>> BFTtable, String key)
	{
		if (BFTtable.get(key) != null)
			if (BFTtable.get(key).size() > 0)
				if (BFTtable.get(key).get(0).hasVtype())
					if (BFTtable.get(key).get(0).getVtype().contains("HashMap") || BFTtable.get(key).get(0).getVtype().contains("BasicNameValuePair"))
						return true;

		return false;

	}

	public static ArrayList<BFNode_Response> CopyList(ArrayList<BFNode_Response> src)
	{
		if (src == null)
			return null;
		ArrayList<BFNode_Response> tlist = new ArrayList<BFNode_Response>();

		for (BFNode_Response bfn : src)
		{
			try
			{
				tlist.add((BFNode_Response) bfn.clone());
			} catch (CloneNotSupportedException e)
			{
				e.printStackTrace();
			}
		}
		return tlist;
	}

	
	public static String CropVar(String var)
	{
		if (var.startsWith("$"))
			return var.substring(var.indexOf(".") + 1);
		else
			return var;
	}

	
	
	public static boolean hasNode(HashMap<String, ArrayList<BFNode_Response>> bufttable, String Key)
	{
		return bufttable.containsKey(Key);
	}

	
	
	// draw visualization graph
	public static DirectedGraph<String, DefaultEdge> createStringGraph(BriefBlockGraph bg)
	{
		DirectedGraph<String, DefaultEdge> dg = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		Set<String> EdgeSet = new HashSet<String>();

		for (Block b : bg.getBlocks())
		{
			String Current = String.valueOf(bg.getBlocks().indexOf(b));
			System.out.println("{ id: " + Current + ", label: '" + Current + "'},");
		}

		for (Block b : bg.getBlocks())
		{
			String Current = String.valueOf(bg.getBlocks().indexOf(b));
			// dg.addVertex( Current );

			// for (Block b2 : bg.getPredsOf(b)) {
			// String Pred = String.valueOf(bg.getBlocks().indexOf(b2));
			// // dg.addVertex( Pred );
			// // dg.addEdge( Pred , Current);
			//
			// EdgeSet.add("{from: " + Pred + ", to:" + Current
			// +", arrows:'to'},");
			// }

			for (Block b2 : bg.getSuccsOf(b))
			{
				String Succ = String.valueOf(bg.getBlocks().indexOf(b2));
				// dg.addVertex( Succ );
				// dg.addEdge( Current, Succ );

				EdgeSet.add("{from: " + Current + ", to:" + Succ + ", arrows:'to'},");
			}
		}

		for (String a : EdgeSet)
			System.out.println(a);

		return dg;
	}
}
