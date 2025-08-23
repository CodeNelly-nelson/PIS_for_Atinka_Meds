package atinka.dsa;

public final class LinkedStack<T> {
    private static final class Node<E> { E v; Node<E> next; Node(E v, Node<E> n){ this.v = v; this.next = n; } }
    private Node<T> top;
    private int n;

    public void push(T v){ top = new Node<>(v, top); n++; }
    public T pop(){ if (top == null) return null; T v = top.v; top = top.next; n--; return v; }
    public T peek(){ return top == null ? null : top.v; }
    public boolean isEmpty(){ return top == null; }
    public int size(){ return n; }
}
