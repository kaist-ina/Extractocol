package Extractocol.Outputs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import Extractocol.Constants;
import Extractocol.Pairing.SemanticAppliedList;
import Extractocol.Pairing.SemanticAppliedEntry;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapEntry;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapTreeNode;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapEntry.SOURCE_TYPE;
import Extractocol.Outputs.ResponseInfoEntry;


public class ResponseInfoList {
	public static ArrayList<ResponseInfoEntry> lstResponseInfo = new ArrayList<ResponseInfoEntry> ();
	
	public static void AddRespInfoEntry(ResponseInfoEntry entry){
		lstResponseInfo.add(entry);
	}
	public static void AddPairingInfo(String body, SemanticAppliedList sal, String DP, String DPStmt)
	{
		if (!contains(DP, DPStmt)){ // BK Is it really required?
			ResponseInfoEntry newRie = new ResponseInfoEntry(body, sal);
			lstResponseInfo.add(newRie);
		}else{
			int idx = getIndexof(DP, DPStmt);
			lstResponseInfo.get(idx).setSignature(body);
			lstResponseInfo.get(idx).getSemanticAppliedList().setEpList(sal.getEpList());
		}
	}	
	
	
	/*public static boolean addJSONKeyintoHeap(String heap, String key){
		int idx = getIndexof(Constants.CurrentEP, Constants.DPStmt);
		
		if(idx < 0)
			return false;
		
		lstResponseInfo.get(idx).heapTable.addJSONKey(heap, key);
		return true;
	}
	
	public static boolean addJSONKeyListintoHeap(String heap, ArrayList<String> keyList){
		int idx = getIndexof(Constants.CurrentEP, Constants.DPStmt);
		
		if(idx < 0)
			return false;
		
		lstResponseInfo.get(idx).heapTable.addJSONKeyList(heap, keyList);
		return true;
	}
	
	public static boolean addConstantValueintoHeap(String heap, String value){
		return addHeapEntry(heap, value, SOURCE_TYPE.CONSTANT, Constants.CurrentEP, Constants.DPStmt);
	}
	
	public static boolean addHeapValueintoHeap(String dstHeapName, String srcHeapName){
		return addHeapEntry(dstHeapName, srcHeapName, SOURCE_TYPE.HEAP, Constants.CurrentEP, Constants.DPStmt);
	}
	
	private static boolean addHeapEntry(String heap, String value, SOURCE_TYPE stype, String EP, String DPStmt){
		int idx = getIndexof(EP, DPStmt);
		
		if(idx < 0) 
			return false;
		
		lstResponseInfo.get(idx).heapTable.addValueEntrytoHeapTable(heap, value, stype);
		return true;
	}
	
	public static void addHeapEntryListofHeap(String heap, ArrayList<HeapEntry> EntryList){
		int idx = getIndexof(Constants.CurrentEP, Constants.DPStmt);
		
		if(idx < 0)
			return;
		
		lstResponseInfo.get(idx).heapTable.AddHeapEntryListTo(heap, EntryList);
	}
	
	public static void setHeapEntryListofHeap(String heap, ArrayList<HeapEntry> EntryList){
		int idx = getIndexof(Constants.CurrentEP, Constants.DPStmt);
		
		if(idx < 0)
			return;
		
		lstResponseInfo.get(idx).heapTable.OverwriteHeapEntryListTo(heap, EntryList);;
	}
	
	public static ArrayList<HeapEntry> getHeapEntryListofHeap(String heap){
		int idx = getIndexof(Constants.CurrentEP, Constants.DPStmt);
		
		return (idx < 0)? null : lstResponseInfo.get(idx).heapTable.getHeapEntryList(heap);
	}*/
	
	private static int getIndexof(String DP, String DPStmt){
		for (ResponseInfoEntry rie : lstResponseInfo){
			//if(rie.getEPorSPMethod().equals(EP) && rie.getDPMethod().equals(DP))
			if(rie.getDPStmt().equals(DPStmt) && rie.getDPMethod().equals(DP)) // temporarily
				return lstResponseInfo.indexOf(rie);
		}
		return -1;
	}

	

	public static void printEntireBody(ResponseInfoEntry newRie)
	{
		//System.out.println("== Printing Heap Object Tree of URL String ==");
		//Collection<HeapTreeNode> list = newRie.HeapTree.inOrderTraversal();
		System.out.println("Entire URL : " + newRie.getSignature());
		//System.out.println(list.iterator().next().listURL);
	}
	
	public static boolean contains(String DP, String DPStmt)
	{
		return getIndexof(DP, DPStmt) != -1;
	}
	
	public static boolean contains(String body)
	{
		for (ResponseInfoEntry rie : lstResponseInfo)
		{
			if (rie.equals(body))
				return true;
		}
		return false;
	}
	
	public static void Serialize(ArrayList<ResponseInfoEntry> target)
	{
		Kryo kryo = new Kryo();
		Output output;
		try
		{
			output = new Output(new FileOutputStream(Constants.ResponseInfoPath()));
			kryo.writeObject(output, target);
			output.close();
		}
		catch (FileNotFoundException e)
		{
			// JM Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void LoadFromFile()
	{
		lstResponseInfo = Deserialize();
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<ResponseInfoEntry> Deserialize()
	{
		Kryo kryo = new Kryo();
		Input input;
		ArrayList<ResponseInfoEntry> result = null;
		try
		{
			input = new Input(new FileInputStream(Constants.ResponseInfoPath()));
			result = (ArrayList<ResponseInfoEntry>) kryo.readObject(input, ArrayList.class);
			input.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public static interface Task
	{
		public void execute(HeapTreeNode node, ResponseInfoEntry entry);
	}
	
	public abstract static class ConcreteTask implements Task
	{
	}
	
	/**
	 * To iteratively handle each heap object.
	 * @author Jeongmin Kim 
	 * @version 1.00
	 * @param PrintTreeIdx : Tree index number for Debugging mode.
	 * @param handler : a task for each node.
	 */
	public static void RespListIteration(boolean PrintTreeIdx, ConcreteTask handler) {
		/*int treecount = 0;
		for (ResponseInfoEntry entry : lstResponseInfo)
		{
			if (PrintTreeIdx)
				System.out.println("Tree" + treecount++);
			
			Collection<HeapTreeNode> tree = entry.HeapTree.inOrderTraversal();
			for (HeapTreeNode node : tree)
			{
				handler.execute(node, entry);
			}
		}*/
	}
}
