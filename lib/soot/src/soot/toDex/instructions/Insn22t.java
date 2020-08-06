package soot.toDex.instructions;

import java.util.BitSet;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.instruction.BuilderInstruction22t;

import soot.toDex.LabelAssigner;
import soot.toDex.Register;

/**
 * The "22t" instruction format: It needs two 16-bit code units, has two registers
 * and is used for jump targets (hence the "t").<br>
 * <br>
 * It is used e.g. by the opcode "if-eq" for conditional jumps to a 16-bit wide offset.
 */
public class Insn22t extends InsnWithOffset implements TwoRegInsn {

	public Insn22t(Opcode opc, Register regA, Register regB) {
		super(opc);
		regs.add(regA);
		regs.add(regB);
	}
	
	public Register getRegA() {
		return regs.get(REG_A_IDX);
	}
	
	public Register getRegB() {
		return regs.get(REG_B_IDX);
	}

	@Override
	protected BuilderInstruction getRealInsn0(LabelAssigner assigner) {
		return new BuilderInstruction22t(opc, (byte) getRegA().getNumber(),
				(byte) getRegB().getNumber(), assigner.getOrCreateLabel(target));
	}
	
	@Override
	public BitSet getIncompatibleRegs() {
		BitSet incompatRegs = new BitSet(2);
		if (!getRegA().fitsByte()) {
			incompatRegs.set(REG_A_IDX);
		}
		if (!getRegB().fitsByte()) {
			incompatRegs.set(REG_B_IDX);
		}
		return incompatRegs;
	}

	@Override
	public int getMaxJumpOffset() {
		return Short.MAX_VALUE;
	}
	
}