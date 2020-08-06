/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam, Patrick Pominville and Raja Vallee-Rai
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

package soot.baf.internal;

import java.util.List;

import org.objectweb.asm.Opcodes;

import soot.*;
import soot.baf.*;
import soot.jimple.Jimple;
import soot.util.*;

@SuppressWarnings({ "serial", "unchecked" })
public class BDynamicInvokeInst extends AbstractInvokeInst implements DynamicInvokeInst
{	
    protected final SootMethodRef bsmRef;
	private final List<Value> bsmArgs;
	protected int tag;

	public BDynamicInvokeInst(SootMethodRef bsmMethodRef, List<Value> bsmArgs, SootMethodRef methodRef, int tag) { 
        this.bsmRef = bsmMethodRef;
		this.bsmArgs = bsmArgs;
		this.methodRef = methodRef;
		this.tag = tag;
    }
	
    public int getInCount()
    {
        return methodRef.parameterTypes().size();
        
    }

    public Object clone() 
    {
        return new  BDynamicInvokeInst(bsmRef, bsmArgs, methodRef, tag);
    }
   
    public int getOutCount()
    {
        if(methodRef.returnType() instanceof VoidType)
            return 0;
        else
            return 1;
    }

    public SootMethodRef getBootstrapMethodRef() {
		return bsmRef;
	}   
    
    public List<Value> getBootstrapArgs() {
    	return bsmArgs;
    }

    public String getName() { return "dynamicinvoke"; }

    public void apply(Switch sw)
    {
        ((InstSwitch) sw).caseDynamicInvokeInst(this);
    }   
    
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append(Jimple.DYNAMICINVOKE);
        buffer.append(" \"");
        buffer.append(methodRef.name()); //quoted method name (can be any UTF8 string)
        buffer.append("\" <");
        buffer.append(SootMethod.getSubSignature(""/* no method name here*/, methodRef.parameterTypes(), methodRef.returnType()));
        buffer.append(">");
        buffer.append(bsmRef.getSignature());
        buffer.append("(");
        for(int i = 0; i < bsmArgs.size(); i++)
        {
            if(i != 0)
                buffer.append(", ");

            buffer.append(bsmArgs.get(i).toString());
        }
        buffer.append(")");

        return buffer.toString();
    }
    
    public void toString(UnitPrinter up)
    {
        up.literal(Jimple.DYNAMICINVOKE);        
        up.literal(" \"" + methodRef.name() + "\" <" + SootMethod.getSubSignature(""/* no method name here*/, methodRef.parameterTypes(), methodRef.returnType()) +"> ");        
        up.methodRef(bsmRef);
        up.literal("(");
        
        for(int i = 0; i < bsmArgs.size(); i++)
        {
            if(i != 0)
                up.literal(", ");
                
            bsmArgs.get(i).toString(up);
        }

        up.literal(")");
    }

	@Override
	public int getHandleTag() {
		return tag;
	}
}
