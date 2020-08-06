package extractocol.backend.request.helper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import extractocol.Constants;
import extractocol.backend.request.basics.EPcontainer;

public class CFGSerializer
{
	private IInfoflowCFG iCfg;
	private SerializableCFG sCFG;
	private SerEPClass serEpClass;
	public enum ICFG_CASE {BACKWARD, FORWARD, HEAP, ARG};
	
	public CFGSerializer()
	{
	}
	
	public CFGSerializer(IInfoflowCFG _iCfg, List<EPcontainer> eplist)
	{
		iCfg = _iCfg;

		sCFG = new SerializableCFG(iCfg, eplist);
		sCFG.SerializeICfg();
		
		serEpClass = new SerEPClass(eplist);		
	}

	public void Serialize(ICFG_CASE icfg_case) throws FileNotFoundException
	{
		String path;
		
		switch(icfg_case){
		case BACKWARD: path = Constants.SerPath(); break;
		case FORWARD: path = Constants.SerPath_forward(); break;
		case HEAP: path = Constants.SerPath_heap(); break;
		default: return;
		}
		
		Kryo kryo = new Kryo();
		Output output = new Output(new FileOutputStream(path));
		kryo.writeObject(output, sCFG);
		output.close();
	}

	public SerializableCFG Deserialize(ICFG_CASE icfg_case) throws FileNotFoundException
	{
		String path;
		
		switch(icfg_case){
		case BACKWARD: path = Constants.SerPath(); break;
		case FORWARD: path = Constants.SerPath_forward(); break;
		case HEAP: path = Constants.SerPath_heap(); break;
		default: return null;
		}
		
		Kryo kryo = new Kryo();
		Input input = new Input(new FileInputStream(path));
		sCFG = kryo.readObject(input, SerializableCFG.class);
		input.close();
		return sCFG;
	}

	public void SerializeEPC(ICFG_CASE icfg_case) throws FileNotFoundException
	{
		String path;
		
		switch(icfg_case){
		case BACKWARD: path = Constants.EPContainorPath(); break;
		case FORWARD: path = Constants.EPContainorPath_forward(); break;
		case HEAP: path = Constants.EPContainorPath_Heap(); break;
		default: return;
		}

		Kryo kryo = new Kryo();
		Output output = new Output(new FileOutputStream(path));
		kryo.writeObject(output, serEpClass);
		output.close();
	}
	
	public List<SerEPcontainer> DeserializeEPC(ICFG_CASE icfg_case) throws FileNotFoundException
	{
		String path;
		
		switch(icfg_case){
		case BACKWARD: path = Constants.EPContainorPath(); break;
		case FORWARD: path = Constants.EPContainorPath_forward(); break;
		case HEAP: path = Constants.EPContainorPath_Heap(); break;
		default: return null;
		}
		
		Kryo kryo = new Kryo();
		Input input = new Input(new FileInputStream(path));
		serEpClass = kryo.readObject(input, SerEPClass.class);
		input.close();
		return serEpClass.serEpList;		
	}
}
class SerEPClass
{
	public List<SerEPcontainer> serEpList;

	public SerEPClass(List<EPcontainer> eplist) {
		serEpList = new ArrayList<SerEPcontainer>();
		for (int i = 0; i < eplist.size(); i++)
		{
			EPcontainer EPC = eplist.get(i);
			SerEPcontainer serEPC = new SerEPcontainer(EPC);
			serEpList.add(serEPC);
		}
	}
}
