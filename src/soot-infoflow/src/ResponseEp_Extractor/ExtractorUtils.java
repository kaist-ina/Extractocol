
package ResponseEp_Extractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import Extractocol.Constants;
import soot.SootClass;
import soot.jimple.parser.JimpleAST;

public class ExtractorUtils
{

	public static SootClass readClass(String className)
	{
		InputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(Constants.jimplePath() + "/" + className);
			return new JimpleAST(inputStream).createSootClass();
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public static ArrayList<SootClass> readAllClasses()
	{
		File rootFolder = new File(Constants.jimplePath());
		File[] listOfFiles = rootFolder.listFiles();
		ArrayList<SootClass> ArrClass = new ArrayList<SootClass>();

		for (File jimpleCandidates : listOfFiles)
		{
			if (jimpleCandidates.getName().contains(".jimple") && !jimpleCandidates.getName().contains("dummyMainClass.jimple"))
			{
				InputStream inputStream = null;
				try
				{
					inputStream = new FileInputStream(jimpleCandidates);
				} catch (FileNotFoundException e1)
				{
					continue;
				}
				try
				{
					// System.out.println(jimpleCandidates);
					ArrClass.add((new JimpleAST(inputStream)).createSootClass());
				} catch (Exception e)
				{
					continue;
				}
			}
		}

		return ArrClass;
	}

	public static void WriteEPs(ArrayList<EPCandidate> list) throws IOException
	{
		for (EPCandidate epc : list)
		{
			String rootPath = Constants.GetForwardEP_path();
			BufferedWriter out = new BufferedWriter(new FileWriter(rootPath, true));

			if (epc.getDPStmt() != null)
			{
				out.write(epc.getEPSig() + "####" + epc.getDPStmt());
				out.newLine();
			}
			out.close();
		}
	}
}
