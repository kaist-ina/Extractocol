package extractocol.frontend.TransitionStrategy.ForwardSolver.CallToReturnFlowRule;

import extractocol.frontend.TransitionStrategy.StmtProcessor;
import extractocol.frontend.output.basic.TaintResultEntry;
import extractocol.frontend.problem.AliasUtil;
import soot.CallbackCandidateFinder;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.jimple.infoflow.aliasing.Aliasing;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.problems.InfoflowProblem;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017-04-17.
 */
public class TransitionRule extends StmtProcessor {

    private Abstraction d1, source;
    private Unit src;
    private SootMethod thisMethod;
    private Aliasing aliasing;

    public TransitionRule(Stmt _src, Abstraction _d1, Abstraction _source, SootMethod _thisMethod, Aliasing _aliasing)
    {
        super((Stmt)_src);
        src = _src;
        d1 = _d1;
        source = _source;
        aliasing = _aliasing;
        thisMethod = _thisMethod;
        AnalysisStmt(this.stmt);
    }

    @Override
    public void AssignStmtHandler(AssignStmt stmt) {
        if (stmt.getRightOp() instanceof InstanceInvokeExpr )
            InstanceInvokeExprHandler((InstanceInvokeExpr) stmt.getRightOp());
    }

    @Override
    public void DefineStmtHandler(DefinitionStmt stmt) {
    }

    @Override
    public void InstanceInvokeExprHandler(InstanceInvokeExpr iie) {
        // Wish case 1
        // if  virtualinvoke $r3.<java.util.concurrent.ConcurrentHashMap: java.lang.Object put(java.lang.Object,java.lang.Object)>($r1, $r2)
        // $r1, $r2 are tainted. And then, pass to backwardSolver
        if (CallbackCandidateFinder.isClassInSystemPackage(iie.getMethodRef().declaringClass().toString())&& iie.getBase().equals(source.getAccessPath().getPlainValue())) {
            for (Value param : iie.getArgs()) {
                if (!(param instanceof Constant)) {
                    Abstraction newAbs = source.deriveNewAbstraction(param, true, (Stmt) src, param.getType());
                    res.add(newAbs);
                    aliasing.computeAliases(d1, (Stmt) src, param, res, thisMethod, newAbs);
                }
            }
        }
    }

    @Override
    public void Other(Stmt stmt) {
    }
}
