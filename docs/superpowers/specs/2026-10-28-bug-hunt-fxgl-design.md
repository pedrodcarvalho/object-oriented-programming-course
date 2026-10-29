# Bug Hunt — FXGL port of Caça-Bugs (design spec)

Date: 2026-10-28 · Status: approved in brainstorming, awaiting written-spec review

## 1. Intent

**What the owner asked for.** Migrate the terminal Pac-Man clone *Caça-Bugs* (`project-1/`, pure Java, ~3.3k lines) to the **FXGL** engine as a real windowed game, built by a team of agents who each play one role in an indie studio. The new game must **not** be a 1:1 copy of the terminal version: it needs menus, screens and polish.

**Who it is for.** It is an **OOP course deliverable**. The object-oriented structure (abstraction, inheritance, polymorphism, encapsulation, interfaces, composition, the State / Template Method / Observer / Factory patterns, enums, exceptions, collections) has to stay visible, and the README has to map each concept to its classes.

**Success criteria**
1. `cd project-2 && ./mvnw javafx:run` opens a 1280×720 window with the full screen flow (§4), using only a JDK 21+.
2. The game rules match `project-1` (§5): speeds, waves, the four bug personalities, scoring, coffee, extra life.
3. `./mvnw test` passes, with JUnit tests covering the model (§8).
4. The `bughunt.model` package has zero `javafx.*` / `com.almasb.*` imports, and a test enforces it.
5. Everything (identifiers, comments, UI text, README) is in **English**.
6. `project-2/README.md` explains how to run and play, and has the OO-concepts → classes table.

**Explicit decisions** (from the owner): the new game lives in `project-2/` and `project-1/` is left untouched as the reference. The look is "neon circuit vector". Every feature group is in scope: menus & screens, animation & FX, sound & music, settings & top-5 scores. "Dedicated process" means a standalone desktop window.

## 2. Tech stack

- Java **21** (`maven.compiler.release=21`). FXGL **21.1** (`com.github.almasb:fxgl:21.1`) supplies JavaFX transitively.
- Maven via the **script-only Maven wrapper** (`mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties` with `distributionType=only-script`), because the repo's `.gitignore` ignores `*.jar`.
- `org.openjfx:javafx-maven-plugin:0.0.8` with `mainClass=bughunt.BugHuntApp`; `./mvnw javafx:run` runs the game.
- JUnit Jupiter 5 (latest 5.x) with Surefire.
- The repo `.gitignore` also ignores `*.txt`, so **maze files use the `.maze` extension**.
- The game title is **Bug Hunt**, with root package `bughunt`.

## 3. Architecture

```
project-2/
  pom.xml  mvnw  mvnw.cmd  .mvn/wrapper/maven-wrapper.properties
  README.md
  tools/SoundForge.java            chiptune synthesiser, writes the WAV assets (run manually, output committed)
  src/main/java/bughunt/
    BugHuntApp.java                GameApplication: settings, scene factory, input, fixed-timestep bridge
    model/                         PURE JAVA — no javafx.*, no com.almasb.*
      core/     Entity, Updatable, Position, Direction, Ticks, Match
      actors/   Actor, Debugger, Bug, NullPointer, RaceCondition, Deadlock, Heisenbug
      pickups/  Pickup, Bit, Breakpoint, Coffee
      modes/    BugMode, HostileMode, Scatter, Chase, Vulnerable, Captured, InBacklog, Look
      maze/     Maze, Cell, CellType, Level, LevelFactory, InvalidMazeException, MazeLibrary
      events/   EventBus, EventListener, GameEvent, EventType
      rules/    ScoreBoard, LevelManager, ModeController, CollisionManager, LevelConfig
      flow/     GamePhase (State), Ready, Playing, LifeLost, LevelCleared, GameOver, MatchSession
      persistence/ HighScoreTable, HighScoreEntry, Settings, SaveFiles
    view/      Theme, MazeRenderer, DebuggerView, BugView, PickupViews, Effects, FloatingText, MatchView
    ui/        BugHuntSceneFactory, SplashIntro, MainMenu, PauseMenu, SettingsScreen, HighScoresScreen,
               HowToPlayScreen, NameEntryDialog, Hud, ErrorDialog
    audio/     AudioDirector, Sfx
    qa/        QaShots (dev-only screenshot harness, see §8)
  src/main/resources/assets/
    levels/maze1.maze maze2.maze maze3.maze
    sounds/*.wav   music/*.wav
  src/test/java/bughunt/...
```

### 3.1 Dependency rules
- `model` depends on nothing but the JDK.
- `view`, `ui` and `audio` read the model and subscribe to its `EventBus`. **The model never calls into them.**
- `BugHuntApp` is the only class that wires all layers together.

