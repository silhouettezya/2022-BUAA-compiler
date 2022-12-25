package middle;

import error.ErrorTable;
import front.PaserUnit;
import middle.middlecode.*;
import middle.symboltable.*;

import java.util.*;

public class IRBuilder {
    private SymbolTable currentSymbolTable = new SymbolTable(); // 栈式符号表
    private MiddleCode middleCode = new MiddleCode(); // 最终生成的中间代码
    private FuncInfo currentFunc = null;
    private BasicBlock currentBlock;
    private int stackSize = 0;
    private int blockCount = 0;
    private int blockDepth = 0;
    private Stack<BasicBlock> loopBlocks = new Stack<>();
    private Stack<BasicBlock> loopFollows = new Stack<>();

    public IRBuilder() {
    }

    public MiddleCode getIntermediate() {
        return middleCode;
    }

    private int newBlockCount() {
        blockCount += 1;
        return blockCount;
    }

    private void RepeatError(PaserUnit ident) {
        int lineNumber = ident.getLastLineNumber();
        ErrorTable.getInstance().addError(lineNumber, 'b');
    }

    private void UndefinedError(PaserUnit ident) {
        int lineNumber = ident.getLastLineNumber();
        ErrorTable.getInstance().addError(lineNumber, 'c');
    }

    private void ConstError(PaserUnit ident) {
        int lineNumber = ident.getLastLineNumber();
        ErrorTable.getInstance().addError(lineNumber, 'h');
    }

    private void FuncRParamsError(PaserUnit ident) {
        int lineNumber = ident.getLastLineNumber();
        ErrorTable.getInstance().addError(lineNumber, 'd');
    }

    private void FuncRParamsTypeError(PaserUnit ident) {
        int lineNumber = ident.getLastLineNumber();
        ErrorTable.getInstance().addError(lineNumber, 'e');
    }

    private void FormatStringParamError(PaserUnit pf) {
        int lineNumber = pf.getLastLineNumber();
        ErrorTable.getInstance().addError(lineNumber, 'l');
    }

    private void VoidReturnError(PaserUnit rt) {
        int lineNumber = rt.getLastLineNumber();
        ErrorTable.getInstance().addError(lineNumber, 'f');
    }

    private void ReturnLossError(PaserUnit rb) {
        int lineNumber = rb.getLastLineNumber();
        ErrorTable.getInstance().addError(lineNumber, 'g');
    }

    private void BreakOrContinueError(PaserUnit brkOrCon) {
        int lineNumber = brkOrCon.getLastLineNumber();
        ErrorTable.getInstance().addError(lineNumber, 'm');
    }

    public void parseCompUnit(PaserUnit paserUnit) {
        ArrayList<PaserUnit> units = paserUnit.getUnits();
        for (PaserUnit unit : units) {
            if (unit.getType().equals("Decl")) {
                parseDecl(unit);
            } else if (unit.getType().equals("FuncDef")) {
                parseFuncDef(unit);
            } else if (unit.getType().equals("MainFuncDef")) {
                FuncInfo mainMeta = new FuncInfo(currentSymbolTable);
                currentFunc = mainMeta;
                middleCode.putFunction(mainMeta);
                PaserUnit mainBlock = unit.getUnits().get(4);
                parseFuncBlock(mainBlock, mainMeta);
                middleCode.setMainFunction(mainMeta);
            }
        }
    }

