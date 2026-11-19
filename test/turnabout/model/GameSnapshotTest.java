package turnabout.model;

import java.util.ArrayList;
import java.util.List;
import student.TestCase;

/**
 * Tests for GameSnapshot: null checks, the read-only hand, and the winner.
 *
 * @author Ruhaan Singh
 * @version Nov 2026
 */
public class GameSnapshotTest extends TestCase
{
    private Card top;
    private List<Card> hand;
    private GameSnapshot snap;

    /**
     * Builds a normal snapshot with no winner.
     */
    public void setUp()
    {
        top = new Card(CardColor.RED, 5);
        hand = new ArrayList<Card>();
        hand.add(new Card(CardColor.BLUE, 1));
        hand.add(Card.reverse(CardColor.GREEN));
        snap = new GameSnapshot("Ann", "Ben", top, hand, null);
    }

    /**
     * Calls the constructor and returns what it threw, or null.
     */
    private Exception build(String cur, String next, Card t, List<Card> h)
    {
        try
        {
            new GameSnapshot(cur, next, t, h, null);
        }
        catch (Exception e)
        {
            return e;
        }
        return null;
    }

    /**
     * The getters return what was passed to the constructor.
     */
    public void testConstructorAndGetters()
    {
        assertEquals("Ann", snap.getCurrentName());
        assertEquals("Ben", snap.getNextName());
        assertEquals(top, snap.getTopCard());
        assertEquals(2, snap.getCurrentHand().size());
        assertEquals("BLUE 1", snap.getCurrentHand().get(0).toString());
        assertTrue(snap.getCurrentHand().get(1).isReverse());
    }

    /**
     * Each required argument being null throws IllegalArgumentException.
     */
    public void testConstructorNulls()
    {
        assertTrue(build(null, "Ben", top, hand)
            instanceof IllegalArgumentException);
        assertTrue(build("Ann", null, top, hand)
            instanceof IllegalArgumentException);
        assertTrue(build("Ann", "Ben", null, hand)
            instanceof IllegalArgumentException);
        assertTrue(build("Ann", "Ben", top, null)
            instanceof IllegalArgumentException);
    }

    /**
     * An empty hand is allowed.
     */
    public void testEmptyHand()
    {
        GameSnapshot s = new GameSnapshot("Ann", "Ben", top,
            new ArrayList<Card>(), "Ann");
        assertTrue(s.getCurrentHand().isEmpty());
    }

    /**
     * Changing the returned hand throws UnsupportedOperationException.
     */
    public void testHandIsUnmodifiable()
    {
        List<Card> h = snap.getCurrentHand();
        Exception thrown = null;
        try
        {
            h.add(new Card(CardColor.RED, 1));
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertTrue(thrown instanceof UnsupportedOperationException);

        thrown = null;
        try
        {
            h.remove(0);
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertTrue(thrown instanceof UnsupportedOperationException);

        thrown = null;
        try
        {
            h.clear();
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertTrue(thrown instanceof UnsupportedOperationException);
        assertEquals(2, snap.getCurrentHand().size());
    }

    /**
     * Changing the original list after building does not change the
     * snapshot.
     */
    public void testHandIsCopied()
    {
        hand.clear();
        assertEquals(2, snap.getCurrentHand().size());
    }

    /**
     * The winner name is null when nobody has won.
     */
    public void testWinnerNameNull()
    {
        assertNull(snap.getWinnerName());
    }

    /**
     * The winner name is returned when someone has won.
     */
    public void testWinnerNameSet()
    {
        GameSnapshot s = new GameSnapshot("Ann", "Ben", top, hand, "Ann");
        assertEquals("Ann", s.getWinnerName());
    }
}
