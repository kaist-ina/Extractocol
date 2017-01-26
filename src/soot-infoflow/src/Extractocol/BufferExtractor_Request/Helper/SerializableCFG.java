
package Extractocol.BufferExtractor_Request.Helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.Basics.EPcontainer;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;

public class SerializableCFG
{
	private transient IInfoflowCFG iCfg;
	private List<CallerEntry> Callers = Lists.newLinkedList();
	private List<CalleeEntry> Callees = Lists.newLinkedList();
	private List<TaintEntry> TaintFunctions = Lists.newLinkedList();
	private List<CallEntry> CallFlows = Lists.newLinkedList();
	private transient List<EPcontainer> epcontainerlist;
	private HashMap<String, List<String>> SuperClassTable = new HashMap<String, List<String>>();
	private HashMap<String, List<String>> SubClassTable = new HashMap<String, List<String>>();
	private HashMap<String, List<String>> SubInterfaceTable = new HashMap<String, List<String>>();
	private HashMap<String, List<String>> SuperInterfaceTable = new HashMap<String, List<String>>();
	public SerializableCFG(IInfoflowCFG _iCfg, List<EPcontainer> _epcontainerlist)
	{
		iCfg = _iCfg;
		epcontainerlist = _epcontainerlist;
	}
	public void SerializeICfg()
	{
		System.out.println("Processing CalleeAndCallers");
		ProcessingCalleeAndCallers();
		System.out.println("Processing Super, Sub table");
		ProcessingSuperSubClass();
		System.out.println("Set TaintFunctions");
		SetTaintFunctions();
		System.out.println("Set CallFlows");
		SetCallFlows();
	}
	public void SetTaintFunctions()
	{
		for (EPcontainer epc : epcontainerlist)
		{
			TaintEntry Te = new TaintEntry();
			Te.DemarcationPoint = epc.getDP() == null ? null : epc.getDP().getSignature();
			Te.DpStmt = epc.getDPStmt() == null ? null : epc.getDPStmt().toString();
			for (SootMethod sm : epc.getMethodSet())
				Te.TaintFunctions.add(sm.getSignature());
			TaintFunctions.add(Te);
		}
	}
	public void SetCallFlows()
	{
		for (EPcontainer epc : epcontainerlist)
		{
			int i = 0;
			for (SootMethod ep : epc.getEPlist())
			{
				CallEntry CE = new CallEntry();
				if (ep == null)
					continue;
				CE.EntryPoint = ep.getSignature();
				if (epc.getCallflow().size() > 0)
				{
					for (SootMethod cf : epc.getCallflow().get(i))
					{
						CE.CallFuctions.add(cf.getSignature());
					}
				}
				i++;
				CallFlows.add(CE);
			}
		}
	}
	public ArrayList<String> GetCallFlows(String Entrypoint)
	{
		for (CallEntry Ce : CallFlows)
		{
			if (Ce.EntryPoint.equals(Entrypoint))
				return Ce.CallFuctions;
		}
		return null;
	}
	public ArrayList<String> GetTaintFunctions(String DemarcationPoint, String string)
	{
		for (TaintEntry Te : TaintFunctions)
		{
			if (Te.DemarcationPoint.equals(DemarcationPoint) && Te.DpStmt.equals(string))
				return Te.TaintFunctions;
		}
		return null;
	}
	public class TaintEntry
	{
		public String DemarcationPoint;
		public String DpStmt;
		public ArrayList<String> TaintFunctions = new ArrayList<String>();
	}
	public class CallEntry
	{
		public String EntryPoint;
		public ArrayList<String> CallFuctions = new ArrayList<String>();
	}
	public class CalleeEntry
	{
		public SerializableUnit Su;
		public List<String> Callees = Lists.newLinkedList();
		public CalleeEntry(SerializableUnit _Su)
		{
			Su = _Su;
		}
	}
	public class CallerEntry
	{
		public String DestinationMethod;
		public List<SerializableUnit> Callers = Lists.newLinkedList();
		public CallerEntry(String Destination)
		{
			DestinationMethod = Destination;
		}
	}
	public class SerializableUnit
	{
		public SerializableUnit(String _Method, String _Ut)
		{
			Ut = _Ut;
			ParentMethod = _Method;
		}
		@Override
		public boolean equals(Object obj)
		{
			SerializableUnit su = (SerializableUnit) obj;
			if (this.Ut.equals(su.Ut) && this.ParentMethod.equals(su.ParentMethod))
				return true;
			return false;
		}
		public String Ut;
		public String ParentMethod;
	}
	private void ProcessingSuperSubClass()
	{
		for (SootClass sc : Scene.v().getClasses())
		{
			if (sc.getName() != null)
			{
				if (sc.isConcrete())
				{
					// SuperClass list
					List<String> superlist = new ArrayList<String>();
					for (SootClass supers : Scene.v().getActiveHierarchy().getSuperclassesOf(sc))
						superlist.add(supers.getName());
					SuperClassTable.put(sc.getName(), superlist.size() == 0 ? null : superlist);
					// SubClass list
					List<String> sublist = new ArrayList<String>();
					for (SootClass subs : Scene.v().getActiveHierarchy().getSubclassesOf(sc))
						sublist.add(subs.getName());
					SubClassTable.put(sc.getName(), sublist.size() == 0 ? null : sublist);
				}
				else
					if (sc.isInterface())
					{
						// SuperInterface List
						try
						{
							List<String> superinterlist = new ArrayList<String>();
							for (SootClass superinters : Scene.v().getActiveHierarchy().getSuperinterfacesOf(sc))
								superinterlist.add(superinters.getName());
							SuperInterfaceTable.put(sc.getName(), superinterlist.size() == 0 ? null : superinterlist);
						}
						catch (Exception e)
						{
							// System.out.println("super inter list
							// exception!");
							continue;
						}
						// SubInterface list
						List<String> Subinterlist = new ArrayList<String>();
						for (SootClass Subinters : Scene.v().getActiveHierarchy().getSubinterfacesOf(sc))
							Subinterlist.add(Subinters.getName());
						SubInterfaceTable.put(sc.getName(), Subinterlist.size() == 0 ? null : Subinterlist);
					}
			}
		}
	}
	private void ProcessingCalleeAndCallers()
	{
		for (SootClass sc : Scene.v().getClasses())
		{
			for (SootMethod sm : sc.getMethods())
			{
				// For Callers
				Collection<Unit> callers = iCfg.getCallersOf(sm);
				CallerEntry CallerEt = new CallerEntry(sm.getSignature());
				if (callers.size() > 0)
				{
					for (Unit ut : callers)
					{
						SootMethod target = null;
						try
						{
							target = iCfg.getMethodOf(ut);
						}
						catch (Exception e)
						{
							// System.out.println("getMethodOf Exception");
							continue;
						}
						SerializableUnit sut = new SerializableUnit(target.getSignature(), ut.toString());
						CallerEt.Callers.add(sut);
					}
					Callers.add(CallerEt);
				}
				// For Callees
				if (!sm.hasActiveBody())
					continue;
				for (Unit ut : sm.getActiveBody().getUnits())
				{
					// Unit to Callees
					SerializableUnit Su = new SerializableUnit(sm.getSignature(), ut.toString());
					CalleeEntry CalleeEt = new CalleeEntry(Su);
					Collection<SootMethod> callees = iCfg.getCalleesOfCallAt(ut);
					if (callees.size() == 0)
						continue;
					for (SootMethod callee : callees)
						CalleeEt.Callees.add(callee.getSignature());
					Callees.add(CalleeEt);
				}
			}
		}
	}
	public List<String> getCalleesAt(String ParentMethod, String Unit)
	{
		SerializableUnit SUT = new SerializableUnit(ParentMethod, Unit);
		for (CalleeEntry CE : Callees)
		{
			if (CE.Su.equals(SUT))
			{
				return CE.Callees;
			}
		}
		return null;
	}
	public List<SerializableUnit> getCallersOf(String ParentMethod)
	{
		for (CallerEntry CE : Callers)
		{
			if (CE.DestinationMethod.equals(ParentMethod))
				return CE.Callers;
		}
		return null;
	}
	public SootMethod CopyMethod(SootMethod oldMethod)
	{
		SootMethod newMethod = new SootMethod(oldMethod.getName(), oldMethod.getParameterTypes(),
				oldMethod.getReturnType(), oldMethod.getModifiers(), oldMethod.getExceptions());
		JimpleBody body = Jimple.v().newBody(newMethod);
		body.importBodyContentsFrom(oldMethod.retrieveActiveBody());
		newMethod.setActiveBody(body);
		newMethod.setDeclaringClass(oldMethod.getDeclaringClass());
		newMethod.setDeclared(true);
		return newMethod;
	}
	public SootMethod getMethodOf(String MSig)
	{
		SootMethod target = null;
		SootClass sc = null;
		if (Constants.jimplePath().equals(""))
			return null;
		else
		{
			if (MSig.contains("dummyMainClass"))
				return null;
			String JimpleName = MSig.substring(1, MSig.indexOf(":"));
			
			sc = Constants.fsc.getSootClass(JimpleName);
			if (sc == null)
				return null;
			
			
			List<SootClass> arrSubs = Scene.v().getActiveHierarchy().getSuperclassesOf(sc);
			if (arrSubs == null)
				return null;
			
			for (SootMethod sm : sc.getMethods())
			{
				if (sm.getSignature().equals(MSig))
					target = sm;
			}
			if (target == null)
				for (SootClass subs : arrSubs)
					for (SootMethod sm : subs.getMethods())
					{
						if (sm.getSignature().equals(MSig))
							target = sm;
					}
		}
		return target;
	}
	public List<String> getSuperclassOf(String classname)
	{
		return SuperClassTable.get(classname);
	}
	public List<String> getSubclassOf(String classname)
	{
		return SubClassTable.get(classname);
	}
	public List<String> getSuperinterfaceOf(String classname)
	{
		return SuperInterfaceTable.get(classname);
	}
	public List<String> getSubinterfaceOf(String classname)
	{
		return SubInterfaceTable.get(classname);
	}
}
