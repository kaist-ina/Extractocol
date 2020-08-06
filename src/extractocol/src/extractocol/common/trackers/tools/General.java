package extractocol.common.trackers.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import extractocol.Constants;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntryTable;

public class General {
	static ValueEntryTable heapResult = null;
	
	public static void SaveHeapResult(String Heap, String res){
		heapResult.setConstantValue(Heap, res, false);
		
		// Save the result every time (to save the analysis time)
		SerializeHeapResults();
	}
	
	public static void SaveHeapResult(String Heap, ValueEntryList res){
		heapResult.setValueEntryList(Heap, res, false);
		
		// Save the result every time (to save the analysis time)
		SerializeHeapResults();
	}
	
	public static ValueEntryList getValueEntryList(String heap) {
		return heapResult.getValueEntryList(heap);
	}
	
	public static boolean isHeapAlreadyAnalyzed(String heap) {
		return getValueEntryList(heap) != null;
	}
	
	/** Save the heap results into file
	 * 
	 */
	public static void SerializeHeapResults(){
		Kryo kryo = new Kryo();
		Output output;
		try
		{
			output = new Output(new FileOutputStream(getHeapResultPath()));
			kryo.writeObject(output, heapResult);
			output.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	/** Read the heap results from file
	 * 
	 */
	public static void DeserializeHeapResults()
	{
		if(heapResult != null)
			return;
		
		Kryo kryo = new Kryo();
		Input input;
		ValueEntryTable result = null;
		try
		{
			input = new Input(new FileInputStream(getHeapResultPath()));
			result = (ValueEntryTable) kryo.readObject(input, ValueEntryTable.class);
			input.close();
			
			heapResult = result;
		}
		catch (FileNotFoundException e)
		{
			//e.printStackTrace();
			heapResult = new ValueEntryTable();
		}
	}
	
	private static String getHeapResultPath()
	{
		File serializationDir = new File(Constants.serializationDirName);
		if (!serializationDir.exists())
		{
			serializationDir.mkdir();
		}
		
		return Constants.getHeapHandlerOutputDirPath() + "/HeapResult.ser";
	}
	
}
