package turnabout.model;

import java.util.NoSuchElementException;

/**
 * Our own singly linked FIFO queue with head and tail references.
 * enqueue, dequeue, peek, isEmpty and size are all O(1).
 */
public class LinkedQueue<T> {

    /** One link in the chain. Private so nothing outside can break the links. */
    private static class Node<T> {
        private final T value;
        private Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> head; // front: dequeue and peek happen here
    private Node<T> tail; // back: enqueue happens here
    private int size;

    /**
     * Adds item at the back of the queue.
     * @throws IllegalArgumentException if item is null
     */
    public void enqueue(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot enqueue null");
        }
        Node<T> node = new Node<>(item);
        if (tail == null) {        // empty queue: new node is both ends
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
        size++;
    }

    /**
     * Removes and returns the item at the front of the queue.
     * @throws NoSuchElementException if the queue is empty
     */
    public T dequeue() {
        if (head == null) {
            throw new NoSuchElementException("Queue is empty");
        }
        T value = head.value;
        head = head.next;
        if (head == null) {        // removed the last item: tail must not point at it
            tail = null;
        }
        size--;
        return value;
    }

    /**
     * Returns the item at the front without removing it.
     * @throws NoSuchElementException if the queue is empty
     */
    public T peek() {
        if (head == null) {
            throw new NoSuchElementException("Queue is empty");
        }
        return head.value;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}
