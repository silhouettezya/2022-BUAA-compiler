package middle.middlecode;

public class Node {
    private Node prev;
    private Node next;

    public Node() {}

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public Node getPrev() {
        return prev;
    }

    public Node getNext() {
        return next;
    }

    public boolean hasPrev() {
        return prev != null;
    }

    public boolean hasNext() {
        return next != null;
    }

    public void remove() {
        // prev.next = next
        if (hasPrev()) {
            getPrev().setNext(getNext());
        }
        // next.prev = prev
        if (hasNext()) {
            getNext().setPrev(getPrev());
        }
    }

    // usage: current.insertAfter(another)
    public void insertAfter(Node node) {
        node.setPrev(this);
        node.setNext(getNext());
        if (hasNext()) {
            getNext().setPrev(node);
        }
        setNext(node);
    }

    public void insertBefore(Node node) {
        node.setNext(this);
        node.setPrev(getPrev());
        if (hasPrev()) {
            getPrev().setNext(node);
        }
        setPrev(node);
    }
}
