package Extractocol.BufferExtractor_Response.Basics;


public class BFNode_Response implements Cloneable
{
	// 1. Variable type
	//   * All BFNode have same variable type.
	public static enum VAR_TYPE {VAR_TYPE_NONE, VAR_TYPE_PHINODE, VAR_TYPE_STRING, VAR_TYPE_JSON, VAR_TYPE_XML};
	
	// 2. JSON Node TYPE 
	//  - JSON_TYPE_FINAL: This node is JSON node, but, it includes a final signature. 
	//  * Only for BFNode belonging to JSON type variable
	public static enum JSON_TYPE {JSON_TYPE_NONE, JSON_TYPE_BOOLEAN, JSON_TYPE_INT_LONG, JSON_TYPE_STRING, JSON_TYPE_JSONOBJECT, JSON_TYPE_JSONTREE, JSON_TYPE_FINAL};

	// 3. Direct/Indirect
	//   - It presents whether the value in BFNode can be used directly or not.
	public static enum VALUE_TYPE {VALUE_TYPE_NONE, VALUE_TYPE_DIRECT, VALUE_TYPE_INDIRECT};
	
	// variable type
	public VAR_TYPE type;
	
	// value type ('direct' means the value is string. 'indirect' means the value is variable) 
	public VALUE_TYPE value_type;
	
	// string type
	public String stringValue;

	// json type
	public String jsonKey;
	public String jsonValue;
	public JSON_TYPE jsonVtype;
	
	public String treeLeft;
	public String treeMarker;
	public String treeRight;

	// for phinode
//	public String phinodeVar;

	public BFNode_Response()
	{
		type = VAR_TYPE.VAR_TYPE_NONE;
		value_type = VALUE_TYPE.VALUE_TYPE_NONE;
		stringValue = null;
		jsonKey = null;
		jsonValue = null;
		jsonVtype = JSON_TYPE.JSON_TYPE_NONE;
	}
	
	public BFNode_Response(String s, VAR_TYPE var_t)
	{
		type = var_t;
		value_type = VALUE_TYPE.VALUE_TYPE_DIRECT;
		stringValue = s;
		jsonKey = null;
		jsonValue = null;
		jsonVtype = JSON_TYPE.JSON_TYPE_NONE;
	}
	
	public BFNode_Response(String s, VAR_TYPE var_t, JSON_TYPE json_t)
	{
		type = var_t;
		value_type = VALUE_TYPE.VALUE_TYPE_DIRECT;
		stringValue = s;
		jsonKey = null;
		jsonValue = null;
		jsonVtype = json_t;
	}
	
	public BFNode_Response(String s, VAR_TYPE var_t, VALUE_TYPE val_t, JSON_TYPE json_t)
	{
		type = var_t;
		value_type = val_t;
		stringValue = s;
		jsonKey = null;
		jsonValue = null;
		jsonVtype = json_t;
	}


	public void makePhinodeBfn(String _phiVar)
	{
		type = VAR_TYPE.VAR_TYPE_PHINODE;
		stringValue = _phiVar;
		value_type = VALUE_TYPE.VALUE_TYPE_INDIRECT;
	}
	
	// This BFNode includes a final JSON signature.
	public void makeJSONBfn(String _stringValue)
	{
		type = VAR_TYPE.VAR_TYPE_JSON;
		value_type = VALUE_TYPE.VALUE_TYPE_DIRECT;
		jsonVtype = JSON_TYPE.JSON_TYPE_FINAL;
		
		if (_stringValue != null && !_stringValue.isEmpty())
			stringValue = _stringValue.replaceAll("\"", "");
		else {
			stringValue = "ERROR_BFNode_RESPONSE";
		}
	}

	public void makeStringBfn(String _stringValue)
	{
		type = VAR_TYPE.VAR_TYPE_STRING;
		if (_stringValue != null && !_stringValue.isEmpty())
			stringValue = _stringValue.replaceAll("\"", "");
		else {
			stringValue = "ERROR_BFNode_RESPONSE";
		}
	}
	
	public void makeStringBfn_Direct(String _stringValue)
	{
		type = VAR_TYPE.VAR_TYPE_STRING;
		value_type = VALUE_TYPE.VALUE_TYPE_DIRECT;
		
		if (_stringValue != null && !_stringValue.isEmpty())
			stringValue = _stringValue.replaceAll("\"", "");
		else {
			stringValue = "ERROR_BFNode_RESPONSE";
		}
	}
	
	public void makeStringBfn_Indirect(String _stringValue)
	{
		type = VAR_TYPE.VAR_TYPE_STRING;
		value_type = VALUE_TYPE.VALUE_TYPE_INDIRECT;
		
		if (_stringValue != null && !_stringValue.isEmpty())
			stringValue = _stringValue.replaceAll("\"", "");
		else {
			stringValue = "ERROR_BFNode_RESPONSE";
		}
	}

	public void makeJsonBfn(String _key, String _value, JSON_TYPE _Vtype)
	{
		type = VAR_TYPE.VAR_TYPE_JSON;
		jsonKey = _key;
		jsonValue = _value;
		jsonVtype = _Vtype;
	}
	
	public void makeJsonBfn_Direct(String _key, String _value, JSON_TYPE _Vtype)
	{
		type = VAR_TYPE.VAR_TYPE_JSON;
		value_type = VALUE_TYPE.VALUE_TYPE_DIRECT;
		
		jsonKey = _key;
		jsonValue = _value;
		jsonVtype = _Vtype;
	}
	
	public void makeJsonBfn_Indirect(String _key, String _value, JSON_TYPE _Vtype)
	{
		type = VAR_TYPE.VAR_TYPE_JSON;
		value_type = VALUE_TYPE.VALUE_TYPE_INDIRECT;
		
		jsonKey = _key;
		jsonValue = _value;
		jsonVtype = _Vtype;
	}
	
	public void makeJsonTreeBfn(String _left, String _marker_variable, String _right)
	{
		type = VAR_TYPE.VAR_TYPE_JSON;
		value_type = VALUE_TYPE.VALUE_TYPE_INDIRECT;
		treeLeft = _left;
		treeMarker = _marker_variable;
		treeRight = _right;
		jsonVtype = JSON_TYPE.JSON_TYPE_JSONTREE;
	}


	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
