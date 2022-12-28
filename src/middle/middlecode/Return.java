package middle.middlecode;

import java.util.Objects;

public class Return extends Node {
    private Operand value; // Nullable

    public Return() {
        this.value = null;
    }

    public Return(Operand value) {
        this.value = value;
    }

    public Operand getValue() {
        return value;
    }

    public boolean hasValue() {
        return Objects.nonNull(value);
    }

    @Override
    public String toString() {
        return "ret" + (hasValue() ? " " + value : "");
        //return "RETURN" + (hasValue() ? " " + value : "");
    }
}
