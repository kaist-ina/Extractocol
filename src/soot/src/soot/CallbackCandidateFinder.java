package soot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

//By Jeongmin Kim
//2015.10.18 Sun
//This class is used to find callback Candidates.
//First of all, this class find functions which call networking functions.
public class CallbackCandidateFinder {
    public static Collection<SootMethod> findCallbackCands() {
        Collection<SootMethod> ret = new ArrayList<SootMethod>();
        ret.addAll(SearchAllContainer(
                "<com.github.kevinsawicki.http.HttpRequest: com.github.kevinsawicki.http.HttpRequest get(java.lang.CharSequence,java.util.Map,boolean)>"));
        // ret.add(Scene.v().getMethod("<org.mediawiki.api.json.Api:
        // org.mediawiki.api.json.ApiResult
        // setupRequest(int,org.mediawiki.api.json.RequestBuilder)>"));
        return ret;
    }

    public static List<SootMethod> SearchAllContainer(
            String TargetCallSignature) {
        List<SootMethod> ret = new ArrayList<SootMethod>();
        for (SootClass sc : Scene.v().getClasses())
            for (SootMethod sm : sc.getMethods())
                if (sm.hasActiveBody()) // &&
                    // sm.getName().contains("setupRequest")
                    // // &&
                    // sm.getName().contains("performTask")
                    for (Unit ut : sm.getActiveBody().getUnits()) {
                        if (ut instanceof AssignStmt) {
                            AssignStmt is = (AssignStmt) ut;
                            if (is.containsInvokeExpr())
                                if (is.getInvokeExpr().toString()
                                        .contains(TargetCallSignature)) {
                                    ret.add(sm);
                                }
                        } else if (ut instanceof InvokeStmt) {
                            InvokeStmt is = (InvokeStmt) ut;
                            if (is.getInvokeExpr().toString()
                                    .contains(TargetCallSignature)) {
                                ret.add(sm);
                            }
                        }
                    }
        return ret;
    }

    public static List<SootMethod> SearchContainer(String TargetCallSignature,
                                                   String TargetMethodSig) {
        List<SootMethod> ret = new ArrayList<SootMethod>();
        SootMethod sm = Scene.v().getMethod(TargetMethodSig);
        if (sm.hasActiveBody()) // && sm.getName().contains("setupRequest") //
            // && sm.getName().contains("performTask")
            for (Unit ut : sm.getActiveBody().getUnits()) {
                if (ut instanceof AssignStmt) {
                    AssignStmt is = (AssignStmt) ut;
                    if (is.containsInvokeExpr())
                        if (is.getInvokeExpr().toString()
                                .contains(TargetCallSignature)) {
                            ret.add(sm);
                        }
                } else if (ut instanceof InvokeStmt) {
                    InvokeStmt is = (InvokeStmt) ut;
                    if (is.getInvokeExpr().toString()
                            .contains(TargetCallSignature)) {
                        ret.add(sm);
                    }
                }
            }
        return ret;
    }

    public static List<Unit> SearchAllInvokeStmt(String TargetCallSignature) {
        List<Unit> ret = new ArrayList<Unit>();
        for (SootClass sc : Scene.v().getClasses())
            for (SootMethod sm : sc.getMethods())
                if (sm.hasActiveBody())
                    for (Unit ut : sm.getActiveBody().getUnits()) {
                        if (ut instanceof AssignStmt) {
                            AssignStmt is = (AssignStmt) ut;
                            if (is.containsInvokeExpr())
                                if (is.getInvokeExpr().toString()
                                        .contains(TargetCallSignature)) {
                                    ret.add(ut);
                                }
                        }
                    }
        return ret;
    }

    public static List<Unit> SearchInvokeStmt(String TargetCallSignature,
                                              String TargetContainer) {
        List<Unit> ret = new ArrayList<Unit>();
        SootMethod sm = Scene.v().getMethod(TargetContainer);
        if (sm.hasActiveBody())
            for (Unit ut : sm.getActiveBody().getUnits()) {
                if (ut instanceof AssignStmt) {
                    AssignStmt is = (AssignStmt) ut;
                    if (is.containsInvokeExpr())
                        if (is.getInvokeExpr().toString()
                                .contains(TargetCallSignature)) {
                            ret.add(ut);
                        }
                } else if (ut instanceof InvokeStmt) {
                    InvokeStmt is = (InvokeStmt) ut;
                    if (is.getInvokeExpr().toString()
                            .contains(TargetCallSignature)) {
                        ret.add(ut);
                    }
                }
            }
        return ret;
    }

    public static class DotEdge {

        public DotEdge(SootMethod s, SootMethod d) {
            Src = s;
            Dst = d;
        }

        SootMethod Src;
        SootMethod Dst;
    }

