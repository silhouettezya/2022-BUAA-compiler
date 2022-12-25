package back.optimize;

import back.Mips;
import back.instruction.j;
import back.instruction.MipsInstruction;

import java.util.Objects;

public class JumpFollow {

    public JumpFollow() {}

    public void optimize(Mips mips) {
        MipsInstruction instr = mips.getFirstInstruction();
        while (Objects.nonNull(instr) && instr.hasNext()) {
            if (instr instanceof j && !instr.hasLabel()
                    && instr.getJumpTarget().equals(((MipsInstruction) instr.getNext()).getLabel())) {
                instr.remove();
            }
            instr = (MipsInstruction) instr.getNext();
        }
    }
}
