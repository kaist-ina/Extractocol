package extractocol.backend.response.helper;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import extractocol.Constants;
import extractocol.backend.request.helper.ExtractocolLoopFinder;
import extractocol.common.MD5;
import extractocol.common.tools.highScaleLib.NonBlockingHashMap;
import extractocol.frontend.basic.ExtractocolLogger;
import soot.Body;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BriefBlockGraph;

public class BlockTravelerHelper {
	private static NonBlockingHashMap<String,boolean[][]> backEdgeMap = new NonBlockingHashMap<String,boolean[][]>();
	
	/**********************************************************************/
	/***                   APIs for Block Alignment                     ***/
	/**********************************************************************/
	/** Wrapper function of block aligner
	 * 
	 * @param rootBlock Root block (starting point)
	 * @return Aligned block list
	 */
	public static List<Block> BlockAlignerWrapper(Block rootBlock){
		List<Block> sortedBlockList = new ArrayList<Block>();
		sortedBlockList.add(rootBlock);
		
		// Order block list
		for(Block successor: rootBlock.getSuccs())
			BlockAligner(successor, sortedBlockList);
		
		return sortedBlockList;
	}
	
	/** Add blocks in order
	 * 
	 * @param block Current block
	 * @param sortedBlockList List of blocks that has been already included
	 */
	private static void BlockAligner(Block block, List<Block> sortedBlockList){
		// 1. Find untreated predecessors
		List<Block> untreatedPreds = untreatedPredsFinderWrapper(block, sortedBlockList);

		// 2. Handle untreated predecessors
		for (Block untreatedPred : untreatedPreds)
			BlockAligner(untreatedPred, sortedBlockList);

		// 3. Add itself
		if (!sortedBlockList.contains(block))
			sortedBlockList.add(block);

		// 4. Handle successors
		for (Block succOfsucc : block.getSuccs())
			if (!sortedBlockList.contains(succOfsucc))
				BlockAligner(succOfsucc, sortedBlockList);
	}
	
	/** Get untreated predecessors
	 * 
	 * @param block Current block
	 * @param sortedBlockList List of blocks that has been already included
	 * @return List of untreated predecessors
	 */
	private static List<Block> untreatedPredsFinderWrapper(Block block, List<Block> sortedBlockList){
		// untreatedPreds: predecessor blocks that were not treated and need to be treated now
		List<Block> untreatedPreds = new ArrayList<Block>(); 
		// blockStack : To detect a block that has appeared before
		List<Block> blockStack = new ArrayList<Block>();
		
		blockStack.add(block);
		
		// No need to search untreated predecessors if the current block has been already treated (included in the list) 
		if(sortedBlockList.contains(block))
			return untreatedPreds;
		
		// Search untreated predecessors
		for (Block predecessor : block.getPreds())
			// Add a predecessor if the predecessor is not treated
			if (untreatedPredsFinder(predecessor, blockStack, sortedBlockList))
				untreatedPreds.add(predecessor);
		
		return untreatedPreds;
	}
	
	/** Check whether a block must be treated now or not
	 * 
	 * @param block Target block
	 * @param blockStack stack of blocks that has appeared before
	 * @param sortedBlockList List of blocks that has been already included
	 * @return True if the block must be treated now
	 */
	private static boolean untreatedPredsFinder(Block block, List<Block> blockStack, List<Block> sortedBlockList){
		if(sortedBlockList.contains(block))
			return false;
		
		for(Block predecessor: block.getPreds()){
			// Not need to treat the block if it has appeared before
			if(blockStack.contains(predecessor))
				return false;
			
			// If it did not appear before and is not included in the block list 
			if(!sortedBlockList.contains(predecessor)){
				List<Block> bStack = new ArrayList<Block>(blockStack);
				bStack.add(predecessor);
				
				// need to check its predecessors recursively
				if(!untreatedPredsFinder(predecessor, bStack, sortedBlockList))
					return false;
			}
		}
		
		return true;
	}
	
