package middle.middlecode;

import middle.symboltable.Symbol;

import java.util.HashMap;

public class UnaryOp extends Node {
    public enum Op {
        MOV,    // INT to INT
        NEG,    // INT to INT
        NOT,    // INT to INT
        CLO,    // INT to INT
        CLZ,    // INT to INT
        ABS,    // INT to INT
    }

    HashMap<UnaryOp.Op, String> map = new HashMap<UnaryOp.Op, String>() {
        {
            put(Op.MOV, "");
            put(Op.NEG, "-");
            put(Op.NOT, "!");
        }
    };

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
        if (map.containsKey(op)) {
            return dst + " = " + map.get(op) + src;
        }
        return op.name() + " " + src + ", " + dst;
    }
}