    public void parseDecl(PaserUnit paserUnit) {
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
        // 完成了普通变量和数组
        PaserUnit ident = units.get(0);
        String name = ident.getName();
        boolean isConst = true;
        // 检查同级符号
        if (currentSymbolTable.contains(name, false)) {
            RepeatError(ident);
            return;
        }
        // 检查函数参数
        if (blockDepth == 1 && currentFunc != null && currentFunc.getParamTable().contains(name, false)) {
            RepeatError(ident);
            return;
        }

        PaserUnit ConstInitial = units.get(units.size() - 1);
        if (units.size() == 3) { // 非数组定义
            PaserUnit ConstExp = ConstInitial.getUnits().get(0);
            int value = new CalcUtil(currentSymbolTable).calcExp(ConstExp);
            Symbol s = new Symbol(Symbol.Type.ITEM, name, value, isConst);
            if (currentFunc != null) { // 是函数体内的常值变量
                stackSize += s.capacity(); // 计算当前栈地址
                s.setAddress(stackSize); // 设置偏移地址
                currentFunc.updateStackSize(stackSize); // 更新函数块的局部空间所占大小
                s.setLocal(true); // 标记为局部变量
                currentBlock.append(new UnaryOp(UnaryOp.Op.MOV, new Immediate(value), s)); // 向block输出中间代码 MOV value sym
            } else { // 是全局常值变量
                s.setAddress(currentSymbolTable.capacity()); // 设置全局空间偏移地址
                middleCode.addGlobalVariable(s.getName(), s.getInitValue(), s.getAddress()); // 向总类中加入该全局变量
            }
            currentSymbolTable.addSymbol(s); // 向当前符号表加入该符号
        } else { // 数组定义
            ArrayList<Integer> dims = new ArrayList<>(); // 计算数组每个维度大小
            ArrayList<Integer> initValue = new ArrayList<>(); // 计算所有初值
            for (PaserUnit unit : units) {
                if (unit.getType().equals("ConstExp")) {
                    PaserUnit ConstExp = unit;
                    dims.add(new CalcUtil(currentSymbolTable).calcExp(ConstExp));
                }
            }
            ArrayList<PaserUnit> inits = ConstInitial.getUnits();
            for (PaserUnit init : inits) { // 将初始化的所有值保存成一维数组
                if (init.getType().equals("ConstInitVal")) {
                    ArrayList<PaserUnit> inits2 = init.getUnits();
                    if (inits2.get(0).getType().equals("ConstExp")) { // 一维数组
                        PaserUnit ConstExp = inits2.get(0);
                        initValue.add(new CalcUtil(currentSymbolTable).calcExp(ConstExp));
                    } else { // 二维数组
                        for (PaserUnit unit : inits2) {
                            if (unit.getType().equals("ConstInitVal")) {
                                PaserUnit ConstExp = unit.getUnits().get(0);
                                initValue.add(new CalcUtil(currentSymbolTable).calcExp(ConstExp));
                            }
                        }

                    }
                }
            }

            Symbol s = new Symbol(name,  dims, true, initValue);

            if (currentFunc != null) { // 函数内定义的常量数组
                stackSize += s.capacity(); // 计算当前栈大小
                s.setAddress(stackSize); // 设置偏移地址
                currentFunc.updateStackSize(stackSize); // 更新函数信息块保存的栈大小
                s.setLocal(true); // 标记为局部变量
                // 初始化
                int offset = 0;
                for (int value : initValue) { // 生成初始化的中间代码
                    Symbol ptr = Symbol.temporary(Symbol.Type.POINTER); // 设置类型为指针的临时变量
                    currentBlock.append(new AddressOffset(s, new Immediate(offset * 4), ptr));
                    currentBlock.append(new PointerOp(PointerOp.Op.STORE, ptr, new Immediate(value)));
                    offset++;
                }
            } else { // 全局常数数组
                s.setAddress(currentSymbolTable.capacity()); // 设置偏移地址
                middleCode.addGlobalArray(s.getName(), s.getInitArray(), s.getAddress()); // 将初始值与偏移地址存入全局信息块，生成global区的中间代码
            }
            currentSymbolTable.addSymbol(s); // 向当前符号表加入该符号
        }

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
        // 完成了普通变量与数组
        PaserUnit ident = units.get(0);
        String name = ident.getName();
        boolean isConst = false;
        // 检查同级符号
        if (currentSymbolTable.contains(name, false)) {
            RepeatError(ident);
            return;
        }
        // 检查函数参数
        if (blockDepth == 1 && currentFunc != null && currentFunc.getParamTable().contains(name, false)) {
            RepeatError(ident);
            return;
        }

        if (units.size() > 1 && units.get(1).getType().equals("[")) { // 是数组
            ArrayList<Integer> dims = new ArrayList<>(); // 计算数组每个维度大小
            for (PaserUnit unit : units) {
                if (unit.getType().equals("ConstExp")) {
                    PaserUnit ConstExp = unit;
                    dims.add(new CalcUtil(currentSymbolTable).calcExp(ConstExp));
                }
            }
            if (units.get(units.size() - 1).getType().equals("InitVal")) { // 有初值
                PaserUnit InitVal = units.get(units.size() - 1);
                ArrayList<PaserUnit> inits = InitVal.getUnits();
                if (currentFunc == null) { // 为全局数组
                    ArrayList<Integer> initValue = new ArrayList<>(); // 计算所有初值
                    for (PaserUnit init : inits) { // 将初始化的所有值保存成一维数组
                        if (init.getType().equals("InitVal")) {
                            ArrayList<PaserUnit> inits2 = init.getUnits();
                            if (inits2.get(0).getType().equals("Exp")) { // 一维数组
                                PaserUnit Exp = inits2.get(0);
                                initValue.add(new CalcUtil(currentSymbolTable).calcExp(Exp));
                            } else { // 二维数组
                                for (PaserUnit unit : inits2) {
                                    if (unit.getType().equals("InitVal")) {
                                        PaserUnit Exp = unit.getUnits().get(0);
                                        initValue.add(new CalcUtil(currentSymbolTable).calcExp(Exp));
                                    }
                                }

                            }
                        }
                    }

                    Symbol s = new Symbol(name,  dims, false, initValue);

                    s.setAddress(currentSymbolTable.capacity()); // 设置偏移地址
                    middleCode.addGlobalArray(s.getName(), s.getInitArray(), s.getAddress()); // 将初始值与偏移地址存入全局信息块，生成data区的中间代码
                    currentSymbolTable.addSymbol(s); // 向当前符号表加入该符号
                } else { // 为有初值的局部数组，在运行时加载
                    Symbol s = new Symbol(name, dims);
                    stackSize += s.capacity();
                    s.setAddress(stackSize);
                    currentFunc.updateStackSize(stackSize);
                    s.setLocal(true);

                    currentSymbolTable.addSymbol(s); // 加入符号表

                    // 生成运行时加载的中间代码
                    int offset = 0;
                    for (PaserUnit init : inits) { // 将初始化的所有值保存成一维数组
                        if (init.getType().equals("InitVal")) {
                            ArrayList<PaserUnit> inits2 = init.getUnits();
                            if (inits2.get(0).getType().equals("Exp")) { // 一维数组
                                PaserUnit Exp = inits2.get(0);
                                Operand op = parseExp(Exp);
                                Symbol ptr = Symbol.temporary(Symbol.Type.POINTER); // 类型为指针(地址)的临时变量
                                currentBlock.append(new AddressOffset(s, new Immediate(offset * 4), ptr)); // 计算当前单元的地址
                                currentBlock.append(new PointerOp(PointerOp.Op.STORE, ptr, op)); // 向地址中存入保存了exp的计算结果的临时变量
                                offset++;
                            } else { // 二维数组
                                for (PaserUnit unit : inits2) {
                                    if (unit.getType().equals("InitVal")) {
                                        PaserUnit Exp = unit.getUnits().get(0);
                                        Operand op = parseExp(Exp);
                                        Symbol ptr = Symbol.temporary(Symbol.Type.POINTER); // 类型为指针(地址)的临时变量
                                        currentBlock.append(new AddressOffset(s, new Immediate(offset * 4), ptr)); // 计算当前单元的地址
                                        currentBlock.append(new PointerOp(PointerOp.Op.STORE, ptr, op)); // 向地址中存入保存了exp的计算结果的临时变量
                                        offset++;
                                    }
                                }

                            }
                        }
                    }
                }
            } else { // 没有初值
                Symbol s;
                if (currentFunc != null) { // 如果是局部变量
                    s = new Symbol(name, dims); // 只分配空间，无初值
                    stackSize += s.capacity();
                    s.setAddress(stackSize);
                    currentFunc.updateStackSize(stackSize);
                    s.setLocal(true);
                } else { // 如果是全局变量，默认初值为0
                    List<Integer> initZeros = new ArrayList<>();
                    int n = (dims.size() == 1) ? dims.get(0) : (dims.get(0) * dims.get(1));
                    for (int i = 0; i < n; i++) {
                        initZeros.add(0);
                    }
                    s = new Symbol(name, dims, false, initZeros);
                    s.setAddress(currentSymbolTable.capacity());
                    middleCode.addGlobalArray(s.getName(), s.getInitArray(), s.getAddress());
                }
                currentSymbolTable.addSymbol(s);
            }
        } else { // 如果不是数组
            if (units.get(units.size() - 1).getType().equals("InitVal")) { // 有初值
                PaserUnit Initial = units.get(units.size() - 1);
                PaserUnit Exp = Initial.getUnits().get(0);
                if (currentFunc == null) { // 没有在函数里，则必须能编译期算出，这是一个全局变量
                    int value = new CalcUtil(currentSymbolTable).calcExp(Exp); // 计算初值
                    Symbol s = new Symbol(Symbol.Type.ITEM, name, value, isConst); // 生成符号，这里标记了为变量
                    s.setAddress(currentSymbolTable.capacity()); // 设置全局空间偏移地址
                    currentSymbolTable.addSymbol(s); // 向当前符号表加入该符号
                    middleCode.addGlobalVariable(s.getName(), s.getInitValue(), s.getAddress()); // 向总类加入该全局变量
                } else {    // 在函数里的非常量，可以运行时计算
                    Symbol s = new Symbol(name); // 初始值在函数中，需通过中间代码进行计算
                    stackSize += s.capacity(); // 计算栈内偏移地址
                    s.setAddress(stackSize); // 设置偏移地址
                    currentFunc.updateStackSize(stackSize); // 更新函数块的局部变量空间
                    s.setLocal(true); // 标记为变量
                    currentSymbolTable.addSymbol(s); // 向符号表加入该符号
                    Operand val = parseExp(Exp); // 生成计算初值的中间代码，返回值为结果的临时变量
                    currentBlock.append(new UnaryOp(UnaryOp.Op.MOV, val, s)); // 生成将临时变量赋值到该变量的中间代码 mov val sym
                }
            } else { // 没有初值
                Symbol s;
                if (Objects.isNull(currentFunc)) { // 是全局变量
                    s = new Symbol(name, 0, isConst); // 初值为0
                    s.setAddress(currentSymbolTable.capacity()); // 设置全局偏移地址
                    middleCode.addGlobalVariable(s.getName(), s.getInitValue(), s.getAddress()); // 向总类加入该变量
                } else { // 是局部变量，仅加入符号表并设置地址
                    s = new Symbol(name);
                    stackSize += s.capacity();
                    s.setAddress(stackSize);
                    currentFunc.updateStackSize(stackSize);
                    s.setLocal(true);
                }
                currentSymbolTable.addSymbol(s);
            }
        }
    }

