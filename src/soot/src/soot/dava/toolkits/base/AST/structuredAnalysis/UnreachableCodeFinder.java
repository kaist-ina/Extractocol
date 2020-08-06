/* Soot - a J*va Optimization Framework
 * Copyright (C) 2006 Nomair A. Naeem
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/**
 * Maintained by: Nomair A. Naeem
 */


/**
 * CHANGE LOG:
 *
 */ 
package soot.dava.toolkits.base.AST.structuredAnalysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Local;
import soot.Value;
import soot.dava.DecompilationException;
import soot.dava.internal.AST.ASTDoWhileNode;
import soot.dava.internal.AST.ASTForLoopNode;
import soot.dava.internal.AST.ASTIfElseNode;
import soot.dava.internal.AST.ASTIfNode;
import soot.dava.internal.AST.ASTLabeledBlockNode;
import soot.dava.internal.AST.ASTNode;
import soot.dava.internal.AST.ASTStatementSequenceNode;
import soot.dava.internal.AST.ASTSwitchNode;
import soot.dava.internal.AST.ASTSynchronizedBlockNode;
import soot.dava.internal.AST.ASTTryNode;
import soot.dava.internal.AST.ASTUnaryBinaryCondition;
import soot.dava.internal.AST.ASTUnconditionalLoopNode;
import soot.dava.internal.AST.ASTWhileNode;
import soot.dava.internal.SET.SETNodeLabel;
import soot.dava.internal.javaRep.DAbruptStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.toolkits.scalar.FlowSet;

/*
 * Sort of the mark phase of a mark and sweep dead code eliminator.
 * Carry NOPATH information through the flowsets
 * 
 * Will need to over ride the processAbruptStatement method of parent
 * class since we plan to do something special with abrupt stmts
 * 
 * ONLY PROCESS A CONSTRUCT IF ITS INSET HOLDS TRUE i.e. it is REACHABLE otherwise
 * simply pass on the inset
 * 
 * TODO:
 *   1, The child after any loop is always reachable (even for a while(true) which returns)
 *      Will probably have to override loop process methods, invoke the super process method and then change the outset
 *   2, handleBreak would need to see if there is any break stmt then set the out to contain true   
 */

public class UnreachableCodeFinder extends StructuredAnalysis {
	public static boolean DEBUG=false;
	public class UnreachableCodeFlowSet extends DavaFlowSet{
		
		public UnreachableCodeFlowSet clone(){
			if(this.size() != 1)
				throw new DecompilationException("unreachableCodeFlow set size should always be 1");
			Boolean temp = (Boolean)this.elements[0];
			UnreachableCodeFlowSet toReturn = new UnreachableCodeFlowSet();
			toReturn.add(new Boolean(temp.booleanValue()));
			toReturn.copyInternalDataFrom(this);
			return toReturn;
		}
		
		
		@Override
		public void intersection(FlowSet otherFlow, FlowSet destFlow){
			if(DEBUG) System.out.println("In intersection");
			if(!(otherFlow instanceof UnreachableCodeFlowSet) || 
						!(destFlow instanceof UnreachableCodeFlowSet)){
		          super.intersection(otherFlow, destFlow);
		          return;
			}
			
			UnreachableCodeFlowSet other = (UnreachableCodeFlowSet)otherFlow;
			UnreachableCodeFlowSet dest = (UnreachableCodeFlowSet)destFlow;
			
			UnreachableCodeFlowSet workingSet;
		            
            if(dest == other || dest == this)
                workingSet = new UnreachableCodeFlowSet();
            else { 
                workingSet = dest;
                workingSet.clear();
            }
            
    
			if( other.size()!=1 || this.size() != 1){
				System.out.println("Other size = "+other.size());
				System.out.println("This size = "+this.size());
				throw new DecompilationException("UnreachableCodeFlowSet size should always be one");
			}
			
			Boolean thisPath = (Boolean)this.elements[0];
			Boolean otherPath = (Boolean)other.elements[0];
			if(!thisPath.booleanValue() && !otherPath.booleanValue()){
				//both say there is no path
				workingSet.add((new Boolean(false)));
			}
			else{
				workingSet.add((new Boolean(true)));
			}
			(workingSet).copyInternalDataFrom(this);
			if (otherFlow instanceof DavaFlowSet)
				(workingSet).copyInternalDataFrom((DavaFlowSet) otherFlow);
			
			
			
			  if(workingSet != dest)
	                workingSet.copy(dest);
			  
			  
			if(DEBUG) System.out.println("destFlow contains size:"+ ((UnreachableCodeFlowSet)destFlow).size());
		}

	}//end UnreachableCodeFlowSet

	
    public UnreachableCodeFinder(Object analyze){
    	super();
    	//the input to the process method is newInitialFlow
    	DavaFlowSet temp = (DavaFlowSet)process(analyze,newInitialFlow());
    }

	
	/*
	 * Merge is intersection but our SPECIALIZED intersection hence 
	 * creating our own specialize flow set with overriding the intersection method
	 */
	public void setMergeType() {
		MERGETYPE=INTERSECTION;
	}

	
	
