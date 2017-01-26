package Extractocol.BufferExtractor_Request.Helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import Extractocol.Constants;
import soot.SootClass;
import soot.jimple.parser.JimpleAST;

public class FakeScene
{
	ArrayList<SootClass> ArrClass = new ArrayList<SootClass>();
	
	public FakeScene()
	{
		File rootFolder = new File(Constants.jimplePath());
		File[] listOfFiles = rootFolder.listFiles();
		
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
//					System.out.println(jimpleCandidates);
					ArrClass.add((new JimpleAST(inputStream)).createSootClass());
				} catch (Exception e)
				{
					continue;
				}
			}
		}
	}
	
	public ArrayList<SootClass> getClasses()
	{
		return ArrClass;
	}
	
	public SootClass getSootClass(String SootClassName)
	{
		for (SootClass sc : ArrClass)
		{
			if (sc.getName().equals(SootClassName))
				return sc;
		}
		return null;
	}
}
