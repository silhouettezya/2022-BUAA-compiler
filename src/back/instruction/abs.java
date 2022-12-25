package back.instruction;

import back.store.RegisterFile;

public class abs extends MipsInstruction {
    private int regSrc;
    private int regDst;

    public abs(int regDst, int regSrc) {
        this.regSrc = regSrc;
        this.regDst = regDst;
    }

    @Override
    public String instrToString() {
        return String.format("abs $%s, $%s", RegisterFile.getRegisterName(regDst), RegisterFile.getRegisterName(regSrc));
    }
}
