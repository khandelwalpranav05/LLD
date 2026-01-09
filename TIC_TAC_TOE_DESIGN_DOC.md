# Tic Tac Toe LLD - Staff-Level Design Document

## Table of Contents
1. [Problem Statement](#problem-statement)
2. [Current Implementation Analysis](#current-implementation-analysis)
3. [Design Patterns Used](#design-patterns-used)
4. [Key Design Decisions](#key-design-decisions)
5. [Interview Complexity Assessment](#interview-complexity-assessment)
6. [Simplified Version for Interview](#simplified-version-for-interview)
7. [Common Interview Questions](#common-interview-questions)

---

## Problem Statement

Design a Tic Tac Toe game that:
- Supports NxN board (extensible beyond 3x3)
- Supports Human vs Human, Human vs Bot, Bot vs Bot
- Detects win/draw conditions
- (Optional) Supports undo functionality

---

## Current Implementation Analysis

### File Count: 15 files (COMPLEX for 45-min interview)

```
com.tictactoe/
├── model/           (10 files)
│   ├── Symbol.java          (enum: X, O)
│   ├── Cell.java            (row, col, symbol)
│   ├── Board.java           (grid + memento support)
│   ├── BoardMemento.java    (snapshot for undo)
│   ├── Player.java          (abstract)
│   ├── HumanPlayer.java     (input from console)
│   ├── BotPlayer.java       (simple AI)
│   ├── PlayerType.java      (enum: HUMAN, BOT)
│   ├── Move.java            (cell + player)
│   └── GameStatus.java      (enum)
├── service/
│   └── Game.java            (main game loop)
├── strategy/        (2 files)
│   ├── WinningStrategy.java (interface)
│   └── OrderOneWinningStrategy.java (O(1) check!)
├── factory/
│   └── PlayerFactory.java
└── TicTacToeDemo.java
```

### Design Patterns Used: 3
1. **Strategy** - WinningStrategy (O(1) vs O(n²))
2. **Memento** - BoardMemento (Undo support)
3. **Factory** - PlayerFactory (Human/Bot creation)

### Verdict: **Good design but too complex for 45-min interview. Need simplified version.**

---

## Design Patterns Used

### 1. Strategy Pattern (Winning Check)

```java
public interface WinningStrategy {
    boolean checkWinner(Board board, Move lastMove);
}

// O(1) Implementation - counts symbols in rows/cols/diagonals
public class OrderOneWinningStrategy implements WinningStrategy {
    private Map<Integer, Map<Symbol, Integer>> rowCounts;
    private Map<Integer, Map<Symbol, Integer>> colCounts;
    private Map<Symbol, Integer> leftDiagonalCounts;
    private Map<Symbol, Integer> rightDiagonalCounts;
    
    @Override
    public boolean checkWinner(Board board, Move lastMove) {
        // Update and check 4 counters max (row, col, 2 diagonals)
        // O(1) time!
    }
}
```

**Why Strategy?**
- O(1) strategy: Fast but complex, needs state management
- O(n) strategy: Simple, scan row/col/diagonal after each move
- Interviewer might ask for either - strategy pattern lets us swap!

### 2. Memento Pattern (Undo)

```java
public class BoardMemento {
    private final List<List<Symbol>> gridSnapshot;
    
    public BoardMemento(List<List<Cell>> cells) {
        // Deep copy of current board state
    }
}

// In Board.java
public BoardMemento createMemento() {
    return new BoardMemento(cells);
}

public void restore(BoardMemento memento) {
    // Restore grid from snapshot
}
```

**Why Memento?**
- Enables undo without exposing Board's internal state
- Clean separation: Board doesn't know about Game's undo logic

### 3. Factory Pattern (Player Creation)

```java
public class PlayerFactory {
    public static Player createPlayer(PlayerType type, String name, Symbol symbol) {
        switch (type) {
            case HUMAN: return new HumanPlayer(name, symbol);
            case BOT:   return new BotPlayer(name, symbol);
        }
    }
}
```

**Why Factory?**
- Centralizes player creation logic
- Easy to add new player types (e.g., SmartBot)

---

## Key Design Decisions

### Decision 1: Symbol as Enum vs Character

```java
// ✅ GOOD: Type-safe, clear intent
public enum Symbol { X, O }

// ❌ BAD: Error-prone, can set invalid values
private char symbol; // 'X', 'O', or 'A'??
```

### Decision 2: Abstract Player Class

```java
public abstract class Player {
    public abstract Cell decideMove(Board board);
}

public class HumanPlayer extends Player {
    @Override
    public Cell decideMove(Board board) {
        // Read from Scanner
    }
}

public class BotPlayer extends Player {
    @Override
    public Cell decideMove(Board board) {
        // Find first empty cell (or use AI)
    }
}
```

**Why**: Polymorphism - Game doesn't need to know if player is human or bot.

### Decision 3: O(1) vs O(n) Winning Strategy

| Approach | Time | Space | Complexity |
|----------|------|-------|------------|
| O(n²) - Scan all | O(n²) per move | O(1) | Simple |
| O(n) - Scan affected row/col/diag | O(n) per move | O(1) | Medium |
| **O(1) - Count tracking** | **O(1) per move** | **O(n)** | Complex |

**Trade-off**: O(1) is optimal but requires maintaining counters for rows, columns, and diagonals.

### Decision 4: Move Validation in Game vs Board

```java
// ✅ Current: Validation in Game (SRP - Game manages rules)
private boolean validateMove(Cell cell) {
    if (r < 0 || r >= size || c < 0 || c >= size) return false;
    return board.getCells().get(r).get(c).isEmpty();
}
```

**Why in Game**: Board is just data. Game enforces rules.

---

## Interview Complexity Assessment

| Component | Current | Interview Version |
|-----------|---------|-------------------|
| Files | 15 | **6-7** |
| Patterns | 3 (Strategy, Memento, Factory) | **0-1** |
| Win Check | O(1) with counters | **O(n) simple scan** |
| Undo | Yes (Memento) | **Skip** |
| Bot AI | Simple | **Simple or skip** |
| Player hierarchy | Abstract + 2 subclasses | **Simple class or skip** |

---

## Simplified Version for Interview ⭐

### Target: 6 files, ~150 lines, 35 minutes

```
com.tictactoe.simple/
├── Symbol.java          (5 lines)
├── Cell.java            (20 lines)
├── Board.java           (60 lines - includes win check)
├── Player.java          (15 lines)
├── Game.java            (50 lines)
└── TicTacToeDemo.java   (30 lines)
```

### Key Simplifications

| Full Version | Simple Version |
|--------------|----------------|
| Abstract Player + Human/Bot | Simple Player class |
| WinningStrategy interface | Win check in Board |
| O(1) winning algorithm | O(n) row/col/diag scan |
| Memento for undo | Skip undo |
| PlayerFactory | Direct instantiation |
| Move class | Pass row, col directly |

---

## Common Interview Questions

### Q1: "How do you check for a winner?"
**Quick Answer (O(n))**:
```java
// After each move at (row, col), check:
// 1. Row - all cells in that row have same symbol
// 2. Column - all cells in that column have same symbol
// 3. Main diagonal (if row == col)
// 4. Anti-diagonal (if row + col == n-1)
```

**Advanced Answer (O(1))**:
```java
// Maintain counters:
// - rowCount[row][symbol]
// - colCount[col][symbol]
// - diagonalCount[symbol], antiDiagonalCount[symbol]
// When any count reaches n → winner!
```

### Q2: "How would you handle NxN board?"
**Answer**: "Board size is configurable. Winning condition changes: need N in a row, not just 3. The O(1) strategy works for any N."

### Q3: "How would you add different bot difficulties?"
**Answer**: "Use Strategy pattern for BotStrategy. EasyBot picks random empty cell. MediumBot blocks opponent. HardBot uses Minimax algorithm."

### Q4: "How would you add undo?"
**Answer**: "Memento pattern. Before each move, save board snapshot to a stack. Undo pops the stack and restores the board."

### Q5: "Human vs Bot - how is move different?"
**Answer**: "Abstract Player class with decideMove() method. HumanPlayer reads from input. BotPlayer computes the move. Game just calls player.decideMove() polymorphically."

---

## Interview Flow (45 mins)

| Time | Task | What to Write |
|------|------|---------------|
| 0-3 | Requirements | Clarify: board size, 2 players, symbols |
| 3-8 | Enums + Cell | Symbol enum, Cell class |
| 8-18 | Board | Grid + printBoard + checkWinner |
| 18-25 | Player | Simple player class |
| 25-35 | Game | makeMove loop, turn management |
| 35-45 | Discussion | Extensions, edge cases |

---

## Simplified Code Reference

### Symbol.java
```java
public enum Symbol { X, O }
```

### Cell.java
```java
public class Cell {
    private int row, col;
    private Symbol symbol;
    
    public boolean isEmpty() { return symbol == null; }
    // getters, setters
}
```

### Board.java (Key Focus!)
```java
public class Board {
    private int size;
    private Cell[][] grid;
    
    public Board(int size) { /* init grid */ }
    
    public boolean makeMove(int row, int col, Symbol symbol) {
        if (!isValid(row, col) || !grid[row][col].isEmpty()) return false;
        grid[row][col].setSymbol(symbol);
        return true;
    }
    
    // O(n) win check - scan row, col, diagonals
    public boolean checkWinner(int row, int col, Symbol symbol) {
        return checkRow(row, symbol) || 
               checkCol(col, symbol) ||
               checkDiagonal(symbol) ||
               checkAntiDiagonal(symbol);
    }
    
    private boolean checkRow(int row, Symbol symbol) {
        for (int c = 0; c < size; c++) {
            if (grid[row][c].getSymbol() != symbol) return false;
        }
        return true;
    }
    // Similar for col, diagonal, anti-diagonal
}
```

### Game.java
```java
public class Game {
    private Board board;
    private Player[] players;
    private int currentPlayer = 0;
    private boolean gameOver = false;
    
    public void play() {
        while (!gameOver) {
            Player p = players[currentPlayer];
            // Get move from player
            // Make move on board
            // Check winner → gameOver = true
            // Check draw → gameOver = true
            // Switch player
        }
    }
}
```

---

## What Makes This Answer Stand Out

### 1. Trade-off Awareness
> "I chose O(n) win check for simplicity. O(1) is possible by tracking counts per row/col/diagonal, trading space for time."

### 2. Extensibility Thinking
> "The design supports NxN boards. For larger boards, we might want O(1) winning strategy."

### 3. Pattern Knowledge (Mention, Don't Over-engineer)
> "For undo, I would use Memento pattern. For different bot difficulties, Strategy pattern. But for this interview, I'll keep it simple."

### 4. Clean Code
- Enums over magic strings
- Single Responsibility (Board manages grid, Game manages rules)
- Clear method names

---

## Final Interview Quote

> "This simplified design handles core Tic Tac Toe: NxN board, two players taking turns, win/draw detection with O(n) check on affected row/col/diagonals. For extensions like undo (Memento), different bot AI (Strategy), or O(1) win check, the architecture can evolve. But the core is clean and testable."

