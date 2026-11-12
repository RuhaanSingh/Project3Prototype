package turnabout.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Holds the draw pile and discard pile, shuffles, draws, and reshuffles the
 * discard pile when the draw pile runs out.
 * Both piles keep their "top" card at the END of the list, so drawing and
 * discarding are O(1).
 */
public class Deck {
    private final List<Card> drawPile = new ArrayList<>();
    private final List<Card> discardPile = new ArrayList<>();
    private final Random rng;

    /**
     * Builds the standard 40-card deck (4 colors x numbers 0-9) and shuffles it.
     * @throws IllegalArgumentException if rng is null
     */
    public Deck(Random rng) {
        if (rng == null) {
            throw new IllegalArgumentException("Random cannot be null");
        }
        this.rng = rng;
        for (CardColor color : CardColor.values()) {
            for (int n = 0; n <= 9; n++) {
                drawPile.add(new Card(color, n));
            }
        }
        Collections.shuffle(drawPile, rng);
    }

    /**
     * A deck with a known order, for tests: the first card in the list is drawn first.
     * Not shuffled. rng is still used if the discard pile is ever reshuffled.
     * @throws IllegalArgumentException if either argument is null or the list is empty
     */
    public Deck(List<Card> cardsInDrawOrder, Random rng) {
        if (cardsInDrawOrder == null || cardsInDrawOrder.isEmpty()) {
            throw new IllegalArgumentException("Card list cannot be null or empty");
        }
        if (rng == null) {
            throw new IllegalArgumentException("Random cannot be null");
        }
        this.rng = rng;
        for (int i = cardsInDrawOrder.size() - 1; i >= 0; i--) { // reversed: top = end
            Card c = cardsInDrawOrder.get(i);
            if (c == null) {
                throw new IllegalArgumentException("Card list cannot contain null");
            }
            drawPile.add(c);
        }
    }

    /**
     * Draws the top card. If the draw pile is empty, first shuffles every discard
     * except its top card back into the draw pile.
     * @return the card, or null if there is nothing left to draw at all
     */
    public Card draw() {
        if (drawPile.isEmpty()) {
            reshuffleDiscards();
        }
        if (drawPile.isEmpty()) {
            return null;
        }
        return drawPile.remove(drawPile.size() - 1);
    }

    /** @throws IllegalArgumentException if c is null */
    public void discard(Card c) {
        if (c == null) {
            throw new IllegalArgumentException("Cannot discard a null card");
        }
        discardPile.add(c);
    }

    /** The card on top of the discard pile, or null if nothing has been discarded. */
    public Card topCard() {
        return discardPile.isEmpty() ? null : discardPile.get(discardPile.size() - 1);
    }

    private void reshuffleDiscards() {
        if (discardPile.size() <= 1) {
            return; // only the top card (or nothing) is left; it must stay visible
        }
        Card top = discardPile.remove(discardPile.size() - 1);
        drawPile.addAll(discardPile);
        discardPile.clear();
        discardPile.add(top);
        Collections.shuffle(drawPile, rng);
    }
}
