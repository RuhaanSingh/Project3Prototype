package turnabout.model;

import student.TestCase;

/**
 * Tests for Card: number cards, Reverse cards, and the matching rules.
 *
 * @author Ruhaan Singh
 * @version Nov 2026
 */
public class CardTest extends TestCase
{
    private Card red7;
    private Card redReverse;

    /**
     * Builds the cards used by most tests.
     */
    public void setUp()
    {
        red7 = new Card(CardColor.RED, 7);
        redReverse = Card.reverse(CardColor.RED);
    }

    // ---- constructor ----

    /**
     * The constructor accepts both ends of the 0 to 9 range.
     */
    public void testConstructorEdges()
    {
        assertEquals(0, new Card(CardColor.BLUE, 0).getNumber());
        assertEquals(9, new Card(CardColor.BLUE, 9).getNumber());
    }

    /**
     * The constructor rejects a null color.
     */
    public void testConstructorNullColor()
    {
        Exception thrown = null;
        try
        {
            new Card(null, 3);
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertTrue(thrown instanceof IllegalArgumentException);
    }

    /**
     * The constructor rejects numbers below 0 and above 9, including -1.
     */
    public void testConstructorBadNumber()
    {
        int[] bad = {-1, 10, -100, 100};
        for (int n : bad)
        {
            Exception thrown = null;
            try
            {
                new Card(CardColor.RED, n);
            }
            catch (Exception e)
            {
                thrown = e;
            }
            assertTrue("number " + n,
                thrown instanceof IllegalArgumentException);
        }
    }

    // ---- getColor / getNumber ----

    /**
     * getColor returns the color for number and Reverse cards.
     */
    public void testGetColor()
    {
        assertEquals(CardColor.RED, red7.getColor());
        assertEquals(CardColor.RED, redReverse.getColor());
        assertEquals(CardColor.GREEN,
            Card.reverse(CardColor.GREEN).getColor());
    }

    /**
     * getNumber returns the number, or -1 for a Reverse card.
     */
    public void testGetNumber()
    {
        assertEquals(7, red7.getNumber());
        assertEquals(-1, redReverse.getNumber());
    }

    // ---- isReverse / reverse factory ----

    /**
     * isReverse is false for number cards and true for Reverse cards.
     */
    public void testIsReverse()
    {
        assertFalse(red7.isReverse());
        assertFalse(new Card(CardColor.BLUE, 0).isReverse());
        assertTrue(redReverse.isReverse());
    }

    /**
     * The reverse factory makes a Reverse card for every color.
     */
    public void testReverseFactory()
    {
        for (CardColor color : CardColor.values())
        {
            Card c = Card.reverse(color);
            assertTrue(c.isReverse());
            assertEquals(color, c.getColor());
            assertEquals(-1, c.getNumber());
        }
    }

    /**
     * The reverse factory rejects a null color.
     */
    public void testReverseFactoryNull()
    {
        Exception thrown = null;
        try
        {
            Card.reverse(null);
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertTrue(thrown instanceof IllegalArgumentException);
    }

    // ---- matches: number cards ----

    /**
     * Number cards match by color or by number.
     */
    public void testMatchesNumberCards()
    {
        assertTrue(red7.matches(new Card(CardColor.RED, 2)));
        assertTrue(red7.matches(new Card(CardColor.BLUE, 7)));
        assertTrue(red7.matches(new Card(CardColor.RED, 7)));
        assertFalse(red7.matches(new Card(CardColor.BLUE, 2)));
    }

    /**
     * matches(null) returns false for both kinds of card.
     */
    public void testMatchesNull()
    {
        assertFalse(red7.matches(null));
        assertFalse(redReverse.matches(null));
    }

    // ---- matches: Reverse rules ----

    /**
     * A Reverse card matches a number card of the same color, both ways.
     */
    public void testReverseMatchesSameColorNumber()
    {
        assertTrue(redReverse.matches(red7));
        assertTrue(red7.matches(redReverse));
    }

    /**
     * A Reverse card matches any other Reverse card, even of another color.
     */
    public void testReverseMatchesReverse()
    {
        assertTrue(redReverse.matches(Card.reverse(CardColor.RED)));
        assertTrue(redReverse.matches(Card.reverse(CardColor.BLUE)));
        assertTrue(Card.reverse(CardColor.BLUE).matches(redReverse));
    }

    /**
     * A Reverse card does not match a number card of a different color,
     * either way round, for any number.
     */
    public void testReverseDoesNotMatchOtherColorNumber()
    {
        for (int n = 0; n <= 9; n++)
        {
            Card blue = new Card(CardColor.BLUE, n);
            assertFalse(redReverse.matches(blue));
            assertFalse(blue.matches(redReverse));
        }
    }

    // ---- toString ----

    /**
     * toString shows the color and number, or the color and REVERSE.
     */
    public void testToString()
    {
        assertEquals("RED 7", red7.toString());
        assertEquals("BLUE 0", new Card(CardColor.BLUE, 0).toString());
        assertEquals("RED REVERSE", redReverse.toString());
        assertEquals("YELLOW REVERSE",
            Card.reverse(CardColor.YELLOW).toString());
    }
}
