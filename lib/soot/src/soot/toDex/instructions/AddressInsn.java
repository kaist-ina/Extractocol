package soot.toDex.instructions;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.builder.BuilderInstruction;

import soot.toDex.LabelAssigner;

/** 
 * Inspired by com.android.dx.dex.code.CodeAddress: pseudo instruction for use
 * as jump target or start/end of an exception handler range. It has size zero,
 * so that its offset is the same as the following real instruction.
 */
public class AddressInsn extends AbstractInsn {
	
	private Object originalSource;

	public AddressInsn(Object originalSource) {
		super(Opcode.NOP);
		this.originalSource = originalSource;
	}
	
	public Object getOriginalSource() {
		return originalSource;
	}

	@Override
	protected BuilderInstruction getRealInsn0(LabelAssigner assigner) {
		return null;
	}
	
	@Override
	public int getSize() {
		return 0;
	}
	
	@Override
	public String toString() {
		return "address instruction for " + originalSource;
	}
}