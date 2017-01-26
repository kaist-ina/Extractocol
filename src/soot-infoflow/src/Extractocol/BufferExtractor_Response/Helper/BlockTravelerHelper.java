package Extractocol.BufferExtractor_Response.Helper;


import java.util.Iterator;
import java.util.List;
import java.util.Set;

import Extractocol.BufferExtractor_Request.Helper.ExtractocolLoopFinder;
import soot.Body;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;

public class BlockTravelerHelper {
	
	
	public static int getBlockNum(BriefBlockGraph Bg, Unit ut)
	{
		for (Block b : Bg.getBlocks())
		{

			Iterator<Unit> IterUnit = b.iterator();

			while (IterUnit.hasNext())
			{
				Unit in = IterUnit.next();
				if (in.equals(ut))
				{
					return Bg.getBlocks().indexOf(b);
				}
			}
		}
		return -1;
	}


	public static void CalcEdgeTable(Integer[][] edgeTable, BriefBlockGraph bg2)
	{
		for (Block b : bg2)
		{
			for (Block outedge : bg2.getSuccsOf(b))
			{
				edgeTable[bg2.getBlocks().indexOf(b)][bg2.getBlocks().indexOf(outedge)] = 1;
			}
		}
	}

	public static Integer[][] ManipulateBackEdge(Integer[][] edgeTable, BriefBlockGraph bg)
	{
		Integer[][] rtn = new Integer[bg.getBlocks().size()][bg.getBlocks().size()];

		for (int i = 0; i < bg.size(); i++)
		{
			for (int j = 0; j < bg.size(); j++)
			{
				rtn[i][j] = 0;
			}
		}

		for (int i = 0; i < bg.getBlocks().size(); i++)
		{
			for (int j = 0; j < bg.getBlocks().size(); j++)
			{
				if (isBackEdge(bg, "", i, j))
				{

					for (int k = 0; k < bg.getBlocks().size(); k++)
					{
						if (edgeTable[j][k] == 1 && k != i)
						{
							edgeTable[i][k] = 1;
							rtn[i][k] = 1;
							break;
						}
					}
					edgeTable[i][j] = 0;
				}
			}
		}
		return rtn;
	}
	
	public static boolean isManiEdge(Integer[][] maniEdge, int blockNumber, int bgsize)
	{
		for (int i = 0; i < bgsize; i++)
		{
			if (maniEdge[i][blockNumber] == 1)
				return true;
		}
		return false;
	}

	
	public static boolean isBackEdge(BriefBlockGraph Bg, String sig, int from, int to)
	{
		Body b = Bg.getBody();

		ExtractocolLoopFinder loopFinder = new ExtractocolLoopFinder();
		Set<List<Stmt>> loops = loopFinder.getLoops(b);
		// Set<List<StmtLine>> loops = loopFinder.getLoops(b);

		for (List<Stmt> lp : loops)
		{
			if (lp.size() < 2)
			{
				return false;
			}
			if (isBlockContainsStmt(Bg, from, lp.get(lp.size() - 2)) && isBlockContainsStmt(Bg, to, lp.get(lp.size() - 1)))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean BlockSelector(int bidx, BriefBlockGraph Bg, String sig, Integer[][] EdgeEntry, Integer[][] maniEdge)
	{
		for (int i = 0; i < Bg.getBlocks().size(); i++)
		{
			if (EdgeEntry[i][bidx] == 1 && maniEdge[i][bidx] == 1)
				return true;
			else if (EdgeEntry[i][bidx] == 1 && (!isBackEdge(Bg, sig, i, bidx)))
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean isBlockContainsStmt(BriefBlockGraph Bg, int index, Stmt stmt)
	{
		for (Iterator<Unit> iter = Bg.getBlocks().get(index).iterator(); iter.hasNext();)
		{
			Unit ut = iter.next();
			if (ut.toString().equals(stmt.toString()))
			{
				return true;
			}
		}
		return false;
	}
}
