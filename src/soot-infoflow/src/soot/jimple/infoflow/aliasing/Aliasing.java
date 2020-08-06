package soot.jimple.infoflow.aliasing;

import heros.solver.IDESolver;

import java.util.Collection;
import java.util.Set;

import soot.ArrayType;
import soot.Local;
import soot.PrimType;
import soot.RefLikeType;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.Constant;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.data.AccessPath;
import soot.jimple.infoflow.data.AccessPathFactory;
import soot.jimple.infoflow.data.AccessPathFactory.BasePair;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.util.TypeUtils;
import soot.jimple.toolkits.pointer.LocalMustAliasAnalysis;
import soot.jimple.toolkits.pointer.StrongLocalMustAliasAnalysis;
import soot.toolkits.graph.UnitGraph;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Helper class for aliasing operations
 * 
 * @author Steven Arzt
 */
public class Aliasing {
	
	private final IAliasingStrategy aliasingStrategy;
	private final IAliasingStrategy implicitFlowAliasingStrategy;
	private final IInfoflowCFG cfg;
	
	protected final LoadingCache<SootMethod,LocalMustAliasAnalysis> strongAliasAnalysis =
			IDESolver.DEFAULT_CACHE_BUILDER.build( new CacheLoader<SootMethod,LocalMustAliasAnalysis>() {
				@Override
				public LocalMustAliasAnalysis load(SootMethod method) throws Exception {
					return new StrongLocalMustAliasAnalysis
							((UnitGraph) cfg.getOrCreateUnitGraph(method));
				}
			});
	
	public Aliasing(IAliasingStrategy aliasingStrategy, IInfoflowCFG cfg) {
		this.aliasingStrategy = aliasingStrategy;
		this.implicitFlowAliasingStrategy = new ImplicitFlowAliasStrategy(cfg);
		this.cfg = cfg;
	}
	
	/**
	 * Computes the taints for the aliases of a given tainted variable
	 * @param d1 The context in which the variable has been tainted
	 * @param src The statement that tainted the variable
	 * @param targetValue The target value which has been tainted
	 * @param taintSet The set to which all generated alias taints shall be
	 * added
	 * @param method The method containing src
	 * @param newAbs The newly generated abstraction for the variable taint
	 * @return The set of immediately available alias abstractions. If no such
	 * abstractions exist, null is returned
	 */
	public void computeAliases
			(final Abstraction d1, final Stmt src,
			final Value targetValue, Set<Abstraction> taintSet,
			SootMethod method, Abstraction newAbs) {
		// We never ever handle primitives as they can never have aliases
		if (newAbs.getAccessPath().isStaticFieldRef()) {
			if (newAbs.getAccessPath().getFirstFieldType() instanceof PrimType)
				return;
		}
		else if (newAbs.getAccessPath().getBaseType() instanceof PrimType)
			return;
		
		// If we are not in a conditionally-called method, we run the
		// full alias analysis algorithm. Otherwise, we use a global
		// non-flow-sensitive approximation.
		if (!d1.getAccessPath().isEmpty()) {
			aliasingStrategy.computeAliasTaints(d1,
					src, targetValue, taintSet, method, newAbs);
		}
		else if (targetValue instanceof InstanceFieldRef) {
			implicitFlowAliasingStrategy.computeAliasTaints(d1, src,
					targetValue, taintSet, method, newAbs);
		}
	}
	
	/**
	 * Gets whether an access path can point to the same runtime object as another
	 * or to an object reachable through the other
	 * @param taintedAP The access path that is tainted
	 * @param referencedAP The access path that is accessed
	 * @return The access path that actually matched if the access paths alias.
	 * In the simplest case, this is the given tainted access path.
	 * When using recursive access paths, it can however also be a base
	 * expansion. If the given access paths do not alias, null is returned.
	 */
	public AccessPath mayAlias(AccessPath taintedAP, AccessPath referencedAP) {
		// Check whether the access paths are directly equal
		if (taintedAP.equals(referencedAP))
			return taintedAP;
		
		// Ask an interactive aliasing strategy if we have one
		// TODO
		/*
		if (aliasingStrategy.isInteractive())
			return aliasingStrategy.mayAlias(taintedAP, referencedAP);
		*/
		
		if (taintedAP.isInstanceFieldRef() || taintedAP.isLocal()) {
			// For instance field references, the base must match
			if (taintedAP.getPlainValue() != referencedAP.getPlainValue())
				return null;
			
			// Shortcut: If we have no fields and the base matches, we're done
			if (referencedAP.getFieldCount() == 0)
				return taintedAP;
			
			// If the referenced AP is not an instance field reference, we're done
			if (!referencedAP.isInstanceFieldRef())
				return null;
		}
		
		// If one reference is static, the other one must be static as well
		if (taintedAP.isStaticFieldRef())
			if (!referencedAP.isStaticFieldRef())
				return null;
		
		// Match the bases
		return getReferencedAPBase(taintedAP, referencedAP.getFields());
	}
	
