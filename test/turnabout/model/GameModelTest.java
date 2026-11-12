package turnabout.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import student.TestCase;

public class GameModelTest extends TestCase {
    /** Saves every snapshot it is sent. */
    private static class Recorder implements GameListener {
        final List<GameSnapshot> snaps = new ArrayList<>();

        public void gameChanged(GameSnapshot s) {
            snaps.add(s);
        }
    }

    private GameModel model;
    private Recorder rec;

    private static Card card(CardColor color, int n) {
        return new Card(color, n);
    }

    /**
     * Builds a model with Ana and Ben. The deal is round-robin, so Ana holds
     * RED 1-5 and Ben holds BLUE 1-5. startCard is the first pile card and
     * extras are drawn after it, first one first.
     */
    private GameModel game(Card startCard, Card... extras) {
        List<Card> order = new ArrayList<>();
        for (int n = 1; n <= 5; n++) {
            order.add(card(CardColor.RED, n));
            order.add(card(CardColor.BLUE, n));
        }
        order.add(startCard);
        for (Card e : extras) {
            order.add(e);
        }
        GameModel m = new GameModel(new Deck(order, new Random(1)));
        rec = new Recorder();
        m.addListener(rec);
        m.addPlayer("Ana");
        m.addPlayer("Ben");
        return m;
    }

    private GameModel started(Card startCard, Card... extras) {
        GameModel m = game(startCard, extras);
        m.startGame();
        return m;
    }

    // ---- construction and listeners ----

    public void testNullDeckAndListener() {
        try { new GameModel(null); fail("null deck"); } catch (IllegalArgumentException e) { }
        try { game(card(CardColor.RED, 9)).addListener(null); fail("null listener"); }
        catch (IllegalArgumentException e) { }
    }

    public void testEveryListenerGetsEverySnapshot() {
        GameModel m = game(card(CardColor.RED, 9), card(CardColor.GREEN, 0));
        Recorder second = new Recorder();
        m.addListener(second);
        m.startGame();
        m.drawCard("Ana");
        assertEquals(2, rec.snaps.size());
        assertEquals(2, second.snaps.size());
    }

    // ---- addPlayer: bad names ----

    public void testAddPlayerTrimsName() {
        model = game(card(CardColor.RED, 9));
        model.addPlayer("  Cy  ");
        assertEquals(3, model.getPlayerCount());
        try { model.addPlayer("cy"); fail("trimmed duplicate"); } catch (IllegalArgumentException e) { }
    }

    public void testAddPlayerRejectsBadNames() {
        model = game(card(CardColor.RED, 9));
        String[] bad = {null, "", "   ", "ana", "ANA", "123456789012345678901"};
        for (String name : bad) {
            try {
                model.addPlayer(name);
                fail("expected IllegalArgumentException for \"" + name + "\"");
            } catch (IllegalArgumentException e) {
                assertEquals(2, model.getPlayerCount());
            }
        }
    }

    public void testAddPlayerAccepts20Characters() {
        model = game(card(CardColor.RED, 9));
        model.addPlayer("12345678901234567890");
        assertEquals(3, model.getPlayerCount());
    }

    public void testSeventhPlayerRejected() {
        model = game(card(CardColor.RED, 9));
        for (String n : new String[] {"C", "D", "E", "F"}) {
            model.addPlayer(n);
        }
        assertEquals(GameModel.MAX_PLAYERS, model.getPlayerCount());
        try { model.addPlayer("G"); fail("7th player"); } catch (IllegalStateException e) { }
    }

    public void testAddPlayerAfterStartRejected() {
        model = started(card(CardColor.RED, 9));
        try { model.addPlayer("Cy"); fail("after start"); } catch (IllegalStateException e) { }
    }

    public void testIsValidPlayerCount() {
        assertFalse(GameModel.isValidPlayerCount(1));
        assertTrue(GameModel.isValidPlayerCount(2));
        assertTrue(GameModel.isValidPlayerCount(6));
        assertFalse(GameModel.isValidPlayerCount(7));
    }

    // ---- startGame and snapshot ----

    public void testStartGameDealsAndNotifiesOnce() {
        model = started(card(CardColor.RED, 9));
        assertTrue(model.isActive());
        assertEquals(1, rec.snaps.size());
        GameSnapshot s = rec.snaps.get(0);
        assertEquals("Ana", s.getCurrentName());
        assertEquals("Ben", s.getNextName());
        assertEquals("RED 9", s.getTopCard().toString());
        assertEquals(GameModel.HAND_SIZE, s.getCurrentHand().size());
        assertNull(s.getWinnerName());
    }

    public void testStartGameNeedsTwoPlayersAndOnlyOnce() {
        GameModel m = new GameModel(new Deck(new Random(1)));
        m.addPlayer("Ana");
        try { m.startGame(); fail("one player"); } catch (IllegalStateException e) { }
        model = started(card(CardColor.RED, 9));
        try { model.startGame(); fail("twice"); } catch (IllegalStateException e) { }
    }

    public void testSnapshotBeforeStartThrows() {
        model = game(card(CardColor.RED, 9));
        try { model.getSnapshot(); fail("not started"); } catch (IllegalStateException e) { }
    }

