
package extractocol.common.pairing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ResponseParser
{

	public static void main(String args[])
	{
		HashMap<String, String> EPandResTable = new HashMap<String, String>();

		ReadResponse(EPandResTable);
	}

	public static void ReadResponse(HashMap<String, String> ePandResTable)
	{
		String EPPath = "D:\\new_extractocol\\SerializationFiles\\wish\\wish_response_nonsearchmethod.txt";
		String JsonPath = "D:\\new_extractocol\\SerializationFiles\\wish\\pairinfo.txt";
		JSONArray Pairinfo = null;
		JSONParser parser = new JSONParser();
		JSONArray Responses = null;
		try
		{
			Object obj = parser.parse(new FileReader(JsonPath));
			Pairinfo = (JSONArray) obj;
			JSONObject DP = (JSONObject) Pairinfo.get(0);
			Responses = (JSONArray) DP.get("Responses");
		} catch (FileNotFoundException e2)
		{
			// JM Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2)
		{
			// JM Auto-generated catch block
			e2.printStackTrace();
		} catch (ParseException e2)
		{
			// JM Auto-generated catch block
			e2.printStackTrace();
		}

		BufferedReader in = null;
		try
		{
			in = new BufferedReader(new FileReader(EPPath));
		} catch (FileNotFoundException e)
		{
			// JM Auto-generated catch block
			e.printStackTrace();
		}
		String s;

		try
		{

			String prev = "";
			String current = "";

			while ((s = in.readLine()) != null)
			{
				prev = current;
				current = s;

				if (current.contains("[response]"))
				{

					JSONObject targetRes = SearchTarget(prev.trim(), Responses, current.trim());

					// System.out.println("prev : " + prev.trim());
					// System.out.println("current :" + current.trim());
				}
			}

			// Building SourceAndSinks
			BuildSourceAndSinks(Pairinfo);

		} catch (IOException e1)
		{
			// JM Auto-generated catch block
			e1.printStackTrace();
		}
		try
		{
			in.close();
		} catch (IOException e)
		{
			// JM Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void BuildSourceAndSinks(JSONArray pairinfo)
	{
		StringBuilder sourceandSink = new StringBuilder();
		JSONObject Dp = (JSONObject) pairinfo.get(0);
		JSONArray Requests = (JSONArray) Dp.get("Requests");
		JSONArray Responses = (JSONArray) Dp.get("Responses");

		int idx = 0;
		System.out.println("=== Dependency Source And Sink ===");

		for (Object o : Responses)
		{
			if (o instanceof JSONObject)
			{
				JSONObject res = (JSONObject) o;

				if (!res.get("BODY").equals("(.*"))
				{
					if (res.containsKey("EP_Stmts"))
					{
						JSONArray EP_Stmts = (JSONArray) res.get("EP_Stmts");

						for (Object o2 : EP_Stmts)
						{
							if (o2 instanceof JSONObject)
							{
								JSONObject stmt = (JSONObject) o2;
								sourceandSink.append(stmt.get("stmt") + " -> _SOURCE_ ");
								sourceandSink.append("\n");
							}
						}

						for (Object o3 : Requests)
						{
							if (o3 instanceof JSONObject)
							{
								JSONObject req = (JSONObject) o3;
								sourceandSink.append(req.get("EP_Method") + " -> _SINK_");
								sourceandSink.append("\n");
							}
						}
					}
				}
			}
			if (sourceandSink.length() > 0)
			{
				System.out.println("%Response Index : " + idx);
				System.out.println("%Response Body : " + ((JSONObject) Responses.get(idx)).get("BODY"));
				System.out.println(sourceandSink.toString());
				System.out.println("==============================================================================================================");

				WriteSourceAndSink(idx, ((JSONObject) Responses.get(idx)).get("BODY"),
						sourceandSink.toString());

				sourceandSink.setLength(0);
			}
			idx++;
		}
	}

	private static void WriteSourceAndSink(int idx, Object  body, String sourcesink)
	{
		try
		{
			File file = new File("D:\\new_extractocol\\src\\soot-infoflow-android\\SourcesAndSinksPairing\\SourceSink_" + idx + ".txt");
			FileWriter fw = new FileWriter(file, false);

			fw.write("%Response Index : " + String.valueOf(idx) + "\n");
			fw.write("%Response Body : " +  (String) body + "\n");
			fw.write(sourcesink + "\n");
			fw.flush();
			fw.close();

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static JSONObject SearchTarget(String trim, JSONArray responses, String body)
	{
		for (Object o : responses)
		{
			if (o instanceof JSONObject)
			{
				JSONObject res = (JSONObject) o;
				if (res.containsKey("EP_Method"))
				{
					if (res.get("EP_Method").equals(trim))
					{
						res.put("BODY", body);
						// System.out.println(res);
					}
				}
			}
		}
		return null;
	}
}
