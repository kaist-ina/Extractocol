
package extractocol.common.pairing;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.ListenableGraph;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import sun.security.provider.certpath.Vertex;

public class BuildDependencyGraph
{
	public static void main(String args[])
	{
//		PrintDotGraph();
		ListenableGraph g = BuildJgraph();
		
		System.out.println("All Edges : " + g.edgeSet().size());
		System.out.println("Dependency Vertaxes : " + g.vertexSet().size());
		FloydWarshallShortestPaths<String, DefaultEdge> floyd = new FloydWarshallShortestPaths<>(g);
		
		//Get longestPath
		for (Object o1 : g.vertexSet())
		{
			String src = (String) o1;
			for (Object o2 : g.vertexSet())
			{
				String dst = (String) o2;
				if (floyd.getShortestPath(src, dst) != null)
				{
					System.out.println("Src : " + src + " Dst : " + dst + " Length : " + floyd.getShortestPath(src, dst).getEdgeList().size());
					System.out.println(floyd.getShortestPath(src, dst));
				}
			}
		}
	}

	private static ListenableGraph BuildJgraph()
	{
		ListenableGraph gp = new ListenableDirectedGraph (DefaultEdge.class);
		int Maxlength = 0;
		String MaxlengthNode = "";
		// JM Auto-generated method stub
		JSONArray PairInfo = ReadPairInfo();

		JSONObject Pair = (JSONObject) PairInfo.get(0);
		JSONObject Depen = (JSONObject) Pair.get("Dependency");
		
		Set<String> keys = Depen.keySet();
		
		for (String key : keys)
		{
			JSONObject depEntry = (JSONObject) Depen.get(key);
			JSONArray Depidxes = (JSONArray) depEntry.get("DepIdxes");
			
			if (Maxlength < Depidxes.size())
			{
				MaxlengthNode = key;
				Maxlength = Depidxes.size();
			}
			
			
			for (int i = 0; i < Depidxes.size(); i++)
			{
//				System.out.println(key + " -> " + Depidxes.get(i));
				gp.addVertex(key);
				gp.addVertex(Depidxes.get(i));
				gp.addEdge(key, Depidxes.get(i));
			}
//			System.out.println("====================================");
		}
		
		System.out.println("MaxLength Node : " + MaxlengthNode + " Size : " + Maxlength);
		
		return gp;
	}

	private static void PrintDotGraph()
	{
		// JM Auto-generated method stub
		JSONArray PairInfo = ReadPairInfo();

		JSONObject Pair = (JSONObject) PairInfo.get(0);
		JSONObject Depen = (JSONObject) Pair.get("Dependency");

		String strGraph = "digraph G {";
		strGraph += "\n";

		Set<String> keys = Depen.keySet();

		for (String key : keys)
		{
			JSONObject depEntry = (JSONObject) Depen.get(key);
			JSONArray Depidxes = (JSONArray) depEntry.get("DepIdxes");
			for (int i = 0; i < Depidxes.size(); i++)
			{
				System.out.println(key + " -> " + Depidxes.get(i));
				strGraph += key + " -> " + Depidxes.get(i) + ";";
				strGraph += "\n";
			}
			System.out.println("====================================");
		}
		strGraph += "}";

		System.out.println("Graph");
		System.out.println(strGraph);
	}

	private static JSONArray ReadPairInfo()
	{
		String JsonPath = "D:\\new_extractocol\\src\\soot-infoflow-android\\SourcesAndSinksPairing\\pair.json";
		JSONArray Pairinfo = null;
		JSONParser parser = new JSONParser();
		JSONArray Responses = null;

		Object obj;
		try
		{
			obj = parser.parse(new FileReader(JsonPath));
			Pairinfo = (JSONArray) obj;
			return Pairinfo;
		} catch (IOException | ParseException e)
		{
			// JM Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
}
