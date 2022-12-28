package middle.middlecode;

import middle.symboltable.*;

import java.util.HashMap;

public class BinaryOp extends Node {
    public enum Op {
        ADD,
        SUB,
        MUL,
        DIV,
        MOD,
        GT,
        GE,
        LT,
        LE,
        EQ,
        NE,
        ANDL,   // logical
        ORL,    // logical
        AND,    // bitwise
        OR,     // bitwise
        XOR,    // bitwise
        SLL,
        SRA,
        MOVN,   // MOV src1 TO dst IF src2
        MOVZ,   // MOV src1 TO dst IF !src2
        MULHI,  // HI part of MUL
    }

    HashMap<Op, String> map = new HashMap<Op, String>() {
        {
            put(Op.ADD, " + ");
            put(Op.SUB, " - ");
            put(Op.MUL, " * ");
            put(Op.DIV, " / ");
            put(Op.MOD, " % ");
            put(Op.GT, " > ");
            put(Op.GE, " >= ");
            put(Op.LT, " < ");
            put(Op.LE, " <= ");
            put(Op.EQ, " == ");
            put(Op.NE, " != ");
            put(Op.ANDL, " && ");
            put(Op.ORL, " || ");
            put(Op.AND, " & ");
            put(Op.OR, " | ");
            put(Op.XOR, " ^ ");
            put(Op.SLL, " << ");
            put(Op.SRA, " >> ");
            put(Op.MULHI, " * ");
        }
    };


    private Op op;
    private Operand src2;
    private Operand src1;
    private Symbol dst;

    public BinaryOp(Op op, Operand src1, Operand src2, Symbol dst) {
        this.op = op;
        this.src1 = src1;
        this.src2 = src2;
        this.dst = dst;
    }

    public Op getOp() {
        return op;
    }

    public Operand getSrc1() {
        return src1;
    }

    public Operand getSrc2() {
        return src2;
    }

    public Symbol getDst() {
        return dst;
    }

    @Override
    public String toString() {
        if (op != Op.MOVN && op != Op.MOVZ) {
            return dst + " = " + src1 + map.get(op) + src2;
        }
        return op.name() + " " + src1 + ", " + src2 + ", " + dst;
    }
}
