package middle.middlecode;

public class BranchIfElse extends Node {
    private Operand condition;
    private BasicBlock thenTarget;
    private BasicBlock elseTarget;

    public BranchIfElse(Operand condition, BasicBlock thenTarget, BasicBlock elseTarget) {
        this.condition = condition;
        this.thenTarget = thenTarget;
        this.elseTarget = elseTarget;
    }

    public BasicBlock getElseTarget() {
        return elseTarget;
    }

    public BasicBlock getThenTarget() {
        return thenTarget;
    }

    public Operand getCondition() {
        return condition;
    }

    @Override
    public String toString() {
        return "BR " + condition + " ? " + thenTarget + " : " + elseTarget;
    }
}
