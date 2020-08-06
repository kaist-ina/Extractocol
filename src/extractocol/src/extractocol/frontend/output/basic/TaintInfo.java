package extractocol.frontend.output.basic;

import soot.SootMethod;
import soot.Unit;
import soot.jimple.infoflow.data.Abstraction;

/**
 * Created by Administrator on 2017-06-02.
 */
public class TaintInfo {
    public String obj;
    public Abstraction abs;
    public Unit unit;
    public SootMethod sm;
    public int selfcount = 0;

    public TaintInfo Clone()
    {
        TaintInfo newTaintInfo = new TaintInfo();
        newTaintInfo.obj = new String(this.obj);
        newTaintInfo.abs = this.abs.clone();
        newTaintInfo.unit = (Unit) this.unit.clone();
        newTaintInfo.sm = this.sm;
        newTaintInfo.selfcount = new Integer(selfcount);
        return newTaintInfo;
    }
}
