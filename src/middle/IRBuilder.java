package middle;

import error.ErrorTable;
import middle.middlecode.*;
import middle.symboltable.*;

import java.util.ArrayList;
import java.util.Stack;

public class IRBuilder {
    private SymbolTable currentSymTable = new SymbolTable();   // 栈式符号表

    private MiddleCode middleCode = new MiddleCode(); // 最终生成的中间代码

    private FuncInfo currentFunc = null;

    private int blockCount = 0;
    private int blockDepth = 0;

    private BasicBlock currentBlock;
    private int stackSize = 0;

    public IRBuilder() {
    }

    public MiddleCode getIntermediate() {
        return middleCode;
    }

    private int newBlockCount() {
        blockCount += 1;
        return blockCount;
    }
}

/*
    public middle.IRBuilder(PaserUnit compUnit) {
        Codes = new ArrayList<>();
        this.compUnit = compUnit;
    }

    public boolean checkRepeat(String name) {
        return STable.isRepeat(name);
    }

    public void RepeatError(PaserUnit ident) {
        int lineNumber = ident.getLastLineNumber();
        errorTable.getInstance().addError(lineNumber, 'b');
    }

    public void AddSymbol(Symbol s) {
        STable.addSymbol(s);
    }

    private void parseCompUnit() {
        ArrayList<PaserUnit> units = compUnit.getUnits();
        STable = new SymbolTable();
        for (PaserUnit unit : units) {
            if (unit.getType().equals("Decl")) {
                parseDecl(unit);
            } else if (unit.getType().equals("FuncDef")) {
                parseFuncDef(unit);
            } else if (unit.getType().equals("MainFuncDef")) {
                parseMainFuncDef(unit);
            }
        }
    }

    private void parseDecl(PaserUnit paserUnit) {
        ArrayList<PaserUnit> units = paserUnit.getUnits();
        for (PaserUnit unit : units) {
            if (unit.getType().equals("ConstDecl")) {
                parseConstDecl(unit);
            } else if (unit.getType().equals("VarDecl")) {
                parseVarDecl(unit);
            }
        }
    }

    private void parseConstDecl(PaserUnit paserUnit) {
        ArrayList<PaserUnit> units = paserUnit.getUnits();
        for (PaserUnit unit : units) {
            if (unit.getType().equals("ConstDef")) {
                parseConstDef(unit);
            }
        }
    }

    private void parseConstDef(PaserUnit paserUnit) {
        ArrayList<PaserUnit> units = paserUnit.getUnits();
        // TODO 仅完成了普通变量，没有数组
        PaserUnit ident = units.get(0);
        String name = ident.getName();
        if (checkRepeat(name)) {
            RepeatError(ident);
            return;
        }
        PaserUnit ConstInitial = units.get(2);
        PaserUnit ConstExp = ConstInitial.getUnits().get(0);
        int value = ConstExp.CalConstExp(STable);
        Symbol s = new Symbol(name, value);
        AddSymbol(s);
    }

    private void parseVarDecl(PaserUnit paserUnit) {
        ArrayList<PaserUnit> units = paserUnit.getUnits();
        for (PaserUnit unit : units) {
            if (unit.getType().equals("VarDef")) {
                parseVarDef(unit);
            }
        }
    }

    private void parseVarDef(PaserUnit paserUnit) {
        ArrayList<PaserUnit> units = paserUnit.getUnits();
        // TODO 仅完成了普通变量，没有数组
        PaserUnit ident = units.get(0);
        String name = ident.getName();
        if (checkRepeat(name)) {
            RepeatError(ident);
            return;
        } else {
            Symbol s = new Symbol(Symbol.Type.VAR, name, 0);
        }
        if (units.size() > 1) {
            parseExp(s);
        }
    }

    private void parseMainFuncDef(PaserUnit paserUnit) {
        ArrayList<PaserUnit> units = paserUnit.getUnits();
        MainFuncTable = new SymbolTable(STable);
        STable = MainFuncTable;
        parseFuncBlock(units.get(units.size() - 1));
        STable = STable.getFather();
    }

    private void parseFuncDef(PaserUnit paserUnit) {
        ArrayList<PaserUnit> units = paserUnit.getUnits();
        String type = units.get(0).getUnits().get(0).getType();
        PaserUnit ident = units.get(1);
        String name = ident.getName();
        if (checkRepeat(name)) {
            RepeatError(ident);
            return;
        }
        ArrayList<Symbol> symbols;
        //建函数块内的符号表
        SymbolTable stable = new SymbolTable(STable);
        if (units.get(3).getType().equals("FuncFParams")) {
            symbols = parseFuncFParams(units.get(3), stable);
        } else {
            symbols = null;
        }
        Symbol s;
        if (type.equals("int")) {
            s = new Symbol(Symbol.Type.FUNC_INT, name, symbols);
        } else {
            s = new Symbol(Symbol.Type.FUNC_VOID, name, symbols);
        }
        AddSymbol(s);
        STable = stable;//提前将符号表切换好再解析block
        parseFuncBlock(units.get(units.size() - 1));
        STable = stable.getFather();//返回上一层级
    }

    private ArrayList<Symbol> parseFuncFParams(PaserUnit paserUnit, SymbolTable stable) {
        ArrayList<PaserUnit> units = paserUnit.getUnits();
        ArrayList<Symbol> symbols = new ArrayList<>();
        for (PaserUnit unit : units) {
            if (unit.getType().equals("FuncFParam")) {
                PaserUnit ident = unit.getUnits().get(1);
                if (stable.isRepeat(ident.getName())) {
                    RepeatError(ident);//只报错并阻止该符号进入符号表，没有跳过其他程序的解析
                } else {
                    stable.addSymbol(parseFuncFParam(unit));//将函数块内的参数加入符号表
                    symbols.add(parseFuncFParam(unit));
                }
            }
        }
        return symbols;
    }

    private Symbol parseFuncFParam(PaserUnit paserUnit) {
        //TODO 这里仅考虑第一次作业只需普通变量的情形
        ArrayList<PaserUnit> units = paserUnit.getUnits();
        PaserUnit ident = units.get(1);
        String name = ident.getName();
        Symbol s = new Symbol(Symbol.Type.VAR, name, 0);
        return s;
    }

    private void parseFuncBlock(PaserUnit paserUnit) {
        ArrayList<PaserUnit> units = paserUnit.getUnits();
        for (PaserUnit unit : units) {
            if (unit.getType().equals("BlockItem")) {
                parseBlockItem(unit);
            }
        }
    }

    private void parseBlockItem(PaserUnit paserUnit) {
        PaserUnit unit = paserUnit.getUnits().get(0);
        if (unit.getType().equals("Decl")) {
            parseDecl(unit);
        } else if (unit.getType().equals("Stmt")) {
            parseStmt(unit);
        }
    }

    private void parseStmt(PaserUnit paserUnit) {
        //TODO
    }

    private void parseExp(PaserUnit paserUnit) {

    }

}*/