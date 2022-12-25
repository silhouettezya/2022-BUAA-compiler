package back.instruction;

import back.store.RegisterFile;

public class mflo extends MipsInstruction {

    private int regDst;

    public mflo(int regDst) {
        this.regDst = regDst;
    }

    @Override
    public String instrToString() {
        return "mflo $" + RegisterFile.getRegisterName(regDst);
    }
}
