package extractocol.common.outputs.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import extractocol.Constants;
import extractocol.common.outputs.backendOutputHelper.ReqRespInfo;
import extractocol.common.tools.highScaleLib.NonBlockingHashSet;

public class BackendOutputHelper {
	public static void SerializeBackendOutputs(ArrayList<ReqRespInfo> outputList){
		Kryo kryo = new Kryo();
		Output output;
		try
		{
			output = new Output(new FileOutputStream(Constants.RequestInfoPath()));
			kryo.writeClassAndObject(output, outputList);
			output.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList<ReqRespInfo> DeserializeBackendOutputs()
	{
		Kryo kryo = new Kryo();
		Input input;
		ArrayList<ReqRespInfo> result = null;
		try
		{
			input = new Input(new FileInputStream(Constants.RequestInfoPath()));
			result = (ArrayList<ReqRespInfo>) kryo.readClassAndObject(input);
			input.close();
			
			return result;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean needToRunBackend() {
		return !(new File(Constants.RequestInfoPath()).exists());
	}
}
