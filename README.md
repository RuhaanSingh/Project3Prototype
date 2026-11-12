# Turnabout

Turnabout is a pass-and-play card game for 2–6 players sharing one computer.
On your turn you play a card that matches the top card's color or number, or
you draw one. The first player to empty their hand wins. Turn order is kept by
our own linked queue: the player who just went moves to the back of the line.

CS 2114 · Project 3 · Deliverable 3 (First Prototype)
Group: Raul, Ruhaan, Toby

## How to run

Source files are in `src/turnabout/`, split into three packages:
`turnabout.model`, `turnabout.view`, and `turnabout.controller`.

**Compile**

```
javac -d out $(find src -name "*.java")
```

**Play the game (thin console view)**

```
java -cp out turnabout.controller.TurnaboutMain
```

Enter the number of players and their names. On your turn, type a card number
to play it, or `D` to draw.

**Run the model with no view (tests)**

The tests are in `test/turnabout/model/` and use the course's `student.TestCase`
(from `student.jar`). Run them from the IDE, or from the command line with
`student.jar` on the classpath:

```
javac -d out -cp student.jar $(find src test -name "*.java")
java -cp out:student.jar org.junit.runner.JUnitCore turnabout.model.LinkedQueueTest turnabout.model.DeckTest turnabout.model.PlayerTest turnabout.model.GameModelTest
```

There are 41 tests across `LinkedQueueTest`, `DeckTest`, `PlayerTest`, and
`GameModelTest`. The tests build a `GameModel` directly, register a recording `GameListener`,
and never create a `ConsoleView`. This shows the model runs on its own.

## Project structure

| Package | Classes |
| --- | --- |
| `turnabout.model` | `LinkedQueue`, `Card`, `CardColor`, `Deck`, `Player`, `GameModel`, `GameSnapshot`, `MoveResult`, `GameListener` |
| `turnabout.view` | `ConsoleView` |
| `turnabout.controller` | `GameController`, `TurnaboutMain` |
| Tests (`test/turnabout/model`) | `LinkedQueueTest`, `DeckTest`, `PlayerTest`, `GameModelTest` |

Nothing in `turnabout.model` imports `turnabout.view` or
`turnabout.controller`, so the model never depends on the view. The view learns about changes
through `GameListener.gameChanged(GameSnapshot)`, and only ever sees a
read-only `GameSnapshot`.

## Reflection

### What changed from our specification?

- **Packages.** The spec didn't name packages. We split the code into
  `turnabout.model`, `turnabout.view`, and `turnabout.controller` so the
  model/view line is visible in the imports.
- **`Card` gained `getColor()` and `getNumber()`** so the view can print a card.
- **`Card.matches(null)` returns false** instead of throwing
  IllegalArgumentException as the spec said. The model never passes null, so
  this doesn't change how the game plays.
- **`GameSnapshot` getters are `getCurrentName()` and `getNextName()`**
  instead of `getCurrentPlayerName()` / `getNextPlayerName()`, and its
  constructor rejects null values.
- **`GameModel` gained `isValidPlayerCount(int)`, `getPlayerCount()`,
  `isGameOver()`, and `getWinnerName()`.** The controller needs the first two
  to re-prompt for the player count and names without repeating the rule; the
  model still owns the rule.
- **`ConsoleView` reads with a `Scanner` and `readLine(String prompt)`**
  instead of a `BufferedReader` and `readLine()`. Taking a prompt keeps the
  question and the typing on the same line.
- **`Deck.topCard()` returns null before the first discard.** This can't
  happen during a game, because `startGame()` always flips a starting card.
- **Tests:** the plan in Section 7 is covered for `LinkedQueue`, `Deck`,
  `Player`, and `GameModel`. `Card` has no tests yet (see below).


### Did the model really stand alone?

Yes. The model only knows the `GameListener` interface. Nothing in the model
prints, reads input, or refers to `ConsoleView` or `GameController`. Nothing
had to move across the model/view line to make the game run.


### What happened to our unknowns?

| Unknown from our scope | Status |
| --- | --- |
| How does the view learn the model changed? | **Solved:** observer pattern (`GameListener` + `GameSnapshot`). |
| Should the controller or the model decide whether a play is legal? | **Solved:** the model decides and returns a `MoveResult`; the controller only checks that input is a number. |
| How do we test the model with no console? | **Solved:** tests register a recording listener instead of a view. |
| How do we test code that uses randomness? | **Solved:** `Deck(List<Card>, Random)` takes a fixed card order, so tests know every hand. |
| How do we reverse the queue, including the tail reference? | **Still open.** Needed for our change feature in Deliverable 4. |

### What doesn't work yet?

- The Reverse card is not built yet (planned for Deliverable 4).
- After a move, the result message ("Card played.") prints below the next
  player's board instead of above it. The model notifies the view during
  `playCard`/`drawCard`, before the controller shows its message.
- **No tests for `Card` yet.** Section 7 of the spec planned tests for the
  `Card` constructor and `Card.matches`.
