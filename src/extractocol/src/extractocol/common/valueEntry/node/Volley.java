package extractocol.common.valueEntry.node;

import extractocol.common.tools.Pair;
import extractocol.common.valueEntry.ValueEntry;
import extractocol.common.valueEntry.ValueEntryTable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017-05-12.
 */
@SuppressWarnings("serial")
public class Volley extends ValueEntry implements Serializable {
    String url;
    ArrayList<Pair> paramList = new ArrayList<Pair>();
    int method;

    public Volley(String _url, int _method)
    {
        url = _url;
        method = _method;
    }

    @Override
    public SOURCE_TYPE getSourceType() {
        return SOURCE_TYPE.VOLLEY;
    }

    @Override
    public void Clear() {
        url = null;
        method = 0;
        paramList.clear();
        paramList = null;
    }

    @Override
    public String GenRegex() {
        return null;
    }

    @Override
    public ValueEntry Clone() {
        return null;
    }

    @Override
    public void addEntry(ValueEntry ve) {

    }
}
