package back.instruction;

import back.store.RegisterFile;

public class andi extends MipsInstruction {

    private int regSrc;
    private int regDst;
    private int immediate;


    public andi(int regSrc, int immediate, int regDst) {
        this.regSrc = regSrc;
        this.regDst = regDst;
        this.immediate = immediate;
    }

    @Override
    public String instrToString() {
        return String.format("andi $%s, $%s, %d", RegisterFile.getRegisterName(regDst), RegisterFile.getRegisterName(regSrc), immediate);
    }
}
