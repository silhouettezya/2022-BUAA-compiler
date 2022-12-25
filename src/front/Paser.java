package front;

import error.ErrorTable;

import java.util.ArrayList;
import java.util.HashSet;

public class Paser {
    private Token token;
    private TokenTable table;
    private PaserUnit compUnit;
    private PaserUnit curUnit;
    private HashSet<TKtype> stmtSet = new HashSet<TKtype>(){{
        add(TKtype.IDENFR);
        add(TKtype.SEMICN);
        add(TKtype.LPARENT);
        add(TKtype.INTCON);
        add(TKtype.LBRACE);
        add(TKtype.IFTK);
        add(TKtype.WHILETK);
        add(TKtype.BREAKTK);
        add(TKtype.CONTINUETK);
        add(TKtype.RETURNTK);
        add(TKtype.PRINTFTK);
        add(TKtype.PLUS);
        add(TKtype.MINU);
    }};

    public Paser(TokenTable tokenTable) {
        table = tokenTable;
        compUnit = CompUnit();
    }

    public PaserUnit getCompUnit() {
        return compUnit;
    }

    public PaserUnit CompUnit() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        token = table.getCurToken();
        while (curType() == TKtype.CONSTTK ||
                (curType() == TKtype.INTTK && getType(1) == TKtype.IDENFR
                && (getType(2) != TKtype.LPARENT))) {
            curUnit = Decl();
            units.add(curUnit);
        }

        while (curType() == TKtype.VOIDTK || (curType() == TKtype.INTTK && getType(1) == TKtype.IDENFR)) {
            curUnit = FuncDef();
            units.add(curUnit);
        }

        curUnit = MainFuncDef();
        units.add(curUnit);

