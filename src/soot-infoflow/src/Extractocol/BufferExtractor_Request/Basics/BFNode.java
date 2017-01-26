
package Extractocol.BufferExtractor_Request.Basics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import com.gaurav.tree.Tree;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapHandler;
import Extractocol.BufferExtractor_Request.Semantic.URL.UrlBuilder;
import soot.Unit;
import soot.Value;

// The key of this node is Variable name.
// Some variables which include a prefix (d-) are used to debug.

// array design -- woohyun 20160331
// assume array is a single bfn and single stringforurl value
// delimiter = ###
// Ex) A = newarray int [3]
// BFTtable.get(A).get(0).getstringForUrl = "### ### ###"
// EX) A[2] = "hi"
// BFTtable.get(A).get(0).getstringForUrl = "### ### ### hi"
// Ex) $r1[$i1]= $r2
// $r2 = "hi"
// We can assume anything -> $r2(hi) is in the array
// BFTtable.get($r1).get(0).getarraycase = "hi"

public class BFNode implements Cloneable
{

	// for url
	private String stringForUrl;

	private ArrayList<String> arrayorcase;

	// for response
	private String Key;

	private String Value;

	private String Vtype;

	private boolean isLoop;

	// for phinode
	private boolean isPhiNode;

	private String phinodeVar;
	
	// for JSON
	private boolean isStartJson;
	private boolean isEndJson;

	// for heap object
	private boolean isHeapObject;

	private String SootValue;

	private String ExtendedType;

	// for Orcae
	private boolean OR;

	// for array size
	private int size;

	// for delimiter
	public static final String delimiter = "###";
	
	//for mergebft
	public int blocknum;
	
	public BFNode()
	{
		Key = null;
		Value = null;
		Vtype = null;
		SootValue = null;
		setPhiNode(false);
		setPhinodeVar(null);
	}

	public boolean hasVtype()
	{
		return this.Vtype == null ? false : true;
	}

	public void makePhinodeBfn(String _phiVar)
	{
		setPhiNode(true);
		setPhinodeVar(_phiVar);
	}

	public void makeUrlBfn(String _stringForUrl)
	{
		//System.out.println("BFNode");
		//System.out.println(_stringForUrl);
		setPhiNode(false);
		if (_stringForUrl != null && !_stringForUrl.isEmpty()){
			//Duong
			/*stringForUrl = _stringForUrl.replaceAll("\"", "");
			stringForUrl = stringForUrl.replace("\\", "");*/
			stringForUrl = _stringForUrl.replace("\\", "");
			if (_stringForUrl.charAt(0) == '"' && _stringForUrl.charAt(_stringForUrl.length()-1) == '"'){
				stringForUrl = stringForUrl.replaceFirst("\"", "");
				stringForUrl = replaceLast(stringForUrl, "\"", "");
			}
		}
		else
			stringForUrl = ".*";
		//System.out.println(stringForUrl);
	}

	public void makeUrlParamsBfn(ArrayList<BFNode> params)
	{
		setPhiNode(false);
		int idx = 0;
		for (BFNode bfn : params)
		{
			if (bfn.getKey() != null && !bfn.getKey().equals("(.*)"))
			{
				stringForUrl += (idx++ == 0 ? "?" : "&") + bfn.getKey().replaceAll("\"", "") + "=" + bfn.getValue().replaceAll("\"", "");
			}
		}
	}

	public void makeUrlBfn(Set<String> _stringForUrl)
	{
		setPhiNode(false);
		stringForUrl = "";
		for (String a : _stringForUrl)
		{
			if (a != null || !a.equals("null"))
				stringForUrl += "(" + a.replaceAll("\"", "") + ")|";

			if (stringForUrl.length() > 2000)
			{
				stringForUrl = "(.*)";
				break;
			}
		}

		if (stringForUrl.endsWith("|"))
			stringForUrl = stringForUrl.substring(0, stringForUrl.length() - 1);
	}

	public void makeResponseBfn(String _key, String _value, String _Vtype)
	{
		setPhiNode(false);
		Key = _key;
		Value = _value;
		Vtype = _Vtype;
	}

	public void makeHeapObjectBfn(String _SootValue)
	{
		setPhiNode(false);
		setHeapObject(true);
		SootValue = _SootValue;
	}

