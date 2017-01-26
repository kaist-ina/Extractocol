
package Extractocol.BufferExtractor_Request.HeapManagement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class HeapTreeNode
{
	/*
	 * For Root Node
	 */
	public List<String> listURL = new LinkedList<String>();
	/*
	 * For Normal Node
	 */
	String strResult;
	public String getStrResult()
	{
		return strResult;
	}
	public void setStrResult(String strResult)
	{
		this.strResult = strResult;
	}
	String strThisHeap;
	List<SourceforTaint> lstTaintSourceInfo = new ArrayList<SourceforTaint>();
	boolean isAnalyzed = false;
	public boolean isAnalyzed()
	{
		return isAnalyzed;
	}
	public List<SourceforTaint> getLstTaintSourceInfo()
	{
		return lstTaintSourceInfo;
	}
	public String getstrThisHeap()
	{
		return strThisHeap;
	}
	public void setAnalyzed(boolean isAnalyzed)
	{
		this.isAnalyzed = isAnalyzed;
	}
	public class SourceforTaint
	{
		String SourceMethod;
		String SeedObject;
		String Unit;
		public String getSourceMethod()
		{
			return SourceMethod;
		}
		public String getSeedObject()
		{
			return SeedObject;
		}
		public String getUnit()
		{
			return Unit;
		}
	}
	public void addSourcesforTaint(String _methodsig, String _seedobject, String _unit)
	{
		SourceforTaint newsource = new SourceforTaint();
		newsource.SeedObject = _seedobject;
		newsource.SourceMethod = _methodsig;
		newsource.Unit = _unit;
		lstTaintSourceInfo.add(newsource);
	}
}
