package back.instruction;

import back.store.RegisterFile;

public class ori extends MipsInstruction {

    private int immediate;
    private int regDst;
    private int regSrc;

    public ori(int regSrc, int immediate, int regDst) {
        this.immediate = immediate;
        this.regDst = regDst;
        this.regSrc = regSrc;
    }

    @Override
    public String instrToString() {
        return String.format("ori $%s, $%s, %d", RegisterFile.getRegisterName(regDst), RegisterFile.getRegisterName(regSrc), immediate);
    }
}
