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

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;

import soot.IntType;
import soot.Local;
import soot.dexpler.Debug;
import soot.dexpler.DexBody;
import soot.dexpler.IDalvikTyper;
import soot.dexpler.InvalidDalvikBytecodeException;
import soot.dexpler.tags.ObjectOpTag;
import soot.dexpler.typing.DalvikTyper;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;

public class AgetInstruction extends DexlibAbstractInstruction {
  
    public AgetInstruction (Instruction instruction, int codeAdress) {
        super(instruction, codeAdress);
    }

    public void jimplify (DexBody body) throws InvalidDalvikBytecodeException {
        if(!(instruction instanceof Instruction23x))
            throw new IllegalArgumentException("Expected Instruction23x but got: "+instruction.getClass());

        Instruction23x aGetInstr = (Instruction23x)instruction;
        int dest = aGetInstr.getRegisterA();
       
        Local arrayBase = body.getRegisterLocal(aGetInstr.getRegisterB());
        Local index = body.getRegisterLocal(aGetInstr.getRegisterC());

        ArrayRef arrayRef = Jimple.v().newArrayRef(arrayBase, index);
        Local l = body.getRegisterLocal(dest);
        
        AssignStmt assign = Jimple.v().newAssignStmt(l, arrayRef);
        if (aGetInstr.getOpcode() == Opcode.AGET_OBJECT)
          assign.addTag(new ObjectOpTag());

        setUnit(assign);
        addTags(assign);
        body.add(assign);
        
		if (IDalvikTyper.ENABLE_DVKTYPER) {
			Debug.printDbg(IDalvikTyper.DEBUG, "constraint: "+ assign);
          DalvikTyper.v().addConstraint(assign.getLeftOpBox(), assign.getRightOpBox());
          DalvikTyper.v().setType(arrayRef.getIndexBox(), IntType.v(), true);
        }
    }

    @Override
    boolean overridesRegister(int register) {
        OneRegisterInstruction i = (OneRegisterInstruction) instruction;
        int dest = i.getRegisterA();
        return register == dest;
    }
}
