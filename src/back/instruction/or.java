package back.instruction;

import back.store.RegisterFile;

public class or extends MipsInstruction {

    private int regDst;
    private int regSrc1;
    private int regSrc2;

    public or(int regSrc1, int regSrc2, int regDst) {
        this.regDst = regDst;
        this.regSrc1 = regSrc1;
        this.regSrc2 = regSrc2;
    }

    @Override
    public String instrToString() {
        return String.format("or $%s, $%s, $%s",
                RegisterFile.getRegisterName(regDst),
                RegisterFile.getRegisterName(regSrc1),
                RegisterFile.getRegisterName(regSrc2));
    }
}
