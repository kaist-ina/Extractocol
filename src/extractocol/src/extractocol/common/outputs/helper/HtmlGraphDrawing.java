package extractocol.common.outputs.helper;

import java.io.*;
import java.util.ArrayList;

import extractocol.Constants;
import extractocol.common.outputs.backendOutputHelper.ReqRespInfo;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;

public class HtmlGraphDrawing {
	static String BasicPath = "../../graph_library";

	/*
		Author : Jeongmin
		Purpose: debugging.
		Comment: To draw block graph as a html.
	 */
	public static void DrawingBlockGraph(SootMethod sm)
	{
		// The objects related to file handling
		File output = new File(Constants.getBasePath() + "/" + "Blockgraph.html");
		BufferedWriter bw = null;
		BufferedReader br = null;
		String line = null;

		// The block graph.
		BriefBlockGraph Bg = new BriefBlockGraph(sm.getActiveBody());

		System.out.println("[Extractocol]: Drawing blockgraph...");
		try
		{
			StringBuilder sb = new StringBuilder();

			//Building blockgraph data in order that vs library can consume it.
			for (Block b : Bg.getBlocks())
			{
				sb.append("{id:" + b.getIndexInMethod() + ", label: 'block" + b.getIndexInMethod() + "'},\n");
			}

			bw = new BufferedWriter(new FileWriter(output));
			bw = ReadPartofFile(bw,"begin.html");

			//Writing nodes into bw.
			bw.write(sb.toString());

			//Read Middle
			bw = ReadPartofFile(bw,"middle.html");

			//Writing edges into bw.
			sb = new StringBuilder();
			for (Block b : Bg.getBlocks())
			{
				for (Block outer: b.getSuccs())
				{
					sb.append("{from:" + b.getIndexInMethod() + ", to: " + outer.getIndexInMethod() + ", arrows:'to'},\n");
				}
			}
			bw.write(sb.toString());


			// Read Last
			bw = ReadPartofFile(bw, "end.html");


			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("[Extractocol]: finish drawing.");
		return;
	}

	public static BufferedWriter ReadPartofFile(BufferedWriter bw, String filename)
	{
		BufferedReader br = null;
		String line = null;

		// 1. Read Begin part
		try{
			br = new BufferedReader(new FileReader(new File(BasicPath, filename)));
			while((line = br.readLine()) != null){
				bw.write(line);
				bw.newLine();
			}
		} catch (IOException e){
			e.printStackTrace();
		}finally {
			if(br != null) try {br.close(); } catch (IOException e) {}
		}

		return bw;
	}
	
	public static void Drawing(ArrayList<ReqRespInfo> list, int[][] depGraph, String resultFile){
		int graphSize = depGraph.length;
		
		File output = new File(resultFile);
		BufferedWriter bw = null;
		BufferedReader br = null;
		String line = null;
		
		try{
			bw = new BufferedWriter(new FileWriter(output));
			
			// 1. Read Begin part
			try{
				br = new BufferedReader(new FileReader(new File(BasicPath, "begin.html")));
				while((line = br.readLine()) != null){
					bw.write(line);
					bw.newLine();
				}
			} catch (IOException e){
				e.printStackTrace();
			}finally {
	            if(br != null) try {br.close(); } catch (IOException e) {}
	        }
			
			// 2. Print nodes
			for(int i = 0; i < graphSize; i++){
				if(list.get(i).reqie.getSignature() != null){
					line = "{id: " + i + ", label: 'T" + i + "'},";
					bw.write(line);
					bw.newLine();
				}
			}
			
			// 3. Read Middle part
			try{
				br = new BufferedReader(new FileReader(new File(BasicPath, "middle.html")));
				while((line = br.readLine()) != null){
					bw.write(line);
					bw.newLine();
				}
			} catch (IOException e){
				e.printStackTrace();
			}finally {
	            if(br != null) try {br.close(); } catch (IOException e) {}
	        }

			// 4. Print edges
			for(int i = 0; i < graphSize; i++)
				for(int j = 0; j < graphSize; j++){
					if(depGraph[i][j] == 1){
						line = "{from: " + i + ", to: " + j + ", arrows:'to'},";
						bw.write(line);
						bw.newLine();
					}
				}
			
			// 5. Read End part
			try{
				br = new BufferedReader(new FileReader(new File(BasicPath, "end.html")));
				while((line = br.readLine()) != null){
					bw.write(line);
					bw.newLine();
				}
			} catch (IOException e){
				e.printStackTrace();
			}finally {
	            if(br != null) try {br.close(); } catch (IOException e) {}
	        }
			
			
		}catch (IOException e){
			e.printStackTrace();
		}finally{
			if(bw != null) try {bw.close(); } catch (IOException e) {}
		}
	}
}
