package turnabout.controller;

import turnabout.model.GameModel;
import turnabout.model.MoveResult;
import turnabout.view.ConsoleView;

/**
 * Turns typed text into model calls and model answers into messages.
 * Holds no game state.
 */
public class GameController {
    private final GameModel model;
    private final ConsoleView view;

    /** @throws IllegalArgumentException if model or view is null */
    public GameController(GameModel model, ConsoleView view) {
        if (model == null || view == null) {
            throw new IllegalArgumentException("Model and view cannot be null");
        }
        this.model = model;
        this.view = view;
    }

    /** Runs setup, then the turn loop. Returns when someone wins or input closes. */
    public void run() {
        if (!setup()) {
            view.showMessage("Input closed.");
            return;
        }
        while (model.isActive()) {
            String name = model.getSnapshot().getCurrentName();
            String line = view.readLine(name + ", enter a card number, or D to draw: ");
            if (line == null) {
                view.showMessage("Input closed.");
                return;
            }
            line = line.trim();
            if (line.equalsIgnoreCase("D") || line.equalsIgnoreCase("draw")) {
                show(model.drawCard(name));
                continue;
            }
            int number;
            try {
                number = parseNumber(line);
            } catch (NumberFormatException e) {
                view.showMessage("Please type a card number, or D to draw.");
                continue;
            }
            show(model.playCard(name, number - 1)); // screen is 1-based, model is 0-based
        }
    }

    /** @return false if input closed before the game could start */
    private boolean setup() {
        int count = 0;
        while (!GameModel.isValidPlayerCount(count)) {
            String line = view.readLine("Number of players (" + GameModel.MIN_PLAYERS
                    + "-" + GameModel.MAX_PLAYERS + "): ");
            if (line == null) {
                return false;
            }
            try {
                count = parseNumber(line);
            } catch (NumberFormatException e) {
                count = 0;
            }
            if (!GameModel.isValidPlayerCount(count)) {
                view.showMessage("Please enter a number from " + GameModel.MIN_PLAYERS
                        + " to " + GameModel.MAX_PLAYERS + ".");
            }
        }
        while (model.getPlayerCount() < count) {
            String name = view.readLine("Name for player " + (model.getPlayerCount() + 1) + ": ");
            if (name == null) {
                return false;
            }
            try {
                model.addPlayer(name);
            } catch (IllegalArgumentException e) {
                view.showMessage(e.getMessage());
            }
        }
        model.startGame();
        return true;
    }

    private void show(MoveResult result) {
        String text = messageFor(result);
        if (text != null) {
            view.showMessage(text);
        }
    }

    /**
     * Parses a typed number.
     * @throws NumberFormatException if text is null, blank, or not a whole number
     */
    static int parseNumber(String text) {
        if (text == null) {
            throw new NumberFormatException("No input");
        }
        return Integer.parseInt(text.trim());
    }

    /** The message for a move result, or null for WON (the view already prints "X wins!"). */
    static String messageFor(MoveResult result) {
        switch (result) {
            case PLAYED:
                return "Card played.";
            case REVERSED:
                return "Direction reversed!";
            case DREW:
                return "You drew a card.";
            case NOTHING_TO_DRAW:
                return "No cards left to draw. Turn passes.";
            case NOT_YOUR_TURN:
                return "It is not your turn.";
            case ILLEGAL_PLAY:
                return "That card must match the top card by color or number (a Reverse matches a Reverse).";
            case BAD_INDEX:
                return "No card with that number in your hand.";
            case GAME_NOT_ACTIVE:
                return "The game is not active.";
            default: // WON
                return null;
        }
    }
}
