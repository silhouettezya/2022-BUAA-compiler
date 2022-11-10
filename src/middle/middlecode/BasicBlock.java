package middle.middlecode;

public class BasicBlock extends Node {
    private final String label;
    private final Node tail = new Node() {};

    public enum Type {
        FUNC,   // 函数体
        BRANCH, // 分支
        LOOP,   // 循环
        BASIC   // 普通的 {}
    }

    private final Type type;

    public BasicBlock(String label, Type type) {
        this.label = label;
        this.type = type;
        this.setNext(tail);
        this.tail.setPrev(this);
    }

    public String getLabel() {
        return label;
    }

    public Node getHead() {
        return getNext();
    }

    public Type getType() {
        return type;
    }

    public void setHead(Node node) {
        setNext(node);
        Node tail = node;
        while (tail.hasNext()) {
            tail = tail.getNext();
        }
        tail.setNext(this.tail);
        this.tail.setPrev(tail);
    }

    public Node getTail() {
        return tail.getPrev();
    }

    public void append(Node follow) {
        Node last = tail.getPrev();
        last.setNext(follow);
        follow.setPrev(last);
        Node tail = follow;
        while (tail.hasNext()) {
            tail = tail.getNext();
        }
        tail.setNext(this.tail);
        this.tail.setPrev(tail);
    }

    @Override
    public String toString() {
        return getLabel();
    }
}