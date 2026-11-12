package turnabout.model;

/** Anything that wants to hear about game state changes. The model calls it after every change. */
public interface GameListener {
    /**
     * Called after the game state changes.
     * @param snapshot a read-only copy of the new state
     */
    void gameChanged(GameSnapshot snapshot);
}