    public void parseFuncDef(PaserUnit paserUnit) {
        ArrayList<PaserUnit> units = paserUnit.getUnits();
        // 维护函数符号表
        // 获取函数名与函数类型
        String type = units.get(0).getUnits().get(0).getType();
        PaserUnit ident = units.get(1);
        String name = ident.getName();
        FuncInfo.ReturnType returnType = type.equals("void") ? FuncInfo.ReturnType.VOID : FuncInfo.ReturnType.INT;
        if (middleCode.getFunctions().containsKey(name) || currentSymbolTable.contains(name, false)) {
            RepeatError(ident);
            return;
        }
        // 创建函数管理类，用于保存当前函数的信息，提供给定义变量等地方使用
        FuncInfo func = new FuncInfo(name, returnType, currentSymbolTable);
        currentFunc = func; // 将解读器的函数信息指定为当前解读中的函数信息块
        middleCode.putFunction(func); // 向中间代码总类中加入该函数信息块
        // 遍历形参表
        if (units.get(3).getType().equals("FuncFParams")) {
            parseFuncFParams(units.get(3), func);
        }
        // 处理函数体
        parseFuncBlock(units.get(units.size() - 1), func);
    }

    private void parseFuncFParams(PaserUnit paserUnit, FuncInfo func) {
        ArrayList<PaserUnit> units = paserUnit.getUnits();
        ArrayList<Symbol> symbols = new ArrayList<>();
        for (PaserUnit unit : units) {
            if (unit.getType().equals("FuncFParam")) {
                PaserUnit ident = unit.getUnits().get(1);
                if (func.getParamTable().contains(ident.getName(), false)) {
                    RepeatError(ident); // 只报错并阻止该符号进入符号表，没有跳过其他程序的解析
                } else {
                    parseFuncFParam(unit, func); // 将函数块内的参数加入符号表
                }
            }
        }
    }

