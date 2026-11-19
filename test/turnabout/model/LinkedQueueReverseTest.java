package turnabout.model;

import java.util.NoSuchElementException;
import student.TestCase;

/**
 * Tests for LinkedQueue.reverse(). Kept in test-pending/ until Raul's
 * reverse() is pushed; then move this file to test/turnabout/model/.
 * Written against the contract: reverse() flips the order in place and
 * keeps head, tail and size correct.
 *
 * @author Ruhaan Singh
 * @version Nov 2026
 */
public class LinkedQueueReverseTest extends TestCase
{
    private LinkedQueue<String> q;

    /**
     * Makes a fresh empty queue.
     */
    public void setUp()
    {
        q = new LinkedQueue<String>();
    }

    /**
     * Several items come out in the opposite order.
     */
    public void testReverseSeveral()
    {
        q.enqueue("A");
        q.enqueue("B");
        q.enqueue("C");
        q.enqueue("D");
        q.reverse();
        assertEquals(4, q.size());
        assertEquals("D", q.peek());
        assertEquals("D", q.dequeue());
        assertEquals("C", q.dequeue());
        assertEquals("B", q.dequeue());
        assertEquals("A", q.dequeue());
        assertTrue(q.isEmpty());
    }

    /**
     * One item stays the same, and tail still works.
     */
    public void testReverseOne()
    {
        q.enqueue("A");
        q.reverse();
        assertEquals(1, q.size());
        assertEquals("A", q.peek());
        q.enqueue("B");
        assertEquals("A", q.dequeue());
        assertEquals("B", q.dequeue());
    }

    /**
     * Reversing an empty queue does nothing and does not throw.
     */
    public void testReverseEmpty()
    {
        q.reverse();
        assertTrue(q.isEmpty());
        assertEquals(0, q.size());
        Exception thrown = null;
        try
        {
            q.peek();
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertTrue(thrown instanceof NoSuchElementException);
        q.enqueue("A");
        assertEquals("A", q.peek());
    }

    /**
     * After reversing, a new item goes to the true back (tail was fixed).
     */
    public void testEnqueueAfterReverseGoesToBack()
    {
        q.enqueue("A");
        q.enqueue("B");
        q.enqueue("C");
        q.reverse();
        q.enqueue("E");
        assertEquals(4, q.size());
        assertEquals("C", q.dequeue());
        assertEquals("B", q.dequeue());
        assertEquals("A", q.dequeue());
        assertEquals("E", q.dequeue());
        assertTrue(q.isEmpty());
    }

    /**
     * Dequeue right after reverse, then reverse again: the turn-order case
     * from the spec ([B, C, D] then A joins the back).
     */
    public void testReverseThenDequeueOrder()
    {
        q.enqueue("B");
        q.enqueue("C");
        q.enqueue("D");
        q.reverse();
        q.enqueue("A");
        assertEquals("D", q.dequeue());
        q.reverse();
        assertEquals("A", q.dequeue());
        assertEquals("B", q.dequeue());
        assertEquals("C", q.dequeue());
        assertTrue(q.isEmpty());
    }

    /**
     * Reversing twice gives back the original order.
     */
    public void testReverseTwice()
    {
        q.enqueue("A");
        q.enqueue("B");
        q.enqueue("C");
        q.reverse();
        q.reverse();
        assertEquals("A", q.dequeue());
        assertEquals("B", q.dequeue());
        assertEquals("C", q.dequeue());
    }
}
