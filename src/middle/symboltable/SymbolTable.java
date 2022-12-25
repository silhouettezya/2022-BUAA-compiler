package middle.symboltable;

import java.util.HashMap;
import java.util.Objects;

public class SymbolTable {
    private SymbolTable father;
    private HashMap<String, Symbol> map;
    private SymbolTable firstFather;
    private int capacity = 0;

    public SymbolTable() {
        father = null;
        map = new HashMap<>();
        firstFather = null;
    }

    public SymbolTable(SymbolTable father) {
        this.father = father;
        map = new HashMap<>();
        if (father.firstFather == null) {
            this.firstFather = father;
        } else {
            this.firstFather = father.firstFather;
        }
    }

    public boolean isRepeat(String name) {
        return map.containsKey(name);
    }

    public Symbol getSymbol(String name) {
        if (map.containsKey(name)) {
            return map.get(name);
        } else if (father != null) {
            return father.getSymbol(name);
        } else {
            return null;
        }
    }

    public void addSymbol(Symbol s) {
        map.put(s.getName(), s);
        capacity += s.capacity();
    }

    public int capacity() {
        return capacity;
    }

    public SymbolTable getFather() {
        return father;
    }

    public boolean contains(String name, boolean recursive) {
        if (map.containsKey(name)) {
            return true;
        } else {
            if (father != null && recursive) {
                return father.contains(name, true);
            }
            return false;
        }
    }
}