    public void testSnapshotMatchesModel() {
        model = started(card(CardColor.RED, 9));
        GameSnapshot s = model.getSnapshot();
        assertEquals("Ana", s.getCurrentName());
        assertEquals(5, s.getCurrentHand().size());
        assertEquals("RED 1", s.getCurrentHand().get(0).toString());
    }

    // ---- playCard ----

    public void testPlayMatchingCardPassesTurn() {
        model = started(card(CardColor.RED, 9));
        assertEquals(MoveResult.PLAYED, model.playCard("Ana", 0));
        assertEquals(2, rec.snaps.size());
        GameSnapshot s = rec.snaps.get(1);
        assertEquals("Ben", s.getCurrentName());
        assertEquals("Ana", s.getNextName());
        assertEquals("RED 1", s.getTopCard().toString());
        assertEquals(5, s.getCurrentHand().size());
        assertTrue(model.isActive());
    }

    public void testWrongTurnIsRejectedWithoutChange() {
        model = started(card(CardColor.RED, 9));
        assertEquals(MoveResult.NOT_YOUR_TURN, model.playCard("Ben", 0));
        assertEquals(MoveResult.NOT_YOUR_TURN, model.playCard(null, 0));
        assertEquals(MoveResult.NOT_YOUR_TURN, model.drawCard("Ben"));
        assertUnchanged();
    }

    public void testBadIndexIsRejectedWithoutChange() {
        model = started(card(CardColor.RED, 9));
        assertEquals(MoveResult.BAD_INDEX, model.playCard("Ana", -1));
        assertEquals(MoveResult.BAD_INDEX, model.playCard("Ana", 5));
        assertEquals(MoveResult.BAD_INDEX, model.playCard("Ana", 9));
        assertUnchanged();
    }

    public void testIllegalPlayIsRejectedWithoutChange() {
        model = started(card(CardColor.GREEN, 9)); // Ana holds only red 1-5
        assertEquals(MoveResult.ILLEGAL_PLAY, model.playCard("Ana", 0));
        assertUnchanged();
    }

    public void testTurnIsCheckedBeforeIndex() {
        model = started(card(CardColor.RED, 9));
        assertEquals(MoveResult.NOT_YOUR_TURN, model.playCard("Ben", 99));
    }

    private void assertUnchanged() {
        assertEquals(1, rec.snaps.size()); // only the start notification
        GameSnapshot s = model.getSnapshot();
        assertEquals("Ana", s.getCurrentName());
        assertEquals(5, s.getCurrentHand().size());
        assertTrue(model.isActive());
    }

    public void testMovesBeforeStartAreNotActive() {
        model = game(card(CardColor.RED, 9));
        assertFalse(model.isActive());
        assertEquals(MoveResult.GAME_NOT_ACTIVE, model.playCard("Ana", 0));
        assertEquals(MoveResult.GAME_NOT_ACTIVE, model.drawCard("Ana"));
        assertEquals(0, rec.snaps.size());
    }

    // ---- drawCard ----

    public void testDrawAddsCardAndPassesTurn() {
        model = started(card(CardColor.RED, 9), card(CardColor.GREEN, 0), card(CardColor.GREEN, 1));
        assertEquals(MoveResult.DREW, model.drawCard("Ana"));
        assertEquals(2, rec.snaps.size());
        assertEquals("Ben", model.getSnapshot().getCurrentName());
        model.drawCard("Ben");
        GameSnapshot s = model.getSnapshot();
        assertEquals("Ana", s.getCurrentName());
        assertEquals(6, s.getCurrentHand().size());
        assertEquals("GREEN 0", s.getCurrentHand().get(5).toString());
    }

    public void testDrawWithNothingLeftStillPassesTurn() {
        model = started(card(CardColor.RED, 9)); // no extras: nothing to draw
        assertEquals(MoveResult.NOTHING_TO_DRAW, model.drawCard("Ana"));
        assertEquals("Ben", model.getSnapshot().getCurrentName());
        assertEquals(2, rec.snaps.size());
    }

    // ---- winning ----

    public void testWinningEndsTheGame() {
        // Ana plays all five reds; Ben draws a spare green on each of his four turns.
        model = started(card(CardColor.RED, 9), card(CardColor.GREEN, 0), card(CardColor.GREEN, 1),
                card(CardColor.GREEN, 2), card(CardColor.GREEN, 3));
        for (int i = 0; i < 4; i++) {
            assertEquals(MoveResult.PLAYED, model.playCard("Ana", 0));
            assertEquals(MoveResult.DREW, model.drawCard("Ben"));
        }
        int before = rec.snaps.size();
        assertEquals(MoveResult.WON, model.playCard("Ana", 0));
        assertEquals(before + 1, rec.snaps.size());
        assertEquals("Ana", rec.snaps.get(before).getWinnerName());
        assertEquals("Ana", model.getWinnerName());
        assertTrue(model.isGameOver());
        assertFalse(model.isActive());
        assertEquals(MoveResult.GAME_NOT_ACTIVE, model.playCard("Ben", 0));
        assertEquals(MoveResult.GAME_NOT_ACTIVE, model.drawCard("Ben"));
        assertEquals(before + 1, rec.snaps.size());
    }

    public void testNoWinnerYet() {
        model = started(card(CardColor.RED, 9));
        assertNull(model.getWinnerName());
        assertFalse(model.isGameOver());
    }
}
