package turnabout.model;

/**
 * One immutable playing card. A card is either a number card (a color and a
 * number from 0 to 9) or a Reverse card (a color and no number).
 *
 * @author Ruhaan Singh
 * @version Nov 2026
 */
public class Card
{
    /** The value getNumber() returns for a Reverse card. */
    private static final int REVERSE_NUMBER = -1;

    private final CardColor color;
    private final int number;
    private final boolean reverse;

    /**
     * Creates a number card.
     *
     * @param color the card's color
     * @param number the card's number, 0 to 9
     * @throws IllegalArgumentException if color is null or number is not
     *         0 to 9
     */
    public Card(CardColor color, int number)
    {
        if (color == null)
        {
            throw new IllegalArgumentException("Color must not be null");
        }
        if (number < 0 || number > 9)
        {
            throw new IllegalArgumentException("Number must be 0 to 9");
        }
        this.color = color;
        this.number = number;
        this.reverse = false;
    }

    /**
     * Builds a Reverse card. Only reached through the reverse factory,
     * which has already checked the color.
     *
     * @param color the card's color, not null
     */
    private Card(CardColor color)
    {
        this.color = color;
        this.number = REVERSE_NUMBER;
        this.reverse = true;
    }

    /**
     * Creates a Reverse card of the given color.
     *
     * @param color the card's color
     * @return a new Reverse card
     * @throws IllegalArgumentException if color is null
     */
    public static Card reverse(CardColor color)
    {
        if (color == null)
        {
            throw new IllegalArgumentException("Color must not be null");
        }
        return new Card(color);
    }

    /**
     * Gets the color of this card.
     *
     * @return the card's color
     */
    public CardColor getColor()
    {
        return color;
    }

    /**
     * Gets the number on this card.
     *
     * @return the card's number, or -1 for a Reverse card
     */
    public int getNumber()
    {
        return number;
    }

    /**
     * Tells whether this is a Reverse card.
     *
     * @return true for a Reverse card, false for a number card
     */
    public boolean isReverse()
    {
        return reverse;
    }

    /**
     * Checks whether this card can be played on another card. Cards match
     * if they have the same color. Two Reverse cards always match. Two
     * number cards also match if they have the same number. A Reverse card
     * and a number card of different colors do not match.
     *
     * @param other the card to compare against
     * @return true if the cards match, false if not or if other is null
     */
    public boolean matches(Card other)
    {
        if (other == null)
        {
            return false;
        }
        if (color == other.color)
        {
            return true;
        }
        if (reverse || other.reverse)
        {
            return reverse && other.reverse;
        }
        return number == other.number;
    }

    /**
     * Returns readable text for this card, such as "RED 7" or
     * "RED REVERSE".
     *
     * @return the card as text
     */
    @Override
    public String toString()
    {
        if (reverse)
        {
            return color + " REVERSE";
        }
        return color + " " + number;
    }
}
