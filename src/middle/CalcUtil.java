package middle;

import error.ErrorTable;
import middle.symboltable.Symbol;
import middle.symboltable.SymbolTable;
import front.*;

import java.util.ArrayList;

public class CalcUtil {

    private SymbolTable symbolTable;

    public CalcUtil(SymbolTable symTable) {
        this.symbolTable = symTable;
    }

    private void UndefinedError(PaserUnit ident) {
        int lineNumber = ident.getLastLineNumber();
        ErrorTable.getInstance().addError(lineNumber, 'c');
    }

    public int calcExp(PaserUnit exp) {
        PaserUnit addExp = exp.getUnits().get(0);
        return calcAddExp(addExp);
    }

    public int calcAddExp(PaserUnit addExp) {
        ArrayList<PaserUnit> units = addExp.getUnits();
        int result = calcMulExp(units.get(0));
        for (int i = 1; i < units.size(); i = i + 2) {
            PaserUnit op = units.get(i);
            PaserUnit mulExp = units.get(i + 1);
            if (op.getType().equals("+")) {
                result = result + calcMulExp(mulExp);
            } else if (op.getType().equals("-")) {
                result = result - calcMulExp(mulExp);
            }
        }
        return result;
    }

    public int calcMulExp(PaserUnit mulExp) {
        ArrayList<PaserUnit> units = mulExp.getUnits();
        int result = calcUnaryExp(units.get(0));
        for (int i = 1; i < units.size(); i = i + 2) {
            PaserUnit op = units.get(i);
            PaserUnit unaryExp = units.get(i + 1);
            if (op.getType().equals("*")) {
                result = result * calcUnaryExp(unaryExp);
            } else if (op.getType().equals("/")) {
                result = result / calcUnaryExp(unaryExp);
            } else if (op.getType().equals("%")) {
                result = result % calcUnaryExp(unaryExp);
            }
        }
        return result;
    }

    public int calcUnaryExp(PaserUnit unaryExp) {
        PaserUnit baseExp = unaryExp.getBaseUnaryExp();
        ArrayList<PaserUnit> ops = unaryExp.getUnaryOp();
        int result = 0;
        if (baseExp.getUnits().get(0).getType().equals("PrimaryExp")) {
            PaserUnit primaryExp = baseExp.getUnits().get(0);
            if (primaryExp.getPrimaryExpType().equals("Exp")) {
                result = calcExp(primaryExp.getUnits().get(1));
            } else if (primaryExp.getPrimaryExpType().equals("LVal")) {
                result = calcLVal(primaryExp.getUnits().get(0));
            } else if (primaryExp.getPrimaryExpType().equals("Number")) {
                result = calcNumber(primaryExp.getUnits().get(0));
            }
        }
        for (PaserUnit op : ops) {
            if (op.getType().equals("-")) {
                result = -result;
            } else if (op.getType().equals("!")) {
                result = (result == 0) ? 1 : 0;
            }
        }
        return result;
    }

    public int calcLVal(PaserUnit lVal) {
        ArrayList<PaserUnit> units = lVal.getUnits();
        PaserUnit ident = units.get(0);
        String name = ident.getName();
        if (!symbolTable.contains(name, true)) {
            UndefinedError(ident);
            return 0;
        }
        Symbol symbol = symbolTable.getSymbol(name);

        if (symbol.getType().equals(Symbol.Type.ITEM)) {
            return symbol.getInitValue();
        } else if (symbol.getType().equals(Symbol.Type.ARRAY)) {
            ArrayList<Integer> indexes = new ArrayList<>();
            for (PaserUnit unit : units) {
                if (unit.getType().equals("Exp")) {
                    indexes.add(calcExp(unit));
                }
            }
            int base = 1;
            int offset = 0;
            for (int i = indexes.size() - 1; i >= 0; i--) {
                offset += indexes.get(i) * base;
                if (i > 0) {
                    base = base * symbol.getDimSize().get(i);
                }
            }
            return symbol.getInitArray().get(offset);
        } else {
            return 0;
        }
    }

    public int calcNumber(PaserUnit number) {
        return number.getUnits().get(0).getNum();
    }
}

