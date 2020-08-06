package extractocol.frontend.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import extractocol.Constants;
import extractocol.backend.request.helper.SerializableCFG;
import extractocol.backend.request.helper.CFGSerializer.ICFG_CASE;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

public class CFGResultContainer {
	static SerializableCFG sCFG = null;
	
	public static void BuildSerializableCFG(IInfoflowCFG _iCfg){
		sCFG = new SerializableCFG(_iCfg, null);
		sCFG.SerializeICfg();
	}
	
	public static boolean doesICFGfileExist(ICFG_CASE icfg_case) {
		String path;
		
		switch(icfg_case){
		case BACKWARD: path = Constants.SerPath(); break;
		case FORWARD: path = Constants.SerPath_forward(); break;
		case HEAP: path = Constants.SerPath_heap(); break;
		default: return false;
		}
		
		File f = new File(path);
		return f.exists();
	}
	
	public static void Serialization(ICFG_CASE icfg_case)
	{
		String path;
		
		switch(icfg_case){
		case BACKWARD: path = Constants.SerPath(); break;
		case FORWARD: path = Constants.SerPath_forward(); break;
		case HEAP: path = Constants.SerPath_heap(); break;
		default: return;
		}
		
		Output output = null;
		
		try{
			Kryo kryo = new Kryo();
			output = new Output(new FileOutputStream(path));
			kryo.writeObject(output, sCFG);
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			if(output != null) output.close();
		}
	}
	
	public SerializableCFG Deserialization(ICFG_CASE icfg_case)
	{
		String path;
		
		switch(icfg_case){
		case BACKWARD: path = Constants.SerPath(); break;
		case FORWARD: path = Constants.SerPath_forward(); break;
		case HEAP: path = Constants.SerPath_heap(); break;
		default: return null;
		}
		
		Input input = null;
		
		try{
			Kryo kryo = new Kryo();
			input = new Input(new FileInputStream(path));
			sCFG = kryo.readObject(input, SerializableCFG.class);
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			if(input != null) input.close();
		}
		
		return sCFG;
	}
}
