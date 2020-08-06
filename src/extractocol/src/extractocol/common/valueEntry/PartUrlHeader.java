package extractocol.common.valueEntry;

/**
 * Created by Administrator on 2017-05-20.
 */
public class PartUrlHeader {
    private String HeapStr;
    private String EP;

    public PartUrlHeader(String _HeapStr, String _EP)
    {
        HeapStr = _HeapStr;
        EP = _EP;
    }

    public String getHeapStr()
    {
        return HeapStr;
    }

    public boolean isEqual(PartUrlHeader _puh)
    {
        return _puh.HeapStr.contains(HeapStr) && EP.equals(_puh.EP);
    }
}
