import back.Mips;
import back.Translator;
import back.optimize.JumpFollow;
import front.*;
import error.*;
import middle.IRBuilder;
import middle.MiddleCode;
import middle.optimize.MergeBlock;
import middle.optimize.MulDivOpt;
import middle.optimize.PrintfTrans;
import middle.optimize.RemoveAfterJump;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Objects;

public class Compiler {
    public static void main(String[] args) {
        //String filePath = "./output.txt";
        //String filePath = "./error.txt";
        String filePath = "./mips.txt";
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
        //ErrorTable.getInstance().output();

        MiddleCode ir = irBuilder.getIntermediate();
        if (Objects.isNull(ir)) {
            return;
        }
        new PrintfTrans().optimize(ir); // NECESSARY transformer! This is NOT an optimizer.

        new RemoveAfterJump().optimize(ir);
        new MergeBlock().optimize(ir);
        new MulDivOpt().optimize(ir);

        Mips mips = new Translator(ir).toMips();;

        /* ------ Mips Optimize Begin ------ */
        new JumpFollow().optimize(mips);
        /* ------ Mips Optimize End ------ */

        mips.output();
    }
}
