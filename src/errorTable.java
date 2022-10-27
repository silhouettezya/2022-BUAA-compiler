import java.util.ArrayList;
import java.util.Collections;

public class errorTable {
    private static final errorTable errorTable = new errorTable();
    private ArrayList<error> errors;

    public static errorTable getInstance() {
        return errorTable;
    }

    public void initial() {
        this.errors = new ArrayList<>();
    }

    public void addError(int line, char t) {
        errors.add(new error(t, line));
    }

    public void output() {
        Collections.sort(errors);
        for (error error : errors) {
            System.out.println(error);
        }
    }
}
