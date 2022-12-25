package back.instruction;

import back.store.RegisterFile;

public class clz extends MipsInstruction {

    private int regSrc;
    private int regDst;

    public clz(int regDst, int regSrc) {
        this.regSrc = regSrc;
        this.regDst = regDst;
    }

    @Override
    public String instrToString() {
        return String.format("clz $%s, $%s", RegisterFile.getRegisterName(regDst), RegisterFile.getRegisterName(regSrc));
    }
}
