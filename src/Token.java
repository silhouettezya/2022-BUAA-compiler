import java.math.BigInteger;

public class Token {
    private String Type;
    private String content;
    private int lineNumber;

    public Token(String Type, String content, int lineNumber) {
        this.Type = Type;
        this.content = content;
        this.lineNumber = lineNumber;
    }

    public String getType() {
        return Type;
    }

    public String getContent() {
        return content;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
