package extractocol.common.trackers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Set;

import extractocol.Constants;
import extractocol.common.helper.InvokeHelper;
import extractocol.common.tools.Pair2;
import extractocol.common.trackers.tools.ArgToVEL;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntryTable;
import extractocol.common.valueEntry.ValueEntryTracking;
import extractocol.frontend.basic.ExtractocolLogger;
import extractocol.frontend.helper.PropagateHelper;
import extractocol.frontend.output.basic.TaintResultEntry;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;

public class IntentMapTracker {
	static boolean c;
	static Set<String> putMethods = new HashSet<String>();
	static Set<String> getMethods = new HashSet<String>();
	static ValueEntryTable intentMap = new ValueEntryTable();
	
	static int oldMaxMainStream;
	static int oldMaxDepth;
	static boolean oldIsRetrofit;
	
	public static void M(boolean debug) {
		// 1. init
		init();
		
		// 2. Try to de-serialize the intent map
		if(!Deserialize()) {
			// 3. get result
			Map<Pair2<Unit, SootMethod>, List<ValueEntryList>> res = ArgToVEL.M(putMethods, 5 /*maxMainStream*/, 2 /*maxDepth*/, debug);
			
			// 4. build intentMap
			processing(res);
		}
		
		// 5. post-processing
		postProcessing();
		
		if(Constants.isNotFullStack())
			System.exit(0);
	}
	
	private static void init() {
		// 1. set put methods
		putMethods.add("<android.os.Bundle: void putString(java.lang.String,java.lang.String)>");
		putMethods.add("<android.os.BaseBundle: void putString(java.lang.String,java.lang.String)>");
		
		
		// 2. set get methods
		getMethods.add("<android.os.Bundle: void getString(java.lang.String)>");
		
		// 4.
		c = Constants.setMonochromeOutput();
		oldIsRetrofit = Constants.setIsRetrofitFalse();
	}
	
	private static void processing(Map<Pair2<Unit, SootMethod>, List<ValueEntryList>> res) {
		// 1. build intentMap
		for (Entry<Pair2<Unit, SootMethod>, List<ValueEntryList>> e : res.entrySet()) {
			Pair2<Unit, SootMethod> p = e.getKey();
			Unit u = p.getE1();
			List<ValueEntryList> VEL = e.getValue();
			InvokeExpr ie = InvokeHelper.getInvokeExpr(u);
			
			String key;
			ValueEntryList vel;

			if (ie == null)
				continue;

			if (!putMethods.contains(ie.getMethodRef().toString()))
				continue;

			if(VEL.size() == 0)
				continue;
			
			key = VEL.get(0).GenRegex();
			
			if(VEL.size() == 1)
				vel = new ValueEntryList();
			else
				vel = VEL.get(1).Clone();
			
     			intentMap.addValueEntryList(key, vel, false);
		}
		
		// 2. serialize intentMap
		Serialize();
	}
	
	public static String getIntentValue(Unit ut, ValueEntryTracking vet) {
		// Check whether the unit contains invoke expr
		InvokeExpr ie = InvokeHelper.getInvokeExpr(ut);
		if(ie == null)
			return null;
		
		// Check whether it contains one of target getMethods
		if(!getMethods.contains(ie.getMethod().toString()))
			return null;
		
		// Get arg string (using genRegex)
		String arg = vet.varTable.GenRegex(ie.getArg(0).toString());
		
		// get the corresponding value
		return getIntentValue(arg);
	}
	
	public static String getIntentValue(String key) {
		ValueEntryList vel = intentMap.getValueEntryList(key);
		if(vel == null)
			return ".*";
		else
			return vel.GenRegex();
	}
	
	private static void postProcessing() {
		ArgToVEL.clear();
		Constants.setOutputColor(c);
		Constants.setIsRetrofit(oldIsRetrofit);
	}
	
	/** Save the heap results into file
	 * 
	 */
	public static void Serialize(){
		Kryo kryo = new Kryo();
		Output output;
		try
		{
			output = new Output(new FileOutputStream(getIntentMapTablePath()));
			kryo.writeClassAndObject(output, intentMap);
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
	public static boolean Deserialize()
	{
		Kryo kryo = new Kryo();
		Input input;
		ValueEntryTable result = null;
		try
		{
			input = new Input(new FileInputStream(getIntentMapTablePath()));
			result = (ValueEntryTable) kryo.readClassAndObject(input);
			input.close();
			
			intentMap = result;
			ExtractocolLogger.Log("The intent/bundle map was successfully deserialized (# keys: " + intentMap.getValueEntryTable().keySet().size() + ")");
			return true;
		}
		catch (FileNotFoundException e)
		{
			//e.printStackTrace();
			intentMap = new ValueEntryTable();
			return false;
		}
	}
	
	private static String getIntentMapTablePath()
	{
		File serializationDir = new File(Constants.serializationDirName);
		if (!serializationDir.exists())
		{
			serializationDir.mkdir();
		}
		
		return Constants.getIntentMapOutputDirPath() + "/IntentMapTable.ser";
	}
}
