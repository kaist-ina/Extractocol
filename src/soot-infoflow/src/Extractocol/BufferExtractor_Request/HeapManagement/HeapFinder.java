//
//package Extractocol.BufferExtractor_Request.HeapManagement;
//
//import java.io.BufferedWriter;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Set;
//import com.esotericsoftware.kryo.Kryo;
//import com.esotericsoftware.kryo.io.Input;
//import com.esotericsoftware.kryo.io.Output;
//import soot.Body;
//import soot.BodyTransformer;
//import soot.PackManager;
//import soot.PatchingChain;
//import soot.Transform;
//import soot.Unit;
//import soot.jimple.AbstractStmtSwitch;
//import soot.jimple.AssignStmt;
//import soot.options.Options;
//
//public class HeapFinder
//{
//
//	public static void main(final String[] args)
//	{
//		String targetObjectPath = args[4];
//		final int Phase = Integer.valueOf(args[5]);
//		final Set<String> CurrentDpList = new HashSet<String>();
//		final SerializableHeapContainerList ShContainerList = getSerHeap(targetObjectPath);
//		final SerializableHeapContainer ShContainer = ShContainerList.get(Phase);
//		int lastslash = args[3].lastIndexOf("\\");
//		final String path = "D:\\new_extractocol\\src\\soot-infoflow-android\\SourcesAndSinks\\";
//		String appName = args[3].substring(lastslash + 1);
//		appName = appName.substring(0, appName.lastIndexOf("."));
//		appName += "_HeapDPs.txt";
//		final String appPath = path + appName;
//
//		Options.v().set_src_prec(Options.src_prec_apk);
//		// output as APK, too//-f J
//		Options.v().set_allow_phantom_refs(true);
//
//		System.out.println("Heap Names");
//		// JM phase 0일때만 Heap Name을 가져오고 이 후에는 value를 가져와야함.
//		ShContainerList.printCurrentHeapKeyValues(Phase);
//
//		PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer()
//		{
//
//			@Override
//			protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options)
//			{
//				final PatchingChain<Unit> units = b.getUnits();
//
//				// important to use snapshotIterator here
//				for (Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();)
//				{
//					final Unit u = iter.next();
//					u.apply(new AbstractStmtSwitch()
//					{
//
//						public void caseAssignStmt(AssignStmt stmt)
//						{
//							if (Phase == 0)
//							{
//								for (String heapkey : ShContainer.getHeapNames())
//								{
//									if (stmt.getLeftOp().toString().contains(heapkey))
//									{
//										System.out.println(b.getMethod().getSignature());
//										ShContainer.addDps(heapkey, b.getMethod().getSignature() + " -> _SOURCE_");
//										CurrentDpList.add(b.getMethod().getSignature() + " -> _SOURCE_");
//									}
//								}
//							}
//								
//							else
//							{
//								Set<String> keyset = ShContainer.getHeapList().keySet();
//
//								for (String key : keyset)
//								{
//									for (String values : ShContainer.getHeapList().get(key))
//									{
//
//										// String val = values.replaceAll("\\(",
//										// "").replaceAll("\\)", "");
//										
//										ArrayList<String> result = parsingHeapName(values);
//										
//										if (result == null)
//											continue;
//										
//										for (String val : result)
//										{
//											if (stmt.getLeftOp().toString().contains(val))
//											{
//												System.out.println(b.getMethod().getSignature());
//												ShContainer.addDps(val, b.getMethod().getSignature() + " -> _SOURCE_");
//												CurrentDpList.add(b.getMethod().getSignature() + " -> _SOURCE_");
//											}
//										}
//									}
//								}
//							}
//						}
//					});
//				}
//			}
//		}));
//		soot.Main.main(args);
//		try
//		{
//			SerializeHeapList(ShContainerList, targetObjectPath);
//			BufferedWriter file = new BufferedWriter(new FileWriter(appPath));
//			for (String dp : CurrentDpList)
//			{
//				file.write(dp);
//				file.newLine();
//			}
//			file.close();
//		} catch (Exception e)
//		{
//			// JM Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	private static SerializableHeapContainerList getSerHeap(String targetObjectPath)
//	{
//		SerializableHeapContainerList Shcontainer = null;
//		Kryo kryo = new Kryo();
//		Input input;
//		try
//		{
//			input = new Input(new FileInputStream(targetObjectPath));
//			Shcontainer = kryo.readObject(input, SerializableHeapContainerList.class);
//			input.close();
//		} catch (FileNotFoundException e1)
//		{
//			// JM Auto-generated catch block
//			e1.printStackTrace();
//		}
//
//		return Shcontainer;
//	}
//
//	private static ArrayList<String> parsingHeapName(String url)
//	{
//		if (!url.contains("<") && !url.contains(">"))
//			return null;
//
//		ArrayList<String> result = new ArrayList<String>();
//		String remains = url;
//
//		while (remains.length() > 0)
//		{
//			int firstLt = remains.indexOf("<");
//			int firstGt = remains.indexOf(">");
//
//			result.add(remains.substring(firstLt, firstGt + 1));
//			remains = remains.substring(firstGt + 1);
//
//			if (!remains.contains("<") && !remains.contains(">"))
//				break;
//		}
//
//		return result;
//	}
//
//	private static boolean SerializeHeapList(SerializableHeapContainerList ShContainerList, String targetObjectPath) throws FileNotFoundException
//	{
//		Kryo kryo = new Kryo();
//		Output output = new Output(new FileOutputStream(targetObjectPath));
//		kryo.writeObject(output, ShContainerList);
//		output.close();
//
//		return true;
//	}
//}
