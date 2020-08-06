package extractocol.frontend.TransitionStrategy.BackwordSolver.NormalFlowRule;

import extractocol.frontend.TransitionStrategy.StmtProcessor;
import extractocol.frontend.helper.PropagateHelper.PROPA_TYPE;
import extractocol.frontend.output.basic.TaintResultEntry;
import extractocol.frontend.problem.AliasUtil;
import extractocol.frontend.solver.MyInfoflowSolver;
import heros.solver.PathEdge;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.data.Abstraction;
import soot.jimple.infoflow.solver.fastSolver.InfoflowSolver;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Jeongmin on 2017-04-15.
 */
public class TransitionRule extends StmtProcessor {
    private Abstraction d1, source;
    private Value leftValue;
    private Unit src;
    private MyInfoflowSolver fSolver;
    private List<Unit> succsOf;

    public TransitionRule(Stmt _src, Abstraction _d1, Value _leftValue, Abstraction _source, MyInfoflowSolver _fsolver, List<Unit> _succsOf)
    {
        super((Stmt)_src);
        src = (Unit) _src;
        d1 = _d1;
        source = _source;
        leftValue = _leftValue;
        fSolver = _fsolver;
        succsOf = _succsOf;
        AnalysisStmt(this.stmt);
    }

    @Override
    public void AnalysisStmt(Stmt stmt) {
        super.AnalysisStmt(stmt);
    }

    @Override
    public void InstanceInvokeExprHandler(InstanceInvokeExpr iie) {
    }

    @Override
    public void DefineStmtHandler(DefinitionStmt stmt) {

    }

    @Override
    public void AssignStmtHandler(AssignStmt stmt) {

        // Wish rule 1
        if (AliasUtil.baseMatches(stmt.getRightOp(), source))
        {
            Abstraction newLeftAbs = source.deriveNewAbstraction(
                    source.getAccessPath().copyWithNewValue(leftValue, leftValue.getType(), false), (AssignStmt) src);

            for (Unit nextUt : succsOf) {
            	TaintResultEntry tre = d1.getTRE().Clone();
            	tre.setPrevStmt(nextUt.toString());
            	tre.setPropaType(PROPA_TYPE.TR_B2F);
                fSolver.processEdge(new PathEdge<Unit, Abstraction>(d1, nextUt, newLeftAbs), tre);
//                break;
            }
        }
    }

    @Override
    public void Other(Stmt stmt) {

    }
}
