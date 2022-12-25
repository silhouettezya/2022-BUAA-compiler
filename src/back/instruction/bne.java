package back.instruction;

import back.store.RegisterFile;

public class bne extends MipsInstruction {

    private String target;
    private int regSrc1;
    private int regSrc2;


    public bne(int regSrc1, int regSrc2, String target) {
        this.target = target;
        this.regSrc1 = regSrc1;
        this.regSrc2 = regSrc2;
    }

    @Override
    public String getJumpTarget() {
        return target;
    }

    @Override
    public String instrToString() {
        return String.format("bne $%s, $%s, %s", RegisterFile.getRegisterName(regSrc1), RegisterFile.getRegisterName(regSrc2), target);
    }
}
