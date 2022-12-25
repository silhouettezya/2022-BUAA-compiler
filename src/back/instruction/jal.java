package back.instruction;

public class jal extends MipsInstruction {

    private String target;

    public jal(String target) {
        this.target = target;
    }

    @Override
    public String instrToString() {
        return "jal " + target;
    }

    @Override
    public String getJumpTarget() {
        return target;
    }
}
