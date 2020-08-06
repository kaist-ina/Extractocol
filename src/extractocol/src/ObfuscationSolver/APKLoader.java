package ObfuscationSolver;

import extractocol.Constants;
import extractocol.backend.request.helper.JimpleLoader;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import java.util.ArrayList;

public class APKLoader extends BasicLoader {
    private String _jarpath;
    private String _apkpath;
    public APKLoader (String jarpath, String apkpath)
    {
        _jarpath = jarpath;
        _apkpath = apkpath;
    }

    @Override
    public ArrayList<SootClass> load() {
        JimpleLoader jl = new JimpleLoader(_jarpath, _apkpath,Constants.getSourcesAndSinksPath());
        ArrayList<SootClass> result = new ArrayList<>();

        for (SootClass sc : Scene.v().getClasses())
        {
            for ( String prefix : this._targetPackages) {
                if (sc.getName().startsWith(prefix)) {
                    result.add(sc);
                }
            }
        }
        return result;
    }
}
