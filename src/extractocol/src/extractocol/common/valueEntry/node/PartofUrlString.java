package extractocol.common.valueEntry.node;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jeongmin on 2017-05-19.
            * values: remains string we must to apply semantic model.
            */
    public class PartofUrlString {
        private String[] values;
        private String semantic;

        public PartofUrlString(String[] _values, String _semantic)
        {
            semantic = _semantic;
            values = _values;
        }

        public String[] getValues()
        {
            return values;
        }
        public String getSemantic() {return semantic;}
}