	/**
	 * Matches the given access path against the given array of fields
	 * @param taintedAP The tainted access paths
	 * @param referencedFields The array of referenced access paths
	 * @return The actually matched access path if a matching was possible,
	 * otherwise null
	 */
	private AccessPath getReferencedAPBase(AccessPath taintedAP,
			SootField[] referencedFields) {
		final Collection<BasePair> bases = taintedAP.isStaticFieldRef()
				? AccessPathFactory.v().getBaseForType(taintedAP.getFirstFieldType())
						: AccessPathFactory.v().getBaseForType(taintedAP.getBaseType());
		
		int fieldIdx = 0;
		while (fieldIdx < referencedFields.length) {
			// If we reference a.b.c, this only matches a.b.*, but not a.b
			if (fieldIdx >= taintedAP.getFieldCount()) {
				if (taintedAP.getTaintSubFields())
					return taintedAP;
				else
					return null;
			}
			
			// a.b does not match a.c
			if (taintedAP.getFields()[fieldIdx] != referencedFields[fieldIdx]) {
				// If the referenced field is a base, we add it in. Note that
				// the first field in a static reference is the base, so this
				// must be excluded from base matching.
				if (bases != null && !(taintedAP.isStaticFieldRef() && fieldIdx == 0)) {
					// Check the base. Handles A.y (taint) ~ A.[x].y (ref)
					for (BasePair base : bases) {
						if (base.getFields()[0] == referencedFields[fieldIdx]) {
							// Build the access path against which we have
							// actually matched
							SootField[] cutFields = new SootField
									[taintedAP.getFieldCount() + base.getFields().length];
							Type[] cutFieldTypes = new Type[cutFields.length];
							
							System.arraycopy(taintedAP.getFields(), 0, cutFields, 0, fieldIdx);
							System.arraycopy(base.getFields(), 0, cutFields, fieldIdx, base.getFields().length);
							System.arraycopy(taintedAP.getFields(), fieldIdx, cutFields,
									fieldIdx + base.getFields().length, taintedAP.getFieldCount() - fieldIdx);
							
							System.arraycopy(taintedAP.getFieldTypes(), 0, cutFieldTypes, 0, fieldIdx);
							System.arraycopy(base.getTypes(), 0, cutFieldTypes, fieldIdx, base.getTypes().length);
							System.arraycopy(taintedAP.getFieldTypes(), fieldIdx, cutFieldTypes,
									fieldIdx + base.getTypes().length, taintedAP.getFieldCount() - fieldIdx);

							return AccessPathFactory.v().createAccessPath(taintedAP.getPlainValue(),
									cutFields, taintedAP.getBaseType(), cutFieldTypes,
									taintedAP.getTaintSubFields(), false, false, taintedAP.getArrayTaintType());
						}
					}
					
				}
				return null;
			}
			
			fieldIdx++;
		}
		
		return taintedAP;
	}

	/**
	 * Gets whether two values may potentially point to the same runtime object
	 * @param val1 The first value
	 * @param val2 The second value
	 * @return True if the two values may potentially point to the same runtime
	 * object, otherwise false
	 */
	public boolean mayAlias(Value val1, Value val2) {
		// What cannot be represented in an access path cannot alias
		if (!AccessPath.canContainValue(val1) || !AccessPath.canContainValue(val2))
			return false;
		
		// Constants can never alias
		if (val1 instanceof Constant || val2 instanceof Constant)
			return false;
		
		// If the two values are equal, they alias by definition
		if (val1 == val2)
			return true;
		
		// If we have an interactive aliasing algorithm, we check that as well
		if (aliasingStrategy.isInteractive())
			return aliasingStrategy.mayAlias(
					AccessPathFactory.v().createAccessPath(val1, false),
					AccessPathFactory.v().createAccessPath(val2, false));
		
		return false;		
	}
	
	/**
	 * Gets whether a value and an access path may potentially point to the same
	 * runtime object
	 * @param ap The access path
	 * @param val The value
	 * @return The access path that actually matched if the given value and
	 * access path alias. In the simplest case, this is the given access path.
	 * When using recursive access paths, it can however also be a base
	 * expansion. If the given access path and value do not alias, null is
	 * returned.
	 */
	public AccessPath mayAlias(AccessPath ap, Value val) {
		// What cannot be represented in an access path cannot alias
		if (!AccessPath.canContainValue(val))
			return null;
		
		// Constants can never alias
		if (val instanceof Constant)
			return null;
		
		// For instance field references, the base must match
		if (val instanceof Local)
			if (ap.getPlainValue() != val)
				return null;
		
		// For array references, the base must match
		if (val instanceof ArrayRef)
			if (ap.getPlainValue() != ((ArrayRef) val).getBase())
				return null;
		
		// For instance field references, the base local must match
		if (val instanceof InstanceFieldRef) {
			if (!ap.isLocal() && !ap.isInstanceFieldRef())
				return null;
			if (((InstanceFieldRef) val).getBase() != ap.getPlainValue())
				return null;
		}
		
		// If the value is a static field reference, the access path must be
		// static as well
		if (val instanceof StaticFieldRef)
			if (!ap.isStaticFieldRef())
				return null;
						
		// If we have an interactive aliasing algorithm, we check that as well
		/*
		if (aliasingStrategy.isInteractive())
			return aliasingStrategy.mayAlias(new AccessPath(val1, false), new AccessPath(val2, false));
		*/
		
		// Get the field set from the value
		SootField[] fields = val instanceof FieldRef
				? new SootField[] { ((FieldRef) val).getField() } : new SootField[0];
		return getReferencedAPBase(ap, fields);
	}
	
