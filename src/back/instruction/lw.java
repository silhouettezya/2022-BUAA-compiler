package back.instruction;

import back.store.RegisterFile;

public class lw extends MipsInstruction {

    private int regDst;
    private int regBase;
    private int offset;


    public lw(int regBase, int offset, int regDst) {
        this.regDst = regDst;
        this.regBase = regBase;
        this.offset = offset;
    }

    @Override
    public String instrToString() {
        return String.format("lw $%s, %d($%s)", RegisterFile.getRegisterName(regDst), offset, RegisterFile.getRegisterName(regBase));
    }
}
