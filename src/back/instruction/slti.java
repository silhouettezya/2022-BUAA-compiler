package back.instruction;

public class slti extends MipsInstruction {

    private int immediate;
    private int regSrc;
    private int regDst;

    public slti(int regSrc, int immediate, int regDst) {
        this.immediate = immediate;
        this.regDst = regDst;
        this.regSrc = regSrc;
    }

    @Override
    public String instrToString() {
        return String.format("slti $%s, $%s, %d", regDst, regSrc, immediate);
    }
}
