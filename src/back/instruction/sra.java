package back.instruction;

import back.store.RegisterFile;

public class sra extends MipsInstruction {

    private int regDst;
    private int regSrc;
    private int bits;

    public sra(int regSrc, int bits, int regDst) {
        this.regDst = regDst;
        this.regSrc = regSrc;
        this.bits = bits;
    }

    @Override
    public String instrToString() {
        return String.format("sra $%s, $%s, %d", RegisterFile.getRegisterName(regDst), RegisterFile.getRegisterName(regSrc), bits);
    }
}
