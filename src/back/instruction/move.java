package back.instruction;

import back.store.RegisterFile;

public class move extends MipsInstruction {

    private int regSrc;
    private int regDst;

    public move(int regDst, int regSrc) {
        this.regSrc = regSrc;
        this.regDst = regDst;
    }

    @Override
    public String instrToString() {
        return String.format("move $%s, $%s", RegisterFile.getRegisterName(regDst), RegisterFile.getRegisterName(regSrc));
    }
}
