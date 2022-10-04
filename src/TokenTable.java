import java.util.ArrayList;

public class TokenTable {
    private ArrayList<Token> tokenTable;
    private int pos;
    private Token curToken;

    public TokenTable(ArrayList<Token> tokenTable) {
        this.tokenTable = tokenTable;
        this.pos = 0;
    }

    public Token getCurToken() {
        if (pos >= 0 && pos < tokenTable.size()) {
            curToken = tokenTable.get(pos);
        } else {
            curToken = null;
        }
        pos++;
        return curToken;
    }

    public void retract() {
        pos--;
    }

    public Token readToken(int i) {
        if (pos + i >= 0 && pos + i < tokenTable.size()) {
            return tokenTable.get(pos + i);
        } else {
            return null;
        }
    }
}
