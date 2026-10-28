# Bug Hunt (FXGL port) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Port the terminal Pac-Man clone in `project-1/` to a windowed FXGL game, *Bug Hunt*, in `project-2/`, with menus, neon vector visuals, synthesised audio, settings and a top-5 table.

**Architecture:** A pure-Java rules model (`bughunt.model`, translated from `project-1`, ticked at a fixed 60 Hz) plus FXGL presentation layers (`view`, `ui`, `audio`) that read the model and subscribe to its `EventBus`. `BugHuntApp` is the only class that wires all the layers together.

**Tech Stack:** Java 21, FXGL 21.1, Maven 3.9.16 via the script-only wrapper, JUnit Jupiter 5.14.4, Surefire 3.6.1, javafx-maven-plugin 0.0.8.

**Spec:** `docs/superpowers/specs/2026-10-28-bug-hunt-fxgl-design.md`. Read the § it cites for your task. **Rule authority:** `project-1/src/cacabugs/**` is authoritative for every gameplay rule and value (spec §5). When porting a class, open its project-1 original and translate it faithfully.

## MVP re-scope (owner decision, 2026-10-28, after Task 2)

The owner switched the project to an MVP. **Kept:** Tasks 1–3 (1–2 already done), Task 6 (slim), Task 7 (+ minimal HUD), Task 12. **Dropped:** Tasks 4, 5, 8, 9, 10, 11.
- Task 8 (audio) was not named by the owner; the Producer dropped it as outside the MVP (ledger ruling).
- **No persistence:** the high score is in-memory for the session, there are no settings, and controls are fixed (WASD + arrows, P/Esc pause, F11 fullscreen).
- **Menus are FXGL's built-in defaults:** the main menu is FXGL's default, and the game menu (P/Esc) is FXGL's default pause menu.
- Only `maze1` exists; `MazeLibrary.MAZE_FILES` stays `List.of("maze1.maze")`, so every level uses maze1.
- The spec sections covering dropped features (§4.1 settings and rebinding, §4.2 custom screens, §4.4 audio, §5.5 items 1–2, §6) are deferred and out of scope for this branch.

## Global Constraints

