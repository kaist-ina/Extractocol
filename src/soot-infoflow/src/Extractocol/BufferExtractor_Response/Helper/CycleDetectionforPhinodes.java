package Extractocol.BufferExtractor_Response.Helper;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.*;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;

public class CycleDetectionforPhinodes
{
	DirectedGraph<String, DefaultEdge> directedGraph = new DefaultDirectedGraph<String, DefaultEdge> (DefaultEdge.class);
	
	public void addVertaxAndEdge(String src, String dst)
	{
		directedGraph.addVertex(src);
		directedGraph.addVertex(dst);
		directedGraph.addEdge(src, dst);
	}
	
	public boolean HasCycle()
	{
		CycleDetector cd = new CycleDetector<>(directedGraph);
		return cd.detectCycles();
	}
}
