package turnabout.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import student.TestCase;

public class DeckTest extends TestCase {
    private final Card a = new Card(CardColor.RED, 1);
    private final Card b = new Card(CardColor.BLUE, 2);
    private final Card c = new Card(CardColor.GREEN, 3);

    private Deck fixed() {
        List<Card> order = new ArrayList<>();
        order.add(a);
        order.add(b);
        order.add(c);
        return new Deck(order, new Random(1));
    }

    public void testFixedDeckDrawsInOrder() {
        Deck d = fixed();
        assertSame(a, d.draw());
        assertSame(b, d.draw());
        assertSame(c, d.draw());
        assertNull(d.draw());
    }

    public void testStandardDeckHas40DistinctCards() {
        Deck d = new Deck(new Random(7));
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 40; i++) {
            Card card = d.draw();
            assertNotNull(card);
            seen.add(card.toString());
        }
        assertEquals(40, seen.size());
        assertNull(d.draw());
    }

    public void testTopCardEmptyThenDiscard() {
        Deck d = fixed();
        assertNull(d.topCard());
        d.discard(a);
        d.discard(b);
        assertSame(b, d.topCard());
    }

    public void testDrawReshufflesDiscardsButKeepsTop() {
        Deck d = fixed();
        d.discard(d.draw());
        d.discard(d.draw());
        d.discard(d.draw()); // draw pile empty; discards a, b, c with c on top
        Card x = d.draw();
        Card y = d.draw();
        assertTrue(x == a || x == b);
        assertTrue(y == a || y == b);
        assertNotSame(x, y);
        assertSame(c, d.topCard());
        assertNull(d.draw());
    }

    public void testLoneTopDiscardIsNeverReshuffled() {
        Deck d = fixed();
        d.draw();
        d.draw();
        d.discard(d.draw());
        assertNull(d.draw());
        assertSame(c, d.topCard());
    }

    public void testBadConstructorArguments() {
        List<Card> one = new ArrayList<>();
        one.add(a);
        List<Card> withNull = new ArrayList<>();
        withNull.add(null);
        try { new Deck(null); fail("null rng"); } catch (IllegalArgumentException e) { }
        try { new Deck(null, new Random()); fail("null list"); } catch (IllegalArgumentException e) { }
        try { new Deck(new ArrayList<Card>(), new Random()); fail("empty list"); }
        catch (IllegalArgumentException e) { }
        try { new Deck(one, null); fail("null rng"); } catch (IllegalArgumentException e) { }
        try { new Deck(withNull, new Random()); fail("null card"); } catch (IllegalArgumentException e) { }
    }

    public void testDiscardNull() {
        try {
            fixed().discard(null);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
