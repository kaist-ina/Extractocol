package extractocol.backend.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Stack;

import extractocol.Constants;
import extractocol.common.outputs.BackendOutput;
import extractocol.common.outputs.backendOutputHelper.ReqRespInfo;
import extractocol.common.trackers.tools.ArgToVEL;
import extractocol.frontend.basic.ExtractocolLogger;
import extractocol.frontend.output.basic.DPContainer;
import extractocol.frontend.output.basic.EPContainer;

public class BackendThread implements Runnable {
	int id;
	
	DPContainer DPC;
	EPContainer EPC;
	
	String Req_EPMEthod;
	String DPMEthod;
	String DPStmt;
	
	String Resp_EPMethod;
	String Resp_EPStmt;
	
	List<String> taintMethods;
	
	ReqRespInfo reqRespInfo;
	
	boolean foundDPStmt;
	Stack<String> methodStack;
	int methodVisitCount;
	int totalMethodVisitCount;
	int depthCount;
	int reqBuildTime;
	int respBuildTime;
	
	boolean multiThread;
	
	public DPContainer DPC() { return this.DPC; }
	public EPContainer EPC() { return this.EPC; }
	
	public String getReqEPMethod() { return this.Req_EPMEthod; }
	public String getDPMethod() { return this.DPMEthod; }
	public String getDPStmt() { return this.DPStmt; }
	
	public String getRespEPMethod() { return this.Resp_EPMethod; }
	public String getRespEPStmt() { return this.Resp_EPStmt; }
	public void setRespEP(String method, String stmt) {
		this.Resp_EPMethod = method;
		this.Resp_EPStmt = stmt;
	}
	
	public List<String> getTaintMethodSet() { return this.taintMethods; }
	public ReqRespInfo RRI() { return this.reqRespInfo; }
	
	public void setFoundDPStmt(boolean b) { this.foundDPStmt = b; }
	public boolean hasFoundDPStmt() { return this.foundDPStmt; }
	public Stack<String> getMethodStack() { return this.methodStack; }
	
	public int getMethodVisitCount() { return this.methodVisitCount; }
	public void increaseMethodVisitCount() { this.methodVisitCount++; }
	public void decreaseMethodVisitCount() { this.methodVisitCount--; }
	public boolean stopVisitMethod() { return this.methodVisitCount >= Constants.maxMethodVisitCount; }
	
	public int getDepthCount() { return this.depthCount; }
	public void increaseDepthCount() { this.depthCount++; }
	public void decreaseDepthCount() { this.depthCount--; }
	
	public int getTotalMethodVisitCount() { return this.totalMethodVisitCount; }
	public void setTotalMethodVisitCount(int i) { this.totalMethodVisitCount = i; }
	
	public int getReqBuildTime() { return this.reqBuildTime; }
	public void setReqBuildTime(int t) { this.reqBuildTime = t; }
	
	public int getRespBuildTime() { return this.respBuildTime; }
	public void setRespBuildTime(int t) { this.respBuildTime = t; }
	
	public boolean isMultiThread() { return this.multiThread; }
	
	
	public BackendThread(int _id, DPContainer _DPC, EPContainer _EPC, boolean _multiThread) {
		this.id = _id;
		this.DPC = _DPC;
		this.EPC = _EPC;
		
		this.Req_EPMEthod = this.EPC.getEP();
		this.DPMEthod = this.DPC.getDPMethod();
		this.DPStmt = this.DPC.getDPStmt();
		
		this.Resp_EPMethod = this.DPC.getDPMethod();
		this.Resp_EPStmt = this.DPC.getDPStmt();
		
		this.taintMethods = EPC.getTaintMethodSet();
		this.reqRespInfo = new ReqRespInfo();
		
		this.foundDPStmt = false;
		this.methodStack = new Stack<String>();
		this.methodVisitCount = 0;
		this.totalMethodVisitCount = 0;
		this.depthCount = 0;
		
		this.reqBuildTime = 0;
		this.respBuildTime = 0;
		
		this.multiThread = _multiThread;
	}
	
	public void run() {
		// 1. Request Sig. Building
		ReqSigBuilding.Main(this);
		postReqBuild();
		
		// 2. Response Sig. Building
		if(needToRunRespBuilding(this.totalMethodVisitCount))
			RespSigBuilding.Main(this);
		
		// 3. Save result
		this.reqRespInfo.reqie.taintMethods = this.taintMethods;
		this.reqRespInfo.SaveResult(this.totalMethodVisitCount);
	}
	
	private void postReqBuild() {
		this.foundDPStmt = false;
		if(this.methodStack != null) this.methodStack.clear();
		this.methodStack = new Stack<String>();
		this.methodVisitCount = 0;
		this.depthCount = 0;
	}
	
	private boolean needToRunRespBuilding(int visitCnt) {
		// Not need to perform forward analysis if the Back-end is working for tracking candidate values of heap
		if (Constants.heapobject)
		{
			this.reqRespInfo.heapTable.BringOriginalFromReferenceTable();
			this.reqRespInfo.SaveResult(visitCnt);
			return false;
		}
		
		// Not need to analyze the response signature if backend is running for arg tracking
		if(ArgToVEL.isArgTracking())
			return false;
		
		// Not need to analyze the response signature if the request URI analyzed is null
		if(!this.reqRespInfo.reqie.isValid()){
			if(!this.multiThread)
				System.out.println("\t\t - Skipping the response signature building because the analyzed URI is invalid.\n");
			return false;
		}
			
		
		/*******************************************************/
		/** 2. Forward analysis (Response signature building) **/
		/*******************************************************/
		// Check whether the response signature building is required
		if(!this.reqRespInfo.getIsRSBRequired())
			return false;
			
		return true;
	}
	
	private void SerializeResult(){
		try{
			FileOutputStream fos = new FileOutputStream(getRRIFilePath());
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(this.reqRespInfo);
			fos.close();
			oos.flush();
			oos.close();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private boolean DeserializeResult(){
		try{
			FileInputStream fis = new FileInputStream(getRRIFilePath());
			ObjectInputStream ois = new ObjectInputStream(fis);
			this.reqRespInfo = (ReqRespInfo) ois.readObject();
			ois.close();
			fis.close();
			return true;
		}catch (Exception e){
			//e.printStackTrace();
			return false;
		}
	}
	
	private String getRRIFilePath() { return Constants.getPartialRRIDirPath() + "/" + this.id + ".ser";}
}
