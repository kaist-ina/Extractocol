package extractocol.common.outputs.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import extractocol.Constants;
import extractocol.common.outputs.backendOutputHelper.ReqRespInfo;
import extractocol.common.tools.Pair;
import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntryList;
import extractocol.common.valueEntry.ValueEntry.SOURCE_TYPE;
import extractocol.common.valueEntry.node.*;

public class HeapHandlingHelper {
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static enum POSITION1 {URI, HEADER, BODY, QUERY};
	public static enum POSITION2 {REQUEST, RESPONSE};
	
	static int[][] dependencyGraph;
	
	/**************************************************/
	/***                Basic APIs                  ***/
	/**************************************************/
	public static void Initialization(ArrayList<ReqRespInfo> list){
		int numOfTransactions = list.size();
		
		// 1. Initialize the dependency graph
		InitDependencyGraph(numOfTransactions);
		
		// 2. make directory to save proxy signatures
		makeDIR(); 
		
		// 3. Empty all of the proxy signature files
		EmptyAllSignatureFiles(list);
	}
	
	private static void makeDIR(){
		File serializationDir;
		
		serializationDir = new File(getSignatureDIRPath());
		if (!serializationDir.exists()) serializationDir.mkdir();
		
		serializationDir = new File(getProxySignatureDIRPath());
		if (!serializationDir.exists()) serializationDir.mkdir();
		for(File file: serializationDir.listFiles())
			file.delete();
		
		serializationDir = new File(getRegexSignatureDIRPath());
		if (!serializationDir.exists()) serializationDir.mkdir();
		for(File file: serializationDir.listFiles())
			file.delete();
	}
	
	public static void printURIs(ArrayList<ReqRespInfo> list){
		System.out.println("# of Transactions: " + list.size());
		System.out.println("===============================================================");
		for(ReqRespInfo rri: list)
			System.out.println("URI [" + list.indexOf(rri) + "]: " + rri.reqie.getSignature());
		System.out.println("===============================================================");
	}
	
	
	
	/**************************************************/
	/***          APIs for tracking dependency      ***/
	/**************************************************/
	public static boolean doesHeapComeFromOtherTransactions(int succID, String heapName, ArrayList<ReqRespInfo> infoList){
		for(ReqRespInfo rri: infoList){
			if(infoList.indexOf(rri) == succID)
				continue;
			
			ValueEntryList list = rri.getValueEntryListOf(heapName);
			
			if(list != null)
				for(ValueEntry he: list.getValueEntryList())
					switch(he.getSourceType()){
					// JSON is from the body of the predecessor.
					case JSONKEY:
						return true;
					default:
							break;
					}
		}
		
		return false;
	}
	
	public static boolean TrackDependencyAndGenerateSignature(int succID, POSITION1 succPos, String succKey, String heapName, ArrayList<ReqRespInfo> infoList){
		boolean hasFound = false;
		
		for(ReqRespInfo rri: infoList){
			if(infoList.indexOf(rri) == succID)
				continue;
			
			ValueEntryList list = rri.getValueEntryListOf(heapName);
			
			// A non-empty list means the successor takes some data in the predecessor.
			if(list != null){
				for(ValueEntry ve: list.getValueEntryList()){
					switch(ve.getSourceType()){
					// JSON is from the body of the predecessor.
					case JSONKEY: 
						setDependencyGraph(infoList.indexOf(rri), succID);
						for(String JSONKeys : ((JSONKey)ve).getJSONKeyString())
							GenerateDependencySignature(succID, succPos, succKey, infoList.indexOf(rri), POSITION1.BODY, POSITION2.RESPONSE, JSONKeys);
						
						hasFound = true;
						break;
					// TODO: handling header, body
					default:
						break;
					}
				}
			}
		}
		
		return hasFound;
	}
	
	/**************************************************/
	/***          APIs for dependency graph         ***/
	/**************************************************/
	private static void setDependencyGraph(int pred, int succ){
		dependencyGraph[pred][succ] = 1;
	}
	
