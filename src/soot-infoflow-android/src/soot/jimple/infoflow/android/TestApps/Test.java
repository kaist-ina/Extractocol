/*******************************************************************************
 * Copyright (c) 2012 Secure Software Engineering Group at EC SPRIDE.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors: Christian Fritz, Steven Arzt, Siegfried Rasthofer, Eric
 * Bodden, and others.
 ******************************************************************************/

package soot.jimple.infoflow.android.TestApps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.xmlpull.v1.XmlPullParserException;

import Extractocol.Constants;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapTreeNode;
import Extractocol.BufferExtractor_Request.HeapManagement.HeapTreeNode.SourceforTaint;
import Extractocol.BufferExtractor_Request.Semantic.Retrofit.retrofit_http_old;
import Extractocol.Outputs.RequestInfoEntry;
import Extractocol.Outputs.RequestInfoList;
import Extractocol.Outputs.ResponseInfoList;
import soot.jimple.infoflow.IInfoflow.CallgraphAlgorithm;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.source.AndroidSourceSinkManager.LayoutMatchingMode;
import soot.jimple.infoflow.data.pathBuilders.DefaultPathBuilderFactory.PathBuilder;
import soot.jimple.infoflow.handlers.ResultsAvailableHandler;
import soot.jimple.infoflow.ipc.IIPCManager;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSinkInfo;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.infoflow.solver.cfg.IInfoflowCFG;
import soot.jimple.infoflow.taintWrappers.EasyTaintWrapper;
import soot.jimple.infoflow.taintWrappers.ITaintPropagationWrapper;
import soot.jimple.infoflow.taintWrappers.TaintWrapperSet;
import soot.jimple.infoflow.pairing.ReconstructHttpTransaction;

