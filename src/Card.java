package turnabout.model;

/**
 * One immutable playing card with a color and a number from 0 to 9.
 *
 * @author Ruhaan Singh
 * @version Nov 2026
 */
public class Card
{
    private final CardColor color;
    private final int number;

    /**
     * Creates a card.
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
     * @return the card's number
     */
    public int getNumber()
    {
        return number;
    }

    /**
     * Checks whether this card can be played on another card. Two cards
     * match if they have the same color or the same number.
     *
     * @param other the card to compare against
     * @return true if the cards match, false if not or if other is null
     */
    public boolean matches(Card other)
    {
        throw new UnsupportedOperationException("Not written yet");
    }

    /**
     * Returns readable text for this card, such as "RED 7".
     *
     * @return the card as text
     */
    @Override
    public String toString()
    {
        throw new UnsupportedOperationException("Not written yet");
    }
}