    private void parseFuncFParam(PaserUnit paserUnit, FuncInfo func) {
        // 完成了普通变量与数组的情况
        ArrayList<PaserUnit> units = paserUnit.getUnits();
        PaserUnit ident = units.get(1);
        String name = ident.getName();
        if (units.size() == 2) { // 如果是非数组变量
            Symbol s = new Symbol(name);
            func.addParam(s); // 向函数信息块中保存该形参
            s.setAddress(func.getParamTable().capacity()); // 设置栈内偏移地址
            s.setLocal(true); // 标记为变量
        } else { // 数组变量
            List<Integer> dims = new ArrayList<>();
            // first dim is ignored because Array-FParam is Pointer

            if (units.size() > 5) {
                // 参数为二维数组
                PaserUnit ConstExp = units.get(5);
                int length = new CalcUtil(currentSymbolTable).calcExp(ConstExp);
                dims.add(length);
            }
            Symbol s = new Symbol(name, dims, false);
            func.addParam(s); // 向函数信息块中保存该形参
            s.setAddress(func.getParamTable().capacity()); // 设置栈内偏移地址
            s.setLocal(true); // 标记为变量
        }
    }

    private void parseFuncBlock(PaserUnit funcBody, FuncInfo func) {
        ArrayList<PaserUnit> units = funcBody.getUnits();
        currentSymbolTable = func.getParamTable(); // 将目前的符号表设置为函数的符号表
        stackSize = func.getParamTable().capacity(); // 栈大小为目前的栈大小
        BasicBlock block = parseBlock(funcBody); // 解读block
        BasicBlock body = new BasicBlock(func.getLabelName(), BasicBlock.Type.FUNC); // 用func来设置跳转，包装解读后的block语句
        body.append(new Jump(block));
        currentBlock.append(new Return()); // 给当前block语句块插入返回语句
        currentBlock = null;
        func.loadBody(body); // 将完整的函数中间代码链保存在函数信息块中
        currentSymbolTable = currentSymbolTable.getFather(); // 将全局符号表设置为上层符号表，退出本层级的解读
        currentFunc = null;
        // TODO 检查是否存在返回语句
        boolean returnFlag = false;
        PaserUnit blockItem = units.get(units.size() - 2);
        if (blockItem.getType().equals("BlockItem")) {
            PaserUnit stmt = blockItem.getUnits().get(0);
            if (stmt.getType().equals("Stmt")) {
                // 如果有return且为int 就不会报错
                returnFlag = stmt.getStmtType().equals("Return");
            }
        }
        if (!(returnFlag || func.getReturnType().equals(FuncInfo.ReturnType.VOID))) {
            ReturnLossError(units.get(units.size() - 1));
        }
    }

    public BasicBlock parseBlock(PaserUnit paserUnit) {
        ArrayList<PaserUnit> units = paserUnit.getUnits();
        BasicBlock block = new BasicBlock("B_" + newBlockCount(), BasicBlock.Type.BASIC); // 创建block块，命名按照全局block顺序即可
        if (currentBlock != null) { // 如果是在语句块内
            currentBlock.append(new Jump(block)); // 加入跳转
        }
        currentBlock = block; // 将current块指向目前块
        BasicBlock follow = new BasicBlock("B_" + newBlockCount(), BasicBlock.Type.BASIC);
        currentSymbolTable = new SymbolTable(currentSymbolTable);  // symbol push stack
        blockDepth++; // 记录目前的块深度，方便将函数块符号表的形参当作处于目前块，不需要重写符号表生成（在函数解读中需要先把形参加入符号表，但块解读时会再生成一层符号表）
        // 一条一条语句去遍历就行
        for (PaserUnit unit : units) {
            if (unit.getType().equals("BlockItem")) {
                parseBlockItem(unit);
            }
        }
        currentBlock.append(new Jump(follow)); // 这里分割了块，因为链式存储的中间代码没办法没办法回到原来的块后
        currentBlock = follow; // 原块剩余的部分分为了新的块
        currentSymbolTable = currentSymbolTable.getFather();  // symbol pop stack
        blockDepth--; // 深度--
        return block;
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
        if (paserUnit.getStmtType().equals("Getint")) {
            parseGetint(paserUnit);
        } else if (paserUnit.getStmtType().equals("Assign")) {
            parseAssign(paserUnit);
        } else if (paserUnit.getStmtType().equals("Exp")) {
            parseExpStmt(paserUnit);
        } else if (paserUnit.getStmtType().equals("Printf")) {
            parsePrintf(paserUnit);
        } else if (paserUnit.getStmtType().equals("Block")) {
            PaserUnit block = paserUnit.getUnits().get(0);
            parseBlock(block);
        } else if (paserUnit.getStmtType().equals("Return")) {
            parseReturn(paserUnit);
        } else if (paserUnit.getStmtType().equals("Break")) {
            parseBreak(paserUnit);
        } else if (paserUnit.getStmtType().equals("Continue")) {
            parseContinue(paserUnit);
        } else if (paserUnit.getStmtType().equals("If")) {
            parseIf(paserUnit);
        } else if (paserUnit.getStmtType().equals("While")) {
            parseWhile(paserUnit);
        }
        // TODO if, while, break, continue,
    }

    public void parseGetint(PaserUnit paserUnit) {
        PaserUnit lval = paserUnit.getUnits().get(0);
        // 检查符号表，检查变量类型
        Symbol leftSym = checkLVal(lval);
        currentBlock.append(new Input(leftSym)); // 生成中间代码 input sym
    }

    public void parseAssign(PaserUnit stmt) {
        ArrayList<PaserUnit> units = stmt.getUnits();
        // 常量修改错误
        PaserUnit lVal = units.get(0); // 左节点为LVal
        PaserUnit exp = units.get(2); // 右节点为Exp
        Symbol leftSym = checkLVal(lVal); // 通过符号表进行错误处理并获取符号表对应的符号
        Operand rn = parseExp(exp); // 解析Exp
        if (Objects.isNull(leftSym)) { // LVal出错
            return;
        }
        if (leftSym.getType().equals(Symbol.Type.ITEM)) {
            currentBlock.append(new UnaryOp(UnaryOp.Op.MOV, rn, leftSym)); // 普通变量更新
        } else if (leftSym.getType().equals(Symbol.Type.POINTER)) { // 如果是地址，说明原来是数组变量，要向该地址处保存信息
            currentBlock.append(new PointerOp(PointerOp.Op.STORE, leftSym, rn));
        }
    }

