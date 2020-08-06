package extractocol.common.retrofit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import extractocol.Constants;
import extractocol.common.helper.InvokeHelper;
import extractocol.common.retrofit.struct.Transaction;
import extractocol.common.tools.Pair2;
import extractocol.common.trackers.tools.ArgToVEL;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntryTable;
import extractocol.frontend.basic.ExtractocolLogger;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;

public class RetrofitBaseURLTracker {
	static Set<String> baseUrlSetMethods = new HashSet<String>();
	static String baseURL =".*";
	static ValueEntryList vel;
	
	public static void M(boolean debug) {
		// 1. init
		init();
		
		// 2. Try to de-serialize the base Url of Retrofit
		if(!DeserializeBaseUrl()) {
			// 2. get result
			Map<Pair2<Unit, SootMethod>, List<ValueEntryList>> res = ArgToVEL.M(baseUrlSetMethods, 3, 3, debug);
			
			// 3. processing
			processing(res);
		}
		
		// 4. postProcessing
		postProcessing();
	}
	
	private static void init() {
		// 1. Set retrofit baseURL methods
		// retrofit1 (e.g., AccuWeather)
		baseUrlSetMethods.add("<retrofit.RestAdapter$Builder: retrofit.RestAdapter$Builder setEndpoint(java.lang.String)>");
		
		// retrofit2 (e.g., Flipagram, DoorDash)
		baseUrlSetMethods.add("<retrofit2.Retrofit$Builder: retrofit2.Retrofit$Builder baseUrl(java.lang.String)>");
		
		// 2. vel
		vel = new ValueEntryList();
	}
	
	private static void processing(Map<Pair2<Unit, SootMethod>, List<ValueEntryList>> res) {
		// 1. get the corresponding valueEntryLists
		for(Entry<Pair2<Unit, SootMethod>, List<ValueEntryList>> e: res.entrySet()) {
			Pair2<Unit, SootMethod> p = e.getKey();
			Unit u = p.getE1();
			List<ValueEntryList> VEL = e.getValue();
			
			InvokeExpr ie = InvokeHelper.getInvokeExpr(u);
			
			if(ie == null)
				continue;
			
			if(!baseUrlSetMethods.contains(ie.getMethodRef().toString()))
				continue;
			
			vel.addValueEntryList(VEL.get(0), false);
		}
		
		// 2. getBaseURL
		baseURL = vel.GenRegex();
		
		// 3. serialize the base Url
		SerializeBaseUrl();
	}
	
	public static String getRetrofitBaseURL() { return "(" + baseURL + ")"; }
	
	private static void postProcessing() {
		ExtractocolLogger.Log("The base Url of Retrofit: " + baseURL);
		
		ArgToVEL.clear();
		vel.Clear();
	}
	
	/** Save the heap results into file
	 * 
	 */
	public static void SerializeBaseUrl(){
		// 0. get path
		String path = getRetrofitBaseURLPath(); 
		
		// 1. open file
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		// 2. write DP info
		pw.println(baseURL);
		
		// 3. close the file
		pw.close();
	}
	
	/** Read the heap results from file
	 * 
	 */
	public static boolean DeserializeBaseUrl()
	{
		try {
			BufferedReader br = new BufferedReader(new FileReader(getRetrofitBaseURLPath()));
			while(true) {
				String line = br.readLine();
				if(line == null) break;
				baseURL = line;
			}
			
			br.close();
			return true;
		}catch (IOException e) {
			return false;
		}
	}
	
	private static String getRetrofitBaseURLPath()
	{
		File serializationDir = new File(Constants.serializationDirName);
		if (!serializationDir.exists())
		{
			serializationDir.mkdir();
		}
		
		return Constants.getRetrofitOutputDirPath() + "/retrofitBaseUrl.txt";
	}
}