	/*
	 * For catch bodies.
	 * 
	 *  If you are processing the catch body that means you can reach to the try
	 *  Since you can always come to a catchbody the inset to the catch body to should be 
	 *  that there is a path
	 */
	public DavaFlowSet newInitialFlow() {
		DavaFlowSet newSet = emptyFlowSet();
		newSet.add(new Boolean(true));
		return newSet;
	}

	
	
	public DavaFlowSet emptyFlowSet() {
    	return new UnreachableCodeFlowSet();
	}

	
	
	@Override
	public UnreachableCodeFlowSet cloneFlowSet(DavaFlowSet flowSet) {
		if(flowSet instanceof UnreachableCodeFlowSet){
			return ((UnreachableCodeFlowSet)flowSet).clone();
		}
		throw new RuntimeException("Clone only implemented for UnreachableCodeFlowSet");
	}	
	
	@Override
	public DavaFlowSet processUnaryBinaryCondition(ASTUnaryBinaryCondition cond, DavaFlowSet input) {
		return input;
	}

	@Override
	public DavaFlowSet processSynchronizedLocal(Local local, DavaFlowSet input) {
		return input;
	}

	@Override
	public DavaFlowSet processSwitchKey(Value key, DavaFlowSet input) {
		return input;
	}

	@Override
	public DavaFlowSet processStatement(Stmt s, DavaFlowSet input) {
		return input;
	}
	
	
	@Override
	public DavaFlowSet processAbruptStatements(Stmt s, DavaFlowSet input){
    	if(DEBUG)	System.out.println("processing stmt "+s);
    	if(s instanceof ReturnStmt || s instanceof RetStmt || s instanceof ReturnVoidStmt){
    	    //dont need to remember this path
    		UnreachableCodeFlowSet toReturn = new UnreachableCodeFlowSet();
    		toReturn.add(new Boolean(false));
    		toReturn.copyInternalDataFrom(input);
    		//false indicates NOPATH
        	if(DEBUG)	System.out.println("\tstmt is a return stmt. Hence sending forward false");
    	    return toReturn;
    	}
    	else if(s instanceof DAbruptStmt){
    	    DAbruptStmt abStmt = (DAbruptStmt)s;
    	    
    	    //see if its a break or continue
    	    if(!(abStmt.is_Continue()|| abStmt.is_Break())){
    	    	//DAbruptStmt is of only two kinds
    	    	throw new RuntimeException("Found a DAbruptStmt which is neither break nor continue!!");
    	    }		    
    	    
    	    DavaFlowSet temp = new UnreachableCodeFlowSet();
    	    SETNodeLabel nodeLabel = abStmt.getLabel();

    	    //    	  notice we ignore continues for this analysis
    		if (abStmt.is_Break()){
    			if(nodeLabel != null && nodeLabel.toString() != null){
    				//explicit break stmt    	    
    	    		temp.addToBreakList(nodeLabel.toString(),input);			
    			}
    			else{
    				//found implicit break
    	    		temp.addToImplicitBreaks(abStmt,input);    	    	
    			}
    	    }
    	    temp.add(new Boolean(false));
    	    temp.copyInternalDataFrom(input);
        	if(DEBUG)	System.out.println("\tstmt is an abrupt stmt. Hence sending forward false");
    	    return temp;
    	}
    	else{
        	if(DEBUG)	System.out.println("\tstmt is not an abrupt stmt.");
    	    return processStatement(s,input);
    	}
    }

    
    /*
     * If a particular node is targeted by a break statement then that means there is always a path to it
     * Hence if there is even a single entry in the implicit or explicit break set
     * return a flow set which contains true since there is a path to this point
     */
	@Override
    public DavaFlowSet handleBreak(String label, DavaFlowSet output, ASTNode node){
    	if(DEBUG)	System.out.println("Handling break. Output contains"+ ((UnreachableCodeFlowSet)output).size());
    	if( !(output instanceof UnreachableCodeFlowSet) )
    	    throw new RuntimeException("handleBreak is only implemented for UnreachableCodeFlowSet type");

    	DavaFlowSet out = (DavaFlowSet)output;

    	//get the explicit list with this label from the breakList
    	List explicitSet = out.getBreakSet(label);

    	//getting the implicit list now
    	if(node ==null)
    	    throw new RuntimeException("ASTNode sent to handleBreak was null");
    	
    	List implicitSet = out.getImplicitlyBrokenSets(node);
    	//System.out.println("\n\nImplicit set is"+implicitSet);
    
		DavaFlowSet toReturn = emptyFlowSet();
		toReturn.copyInternalDataFrom(output);
    	if( (explicitSet != null && explicitSet.size()>0) || (implicitSet != null && implicitSet.size()>0 )){
        	if(DEBUG)	System.out.println("\tBreak sets (implicit and explicit are nonempty hence forwarding true");
    		//some break targets node
    		toReturn.add(new Boolean(true));
    		return toReturn;
    	}
    	else{
    		//no break targets node, maybe output was true though hence use merge
    		toReturn.add(new Boolean(false));
        	if(DEBUG)	System.out.println("\tBreak sets (implicit and explicit are empty hence forwarding merge of false with inset");
    		return merge(toReturn,output);
    	}
    }

    
    
