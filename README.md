# Turnabout

Turnabout is a pass-and-play card game for 2–6 players sharing one computer.
On your turn you play a card that matches the top card's color or number, or
you draw one. The first player to empty their hand wins. Turn order is kept by
our own linked queue: the player who just went moves to the back of the line.

CS 2114 · Project 3 · Deliverable 3 (First Prototype)
Group: Raul, Ruhaan, Toby

## How to run

All source files are in `src/` .

**Compile**

```
javac -d out src/*.java
```

**Play the game (thin console view)**

```
java -cp out TurnaboutMain
```

Enter the number of players and their names. On your turn, type a card number
to play it, or `D` to draw.



```
javac -d out -cp junit-platform-console-standalone.jar src/*.java test/*.java
java -jar junit-platform-console-standalone.jar -cp out --scan-class-path
```

The tests build a `GameModel` directly, register a recording `GameListener`,
and never create a `ConsoleView`. This shows the model runs on its own.

## Project structure

| Role | Classes |
| --- | --- |
| Model | `LinkedQueue`, `Card`, `CardColor`, `Deck`, `Player`, `GameModel`, `GameSnapshot`, `MoveResult`, `GameListener` |
| View | `ConsoleView` |
| Controller | `GameController`, `TurnaboutMain` |
| Tests | [test file names] |

The model never imports or calls the view. The view learns about changes
through `GameListener.gameChanged(GameSnapshot)`, and only ever sees a
read-only `GameSnapshot`.

## Reflection

### What changed from our specification?

- **No packages.** The spec didn't name packages; the group put every file
  directly in `src/` so the classes can see each other without imports.
- **`Card` gained `getColor()` and `getNumber()`.** The view needs them to
  print a card. Card also has `equals`, `hashCode`, and a debug-only
  `toString`; how cards look on screen is still the view's decision.
- **`GameSnapshot` gained `isGameOver()`**, a shortcut for
  `getWinnerName() != null`. Its constructor is package-private (no `public`), so
  only `GameModel` builds snapshots.
- **`Deck.topCard()` returns null before the first discard.** This can't
  happen during a game, because `startGame()` always flips a starting card.
- **Model method signatures are otherwise the same as Section 5 of the spec.**
- [Ruhaan: any changes to ConsoleView from the spec, and why]
- [Toby: any changes to GameController, TurnaboutMain, or the test plan, and why]

### Did the model really stand alone?

Yes. The model only knows the `GameListener` interface. Nothing in the model
prints, reads input, or refers to `ConsoleView` or `GameController`. Nothing
had to move across the model/view line to make the game run.
[Edit if your experience joining the parts was different.]

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
- [Any MVP feature that is missing or buggy]
- [Any bad-input case from the spec that isn't handled yet]
- [Any test that is missing or failing]

## Next Steps

### What is left to finish the MVP?

In order:

1. [Remaining MVP fix or feature]
2. [Remaining MVP fix or feature]
3. Play three full games (2, 4, and 6 players) and try every bad-input case
   from Section 6 of the spec.

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

[Group to confirm before submitting]

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
| Reverse in `Card` | [owner of Card] |
| Printing Reverse cards in `ConsoleView` | Ruhaan |
| New tests listed above | Toby |

## Attribution

One row per class and test file.

| File | Who | How |
| --- | --- | --- |
| `LinkedQueue.java` | Raul | GenAI-assisted: Did the version originaly myself and then revied with claude |
| `Player.java` | Raul | GenAI-assisted; Did the version originaly myself and then revied with claude  |
| `Deck.java` | Raul | GenAI-assisted; Did the version originaly myself and then revied with claude  |
| `GameModel.java` | Raul | GenAI-assisted; Did the version originaly myself and then revied with claude  |
| `MoveResult.java` | Raul | GenAI-assisted; Did the version originaly myself and then revied with claude  |
| `GameListener.java` | Raul | GenAI-assisted; Did the version originaly myself and then revied with claude  |
| `CardColor.java` | Ruhaan | [Wrote / GenAI-assisted / ...] |
| `Card.java` | [name] | [how] |
| `GameSnapshot.java` | [name] | [how] |
| `ConsoleView.java` | Ruhaan | [how] |
| `GameController.java` | Toby | [how] |
| `TurnaboutMain.java` | Toby | [how] |
| [test file] | Toby | [how] |
| `README.md` | [names] | Drafted with GenAI (Claude); [who filled in and reviewed it] |

## GenAI note

**Raul:** I completed the mayorty of the classes that where corresponded to me, after doing an initial version I gave Claude the files that I wrote and told it to give me what I had done wrong and how to fix it, after reviewed I commited the changes.

**Ruhaan:** [How you used GenAI, or "I didn't use GenAI for this deliverable."]

**Toby:** [How you used GenAI, or "I didn't use GenAI for this deliverable."]