    public static ArrayList<SootMethod> refineCallFlowForEachEp(SootMethod sm,
                                                                SootMethod dpMethod, Set<SootMethod> callflowset) {
        Queue<SootMethod> smQueue = new LinkedList<SootMethod>();
        CallGraph cg = Scene.v().getCallGraph();
        ArrayList<DotEdge> EdgeList = new ArrayList<DotEdge>();
        ArrayList<SootMethod> RefinedCallFlow = new ArrayList<SootMethod>();

        smQueue.add(sm);
        while (!smQueue.isEmpty()) {
            SootMethod tgt = smQueue.poll();

            // Please This loops must be refactoring.
            if (tgt == null)
                continue;
            if (tgt.hasActiveBody() && !isClassInSystemPackage(
                    tgt.method().getDeclaringClass().getName())) {
                for (Unit ut : tgt.getActiveBody().getUnits()) {
                    if (hasInvokeExprORisInvokeStmt(ut)) {
                        Iterator<Edge> callees = cg.edgesOutOf(ut);
                        for (; callees.hasNext();) {
                            Edge ed = callees.next();

                            DotEdge thisEdge = new DotEdge(tgt.method(),
                                    ed.tgt().method());

                            if (!isSystemApi(
                                    ed.tgt().getDeclaringClass().getName())
                                    && !hasDotEdge(EdgeList, thisEdge)) {
                                // Define nodes
                                RefinedCallFlow.add(tgt);

                                if (ed.tgt().getSignature()
                                        .equals(dpMethod.getSignature())) {
                                    RefinedCallFlow.add(ed.tgt());
                                } else if (callflowset.contains(ed.tgt())
                                        && callflowset.contains(tgt)) {
                                    RefinedCallFlow.add(ed.tgt());
                                    smQueue.add(ed.tgt().method());
                                }
                                EdgeList.add(thisEdge);
                            }
                        }
                    }
                }
            }
        }

        return RefinedCallFlow;
    }