    public boolean isReachable(Object input){
    	if(!(input instanceof UnreachableCodeFlowSet))
			throw new DecompilationException("Implemented only for UnreachableCodeFlowSet");
	
    	UnreachableCodeFlowSet checking = (UnreachableCodeFlowSet)input;
    	if(checking.size()!=1)
    		throw new DecompilationException("unreachableCodeFlow set size should always be 1");
	
    	if(((Boolean)checking.elements[0]).booleanValue()){
    		//it is reachable
    		if(DEBUG)	System.out.println("\t Reachable");
    		return true;
    	}
    	else{
    		if(DEBUG)	System.out.println("\t NOT Reachable");
    		return false;
    	}
    }

    
    
    @Override
    public DavaFlowSet processASTStatementSequenceNode(ASTStatementSequenceNode node, DavaFlowSet input){
		if(DEBUG)	System.out.println("Processing statement sequence node");
    	if(!isReachable(input)){
    		//this sequence is not reachable hence simply return inset
    		return input;
    	}
    	else
    		return super.processASTStatementSequenceNode(node,input);
    }
    
    @Override
    public DavaFlowSet processASTLabeledBlockNode(ASTLabeledBlockNode node, DavaFlowSet input){
		if(DEBUG)	System.out.println("Processing labeled block node node");
    	if(!isReachable(input)){
    		//this sequence is not reachable hence simply return inset
    		return input;
    	}
    	else
    		return super.processASTLabeledBlockNode(node,input);
    }
    
    
    @Override
    public DavaFlowSet processASTSynchronizedBlockNode(ASTSynchronizedBlockNode node, DavaFlowSet input){
		if(DEBUG)	System.out.println("Processing synchronized block node");
    	if(!isReachable(input)){
    		//this sequence is not reachable hence simply return inset
    		return input;
    	}
    	else
    		return super.processASTSynchronizedBlockNode(node,input);
    }

    
    @Override
    public DavaFlowSet processASTIfElseNode(ASTIfElseNode node, DavaFlowSet input){
		if(DEBUG)	System.out.println("Processing ifelse node");
    	if(!isReachable(input)){
    		//this sequence is not reachable hence simply return inset
    		return input;
    	}
    	else{
    		//the output will be false if both branches are abrupt
    		//and also there is no break targetting this construct
    		return super.processASTIfElseNode(node,input);
    	}
    }
    
    
    public DavaFlowSet ifNotReachableReturnInputElseProcessBodyAndReturnTrue(ASTNode node, DavaFlowSet input){
		if(DEBUG)	System.out.println("Processing "+node.getClass()+" node");
    	if(!isReachable(input)){
    		//this sequence is not reachable hence simply return inset
    		return input;
    	}
    	else{
    		DavaFlowSet output = processSingleSubBodyNode(node,input);
    		
    		UnreachableCodeFlowSet toReturn = new UnreachableCodeFlowSet();
    		toReturn.add(new Boolean(true));
    		toReturn.copyInternalDataFrom(output);
    		return toReturn;
        }
    	
    }

    
    /*
     * If the if stmt is reachable the outset if always reachable
     */
    @Override
    public DavaFlowSet processASTIfNode(ASTIfNode node, DavaFlowSet input){
    	return ifNotReachableReturnInputElseProcessBodyAndReturnTrue(node, input);    
    }
    
    

    
    
