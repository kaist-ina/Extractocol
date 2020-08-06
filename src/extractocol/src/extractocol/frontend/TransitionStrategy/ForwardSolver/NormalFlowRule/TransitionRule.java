package extractocol.frontend.TransitionStrategy.ForwardSolver.NormalFlowRule;

import extractocol.frontend.TransitionStrategy.StmtProcessor;
import extractocol.frontend.problem.MyInfoflowProblem;
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
 * Created by Jeongmin on 2017-04-15.
 */
public class TransitionRule extends StmtProcessor{

    private Abstraction d1, source;
    private Unit src;
    private Aliasing aliasing;

    public TransitionRule(Stmt _src, Abstraction _d1, Abstraction _source, Aliasing _aliasing)
    {
        super((Stmt)_src);
        src = _src;
        d1 = _d1;
        source = _source;
        aliasing = _aliasing;
        AnalysisStmt(this.stmt);
    }

    @Override
    public void AssignStmtHandler(AssignStmt stmt) {
    }

    @Override
    public void DefineStmtHandler(DefinitionStmt stmt) {
    }

    @Override
    public void InstanceInvokeExprHandler(InstanceInvokeExpr iie) {
    }

    @Override
    public void Other(Stmt stmt) {
    }
}
