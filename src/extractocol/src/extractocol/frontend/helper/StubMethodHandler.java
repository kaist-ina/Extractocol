package extractocol.frontend.helper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import extractocol.Constants;
import extractocol.frontend.basic.ExtractocolLogger;
import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.graph.Block;

/**
 * 
 * @author Byungkwon Choi
 *
 */
public class StubMethodHandler {
	/** stubMap contains whether a method contains "stub" only. */
	private static Map<String, Boolean> stubMap = new HashMap<String, Boolean>();
	
	/** Check whether the method contains "stub" only
	 * 
	 * @param m method name
	 * @return True if the method contains "stub" only
	 */
	public static boolean IsStubMethod(String m){
		if(m == null)
			return false;
		
		return stubMap.get(m);
	}
	
	/** Read stub method map from file
	 */
	public static void readStubMapFromFile(){
		stubMap = Deserialization();
	}
	
	/** Evaluate stubMethod map
	 * 
	 * @param smSet SootMethod set
	 */
	public static void EvaluateStubMethodMap(Collection<SootMethod> smSet){
		// Try to get stubMethod map from file
		readStubMapFromFile();
		
		if(stubMap != null){
			// Check whether the loaded stubMap contains all sootMethod signatures as key
			if(Checker(smSet)){
				ExtractocolLogger.Log("Get StubMethodMap from a file.");
				return;
			}
		}
		
		// Evaluate stubMethod map if there is no serialized file
		stubMap = new HashMap<String, Boolean>();
		for(SootMethod sm: smSet)
			DetermineWhetherMethodContainStub(sm, false);
		
		// Serialize stubMethod map to file 
		Serialization();
		
		ExtractocolLogger.Log("StubMethodMap has been evaluated and serialized.");
	}
	
	/** Check whether stubMap contains all sootMethod signatures as keys
	 * 
	 * @param smSet SootMethod set
	 * @return True if stubMap contains all sootmethod signatures as keys
	 */
	private static boolean Checker(Collection<SootMethod> smSet){
		if(stubMap == null)
			return false;
		
		for(SootMethod sm: smSet)
			if(stubMap.get(sm.toString()) == null)
				return false;
		
		return true;
	}
	
	/** Check whether a method contains "stub" only
	 * 
	 * @param m method name
	 * @param isDebug True when you want to print the process
	 */
	private static void DetermineWhetherMethodContainStub(SootMethod m, boolean isDebug){
		if(!m.hasActiveBody()){
			if(isDebug)
				System.out.println(m.toString() + " : Has no active body.");
			return;
		}
		
		Body body = m.getActiveBody();
		BriefBlockGraph bg = new BriefBlockGraph(body);
		
		Boolean isStubMethod = false;
		loops:
		for(Block b : bg.getBlocks()){
			for (Iterator<Unit> UnitintheBlock = b.iterator(); UnitintheBlock.hasNext();){
				Unit u = UnitintheBlock.next();
				if(u.toString().contains("<java.lang.RuntimeException: void <init>(java.lang.String)>(\"Stub!\")")){
					isStubMethod = true;
					break loops;
				}
			}
		}
		
		if(isDebug)
			System.out.println(m.toString() + " : " + (isStubMethod ? "Stub" : "Not stub"));
		
		stubMap.put(m.toString(), isStubMethod);
	}
	
	/** Serialize stubMethod map to file */
	private static void Serialization()
	{
		String path = getMapPath();
		
		Output output = null;
		
		try{
			Kryo kryo = new Kryo();
			output = new Output(new FileOutputStream(path));
			kryo.writeObject(output, stubMap);
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			if(output != null) output.close();
		}
	}
	
	/** Get stubMethod map from file */
	@SuppressWarnings("unchecked")
	private static HashMap<String, Boolean> Deserialization()
	{
		String path = getMapPath();
		
		Input input = null;
		HashMap<String, Boolean> result = null;
		
		try{
			Kryo kryo = new Kryo();
			input = new Input(new FileInputStream(path));
			result = (HashMap<String, Boolean>) kryo.readObject(input, HashMap.class);
		}catch (Exception e){
		}finally{
			if(input != null) input.close();
		}
		
		return result;
	}

	/** Get the path */
	private static String getMapPath(){
		return Constants.getFrontendOutputDirPath() + "/stubMethod.map";
	}
}
