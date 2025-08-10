package atinka.dsa;
//  Singly Linked
public final class LinkedList<T> {
    private static final class Node<E>{E v; Node<E> next; Node(E v){this.v=v;}}
    private Node<T> head, tail; private int n;
    public void addLast(T v){Node<T> x=new Node<>(v); if(tail==null){head=tail=x;} else {tail.next=x; tail=x;} n++;}
    public T removeFirst(){ if(head==null) return null; T v=head.v; head=head.next; if(head==null) tail=null; n--; return v;}
    public T peekFirst(){ return head==null?null:head.v; }
    public int size(){ return n; }
    public boolean isEmpty(){ return n==0; }
}