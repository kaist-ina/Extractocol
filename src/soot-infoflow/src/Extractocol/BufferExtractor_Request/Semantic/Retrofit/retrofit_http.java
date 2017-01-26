
package Extractocol.BufferExtractor_Request.Semantic.Retrofit;

import java.awt.print.Printable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.security.auth.callback.Callback;
import javax.swing.JEditorPane;
import javax.xml.crypto.KeySelector.Purpose;

import Extractocol.Constants;
import soot.asm.backend.targets.Returns;
import soot.coffi.constant_element_value;

public class retrofit_http
{

	public class classtype
	{

		String name;

		boolean isList = false;

		boolean isArray = false;

		String Jsonresult = "";
	}

	public retrofit_http()
	{
		// TODO Auto-generated constructor stub
	}

	public String http_method = "";

	public String suburl = "";

	public String query = "?";

	public String method = "";

	public String origin = "";

	public String namespace = "";

	public classtype responseclass = new classtype();

	public classtype Callbackresponse = new classtype();

	public String header = "";

	public Map<Integer, String> paramset = new HashMap<Integer, String>();

	public ArrayList<String> paramarray = new ArrayList<String>();

	public ArrayList<String> querymap = new ArrayList<String>();

	public ArrayList<String> body = new ArrayList<String>();

	public ArrayList<String> part = new ArrayList<String>();

	public boolean isDead = true;

	public boolean isList = false;

	public String methodref = "";

	public String returnclass = "";

	public static boolean LOWER_CASE_WITH_UNDERSCORES;

	public static boolean isIgnore = false;

	public static String basepath;

	public static String beforeClassname = "";

	public static Map<String, String> Cache = new HashMap<String, String>();

	public static void LastparamChecker(retrofit_http rh, String param)
	{
		// last param
		if (param.contains("Callback") && !param.startsWith("Callback"))
		{
			String[] temp = param.split("Callback");
			rh.paramarray.add("Callback");
			rh.Callbackresponse.name = temp[1].split(" ")[0];

			rh.Callbackresponse.name = find_purecalss(rh.Callbackresponse, rh);

		} else
			return;

	}

