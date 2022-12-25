package back.instruction;

import back.store.RegisterFile;

public class srav extends MipsInstruction {

    private int regDst;
    private int regSrc;
    private int regBits;

    public srav(int regSrc, int regBits, int regDst) {
        this.regDst = regDst;
        this.regSrc = regSrc;
        this.regBits = regBits;
    }

    @Override
    public String instrToString() {
        return String.format("srav $%s, $%s, $%s", RegisterFile.getRegisterName(regDst), RegisterFile.getRegisterName(regSrc), RegisterFile.getRegisterName(regBits));
    }
}
