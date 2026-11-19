package turnabout.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import student.TestCase;

/**
 * Tests for Reverse cards in Deck (the 44-card standard deck).
 *
 * @author Raul (GenAI-assisted, see README)
 * @version Nov 2026
 */
public class DeckReverseTest extends TestCase
{
    /** Draws every card from d. */
    private List<Card> drawAll(Deck d)
    {
        List<Card> cards = new ArrayList<Card>();
        Card c = d.draw();
        while (c != null)
        {
            cards.add(c);
            c = d.draw();
        }
        return cards;
    }

    public void testStandardDeckHas44Cards()
    {
        assertEquals(44, Deck.STANDARD_SIZE);
        assertEquals(44, drawAll(Deck.standard(new Random(7))).size());
    }

    public void testConstructorBuildsSameDeckAsStandard()
    {
        assertEquals(Deck.STANDARD_SIZE, drawAll(new Deck(new Random(7))).size());
    }

    public void testStandardDeckHasAll40NumberCards()
    {
        Set<String> numbers = new HashSet<String>();
        for (Card c : drawAll(Deck.standard(new Random(3))))
        {
            if (!c.isReverse())
            {
                numbers.add(c.toString());
            }
        }
        assertEquals(40, numbers.size());
    }

    public void testOneReversePerColor()
    {
        int[] perColor = new int[CardColor.values().length];
        for (Card c : drawAll(Deck.standard(new Random(11))))
        {
            if (c.isReverse())
            {
                perColor[c.getColor().ordinal()]++;
            }
        }
        for (int count : perColor)
        {
            assertEquals(Deck.REVERSES_PER_COLOR, count);
        }
        assertEquals(1, Deck.REVERSES_PER_COLOR);
    }

    public void testStandardNullRandomThrows()
    {
        try
        {
            Deck.standard(null);
            fail("expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    public void testFixedDeckCanHoldReverseCards()
    {
        Card rev = Card.reverse(CardColor.GREEN);
        List<Card> order = new ArrayList<Card>();
        order.add(rev);
        Deck d = new Deck(order, new Random(1));
        assertSame(rev, d.draw());
        assertNull(d.draw());
    }
}
