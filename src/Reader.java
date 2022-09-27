import java.io.*;
import java.util.ArrayList;

public class Reader {
    private ArrayList<String> lines;

    public Reader() {
        lines = new ArrayList<>();
    }

    // 读入源程序，并将源程序中的注释处理掉后返回数组
    public ArrayList<String> readLines() {
        String filePath = "./testfile.txt";
        File file = new File(filePath);
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String tempStr;
        while (true) {
            try {
                if (null == (tempStr = bufferedReader.readLine())) {
                    break;
                } else {
                    lines.add(tempStr);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        cleanNotes();
        return lines;
    }

    // 处理注释函数
    public void cleanNotes() {
        boolean isNote1 = false;
        boolean isNoteLines = false;
        boolean isString = false;
        boolean isNoteEnd1 = false;
        int notePos = -1;
        int noteEnd = -1;
        int noteLine = -1;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int pos = 0;
            char c;
            while (pos < line.length()) {
                c = line.charAt(pos);
                // 非注释或格式字符串的情况
                if (!(isNoteLines || isString)) {
                    // 如果已经读入一个'/'
                    if (isNote1) {
                        isNote1 = false;
                        if (c == '/') { // 为单行注释，直接处理
                            notePos = pos - 1;
                            lines.set(noteLine, line.substring(0, notePos));
                            notePos = -1;
                            break;
                        } else if (c == '*') { // 为多行注释，标记并记录当前位置，等待多行注释结束符
                            isNoteLines = true;
                            notePos = pos - 1;
                            noteLine = i;
                        }
                    } else if (c == '/') { // 读入了'/'，标记，等待下一个字符
                        isNote1 = true;
                    }
                    if (c == '"') { // 格式化字符串标识符，标记，等待字符串结束
                        isString = true;
                    }
                } else if (isNoteLines) { // 处理多行注释
                    if (isNoteEnd1) { // 已经读入'*'，等待读入'/'
                        isNoteEnd1 = false;
                        if (c == '/') { // 标志多行注释结束，对所有注释进行处理
                            isNoteLines = false;
                            noteEnd = pos + 1;
                            if (noteLine == i) { // 如果在同一行，简单处理即可
                                // TODO 可能会要求处理标识符中的多行字符这种error
                                String s = line.substring(0, notePos) + " " + line.substring(noteEnd);
                                lines.set(i, s);
                            } else { // 不同行，需要一起处理
                                lines.set(noteLine, lines.get(noteLine).substring(0, notePos));// 清除第一行
                                String s;
                                for (int j = noteLine + 1; j < i; j++) {// 清除中间行
                                    s = "";
                                    lines.set(j, s);
                                }
                                lines.set(i, line.substring(noteEnd));// 清除尾行
                            }
                            notePos = -1;
                            noteEnd = -1;
                            break;
                        }
                    }
                    if (c == '*') {// 读入'*'
                        isNoteEnd1 = true;
                    }
                } else if (isString) {
                    if (c == '"') {
                        isString = false;
                    }
                }
                pos++;
            }
            isNote1 = false;// 防止换行被当作连续读入
            isNoteEnd1 = false;
        }
    }
}
