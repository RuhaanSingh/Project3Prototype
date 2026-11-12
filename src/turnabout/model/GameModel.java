package turnabout.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns the rules, the turn order, the deck, and the players, and decides
 * whether a move is legal. Has no printing or input code and no reference
 * to any view: it only talks to GameListener.
 *
 * Turn order: {@code current} is the player whose turn it is; {@code waiting}
 * holds everyone else in order. Passing the turn = enqueue current at the back,
 * then dequeue the front player into current. So waiting.peek() is always next.
 */
public class GameModel {
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 6;
    public static final int MAX_NAME_LENGTH = 20;
    public static final int HAND_SIZE = 5;

    private final Deck deck;
    private final List<Player> players = new ArrayList<>();      // setup checks only
    private final LinkedQueue<Player> waiting = new LinkedQueue<>();
    private final List<GameListener> listeners = new ArrayList<>();
    private Player current;        // null until startGame()
    private boolean active;
    private String winnerName;     // null until someone wins

    /** @throws IllegalArgumentException if deck is null */
    public GameModel(Deck deck) {
        if (deck == null) {
            throw new IllegalArgumentException("Deck cannot be null");
        }
        this.deck = deck;
    }

    /** Registers a view (or a test) to be told after every change. */
    public void addListener(GameListener l) {
        if (l == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners.add(l);
    }

    /**
     * Trims the name and adds the player.
     * @throws IllegalStateException if the game has started or there are already 6 players
     * @throws IllegalArgumentException if the name is null, blank, over 20 characters,
     *         or already taken (ignoring case)
     */
    public void addPlayer(String name) {
        if (hasStarted()) {
            throw new IllegalStateException("Cannot add players after the game has started");
        }
        if (players.size() >= MAX_PLAYERS) {
            throw new IllegalStateException("At most " + MAX_PLAYERS + " players");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Name must be at most " + MAX_NAME_LENGTH + " characters");
        }
        for (Player p : players) {
            if (p.getName().equalsIgnoreCase(trimmed)) {
                throw new IllegalArgumentException("Name \"" + trimmed + "\" is already taken");
            }
        }
        players.add(new Player(trimmed));
    }

    /**
     * Deals HAND_SIZE cards to each player, flips the starting card, gives the
     * first turn to the first player added, and notifies listeners.
     * @throws IllegalStateException if already started or fewer than 2 players
     */
    public void startGame() {
        if (hasStarted()) {
            throw new IllegalStateException("The game has already started");
        }
        if (players.size() < MIN_PLAYERS) {
            throw new IllegalStateException("Need at least " + MIN_PLAYERS + " players");
        }
        for (int round = 0; round < HAND_SIZE; round++) {   // deal one at a time, like a real table
            for (Player p : players) {
                p.addCard(deck.draw());
            }
        }
        deck.discard(deck.draw());                         // starting card
        current = players.get(0);
        for (int i = 1; i < players.size(); i++) {
            waiting.enqueue(players.get(i));
        }
        active = true;
        notifyListeners();
    }

    /**
     * Plays the card at cardIndex (0-based) from playerName's hand.
     * Checks, in order: GAME_NOT_ACTIVE, NOT_YOUR_TURN, BAD_INDEX, ILLEGAL_PLAY.
     * A rejected move changes nothing and sends no notification.
     * @return PLAYED, WON, or the reason it was rejected
     */
    public MoveResult playCard(String playerName, int cardIndex) {
        if (!active) {
            return MoveResult.GAME_NOT_ACTIVE;
        }
        if (!isCurrentPlayer(playerName)) {
            return MoveResult.NOT_YOUR_TURN;
        }
        List<Card> hand = current.getHand();
        if (cardIndex < 0 || cardIndex >= hand.size()) {
            return MoveResult.BAD_INDEX;
        }
        if (!hand.get(cardIndex).matches(deck.topCard())) {
            return MoveResult.ILLEGAL_PLAY;
        }

        deck.discard(current.removeCard(cardIndex));
        if (current.getHand().isEmpty()) {
            winnerName = current.getName();
            active = false;
            notifyListeners();
            return MoveResult.WON;
        }
        advanceTurn();
        notifyListeners();
        return MoveResult.PLAYED;
    }

    /**
     * playerName draws one card and the turn passes.
     * @return DREW; NOTHING_TO_DRAW if no cards are left (the turn still passes);
     *         or GAME_NOT_ACTIVE / NOT_YOUR_TURN, in which case nothing changes
     */
    public MoveResult drawCard(String playerName) {
        if (!active) {
            return MoveResult.GAME_NOT_ACTIVE;
        }
        if (!isCurrentPlayer(playerName)) {
            return MoveResult.NOT_YOUR_TURN;
        }
        Card drawn = deck.draw();
        if (drawn != null) {
            current.addCard(drawn);
        }
        advanceTurn();
        notifyListeners();
        return drawn != null ? MoveResult.DREW : MoveResult.NOTHING_TO_DRAW;
    }

    /**
     * A read-only copy of the current state.
     * @throws IllegalStateException if the game has not started
     */
    public GameSnapshot getSnapshot() {
        if (!hasStarted()) {
            throw new IllegalStateException("The game has not started");
        }
        return new GameSnapshot(current.getName(), waiting.peek().getName(),
                deck.topCard(), current.getHand(), winnerName);
    }

    /** True between startGame() and a win. */
    public boolean isActive() {
        return active;
    }

    // ---- private helpers ----

    private boolean hasStarted() {
        return current != null;
    }

    private boolean isCurrentPlayer(String playerName) {
        return playerName != null && playerName.equals(current.getName());
    }

    /** current goes to the back of the line; the front of the line becomes current. */
    private void advanceTurn() {
        waiting.enqueue(current);
        current = waiting.dequeue();
    }

    private void notifyListeners() {
        GameSnapshot snapshot = getSnapshot();
        for (GameListener l : new ArrayList<>(listeners)) {
            l.gameChanged(snapshot);
        }
    }
}
