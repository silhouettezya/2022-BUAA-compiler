import java.io.*;
import java.util.ArrayList;

public class Reader {
    private ArrayList<String> lines;

    public Reader() {
        lines = new ArrayList<>();
    }

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

    public void cleanNotes() {
        boolean isNote1 = false;
        //boolean isNote = false;
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
                if (!(isNoteLines || isString)) {
                    if (isNote1) {
                        isNote1 = false;
                        if (c == '/') {
                            notePos = pos - 1;
                            lines.set(i, line.substring(0, notePos));
                            notePos = -1;
                            break;
                        } else if (c == '*') {
                            isNoteLines = true;
                            notePos = pos - 1;
                            noteLine = i;
                        }
                    } else if (c == '/') {
                        isNote1 = true;
                    }
                    if (c == '"') {
                        isString = true;
                    }
                } /*else if (isNote) {
                    lines.set(noteLine, lines.get(noteLine).substring(0, notePos));
                    isNote = false;
                    notePos = -1;
                    if (noteLine == i) {
                        noteLine = -1;
                        break;
                    }
                    noteLine = -1;
                }*/ else if (isNoteLines) {
                    if (isNoteEnd1) {
                        isNoteEnd1 = false;
                        if (c == '/') {
                            isNoteLines = false;
                            noteEnd = pos + 1;
                            /*if (notePos != -1) {
                                String s = line.substring(0, notePos) + " " + line.substring(noteEnd);
                                lines.set(i, s);
                                notePos = -1;
                            } else {
                                lines.set(i, line.substring(noteEnd));
                            }*/
                            if (noteLine == i) {
                                String s = line.substring(0, notePos) + " " + line.substring(noteEnd);
                                lines.set(i, s);
                                notePos = -1;
                                noteEnd = -1;
                            } else {
                                lines.set(noteLine, lines.get(noteLine).substring(0, notePos));
                                String s;
                                for (int j = noteLine + 1; j < i; j++) {
                                    s = "";
                                    lines.set(j, s);
                                }
                                lines.set(i, line.substring(noteEnd));
                                notePos = -1;
                                noteEnd = -1;
                            }
                            break;
                        }
                    }
                    if (c == '*') {
                        isNoteEnd1 = true;
                    }
                    /*if (pos == line.length() - 1) {
                        isNoteEnd1 = false;
                        if (notePos != -1) {
                            lines.set(i, line.substring(0, notePos));
                            notePos = -1;
                        } else {
                            lines.set(i, "");
                        }
                        break;
                    }*/
                } else if (isString) {
                    if (c == '"') {
                        isString = false;
                    }
                }
                pos++;
            }
            isNote1 = false;
            isNoteEnd1 = false;
        }
    }
}
