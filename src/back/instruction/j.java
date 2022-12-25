package back.instruction;

public class j extends MipsInstruction {

    private String target;

    public j(String target) {
        this.target = target;
    }

    @Override
    public String instrToString() {
        return "j " + getJumpTarget();
    }

    @Override
    public String getJumpTarget() {
        return target;
    }
}
