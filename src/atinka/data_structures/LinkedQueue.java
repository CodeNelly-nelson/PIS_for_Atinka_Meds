package atinka.data_structures;

import java.util.NoSuchElementException;

public class LinkedQueue<T> {
    private static final class Node<E> {
        E v; Node<E> next;
        Node(E v) { this.v = v; }
    }

    private Node<T> head, tail; // head = front (dequeue), tail = back (enqueue)
    private int size = 0;

    public void enqueue(T v) {
        Node<T> n = new Node<>(v);
        if (tail == null) { head = tail = n; }
        else { tail.next = n; tail = n; }
        size++;
    }

    public T dequeue() {
        if (head == null) throw new NoSuchElementException("Queue is empty");
        T v = head.v; head = head.next; if (head == null) tail = null; size--; return v;
    }

    public T peek() {
        if (head == null) throw new NoSuchElementException("Queue is empty");
        return head.v;
    }

    public boolean isEmpty() { return size == 0; }
    public int size() { return size; }
    public void clear() { head = tail = null; size = 0; }
}