    public void parseExpStmt(PaserUnit stmt) {
        parseExp(stmt.getUnits().get(0));
    }

    public void parsePrintf(PaserUnit stmt) {
        // 检查 FormatString, 检查参数和格式符的个数(以及类型)
        // 生成输出语句
        ArrayList<PaserUnit> units = stmt.getUnits();
        String format = units.get(2).getFormatStringInner();
        int count = checkFormatString(format);
        if (count < 0) {
            return;
        }
        List<Operand> params = new ArrayList<>();
        for (PaserUnit unit : units) {
            if (unit.getType().equals("Exp")) {
                Operand param = parseExp(unit);
                params.add(param);
            }
        }
        if (params.size() != count) {
            FormatStringParamError(units.get(0));
            return;
        }
        currentBlock.append(new PrintFormat(format, params));
    }

    private int checkFormatString(String format) {
        int l = format.length();
        int count = 0;
        for (int i = 0; i < l; i++) {
            char c = format.charAt(i);
            if (c != 32 && c != 33 && !(c >= 40 && c <= 126)) {
                if (c == '%') {
                    if (i < l - 1 && format.charAt(i + 1) == 'd') {
                        count = count + 1;
                        continue;
                    } else {
                        return -1;
                    }
                }
                return -1;
            }
            if (c == 92 && (i >= l - 1 || format.charAt(i + 1) != 'n')) {
                return -1;
            }
        }
        return count;
    }

    public void parseReturn(PaserUnit stmt) {
        ArrayList<PaserUnit> units = stmt.getUnits();
        // return 语句类型和当前函数的类型是不是匹配
        if (currentFunc.getReturnType().equals(FuncInfo.ReturnType.INT)) {
            // 默认不出现 return;
            Operand value = parseExp(units.get(1));
            currentBlock.append(new Return(value));
        } else {
            if (units.size() > 2) {
                VoidReturnError(units.get(0));
            } else {
                currentBlock.append(new Return());
            }
        }
    }

    public void parseBreak(PaserUnit stmt) {
        // 就是一个跳转，跳到往上的循环的下一层
        // 检查是否非循环块
        if (loopBlocks.empty()) {
            BreakOrContinueError(stmt.getUnits().get(0));
            return;
        }
        BasicBlock follow = loopFollows.peek();
        currentBlock.append(new Jump(follow));
    }

    public void parseContinue(PaserUnit stmt) {
        // 也是一个跳转，跳到往上的循环的头
        // 检查是否非循环块
        if (loopBlocks.empty()) {
            BreakOrContinueError(stmt.getUnits().get(0));
            return;
        }
        BasicBlock loop = loopBlocks.peek();
        currentBlock.append(new Jump(loop));
    }

    public void parseIf(PaserUnit stmt) {
        ArrayList<PaserUnit> units = stmt.getUnits();
        // 生成新的基本块
        Operand cond = parseCond(units.get(2));
        BasicBlock current = currentBlock;
        BasicBlock follow = new BasicBlock("B_" + newBlockCount(), BasicBlock.Type.BASIC);
        BasicBlock then = new BasicBlock("IF_THEN_" + newBlockCount(), BasicBlock.Type.BRANCH);
        if (units.size() > 5) {
            PaserUnit thenStmt = units.get(4);
            PaserUnit elseStmt = units.get(6);
            BasicBlock elseBlk = new BasicBlock("IF_ELSE_" + newBlockCount(), BasicBlock.Type.BRANCH);
            current.append(new BranchIfElse(cond, then, elseBlk));
            currentBlock = then;
            parseStmt(thenStmt);
            currentBlock.append(new Jump(follow));
            currentBlock = elseBlk;
            parseStmt(elseStmt);
        } else {
            current.append(new BranchIfElse(cond, then, follow));
            currentBlock = then;
            PaserUnit thenStmt = units.get(4);
            parseStmt(thenStmt);
        }
        currentBlock.append(new Jump(follow));
        currentBlock = follow;
    }

    public void parseWhile(PaserUnit stmt) {
        // 生成新的基本块
        BasicBlock current = currentBlock;
        BasicBlock follow = new BasicBlock("B_" + newBlockCount(), BasicBlock.Type.BASIC);
        BasicBlock body = new BasicBlock("LOOP_" + newBlockCount(), BasicBlock.Type.BASIC);
        BasicBlock loop = new BasicBlock("WHILE_" + newBlockCount(), BasicBlock.Type.LOOP);
        current.append(new Jump(loop));
        loopBlocks.push(loop);
        loopFollows.push(follow);
        currentBlock = loop;
        ArrayList<PaserUnit> units = stmt.getUnits();
        Operand cond = parseCond(units.get(2));
        currentBlock.append(new BranchIfElse(cond, body, follow));
        currentBlock = body;
        parseStmt(units.get(4));
        loopFollows.pop();
        loopBlocks.pop();
        currentBlock.append(new Jump(loop));
        currentBlock = follow;
    }








    private Symbol checkLVal(PaserUnit lval) {
        Operand ln = parseLVal(lval, true);
        if (Objects.isNull(ln) || ln instanceof Immediate) {
            return null;
        }
        if (!(ln instanceof Symbol)) {
            return null; // due to undefined symbol error
        }
        Symbol leftSym = (Symbol) ln;
        if (leftSym.isConst()) {
            ConstError(lval.getUnits().get(0));
            return null;
        }
        return leftSym;
    }

