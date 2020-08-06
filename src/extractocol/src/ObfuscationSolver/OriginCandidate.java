package ObfuscationSolver;

public class OriginCandidate {
    private String _secondSHAkey;
    private String _methodSignature;

    public OriginCandidate (String key, String signature)
    {
        _secondSHAkey = key;
        _methodSignature = signature;
    }

    public String getsignature()
    {
        return _methodSignature;
    }

    public String getkey()
    {
        return _secondSHAkey;
    }
}
