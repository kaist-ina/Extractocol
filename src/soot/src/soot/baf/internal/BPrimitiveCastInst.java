/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam, Patrick Pominville and Raja Vallee-Rai
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

import soot.*;
import soot.baf.*;
import soot.util.*;

public class BPrimitiveCastInst extends AbstractInst 
                            implements PrimitiveCastInst
{
    Type fromType;
    
    protected Type toType;

    public int getInCount()
    {
        return 1;
    }

    public int getInMachineCount()
    {
        return AbstractJasminClass.sizeOfType(fromType);
    }
    
    public int getOutCount()
    {
        return 1;
    }

    public int getOutMachineCount()
    {
        return AbstractJasminClass.sizeOfType(toType);
    }

    
    public BPrimitiveCastInst(Type fromType, Type toType) 
    { 
        
        if( fromType instanceof NullType )
            throw new RuntimeException("invalid fromType " + fromType);
        this.fromType = fromType;
        this.toType = toType;
    }

    
    public Object clone() 
    {
        return new BPrimitiveCastInst(getFromType(), toType);
    }



    // after changing the types, use getName to check validity
    public Type getFromType() { return fromType; }
    public void setFromType(Type t) { fromType = t;}
    
    public Type getToType() { return toType; }
    public void setToType(Type t) { toType = t;}

    final public String getName() 
    {
        TypeSwitch sw;

        fromType.apply(sw = new TypeSwitch()
        {
            public void defaultCase(Type ty)
                {
                    throw new RuntimeException("invalid fromType " + fromType);
                }

            public void caseDoubleType(DoubleType ty)
                {
                    if(toType.equals(IntType.v()))
                        setResult("d2i");
                    else if(toType.equals(LongType.v()))
                        setResult("d2l");
                    else if(toType.equals(FloatType.v()))
                        setResult("d2f");
                    else
                        throw new RuntimeException
                            ("invalid toType from double: " + toType);
                }

            public void caseFloatType(FloatType ty)
                {
                    if(toType.equals(IntType.v()))
                        setResult("f2i");
                    else if(toType.equals(LongType.v()))
                        setResult("f2l");
                    else if(toType.equals(DoubleType.v()))
                        setResult("f2d");
                    else
                        throw new RuntimeException
                            ("invalid toType from float: " + toType);
                }

            public void caseIntType(IntType ty)
                {
                    emitIntToTypeCast();
                }

            public void caseBooleanType(BooleanType ty)
                {
                    emitIntToTypeCast();
                }

            public void caseByteType(ByteType ty)
                {
                    emitIntToTypeCast();
                }

            public void caseCharType(CharType ty)
                {
                    emitIntToTypeCast();
                }
            
            public void caseShortType(ShortType ty)
                {
                    emitIntToTypeCast();
                }

            private void emitIntToTypeCast()
                {
                    if(toType.equals(ByteType.v()))
                        setResult("i2b");
                    else if(toType.equals(CharType.v()))
                        setResult("i2c");
                    else if(toType.equals(ShortType.v()))
                        setResult("i2s");
                    else if(toType.equals(FloatType.v()))
                        setResult("i2f");
                    else if(toType.equals(LongType.v()))
                        setResult("i2l");
                    else if(toType.equals(DoubleType.v()))
                        setResult("i2d");
                    else if(toType.equals(IntType.v()))
                        setResult("");  // this shouldn't happen?
		    else if(toType.equals(BooleanType.v()))
			setResult("");
                    else
                        throw new RuntimeException
                            ("invalid toType from int: " + toType);
                }

            public void caseLongType(LongType ty)
                {
                    if(toType.equals(IntType.v()))
                        setResult("l2i");
                    else if(toType.equals(FloatType.v()))
                        setResult("l2f");
                    else if(toType.equals(DoubleType.v()))
                        setResult("l2d");
                    else
                        throw new RuntimeException
                              ("invalid toType from long: " + toType);
                            
                }
        });
        return (String)sw.getResult();
    }

    /* override toString with our own, *not* including types */
    public String toString()
    {
        return getName() + 
            getParameters();
    }

    public void apply(Switch sw)
    {
        ((InstSwitch) sw).casePrimitiveCastInst(this);
    }   
}

