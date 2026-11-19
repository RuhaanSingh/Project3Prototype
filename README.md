# Turnabout

Turnabout is a pass-and-play card game for 2–6 players sharing one computer.
On your turn you play a card that matches the top card's color or number, or
you draw one. The first player to empty their hand wins. Turn order is kept by
our own linked queue: the player who just went moves to the back of the line.

CS 2114 · Project 3 · Deliverable 3 (First Prototype)
Group: Raul, Ruhaan, Toby

## How to run

Source files are in `src/turnabout/`, split into three packages:
`turnabout.model`, `turnabout.view`, and `turnabout.controller`. Tests are in
`test/turnabout/`. All commands below are run from the project folder (the
one with this README) and were checked with Java 17 on macOS.

**Compile and play the game**

```
javac -d out $(find src -name "*.java")
java -cp out turnabout.controller.TurnaboutMain
```

Enter the number of players and their names. On your turn, type a card number
to play it, or `D` to draw. Closing input (Ctrl+D) ends the game.

**Compile and run the tests**

The tests use the course's `student.TestCase`, which comes in `student.jar`
(it also contains JUnit 4). Copy `student.jar` into the project folder first
(it is not committed), then:

```
javac -d out -cp student.jar $(find src test -name "*.java")
java -cp out:student.jar org.junit.runner.JUnitCore turnabout.model.LinkedQueueTest turnabout.model.DeckTest turnabout.model.PlayerTest turnabout.model.GameModelTest turnabout.model.CardTest turnabout.model.GameSnapshotTest turnabout.view.ConsoleViewTest
```

This runs 73 tests and ends with `OK (73 tests)`. The `WARNING: ...
setSecurityManager` lines come from `student.jar` on newer Java and can be
ignored. On Windows, use `;` instead of `:` in the `-cp` value.

The tests build a `GameModel` directly, register a recording `GameListener`,
and never create a `ConsoleView`. This shows the model runs on its own.
`ConsoleViewTest` drives the view with a `Scanner` over a fixed string and a
`PrintStream` over a byte buffer.

**Tests waiting on `LinkedStack` and `LinkedQueue.reverse()`**

`test-pending/turnabout/model/` holds `LinkedStackTest` and
`LinkedQueueReverseTest`. They are written against the contract but do not
compile until `LinkedStack` and `LinkedQueue.reverse()` are pushed. Once they
are, move both files into `test/turnabout/model/` and add
`turnabout.model.LinkedStackTest turnabout.model.LinkedQueueReverseTest` to
the end of the run command above.

## Project structure

| Package | Classes |
| --- | --- |
| `turnabout.model` | `LinkedQueue`, `LinkedStack` ,`Card`, `CardColor`, `Deck`, `Player`, `GameModel`, `GameSnapshot`, `MoveResult`, `GameListener` |
| `turnabout.view` | `ConsoleView` |
| `turnabout.controller` | `GameController`, `TurnaboutMain` |
| Tests (`test/turnabout/model`) | `LinkedQueueTest`, `DeckTest`, `PlayerTest`, `GameModelTest`, `CardTest`, `GameSnapshotTest`,  `DeckReverseTest`, `GameModelReverseTest` |
| Tests (`test/turnabout/view`) | `ConsoleViewTest` |
| Pending tests (`test-pending/turnabout/model`) | `LinkedStackTest`, `LinkedQueueReverseTest` |

Nothing in `turnabout.model` imports `turnabout.view` or
`turnabout.controller`, so the model never depends on the view. The view learns about changes
through `GameListener.gameChanged(GameSnapshot)`, and only ever sees a
read-only `GameSnapshot`.

## Reflection
### How did our design absorb the change?

The Reverse card touched six model classes. This list comes from
`git diff 9a7ec49` (the last commit before the Reverse work started) over `src/`:

| Class | Owner | What changed |
| --- | --- | --- |
| `Card` | Ruhaan | `Card.reverse(color)` factory, `isReverse()`, Reverse matching rules, `toString()` like `RED REVERSE` |
| `LinkedStack` | Raul | **New class**: our own linked stack (`push`, `pop`, `peek`, `isEmpty`, `size`) |
| `LinkedQueue` | Raul | New `reverse()`, which pours the queue into a `LinkedStack` and back |
| `MoveResult` | Raul | New value `REVERSED` |
| `Deck` | Raul | Standard deck is 44 cards (one Reverse per color); `Deck.standard(Random)` factory; `REVERSES_PER_COLOR` and `STANDARD_SIZE` constants |
| `GameModel` | Raul | `playCard` reverses the waiting players before passing the turn and returns `REVERSED` |