### 3.2 Fixed timestep
The model keeps the terminal game's 60 ticks/s. `BugHuntApp.onUpdate(tpf)` adds `tpf` to an accumulator and calls `session.tick()` once for each full 1/60 s, at most 5 ticks per frame to avoid a spiral of death. While a menu is open (FXGL pauses `onUpdate`), the model does not advance.

### 3.3 Interpolation
`Actor` exposes `position()`, `direction()` and `progress()` (0.0–1.0, the fraction of the way to the next tile, i.e. `Personagem.progresso`). *(Amended by ruling R12 during implementation.)* The view uses **trailing** interpolation. It remembers each actor's previous tile A and current tile B and draws it at `A + (B − A) × t`, where `t = max(t, progress())` only grows within a segment. Both tiles are always passable, so actors never cut into walls. The drawing trails the logic by at most one tile. A tunnel wrap or a respawn (A and B not adjacent) snaps without interpolation. The original `position + direction × progress` formula drew actors inside walls, because `direction()` is the direction the actor arrived from.

### 3.4 Appearance is model state, not sprites
`BugMode` drops `aparencia(Sprite)` and gains `Look look(Bug, Match)`, where `Look` is `NORMAL | VULNERABLE | FLASHING | EYES`. `Debugger` exposes its `direction()`. The view maps these to visuals.

## 4. Player experience

### 4.1 Window, HUD, input
- 1280×720, title "Bug Hunt", resizable with letterboxing. **F11** toggles fullscreen (setting persisted).
- Maze tiles are **24 px**. A 28×23 maze is 672×552, centred in the left area.
- A HUD panel on the right shows: SCORE, HIGH SCORE, LEVEL, lives (small cursor icons), wave name (`SCATTER`/`CHASE`; `BREAKPOINT` while bugs are vulnerable), a breakpoint countdown bar, and the bits remaining.
- Default keys: arrows **and** WASD to move, **P / Esc** to pause. The four directions and pause can be rebound in Settings; the rebinding replaces the primary key, and the arrow keys always keep working.

### 4.2 Screen flow
1. **Splash** (FXGL intro scene, about 2 s, skippable with any key): a "Bug Hunt" logo that glitches in.
2. **Main menu**: PLAY, HIGH SCORES, SETTINGS, HOW TO PLAY, QUIT. Animated bugs wander behind a translucent panel. Fully keyboard navigable, mouse also works.
3. **Match.** Each round begins in **Ready** ("READY!" for 2 s). Then **Playing**.
   - Losing a life → **LifeLost**: freeze 0.7 s, the cursor spins/glitches until 1.7 s, "SEGFAULT" text, screen shake. 2.4 s in total. Then Ready, or GameOver if no lives are left.
   - Clearing all pickups → **LevelCleared**: freeze 1 s, then the maze traces flash every 0.2 s until 3 s, with "BUILD PASSED". Then the next level's Ready.
   - **Pause** (P/Esc) opens the FXGL game menu: RESUME, SETTINGS, QUIT TO MENU.
4. **GameOver**: "GAME OVER". If the score qualifies for the top 5, **NameEntryDialog** opens (A–Z, 0–9, up to 8 characters, default `PLAYER`). Then **High scores**, then the main menu.
5. **Settings**: music volume, SFX volume (0–100%, slider), fullscreen toggle, key rebinding. Saved immediately.
6. **How to play**: controls, the pickups and their values, and the four bugs with each one's chase rule (the text from §5.3, in English).

These timings come from `project-1`'s `estados/` and are binding.

### 4.3 Look ("neon circuit")
- **Theme** constants live in one class (`view/Theme`). Background is a near-black green PCB (`#07130d`) with a faint grid.
- **Walls**: glowing traces (stroke about 3 px plus `DropShadow`/`Bloom` glow) drawn along the **outline** of wall regions, not as filled blocks. The backlog door is a magenta line.
- **Bit**: a small warm-white solder dot. **Breakpoint**: a red pad pulsing every 0.25 s. **Coffee**: a brown cup with animated steam.
- **Debugger**: a yellow cursor arrow facing its direction of travel.
- **Bugs**: beetle silhouettes with animated legs. Colours: NullPointer red, RaceCondition pink, Deadlock cyan, Heisenbug orange. VULNERABLE is blue; FLASHING alternates blue and white every 0.2 s; EYES is just two eyes.
- **Juice**: smooth movement; a particle burst plus floating text (200/400/800/1600) when a bug is captured (the existing 0.5 s freeze stays); floating coffee value; a small pop when a bit is eaten; screen shake on death; the maze flashes on level clear.

### 4.4 Audio
`tools/SoundForge.java` (single-file program, `java tools/SoundForge.java <outDir>`) synthesises 16-bit mono 44.1 kHz WAVs with square/triangle/noise waves and envelopes. Its output is committed under `assets/`.
- SFX: `chomp`, `breakpoint`, `bug_captured`, `death`, `extra_life`, `level_clear`, `coffee`, `menu_move`, `menu_select`.
- Music: `menu_loop`, `game_loop` (each 15–30 s, seamlessly loopable).

