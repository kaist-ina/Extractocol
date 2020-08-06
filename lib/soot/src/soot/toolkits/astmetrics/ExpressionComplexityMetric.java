/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
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

package soot.toolkits.astmetrics;

import polyglot.ast.Expr;
import polyglot.ast.Node;
import polyglot.visit.NodeVisitor;

/**
 * @author Michael Batchelder 
 * 
 * Created on 7-Mar-2006 
 */
public class ExpressionComplexityMetric extends ASTMetric {

  	int currentExprDepth;
	int exprDepthSum;
	int exprCount;
	int inExpr;

	public ExpressionComplexityMetric(polyglot.ast.Node node) {
	  super(node);
	}
	
	public void reset() {
	  currentExprDepth = 0;
	  exprDepthSum = 0;
	  exprCount = 0;
	  inExpr = 0;
	}

	public void addMetrics(ClassData data) {
	  double a = exprDepthSum;
	  double b = exprCount;
	  
	  data.addMetric(new MetricData("Expr-Complexity",new Double(a)));
	  data.addMetric(new MetricData("Expr-Count",new Double(b)));
	}

	public NodeVisitor enter(Node parent, Node n){
	  if(n instanceof Expr){
	    inExpr++;
	    currentExprDepth++;
	  }
	  
	  return enter(n);
	}
	
	public Node leave(Node old, Node n, NodeVisitor v){
	  if(n instanceof Expr){
	    if (currentExprDepth==1) {
	      exprCount++;
	      exprDepthSum+=inExpr;
	      inExpr = 0;
	    }
	    currentExprDepth--;
	  }
	  
	  return super.leave(old,n,v);
	}
}

