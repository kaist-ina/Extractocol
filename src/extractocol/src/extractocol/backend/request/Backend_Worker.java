/*
package extractocol.backend.request;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

import extractocol.Constants;
import extractocol.backend.request.basics.EPcontainer;
import soot.SootMethod;

public class Backend_Worker
{
	public static final Logger logger = Logger.getLogger(Backend_Worker.class);
	public static ArrayList<String> paths = new ArrayList<String>();
	public static HashSet<String> VisitTable = new HashSet<String>();
	public Backend_Worker()
	{}
	public void Start_BackEnd(List<EPcontainer> epcontainerlist, boolean isForward) throws Exception
	{
		List<String> eplist = new ArrayList<String>();
		Iterator<EPcontainer> itr = epcontainerlist.iterator();
		while (itr.hasNext())
		{
			EPcontainer ep = itr.next();
			List<SootMethod> listsm = ep.getEPlist();
			Iterator<SootMethod> smitr = listsm.iterator();
			while (smitr.hasNext())
			{
				SootMethod sm = smitr.next();
				eplist.add(sm.toString());
			}
		}
		long start;
		long end;
		System.out.println("DP List");
		for (EPcontainer EPC : epcontainerlist)
		{
			System.out.println("DP Name : " + EPC.getDP().getSignature());
		}
		System.out.println();
		for (int i = 0; i < epcontainerlist.size(); i++)
		{
			EPcontainer EPC = epcontainerlist.get(i);
			System.out.println((i + 1) + "th DP : " + EPC.getDP());
			Set<SootMethod> ssm = (HashSet<SootMethod>) EPC.getMethodSet();
			Constants.taintset = new HashSet<SootMethod>(ssm);
			for (int j = 0; j < EPC.getEPlist().size(); j++)
			{
				start = System.currentTimeMillis();
				// SootClass sc = scs.get(i);
				if (EPC.getCallflow() != null && EPC.getCallflow().size() > 0)
				{
					Set<SootMethod> Setcallflow = EPC.getCallflow().get(j);
					Constants.callflow = Setcallflow;
				}
				Constants.allmethods = EPC.getMethodSet();
				System.out.println();
				System.out.println("\t" + (i + 1) + "-" + (j + 1) + "th EP : " + EPC.getEPlist().get(j));
				if (!isForward)
				{
					System.out.println("\tDP parent Method : " + EPC.getDP().getSignature());
					System.out.println("\tDP Stmt : " + EPC.DemarcationInvoke);
				}
				//Constants.EPcont = EPC;
				Constants.CurrentEP = EPC.getEPlist().get(j).getSignature();
				Constants.foundDPStmt = false;
				Constants.methodVisitCount = 0;
				VisitTable.clear();
				end = System.currentTimeMillis();
				if (Constants.stopVisitMethod())
				{
					System.out.println("\t stop visit method. Reached to max count " + Constants.maxMethodVisitCount);
				}
				System.out.println("\t" + (i + 1) + "-" + (j + 1) + "th EP : " + "Runtime : " + (end - start) / 1000.0 + " method visit count :" + Constants.methodVisitCount);
				System.out.println();
				System.out.println();
				System.out.println();
			}
			for (Iterator<SootMethod> remainsm = Constants.taintset.iterator(); itr.hasNext();)
			{
				System.out.println("REMAINS :" + remainsm.next());
			}
		}
		System.out.println("exit");
	}
}
*/