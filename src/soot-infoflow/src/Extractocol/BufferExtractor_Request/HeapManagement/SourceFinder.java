
package Extractocol.BufferExtractor_Request.HeapManagement;

import java.util.ArrayList;
import java.util.Iterator;
import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapTreeNode.SourceforTaint;
import Extractocol.Outputs.RequestInfoEntry;
import Extractocol.Outputs.RequestInfoList;
import Extractocol.Outputs.RequestInfoList.ConcreteTask;
import soot.Body;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.Constant;

/**
 * For find source method and taint seed for Multi-level HeapObject.
 * 
 * @author Jeongmin Kim
 * @Date : 2016. 9. 6.
 * @Version : 1.00
 */
public class SourceFinder
{
	/**
	 * Find source method of Heap Object by iterating all methods.
	 * 
	 * @author Jeongmin Kim
	 * @version 1.00
	 * @param lstRequestInfo
	 *            arraylist of request information which includes one url signature.
	 */
	public void FindSource()
	{
		ArrayList<SootClass> AllClasses = Constants.fsc.getClasses();
		SplitTreeForEachHeaps();
		for (SootClass sc : AllClasses)
		{
			for (final SootMethod sm : sc.getMethods())
			{
				if (sm.hasActiveBody())
				{
					RequestInfoList.ReqListIteration(false, new ConcreteTask()
					{
						@Override
						public void execute(HeapTreeNode node, RequestInfoEntry entry)
						{
							if (node.strThisHeap != null)
							{
								FindHeapInUnit(sm.getActiveBody(), node);
							}
						}
					});
				}
			}
		}
	}
	/**
	 * We try to find target heapobject for each
	 * 
	 * @author Jeongmin Kim
	 * @version 1.00
	 * @param b
	 *            body of sootmethod
	 * @param targetHeapStrings
	 *            arraylist of targetheap strings.
	 */
	private void FindHeapInUnit(final Body b, final HeapTreeNode node)
	{
		final PatchingChain<Unit> units = b.getUnits();
		for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();)
		{
			final Unit u = iter.next();
			u.apply(new AbstractStmtSwitch()
			{
				public void caseAssignStmt(AssignStmt as)
				{
					if (hasHeapObjectInAssignUnit(as.getLeftOp().toString(), node.strThisHeap))
					{
						if (as.getRightOp() instanceof Constant)
						{
							node.strResult = as.getRightOp().toString().replaceAll("\"", "");
						}
						else
						{
							// JM Side Case!!!!
							// System.out.println("Found target heap in assignstatement : " + as.toString() + " in method " + b.getMethod().getSignature());
							node.addSourcesforTaint(b.getMethod().getSignature(), as.getRightOp().toString(), as.toString());
						}
					}
				}
			});
		}
	}
	public boolean hasHeapObjectInAssignUnit(String LeftOpOfAssign, String targetHeapString)
	{
		if (LeftOpOfAssign.contains(targetHeapString))
			return true;
		return false;
	}
	/**
	 * When this function is start, HeapTree has only root tree. so we split Tree for Each nodes.
	 * 
	 * @author Jeongmin Kim
	 * @version 1.00
	 * @param lstRequestInfo
	 *            arraylist of request information which includes one url signature.
	 * @return void
	 */
	private void SplitTreeForEachHeaps()
	{
		RequestInfoList.ReqListIteration(false, new ConcreteTask()
		{
			@Override
			public void execute(HeapTreeNode node, RequestInfoEntry entry)
			{
				for (String EachHeap : node.listURL)
				{
					if (Constants.isDebug)
						System.out.println(EachHeap);
					HeapTreeNode newNode = new HeapTreeNode();
					newNode.strThisHeap = EachHeap;
					entry.HeapTree.add(newNode);
				}
			}
		});
	}
	/**
	 * @author Jeongmin Kim
	 * @version 1.00
	 * @param lstRequestInfo
	 *            a list of request URL information. Such as URL signatures. lstTaintSourceInfo Source Methods and Seed Objects for a heap object.
	 */
	public void PrintHeapResult()
	{
		RequestInfoList.ReqListIteration(true, new ConcreteTask()
		{
			@Override
			public void execute(HeapTreeNode node, RequestInfoEntry entry)
			{
				if (node.strResult != null)
				{
					System.out.println(node.strThisHeap + " = " + node.strResult);
					System.out.println("Result Url : " + entry.getSignature().replaceAll(node.strThisHeap, node.strResult));
				}
				else
				{
					if (node.lstTaintSourceInfo.size() > 0)
					{
						System.out.println("Print Source Information");
						for (SourceforTaint sft : node.lstTaintSourceInfo)
						{
							System.out.println(sft.SourceMethod + " in " + sft.SeedObject);
						}
					}
				}
			}
		});
	}
}
