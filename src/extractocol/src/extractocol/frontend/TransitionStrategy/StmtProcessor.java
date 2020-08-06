package extractocol.frontend.TransitionStrategy;

import soot.jimple.*;
import soot.jimple.infoflow.data.Abstraction;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017-04-15.
 */
public abstract class StmtProcessor implements StmtParser{
    protected Stmt stmt;
    public StmtProcessor (Stmt src)
    {
        stmt = src;
    }
    protected Set<Abstraction> res = new HashSet<Abstraction>();

    public abstract void AssignStmtHandler(AssignStmt stmt);
    public abstract void DefineStmtHandler(DefinitionStmt stmt);
    public abstract void InstanceInvokeExprHandler(InstanceInvokeExpr iie);
    public abstract void Other(Stmt stmt);

    @Override
    public void AnalysisStmt(Stmt stmt) {

        if (stmt instanceof AssignStmt)
            AssignStmtHandler((AssignStmt)stmt);
        else if (stmt instanceof DefinitionStmt)
            DefineStmtHandler((DefinitionStmt)stmt);
        else if (stmt instanceof InvokeStmt)
        {
            InvokeStmt is = (InvokeStmt)stmt;
            if (is.getInvokeExpr() instanceof InstanceInvokeExpr)
                InstanceInvokeExprHandler((InstanceInvokeExpr)is.getInvokeExpr());
        }
        else Other(stmt);
    }

    public Set<Abstraction> getRes()
    {
        return res;
    }
}
