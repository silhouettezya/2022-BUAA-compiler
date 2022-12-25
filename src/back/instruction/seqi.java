package back.instruction;

public class seqi extends MipsInstruction {

    private int regSrc;
    private int regDst;
    private int immediate;

    public seqi(int regSrc, int immediate, int regDst) {
        this.regDst = regDst;
        this.regSrc = regSrc;
        this.immediate = immediate;
    }

    @Override
    public String instrToString() {
        return String.format("seq $%s, $%s, %d", regDst, regSrc, immediate);
    }
}
