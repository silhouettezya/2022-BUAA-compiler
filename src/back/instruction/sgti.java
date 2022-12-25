package back.instruction;

public class sgti extends MipsInstruction {

    private int immediate;
    private int regSrc;
    private int regDst;

    public sgti(int regSrc, int immediate, int regDst) {
        this.immediate = immediate;
        this.regDst = regDst;
        this.regSrc = regSrc;
    }

    @Override
    public String instrToString() {
        return String.format("sgt $%s, $%s, %d", regDst, regSrc, immediate);
    }
}
