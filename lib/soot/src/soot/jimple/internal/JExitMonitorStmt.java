/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
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


import soot.util.*;
import java.util.*;
import soot.*;
import soot.jimple.*;
import soot.baf.*;

public class JExitMonitorStmt extends AbstractOpStmt 
    implements ExitMonitorStmt
{
    public JExitMonitorStmt(Value op)
    {
        this(Jimple.v().newImmediateBox(op));
    }

    protected JExitMonitorStmt(ValueBox opBox)
    {
        super(opBox);
    }

    public Object clone() 
    {
        return new JExitMonitorStmt(Jimple.cloneIfNecessary(getOp()));
    }

    public String toString()
    {
        return Jimple.EXITMONITOR + " "  + opBox.getValue().toString();
    }
    
    public void toString(UnitPrinter up) {
        up.literal(Jimple.EXITMONITOR);
        up.literal(" ");
        opBox.toString(up);
    }

    public void apply(Switch sw)
    {
        ((StmtSwitch) sw).caseExitMonitorStmt(this);

    }    

  public void convertToBaf(JimpleToBafContext context, List<Unit> out)
  {
    ((ConvertToBaf)(getOp())).convertToBaf(context, out);

    Unit u = Baf.v().newExitMonitorInst();
    u.addAllTagsOf(this);
    out.add(u);
  }


    
    public boolean fallsThrough(){return true;}
    public boolean branches(){return false;}        
    


}
