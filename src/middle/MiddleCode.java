package middle;

import middle.middlecode.BasicBlock;
import middle.middlecode.BranchIfElse;
import middle.middlecode.Jump;
import middle.middlecode.Node;
import middle.symboltable.FuncInfo;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

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

    public void output() {
        // global variables
        System.out.println("======= IR =======");
        System.out.println("\n== Global Variables ==");
        for (Map.Entry<String, Integer> entry : globalVariables.entrySet().stream()
                .sorted(Comparator.comparingInt(stringIntegerEntry ->
                        globalAddress.get(stringIntegerEntry.getKey()))).collect(Collectors.toList())) {
            System.out.printf("%s[0x%x]: %d\n", entry.getKey(), globalAddress.get(entry.getKey()), entry.getValue());
        }
        System.out.println(("\n== Global Arrays =="));
        for (Map.Entry<String, List<Integer>> entry : globalArrays.entrySet().stream()
                .sorted(Comparator.comparingInt(stringListEntry ->
                        globalAddress.get(stringListEntry.getKey()))).collect(Collectors.toList())) {
            System.out.printf("%s[0x%x]: [%s]\n", entry.getKey(), globalAddress.get(entry.getKey()), entry.getValue().stream()
                    .map(Object::toString).reduce((s, s2) -> s + ", " + s2).orElse(""));
        }
        System.out.println("\n== Global Strings ==");
        for (Map.Entry<String, String> entry : globalStrings.entrySet()) {
            System.out.printf("%s: \"%s\"\n", entry.getKey(), entry.getValue());
        }
        System.out.println("\n== Text ==\n");
        HashSet<BasicBlock> visited = new HashSet<>();
        Queue<BasicBlock> queue = new LinkedList<>(); // BFS

        for (FuncInfo func : functions.values()) {
            outputFuncHelper(System.out, func, visited, queue);
        }
        // outputFuncHelper(ps, mainFunction, visited, queue);
    }

    private void outputFuncHelper(PrintStream ps, FuncInfo func, HashSet<BasicBlock> visited, Queue<BasicBlock> queue) {
        ps.printf("# Function %s: stack size = 0x%x\n", func.getName(), func.getStackSize());
        queue.offer(func.getBody());
        while (!queue.isEmpty()) {
            BasicBlock front = queue.poll();
            if (visited.contains(front)) {
                continue;
            }
            visited.add(front);
            ps.println(front.getLabel() + ":");
            Node node = front.getHead();
            while (Objects.nonNull(node) && node.hasNext()) {
                if (node instanceof Jump) {
                    BasicBlock target = ((Jump) node).getTarget();
                    queue.offer(target);
                } else if (node instanceof BranchIfElse) {
                    BasicBlock then = ((BranchIfElse) node).getThenTarget();
                    queue.offer(then);
                    BasicBlock elseBlk = ((BranchIfElse) node).getElseTarget();
                    queue.offer(elseBlk);
                }
                ps.println("    " + node);
                node = node.getNext();
            }
            ps.println();
        }
    }
}