`AudioDirector` subscribes to model events and plays them, using the volumes from `Settings`. A missing audio file logs a warning and stays silent; it never crashes.

## 5. Game rules (binding — ported from `project-1`)

**`project-1/src/cacabugs/**` is the authority for every rule value.** The port translates names and removes terminal rendering; it does not change behaviour except where listed in §5.5.

### 5.1 Name mapping (Portuguese → English)

| project-1 | project-2 |
|---|---|
| nucleo: Atualizavel, Posicao, Direcao, Entidade, Tempo, Partida | core: Updatable, Position, Direction, Entity, Ticks, Match |
| nucleo: Desenhavel, Camada | removed (rendering lives in `view`) |
| entidades: Personagem, Debugger, Bug, NullPointer, RaceCondition, Deadlock, Heisenbug | actors: Actor, Debugger, Bug, NullPointer, RaceCondition, Deadlock, Heisenbug |
| entidades: TextoFlutuante | view: FloatingText (triggered by events) |
| coletaveis: Coletavel, Bit, Breakpoint, Cafe | pickups: Pickup, Bit, Breakpoint, Coffee |
| modos: ModoDoBug, ModoHostil, Dispersao, Perseguicao, Vulneravel, Capturado, NoBacklog | modes: BugMode, HostileMode, Scatter, Chase, Vulnerable, Captured, InBacklog |
| mapa: Labirinto, Celula, TipoCelula, Fase, FabricaDeFase, MapaInvalidoException | maze: Maze, Cell, CellType, Level, LevelFactory, InvalidMazeException |
| eventos: CentralDeEventos, OuvinteDeEventos, Evento, TipoEvento | events: EventBus, EventListener, GameEvent, EventType |
| gerenciadores: Placar, GerenciadorDeFases, ControladorDeModos, GerenciadorDeColisoes, ConfiguracaoDaFase | rules: ScoreBoard, LevelManager, ModeController, CollisionManager, LevelConfig |
| gerenciadores: GerenciadorDeEntrada, Comando, GerenciadorDeRecursos, GerenciadorDeEstados | replaced by FXGL input/assets/scenes + `MazeLibrary` + `MatchSession` |
| estados: EstadoJogando, EstadoVidaPerdida, EstadoFaseConcluida, EstadoFimDeJogo | flow: Ready + Playing, LifeLost, LevelCleared, GameOver |
| estados: EstadoMenu, EstadoPausa, PainelLateral, Avisos, TextoGrande | ui: MainMenu, PauseMenu, Hud, ErrorDialog (TextoGrande dropped) |
| terminal/* | removed |

Method names are translated the same way (`calcularAlvo` → `computeTarget`, `aoEncostar` → `onTouch`, `escolherDirecao` → `chooseDirection`, …). `Bug.chooseDirection()` stays `final` (Template Method).

### 5.2 Values (summary; the code in project-1 wins any disagreement)
- 60 ticks/s. Max speed 8.5 tiles/s. Debugger 0.80 / 0.90 / 1.00 × max (level 1 / levels 2–4 / level 5+). Bug 0.75 / 0.85 / 0.95. Vulnerable 0.50 / 0.55 / 0.60. Tunnel and backlog 0.40 / 0.45 / 0.50. Captured (eyes) 1.6 × max.
- Breakpoint lasts `max(1, 7 − level)` s; bugs flash for the last `min(2 s, duration/2)`.
- Waves in seconds, alternating scatter/chase, with a final endless chase: L1 `7,20,7,20,5,20,5`; L2–4 `7,20,7,20,5`; L5+ `5,20,5,20,5`. The wave clock stops during a breakpoint.
- Backlog release delay for bug order *n*≥1: `1 + (n−1) × (4 | 3 | 2)` s.
- Bit 10, breakpoint 50. Captures 200/400/800/1600 per breakpoint. Coffee appears after 70 and 170 pickups and lasts 10 s. Coffee values by level: `100,300,500,500,700,700,1000,1000,2000,2000,3000,3000,5000` (capped at the last). Lives: 3, plus one extra at 10,000 points. Freeze of 0.5 s on capture.

### 5.3 Bug personalities (chase target)

| Bug | Colour | Chase target |
|---|---|---|
| NullPointer | red | the Debugger's tile |
| RaceCondition | pink | 4 tiles ahead of the Debugger |
| Deadlock | cyan | twice the vector from NullPointer to the tile 2 ahead of the Debugger |
| Heisenbug | orange | the Debugger if more than 8 tiles away, otherwise its scatter corner |

### 5.4 Maze file format (`.maze`, same legend as `labirinto.txt`)
`#` wall, `.` bit, `o` breakpoint, space corridor, `T` tunnel, `-` backlog door, `H` backlog, `P` Debugger start, `C` coffee spot, `1`–`4` bug starts. All lines must have equal length. `maze1.maze` is `project-1/src/recursos/labirinto.txt` copied verbatim.

### 5.5 Intentional changes vs project-1
1. **Maze rotation**: level *n* uses maze index `((n − 1) / 2) % 3` (integer division) → levels 1–2 maze1, 3–4 maze2, 5–6 maze3, 7–8 maze1, … (`MazeLibrary`).
   - Mazes 2 and 3 are new and authored by the Game Designer. Each must be exactly **28×23**, pass `LevelFactory` validation, have exactly **4** breakpoints, at least one tunnel row, the same backlog/door block shape as maze1, between **180 and 260** pickups, and every pickup reachable from `P`. A test checks all of this.
2. **High scores**: a top-5 table with names (`HighScoreTable`) replaces the single `recorde.txt`. The HUD HIGH SCORE shows the table's best entry.
3. Extra events for feedback: `ROUND_STARTED`, `LEVEL_CLEARED`, `GAME_OVER`, `BUG_RELEASED` (bug leaves the backlog), `BIT_COLETADO` → `BIT_EATEN` (all event names in English).
4. `TextoFlutuante` and the `Layer` ordering move to the view.

## 6. Persistence
- Directory: `${user.home}/.bughunt/` (created on demand).
- `scores.properties` holds the top 5 as `n.name` / `n.score`.
- `settings.properties` holds `musicVolume`, `sfxVolume` (0.0–1.0), `fullscreen`, and `key.UP/DOWN/LEFT/RIGHT/PAUSE` (JavaFX `KeyCode` names).
- An unreadable or corrupt file falls back to defaults and logs a warning; it never crashes. Write failures are logged and ignored.
- `SaveFiles` takes its directory as a constructor parameter so tests use a temp dir.

## 7. Error handling
- `InvalidMazeException` (checked), thrown by `LevelFactory`/`MazeLibrary`, is shown in `ErrorDialog` from the main menu, and the game returns to the menu.
- A missing asset logs a warning and the game continues without it.
- Unexpected exceptions in `onUpdate` are logged with the stack trace and shown in `ErrorDialog`; the game returns to the main menu.

## 8. Testing & QA
- **Unit tests (JUnit 5)** for the model:
  - Position/Direction math and tunnel wrap
  - LevelFactory validation errors
  - each bug's `computeTarget`
  - ModeController wave sequence and breakpoint pause
  - Vulnerable → Captured → InBacklog cycle
  - ScoreBoard capture chain and extra life
  - LevelManager coffee at 70/170 and level cleared
  - LevelConfig tables
  - MazeLibrary rotation and maze constraints (§5.5)
  - phase transitions in `MatchSession` (Ready → Playing → LifeLost → Ready/GameOver; Playing → LevelCleared → Ready of the next level)
  - HighScoreTable (insert, ordering, cap 5, qualifies) and Settings round-trip
- **Architecture test**: scans `src/main/java/bughunt/model/**` and fails on any `import javafx` / `import com.almasb`.
- **QA harness**: launching the game with the program argument `--qa-shots=<dir>` (the plan fixes the exact `mvnw` command and documents it in the README) starts the game and auto-navigates menu → match. After set delays it saves PNG snapshots of the scene (main menu, gameplay, pause, game over), then exits with code 0. Runs headless under `xvfb-run`. QA reviews the shots and the log.
- No automated UI tests beyond the harness.

## 9. Team (agent roles) and ownership
Every role is an Opus subagent dispatched by the Producer (the session controller) under subagent-driven development:

| Role | Owns |
|---|---|
| Producer (controller) | plan, ledger, rulings, owner questions |
| Lead Engineer | Maven scaffold, model port (core, actors, pickups, modes, maze, events, rules), architecture test |
| Gameplay Programmer | `model/flow`, `MatchSession`, `BugHuntApp` wiring, fixed timestep, input & rebinding |
| Game Designer | mazes 2 & 3, MazeLibrary constraints check, How-to-Play copy |
| Technical Artist | `view/` (Theme, maze renderer, actor views, effects) |
| UI/UX Developer | `ui/`, `model/persistence` |
| Audio Engineer | `tools/SoundForge.java`, the WAV assets, `audio/` |
| QA Engineer | `qa/QaShots`, smoke run under xvfb, playtest report |
| Senior Code Reviewer | per-task reviews, re-reviews, final whole-branch review |
| Technical Writer | `project-2/README.md` with the OO-concepts table |

## 10. Out of scope
Gamepad support, mobile/web builds, native installers (jpackage), online leaderboards, a level editor, localisation beyond English.
