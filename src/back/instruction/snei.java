package back.instruction;

public class snei extends MipsInstruction {

    private int regSrc;
    private int regDst;
    private int immediate;

    public snei(int regSrc, int immediate, int regDst) {
        this.regDst = regDst;
        this.regSrc = regSrc;
        this.immediate = immediate;
    }

    @Override
    public String instrToString() {
        return String.format("sne $%s, $%s, %d", regDst, regSrc, immediate);
    }
}
