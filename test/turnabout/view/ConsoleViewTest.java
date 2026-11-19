package turnabout.view;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import student.TestCase;
import turnabout.model.Card;
import turnabout.model.CardColor;
import turnabout.model.GameSnapshot;

/**
 * Tests for ConsoleView using a Scanner on a fixed string and a PrintStream
 * over a byte buffer, so nothing touches the real console.
 *
 * @author Ruhaan Singh
 * @version Nov 2026
 */
public class ConsoleViewTest extends TestCase
{
    private ByteArrayOutputStream buffer;
    private PrintStream printer;

    /**
     * Makes a fresh output buffer.
     */
    public void setUp()
    {
        buffer = new ByteArrayOutputStream();
        printer = new PrintStream(buffer, true);
    }

    /**
     * Makes a view that reads the given text.
     */
    private ConsoleView viewReading(String text)
    {
        return new ConsoleView(new Scanner(text), printer);
    }

    /**
     * Everything printed so far, with line endings made the same.
     */
    private String printed()
    {
        printer.flush();
        return buffer.toString().replace("\r\n", "\n");
    }

    // ---- constructor ----

    /**
     * A view can be built with real arguments.
     */
    public void testConstructor()
    {
        assertNotNull(viewReading(""));
    }

    /**
     * Null input or output throws IllegalArgumentException.
     */
    public void testConstructorNulls()
    {
        Exception thrown = null;
        try
        {
            new ConsoleView(null, printer);
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertTrue(thrown instanceof IllegalArgumentException);

        thrown = null;
        try
        {
            new ConsoleView(new Scanner(""), null);
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertTrue(thrown instanceof IllegalArgumentException);
    }

    // ---- gameChanged ----

    /**
     * A normal board shows the turn, who is next, the top card, and the
     * hand numbered from 1, including Reverse cards.
     */
    public void testGameChangedNormal()
    {
        List<Card> hand = new ArrayList<Card>();
        hand.add(new Card(CardColor.RED, 3));
        hand.add(Card.reverse(CardColor.BLUE));
        viewReading("").gameChanged(new GameSnapshot("Ann", "Ben",
            Card.reverse(CardColor.GREEN), hand, null));
        String out = printed();
        assertTrue(out.contains("Turn: Ann\n"));
        assertTrue(out.contains("Up next: Ben\n"));
        assertTrue(out.contains("Top card: GREEN REVERSE\n"));
        assertTrue(out.contains("  1. RED 3\n"));
        assertTrue(out.contains("  2. BLUE REVERSE\n"));
        assertFalse(out.contains("wins"));
    }

    /**
     * A null snapshot prints nothing and does not throw.
     */
    public void testGameChangedNull()
    {
        viewReading("").gameChanged(null);
        assertEquals("", printed());
    }

    /**
     * A winner snapshot prints the winner and top card, not the hand.
     */
    public void testGameChangedWinner()
    {
        List<Card> hand = new ArrayList<Card>();
        hand.add(new Card(CardColor.RED, 3));
        viewReading("").gameChanged(new GameSnapshot("Ann", "Ben",
            Card.reverse(CardColor.RED), hand, "Ann"));
        String out = printed();
        assertTrue(out.contains("Ann wins!\n"));
        assertTrue(out.contains("Top card: RED REVERSE\n"));
        assertFalse(out.contains("Your hand"));
        assertFalse(out.contains("RED 3"));
    }

    /**
     * An empty hand prints the heading and no numbered lines.
     */
    public void testGameChangedEmptyHand()
    {
        viewReading("").gameChanged(new GameSnapshot("Ann", "Ben",
            new Card(CardColor.RED, 1), new ArrayList<Card>(), null));
        String out = printed();
        assertTrue(out.contains("Your hand:\n"));
        assertFalse(out.contains("1. "));
    }

    /**
     * A very long name is printed in full.
     */
    public void testGameChangedLongName()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++)
        {
            sb.append('x');
        }
        String longName = sb.toString();
        viewReading("").gameChanged(new GameSnapshot(longName, longName,
            new Card(CardColor.RED, 1), new ArrayList<Card>(), null));
        assertTrue(printed().contains("Turn: " + longName + "\n"));
    }

    // ---- showMessage ----

    /**
     * A message is printed on its own line.
     */
    public void testShowMessage()
    {
        viewReading("").showMessage("Card played.");
        assertEquals("Card played.\n", printed());
    }

    /**
     * A null message prints nothing.
     */
    public void testShowMessageNull()
    {
        viewReading("").showMessage(null);
        assertEquals("", printed());
    }

    // ---- readLine ----

    /**
     * readLine prints the prompt and returns each typed line in order.
     */
    public void testReadLine()
    {
        ConsoleView view = viewReading("3\nD\n");
        assertEquals("3", view.readLine("> "));
        assertEquals("D", view.readLine("> "));
        assertEquals("> > ", printed());
    }

    /**
     * readLine returns null once the input has ended, and keeps doing so.
     */
    public void testReadLineEndOfInput()
    {
        ConsoleView view = viewReading("only\n");
        assertEquals("only", view.readLine("? "));
        assertNull(view.readLine("? "));
        assertNull(view.readLine("? "));
        assertNull(viewReading("").readLine("? "));
    }
}
