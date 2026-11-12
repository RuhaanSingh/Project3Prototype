package turnabout.controller;

import java.util.Random;
import java.util.Scanner;
import turnabout.model.Deck;
import turnabout.model.GameModel;
import turnabout.view.ConsoleView;

/** Builds the model, view and controller, connects them, and runs the game. */
public class TurnaboutMain {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        ConsoleView view = new ConsoleView(in, System.out);
        GameModel model = new GameModel(new Deck(new Random()));
        model.addListener(view);
        new GameController(model, view).run();
    }
}
