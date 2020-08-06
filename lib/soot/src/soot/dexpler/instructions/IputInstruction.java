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
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.TwoRegisterInstruction;
import org.jf.dexlib2.iface.reference.FieldReference;

import soot.Local;
import soot.Type;
import soot.dexpler.Debug;
import soot.dexpler.DexBody;
import soot.dexpler.DexType;
import soot.dexpler.IDalvikTyper;
import soot.dexpler.typing.DalvikTyper;
import soot.jimple.AssignStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.Jimple;

public class IputInstruction extends FieldInstruction {

    public IputInstruction (Instruction instruction, int codeAdress) {
        super(instruction, codeAdress);
    }

    public void jimplify (DexBody body) {
        TwoRegisterInstruction i = (TwoRegisterInstruction)instruction;
        int source = i.getRegisterA();
        int object = i.getRegisterB();
        FieldReference f = (FieldReference)((ReferenceInstruction)instruction).getReference();
        InstanceFieldRef instanceField = Jimple.v().newInstanceFieldRef(body.getRegisterLocal(object),
                             getSootFieldRef(f));
        Local sourceValue = body.getRegisterLocal(source);
        AssignStmt assign = getAssignStmt(body, sourceValue, instanceField);
        setUnit(assign);
        addTags(assign);
        body.add(assign);

		if (IDalvikTyper.ENABLE_DVKTYPER) {
			Debug.printDbg(IDalvikTyper.DEBUG, "constraint: "+ assign);
          DalvikTyper.v().setType(assign.getRightOpBox(), instanceField.getType(), true);
        }
    }

    @Override
    protected Type getTargetType(DexBody body) {
        FieldReference f = (FieldReference)((ReferenceInstruction) instruction).getReference();
        return DexType.toSoot(f.getType());
    }
}