	/**
	 * Gets whether the two fields must always point to the same runtime object
	 * @param field1 The first field
	 * @param field2 The second field
	 * @return True if the two fields must always point to the same runtime
	 * object, otherwise false
	 */
	public boolean mustAlias(SootField field1, SootField field2) {
		return field1 == field2;
	}

	/**
	 * Gets whether the two values must always point to the same runtime object
	 * @param field1 The first value
	 * @param field2 The second value
	 * @param position The statement at which to check for an aliasing
	 * relationship
	 * @return True if the two values must always point to the same runtime
	 * object, otherwise false
	 */
	public boolean mustAlias(Local val1, Local val2, Stmt position) {
		if (val1 == val2)
			return true;
		if (!(val1.getType() instanceof RefLikeType) || !(val2.getType() instanceof RefLikeType))
			return false;

		LocalMustAliasAnalysis lmaa = strongAliasAnalysis.getUnchecked(cfg.getMethodOf(position));
		return lmaa.mustAlias(val1, position, val2, position);
	}
	
	/**
	 * Checks whether the given newly created taint can have an alias at the
	 * given statement. Assume a statement a.x = source(). This will check
	 * whether tainting a.<?> can induce new aliases or not.
	 * @param val The value which gets tainted
	 * @param source The source from which the taints comes from
	 * @return True if the analysis must look for aliases for the newly
	 * constructed taint, otherwise false
	 */
	public static boolean canHaveAliases(Stmt stmt, Value val, Abstraction source) {
		if (stmt instanceof DefinitionStmt) {
			DefinitionStmt defStmt = (DefinitionStmt) stmt;
			// If the left side is overwritten completely, we do not need to
			// look for aliases. This also covers strings.
			if (defStmt.getLeftOp() instanceof Local
					&& defStmt.getLeftOp() == source.getAccessPath().getPlainValue())
				return false;
			
			// Arrays are heap objects
			if (val instanceof ArrayRef)
				return true;
			if (val instanceof FieldRef)
				return true;
		}
		
		// Primitive types or constants do not have aliases
		if (val.getType() instanceof PrimType)
			return false;
		if (val instanceof Constant)
			return false;
		
		// String cannot have aliases
		if (TypeUtils.isStringType(val.getType())
				&& !source.getAccessPath().getCanHaveImmutableAliases())
			return false;
		
		return val instanceof FieldRef
				|| (val instanceof Local && ((Local)val).getType() instanceof ArrayType);
	}
	
	/**
	 * Checks whether the given base value matches the base of the given
	 * taint abstraction
	 * @param baseValue The value to check
	 * @param source The taint abstraction to check
	 * @return True if the given value has the same base value as the given
	 * taint abstraction, otherwise false
	 */
	public static boolean baseMatches(final Value baseValue, Abstraction source) {
		if (baseValue instanceof Local) {
			if (baseValue.equals(source.getAccessPath().getPlainValue()))
				return true;
		}
		else if (baseValue instanceof InstanceFieldRef) {
			InstanceFieldRef ifr = (InstanceFieldRef) baseValue;
			if (ifr.getBase().equals(source.getAccessPath().getPlainValue())
					&& source.getAccessPath().firstFieldMatches(ifr.getField()))
				return true;
		}
		else if (baseValue instanceof StaticFieldRef) {
			StaticFieldRef sfr = (StaticFieldRef) baseValue;
			if (source.getAccessPath().firstFieldMatches(sfr.getField()))
				return true;
		}
		return false;
	}
	
	/**
	 * Checks whether the given base value matches the base of the given
	 * taint abstraction and ends there. So a will match a, but not a.x.
	 * Not that this function will still match a to a.*.
	 * @param baseValue The value to check
	 * @param source The taint abstraction to check
	 * @return True if the given value has the same base value as the given
	 * taint abstraction and no further elements, otherwise false
	 */
	public static boolean baseMatchesStrict(final Value baseValue, Abstraction source) {
		if (!baseMatches(baseValue, source))
			return false;
		
		if (baseValue instanceof Local)
			return source.getAccessPath().isLocal();
		else if (baseValue instanceof InstanceFieldRef || baseValue instanceof StaticFieldRef)
			return source.getAccessPath().getFieldCount() == 1;
		
		throw new RuntimeException("Unexpected left side");
	}

}