    public Operand parseLVal(PaserUnit paserUnit, boolean isLeft) {
        // 完成了对数字，数组和数组参数的解读
        // 符号表相关错误(变量未定义等)
        ArrayList<PaserUnit> units = paserUnit.getUnits();
        PaserUnit ident = units.get(0);
        if (!currentSymbolTable.contains(ident.getName(), true)) {
            UndefinedError(ident);
            return new Immediate(0);
        }
        Symbol symbol = currentSymbolTable.getSymbol(ident.getName()); // 从符号表中递归获取符号
        if (symbol.getType() == Symbol.Type.ITEM) { // 如果只是普通变量
            return symbol; // 直接返回
        } else {
            List<Operand> indexes = new ArrayList<>(); // 获取所有下标
            for (PaserUnit unit : units) {
                if (unit.getType().equals("Exp")) {
                    indexes.add(parseExp(unit)); // 解析Exp得到下标
                }
            }
            Operand offset = new Immediate(0); // 存储相对数组首地址的偏移
            for (int i = indexes.size() - 1; i >= 0; i--) {
                // 计算当前值位于数组空间的哪个位置
                Symbol prod = Symbol.temporary(Symbol.Type.ITEM);
                Operand offsetBase = new Immediate(symbol.getBaseOfDim(i)); // 获取后缀积用于计算偏移
                currentBlock.append(new BinaryOp(BinaryOp.Op.MUL, indexes.get(i), offsetBase, prod)); // 输出计算偏移的中间代码 prod = j * prod(按数组层级得到的数据空间大小)
                Symbol sum = Symbol.temporary(Symbol.Type.ITEM);
                currentBlock.append(new BinaryOp(BinaryOp.Op.ADD, offset, prod, sum)); // 输出计算偏移的中间代码 sum = prod + offset
                offset = sum; // 将值赋给offset用于下一次迭代
            }
            if (symbol.getType().equals(Symbol.Type.ARRAY)) {
                // ARRAY
                int depth = indexes.size();
                Symbol ptr = symbol.toPointer().subPointer(depth);
                currentBlock.append(new AddressOffset(symbol, offset, ptr));
                if (isLeft || depth < symbol.getDimCount()) {
                    return ptr;
                } else {
                    Symbol value = Symbol.temporary(Symbol.Type.ITEM);
                    currentBlock.append(new PointerOp(PointerOp.Op.LOAD, ptr, value));
                    return value;
                }
            } else {
                // POINTER
                int depth = indexes.size();
                Symbol ptr = symbol.subPointer(depth);
                currentBlock.append(new AddressOffset(symbol, offset, ptr));
                if (isLeft || depth <= symbol.getDimCount()) {
                    return ptr;
                } else {
                    Symbol value = Symbol.temporary(Symbol.Type.ITEM);
                    currentBlock.append(new PointerOp(PointerOp.Op.LOAD, ptr, value));
                    return value;
                }
            }
        }
    }

    public Operand parseUnaryExp(PaserUnit paserUnit) {
        PaserUnit base = paserUnit.getBaseUnaryExp();
        Operand result = null;
        if (base.getUnits().get(0).getType().equals("PrimaryExp")) { // 该表达式为一元表达式，(Exp) | LVal | Number
            result = parseBasePrimaryExp(base.getUnits().get(0)); // 分析基础一元表达式
        } else {
            // 如果该表达式为函数调用
            // 查符号表, 确认参数，传递参数，参数不匹配错误
            // 如果调用了 void 函数，返回 null
            ArrayList<PaserUnit> units = base.getUnits();
            PaserUnit ident = units.get(0);
            String name = ident.getName();
            if (!middleCode.getFunctions().containsKey(name)) { // 查符号表，未定义错误
                UndefinedError(ident);
                return new Immediate(0);
            }
            FuncInfo func = middleCode.getFunctions().get(name); // 获取函数信息块
            // match arguments
            ArrayList<Operand> params = new ArrayList<>();
            List<Symbol> args = func.getParams(); // 从函数信息块中获取参数表
            if (units.get(2).getType().equals("FuncRParams")) { // 如果有参数传递，获取函数调用的传参
                PaserUnit rParams = units.get(2);
                ArrayList<PaserUnit> rParamsUnits = rParams.getUnits();
                for (PaserUnit paramsUnit : rParamsUnits) {
                    if (paramsUnit.getType().equals("Exp")) {
                        Operand r = parseExp(paramsUnit); // 分析函数传参的表达式
                        params.add(r); // 将函数参数表达式的中间代码保存在params中
                    }
                }
            }
            boolean error = false; // 检查参数列表是否匹配
            if (params.size() != args.size()) {
                FuncRParamsError(ident);
                error = true;
            } else {
                Iterator<Operand> iterParam = params.listIterator();
                Iterator<Symbol> iterArg = args.listIterator();
                while (iterParam.hasNext() && iterArg.hasNext()) {
                    Operand param = iterParam.next();
                    Symbol arg = iterArg.next();
                    if (Objects.isNull(param)) {
                        FuncRParamsTypeError(ident);
                        error = true;
                        break;
                    } else if (param instanceof Immediate) {
                        if (!arg.getType().equals(Symbol.Type.ITEM)) {
                            FuncRParamsTypeError(ident);
                            error = true;
                            break;
                        }
                    } else {
                        if (!((Symbol) param).getType().equals(arg.getType())) {
                            FuncRParamsTypeError(ident);
                            error = true;
                            break;
                        }
                    }
                }
            }
            // check argument match

            if (func.getReturnType().equals(FuncInfo.ReturnType.VOID)) { // 返回值为void
                if (!error) {
                    currentBlock.append(new Call(func, params)); // 生成中间代码 函数调用 call func params
                }
                return null; // 返回null
            } else { // 返回值为int
                if (!error) { // 没出错时插入中间代码
                    Symbol r = Symbol.temporary(Symbol.Type.ITEM); // 生成临时变量来保存函数返回值
                    currentBlock.append(new Call(func, params, r)); // 生成中间代码 函数调用 call func params r
                    result = r; // 返回临时变量符号
                } else { // 有出错
                    return new Immediate(0);
                }
            }
        }
        ArrayList<PaserUnit> ops = paserUnit.getUnaryOp();
        for (PaserUnit op : ops) { // 对一元运算符进行统一处理
            Symbol tmp = Symbol.temporary(Symbol.Type.ITEM);
            UnaryOp ir = new UnaryOp(tokenToUnaryOp(op), result, tmp); // 生成中间代码 一元运算 op result tmp
            currentBlock.append(ir);
            result = tmp;
        }
        return result;
    }