        finPaser("CompUnit");
        return new PaserUnit("CompUnit", units);
    }

    public PaserUnit Decl() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        if (curType() == TKtype.CONSTTK) {
            curUnit = ConstDecl();
            units.add(curUnit);
        } else {
            curUnit = VarDecl();
            units.add(curUnit);
        }
        return new PaserUnit("Decl", units);
    }

    public PaserUnit ConstDecl() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "ConstDecl";
        if (curType() == TKtype.CONSTTK) {
            addEndUnit(units);
            if (curType() == TKtype.INTTK) {
                addEndUnit(units);
                curUnit = ConstDef();
                units.add(curUnit);
                while (curType() == TKtype.COMMA) {
                    addEndUnit(units);
                    curUnit = ConstDef();
                    units.add(curUnit);
                }
                if (curType() == TKtype.SEMICN) {
                    addEndUnit(units);
                } else {
                    SEMICNError(units);
                }
            }
        }
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit ConstDef() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "ConstDef";
        if (curType() == TKtype.IDENFR) {
            addEndUnit(units);
            int times = 0;
            while (curType() == TKtype.LBRACK && times < 2) {
                addEndUnit(units);
                curUnit = ConstExp();
                units.add(curUnit);
                if (curType() == TKtype.RBRACK) {
                    addEndUnit(units);
                } else {
                    RBRACKError(units);
                }
                times++;
            }
            if (curType() == TKtype.ASSIGN) {
                addEndUnit(units);
                curUnit = ConstInitVal();
                units.add(curUnit);
            }
        }
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit ConstInitVal() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "ConstInitVal";
        if (curType() == TKtype.LBRACE) {
            addEndUnit(units);
            if (curType() == TKtype.RBRACE) {
                addEndUnit(units);
            } else {
                curUnit = ConstInitVal();
                units.add(curUnit);
                while (curType() == TKtype.COMMA) {
                    addEndUnit(units);
                    curUnit = ConstInitVal();
                    units.add(curUnit);
                }
                if (curType() == TKtype.RBRACE) {
                    addEndUnit(units);
                }
            }
        } else {
            curUnit = ConstExp();
            units.add(curUnit);
        }
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit VarDecl() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "VarDecl";
        if (curType() == TKtype.INTTK) {
            addEndUnit(units);
            curUnit = VarDef();
            units.add(curUnit);
            while (curType() == TKtype.COMMA) {
                addEndUnit(units);
                curUnit = VarDef();
                units.add(curUnit);
            }
            if (curType() == TKtype.SEMICN) {
                addEndUnit(units);
            } else {
                SEMICNError(units);
            }
        }
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit VarDef() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "VarDef";
        if (curType() == TKtype.IDENFR) {
            addEndUnit(units);
            int times = 0;
            while (curType() == TKtype.LBRACK && times < 2) {
                addEndUnit(units);
                curUnit = ConstExp();
                units.add(curUnit);
                if (curType() == TKtype.RBRACK) {
                    addEndUnit(units);
                } else {
                    RBRACKError(units);
                }
                times++;
            }
            if (curType() == TKtype.ASSIGN) {
                addEndUnit(units);
                curUnit = InitVal();
                units.add(curUnit);
            }
        }
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit InitVal() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "InitVal";
        if (curType() == TKtype.LBRACE) {
            addEndUnit(units);
            if (curType() == TKtype.RBRACE) {
                addEndUnit(units);
            } else {
                curUnit = InitVal();
                units.add(curUnit);
                while (curType() == TKtype.COMMA) {
                    addEndUnit(units);
                    curUnit = InitVal();
                    units.add(curUnit);
                }
                if (curType() == TKtype.RBRACE) {
                    addEndUnit(units);
                }
            }
        } else {
            curUnit = Exp();
            units.add(curUnit);
        }
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit FuncDef() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "FuncDef";

        curUnit = FuncType();
        units.add(curUnit);
        if (curType() == TKtype.IDENFR) {
            addEndUnit(units);
            if (curType() == TKtype.LPARENT) {
                addEndUnit(units);
                if (curType() == TKtype.RPARENT) {
                    addEndUnit(units);
                } else if (curType() == TKtype.INTTK) {
                    curUnit = FuncFParams();
                    units.add(curUnit);
                    if (curType() == TKtype.RPARENT) {
                        addEndUnit(units);
                    } else {
                        RPARENTError(units);
                    }
                } else {
                    RPARENTError(units);
                }
                curUnit = Block();
                units.add(curUnit);
            }
        }

        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit MainFuncDef() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "MainFuncDef";
        if (curType() == TKtype.INTTK) {
            addEndUnit(units);
            if (curType() == TKtype.MAINTK) {
                addEndUnit(units);
                if (curType() == TKtype.LPARENT) {
                    addEndUnit(units);
                    if (curType() == TKtype.RPARENT) {
                        addEndUnit(units);
                    } else {
                        RPARENTError(units);
                    }
                    curUnit = Block();
                    units.add(curUnit);
                }
            }
        }
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit FuncType() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "FuncType";
        if (curType() == TKtype.VOIDTK) {
            addEndUnit(units);
        } else if (curType() == TKtype.INTTK) {
            addEndUnit(units);
        }
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit FuncFParams() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "FuncFParams";
        curUnit = FuncFParam();
        units.add(curUnit);
        while (curType() == TKtype.COMMA) {
            addEndUnit(units);
            curUnit = FuncFParam();
            units.add(curUnit);
        }
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit FuncFParam() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "FuncFParam";
        if (curType() == TKtype.INTTK) {
            addEndUnit(units);
            if (curType() == TKtype.IDENFR) {
                addEndUnit(units);
                if (curType() == TKtype.LBRACK) {
                    addEndUnit(units);
                    if (curType() == TKtype.RBRACK) {
                        addEndUnit(units);
                    } else {
                        RBRACKError(units);
                    }
                    if (curType() == TKtype.LBRACK) {
                        addEndUnit(units);
                        curUnit = ConstExp();
                        units.add(curUnit);
                        if (curType() == TKtype.RBRACK) {
                            addEndUnit(units);
                        } else {
                            RBRACKError(units);
                        }
                    }
                }
            }
        }
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit Block() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "Block";
        if (curType() == TKtype.LBRACE) {
            addEndUnit(units);
            while (stmtSet.contains(curType()) || curType() == TKtype.INTTK || curType() == TKtype.CONSTTK) {
                curUnit = BlockItem();
                units.add(curUnit);
            }
            if (curType() == TKtype.RBRACE) {
                addEndUnit(units);
            }
        }
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit BlockItem() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "BlockItem";
        if (curType() == TKtype.INTTK || curType() == TKtype.CONSTTK) {
            curUnit = Decl();
            units.add(curUnit);
        } else {
            curUnit = Stmt();
            units.add(curUnit);
        }
        return new PaserUnit(tp, units);
    }

    public PaserUnit Stmt() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "Stmt";
        String stmtType = "Wrong";
        if (curType() == TKtype.LBRACE) {
            curUnit = Block();
            units.add(curUnit);
            stmtType = "Block";
        } else if (curType() == TKtype.IFTK) {
            addEndUnit(units);
            if (curType() == TKtype.LPARENT) {
                addEndUnit(units);
                curUnit = Cond();
                units.add(curUnit);
                if (curType() == TKtype.RPARENT) {
                    addEndUnit(units);
                } else {
                    RPARENTError(units);
                }
                curUnit = Stmt();
                units.add(curUnit);
                if (curType() == TKtype.ELSETK) {
                    addEndUnit(units);
                    curUnit = Stmt();
                    units.add(curUnit);
                }
            }
            stmtType = "If";
        } else if (curType() == TKtype.WHILETK) {
            addEndUnit(units);
            if (curType() == TKtype.LPARENT) {
                addEndUnit(units);
                curUnit = Cond();
                if (curType() == TKtype.RPARENT) {
                    addEndUnit(units);
                } else {
                    RPARENTError(units);
                }
                curUnit = Stmt();
                units.add(curUnit);
            }
            stmtType = "While";
        } else if (curType() == TKtype.BREAKTK) {
            addEndUnit(units);
            if (curType() == TKtype.SEMICN) {
                addEndUnit(units);
            } else {
                SEMICNError(units);
            }
            stmtType = "Break";
        } else if (curType() == TKtype.CONTINUETK) {
            addEndUnit(units);
            if (curType() == TKtype.SEMICN) {
                addEndUnit(units);
            } else {
                SEMICNError(units);
            }
            stmtType = "Continue";
        } else if (curType() == TKtype.RETURNTK) {
            addEndUnit(units);
            if (curType() == TKtype.SEMICN) {
                addEndUnit(units);
            } else if (curType() == TKtype.IDENFR || curType() == TKtype.PLUS || curType() == TKtype.NOT ||
                    curType() == TKtype.MINU || curType() == TKtype.LPARENT || curType() == TKtype.INTCON) {
                curUnit = Exp();
                units.add(curUnit);
                if (curType() == TKtype.SEMICN) {
                    addEndUnit(units);
                } else {
                    SEMICNError(units);
                }
            } else {
                SEMICNError(units);
            }
            stmtType = "Return";
        } else if (curType() == TKtype.PRINTFTK) {
            addEndUnit(units);
            if (curType() == TKtype.LPARENT) {
                addEndUnit(units);
                if (curType() == TKtype.STRCON) {
                    addEndUnit(units);
                    while (curType() == TKtype.COMMA) {
                        addEndUnit(units);
                        curUnit = Exp();
                        units.add(curUnit);
                    }
                    if (curType() == TKtype.RPARENT) {
                        addEndUnit(units);
                    } else {
                        RPARENTError(units);
                    }
                    if (curType() == TKtype.SEMICN) {
                        addEndUnit(units);
                    } else {
                        SEMICNError(units);
                    }
                }
            }
            stmtType = "Printf";
        } else if (curType() == TKtype.IDENFR) {
            int i = 1;
            int line = token.lineNumber;
            boolean isExp = true;
            while (getType(i) != TKtype.SEMICN && getLineNumber(i) == line) {
                if (getType(i) == TKtype.ASSIGN) {
                    isExp = false;
                    break;
                }
                i++;
            }
            if (!isExp) {
                curUnit = LVal();
                units.add(curUnit);
                if (curType() == TKtype.ASSIGN) {
                    addEndUnit(units);
                    if (curType() == TKtype.GETINTTK) {
                        addEndUnit(units);
                        if (curType() == TKtype.LPARENT) {
                            addEndUnit(units);
                            if (curType() == TKtype.RPARENT) {
                                addEndUnit(units);
                            } else {
                                RPARENTError(units);
                            }
                        }
                        stmtType = "Getint";
                    } else {
                        curUnit = Exp();
                        units.add(curUnit);
                        stmtType = "Assign";
                    }
                    if (curType() == TKtype.SEMICN) {
                        addEndUnit(units);
                    } else {
                        SEMICNError(units);
                    }
                }
            } else {
                curUnit = Exp();
                units.add(curUnit);
                if (curType() == TKtype.SEMICN) {
                    addEndUnit(units);
                } else {
                    SEMICNError(units);
                }
                stmtType = "Exp";
            }
        } else if (curType() == TKtype.SEMICN) {
            addEndUnit(units);
            stmtType = "Null";
        } else {
            curUnit = Exp();
            units.add(curUnit);
            if (curType() == TKtype.SEMICN) {
                addEndUnit(units);
            } else {
                SEMICNError(units);
            }
            stmtType = "Exp";
        }
        finPaser(tp);
        return new PaserUnit(tp, units, stmtType);
    }

    public PaserUnit Exp() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "Exp";
        curUnit = AddExp();
        units.add(curUnit);
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit Cond() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "Cond";
        curUnit = LOrExp();
        units.add(curUnit);
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit LVal() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "LVal";
        if (curType() == TKtype.IDENFR) {
            addEndUnit(units);
            int times = 0;
            while (curType() == TKtype.LBRACK && times < 2) {
                addEndUnit(units);
                curUnit = Exp();
                units.add(curUnit);
                if (curType() == TKtype.RBRACK) {
                    addEndUnit(units);
                } else {
                    RBRACKError(units);
                }
                times++;
            }
        }
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit PrimaryExp() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "PrimaryExp";
        String Pritp;
        if (curType() == TKtype.LPARENT) {
            Pritp = "Exp";
            addEndUnit(units);
            curUnit = Exp();
            units.add(curUnit);
            if (curType() == TKtype.RPARENT) {
                addEndUnit(units);
            } else {
                RPARENTError(units);
            }
        } else if (curType() == TKtype.INTCON) {
            Pritp = "Number";
            curUnit = Number();
            units.add(curUnit);
        } else {
            Pritp = "LVal";
            curUnit = LVal();
            units.add(curUnit);
        }
        finPaser(tp);
        PaserUnit unit = new PaserUnit(tp, units);
        unit.setPrimaryExpType(Pritp);
        return unit;
    }

    public PaserUnit Number() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "Number";
        if (curType() == TKtype.INTCON) {
            addEndUnit(units);
        }
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit UnaryExp() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "UnaryExp";
        if (curType() == TKtype.IDENFR && getType(1) == TKtype.LPARENT) {
            addEndUnit(units);
            addEndUnit(units);
            if (curType() == TKtype.RPARENT) {
                addEndUnit(units);
            } else if (curType() == TKtype.IDENFR || curType() == TKtype.PLUS || curType() == TKtype.MINU
                || curType() == TKtype.LPARENT || curType() == TKtype.INTCON) {
                curUnit = FuncRParams();
                units.add(curUnit);
                if (curType() == TKtype.RPARENT) {
                    addEndUnit(units);
                } else {
                    RPARENTError(units);
                }
            } else {
                RPARENTError(units);
            }
        } else if (curType() == TKtype.PLUS || curType() == TKtype.MINU || curType() == TKtype.NOT) {
            curUnit = UnaryOp();
            units.add(curUnit);
            curUnit = UnaryExp();
            units.add(curUnit);
        } else {
            curUnit = PrimaryExp();
            units.add(curUnit);
        }
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit UnaryOp() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "UnaryOp";
        if (curType() == TKtype.PLUS || curType() == TKtype.MINU || curType() == TKtype.NOT) {
            addEndUnit(units);
        }
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit FuncRParams() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "FuncRParams";
        curUnit = Exp();
        units.add(curUnit);
        while (curType() == TKtype.COMMA) {
            addEndUnit(units);
            curUnit = Exp();
            units.add(curUnit);
        }
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    public PaserUnit  MulExp() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "MulExp";
        curUnit = UnaryExp();
        units.add(curUnit);
        finPaser(tp);
        while (curType() == TKtype.MULT || curType() == TKtype.DIV || curType() == TKtype.MOD) {
            addEndUnit(units);
            curUnit = UnaryExp();
            units.add(curUnit);
            finPaser(tp);
        }

        return new PaserUnit(tp, units);
    }

    public PaserUnit AddExp() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "AddExp";

        curUnit = MulExp();
        units.add(curUnit);
        finPaser(tp);
        while (curType() == TKtype.PLUS || curType() == TKtype.MINU) {
            addEndUnit(units);
            curUnit = MulExp();
            units.add(curUnit);
            finPaser(tp);
        }

        return new PaserUnit(tp, units);
    }

    public PaserUnit RelExp() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "RelExp";

        curUnit = AddExp();
        units.add(curUnit);
        finPaser(tp);
        while (curType() == TKtype.LSS || curType() == TKtype.LEQ ||
                curType() == TKtype.GRE || curType() == TKtype.GEQ) {
            addEndUnit(units);
            curUnit = AddExp();
            units.add(curUnit);
            finPaser(tp);
        }

        return new PaserUnit(tp, units);
    }

    public PaserUnit EqExp() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "EqExp";

        curUnit = RelExp();
        units.add(curUnit);
        finPaser(tp);
        while (curType() == TKtype.EQL || curType() == TKtype.NEQ) {
            addEndUnit(units);
            curUnit = RelExp();
            units.add(curUnit);
            finPaser(tp);
        }

        return new PaserUnit(tp, units);
    }

    public PaserUnit LAndExp() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "LAndExp";

        curUnit = EqExp();
        units.add(curUnit);
        finPaser(tp);
        while (curType() == TKtype.AND) {
            addEndUnit(units);
            curUnit = EqExp();
            units.add(curUnit);
            finPaser(tp);
        }

        return new PaserUnit(tp, units);
    }

    public PaserUnit LOrExp() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "LOrExp";

        curUnit = LAndExp();
        units.add(curUnit);
        finPaser(tp);
        while (curType() == TKtype.OR) {
            addEndUnit(units);
            curUnit = LAndExp();
            units.add(curUnit);
            finPaser(tp);
        }

        return new PaserUnit(tp, units);
    }

    public PaserUnit ConstExp() {
        ArrayList<PaserUnit> units = new ArrayList<>();
        String tp = "ConstExp";
        curUnit = AddExp();
        units.add(curUnit);
        finPaser(tp);
        return new PaserUnit(tp, units);
    }

    private void addEndUnit(ArrayList<PaserUnit> units) {
        // System.out.println(token);
        units.add(new PaserUnit(token));
        token = table.getCurToken();
    }

    private void finPaser(String Type) {
        // System.out.println("<" + Type + ">");
    }

    public TKtype curType() {
        if (token != null) {
            return token.Type;
        } else {
            return null;
        }
    }

    public TKtype getType(int i) {
        if (table.readToken(i) != null) {
            return table.readToken(i).Type;
        } else {
            return null;
        }
    }

    public int getLineNumber(int i) {
        if (table.readToken(i) != null) {
            return table.readToken(i).lineNumber;
        } else {
            return -1;
        }
    }

    public void SEMICNError(ArrayList<PaserUnit> units) {
        int lineNumber = units.get(units.size() - 1).getLastLineNumber();
        ErrorTable.getInstance().addError(lineNumber, 'i');
        units.add(new PaserUnit(new Token(TKtype.SEMICN, ";", lineNumber)));
    }

    public void RPARENTError(ArrayList<PaserUnit> units) {
        int lineNumber = units.get(units.size() - 1).getLastLineNumber();
        ErrorTable.getInstance().addError(lineNumber, 'j');
        units.add(new PaserUnit(new Token(TKtype.RPARENT, ")", lineNumber)));
    }

    public void RBRACKError(ArrayList<PaserUnit> units) {
        int lineNumber = units.get(units.size() - 1).getLastLineNumber();
        ErrorTable.getInstance().addError(lineNumber, 'k');
        units.add(new PaserUnit(new Token(TKtype.RBRACK, "]", lineNumber)));
    }
}
