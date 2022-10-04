import java.math.BigInteger;

public class Token {
    private TKtype Type;
    private String content;
    private int lineNumber;

    public Token(TKtype Type, String content, int lineNumber) {
        this.Type = Type;
        this.content = content;
        this.lineNumber = lineNumber;
    }

    public TKtype getType() {
        return Type;
    }

    public String getContent() {
        return content;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
