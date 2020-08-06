package ObfuscationSolver;

import extractocol.Constants;
import extractocol.frontend.basic.MyCallGraphBuilder;
import extractocol.tester.HeapHandling;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.infoflow.entryPointCreators.IEntryPointCreator;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

import java.io.File;
import java.util.ArrayList;

public class JarLoader extends BasicLoader{

    private String _jarpath;
    private String _apkpath;

    public JarLoader (String jarpath, String apkpath)
    {
        _jarpath = jarpath;
        _apkpath = apkpath;
    }

    @Override
    public ArrayList<SootClass> load() {
        loadervalidation();
        return null;
    }
}
