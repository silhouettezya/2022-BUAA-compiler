import front.*;
import error.*;
import middle.IRBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Compiler {
    public static void main(String[] args) {
        //String filePath = "./output.txt";
        String filePath = "./error.txt";
        File file = new File(filePath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.setOut(new PrintStream(fos));

        ErrorTable.getInstance().initial();

        Reader reader = new Reader();
        Lexer lexer = new Lexer(reader.readLines());
        //lexer.output();
        TokenTable tokenTable = new TokenTable(lexer.getTokenTable());
        Paser paser = new Paser(tokenTable);
        PaserUnit compUnit = paser.getCompUnit();
        IRBuilder irBuilder = new IRBuilder();
        irBuilder.parseCompUnit(compUnit);
        ErrorTable.getInstance().output();
    }
}
