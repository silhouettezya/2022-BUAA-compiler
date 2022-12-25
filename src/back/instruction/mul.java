package back.instruction;

import back.store.RegisterFile;

public class mul extends MipsInstruction {

    private int regSrc1;
    private int regSrc2;

    public mul(int regSrc1, int regSrc2) {
        this.regSrc1 = regSrc1;
        this.regSrc2 = regSrc2;
    }

    @Override
    public String instrToString() {
        return String.format("mult $%s, $%s", RegisterFile.getRegisterName(regSrc1), RegisterFile.getRegisterName(regSrc2));
    }
}
