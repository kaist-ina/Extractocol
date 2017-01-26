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
import Extractocol.BufferExtractor_Request.HeapManagement.HeapEntry;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapTreeNode;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapEntry.SOURCE_TYPE;
import Extractocol.Pairing.PairingDPEntry;
import Extractocol.Pairing.SemanticAppliedEntry;
import Extractocol.Pairing.SemanticAppliedList;


public class RequestInfoList
{
	public static ArrayList<RequestInfoEntry> lstRequestInfo = new ArrayList<RequestInfoEntry> ();
	/**
	 * Save Request Information for each URLs.
	 * @author : Jeongmin Kim 
	 * @version
	 * @param url specific url signature
	 */
	
	public static void AddReqInfoEntry(RequestInfoEntry entry){
		lstRequestInfo.add(entry);
	}
	
	public static void AddPairingInfo(String url, SemanticAppliedList semanticAppliedList, String EP, String DP)
	{
		if (!contains(EP, DP)) // Is it really required?
		{
			RequestInfoEntry newRie = new RequestInfoEntry(url, EP, DP);
			lstRequestInfo.add(newRie);
			
			if(url != null)
				newRie.addHeapTreeRoot();
			
			if (Constants.isDebug)
				printEntireUrl(newRie);
		}else{
			int idx = getIndexof(EP, DP);
			boolean trigger = false;
			
			if(lstRequestInfo.get(idx).getSignature() == null) 
				trigger = true;
			
			lstRequestInfo.get(idx).setSignature(url);
			
			if(trigger) 
				lstRequestInfo.get(idx).addHeapTreeRoot();
			
			lstRequestInfo.get(idx).getSemanticAppliedList().setEpList(semanticAppliedList.getEpList());
			
		}
	}	
	
	/*public static boolean addJSONKeyintoHeap(String heap, String key){
		int idx = getIndexof(Constants.CurrentEP, Constants.CurrentParentMethod);
		
		if(idx < 0)
			return false;
		
		lstRequestInfo.get(idx).heapTable.addJSONKey(heap, key);
		return true;
	}
	
	public static boolean addJSONKeyListintoHeap(String heap, ArrayList<String> keyList){
		int idx = getIndexof(Constants.CurrentEP, Constants.CurrentParentMethod);
		
		if(idx < 0)
			return false;
		
		lstRequestInfo.get(idx).heapTable.addJSONKeyList(heap, keyList);
		return true;
	}
	
	public static boolean addConstantValueintoHeap(String heap, String value){
		return addHeapEntry(heap, value, SOURCE_TYPE.CONSTANT, Constants.CurrentEP, Constants.CurrentParentMethod);
	}
	
	public static boolean addHeapValueintoHeap(String dstHeapName, String srcHeapName){
		return addHeapEntry(dstHeapName, srcHeapName, SOURCE_TYPE.HEAP, Constants.CurrentEP, Constants.CurrentParentMethod);
	}
	
	private static boolean addHeapEntry(String heap, String value, SOURCE_TYPE stype, String EP, String CurrentParentMethod){
		int idx = getIndexof(EP, CurrentParentMethod);
		
		if(idx < 0) 
			return false;
		
		lstRequestInfo.get(idx).heapTable.addValueEntrytoHeapTable(heap, value, stype);
		return true;
	}
	
	public static void addHeapEntryListofHeap(String heap, ArrayList<HeapEntry> EntryList){
		int idx = getIndexof(Constants.CurrentEP, Constants.CurrentParentMethod);
		if(idx < 0)
			return;
		
		lstRequestInfo.get(idx).heapTable.AddHeapEntryListTo(heap, EntryList);
	}
	
	public static void setHeapEntryListofHeap(String heap, ArrayList<HeapEntry> EntryList){
		int idx = getIndexof(Constants.CurrentEP, Constants.CurrentParentMethod);
		if(idx < 0)
			return;
		
		lstRequestInfo.get(idx).heapTable.OverwriteHeapEntryListTo(heap, EntryList);
	}
	
	public static ArrayList<HeapEntry> getHeapEntryListofHeap(String heap){
		int idx = getIndexof(Constants.CurrentEP, Constants.CurrentParentMethod);
		
		return (idx < 0)? null : lstRequestInfo.get(idx).heapTable.getHeapEntryList(heap);
	}*/
	
	private static int getIndexof(String EP, String DP){
		for (RequestInfoEntry rie : lstRequestInfo){
			if(rie.getEPorSPMethod().equals(EP) && rie.getDPMethod().equals(DP))
				return lstRequestInfo.indexOf(rie);
		}
		return -1;
	}
	
	

	public static void printEntireUrl(RequestInfoEntry newRie)
	{
		System.out.println("== Printing Heap Object Tree of URL String ==");
		Collection<HeapTreeNode> list = newRie.HeapTree.inOrderTraversal();
		System.out.println("Entire URL : " + newRie.getSignature());
		System.out.println(list.iterator().next().listURL);
	}
		
	public static boolean contains(String EP, String DP)
	{
		return getIndexof(EP, DP) != -1;
	}
	
	public static void Serialize(ArrayList<RequestInfoEntry> target)
	{
		Kryo kryo = new Kryo();
		Output output;
		try
		{
			output = new Output(new FileOutputStream(Constants.RequestInfoPath()));
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
		lstRequestInfo = Deserialize();
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<RequestInfoEntry> Deserialize()
	{
		Kryo kryo = new Kryo();
		Input input;
		ArrayList<RequestInfoEntry> result = null;
		try
		{
			input = new Input(new FileInputStream(Constants.RequestInfoPath()));
			result = (ArrayList<RequestInfoEntry>) kryo.readObject(input, ArrayList.class);
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
		public void execute(HeapTreeNode node, RequestInfoEntry entry);
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
	public static void ReqListIteration(boolean PrintTreeIdx, ConcreteTask handler) {
		int treecount = 0;
		for (RequestInfoEntry entry : lstRequestInfo)
		{
			if (PrintTreeIdx)
				System.out.println("Tree" + treecount++);
			
			Collection<HeapTreeNode> tree = entry.HeapTree.inOrderTraversal();
			for (HeapTreeNode node : tree)
			{
				handler.execute(node, entry);
			}
		}
	}
}
