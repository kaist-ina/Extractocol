/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
 * Copyright (C) 2004 Ondrej Lhotak
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

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */






package soot.jimple.internal;

import soot.*;
import soot.jimple.*;

import java.util.*;

public class JInterfaceInvokeExpr extends AbstractInterfaceInvokeExpr
{	
    public JInterfaceInvokeExpr(Value base, SootMethodRef methodRef, List<? extends Value> args)
    {
        super(Jimple.v().newLocalBox(base), methodRef, new ValueBox[args.size()]);

        //Check that the method's class is resolved enough
        //CheckLevel returns without doing anything because we can be not 'done' resolving
        methodRef.declaringClass().checkLevelIgnoreResolving(SootClass.HIERARCHY);
        //now check if the class is valid
        if(!methodRef.declaringClass().isInterface() && !methodRef.declaringClass().isPhantom()) {
//        	throw new RuntimeException("Trying to create interface invoke expression for non-interface type: " +
//        			methodRef.declaringClass() +
//        			" Use JVirtualInvokeExpr or JSpecialInvokeExpr instead!");
        }

        for(int i = 0; i < args.size(); i++)
            this.argBoxes[i] = Jimple.v().newImmediateBox(args.get(i));
    }    
    
    public Object clone() 
    {
        List<Value> argList = new ArrayList<Value>(getArgCount());

        for(int i = 0; i < getArgCount(); i++) {
            argList.add(i, Jimple.cloneIfNecessary(getArg(i)));
        }
            
        return new  JInterfaceInvokeExpr(Jimple.cloneIfNecessary(getBase()), methodRef, argList);
    }

}


