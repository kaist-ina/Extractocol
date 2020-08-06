package extractocol.backend.request.unitHandle;
import extractocol.backend.request.SignatureBuilder_Request;
import extractocol.backend.request.basics.ParameterBucket;
import soot.Unit;

abstract class AbstractUnitHandler
{
	public abstract void HandleUnit(Unit ut, ParameterBucket pb, SignatureBuilder_Request sb);
}
