package back.instruction;

public class sgei extends MipsInstruction {

    private int regSrc;
    private int regDst;
    private int immediate;

    public sgei(int regSrc, int immediate, int regDst) {
        this.regDst = regDst;
        this.regSrc = regSrc;
        this.immediate = immediate;
    }

    @Override
    public String instrToString() {
        return String.format("sge $%s, $%s, %d", regDst, regSrc, immediate);
    }
}
