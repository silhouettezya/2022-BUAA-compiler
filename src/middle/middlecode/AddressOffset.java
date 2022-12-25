package middle.middlecode;

import middle.symboltable.Symbol;

/**
 * 寻址操作
 */
public class AddressOffset extends Node {
    private Symbol base; // pointer or array
    private Operand offset;
    private Symbol target; // pointer

    public AddressOffset(Symbol base, Operand offset, Symbol target) {
        this.base = base;
        this.offset = offset;
        this.target = target;
    }

    public Symbol getBase() {
        return base;
    }

    public Operand getOffset() {
        return offset;
    }

    public Symbol getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "OFFSET " + base + ", " + offset + ", " + target;
    }
}
