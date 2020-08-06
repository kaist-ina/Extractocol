package extractocol.backend.request.semantic.url.models;


public class DebugDecorator extends AbstractDecorator
{
	public void applySemantic(SemanticParameterBucket spb)
	{
		super.applySemantic(spb);
//		System.out.println("Debugging Code!!!");
//		System.out.println("Current Instruction : " +  spb.iie);
	}
}
