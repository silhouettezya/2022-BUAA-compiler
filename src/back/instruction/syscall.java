package back.instruction;

public class syscall extends MipsInstruction {

    public static int PRINT_INTEGER = 1;
    public static int PRINT_STRING = 4;
    public static int READ_INTEGER = 5;
    public static int TERMINATE = 10;

    @Override
    public String instrToString() {
        return "syscall";
    }
}
