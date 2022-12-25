package back.instruction;

import back.store.RegisterFile;

public class div extends MipsInstruction {

    private int regSrc2;
    private int regSrc1;

    public div(int regSrc1, int regSrc2) {
        this.regSrc2 = regSrc2;
        this.regSrc1 = regSrc1;
    }

    @Override
    public String instrToString() {
        return String.format("div $%s, $%s", RegisterFile.getRegisterName(regSrc1), RegisterFile.getRegisterName(regSrc2));
    }
}
