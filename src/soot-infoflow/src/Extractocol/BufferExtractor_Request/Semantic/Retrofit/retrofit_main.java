package Extractocol.BufferExtractor_Request.Semantic.Retrofit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class retrofit_main {

	public static void main(String[] args) throws FileNotFoundException {
		String apkname = args[0];
		String targetnamespace = args[1];

		ArrayList<retrofit_http_old> retrof_results = new ArrayList<retrofit_http_old>();
		File[] files = new File(
				"D:/new_extractocol/tools/dex2jar-2.0/dex2jar-2.0/" + apkname
						+ "-dex2jar.src/").listFiles();
		ArrayList<String> result = retrofit_http_old.retrofit_file_finder(files,
				targetnamespace);
		System.out.println("RESULT--------------------");
		System.out.println();
		System.out.println();
		System.out.println();

		for (String retrofit_files : result)
			retrof_results.addAll(retrofit_http_old.retrofitparser(retrofit_files));

		System.out.println("Result retrofits");

		for (int i = 0; i < retrof_results.size(); i++) {
			retrofit_http_old tt = retrof_results.get(i);
			System.out.println("SUB = " + tt.suburl);
			System.out.println("Method = " + tt.methodref);
			System.out.println("query = " + tt.query);
			System.out.println("querymap = " + tt.querymap);
		}

		String input = "D:/new_extractocol/doc/" + apkname + "/front.txt";
		System.out.println("file SIZE=" + result.size());
		System.out.println("retrofits SIZE=" + retrof_results.size());

		Set<String> sets = new HashSet<String>();
		System.out.println();
		System.out.println();
		System.out.println();

		System.out.println("DEAD retrofits");
		int num = 0;

		boolean isfrontdead = false;
		if (!isfrontdead) {
			try {
				BufferedReader in;

				in = new BufferedReader(new FileReader(input));

				String s;

				int line = 0;
				try {
					while ((s = in.readLine()) != null) {

						for (int i = 0; i < retrof_results.size(); i++) {
							if (s.contains(retrof_results.get(i).methodref))
								retrof_results.get(i).isDead = false;

						}

						line++;

					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			for (int i = 0; i < retrof_results.size(); i++) {
				if (retrof_results.get(i).isDead) {
					retrofit_http_old tt = retrof_results.get(i);
					System.out.println("SUB = " + tt.suburl);
					System.out.println(tt.methodref);
					System.out.println("query = " + tt.query);
					System.out.println("querymap = " + tt.querymap);
					System.out.println("origin = " + tt.origin);

					System.out.println();
					num++;
				}
			}

			System.out.println("Dead=" + num);
		}

		// for (int i = 0; i < retrof_results.size(); i++) {
		// retrofit_http tt = retrof_results.get(i);
		// if(tt.methodref.contains("<com.offerup.android.network.ItemService: void createItem(java.lang.String,java.lang.Integer,java.lang.Integer,java.lang.String,java.lang.Double,java.lang.Integer,java.lang.String,java.lang.Integer,java.lang.String,java.lang.Double,java.lang.Double,int,java.lang.String)>")
		// || tt.methodref.contains("")
		//
		// )
		// System.out.println("SUB = " + tt.suburl);
		// System.out.println("Method = " + tt.methodref);
		// System.out.println("query = " + tt.query);
		// System.out.println("querymap = " + tt.querymap);
		// }

		num = 0;
		for (int i = 0; i < retrof_results.size(); i++) {
			retrofit_http_old tt = retrof_results.get(i);

			if (!tt.isDead || isfrontdead) {
				String url = "";

				String urlgg = tt.suburl.replaceAll("\\{.*\\}", "(.*)");
				if (tt.querymap.size() > 0) {
					System.out.println();
					System.out.println("SUB = " + tt.http_method + tt.suburl);
					System.out.println("Method = " + tt.methodref);
					System.out.println("query = " + tt.query);
					System.out.println("querymap = " + tt.querymap);
					System.out.println();
				} else if (!tt.query.equals("?")) {
					url = tt.http_method + "(.*)" + urlgg + tt.query;

				} else {
					url = tt.http_method + "(.*)" + urlgg;

				}

				if (tt.part.size() > 0)
					System.out.println("URL = " + url + "###PART"
							+ tt.part.toString());
				else
					System.out.println("URL = " + url);
				sets.add(url);
				num++;
			}

		}
		System.out.println("LIVE=" + num);
		System.out.println();

		for (String ssss : sets) {
			System.out.println(ssss);
		}
		System.out.println("중복제거=" + sets.size());

	}
}
// UNIQUE SET FINDER
//
//
//
// import java.io.BufferedReader;
// import java.io.BufferedWriter;
// import java.io.File;
// import java.io.FileNotFoundException;
// import java.io.FileReader;
// import java.io.FileWriter;
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.HashSet;
// import java.util.Iterator;
// import java.util.Set;
//
//
// public class main {
//
// public static void main(String[] args) {
//
// String input = "C:/Users/EllStar/Desktop/letitgo.txt";
//
// try {
// BufferedReader in;
//
// in = new BufferedReader(new FileReader(input));
//
// String s;
// Set<String> sets = new HashSet<String>();
//
// int line =0;
// try {
// while ((s = in.readLine()) != null) {
// if(s.startsWith("url"))
// sets.add(s);
// line++;
//
// }
// } catch (IOException e) {
// e.printStackTrace();
// }
//
// Iterator<String> ss= sets.iterator();
// String []array = new String[100];
// int i=0;
// while(ss.hasNext())
// {
// String sss= ss.next();
// System.out.println(sss);
// if(sss.length()!=0)
// array[i]=sss;
// i++;
// }
// System.out.println(sets.size());
//
//
//
//
// Arrays.sort(array, String.CASE_INSENSITIVE_ORDER);
//
// for(int j=0; j<array.length; j++)
// {
// System.out.println(array[j]);
// }
//
//
// } catch (FileNotFoundException e) {
// e.printStackTrace();
// }
//
//
// }
//
//
//
// }
//
