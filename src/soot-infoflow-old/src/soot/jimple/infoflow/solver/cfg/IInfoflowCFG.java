/**
 * (c) 2013 Tata Consultancy Services & Ecole Polytechnique de Montreal
 * All rights reserved
 */
package soot.jimple.infoflow.solver.cfg;

import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;

public interface IInfoflowCFG extends BiDiInterproceduralCFG<Unit,SootMethod> {

    /**
     * Abstraction of a postdominator. This is normally a unit. In cases in which
     * a statement does not have a postdominator, we record the statement's
     * containing method and say that the postdominator is reached when the method
     * is left. This class MUST be immutable.
     *
     * @author Steven Arzt
     */
    public static final class UnitContainer {

        private final Unit unit;
        private final SootMethod method;

        public UnitContainer(Unit u) {
            unit = u;
            method = null;
        }

        public UnitContainer(SootMethod sm) {
            unit = null;
            method = sm;
        }

        @Override
        public int hashCode() {
            return 31 * (unit == null ? 0 : unit.hashCode())
                    + 31 * (method == null ? 0 : method.hashCode());
        }

        @Override
        public boolean equals(Object other) {
            if (other == null || !(other instanceof UnitContainer))
                return false;
            UnitContainer container = (UnitContainer) other;
            if (this.unit == null) {
                if (container.unit != null)
                    return false;
            }
            else
            if (!this.unit.equals(container.unit))
                return false;
            if (this.method == null) {
                if (container.method != null)
                    return false;
            }
            else
            if (!this.method.equals(container.method))
                return false;

            assert this.hashCode() == container.hashCode();
            return true;
        }

        public Unit getUnit() {
            return unit;
        }

        public SootMethod getMethod() {
            return method;
        }

        @Override
        public String toString() {
            if (method != null)
                return "(Method) " + method.toString();
            else
                return "(Unit) " + unit.toString();
        }

    }


    /**
     * Gets the postdominator of the given unit. If this unit is a conditional,
     * the postdominator is the join point behind both branches of the conditional.
     * @param u The unit for which to get the postdominator.
     * @return The postdominator of the given unit
     */
    public UnitContainer getPostdominatorOf(Unit u);
    
    /**
     * Checks whether the given static field is read inside the given method or
     * one of its transitive callees.
     * @param method The method to check
     * @param variable The static field to check
     * @return True if the given static field is read inside the given method,
     * otherwise false
     */
    public boolean isStaticFieldRead(SootMethod method, SootField variable);
    
    /**
     * Checks whether the given static field is used (read or written) inside the
     * given method or one of its transitive callees.
     * @param method The method to check
     * @param variable The static field to check
     * @return True if the given static field is used inside the given method,
     * otherwise false
     */
    public boolean isStaticFieldUsed(SootMethod method, SootField variable);
    
    /**
     * Checks whether the given method or any of its transitive callees has side
     * effects
     * @param method The method to check
     * @return True if the given method or one of its transitive callees has
     * side effects, otherwise false
     */
    public boolean hasSideEffects(SootMethod method);
    
    /**
     * Re-initializes the mapping betwween statements and owning methods after a
     * method has changed.
     * @param m The method for which to re-initialize the mapping
     */
	public void notifyMethodChanged(SootMethod m);
	
	/**
	 * Checks whether the given method reads the given value
	 * @param m The method to check
	 * @param v The value to check
	 * @return True if the given method reads the given value, otherwise false
	 */
	public boolean methodReadsValue(SootMethod m, Value v);
	
}
