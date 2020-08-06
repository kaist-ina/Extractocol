
package extractocol.backend.request.heapManagement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
public class HeapTreeNode implements Serializable 
{
	/*
	 * For Root Node
	 */
	public List<String> listURL = new LinkedList<String>();
	/*
	 * For Normal Node
	 */
	String strResult;
	
	String strThisHeap;
	List<SourceforTaint> lstTaintSourceInfo = new ArrayList<SourceforTaint>();
	boolean isAnalyzed = false;
	
	public String getStrResult()
	{
		return strResult;
	}
	public void setStrResult(String strResult)
	{
		this.strResult = strResult;
	}
	
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
	
	public void clear() {
		if(this.listURL != null)
			this.listURL.clear();
		
		if(this.lstTaintSourceInfo != null)
			this.lstTaintSourceInfo.clear();
		
		this.strResult = null;
		this.strThisHeap = null;
	}
	
	/*public void addSourcesforTaint(String _methodsig, String _seedobject, String _unit)
	{
		SourceforTaint newsource = new SourceforTaint();
		newsource.SeedObject = _seedobject;
		newsource.SourceMethod = _methodsig;
		newsource.Unit = _unit;
		lstTaintSourceInfo.add(newsource);
	}*/
}
