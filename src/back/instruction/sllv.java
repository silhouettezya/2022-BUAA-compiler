package back.instruction;

import back.store.RegisterFile;

public class sllv extends MipsInstruction {

    private int regDst;
    private int regBits;
    private int regSrc;

    public sllv(int regSrc, int regBits, int regDst) {
        this.regDst = regDst;
        this.regBits = regBits;
        this.regSrc = regSrc;
    }

    @Override
    public String instrToString() {
        return String.format("sllv $%s, $%s, $%s", RegisterFile.getRegisterName(regDst), RegisterFile.getRegisterName(regSrc), RegisterFile.getRegisterName(regBits));
    }
}
