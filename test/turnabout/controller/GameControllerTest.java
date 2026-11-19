package turnabout.controller;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import student.TestCase;
import turnabout.model.Card;
import turnabout.model.CardColor;
import turnabout.model.Deck;
import turnabout.model.GameModel;
import turnabout.model.MoveResult;
import turnabout.view.ConsoleView;

/**
 * Tests for GameController: the pure helpers, and full scripted runs through
 * a real ConsoleView reading a fixed string, with a fixed deck (no randomness).
 */
public class GameControllerTest extends TestCase {
    private ByteArrayOutputStream buffer;

    private static Card num(CardColor c, int n) {
        return new Card(c, n);
    }

    /** Ana holds RED 1-5, Ben BLUE 1-5; then the start card, then extras. */
    private static Deck deck(Card start, Card... extras) {
        List<Card> order = new ArrayList<>();
        for (int n = 1; n <= 5; n++) {
            order.add(num(CardColor.RED, n));
            order.add(num(CardColor.BLUE, n));
        }
        order.add(start);
        for (Card e : extras) {
            order.add(e);
        }
        return new Deck(order, new Random(1));
    }

    /** Runs the controller on the typed script; returns everything printed. */
    private String run(Deck d, String script) {
        buffer = new ByteArrayOutputStream();
        ConsoleView view = new ConsoleView(new Scanner(script), new PrintStream(buffer));
        GameModel model = new GameModel(d);
        model.addListener(view);
        new GameController(model, view).run();
        return buffer.toString();
    }

    private static int count(String text, String part) {
        int n = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + 1)) {
            n++;
        }
        return n;
    }

    // ---- helpers ----

    public void testParseNumberValid() {
        assertEquals(3, GameController.parseNumber("3"));
        assertEquals(12, GameController.parseNumber("  12 "));
    }

    public void testParseNumberBadInput() {
        for (String bad : new String[] {"", "   ", "abc", "2x", "1.5", null}) {
            try {
                GameController.parseNumber(bad);
                fail("should reject: " + bad);
            } catch (NumberFormatException e) {
                // expected
            }
        }
    }

    public void testMessageForEveryResult() {
        for (MoveResult r : MoveResult.values()) {
            if (r == MoveResult.WON) {
                assertNull(GameController.messageFor(r));
            } else {
                assertNotNull("no message for " + r, GameController.messageFor(r));
            }
        }
        assertEquals("Direction reversed!", GameController.messageFor(MoveResult.REVERSED));
    }

    public void testNullArgumentsRejected() {
        try {
            new GameController(null, null);
            fail("null");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ---- scripted runs ----

    public void testWinPrintsWinnerOnce() {
        // Ben draws a green each turn; Ana plays all five reds and wins.
        Deck d = deck(num(CardColor.RED, 9), num(CardColor.GREEN, 0), num(CardColor.GREEN, 1),
                num(CardColor.GREEN, 2), num(CardColor.GREEN, 3));
        String out = run(d, "2\nAna\nBen\n1\nd\n1\ndraw\n1\nD\n1\nd\n1\n");
        assertEquals(1, count(out, "Ana wins!"));
        assertEquals(4, count(out, "You drew a card."));
    }

    public void testBadInputRePrompts() {
        // bad count (word, 1, 7), bad names (blank, duplicate, too long), bad moves, then EOF
        String longName = "123456789012345678901";
        Deck d = deck(num(CardColor.RED, 9));
        String out = run(d, "two\n1\n7\n2\nAna\n\nana\n" + longName + "\nBen\n"
                + "\nabc\n9\n0\n-1\n");
        assertTrue(out.contains("Please enter a number from 2 to 6."));
        assertTrue(out.contains("Name cannot be blank"));
        assertTrue(out.contains("already taken"));
        assertTrue(out.contains("at most 20"));
        assertTrue(out.contains("Please type a card number, or D to draw."));
        assertTrue(out.contains("No card with that number in your hand."));
        assertTrue(out.contains("Input closed."));
        assertTrue(out.contains("Turn: Ana")); // still Ana's turn, no crash
        assertFalse(out.contains("Turn: Ben"));
    }

    public void testIllegalPlayKeepsSamePlayer() {
        // top is GREEN 9: nothing in Ana's red hand matches
        String out = run(deck(num(CardColor.GREEN, 9)), "2\nAna\nBen\n1\n");
        assertTrue(out.contains("That card must match"));
        assertFalse(out.contains("Turn: Ben"));
    }

    public void testEndOfInputBeforeStartEndsCleanly() {
        assertTrue(run(deck(num(CardColor.RED, 9)), "").contains("Input closed."));
        assertTrue(run(deck(num(CardColor.RED, 9)), "2\nAna\n").contains("Input closed."));
    }

    public void testReverseMessageAndOrder() {
        // 3 players: Ana plays her Reverse, so Cy (not Ben) is next.
        List<Card> order = new ArrayList<>();
        Card[][] hands = {
            {Card.reverse(CardColor.RED), num(CardColor.RED, 1), num(CardColor.RED, 2),
                num(CardColor.RED, 3), num(CardColor.RED, 4)},
            {num(CardColor.BLUE, 1), num(CardColor.BLUE, 2), num(CardColor.BLUE, 3),
                num(CardColor.BLUE, 4), num(CardColor.BLUE, 5)},
            {num(CardColor.GREEN, 1), num(CardColor.GREEN, 2), num(CardColor.GREEN, 3),
                num(CardColor.GREEN, 4), num(CardColor.GREEN, 5)}};
        for (int r = 0; r < 5; r++) {
            for (Card[] h : hands) {
                order.add(h[r]);
            }
        }
        order.add(num(CardColor.RED, 9));
        String out = run(new Deck(order, new Random(1)), "3\nAna\nBen\nCy\n1\n");
        assertTrue(out, out.contains("Direction reversed!"));
        assertTrue(out.contains("Turn: Cy"));
        assertTrue(out.contains("Up next: Ben"));
    }
}
