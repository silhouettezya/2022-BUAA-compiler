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
    private String Type;
    private ArrayList<Token> tokenTable = new ArrayList<>();
    private HashMap<String, String> content2Type = new HashMap<String, String>(){
        {
            put("main", "MAINTK");
            put("const", "CONSTTK");
            put("int", "INTTK");
            put("break", "BREAKTK");
            put("continue", "CONTINUETK");
            put("if", "IFTK");
            put("else", "ELSETK");
            put("while", "WHILETK");
            put("getint", "GETINTTK");
            put("printf", "PRINTFTK");
            put("return", "RETURNTK");
            put("void", "VOIDTK");
            put("+", "PLUS");
            put("-", "MINU");
            put("*", "MULT");
            put("/", "DIV");
            put("%", "MOD");
            put(";", "SEMICN");
            put(",", "COMMA");
            put("(", "LPARENT");
            put(")", "RPARENT");
            put("[", "LBRACK");
            put("]", "RBRACK");
            put("{", "LBRACE");
            put("}", "RBRACE");
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
                // 这里仅处理空格与制表符，若有换行则标志到达了句子末尾，直接循环结束即可
                while (curc == ' ' || curc == '\t') getCurc();
                if (isLetter()) {
                    while (isLetter() || isDigit()) { // 处理标识符与保留字
                        catToken();
                        getCurc(); //将字符拼接成字符串
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
                    Type = "INTCON";
                } else if (curc == '!') { // 处理单双符号
                    getCurc();
                    if (curc == '=') {
                        content = "!=";
                        Type = "NEQ";
                    } else {
                        retract();
                        content = "!";
                        Type = "NOT";
                    }
                } else if (curc == '&') {
                    getCurc();
                    if (curc == '&') {
                        content = "&&";
                        Type = "AND";
                    } else {
                        retract();
                        //TODO && error
                    }
                } else if (curc == '|') {
                    getCurc();
                    if (curc == '|') {
                        content = "||";
                        Type = "OR";
                    } else {
                        retract();
                        //TODO || error
                    }
                } else if (curc == '<') {
                    getCurc();
                    if (curc == '=') {
                        content = "<=";
                        Type = "LEQ";
                    } else {
                        retract();
                        content = "<";
                        Type = "LSS";
                    }
                } else if (curc == '>') {
                    getCurc();
                    if (curc == '=') {
                        content = ">=";
                        Type = "GEQ";
                    } else {
                        retract();
                        content = ">";
                        Type = "GRE";
                    }
                } else if (curc == '=') {
                    getCurc();
                    if (curc == '=') {
                        content = "==";
                        Type = "EQL";
                    } else {
                        retract();
                        content = "=";
                        Type = "ASSIGN";
                    }
                } else if (curc == '"') { // 处理格式化字符串
                    catToken();
                    getCurc();
                    while (curc != '"') {
                        catToken();
                        getCurc();
                    }
                    catToken();
                    //TODO FormatString error
                    content = s.toString();
                    Type = "STRCON";
                } else { // 处理单个字符
                    content = Character.toString(curc);
                    if (content2Type.containsKey(content)) {
                        Type = content2Type.get(content);
                    }
                }
                putToken();
            }
        }
    }

    // 读取指针当前位置的符号并移动指针，若为末尾则返回'\n'
    public void getCurc() {
        if (pos >= line.length()) {
            curc = '\n';
        } else {
            curc = line.charAt(pos);
        }
        pos++;
    }

    // 后退一个指针
    public void retract() {
        pos--;
    }

    // 拼接字符串
    public void catToken() {
        s.append(curc);
    }

    // 处理标识符与保留字
    public void buildIdent() {
        content = s.toString();
        Type = content2Type.getOrDefault(content, "IDENFR");
    }

    // 清空上一个单词的情况
    public void clearToken() {
        s = new StringBuilder();
        Type = null;
        content = null;
    }

    // 如果单词有效则向单词表中输出一个单词
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
        String filePath = "./output.txt";
        File file = new File(filePath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.setOut(new PrintStream(fos));
        for (Token token : tokenTable) {
            System.out.println(token.getType() + ' ' + token.getContent());
        }
    }
}
