package turnabout.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import student.TestCase;

/**
 * Tests for the Reverse rule in GameModel.playCard.
 *
 * Rules tested:
 * - An accepted Reverse returns REVERSED, flips the order of the waiting
 *   players, and passes the turn: with 3+ players the player who went before
 *   goes next.
 * - With 2 players, a Reverse is played normally: the other player goes next.
 * - A Reverse matches the same color or another Reverse.
 * - A Reverse played as the last card returns WON and does not flip the order.
 * - A Reverse flipped as the first top card is a normal top card.
 * - Rejected moves change nothing and send no notification.
 *
 * @author Raul (GenAI-assisted, see README)
 * @version Nov 2026
 */
public class GameModelReverseTest extends TestCase
{
    /** Saves every snapshot it is sent. */
    private static class Recorder implements GameListener
    {
        private final List<GameSnapshot> snaps = new ArrayList<GameSnapshot>();

        public void gameChanged(GameSnapshot s)
        {
            snaps.add(s);
        }
    }

    private Recorder rec;

    private static Card num(CardColor color, int n)
    {
        return new Card(color, n);
    }

    private static Card rev(CardColor color)
    {
        return Card.reverse(color);
    }

    /**
     * Starts a game. hands[i] is player i's 5 cards, dealt round-robin the
     * way startGame deals. Players are P0, P1, ... The first card of rest is
     * the starting card; the others are drawn after it.
     */
    private GameModel started(Card[][] hands, Card... rest)
    {
        List<Card> order = new ArrayList<Card>();
        for (int round = 0; round < GameModel.HAND_SIZE; round++)
        {
            for (Card[] hand : hands)
            {
                order.add(hand[round]);
            }
        }
        for (Card c : rest)
        {
            order.add(c);
        }
        GameModel m = new GameModel(new Deck(order, new Random(1)));
        rec = new Recorder();
        m.addListener(rec);
        for (int i = 0; i < hands.length; i++)
        {
            m.addPlayer("P" + i);
        }
        m.startGame();
        return m;
    }

    /** A hand starting with first, then blue filler. */
    private static Card[] hand(Card first)
    {
        return new Card[] {first, num(CardColor.BLUE, 6), num(CardColor.BLUE, 7),
            num(CardColor.BLUE, 8), num(CardColor.BLUE, 9)};
    }

    private static Card[] filler()
    {
        return hand(num(CardColor.YELLOW, 0));
    }

    // ---- normal cases ----

    public void testReverseWithFourPlayers()
    {
        // order P0,P1,P2,P3: after P0's Reverse, P3 goes, then P2
        GameModel m = started(new Card[][] {hand(rev(CardColor.RED)), filler(),
            filler(), filler()}, num(CardColor.RED, 5));
        assertEquals(MoveResult.REVERSED, m.playCard("P0", 0));
        GameSnapshot s = m.getSnapshot();
        assertEquals("P3", s.getCurrentName());
        assertEquals("P2", s.getNextName());
        assertTrue(s.getTopCard().isReverse());
    }

    public void testReverseWithThreePlayers()
    {
        GameModel m = started(new Card[][] {hand(rev(CardColor.RED)), filler(),
            filler()}, num(CardColor.RED, 5));
        assertEquals(MoveResult.REVERSED, m.playCard("P0", 0));
        assertEquals("P2", m.getSnapshot().getCurrentName());
        assertEquals("P1", m.getSnapshot().getNextName());
    }

    public void testOrderStaysReversed()
    {
        GameModel m = started(new Card[][] {hand(rev(CardColor.RED)), filler(),
            filler(), filler()}, num(CardColor.RED, 5),
            num(CardColor.GREEN, 0), num(CardColor.GREEN, 1), num(CardColor.GREEN, 2));
        m.playCard("P0", 0);
        assertEquals(MoveResult.DREW, m.drawCard("P3"));
        assertEquals(MoveResult.DREW, m.drawCard("P2"));
        assertEquals(MoveResult.DREW, m.drawCard("P1"));
        assertEquals("P0", m.getSnapshot().getCurrentName());
        assertEquals("P3", m.getSnapshot().getNextName());
    }

    public void testTwoReversesRestoreOrder()
    {
        GameModel m = started(new Card[][] {hand(rev(CardColor.RED)), filler(),
            filler(), hand(rev(CardColor.BLUE))}, num(CardColor.RED, 5));
        m.playCard("P0", 0);
        assertEquals(MoveResult.REVERSED, m.playCard("P3", 0)); // Reverse on Reverse
        assertEquals("P0", m.getSnapshot().getCurrentName());
        assertEquals("P1", m.getSnapshot().getNextName());
    }

    public void testReverseWithTwoPlayersPassesTurn()
    {
        GameModel m = started(new Card[][] {hand(rev(CardColor.RED)), filler()},
            num(CardColor.RED, 5));
        assertEquals(MoveResult.REVERSED, m.playCard("P0", 0));
        assertEquals("P1", m.getSnapshot().getCurrentName());
        assertEquals("P0", m.getSnapshot().getNextName());
    }

