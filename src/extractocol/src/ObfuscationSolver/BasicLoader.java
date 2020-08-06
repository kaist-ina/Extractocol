package ObfuscationSolver;

import soot.SootClass;
import soot.SootMethod;

import java.util.ArrayList;

public class BasicLoader implements ILoader {

    protected ArrayList<String> _targetPackages;
    protected ArrayList<SootMethod> _targetMethods;

    @Override
    public ArrayList<SootClass> load() {
        return null;
    }

    protected void loadervalidation()
    {
        assert this._targetPackages != null : "target packages are not specified.";
    }

    public int setPackages(ArrayList<String> targetPackages)
    {
        _targetPackages = targetPackages;
        return _targetPackages.size();
    }
}
