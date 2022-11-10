package error;

public class error implements Comparable<error>{
    /*public enum type {
        a,//wrong char, lexer
        b,//repeat ident
        c,//
        d,
        e,
        f,
        g,
        h,
        i,//lose ;
        j,//lose )
        k,//lose ]
        l,
        m
    }*/

    public char type;
    private final int line;

    public error(char t, int line) {
        type = t;
        this.line = line;
    }

    @Override
    public String toString() {
        return line + " " + type;
    }

    @Override
    public int compareTo(error o) {
        return Integer.compare(this.line, o.line);
    }
}
