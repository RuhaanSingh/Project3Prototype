package turnabout.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Holds one player's name and hand. Name rules are checked by GameModel. */
public class Player {
    private final String name;
    private final List<Card> hand = new ArrayList<>();

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /** Read-only view of the hand; only Player's own methods can change it. */
    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    /** @throws IllegalArgumentException if c is null */
    public void addCard(Card c) {
        if (c == null) {
            throw new IllegalArgumentException("Cannot add a null card");
        }
        hand.add(c);
    }

    /** @throws IndexOutOfBoundsException if index is outside the hand */
    public Card removeCard(int index) {
        if (index < 0 || index >= hand.size()) {
            throw new IndexOutOfBoundsException("No card at index " + index);
        }
        return hand.remove(index);
    }
}
