package soot.jimple.infoflow.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import soot.ArrayType;
import soot.Local;
import soot.RefType;
import soot.SootField;
import soot.Type;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.collect.ConcurrentHashSet;
import soot.jimple.infoflow.collect.MyConcurrentHashMap;
import soot.jimple.infoflow.data.AccessPath.ArrayTaintType;
import soot.jimple.infoflow.util.TypeUtils;

public class AccessPathFactory {
	
	private static AccessPathFactory instance = new AccessPathFactory();
	
	public static AccessPathFactory v() {
		return instance;
	}
	
	/**
	 * Specialized pair class for field bases
	 * 
	 * @author Steven Arzt
	 *
	 */
	public static class BasePair {
		
		private final SootField[] fields;
		private final Type[] types;
		private int hashCode = 0;
		
		private BasePair(SootField[] fields, Type[] types) {
			this.fields = fields;
			this.types = types;
			
			// Check whether this base makes sense
			if (fields == null || fields.length == 0)
				throw new RuntimeException("A base must contain at least one field");
		}
		
		public SootField[] getFields() {
			return this.fields;
		}
		
		public Type[] getTypes()  {
			return this.types;
		}
		
		@Override
		public int hashCode() {
			if (hashCode == 0) {			
				final int prime = 31;
				int result = 1;
				result = prime * result + Arrays.hashCode(fields);
				result = prime * result + Arrays.hashCode(types);
				hashCode = result;
			}
			return hashCode;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BasePair other = (BasePair) obj;
			if (!Arrays.equals(fields, other.fields))
				return false;
			if (!Arrays.equals(types, other.types))
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return Arrays.toString(fields);
		}
		
	}
	
	private MyConcurrentHashMap<Type, Set<BasePair>> baseRegister
			= new MyConcurrentHashMap<Type, Set<BasePair>>();
	
	public AccessPath createAccessPath(Value val, boolean taintSubFields){
		return createAccessPath(val, (SootField[]) null, null, (Type[]) null, taintSubFields,
				false, true, ArrayTaintType.ContentsAndLength);
	}
	
	public AccessPath createAccessPath(Value val, Type valType,
			boolean taintSubFields, ArrayTaintType arrayTaintType) {
		return createAccessPath(val, null, valType, null, taintSubFields,
				false, true, arrayTaintType);
	}
	
	public AccessPath createAccessPath(Value val, SootField[] appendingFields,
			boolean taintSubFields) {
		return createAccessPath(val, appendingFields, null, null, taintSubFields,
				false, true, ArrayTaintType.ContentsAndLength);
	}

	public AccessPath createAccessPath(Value val, SootField[] appendingFields, Type valType,
									   Type[] appendingFieldTypes, boolean taintSubFields,
									   boolean cutFirstField, boolean reduceBases,
									   ArrayTaintType arrayTaintType) {
		return createAccessPath(val, appendingFields, valType, appendingFieldTypes, taintSubFields,
				cutFirstField, reduceBases, arrayTaintType, false);
	}
	
