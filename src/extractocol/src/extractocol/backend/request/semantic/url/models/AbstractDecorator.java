package extractocol.backend.request.semantic.url.models;

public class AbstractDecorator extends BaseModel
{
	protected BaseModel bm;
	public void setBaseModel(BaseModel _bm)
	{
		bm = _bm;
	}
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (bm != null)
			bm.applySemantic(spb);
	}
}