	public void makeOrbfn()
	{
		setOR(true);
	}

	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	/* Getter & Setter */
	public String getKey()
	{
		return Key;
	}

	public void setKey(String key)
	{
		Key = key;
	}

	public String getValue()
	{
		return Value;
	}

	public void setValue(String value)
	{
		Value = value;
	}

	public String getVtype()
	{
		return Vtype;
	}

	public void setVtype(String vtype)
	{
		Vtype = vtype;
	}

	public String getSootValue()
	{
		return SootValue;
	}

	public void setSootValue(String sootValue)
	{
		SootValue = sootValue;
	}

	public String getStringForUrl()
	{
		return stringForUrl;
	}

	public void setStringForUrl(String stringForUrl)
	{
		this.stringForUrl = stringForUrl;
	}

	public boolean isHeapObject()
	{
		return isHeapObject;
	}

	public void setHeapObject(boolean isHeapObject)
	{
		this.isHeapObject = isHeapObject;
	}

	public boolean isPhiNode()
	{
		return isPhiNode;
	}

	public void setPhiNode(boolean isPhiNode)
	{
		this.isPhiNode = isPhiNode;
	}

	public String getPhinodeVar()
	{
		return phinodeVar;
	}

	public void setPhinodeVar(String phinodeVar)
	{
		this.phinodeVar = phinodeVar;
	}
	
	public boolean isStartJson()
	{
		return isStartJson;
	}

	public void setStartJson(boolean isStart)
	{
		isStartJson = isStart;
	}
	
	public boolean isEndJson()
	{
		return isEndJson;
	}

	public void setEndJson(boolean isEnd)
	{
		isEndJson = isEnd;
	}

	public boolean isOR()
	{
		return OR;
	}

	public void setOR(boolean oR)
	{
		OR = oR;
	}

	public String bfnRegex()
	{
		String result = "";
		if (this.isPhiNode())
		{
			return result;
		}

		if (this.isHeapObject())
		{
			result += HeapHandler.getHeapObjectString(this.getSootValue());
		} else if (this.getStringForUrl() == null)
		{
			System.out.println("bfn url string is null - 20150914 hnamkoong");
			result += "(.*)";
		} else if (this.getStringForUrl() != null)
		{
			String urlString = this.getStringForUrl();
			if (!urlString.equals("\"\""))
			{
				result += UrlBuilder.alignHttpString(urlString).replaceAll("\"", "");

				// this is for debug purpose
				// result += "(" + alignHttpString(urlString).replaceAll("\"",
				// "") + ")";
			}
		}

		return result;

	}

	static public boolean isArray(BFNode bfn)
	{
		if (bfn.getVtype() != null)
		{
			if (bfn.getVtype().contains("[") || bfn.getVtype().contains("array"))
				return true;
		}

		if (bfn.getSootValue() != null)
		{
			if (bfn.getSootValue().contains("[") || bfn.getSootValue().contains("array"))
				return true;
		}

		return false;

	}

	public void setarray(int index, String string)
	{
		String original = this.stringForUrl;
		String returnstring = "";
		String[] array;

		if (original == null)
			return;

		if (!original.contains("###"))
		{
			this.initarray(index + 1);
			/*
			 * System.out.println(" ERROR : " + this.Key +
			 * " is not initialized or not an array");
			 */
			original = this.stringForUrl;
		}

		array = original.split("###");

		for (int i = 1; i <= array.length - 1; i++)
		{
			if (i == index + 1)
				returnstring += "###" + string;

			else
				returnstring += "###" + array[i];
		}

		this.stringForUrl = returnstring;

	}

	public void initarray(int index)
	{
		String returnstring = "";
		for (int i = 0; i < index; i++)
		{
			returnstring += "###(.*)";
		}
		this.stringForUrl = returnstring;
		this.setVtype("array");
		this.setSize(index);
		this.arrayorcase = new ArrayList<String>();
	}

	public void setrandomarray()
	{

		this.stringForUrl = ".*";
		this.setVtype("randomarray");
		this.setSize(-1);
		this.arrayorcase = new ArrayList<String>();
	}

	public String getarraystring(int index)
	{
		String original = this.stringForUrl;
		String[] array;
		if (!original.contains("###"))
			System.out.println(" ERROR : " + this.Key + " is not initialized or not an array");
		array = original.split("###");
		return array[index + 1];
	}

