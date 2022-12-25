package back.instruction;

import back.store.RegisterFile;

public class sw extends MipsInstruction {

    private int regBase;
    private int offset;
    private int regSrc;

    public sw(int regBase, int offset, int regSrc) {
        this.regBase = regBase;
        this.offset = offset;
        this.regSrc = regSrc;
    }

    @Override
    public String instrToString() {
        return String.format("sw $%s, %d($%s)", RegisterFile.getRegisterName(regSrc), offset, RegisterFile.getRegisterName(regBase));
    }
}
