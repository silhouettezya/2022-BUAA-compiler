package back.instruction;

import back.store.RegisterFile;

public class mfhi extends MipsInstruction {

    private int regDst;

    public mfhi(int regDst) {
        this.regDst = regDst;
    }

    @Override
    public String instrToString() {
        return "mfhi $" + RegisterFile.getRegisterName(regDst);
    }
}
