package middle.middlecode;

import middle.symboltable.Symbol;

public class PointerOp extends Node {
    public enum Op {
        LOAD,
        STORE
    }

    private Op op;
    private Symbol address;
    private Symbol dst;
    private Operand src;

    public PointerOp(Op op, Symbol address, Operand another) {
        this.op = op;
        this.address = address;

        if (op.equals(Op.LOAD)) {
            assert another instanceof Symbol;
            this.dst = (Symbol) another;
            this.src = null;
        } else {
            assert op.equals(Op.STORE);
            this.dst = null;
            this.src = another;
        }
    }

    public Op getOp() {
        return op;
    }

    public Symbol getAddress() {
        return address;
    }

    public Symbol getDst() {
        // op.equals(Op.LOAD);
        return dst;
    }

    public Operand getSrc() {
        // op.equals(Op.STORE);
        return src;
    }

    @Override
    public String toString() {
        return op.name() + " " + address + ", " + (op.equals(Op.LOAD) ? getDst() : getSrc());
    }
}
