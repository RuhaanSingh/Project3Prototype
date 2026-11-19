package turnabout.model;

import java.util.NoSuchElementException;
import student.TestCase;

/**
 * Tests for LinkedStack. Kept in test-pending/ until Raul's LinkedStack is
 * pushed; then move this file to test/turnabout/model/. Written against
 * the contract, assuming it matches LinkedQueue: push(null) throws
 * IllegalArgumentException, and pop/peek on an empty stack throw
 * NoSuchElementException.
 *
 * @author Ruhaan Singh
 * @version Nov 2026
 */
public class LinkedStackTest extends TestCase
{
    private LinkedStack<String> s;

    /**
     * Makes a fresh empty stack.
     */
    public void setUp()
    {
        s = new LinkedStack<String>();
    }

    /**
     * A new stack is empty with size 0.
     */
    public void testNewStackIsEmpty()
    {
        assertTrue(s.isEmpty());
        assertEquals(0, s.size());
    }

    /**
     * push adds to the top and grows the size.
     */
    public void testPush()
    {
        s.push("A");
        s.push("B");
        assertFalse(s.isEmpty());
        assertEquals(2, s.size());
        assertEquals("B", s.peek());
    }

    /**
     * push(null) throws and leaves the stack unchanged.
     */
    public void testPushNull()
    {
        s.push("A");
        Exception thrown = null;
        try
        {
            s.push(null);
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertTrue(thrown instanceof IllegalArgumentException);
        assertEquals(1, s.size());
        assertEquals("A", s.peek());
    }

    /**
     * pop returns items last in, first out.
     */
    public void testPop()
    {
        s.push("A");
        s.push("B");
        s.push("C");
        assertEquals("C", s.pop());
        assertEquals("B", s.pop());
        assertEquals("A", s.pop());
        assertTrue(s.isEmpty());
        assertEquals(0, s.size());
    }

    /**
     * pop on an empty stack throws, also after emptying it.
     */
    public void testPopEmpty()
    {
        Exception thrown = null;
        try
        {
            s.pop();
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertTrue(thrown instanceof NoSuchElementException);

        s.push("A");
        s.pop();
        thrown = null;
        try
        {
            s.pop();
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertTrue(thrown instanceof NoSuchElementException);
    }

    /**
     * peek returns the top without removing it.
     */
    public void testPeek()
    {
        s.push("A");
        s.push("B");
        assertEquals("B", s.peek());
        assertEquals("B", s.peek());
        assertEquals(2, s.size());
    }

    /**
     * peek on an empty stack throws.
     */
    public void testPeekEmpty()
    {
        Exception thrown = null;
        try
        {
            s.peek();
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertTrue(thrown instanceof NoSuchElementException);
    }

    /**
     * isEmpty and size track pushes and pops, and the stack can be reused.
     */
    public void testIsEmptyAndSize()
    {
        assertTrue(s.isEmpty());
        s.push("A");
        assertFalse(s.isEmpty());
        assertEquals(1, s.size());
        s.pop();
        assertTrue(s.isEmpty());
        assertEquals(0, s.size());
        s.push("B");
        assertEquals("B", s.peek());
        assertEquals(1, s.size());
    }
}
