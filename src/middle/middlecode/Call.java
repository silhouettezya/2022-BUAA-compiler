package middle.middlecode;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import middle.symboltable.Symbol;
import middle.symboltable.FuncInfo;

public class Call extends Node {
    private FuncInfo function;
    private List<Operand> params;
    private Symbol ret;

    public Call(FuncInfo function, List<Operand> params) {
        this.function = function;
        this.params = Collections.unmodifiableList(params);
        this.ret = null;
    }

    public Call(FuncInfo function, List<Operand> params, Symbol ret) {
        this.function = function;
        this.params = Collections.unmodifiableList(params);
        this.ret = ret;
    }

    public List<Operand> getParams() {
        return params;
    }

    public FuncInfo getFunction() {
        return function;
    }

    public Symbol getRet() {
        return ret;
    }

    public boolean hasRet() {
        return Objects.nonNull(ret);
    }

    @Override
    public String toString() {
        return params.stream().map(Object::toString).reduce((s, s2) -> s + "push " + s2 + "\n").orElse("")
                + "    call " + function.getLabelName() + "\n"
                + (Objects.nonNull(ret) ? "    " + ret + " = RET" : "");
        /*return "CALL " + function.getLabelName() + ", ["
                + params.stream().map(Object::toString).reduce((s, s2) -> s + ", " + s2 + "\n").orElse("") + "]"
                + (Objects.nonNull(ret) ? " -> " + ret : "");*/
    }
}
