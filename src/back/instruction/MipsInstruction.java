package back.instruction;

import middle.middlecode.Node;

/**
 * MIPS 指令的基本类型
 */
public abstract class MipsInstruction extends Node {

    public MipsInstruction() {}

    private String description = ""; // 位于当前指令前面
    private String label = "";  // 指令自己的标签(不是跳转指令的目标!)
    private String comment = ""; // 位于当前指令后面

    public boolean hasLabel() {
        return !label.isEmpty();
    }

    public boolean hasComment() {
        return !comment.isEmpty();
    }

    public boolean hasDescription() {
        return !description.isEmpty();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public abstract String instrToString();

    public String getJumpTarget() {
        return "";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (hasLabel()) {
            sb.append(getLabel()).append(":\n");
        }
        /*if (hasDescription()) {
            sb.append("# ").append(getDescription()).append("\n");
        }*/
        sb.append(instrToString());
        /*if (hasComment()) {
            sb.append("  # ").append(getComment());
        }*/
        return sb.toString();
    }

    public static MipsInstruction nop() {
        return new MipsInstruction() {
            @Override
            public String instrToString() {
                return "";
            }
        };
    }
}
