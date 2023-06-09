package back;

import back.store.Memory;
import back.store.RegisterFile;
import back.instruction.MipsInstruction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 存储所有目标代码的容器
 */
public class Mips {
    // 字符串常量从 .data 起始位置开始存储, 全局变量从 $gp 开始位置存储; 所有有地址的变量在目标代码中不保存标签名
    public static final int STRING_START_ADDRESS = 0x10000000; // .data 0x10000000 --> string constant
    public static final int DATA_START_ADDRESS = RegisterFile.GLOBAL_POINTER_INIT; // .data 0x10008000 --> $gp
    public static final int DATA_LIMIT_ADDRESS = 0x10040000; // heap base address

    // 字符串常量, 单独管理标签名以及内容: 从 .data 起始位置开始存
    private final Map<String, String> stringConstant = new HashMap<>();
    private final Map<String, Integer> stringConstantAddress = new HashMap<>();
    private int stringConstantSize = 0;

    // 初始内存布局（在 .data 中用伪指令填入）
    private final Memory initMem = new Memory();

    // 即将插入标签
    private String label = null;
    private String description = ""; // 即将插入的指令的说明

    private final MipsInstruction entry = MipsInstruction.nop();
    private final MipsInstruction tail = MipsInstruction.nop();

    public Mips() {
        tail.setPrev(entry);
        entry.setNext(tail);
    }

    // String Constant
    public void addStringConstant(String label, String content) {
        stringConstant.put(label, content);
        stringConstantAddress.put(label, stringConstantSize);
        initMem.putString(STRING_START_ADDRESS + stringConstantSize, content);
        // Replace escapes (e.g. LF)!
        String replaceEscape = content.replace("\\n", "X");
        stringConstantSize += replaceEscape.length() + 1; // terminate char '\0'
    }

    public int getStringAddress(String label) {
        assert stringConstantAddress.containsKey(label);
        return stringConstantAddress.get(label);
    }

    public String getString(String label) {
        return stringConstant.get(label);
    }

    public Memory getInitMem() {
        return initMem;
    }

    public MipsInstruction getFirstInstruction() {
        return (MipsInstruction) entry.getNext();
    }

    public void append(MipsInstruction follow) {
        if (Objects.nonNull(label)) {
            follow.setLabel(label);
            label = null;
        }
        /*if (!description.isEmpty()) {
            follow.setDescription(description);
            description = "";
        }*/
        MipsInstruction last = (MipsInstruction) tail.getPrev();
        last.setNext(follow);
        follow.setPrev(last);
        MipsInstruction tail = follow;
        while (tail.hasNext()) {
            tail = (MipsInstruction) tail.getNext();
        }
        tail.setNext(this.tail);
        this.tail.setPrev(tail);
    }

    public void append(MipsInstruction follow, String comment) {
        follow.setComment(comment);
        append(follow);
    }

    public void setLabel(String label) { // 下一条插入的指令将带有标签, 用该方法定义基本块头部
        this.label = label;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void output() {
        // String Constants
        System.out.printf(".data 0x%x # String Constants\n", STRING_START_ADDRESS);
        for (Map.Entry<String, String> entry : stringConstant.entrySet()) {
            System.out.printf(".asciiz \"%s\" # %s @ 0x%x\n", entry.getValue(), entry.getKey(), stringConstantAddress.get(entry.getKey()));
        }
        // Global Variables
        System.out.printf(".data 0x%x # Global\n", DATA_START_ADDRESS);
        System.out.print(".word ");
        int lastAddress = -4;
        for (int address : initMem.modifiedAddresses()) {
            assert address > lastAddress && address % 4 == 0;
            if (address - lastAddress > 4) {
                System.out.printf("\n.space %d\n", (address - lastAddress - 4));
                System.out.print(".word ");
            }
            System.out.printf("%d ", initMem.loadWord(address));
            lastAddress = address;
        }
        // Text
        System.out.println("\n.text");
        MipsInstruction instruction = getFirstInstruction();
        while (instruction.hasNext()) {
            System.out.println(instruction);
            instruction = (MipsInstruction) instruction.getNext();
        }
    }
}
