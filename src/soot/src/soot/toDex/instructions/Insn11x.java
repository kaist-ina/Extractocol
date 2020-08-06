package soot.toDex.instructions;

import java.util.BitSet;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction11x;

import soot.toDex.LabelAssigner;
import soot.toDex.Register;

/**
 * The "11x" instruction format: It needs one 16-bit code unit, has one register
 * and is used for general purposes (hence the "x").<br>
 * <br>
 * It is used e.g. by the opcodes "monitor-enter", "monitor-exit", "move-result" and "return".
 */
public class Insn11x extends AbstractInsn implements OneRegInsn {

	public Insn11x(Opcode opc, Register regA) {
		super(opc);
		regs.add(regA);
	}
	
	public Register getRegA() {
		return regs.get(REG_A_IDX);
	}

	@Override
	protected BuilderInstruction getRealInsn0(LabelAssigner assigner) {
		return new BuilderInstruction11x(opc, (short) getRegA().getNumber());
	}
	
	@Override
	public BitSet getIncompatibleRegs() {
		BitSet incompatRegs = new BitSet(1);
		if (!getRegA().fitsShort()) {
			incompatRegs.set(REG_A_IDX);
		}
		return incompatRegs;
	}
}