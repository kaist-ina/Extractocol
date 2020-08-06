package extractocol.common.valueEntry;

import java.io.Serializable;

/** ValueEntry
 * 
 * 
 * 2. How to add new type of valueEntry?
 * 
 * @author Brad
 *
 */
@SuppressWarnings("serial")
public abstract class ValueEntry implements Serializable {
	public static enum SOURCE_TYPE {JSONKEY, 
									CONSTANT, 
									HEAP, 
									TYPE, 
									REFERENCE, 
									MAP,
									LIST,
									ARRAY,
									STRBUILDER,
									VOLLEY,
									NULL,
									COLLECTIONTYPE,
									CLASS};
	
	
	/** Get type of this entry **/
	public abstract SOURCE_TYPE getSourceType();
	
	/** Clear this entry instance **/
	public abstract void Clear();
	
	public abstract String GenRegex();
	
	/** Clone this entry instance **/
	public abstract ValueEntry Clone();
	
	public abstract void addEntry(ValueEntry ve);
}
