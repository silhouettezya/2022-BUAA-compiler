package back.instruction;

import back.store.RegisterFile;

public class li extends MipsInstruction {

    private int immediate;
    private int regDst;

    public li(int regDst, int immediate) {
        this.immediate = immediate;
        this.regDst = regDst;
    }

    @Override
    public String instrToString() {
        return String.format("li $%s, %d", RegisterFile.getRegisterName(regDst), immediate);
    }
}
