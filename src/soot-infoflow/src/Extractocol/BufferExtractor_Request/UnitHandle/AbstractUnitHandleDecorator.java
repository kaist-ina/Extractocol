package Extractocol.BufferExtractor_Request.UnitHandle;

import Extractocol.BufferExtractor_Request.SignatureBuilder_Request;
import Extractocol.BufferExtractor_Request.Basics.ParameterBucket;
import soot.Unit;

public class AbstractUnitHandleDecorator extends AbstractUnitHandler
{
	protected AbstractUnitHandler auh;
	
	public void SetTheUnitHandler(AbstractUnitHandler _auh)
	{
		auh = _auh;
	}
	
	@Override
	public void HandleUnit(Unit ut, ParameterBucket pb, SignatureBuilder_Request sb)
	{
		if (auh != null)
		{
			auh.HandleUnit(ut, pb, sb);
		}
	}
}
