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
java -cp out:student.jar org.junit.runner.JUnitCore turnabout.model.LinkedQueueTest turnabout.model.DeckTest turnabout.model.PlayerTest turnabout.model.GameModelTest turnabout.model.CardTest turnabout.model.GameSnapshotTest turnabout.view.ConsoleViewTest turnabout.model.LinkedStackTest turnabout.model.LinkedQueueReverseTest turnabout.model.DeckReverseTest turnabout.model.GameModelReverseTest turnabout.controller.GameControllerTest turnabout.controller.TurnaboutMainTest
```

This runs 118 tests and ends with `OK (118 tests)`. The `WARNING: ...
setSecurityManager` lines come from `student.jar` on newer Java and can be
ignored. On Windows, use `;` instead of `:` in the `-cp` value.

The tests build a `GameModel` directly, register a recording `GameListener`,
and never create a `ConsoleView`. This shows the model runs on its own.
`ConsoleViewTest` drives the view with a `Scanner` over a fixed string and a
`PrintStream` over a byte buffer.

**Repeatable game.** `java -cp out turnabout.controller.TurnaboutMain 25`
(an optional number) seeds the shuffle, so the same game is dealt every time.
`demo/demo-input.txt` is a scripted run of that seeded game; see "The
3-minute demo path" under Next Steps.

## Project structure

| Package | Classes |
| --- | --- |
| `turnabout.model` | `LinkedQueue`, `LinkedStack` ,`Card`, `CardColor`, `Deck`, `Player`, `GameModel`, `GameSnapshot`, `MoveResult`, `GameListener` |
| `turnabout.view` | `ConsoleView` |
| `turnabout.controller` | `GameController`, `TurnaboutMain` |
| Tests (`test/turnabout/model`) | `LinkedQueueTest`, `DeckTest`, `PlayerTest`, `GameModelTest`, `CardTest`, `GameSnapshotTest`,  `DeckReverseTest`, `GameModelReverseTest` |
| Tests (`test/turnabout/view`) | `ConsoleViewTest` |

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

- **The illegal-play message says "must match the top card by color or
  number",** which is not quite right for a Reverse card (a Reverse matches by
  color or another Reverse).
- **The result message still prints below the next player's board** instead
  of above it (carried over from Deliverable 3).

## Next Steps

Written by Toby after running everything. Facts come first; anything marked
**[team decides]** or **[team confirms]** is a proposal, not a decision.

### Where the build stands (facts)

- The game runs from setup to a win and a "Play again?" prompt through
  `TurnaboutMain`, with Reverse working. A scripted run is in `demo/`.
- **118 tests, all pass.** Before Toby's changes 1 failed: `DeckTest` still
  expected a 40-card deck, but the deck now has 44 (40 number + 4 Reverse).
  It now checks 44 cards and that the same seed gives the same order.
- `GameController` now has a message for `REVERSED` ("Direction reversed!"),
  prints no second winner line on `WON`, and re-prompts on every bad input:
  letters or blank at a card choice, a number outside the hand, a card that
  doesn't match, a player count that isn't 2 to 6, blank / duplicate /
  over-20-character names, and Ctrl+D (ends with "Goodbye!", no stack trace).
- `LinkedStackTest` and `LinkedQueueReverseTest` moved out of `test-pending/`
  into `test/turnabout/model/`; both pass against Raul's `LinkedStack` and
  `reverse()`.
- **Not verified against the real `student.jar`.** It is not in the repo and
  Toby didn't have it, so the tests were run with JUnit 4.13.2 and a
  one-line stand-in for `student.TestCase`. Someone with `student.jar`
  should re-run the command in How to run.
- `PlayerTest` covers the real `Player` API (`addCard`, `removeCard`, and
  `getHand` is read-only). `Player` has no `getCard`, `playCard` or
  `handSize`, so there are no tests for those.

### What is left for the final build, in order

1. **Result message prints below the next board** (still open, see "What
   doesn't work yet"). Cause: the model notifies the view inside
   `playCard`, before the controller can print its message. Proposal
   [team decides]: have the view print the board only when the controller
   asks for it, or have the message travel in the snapshot.
2. **Re-run the tests with the real `student.jar`** and fix anything that
   differs.
3. **Stretch goal work, if any is committed** (next section).
4. **Final play-test** of the demo path below on the machine we present from.
5. **Finish the attribution table:** Raul still has `[fill in ...]` cells.

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

### Stretch goals (from the scope)

| Goal | Proposal | Reason |
| --- | --- | --- |
| Skip card | Committed [team decides] | Reuses the turn-passing code. Needs a new `MoveResult`; `GameControllerTest` fails until `messageFor` handles it, which is the intended reminder. |
| Draw Two card | Dropped [team decides] | Needs a draw-penalty rule and another new result; no time. |
| Swing view | Dropped [team decides] | A whole new view class; the console view meets the spec. |
| Leaving mid-game | Dropped [team decides] | Today Ctrl+D ends the whole game; removing one player from the queue needs new model code and tests. |

### The 3-minute demo path

Run on Windows with Java 26, from the project folder. Compile, then start the
seeded game (on Windows use `;` instead of `:` in a `-cp` that lists several
paths; this one has only `out`):

```
javac -d out $(find src -name "*.java")
java -cp out turnabout.controller.TurnaboutMain 25
```

Type these, one per line. The same inputs piped from
`demo/demo-input.txt` were run and ended with "Ana wins!".

1. **Set up players.** Type `9`: "Please enter a number from 2 to 6." Type
   `3`. Names: `Ana`, then `ana` (rejected: already taken), then `Ben`,
   `Cy`.
2. **Show a bad input.** Ana's board shows top card GREEN 9 and her hand.
   Type `abc`: "Please type a card number, or D to draw." Type `9`: "No card
   with that number in your hand." It is still Ana's turn.
3. **A normal turn.** Type `2` (GREEN 0, matches by color). The board moves
   to Ben; the order is Ben, Cy, Ana ("Up next: Cy").
4. **The new feature.** Ben types `4` (GREEN REVERSE): "Direction reversed!"
   and the next turn is **Ana**, who went before him, not Cy.
5. **Keep playing.** Type `4`, `d`, `d`, `1`, `d`, `1`, `2`, `2`, `1`, `1`.
   This includes draws (`d`) and a second Reverse.
6. **Finish with a win.** Ana plays her last card: "Ana wins!" prints once.
   Type `n` at "Play again?" and the program prints "Goodbye!".

### Who does what for the remaining items [team confirms]

| Item | Owner |
| --- | --- |
| Message-before-board fix (item 1) | Toby (controller) with Ruhaan (view) |
| Re-run tests with real `student.jar` | Ruhaan |
| Skip card: `Card`, `Deck`, `GameModel`, `MoveResult` | Raul |
| Skip card: printing it in `ConsoleView` | Ruhaan |
| Skip card: `messageFor` and controller/model tests | Toby |
| Final play-test of the demo path | Toby |
| Fill in the remaining attribution cells | Raul |

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
| `GameController.java` | Toby | GenAI-assisted: Deliverable 4: `REVERSED` message, `parseNumber` and `messageFor` helpers, no second winner line, re-prompts on bad input, Ctrl+D. Written with Claude Code; I ran it with piped input. |
| `TurnaboutMain.java` | Toby | GenAI-assisted: Deliverable 4: play-again loop, `play(Scanner, PrintStream, Random)` so it can be tested, optional seed argument. Written with Claude Code; I ran the scripted demo. |
| `DeckTest.java` | Toby | GenAI-assisted: Deliverable 4: fixed the 40-card test to 44 cards (40 number, 4 Reverse), added same-seed test. Written with Claude Code; tests pass. |
| `GameModelTest.java` | Toby | GenAI-assisted: Deliverable 4: renamed the recording listener `FakeListener`, added the 3-player turn rotation test. Written with Claude Code; tests pass. |
| `GameControllerTest.java` | Toby | GenAI-assisted: new in Deliverable 4: `parseNumber`, `messageFor` for every result, scripted runs (win, bad input, illegal play, Ctrl+D, Reverse). Written with Claude Code; tests pass. |
| `TurnaboutMainTest.java` | Toby | GenAI-assisted: new in Deliverable 4: smoke test of `play` with the real random deck. Written with Claude Code; tests pass. |
| `demo/demo-input.txt` | Toby | GenAI-assisted: scripted input for seed 25 (bad inputs, two Reverses, a win), found with a small throwaway bot written with Claude Code; I ran it. |
| `LinkedQueueTest.java` | Toby | GenAI-assisted: Did the version originaly myself and then revied with claude  |
| `PlayerTest.java` | Toby | GenAI-assisted: Did the version originaly myself and then revied with claude  |
| `CardTest.java` | Ruhaan | GenAI-assisted: written with Claude Code from our contract; I ran them. |
| `GameSnapshotTest.java` | Ruhaan | GenAI-assisted: written with Claude Code from our contract; I ran them. |
| `ConsoleViewTest.java` | Ruhaan | GenAI-assisted: written with Claude Code from our contract; I ran them. |
| `LinkedStackTest.java`  | Ruhaan | GenAI-assisted: written with Claude Code from our contract; now in `test/` and passing against `LinkedStack`. |
| `LinkedQueueReverseTest.java`  | Ruhaan | GenAI-assisted: written with Claude Code from our contract; now in `test/` and passing against `reverse()`. |
| `README.md` | Raul, Ruhaan | Raul: drafted with GenAI (Claude). Ruhaan: How to run section and this table, drafted with Claude Code; commands were run before writing them down. |
## GenAI note

**Raul:** I completed the mayorty of the classes that where corresponded to me, after doing an initial version I gave Claude the files that I wrote and told it to give me what I had done wrong and how to fix it, after reviewed I commited the changes.

**Ruhaan:** I tried to write most of my classes myself and used GenAI mainly as a debugging tool. For this deliverable I used Claude Code to add the Reverse card to `Card`, check that `ConsoleView` and `GameSnapshot` still work with the real model, write `CardTest`, `GameSnapshotTest`, `ConsoleViewTest` and the pending `LinkedStackTest`/`LinkedQueueReverseTest`, and draft the How to run section and attribution table. I ran the tests and the program before committing.

**Toby:** I used Claude Code (Sonnet 5.5) to finish `GameController` and `TurnaboutMain` (REVERSED message, no double winner line, input re-prompts, Ctrl+D, play again, optional seed), update `DeckTest` and `GameModelTest`, write `GameControllerTest` and `TurnaboutMainTest`, and draft the Next Steps section. I read the generated code and the test output; I ran the full suite and the scripted demo before committing. I did not have `student.jar`, so I ran the tests with JUnit 4.13.2 and a stand-in `student.TestCase`.