Unchanged: `GameSnapshot`, `GameListener`, `Player`, `CardColor`,
`ConsoleView`, `GameController`, `TurnaboutMain`.

**Compared with our Deliverable 3 prediction:**

- **Matched:** `LinkedQueue`, `Card`, `Deck`, and `GameModel` changed, as
  predicted. `GameController`, `TurnaboutMain`, `GameListener`,
  `GameSnapshot`, `Player`, and `CardColor` did not change, as predicted.
- **Did not match:**
    - `MoveResult` changed; we predicted it wouldn't. We added `REVERSED`
      so the controller can tell a Reverse apart from a normal play.
    - `LinkedStack` is a new class. The prediction said `reverse()` would use
      "a stack we write" but didn't list it as a class.
    - `ConsoleView` did **not** change; we predicted a small change. It
      prints cards with `toString()`, and `Card.toString()` already returns
      `RED REVERSE`.
    - We predicted the matching rule would change in `GameModel`. It changed
      in `Card.matches()` instead; `GameModel`'s legality check is the same.
- [fill in: what surprised the team]

### Did the change cross the model/view line?

No. `ConsoleView.java` and `GameSnapshot.java` have no changes since
`9a7ec49` (`git diff` is empty for both). No class in `turnabout.model`
imports or mentions `turnabout.view`, `turnabout.controller`, `ConsoleView`,
or `GameController` (checked with `grep`). The view didn't have to learn
anything new: a Reverse card reaches it as an ordinary `Card` inside the
`GameSnapshot`. The only new thing that leaves the model is the
`MoveResult.REVERSED` value, which goes to the controller, not the view.

### What changed from our Deliverable 3 plan?

- **44-card deck:** one Reverse card per color. The plan didn't fix a number.
- **Reverse cards are made with `Card.reverse(color)`** and checked with
  `isReverse()`.
- **`MoveResult.REVERSED` was added**, although the plan said `MoveResult`
  wouldn't change.
- **With 2 players, a Reverse is played normally and the other player goes
  next.** The plan left open whether it would act as a skip; reversing a line
  of one waiting player changes nothing, so we kept that.
- **A Reverse flipped as the first top card is just a normal top card.** The
  order isn't reversed at the start.
- **A Reverse played as your last card wins** (`WON`), and the order is not
  reversed.
- **`LinkedQueue.reverse()` uses `LinkedStack`:** every item is dequeued onto
  the stack, then popped back in. It only uses `enqueue` and `dequeue`, so
  `head`, `tail`, and `size` stay correct. This answers the unknown we left
  open in Deliverable 3.
- **`Deck.standard(Random)`** was added; the old `Deck(Random)` constructor
  still builds the same 44-card deck, so `TurnaboutMain` didn't change.
- **Tests:** the plan gave the new tests to Toby. Ruhaan wrote
  `LinkedStackTest`, `LinkedQueueReverseTest`, `CardTest`,
  `GameSnapshotTest`, and `ConsoleViewTest`; Raul added `DeckReverseTest`
  and `GameModelReverseTest` for the Deck and `GameModel` changes.



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

- **The player isn't told when a Reverse flips the order.**
  `GameController.show()` has no `REVERSED` case, so no message prints; the
  board does show the new order.
- **The illegal-play message says "must match the top card by color or
  number",** which is not quite right for a Reverse card (a Reverse matches by
  color or another Reverse).
- **The result message still prints below the next player's board** instead
  of above it (carried over from Deliverable 3).
- **`LinkedStackTest` and `LinkedQueueReverseTest` are still in
  `test-pending/`.** They compile and pass now, so they can move to
  `test/turnabout/model/` and join the run command.

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

One row per class and test file. Each owner fills in their own How column.