    /*
     *  No need to process Condition
     *  No need to do fixed point
     *  No need to handleContinues since they dont change reachability 
     */
    @Override
    public DavaFlowSet processASTWhileNode(ASTWhileNode node, DavaFlowSet input){
    	return ifNotReachableReturnInputElseProcessBodyAndReturnTrue(node, input);
    }
    

    
    
    /*
     * Same as while loop
     */
    @Override
    public DavaFlowSet processASTDoWhileNode(ASTDoWhileNode node, DavaFlowSet input){
    	return ifNotReachableReturnInputElseProcessBodyAndReturnTrue(node, input);
    }


    
    @Override
    public DavaFlowSet processASTUnconditionalLoopNode(ASTUnconditionalLoopNode node, DavaFlowSet input){
    	return ifNotReachableReturnInputElseProcessBodyAndReturnTrue(node, input);
    }

    
    
    /*
     * No need to process Init
     * No need to process Condition 	
     * No need to process Update
     * No need to handle continue
     */
    @Override
    public DavaFlowSet processASTForLoopNode(ASTForLoopNode node, DavaFlowSet input){
    	return ifNotReachableReturnInputElseProcessBodyAndReturnTrue(node, input);
    }
    
    
    

    
    
    
    /*
     * No need to process SwitchKey
     * TODO test and reason
     */
    @Override
    public DavaFlowSet processASTSwitchNode(ASTSwitchNode node, DavaFlowSet input){
		if(DEBUG)	System.out.println("Processing switch node");
    	if(!isReachable(input)){
    		//this sequence is not reachable hence simply return inset
    		return input;
    	}
    	//if reachable
    	List<Object> indexList = node.getIndexList();
    	Map<Object, List<Object>> index2BodyList = node.getIndex2BodyList();

    	DavaFlowSet initialIn = cloneFlowSet(input);
    	DavaFlowSet out = null;
    	DavaFlowSet defaultOut = null;
    	List<DavaFlowSet> toMergeBreaks = new ArrayList<DavaFlowSet>();
    	
    	Iterator<Object> it = indexList.iterator();
    	while (it.hasNext()) {//going through all the cases of the switch statement
    		Object currentIndex = it.next();
    		List body = index2BodyList.get( currentIndex);

    		if(body == null)
    			continue;

    		//although the input is usually the merge of the out of previous
    		//but since we know this case is always reachable as switch is reachable
    		//there is no need to merge
    		
    		out=process(body,cloneFlowSet(initialIn));
    		toMergeBreaks.add(cloneFlowSet(out));
    		if(currentIndex instanceof String){
    			//this is the default
    			defaultOut=out;
    		}
    	}

    	//have to handle the case when no case matches. The input is the output
    	DavaFlowSet output=initialIn;
    	if(out!=null){//just to make sure that there were some cases present
    		if(defaultOut!=null)   			
    			output=merge(defaultOut,out);
    		else
    			output = merge(initialIn,out);
    	}

    	//handle break
    	String label = getLabel(node);
    	//have to handleBreaks for all the different cases
    	List<DavaFlowSet> outList = new ArrayList<DavaFlowSet>();
    	//handling breakLists of each of the toMergeBreaks
    	for (DavaFlowSet tmb : toMergeBreaks)
    		outList.add(handleBreak(label, tmb, node));
    	
    	//merge all outList elements. since these are the outputs with breaks handled
    	DavaFlowSet finalOut=output;
    	for (DavaFlowSet fo : outList)
    		finalOut = merge(finalOut, fo);
    	
    	return finalOut;
    }

