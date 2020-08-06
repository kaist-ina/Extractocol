package extractocol.backend.request.basics;

import soot.Type;

public class BufferEntry implements Cloneable {
	
	public	String		arg1;
	public 	String		arg2;
	public	Type		arg1type;
	public	Type		arg2type;
	
	/* for JSONArray object */
	public	Type		srctype;	//the type of variable in the hash buffer.
	public	String		srcname;	//the name of variable in the hash buffer. 
	public	Type		dsttype;
	public	String		dstname;
	/* ******************** */
	
	public	String		argtype1;	// 1: int 2: string
	public	String		argtype2;	// 1: int 2: string
	public	int			betype;		// 1: const 2: const, object 3: object, const
	public	int			belevel=1;
	
	public int size(){
		if ( arg1 != "" && arg2 != "")
			return 2;
		else if ( arg1 != "" || arg2 != "" )
			return 1;
		else
			return 0;
	}
	
	public String toString()
	{
		return "arg1 : " + arg1 + " arg2 : " + arg2 + " argtype1 : " + argtype1 + " argtype2 : " + argtype2 + " betype : " + betype  +" dsttype : " + dsttype + " dstname : " + dstname;
	}

	public Object clone() throws CloneNotSupportedException 
	{
		BufferEntry a = (BufferEntry)super.clone();
		return a;
	}
}