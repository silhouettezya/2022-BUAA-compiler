package back.instruction;

import back.store.RegisterFile;

public class sll extends MipsInstruction {

    private int regDst;
    private int bits;
    private int regSrc;

    public sll(int regSrc, int bits, int regDst) {
        this.regDst = regDst;
        this.bits = bits;
        this.regSrc = regSrc;

    }

    @Override
    public String instrToString() {
        return String.format("sll $%s, $%s, %d", RegisterFile.getRegisterName(regDst), RegisterFile.getRegisterName(regSrc), bits);
    }
}
