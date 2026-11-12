package turnabout.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A read-only copy of exactly what the view is allowed to see. The model
 * builds one after every state change and hands it to each listener.
 *
 * @author Ruhaan Singh
 * @version Nov 2026
 */
public class GameSnapshot
{
    private final String currentName;
    private final String nextName;
    private final Card topCard;
    private final List<Card> currentHand;
    private final String winnerName;

    /**
     * Creates a snapshot. The hand is copied so later changes to the
     * original list do not show up here.
     *
     * @param currentName the name of the player whose turn it is
     * @param nextName the name of the player who is up next
     * @param topCard the top card of the discard pile
     * @param hand the current player's cards
     * @param winnerName the winner's name, or null if there is no winner
     * @throws IllegalArgumentException if currentName, nextName, topCard,
     *         or hand is null
     */
    public GameSnapshot(String currentName, String nextName, Card topCard,
        List<Card> hand, String winnerName)
    {
        if (currentName == null || nextName == null || topCard == null
            || hand == null)
        {
            throw new IllegalArgumentException(
                "Snapshot values must not be null");
        }
        this.currentName = currentName;
        this.nextName = nextName;
        this.topCard = topCard;
        this.currentHand = Collections.unmodifiableList(
            new ArrayList<Card>(hand));
        this.winnerName = winnerName;
    }

    /**
     * Gets the name of the player whose turn it is.
     *
     * @return the current player's name
     */
    public String getCurrentName()
    {
        return currentName;
    }

    /**
     * Gets the name of the player who is up next.
     *
     * @return the next player's name
     */
    public String getNextName()
    {
        return nextName;
    }

    /**
     * Gets the top card of the discard pile.
     *
     * @return the top card
     */
    public Card getTopCard()
    {
        return topCard;
    }

    /**
     * Gets the current player's hand. The list cannot be changed.
     *
     * @return an unmodifiable list of the current player's cards
     */
    public List<Card> getCurrentHand()
    {
        throw new UnsupportedOperationException("Not written yet");
    }

    /**
     * Gets the winner's name.
     *
     * @return the winner's name, or null if nobody has won yet
     */
    public String getWinnerName()
    {
        throw new UnsupportedOperationException("Not written yet");
    }
}
