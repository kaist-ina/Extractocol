package soot.jimple.infoflow.data.pathBuilders;

import java.util.HashSet;
import java.util.Set;

import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

/**
 * Abstract base class for all abstraction path builders
 * 
 * @author Steven Arzt
 */
public abstract class AbstractAbstractionPathBuilder implements
		IAbstractionPathBuilder {
	
	protected final IInfoflowCFG icfg;
	protected final boolean reconstructPaths;
	protected Set<OnPathBuilderResultAvailable> resultAvailableHandlers = null;
	
	/**
	 * Creates a new instance of the {@link AbstractAbstractionPathBuilder} class
	 * @param icfg The interprocedural control flow graph
	 * @param reconstructPaths True if the exact propagation path between source
	 * and sink shall be reconstructed.
	 */
	public AbstractAbstractionPathBuilder(IInfoflowCFG icfg,
			boolean reconstructPaths) {
		this.icfg = icfg;
		this.reconstructPaths = reconstructPaths;
	}
	
	@Override
	public void addResultAvailableHandler(OnPathBuilderResultAvailable handler) {
		if (this.resultAvailableHandlers == null)
			this.resultAvailableHandlers = new HashSet<>();
		this.resultAvailableHandlers.add(handler);
	}

}