| File | Who | How |
| --- | --- | --- |
| `LinkedQueue.java` | Raul | GenAI-assisted. Deliverable 3: first version written by Claude from our spec; Raul committed it. Deliverable 4: `reverse()` written by Claude from our contract, using `LinkedStack`. Raul: [fill in what you reviewed or changed] |
| `Player.java` | Raul | GenAI-assisted. Deliverable 3: written by Claude from our spec; Raul committed it. No changes in Deliverable 4. |
| `Deck.java` | Raul | GenAI-assisted. Deliverable 3: written by Claude from our spec; Raul committed it. Deliverable 4: 44-card deck, `standard(Random)`, and constants written by Claude from our contract. Raul: [fill in what you reviewed or changed] |
| `GameModel.java` | Raul, Ruhaan | GenAI-assisted. Deliverable 3: written by Claude from our spec; Raul committed it; Ruhaan added `getPlayerCount`, `isValidPlayerCount`, `isGameOver`, `getWinnerName`. Deliverable 4: the Reverse rule in `playCard` written by Claude from our contract. Raul: [fill in what you reviewed or changed] |
| `MoveResult.java` | Raul | GenAI-assisted. Deliverable 3: written by Claude from our spec; Raul committed it. Deliverable 4: `REVERSED` added by Claude. |
| `GameListener.java` | Raul | GenAI-assisted. Deliverable 3: written by Claude from our spec; Raul committed it. No changes in Deliverable 4. |
| `LinkedStack.java` | Raul | GenAI-assisted. New in Deliverable 4: written by Claude from our contract. Raul: [fill in what you reviewed or changed] |
| `DeckReverseTest.java` | Raul | GenAI-assisted: written by Claude for the 44-card deck; 6 tests, all pass. Raul: [fill in what you reviewed or ran] |
| `GameModelReverseTest.java` | Raul | GenAI-assisted: written by Claude for the Reverse rule in `playCard`; 13 tests (normal and bad input), all pass. Raul: [fill in what you reviewed or ran] |
| `CardColor.java` | Ruhaan | Wrote fully myself. |
| `Card.java` | Ruhaan | GenAI-assisted. Mostly finished myself, then had AI debug one function I couldn't get to work. The Reverse card (`reverse`, `isReverse`, the Reverse `matches` rules, `toString`) was added with Claude Code from our contract. |
| `GameSnapshot.java` | Ruhaan | GenAI-assisted. Wrote myself, then had AI check it. Something I did didn't match what we agreed, so AI helped fix it. |
| `ConsoleView.java` | Ruhaan | GenAI-assisted. Did the version originally myself, then fixed it up with Claude. Checked with Claude Code against the real model and controller for Deliverable 4: no changes needed. |
| `GameController.java` | Toby | GenAI-assisted: Did the version originaly myself and then revied with claude  |
| `TurnaboutMain.java` | Toby | GenAI-assisted: Did the version originaly myself and then revied with claude  |
| `LinkedQueueTest.java` | Toby | GenAI-assisted: Did the version originaly myself and then revied with claude  |
| `DeckTest.java` | Toby | GenAI-assisted: Did the version originaly myself and then revied with claude  |
| `PlayerTest.java` | Toby | GenAI-assisted: Did the version originaly myself and then revied with claude  |
| `GameModelTest.java` | Toby | GenAI-assisted: Did the version originaly myself and then revied with claude  |
| `CardTest.java` | Ruhaan | GenAI-assisted: written with Claude Code from our contract; I ran them. |
| `GameSnapshotTest.java` | Ruhaan | GenAI-assisted: written with Claude Code from our contract; I ran them. |
| `ConsoleViewTest.java` | Ruhaan | GenAI-assisted: written with Claude Code from our contract; I ran them. |
| `LinkedStackTest.java`  | Ruhaan | GenAI-assisted: written with Claude Code from our contract; not run yet (waiting on `LinkedStack`). |
| `LinkedQueueReverseTest.java`  | Ruhaan | GenAI-assisted: written with Claude Code from our contract; not run yet (waiting on `reverse()`). |
| `README.md` | Raul, Ruhaan | Raul: drafted with GenAI (Claude). Ruhaan: How to run section and this table, drafted with Claude Code; commands were run before writing them down. |
## GenAI note

**Raul:** I completed the mayorty of the classes that where corresponded to me, after doing an initial version I gave Claude the files that I wrote and told it to give me what I had done wrong and how to fix it, after reviewed I commited the changes.

**Ruhaan:** I tried to write most of my classes myself and used GenAI mainly as a debugging tool. For this deliverable I used Claude Code to add the Reverse card to `Card`, check that `ConsoleView` and `GameSnapshot` still work with the real model, write `CardTest`, `GameSnapshotTest`, `ConsoleViewTest` and the pending `LinkedStackTest`/`LinkedQueueReverseTest`, and draft the How to run section and attribution table. I ran the tests and the program before committing.

**Toby:** [owner fills in]
