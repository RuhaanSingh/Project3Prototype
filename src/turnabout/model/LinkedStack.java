package turnabout.model;

import java.util.NoSuchElementException;

/**
 * Our own singly linked LIFO stack. push, pop, peek, isEmpty and size are
 * all O(1). Used by LinkedQueue.reverse().
 *
 * @author Raul (GenAI-assisted, see README)
 * @version Nov 2026
 */
public class LinkedStack<T> {

    /** One link in the chain. Private so nothing outside can break the links. */
    private static class Node<T> {
        private final T value;
        private final Node<T> next;

        Node(T value, Node<T> next) {
            this.value = value;
            this.next = next;
        }
    }

    private Node<T> top;  // push, pop and peek all happen here
    private int size;

    /**
     * Puts item on top of the stack.
     * @throws IllegalArgumentException if item is null
     */
    public void push(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot push null");
        }
        top = new Node<>(item, top);
        size++;
    }

    /**
     * Removes and returns the item on top of the stack.
     * @throws NoSuchElementException if the stack is empty
     */
    public T pop() {
        if (top == null) {
            throw new NoSuchElementException("Stack is empty");
        }
        T value = top.value;
        top = top.next;
        size--;
        return value;
    }

    /**
     * Returns the item on top without removing it.
     * @throws NoSuchElementException if the stack is empty
     */
    public T peek() {
        if (top == null) {
            throw new NoSuchElementException("Stack is empty");
        }
        return top.value;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}
