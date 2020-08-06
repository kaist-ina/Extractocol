package ObfuscationSolver;

import extractocol.common.valueEntry.node.Array;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeStmt;

import java.util.*;

public class MappingTable implements java.io.Serializable {
    static HashMap<String, HashSet<OriginCandidate>> _map = new HashMap<>();

    public void setmap(HashMap<String, HashSet<OriginCandidate>> map)
    {
        _map = map;
    }

    public boolean build (BasicLoader bl)
    {
        bl.loadervalidation();
        ArrayList<SootClass> list = bl.load();

        for (SootClass sc : list) {
            for (SootMethod sm : sc.getMethods()) {

                if (!sm.hasActiveBody())
                    continue;

                ArrayList<String> firstSeed = genkeyseed(sm, true);
                String firstkey = genkey(firstSeed);

                ArrayList<String> secondSeed = genkeyseed(sm, false);
                String secondkey = genkey(secondSeed);
                OriginCandidate oc = new OriginCandidate(secondkey, sm.getSignature());

                addoc(firstkey, oc);
            }
        }

        System.out.println("size : " + _map.size());

        return _map.size() <= 0 ? false : true;
    }

    private void addoc(String firstkey, OriginCandidate oc) {
        HashSet<OriginCandidate> oclist = null;
        if (_map.containsKey(firstkey))
        {
            oclist = _map.get(firstkey);
        }
        else
        {
            oclist = new HashSet<>();
        }

        for (OriginCandidate src: oclist)
        {
            if (src.getkey().equals(oc.getkey()))
                return;
        }

        oclist.add(oc);
        _map.put(firstkey, oclist);
    }

    private ArrayList<String> genkeyseed(SootMethod sm, boolean isFirst)
    {
        ArrayList<String> seeds = new ArrayList<>();
        if (isFirst) {
            seeds.add(sm.getReturnType().toString());

            for (Type arg : sm.getParameterTypes()) {
                seeds.add(arg.toString());
            }
            seeds.add(Integer.toString(sm.getActiveBody().getUnits().size()));
        }
        else
        {
            String[] argtypes = new String[sm.getActiveBody().getLocalCount()];
            int index = 0;
            for (Value arg : sm.getActiveBody().getLocals()) {
                argtypes[index++] = arg.getType().toString();
            }
            Arrays.sort(argtypes);
            seeds.addAll(Arrays.asList(argtypes));

            for (Unit ut: sm.getActiveBody().getUnits())
            {
                seeds.add(gettypeofunit(ut));
            }
        }
        return seeds;
    }

    private String gettypeofunit(Unit u)
    {
        if (u instanceof DefinitionStmt)
            return "DefinitionStmt";
        else if (u instanceof IdentityStmt)
            return "IdentityStmt";
        else if (u instanceof AssignStmt)
            return "AssignStmt";
        else if (u instanceof InvokeStmt)
            return "InvokeStmt";
        else return "Other";
    }

    private HashSet<OriginCandidate> filter1(String hashkey)
    {
        return _map.get(hashkey);
    }

    public String solve(SootMethod targetsm)
    {
    	if(!targetsm.hasActiveBody())
    		return targetsm.getSignature();
    	
        //make first hashkey. this key includes targetsm's signatures;
        //this result might include methods of same method signatures.
        //So we must find exact origin method using second hashkey including method body.
        ArrayList<String> seeds = genkeyseed(targetsm, true);
        String firstkey = genkey(seeds);
        HashSet<OriginCandidate> result1 = filter1(firstkey);
        if (result1 == null)
            return targetsm.getSignature();


        seeds = genkeyseed(targetsm, false);
        String secondkey = genkey(seeds);

        //second phase - finding exact origin methods.
        //make key for target method body;
        //also try to find exact original method in result1.
        String result = findoc(result1, secondkey);
        if (result == null)
            return targetsm.getSignature();
        else
            return result;
    }

    private String findoc(HashSet<OriginCandidate> result1, String secondkey) {
        for (OriginCandidate src : result1)
        {
            if (src.getkey().equals(secondkey))
                return src.getsignature();
        }
        return null;
    }

    //First, Return type, Param types, number of instructions.
    private String genkey(ArrayList<String> seeds)
    {
        StringBuilder sb = new StringBuilder();

        for (String s : seeds)
        {
            sb.append(s);
        }
        return SHAMaker.make(sb.toString());
    }
}
