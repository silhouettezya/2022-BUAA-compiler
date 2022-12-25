package middle.symboltable;

import middle.middlecode.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FuncInfo {
    private final String name;
    private final SymbolTable paramTable; // 函数的符号表，上层符号表仅为全局符号表，所以可以自己设置
    private final List<Symbol> params = new ArrayList<>();
    private BasicBlock body;
    private int stackSize = 0; // 所有局部变量所占的空间(如临时变量需要分配空间必须在所有局部变量之后)
    private final boolean main;

    public enum ReturnType {
        INT,
        VOID
    }

    private final ReturnType refType;


    public FuncInfo(String name, ReturnType refType, SymbolTable global) {
        this.name = name;
        this.paramTable = new SymbolTable(global); // 因为是函数的符号表，其上级符号表仅为全局符号表，可以直接设置
        this.refType = refType;
        this.main = false;
    }

    public FuncInfo(SymbolTable global) {
        this.name = "main";
        this.paramTable = new SymbolTable(global);
        this.refType = ReturnType.INT;
        this.main = true;
    }

    public boolean isMain() {
        return main;
    }

    public ReturnType getReturnType() {
        return refType;
    }

    public String getName() { // 返回原始的函数名，用于符号表
        return name;
    }

    public String getLabelName() { // 返回加前缀的函数名，用于生成跳转标签
        return "FUNC_" + name; // 生成标签名时加上前缀，防止函数名与自动生成的标签重名
    }

    public SymbolTable getParamTable() {
        return paramTable;
    }

    public List<Symbol> getParams() {
        return params;
    }

    public void addParam(Symbol param) {
        paramTable.addSymbol(param);
        params.add(param);
        updateStackSize(paramTable.capacity());
    }

    public void loadBody(BasicBlock body) {
        this.body = body;
    }

    public BasicBlock getBody() {
        return body;
    }

    public void updateStackSize(int size) {
        stackSize = Math.max(stackSize, size);
    }

    public int getStackSize() {
        return stackSize;
    }
}
