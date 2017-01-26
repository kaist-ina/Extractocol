package Extractocol.Outputs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import Extractocol.Constants;

public class BackendOutputHelper {
	public static void SerializeBackendOutputs(){
		Kryo kryo = new Kryo();
		Output output;
		try
		{
			output = new Output(new FileOutputStream(Constants.RequestInfoPath()));
			kryo.writeObject(output, Constants.ReqRespInfoList);
			output.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void DeserializeBackendOutputs()
	{
		Kryo kryo = new Kryo();
		Input input;
		ArrayList<ReqRespInfo> result = null;
		try
		{
			input = new Input(new FileInputStream(Constants.RequestInfoPath()));
			result = (ArrayList<ReqRespInfo>) kryo.readObject(input, ArrayList.class);
			input.close();
			
			Constants.ReqRespInfoList.clear();
			Constants.ReqRespInfoList.addAll(result);
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
}
