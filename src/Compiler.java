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
        //String filePath = "./middlecode.txt";
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
        new PrintfTrans().optimize(ir);

        // 输出未优化的目标码和中间代码
        /*{
            filePath = "./testfile1_20373384_朱彦安_优化前中间代码.txt";
            file = new File(filePath);
            fos = null;
            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            System.setOut(new PrintStream(fos));
            ir.output();
            filePath = "./testfile1_20373384_朱彦安_优化前目标代码.txt";
            file = new File(filePath);
            fos = null;
            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            System.setOut(new PrintStream(fos));
            Mips mips = new Translator(ir).toMips();
            mips.output();
        }

        {
            filePath = "./testfile1_20373384_朱彦安_优化后中间代码.txt";
            file = new File(filePath);
            fos = null;
            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            System.setOut(new PrintStream(fos));
            new RemoveAfterJump().optimize(ir);
            new MergeBlock().optimize(ir);
            new MulDivOpt().optimize(ir);
            ir.output();
            filePath = "./testfile1_20373384_朱彦安_优化后目标代码.txt";
            file = new File(filePath);
            fos = null;
            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            System.setOut(new PrintStream(fos));
            Mips mips = new Translator(ir).toMips();
            new JumpFollow().optimize(mips);
            mips.output();
        }*/

        //new RemoveAfterJump().optimize(ir);
        //new MergeBlock().optimize(ir);
        //new MulDivOpt().optimize(ir);

        //ir.output();

        Mips mips = new Translator(ir).toMips();
        //new JumpFollow().optimize(mips);

        mips.output();
    }
}
