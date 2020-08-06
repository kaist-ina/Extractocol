
package extractocol.common.pairing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import extractocol.Constants;
import soot.Body;
import soot.BodyTransformer;
import soot.EntryPoints;
import soot.G;
import soot.MethodOrMethodContext;
import soot.Pack;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.CallGraphBuilder;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;

public class WishPairInfoReq
{

	static int URLcount = 0;

	static boolean FoundAllApis = false;

	public static void main(String args[])
	{
		Constants.apkName = "wish";

		final String BaseUrl = "htps://www.wish.com/api/";
		final BuildPairJson PairInfo = new BuildPairJson();
		final ArrayList<String> EPs = new ArrayList<String>();
		ArrayList<String> DPstmts = new ArrayList<String>();
		ParsingEPs(EPs, DPstmts);
		PairInfo.Add_dp();
		JSONObject SigEntry = PairInfo.getCurrentSigEntry();
		final JSONArray responseEntries = new JSONArray();
		PairInfo.Add_request_array(SigEntry, responseEntries, false);

		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_output_format(Options.output_format_none);
		Options.v().set_whole_program(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_verbose(true);
		Options.v().setPhaseOption("cg.spark", "on");
		Options.v().setPhaseOption("cg.spark", "string-constants:true");
		Options.v().setPhaseOption("cg.spark", "pre-jimplify:true");
		PackManager.v().getPack("cg").apply();

		soot.Main.main(args);
	}

	public static void ParsingEPs(ArrayList<String> EPs, ArrayList<String> DPstmts)
	{
		String EPPath = Constants.GetForwardEP_path();
		BufferedReader in = null;
		try
		{
			in = new BufferedReader(new FileReader(EPPath));
		} catch (FileNotFoundException e)
		{
			// JM Auto-generated catch block
			e.printStackTrace();
		}
		String s;

		try
		{
			while ((s = in.readLine()) != null)
			{
				String tok[] = s.split("####");
				EPs.add(tok[0].substring(1, tok[0].indexOf(":")));
				DPstmts.add(tok[1]);
			}
		} catch (IOException e1)
		{
			// JM Auto-generated catch block
			e1.printStackTrace();
		}
		try
		{
			in.close();
		} catch (IOException e)
		{
			// JM Auto-generated catch block
			e.printStackTrace();
		}

		if (EPs.size() == 0)
		{
			System.out.println("EP Parsing fail");
			System.exit(1);
		}
	}
}
