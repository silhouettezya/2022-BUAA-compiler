package middle.middlecode;

/**
 * 输出整数，对应 Mars 的 1 号 syscall
 */
public class PrintInt extends Node {
    private Operand value;

    public PrintInt(Operand value) {
        this.value = value;
    }

    public Operand getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "PRINT_INT " + value;
    }
}
