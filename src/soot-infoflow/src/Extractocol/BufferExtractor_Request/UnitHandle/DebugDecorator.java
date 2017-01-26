package Extractocol.BufferExtractor_Request.UnitHandle;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.SignatureBuilder_Request;
import Extractocol.BufferExtractor_Request.Basics.ParameterBucket;
import soot.Unit;

public class DebugDecorator extends AbstractUnitHandleDecorator
{
	public void HandleUnit(Unit ut, ParameterBucket pb, SignatureBuilder_Request sb)
	{
		super.HandleUnit(ut, pb, sb);
		
		if (!Constants.isDiffMode)
		{
			Constants.DebugInfo.AddUnitBFTPair(ut, pb.CurrentSM, pb.blockBFTtable);
		}
		else
		{
			Constants.DebugInfo.CheckDiff(ut, pb.CurrentSM, pb.blockBFTtable);
		}
	}	
}
