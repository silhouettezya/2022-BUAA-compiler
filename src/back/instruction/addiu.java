package back.instruction;

import back.store.RegisterFile;

public class addiu extends MipsInstruction {

    private int regSrc;
    private int regDst;
    private int immediate;


    public addiu(int regSrc, int immediate, int regDst) {
        this.regSrc = regSrc;
        this.regDst = regDst;
        this.immediate = immediate;
    }

    @Override
    public String instrToString() {
        return String.format("addiu $%s, $%s, %d", RegisterFile.getRegisterName(regDst), RegisterFile.getRegisterName(regSrc), immediate);
    }
}
