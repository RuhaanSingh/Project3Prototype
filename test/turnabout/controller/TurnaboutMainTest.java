package turnabout.controller;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Random;
import java.util.Scanner;
import student.TestCase;

/** Smoke test: the real random deck plays through setup, bad input, a draw, and Ctrl+D. */
public class TurnaboutMainTest extends TestCase {
    public void testPlayEndsCleanlyWhenInputCloses() {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        TurnaboutMain.play(new Scanner("2\nAna\nBen\nx\nd\n"), new PrintStream(buffer), new Random(3));
        String out = buffer.toString();
        assertTrue(out.contains("Please type a card number, or D to draw."));
        assertTrue(out.contains("You drew a card."));
        assertTrue(out.contains("Goodbye!"));
    }
}
