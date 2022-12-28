package middle.middlecode;

public class Jump extends Node {

    private BasicBlock target;

    public Jump(BasicBlock target) {
        this.target = target;
    }

    public BasicBlock getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "goto " + target.getLabel();
        //return "J " + target.getLabel();
    }
}
