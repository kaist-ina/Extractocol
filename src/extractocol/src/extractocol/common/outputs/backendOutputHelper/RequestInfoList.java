package extractocol.common.outputs.backendOutputHelper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import extractocol.Constants;
import extractocol.backend.request.heapManagement.HeapTreeNode;
import extractocol.common.pairing.PairingDPEntry;
import extractocol.common.pairing.SemanticAppliedEntry;
import extractocol.common.pairing.SemanticAppliedList;
import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;


public class RequestInfoList
{
	/*public static ArrayList<RequestInfoEntry> lstRequestInfo = new ArrayList<RequestInfoEntry> ();
	*//**
	 * Save Request Information for each URLs.
	 * @author : Jeongmin Kim 
	 * @version
	 * @param url specific url signature
	 *//*
	
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
	
	*//**
	 * To iteratively handle each heap object.
	 * @author Jeongmin Kim 
	 * @version 1.00
	 * @param PrintTreeIdx : Tree index number for Debugging mode.
	 * @param handler : a task for each node.
	 *//*
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
	}*/
}