	public static ArrayList<retrofit_http> retrofitparser(String input)
	{
		ArrayList<retrofit_http> result = new ArrayList<retrofit_http>();

		try
		{
			BufferedReader in;

			System.out.println("FILENAME :" + input);

			in = new BufferedReader(new FileReader(input));

			String s;
			boolean parsing_status = false;
			boolean http_status = false;
			retrofit_http rh = null;
			String namespace = "";
			int line = 0;
			ArrayList<String> importclasses = new ArrayList<String>();

			try
			{
				while ((s = in.readLine()) != null)
				{
					line++;
					s = s.trim();

					ArrayList<String> arraynamespace = new ArrayList<String>();
					arraynamespace.add(namespace);
					// 1 Step
					parsing_status = Setimport(rh, s, parsing_status, arraynamespace, line, importclasses);
					namespace = arraynamespace.get(0);

					// 2 step
					// Parsing_retrofit()
					if (parsing_status)
					{
						if (s.startsWith("{"))
							continue;
						else if (s.startsWith("@"))
						{
							s = s.substring(1);
							if (http_status == false)
							{
								if (s.substring(0, 6).contains("GET"))
								{
									rh = new retrofit_http();
									rh.http_method = "GET";
									rh.suburl = s.substring(s.indexOf("(") + 2, s.indexOf(")") - 1);
									http_status = true;

								} else if (s.substring(0, 6).contains("POST"))
								{
									rh = new retrofit_http();
									rh.http_method = "POST";
									rh.suburl = s.substring(s.indexOf("(") + 2, s.indexOf(")") - 1);
									http_status = true;

								} else if (s.substring(0, 6).contains("PUT"))
								{
									rh = new retrofit_http();
									rh.http_method = "PUT";
									rh.suburl = s.substring(s.indexOf("(") + 2, s.indexOf(")") - 1);
									http_status = true;

								} else if (s.substring(0, 6).contains("DELETE"))
								{
									rh = new retrofit_http();
									rh.http_method = "DELETE";
									rh.suburl = s.substring(s.indexOf("(") + 2, s.indexOf(")") - 1);
									http_status = true;

								} else if (s.substring(0, 6).contains("HEAD"))
								{
									rh = new retrofit_http();
									rh.suburl = s.substring(s.indexOf("(") + 2, s.indexOf(")") - 1);
									http_status = true;
									rh.http_method = "HEAD";

								} else
									continue;
								// System.err.println("Unvalid HTTP metohd, it is not GET, POST, PUT, DELETE, HEAD");
							}
						} else if (http_status && (s.substring(0, 6).contains("public") || s.substring(0, 7).contains("private")))
						{
							int splitindex = s.indexOf("(");
							String front = s.substring(0, splitindex);
							String back = s.substring(splitindex + 1);
							String[] frontarray = front.split(" ");

							if (frontarray[2].contains("<"))
								rh.returnclass = frontarray[2].split("<")[0];
							else
								rh.returnclass = frontarray[2];

							String responseclass = "";

							for (int i = 2; i < frontarray.length - 1; i++)
								responseclass += frontarray[i];

							// observable, list remove
							String pureclass = responseclass;
							rh.responseclass.name = responseclass;

							pureclass = find_purecalss(rh.responseclass, rh);

							String last2chars = "";
							pureclass = pureclass.trim();
							if (pureclass.length() > 2)
								last2chars = pureclass.substring(pureclass.length() - 2, pureclass.length());

							if (last2chars.equals("[]"))
							{
								rh.responseclass.isArray = true;
								pureclass = pureclass.substring(0, pureclass.length() - 2);
							}

							// response_findnamespace
							rh.responseclass.name = find_namespace(importclasses, pureclass.replaceAll("<", "").replaceAll(">", ""), responseclass);

							if (last2chars.length() > 2)
								last2chars = rh.returnclass.substring(rh.returnclass.length() - 2, rh.returnclass.length());
							boolean isReturnArray = false;
							if (last2chars.equals("[]"))
							{
								isReturnArray = true;
								rh.returnclass = rh.returnclass.substring(0, rh.returnclass.length() - 2);
							}

							// return_findnamespace
							rh.returnclass = find_namespace(importclasses, rh.returnclass, rh.returnclass);

							rh.returnclass = rh.returnclass.trim();
							if (!isReturnArray)
								rh.returnclass = rh.returnclass;
							else
								rh.returnclass = rh.returnclass + "[]";

							rh.method = frontarray[frontarray.length - 1];
							rh.origin = s;

							String[] backarray = back.split("@");

							int numofquery = 0;

							for (int i = 0; i < backarray.length; i++)
							{
								String queryORpath = backarray[i];
								if (queryORpath.length() > 7)
								{

									if (queryORpath.substring(0, 8).contains("QueryMap") || queryORpath.substring(0, 8).contains("FieldMap"))
									{
										String[] temparray = queryORpath.split(" ");
										String paramString = temparray[1] + " " + temparray[2];
										rh.querymap.add(paramString);
										rh.paramarray.add(paramString);
										String remaining = "";

										if (paramString.contains(","))
											remaining = queryORpath.replaceFirst(",", "");

										LastparamChecker(rh, queryORpath);

										// if (remaining.contains(",")
										// && remaining.indexOf(",") + 2 <
										// remaining
										// .length()) {
										// String newvalueString = remaining
										// .split(",")[1].split(" ")[1];
										// rh.paramarray.add(newvalueString);
										//
										// }

									} else if (queryORpath.substring(0, 8).contains("Body"))
									{
										String[] temparray = queryORpath.split(" ");
										String paramString = "";
										if (temparray[1].contains("Map"))
										{
											paramString = temparray[1] + " " + temparray[2];
										} else
										{
											paramString = temparray[1];
										}
										rh.body.add(paramString);
										rh.paramarray.add(paramString);

										// String remaining ="";

										LastparamChecker(rh, queryORpath);

										// if (remaining.contains(",")
										// && remaining.indexOf(",") + 2 <
										// remaining
										// .length()) {
										// String newvalueString = remaining
										// .split(",")[1]
										// .split(" ")[1];
										// rh.paramarray
										// .add(newvalueString);
										//
										// }

									}

									else if (queryORpath.substring(0, 6).contains("Field") || queryORpath.substring(0, 6).contains("Query"))
									{
										String keyString = queryORpath.substring(queryORpath.indexOf("(") + 2, queryORpath.indexOf(")") - 1);
										String valueString = queryORpath.substring(queryORpath.indexOf(")") + 1);

										String[] valuearray = valueString.split(" ");
										String key = keyString;
										String valuetype = valuearray[1];
										String value = null;

										if (valuetype.equals("int") || valuetype.equals("Integer"))
											value = "[0-9]*";
										else
											value = "(.*)";

										if (numofquery == 0)
											rh.query += "(" + key + "=" + value + ")?";
										else
											rh.query += "(&)?(" + key + "=" + value + ")?";

										numofquery++;
										rh.paramarray.add(valuetype);
										LastparamChecker(rh, queryORpath);

										// if (queryORpath.contains(")")) {
										// String remaining = queryORpath
										// .split("\\)")[1];
										//
										// if (remaining.contains(",")
										// && remaining.indexOf(",") + 2 <
										// remaining
										// .length()) {
										// String newvalueString = remaining
										// .split(",")[1]
										// .split(" ")[1];
										// rh.paramarray
										// .add(newvalueString);
										//
										// }
										// }

									} else if (queryORpath.substring(0, 6).contains("Path") || queryORpath.substring(0, 12).contains("EncodedPath"))
									{
										String paramString = queryORpath.substring(queryORpath.indexOf("(") + 1, queryORpath.indexOf(")") - 1);
										String valueString = queryORpath.substring(queryORpath.indexOf(")") + 1);
										String param = paramString;

										String[] valuearray = valueString.split(" ");
										String valuetype = valuearray[1];

										if (paramString.contains("value"))
										{
											param = paramString.split("value")[1];
											param = param.replace("=", "");
											param = param.replace("\"", "");
										} else
										{
											param = param.replace("=", "");
											param = param.replace("\"", "");
										}

										rh.paramset.put(numofquery, param);
										rh.paramarray.add(valuetype);

										LastparamChecker(rh, queryORpath);

										// if (queryORpath.contains(")")) {
										// String remaining = queryORpath
										// .split("\\)")[1];
										//
										// if (remaining.contains(",")
										// && remaining.indexOf(",") + 2 <
										// remaining
										// .length()) {
										// String newvalueString = remaining
										// .split(",")[1]
										// .split(" ")[1];
										// rh.paramarray
										// .add(newvalueString);
										//
										// }
										// }

									} else if (queryORpath.substring(0, 6).contains("Part"))
									{
										String paramString = queryORpath.substring(queryORpath.indexOf("(") + 2, queryORpath.indexOf(")") - 1);
										String valueString = queryORpath.substring(queryORpath.indexOf(")") + 1);
										String param = paramString;

										String[] valuearray = valueString.split(" ");
										String valuetype = valuearray[1];

										if (paramString.contains("value"))
										{
											param = paramString.split("value")[1];
											param = param.replace("=", "");
											param = param.replace("\"", "");
										} else
										{
											param = param.replace("=", "");
											param = param.replace("\"", "");
										}

										rh.part.add(paramString + "=" + valuetype);
										rh.paramarray.add(valuetype);
										// String remaining = "";
										//
										// if (paramString.contains(","))
										// remaining = queryORpath
										// .replaceFirst(",", "");
										//
										// if (remaining.contains(",")
										// && remaining.indexOf(",") + 2 <
										// remaining
										// .length()) {
										// String newvalueString = remaining
										// .split(",")[1].split(" ")[1];
										// rh.paramarray.add(newvalueString);
										//
										// }
										LastparamChecker(rh, queryORpath);

										// if (queryORpath.contains(")")) {
										// String remaining = queryORpath
										// .split("\\)")[1];
										//
										// if (remaining.contains(",")
										// && remaining.indexOf(",") + 2 <
										// remaining
										// .length()) {
										// String newvalueString = remaining
										// .split(",")[1]
										// .split(" ")[1];
										// rh.paramarray
										// .add(newvalueString);
										//
										// }
										// }

									}

									else if (queryORpath.substring(0, 6).contains("Header"))
									{
										String paramString = queryORpath.substring(queryORpath.indexOf("(") + 2, queryORpath.indexOf(")") - 1);
										String valueString = queryORpath.substring(queryORpath.indexOf(")") + 1);
										String param = paramString;

										String[] valuearray = valueString.split(" ");
										String valuetype = valuearray[1];

										if (paramString.contains("value"))
										{
											param = paramString.split("value")[1];
											param = param.replace("=", "");
											param = param.replace("\"", "");
										} else
										{
											param = param.replace("=", "");
											param = param.replace("\"", "");
										}

										rh.header += paramString + "=" + valuetype;
										rh.paramarray.add(valuetype);
										// String remaining = "";
										//
										// if (paramString.contains(","))
										// remaining = queryORpath
										// .replaceFirst(",", "");
										//
										// if (remaining.contains(",")
										// && remaining.indexOf(",") + 2 <
										// remaining
										// .length()) {
										// String newvalueString = remaining
										// .split(",")[1].split(" ")[1];
										// rh.paramarray.add(newvalueString);
										//
										// }
										LastparamChecker(rh, queryORpath);

										// if (queryORpath.contains(")")) {
										// String remaining = queryORpath
										// .split("\\)")[1];
										//
										// if (remaining.contains(",")
										// && remaining.indexOf(",") + 2 <
										// remaining
										// .length()) {
										// String newvalueString = remaining
										// .split(",")[1]
										// .split(" ")[1];
										// rh.paramarray
										// .add(newvalueString);
										//
										// }
										// }

									}

									else
									{
										if (queryORpath.contains("Callback"))
										{
											String[] temp = queryORpath.split("Callback");
											rh.paramarray.add("Callback");
											rh.Callbackresponse.name = temp[1].split(" ")[0];

											rh.Callbackresponse.name = find_purecalss(rh.Callbackresponse, rh);

										}
									}

								}

							}

							rh.namespace = namespace;
							http_status = false;

							if (rh.Callbackresponse.name != null)
							{
								rh.Callbackresponse.name = rh.Callbackresponse.name.replaceAll(">", "").replaceAll("<", "");
								rh.Callbackresponse.name = find_namespace(importclasses, rh.Callbackresponse.name, rh.Callbackresponse.name);
							}

							// find response namespace
							// for (int i = 0; i < importclasses.size(); i++) {
							// String imports = importclasses.get(i);
							// String[] splited = imports.split("\\.");
							// imports = splited[splited.length - 1];
							// if (imports.equals(rh.returnclass)) {
							// rh.returnclass = importclasses.get(i);
							// hits++;
							// }
							// }
							// method ref making
							rh.methodref = "<" + rh.namespace + ": " + rh.returnclass + " " + rh.method + "(";

							ArrayList<String> paramarray = rh.paramarray;
							boolean isHasparam = false;
							for (int i = 0; i < paramarray.size(); i++)
							{
								isHasparam = true;

								String param = paramarray.get(i);
								String namespaceparam = "";
								if (param.equals("String"))
									namespaceparam = "java.lang.String";
								else if (param.equals("Integer"))
									namespaceparam = "java.lang.Integer";
								else if (param.equals("int"))
									namespaceparam = "int";
								else if (param.equals("boolean") || param.equals("Boolean"))
									namespaceparam = "boolean";
								else if (param.equals("Double"))
									namespaceparam = "java.lang.Double";
								else if (param.equals("double"))
									namespaceparam = "double";
								else if (param.equals("Float"))
									namespaceparam = "java.lang.Float";
								else if (param.equals("float"))
									namespaceparam = "float";
								else if (param.equals("long"))
									namespaceparam = "long";
								else if (param.equals("Long"))
									namespaceparam = "java.lang.Long";
								else if (param.equals("String[]"))
									namespaceparam = "java.lang.String[]";
								else
								{
									/*
									 * if(param.contains("Map<")) param="Map";
									 */

									int hitss = 0;
									// find response namespace
									for (int j = 0; j < importclasses.size(); j++)
									{
										String imports = importclasses.get(j);
										String[] splited = imports.split("\\.");
										imports = splited[splited.length - 1];
										if (imports.equals(param.split("<")[0]))
										{
											namespaceparam = importclasses.get(j);
											hitss++;
										}
									}
									if (hitss == 0)
										System.out.println("dddd");

								}
								rh.methodref += namespaceparam + ",";

							}
							if (isHasparam)
								rh.methodref = rh.methodref.substring(0, rh.methodref.length() - 1) + ")>";
							else
								rh.methodref = rh.methodref + ")>";

							// parameter parsing
							/*
							 * if(rh.paramset.size()>0) { for( Integer key :
							 * rh.paramset.keySet() ){ String param =
							 * "\\{"+rh.paramset.get(key)+"\\}";
							 * rh.suburl=rh.suburl.replaceAll(param, "(.*)");
							 * 
							 * } System.out.println("dddd");
							 * 
							 * }
							 */

							result.add(rh);

						}

					}

				}

			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;

	}

	private static String find_purecalss(classtype callbackresponse2, retrofit_http rh)
	{
		// offerupcase
		if (callbackresponse2.name.startsWith("a<"))
			callbackresponse2.name = callbackresponse2.name.substring(1);
		String pureclass = callbackresponse2.name;
		if (pureclass.contains("Observable<"))
		{
			pureclass = pureclass.split("Observable")[1];

			pureclass = pureclass.replaceFirst("<", "");
			pureclass = pureclass.replaceFirst(">", "");
		}
		if (pureclass.contains("List<"))
		{
			pureclass = pureclass.split("List<")[1];
			callbackresponse2.isList = true;
			// pureclass=pureclass.replaceFirst("<", "");
			pureclass = pureclass.replaceFirst(">", "");
		}
		if (pureclass.contains("[]"))
		{
			callbackresponse2.isArray = true;
			pureclass = pureclass.replace("]", "").replace("[", "");
		}
		return pureclass;
	}

	private static String find_namespace(ArrayList<String> importclasses, String pureclass, String responseclass)
	{
		String Respon_namespace = "";
		int hit = 0;
		// find response namespace
		for (int i = 0; i < importclasses.size(); i++)
		{
			String imports = importclasses.get(i);
			String[] splited = imports.split("\\.");
			imports = splited[splited.length - 1];
			if (imports.equals(pureclass))
			{
				Respon_namespace = importclasses.get(i);
				hit++;
			}
		}

		if (hit == 0)
			Respon_namespace = responseclass;

		return Respon_namespace;
	}

	private static boolean Setimport(retrofit_http rh, String s, boolean parsing_status, ArrayList<String> arraynamespace, int line,
			ArrayList<String> importclasses)
	{
		String arraynameString = arraynamespace.get(0);
		if (line == 1 && s.substring(0, 7).contains("package"))
		{
			arraynameString = s.substring(8, s.length() - 1) + ".";
		}

		if (parsing_status == false && s.length() > 7 && s.substring(0, 7).contains("import"))
		{
			s = s.replaceAll(";", "");
			importclasses.add(s.split("import ")[1]);
		}

		if (parsing_status == false && s.contains("abstract") && s.contains("interface"))
		{
			parsing_status = true;
			arraynameString += s.split("interface")[1].substring(1);
		}
		arraynamespace.clear();
		arraynamespace.add(arraynameString);
		return parsing_status;
	}

	public static ArrayList<String> retrofit_file_finder(File[] files, String namespaces) throws FileNotFoundException
	{
		ArrayList<String> candidate_retrofits = new ArrayList<String>();

		for (File file : files)
		{
			if (file.isDirectory())
			{
				// System.out.println("Directory: " + file.getName());
				ArrayList<String> child_result = retrofit_file_finder(file.listFiles(), namespaces); // Calls
																										// same
																										// method
																										// again.
				candidate_retrofits.addAll(child_result);
			} else
			{
				// System.out.println("File: " + file.getName());

				BufferedReader in;

				in = new BufferedReader(new FileReader(file));

				String s;
				boolean parsing_status = false;
				boolean import_status = false;

				boolean http_status = false;
				boolean isValidnamespace = false;
				boolean isValidimports = false;
				boolean isValidclass = false;
				boolean isURL = false;

				String namespace = "";
				int line = 0;
				ArrayList<String> importclasses = new ArrayList<String>();

				try
				{
					while ((s = in.readLine()) != null)
					{
						line++;
						s = s.trim();

						// namespace find
						if (line == 1 && s.substring(0, 7).contains("package"))
						{
							namespace = s.substring(8, s.length() - 1) + ".";
							if (namespace.contains(namespaces))
								isValidnamespace = true;
							else
								break;
							import_status = true;
							continue;
						}

						// import find
						// import loading
						if (import_status == true && parsing_status == false && s.length() > 7 && s.substring(0, 7).contains("import"))
						{
							s = s.replaceAll(";", "");
							importclasses.add(s.split("import ")[1]);
						}

						// import end
						else if (import_status == true && line > 2)
						{
							if (importclasses.contains("retrofit.http.POST") || importclasses.contains("retrofit.http.GET")
									|| importclasses.contains("retrofit.http.PUT") || importclasses.contains("retrofit.http.DELETE"))
								isValidimports = true;
							else
								break;
							import_status = false;
							continue;
						}

						if (import_status == false && parsing_status == false)
						{
							if (s.contains("abstract") && s.contains("interface"))
							{
								parsing_status = true;
								namespace += s.split("interface")[1].substring(1);

							} else if (s.length() < 9)
							{
								isValidclass = false;
								break;
							} else if ((s.substring(0, 9).contains("public") || s.substring(0, 9).contains("private")))
							{
								isValidclass = false;
								break;
							}
						}

						if (parsing_status)
						{
							if (s.startsWith("{"))
								continue;
							else if (s.startsWith("@"))
							{
								s = s.substring(1);
								if (http_status == false)
								{
									if (s.substring(0, 6).contains("GET"))
									{
										isURL = true;
										if (!candidate_retrofits.contains(namespace))
											candidate_retrofits.add(file.getAbsolutePath());
										break;

									} else if (s.substring(0, 6).contains("POST"))
									{
										isURL = true;
										if (!candidate_retrofits.contains(namespace))
											candidate_retrofits.add(file.getAbsolutePath());
										break;

									} else if (s.substring(0, 6).contains("PUT"))
									{
										isURL = true;
										if (!candidate_retrofits.contains(namespace))
											candidate_retrofits.add(file.getAbsolutePath());
										break;

									} else if (s.substring(0, 6).contains("DELETE"))
									{
										isURL = true;
										if (!candidate_retrofits.contains(namespace))
											candidate_retrofits.add(file.getAbsolutePath());
										break;

									} else if (s.substring(0, 6).contains("HEAD"))
									{
										isURL = true;
										if (!candidate_retrofits.contains(namespace))
											candidate_retrofits.add(file.getAbsolutePath());
										break;

									} else
										continue;
									// System.err.println("Unvalid HTTP metohd, it is not GET, POST, PUT, DELETE, HEAD");
								}

							}

						}
					}

				} catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		return candidate_retrofits;

	}

	public static String namespaceTOpath(String namespace2)
	{
		if (namespace2.contains("[]"))
			return namespace2.replaceAll("\\.", "/").replaceAll("\\[", "").replaceAll("\\]", "") + ".java";
		else
			return namespace2.replaceAll("\\.", "/") + ".java";
	}

	public static String fromjson(String basepath, String namespace2, Set<String> history, String innerClass) throws IOException
	{
		retrofit_http.basepath = basepath;
		String path = retrofit_http.namespaceTOpath(namespace2);

		String targetCLASS = "";
		if (innerClass.equals(""))
			targetCLASS = namespace2;
		else
			targetCLASS = namespace2 + "$" + innerClass;

		if (Cache.containsKey(targetCLASS))
			return Cache.get(targetCLASS);

		if (history.contains(targetCLASS))
			return "\"" + namespace2 + "\"";

		// history Upadte
		Set<String> newhistory = new HashSet<String>();
		newhistory.add(targetCLASS);
		newhistory.addAll(history);

		// Library obfuscation
		if (namespace2.length() < 9)
			return "(.*)";
		// JsonObject
		if (namespace2.equals("com.google.gson.JsonObject") || namespace2.startsWith("Map<") || namespace2.startsWith("org.json")
				|| namespace2.startsWith("android") || namespace2.startsWith("HashMap<") || namespace2.startsWith("ArrayList<")
				|| namespace2.startsWith("LinkedHashMap<") || namespace2.startsWith("retrofit.client.Response")
				|| namespace2.startsWith("com.fasterxml.jackson."))
			return "(.*)";
		// primitive
		if (namespace2.contains("LinkedHashMap<String, Object>"))
			return "{(.*):(.*)}";
		String previous_classname = "";
		if (beforeClassname.contains("$"))
			previous_classname = beforeClassname.split("\\$")[1].replace(".java", "");
		else
			previous_classname = beforeClassname.replace(".java", "");
		int ad;

		if (namespace2.contains("SearchV2Results"))
		{
			ad = 3;
		}
		// recursive field
		if (beforeClassname.contains("$") && path.split(previous_classname).length > 1 && path.split(previous_classname)[1].equals(".java"))
			return "{(.*)|THIS_CLASS}";

		File file = new File(basepath + path);
		if (!file.exists())
		{
			// System.err.println("NO such file: " + path);
			return "(.*)|NO_SUCH_FILE";
		}

		if(Constants.isDebug)
			System.out.println("File: " + file.getName());

		BufferedReader in = new BufferedReader(new FileReader(file));

		// json map
		Map<String, String> jsonmap = new HashMap<String, String>();
		// annotation key
		String nowjson = "";
		// return string
		String returnjson = "";
		// Number of Bracket
		Stack<String> bracketStack = new Stack<String>();
		boolean innerClassStart = false;

		String s;
		boolean parsing_status = false;
		boolean import_status = false;

		String namespace = "";
		String classname = "";
		int line = 0;
		ArrayList<String> importclasses = new ArrayList<String>();

		// MakeSubClass();

		try
		{
			while ((s = in.readLine()) != null)
			{
				line++;
				s = s.trim();
				if (futurework_Exceptions(s, bracketStack))
				{
					in.close();
					return "{" + "\"SUPERCLASS\":" + "\"" + namespace2 + "\"" + "}";
				}
				// namespace find
				if (line == 1 && s.substring(0, 7).contains("package"))
				{
					namespace = s.substring(8, s.length() - 1) + ".";
					if (!namespace.contains(namespace))
						break;
					import_status = true;
					continue;
				}

				// import find
				// import loading
				if (import_status == true && parsing_status == false && s.length() > 7 && s.substring(0, 7).contains("import"))
				{
					s = s.replaceAll(";", "");
					importclasses.add(s.split("import ")[1]);
				}

				// import end
				else if (import_status == true && line > 2)
				{
					import_status = false;
				}

				// 여기서 부터 parsing
				if (import_status == false && parsing_status == false)
				{
					if (s.contains("public") || s.contains("private") || s.startsWith("class "))
					{
						parsing_status = true;
						if (s.split("class ").length > 1)
						{
							namespace += s.split("class ")[1];
							classname = s.split("class ")[1];
						} else
						{
							in.close();
							return "(.*)";
						}
					}
					continue;
				}
				if (parsing_status)
				{

					if (s.endsWith(" class " + innerClass) && bracketStack.size() == 0)
					{
						// json map
						jsonmap = new HashMap<String, String>();
						// annotation key
						nowjson = "";
						// return string
						returnjson = "";
						innerClassStart = true;
						continue;

					} else if (innerClassStart)
					{
						ArrayList<Object> InClassreturns = InClassParsing(s, importclasses, newhistory, namespace, classname, bracketStack, file,
								jsonmap, nowjson);
						if ((boolean) InClassreturns.get(0))
							continue;
						else
						{
							nowjson = (String) InClassreturns.get(1);
							returnjson += (String) InClassreturns.get(2);
						}
						if (bracketStack.size() == 0)
						{
							innerClassStart = false;
							break;
						}
					} else if (innerClass.equals(""))
					{
						ArrayList<Object> InClassreturns = InClassParsing(s, importclasses, newhistory, namespace, classname, bracketStack, file,
								jsonmap, nowjson);
						if ((boolean) InClassreturns.get(0))
							continue;
						else
						{
							nowjson = (String) InClassreturns.get(1);
							returnjson += (String) InClassreturns.get(2);
						}
					}
				} else if (parsing_status)
				{
					if (s.startsWith("implements "))
					{
						continue;
					} else if (s.startsWith("extends"))
					{
						String extended = s.split("extends ")[1];
						if (extended.contains("<") && extended.contains(">"))
							continue;
						String extended_namespace = find_namespace(importclasses, extended, "ERROR");

						if (extended.contains("."))
							extended = extended.replace(".", "$");

						if (extended_namespace.equals("ERROR"))
							extended_namespace = namespace.split(classname.replace("$", "\\$"))[0] + extended;

						returnjson += fromjson(basepath, extended_namespace, newhistory, "");
						continue;
					}
					// function name start
					else if (s.contains("{") || s.contains("}"))
					{
						if (s.contains("{"))
							bracketStack.push("{");
						if (s.contains("}"))
							bracketStack.pop();
					} else if (bracketStack.size() > 1 || bracketStack.size() == 0)
						continue;
					else
					{
						// field parsing
						if (s.startsWith("@"))
							nowjson = annotation_parsing(s);
						else if (isIgnore)
							isIgnore = false;
						else if (!s.equals(""))
						{
							String remove_public_private = s;
							if (s.contains("(") && s.contains(")") && !s.contains(" new "))
								continue;

							if (s.startsWith("public"))
								remove_public_private = s.split("public ")[1];
							if (s.startsWith("private"))
								remove_public_private = s.split("private ")[1];
							if (s.startsWith("protected"))
								remove_public_private = s.split("protected ")[1];
							if (s.contains(" transient "))
								remove_public_private = s.split("transient ")[1];
							if (remove_public_private.contains("="))
								remove_public_private = remove_public_private.split(" =")[0];
							if (remove_public_private.contains("WeakReference<"))
							{
								remove_public_private = remove_public_private.replaceAll("WeakReference<", "");
								String front = remove_public_private.substring(0, lastspaceIndex(remove_public_private) - 1);
								String second = remove_public_private.substring(lastspaceIndex(remove_public_private));
								remove_public_private = front + second;
							}

							if (s.contains(" static ") || s.startsWith("static") || s.startsWith("/*"))
								continue;

							beforeClassname = file.getName();

							field_parsing(jsonmap, nowjson, remove_public_private, basepath, namespace.split("\\$")[0], classname.split("\\$")[0],
									importclasses, newhistory);
							nowjson = "";
						}
					}

				}
			}

		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String finaljsonreult = "(.*)";

		if (jsonmap.size() > 0 && !(returnjson.equals("") || returnjson.equals("(.*)")))
			finaljsonreult = (jsonmap.toString().substring(0, jsonmap.toString().length() - 1) + ","
					+ returnjson.substring(1, returnjson.length() - 1) + "}").replaceAll("=", ":");
		else if (jsonmap.size() > 0 && (returnjson.equals("") || returnjson.equals("(.*)")))
			finaljsonreult = jsonmap.toString().replaceAll("=", ":");
		else if (!returnjson.equals(""))
			finaljsonreult = returnjson.replaceAll("=", ":");

		finaljsonreult = finaljsonreult.replaceAll("\"\"", "\"").replaceAll("\"\\[", "[").replaceAll("\\]\"", "]").replaceAll("\"\\{", "{")
				.replaceAll("\\}\"", "}");

		in.close();
		Cache.put(targetCLASS, finaljsonreult);

		return finaljsonreult;
	}

	private static boolean futurework_Exceptions(String s, Stack<String> bracketStack)
	{
		if (bracketStack.size() == 0 && (s.startsWith("@JsonSubTypes") || s.startsWith("@JsonTypeInfo")))
			return true;

		return false;
	}

	private static ArrayList<Object> InClassParsing(String s, ArrayList<String> importclasses, Set<String> newhistory, String namespace,
			String classname, Stack<String> bracketStack, File file, Map<String, String> jsonmap, String nowjson) throws IOException
	{
		ArrayList<Object> returns;
		String returnjson = "";
		boolean isContinue = false;

		if (s.startsWith("implements "))
		{
			isContinue = true;
			returns = makereturns(isContinue, nowjson, returnjson);
			return returns;
		} else if (s.startsWith("extends"))
		{
			String extended = s.split("extends ")[1];
			if (extended.contains("<") && extended.contains(">"))
			{
				isContinue = true;
				returns = makereturns(isContinue, nowjson, returnjson);
				return returns;
			}
			String extended_namespace = find_namespace(importclasses, extended, "ERROR");

			if (extended.contains("."))
				extended = extended.replace(".", "$");

			if (extended_namespace.equals("ERROR"))
				extended_namespace = namespace.split(classname.replace("$", "\\$"))[0] + extended;

			returnjson += fromjson(basepath, extended_namespace, newhistory, "");
			returns = makereturns(isContinue, nowjson, returnjson);
			return returns;
		}
		// function name start
		else if (s.contains("{") || s.contains("}"))
		{
			if (s.contains("{"))
				bracketStack.push("{");
			if (s.contains("}"))
				bracketStack.pop();
		} else if (bracketStack.size() > 1 || bracketStack.size() == 0)
		{
			isContinue = true;
			returns = makereturns(isContinue, nowjson, returnjson);
			return returns;
		} else
		{
			// field parsing
			if (s.startsWith("@"))
				nowjson = annotation_parsing(s);
			else if (isIgnore)
				isIgnore = false;
			else if (!s.equals(""))
			{
				String remove_public_private = s;
				if (s.contains("(") && s.contains(")") && !s.contains(" new ")
						&& !(s.contains("=") && s.split("=").length == 2 && isPrimitiveAssign(s.split("=")[1].trim())))
				{
					isContinue = true;
					returns = makereturns(isContinue, nowjson, returnjson);
					return returns;
				}

				if (s.startsWith("public"))
					remove_public_private = s.split("public ")[1];
				if (s.startsWith("private"))
					remove_public_private = s.split("private ")[1];
				if (s.startsWith("protected"))
					remove_public_private = s.split("protected ")[1];
				if (s.contains(" transient "))
					remove_public_private = s.split("transient ")[1];
				if (remove_public_private.contains("="))
					remove_public_private = remove_public_private.split(" =")[0];
				if (remove_public_private.contains("WeakReference<"))
				{
					remove_public_private = remove_public_private.replaceAll("WeakReference<", "");
					String front = remove_public_private.substring(0, lastspaceIndex(remove_public_private) - 1);
					String second = remove_public_private.substring(lastspaceIndex(remove_public_private));
					remove_public_private = front + second;
				}

				if (s.contains(" static ") || s.startsWith("static") || s.startsWith("/*"))
				{
					isContinue = true;
					returns = makereturns(isContinue, nowjson, returnjson);
					return returns;
				}

				beforeClassname = file.getName();

				field_parsing(jsonmap, nowjson, remove_public_private, basepath, namespace.split("\\$")[0], classname.split("\\$")[0], importclasses,
						newhistory);
				nowjson = "";
			}
		}
		isContinue = false;
		returns = makereturns(isContinue, nowjson, returnjson);
		return returns;

	}

	private static boolean isPrimitiveAssign(String trim)
	{
		if (trim.startsWith("Integer.valueOf"))
			return true;
		if (trim.startsWith("Long.valueOf"))
			return true;
		if(trim.contains("calueOf"))
			return false;

		return false;
	}

	private static ArrayList<Object> makereturns(boolean isContinue, String nowjson, String returnjson)
	{
		ArrayList<Object> temp = new ArrayList<Object>();
		temp.add(isContinue);
		temp.add(nowjson);
		temp.add(returnjson);
		return temp;
	}

	private static boolean CheckisFieldDone(String s)
	{
		if (!s.startsWith("@") && !s.contains("=") && s.contains("(") && s.contains(")") || s.startsWith("/*") || s.equals("static"))
			return true;
		else
			return false;
	}

	private static void field_parsing(Map<String, String> jsonmap, String nowjson, String s, String basepath, String origin_namespace,
			String classname, ArrayList<String> importclasses, Set<String> history) throws IOException
	{
		String key = "";
		String value = "";

		if (s.startsWith("final "))
			s = s.split("final ")[1];
		String[] handlerReturn =
		{ "", "" };
		boolean isArray = false;

		if (s.contains("[]"))
		{
			s = s.replace("[]", "");
			isArray = true;
		}

		if (isPrimitive(s))
			handlerReturn = Primitive_parsing(s, nowjson);
		else if (isCollection(s))
			handlerReturn = Collection_parsing(s, nowjson, importclasses, origin_namespace, classname, history);
		else if (isSubClass(s))
			handlerReturn = SubClass_parsing(s, nowjson, origin_namespace, classname, history);
		else
			handlerReturn = Class_parsing(s, nowjson, importclasses, origin_namespace, classname, history);

		// String[] primitive_return = Primitive_parsing(s, nowjson);
		// key = primitive_return[0];
		// value = primitive_return[1];
		//
		// if (!value.equals(""))
		// {
		//
		// }
		// // Map
		// else if (s.startsWith("Map<"))
		// {
		//
		// String fulllist = s.substring(0, lastspaceIndex(s));
		// String targetclass = fulllist.replaceFirst("Map<", "");
		// targetclass = targetclass.substring(0, targetclass.length() - 1);
		// String leftkey = targetclass.split(", ")[0];
		// String rightkey = targetclass.split(", ")[1];
		//
		// if (targetclass.contains("HashMap<") || targetclass.contains("List<")
		// || targetclass.contains("ArrayList<")
		// || targetclass.contains("Map<"))
		// {
		// System.err.println("DOUBLE MAP OR LIST");
		//
		// } else
		// {
		// key = fieldnamekey(s, nowjson, targetclass + "> ");
		//
		// // get key regex
		// String tempS = leftkey + " tempkey";
		// String[] returns = Primitive_parsing(tempS, "");
		//
		// // get value regex
		// Map<String, String> newjsonmap = new HashMap<String, String>();
		// tempS = rightkey + " tempkey";
		//
		// field_parsing(newjsonmap, "", tempS, basepath, origin_namespace,
		// classname, importclasses, history);
		// String valueRegex = newjsonmap.get("tempkey");
		//
		// if (returns[1].equals(""))
		// System.err.println("Map<NonPrimitive, ?> error");
		//
		// // value = "[" + fromjson(basepath,
		//
		// // getTargetClasspath(importclasses, targetclass,
		// // origin_namespace, classname), history) + "]";
		// else
		// {
		// value = "[" + returns[1] + ":" + valueRegex + "]";
		// }
		// }
		// }
		// // HashMap
		// else if (s.startsWith("HashMap<"))
		// {
		// String fulllist = s.substring(0, lastspaceIndex(s));
		// String targetclass = fulllist.replaceFirst("HashMap<", "");
		// targetclass = targetclass.substring(0, targetclass.length() - 1);
		// String leftkey = targetclass.split(", ")[0];
		// String rightkey = targetclass.split(", ")[1];
		//
		// if (targetclass.contains("HashMap<") || targetclass.contains("List<")
		// || targetclass.contains("ArrayList<")
		// || targetclass.contains("Map<"))
		// {
		// System.err.println("DOUBLE MAP OR LIST");
		//
		// } else
		// {
		// key = fieldnamekey(s, nowjson, targetclass + "> ");
		//
		// // get key regex
		// String tempS = leftkey + " tempkey";
		// String[] returns = Primitive_parsing(tempS, "");
		//
		// // get value regex
		// Map<String, String> newjsonmap = new HashMap<String, String>();
		// tempS = rightkey + " tempkey";
		//
		// field_parsing(newjsonmap, "", tempS, basepath, origin_namespace,
		// classname, importclasses, history);
		// String valueRegex = newjsonmap.get("tempkey");
		//
		// if (returns[1].equals(""))
		// System.err.println("HashMap<NonPrimitive, ?> error");
		//
		// // value = "[" + fromjson(basepath,
		// // getTargetClasspath(importclasses, targetclass,
		// // origin_namespace, classname), history) + "]";
		// else
		// {
		// value = "[" + returns[1] + ":" + valueRegex + "]";
		// }
		// }
		// }
		// // LinkedHashMap
		// else if (s.startsWith("LinkedHashMap<"))
		// {
		// String fulllist = s.substring(0, lastspaceIndex(s));
		// String targetclass = fulllist.replaceFirst("LinkedHashMap<", "");
		// targetclass = targetclass.substring(0, targetclass.length() - 1);
		// String leftkey = targetclass.split(", ")[0];
		// String rightkey = targetclass.split(", ")[1];
		//
		// if (targetclass.contains("HashMap<") || targetclass.contains("List<")
		// || targetclass.contains("ArrayList<")
		// || targetclass.contains("Map<"))
		// {
		// System.err.println("DOUBLE MAP OR LIST");
		//
		// } else
		// {
		// key = fieldnamekey(s, nowjson, targetclass + "> ");
		//
		// // get key regex
		// String tempS = leftkey + " tempkey";
		// String[] returns = Primitive_parsing(tempS, "");
		//
		// // get value regex
		// Map<String, String> newjsonmap = new HashMap<String, String>();
		// tempS = rightkey + " tempkey";
		//
		// field_parsing(newjsonmap, "", tempS, basepath, origin_namespace,
		// classname, importclasses, history);
		// String valueRegex = newjsonmap.get("tempkey");
		//
		// if (returns[1].equals(""))
		// System.err.println("HashMap<NonPrimitive, ?> error");
		//
		// // value = "[" + fromjson(basepath,
		// // getTargetClasspath(importclasses, targetclass,
		// // origin_namespace, classname), history) + "]";
		// else
		// {
		// value = "[" + returns[1] + ":" + valueRegex + "]";
		// }
		// }
		// }
		// // list
		// else if (s.startsWith("List<"))
		// {
		// String fulllist = s.substring(0, lastspaceIndex(s));
		// String targetclass = fulllist.replaceFirst("List<", "");
		// targetclass = targetclass.substring(0, targetclass.length() - 1);
		//
		// key = fieldnamekey(s, nowjson, targetclass + "> ");
		//
		// if (targetclass.contains("HashMap<") || targetclass.contains("List<")
		// || targetclass.contains("ArrayList<")
		// || targetclass.contains("Map<"))
		// {
		// Map<String, String> newjsonmap = new HashMap<String, String>();
		// String tempS = targetclass + " tempkey";
		//
		// field_parsing(newjsonmap, "", tempS, basepath, origin_namespace,
		// classname, importclasses, history);
		// String valueRegex = newjsonmap.get("tempkey");
		//
		// value = "[" + valueRegex + "]";
		//
		// } else
		// {
		//
		// String tempS = targetclass + " tempkey";
		// String[] returns = Primitive_parsing(tempS, nowjson);
		//
		// if (returns[1].equals(""))
		// {
		// // subclass
		// if (targetclass.contains("."))
		// {
		// String subclassfull = targetclass.split(" ")[0];
		// String subclassname = subclassfull.replace(".", "$");
		// value = fromjson(basepath,
		// origin_namespace.split(classname.replace("$", "\\$"))[0] +
		// subclassname, history);
		// } else
		// value = "[" + fromjson(basepath, getTargetClasspath(importclasses,
		// targetclass, origin_namespace, classname), history) + "]";
		// } else
		// value = "[" + returns[1] + "]";
		// }
		// }
		// // Arraylist
		// else if (s.startsWith("ArrayList<"))
		// {
		// String fulllist = s.substring(0, lastspaceIndex(s));
		// String targetclass = fulllist.replaceFirst("ArrayList<", "");
		// targetclass = targetclass.substring(0, targetclass.length() - 1);
		//
		// key = fieldnamekey(s, nowjson, targetclass + "> ");
		//
		// if (targetclass.contains("HashMap<") || targetclass.contains("List<")
		// || targetclass.contains("ArrayList<")
		// || targetclass.contains("Map<"))
		// {
		// Map<String, String> newjsonmap = new HashMap<String, String>();
		// String tempS = targetclass + " tempkey";
		//
		// field_parsing(newjsonmap, "", tempS, basepath, origin_namespace,
		// classname, importclasses, history);
		// String valueRegex = newjsonmap.get("tempkey");
		//
		// value = "[" + valueRegex + "]";
		//
		// } else
		// {
		//
		// String tempS = targetclass + " tempkey";
		// String[] returns = Primitive_parsing(tempS, nowjson);
		//
		// if (returns[1].equals(""))
		// {
		// // subclass
		// if (targetclass.contains("."))
		// {
		// String subclassfull = targetclass.split(" ")[0];
		// String subclassname = subclassfull.replace(".", "$");
		// value = fromjson(basepath,
		// origin_namespace.split(classname.replace("$", "\\$"))[0] +
		// subclassname, history);
		// } else
		// value = "[" + fromjson(basepath, getTargetClasspath(importclasses,
		// targetclass, origin_namespace, classname), history) + "]";
		// } else
		// value = "[" + returns[1] + "]";
		// }
		// }
		// // subclass
		// else if (s.contains("."))
		// {
		// String subclassfull = s.split(" ")[0];
		// String subclassname = subclassfull.replace(".", "$");
		// // String subclassname = subclassfull.split("\\.")[1];
		//
		// key = fieldnamekey(s, nowjson, subclassfull + " ");
		// value = fromjson(basepath,
		// origin_namespace.split(classname.replace("$", "\\$"))[0] +
		// subclassname, history);
		// }
		// // class
		// else
		// {
		// String targetclass = s.split(" ")[0];
		// boolean isArray = false;
		// if (targetclass.contains("[]"))
		// {
		// targetclass = targetclass.substring(0, targetclass.length() - 2);
		// isArray = true;
		// }
		// if (isArray)
		// key = fieldnamekey(s, nowjson, targetclass + "\\[\\] ");
		// else
		// key = fieldnamekey(s, nowjson, targetclass + " ");
		// if (isArray)
		// value = "[" + fromjson(basepath, getTargetClasspath(importclasses,
		// targetclass, origin_namespace, classname), history) + "]";
		// else
		// value = fromjson(basepath, getTargetClasspath(importclasses,
		// targetclass, origin_namespace, classname), history);
		//
		// }

		key = handlerReturn[0];
		value = handlerReturn[1];

		if (isArray)
			value = "[" + value + "]";

		if (!nowjson.equals(""))
			key = nowjson;
		key = "\"" + key.replaceAll(";", "") + "\"";
		value = "\"" + value + "\"";

		jsonmap.put(key, value);
	}

	private static String[] Collection_parsing(String s, String nowjson, ArrayList<String> importclasses, String origin_namespace, String classname,
			Set<String> history) throws IOException
	{
		String[] handlerReturn =
		{ "", "" };

		if (isMaptype(s))
			handlerReturn = Maptype_parsing(s, nowjson, importclasses, origin_namespace, classname, history);
		else if (isListtype(s))
			handlerReturn = Listtype_parsing(s, nowjson, importclasses, origin_namespace, classname, history);

		return handlerReturn;
	}

	private static String[] Listtype_parsing(String s, String nowjson, ArrayList<String> importclasses, String origin_namespace, String classname,
			Set<String> history) throws IOException
	{
		String key = "";
		String value = "";
		String[] returnarray =
		{ "", "" };

		String fulllist = "";
		String targetclass = "";
		// List
		if (s.startsWith("List<"))
		{
			fulllist = s.substring(0, lastspaceIndex(s));
			targetclass = fulllist.replaceFirst("List<", "");
			targetclass = targetclass.substring(0, targetclass.length() - 1);
		}
		// Arraylist
		else if (s.startsWith("ArrayList<"))
		{
			fulllist = s.substring(0, lastspaceIndex(s));
			targetclass = fulllist.replaceFirst("ArrayList<", "");
			targetclass = targetclass.substring(0, targetclass.length() - 1);
		}
		// SortedSet
		else if (s.startsWith("SortedSet<"))
		{
			fulllist = s.substring(0, lastspaceIndex(s));
			targetclass = fulllist.replaceFirst("SortedSet<", "");
			targetclass = targetclass.substring(0, targetclass.length() - 1);
		}
		// TreeSet
		else if (s.startsWith("TreeSet<"))
		{
			fulllist = s.substring(0, lastspaceIndex(s));
			targetclass = fulllist.replaceFirst("TreeSet<", "");
			targetclass = targetclass.substring(0, targetclass.length() - 1);
		}

		key = fieldnamekey(s, nowjson, fulllist + " ");

		// get value regex
		Map<String, String> newjsonmap = new HashMap<String, String>();
		String tempS = targetclass + " tempkey";

		field_parsing(newjsonmap, "", tempS, basepath, origin_namespace, classname, importclasses, history);
		String valueRegex = newjsonmap.get("\"tempkey\"");

		valueRegex = addDoubleQuote(valueRegex);

		value = "[" + valueRegex + "]";

		returnarray[0] = key;
		returnarray[1] = value;
		return returnarray;

	}

	private static String[] Maptype_parsing(String s, String nowjson, ArrayList<String> importclasses, String origin_namespace, String classname,
			Set<String> history) throws IOException
	{
		String key = "";
		String value = "";
		String[] returnarray =
		{ "", "" };

		String fulllist = "";
		String targetclass = "";

		if (s.startsWith("Map<"))
		{
			// Map delete
			fulllist = s.substring(0, lastspaceIndex(s));
			targetclass = fulllist.replaceFirst("Map<", "");
			targetclass = targetclass.substring(0, targetclass.length() - 1);
		}
		// HashMap
		else if (s.startsWith("HashMap<"))
		{
			// HashMap delete
			fulllist = s.substring(0, lastspaceIndex(s));
			targetclass = fulllist.replaceFirst("HashMap<", "");
			targetclass = targetclass.substring(0, targetclass.length() - 1);
		}
		// LinkedHashMap
		else if (s.startsWith("LinkedHashMap<"))
		{
			// HashMap delete
			fulllist = s.substring(0, lastspaceIndex(s));
			targetclass = fulllist.replaceFirst("LinkedHashMap<", "");
			targetclass = targetclass.substring(0, targetclass.length() - 1);
		}

		String leftkey = targetclass.substring(0, findfirstComma(targetclass));
		String rightvalue = targetclass.substring(findfirstComma(targetclass) + 2);

		key = fieldnamekey(s, nowjson, fulllist + " ");

		// get key regex
		String tempS = leftkey + " tempkey";
		String[] returns = Primitive_parsing(tempS, "");

		if (returns[1].equals(""))
			returns[1] = "(.*)";
		// System.err.println("Map<NonPrimitive, ?> error");
		returns[1] = addDoubleQuote(returns[1]);

		// get value regex
		Map<String, String> newjsonmap = new HashMap<String, String>();
		tempS = rightvalue + " tempkey";

		field_parsing(newjsonmap, "", tempS, basepath, origin_namespace, classname, importclasses, history);
		String valueRegex = newjsonmap.get("\"tempkey\"");

		value = "[" + "{" + returns[1] + ":" + valueRegex + "}" + "]";

		returnarray[0] = key;
		returnarray[1] = value;
		return returnarray;
	}

	private static String addDoubleQuote(String string)
	{
		return "\"" + string + "\"";
	}

	private static boolean isListtype(String s)
	{
		if (s.startsWith("List<"))
			return true;
		else if (s.startsWith("ArrayList<"))
			return true;
		else if (s.startsWith("SortedSet<"))
			return true;
		else if (s.startsWith("TreeSet<"))
			return true;
		return false;
	}

	private static boolean isMaptype(String s)
	{
		if (s.startsWith("Map<"))
			return true;
		else if (s.startsWith("HashMap<"))
			return true;
		else if (s.startsWith("LinkedHashMap<"))
			return true;
		return false;
	}

	private static String[] Class_parsing(String s, String nowjson, ArrayList<String> importclasses, String origin_namespace, String classname,
			Set<String> history) throws IOException
	{
		String key = "";
		String value = "";
		String[] returnarray =
		{ "", "" };

		String targetclass = s.split(" ")[0];
		boolean isArray = false;
		if (targetclass.contains("[]"))
		{
			targetclass = targetclass.substring(0, targetclass.length() - 2);
			isArray = true;
		}
		if (isArray)
			key = fieldnamekey(s, nowjson, targetclass + "\\[\\] ");
		else
			key = fieldnamekey(s, nowjson, targetclass + " ");
		value = fromjson(basepath, getTargetClasspath(importclasses, targetclass, origin_namespace, classname), history, "");

		// Inner class case
		if (value.equals("(.*)|NO_SUCH_FILE"))
		{
			String InnerClass = targetclass;
			value = fromjson(basepath, origin_namespace, history, InnerClass);
		}

		if (isArray)
			value = "[" + value + "]";

		returnarray[0] = key;
		returnarray[1] = value;
		return returnarray;
	}

	private static String[] SubClass_parsing(String s, String nowjson, String origin_namespace, String classname, Set<String> history)
			throws IOException
	{
		String key = "";
		String value = "";
		String[] returnarray =
		{ "", "" };

		String subclassfull = s.substring(0, lastspaceIndex(s));
		String subclassname = subclassfull.replace(".", "$");

		key = fieldnamekey(s, nowjson, subclassfull + " ");
		value = fromjson(basepath, origin_namespace.split(classname.replace("$", "\\$"))[0] + subclassname, history, "");

		// Inner class case
		if (value.equals("(.*)|NO_SUCH_FILE"))
		{
			String InnerClass = subclassname.split("\\$")[1];
			value = fromjson(basepath, origin_namespace.split(classname.replace("$", "\\$"))[0] + subclassname.split("\\$")[0], history, InnerClass);
		}

		returnarray[0] = key;
		returnarray[1] = value;
		return returnarray;

	}

	private static boolean isSubClass(String s)
	{
		if (s.contains("."))
			return true;
		return false;
	}

	private static boolean isCollection(String s)
	{
		if (s.startsWith("Map<"))
			return true;
		else if (s.startsWith("HashMap<"))
			return true;
		else if (s.startsWith("LinkedHashMap<"))
			return true;
		else if (s.startsWith("List<"))
			return true;
		else if (s.startsWith("ArrayList<"))
			return true;
		else if (s.startsWith("SortedSet<"))
			return true;
		else if (s.startsWith("TreeSet<"))
			return true;
		return false;
	}

	private static boolean isPrimitive(String s)
	{

		if (s.startsWith("boolean "))
		{
			return true;
		} else if (s.startsWith("Boolean "))
		{
			return true;
		} else if (s.startsWith("Date "))
		{
			return true;
		} else if (s.startsWith("String "))
		{
			return true;
		} else if (s.startsWith("int "))
		{
			return true;
		} else if (s.startsWith("Integer "))
		{
			return true;
		} else if (s.startsWith("Double "))
		{
			return true;
		} else if (s.startsWith("double "))
		{
			return true;
		} else if (s.startsWith("Uri "))
		{
			return true;
		} else if (s.startsWith("long "))
		{
			return true;
		} else if (s.startsWith("Long "))
		{
			return true;
		} else if (s.startsWith("float "))
		{
			return true;
		} else if (s.startsWith("Float "))
		{
			return true;
		} else if (s.startsWith("Object"))
		{
			return true;
		} else if (s.startsWith("Spanned"))
		{
			return true;
		} else if (s.startsWith("T "))
		{
			return true;
		} else if (s.startsWith("PointF "))
		{
			return true;
		} else if (s.startsWith("File "))
		{
			return true;
		} else if (s.startsWith("Class<"))
		{
			return true;
		}

		return false;
	}

	private static String[] Primitive_parsing(String s, String nowjson)
	{
		String key = "";
		String value = "";
		String[] returnarray =
		{ "", "" };
		if (s.startsWith("boolean "))
		{
			key = fieldnamekey(s, nowjson, "boolean ");
			value = "(1|0)";
		} else if (s.startsWith("Boolean "))
		{
			key = fieldnamekey(s, nowjson, "Boolean ");
			value = "(1|0)";
		} else if (s.startsWith("Date "))
		{
			key = fieldnamekey(s, nowjson, "Date ");
			value = "(.*|java.util.Date)";
		} else if (s.startsWith("String "))
		{
			key = fieldnamekey(s, nowjson, "String ");
			value = "(.*)";
		} else if (s.startsWith("int "))
		{
			key = fieldnamekey(s, nowjson, "int ");
			value = "<0-9>*";
		} else if (s.startsWith("Integer "))
		{
			key = fieldnamekey(s, nowjson, "Integer ");
			value = "<0-9>*";
		} else if (s.startsWith("Double "))
		{
			key = fieldnamekey(s, nowjson, "Double ");
			value = "/^[0-9]+(\\.[0-9]+)?$";
		} else if (s.startsWith("double "))
		{
			key = fieldnamekey(s, nowjson, "double ");
			value = "/^[0-9]+(\\.[0-9]+)?$";
		} else if (s.startsWith("Uri "))
		{
			key = fieldnamekey(s, nowjson, "Uri ");
			value = "(.*|Uri)";
		} else if (s.startsWith("long "))
		{
			key = fieldnamekey(s, nowjson, "long ");
			value = "<0-9>*";
		} else if (s.startsWith("Long "))
		{
			key = fieldnamekey(s, nowjson, "Long ");
			value = "<0-9>*";
		} else if (s.startsWith("float "))
		{
			key = fieldnamekey(s, nowjson, "float ");
			value = "^<->?(0|<1-9><0-9>*)(\\.<0-9>+)?(<eE><+->?<0-9>+)?$";
		} else if (s.startsWith("Float "))
		{
			key = fieldnamekey(s, nowjson, "Float ");
			value = "^<->?(0|<1-9><0-9>*)(\\.<0-9>+)?(<eE><+->?<0-9>+)?$";
		} else if (s.startsWith("Object "))
		{
			key = fieldnamekey(s, nowjson, "Object ");
			value = "(.*)";
		} else if (s.startsWith("Spanned "))
		{
			key = fieldnamekey(s, nowjson, "Spanned ");
			value = "(.*)";
		} else if (s.startsWith("T "))
		{
			key = fieldnamekey(s, nowjson, "T ");
			value = "(.*)";
		} else if (s.startsWith("PointF "))
		{
			key = fieldnamekey(s, nowjson, "PointF ");
			value = "(.*)";
		} else if (s.startsWith("File "))
		{
			key = fieldnamekey(s, nowjson, "File ");
			value = "(.*)";
		} else if (s.startsWith("Class<"))
		{
			key=s.substring(lastspaceIndex(s));
			value = "(.*)";
		}

		returnarray[0] = key;
		returnarray[1] = value;
		return returnarray;
	}

	private static String getTargetClasspath(ArrayList<String> importclasses, String targetclass, String origin_namespace, String classname)
	{
		String returnstring = "";
		String targetnamespace = find_namespace(importclasses, targetclass, "ERROR");

		if (targetnamespace.equals("ERROR"))
		{
			String packagepath = "";
			if(!origin_namespace.equals(classname))
				packagepath = origin_namespace.split(classname)[0];
			
			returnstring = packagepath + targetclass;
		} else
			returnstring = targetnamespace;
		return returnstring;
	}

	private static int lastspaceIndex(String s)
	{
		for (int i = s.length() - 1; i >= 0; i--)
		{
			if (s.charAt(i) == ' ')
				return i;
		}
		return -1;
	}

	private static int findfirstComma(String s)
	{
		for (int i = 0; i < s.length(); i++)
		{
			if (s.charAt(i) == ',')
				return i;
		}
		return -1;
	}

	private static String fieldnamekey(String s, String nowjson, String parsing)
	{
		String key = "";
		if (nowjson.equals(""))
			key = s.replace(parsing, "");
		if (retrofit_http.LOWER_CASE_WITH_UNDERSCORES)
		{
			ArrayList<String> result = new ArrayList<String>();
			int lastindex = 0;
			for (int i = 0; i < key.length(); i++)
			{
				if (Character.isUpperCase(key.charAt(i)))
				{
					String beforestring = key.substring(lastindex, i);
					result.add(beforestring);
					String changestring = "_" + Character.toLowerCase(key.charAt(i));
					result.add(changestring);
					lastindex = i + 1;
				}
			}
			result.add(key.substring(lastindex));
			String resultstring = "";
			for (String temp : result)
				resultstring += temp;
			return resultstring;
		} else
			return key;

	}

	private static String annotation_parsing(String s)
	{
		Set<String> annotations = new HashSet<String>();
		annotations.add("@Expose");
		annotations.add("@Nullable");
		String nowjson = "";
		if (s.startsWith("@SerializedName"))
		{
			nowjson = s.split("@SerializedName")[1];
			if (nowjson.contains("="))
				nowjson = s.split("=")[1];
		} else if (s.startsWith("@JsonProperty"))
		{
			nowjson = s.split("@JsonProperty")[1];
			if (nowjson.contains("="))
				nowjson = s.split("=")[1];
		} else if (s.startsWith("@JsonIgnore"))
		{
			isIgnore = true;
		} else if (!annotations.contains(s))
			System.err.println(s);
		return nowjson.replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\"", "");
	}

}
