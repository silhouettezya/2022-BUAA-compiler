package middle.middlecode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrintFormat extends Node {
    private String format;    // 格式字符串对应全局的标签
    private List<Operand> params;

    public PrintFormat(String format, List<Operand> params) {
        this.format = format;
        this.params = Collections.unmodifiableList(new ArrayList<>(params));
    }

    public String getFormat() {
        return format;
    }

    public List<Operand> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "PRINTF \"" + format + "\", [" + params.stream().map(Object::toString).reduce((s, s2) -> s + ", " + s2).orElse("") + "]";
    }
}