	/**********************************************************************/
	/***                 APIs for Serialization Common                  ***/
	/**********************************************************************/
	private static boolean doesEdgeFileExist(String sootMethod){
		File file = new File(getEdgeFilePath(sootMethod));
		return file.exists();
	}
	
	private static boolean doesBackEdgeFileExist(String hashKey){
		File file = new File(getBackEdgeFilePath(hashKey));
		return file.exists();
	}
	
	private static boolean doesManiEdgeFileExist(String sootMethod){
		File file = new File(getManiEdgeFilePath(sootMethod));
		return file.exists();
	}
	
	private static String getEdgeFilePath(String sootMethod){
		return getEdgeTablePath() + "/" + getRefinedFileName(sootMethod) + ".Edge";
	}
	
	private static String getBackEdgeFilePath(String hashKey){
		return getEdgeTablePath() + "/" + hashKey + ".backEdge";
	}
	
	private static String getManiEdgeFilePath(String sootMethod){
		return getEdgeTablePath() + "/" + getRefinedFileName(sootMethod) + ".maniEdge";
	}
	
	private static String getEdgeTablePath()
	{
		String path = Constants.getBackendOutputDirPath() + "/EdgeTables";
		
		File serializationDir = new File(path);
		
		if (!serializationDir.exists())
			serializationDir.mkdir();
		
		return path;
	}
	
	private static String getRefinedFileName(String sootMethod)
	{
		return MD5.getMD5(sootMethod);
	}
	
	public static Integer[][] getEdgeTable(BriefBlockGraph bg, String sootMethod){
		if(doesEdgeFileExist(sootMethod))
			return DeserializeEdge(sootMethod);
		else{
			Integer[][] edgeTable = new Integer[bg.size()][bg.size()];
			for(int i = 0; i < bg.size(); i++)
				for(int j = 0; j < bg.size(); j++)
					edgeTable[i][j] = 0;
			
			CalcEdgeTable(edgeTable, bg);
			
			return edgeTable;
		}
	}
	
	public static Integer[][] ManipulateBackEdge_Wrapper(Integer[][] edgeTable, boolean[][] BackEdge, BriefBlockGraph bg, String sootMethod)
	{
		if(doesEdgeFileExist(sootMethod) && doesManiEdgeFileExist(sootMethod)){
			edgeTable = DeserializeEdge(sootMethod); 
			return DeserializeManiEdge(sootMethod);
		}else{
			Integer[][] rtn = ManipulateBackEdge(edgeTable, BackEdge, bg);

			SerializeManiEdge(rtn, sootMethod);
			SerializeEdge(edgeTable, sootMethod);
				
			return rtn;
		}
	}
	
