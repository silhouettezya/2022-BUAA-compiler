package back.instruction;

import back.store.RegisterFile;

public class xori extends MipsInstruction {

    private int regSrc;
    private int immediate;
    private int regDst;

    public xori(int regSrc, int immediate, int regDst) {
        this.regDst = regDst;
        this.regSrc = regSrc;
        this.immediate = immediate;
    }

    @Override
    public String instrToString() {
        return String.format("xori $%s, $%s, %d", RegisterFile.getRegisterName(regDst), RegisterFile.getRegisterName(regSrc), immediate);
    }
}
