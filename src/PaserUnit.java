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
}
