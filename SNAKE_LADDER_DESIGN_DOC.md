# Snake & Ladder - Low Level Design Document

## 📋 Table of Contents
1. [Problem Statement](#problem-statement)
2. [Requirements](#requirements)
3. [High-Level Architecture](#high-level-architecture)
4. [Class Design & Responsibilities](#class-design--responsibilities)
5. [Design Patterns Used](#design-patterns-used)
6. [Key Design Decisions & Trade-offs](#key-design-decisions--trade-offs)
7. [Edge Cases & Failure Handling](#edge-cases--failure-handling)
8. [Extensibility & Future Enhancements](#extensibility--future-enhancements)
9. [Interview Discussion Points](#interview-discussion-points)

---

## Problem Statement

Design a Snake & Ladder game that:
- Supports multiple players
- Has a configurable board with snakes and ladders
- Handles dice rolling mechanics
- Determines the winner when a player reaches the final cell

---

## Requirements

### Functional Requirements
| Requirement | Description |
|-------------|-------------|
| FR1 | Board of configurable size (typically 100 cells) |
| FR2 | Place snakes (move player down) and ladders (move player up) |
| FR3 | Support 2+ players taking turns |
| FR4 | Roll dice and move player accordingly |
| FR5 | Player must land exactly on final cell to win |
| FR6 | First player to reach final cell wins |

### Non-Functional Requirements
| Requirement | Description |
|-------------|-------------|
| NFR1 | **Extensibility**: Easy to add new dice types, special cells |
| NFR2 | **Testability**: Components should be independently testable |
| NFR3 | **Server-Ready**: Non-blocking API design for web/mobile integration |
| NFR4 | **Thread Safety**: Handle concurrent requests (future scope) |

---

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                             │
│                  (Web/Mobile/Console)                           │
└───────────────────────────┬─────────────────────────────────────┘
                            │ makeMove(playerId)
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                      SERVICE LAYER                              │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                      Game                               │    │
│  │  - Orchestrates game flow                               │    │
│  │  - Validates turn order                                 │    │
│  │  - Returns response strings (server-friendly)           │    │
│  └─────────────────────────────────────────────────────────┘    │
└───────────────────────────┬─────────────────────────────────────┘
                            │
          ┌─────────────────┼─────────────────┐
          ▼                 ▼                 ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────────┐
│    Board     │   │    Player    │   │  DiceStrategy    │
│              │   │              │   │   (Interface)    │
│ - size       │   │ - name       │   │                  │
│ - jumps map  │   │ - position   │   │  ┌────────────┐  │
│              │   │              │   │  │NormalDice  │  │
│  ┌────────┐  │   │              │   │  │CrookedDice │  │
│  │  Jump  │  │   │              │   │  │DoubleDice  │  │
│  └────────┘  │   │              │   │  └────────────┘  │
└──────────────┘   └──────────────┘   └──────────────────┘
```

---

## Class Design & Responsibilities

### 1. Model Layer

#### `Board.java`
```java
Responsibilities:
├── Store board size (total cells)
├── Maintain jump mappings (snakes + ladders)
├── Validate jump positions during setup
└── Provide O(1) lookup for jumps
```

**Key Design Choice**: Using a single `Jump` class for both snakes and ladders.

#### `Jump.java`
```java
Responsibilities:
├── Represent a transition from one cell to another
└── Store start and end positions
```

**Why unified Jump instead of separate Snake/Ladder classes?**

| Approach | Pros | Cons |
|----------|------|------|
| **Unified Jump** (Our choice) | Simple, less code, easy to add new jump types | Less semantic clarity |
| Separate Snake/Ladder | Clear intent, type-safe | More classes, duplicate logic |

**Staff-Level Answer**: "We chose unified Jump because the behavior is identical - both are just position transitions. The direction (up/down) is derived from comparing start/end. This follows the **DRY principle** and makes adding new jump types (like teleporters) trivial."

#### `Player.java`
```java
Responsibilities:
├── Store player identity (name/id)
├── Track current position on board
└── Mutable position (updated during game)
```

### 2. Strategy Layer

#### `DiceStrategy.java` (Interface)
```java
public interface DiceStrategy {
    int roll();
}
```

**Why Strategy Pattern for Dice?**

This is a **textbook application** of the Strategy Pattern:

```
┌─────────────────────────────────────────────────────────────┐
│                   WITHOUT Strategy Pattern                   │
├─────────────────────────────────────────────────────────────┤
│  class Game {                                                │
│      int rollDice(String diceType) {                        │
│          if (diceType.equals("normal")) return rand(1,6);   │
│          if (diceType.equals("crooked")) return rand(1,6)*2;│
│          if (diceType.equals("double")) return 2*rand(1,6); │
│          // Violates Open-Closed Principle!                  │
│      }                                                       │
│  }                                                           │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    WITH Strategy Pattern                     │
├─────────────────────────────────────────────────────────────┤
│  interface DiceStrategy { int roll(); }                      │
│                                                              │
│  class NormalDice implements DiceStrategy { ... }            │
│  class CrookedDice implements DiceStrategy { ... }           │
│  class DoubleDice implements DiceStrategy { ... }            │
│                                                              │
│  class Game {                                                │
│      private DiceStrategy dice; // Injected                  │
│      void play() { int val = dice.roll(); } // Polymorphism! │
│  }                                                           │
└─────────────────────────────────────────────────────────────┘
```

**Benefits**:
- ✅ **Open-Closed Principle**: Add new dice without modifying Game
- ✅ **Testability**: Inject mock dice that returns predictable values
- ✅ **Runtime Flexibility**: Switch dice mid-game if needed
- ✅ **Single Responsibility**: Dice logic isolated from game logic

### 3. Service Layer

#### `Game.java`
```java
Responsibilities:
├── Orchestrate the game flow
├── Validate player turns
├── Apply game rules (bounds check, jump check, win check)
├── Maintain game state
└── Return structured responses (server-ready)
```

---

## Design Patterns Used

### 1. Strategy Pattern (Dice)
**Where**: `DiceStrategy` interface + `NormalDiceStrategy` implementation

**Why**: Decouples dice rolling logic from game logic, enabling:
- Easy testing with deterministic dice
- Adding variants (CrookedDice, DoubleDice)
- Runtime swapping of dice behavior

### 2. Composition over Inheritance
**Where**: `Game` has-a `Board`, `DiceStrategy`, `List<Player>`

**Why**: 
- Game doesn't extend any class
- All dependencies are injected
- Easy to swap components (different boards, different dice)

### 3. Dependency Injection (Constructor Injection)
**Where**: `Game(Board board, List<Player> players, DiceStrategy diceStrategy)`

**Why**:
- All dependencies explicit and testable
- No hidden `new` calls inside Game
- Follows Inversion of Control

### 4. Information Hiding
**Where**: `Board` encapsulates jump logic; Game doesn't know internal representation

**Why**:
- Board could change from HashMap to TreeMap without affecting Game
- Jump lookup logic is Board's responsibility

---

## Key Design Decisions & Trade-offs

### Decision 1: Unified Jump vs Separate Snake/Ladder Classes

| Factor | Unified Jump | Separate Classes |
|--------|--------------|------------------|
| Code Complexity | ✅ Less code | ❌ More classes |
| Extensibility | ✅ Easy to add teleporters, portals | ⚠️ Each new type needs new class |
| Type Safety | ⚠️ Runtime check for direction | ✅ Compile-time type checking |
| Memory | ✅ Smaller footprint | ❌ More objects |

**Our Choice**: Unified Jump - because behavior is identical, only semantics differ.

### Decision 2: Player Starts at Position 0 (Off-Board)

```
Position 0: Player hasn't entered the board yet
Position 1-100: Valid board positions
```

**Why not start at 1?**
- Clearer semantics: "Roll to enter the board"
- Matches real-world game rules
- Simplifies "first move" logic

### Decision 3: Non-Blocking API Design

```java
// Instead of blocking game loop:
while (!gameEnded) {
    player.makeMove(); // Blocks thread
}

// We use request-response:
String response = game.makeMove(playerId); // Returns immediately
```

**Why**:
- **Server-Ready**: Each HTTP request calls makeMove()
- **Scalable**: No thread blocked waiting for player
- **Async Compatible**: Works with websockets, polling, etc.

### Decision 4: Return String vs Structured Response

**Current**: `String makeMove(playerId)`

**Alternative**: `MoveResult makeMove(playerId)`

| Approach | Pros | Cons |
|----------|------|------|
| String | Simple, human-readable | Parsing needed for programmatic use |
| Structured Object | Type-safe, easy to serialize to JSON | More boilerplate |

**Trade-off Acknowledged**: For production, we'd return a `MoveResult` object. String is used here for simplicity.

### Decision 5: Exact Landing Requirement

```java
if (nextPos > board.getSize()) {
    // Cannot move - need exact landing
}
```

**Why**:
- Matches traditional game rules
- Adds strategic element (tension near end)
- Easy to disable if needed: change `>` to `>=` and clamp

---

## Edge Cases & Failure Handling

### Edge Case Matrix

| Edge Case | How We Handle | Code Location |
|-----------|---------------|---------------|
| **Invalid jump positions** | Throw `IllegalArgumentException` during setup | `Board.addJump()` |
| **Player moves beyond board** | Reject move, player stays in place | `Game.makeMove()` line 43 |
| **Wrong player's turn** | Return error message, no state change | `Game.makeMove()` line 30 |
| **Move after game ended** | Return "Game Already Ended" | `Game.makeMove()` line 27 |
| **Chained jumps (snake→ladder)** | Only one jump per move (by design) | `Board.getJump()` returns single jump |
| **Jump at final cell** | Win check happens after jump | Line 62 checks final position |
| **Landing on occupied cell** | Allowed (multiple players per cell) | No collision logic |

### Failure Mode Analysis

```
┌────────────────────────────────────────────────────────────┐
│                    FAILURE SCENARIOS                        │
├────────────────────────────────────────────────────────────┤
│                                                             │
│  1. Invalid Board Setup                                     │
│     ┌─────────────────────────────────────────────────┐    │
│     │ board.addJump(105, 50); // Start > board size   │    │
│     │ → IllegalArgumentException thrown                │    │
│     │ → Fail-fast at setup, not runtime               │    │
│     └─────────────────────────────────────────────────┘    │
│                                                             │
│  2. Concurrent Access (Future Concern)                      │
│     ┌─────────────────────────────────────────────────┐    │
│     │ Current: Not thread-safe                         │    │
│     │ Solution: Add synchronized or use locks          │    │
│     │ Better: Use event-sourcing pattern               │    │
│     └─────────────────────────────────────────────────┘    │
│                                                             │
│  3. Player Disconnect                                       │
│     ┌─────────────────────────────────────────────────┐    │
│     │ Current: Game waits indefinitely                 │    │
│     │ Solution: Add timeout, skip turn, or forfeit     │    │
│     └─────────────────────────────────────────────────┘    │
│                                                             │
└────────────────────────────────────────────────────────────┘
```

---

## Extensibility & Future Enhancements

### How to Add New Features

| Feature | Changes Required | Difficulty |
|---------|------------------|------------|
| **Crooked Dice** | Add `CrookedDiceStrategy implements DiceStrategy` | 🟢 Easy |
| **Multiple Dice** | Add `MultiDiceStrategy` that aggregates rolls | 🟢 Easy |
| **Power-ups** | Add new cell types in Board, handle in Game | 🟡 Medium |
| **Undo Move** | Add Memento pattern (like TicTacToe) | 🟡 Medium |
| **Multiplayer Online** | Add GameServer, PlayerSession classes | 🔴 Complex |
| **Leaderboard** | Add GameResult, LeaderboardService | 🟡 Medium |

### Proposed: Enhanced Architecture

```
Future Enhancements:
                    
┌─────────────────────────────────────────────────────────┐
│                  CellEffect (Interface)                  │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌───────────┐   │
│  │  Snake  │  │ Ladder  │  │PowerUp  │  │Teleporter │   │
│  └─────────┘  └─────────┘  └─────────┘  └───────────┘   │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│                  TurnRule (Interface)                    │
│  ┌────────────────┐  ┌────────────────┐                 │
│  │ ExtraOnSix     │  │ SkipOnSnake    │                 │
│  └────────────────┘  └────────────────┘                 │
└─────────────────────────────────────────────────────────┘
```

---

## Interview Discussion Points

### 🎯 Questions Interviewer Might Ask

#### Q1: "Why not use inheritance for Snake and Ladder?"

**Answer**: 
"Both Snake and Ladder have identical behavior - they move a player from point A to point B. The only difference is semantic (up vs down). Using inheritance here would be **over-engineering**. The current `Jump` class handles both, and we derive the type by comparing `start` and `end`. This follows the **YAGNI principle** - we don't add complexity we don't need."

#### Q2: "How would you make this thread-safe for a web server?"

**Answer**:
"Three options with increasing sophistication:

1. **Synchronized methods**: Simple but creates bottleneck
2. **ReentrantLock per game**: Better concurrency, one lock per game instance
3. **Event Sourcing**: Store moves as events, replay for state. Best for distributed systems.

For a typical web game, option 2 with a lock per game instance would suffice."

#### Q3: "Why Strategy pattern for Dice and not Factory?"

**Answer**:
"They solve different problems:
- **Factory** decides *what object to create*
- **Strategy** decides *what algorithm to use*

Dice rolling is a *behavior*, not object creation. We inject a dice strategy that defines *how* to roll, not *which* dice to create. The Game doesn't care about dice instantiation, just rolling behavior."

#### Q4: "What if two players land on the same cell?"

**Answer**:
"In standard Snake & Ladder, multiple players can occupy the same cell - there's no collision. If we wanted to add collision rules (like Ludo), we'd:
1. Add a `Map<Integer, List<Player>> cellOccupants` in Board
2. Add a `CollisionStrategy` interface with implementations like `BumpBackStrategy`, `SwapStrategy`
3. Apply collision logic after movement in Game"

#### Q5: "How would you test this?"

**Answer**:
```java
// 1. Deterministic Dice for Testing
class FixedDiceStrategy implements DiceStrategy {
    private int[] values;
    private int index = 0;
    
    FixedDiceStrategy(int... values) { this.values = values; }
    
    public int roll() { return values[index++]; }
}

// 2. Test winning condition
@Test
void playerWinsOnExactLanding() {
    Board board = new Board(10);
    List<Player> players = List.of(new Player("Alice"));
    DiceStrategy dice = new FixedDiceStrategy(5, 5); // Two rolls of 5
    
    Game game = new Game(board, players, dice);
    game.makeMove("Alice"); // Position 5
    String result = game.makeMove("Alice"); // Position 10
    
    assertTrue(result.contains("WINNER"));
}
```

---

## Summary: What Makes This Design Staff-Level?

| Aspect | How We Demonstrate It |
|--------|----------------------|
| **SOLID Principles** | Open-Closed (DiceStrategy), Single Responsibility (each class has one job) |
| **Design Patterns** | Strategy pattern applied correctly with clear justification |
| **Trade-off Analysis** | Explicit discussion of alternatives and why we chose our approach |
| **Edge Case Handling** | Comprehensive coverage with fail-fast validation |
| **Extensibility** | Clear path to add new features without modifying existing code |
| **Production Readiness** | Non-blocking API, server-friendly design |
| **Testing Strategy** | Dependency injection enables easy mocking |

---

## Quick Reference Card (For Interview)

```
┌─────────────────────────────────────────────────────────────┐
│                 SNAKE & LADDER - CHEAT SHEET                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  PATTERNS USED:                                             │
│  ✓ Strategy Pattern → DiceStrategy                          │
│  ✓ Dependency Injection → Constructor injection in Game     │
│  ✓ Composition → Game HAS-A Board, Players, DiceStrategy    │
│                                                             │
│  KEY TRADE-OFFS:                                            │
│  ✓ Unified Jump vs Separate Snake/Ladder → Chose unified    │
│  ✓ String vs Object response → Chose String (simplicity)    │
│  ✓ Blocking vs Non-blocking → Chose Non-blocking (servers)  │
│                                                             │
│  EDGE CASES HANDLED:                                        │
│  ✓ Invalid jump positions → IllegalArgumentException        │
│  ✓ Move beyond board → Reject, stay in place                │
│  ✓ Wrong player's turn → Error message, no state change     │
│  ✓ Game already ended → "Game Already Ended" response       │
│                                                             │
│  TIME COMPLEXITY:                                           │
│  ✓ makeMove() → O(1) - HashMap lookup for jumps             │
│  ✓ addJump() → O(1) - HashMap insertion                     │
│                                                             │
│  SPACE COMPLEXITY:                                          │
│  ✓ Board → O(J) where J = number of jumps                   │
│  ✓ Game → O(P) where P = number of players                  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

