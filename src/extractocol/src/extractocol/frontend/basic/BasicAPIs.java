package extractocol.frontend.basic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.google.common.io.Files;

import extractocol.Constants;
import soot.FastHierarchy;
import soot.Printer;
import soot.Scene;
import soot.SootClass;
import soot.jimple.infoflow.InfoflowConfiguration;
import soot.jimple.infoflow.InfoflowManager;
import soot.jimple.infoflow.solver.IInfoflowSolver;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.source.ISourceSinkManager;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;

public class BasicAPIs {
	public static void GenJimple(){
		// Write Jimple files
		ExtractocolLogger.LogNoLineBreak("Generating Jimple files ... ");
		if(jimpleExist()) {
			System.out.print("already exists. ");
		}else {
			for (SootClass sc : Scene.v().getClasses())
			{
				try {
					WriteSootClass(sc, true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("finished.");
	}
	
	public static void GenShimple(){
		// Write Shimple files
		ExtractocolLogger.LogNoLineBreak("Generating writing Shimple files ... ");
		if(shimpleExist()) {
			System.out.print("already exists. ");
		}else {
			for (SootClass sc : Scene.v().getClasses()) {
				try {
					WriteSootClass(sc, false);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("finished.");
	}
	
	private static boolean jimpleExist() {
		return new File(Constants.jimplePath()).exists();
	}
	private static boolean shimpleExist() {
		return new File(Constants.shimplePath()).exists();
	}
	
	private static void WriteSootClass(SootClass sc, boolean isJimple) throws IOException
	{
		File outputFile = null;
		if(isJimple) outputFile = new File(Constants.jimplePath());
		else outputFile = new File(Constants.shimplePath());
		
		outputFile.mkdir();
		String fileName = null;
		if(isJimple) fileName= Constants.jimplePath() + "/" + sc.getName() + ".jimple";
		else fileName= Constants.shimplePath() + "/" + sc.getName() + ".shimple";
		
		try
		{
			OutputStream streamOut = new FileOutputStream(fileName);
			PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
			Printer.v().printTo(sc, writerOut, isJimple);
			writerOut.flush();
			streamOut.close();
		}
		catch (Exception e)
		{}
	}
	
}
