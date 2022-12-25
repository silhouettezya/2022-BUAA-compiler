package back.store;

import java.util.*;
import java.util.stream.Collectors;

public class Memory {
    private static final boolean ENABLE_TRACE = false;

    private TreeMap<Integer, Integer> memory = new TreeMap<>(); // <Address-by-word, Word>, not store in this map if not modified
    private HashMap<Integer, String> stringConst = new HashMap<>();

    public Memory() {
    }

    // address 均以字节为单位，与 MIPS 指令相同
    public int loadWord(int address) {
        int alignedAddress = address - (address & 0x3);
        return memory.getOrDefault(alignedAddress, 0);
    }

    public void storeWord(int address, int value) {
        int alignedAddress = address - (address & 0x3);
        if (ENABLE_TRACE) {
            System.err.printf("*%08x <= %08x", alignedAddress, value);
        }
        if (value == 0) {
            memory.remove(alignedAddress);
        } else {
            memory.put(alignedAddress, value);
        }
    }

    public void putString(int address, String string) {
        stringConst.put(address, string);
    }

    public List<Integer> modifiedAddresses() {
        return Collections.unmodifiableList(memory.keySet().stream().sorted(Integer::compare).collect(Collectors.toList()));
    }
}
