package middle.symboltable;

import java.util.ArrayList;
import java.util.List;

public class Symbol {
    private Type type;
    private String name;
    private int dim;
    private int dim_1st;
    private int dim_2nd;
    private int constValue;
    private List<Integer> constDim1Value;
    private List<List<Integer>> constDim2Value;
    private ArrayList<Symbol> funcParams;

    public enum Type {
        ITEM,
        ARRAY,      // 数组
        POINTER,    // 作为函数参数或者数组寻址后的临时变量，**比数组少一维**！
    }

    public Symbol(Type type, String name, int dim) {
        this.type = type;
        this.name = name;
        this.dim = dim;
    }

    public int capacity() {
        if (type == Type.ITEM) {
            return 4;
        } else if (type == Type.POINTER) {
            return 4;
        }
        return 0;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public int getDim() {
        return dim;
    }

    public ArrayList<Symbol> getFuncParams() {
        return funcParams;
    }

    public int getConst() {
        return constValue;
    }

    public int getDim1Const(int i) {
        return constDim1Value.get(i);
    }

    public int getDim2Const(int i, int j) {
        return constDim2Value.get(i).get(j);
    }

    public void setConst(int value) {
        constValue = value;
    }

    public void setDim1Const(List<Integer> Dim1Value) {
        constDim1Value = Dim1Value;
    }

    public void setDim2Const(List<List<Integer>> Dim2Value) {
        constDim2Value = Dim2Value;
    }
}
