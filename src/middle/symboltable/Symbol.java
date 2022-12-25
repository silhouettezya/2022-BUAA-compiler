package middle.symboltable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import middle.middlecode.Operand;

public class Symbol implements Operand {
    private Type type;
    private String name;
    private int dim;
    private int dim_1st;
    private int dim_2nd;
    private int initValue;
    private List<Integer> constDim1Value;
    private List<List<Integer>> constDim2Value;
    private boolean local; // 是否为局部变量，如果是则基地址为当前运行栈栈底；如果否则基地址为全局空间头部
    private int address = 0; // 相对基地址的位移
    private boolean isConst;
    private List<Integer> dimSize;    // 每一维的长度，如果是指针则少一维
    private List<Integer> dimBase;    // dimSize 的后缀积
    private List<Integer> initArray;  // 数组的初值（展平了的）

    public enum Type {
        ITEM,
        ARRAY,      // 数组
        POINTER,    // 作为函数参数或者数组寻址后的临时变量，**比数组少一维**！
    }

    private static int tempCount = 0;
    public static Symbol temporary(Type type) {
        tempCount = tempCount + 1;
        Symbol sym;
        if (type.equals(Type.POINTER)) { // 计算地址的中间变量?
            sym = new Symbol("ptr_" + tempCount, Type.POINTER, false);
        } else {
            sym = new Symbol("tmp_" + tempCount);
        }
        sym.setLocal(true);
        return sym;
    }

    public Symbol(Type type, String name, int value, boolean isConst) {
        this.type = type;
        this.name = name;
        this.dim = 0;
        this.isConst = isConst;
        this.initValue = value;
    }

    public Symbol(String name) {
        this.type = Type.ITEM;
        this.name = name;
        this.dim = 0;
        this.isConst = false;
        this.initValue = 0;
    }

    public Symbol(String name, int value, boolean isConst) {
        this.type = Type.ITEM;
        this.name = name;
        this.initValue = value;
        this.isConst = isConst;
        this.dim = 0;
    }

    public Symbol(String name, Type type, boolean isConst) {
        if (type == Type.POINTER) {
            this.name = name;
            this.type = type;
            this.isConst = isConst;
            this.dimSize = Collections.emptyList();
            this.initValue = 0;
            this.initArray = Collections.emptyList();
            this.dimBase = suffixProduct(dimSize, 4, true);
        }
    }

    public Symbol(String name, List<Integer> dimSize, boolean isConst, List<Integer> init) {
        this.name = name;
        this.type = Type.ARRAY;
        this.isConst = isConst;
        this.dimSize = dimSize;
        this.initArray = init;
        this.dimBase = suffixProduct(dimSize, 4,false);
    }

    public Symbol(String name, List<Integer> dimSize) {
        this.name = name;
        this.type = Type.ARRAY;
        this.isConst = false;
        this.dimSize = dimSize;
        this.initArray = Collections.emptyList();
        this.dimBase = suffixProduct(dimSize, 4,false);
    }

    public Symbol(String name, List<Integer> dimSize, boolean isConst) {
        // 代表着数组与地址的变量(函数请求变量为数组时，因为有一维是空缺的，所以只能保存数组首地址与数组元素大小，数组维数)
        this.name = name;
        this.type = Type.POINTER;
        this.isConst = isConst;
        this.dimSize = dimSize;
        this.initArray = Collections.emptyList();
        this.dimBase = suffixProduct(dimSize, 4, true);
    }

    public int getDimCount() {
        return dimSize.size();
    }

    public Symbol toPointer() {
        // 将数组变量转化为地址变量(传参为数组而不是数组的某个值时)
        ArrayList<Integer> reducedDimSize = new ArrayList<>();
        for (int i = 1; i < dimSize.size(); i++) {
            // 第一维不会计入
            reducedDimSize.add(dimSize.get(i));
        }
        tempCount = tempCount + 1;
        Symbol sym = new Symbol("ptr_" + tempCount, reducedDimSize, isConst);
        sym.setLocal(true);
        return sym;
    }

    public Symbol subPointer(int depth) {
        // 通过层数取出要求的数组模式(二维取二维或一维，一维只能取一维)
        ArrayList<Integer> reducedDimSize = new ArrayList<>();
        for (int i = depth; i < dimSize.size(); i++) {
            reducedDimSize.add(dimSize.get(i));
        }
        tempCount = tempCount + 1;
        Symbol sym = new Symbol("ptr_" + tempCount, reducedDimSize, isConst);
        sym.setLocal(true);
        return sym;
    }

    public int getBaseOfDim(int dim) {
        // 获取指定的后缀积
        return dimBase.get(dim);
    }

    public int capacity() {
        if (type == Type.ITEM) {
            return 4;
        } else if (type == Type.POINTER) {
            return 4;
        } else if (type == Type.ARRAY) {
            return (dimSize.size() == 1) ? 4 * dimSize.get(0) : 4 * dimSize.get(0) * dimSize.get(1);
        }
        return 0;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public boolean isConst() {
        return isConst;
    }

    public int getAddress() {
        return address;
    }

    public int getDim() {
        return dim;
    }

    public int getInitValue() {
        return initValue;
    }

    public List<Integer> getInitArray() {
        return initArray;
    }

    public int getDim1Const(int i) {
        return constDim1Value.get(i);
    }

    public int getDim2Const(int i, int j) {
        return constDim2Value.get(i).get(j);
    }

    public void setConst(int value) {
        initValue = value;
    }

    public void setDim1Const(List<Integer> Dim1Value) {
        constDim1Value = Dim1Value;
    }

    public void setDim2Const(List<List<Integer>> Dim2Value) {
        constDim2Value = Dim2Value;
    }

    public List<Integer> getDimSize() {
        return dimSize;
    }

    public boolean isLocal() {
        return local;
    }

    public boolean hasAddress() {
        return this.address >= 0;
    }

    private ArrayList<Integer> suffixProduct(List<Integer> list, int basicSize, boolean pointer) {
        ArrayList<Integer> suffix = new ArrayList<>();
        int prod = basicSize;
        ArrayList<Integer> revInput = new ArrayList<>(list);
        Collections.reverse(revInput);
        for (int num : revInput) {
            suffix.add(prod);
            prod *= num;
        }
        if (pointer) {
            suffix.add(prod); // 如果是地址则还要存整个二维数组的大小
        }
        Collections.reverse(suffix); // 依次保存了二维数组大小空间，第二维的一维数组大小空间，整数大小空间
        return suffix;
    }
}