    public void testReverseSendsOneSnapshot()
    {
        GameModel m = started(new Card[][] {hand(rev(CardColor.RED)), filler(),
            filler()}, num(CardColor.RED, 5));
        int before = rec.snaps.size();
        m.playCard("P0", 0);
        assertEquals(before + 1, rec.snaps.size());
        assertEquals("P2", rec.snaps.get(rec.snaps.size() - 1).getCurrentName());
    }

    public void testNumberCardOnReverseOfSameColor()
    {
        GameModel m = started(new Card[][] {hand(rev(CardColor.RED)), filler(),
            hand(num(CardColor.RED, 3))}, num(CardColor.RED, 5));
        m.playCard("P0", 0);
        assertEquals(MoveResult.PLAYED, m.playCard("P2", 0));
        assertEquals("P1", m.getSnapshot().getCurrentName()); // still reversed
    }

    public void testReverseAsFirstTopCardIsNormal()
    {
        GameModel m = started(new Card[][] {hand(rev(CardColor.GREEN)), filler(),
            filler()}, rev(CardColor.RED));
        GameSnapshot s = m.getSnapshot();
        assertTrue(s.getTopCard().isReverse());
        assertEquals("P0", s.getCurrentName()); // order not flipped at start
        assertEquals("P1", s.getNextName());
        assertEquals(MoveResult.REVERSED, m.playCard("P0", 0)); // Reverse on Reverse
    }

    public void testReverseAsLastCardWins()
    {
        Card[] p0 = {num(CardColor.BLUE, 1), num(CardColor.BLUE, 2),
            num(CardColor.BLUE, 3), num(CardColor.BLUE, 4), rev(CardColor.BLUE)};
        Card[] other = {num(CardColor.YELLOW, 0), num(CardColor.YELLOW, 1),
            num(CardColor.YELLOW, 2), num(CardColor.YELLOW, 3), num(CardColor.YELLOW, 4)};
        GameModel m = started(new Card[][] {p0, other, other.clone()},
            num(CardColor.BLUE, 9),
            num(CardColor.GREEN, 0), num(CardColor.GREEN, 1), num(CardColor.GREEN, 2),
            num(CardColor.GREEN, 3), num(CardColor.GREEN, 4), num(CardColor.GREEN, 5),
            num(CardColor.GREEN, 6), num(CardColor.GREEN, 7));
        for (int turn = 0; turn < 4; turn++)
        {
            assertEquals(MoveResult.PLAYED, m.playCard("P0", 0));
            m.drawCard("P1");
            m.drawCard("P2");
        }
        assertEquals(MoveResult.WON, m.playCard("P0", 0));
        assertFalse(m.isActive());
        assertEquals("P0", m.getWinnerName());
        GameSnapshot s = rec.snaps.get(rec.snaps.size() - 1);
        assertEquals("P0", s.getWinnerName());
        assertEquals("P1", s.getNextName()); // order not flipped
        assertNotNull(s.getTopCard());
        assertEquals(0, s.getCurrentHand().size());
    }

    // ---- bad-input cases ----

    public void testReverseOfWrongColorIsIllegal()
    {
        GameModel m = started(new Card[][] {hand(rev(CardColor.GREEN)), filler(),
            filler()}, num(CardColor.RED, 5));
        int before = rec.snaps.size();
        assertEquals(MoveResult.ILLEGAL_PLAY, m.playCard("P0", 0));
        assertEquals("P0", m.getSnapshot().getCurrentName());
        assertEquals("P1", m.getSnapshot().getNextName());
        assertEquals(5, m.getSnapshot().getCurrentHand().size());
        assertEquals(before, rec.snaps.size()); // no notification
    }

    public void testReverseOutOfTurnIsRejected()
    {
        GameModel m = started(new Card[][] {filler(), hand(rev(CardColor.RED)),
            filler()}, num(CardColor.RED, 5));
        int before = rec.snaps.size();
        assertEquals(MoveResult.NOT_YOUR_TURN, m.playCard("P1", 0));
        assertEquals("P0", m.getSnapshot().getCurrentName());
        assertEquals("P1", m.getSnapshot().getNextName());
        assertEquals(before, rec.snaps.size());
    }

    public void testReverseAfterGameOverIsNotActive()
    {
        GameModel m = new GameModel(Deck.standard(new Random(1)));
        m.addPlayer("A");
        m.addPlayer("B");
        assertEquals(MoveResult.GAME_NOT_ACTIVE, m.playCard("A", 0));
    }

    public void testDrawWithEveryPileEmptyPassesTurn()
    {
        GameModel m = started(new Card[][] {filler(), filler()}, num(CardColor.RED, 5));
        int before = rec.snaps.size();
        assertEquals(MoveResult.NOTHING_TO_DRAW, m.drawCard("P0"));
        assertEquals("P1", m.getSnapshot().getCurrentName());
        assertEquals(before + 1, rec.snaps.size());
    }
}