- All 41 existing tests pass. The MVP path works: games with 2, 4, and 6
  players play to a winner, and every bad-input case from the spec is
  handled (bad player counts, blank/duplicate/long names, letters, card
  numbers outside the hand, illegal plays, and closing input mid-game).

## Next Steps

### What is left to finish the MVP?

In order:

The MVP plays from start to winner, so what's left is polish:

1. Show the result message ("Card played.") before the next player's board.
2. Add `CardTest` for the constructor and `matches()`.
3. Play-test again after both changes.

### Our change feature: the Reverse card

A Reverse card flips the direction of play, so the player who went before
you goes next. It changes model state (the turn order) and logic (a new card
rule), and players reach it through the normal play-a-card input.

How it works: when a Reverse card is played, the model reverses the order of
the waiting players, then puts the current player at the back. With players
A, B, C, D and A playing Reverse: waiting `[B, C, D]` becomes `[D, C, B]`,
then A joins the back: `[D, C, B, A]`, so D goes next. With only 2 players,
reversing leaves the order the same; we will decide in Deliverable 4 whether
Reverse acts as a skip in that case.

**Prediction: classes we expect to change**

| Class | Role | Expected change |
| --- | --- | --- |
| `LinkedQueue` | Model | New `reverse()` method, using a stack we write; must also update `tail`. |
| `Card` | Model | Needs to tell a Reverse card from a number card. |
| `Deck` | Model | Adds Reverse cards when building the deck. |
| `GameModel` | Model | `playCard` reverses the waiting queue when a Reverse card is played; matching rule extended. |
| `ConsoleView` | View | Small change: print a Reverse card differently from a number card. |

**Prediction: classes that should NOT need to change**

`GameController`, `TurnaboutMain`, `GameListener`, `GameSnapshot`,
`MoveResult`, `Player`, `CardColor`. Playing a Reverse card is still
"play the card at index i", so the controller and the snapshot don't change.
If any of these do change, we will explain why in Deliverable 4.

### Which stretch goals will we attempt?



- **Committing to:** Skip card. It reuses the turn-passing code, so it is
  small once Reverse works.
- **Dropping:** Graphical (Swing) view, Draw Two card, and leaving mid-game,
  to keep time for the change feature and its tests.

### What new tests will we need?

- `LinkedQueue.reverse()`: on an empty queue, a one-item queue, and a
  four-item queue (order is reversed, and `tail` is correct: enqueuing after
  reversing puts the item at the true back).
- `Card`: a Reverse card matches the same color and another Reverse card,
  and doesn't match a different color.
- `GameModel.playCard` with Reverse: with 4 players the next player becomes
  the one who went before; with 2 players, the agreed behavior.
- Bad input: playing a Reverse card that doesn't match the top card returns
  `ILLEGAL_PLAY` and leaves the turn order unchanged.

### Who does what?

| Item | Owner |
| --- | --- |
| `LinkedQueue.reverse()` and the Reverse rule in `GameModel` | Raul |
| Reverse cards in `Deck` | Raul |
| Reverse in `Card` | Ruhaan |
| Printing Reverse cards in `ConsoleView` | Ruhaan |
| New tests listed above | Toby |
| Remaining MVP fixes | Raul |

## Attribution

One row per class and test file.

| File | Who | How |
| --- | --- | --- |
| `LinkedQueue.java` | Raul | GenAI-assisted: Did the version originaly myself and then revied with claude  |
| `Player.java` | Raul |GenAI-assisted: Did the version originaly myself and then revied with claude  |
| `Deck.java` | Raul | GenAI-assisted: Did the version originaly myself and then revied with claude  |
| `GameModel.java` | Raul, Ruhaan | GenAI-assisted: Did the version originaly myself (Raul) and then revied with claude. Ruhaan added `getPlayerCount`, `isValidPlayerCount`, `isGameOver`, `getWinnerName`|
| `MoveResult.java` | Raul | GenAI-assisted: Did the version originaly myself and then revied with claude  |
| `GameListener.java` | Raul | GenAI-assisted: Did the version originaly myself and then revied with claude  |
| `CardColor.java` | Ruhaan | [how] |
| `Card.java` | Ruhaan | [how] |
| `GameSnapshot.java` | Ruhaan | [how] |
| `ConsoleView.java` | Ruhaan | [how] |
| `GameController.java` | Toby | [how] |
| `TurnaboutMain.java` | Toby | [how] |
| `LinkedQueueTest.java` | Toby | [how] |
| `DeckTest.java` | Toby | [how] |
| `PlayerTest.java` | Toby | [how] |
| `GameModelTest.java` | Toby | [how] |
| `README.md` | [names] | Drafted with GenAI (Claude); [who filled in and reviewed it] |

## GenAI note

**Raul:** I completed the mayorty of the classes that where corresponded to me, after doing an initial version I gave Claude the files that I wrote and told it to give me what I had done wrong and how to fix it, after reviewed I commited the changes.

**Ruhaan:** [How you used GenAI, or "I didn't use GenAI for this deliverable."]

**Toby:** [How you used GenAI, or "I didn't use GenAI for this deliverable."]
