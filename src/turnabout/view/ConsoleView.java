package turnabout.view;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import turnabout.model.Card;
import turnabout.model.GameListener;
import turnabout.model.GameSnapshot;

/**
 * The console view for Turnabout. Prints the board and messages and reads
 * what the player types. It only knows about GameSnapshot, never the model.
 *
 * @author Ruhaan Singh
 * @version Nov 2026
 */
public class ConsoleView implements GameListener
{
    private final Scanner in;
    private final PrintStream out;

    /**
     * Creates a console view.
     *
     * @param in where typed text comes from
     * @param out where text is printed
     * @throws IllegalArgumentException if in or out is null
     */
    public ConsoleView(Scanner in, PrintStream out)
    {
        if (in == null || out == null)
        {
            throw new IllegalArgumentException(
                "Input and output must not be null");
        }
        this.in = in;
        this.out = out;
    }

    /**
     * Prints the board for a new game state: whose turn it is, who is up
     * next, the top card, and the current hand numbered from 1. If there
     * is a winner, prints that instead of the hand.
     *
     * @param snapshot the new state, ignored if null
     */
    @Override
    public void gameChanged(GameSnapshot snapshot)
    {
        if (snapshot == null)
        {
            return;
        }
        out.println();
        out.println("------------------------------");
        if (snapshot.getWinnerName() != null)
        {
            out.println(snapshot.getWinnerName() + " wins!");
            out.println("Top card: " + snapshot.getTopCard());
            return;
        }
        out.println("Turn: " + snapshot.getCurrentName());
        out.println("Up next: " + snapshot.getNextName());
        out.println("Top card: " + snapshot.getTopCard());
        out.println();
        out.println("Your hand:");
        List<Card> hand = snapshot.getCurrentHand();
        for (int i = 0; i < hand.size(); i++)
        {
            out.println("  " + (i + 1) + ". " + hand.get(i));
        }
    }

    /**
     * Prints a message on its own line.
     *
     * @param text the message, nothing is printed if null
     */
    public void showMessage(String text)
    {
        if (text == null)
        {
            return;
        }
        out.println(text);
    }

    /**
     * Prints a prompt and returns what the player typed.
     *
     * @param prompt the text to show before waiting for input
     * @return the line that was typed, or null if the input has closed
     */
    public String readLine(String prompt)
    {
        out.print(prompt);
        out.flush();
        if (!in.hasNextLine())
        {
            return null;
        }
        return in.nextLine();
    }
}
