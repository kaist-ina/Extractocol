package extractocol.frontend.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;

import extractocol.Constants;
import extractocol.backend.request.helper.CFGSerializer.ICFG_CASE;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntryTable;
import extractocol.frontend.basic.ExtractocolLogger;
import extractocol.frontend.output.basic.DPContainer;
import extractocol.frontend.output.basic.EPContainer;
import extractocol.tester.Frontend;
import soot.Unit;

public class TaintResultContainer {
	static List<DPContainer> dpList = new ArrayList<DPContainer>();
	
	/** Save/categorize the refined results with respect to DP (Demarcation Point) 
	 * 
	 * @param _dpMethod DP method
	 * @param _dpStmt DP statement
	 * @param _ep EP (Entry point)
	 * @param _taintMethodSet taint method set
	 */
	public static void addTaintResult(String _dpMethod, String _dpStmt, int _dpStmtHash, String _ep, List<String> _taintMethodSet){
		boolean doesContain = false;
		
		// Check whether there is an entry whose DP is same with _dpMethod & _dpStmt
		for(DPContainer dpc: dpList){
			if(dpc.isSameWith(_dpMethod, _dpStmt, _dpStmtHash)){
				dpc.addEP(_ep, _taintMethodSet);
				doesContain = true;
				break;
			}
		}
		
		// If it is not, allocate new DP container and save the result
		if(!doesContain){
			DPContainer newDPC = new DPContainer(_dpMethod, _dpStmt, _dpStmtHash, _ep, _taintMethodSet);
			dpList.add(newDPC);
		}
	}
	
	public static void clearDPList() {
		if(dpList != null) {
			for(DPContainer e: dpList)
				e.clear();
			
			dpList.clear();
		}
	}
	
	public static void Print(){
		Print(dpList);
	}
	
	/** Print list of DPContainer
	 * 
	 * @param _dpList list of DPContainer
	 * @return Total number of EPs
	 */
	public static int Print(List<DPContainer> _dpList){
		int totepCnt = 0;
		System.out.println("\n\n\n=============================================================\n\n\n");
		
		for(int i = 0; i < _dpList.size(); i++){
			DPContainer dpc = _dpList.get(i);
			
			System.out.println("\n(" + (i+1) + "/" + _dpList.size() + ") Printing ...");
			System.out.println("+ DP Stmt : " + dpc.getDPStmt());
			System.out.println("+ DP SootMethod : " + dpc.getDPMethod());
			
			for(int j = 0; j < dpc.getEPList().size(); j++){
				totepCnt++;
				
				EPContainer epc = dpc.getEPList().get(j);
				
				System.out.println();
				System.out.println("\t" + (i+1) + "-" + (j+1) +"th EntryPoint :");
				System.out.println("\t\t+ EP method: \n\t\t\t+ " + epc.getEP() + "\n");
				System.out.println("\t\t+ Taint Methods : ");
				
				for(String tm: epc.getTaintMethodSet())
					System.out.println("\t\t\t++ " + tm);
			}
			
			System.out.println();
		}
		
		return totepCnt;
	}
	
	public static void Serialization(ICFG_CASE icfg_case)
	{
		String path;
		
		switch(icfg_case){
		case BACKWARD: path = Frontend.getCurrentDPListPath(true); break;
		case FORWARD: path = Frontend.getCurrentDPListPath(false); break;
		case HEAP: path = getHeapPath(); break;
		case ARG: path = getArgPath(); break;
		default: return;
		}
		
		Serialization(path);
	}
	
	public static void Serialization(String path){
		Serialization(path, dpList);
	}
	
	public static void Serialization(String path, List<DPContainer> _dpList){
		try{
			FileOutputStream fos = new FileOutputStream(path);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(_dpList);
			fos.close();
			oos.flush();
			oos.close();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public static List<DPContainer> Deserialization(ICFG_CASE icfg_case)
	{
		String path;
		
		switch(icfg_case){
		case BACKWARD: path = getBackwardPath(); break;
		case FORWARD: path = getForwardPath(); break;
		case HEAP: path = getHeapPath(); break;
		case ARG: path = getArgPath(); break;
		default: return null;
		}
		
		return Deserialization(path);
	}
	
	@SuppressWarnings("unchecked")
	public static List<DPContainer> Deserialization(String path){
		if(!new File(path).exists()) {
			ExtractocolLogger.Log("There is no file: " + path);
			return null;
		}
		
		List<DPContainer> result = null;
		
		try{
			FileInputStream fis = new FileInputStream(path);
			ObjectInputStream ois = new ObjectInputStream(fis);
			result = (List<DPContainer>) ois.readObject();
			ois.close();
			fis.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static String getBackwardPath() { return Constants.getFrontendOutputDirPath() + "/DPList.backward"; }
	public static String getForwardPath() { return Constants.getFrontendOutputDirPath() + "/DPList.forward"; }
	private static String getHeapPath() { return Constants.getTempDirPath() + "/DPList.heap"; }
	private static String getArgPath() { return Constants.getTempDirPath() + "/DPList.arg"; }
	
	public static boolean doesBackwardDPListExist() {
		return doesExist(getBackwardPath());
	}
	private static boolean doesExist(String path) {
		return new File(path).exists();
	}
}
