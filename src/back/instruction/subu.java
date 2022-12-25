package back.instruction;

import back.store.RegisterFile;

public class subu extends MipsInstruction {

    private int regSrc1;
    private int regSrc2;
    private int regDst;

    public subu(int regSrc1, int regSrc2, int regDst) {
        this.regSrc1 = regSrc1;
        this.regSrc2 = regSrc2;
        this.regDst = regDst;
    }

    @Override
    public String instrToString() {
        return String.format("subu $%s, $%s, $%s",
                RegisterFile.getRegisterName(regDst),
                RegisterFile.getRegisterName(regSrc1),
                RegisterFile.getRegisterName(regSrc2));
    }
}
