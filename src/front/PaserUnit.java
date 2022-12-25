package front;

import java.util.ArrayList;

public class PaserUnit {
    private boolean isEnd; // false is not end, true is end
    private Token token;
    private String Type;
    private ArrayList<PaserUnit> units;
    private String StmtType;
    private String PrimaryExpType;

    public PaserUnit(String Type, ArrayList<PaserUnit> units) {
        this.isEnd = false;
        this.Type = Type;
        this.units = units;
    }

    public PaserUnit(Token token) {
        this.isEnd = true;
        this.token = token;
    }

    public PaserUnit(String Type, ArrayList<PaserUnit> units, String StmtType) {
        this.isEnd = false;
        this.Type = Type;
        this.units = units;
        this.StmtType = StmtType;
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

    public String getFormatStringInner() {
        return token.content.substring(1, (token.content.length()) -1);
    }

    public String getStmtType() {
        return StmtType;
    }

    public void setPrimaryExpType(String type) {
        this.PrimaryExpType = type;
    }

    public String getPrimaryExpType() {
        return PrimaryExpType;
    }

    public String getName() {
        return token.content;
    }

    public int getNum() {
        return token.getNum();
    }

    public PaserUnit getBaseUnaryExp() {
        if (units.get(0).getType().equals("UnaryOp")) {
            return units.get(1).getBaseUnaryExp();
        } else {
            PaserUnit paserUnit = this;
            return paserUnit;
        }
    }

    public ArrayList<PaserUnit> getUnaryOp() {
        ArrayList<PaserUnit> ops = new ArrayList<>();
        PaserUnit unit = this;
        while (unit.units.get(0).getType().equals("UnaryOp")) {
            ops.add(unit.units.get(0).units.get(0));
            unit = units.get(1);
        }
        return ops;
    }
}