    @Override
    public DavaFlowSet processASTTryNode(ASTTryNode node, DavaFlowSet input){
		if(DEBUG)	System.out.println("Processing try node");
    	if(!isReachable(input)){
    		//this sequence is not reachable hence simply return inset
    		return input;
    	}
    	
    	//if reachable
    	List<Object> tryBody = node.get_TryBody();
    	DavaFlowSet tryBodyOutput = process(tryBody,input);

    	//catch is always reachable if try is reachable
    	DavaFlowSet inputCatch = newInitialFlow();

       	List<Object> catchList = node.get_CatchList();
       	Iterator<Object> it = catchList.iterator();
       	List<DavaFlowSet> catchOutput = new ArrayList<DavaFlowSet>();

       	while (it.hasNext()) {
       		ASTTryNode.container catchBody = (ASTTryNode.container)it.next();
	    
       		List body = (List)catchBody.o;
       		//list of ASTNodes

       		//result because of going through the catchBody
       		DavaFlowSet tempResult = process(body,cloneFlowSet(inputCatch));
       		//System.out.println("TempResult going through body"+tempResult);
       		catchOutput.add(tempResult);
       	}
		
       	//handle breaks
       	String label = getLabel(node);



       	List<DavaFlowSet> outList = new ArrayList<DavaFlowSet>();	
       	//handle breaks out of tryBodyOutput
       	outList.add(handleBreak(label,tryBodyOutput,node));
	
       	//handling breakLists of each of the catchOutputs
       	for (DavaFlowSet co : catchOutput) {
       		DavaFlowSet temp = handleBreak(label, co, node);
       		outList.add(temp);
       	}

       	
       	//merge all outList elements. since these are the outputs with breaks handled
       	DavaFlowSet out= tryBodyOutput;
       	for (DavaFlowSet oe : outList)
       		out = merge(out, oe);

       	for (DavaFlowSet ce : catchOutput)
       		out = merge(out, ce);
       	
       	return out;
    }
    
    public boolean isConstructReachable(Object construct){
    	Object temp = getBeforeSet(construct);
    	if(temp == null)
    		return true;
    	else{
    		if(DEBUG)
    			System.out.println("Have before set");
    		Boolean reachable = (Boolean)((UnreachableCodeFlowSet)temp).elements[0];
    		return reachable.booleanValue();
    	}
    }
}
