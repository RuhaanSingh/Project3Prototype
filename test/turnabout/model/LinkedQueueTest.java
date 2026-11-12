package turnabout.model;

import java.util.NoSuchElementException;
import student.TestCase;

public class LinkedQueueTest extends TestCase {
    private LinkedQueue<String> q;

    public void setUp() {
        q = new LinkedQueue<>();
    }

    public void testNewQueueIsEmpty() {
        assertTrue(q.isEmpty());
        assertEquals(0, q.size());
    }

    public void testEnqueueDequeueKeepsOrder() {
        q.enqueue("A");
        q.enqueue("B");
        q.enqueue("C");
        assertFalse(q.isEmpty());
        assertEquals(3, q.size());
        assertEquals("A", q.dequeue());
        assertEquals("B", q.dequeue());
        assertEquals("C", q.dequeue());
        assertTrue(q.isEmpty());
    }

    public void testPeekDoesNotRemove() {
        q.enqueue("A");
        q.enqueue("B");
        assertEquals("A", q.peek());
        assertEquals(2, q.size());
    }

    public void testReuseAfterEmptying() {
        q.enqueue("A");
        q.dequeue();
        q.enqueue("B"); // tail must have been reset
        assertEquals("B", q.peek());
        assertEquals(1, q.size());
    }

    public void testEnqueueNull() {
        try {
            q.enqueue(null);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(0, q.size());
        }
    }

    public void testPeekEmpty() {
        try {
            q.peek();
            fail("expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    public void testDequeueEmpty() {
        try {
            q.dequeue();
            fail("expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }
}