	private static void InitDependencyGraph(int numOfTransactions){
		// 1. allocation
		dependencyGraph = new int[numOfTransactions][numOfTransactions];
		
		// 2. set the graph to zero
		for(int i = 0 ; i < numOfTransactions; i++)
			Arrays.fill(dependencyGraph[i], 0);
	}
	
	
	/**************************************************/
	/***       APIs for signature generation        ***/
	/**************************************************/
	private static void GenerateDependencySignature(int succID, POSITION1 succPos, String succKey, int predID, POSITION1 predPos1, POSITION2 predPos2, String predKey){
		try{
			FileWriter fw = new FileWriter(new File(getDependencySignaturePath(succID)), true); 
			
			fw.write(succPos.toString() + " " + succKey + " " + predID + " " + predPos2.toString() + " " + predPos1.toString() + " " + predKey);
			fw.write(LINE_SEPARATOR);
			fw.flush();
			fw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void EmptyAllSignatureFiles(ArrayList<ReqRespInfo> list){
		// 1. Empty dependency graph
		MakeFileEmpty(getDependencyGraphPath());
		
		// 2-1. Empty URI signatures (Regex)
		MakeFileEmpty(getURIRegexSignaturePath());

		for(int i = 0; i < list.size(); i++){
			if(list.get(i).reqie.getSignature() == null)
				continue;
			
			// 2-2. Empty URI signatures (Regex)
			MakeFileEmpty(getURIRegexSignaturePath(i));
						
			// 3. Empty URI signatures (Proxy)
			MakeFileEmpty(getURIProxySignaturePath(i));
			
			// 4. Empty dependency signatures
			MakeFileEmpty(getDependencySignaturePath(i));
			
			// 5. Empty Header signatures (Regex)
			MakeFileEmpty(getHeaderRegexSignaturePath(i));
			
			// 6. Empty Header signatures (Proxy)
			MakeFileEmpty(getHeaderProxySignaturePath(i));
			
			// 7. Empty Body signatures (Regex)
			MakeFileEmpty(getBodyRegexSignaturePath(i));

			// 8. Empty Body signatures (Proxy)
			MakeFileEmpty(getBodyProxySignaturePath(i));
			
			// 7. Empty Query signatures (Regex)
			MakeFileEmpty(getQueryRegexSignaturePath(i));

			// 8. Empty Query signatures (Proxy)
			MakeFileEmpty(getQueryProxySignaturePath(i));
		}
	}
	
	
	public static void GenerageSignatures(ArrayList<ReqRespInfo> list){
		// 1. Generate dependency graph
		GerateDependencyGraph(list);
		
		// 2. Generate signatures for URI, Header, and Body
		for(ReqRespInfo rri: list){
			if(rri.reqie.getSignature() == null)
				continue;
			
			int ID = list.indexOf(rri);
			
			GenerateURISignature(ID, rri); // URI
			GenerateHeaderSignature(ID, rri); // Header
			GenerateBodySignature(ID, rri); // Body
			GenerateQuerySignature(ID, rri); // Query
		}
	}
	
	private static void GerateDependencyGraph(ArrayList<ReqRespInfo> list){
		// 1. Draw dependency graph using html
		HtmlGraphDrawing.Drawing(list, dependencyGraph, HeapHandlingHelper.getDependencyGraphHtmlPath());
		
		// 2. Save as a proxy signature
		for(int i = 0; i < dependencyGraph.length; i++)
			for(int j = 0; j < dependencyGraph.length; j++)
				if(dependencyGraph[i][j] == 1)
					GenerateSignature(i + " " + j, getDependencyGraphPath());
	}
	
	private static void GenerateHeaderSignature(int succID, ReqRespInfo rri){
		if(rri.reqie.Header == null)
			return; 
		
		for(Pair p: rri.reqie.Header){
			// 1. Print Regex Signature for header
			GenerateSignature(p.getRegexSigKey() + " " + p.getRegexSigValue(), getHeaderRegexSignaturePath(succID));
			
			// 2. Print Proxy Signature for header
			GenerateSignature(p.getProxySigKey() + " " + p.getProxySigValue(), getHeaderProxySignaturePath(succID));
		}
	}
	
	private static void GenerateBodySignature(int succID, ReqRespInfo rri){
		if(rri.reqie.Body == null)
			return;
		
		for(Pair p: rri.reqie.Body){
			// 1. Print Regex Signature for body
			GenerateSignature(p.getRegexSigKey() + " " + p.getRegexSigValue(), getBodyRegexSignaturePath(succID));
			
			// 2. Print Proxy Signature for body
			GenerateSignature(p.getProxySigKey() + " " + p.getProxySigValue(), getBodyProxySignaturePath(succID));
		}
	}
	
	private static void GenerateQuerySignature(int succID, ReqRespInfo rri){
		if(rri.reqie.Query == null)
			return;
		
		for(Pair p: rri.reqie.Query){
			// 1. Print Regex Signature for body
			GenerateSignature(p.getRegexSigKey() + " " + p.getRegexSigValue(), getQueryRegexSignaturePath(succID));
			
			// 2. Print Proxy Signature for body
			GenerateSignature(p.getProxySigKey() + " " + p.getProxySigValue(), getQueryProxySignaturePath(succID));
		}
	}
	
	private static void GenerateURISignature(int ID, ReqRespInfo rri){
		// 1. Print whole Regex Signature for URI
		GenerateSignature(rri.reqie.getMethod() + " " + rri.reqie.getWholeRegexSignature(), getURIRegexSignaturePath());
		
		// 2. Print regex sub-signature
		for(String regexSig: rri.reqie.getRegexSignature())
			GenerateSignature(regexSig, getURIRegexSignaturePath(ID));
			
		// 2. Print Proxy sub-Signature for URI
		for(String proxySig: rri.reqie.getProxySignature())
			GenerateSignature(proxySig, getURIProxySignaturePath(ID));
	}
	
	private static void GenerateSignature(String text, String path){
		try{
			// 2. Print Proxy Signature for URI
			FileWriter fw = new FileWriter(new File(path), true);
			fw.write(text);
			fw.write(LINE_SEPARATOR);
			fw.flush();
			fw.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	/** Make a file empty **/
	static public void MakeFileEmpty(String path){
		try{
			FileWriter file = new FileWriter(new File(path), true);
			file = new FileWriter(new File(path));
			file.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	private static String getSignatureDIRPath(){ return Constants.serializationDirName + "/" + Constants.apkName + "/[Signatures]"; }
	private static String getProxySignatureDIRPath(){ return getSignatureDIRPath() + "/Proxy Signatures"; }
	private static String getRegexSignatureDIRPath(){ return getSignatureDIRPath() + "/Regex Signatures"; }
	
	private static String getURIRegexSignaturePath(){ return getRegexSignatureDIRPath() + "/RegexSig.URIs"; }
	private static String getURIRegexSignaturePath(int ID){ return getRegexSignatureDIRPath() + "/RegexSig_" + ID + ".URIs"; }
	private static String getBodyRegexSignaturePath(int ID){ return getRegexSignatureDIRPath() + "/RegexSig_" + ID + ".body"; }
	private static String getHeaderRegexSignaturePath(int ID){ return getRegexSignatureDIRPath() + "/RegexSig_" + ID + ".header"; }
	private static String getQueryRegexSignaturePath(int ID){ return getRegexSignatureDIRPath() + "/RegexSig_" + ID + ".query"; }
	
	private static String getURIProxySignaturePath(int ID){ return getProxySignatureDIRPath() + "/ProxySig_" + ID + ".URIs"; }
	private static String getBodyProxySignaturePath(int ID){ return getProxySignatureDIRPath() + "/ProxySig_" + ID + ".body"; }
	private static String getHeaderProxySignaturePath(int ID){ return getProxySignatureDIRPath() + "/ProxySig_" + ID + ".header"; }
	private static String getQueryProxySignaturePath(int ID){ return getProxySignatureDIRPath() + "/ProxySig_" + ID + ".query"; }
	
	private static String getDependencyGraphPath(){ return getProxySignatureDIRPath() + "/Dependency.graphSig"; }
	private static String getDependencySignaturePath(int ID){ return getProxySignatureDIRPath() + "/Dependency_" + ID + ".detailSig"; }
	
	public static String getDependencyGraphHtmlPath(){ return getSignatureDIRPath() + "/Dependency Graph.html"; }
	
	
	
	
	private static String getStringOfPOS1(POSITION1 pos){
		switch(pos){
		case URI: return "URI";
		case HEADER: return "HEADER";
		case BODY: return "BODY";
		default: return null;
		}
	}
	
	private static String getStringOfPOS2(POSITION2 pos){
		switch(pos){
		case REQUEST: return "REQUEST";
		case RESPONSE: return "RESPONSE";
		default: return null;
		}
	}
	
	
}
