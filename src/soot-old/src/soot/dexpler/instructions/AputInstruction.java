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
import org.jf.dexlib2.iface.instruction.formats.Instruction23x;

import soot.ArrayType;
import soot.IntType;
import soot.Local;
import soot.Type;
import soot.UnknownType;
import soot.dexpler.Debug;
import soot.dexpler.DexBody;
import soot.dexpler.IDalvikTyper;
import soot.dexpler.tags.ObjectOpTag;
import soot.dexpler.typing.DalvikTyper;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;

public class AputInstruction extends FieldInstruction {

    AssignStmt assign = null;
  
    public AputInstruction (Instruction instruction, int codeAdress) {
        super(instruction, codeAdress);
    }

    public void jimplify (DexBody body) {
        if(!(instruction instanceof Instruction23x))
            throw new IllegalArgumentException("Expected Instruction23x but got: "+instruction.getClass());

        Instruction23x aPutInstr = (Instruction23x)instruction;
        int source = aPutInstr.getRegisterA();

        Local arrayBase = body.getRegisterLocal(aPutInstr.getRegisterB());
        Local index = body.getRegisterLocal(aPutInstr.getRegisterC());
        ArrayRef arrayRef = Jimple.v().newArrayRef(arrayBase, index);

        Local sourceValue = body.getRegisterLocal(source);
        assign = getAssignStmt(body, sourceValue, arrayRef);
        if (aPutInstr.getOpcode().value == Opcode.APUT_OBJECT.value)
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
    protected Type getTargetType(DexBody body) {
        Instruction23x aPutInstr = (Instruction23x)instruction;
        Type t = body.getRegisterLocal(aPutInstr.getRegisterB()).getType();
        if (t instanceof ArrayType)
            return ((ArrayType) t).getElementType();
        else
            return UnknownType.v();
    }
}
