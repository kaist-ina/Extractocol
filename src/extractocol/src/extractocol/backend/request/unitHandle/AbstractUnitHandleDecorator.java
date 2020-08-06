package extractocol.backend.request.unitHandle;

import extractocol.Constants;
import extractocol.backend.request.SignatureBuilder_Request;
import extractocol.backend.request.basics.ParameterBucket;
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
