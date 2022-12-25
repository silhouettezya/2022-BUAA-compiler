package back.instruction;

public class slei extends MipsInstruction {

    private int immediate;
    private int regSrc;
    private int regDst;

    public slei(int regSrc, int immediate, int regDst) {
        this.immediate = immediate;
        this.regDst = regDst;
        this.regSrc = regSrc;

    }

    @Override
    public String instrToString() {
        return String.format("sle $%s, $%s, %d", regDst, regSrc, immediate);
    }
}