- All new code lives in `project-2/`. Never modify `project-1/`. Run every Maven command from `project-2/` with `./mvnw`.
- Java release 21; `com.github.almasb:fxgl:21.1`; root package `bughunt`.
- Everything is **English**: identifiers, comments, UI text, log messages, exception messages, README.
- `bughunt.model..` must not import `javafx.*` or `com.almasb.*` (enforced by `ArchitectureTest`).
- The model is ticked at 60 ticks/s (`Ticks.PER_SECOND = 60`); all durations are in ticks via `Ticks.of(seconds)`.
- Model accessors are record-style (`position()`, `direction()`, `progress()`, `lives()`), with no `get` prefix.
- Maze files use the `.maze` extension under `src/main/resources/assets/levels/` (the repo `.gitignore` ignores `*.txt` and `*.jar`; never commit a jar).
- Window 1280×720; tile 24 px; layout constants live only in `bughunt.view.Theme`.
- Persistence lives under `${user.home}/.bughunt/` (`scores.properties`, `settings.properties`). Corrupt or unwritable files must never crash the game.
- A missing asset logs a warning and the game continues.
- Match the OO style of project-1: small focused classes, Javadoc on each type explaining its role, private fields, and patterns kept explicit (State, Template Method, Observer, Factory).
- Comment density similar to project-1: one Javadoc line or paragraph per type and per non-obvious method, nothing more.
- Commits: small, conventional (`feat:`, `test:`, `chore:`, `docs:`), each ending with the trailer `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.

## Review Focus

1. **Corrupt or unwritable save files** (garbage in `scores.properties`, a directory where the file should be, a read-only dir): the game starts with defaults and logs a warning. Tests in Task 4.
2. **Hostile name entry** (empty, spaces, lowercase, emoji, more than 8 characters): it is sanitised to `A–Z0–9`, max 8, and blank becomes `PLAYER`. Tests in Task 4.
3. **Long runs** (level 20+): coffee is capped at 5000, the breakpoint never drops below 1 s, the maze rotation keeps cycling. Tests in Tasks 1 and 5.
4. **Frame stalls** (window drag, GC pause, tpf = 2 s): at most 5 ticks run per frame, and the leftover time is discarded instead of banked. Test in Task 3.
5. **Quit to menu, then PLAY again**: a completely fresh match (score 0, 3 lives, level 1, maze1), with no listeners or effects leaking from the old match. Test in Task 3 (fresh `MatchSession`); checked again in Task 7 (no static state) and Task 11 (QA run).

---

## File structure (locked)

```
project-2/
  pom.xml  mvnw  mvnw.cmd  .mvn/wrapper/maven-wrapper.properties  README.md
  tools/SoundForge.java
  src/main/java/bughunt/
    BugHuntApp.java  GameControls.java
    model/core/      Direction, Position, Ticks, Updatable, Entity, Match, FixedStepClock
    model/events/    EventType, GameEvent, EventListener, EventBus
    model/actors/    Actor, Debugger, Bug (sealed), NullPointer, RaceCondition, Deadlock, Heisenbug
    model/pickups/   Pickup (sealed), Bit, Breakpoint, Coffee
    model/modes/     BugMode, Look, HostileMode, Scatter, Chase, Vulnerable, Captured, InBacklog
    model/maze/      CellType, Cell, Maze, Level, LevelFactory, InvalidMazeException, MazeLibrary
    model/rules/     LevelConfig, ScoreBoard, ModeController, CollisionManager, LevelManager
    model/flow/      PhaseKind, GamePhase, Ready, Playing, LifeLost, LevelCleared, GameOver, MatchSession
    model/persistence/ HighScoreEntry, HighScoreTable, Control, Settings, SaveFiles
    view/            Theme, WallOutline, MazeRenderer, DebuggerView, BugView, PickupView, Effects, MatchView
    ui/              ErrorDialog, HowToPlayCopy, SettingsScreen, HighScoresScreen, HowToPlayScreen,
                     NameEntryDialog, Hud, SplashIntro, MainMenu, PauseMenu, BugHuntSceneFactory, GameOverFlow
    audio/           Sfx, AudioDirector
    qa/              QaShots
  src/main/resources/assets/levels/maze{1,2,3}.maze  sounds/*.wav  music/*.wav
  src/test/java/bughunt/...  (mirrors main packages)
```

---

### Task 1: Maven scaffold, core value types, events, LevelConfig  — *Lead Engineer*

**Files:**
- Create: `project-2/pom.xml`, `project-2/mvnw`, `project-2/mvnw.cmd`, `project-2/.mvn/wrapper/maven-wrapper.properties`
- Create: `project-2/src/main/java/bughunt/model/core/{Direction,Position,Ticks}.java`
- Create: `project-2/src/main/java/bughunt/model/events/{EventType,GameEvent,EventListener,EventBus}.java`
- Create: `project-2/src/main/java/bughunt/model/rules/LevelConfig.java`
- Test: `project-2/src/test/java/bughunt/ArchitectureTest.java`, `.../model/core/PositionTest.java`, `.../model/events/EventBusTest.java`, `.../model/rules/LevelConfigTest.java`

**Interfaces:**
- Produces:
  - `enum Direction { UP, LEFT, DOWN, RIGHT; int dx(); int dy(); Direction opposite(); }`. The declaration order is the tie-break order (from `Direcao`).
  - `record Position(int x, int y) { Position move(Direction d); Position move(Direction d, int tiles); double distanceTo(Position o); }`
  - `final class Ticks { static final int PER_SECOND = 60; static int of(double seconds); static int toSecondsCeil(int ticks); }`
  - `enum EventType { BIT_EATEN, BREAKPOINT_EATEN, COFFEE_EATEN, COFFEE_SPAWNED, BUG_CAPTURED, BUG_RELEASED, DEBUGGER_CAUGHT, EXTRA_LIFE, ROUND_STARTED, LEVEL_CLEARED, GAME_OVER }`
  - `record GameEvent(EventType type, Position position, int value)`. `position` may be null.
  - `@FunctionalInterface interface EventListener { void onEvent(GameEvent e); }`
  - `class EventBus { void subscribe(EventType t, EventListener l); void publish(GameEvent e); }`. It iterates over a copy of the listener list (as `CentralDeEventos` does).
  - `final class LevelConfig` with `static LevelConfig forLevel(int level)` (clamps to ≥1), `number()`, `debuggerSpeed()`, `bugSpeed()`, `vulnerableBugSpeed()`, `tunnelBugSpeed()`, `backlogSpeed()`, `capturedBugSpeed()`, `breakpointDuration()` (ticks), `flashingTicks()`, `coffeeValue()`, `int[] wavesInTicks()`, `releaseDelay(int order)` (ticks). It is a faithful port of `ConfiguracaoDaFase`.

- [ ] **Step 1: Scaffold Maven.** Maven is not installed. Download Apache Maven 3.9.16 (`https://repo1.maven.org/maven2/org/apache/maven/apache-maven/3.9.16/apache-maven-3.9.16-bin.tar.gz`) into a scratch dir **outside the repo**. In `project-2/`, run `<scratch>/apache-maven-3.9.16/bin/mvn -N org.apache.maven.plugins:maven-wrapper-plugin:3.3.4:wrapper -Dtype=only-script -Dmaven=3.9.16`. Write `pom.xml` with:
  - `groupId=bughunt`, `artifactId=bug-hunt`, `version=1.0.0`, `maven.compiler.release=21`, UTF-8.
  - dep `com.github.almasb:fxgl:21.1`; `org.junit:junit-bom:5.14.4` (import) + `junit-jupiter` (test).
  - `maven-surefire-plugin:3.6.1`.
  - `org.openjfx:javafx-maven-plugin:0.0.8` with `<mainClass>bughunt.BugHuntApp</mainClass>` and `<commandlineArgs>${qa.args}</commandlineArgs>`, plus an empty `<qa.args></qa.args>` property.

  Confirm `git status` shows no `.jar` under `.mvn/`.
- [ ] **Step 2: Write failing tests.**
  - `ArchitectureTest.modelHasNoEngineImports`: walk `src/main/java/bughunt/model` (if the directory exists) and assert no line starts with `import javafx` or `import com.almasb`. The failure message names the file.
  - `PositionTest`: `new Position(3,4).move(Direction.LEFT, 4)` equals `(-1,4)`; `Direction.UP.opposite()==DOWN`; `distanceTo` of (0,0)→(3,4) is 5.0; `Direction.values()` is `[UP, LEFT, DOWN, RIGHT]`.
  - `EventBusTest`: a listener receives only its type; a listener that subscribes another listener during `publish` does not get a ConcurrentModificationException.
  - `LevelConfigTest`:
    - L1 `debuggerSpeed()==6.8` and `bugSpeed()==6.375` (delta 1e-9); `capturedBugSpeed()==13.6`.
    - L3 `breakpointDuration()==240`; L20 `breakpointDuration()==60`; `flashingTicks()` at L1 is 120 and at L6 is 30.
    - `coffeeValue()` at L1 is 100 and at L20 is 5000.
    - `wavesInTicks()` at L1 has length 8, starts `420, 1200` and ends `Integer.MAX_VALUE`; at L5 it starts `300`.
    - `releaseDelay(0)==0`; L1 `releaseDelay(2)==300`; L5 `releaseDelay(3)==300`.
    - `forLevel(0).number()==1`.
- [ ] **Step 3: Run `./mvnw -q test`.** Expected: compilation FAIL (types missing). This also proves the wrapper downloads Maven, FXGL and JUnit.
- [ ] **Step 4: Implement the types listed under Interfaces.**
- [ ] **Step 5: Run `./mvnw -q test`.** Expected: BUILD SUCCESS, all tests pass.
- [ ] **Step 6: Commit** `chore: scaffold project-2 Maven build` and `feat(model): core types, events and level config`.

### Task 2: Rules model port — maze, actors, pickups, modes, rules, Match  — *Lead Engineer*

Port the rest of `project-1`'s model faithfully, translated to English per spec §5.1. Drop every `desenhar`, `Tela`, `Sprite`, `Camada`, `Desenhavel` and `TextoFlutuante` (the view handles those). `GerenciadorDeRecursos` is replaced by `MazeLibrary`.

**Files:**
- Create: `project-2/src/main/java/bughunt/model/core/{Updatable,Entity,Match}.java`
- Create: `.../model/maze/{CellType,Cell,Maze,Level,LevelFactory,InvalidMazeException,MazeLibrary}.java`
- Create: `.../model/actors/{Actor,Debugger,Bug,NullPointer,RaceCondition,Deadlock,Heisenbug}.java`
- Create: `.../model/pickups/{Pickup,Bit,Breakpoint,Coffee}.java`
- Create: `.../model/modes/{BugMode,Look,HostileMode,Scatter,Chase,Vulnerable,Captured,InBacklog}.java`
- Create: `.../model/rules/{ScoreBoard,ModeController,CollisionManager,LevelManager}.java`
- Create: `project-2/src/main/resources/assets/levels/maze1.maze` (byte-for-byte copy of `project-1/src/recursos/labirinto.txt`)
- Test: `project-2/src/test/java/bughunt/model/...` (`LevelFactoryTest`, `MazeTest`, `BugTargetTest`, `ModeControllerTest`, `ScoreBoardTest`, `LevelManagerTest`, `MatchTest`, `MazeLibraryTest`), plus a shared test helper `TestMatches` with `static Match maze1Match()` (builds a `Match` from maze1 at level 1).

**Interfaces:**
- Consumes: everything from Task 1.
- Produces (later tasks rely on these exact names):
  - `interface Updatable { void update(Match match); }`
  - `abstract class Entity implements Updatable`: `position()`, `isActive()`, `deactivate()`, `void onRoundReset(Match m)` (default no-op).
  - `abstract class Actor extends Entity`: `direction()`, `double progress()` (0.0 ≤ p < 1.0, fraction toward the next tile), `start()`, `setStart(Position)`, `returnToStart()`, `protected void advance(double tilesPerSecond, Match m)`, `protected abstract boolean step(Match m)`, `protected void move(Maze maze)`.
  - `class Debugger extends Actor`: `LIVES_AT_START = 3`, `requestDirection(Direction)`, `lives()`, `hasLives()`, `loseLife()`, `gainLife()`.
  - `sealed abstract class Bug extends Actor permits NullPointer, RaceCondition, Deadlock, Heisenbug`:
    - `abstract Position computeTarget(Match m)`; `protected final Direction chooseDirection(Match m)` (Template Method).
    - `name()`, `scatterCorner()`, `mode()`, `Look look(Match m)`, `switchTo(BugMode)`, `reverseDirection()`.
    - `onTouch(Match)`, `onWaveChange(BugMode next)`, `onBreakpointStart()`, `onBreakpointEnd(BugMode global)`.
  - `enum Look { NORMAL, VULNERABLE, FLASHING, EYES }`. `Vulnerable.look` returns `FLASHING` only during the *white* half-period (ending phase && `(tick / Ticks.of(0.2)) % 2 == 0`), otherwise `VULNERABLE`. `Captured` returns `EYES`. All other modes return `NORMAL` (the default method).
  - `interface BugMode`:
    - `target(Bug, Match)`, `onTouch(Bug, Match)`, `description()`.
    - defaults: `speed`, `look`, `passesDoor()`, `usesShortestPath()`, `slowsInTunnel()`, `update`, `onWaveChange`, `onBreakpointStart`, `onBreakpointEnd`.
  - `sealed abstract class Pickup extends Entity permits Bit, Breakpoint, Coffee`: `points()`, `abstract void onCollected(Match m)`, `protected void collect(Match m, EventType t)`. Constants `Bit.POINTS = 10`, `Breakpoint.POINTS = 50`.
  - `enum CellType { WALL, CORRIDOR, TUNNEL, DOOR, BACKLOG; boolean isPassable(boolean backlogAccess); }`; `class Cell { type(); pickup(); place(Pickup); }`.
  - `class Maze`:
    - `Maze(Cell[][] cells, Position backlogExit, Position backlogCenter, Position coffeeSpot)` (`cells[y][x]`).
    - `width()`, `height()`, `backlogExit()`, `backlogCenter()`, `coffeeSpot()`.
    - `cell(Position)`, `neighbor(Position, Direction)` (wraps), `isPassable(Position, boolean backlogAccess)`, `isTunnel(Position)`.
    - `Direction shortestPathDirection(Position from, Position to, boolean backlogAccess)` (BFS, null if none or same tile).
  - `record Level(Maze maze, Position debuggerStart, List<Bug> bugs, List<Pickup> pickups)`
  - `class LevelFactory { Level create(List<String> lines) throws InvalidMazeException; }`. Error messages are in English with 1-based line/column numbers (e.g. `"Line 2 has 27 characters; all lines must have 28."`).
  - `class InvalidMazeException extends Exception` (checked).
  - `class MazeLibrary`:
    - `static final List<String> MAZE_FILES = List.of("maze1.maze")` (Task 5 extends it); `static MazeLibrary fromClasspath() throws InvalidMazeException` (reads `/assets/levels/<file>` as UTF-8, drops trailing empty lines; a missing resource throws InvalidMazeException naming the file).
    - `MazeLibrary(List<List<String>> mazes)`, `List<String> mazeFor(int level)`, `int mazeCount()`, `static int indexFor(int level, int mazeCount)` returning `((level - 1) / 2) % mazeCount`.
  - `class ScoreBoard`: `EXTRA_LIFE_AT = 10_000`, `ScoreBoard(int bestScore)`, `newMatch(EventBus)`, `add(int)` (the only way points change), `int registerCapture()` (200/400/800/1600, reset by a breakpoint), `score()`, `highScore()` (= max(best, score)), `beatHighScore()`.
  - `class ModeController`:
    - `start(LevelConfig)`, `BugMode newGlobalMode()`, `update(Match)`, `activateBreakpoint(Match)`.
    - `isBreakpointActive()`, `isVulnerableEnding()`, `int breakpointTicksLeft()`, `int breakpointDurationTicks()`.
    - `String waveName()` returns `"SCATTER"` / `"CHASE"`.
  - `class LevelManager`: `COFFEE_AT = {70, 170}`, `LevelManager(MazeLibrary)`, `connect(Match)`, `load(int level, Match) throws InvalidMazeException` (uses `library.mazeFor(level)`), `number()`, `remaining()`, `config()`. Publishes `COFFEE_SPAWNED` (position = coffee spot) when coffee appears.
  - `class CollisionManager { void check(Match m); }`
  - `final class Match` (port of `Partida`, rendering removed):
    - `Match(MazeLibrary library, ScoreBoard score) throws InvalidMazeException`.
    - Lifecycle: `tick()`, `resetRound()` (publishes `ROUND_STARTED`), `advanceLevel() throws InvalidMazeException`, `install(Level, LevelConfig)`.
    - Called by entities: `captureDebugger()` (publishes `DEBUGGER_CAUGHT` once), `addPickup(Pickup)`, `markLevelCleared()` (publishes `LEVEL_CLEARED`).
    - Queries: `debuggerWasCaught()`, `levelWasCleared()`, `isFrozen()`, `maze()`, `debugger()`, `List<Bug> bugs()`, `List<Pickup> pickups()` (active only, unmodifiable), `events()`, `modes()`, `score()`, `config()`, `random()`, `long tickCount()`, `levelNumber()`, `pickupsRemaining()`.
    - The 0.5 s capture freeze (`Ticks.of(0.5)`) stays in `Match` on `BUG_CAPTURED`.
  - `InBacklog` publishes `BUG_RELEASED` (position = the bug's tile) when it hands over to the global mode.
  - The `VIDA_EXTRA → debugger.gainLife()` and `BREAKPOINT → modes.activateBreakpoint` subscriptions stay in `Match`, as in `Partida`.

- [ ] **Step 1: Write failing tests** (maze1 facts: 28×23, 206 bits, 4 breakpoints, `P`(13,17), `C`(13,13), bug starts `1`(13,8) `2`(13,10) `3`(11,10) `4`(16,10), doors (13,9),(14,9)):
  - `LevelFactoryTest`:
    - maze1 → `width()==28`, `height()==23`, `debuggerStart()==(13,17)`, 4 bugs, `pickups().size()==210`, `backlogExit()==(13,8)`, `backlogCenter()==(13,10)`, `coffeeSpot()==(13,13)`.
    - A line of unequal length throws with a message containing `"Line 2"`; an unknown char `'X'` throws with `"column"` in the message; a maze without `P` throws.
  - `MazeTest`: `neighbor((0,10), LEFT)==(27,10)` (tunnel wrap); `isPassable` of a DOOR cell is false without backlog access and true with it; `shortestPathDirection((13,8),(13,10),true)==DOWN`.
  - `BugTargetTest`, using `TestMatches.maze1Match()` after `resetRound()`, with the debugger at (13,17) facing LEFT:
    - NullPointer target is (13,17); RaceCondition target is (9,17).
    - Deadlock target is (9,26) (NullPointer at (13,8)).
    - Heisenbug at (16,10) is 7.6 tiles away, so its target is its scatter corner (0,24). With the debugger moved via `setStart((1,1))` + `returnToStart()`, Heisenbug targets the debugger.
    - Scatter corners: NullPointer (25,-2), RaceCondition (2,-2), Deadlock (27,24), Heisenbug (0,24).
  - `ModeControllerTest` (L1):
    - The first global mode is `Scatter`; after 420 `update` calls it is `Chase`, and bugs outside the backlog got a reverse request.
    - After `activateBreakpoint`, 1000 updates do not advance the wave.
    - `breakpointDurationTicks()==360` at L1.
    - When the breakpoint ends, vulnerable bugs return to the global mode.
  - `ScoreBoardTest`: four `registerCapture()` calls return 200, 400, 800, 1600, and the fifth returns 1600. Publishing `BREAKPOINT_EATEN` resets the chain to 200. `EXTRA_LIFE` is published exactly once when crossing 10,000. `highScore()` = max(best, score).
  - `LevelManagerTest`: publishing 70 `BIT_EATEN` events puts a `Coffee` in `match.pickups()` at (13,13) and publishes `COFFEE_SPAWNED`. Collecting all 210 marks the level cleared (`LEVEL_CLEARED` published once).
  - `MatchTest`:
    - After `resetRound()` a `ROUND_STARTED` event was published.
    - Running 60 ticks moves the debugger from (13,17) to the left with `progress()` in [0,1).
    - Touching a hostile bug sets `debuggerWasCaught()` and publishes `DEBUGGER_CAUGHT` exactly once.
    - Touching a vulnerable bug makes it `Captured`, scores 200, and `isFrozen()` is true for 30 ticks.
  - `MazeLibraryTest`: `indexFor(1,3)==0`, `indexFor(2,3)==0`, `indexFor(3,3)==1`, `indexFor(5,3)==2`, `indexFor(7,3)==0`, `indexFor(40,3)==1`. `fromClasspath().mazeCount()==MAZE_FILES.size()`. A missing file name throws `InvalidMazeException`.
- [ ] **Step 2: Run `./mvnw -q test`.** Expected: FAIL (types missing).
- [ ] **Step 3: Implement** the Interfaces above by translating the project-1 originals (spec §5.1 mapping). Keep `Bug.chooseDirection` `final` and keep project-1's tie-break order. The model must still pass `ArchitectureTest`.
- [ ] **Step 4: Run `./mvnw -q test`.** Expected: all pass.
- [ ] **Step 5: Commit** in coherent slices (maze, actors+modes, rules+Match), e.g. `feat(model): port maze and level factory`.

### Task 3: Match flow phases, session and fixed-step clock  — *Gameplay Programmer*

**Files:**
- Create: `project-2/src/main/java/bughunt/model/flow/{PhaseKind,GamePhase,Ready,Playing,LifeLost,LevelCleared,GameOver,MatchSession}.java`, `.../model/core/FixedStepClock.java`
- Test: `.../model/flow/MatchSessionTest.java`, `.../model/core/FixedStepClockTest.java`

**Interfaces:**
- Consumes: `Match`, `MazeLibrary`, `ScoreBoard`, `Debugger`, `EventBus`, `Ticks` (Tasks 1–2).
- Produces:
  - `enum PhaseKind { READY, PLAYING, LIFE_LOST, LEVEL_CLEARED, GAME_OVER }`
  - `interface GamePhase { default void enter(MatchSession s) {} GamePhase tick(MatchSession s); PhaseKind kind(); int elapsedTicks(); }`. This is the State pattern: `tick` returns the next phase or `this`. `MatchSession` calls `enter` on every phase change.
  - `Ready`: `DURATION = Ticks.of(2)` → `Playing`.
  - `Playing`: calls `match.tick()`. Caught → `LifeLost`, else cleared → `LevelCleared`.
  - `LifeLost`: `FROZEN = Ticks.of(0.7)`, `SPINNING = Ticks.of(1.7)`, `DURATION = Ticks.of(2.4)`. At the end: `loseLife()`; if lives remain, `match.resetRound()` → `Ready`, else → `GameOver`.
  - `LevelCleared`: `STILL = Ticks.of(1)`, `DURATION = Ticks.of(3)`, `FLASH = Ticks.of(0.2)`, `boolean mazeFlashOn()` = `elapsed > STILL && (elapsed / FLASH) % 2 == 0`. At the end: `match.advanceLevel()` → `Ready`; an `InvalidMazeException` → `GameOver`.
  - `GameOver`: `enter` publishes `GAME_OVER` (value = final score); `tick` returns `this`.
  - `final class MatchSession`: `MatchSession(MazeLibrary library, int bestScore) throws InvalidMazeException`, `void tick()`, `void requestDirection(Direction d)` (forwarded only in READY or PLAYING), `GamePhase phase()`, `Match match()`, `boolean isOver()`.
  - `final class FixedStepClock`: `FixedStepClock(int ticksPerSecond, int maxTicksPerFrame)`, `int advance(double seconds)` returns how many ticks to run now. Above the cap it discards the backlog (it does not bank it).

- [ ] **Step 1: Write failing tests.**
  - `MatchSessionTest`:
    - A new session is READY at level 1, score 0, 3 lives, and `match().maze().width()==28`.
    - After 120 ticks the phase is PLAYING.
    - A `requestDirection(UP)` during READY is honoured once PLAYING starts. Assert that the debugger's next turn is UP: tick until it moves, then check `direction()`.
    - Forcing a catch: drive `match().captureDebugger()` in PLAYING, then one tick → LIFE_LOST; 144 ticks later → READY with 2 lives.
    - With 1 life left the same sequence ends in GAME_OVER, `isOver()` is true, and `GAME_OVER` is published once with value = score.
    - Clearing: `match().markLevelCleared()`, tick → LEVEL_CLEARED; `mazeFlashOn()` is false at elapsed 30 and true at elapsed 72 (72/12=6, even, >60); after 180 ticks → READY at level 2.
    - If caught and cleared happen on the same tick → LIFE_LOST first.
    - Two sessions built one after the other share no state (the second has score 0 after the first scored).
  - `FixedStepClockTest`: `(60,5)`: `advance(1/60.0)` → 1; `advance(0.5/60)` twice → 0 then 1; `advance(2.0)` → 5 and the next `advance(0)` → 0 (backlog discarded).
- [ ] **Step 2: Run `./mvnw -q test -Dtest='MatchSessionTest,FixedStepClockTest'`.** Expected: FAIL (types missing).
- [ ] **Step 3: Implement** the Interfaces above. Phase timings are spec §4.2 / project-1 `estados/`.
- [ ] **Step 4: Run `./mvnw -q test`.** Expected: all pass.
- [ ] **Step 5: Commit** `feat(model): match flow phases and fixed-step clock`.

### Task 4: Persistence — high scores and settings  — *UI/UX Developer*

**DROPPED in the MVP re-scope — do not implement.**

**Files:**
- Create: `project-2/src/main/java/bughunt/model/persistence/{HighScoreEntry,HighScoreTable,Control,Settings,SaveFiles}.java`
- Test: `.../model/persistence/{HighScoreTableTest,SaveFilesTest}.java`

**Interfaces:**
- Produces:
  - `record HighScoreEntry(String name, int score)`
  - `class HighScoreTable`:
    - `CAPACITY = 5`, `MAX_NAME = 8`, `DEFAULT_NAME = "PLAYER"`.
    - `List<HighScoreEntry> entries()` (unmodifiable, score descending; ties keep the earlier entry first).
    - `boolean qualifies(int score)`: score > 0 and (size < 5 or score > lowest).
    - `void add(String rawName, int score)`: ignored if it doesn't qualify; the name passes through `sanitize`.
    - `int best()`: 0 when empty.
    - `static String sanitize(String raw)`: uppercase, keep `A–Z0–9` only, max 8; blank/null → `PLAYER`.
  - `enum Control { UP, DOWN, LEFT, RIGHT, PAUSE }`
  - `class Settings`:
    - `double musicVolume()` / `setMusicVolume(double)`, `sfxVolume()` / `setSfxVolume(double)`: clamped to [0,1]; defaults 0.6 and 0.8.
    - `boolean fullscreen()` / `setFullscreen(boolean)`: default false.
    - `String key(Control)` / `setKey(Control, String)`: JavaFX `KeyCode` *names* as plain strings; defaults `W, S, A, D, P`.
  - `class SaveFiles`:
    - `SaveFiles(Path dir)`, `static SaveFiles inUserHome()` (`${user.home}/.bughunt`).
    - `HighScoreTable loadScores()`, `void saveScores(HighScoreTable)`, `Settings loadSettings()`, `void saveSettings(Settings)`.
    - Uses `java.util.Properties`. Keys: `n.name` / `n.score` for n = 1..5; `musicVolume`, `sfxVolume`, `fullscreen`, `key.UP` … `key.PAUSE`.
    - Every IO or parse problem → defaults (or a skipped entry) plus a `System.Logger` WARNING. It never throws. The directory is created on save.

- [ ] **Step 1: Write failing tests.**
  - `HighScoreTableTest`:
    - Adding 7 scores keeps the top 5 sorted.
    - `qualifies(0)` is false; with a full table whose lowest is 300, `qualifies(300)` is false and `qualifies(301)` is true.
    - `sanitize(" ab c!é9xyzw12 ")=="ABC9XYZW"`, `sanitize("")=="PLAYER"`, `sanitize(null)=="PLAYER"`, `sanitize("😀")=="PLAYER"`.
    - Ties: adding A=500 then B=500 → A before B.
  - `SaveFilesTest` (JUnit `@TempDir`):
    - Scores and settings round-trip.
    - `scores.properties` containing `1.score=abc` plus a valid `2.*` loads only the valid entry.
    - A directory at `settings.properties`'s path → `loadSettings()` returns defaults and `saveSettings` does not throw.
    - Missing dir → defaults.
    - `setMusicVolume(1.7)` reads back 1.0.
- [ ] **Step 2: Run `./mvnw -q test -Dtest='HighScoreTableTest,SaveFilesTest'`.** Expected: FAIL.
- [ ] **Step 3: Implement** the Interfaces above.
- [ ] **Step 4: Run `./mvnw -q test`.** Expected: all pass.
- [ ] **Step 5: Commit** `feat(model): high score table and settings persistence`.

### Task 5: Mazes 2 & 3, rotation, How-to-Play copy  — *Game Designer*

**DROPPED in the MVP re-scope — do not implement.**

**Files:**
- Create: `project-2/src/main/resources/assets/levels/maze2.maze`, `maze3.maze`
- Modify: `project-2/src/main/java/bughunt/model/maze/MazeLibrary.java` (`MAZE_FILES` → `List.of("maze1.maze", "maze2.maze", "maze3.maze")`)
- Create: `project-2/src/main/java/bughunt/ui/HowToPlayCopy.java`
- Test: `project-2/src/test/java/bughunt/model/maze/MazeDesignTest.java`

**Interfaces:**
- Consumes: `LevelFactory`, `MazeLibrary`, `Maze` (Task 2).
- Produces:
  - Three valid mazes.
  - `final class HowToPlayCopy` with `static final List<Section> SECTIONS` and `record Section(String title, List<String> lines)`. Sections: **Goal** (eat every bit, avoid the bugs), **Controls** (arrows/WASD move, P/Esc pause, F11 fullscreen), **Pickups** (bit 10, breakpoint 50 + bugs vulnerable, coffee 100–5000 twice per level, captures 200/400/800/1600, extra life at 10,000), **The bugs** (the four chase rules from spec §5.3, written in-world, one line each).
  - All copy is English, with no line longer than 60 characters.

- [ ] **Step 1: Write `MazeDesignTest`** as a `@ParameterizedTest` over `MazeLibrary.MAZE_FILES`. Each maze must:
  - parse with `LevelFactory`;
  - be exactly 28×23;
  - have exactly 4 breakpoints and between 180 and 260 pickups;
  - have ≥1 TUNNEL cell on each side edge (x=0 and x=27) in the same row;
  - have rows 7–11 (the backlog block: door row, `H` rows, and the 1/2/3/4 positions) identical to maze1 in columns 9–18;
  - have every pickup reachable from the debugger start (BFS with `isPassable(p,false)`).

  Also assert that maze2 and maze3 each differ from maze1 in ≥ 40 cells, and that the outer border is wall except tunnel cells.
- [ ] **Step 2: Run `./mvnw -q test -Dtest=MazeDesignTest`.** Expected: FAIL (maze2/maze3 missing).
- [ ] **Step 3: Author maze2 and maze3.** They need distinct, symmetric (left/right mirror) layouts with fun loops and no dead ends longer than 2 tiles. Extend `MAZE_FILES`. Write `HowToPlayCopy`.
- [ ] **Step 4: Run `./mvnw -q test`.** Expected: all pass, including `MazeLibraryTest` with `mazeCount()==3`.
- [ ] **Step 5: Commit** `feat(content): mazes 2 and 3 with rotation, how-to-play copy`.

### Task 6: Neon view layer (slim, MVP)  — *Technical Artist*

**MVP slim scope:** there is no `Effects` class: no particles, screen shake, floating text, bit pop, coffee steam or leg animation. Actors are static neon shapes. Everything else in this task stands.

**Files:**
- Create: `project-2/src/main/java/bughunt/view/{Theme,WallOutline,MazeRenderer,DebuggerView,BugView,PickupView,MatchView}.java`
- Test: `.../view/WallOutlineTest.java` (pure, no JavaFX toolkit)

**Interfaces:**
- Consumes: `MatchSession`, `GamePhase`/`PhaseKind`, `LifeLost` constants, `LevelCleared.mazeFlashOn()`, `Match` queries, `Actor.position()/direction()/progress()`, `Bug.look(Match)`, the sealed `Bug`/`Pickup` hierarchies (use pattern-matching `switch`), `EventBus` events (Tasks 1–3).
- Produces:
  - `final class Theme`: `TILE = 24`, `WIDTH = 1280`, `HEIGHT = 720`, `MAZE_X = 104`, `MAZE_Y = 84`, `HUD_X = 900`, `HUD_WIDTH = 340`; colour constants (background `#07130d`, trace green, door magenta, bit warm white, breakpoint red, debugger yellow, NullPointer red, RaceCondition pink, Deadlock cyan, Heisenbug orange, vulnerable blue, flash white); `Font title(double size)` and `Font body(double size)` (a monospace family with fallback).
  - `final class WallOutline { record Segment(double x1, double y1, double x2, double y2) {} static List<Segment> compute(Maze maze); }`. Segments are in tile units along every edge between a WALL cell and a non-WALL cell; collinear neighbours are merged into one segment; edges on the map boundary (no neighbour cell) are never outlined.
  - `final class MatchView`: `MatchView(MatchSession session)`, `javafx.scene.layout.Pane root()` (sized to the maze in px, to be placed at `Theme.MAZE_X/Y`), `void update(double tpf)` (called every frame).
  - The view must not mutate the model.

  Visual behaviour (spec §4.3; timings spec §4.2):
  - Actors are drawn at `position + direction × progress`; no interpolation across a tunnel wrap.
  - Pickups are drawn from `match.pickups()`.
  - `Look` mapping: FLASHING = white, VULNERABLE = blue, EYES = eyes only.
  - READY shows "READY!" on the coffee row.
  - LIFE_LOST: everything frozen until `FROZEN`; then the bugs are hidden and the cursor spins (a quarter turn every `Ticks.of(0.08)`) until `SPINNING`, then shows a glitch "X" and the "SEGFAULT" text.
  - LEVEL_CLEARED: the traces turn white whenever `mazeFlashOn()`, and "BUILD PASSED" is shown.
  - GAME_OVER shows "GAME OVER".
  - Breakpoints pulse every 0.25 s (opacity or scale).
  - Shapes: the debugger is a yellow arrow cursor pointing along `direction()`; bugs are a simple beetle silhouette (body + head + 6 short legs) in their colour; EYES is two white eyes; the bit is a small dot; the breakpoint is a larger red pad; coffee is a small brown cup.

- [ ] **Step 1: Write `WallOutlineTest`** (build mazes with the `Maze` constructor from Task 2).
  - A 3×3 grid with only the centre WALL yields exactly the segments (1,1)-(2,1), (1,2)-(2,2), (1,1)-(1,2), (2,1)-(2,2), in any order and either endpoint order.
  - A 5×3 grid whose middle row is WALL at x=1..3 yields exactly (1,1)-(4,1), (1,2)-(4,2), (1,1)-(1,2), (4,1)-(4,2) after merging.
  - maze1 yields a non-empty list in which every segment has integer endpoints within `[0,28]×[0,23]`.
- [ ] **Step 2: Run `./mvnw -q test -Dtest=WallOutlineTest`.** Expected: FAIL.
- [ ] **Step 3: Implement** `Theme` and `WallOutline` until the test passes, then implement the remaining view classes. Glow uses `DropShadow`/`Bloom` on the trace group (not per node) so the frame rate holds.
- [ ] **Step 4: Run `./mvnw -q test` and `./mvnw -q compile`.** Expected: pass. Visual verification happens in Task 7 once the view is wired into the app.
- [ ] **Step 5: Commit** `feat(view): neon maze and actors`.

### Task 7: Game application wiring, input, minimal HUD, QA screenshot harness  — *Gameplay Programmer*

**MVP scope:** no persistence, settings or rebinding (Task 4 was dropped). Menus are FXGL's built-in defaults.

**Files:**
- Create: `project-2/src/main/java/bughunt/BugHuntApp.java`, `project-2/src/main/java/bughunt/GameControls.java`, `project-2/src/main/java/bughunt/ui/ErrorDialog.java`, `project-2/src/main/java/bughunt/ui/Hud.java`, `project-2/src/main/java/bughunt/qa/QaShots.java`

**Interfaces:**
- Consumes: `MatchSession`, `FixedStepClock`, `MazeLibrary.fromClasspath()`, `ModeController`/`ScoreBoard`/`Debugger`/`Match` queries, `MatchView`, `Theme`.
- Produces:
  - `public class BugHuntApp extends GameApplication`, with `main(String[] args)` calling `launch(args)`.
  - `initSettings`:
    - 1280×720, title "Bug Hunt", version "1.0"
    - `setMainMenuEnabled(true)` and `setGameMenuEnabled(true)`, using FXGL's default menus
    - `setManualResizeEnabled(true)`, `setPreserveResizeRatio(true)`, `setFullScreenAllowed(true)`
  - `initGame`:
    - Builds a **new** `MatchSession(MazeLibrary.fromClasspath(), sessionBest)`. `sessionBest` is an instance field holding the best score seen since the app launched (in memory only), updated when a match ends.
    - Builds a new `MatchView` placed at `Theme.MAZE_X/Y` and a new `Hud`, with the `#07130d` background.
    - An `InvalidMazeException` → `ErrorDialog.show(...)` and return to the main menu.
  - `onUpdate(tpf)` runs `clock.advance(tpf)` ticks, then `view.update(tpf)` and `hud.update()`. Any `RuntimeException` → log + `ErrorDialog.show` + main menu.
  - **Game over:** when `session.isOver()` first becomes true, record `sessionBest = max(sessionBest, score)`, wait 3 s (FXGL timer) so the "GAME OVER" view shows, then `getGameController().gotoMainMenu()`.
  - Nothing static except constants (Review Focus 5).
  - `final class GameControls`:
    - `void install(Input input, Supplier<MatchSession> session)` with fixed bindings: W/Up arrow → UP, S/Down → DOWN, A/Left → LEFT, D/Right → RIGHT (FXGL needs one action per key, so name them e.g. `"Up (W)"`, `"Up (arrow)"`); P → `gotoGameMenu()` (Esc already opens FXGL's game menu); F11 → toggle fullscreen.
    - Direction actions call `session.get().requestDirection(...)` on action begin.
  - `final class Hud`: `Hud(Supplier<MatchSession> session)`, `Region root()` placed at `Theme.HUD_X` (width `Theme.HUD_WIDTH`), `void update()` called every frame. It shows, as neon labels using `Theme` fonts and colours:
    - SCORE
    - HIGH SCORE (`match.score().highScore()`)
    - LEVEL
    - lives (number or small cursor icons)
    - wave (`BREAKPOINT` while `modes().isBreakpointActive()`, else `modes().waveName()`)
    - bits remaining (`match.pickupsRemaining()`)
    - a one-line controls hint: "WASD/Arrows move · P pause · F11 fullscreen"
  - `final class ErrorDialog { static void show(String title, String message, Runnable onClose); }`, using FXGL's dialog service.
  - `final class QaShots`:
    - Enabled when a program arg `--qa-shots=<dir>` is present (read in `BugHuntApp` from the launch args).
    - It drives a scripted run: from the main menu save `<dir>/00-main-menu.png`; start a new game; save `<dir>/01-ready.png` at 1 s and `<dir>/02-gameplay.png` at 5 s; open the game menu and save `03-pause.png`; resume; force a game over (call `session().match().debugger().loseLife()` until one life is left, then `captureDebugger()`); save `04-game-over.png` about 2.5 s later; then `exit(0)`.
    - Shots are taken via `Scene.snapshot` + `javax.imageio` (through `javafx.embed.swing.SwingFXUtils`), or FXGL's screenshot facility if simpler.
  - Run command (documented in the README by Task 12): `xvfb-run -a -s "-screen 0 1280x720x24" ./mvnw -q javafx:run -Dqa.args=--qa-shots=target/qa-shots`.

- [ ] **Step 1: Implement** the classes above. No unit tests: the logic under test lives in Task 3. This task is verified by running the game.
- [ ] **Step 2: Run `./mvnw -q test`.** Expected: all pass (no regressions).
- [ ] **Step 3: Run the harness.**

  ```bash
  cd project-2 && rm -rf target/qa-shots && xvfb-run -a -s "-screen 0 1280x720x24" ./mvnw -q javafx:run -Dqa.args=--qa-shots=target/qa-shots; echo "exit=$?"; ls target/qa-shots
  ```

  Expected: `exit=0`; `00-main-menu.png` through `04-game-over.png` exist; no `Exception` in the output. Open the PNGs (Read tool) and confirm:
  - the neon maze is drawn at the right position with the debugger and bugs visible;
  - the HUD is fully visible on the right with no clipping;
  - "READY!" appears in shot 1;
  - the actors have moved in shot 2;
  - "GAME OVER" appears in shot 4.

  If xvfb rendering fails, add `-Dprism.order=sw` via the plugin's `<options>`.
- [ ] **Step 4: Fix any visual defects you can attribute to wiring.** List view-layer defects in your report; do not rewrite the view.
- [ ] **Step 5: Commit** `feat: wire FXGL application, controls, HUD and QA screenshot harness`.

### Task 8: Audio — SoundForge, assets, AudioDirector  — *Audio Engineer*

**DROPPED in the MVP re-scope — do not implement.**

**Files:**
- Create: `project-2/tools/SoundForge.java`, `project-2/src/main/resources/assets/sounds/*.wav`, `project-2/src/main/resources/assets/music/*.wav`
- Create: `project-2/src/main/java/bughunt/audio/{Sfx,AudioDirector}.java`
- Modify: `project-2/src/main/java/bughunt/BugHuntApp.java` (create an `AudioDirector` per match in `initGame`; start game music there)
- Test: `project-2/src/test/java/bughunt/audio/SfxAssetsTest.java`

**Interfaces:**
- Consumes: `EventBus`/`EventType` (Task 1), `BugHuntApp.session()` (Task 7).
- Produces:
  - `tools/SoundForge.java`: a single-file program (`java tools/SoundForge.java src/main/resources/assets`) writing 16-bit mono 44,100 Hz PCM WAVs with square/triangle/noise oscillators and ADSR envelopes. It is deterministic (fixed seeds).
  - SFX files `sounds/{chomp,breakpoint,bug_captured,death,extra_life,level_clear,coffee,menu_move,menu_select}.wav`, each ≤ 2 s.
  - Music files `music/{menu_loop,game_loop}.wav`, 15–30 s each, loopable (the waveform ends at a zero crossing).
  - `enum Sfx { CHOMP, BREAKPOINT, BUG_CAPTURED, DEATH, EXTRA_LIFE, LEVEL_CLEAR, COFFEE, MENU_MOVE, MENU_SELECT; String file(); void play(); }`. `file()` is the lowercase name + `.wav`. `play()` uses `FXGL.play(file())` and catches/logs any failure, so a missing asset is silent.
  - `final class AudioDirector`:
    - `AudioDirector(EventBus bus)` subscribes to events: BIT_EATEN → CHOMP (alternating pitch or throttled to at most one every 0.12 s), BREAKPOINT_EATEN → BREAKPOINT, BUG_CAPTURED, DEBUGGER_CAUGHT → DEATH, EXTRA_LIFE, LEVEL_CLEARED → LEVEL_CLEAR, COFFEE_EATEN → COFFEE.
    - `static void playMenuMusic()`, `static void playGameMusic()`, `static void stopMusic()`. These loop via FXGL's music service, keep a single music track at a time, and log and carry on when an asset is missing.

- [ ] **Step 1: Write `SfxAssetsTest`.** For every `Sfx`, `src/main/resources/assets/sounds/<file()>` exists, parses with `javax.sound.sampled.AudioSystem.getAudioFileFormat`, is 44,100 Hz / 16-bit / mono, and is ≤ 2.0 s. Both music files exist and last 15–30 s.
- [ ] **Step 2: Run `./mvnw -q test -Dtest=SfxAssetsTest`.** Expected: FAIL.
- [ ] **Step 3: Write `SoundForge`, run it, and commit the generated WAVs. Implement `Sfx` and `AudioDirector`, and wire them into `BugHuntApp`.**
- [ ] **Step 4: Run `./mvnw -q test` and the Task 7 harness command.** Expected: tests pass; the harness exits 0 with no audio exceptions in the log (xvfb has no sound device; FXGL/JavaFX must not crash).
- [ ] **Step 5: Commit** `feat(audio): synthesised sfx and music with event-driven director`.

### Task 9: UI screens — settings, high scores, how-to-play, name entry  — *UI/UX Developer*

**DROPPED in the MVP re-scope — do not implement.**

**Files:**
- Create: `project-2/src/main/java/bughunt/ui/{UiKit,SettingsScreen,HighScoresScreen,HowToPlayScreen,NameEntryDialog}.java`

**Interfaces:**
- Consumes: `Settings`, `Control`, `SaveFiles`, `HighScoreTable` (Task 4); `HowToPlayCopy` (Task 5); `Theme` (Task 6); `BugHuntApp.applySettings()/saves()/settings()` (Task 7); `Sfx.MENU_MOVE/MENU_SELECT` (Task 8).
- Produces (each screen is a JavaFX `Region` sized 1280×720 that any scene can add and remove):
  - `final class UiKit`: the shared neon button/label/panel factory, used by Tasks 9 and 10. `static Button button(String text, Runnable action)`: focusable, highlighted on focus/hover, plays `MENU_MOVE` on focus and `MENU_SELECT` on fire. `static Label title(String)`, `static Region panel(double w, double h)`.
  - `SettingsScreen(Runnable onBack)`:
    - Music and SFX sliders (0–100 %), a fullscreen toggle, and five rebind rows ("press a key…" capture; Esc cancels).
    - Every change updates `settings()`, calls `saves().saveSettings(...)`, then `applySettings()`.
    - BACK button; Esc → `onBack`.
  - `HighScoresScreen(HighScoreTable table, int highlightRank, Runnable onBack)`: ranks 1–5 (empty slots shown as `---  0`). `highlightRank` (1-based, or 0 for none) pulses that row.
  - `HowToPlayScreen(Runnable onBack)`: renders `HowToPlayCopy.SECTIONS`, with a small bug glyph in each bug's colour.
  - `NameEntryDialog(int score, Consumer<String> onConfirm)`:
    - A text field limited live to `HighScoreTable.sanitize` rules (uppercase, `A–Z0–9`, max 8), pre-filled `PLAYER`.
    - Enter confirms with the sanitised name.
    - Shows "NEW HIGH SCORE" and the score.
  - Keyboard: every screen is fully navigable (Up/Down/Tab move focus, Enter activates, Esc goes back), and the mouse also works.

- [ ] **Step 1: Implement** the screens using `Theme` fonts and colours (neon on dark, consistent with spec §4.3).
- [ ] **Step 2: Extend `QaShots`** with a dev step list for these screens: after the existing steps, add each screen to the game scene's UI in turn and save `10-settings.png`, `11-highscores.png`, `12-howtoplay.png`, `13-nameentry.png`.
- [ ] **Step 3: Run `./mvnw -q test` and the harness.** Expected: exit 0, all PNGs exist. Read them and confirm the text is legible, nothing is clipped at 1280×720, and focus is visible.
- [ ] **Step 4: Commit** `feat(ui): settings, high scores, how-to-play and name entry screens`.

### Task 10: UI flow — splash, main menu, pause, HUD, game over  — *UI/UX Developer*

**DROPPED in the MVP re-scope — do not implement.**

**Files:**
- Create: `project-2/src/main/java/bughunt/ui/{BugHuntSceneFactory,SplashIntro,MainMenu,PauseMenu,Hud,GameOverFlow}.java`
- Modify: `project-2/src/main/java/bughunt/BugHuntApp.java` (scene factory, intro and menus enabled, HUD added in `initGame`/`initUI`, game-over listener), `project-2/src/main/java/bughunt/qa/QaShots.java`

**Interfaces:**
- Consumes: Task 9 screens, `UiKit`, `BugHuntApp` accessors, `MatchSession`/`ModeController`/`ScoreBoard`/`Debugger` queries, `AudioDirector.playMenuMusic()/playGameMusic()`.
- Produces:
  - `BugHuntSceneFactory extends SceneFactory`: `newIntro()` → `SplashIntro`, `newMainMenu()` → `MainMenu`, `newGameMenu()` → `PauseMenu`.
  - `SplashIntro extends IntroScene`: the "Bug Hunt" logo glitches in, ~2 s, any key skips, then `finishIntro()`.
  - `MainMenu extends FXGLMenu`:
    - PLAY (`fireNewGame()`), HIGH SCORES, SETTINGS, HOW TO PLAY (each swaps in the Task 9 screen with `onBack` returning to the menu), QUIT (`fireExit()`).
    - Decorative bugs wander behind a translucent panel.
    - Plays menu music on show.
  - `PauseMenu extends FXGLMenu`: RESUME (`fireResume()`), SETTINGS, QUIT TO MENU (`fireExitToMainMenu()`); title "PAUSED".
  - `Hud`: `Hud(Supplier<MatchSession>, HighScoreTable)`, `Region root()` at `Theme.HUD_X`, `void update()` called every frame.
    - Shows SCORE; HIGH SCORE (= `match.score().highScore()`); LEVEL; lives icons; wave (`BREAKPOINT` while active, else `waveName()`); a breakpoint bar (`breakpointTicksLeft / breakpointDurationTicks`); bits remaining.
  - `GameOverFlow`: `static void start(BugHuntApp app)`, registered via `addGameOverListener`.
    - It waits 2 s on the "GAME OVER" view.
    - If `highScores().qualifies(score)`, it shows `NameEntryDialog` → `add` + `saveScores`.
    - Then `HighScoresScreen` with the new rank highlighted → on back, `gotoMainMenu()`.
- [ ] **Step 1: Implement** the above and switch `BugHuntApp` to the custom scene factory with intro, main menu and game menu enabled.
- [ ] **Step 2: Rewrite the QaShots script** for the real flow: skip the intro → save `00-main-menu.png` → PLAY → `01-ready.png` → `02-gameplay.png` (with HUD) → pause → `03-pause.png` → force game over by calling `session().match().debugger().loseLife()` until one life is left, then `captureDebugger()` → `04-game-over.png` → name entry shot `05-name-entry.png` (the screens from Task 9's dev steps may be removed or kept) → exit 0.
- [ ] **Step 3: Run `./mvnw -q test` and the harness.** Expected: exit 0. Read every PNG and check it against spec §4.1–4.2: HUD fields present, menus legible, the splash skipped cleanly.
- [ ] **Step 4: Commit** `feat(ui): splash, menus, HUD and game-over flow`.

### Task 11: QA smoke script and playtest report  — *QA Engineer*

**DROPPED in the MVP re-scope — do not implement.**

**Files:**
- Create: `project-2/qa/smoke.sh`, `project-2/qa/PLAYTEST.md`
- Modify: `project-2/src/main/java/bughunt/qa/QaShots.java` (only if a check below needs another shot)

**Interfaces:**
- Consumes: the harness command from Task 7, the script steps from Task 10.
- Produces:
  - `qa/smoke.sh`: `set -e`; runs `./mvnw -q test`, then the xvfb harness into `target/qa-shots`. It fails if the exit code ≠ 0, any expected PNG is missing, or the log contains `Exception` / `ERROR`. It prints `SMOKE OK` on success.
  - `qa/PLAYTEST.md`: a checklist of each spec §4 requirement and success criterion (spec §1) with PASS/FAIL and evidence (screenshot name or test name), plus a **Defects** list with severity (Critical/Important/Minor) and repro steps.
- [ ] **Step 1: Write `smoke.sh` and run it.** Expected: `SMOKE OK`.
- [ ] **Step 2: Inspect every screenshot and run additional scripted checks via `QaShots.addStep` where needed.** Needed checks: a fresh state after QUIT TO MENU → PLAY (Review Focus 5), the level-2 transition, and the vulnerable/flashing bugs.
- [ ] **Step 3: Write `PLAYTEST.md`.** Do not fix product code; defects are routed by the Producer.
- [ ] **Step 4: Commit** `test(qa): smoke script and playtest report`.

### Task 12: README with OO-concepts map  — *Technical Writer*

**Files:**
- Create: `project-2/README.md`

**Interfaces:**
- Consumes: the final code (read it — describe only what exists in this MVP), spec §1/§4/§5, `project-1/README.md` (structure to mirror, translated to English).
- Produces: `project-2/README.md` with these sections:
  - **Bug Hunt** pitch;
  - **How to run** (JDK 21+, `./mvnw javafx:run`, `./mvnw test`, the QA harness command);
  - **Controls**;
  - **Rules** (including the bug table);
  - **Project structure** (the package tree);
  - **Where each OO concept lives**: a table covering Abstraction, Inheritance, Polymorphism, Encapsulation, Interfaces, Composition, State (bug modes and match phases), Template Method, Observer, Factory, sealed hierarchies + pattern matching, enums, exceptions, collections, each naming real classes in project-2;
  - **What changed from the terminal version** (windowed FXGL, neon visuals, smooth movement, HUD, FXGL menus);
  - **MVP scope / roadmap**: one short list of what this MVP leaves out (persistent top-5 scores, settings and key rebinding, audio, extra mazes, custom menus and screens, particles and effects), pointing to the spec;
  - **Customising** (the maze legend and the `.maze` file; `MazeLibrary.MAZE_FILES` lists the mazes and their rotation rule).

  Every class name cited must exist (check with `grep`).
- [ ] **Step 1: Write the README.**
- [ ] **Step 2: Verify that every class named in the OO table exists:** `grep -rl "class <Name>\|interface <Name>\|enum <Name>\|record <Name>" src/main/java` for each. Every one must be found.
- [ ] **Step 3: Commit** `docs: Bug Hunt README with OO concepts map`.