    public static void CreateEPtoDPFlowGraph(SootMethod Ep, SootMethod Dp,
                                             BufferedWriter svfile, Set<SootMethod> callFlowList)
            throws IOException {
        Queue<SootMethod> smQueue = new LinkedList<SootMethod>();
        // boolean isReached = false;
        CallGraph cg = Scene.v().getCallGraph();
        ArrayList<DotEdge> EdgeList = new ArrayList<DotEdge>();

        smQueue.add(Ep);
        while (!smQueue.isEmpty()) {
            SootMethod tgt = smQueue.poll();

            // Please This loops must be refactoring.
            if (tgt.hasActiveBody() && !isClassInSystemPackage(
                    tgt.method().getDeclaringClass().getName())) {
                for (Unit ut : tgt.getActiveBody().getUnits()) {
                    if (hasInvokeExprORisInvokeStmt(ut)) {
                        Iterator<Edge> callees = cg.edgesOutOf(ut);
                        for (; callees.hasNext();) {
                            Edge ed = callees.next();

                            DotEdge thisEdge = new DotEdge(tgt.method(),
                                    ed.tgt().method());

                            if (!isSystemApi(
                                    ed.tgt().getDeclaringClass().getName())
                                    && !hasDotEdge(EdgeList, thisEdge)) {
                                // Define nodes
                                svfile.write("\"" + tgt.getSignature() + "\""
                                        + "[label=\"" + tgt.getSignature()
                                        + "\"];\n");

                                if (ed.tgt().getSignature()
                                        .equals(Dp.getSignature())) {
                                    svfile.write("\"" + ed.tgt().getSignature()
                                            + "\"" + "[color=\"red\", label=\""
                                            + ed.tgt().getSignature()
                                            + "\"];\n");
                                    svfile.write("\"" + tgt.getSignature()
                                            + "\"" + "->" + "\""
                                            + ed.tgt().getSignature()
                                            + "\" [style=bold] ;\n");
                                } else if (callFlowList.contains(ed.tgt())
                                        && callFlowList.contains(tgt)) {
                                    svfile.write("\"" + ed.tgt().getSignature()
                                            + "\"" + "[color=\"red\", label=\""
                                            + ed.tgt().getSignature()
                                            + "\"];\n");

                                    smQueue.add(ed.tgt().method());
                                    svfile.write("\"" + tgt.getSignature()
                                            + "\"" + "->" + "\""
                                            + ed.tgt().getSignature()
                                            + "\" [style=bold] ;\n");
                                }
                                EdgeList.add(thisEdge);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean hasDotEdge(ArrayList<DotEdge> edgeList,
                                      DotEdge thisEdge) {
        for (DotEdge de : edgeList)
            if (de.Dst.getSignature().equals(thisEdge.Dst.getSignature())
                    && de.Src.getSignature()
                    .equals(thisEdge.Src.getSignature()))
                return true;

        return false;
    }

    public static void PrintGraphforDotFramework(String MethodSig) {
        CallGraph cg = Scene.v().getCallGraph();
        try {
            BufferedWriter fw = new BufferedWriter(new FileWriter(
                    "D:\\Dropbox\\Dropbox\\graph\\graph.gv", false));
            fw.write("digraph CFG {\n");
            fw.write("rankdir=LR;\n");
            SootMethod sm = SearchAllContainer(MethodSig).get(0);
            CallerCalleeFinder(sm, fw);
            fw.write("\n}");
            fw.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("complete making gv file.");
    }

    public static void CallerCalleeFinder(SootMethod tgtSm,
                                          BufferedWriter svfile) throws IOException {
        Queue<SootMethod> smQueue = new LinkedList<SootMethod>();
        // Finding Caller
        int MaxCallerCount = 20;
        int MaxCount = 100;
        int Count = 0;
        // String DestinationMethod = "<com.squareup.okhttp.Call: void
        // enqueue(com.squareup.okhttp.Callback)>";
        String DestinationMethod = "<com.mobilemotion.dubsmash.services.UserProvider: com.mobilemotion.dubsmash.events.PropertyCheckedEvent checkEmail(java.lang.String)>";
        boolean isReached = true;
        CallGraph cg = Scene.v().getCallGraph();

        smQueue.add(tgtSm);
        while (!smQueue.isEmpty()) {
            SootMethod tgt = smQueue.poll();

            // For Callers -> it will be modified.
            Iterator<Edge> callers = cg.edgesInto(tgt);
            for (; callers.hasNext();) {
                Edge ed = callers.next();

                // Define nodes
                svfile.write("\"" + tgt.getSignature() + "\"" + "[label=\""
                        + tgt.getSignature() + "\"];\n");
                svfile.write("\"" + ed.src().getSignature() + "\"" + "[label=\""
                        + ed.src().getSignature() + "\"];\n");

                // Define Edges
                // for showing label
                // svfile.write("\""+ ed.src().getSubSignature()+ "\"" + "->" +
                // "\"" + tgt.getSubSignature()+ "\"" + " [label=\"" +
                // ed.src().getSignature() + "\"];\n");

                // no showing label
                svfile.write("\"" + ed.src().getSignature() + "\"" + "->" + "\""
                        + tgt.getSignature() + "\";\n");

                if (Count <= MaxCallerCount) {
                    Count++;
                    smQueue.add(ed.src().method());
                }
            }

            // Please This loops must be refactoring.
            if (tgt.hasActiveBody() && !isClassInSystemPackage(
                    tgt.method().getDeclaringClass().getName())) {
                for (Unit ut : tgt.getActiveBody().getUnits()) {
                    if (hasInvokeExprORisInvokeStmt(ut)) {
                        Iterator<Edge> callees = cg.edgesOutOf(ut);
                        for (; callees.hasNext();) {
                            Edge ed = callees.next();

                            if (!isSystemApi(
                                    ed.tgt().getDeclaringClass().getName())) {
                                // Define nodes
                                svfile.write("\"" + tgt.getSignature() + "\""
                                        + "[label=\"" + tgt.getSignature()
                                        + "\"];\n");
                                svfile.write("\"" + ed.tgt().getSignature()
                                        + "\"" + "[label=\""
                                        + ed.tgt().getSignature() + "\"];\n");
                                // Define Edges
                                svfile.write("\"" + tgt.getSignature() + "\""
                                        + "->" + "\"" + ed.tgt().getSignature()
                                        + "\";\n");

                                if (!isReached && ed.tgt().getSignature()
                                        .contains(DestinationMethod))
                                    isReached = true;
                                else if (isReached && Count <= MaxCount) {
                                    smQueue.add(ed.tgt().method());
                                    Count++;
                                } else if (!isReached)
                                    smQueue.add(ed.tgt().method());
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isSystemApi(String a) {
        if (!a.startsWith("java.") && !a.startsWith("android.")) {
            return false;
        }
        return true;
    }

    private static boolean hasInvokeExprORisInvokeStmt(Unit ut) {
        if (ut instanceof AssignStmt) {
            AssignStmt is = (AssignStmt) ut;
            if (is.containsInvokeExpr())
                return true;
        } else if (ut instanceof InvokeStmt) {
            return true;
        }

        return false;
    }

    public static boolean isClassInSystemPackage(String className) {
        return className.startsWith("android.") || className.startsWith("java.")
                || className.startsWith("sun.")
                || className.startsWith("org.codehaus.jackson.")
                || className.startsWith("org.jsoup.")
                || className.startsWith("com.google.");
    }
}