public class Test
{
	private static final class MyResultsAvailableHandler implements ResultsAvailableHandler
	{
		private final BufferedWriter wr;
		private MyResultsAvailableHandler()
		{
			this.wr = null;
		}
		private MyResultsAvailableHandler(BufferedWriter wr)
		{
			this.wr = wr;
		}
		@Override
		public void onResultsAvailable(IInfoflowCFG cfg, InfoflowResults results)
		{
			// Dump the results
			if (results == null)
			{
				print("No results found.");
			}
			else
			{
				for (ResultSinkInfo sink : results.getResults().keySet())
				{
					print("Found a flow to sink " + sink + ", from the following sources:");
					for (ResultSourceInfo source : results.getResults().get(sink))
					{
						print("\t- " + source.getSource() + " (in " + cfg.getMethodOf(source.getSource()).getSignature() + ")");
						if (source.getPath() != null && !source.getPath().isEmpty())
							print("\t\ton Path " + source.getPath());
					}
				}
			}
		}
		private void print(String string)
		{
			try
			{
				System.out.println(string);
				if (wr != null)
					wr.write(string + "\n");
			}
			catch (IOException ex)
			{
				// ignore
			}
		}
	}
	static String command;
	static boolean generate = false;
	private static int timeout = -1;
	private static int sysTimeout = -1;
	private static boolean stopAfterFirstFlow = false;
	private static boolean implicitFlows = false;
	private static boolean staticTracking = true;
	private static boolean enableCallbacks = true;
	private static boolean enableExceptions = true;
	private static int accessPathLength = 5;
	private static LayoutMatchingMode layoutMatchingMode = LayoutMatchingMode.MatchSensitiveOnly;
	private static boolean flowSensitiveAliasing = true;
	private static boolean computeResultPaths = true;
	private static boolean aggressiveTaintWrapper = false;
	private static boolean librarySummaryTaintWrapper = false;
	private static boolean backwardSlice = false;
	private static boolean pairing = false;
	private static String summaryPath = "";
	private static PathBuilder pathBuilder = PathBuilder.ContextInsensitiveSourceFinder;
	private static CallgraphAlgorithm callgraphAlgorithm = CallgraphAlgorithm.AutomaticSelection;
	private static boolean DEBUG = false;
	private static IIPCManager ipcManager = null;
	public static void setIPCManager(IIPCManager ipcManager)
	{
		Test.ipcManager = ipcManager;
	}
	public static IIPCManager getIPCManager()
	{
		return Test.ipcManager;
	}
	/**
	 * @param args
	 *            Program arguments. args[0] = path to apk-file, args[1] = path to android-dir (path/android-platforms/)
	 */
	public static void main(final String[] args) throws IOException, InterruptedException
	{
		if (args.length < 2)
		{
			printUsage();
			return;
		}
		long start = System.currentTimeMillis();
		// start with cleanup:
		File outputDir = new File("JimpleOutput");
		if (outputDir.isDirectory())
		{
			boolean success = true;
			for (File f : outputDir.listFiles())
			{
				success = success && f.delete();
			}
			if (!success)
			{
				System.err.println("Cleanup of output directory " + outputDir + " failed!");
			}
			outputDir.delete();
		}
		// Parse additional command-line arguments
		if (!parseAdditionalOptions(args))
			return;
		if (!validateAdditionalOptions())
			return;
		List<String> apkFiles = new ArrayList<String>();
		File apkFile = new File(args[0]);
		if (apkFile.isDirectory())
		{
			String[] dirFiles = apkFile.list(new FilenameFilter()
			{
				@Override
				public boolean accept(File dir, String name)
				{
					return (name.endsWith(".apk"));
				}
			});
			for (String s : dirFiles)
				apkFiles.add(s);
		}
		else
		{
			// apk is a file so grab the extension
			String extension = apkFile.getName().substring(apkFile.getName().lastIndexOf("."));
			if (extension.equalsIgnoreCase(".txt"))
			{
				BufferedReader rdr = new BufferedReader(new FileReader(apkFile));
				String line = null;
				while ((line = rdr.readLine()) != null)
					apkFiles.add(line);
				rdr.close();
			}
			else
				if (extension.equalsIgnoreCase(".apk"))
					apkFiles.add(args[0]);
				else
				{
					System.err.println("Invalid input file format: " + extension);
					return;
				}
		}
		for (final String fileName : apkFiles)
		{
			final String fullFilePath;
			// Directory handling
			if (apkFiles.size() > 1)
			{
				if (apkFile.isDirectory())
					fullFilePath = args[0] + File.separator + fileName;
				else
					fullFilePath = fileName;
				System.out.println("Analyzing file " + fullFilePath + "...");
				File flagFile = new File("_Run_" + new File(fileName).getName());
				if (flagFile.exists())
					continue;
				flagFile.createNewFile();
			}
			else
				fullFilePath = fileName;
			// Run the analysis
			if (timeout > 0)
				runAnalysisTimeout(fullFilePath, args[1]);
			else
				if (sysTimeout > 0)
					runAnalysisSysTimeout(fullFilePath, args[1]);
				else
					runAnalysis(fullFilePath, args[1]);
			System.gc();
		}
		long end = System.currentTimeMillis();
		System.out.println("Runtime: " + (end - start) / 1000.0);
	}
	private static boolean parseAdditionalOptions(String[] args)
	{
		int i = 2;
		while (i < args.length)
		{
			if (args[i].equalsIgnoreCase("--timeout"))
			{
				timeout = Integer.valueOf(args[i + 1]);
				i += 2;
			}
			else
				if (args[i].equalsIgnoreCase("--systimeout"))
				{
					sysTimeout = Integer.valueOf(args[i + 1]);
					i += 2;
				}
				else
					if (args[i].equalsIgnoreCase("--singleflow"))
					{
						stopAfterFirstFlow = true;
						i++;
					}
					else
						if (args[i].equalsIgnoreCase("--implicit"))
						{
							implicitFlows = true;
							i++;
						}
						else
							if (args[i].equalsIgnoreCase("--nostatic"))
							{
								staticTracking = false;
								i++;
							}
							else
								if (args[i].equalsIgnoreCase("--aplength"))
								{
									accessPathLength = Integer.valueOf(args[i + 1]);
									i += 2;
								}
								else
									if (args[i].equalsIgnoreCase("--cgalgo"))
									{
										String algo = args[i + 1];
										if (algo.equalsIgnoreCase("AUTO"))
											callgraphAlgorithm = CallgraphAlgorithm.AutomaticSelection;
										else
											if (algo.equalsIgnoreCase("CHA"))
												callgraphAlgorithm = CallgraphAlgorithm.CHA;
											else
												if (algo.equalsIgnoreCase("VTA"))
													callgraphAlgorithm = CallgraphAlgorithm.VTA;
												else
													if (algo.equalsIgnoreCase("RTA"))
														callgraphAlgorithm = CallgraphAlgorithm.RTA;
													else
														if (algo.equalsIgnoreCase("SPARK"))
															callgraphAlgorithm = CallgraphAlgorithm.SPARK;
														else
														{
															System.err.println("Invalid callgraph algorithm");
															return false;
														}
										i += 2;
									}
									else
										if (args[i].equalsIgnoreCase("--nocallbacks"))
										{
											enableCallbacks = false;
											i++;
										}
										else
											if (args[i].equalsIgnoreCase("--noexceptions"))
											{
												enableExceptions = false;
												i++;
											}
											else
												if (args[i].equalsIgnoreCase("--layoutmode"))
												{
													String algo = args[i + 1];
													if (algo.equalsIgnoreCase("NONE"))
														layoutMatchingMode = LayoutMatchingMode.NoMatch;
													else
														if (algo.equalsIgnoreCase("PWD"))
															layoutMatchingMode = LayoutMatchingMode.MatchSensitiveOnly;
														else
															if (algo.equalsIgnoreCase("ALL"))
																layoutMatchingMode = LayoutMatchingMode.MatchAll;
															else
															{
																System.err.println("Invalid layout matching mode");
																return false;
															}
													i += 2;
												}
												else
													if (args[i].equalsIgnoreCase("--aliasflowins"))
													{
														flowSensitiveAliasing = false;
														i++;
													}
													else
														if (args[i].equalsIgnoreCase("--nopaths"))
														{
															computeResultPaths = false;
															i++;
														}
														else
															if (args[i].equalsIgnoreCase("--aggressivetw"))
															{
																aggressiveTaintWrapper = false;
																i++;
															}
															else
																if (args[i].equalsIgnoreCase("--pathalgo"))
																{
																	String algo = args[i + 1];
																	if (algo.equalsIgnoreCase("CONTEXTSENSITIVE"))
																		pathBuilder = PathBuilder.ContextSensitive;
																	else
																		if (algo.equalsIgnoreCase("CONTEXTINSENSITIVE"))
																			pathBuilder = PathBuilder.ContextInsensitive;
																		else
																			if (algo.equalsIgnoreCase("SOURCESONLY"))
																				pathBuilder = PathBuilder.ContextInsensitiveSourceFinder;
																			else
																			{
																				System.err.println("Invalid path reconstruction algorithm");
																				return false;
																			}
																	i += 2;
																}
																else
																	if (args[i].equalsIgnoreCase("--libsumtw"))
																	{
																		librarySummaryTaintWrapper = true;
																		i++;
																	}
																	else
																		if (args[i].equalsIgnoreCase("--summarypath"))
																		{
																			summaryPath = args[i + 1];
																			i += 2;
																		}
																		else
																			if (args[i].equalsIgnoreCase("--backward"))
																			{
																				backwardSlice = true;
																				Constants.serIsForward = false;
																				i++;
																			}
																			else
																				if (args[i].equalsIgnoreCase("--pairing"))
																				{
																					pairing = true;
																					i++;
																				}
																				else
																					if (args[i].equalsIgnoreCase("--serialization"))
																					{
																						Constants.Serializing = true;
																						i++;
																					}
																					else
																						if (args[i].equalsIgnoreCase("--heapobject"))
																						{
																							Constants.heapobject = true;
																							i++;
																						}
																						else
																							i++;
		}
		return true;
	}
	private static boolean validateAdditionalOptions()
	{
		if (timeout > 0 && sysTimeout > 0)
		{
			return false;
		}
		if (!flowSensitiveAliasing && callgraphAlgorithm != CallgraphAlgorithm.OnDemand && callgraphAlgorithm != CallgraphAlgorithm.AutomaticSelection)
		{
			System.err.println("Flow-insensitive aliasing can only be configured for callgraph " + "algorithms that support this choice.");
			return false;
		}
		if (librarySummaryTaintWrapper && summaryPath.isEmpty())
		{
			System.err.println("Summary path must be specified when using library summaries");
			return false;
		}
		return true;
	}
	private static void runAnalysisTimeout(final String fileName, final String androidJar)
	{
		FutureTask<InfoflowResults> task = new FutureTask<InfoflowResults>(new Callable<InfoflowResults>()
		{
			@Override
			public InfoflowResults call() throws Exception
			{
				final BufferedWriter wr = new BufferedWriter(new FileWriter("_out_" + new File(fileName).getName() + ".txt"));
				try
				{
					final long beforeRun = System.nanoTime();
					wr.write("Running data flow analysis...\n");
					final InfoflowResults res = runAnalysis(fileName, androidJar);
					wr.write("Analysis has run for " + (System.nanoTime() - beforeRun) / 1E9 + " seconds\n");
					wr.flush();
					return res;
				}
				finally
				{
					if (wr != null)
						wr.close();
				}
			}
		});
		ExecutorService executor = Executors.newFixedThreadPool(1);
		executor.execute(task);
		try
		{
			System.out.println("Running infoflow task...");
			task.get(timeout, TimeUnit.MINUTES);
		}
		catch (ExecutionException e)
		{
			System.err.println("Infoflow computation failed: " + e.getMessage());
			e.printStackTrace();
		}
		catch (TimeoutException e)
		{
			System.err.println("Infoflow computation timed out: " + e.getMessage());
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			System.err.println("Infoflow computation interrupted: " + e.getMessage());
			e.printStackTrace();
		}
		// Make sure to remove leftovers
		executor.shutdown();
	}
	private static void runAnalysisSysTimeout(final String fileName, final String androidJar)
	{
		String classpath = System.getProperty("java.class.path");
		String javaHome = System.getProperty("java.home");
		String executable = "/usr/bin/timeout";
		String[] command = new String[]
		{ executable, "-s", "KILL", sysTimeout + "m", javaHome + "/bin/java", "-cp", classpath, "soot.jimple.infoflow.android.TestApps.Test", fileName, androidJar,
				stopAfterFirstFlow ? "--singleflow" : "--nosingleflow", implicitFlows ? "--implicit" : "--noimplicit", staticTracking ? "--static" : "--nostatic", "--aplength",
				Integer.toString(accessPathLength), "--cgalgo", callgraphAlgorithmToString(callgraphAlgorithm), enableCallbacks ? "--callbacks" : "--nocallbacks",
				enableExceptions ? "--exceptions" : "--noexceptions", "--layoutmode", layoutMatchingModeToString(layoutMatchingMode), flowSensitiveAliasing ? "--aliasflowsens" : "--aliasflowins",
				computeResultPaths ? "--paths" : "--nopaths", aggressiveTaintWrapper ? "--aggressivetw" : "--nonaggressivetw", "--pathalgo", pathAlgorithmToString(pathBuilder) };
		System.out.println("Running command: " + executable + " " + command);
		try
		{
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.redirectOutput(new File("_out_" + new File(fileName).getName() + ".txt"));
			pb.redirectError(new File("err_" + new File(fileName).getName() + ".txt"));
			Process proc = pb.start();
			proc.waitFor();
		}
		catch (IOException ex)
		{
			System.err.println("Could not execute timeout command: " + ex.getMessage());
			ex.printStackTrace();
		}
		catch (InterruptedException ex)
		{
			System.err.println("Process was interrupted: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	private static String callgraphAlgorithmToString(CallgraphAlgorithm algorihm)
	{
		switch (algorihm)
		{
			case AutomaticSelection:
				return "AUTO";
			case CHA:
				return "CHA";
			case VTA:
				return "VTA";
			case RTA:
				return "RTA";
			case SPARK:
				return "SPARK";
			default:
				return "unknown";
		}
	}
	private static String layoutMatchingModeToString(LayoutMatchingMode mode)
	{
		switch (mode)
		{
			case NoMatch:
				return "NONE";
			case MatchSensitiveOnly:
				return "PWD";
			case MatchAll:
				return "ALL";
			default:
				return "unknown";
		}
	}
	private static String pathAlgorithmToString(PathBuilder pathBuilder)
	{
		switch (pathBuilder)
		{
			case ContextSensitive:
				return "CONTEXTSENSITIVE";
			case ContextInsensitive:
				return "CONTEXTINSENSITIVE";
			case ContextInsensitiveSourceFinder:
				return "SOURCESONLY";
			default:
				return "UNKNOWN";
		}
	}
	private static InfoflowResults runAnalysis(final String fileName, final String androidJar)
	{
		try
		{
			final long beforeRun = System.nanoTime();
			final SetupApplication app;
			if (null == ipcManager)
			{
				app = new SetupApplication(androidJar, fileName);
			}
			else
			{
				app = new SetupApplication(androidJar, fileName, ipcManager);
			}
			String[] parts = fileName.split("/");
			Constants.apkName = parts[parts.length - 1].split("\\.")[0];
			app.setStopAfterFirstFlow(stopAfterFirstFlow);
			app.setEnableImplicitFlows(implicitFlows);
			app.setEnableStaticFieldTracking(staticTracking);
			app.setEnableCallbacks(enableCallbacks);
			app.setEnableExceptionTracking(enableExceptions);
			app.setAccessPathLength(accessPathLength);
			app.setLayoutMatchingMode(layoutMatchingMode);
			app.setFlowSensitiveAliasing(flowSensitiveAliasing);
			app.setPathBuilder(pathBuilder);
			app.setComputeResultPaths(computeResultPaths);
			app.setBackwardSlice(backwardSlice);
			app.setPairing(pairing);
			final ITaintPropagationWrapper taintWrapper;
			if (librarySummaryTaintWrapper)
			{
				taintWrapper = createLibrarySummaryTW();
			}
			else
			{
				final EasyTaintWrapper easyTaintWrapper;
				if (new File("../soot-infoflow/EasyTaintWrapperSource.txt").exists())
					easyTaintWrapper = new EasyTaintWrapper("../soot-infoflow/EasyTaintWrapperSource.txt");
				else
					easyTaintWrapper = new EasyTaintWrapper("EasyTaintWrapperSource.txt");
				easyTaintWrapper.setAggressiveMode(aggressiveTaintWrapper);
				taintWrapper = easyTaintWrapper;
			}
			app.setTaintWrapper(taintWrapper);
			// retrofit DP making
			if (Constants.isRetrofitDP)
			{
				ArrayList<retrofit_http_old> retrofit_results = new ArrayList<retrofit_http_old>();
				File[] files = new File("D:/new_extractocol/tools/dex2jar-2.0/dex2jar-2.0/" + Constants.apkName + "-dex2jar.src/").listFiles();
				ArrayList<String> result = retrofit_http_old.retrofit_file_finder(files, "mobi.ifunny");
				// retrofit method loading
				for (String retrofit_files : result)
					retrofit_results.addAll(retrofit_http_old.retrofitparser(retrofit_files));
				File f = new File("tempsource_sinks.txt");
				if (f.isFile())
				{
					f.delete();
				}
				for (retrofit_http_old retrofits : retrofit_results)
				{
					BufferedWriter file = new BufferedWriter(new FileWriter("tempsource_sinks.txt", true));
					String input = retrofits.methodref + " -> _SOURCE_";
					file.write(input);
					file.newLine();
					file.close();
				}
			}
			if (Constants.isRxJavaDP)
			{
				for (int i = 0; i < Constants.RxJavaDP.length; i++)
				{
					BufferedWriter file = new BufferedWriter(new FileWriter("tempsource_sinks.txt", true));
					String input = Constants.RxJavaDP[i] + " -> _SOURCE_";
					file.write(input);
					file.newLine();
					file.close();
				}
			}

			
			//JM dummy sourceandsinks. not used. this block should be removed.
			if (Constants.heapobject)
			{
				String path = ConvertSourceAndSeedObjectToText();
				if (path != null)
					app.calculateSourcesSinksEntrypoints(path);
			}
			// BK For pairing
			else if (pairing)
			{
				// 1. Generate the SourcesAndSinks.txt from the pairing information
				// 1-1. Load the pairing information (de-serialization)
				System.out.println("Loading pairing information...");
				RequestInfoList.LoadFromFile();
				ResponseInfoList.LoadFromFile();
				
				// 1-2. Generate the SourcesAndSinks.txt
				ReconstructHttpTransaction.makeSourcesAndSinks2();
				
				// 2. Read the SourcesAndSinks.txt
				app.calculateSourcesSinksEntrypoints(ReconstructHttpTransaction.pairingSourcesAndSinksFile);
			}
			else
			{
				// app.calculateSourcesSinksEntrypoints("SourcesAndSinks/" +
				// Constants.apkName + ".txt");
				if (Constants.isRetrofitDP || Constants.isRxJavaDP)
					app.calculateSourcesSinksEntrypoints("tempsource_sinks.txt");
				else
					app.calculateSourcesSinksEntrypoints("SourcesAndSinks/" + Constants.apkName + ".txt");
			}
			// app.calculateSourcesSinksEntrypoints("SuSiExport.xml");
			if (DEBUG)
			{
				app.printEntrypoints();
				app.printSinks();
				app.printSources();
			}
			System.out.println("Running data flow analysis...");
			final InfoflowResults res = app.runInfoflow(new MyResultsAvailableHandler());
			System.out.println("Analysis has run for " + (System.nanoTime() - beforeRun) / 1E9 + " seconds");
			return res;
		}
		catch (IOException ex)
		{
			System.err.println("Could not read file: " + ex.getMessage());
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		catch (XmlPullParserException ex)
		{
			System.err.println("Could not read Android manifest file: " + ex.getMessage());
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}
	/**
	 * This method is used to convert serialized SourceAndSeed list to string array.
	 * 
	 * @author Jeongmin Kim
	 * @version 1.00
	 */
	private static String ConvertSourceAndSeedObjectToText()
	{
		try
		{
			String path = "c:\\Users\\brad\\src.txt";
			BufferedWriter out = new BufferedWriter(new FileWriter(path));
			String s = "";
			
			//JM this block should be refactoried. !! refer to soot unit handler.
			RequestInfoList.lstRequestInfo = RequestInfoList.Deserialize();
			for (RequestInfoEntry reqEntry : RequestInfoList.lstRequestInfo)
			{
				Collection<HeapTreeNode> inorder = reqEntry.getHeapTree().inOrderTraversal();
				for (HeapTreeNode node : inorder)
				{
					{
						if (!node.isAnalyzed() && node.getstrThisHeap() != null && node.getLstTaintSourceInfo().size() > 0)
							System.out.println(node.getLstTaintSourceInfo().get(0).getUnit());

						for (SourceforTaint sft : node.getLstTaintSourceInfo())
						{
							s = sft.getSourceMethod() + " -> _SOURCE_";
							out.write(s);
							out.newLine();
						}
					}
				}
			}
			out.close();
			return path;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Creates the taint wrapper for using library summaries
	 * 
	 * @return The taint wrapper for using library summaries
	 * @throws IOException
	 *             Thrown if one of the required files could not be read
	 */
	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	private static ITaintPropagationWrapper createLibrarySummaryTW() throws IOException
	{
		try
		{
			Class clzLazySummary = Class.forName("soot.jimple.infoflow.methodSummary.data.impl.LazySummary");
			Object lazySummary = clzLazySummary.getConstructor(File.class).newInstance(new File(summaryPath));
			ITaintPropagationWrapper summaryWrapper = (ITaintPropagationWrapper) Class.forName("soot.jimple.infoflow.methodSummary.taintWrappers.SummaryTaintWrapper").getConstructor(clzLazySummary)
					.newInstance(lazySummary);
			final TaintWrapperSet taintWrapperSet = new TaintWrapperSet();
			taintWrapperSet.addWrapper(summaryWrapper);
			taintWrapperSet.addWrapper(new EasyTaintWrapper("EasyTaintWrapperConversion.txt"));
			return taintWrapperSet;
		}
		catch (ClassNotFoundException | NoSuchMethodException ex)
		{
			System.err.println("Could not find library summary classes: " + ex.getMessage());
			ex.printStackTrace();
			return null;
		}
		catch (InvocationTargetException ex)
		{
			System.err.println("Could not initialize library summaries: " + ex.getMessage());
			ex.printStackTrace();
			return null;
		}
		catch (IllegalAccessException | InstantiationException ex)
		{
			System.err.println("Internal error in library summary initialization: " + ex.getMessage());
			ex.printStackTrace();
			return null;
		}
	}
	private static void printUsage()
	{
		System.out.println("FlowDroid (c) Secure Software Engineering Group @ EC SPRIDE");
		System.out.println();
		System.out.println("Incorrect arguments: [0] = apk-file, [1] = android-jar-directory");
		System.out.println("Optional further parameters:");
		System.out.println("\t--TIMEOUT n Time out after n seconds");
		System.out.println("\t--SYSTIMEOUT n Hard time out (kill process) after n seconds, Unix only");
		System.out.println("\t--SINGLEFLOW Stop after finding first leak");
		System.out.println("\t--IMPLICIT Enable implicit flows");
		System.out.println("\t--NOSTATIC Disable static field tracking");
		System.out.println("\t--NOEXCEPTIONS Disable exception tracking");
		System.out.println("\t--APLENGTH n Set access path length to n");
		System.out.println("\t--CGALGO x Use callgraph algorithm x");
		System.out.println("\t--NOCALLBACKS Disable callback analysis");
		System.out.println("\t--LAYOUTMODE x Set UI control analysis mode to x");
		System.out.println("\t--ALIASFLOWINS Use a flow insensitive alias search");
		System.out.println("\t--NOPATHS Do not compute result paths");
		System.out.println("\t--AGGRESSIVETW Use taint wrapper in aggressive mode");
		System.out.println("\t--PATHALGO Use path reconstruction algorithm x");
		System.out.println("\t--LIBSUMTW Use library summary taint wrapper");
		System.out.println("\t--SUMMARYPATH Path to library summaries");
		System.out.println();
		System.out.println("Supported callgraph algorithms: AUTO, CHA, RTA, VTA, SPARK");
		System.out.println("Supported layout mode algorithms: NONE, PWD, ALL");
		System.out.println("Supported path algorithms: CONTEXTSENSITIVE, CONTEXTINSENSITIVE, SOURCESONLY");
	}
}
