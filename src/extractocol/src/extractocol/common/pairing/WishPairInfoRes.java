
package extractocol.common.pairing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import extractocol.Constants;
import soot.Body;
import soot.BodyTransformer;
import soot.PackManager;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.InvokeStmt;
import soot.options.Options;

public class WishPairInfoRes
{
	public static void main(String args[])
	{
		Constants.apkName = "wish";
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
		Options.v().set_allow_phantom_refs(true);

		PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer()
		{

			@Override
			protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options)
			{

				for (String sm : EPs)
				{

					if (sm.equals(b.getMethod().getSignature()))
					{
						final PatchingChain<Unit> units = b.getUnits();

						// important to use snapshotIterator here
						for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();)
						{
							final Unit u = iter.next();
							u.apply(new AbstractStmtSwitch()
							{

								public void caseInvokeStmt(InvokeStmt stmt)
								{
									if (stmt.getInvokeExpr().toString().contains("com.contextlogic.wish.api.data"))
									{
										System.out.println(stmt);
										JSONObject responseEntry = new JSONObject();
										
										PairInfo.Add_request_entry(responseEntries, responseEntry);
//										PairInfo.Add_ep_method(responseEntry, b.getMethod().getSignature());
										PairInfo.Add_uri(responseEntry, "random", false);
										PairInfo.Add_dp_method(PairInfo.getCurrentSigEntry(), "DefaultDP");
										PairInfo.Add_dp_stmt(PairInfo.getCurrentSigEntry(), "DefaultDP Stmt");
										
										//GetSources
										getSources(responseEntry, stmt.getInvokeExpr().getMethod().getDeclaringClass());
										System.out.println(responseEntry);
									}
								}

								@SuppressWarnings("unchecked")
								private void getSources(JSONObject sigEntry, SootClass declaringClass)
								{
									// JM Auto-generated method stub
									JSONArray stmts = new JSONArray();
									PairInfo.Add_ep_stmts(sigEntry, stmts);
									for (SootMethod sm : declaringClass.getMethods())
									{
										if (sm.getName().startsWith("get"))
										{
											JSONObject stmt = new JSONObject();
											stmt.put("stmt", sm.getSignature());
											stmts.add(stmt);
										}
									}
									
								}
							});
						}
					}
				}
			}
		}));

		soot.Main.main(args);
		System.out.println(PairInfo.getSize());
		System.out.println(PairInfo.jsonDPs());
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
				EPs.add(tok[0]);
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
