import java.math.BigInteger;

public class Token {
    public TKtype Type;
    public String content;
    public int lineNumber;

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

    public String toString() {
        return Type + " " + content;
    }
}
