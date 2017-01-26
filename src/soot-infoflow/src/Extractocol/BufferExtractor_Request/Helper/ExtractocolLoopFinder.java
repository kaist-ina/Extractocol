package Extractocol.BufferExtractor_Request.Helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import soot.Body;
import soot.BodyTransformer;
import soot.Unit;
import soot.jimple.Stmt;
import soot.jimple.toolkits.annotation.logic.Loop;
//import soot.jimple.toolkits.annotation.logic.Loop;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.MHGDominatorsFinder;
import soot.toolkits.graph.UnitGraph;

class StmtLine{
  public Stmt st;
  public int line;
  
}

public class ExtractocolLoopFinder {
  
  

    private UnitGraph g;

    private Set<List<Stmt>> loops;

//    public Collection<Loop> loops(){
//        Collection<Loop> result = new HashSet<Loop>();
//        for (Map.Entry<Stmt,List<Stmt>> entry : loops.entrySet()) {
////            result.add(new Loop(entry.getKey(),entry.getValue(),g));
//        }
//        return result;
//    }
       
    public Set<List<Stmt>> getLoops (Body b) {
    
        g = new ExceptionalUnitGraph(b);
        MHGDominatorsFinder a = new MHGDominatorsFinder(g);
        
        loops = new HashSet<List<Stmt>>();
       
        
        Iterator<Unit> stmtsIt = b.getUnits().iterator();
        while (stmtsIt.hasNext()){
            Stmt s = (Stmt)stmtsIt.next();

            List<Unit> succs = g.getSuccsOf(s);
            Collection<Unit> dominaters = (Collection<Unit>)a.getDominators(s);

            ArrayList<Stmt> headers = new ArrayList<Stmt>();

            Iterator<Unit> succsIt = succs.iterator();
            while (succsIt.hasNext()){
                Stmt succ = (Stmt)succsIt.next();
                if (dominaters.contains(succ)){
                	//header succeeds and dominates s, we have a loop
                    headers.add(succ);
                }
            }

            Iterator<Stmt> headersIt = headers.iterator();
            while (headersIt.hasNext()){
                Stmt header = headersIt.next();
                List<Stmt> loopBody = getLoopBodyFor(header, s);

                // for now just print out loops as sets of stmts
                //System.out.println("FOUND LOOP: Header: "+header+" Body: "+loopBody);
                
                
                loops.add(loopBody);

//                if (loops.containsKey(header)){
//                    // merge bodies
//                    List<Stmt> lb1 = loops.get(header);
//                    loops.put(header, union(lb1, loopBody));
//                }
//                else {
//                }
            }
        }
        return loops;
    }
    
    public Set<List<StmtLine>> getLoops1 (Body b) {
      
      g = new ExceptionalUnitGraph(b);
      MHGDominatorsFinder a = new MHGDominatorsFinder(g);
      
      loops = new HashSet<List<Stmt>>();
     
      
      Iterator<Unit> stmtsIt = b.getUnits().iterator();
      while (stmtsIt.hasNext()){
          Stmt s = (Stmt)stmtsIt.next();

          List<Unit> succs = g.getSuccsOf(s);
          Collection<Unit> dominaters = (Collection<Unit>)a.getDominators(s);

          ArrayList<Stmt> headers = new ArrayList<Stmt>();

          Iterator<Unit> succsIt = succs.iterator();
          while (succsIt.hasNext()){
              Stmt succ = (Stmt)succsIt.next();
              if (dominaters.contains(succ)){
                  //header succeeds and dominates s, we have a loop
                  headers.add(succ);
              }
          }

          Iterator<Stmt> headersIt = headers.iterator();
          while (headersIt.hasNext()){
              Stmt header = headersIt.next();
              List<Stmt> loopBody = getLoopBodyFor(header, s);

              // for now just print out loops as sets of stmts
              //System.out.println("FOUND LOOP: Header: "+header+" Body: "+loopBody);
              
              
              loops.add(loopBody);

//              if (loops.containsKey(header)){
//                  // merge bodies
//                  List<Stmt> lb1 = loops.get(header);
//                  loops.put(header, union(lb1, loopBody));
//              }
//              else {
//              }
          }
      }
      return null;
  }
    

    private List<Stmt> getLoopBodyFor(Stmt header, Stmt node){
    
        ArrayList<Stmt> loopBody = new ArrayList<Stmt>();
        Stack<Unit> stack = new Stack<Unit>();

        loopBody.add(header);
        stack.push(node);

        while (!stack.isEmpty()){
            Stmt next = (Stmt)stack.pop();
            if (!loopBody.contains(next)){
                // add next to loop body
                loopBody.add(0, next);
                // put all preds of next on stack
                Iterator<Unit> it = g.getPredsOf(next).iterator();
                while (it.hasNext()){
                    stack.push(it.next());
                }
            }
        }
        
        assert (node==header && loopBody.size()==1) || loopBody.get(loopBody.size()-2)==node;
        assert loopBody.get(loopBody.size()-1)==header;
        
        return loopBody;
    }

    private List<Stmt> union(List<Stmt> l1, List<Stmt> l2){
        Iterator<Stmt> it = l2.iterator();
        while (it.hasNext()){
            Stmt next = it.next();
            if (!l1.contains(next)){
                l1.add(next);
            }
        }
        return l1;
    }
}
