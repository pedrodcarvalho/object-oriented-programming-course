# Bug Hunt

A *Pac-Man*-style game with a programming theme, built with **FXGL** (JavaFX). The maze is a neon circuit board and you are the **Debugger**, the yellow execution arrow. Your job is to collect every **bit** without getting caught by the four **bugs**.

This is the windowed port of the terminal game *Caça-Bugs* (`../project-1`), written for the Object-Oriented Programming final project (Java).

## How to run

Requires **JDK 21 or newer**. Maven comes with the wrapper.

| What | Command (from `project-2/`) |
| --- | --- |
| Play | `./mvnw javafx:run` |
| Tests (73) | `./mvnw test` |
| QA screenshots | `xvfb-run -a -s "-screen 0 1280x720x24" ./mvnw -q javafx:run -Dqa.args=--qa-shots=target/qa-shots` |

The QA harness writes `00-main-menu.png` to `04-game-over.png` into `target/qa-shots` and exits with code 0. Exit code 1 means a shot failed. Exit code 2 means the 60 s watchdog fired.

**Controls:** WASD or arrow keys move · P or Esc pauses (FXGL game menu) · F11 toggles fullscreen.

The high score is kept in memory for the current session only. When the game ends, it returns to the main menu after 3 s.

## Rules

- A bit is worth 10 points. A breakpoint is worth 50 and makes the bugs vulnerable (blue) for a few seconds.
- Capturing bugs with the same breakpoint is worth 200, 400, 800 and 1,600 points.
- Coffee appears twice per level (after 70 and 170 pickups). It is worth 100 to 5,000 points and disappears after 10 s.
- You start with 3 lives and earn an extra one at 10,000 points.
- The bugs alternate **scatter** waves (each heads to its own corner) and **chase** waves. Each level makes them faster and shortens the breakpoint.

| Bug | While chasing, it targets... |
| --- | --- |
| NullPointer (red) | the Debugger's tile |
| RaceCondition (pink) | 4 tiles ahead of the Debugger |
| Deadlock (cyan) | twice the vector from NullPointer to 2 tiles ahead of the Debugger |
| Heisenbug (orange) | the Debugger when more than 8 tiles away; closer than that, its own corner |

## Project structure

```
src/main/java/bughunt/
  BugHuntApp.java      FXGL entry point (GameApplication): menus, game loop, HUD wiring
  GameControls.java    key bindings
  model/               pure-Java rules model (no JavaFX/FXGL, enforced by ArchitectureTest)
    core/              Entity, Updatable, Match, Position, Direction, FixedStepClock, Ticks
    actors/            Actor, Debugger, Bug and the 4 bugs
    pickups/           Pickup, Bit, Breakpoint, Coffee
    modes/             BugMode, HostileMode, Scatter, Chase, Vulnerable, Captured, InBacklog, Look
    maze/              Maze, Cell, CellType, Level, LevelFactory, MazeLibrary, InvalidMazeException
    events/            EventBus, EventListener, GameEvent, EventType
    rules/             ScoreBoard, LevelManager, LevelConfig, ModeController, CollisionManager
    flow/              MatchSession, GamePhase, PhaseKind, Ready, Playing, LifeLost, LevelCleared, GameOver
  view/                MatchView, MazeRenderer, WallOutline, ActorTrail, DebuggerView, BugView, PickupView, Theme
  ui/                  Hud, ErrorDialog
  qa/                  QaShots (screenshot harness)
src/main/resources/assets/levels/maze1.maze
```

## Where each OO concept lives

