package turnabout.controller;

import java.io.PrintStream;
import java.util.Random;
import java.util.Scanner;
import turnabout.model.Deck;
import turnabout.model.GameModel;
import turnabout.view.ConsoleView;

/** Starts Turnabout on the console. */
public class TurnaboutMain {
    public static void main(String[] args) {
        // optional seed argument makes a game repeatable (used for the demo script)
        Random rng = args.length > 0 ? new Random(Long.parseLong(args[0])) : new Random();
        play(new Scanner(System.in), System.out, rng);
    }

    /** Plays games until the players decline another round or input ends. */
    static void play(Scanner in, PrintStream out, Random rng) {
        ConsoleView view = new ConsoleView(in, out);
        boolean again = true;
        while (again) {
            GameModel model = new GameModel(new Deck(rng));
            model.addListener(view);
            new GameController(model, view).run();
            again = model.isGameOver() && wantsAnother(view);
        }
        view.showMessage("Goodbye!");
    }

    private static boolean wantsAnother(ConsoleView view) {
        while (true) {
            String line = view.readLine("Play again? (y/n): ");
            if (line == null || line.trim().equalsIgnoreCase("n")) {
                return false;
            }
            if (line.trim().equalsIgnoreCase("y")) {
                return true;
            }
        }
    }
}
