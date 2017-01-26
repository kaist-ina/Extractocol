
package Extractocol.BufferExtractor_Request.Semantic.URL.Models.Normal;

import java.util.ArrayList;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.Basics.Arraysolver;
import Extractocol.BufferExtractor_Request.Basics.BFNode;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.BaseModel;
import Extractocol.BufferExtractor_Request.Semantic.URL.Models.SemanticParameterBucket;
import soot.jimple.Constant;

public class append extends BaseModel
{
	@Override
	public void applySemantic(SemanticParameterBucket spb)
	{
		if (spb.iie.getMethodRef().toString()
				.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.String)>")
				|| spb.iie.getMethodRef().toString()
						.equals("<java.lang.StringBuffer: java.lang.StringBuffer append(java.lang.String)>"))
		{
			String base = spb.iie.getBase().toString();
			ArrayList<BFNode> list = spb.BFTtable.get(base);
			if (spb.iie.getArg(0) instanceof Constant)
			{
				BFNode bfn = new BFNode();
				bfn.makeUrlBfn(spb.iie.getArg(0).toString());
				list.add(bfn);
			}
			else
			{
				String arg = spb.iie.getArg(0).toString();
				BFNode bfn = new BFNode();
				String r = spb.ub.GenRegex(null, spb.BFTtable, arg);
				bfn.makeUrlBfn(r);
				if (list == null)
					return;
				list.add(bfn);
			}
			if (spb.ub.strDest != null)
			{
				spb.BFTtable.put(spb.ub.strDest, spb.BFTtable.get(base));
			}
		}
		else
		{
			if (spb.iie.getMethodRef().toString()
					.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(int)>")
					|| spb.iie.getMethodRef().toString()
							.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(double)>"))
			{
				String base = spb.iie.getBase().toString();
				ArrayList<BFNode> list = spb.BFTtable.get(base);
				if (spb.iie.getArg(0) instanceof Constant)
				{
					BFNode bfn = new BFNode();
					bfn.makeUrlBfn(spb.iie.getArg(0).toString());
					list.add(bfn);
				}
				else
				{
					String arg = spb.iie.getArg(0).toString();
					BFNode bfn = new BFNode();
					String r = spb.ub.GenRegex(null, spb.BFTtable, arg);
					if (r.equals(""))
					{
						if (spb.iie.getMethodRef().toString()
								.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(int)>"))
							r = "[0-9]*";
						else
							r = "(.*)";
					}
					bfn.makeUrlBfn(r);
					list.add(bfn);
				}
				if (spb.ub.strDest != null)
				{
					spb.BFTtable.put(spb.ub.strDest, spb.BFTtable.get(base));
				}
			}
			else
			{
				if (spb.iie.getMethodRef().toString()
						.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(char)>")
						&& spb.iie.getArg(0) instanceof Constant)
				{
					ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());
					BFNode bfn = new BFNode();
					bfn.makeUrlBfn(String.valueOf(Character.toChars(Integer.parseInt(spb.iie.getArg(0).toString()))));
					list.add(bfn);
				}
				else
				{
					if (spb.iie.getMethodRef().toString()
							.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(char)>")
							&& !(spb.iie.getArg(0) instanceof Constant))
					{
						ArrayList<BFNode> list = spb.BFTtable.get(spb.iie.getBase().toString());
						String result = "";
						BFNode bfn = new BFNode();
						Arraysolver as = Arraysolver
								.notor(spb.ub.GenRegex(null, spb.BFTtable, spb.iie.getArg(0).toString()));
						ArrayList<String> asstring = as.getArraystring();
						int num = 0;
						if (asstring != null)
						{
							for (String str : asstring)
							{
								if(str == null)
									continue;
								
								num++;
								if (num > 1)
									result += "|";
								
								try{
									result += "("
											+ String.valueOf(Character.toChars(Integer.parseInt(str.replaceAll(" ", ""))))
											+ ")";
								}catch (Exception e){
									if(Constants.isDebug)
										System.out.println("ERROR at Semantic.URL.Models.Normal.append: str is not integer or negative value." + str);
								}
							}
							bfn.makeUrlBfn(result);
							list.add(bfn);
						}
					}
					else
					{
						if (spb.iie.getMethodRef().toString()
								.equals("<java.lang.StringBuilder: java.lang.StringBuilder append(java.lang.Object)>"))
						{
							// BFTtable.put(strDest, );
							ArrayList<BFNode> list = spb.BFTtable.get(spb.ub.strDest);
							if (spb.BFTtable.get(spb.iie.getArg(0).toString()) != null && list != null)
								list.addAll(spb.BFTtable.get(spb.iie.getArg(0).toString()));
						}
					}
				}
			}
		}
	}
}
