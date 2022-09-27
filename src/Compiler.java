import java.util.ArrayList;

public class Compiler {
    public static void main(String[] args) {
        Reader reader = new Reader();
        /*ArrayList<String> lines = reader.readLines();
        for (String line : lines) {
            System.out.print(line + "\n");
        }*/
        Lexer lexer = new Lexer(reader.readLines());
        lexer.output();
    }
}
