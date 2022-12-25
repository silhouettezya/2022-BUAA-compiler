package back.instruction;

import back.store.RegisterFile;

public class and extends MipsInstruction {

    private int regSrc1;
    private int regDst;
    private int regSrc2;


    public and(int regSrc1, int regSrc2, int regDst) {
        this.regSrc1 = regSrc1;
        this.regDst = regDst;
        this.regSrc2 = regSrc2;
    }

    @Override
    public String instrToString() {
        return String.format("and $%s, $%s, $%s",
                RegisterFile.getRegisterName(regDst),
                RegisterFile.getRegisterName(regSrc1),
                RegisterFile.getRegisterName(regSrc2));
    }
}