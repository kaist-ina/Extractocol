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
import extractocol.common.outputs.backendOutputHelper.ResponseInfoEntry;
import extractocol.common.pairing.SemanticAppliedEntry;
import extractocol.common.pairing.SemanticAppliedList;
import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;


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
