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
            view.showMessage("Game ended");
            return;
        }
        while (model.isActive()) {
            String name = model.getSnapshot().getCurrentName();
            String line = view.readLine(name + ", enter a card number, or D to draw: ");
            if (line == null) {
                view.showMessage("Game ended");
                return;
            }
            line = line.trim();
            if (line.equalsIgnoreCase("D")) {
                show(model.drawCard(name));
                continue;
            }
            int number;
            try {
                number = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                view.showMessage("Please type a card number or D.");
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
                count = Integer.parseInt(line.trim());
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
        switch (result) {
            case PLAYED:
                view.showMessage("Card played.");
                break;
            case DREW:
                view.showMessage("You drew a card.");
                break;
            case NOTHING_TO_DRAW:
                view.showMessage("No cards left to draw. Turn passes.");
                break;
            case NOT_YOUR_TURN:
                view.showMessage("It is not your turn.");
                break;
            case ILLEGAL_PLAY:
                view.showMessage("That card must match the top card by color or number.");
                break;
            case BAD_INDEX:
                view.showMessage("No card with that number in your hand.");
                break;
            case GAME_NOT_ACTIVE:
                view.showMessage("The game is not active.");
                break;
            case WON:
                break; // the view already prints "<name> wins!"
        }
    }
}
