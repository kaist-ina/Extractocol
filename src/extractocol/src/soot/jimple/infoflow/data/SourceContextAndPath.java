package soot.jimple.infoflow.data;

import heros.solver.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import soot.jimple.Stmt;
import soot.jimple.infoflow.InfoflowConfiguration;

/**
 * Extension of {@link SourceContext} that also allows a paths from the source
 * to the current statement to be stored
 * 
 * @author Steven Arzt
 */
public class SourceContextAndPath extends SourceContext implements Cloneable {
	protected List<Abstraction> path = null;
	protected List<Stmt> callStack = null;
	protected int neighborCounter = 0;
	private int hashCode = 0;
	
	public SourceContextAndPath(AccessPath value, Stmt stmt) {	
		this(value, stmt, null);
	}
	
	public SourceContextAndPath(AccessPath value, Stmt stmt, Object userData) {
		super(value, stmt, userData);
	}
	
	public List<Abstraction> getAbstractionPath() {
		return path;
	}
	
	public List<Stmt> getPath() {
		if (path == null)
			return Collections.<Stmt>emptyList();
		List<Stmt> stmtPath = new ArrayList<Stmt>(this.path.size());
		for (Abstraction abs : this.path)
			if (abs.getCurrentStmt() != null)
				stmtPath.add(abs.getCurrentStmt());
		return stmtPath;
	}
	
	/**
	 * Extends the taint propagation path with the given abstraction
	 * @param abs The abstraction to put on the taint propagation path
	 * @return The new taint propagation path If this path would contain a
	 * loop, null is returned instead of the looping path.
	 */
	public SourceContextAndPath extendPath(Abstraction abs) {
		return extendPath(abs, true);
	}
	
	/**
	 * Extends the taint propagation path with the given abstraction
	 * @param abs The abstraction to put on the taint propagation path
	 * @param trackPath True if the abstraction shall be put on the propagation
	 * path even if does not change the call stack. This is for instance useful
	 * if all statements involved in the taint propagation shall later be
	 * reported.
	 * @return The new taint propagation path. If this path would contain a
	 * loop, null is returned instead of the looping path.
	 */
	public SourceContextAndPath extendPath(Abstraction abs, boolean trackPath) {
		if (abs == null)
			return this;
		
		// If we have no data at all, there is nothing we can do here
		if (abs.getCurrentStmt() == null && abs.getCorrespondingCallSite() == null)
			return this;
		
		// If we don't track paths and have nothing to put on the stack, there
		// is no need to create a new object
		if (abs.getCorrespondingCallSite() == null && !trackPath)
			return this;
		
		SourceContextAndPath scap = null;
		if (trackPath && abs.getCurrentStmt() != null) {
			// Do not add the very same abstraction over and over again. We clone
			// the data object first to ensure a stable state.
			scap = this.clone();
			if (this.path != null) {
				for (Abstraction a : scap.path) {
					if (a == abs)
						return null;
					
					// Do not run into loops. If we come back to the same
					// abstraction, we don't got on with a neighbor
					if (a.getNeighbors() != null && a.getNeighbors().contains(abs))
						return null;
					
					// If this is exactly the same abstraction as one we have seen
					// before, we skip it. Otherwise, we would run through loops
					// infinitely.
					if (a.equals(abs)
							&& a.getCurrentStmt() == abs.getCurrentStmt()
							&& a.getCorrespondingCallSite() == abs.getCorrespondingCallSite())
						return null;
				}
				
				// We cannot leave the same method at two different sites
				Abstraction topAbs = scap.path.get(0);
				if (topAbs.equals(abs)
						&& topAbs.getCorrespondingCallSite() != null
						&& topAbs.getCorrespondingCallSite() == abs.getCorrespondingCallSite()
						&& topAbs.getCurrentStmt() != abs.getCurrentStmt())
					return null;
			}
			
			// Extend the propagation path
			if (scap.path == null)
				scap.path = new ArrayList<Abstraction>();
			scap.path.add(0, abs);
		}
		
		// Extend the call stack
		if (abs.getCorrespondingCallSite() != null
				&& abs.getCorrespondingCallSite() != abs.getCurrentStmt()) {
			if (scap == null)
				scap = this.clone();
			if (scap.callStack == null)
				scap.callStack = new ArrayList<Stmt>();
			scap.callStack.add(0, abs.getCorrespondingCallSite());
		}
		
		this.neighborCounter = abs.getNeighbors() == null ? 0 : abs.getNeighbors().size();
		return scap == null ? this : scap;
	}
	
	/**
	 * Pops the top item off the call stack.
	 * @return The new {@link SourceContextAndPath} object as the first element
	 * of the pair and the call stack item that was popped off as the second
	 * element. If there is no call stack, null is returned.
	 */
	public Pair<SourceContextAndPath, Stmt> popTopCallStackItem() {
		if (callStack == null || callStack.isEmpty())
			return null;
		
		SourceContextAndPath scap = clone();
		return new Pair<>(scap, scap.callStack.remove(0));
	}
	
	/**
	 * Gets whether the current call stack is empty, i.e., the path is in the
	 * method from which it originated
	 * @return True if the call stack is empty, otherwise false
	 */
	public boolean isCallStackEmpty() {
		return this.callStack == null || this.callStack.isEmpty();
	}
	
	public void setNeighborCounter(int counter) {
		this.neighborCounter = counter;
	}
	
	public int getNeighborCounter() {
		return this.neighborCounter;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (other == null || getClass() != other.getClass())
			return false;
		SourceContextAndPath scap = (SourceContextAndPath) other;
		
		if (this.hashCode != 0 && scap.hashCode != 0 && this.hashCode != scap.hashCode)
			return false;
		
		if (this.callStack == null) {
			if (scap.callStack != null)
				return false;
		}
		else if (!this.callStack.equals(scap.callStack))
			return false;
			
		if (!InfoflowConfiguration.getPathAgnosticResults()) {	
			if (!this.path.equals(scap.path))
				return false;
		}
		
		return super.equals(other);
	}
	
	@Override
	public int hashCode() {
		if (hashCode != 0)
			return hashCode;
		
		synchronized(this) {
			hashCode = (!InfoflowConfiguration.getPathAgnosticResults() ? 31 * (path == null ? 0 : path.hashCode()) : 0)
					+ 31 * (callStack == null ? 0 : callStack.hashCode())
					+ 31 * super.hashCode();
		}
		return hashCode;
	}
	
	@Override
	public synchronized SourceContextAndPath clone() {
		final SourceContextAndPath scap = new SourceContextAndPath(getAccessPath(), getStmt(), getUserData());
		if (path != null)
			scap.path = new ArrayList<Abstraction>(this.path);
		if (callStack != null)
			scap.callStack = new ArrayList<Stmt>(callStack);
		return scap;
	}
	
	@Override
	public String toString() {
		return super.toString() + "\n\ton Path: " + path;
	}	
}
