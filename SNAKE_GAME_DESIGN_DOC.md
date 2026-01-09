# Snake Game - Staff-Level LLD Design Document

## Table of Contents
1. [Problem Statement](#problem-statement)
2. [Requirements Analysis](#requirements-analysis)
3. [Architecture Overview](#architecture-overview)
4. [Design Patterns Used](#design-patterns-used)
5. [Class-by-Class Deep Dive](#class-by-class-deep-dive)
6. [The Critical Data Structure Decision](#the-critical-data-structure-decision)
7. [Edge Cases & The Interview Trap](#edge-cases--the-interview-trap)
8. [Game Algorithm Flow](#game-algorithm-flow)
9. [Design Trade-offs & Why Our Choices Win](#design-trade-offs--why-our-choices-win)
10. [SOLID Principles Analysis](#solid-principles-analysis)
11. [Extensibility Showcase](#extensibility-showcase)
12. [Common Interview Questions](#common-interview-questions)
13. [Quick Reference](#quick-reference)

---

## Problem Statement

Design a classic Snake game with the following rules:
- Snake moves continuously in one direction
- Snake grows when eating food
- Game ends on wall collision or self-collision
- Track score (food eaten count)

---

## Requirements Analysis

### Functional Requirements
| Requirement | Priority | Complexity |
|-------------|----------|------------|
| Snake moves in 4 directions | Must Have | Low |
| Snake grows on eating food | Must Have | Medium |
| Wall collision detection | Must Have | Low |
| Self-collision detection | Must Have | **High** ⚠️ |
| Score tracking | Must Have | Low |
| Food spawns randomly (not on snake) | Must Have | Medium |

### Non-Functional Requirements
| Requirement | Target | Solution |
|-------------|--------|----------|
| Move operation | O(1) | Deque data structure |
| Collision check | O(1) | HashSet auxiliary structure |
| Testability | High | Separation of concerns |
| Extensibility | High | Clean interfaces |

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                      PACKAGE STRUCTURE                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  com.snake                                                       │
│  ├── game/                                                       │
│  │   └── SnakeGame.java      ← Game Engine (Orchestrator)       │
│  │                                                               │
│  └── model/                                                      │
│      ├── Cell.java           ← Value Object (Coordinate)        │
│      ├── Direction.java      ← Enum (UP, DOWN, LEFT, RIGHT)     │
│      ├── Snake.java          ← Entity (Body Management)         │
│      └── Board.java          ← Entity (Grid + Food)             │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Component Relationship

```
┌──────────────────────────────────────────────────────────────┐
│                                                               │
│   ┌─────────────┐                                            │
│   │   UI/Input  │  (Not part of core design)                 │
│   └──────┬──────┘                                            │
│          │ move(Direction)                                   │
│          ▼                                                   │
│   ┌─────────────┐                                            │
│   │  SnakeGame  │  ← ORCHESTRATOR                            │
│   │  (Engine)   │     Coordinates Snake + Board              │
│   └──────┬──────┘     Owns game state (score, gameOver)      │
│          │                                                   │
│    ┌─────┴─────┐                                             │
│    │           │                                             │
│    ▼           ▼                                             │
│ ┌──────┐   ┌──────┐                                          │
│ │Snake │   │Board │                                          │
│ │      │   │      │                                          │
│ │body  │   │food  │                                          │
│ │move()│   │isWall│                                          │
│ │grow()│   │      │                                          │
│ └──┬───┘   └──────┘                                          │
│    │                                                         │
│    ▼                                                         │
│ ┌──────┐                                                     │
│ │ Cell │  ← Immutable coordinate                             │
│ └──────┘                                                     │
│                                                               │
└──────────────────────────────────────────────────────────────┘
```

**Why This Architecture?**

| Component | Responsibility | Why Separate? |
|-----------|---------------|---------------|
| `Cell` | Represents (row, col) | Reusable value object, enables HashSet usage |
| `Direction` | Movement constants | Type-safe, no magic strings/numbers |
| `Snake` | Body management | Single responsibility, testable independently |
| `Board` | Grid + Food logic | Decoupled from snake, reusable for different games |
| `SnakeGame` | Orchestration | Coordinates components, owns game state |

---

## Design Patterns Used

### 1. Value Object Pattern (Cell)

```java
public class Cell {
    private final int row;  // Immutable
    private final int col;  // Immutable
    
    @Override
    public boolean equals(Object o) { ... }
    
    @Override
    public int hashCode() { ... }
}
```

**Why Value Object?**
- **Immutability**: Once created, never changes → thread-safe, no defensive copies
- **Identity by Value**: Two Cells with same (row, col) are equal
- **HashSet Friendly**: Proper equals/hashCode enables O(1) lookups

**Interview Quote**: "Cell is a Value Object - it's immutable and identified by its values, not by reference. This is crucial because we store Cells in a HashSet for O(1) collision detection."

### 2. Composition over Inheritance

```java
public class SnakeGame {
    private final Snake snake;   // HAS-A Snake
    private final Board board;   // HAS-A Board
}
```

**Why Composition?**
- SnakeGame is NOT a type of Snake or Board
- Allows independent evolution of Snake and Board
- Easier to test with mock objects

**What NOT to do**:
```java
// BAD: Inheritance abuse
public class SnakeGame extends Snake { }  // SnakeGame IS-A Snake? No!
```

### 3. Encapsulation with Clean Interfaces

```java
// Snake exposes WHAT, not HOW
public class Snake {
    public void move(Cell newHead) { ... }      // What: move
    public void grow(Cell newHead) { ... }      // What: grow
    public boolean checkCrash(...) { ... }      // What: check collision
    
    // Internal: Deque + Set (hidden from outside)
}
```

**Why This Matters**: SnakeGame doesn't know that Snake uses a Deque internally. We could change Snake's implementation (e.g., to use ArrayList) without changing SnakeGame.

### 4. Method Overloading for Flexibility

```java
public class Board {
    // Simple version (for initialization)
    public void generateFood() { ... }
    
    // Safe version (avoids snake collision)
    public void generateFood(Snake snake) { ... }
}
```

```java
public class Snake {
    // Simple version (for external use like food placement)
    public boolean checkCrash(Cell cell) { ... }
    
    // Smart version (handles tail-chasing edge case)
    public boolean checkCrash(Cell nextHead, boolean isGrowing) { ... }
}
```

**Why Overload?**
- Provides simple API for simple cases
- Advanced API for complex cases
- Backward compatibility

---

## Class-by-Class Deep Dive

### 1. Cell - The Foundation

```java
public class Cell {
    private final int row;
    private final int col;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return row == cell.row && col == cell.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
```

**Critical Points**:

1. **`private final`**: Immutable fields prevent accidental modification
2. **`equals()`**: Two Cells with same coordinates are logically equal
3. **`hashCode()`**: MUST be consistent with equals() for HashSet to work

**What Breaks Without Proper equals/hashCode?**
```java
Set<Cell> bodySet = new HashSet<>();
bodySet.add(new Cell(2, 3));
bodySet.contains(new Cell(2, 3));  // Returns FALSE without equals()!
```

### 2. Direction - Type Safety

```java
public enum Direction {
    UP, DOWN, LEFT, RIGHT
}
```

**Why Enum Over Strings/Integers?**

| Alternative | Problem |
|-------------|---------|
| `String "UP"` | Typos: `"up"`, `"Up"`, `"UP "` |
| `int 0, 1, 2, 3` | What does 2 mean? Magic numbers |
| `enum Direction` | Compile-time safety, self-documenting |

### 3. Snake - The Core Entity ⭐

```java
public class Snake {
    private final Deque<Cell> body;      // Order: Head → ... → Tail
    private final Set<Cell> bodySet;     // For O(1) collision check
    
    public Snake(Cell initPos) {
        this.body = new LinkedList<>();
        this.bodySet = new HashSet<>();
        this.body.add(initPos);
        this.bodySet.add(initPos);
    }
}
```

**Key Methods**:

#### getNextHead() - Calculate Next Position
```java
public Cell getNextHead(Direction direction) {
    Cell head = getHead();
    int row = head.getRow();
    int col = head.getCol();

    switch (direction) {
        case UP:    row--; break;
        case DOWN:  row++; break;
        case LEFT:  col--; break;
        case RIGHT: col++; break;
    }
    return new Cell(row, col);
}
```

**Why In Snake Class?**: Snake knows where its head is and can calculate the next position. This keeps movement logic with the entity that moves.

#### move() - Slide Without Growing
```java
public void move(Cell newHead) {
    // Add to front
    body.addFirst(newHead);
    bodySet.add(newHead);

    // Remove from back (maintain length)
    Cell tail = body.removeLast();
    bodySet.remove(tail);
}
```

**Visual**:
```
Before: [H]→[B1]→[B2]→[T]      Length: 4
        newHead = X

After:  [X]→[H]→[B1]→[B2]      Length: 4 (tail removed)
```

#### grow() - Add Without Removing Tail
```java
public void grow(Cell newHead) {
    body.addFirst(newHead);
    bodySet.add(newHead);
    // NO tail removal = length increases
}
```

**Visual**:
```
Before: [H]→[B1]→[B2]→[T]      Length: 4
        newHead = X (food position)

After:  [X]→[H]→[B1]→[B2]→[T]  Length: 5 (tail stays)
```

#### checkCrash() - The Critical Method ⭐
```java
public boolean checkCrash(Cell nextHead, boolean isGrowing) {
    // Case 1: Not in body at all → SAFE
    if (!bodySet.contains(nextHead)) {
        return false;
    }

    // Case 2: In body + growing → CRASH (tail stays)
    if (isGrowing) {
        return true;
    }

    // Case 3: In body + not growing + hitting tail → SAFE
    Cell currentTail = body.peekLast();
    if (nextHead.equals(currentTail)) {
        return false;  // Tail will vacate
    }
    
    // Case 4: In body + not growing + hitting non-tail → CRASH
    return true;
}
```

This method handles the **tail-chasing edge case** - the classic interview trap!

### 4. Board - Grid Management

```java
public class Board {
    private final int height;
    private final int width;
    private Cell food;
    private final Random random;
}
```

**Key Methods**:

#### isWall() - Boundary Check
```java
public boolean isWall(Cell cell) {
    return cell.getRow() < 0 || cell.getRow() >= height ||
           cell.getCol() < 0 || cell.getCol() >= width;
}
```

Simple bounds check. O(1).

#### generateFood() - Safe Food Placement
```java
public void generateFood(Snake snake) {
    Cell newFood;
    while(true) {
        int r = random.nextInt(height);
        int c = random.nextInt(width);
        newFood = new Cell(r, c);
        
        if (!snake.checkCrash(newFood)) {
            break;
        }
    }
    this.food = newFood;
}
```

**Why Loop?**: Must ensure food doesn't spawn on snake body.

**Complexity**: Average O(1) when snake is small. Worst case O(n) when snake fills most of board.

### 5. SnakeGame - The Orchestrator

```java
public class SnakeGame {
    private final Snake snake;
    private final Board board;
    private boolean isGameOver;
    private int score;
}
```

**The move() Method - Core Algorithm**:

```java
public void move(Direction direction) {
    // Guard: No moves after game over
    if (isGameOver) return;

    // Step 1: Calculate next position
    Cell nextHead = snake.getNextHead(direction);

    // Step 2: Wall collision
    if (board.isWall(nextHead)) {
        isGameOver = true;
        return;
    }

    // Step 3: Pre-check food (affects collision logic)
    boolean willGrow = nextHead.equals(board.getFood());

    // Step 4: Self collision (with growth context)
    if (snake.checkCrash(nextHead, willGrow)) {
        isGameOver = true;
        return;
    }

    // Step 5: Execute move
    if (willGrow) {
        snake.grow(nextHead);
        score++;
        board.generateFood(snake);
    } else {
        snake.move(nextHead);
    }
}
```

**Why Check Order Matters**:

```
1. Wall check      → Fail fast if out of bounds
2. Food check      → Need this BEFORE collision (affects tail logic)
3. Self collision  → Pass growth flag for correct tail handling
4. Execute         → Only if all checks pass
```

---

## The Critical Data Structure Decision

### The Problem

Every game tick, we need to:
1. Add new head position → O(1) needed
2. Remove tail position → O(1) needed
3. Check if new position hits body → O(1) needed

### Why Deque + Set?

```java
private final Deque<Cell> body;      // LinkedList
private final Set<Cell> bodySet;     // HashSet
```

| Data Structure | Add Head | Remove Tail | Check Collision |
|----------------|----------|-------------|-----------------|
| ArrayList | O(n) ❌ | O(1) | O(n) ❌ |
| LinkedList only | O(1) ✓ | O(1) ✓ | O(n) ❌ |
| HashSet only | N/A | N/A | O(1) ✓ |
| **Deque + Set** | **O(1) ✓** | **O(1) ✓** | **O(1) ✓** |

### The Trade-off: Space vs Time

```
Space: O(2n) = O(n)  → We store each cell twice
Time:  All operations O(1) → Worth it!
```

**Interview Quote**: "We trade 2x memory for O(1) operations. For a snake game with at most a few hundred cells, this is an excellent trade-off. Memory is cheap; latency is expensive."

### Why Not Just LinkedList?

```java
// O(n) collision check - BAD for long snakes
public boolean checkCrash(Cell cell) {
    for (Cell c : body) {
        if (c.equals(cell)) return true;  // O(n) scan!
    }
    return false;
}
```

With HashSet:
```java
// O(1) collision check - GOOD
public boolean checkCrash(Cell cell) {
    return bodySet.contains(cell);  // O(1) lookup!
}
```

---

## Edge Cases & The Interview Trap

### Edge Case 1: Tail Chasing ⭐⭐⭐

This is **THE interview trap**. Most candidates get this wrong.

**Scenario**:
```
Snake: [H]→[B]→[T]
       Head moves to where Tail currently is
       
Is this a crash?
```

**Wrong Answer**: "Yes, it's in the body set!"

**Correct Answer**: "No! The tail will vacate before head arrives."

```
Before Move:        After Move:
[H]→[B]→[T]        [T']→[H]→[B]
         ↑              ↑
    Tail here      Head now here (tail moved)
```

**The Logic**:
```java
public boolean checkCrash(Cell nextHead, boolean isGrowing) {
    if (!bodySet.contains(nextHead)) return false;
    
    if (isGrowing) return true;  // Tail stays → crash
    
    // Not growing → tail leaves
    return !nextHead.equals(body.peekLast());  // Safe only if hitting tail
}
```

### Edge Case 2: Growing + Tail Position

**Scenario**:
```
Snake eats food that's at its current tail position
```

**Analysis**:
- Eating food → tail stays (snake grows)
- Head moves to tail position → tail is STILL there
- Result: **CRASH** (tail didn't vacate)

**The `isGrowing` flag handles this**:
```java
if (isGrowing) return true;  // Any body collision is fatal when growing
```

### Edge Case 3: Food Spawns on Snake

```java
public void generateFood(Snake snake) {
    Cell newFood;
    while(true) {  // Keep trying until valid
        newFood = new Cell(random.nextInt(height), random.nextInt(width));
        if (!snake.checkCrash(newFood)) break;
    }
    this.food = newFood;
}
```

### Edge Case 4: Moves After Game Over

```java
public void move(Direction direction) {
    if (isGameOver) return;  // Guard clause - ignore all moves
    // ...
}
```

### Edge Case 5: 180-Degree Turn

```
Moving RIGHT → instantly press LEFT → crash into neck
```

**Design Decision**: This is handled at the **UI layer**, not game logic.

```java
// In UI/Controller
if (currentDirection == RIGHT && newInput == LEFT) {
    // Ignore or keep current direction
}
```

**Why UI Responsibility?**
- Game engine is stateless regarding previous direction
- Each `move()` call is independent
- Keeps engine simple and focused

---

## Game Algorithm Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    move(Direction) ALGORITHM                 │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────────┐                                       │
│  │  isGameOver?     │───YES───► return (ignore move)        │
│  └────────┬─────────┘                                       │
│           │ NO                                               │
│           ▼                                                  │
│  ┌──────────────────┐                                       │
│  │ Calculate        │                                       │
│  │ nextHead         │  snake.getNextHead(direction)         │
│  └────────┬─────────┘                                       │
│           │                                                  │
│           ▼                                                  │
│  ┌──────────────────┐                                       │
│  │  isWall(next)?   │───YES───► gameOver = true; return     │
│  └────────┬─────────┘                                       │
│           │ NO                                               │
│           ▼                                                  │
│  ┌──────────────────┐                                       │
│  │ willGrow =       │                                       │
│  │ next == food     │  PRE-CHECK before collision!          │
│  └────────┬─────────┘                                       │
│           │                                                  │
│           ▼                                                  │
│  ┌──────────────────┐                                       │
│  │ checkCrash(next, │                                       │
│  │   willGrow)?     │───YES───► gameOver = true; return     │
│  └────────┬─────────┘                                       │
│           │ NO                                               │
│           ▼                                                  │
│  ┌──────────────────┐                                       │
│  │   willGrow?      │                                       │
│  └────────┬─────────┘                                       │
│      YES  │    NO                                            │
│    ┌──────┴──────┐                                          │
│    ▼             ▼                                          │
│ ┌──────┐    ┌──────┐                                        │
│ │grow()│    │move()│                                        │
│ │score+│    │      │                                        │
│ │food++│    │      │                                        │
│ └──────┘    └──────┘                                        │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Design Trade-offs & Why Our Choices Win

### Trade-off 1: Multiple Classes vs Single File

| Approach | Pros | Cons |
|----------|------|------|
| **Single File** | Fast to write | Hard to test, extend, maintain |
| **Our Approach** (5 files) | Testable, extensible, clear | More boilerplate |

**Why We Win**: In a staff-level interview, demonstrating understanding of separation of concerns matters more than brevity.

### Trade-off 2: Deque + Set vs Just LinkedList

| Approach | Memory | Time |
|----------|--------|------|
| LinkedList only | O(n) | O(n) collision |
| **Deque + Set** | O(2n) | **O(1) collision** |

**Why We Win**: 2x memory for O(1) operations is always worth it in games.

### Trade-off 3: Immutable Cell vs Mutable

| Approach | Thread Safety | HashSet Safety |
|----------|--------------|----------------|
| Mutable Cell | Unsafe | Can break HashSet |
| **Immutable Cell** | **Safe** | **Always works** |

**Why We Win**: Immutable objects can be safely shared, used as keys, and reasoned about.

### Trade-off 4: Method Overloading vs Single Method

| Approach | API Complexity | Flexibility |
|----------|---------------|-------------|
| Single method | Simple | Less flexible |
| **Overloading** | Slightly complex | **More flexible** |

```java
// We provide both:
checkCrash(Cell cell)                    // Simple: just check body
checkCrash(Cell cell, boolean isGrowing) // Smart: handle tail case
```

**Why We Win**: Different callers have different needs. Food placement only needs simple check; game logic needs smart check.

---

## SOLID Principles Analysis

### S - Single Responsibility

| Class | Single Responsibility |
|-------|----------------------|
| Cell | Store coordinates |
| Direction | Define movement constants |
| Snake | Manage snake body |
| Board | Manage grid + food |
| SnakeGame | Orchestrate game flow |

### O - Open/Closed

**Open for Extension**:
```java
// Can add new Board types without modifying SnakeGame
public class MazeBoard extends Board {
    private Set<Cell> walls;
    
    @Override
    public boolean isWall(Cell cell) {
        return super.isWall(cell) || walls.contains(cell);
    }
}
```

**Closed for Modification**: Core classes don't need changes to add features.

### L - Liskov Substitution

If we had `class MazeBoard extends Board`, it could substitute Board everywhere:
```java
SnakeGame game = new SnakeGame(new MazeBoard(...));  // Works!
```

### I - Interface Segregation

Our classes have focused interfaces:
- `Snake`: move(), grow(), checkCrash()
- `Board`: isWall(), generateFood(), getFood()

No "fat interfaces" with unused methods.

### D - Dependency Inversion

SnakeGame depends on abstractions (Snake, Board), not concrete implementations:
```java
public class SnakeGame {
    private final Snake snake;  // Could be interface
    private final Board board;  // Could be interface
}
```

---

## Extensibility Showcase

### Extension 1: Add Internal Walls

```java
public class MazeBoard extends Board {
    private Set<Cell> walls = new HashSet<>();
    
    public void addWall(Cell wall) {
        walls.add(wall);
    }
    
    @Override
    public boolean isWall(Cell cell) {
        return super.isWall(cell) || walls.contains(cell);
    }
    
    @Override
    public void generateFood(Snake snake) {
        Cell newFood;
        while(true) {
            newFood = randomCell();
            if (!snake.checkCrash(newFood) && !walls.contains(newFood)) {
                break;
            }
        }
        this.food = newFood;
    }
}
```

**Impact on existing code**: ZERO changes to Snake or SnakeGame!

### Extension 2: Power-ups (Speed Boost, Invincibility)

```java
public class PowerUp {
    private Cell position;
    private PowerUpType type;
}

public class EnhancedBoard extends Board {
    private List<PowerUp> powerUps = new ArrayList<>();
    
    public PowerUp getPowerUpAt(Cell cell) { ... }
}
```

### Extension 3: Multiplayer

```java
public class MultiplayerSnakeGame {
    private List<Snake> snakes;  // Multiple snakes!
    private Board board;
    
    public void move(int playerId, Direction direction) { ... }
}
```

---

## Common Interview Questions

### Q1: "Why separate Snake and Board classes?"

**Answer**: "Single Responsibility Principle. Snake manages body state and movement physics. Board manages grid boundaries and food. SnakeGame orchestrates them. This separation allows us to:
1. Test each component independently
2. Extend one without affecting others (e.g., MazeBoard)
3. Reason about each piece clearly"

### Q2: "Why use Deque + Set instead of just ArrayList?"

**Answer**: "Let me break down the complexity:
- ArrayList: O(n) for add at front, O(n) for contains
- LinkedList alone: O(1) add/remove, but O(n) contains
- Deque + Set: O(1) for ALL operations

We trade 2x memory for O(1) time. For a game where we might have 100+ body cells and 60 FPS, O(1) operations are essential for smooth gameplay."

### Q3: "Explain the tail-chasing edge case."

**Answer**: "When the snake is not eating food, its tail vacates the cell it's in. So if the head moves to where the tail currently is, it's actually safe - the tail will be gone by the time the head arrives.

My `checkCrash(Cell, boolean isGrowing)` method handles this:
- If not in body → safe
- If eating (tail stays) → any body hit is crash
- If not eating and hitting tail → safe (tail leaves)
- If not eating and hitting non-tail body → crash"

### Q4: "Why is Cell immutable?"

**Answer**: "Three reasons:
1. **HashSet safety**: If Cell were mutable and we changed its row after adding to HashSet, contains() would break (wrong hash bucket).
2. **Thread safety**: Immutable objects can be safely shared between threads without synchronization.
3. **Reasoning**: I can pass Cells around without worrying about unexpected mutations."

### Q5: "How would you add multiplayer?"

**Answer**: "The design is already prepared:
1. SnakeGame can hold `List<Snake>` instead of single Snake
2. `move(int playerId, Direction)` routes to correct snake
3. Collision check expands: `checkCrash` against ALL snake bodies
4. Board remains unchanged

The separation of concerns makes this extension straightforward."

### Q6: "Where does 180-degree turn prevention go?"

**Answer**: "In the UI/input layer, not the game engine. The game engine is stateless regarding previous direction - each `move()` is independent. This keeps the engine simple and testable. The UI/controller tracks current direction and filters out opposite-direction inputs."

---

## Quick Reference

### Time Complexity Summary

| Operation | Complexity |
|-----------|------------|
| move() | O(1) |
| grow() | O(1) |
| checkCrash() | O(1) |
| isWall() | O(1) |
| generateFood() | O(1) average, O(n) worst |

### Space Complexity

| Structure | Space |
|-----------|-------|
| body (Deque) | O(n) |
| bodySet (HashSet) | O(n) |
| Total | O(n) where n = snake length |

### Key Data Structures

```java
Deque<Cell> body      → LinkedList (O(1) addFirst, removeLast)
Set<Cell> bodySet     → HashSet (O(1) contains)
```

### The Tail-Chasing Rule

```
┌────────────────────────────────────────────────┐
│  TAIL CHASING DECISION TREE                    │
├────────────────────────────────────────────────┤
│                                                │
│  Is nextHead in bodySet?                       │
│  ├── NO  → SAFE (not hitting body)            │
│  └── YES → Is snake growing (eating food)?    │
│            ├── YES → CRASH (tail stays)       │
│            └── NO  → Is nextHead the tail?    │
│                     ├── YES → SAFE (tail      │
│                     │         vacates)        │
│                     └── NO  → CRASH (hitting  │
│                               non-tail body)  │
│                                                │
└────────────────────────────────────────────────┘
```

### Class Responsibilities

```
Cell       → Immutable coordinate, proper equals/hashCode
Direction  → Type-safe movement constants
Snake      → Body management (Deque + Set), movement, collision
Board      → Grid boundaries, food generation
SnakeGame  → Orchestration, game state (score, gameOver)
```

---

## Final Interview Quote

> "The key insight in this design is using **Deque for O(1) head/tail operations** combined with **HashSet for O(1) collision detection**. The architecture follows Single Responsibility with separate Snake, Board, and Game classes. The trickiest part is **tail-chasing** - when not growing, the tail cell vacates, so moving to it is safe. This is handled by the `checkCrash(Cell, boolean isGrowing)` method that knows whether to treat tail collision as safe or fatal."