    public Operand parseBasePrimaryExp(PaserUnit paserUnit) {
        String Ptype = paserUnit.getPrimaryExpType();
        if (Ptype.equals("LVal")) { // 如果是变量
            PaserUnit LVal = paserUnit.getUnits().get(0);
            return parseLVal(LVal, false); // 此处不是等待赋值的变量，而是参与计算的变量
        } else if (Ptype.equals("Exp")) { // 如果是表达式
            PaserUnit exp = paserUnit.getUnits().get(1);
            return parseExp(exp);
        } else if (Ptype.equals("Number")) { // 如果是数字
            PaserUnit number = paserUnit.getUnits().get(0);
            return new Immediate(number.getUnits().get(0).getNum()); // 返回符号 类型为立即数
        }
        return null;
    }

    private UnaryOp.Op tokenToUnaryOp(PaserUnit op) {
        if (op.getType().equals("!")) {
            return UnaryOp.Op.NOT;
        } else if (op.getType().equals("+")) {
            return UnaryOp.Op.MOV;
        } else if (op.getType().equals("-")) {
            return UnaryOp.Op.NEG;
        }
        return null;
    }

    private BinaryOp.Op tokenToBinaryOp(PaserUnit op) {
        String type = op.getType();
        if (type.equals("+")) {
            return BinaryOp.Op.ADD;
        } else if (type.equals("-")) {
            return BinaryOp.Op.SUB;
        } else if (type.equals("*")) {
            return BinaryOp.Op.MUL;
        } else if (type.equals("%")) {
            return BinaryOp.Op.MOD;
        } else if (type.equals("/")) {
            return BinaryOp.Op.DIV;
        } else if (type.equals("!=")) {
            return BinaryOp.Op.NE;
        } else if (type.equals("==")) {
            return BinaryOp.Op.EQ;
        } else if (type.equals("<")) {
            return BinaryOp.Op.LT;
        } else if (type.equals("<=")) {
            return BinaryOp.Op.LE;
        } else if (type.equals(">")) {
            return BinaryOp.Op.GT;
        } else if (type.equals(">=")) {
            return BinaryOp.Op.GE;
        } else if (type.equals("||")) {
            return BinaryOp.Op.ORL;
        } else if (type.equals("&&")) {
            return BinaryOp.Op.ANDL;
        }
        return null;
    }

    public Operand parseExp(PaserUnit exp) {
        PaserUnit addExp = exp.getUnits().get(0);
        return parseAddExp(addExp);
    }

    public Operand parseAddExp(PaserUnit addExp) {
        ArrayList<PaserUnit> units = addExp.getUnits();
        Operand result = parseMulExp(units.get(0));
        for (int i = 1; i < units.size(); i = i + 2) {
            PaserUnit op = units.get(i);
            PaserUnit mulExp = units.get(i + 1);
            Operand subResult = parseMulExp(mulExp); // 计算该计算单元
            if (Objects.isNull(subResult)) { // 检查是否有错
                return null;
            }
            Symbol temp = Symbol.temporary(Symbol.Type.ITEM); // 生成临时变量
            currentBlock.append(new BinaryOp(tokenToBinaryOp(op), result, subResult, temp)); // 输出中间代码，op result subResult temp
            result = temp; // 将temp作为新的左操作数
        }
        return result;
    }

    public Operand parseMulExp(PaserUnit mulExp) {
        ArrayList<PaserUnit> units = mulExp.getUnits();
        Operand result = parseUnaryExp(units.get(0));
        for (int i = 1; i < units.size(); i = i + 2) {
            PaserUnit op = units.get(i);
            PaserUnit unaryExp = units.get(i + 1);
            Operand subResult = parseUnaryExp(unaryExp); // 计算该计算单元
            if (Objects.isNull(subResult)) { // 检查是否有错
                return null;
            }
            Symbol temp = Symbol.temporary(Symbol.Type.ITEM); // 生成临时变量
            currentBlock.append(new BinaryOp(tokenToBinaryOp(op), result, subResult, temp)); // 输出中间代码，op result subResult temp
            result = temp; // 将temp作为新的左操作数
        }
        return result;
    }

    public Operand parseRelExp(PaserUnit relExp) {
        ArrayList<PaserUnit> units = relExp.getUnits();
        Operand result = parseAddExp(units.get(0));
        for (int i = 1; i < units.size(); i = i + 2) {
            PaserUnit op = units.get(i);
            PaserUnit addExp = units.get(i + 1);
            Operand subResult = parseAddExp(addExp); // 计算该计算单元
            if (Objects.isNull(subResult)) { // 检查是否有错
                return null;
            }
            Symbol temp = Symbol.temporary(Symbol.Type.ITEM); // 生成临时变量
            currentBlock.append(new BinaryOp(tokenToBinaryOp(op), result, subResult, temp)); // 输出中间代码，op result subResult temp
            result = temp; // 将temp作为新的左操作数
        }
        return result;
    }

