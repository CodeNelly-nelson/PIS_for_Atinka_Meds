package atinka.data_structures;

import java.util.NoSuchElementException;

public class LinkedStack<T> {
    private static final class Node<E> {
        E v; Node<E> next;
        Node(E v, Node<E> next) { this.v = v; this.next = next; }
    }

    private Node<T> top; // top of stack
    private int size = 0;

    public void push(T v) { top = new Node<>(v, top); size++; }

    public T pop() {
        if (top == null) throw new NoSuchElementException("Stack is empty");
        T v = top.v; top = top.next; size--; return v;
    }

    public T peek() {
        if (top == null) throw new NoSuchElementException("Stack is empty");
        return top.v;
    }

    public boolean isEmpty() { return size == 0; }
    public int size() { return size; }
    public void clear() { top = null; size = 0; }
}