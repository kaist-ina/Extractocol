package ObfuscationSolver;

import soot.SootClass;
import soot.SootMethod;

import java.util.ArrayList;

public interface ILoader {
    ArrayList<SootClass> load();
}
