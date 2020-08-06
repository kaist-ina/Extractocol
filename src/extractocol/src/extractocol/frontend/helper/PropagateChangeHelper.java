package extractocol.frontend.helper;

import heros.InterproceduralCFG;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.InvokeStmt;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Jeongmin Kim on 2017-03-13.
 */
public class PropagateChangeHelper {
    public static boolean isSystemAPI(InvokeStmt invokeStmt)
    {
        // We consider APIs which are included in above namespaces as the system APIs.
                String methodSig = invokeStmt.getInvokeExpr().getMethodRef().getSignature();
        if (methodSig.startsWith("<android.") || methodSig.startsWith("java.")
                || methodSig.startsWith("javax.")
                || methodSig.startsWith("sun.")
                || methodSig.startsWith("com.google.")
                || methodSig.startsWith("org.omg.")
                || methodSig.startsWith("org.w3c.dom."))
            return true;

        return false;
    }

    public static boolean hasImplementation(InvokeStmt invokeStmt, InterproceduralCFG icfg)
    {
        Collection<SootMethod> list = icfg.getCalleesOfCallAt(invokeStmt);
        for (SootMethod sm : list)
        {
            if (!sm.isConcrete() || StubMethodHandler.IsStubMethod(sm.getSignature()))
                return true;
        }
        return false;
    }

    public static boolean isPropagateChange(InvokeStmt invokeStmt, InterproceduralCFG icfg)
    {
        if (isSystemAPI(invokeStmt))
            return true;
        else if (hasImplementation(invokeStmt, icfg))
            return true;
        else
            return false;
    }
}
