package middle.middlecode;

import middle.symboltable.Symbol;

public class UnaryOp extends Node {
    public enum Op {
        MOV,    // INT to INT
        NEG,    // INT to INT
        NOT,    // INT to INT
        CLO,    // INT to INT
        CLZ,    // INT to INT
        ABS,    // INT to INT
    }

    private Op op;
    private Operand src;
    private Symbol dst;

    public UnaryOp(Op op, Operand src, Symbol dst) {
        this.op = op;
        this.src = src;
        this.dst = dst;
    }

    public Op getOp() {
        return op;
    }

    public Operand getSrc() {
        return src;
    }

    public Symbol getDst() {
        return dst;
    }

    @Override
    public String toString() {
        return op.name() + " " + src + ", " + dst;
    }
}
