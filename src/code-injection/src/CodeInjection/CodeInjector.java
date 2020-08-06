package CodeInjection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;
import java.util.regex.Pattern;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PackManager;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.StringConstant;
import soot.util.Chain;

import org.apache.commons.cli.*;

/**
 * 1. package name: CodeInjection
 * 2. type name: CodeInjector.java
 * 3. writer: Hyun Ho
 * 4. description: Main class for running CodeInjection  
 */
public class CodeInjector{
	private static String serializationDirName = "../../SerializationFiles";
	private static String sootOutputDirName = "sootOutput/";
	private static String DPsFilePath = "CodeInject/DPs.txt";
	private static String[] keyData = {"resource/my-release-key.jks", "sootOutput/wish.apk", "app", "zizonama5958"}; //key path, apk path, apk alias, key password  
	private static String jarsignerCommand = "jarsigner -verbose " + " " + "-storepass" +  " " + keyData[3] + " " + "-keystore" + " " + keyData[0] + " " + keyData[1] + " " + keyData[2]; 
	private static ArrayList<DP_Pair> DPsList = new ArrayList<DP_Pair>(); 

	public static void main(String[] args)
	{
		/* command line parsing - get an app name from an app loaction path*/
		CommandLineParser parser = new DefaultParser();
		CommandLine line;
		Options options = new Options();
		options.addOption(Option.builder().required(true).hasArg(true).longOpt("process-dirs").build());
		options.addOption(Option.builder().longOpt("android-jars").build());

		System.out.println("CodeInject Start");
		try {
		    line = parser.parse( options, args);
		}
		catch( ParseException exp ) {
		    System.out.println( "Unexpected exception: " + exp.getMessage() );
		    return; 
		}
		
		//app_location: D:\workspace\extractocol\SerializationFiles\wish.apk
		//--> app_name: wish
    	String app_location = line.getOptionValue("process-dirs");
    	String[] tokens = app_location.split("\\\\");
    	String app_name = tokens[tokens.length - 1];
    	tokens = app_name.split("\\.");
    	app_name = tokens[0];
    	System.out.println("target_app: " + app_name);
    	
    	/* Get an app's front-end result about DP */
    	BufferedReader br = null;
    	try {
    		File dps_file = new File(serializationDirName + "/" + app_name + "/" + DPsFilePath);
    		if(dps_file.exists() && !dps_file.isDirectory())
    		{
    			br = new BufferedReader(new FileReader(dps_file));
    			String stmt, mth;
    			Integer i = 0;
 
    			while(((stmt = br.readLine()) != null) && ((mth = br.readLine()) != null))
    			{
    				DPsList.add(new DP_Pair(stmt, mth));
    				System.out.println("(" + i.toString() + ")" + " Printing...");
    				System.out.println("+ DP Stmt: " + stmt);
    				System.out.println("+ DP Sootmethod: " + mth);
    			}
    		}
    		else
    		{
    			System.out.println("DPs doesn't exist");
    		}
		}catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(br != null)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

    	/* Overwrite Soot::internalTransform to implement CodeInject logic*/
    	soot.options.Options.v().set_src_prec(soot.options.Options.src_prec_apk);
    	soot.options.Options.v().set_allow_phantom_refs(true);
    	soot.options.Options.v().set_output_format(soot.options.Options.output_format_dex);
    	soot.options.Options.v().set_force_overwrite(true);

		PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer()
		{
			@Override
			protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options)
			{
				final PatchingChain<Unit> units = b.getUnits();
				AppLogger logger = AppLogger.getInstance();

				for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();)
				{

					final Unit u = iter.next();
					u.apply(new AbstractStmtSwitch()
					{						
						public void caseAssignStmt(AssignStmt stmt)
						{
							for(Iterator<DP_Pair> iter = DPsList.iterator(); iter.hasNext(); )
							{
								DP_Pair dp_pair = iter.next();
								if (b.getMethod().getSignature().equals(dp_pair.getMethod()) && stmt.toString().equals(dp_pair.getStmt()))
								{
									logger.HandleAssignStmt(b, units, u, stmt);
								}
							}
						}

						public void caseInvokeStmt(InvokeStmt stmt)
						{
							for(Iterator<DP_Pair> iter = DPsList.iterator(); iter.hasNext(); )
							{
								DP_Pair dp_pair = iter.next();
								if (b.getMethod().getSignature().equals(dp_pair.getMethod()) && stmt.toString().equals(dp_pair.getStmt()))
								{
									dp_pair = iter.next();
									if (b.getMethod().getSignature().equals(dp_pair.getMethod()) && stmt.toString().equals(dp_pair.getStmt()))
									{
										logger.HandleInvokeStmt(b, units, u, stmt);
									}
								}
							}
						}
					});
				}
			}
		}));
		
		/* Run soot */
		soot.Main.main(args);
		
		/*sign an app*/
		File output_apk = new File(sootOutputDirName + app_name + ".apk");
		if(soot.options.Options.v().output_format() == soot.options.Options.output_format_dex && output_apk.exists())
		{
			System.out.println("apk file created");
			
			ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", jarsignerCommand);
			builder.redirectErrorStream(true);
			Process p = null;
			try {
				p = builder.start();
			} catch (IOException e) {
				e.printStackTrace();
			} 
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        String rline = null;
	        while (true) {
	            try {
					rline = r.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
	            if (rline == null) { break; }
	            System.out.println(rline);
	        }
			
		}
	}
}
