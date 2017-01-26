
package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapHandler;
import Extractocol.BufferExtractor_Request.Helper.ExtractocolLoopFinder;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;
import soot.jimple.Constant;
import soot.jimple.Stmt;

public class put extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString()
				.equals("<org.json.JSONObject: org.json.JSONObject put(java.lang.String,java.lang.Object)>"))
		{
			ArrayList<BFNode> tr = spb.BFTtable.get(spb.iie.getBase().toString());
			if (spb.iie.getArg(1) instanceof Constant)
			{
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(".*");
				bfn.setKey(spb.iie.getArg(0).toString());
				bfn.setValue(spb.iie.getArg(1).toString());
				bfn.setVtype("org.json.JSONObject");
				tr.add(bfn);
			}
			else
			{
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(".*");
				bfn.setKey(spb.iie.getArg(0).toString());
				bfn.setValue(".*");
				bfn.setVtype("org.json.JSONObject");
				tr.add(bfn);
			}
		}
		else
		{
			if (spb.iie.getMethodRef().toString()
					.equals("<org.json.JSONObject: org.json.JSONObject put(java.lang.String,int)>"))
			{
				/*
				ArrayList<BFNode> tr = spb.BFTtable.get(spb.iie.getBase().toString());
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(spb.iie.getArg(0).toString());
				tr.add(bfn);*/
				//Duong				
				String base = spb.iie.getBase().toString();
				ArrayList<BFNode> tr = spb.BFTtable.get(base);
				if (spb.iie.getArg(0) instanceof Constant && spb.iie.getArg(1) instanceof Constant)
				{
					String arg0 = spb.iie.getArg(0).toString().replaceAll("\"","");
					String arg1 = spb.iie.getArg(1).toString().replaceAll("\"","");
					for (BFNode bfn : tr){
						if (bfn.getKey().equals(arg0))
							bfn.setValue(arg1);
					}
				}
				else if (spb.iie.getArg(0) instanceof Constant)
				{
					String arg0 = spb.iie.getArg(0).toString().replaceAll("\"","");
					String arg1 = spb.iie.getArg(1).toString();
					arg1 = spb.ub.GenRegex(null, spb.BFTtable, arg1);
					for (BFNode bfn : tr){
						if (bfn.getKey().equals(arg0))
							bfn.setValue(arg1);
					}
				}				
			}
			else
			{
				if (spb.iie.getMethodRef().toString()
						.equals("<org.json.JSONArray: org.json.JSONArray put(java.lang.Object)>"))
				{
					ArrayList<BFNode> tr = spb.BFTtable.get(spb.iie.getBase().toString());
					BFNode bfn = new BFNode();
					if (spb.iie.getArg(0) instanceof Constant)
					{
						bfn.makeUrlBfn(spb.iie.getArg(0).toString());
						tr.add(bfn);
					}
				}
				else
				{
					if (spb.iie.getMethodRef().toString().equals(
							"<java.util.concurrent.ConcurrentHashMap: java.lang.Object put(java.lang.Object,java.lang.Object)>"))
					{
						ArrayList<BFNode> tr = spb.BFTtable.get(spb.iie.getBase().toString());
						if (spb.iie.getArg(1) instanceof Constant)
						{
							BFNode bfn = new BFNode();
							bfn.makeUrlBfn(spb.iie.getArg(0).toString());
							tr.add(bfn);
						}
						else
						{
							BFNode bfn = new BFNode();
							String r = spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(0).toString());
							bfn.makeUrlBfn(r);
							bfn.setVtype("HashMap");
							boolean isHas = false;
							for (BFNode src : tr)
							{
								if (src.getStringForUrl() != null)
									if (src.getStringForUrl().equals(bfn.getStringForUrl()))
									{
										isHas = true;
									}
							}
							if (!isHas)
								tr.add(bfn);
							HeapHandler.requestUpdate = true;
						}
					}
					else
					{
						if (spb.iie.getMethodRef().getSignature()
								.equals("<java.util.Map: java.lang.Object put(java.lang.Object,java.lang.Object)>"))
						{
							ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());
							BFNode bfn = new BFNode();
							if (list.size() > 0)
								list.get(0).setVtype("Map");
							else
								bfn.setVtype("Map");
							// Loop case
							ExtractocolLoopFinder loopFinder = new ExtractocolLoopFinder();
							Set<List<Stmt>> loops = loopFinder.getLoops(spb.sm.getActiveBody());
							Iterator<List<Stmt>> iter = loops.iterator();
							while (iter.hasNext())
							{
								List<Stmt> loopstmt = iter.next();
								for (Stmt stmt : loopstmt)
								{
									if (stmt.toString().contains(spb.iie.toString()))
										bfn.setLoop(true);
								}
							}
							if (spb.iie.getArg(0) instanceof Constant)
							{
								bfn.setKey(spb.iie.getArg(0).toString().replaceAll("\"", ""));
								if (spb.iie.getArg(1) instanceof Constant)
									bfn.setValue(spb.iie.getArg(1).toString().replaceAll("\"", ""));
								else
									bfn.setValue("(.*)");
								list.add(bfn);
							}
							else
							{
								bfn.setKey("(.*)");
								if (spb.iie.getArg(1) instanceof Constant)
									bfn.setValue(spb.iie.getArg(1).toString().replaceAll("\"", ""));
								else
									bfn.setValue("(.*)");
								list.add(bfn);
							}
						}
						else
						{
							if (spb.iie.getMethodRef().getSignature().equals(
									"<java.util.HashMap: java.lang.Object put(java.lang.Object,java.lang.Object)>"))
							{
								ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());
								BFNode bfn = new BFNode();
								if (list.size() > 0)
									list.get(0).setVtype("HashMap");
								else
									bfn.setVtype("HashMap");
								// Loop case
								ExtractocolLoopFinder loopFinder = new ExtractocolLoopFinder();
								Set<List<Stmt>> loops = loopFinder.getLoops(spb.sm.getActiveBody());
								Iterator<List<Stmt>> iter = loops.iterator();
								while (iter.hasNext())
								{
									List<Stmt> loopstmt = iter.next();
									for (Stmt stmt : loopstmt)
									{
										if (stmt.toString().contains(spb.iie.toString()))
											bfn.setLoop(true);
									}
								}
								if (spb.iie.getArg(0) instanceof Constant)
								{
									bfn.setKey(spb.iie.getArg(0).toString().replaceAll("\"", ""));
									if (spb.iie.getArg(1) instanceof Constant)
										bfn.setValue(spb.iie.getArg(1).toString().replaceAll("\"", ""));
									else
										bfn.setValue("(.*)");
									list.add(bfn);
								}
								else
								{
									bfn.setKey("(.*)");
									if (spb.iie.getArg(1) instanceof Constant)
										bfn.setValue(spb.iie.getArg(1).toString().replaceAll("\"", ""));
									else
										bfn.setValue("(.*)");
									list.add(bfn);
								}
							}
						}
					}
				}
			}
		}
	}
}