| Concept | Where |
| --- | --- |
| Abstraction | `Entity`, `Actor`, `Bug`, `Pickup` and `HostileMode` are abstract |
| Inheritance | `Debugger` and `Bug` inherit movement from `Actor` (which extends `Entity`). The 4 bugs extend `Bug`; `Bit`, `Breakpoint` and `Coffee` extend `Pickup`. `Scatter` and `Chase` extend `HostileMode` |
| Polymorphism | `Match` updates every `Entity` through `update(Match)`. Each bug overrides `computeTarget()` and each pickup overrides `onCollected()` (called by `CollisionManager`) |
| Encapsulation | Private fields. Lives only change through `Debugger.loseLife()`/`gainLife()`, points only through `ScoreBoard.add()` |
| Interfaces | `Updatable`, `BugMode` (with *default* methods), `GamePhase`, `EventListener` |
| Composition | `Match` owns the maze, actors, `ScoreBoard`, `ModeController`, `LevelManager` and `EventBus`; `Maze` is made of `Cell`s; each `Bug` has a `BugMode`; `Deadlock` keeps a reference to `NullPointer`; `MatchView` composes `MazeRenderer`, `BugView`, `PickupView` and `ActorTrail` |
| State (bug modes) | `BugMode` with `Scatter`, `Chase`, `Vulnerable`, `Captured`, `InBacklog` (`modes/`) |
| State (match phases) | `GamePhase` with `Ready`, `Playing`, `LifeLost`, `LevelCleared`, `GameOver`, driven by `MatchSession` (`flow/`) |
| Template Method | `Bug.chooseDirection()` is `final`; only the target (`computeTarget()`) varies |
| Observer | `EventBus` with `EventListener`, `GameEvent` and `EventType`: `Bit` publishes `BIT_EATEN`, and `ScoreBoard` and `LevelManager` react |
| Factory | `LevelFactory` builds a `Level` from a `.maze` file |
| Sealed hierarchies + pattern matching | `Bug` and `Pickup` are `sealed`; `BugView` and `PickupView` use pattern-matching `switch` over them, with no `default` branch |
| Records | `Position`, `GameEvent`, `Level`, `WallOutline.Segment` |
| Enums | `Direction`, `CellType`, `EventType`, `PhaseKind`, `Look` |
| Exceptions | `InvalidMazeException` (checked), shown to the player by `ErrorDialog` |
| Collections | `List`, `Map`/`IdentityHashMap` (view lookup per model object), `Queue`/`ArrayDeque` (BFS in `Maze`) |

`ArchitectureTest` checks that nothing in `bughunt.model` imports JavaFX or FXGL, so the rules model stays pure Java and is unit-tested without a window.

## What changed from the terminal version

- **Windowed FXGL app** instead of an ANSI terminal; FXGL's default main menu and pause menu replace the hand-made screen stack.
- **Neon circuit-board visuals**: glowing wall outlines (`WallOutline`, `MazeRenderer`) and vector-drawn actors (`DebuggerView`, `BugView`, `PickupView`).
- **Smooth movement**: actors glide between tiles. The view trails the model by up to one tile (`ActorTrail`), so sprites never cut into walls.
- **HUD** (`Hud`) with score, high score, level, lives, current wave and bits left.
- **English** names everywhere; the match flow became the `GamePhase` state machine in `MatchSession`.
- The rules are unchanged.

## MVP scope / roadmap

This MVP leaves out (see the spec, `docs/superpowers/specs/2026-10-28-bug-hunt-fxgl-design.md`):

- persistent top-5 scores with name entry;
- a settings screen with key rebinding;
- synthesised audio;
- mazes 2 and 3;
- a custom splash screen, custom menus and a How-to-Play screen;
- particles, screen shake and floating score text.

## Customising

- **Maze:** edit `src/main/resources/assets/levels/maze1.maze`. Legend (same as project-1's `labirinto.txt`): `#` wall, `.` bit, `o` breakpoint, space corridor, `T` tunnel, `-` backlog door, `H` backlog, `P` Debugger, `C` coffee, `1` to `4` bugs. Every line must have the same length. An invalid starting maze, or a missing maze file, is reported in an error dialog at startup. A malformed later maze ends the match when its level loads.
- **More mazes:** add the file next to `maze1.maze` and list it in `MazeLibrary.MAZE_FILES`. Level *n* uses index `((n-1)/2) % count`, so each maze lasts two levels and the list rotates. With one maze, every level uses `maze1`.
- **Colours:** the palette lives in `view/Theme.java`.
