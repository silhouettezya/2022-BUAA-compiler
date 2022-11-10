package front;

import java.util.ArrayList;

public class PaserUnit {
    private boolean isEnd; // false is not end, true is end
    private Token token;
    private String Type;
    private ArrayList<PaserUnit> units;

    public PaserUnit(String Type, ArrayList<PaserUnit> units) {
        this.isEnd = false;
        this.Type = Type;
        this.units = units;
    }

    public PaserUnit(Token token) {
        this.isEnd = true;
        this.token = token;
    }

    public int getLastLineNumber() {
        if (isEnd) {
            return token.getLineNumber();
        } else {
            return units.get(units.size() - 1).getLastLineNumber();
        }
    }

    public ArrayList<PaserUnit> getUnits() {
        return units;
    }

    public String getType() {
        if (!isEnd) {
            return Type;
        } else if (token.getType() == TKtype.IDENFR) {
            return "Ident";
        } else if (token.getType() == TKtype.STRCON) {
            return "Strcon";
        } else if (token.getType() == TKtype.INTCON) {
            return "Intcon";
        } else {
            return token.content;
        }
    }

    public String getName() {
        return token.content;
    }
}