    public Operand parseEqExp(PaserUnit eqExp) {
        ArrayList<PaserUnit> units = eqExp.getUnits();
        Operand result = parseRelExp(units.get(0));
        for (int i = 1; i < units.size(); i = i + 2) {
            PaserUnit op = units.get(i);
            PaserUnit relExp = units.get(i + 1);
            Operand subResult = parseRelExp(relExp); // 计算该计算单元
            if (Objects.isNull(subResult)) { // 检查是否有错
                return null;
            }
            Symbol temp = Symbol.temporary(Symbol.Type.ITEM); // 生成临时变量
            currentBlock.append(new BinaryOp(tokenToBinaryOp(op), result, subResult, temp)); // 输出中间代码，op result subResult temp
            result = temp; // 将temp作为新的左操作数
        }
        return result;
    }

    // 已废弃
    public Operand parseLAndExp(PaserUnit lAndExp) {
        ArrayList<PaserUnit> units = lAndExp.getUnits();
        Operand result = parseEqExp(units.get(0));
        for (int i = 1; i < units.size(); i = i + 2) {
            PaserUnit op = units.get(i);
            PaserUnit eqExp = units.get(i + 1);
            Operand subResult = parseEqExp(eqExp); // 计算该计算单元
            if (Objects.isNull(subResult)) { // 检查是否有错
                return null;
            }
            Symbol temp = Symbol.temporary(Symbol.Type.ITEM); // 生成临时变量
            currentBlock.append(new BinaryOp(tokenToBinaryOp(op), result, subResult, temp)); // 输出中间代码，op result subResult temp
            result = temp; // 将temp作为新的左操作数
        }
        return result;
    }

    // 已废弃
    public Operand parseLOrExp(PaserUnit lOrExp) {
        ArrayList<PaserUnit> units = lOrExp.getUnits();
        Operand result = parseLAndExp(units.get(0));
        for (int i = 1; i < units.size(); i = i + 2) {
            PaserUnit op = units.get(i);
            PaserUnit lAndExp = units.get(i + 1);
            Operand subResult = parseLAndExp(lAndExp); // 计算该计算单元
            if (Objects.isNull(subResult)) { // 检查是否有错
                return null;
            }
            Symbol temp = Symbol.temporary(Symbol.Type.ITEM); // 生成临时变量
            currentBlock.append(new BinaryOp(tokenToBinaryOp(op), result, subResult, temp)); // 输出中间代码，op result subResult temp
            result = temp; // 将temp作为新的左操作数
        }
        return result;
    }

    public Operand parseConstExp(PaserUnit exp) {
        PaserUnit addExp = exp.getUnits().get(0);
        return parseAddExp(addExp);
    }

    public Operand parseCond(PaserUnit cond) {
        PaserUnit lOrExp = cond.getUnits().get(0);
        return analyseLOrExp(lOrExp);
    }

    // 短路求值! 前一项如果为 True 就不用算后面的项了
    public Operand analyseLOrExp(PaserUnit lOrExp) {
        BasicBlock orFollow = new BasicBlock("COND_OR_" + newBlockCount(), BasicBlock.Type.BASIC);
        Symbol or = Symbol.temporary(Symbol.Type.ITEM); // or result
        ArrayList<PaserUnit> units = lOrExp.getUnits();
        Operand and = analyseLAndExp(units.get(0));
        // 如果有错
        if (Objects.isNull(and)) {
            return null;
        }
        currentBlock.append(new UnaryOp(UnaryOp.Op.MOV, and, or));
        BasicBlock next = new BasicBlock("OR_AND_" + newBlockCount(), BasicBlock.Type.BASIC);
        currentBlock.append(new BranchIfElse(or, orFollow, next));
        currentBlock = next;
        for (int i = 1; i < units.size(); i = i + 2) {
            PaserUnit op = units.get(i);
            PaserUnit lAndExp = units.get(i + 1);
            and = analyseLAndExp(lAndExp); // 计算该计算单元
            if (Objects.isNull(and)) { // 检查是否有错
                return null;
            }
            currentBlock.append(new UnaryOp(UnaryOp.Op.MOV, and, or));
            next = new BasicBlock("OR_AND_" + newBlockCount(), BasicBlock.Type.BASIC);
            currentBlock.append(new BranchIfElse(or, orFollow, next));
            currentBlock = next;
        }
        currentBlock.append(new Jump(orFollow));
        currentBlock = orFollow;
        return or;
    }

    public Operand analyseLAndExp(PaserUnit lAndExp) {
        BasicBlock andFollow = new BasicBlock("COND_AND_" + newBlockCount(), BasicBlock.Type.BASIC);
        Symbol and = Symbol.temporary(Symbol.Type.ITEM); // and result
        ArrayList<PaserUnit> units = lAndExp.getUnits();
        Operand item = parseEqExp(units.get(0));
        if (Objects.isNull(item)) {
            return null;
        }
        currentBlock.append(new UnaryOp(UnaryOp.Op.MOV, item, and));
        BasicBlock next = new BasicBlock("AND_ITEM_" + newBlockCount(), BasicBlock.Type.BASIC);
        currentBlock.append(new BranchIfElse(and, next, andFollow));
        currentBlock = next;
        for (int i = 1; i < units.size(); i = i + 2) {
            PaserUnit op = units.get(i);
            PaserUnit eqExp = units.get(i + 1);
            item = parseEqExp(eqExp); // 计算该计算单元
            if (Objects.isNull(item)) { // 检查是否有错
                return null;
            }
            currentBlock.append(new UnaryOp(UnaryOp.Op.MOV, item, and));
            next = new BasicBlock("AND_ITEM_" + newBlockCount(), BasicBlock.Type.BASIC);
            currentBlock.append(new BranchIfElse(and, next, andFollow));
            currentBlock = next;
        }
        currentBlock.append(new Jump(andFollow));
        currentBlock = andFollow;
        return and;
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
    }

    private void parseExp(PaserUnit paserUnit) {

    }

}*/