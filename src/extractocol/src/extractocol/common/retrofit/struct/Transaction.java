package extractocol.common.retrofit.struct;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import extractocol.Constants;
import extractocol.common.outputs.backendOutputHelper.ReqRespInfo;
import extractocol.frontend.basic.ExtractocolLogger;

public class Transaction {
	String invokeStatement;
	List<Param> parameters;
	String DPmethod;
	
	Req request;
	Resp response;
	
	public void clear() {
		this.invokeStatement = null;
		this.DPmethod = null;
		
		if(this.parameters != null) {
			for(Param p: this.parameters)
				p.clear();
		}
		
		if(this.request != null) {
			this.request.clear();
			this.request = null;
		}
		
		if(this.response != null) {
			this.response.clear();
			this.response = null;
		}
	}
	
	public Transaction() {
		this.DPmethod = null;
		this.parameters = new ArrayList<Param>();
		this.request = new Req();
		this.response = new Resp();
	}
	
	public Req Request() { return this.request; }
	public Resp Response() { return this.response; }
	
	public void addParam(Param p) { this.parameters.add(p); }
	public List<Param> getParams() { return this.parameters; }
	
	public void setDPMethod(String s) { this.DPmethod = s; }
	public String getDPMethod() { return this.DPmethod; } 
	
	public String getInvokeStatement() { return this.invokeStatement; }
	public void GenInvokeStatement(String methodName, String package_name, String outerClassName, String innerClassName) {
		// 1. get param list
		String paramTypeList = "";
		for(Param p: this.parameters) {
			if(this.parameters.indexOf(p) == (this.parameters.size() - 1))
				paramTypeList += p.getType();
			else
				paramTypeList = paramTypeList + p.getType() + ",";
		}
		
		// 2. get return type
		String returnType = this.Response().getRetTypeOutermost();
		
		// 3. get class Name
		String className = (innerClassName == null) ? package_name + "." + outerClassName 
				: package_name + "." + outerClassName + "$" + innerClassName;
		
		// 4. generate invoke statement
		this.invokeStatement = className + ": " + returnType + " " + methodName + "(" + paramTypeList + ")";
		this.invokeStatement = "<" + this.invokeStatement + ">";
	}
	
	public static boolean Serialize(Map<String, Transaction> tranMap){
		Kryo kryo = new Kryo();
		Output output;
		try
		{
			output = new Output(new FileOutputStream(Constants.getRetrofitTranMapFilePath()));
			kryo.writeObject(output, tranMap);
			output.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Transaction> Deserialize()
	{
		Kryo kryo = new Kryo();
		Input input;
		Map<String, Transaction> result = null;
		try
		{
			input = new Input(new FileInputStream(Constants.getRetrofitTranMapFilePath()));
			result = (Map<String, Transaction>) kryo.readObject(input, HashMap.class);
			input.close();
			
			return result;
		}
		catch (FileNotFoundException e)
		{
			ExtractocolLogger.Log("This app does not use Retrofit library for networking.");
			//e.printStackTrace();
			return null;
		}
	}
}


