package middle;

import middle.symboltable.FuncInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiddleCode {
    private Map<String, Integer> globalAddress;   // 这里的地址单位是字节（和 Mars 相同）
    private Map<String, Integer> globalVariables;
    private Map<String, FuncInfo> functions;
    private Map<String, String> globalStrings;        // FormatString
    private Map<String, List<Integer>> globalArrays;  // 展平的数组初值


    private FuncInfo mainFunction; // 主函数入口

    public MiddleCode() {
        this.globalAddress = new HashMap<>();
        this.globalVariables = new HashMap<>();
        this.globalStrings = new HashMap<>();
        this.functions = new HashMap<>();
        this.globalArrays = new HashMap<>();
    }

    public Map<String, Integer> getGlobalAddress() {
        return globalAddress;
    }

    public Map<String, Integer> getGlobalVariables() {
        return globalVariables;
    }

    public void addGlobalVariable(String name, int value, int address) {
        globalAddress.put(name, address);
        globalVariables.put(name, value);
    }

    public Map<String, FuncInfo> getFunctions() {
        return functions;
    }

    public FuncInfo getMainFunction() {
        return mainFunction;
    }

    public void putFunction(FuncInfo function) {
        functions.put(function.getName(), function);
    }

    public void setMainFunction(FuncInfo main) {
        mainFunction = main;
    }

    public Map<String, String> getGlobalStrings() {
        return globalStrings;
    }

    private int stringCount = 0;
    public String addGlobalString(String s) {
        stringCount = stringCount + 1;
        String label = "STR_" + stringCount;
        globalStrings.put("STR_" + stringCount, s);
        return label;
    }

    public Map<String, List<Integer>> getGlobalArrays() {
        return globalArrays;
    }

    public void addGlobalArray(String name, List<Integer> values, int address) {
        globalAddress.put(name, address);
        globalArrays.put(name, values);
    }
}
