package ObfuscationSolver;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import extractocol.Constants;
import extractocol.backend.request.helper.JimpleLoader;
import soot.Scene;
import soot.SootMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SolverCaller {
	static MappingTable mapTable = null;
    public static void main (String args[])
    {
        //build table
//        MappingTable mt = buildtable(args[0], true);
//        System.exit(0);

        //load table
        MappingTable mt = new MappingTable();
        mt.setmap(loadtable(args[0]));

        //load apk
        Constants.setAPPName(args[0]);
        JimpleLoader jl = new JimpleLoader(Constants.getAndroidSDKPath(), Constants.getAPKpath(args[0] + ".apk"), Constants.getSourcesAndSinksPath());

        ArrayList<String> targetsms = new ArrayList<>();
        targetsms.add("<rx.Observable: rx.Subscription a(rx.functions.Action1,rx.functions.Action1)>");
        targetsms.add("<rx.Observable: rx.Subscription c(rx.functions.Action1)>");
        targetsms.add("<rx.Observable: rx.Observable e(rx.functions.Func1)>");
        targetsms.add("<rx.Observable: rx.Observable f(rx.functions.Func1)>");
        targetsms.add("<rx.Observable: rx.Observable b(rx.functions.Action1)>");
        targetsms.add("<rx.Observable: rx.Observable g(rx.functions.Func1)>");

        for (String a : targetsms) {
            System.out.println(a + ":" + mt.solve(Scene.v().getMethod(a)));
        }
        System.exit(0);

    }

    /** Get de-obfuscated name of SootMethod
     * @see The active bodies MUST be retrived before using this method.
     * @param m method name
     * @return de-obfuscated name
     */
    public static String M(String m) {
    	try {
    		return M(Scene.v().getMethod(m));
    	}catch (Exception e) {
    		return m;
    	}
    }
    
    /** Get de-obfuscated name of SootMethod
     * 
     * @author Byungkwon Choi
     * @param sm SootMethod whose name is obfuscated
     * @return de-obfuscated name of SootMethod
     */
    public static String M(SootMethod sm) {
    	// 1. load mapTable if it is null
    	init();
    	
    	// 2. solve & return
    	return mapTable.solve(sm);
    }
    
    /** load mapTable if it is null
     * @author Byungkwon Choi
     */
    private static void init() {
    	if(mapTable != null)
    		return;
    	
    	mapTable = new MappingTable();
    	mapTable.setmap(loadtable(Constants.getAPPName()));
    }

    private static HashMap<String, HashSet<OriginCandidate>> loadtable(String appname)
    {
        Kryo kryo = new Kryo();
        Input input = null;
        HashMap<String, HashSet<OriginCandidate>> existmap = null;
        try {
            File f = new File(Constants.getObfsPath());

            if (f.exists()) {
                input = new Input(new FileInputStream(Constants.getObfsPath()));
                existmap = kryo.readObject(input, HashMap.class);
                input.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return existmap;
    }

    private static MappingTable buildtable(String appname, boolean isAPK)
    {
        MappingTable mt = new MappingTable();
        if (isAPK) {
            mt.setmap(loadtable(appname));
            Constants.setAPPName(appname);
            APKLoader apkloader = new APKLoader(Constants.getAndroidSDKPath(), Constants.getAPKpath(appname + ".apk"));
            ArrayList<String> targets = new ArrayList<>();
            targets.add("rx");
            targets.add("retrofit2");
            apkloader.setPackages(targets);
            apkloader.load();
            System.out.println(mt.build(apkloader));

        } else
        {
            // In case of DEX file.
        }
        Kryo kryo = new Kryo();
        Output output = null;
        try {
            output = new Output(new FileOutputStream(Constants.getObfsPath()));
            kryo.register(HashMap.class);
            kryo.writeObject(output, mt._map);
            output.close();
            return mt;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
