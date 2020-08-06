/* Soot - a Java Optimization Framework
 * Copyright (C) 2012 Michael Markert, Frank Hartmann
 * 
 * (c) 2012 University of Luxembourg - Interdisciplinary Centre for
 * Security Reliability and Trust (SnT) - All rights reserved
 * Alexandre Bartel
 * 
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

package soot.dexpler.instructions;

import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;

import soot.Body;
import soot.Local;
import soot.RefType;
import soot.Type;
import soot.dexpler.Debug;
import soot.dexpler.DexBody;
import soot.dexpler.IDalvikTyper;
import soot.dexpler.typing.DalvikTyper;
import soot.jimple.IdentityStmt;
import soot.jimple.Jimple;

public class MoveExceptionInstruction extends DexlibAbstractInstruction implements RetypeableInstruction {

    private Type realType;
    private IdentityStmt stmtToRetype;
    
    public MoveExceptionInstruction (Instruction instruction, int codeAdress) {
        super(instruction, codeAdress);
    }

    public void jimplify (DexBody body) {
        int dest = ((OneRegisterInstruction)instruction).getRegisterA();
        Local l = body.getRegisterLocal(dest);
        stmtToRetype = Jimple.v().newIdentityStmt(l, Jimple.v().newCaughtExceptionRef());
        setUnit(stmtToRetype);
        addTags(stmtToRetype);
        body.add(stmtToRetype);
        
        if (IDalvikTyper.ENABLE_DVKTYPER) {
			Debug.printDbg(IDalvikTyper.DEBUG, "constraint: "+ stmtToRetype);
            DalvikTyper.v().setType(stmtToRetype.getLeftOpBox(), RefType.v("java.lang.Throwable"), false);
        }
    }

    public void setRealType(DexBody body, Type t) {
        realType = t;
        body.addRetype(this);
    }

    public void retype(Body body) {
        if (realType == null)
            throw new RuntimeException("Real type of this instruction has not been set or was already retyped: " + this);
        if (body.getUnits().contains(stmtToRetype)) {
	        Local l = (Local)(stmtToRetype.getLeftOp());
	        l.setType(realType);
	        realType = null;
        }
    }

    @Override
    boolean overridesRegister(int register) {
        OneRegisterInstruction i = (OneRegisterInstruction) instruction;
        int dest = i.getRegisterA();
        return register == dest;
    }
    

}