	// get a string that includes every possible string from array.
	// ex) r2.stringforurl###hi###bi###ci
	// ex) r2.arrayorcase=[di,ei]
	// re.getpossiblestring=[hi,bi,ci,di,ei]
	public String getpossiblestring()
	{
		ArrayList<String> returnarray = new ArrayList<String>();
		Set<String> stringset = new HashSet<String>();

		assert (this.getVtype().contains("array") || this.getVtype().contains("["));

		if (this.Vtype == null || this.getVtype().contains("random"))
			return ".*";

		// get unique stringforurl
		for (int i = 0; i < this.size; i++)
			stringset.add(this.getarraystring(i));
		Object[] array = (Object[]) stringset.toArray();

		// get arrayorcase
		if (this.arrayorcase != null)
			for (int i = 0; i < this.arrayorcase.size(); i++)
				returnarray.add(this.arrayorcase.get(i));
		else
		{
			this.setVtype("array");
			this.arrayorcase = new ArrayList<String>();
		}

		for (int i = 0; i < array.length; i++)
			returnarray.add(array[i].toString());

		String returnstring = "";

		for (int i = 0; i < returnarray.size(); i++)
			returnstring += returnarray.get(i) + "|";

		return returnstring.substring(0, returnstring.length() - 1);
	}

	public ArrayList<String> getpossiblestringarray()
	{
		ArrayList<String> returnarray = new ArrayList<String>();
		Set<String> stringset = new HashSet<String>();

		assert (this.getVtype().contains("array") || this.getVtype().contains("["));

		if (this.Vtype == null || this.getVtype().contains("random"))
		{
			returnarray.add(".*");
			return returnarray;
		}

		// get unique stringforurl
		for (int i = 0; i < this.size; i++)
			stringset.add(this.getarraystring(i));
		Object[] array = (Object[]) stringset.toArray();

		// get arrayorcase
		if (this.arrayorcase != null)
			for (int i = 0; i < this.arrayorcase.size(); i++)
				returnarray.add(this.arrayorcase.get(i));
		else
		{
			this.setVtype("array");
			this.arrayorcase = new ArrayList<String>();
		}

		for (int i = 0; i < array.length; i++)
			returnarray.add(array[i].toString());

		return returnarray;
	}

	public String[] bfnTOarray()
	{

		if (this.Vtype == null)
		{
			String[] returnarray = new String[1];
			returnarray[0] = "unkownarray";
			return returnarray;
		}

		if (!this.Vtype.contains("randomarray"))
		{
			String[] returnarray = new String[this.size];
			if (!BFNode.isArray(this))
			{
				System.out.println("ERROR " + this.Key + "is not a array");
				return null;
			} else
			{

				if(this.stringForUrl.equals("###"))
					this.stringForUrl="###(.*)";
				String[] stringforurl = this.stringForUrl.split("###");
				String orcase = "";
				if (this.arrayorcase != null)
					for (int i = 0; i < this.arrayorcase.size(); i++)
						orcase += "|" + this.arrayorcase.get(i);

				for (int i = 1; i <= this.size; i++)
					returnarray[i - 1] = stringforurl[i] + orcase;
			}
			return returnarray;
		} else
		{
			String[] returnarray = new String[1];
			returnarray[0] = "randomarray";
			return returnarray;

		}

	}

	public void addorcase(String orstring)
	{
		this.getArrayorcase().add(orstring);
	}

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public ArrayList<String> getArrayorcase()
	{
		return arrayorcase;
	}

	public void setArrayorcase(ArrayList<String> object)
	{
		this.arrayorcase = object;
	}

	public String getExtendedType()
	{
		return ExtendedType;
	}

	public void setExtendedType(String extendedType)
	{
		ExtendedType = extendedType;
	}

	public boolean isLoop()
	{
		return isLoop;
	}

	public void setLoop(boolean isLoop)
	{
		this.isLoop = isLoop;
	}
	
	String replaceLast(String string, String substring, String replacement)
	{
	  int index = string.lastIndexOf(substring);
	  if (index == -1)
	    return string;
	  return string.substring(0, index) + replacement
	          + string.substring(index+substring.length());
	}
}