	/**********************************************************************/
	/***            Serialization of Manipulated-Edge Table             ***/
	/**********************************************************************/
	private static Integer[][] DeserializeManiEdge(String sootMethod)
	{
		Kryo kryo = new Kryo();
		Input input;
		Integer[][] result = null;
		try
		{
			input = new Input(new FileInputStream(getManiEdgeFilePath(sootMethod)));
			result = (Integer[][]) kryo.readObject(input, Integer[][].class);
			input.close();
			
			return result;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private static void SerializeManiEdge(Integer[][] edgeTable, String sootMethod){
		Kryo kryo = new Kryo();
		Output output;
		try
		{
			output = new Output(new FileOutputStream(getManiEdgeFilePath(sootMethod)));
			kryo.writeObject(output, edgeTable);
			output.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	/**********************************************************************/
	/***                Serialization of Back-Edge Table                ***/
	/**********************************************************************/
	private static boolean[][] AllocBackEdgeTable(int size){
		return new boolean[size][size];
	}
	
	public static void DeserializeBackEdge()
	{
		ExtractocolLogger.Log("Deserializing backEdge tables ...");
		Kryo kryo = new Kryo();
		Input input;
		boolean[][] BackEdge;
		String hashKey;

		File tempDir = new File(getEdgeTablePath());
		for (File file : tempDir.listFiles()) {
			if(!file.getName().contains("backEdge"))
				continue;
			
			hashKey = file.getName().split("\\.")[0];
			
			try {
				input = new Input(new FileInputStream(file.getPath()));
				BackEdge = (boolean[][]) kryo.readObject(input, boolean[][].class);
				input.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				continue;
			}
			
			putBackEdge(hashKey, BackEdge);
		}
	}
	
	public static void SerializeBackEdge(){
		Kryo kryo = new Kryo();
		Output output;
		
		for(Entry<String,boolean[][]> entry : backEdgeMap.entrySet()) {
			String hashKey = entry.getKey();
			boolean[][] BackEdge = entry.getValue();
			
			if(doesBackEdgeFileExist(hashKey))
				continue;
			
			try
			{
				output = new Output(new FileOutputStream(getBackEdgeFilePath(hashKey)));
				kryo.writeObject(output, BackEdge);
				output.close();
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static boolean isBackEdge(boolean[][] BackEdge, int from, int to){
		return BackEdge[from][to];
	}
	
	private static void CalcBackEdge(boolean[][] BackEdge, BriefBlockGraph Bg){
		int size = Bg.size();
		
		for(int i = 0; i < size; i ++){
			for(int j = 0; j < size; j++){
				BackEdge[i][j] = isBackEdge(Bg, i, j);
			}
		}
	}
	
	public static boolean[][] GetBackEdgeTable(String sootMethod, BriefBlockGraph Bg){
		boolean[][] BackEdge = getBackEdge(sootMethod);
		if(BackEdge != null && BackEdge.length == Bg.size())
			return BackEdge;
		
		BackEdge = new boolean[Bg.size()][Bg.size()]; // Allocation
		CalcBackEdge(BackEdge, Bg); // Calculuation
		
		putBackEdge(BackEdge, sootMethod);

		return BackEdge;
	}
	
	private static boolean[][] getBackEdge(String sootMethod){
		return backEdgeMap.get(getRefinedFileName(sootMethod));
	}
	
	private static void putBackEdge(boolean[][] BackEdge, String sootMethod) {
		backEdgeMap.put(getRefinedFileName(sootMethod), BackEdge);
	}
	
	private static void putBackEdge(String hashKey, boolean[][] BackEdge) {
		backEdgeMap.put(hashKey, BackEdge);
	}
	
	/**********************************************************************/
	/***                Serialization of Edge Table                ***/
	/**********************************************************************/
	private static Integer[][] DeserializeEdge(String sootMethod)
	{
		Kryo kryo = new Kryo();
		Input input;
		Integer[][] result = null;
		try
		{
			input = new Input(new FileInputStream(getEdgeFilePath(sootMethod)));
			result = (Integer[][]) kryo.readObject(input, Integer[][].class);
			input.close();
			
			return result;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private static void SerializeEdge(Integer[][] edgeTable, String sootMethod){
		Kryo kryo = new Kryo();
		Output output;
		try
		{
			output = new Output(new FileOutputStream(getEdgeFilePath(sootMethod)));
			kryo.writeObject(output, edgeTable);
			output.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	/**********************************************************************/
	/***                          Basic APIs                            ***/
	/**********************************************************************/
	public static boolean areBackEdgeTablesSame(Integer[][] Table1, Integer[][] Table2, int size){
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++)
				if(Table1[i][j] != Table2[i][j])
					return false;
		
		return true;
	}
	
	public static void CopyTable(Integer[][] dest, Integer[][] src, int size){
		for(int i = 0; i < size; i++)
			for(int j = 0; j < size; j++)
				dest[i][j] = src[i][j];
	}
	
	
	public static int getBlockNum(BriefBlockGraph Bg, Unit ut)
	{
		for (Block b : Bg.getBlocks())
		{

			Iterator<Unit> IterUnit = b.iterator();

			while (IterUnit.hasNext())
			{
				Unit in = IterUnit.next();
				if (in.equals(ut))
				{
					return Bg.getBlocks().indexOf(b);
				}
			}
		}
		return -1;
	}


	public static void CalcEdgeTable(Integer[][] edgeTable, BriefBlockGraph bg2)
	{
		for (Block b : bg2)
		{
			for (Block outedge : bg2.getSuccsOf(b))
			{
				edgeTable[bg2.getBlocks().indexOf(b)][bg2.getBlocks().indexOf(outedge)] = 1;
			}
		}
	}
	
	public static void CalcEdgeTableDouble(Integer[][] edgeTable1, Integer[][] edgeTable2, BriefBlockGraph bg2)
	{
		for (Block b : bg2)
		{
			for (Block outedge : bg2.getSuccsOf(b))
			{
				edgeTable1[bg2.getBlocks().indexOf(b)][bg2.getBlocks().indexOf(outedge)] = 1;
				edgeTable2[bg2.getBlocks().indexOf(b)][bg2.getBlocks().indexOf(outedge)] = 1;
			}
		}
	}

	public static Integer[][] ManipulateBackEdge(Integer[][] edgeTable, boolean[][] BackEdge, BriefBlockGraph bg)
	{
		Integer[][] rtn = new Integer[bg.getBlocks().size()][bg.getBlocks().size()];

		for (int i = 0; i < bg.size(); i++)
		{
			for (int j = 0; j < bg.size(); j++)
			{
				rtn[i][j] = 0;
			}
		}

		for (int i = 0; i < bg.getBlocks().size(); i++)
		{
			for (int j = 0; j < bg.getBlocks().size(); j++)
			{
				if (isBackEdge(BackEdge, i, j))
				{

					for (int k = 0; k < bg.getBlocks().size(); k++)
					{
						if (edgeTable[j][k] == 1 && k != i)
						{
							edgeTable[i][k] = 1;
							rtn[i][k] = 1;
							break;
						}
					}
					edgeTable[i][j] = 0;
				}
			}
		}
		return rtn;
	}	
	
	public static boolean isManiEdge(Integer[][] maniEdge, int blockNumber, int bgsize)
	{
		for (int i = 0; i < bgsize; i++)
		{
			if (maniEdge[i][blockNumber] == 1)
				return true;
		}
		return false;
	}

	
	public static boolean isBackEdge(BriefBlockGraph Bg, int from, int to)
	{
		Body b = Bg.getBody();

		ExtractocolLoopFinder loopFinder = new ExtractocolLoopFinder();
		Set<List<Stmt>> loops = loopFinder.getLoops(b);
		// Set<List<StmtLine>> loops = loopFinder.getLoops(b);

		for (List<Stmt> lp : loops)
		{
			if (lp.size() < 2)
			{
				return false;
			}
			if (isBlockContainsStmt(Bg, from, lp.get(lp.size() - 2)) && isBlockContainsStmt(Bg, to, lp.get(lp.size() - 1)))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean BlockSelector(int bidx, BriefBlockGraph Bg, String sig, Integer[][] EdgeEntry, Integer[][] maniEdge, boolean[][] BackEdge)
	{
		for (int i = 0; i < Bg.getBlocks().size(); i++)
		{
			if (EdgeEntry[i][bidx] == 1 && maniEdge[i][bidx] == 1)
				return true;
			else if (EdgeEntry[i][bidx] == 1 && (!isBackEdge(BackEdge, i, bidx)))
			{
				return false;
			}
		}
		return true;
	}
	
	public static boolean isBlockContainsStmt(BriefBlockGraph Bg, int index, Stmt stmt)
	{
		for (Iterator<Unit> iter = Bg.getBlocks().get(index).iterator(); iter.hasNext();)
		{
			Unit ut = iter.next();
			if (ut.toString().equals(stmt.toString()))
			{
				return true;
			}
		}
		return false;
	}
}
