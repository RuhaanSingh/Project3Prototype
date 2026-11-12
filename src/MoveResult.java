package turnabout.model;

/** The model's answer to a move. The controller turns each value into a message. */
public enum MoveResult {
    /** A card was played and the turn passed. */
    PLAYED,
    /** A card was played and that player's hand is now empty. The game is over. */
    WON,
    /** The player drew a card and the turn passed. */
    DREW,
    /** No cards were left to draw anywhere; the turn still passed so the game can't stall. */
    NOTHING_TO_DRAW,
    /** Rejected: the named player is not the current player. Nothing changed. */
    NOT_YOUR_TURN,
    /** Rejected: the card does not match the top card by color or number. Nothing changed. */
    ILLEGAL_PLAY,
    /** Rejected: no card at that index in the hand. Nothing changed. */
    BAD_INDEX,
    /** Rejected: the game has not started or is already over. Nothing changed. */
    GAME_NOT_ACTIVE
}
