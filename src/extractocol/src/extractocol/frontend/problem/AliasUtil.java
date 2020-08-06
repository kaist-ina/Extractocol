package extractocol.frontend.problem;

import soot.Value;
import soot.jimple.InstanceFieldRef;
import soot.jimple.infoflow.data.Abstraction;

/**
 * Created by Administrator on 2017-04-17.
 */
public class AliasUtil {
    public static boolean baseMatches(final Value baseValue, Abstraction source) {
        if (baseValue instanceof InstanceFieldRef) {
            InstanceFieldRef ifr = (InstanceFieldRef) baseValue;
            if (ifr.getBase().equals(source.getAccessPath().getPlainValue()))
                return true;
        }
        return false;
    }
}
