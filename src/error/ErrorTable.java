package error;

import java.util.ArrayList;
import java.util.Collections;

public class ErrorTable {
    private static final ErrorTable errorTable = new ErrorTable();
    private ArrayList<error> errors;

    public static ErrorTable getInstance() {
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
