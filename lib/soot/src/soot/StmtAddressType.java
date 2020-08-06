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

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */


package soot;
import soot.util.*;

/**
 *   Soot representation of the Java type for a statement address. Implemented as
 *   a singleton.
 */
@SuppressWarnings("serial")
public class StmtAddressType extends Type
{
    public StmtAddressType( Singletons.Global g ) {}
    public static StmtAddressType v() { return G.v().soot_StmtAddressType(); }

    public boolean equals(Object t)
    {
        return this == t;
    }

    
    public int hashCode()
    {
        return 0x74F368D1;
    }
    
    public String toString()
    {
        return "address";
    }

    public void apply(Switch sw)
    {
        ((TypeSwitch) sw).caseStmtAddressType(this);
    }
}
