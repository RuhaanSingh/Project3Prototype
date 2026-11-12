package turnabout.model;

import student.TestCase;

public class PlayerTest extends TestCase {
    private Player p;
    private final Card r1 = new Card(CardColor.RED, 1);
    private final Card b2 = new Card(CardColor.BLUE, 2);

    public void setUp() {
        p = new Player("Ana");
        p.addCard(r1);
        p.addCard(b2);
    }

    public void testNameAndHand() {
        assertEquals("Ana", p.getName());
        assertEquals(2, p.getHand().size());
        assertSame(r1, p.getHand().get(0));
    }

    public void testRemoveCard() {
        assertSame(r1, p.removeCard(0));
        assertEquals(1, p.getHand().size());
        assertSame(b2, p.getHand().get(0));
    }

    public void testRemoveCardBadIndex() {
        for (int bad : new int[] {-1, 2, 5}) {
            try {
                p.removeCard(bad);
                fail("expected IndexOutOfBoundsException for " + bad);
            } catch (IndexOutOfBoundsException e) {
                assertEquals(2, p.getHand().size());
            }
        }
    }

    public void testAddNullCard() {
        try {
            p.addCard(null);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(2, p.getHand().size());
        }
    }

    public void testHandIsReadOnly() {
        try {
            p.getHand().clear();
            fail("expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals(2, p.getHand().size());
        }
    }
}
