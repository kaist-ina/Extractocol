package extractocol.backend.request.unitHandle;

import extractocol.Constants;
import extractocol.backend.request.SignatureBuilder_Request;
import extractocol.backend.request.basics.ParameterBucket;
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
