package Extractocol.BufferExtractor_Request.UnitHandle;
import Extractocol.BufferExtractor_Request.SignatureBuilder_Request;
import Extractocol.BufferExtractor_Request.Basics.ParameterBucket;
import soot.Unit;

abstract class AbstractUnitHandler
{
	public abstract void HandleUnit(Unit ut, ParameterBucket pb, SignatureBuilder_Request sb);
}
