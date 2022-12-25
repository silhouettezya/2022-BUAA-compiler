package middle.middlecode;

import middle.symboltable.Symbol;

public class Input extends Node {
    private Symbol dst;

    public Input(Symbol dst) {
        this.dst = dst;
    }

    public Symbol getDst() {
        return dst;
    }

    @Override
    public String toString() {
        return "INPUT " + dst;
    }
}