	public AccessPath createAccessPath(Value val, SootField[] appendingFields, Type valType,
			Type[] appendingFieldTypes, boolean taintSubFields,
			boolean cutFirstField, boolean reduceBases,
			ArrayTaintType arrayTaintType, boolean canHaveImmutableAliases) {
		// Make sure that the base object is valid
		assert (val == null && appendingFields != null && appendingFields.length > 0)
		 	|| AccessPath.canContainValue(val);
		
		// Initialize the field type information if necessary
		if (appendingFields != null && appendingFieldTypes == null) {
			appendingFieldTypes = new Type[appendingFields.length];
			for (int i = 0; i < appendingFields.length; i++)
				appendingFieldTypes[i] = appendingFields[i].getType();
		}
		
		Local value;
		Type baseType;
		SootField[] fields;
		Type[] fieldTypes;
		boolean cutOffApproximation;
		
		// Get the base object, field and type
		if(val instanceof FieldRef) {
			FieldRef ref = (FieldRef) val;

			// Set the base value and type if we have one
			if (val instanceof InstanceFieldRef) {
				InstanceFieldRef iref = (InstanceFieldRef) val;
				value = (Local) iref.getBase();
				baseType = value.getType();
			}
			else {
				value = null;
				baseType = null;
			}
			
			// Handle the fields
			fields = new SootField[(appendingFields == null ? 0 : appendingFields.length) + 1];
			fields[0] = ref.getField();
			if (appendingFields != null)
				System.arraycopy(appendingFields, 0, fields, 1, appendingFields.length);
			
			fieldTypes = new Type[(appendingFieldTypes == null ? 0 : appendingFieldTypes.length) + 1];
			fieldTypes[0] = valType != null ? valType : fields[0].getType();
			if (appendingFieldTypes != null)
				System.arraycopy(appendingFieldTypes, 0, fieldTypes, 1, appendingFieldTypes.length);
		}
		else if (val instanceof ArrayRef) {
			ArrayRef ref = (ArrayRef) val;
			value = (Local) ref.getBase();
			baseType = valType == null ? value.getType() : valType;
			
			fields = appendingFields;
			fieldTypes = appendingFieldTypes;
		}
		else {
			value = (Local) val;
			baseType = valType == null ? (value == null ? null : value.getType()) : valType;
			
			fields = appendingFields;
			fieldTypes = appendingFieldTypes;
		}
		
		// If we don't want to track fields at all, we can cut the field
		// processing short
		if (InfoflowConfiguration.getAccessPathLength() == 0) {
			fields = null;
			fieldTypes = null;
		}
		
		// Cut the first field if requested
		if (cutFirstField && fields != null && fields.length > 0) {
			SootField[] newFields = new SootField[fields.length - 1];
			Type[] newTypes = new Type[newFields.length];
			System.arraycopy(fields, 1, newFields, 0, newFields.length);
			System.arraycopy(fieldTypes, 1, newTypes, 0, newTypes.length);
			fields = newFields.length > 0 ? newFields : null;
			fieldTypes = newTypes.length > 0 ? newTypes : null;
		}
		
		// Make sure that the actual types are always as precise as the declared
		// ones. If types become incompatible, we drop the whole access path.
		if (InfoflowConfiguration.getUseTypeTightening()) {
			if (value != null && value.getType() != baseType) {
				baseType = TypeUtils.getMorePreciseType(baseType, value.getType());
				if (baseType == null)
					return null;
				
				// If we have a more precise base type in the first field, we
				// take that
				if (fields != null && fields.length > 0  && !(baseType instanceof ArrayType))
					baseType = TypeUtils.getMorePreciseType(baseType,
							fields[0].getDeclaringClass().getType());
				if (baseType == null)
					return null;
			}
			if (fields != null)
				for (int i = 0; i < fields.length; i++) {
					fieldTypes[i] = TypeUtils.getMorePreciseType(fieldTypes[i], fields[i].getType());
					if (fieldTypes[i] == null)
						return null;
					
					// If we have a more precise base type in the next field, we
					// take that
					if (fields.length > i + 1 && !(fieldTypes[i] instanceof ArrayType))
						fieldTypes[i] = TypeUtils.getMorePreciseType(fieldTypes[i],
								fields[i + 1].getDeclaringClass().getType());
					if (fieldTypes[i] == null)
						return null;
				}
		}
		
		// Make sure that only heap objects may have fields
		assert value == null
				|| value.getType() instanceof RefType 
				|| (value.getType() instanceof ArrayType && (((ArrayType) value.getType()).getArrayElementType() instanceof ArrayType
						|| ((ArrayType) value.getType()).getArrayElementType() instanceof RefType))
				|| fields == null || fields.length == 0;
		
		// We can always merge a.inner.this$0.c to a.c. We do this first so that
		// we don't create recursive bases for stuff we don't need anyway.
		if (InfoflowConfiguration.getUseThisChainReduction() && reduceBases && fields != null) {
			for (int i = 0; i < fields.length; i++) {
				// Is this a reference to an outer class?
				if (fields[i].getName().startsWith("this$")) {
					// Get the name of the outer class
					String outerClassName = ((RefType) fields[i].getType()).getClassName();
					
					// Check the base object
					int startIdx = -1;
					if (value != null
							&& value.getType() instanceof RefType
							&& ((RefType) value.getType()).getClassName().equals(outerClassName)) {
						startIdx = 0;
					}
					else {
						// Scan forward to find the same reference
						for (int j = 0; j < i; j++)
							if (fields[j].getType() instanceof RefType
									&& ((RefType) fields[j].getType()).getClassName().equals(outerClassName)) {
								startIdx = j;
								break;
							}
					}
					
					if (startIdx >= 0) {
						SootField[] newFields = new SootField[fields.length - (i - startIdx) - 1];
						Type[] newFieldTypes = new Type[fieldTypes.length - (i - startIdx) - 1];
						
						System.arraycopy(fields, 0, newFields, 0, startIdx);
						System.arraycopy(fieldTypes, 0, newFieldTypes, 0, startIdx);
						
						System.arraycopy(fields, i + 1, newFields, startIdx, fields.length - i - 1);
						System.arraycopy(fieldTypes, i + 1, newFieldTypes, startIdx, fieldTypes.length - i - 1);
						
						fields = newFields;
						fieldTypes = newFieldTypes;
						break;
					}
				}
			}
		}
		
		// Check for recursive data structures. If a last field maps back to something we
		// already know, we build a repeatable component from it
		boolean recursiveCutOff = false;
		if (InfoflowConfiguration.getUseRecursiveAccessPaths() && reduceBases && fields != null) {
			// f0...fi references an object of type T
			// look for an extension f0...fi...fj that also references an object
			// of type T
			int ei = val instanceof StaticFieldRef ? 1 : 0;
			while (ei < fields.length) {
				final Type eiType = ei == 0 ? baseType : fieldTypes[ei - 1];
				int ej = ei;
				while (ej < fields.length) {
					if (fieldTypes[ej] == eiType || fields[ej].getType() == eiType) {
						// The types match, f0...fi...fj maps back to an object of the
						// same type as f0...fi. We must thus convert the access path
						// to f0...fi-1[...fj]fj+1
						SootField[] newFields = new SootField[fields.length - (ej - ei) - 1];
						Type[] newTypes = new Type[newFields.length];
						
						System.arraycopy(fields, 0, newFields, 0, ei);
						System.arraycopy(fieldTypes, 0, newTypes, 0, ei);
						
						if (fields.length > ej) {
							System.arraycopy(fields, ej + 1, newFields, ei, fields.length - ej - 1);
							System.arraycopy(fieldTypes, ej + 1, newTypes, ei, fieldTypes.length - ej - 1);
						}
						
						// Register the base
						SootField[] base = new SootField[ej - ei + 1];
						Type[] baseTypes = new Type[ej - ei + 1];
						System.arraycopy(fields, ei, base, 0, base.length);
						System.arraycopy(fieldTypes, ei, baseTypes, 0, base.length);
						registerBase(eiType, base, baseTypes);
						
						fields = newFields;
						fieldTypes = newTypes;
						recursiveCutOff = true;
					}
					else
						ej++;
				}
				ei++;
			}
		}
		
		// Cut the fields at the maximum access path length. If this happens,
		// we must always add a star
		if (fields != null) {
			int fieldNum = Math.min(InfoflowConfiguration.getAccessPathLength(), fields.length);
			if (fields.length > fieldNum) {
				taintSubFields = true;
				cutOffApproximation = true;
			}
			else {
				cutOffApproximation = false || recursiveCutOff;
			}
			
			if (fieldNum == 0) {
				fields = null;
				fieldTypes = null;
			}
			else {
				SootField[] newFields = new SootField[fieldNum];
				Type[] newFieldTypes = new Type[fieldNum];
				
				System.arraycopy(fields, 0, newFields, 0, fieldNum);
				System.arraycopy(fieldTypes, 0, newFieldTypes, 0, fieldNum);
				
				fields = newFields;
				fieldTypes = newFieldTypes;
			}
		}
		else {
			cutOffApproximation = false;
			fields = null;
			fieldTypes = null;
		}
		
		// Type checks
		assert value == null || !(!(baseType instanceof ArrayType)
				&& !TypeUtils.isObjectLikeType(baseType)
				&& value.getType() instanceof ArrayType);
		assert value == null || !(baseType instanceof ArrayType
				&& !(value.getType() instanceof ArrayType)
				&& !TypeUtils.isObjectLikeType(value.getType()))
					: "Type mismatch. Type was " + baseType + ", value was: " + (value == null ? null : value.getType());
		
		return new AccessPath(value, fields, baseType, fieldTypes, taintSubFields,
				cutOffApproximation, arrayTaintType, canHaveImmutableAliases);
	}

	private void registerBase(Type eiType, SootField[] base,
			Type[] baseTypes) {
		// Check whether we can further normalize the base
		assert base.length == baseTypes.length;
		for (int i = 0; i < base.length; i++)
			if (baseTypes[i] == eiType) {
				SootField[] newBase = new SootField[i + 1];
				Type[] newTypes = new Type[i + 1];
				
				System.arraycopy(base, 0, newBase, 0, i + 1);
				System.arraycopy(baseTypes, 0, newTypes, 0, i + 1);
				
				base = newBase;
				baseTypes = newTypes;
				break;
			}
		
		Set<BasePair> bases = baseRegister.putIfAbsentElseGet
				(eiType, new ConcurrentHashSet<BasePair>());
		bases.add(new BasePair(base, baseTypes));
	}
	
	public void clearBaseRegister() {
		baseRegister.clear();
	}
	
	public Collection<BasePair> getBaseForType(Type tp) {
		return baseRegister.get(tp);
	}

}
