import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

public class Lexer {
    private ArrayList<String> lines;
    private String line;
    private int lineNumber;
    private int pos;
    private char curc;
    private StringBuilder s;
    private String content;
    private TKtype Type;
    private ArrayList<Token> tokenTable = new ArrayList<>();
    private HashMap<String, TKtype> content2Type = new HashMap<String, TKtype>(){
        {
            put("main", TKtype.MAINTK);
            put("const", TKtype.CONSTTK);
            put("int", TKtype.INTTK);
            put("break", TKtype.BREAKTK);
            put("continue", TKtype.CONTINUETK);
            put("if", TKtype.IFTK);
            put("else", TKtype.ELSETK);
            put("while", TKtype.WHILETK);
            put("getint", TKtype.GETINTTK);
            put("printf", TKtype.PRINTFTK);
            put("return", TKtype.RETURNTK);
            put("void", TKtype.VOIDTK);
            put("+", TKtype.PLUS);
            put("-", TKtype.MINU);
            put("*", TKtype.MULT);
            put("/", TKtype.DIV);
            put("%", TKtype.MOD);
            put(";", TKtype.SEMICN);
            put(",", TKtype.COMMA);
            put("(", TKtype.LPARENT);
            put(")", TKtype.RPARENT);
            put("[", TKtype.LBRACK);
            put("]", TKtype.RBRACK);
            put("{", TKtype.LBRACE);
            put("}", TKtype.RBRACE);
        }
    };

    public Lexer(ArrayList<String> lines) {
        this.lines = lines;
        getToken();
    }

    public void getToken() {
        for (int i = 0; i < lines.size(); i++) {
            lineNumber = i + 1;
            line = lines.get(i);
            pos = 0;
            while (pos < line.length()) {
                clearToken();
                getCurc();
                while (curc == ' ' || curc == '\t') getCurc();
                if (isLetter()) {
                    while (isLetter() || isDigit()) {
                        catToken();
                        getCurc();//将字符拼接成字符串
                    }
                    retract();
                    buildIdent();
                } else if (isDigit()) {
                    while (isDigit()) {
                        catToken();
                        getCurc();
                    }
                    retract();
                    content = s.toString();
                    Type = TKtype.INTCON;
                } else if (curc == '!') {
                    getCurc();
                    if (curc == '=') {
                        content = "!=";
                        Type = TKtype.NEQ;
                    } else {
                        retract();
                        content = "!";
                        Type = TKtype.NOT;
                    }
                } else if (curc == '&') {
                    getCurc();
                    if (curc == '&') {
                        content = "&&";
                        Type = TKtype.AND;
                    } else {
                        retract();
                        //TODO && error
                    }
                } else if (curc == '|') {
                    getCurc();
                    if (curc == '|') {
                        content = "||";
                        Type = TKtype.OR;
                    } else {
                        retract();
                        //TODO || error
                    }
                } else if (curc == '<') {
                    getCurc();
                    if (curc == '=') {
                        content = "<=";
                        Type = TKtype.LEQ;
                    } else {
                        retract();
                        content = "<";
                        Type = TKtype.LSS;
                    }
                } else if (curc == '>') {
                    getCurc();
                    if (curc == '=') {
                        content = ">=";
                        Type = TKtype.GEQ;
                    } else {
                        retract();
                        content = ">";
                        Type = TKtype.GRE;
                    }
                } else if (curc == '=') {
                    getCurc();
                    if (curc == '=') {
                        content = "==";
                        Type = TKtype.EQL;
                    } else {
                        retract();
                        content = "=";
                        Type = TKtype.ASSIGN;
                    }
                } else if (curc == '"') {
                    catToken();
                    getCurc();
                    while (curc != '"'  && curc != '\n') {
                        catToken();
                        getCurc();
                    }
                    catToken();
                    //TODO FormatString error
                    content = s.toString();
                    Type = TKtype.STRCON;
                } else {
                    content = Character.toString(curc);
                    if (content2Type.containsKey(content)) {
                        Type = content2Type.get(content);
                    }
                }
                putToken();
            }
        }
    }

    public void getCurc() {
        if (pos >= line.length()) {
            curc = '\n';
        } else {
            curc = line.charAt(pos);
        }
        pos++;
    }

    public void retract() {
        pos--;
    }

    public void catToken() {
        s.append(curc);
    }

    public void buildIdent() {
        content = s.toString();
        if (content2Type.containsKey(content)) {
            Type = content2Type.get(content);
        } else {
            Type = TKtype.IDENFR;
        }
    }

    public void clearToken() {
        s = new StringBuilder();
        Type = null;
        content = null;
    }

    public void putToken() {
        if (Type != null) {
            Token word = new Token(Type, content, lineNumber);
            tokenTable.add(word);
        }
    }

    public boolean isLetter() {
        return (curc >= 'a' && curc <= 'z') || (curc >= 'A' && curc <= 'Z') || curc == '_';
    }

    public boolean isDigit() {
        return curc >= '0' && curc <= '9';
    }

    public void output() {
        for (Token token : tokenTable) {
            System.out.println(token.getType() + " " + token.getContent());
        }
    }

    public ArrayList<Token> getTokenTable() {
        return tokenTable;
    }
}
