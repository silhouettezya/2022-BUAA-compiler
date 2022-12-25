package back.instruction;

import back.store.RegisterFile;

public class clo extends MipsInstruction {

    private int regSrc;
    private int regDst;

    public clo(int regDst, int regSrc) {
        this.regSrc = regSrc;
        this.regDst = regDst;
    }

    @Override
    public String instrToString() {
        return String.format("clo $%s, $%s", RegisterFile.getRegisterName(regDst), RegisterFile.getRegisterName(regSrc));
    }
}
