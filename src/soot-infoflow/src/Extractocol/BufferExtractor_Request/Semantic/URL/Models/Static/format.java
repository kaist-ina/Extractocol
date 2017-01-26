package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Static;

import java.util.ArrayList;

import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;
import soot.jimple.Constant;
import soot.jimple.StringConstant;

public class format extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.sie.getMethodRef().toString()
				.equals("<org.apache.http.client.utils.URLEncodedUtils: java.lang.String format(java.util.List,java.lang.String)>"))
		{
			ArrayList<BFNode> tr = spb.BFTtable.get(spb.ub.strDest);

			tr.clear();

			String r = "";

			for (BFNode bfn : spb.BFTtable.get(spb.sie.getArg(0).toString()))
			{
				if (!bfn.getStringForUrl().equals(""))
					r += bfn.getStringForUrl() + "=(.*)&";
			}

			if (r.length() > 0)
			{
				BFNode bfn = new BFNode();
				bfn.setVtype("URLEncode");
				bfn.setStringForUrl(r.substring(0, r.length() - 1));
				tr.add(bfn);
			}
			spb.BFTtable.put(spb.ub.strDest, tr);
		} 
		else if (spb.sie.getMethodRef().getSignature().equals("<java.lang.String: java.lang.String format(java.util.Locale,java.lang.String,java.lang.Object[])>"))
		{
			if (spb.ub.strDest != null)
			{
				if (spb.sie.getArgCount() == 3)
				{
					String string = "";
					// arg0 is not constant
					if (!(spb.sie.getArg(1) instanceof StringConstant) || !(spb.sie.getArg(1) instanceof Constant))
					{
						System.out.println("not implemented for this case");
					} else
					{
						string = spb.sie.getArg(1).toString();
						string = string.replaceAll("%d|%s", "(.*)");
					}

					ArrayList<BFNode> list = new ArrayList<BFNode>();
					BFNode bfn = new BFNode();
					bfn.makeUrlBfn(string);
					list.add(bfn);
					spb.BFTtable.put(spb.ub.strDest, list);
				}
			}
		}
		else if (spb.sie.getMethodRef().toString().equals("<java.lang.String: java.lang.String format(java.lang.String,java.lang.Object[])>"))
		{
			if (spb.ub.strDest != null)
			{
				if (spb.sie.getArgCount() == 2)
				{
					String string = "";
					String[] array = null;
					// arg0 is not constant
					if (!(spb.sie.getArg(0) instanceof Constant))
					{
						String arg0 = spb.sie.getArg(0).toString();
						string = spb.ub.GenRegex(null, spb.BFTtable, arg0);
					} else
					{
						string = spb.sie.getArg(0).toString();
					}
					// arg1 is not constant
					if (!(spb.sie.getArg(1) instanceof Constant))
					{
						String arg1 = spb.sie.getArg(1).toString();
						array = spb.BFTtable.get(arg1).get(0).bfnTOarray();
						if (array.length == 0)
						{
							String[] newarray =
							{ "(.*)" };
							array = newarray;
						}
					} else
					{
						System.out.println("ERROR woohyun did not consider this case");
						return;
					}

					ArrayList<BFNode> list = new ArrayList<BFNode>();
					BFNode bfn = new BFNode();

					// String undefined case
					if (!string.equals("(.*)"))
					{
						// generate format

						int randomnumber = 99884488;

						// if array size != string format
						// ex) string =
						// "(a=%s, b=%s, c=%s)|(a=%s, b=%s, c=%s)"
						// array = [a,b,c];
						// I choose first arraysize formatter. So I do
						// not
						// consider other orcase.
						// format(string, array) -> a=a, b=b, c=c
						// woohyun 20160418
						int numofformatter = 0;
						String original = string;

						for (int i = 0; i < string.length(); i++)
						{
							if (string.charAt(i) == '%')
								numofformatter++;
						}

						if (array.length < numofformatter)
						{
							int arraysize = array.length;
							int endindex = string.length();

							for (int i = 0; i < string.length(); i++)
							{

								if (string.charAt(i) == '%')
									arraysize--;

								if (arraysize <= 0 && string.charAt(i) == '|')
								{
									endindex = i;
									break;
								}
							}

							string = string.substring(0, endindex);
							ArrayList<Integer> leftbracket = new ArrayList<Integer>();

							for (int i = 0; i < string.length(); i++)
							{
								if (string.charAt(i) == '(')
									leftbracket.add(i);
								if (string.charAt(i) == ')')
									leftbracket.remove(leftbracket.size() - 1);
							}
							int bracketsize = leftbracket.size();
							for (int i = 0; i < bracketsize; i++)
							{
								int lastone = leftbracket.get(leftbracket.size() - 1);
								leftbracket.remove(leftbracket.size() - 1);
								string = string.substring(0, lastone) + string.substring(lastone + 1);
							}

						}

						ArrayList<Character> formater = new ArrayList<Character>();

						for (int i = 0; i < string.length(); i++)
						{
							if (string.charAt(i) == '%')
								formater.add(string.charAt(i + 1));
						}

						assert (array.length == formater.size());
						int i = 0;
						Object[] realarray;

						// random array case
						if (array[0] == "randomarray")
							realarray = new Object[0];
						// normal case
						else
							realarray = new Object[array.length];

						for (Character ch : formater)
						{
							// %d and integer
							if (ch.equals('d') && isStringDouble(array[i]))
							{
								realarray[i] = Integer.parseInt(array[i]);
								i++;
							}
							// %d and not int
							else if (ch.equals('d') && !isStringDouble(array[i]))
							{
								realarray[i] = randomnumber;
								i++;
							} else if (array.length <= i)
								continue;
							// null -> .*
							else if (array[i].equals("null"))
							{
								realarray[i] = array[i] = ".*";
								i++;
							} else
							{
								realarray[i] = array[i];
								i++;
							}

						}

						if (realarray.length > 0)
							bfn.makeUrlBfn(String.format(string, realarray));
						else
							bfn.makeUrlBfn(original);
						if (bfn.getStringForUrl().contains(String.valueOf(randomnumber)))
							bfn.setStringForUrl(bfn.getStringForUrl().replace(String.valueOf(randomnumber), "[0-9]*"));
					} else
						bfn.makeUrlBfn(spb.BFTtable.get(spb.sie.getArg(1).toString()).get(0).getpossiblestring());

					list.add(bfn);
					spb.BFTtable.put(spb.ub.strDest, list);

				}
			}
		}
	}
	
	private boolean isStringDouble(String s)
	{
		try
		{
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException e)
		{
			return false;
		}
	}
}
