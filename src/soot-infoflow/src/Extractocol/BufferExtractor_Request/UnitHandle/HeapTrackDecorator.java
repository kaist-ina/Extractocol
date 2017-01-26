
package Extractocol.BufferExtractor_Request.UnitHandle;

import java.io.File;
import java.util.Collection;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.SignatureBuilder_Request;
import Extractocol.BufferExtractor_Request.Basics.ParameterBucket;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapTreeNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.UrlBuilder;
import Extractocol.Outputs.RequestInfoEntry;
import Extractocol.Outputs.RequestInfoList;
import soot.Unit;

public class HeapTrackDecorator extends AbstractUnitHandleDecorator
{
	public void HandleUnit(Unit ut, ParameterBucket pb, SignatureBuilder_Request sb)
	{
		super.HandleUnit(ut, pb, sb);
		ExtractResponseHandler.ExtractResponseEP(ut);
		
		// JM why it is disabled?
		/*File f = new File(Constants.RequestInfoPath());
		if (f.exists())
		{
			RequestInfoList.lstRequestInfo = RequestInfoList.Deserialize();
			for (RequestInfoEntry reqEntry : RequestInfoList.lstRequestInfo)
			{
				Collection<HeapTreeNode> inorder = reqEntry.getHeapTree().inOrderTraversal();
				for (HeapTreeNode node : inorder)
				{
					if (!node.isAnalyzed() && node.getstrThisHeap() != null && node.getLstTaintSourceInfo().size() > 0)
					{
						if (ut.toString().equals(node.getLstTaintSourceInfo().get(0).getUnit()))
						{
							if (sb instanceof UrlBuilder)
							{
								UrlBuilder ub = (UrlBuilder) sb;
								String resultUrl = ub.printUrlFromList(pb.blockBFTtable, "$r6");
								System.out.println(resultUrl);
								node.setAnalyzed(true);
								node.setStrResult(resultUrl);
							}
						}
					}
				}
			}
			RequestInfoList.Serialize(RequestInfoList.lstRequestInfo);
		}*/
	}
}
