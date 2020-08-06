package extractocol.backend.request.semantic.retrofit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class retrofit_http_old {

	public retrofit_http_old() {
	}

	public String http_method="";
	public String suburl="";
	public String query = "?";
	public String method="";
	public String origin ="";
	public String namespace="";
	public String responseclass="";
	public String Callbackresponse="";
	public String header="";
	public Map<Integer, String> paramset = new HashMap<Integer, String>();
	public ArrayList<String> paramarray = new ArrayList<String>();
	public ArrayList<String> querymap = new ArrayList<String>();
	public ArrayList<String> body = new ArrayList<String>();
	public ArrayList<String> part = new ArrayList<String>();
	public boolean isDead=true;

	public String methodref = "";
	public String returnclass = "";
	
	public static void LastparamChecker(retrofit_http_old rh, String param)
	{
		//last param
		if(param.contains("Callback") &&  !param.startsWith("Callback"))
		{
			String[] temp = param.split("Callback");
			rh.paramarray.add("Callback");
			rh.Callbackresponse=temp[1].split(" ")[0];
			
		}
		else
			return ;
		
		
		
		
	}
	

	public static ArrayList<retrofit_http_old> retrofitparser(String input) {
		ArrayList<retrofit_http_old> result = new ArrayList<retrofit_http_old>();

		try {
			BufferedReader in;
			
			System.out.println("FILENAME :"+input);
			
			

			in = new BufferedReader(new FileReader(input));

			String s;
			boolean parsing_status = false;
			boolean http_status = false;
			retrofit_http_old rh = null;
			String namespace = "";
			int line = 0;
			ArrayList<String> importclasses = new ArrayList<String>();

			try {
				while ((s = in.readLine()) != null) {
					line++;
					s = s.trim();
					if (line == 1 && s.substring(0, 7).contains("package")) {
						namespace = s.substring(8, s.length() - 1) + ".";
					}

					if (parsing_status == false && s.length() > 7
							&& s.substring(0, 7).contains("import")) {
						s = s.replaceAll(";", "");
						importclasses.add(s.split("import ")[1]);
					}

					if (parsing_status == false && s.contains("abstract")
							&& s.contains("interface")) {
						parsing_status = true;
						namespace += s.split("interface")[1].substring(1);
					}

					if (parsing_status) {
						if (s.startsWith("{"))
							continue;
						else if (s.startsWith("@")) {
							s = s.substring(1);
							if (http_status == false) {
								if (s.substring(0, 6).contains("GET")) {
									rh = new retrofit_http_old();
									rh.http_method = "GET";
									rh.suburl = s.substring(s.indexOf("(") + 2,
											s.indexOf(")") - 1);
									http_status = true;

								} else if (s.substring(0, 6).contains("POST")) {
									rh = new retrofit_http_old();
									rh.http_method = "POST";
									rh.suburl = s.substring(s.indexOf("(") + 2,
											s.indexOf(")") - 1);
									http_status = true;

								} else if (s.substring(0, 6).contains("PUT")) {
									rh = new retrofit_http_old();
									rh.http_method = "PUT";
									rh.suburl = s.substring(s.indexOf("(") + 2,
											s.indexOf(")") - 1);
									http_status = true;

								} else if (s.substring(0, 6).contains("DELETE")) {
									rh = new retrofit_http_old();
									rh.http_method = "DELETE";
									rh.suburl = s.substring(s.indexOf("(") + 2,
											s.indexOf(")") - 1);
									http_status = true;

								} else if (s.substring(0, 6).contains("HEAD")) {
									rh = new retrofit_http_old();
									rh.suburl = s.substring(s.indexOf("(") + 2,
											s.indexOf(")") - 1);
									http_status = true;
									rh.http_method = "HEAD";

								} else
									continue;
								// System.err.println("Unvalid HTTP metohd, it is not GET, POST, PUT, DELETE, HEAD");
							}
						} else if (http_status
								&& (s.substring(0, 6).contains("public") || s
										.substring(0, 7).contains("private"))) {
							int splitindex = s.indexOf("(");
							String front = s.substring(0, splitindex);
							String back = s.substring(splitindex + 1);
							String[] frontarray = front.split(" ");

							if (frontarray[2].contains("<"))
								rh.returnclass = frontarray[2].split("<")[0];
							else
								rh.returnclass = frontarray[2];

							String responseclass = "";
							
							
							for(int i=2; i<frontarray.length-1; i++)
							responseclass+=frontarray[i];
							
//							front
//									.split(frontarray[frontarray.length - 1])[0]
//									.split("abstract")[1];
							String pureclass = responseclass;
							if (pureclass.contains("Observable<")) {
								pureclass = pureclass.split("Observable")[1];

								pureclass = pureclass.replaceFirst("<", "");
								pureclass = pureclass.replaceFirst(">", "");
							}
							if (pureclass.contains("List<")) {
								pureclass = pureclass.split("List<")[1];

								// pureclass=pureclass.replaceFirst("<", "");
								pureclass = pureclass.replaceFirst(">", "");
							}
							String last2chars="";
							pureclass = pureclass.trim();
							if(pureclass.length()>2)
								last2chars = pureclass.substring(pureclass.length()-2, pureclass.length());
							
								
							boolean isResponseArray =false;
							if(last2chars.equals("[]"))
							{
								isResponseArray=true;
								pureclass=pureclass.substring(0,pureclass.length()-2);
							}
							
							String Respon_namespace = pureclass;

							int hit = 0;
							// find response namespace
							for (int i = 0; i < importclasses.size(); i++) {
								String imports = importclasses.get(i);
								String[] splited = imports.split("\\.");
								imports = splited[splited.length - 1];
								if (imports.equals(pureclass)) {
									Respon_namespace = importclasses.get(i);
									hit++;
								}
							}

							if (hit == 0)
								Respon_namespace = responseclass;
							
							hit=0;
							
							String returnclass = rh.returnclass;
							
							
							if(last2chars.length()>2)
							last2chars = rh.returnclass.substring(rh.returnclass.length()-2, rh.returnclass.length());
							boolean isReturnArray =false;
							if(last2chars.equals("[]"))
							{
								isReturnArray=true;
								rh.returnclass=rh.returnclass.substring(0,rh.returnclass.length()-2);
							}
							
							// find return namespace
							for (int i = 0; i < importclasses.size(); i++) {
								String imports = importclasses.get(i);
								String[] splited = imports.split("\\.");
								imports = splited[splited.length - 1];
								if (imports.equals(rh.returnclass)) {
									rh.returnclass = importclasses.get(i);
									hit++;
								}
							}

							if (hit == 0)
								rh.returnclass = returnclass;
							
							rh.returnclass = rh.returnclass.trim();
							if(!isReturnArray)
							rh.returnclass = rh.returnclass;
							else
							rh.returnclass = rh.returnclass+"[]";

							rh.method = frontarray[frontarray.length - 1];
							rh.origin= s;

							String[] backarray = back.split("@");

							int numofquery = 0;

							for (int i = 0; i < backarray.length; i++) {
								String queryORpath = backarray[i];
								if (queryORpath.length() > 7) {

									if (queryORpath.substring(0, 8).contains(
											"QueryMap")  || queryORpath.substring(0, 8).contains(
													"FieldMap")) {
										String[] temparray = queryORpath
												.split(" ");
										String paramString = temparray[1] + " "
												+ temparray[2];
										rh.querymap.add(paramString);
										rh.paramarray.add(paramString);
										String remaining = "";

										if (paramString.contains(","))
											remaining = queryORpath
													.replaceFirst(",", "");

										if (remaining.contains(",")
												&& remaining.indexOf(",") + 2 < remaining
														.length()) {
											String newvalueString = remaining
													.split(",")[1].split(" ")[1];
											rh.paramarray.add(newvalueString);

										}

									} else if (queryORpath.substring(0, 8)
											.contains("Body")) {
										String[] temparray = queryORpath
												.split(" ");
										String paramString="";
										if(temparray[1].contains("Map"))
										{
											paramString= temparray[1] + " "
												+ temparray[2];
										}
										else
										{
											paramString=temparray[1];
										}
										rh.body.add(paramString);
										rh.paramarray.add(paramString);
 
										//	String remaining ="";
											
											LastparamChecker(rh, queryORpath);
											

//											if (remaining.contains(",")
//													&& remaining.indexOf(",") + 2 < remaining
//															.length()) {
//												String newvalueString = remaining
//														.split(",")[1]
//														.split(" ")[1];
//												rh.paramarray
//														.add(newvalueString);
//
//											}
										

									}

									else if (queryORpath.substring(0, 6)
											.contains("Field")
											|| queryORpath.substring(0, 6)
													.contains("Query")) {
										String keyString = queryORpath
												.substring(
														queryORpath
																.indexOf("(") + 2,
														queryORpath
																.indexOf(")") - 1);
										String valueString = queryORpath
												.substring(queryORpath
														.indexOf(")") + 1);

										String[] valuearray = valueString
												.split(" ");
										String key = keyString;
										String valuetype = valuearray[1];
										String value = null;

										if (valuetype.equals("int")
												|| valuetype.equals("Integer"))
											value = "[0-9]*";
										else
											value = "(.*)";

										if (numofquery == 0)
											rh.query += "("+key + "=" + value+")?";
										else
											rh.query += "(&)?(" + key + "=" + value+")?";

										numofquery++;
										rh.paramarray.add(valuetype);

										if (queryORpath.contains(")")) {
											String remaining = queryORpath
													.split("\\)")[1];

											if (remaining.contains(",")
													&& remaining.indexOf(",") + 2 < remaining
															.length()) {
												String newvalueString = remaining
														.split(",")[1]
														.split(" ")[1];
												rh.paramarray
														.add(newvalueString);

											}
										}

									} else if (queryORpath.substring(0, 6)
											.contains("Path") || queryORpath.substring(0, 12)
											.contains("EncodedPath")) {
										String paramString = queryORpath
												.substring(
														queryORpath
																.indexOf("(") + 1,
														queryORpath
																.indexOf(")") - 1);
										String valueString = queryORpath
												.substring(queryORpath
														.indexOf(")") + 1);
										String param = paramString;

										String[] valuearray = valueString
												.split(" ");
										String valuetype = valuearray[1];

										if (paramString.contains("value")) {
											param = paramString.split("value")[1];
											param = param.replace("=", "");
											param = param.replace("\"", "");
										} else {
											param = param.replace("=", "");
											param = param.replace("\"", "");
										}

										rh.paramset.put(numofquery, param);
										rh.paramarray.add(valuetype);
										
										LastparamChecker(rh, queryORpath);


//										if (queryORpath.contains(")")) {
//											String remaining = queryORpath
//													.split("\\)")[1];
//
//											if (remaining.contains(",")
//													&& remaining.indexOf(",") + 2 < remaining
//															.length()) {
//												String newvalueString = remaining
//														.split(",")[1]
//														.split(" ")[1];
//												rh.paramarray
//														.add(newvalueString);
//
//											}
//										}

									}
									else if (queryORpath.substring(0, 6)
											.contains("Part")) {
										String paramString = queryORpath
												.substring(
														queryORpath
																.indexOf("(") + 2,
														queryORpath
																.indexOf(")") - 1);
										String valueString = queryORpath
												.substring(queryORpath
														.indexOf(")") + 1);
										String param = paramString;

										String[] valuearray = valueString
												.split(" ");
										String valuetype = valuearray[1];

										if (paramString.contains("value")) {
											param = paramString.split("value")[1];
											param = param.replace("=", "");
											param = param.replace("\"", "");
										} else {
											param = param.replace("=", "");
											param = param.replace("\"", "");
										}

										rh.part.add(paramString+"="+valuetype);
										rh.paramarray.add(valuetype);
//										String remaining = "";
//
//										if (paramString.contains(","))
//											remaining = queryORpath
//													.replaceFirst(",", "");
//
//										if (remaining.contains(",")
//												&& remaining.indexOf(",") + 2 < remaining
//														.length()) {
//											String newvalueString = remaining
//													.split(",")[1].split(" ")[1];
//											rh.paramarray.add(newvalueString);
//
//										}
										LastparamChecker(rh, queryORpath);

//										if (queryORpath.contains(")")) {
//											String remaining = queryORpath
//													.split("\\)")[1];
//
//											if (remaining.contains(",")
//													&& remaining.indexOf(",") + 2 < remaining
//															.length()) {
//												String newvalueString = remaining
//														.split(",")[1]
//														.split(" ")[1];
//												rh.paramarray
//														.add(newvalueString);
//
//											}
//										}

									}
									
									else if (queryORpath.substring(0, 6)
											.contains("Header")) {
										String paramString = queryORpath
												.substring(
														queryORpath
																.indexOf("(") + 2,
														queryORpath
																.indexOf(")") - 1);
										String valueString = queryORpath
												.substring(queryORpath
														.indexOf(")") + 1);
										String param = paramString;

										String[] valuearray = valueString
												.split(" ");
										String valuetype = valuearray[1];

										if (paramString.contains("value")) {
											param = paramString.split("value")[1];
											param = param.replace("=", "");
											param = param.replace("\"", "");
										} else {
											param = param.replace("=", "");
											param = param.replace("\"", "");
										}

										rh.header+=paramString+"="+valuetype;
										rh.paramarray.add(valuetype);
//										String remaining = "";
//
//										if (paramString.contains(","))
//											remaining = queryORpath
//													.replaceFirst(",", "");
//
//										if (remaining.contains(",")
//												&& remaining.indexOf(",") + 2 < remaining
//														.length()) {
//											String newvalueString = remaining
//													.split(",")[1].split(" ")[1];
//											rh.paramarray.add(newvalueString);
//
//										}
										LastparamChecker(rh, queryORpath);

//										if (queryORpath.contains(")")) {
//											String remaining = queryORpath
//													.split("\\)")[1];
//
//											if (remaining.contains(",")
//													&& remaining.indexOf(",") + 2 < remaining
//															.length()) {
//												String newvalueString = remaining
//														.split(",")[1]
//														.split(" ")[1];
//												rh.paramarray
//														.add(newvalueString);
//
//											}
//										}

									}
									
									
									else {
										String valueString = queryORpath
												.split(" ")[0];
										rh.paramarray.add(valueString);
									}

								}

							}

							rh.namespace = namespace;
							http_status = false;
							int hits = 0;
							// find response namespace
							for (int i = 0; i < importclasses.size(); i++) {
								String imports = importclasses.get(i);
								String[] splited = imports.split("\\.");
								imports = splited[splited.length - 1];
								if (imports.equals(rh.returnclass)) {
									rh.returnclass = importclasses.get(i);
									hits++;
								}
							}
							//method ref making
							rh.methodref = "<" + rh.namespace + ": "
									+ rh.returnclass + " " + rh.method + "(";

							ArrayList<String> paramarray = rh.paramarray;
							boolean isHasparam=false;
							for (int i = 0; i < paramarray.size(); i++) {
								isHasparam=true;
								
								String param = paramarray.get(i);
								String namespaceparam = "";
								if (param.equals("String"))
									namespaceparam = "java.lang.String";
								else if (param.equals("Integer"))
									namespaceparam = "java.lang.Integer";
								else if (param.equals("int"))
									namespaceparam = "int";
								else if (param.equals("boolean")|| param.equals("Boolean"))
									namespaceparam = "boolean";
								else if (param.equals("Double"))
									namespaceparam = "java.lang.Double";
								else if (param.equals("double"))
									namespaceparam = "double";
								else if (param.equals("Float"))
									namespaceparam="java.lang.Float";
								else if (param.equals("float"))
									namespaceparam="float";
								else if (param.equals("long"))
									namespaceparam="long";
								else if (param.equals("Long"))
									namespaceparam="java.lang.Long";
								else if (param.equals("String[]"))
									namespaceparam="java.lang.String[]";
								else {
									/*if(param.contains("Map<"))
										param="Map";*/
									
									int hitss = 0;
									// find response namespace
									for (int j = 0; j < importclasses.size(); j++) {
										String imports = importclasses.get(j);
										String[] splited = imports.split("\\.");
										imports = splited[splited.length - 1];
										if (imports.equals(param.split("<")[0])) {
											namespaceparam = importclasses
													.get(j);
											hitss++;
										}
									}
									if (hitss == 0)
										System.out.println("dddd");

								}
								rh.methodref += namespaceparam + ",";

							}
							if(isHasparam)
							rh.methodref = rh.methodref.substring(0,
									rh.methodref.length() - 1) + ")>";
							else
							rh.methodref = rh.methodref + ")>";
							
							//parameter parsing
							/*if(rh.paramset.size()>0)
							{
								for( Integer key : rh.paramset.keySet() ){
									String param = "\\{"+rh.paramset.get(key)+"\\}";
									rh.suburl=rh.suburl.replaceAll(param, "(.*)");
									
						        }
								System.out.println("dddd");

							}*/
	

							result.add(rh);

						}

					}

				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return result;

	}
	
	

	public static ArrayList<String> retrofit_file_finder(File[] files, String namespaces) throws FileNotFoundException {
		ArrayList<String> candidate_retrofits = new ArrayList<String>();

		
		for (File file : files) {
			if (file.isDirectory()) {
				//System.out.println("Directory: " + file.getName());
				ArrayList<String> child_result=retrofit_file_finder(file.listFiles(), namespaces); // Calls same method again.
				candidate_retrofits.addAll(child_result);
			} else {
				//System.out.println("File: " + file.getName());

				BufferedReader in;

				in = new BufferedReader(new FileReader(file));

				String s;
				boolean parsing_status = false;
				boolean import_status = false;

				boolean http_status = false;
				boolean isValidnamespace=false;
				boolean isValidimports=false;
				boolean isValidclass=false;
				boolean isURL=false;
				
				String namespace = "";
				int line = 0;
				ArrayList<String> importclasses = new ArrayList<String>();

				try {
					while ((s = in.readLine()) != null) {
						line++;
						s = s.trim();
						
						//namespace find
						if (line == 1 && s.substring(0, 7).contains("package")) {
							namespace = s.substring(8, s.length() - 1)+".";
							if(namespace.contains(namespaces))
								isValidnamespace=true;
							else
								break;
							import_status=true;
							continue;
						}						
						
						
						//import find
						//import loading
						if (import_status==true&&parsing_status == false && s.length() > 7
								&& s.substring(0, 7).contains("import")) {
							s = s.replaceAll(";", "");
							importclasses.add(s.split("import ")[1]);
						}
						
						//import end
						else if(import_status==true&& line>2)
						{
							if(importclasses.contains("retrofit.http.POST")||importclasses.contains("retrofit.http.GET")||importclasses.contains("retrofit.http.PUT")||importclasses.contains("retrofit.http.DELETE"))
								isValidimports=true;
							else
								break;
							import_status=false;
							continue;
						}
						
						if(import_status==false&& parsing_status == false)
						{
						if (s.contains("abstract")
								&& s.contains("interface")) {
							parsing_status = true;
							namespace += s.split("interface")[1].substring(1);
							
						}
						else if(s.length() <9)
						{
							isValidclass=false;
							break;
						}
						else if((s.substring(0, 9).contains("public") || s.substring(0, 9).contains("private")))
						{
							isValidclass=false;
							break;
						}
						}
						

						if (parsing_status) {
							if (s.startsWith("{"))
								continue;
							else if (s.startsWith("@")) {
								s = s.substring(1);
								if (http_status == false) {
									if (s.substring(0, 6).contains("GET")) {
										isURL=true;
										if(!candidate_retrofits.contains(namespace))
											candidate_retrofits.add(file.getAbsolutePath());
										break;

									} else if (s.substring(0, 6).contains(
											"POST")) {
										isURL=true;
										if(!candidate_retrofits.contains(namespace))
										candidate_retrofits.add(file.getAbsolutePath());
										break;
										
									} else if (s.substring(0, 6)
											.contains("PUT")) {
										isURL=true;
										if(!candidate_retrofits.contains(namespace))
										candidate_retrofits.add(file.getAbsolutePath());
										break;


									} else if (s.substring(0, 6).contains(
											"DELETE")) {
										isURL=true;
										if(!candidate_retrofits.contains(namespace))
										candidate_retrofits.add(file.getAbsolutePath());
										break;


									} else if (s.substring(0, 6).contains(
											"HEAD")) {
										isURL=true;
										if(!candidate_retrofits.contains(namespace))
										candidate_retrofits.add(file.getAbsolutePath());
										break;


									} else
										continue;
									// System.err.println("Unvalid HTTP metohd, it is not GET, POST, PUT, DELETE, HEAD");
								}
						

						}

					}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return candidate_retrofits;

	}

	
}
