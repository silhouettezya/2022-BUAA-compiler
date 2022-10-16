import java.util.ArrayList;

public class TokenTable {
    private ArrayList<Token> tokenTable;
    private int pos;
    private Token curToken;

    public TokenTable(ArrayList<Token> tokenTable) {
        this.tokenTable = tokenTable;
        this.pos = -1;
    }

    // 为了和readToken统一，curToken保存的是当前pos的Token
    public Token getCurToken() {
        if (pos >= -1 && pos < tokenTable.size() - 1) {
            curToken = tokenTable.get(++pos);
        } else {
            curToken = null;
        }
        return curToken;
    }

    public void retract() {
        pos--;
    }

    // i表示距离当前pos为i的位置
    public Token readToken(int i) {
        if (pos + i >= 0 && pos + i < tokenTable.size()) {
            return tokenTable.get(pos + i);
        } else {
            return null;
        }
    }
}
