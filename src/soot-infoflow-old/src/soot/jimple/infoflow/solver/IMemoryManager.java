package soot.jimple.infoflow.solver;

/**
 * Common interface of all memory managers that can be used with FlowDroid's
 * solvers. Note that not every solver is required to support memory managers.
 * 
 * @author Steven Arzt
 *
 */
public interface IMemoryManager<D> {
	
	/**
	 * Tells the memory manager to handle the given object. Implementations are
	 * free to replace the incoming object with a different reference.
	 * @param obj The object to handle
	 * @return The new reference that shall be used instead of the old one
	 */
	public D handleMemoryObject(D obj);
	
	/**
	 * Tells the memory manager to optimize the given generated object. The
	 * output was computed from the input object using a flow function.
	 * @param input The original input to the flow function
	 * @param output The output of the flow function
	 * @return The new refrence to use instead of the original output
	 */
	public D handleGeneratedMemoryObject(D input, D output);
	
}
