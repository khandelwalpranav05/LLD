# Chess Game - Low-Level Design Document

## Staff-Level Interview Reference Guide

---

## 1. Executive Summary

This document presents a **production-ready Chess game implementation** demonstrating:
- **Command Pattern** for move execution with full undo support
- **Template Method Pattern** for piece movement validation
- **Polymorphism** for extensible piece behaviors
- **Separation of Concerns** between game rules, board state, and piece logic

**Key Differentiator**: This design prioritizes **reversibility** and **extensibility** - critical for chess engines, analysis tools, and multiplayer scenarios.

---

## 2. Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────┐
│                           ChessDemo                                  │
│                    (Entry Point / Client)                           │
└────────────────────────────┬────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                             Game                                     │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ • Orchestrates game flow                                     │   │
│  │ • Validates turn order                                       │   │
│  │ • Detects check/checkmate                                    │   │
│  │ • Manages move history (Stack<Move>)                         │   │
│  │ • Synchronizes concurrent access                             │   │
│  └─────────────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────────────┘
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
        ┌──────────┐   ┌──────────┐   ┌──────────┐
        │  Board   │   │   Move   │   │  Player  │
        │          │   │(Command) │   │          │
        │ 8x8 Grid │   │execute() │   │ Name     │
        │ Cells    │   │undo()    │   │ Color    │
        └────┬─────┘   └──────────┘   └──────────┘
             │
             ▼
        ┌──────────┐
        │   Cell   │
        │ row, col │
        │  piece   │
        └────┬─────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Piece (Abstract)                              │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ + canMove(board, start, end): boolean [ABSTRACT]            │   │
│  │ + getType(): PieceType [ABSTRACT]                           │   │
│  │ + getSymbol(): String [ABSTRACT]                            │   │
│  │ # isPathClear(board, start, end): boolean [TEMPLATE HELPER] │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                              △                                       │
│          ┌───────┬───────┬───┴───┬───────┬───────┐                  │
│        King    Queen   Rook   Bishop  Knight   Pawn                 │
│    (1 sq any)(R+B)  (straight)(diagonal)(L-shape)(fwd+capture)      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 3. Design Patterns Deep Dive

### 3.1 Command Pattern (Move with Undo)

**Problem**: Chess requires move reversibility for:
- Undo functionality
- Check validation (try move → check if king exposed → rollback if illegal)
- Game analysis and replay
- AI move exploration (minimax, alpha-beta pruning)

**Solution**: `Move` class encapsulates the action as an object.

```java
// Command Pattern: Move is a command with execute() and undo()
public class Move {
    private Piece piece;
    private Cell startCell;
    private Cell endCell;
    private Piece capturedPiece;      // For undo - restore captured piece
    private boolean wasFirstMove;      // For undo - restore hasMoved state
    
    public void execute(Board board) {
        capturedPiece = endCell.getPiece();  // Save state
        endCell.setPiece(piece);
        startCell.setPiece(null);
        piece.setMoved(true);
    }
    
    public void undo(Board board) {
        startCell.setPiece(piece);
        endCell.setPiece(capturedPiece);     // Restore captured
        if (wasFirstMove) {
            piece.setMoved(false);           // Restore first-move state
        }
    }
}
```

**Why This Matters (Interview Talking Point)**:
> "I chose Command Pattern because chess requires transactional moves. When validating if a move is legal, we execute it, check if our king is now in check, and rollback if illegal. This same pattern enables undo functionality, game replay, and is essential if we extend this to support AI - where we need to explore thousands of moves without corrupting state."

**Trade-off Analysis**:

| Approach | Pros | Cons |
|----------|------|------|
| **Command Pattern (Chosen)** | Reversible, composable, supports history | Slight memory overhead per move |
| State Snapshot | Simple to implement | O(n²) memory per move for 8x8 board |
| Event Sourcing | Full audit trail | Over-engineered for this use case |

---

### 3.2 Template Method Pattern (Piece Movement)

**Problem**: All pieces share common validation logic (path clearing) but have unique movement rules.

**Solution**: Abstract `Piece` class with template helper method.

```java
public abstract class Piece {
    public abstract boolean canMove(Board board, Cell start, Cell end);
    
    // TEMPLATE HELPER: Shared by Rook, Bishop, Queen
    protected boolean isPathClear(Board board, Cell start, Cell end) {
        int rowDir = Integer.signum(end.getRow() - start.getRow());
        int colDir = Integer.signum(end.getCol() - start.getCol());
        
        int row = start.getRow() + rowDir;
        int col = start.getCol() + colDir;
        
        while (row != end.getRow() || col != end.getCol()) {
            if (!board.getCell(row, col).isEmpty()) {
                return false;
            }
            row += rowDir;
            col += colDir;
        }
        return true;
    }
}
```

**Usage in Subclasses**:

```java
// Queen = Rook + Bishop movement
public class Queen extends Piece {
    @Override
    public boolean canMove(Board board, Cell start, Cell end) {
        boolean straightLine = (start.getRow() == end.getRow() || 
                                start.getCol() == end.getCol());
        boolean diagonal = (rowDiff == colDiff);
        
        return (straightLine || diagonal) && isPathClear(board, start, end);
    }
}
```

**Interview Talking Point**:
> "The Template Method pattern here allows piece subclasses to focus solely on their unique movement geometry while inheriting the path-clearing logic. This eliminates code duplication across Rook, Bishop, and Queen, and ensures consistency. If we need to handle edge cases like 'jumping' pieces in the future, we only modify `isPathClear` once."

---

### 3.3 Polymorphism (Open/Closed Principle)

**Problem**: Adding new piece types (e.g., custom chess variants) should not require modifying existing code.

**Solution**: All piece-specific behavior delegated through abstract methods.

```java
// Game.java - Doesn't know about specific piece types
Piece piece = start.getPiece();
if (!piece.canMove(board, start, end)) {  // Polymorphic call
    return false;
}
```

**Adding a New Piece (e.g., "Amazon" = Queen + Knight)**:

```java
public class Amazon extends Piece {
    @Override
    public boolean canMove(Board board, Cell start, Cell end) {
        return new Queen(color).canMove(board, start, end) ||
               new Knight(color).canMove(board, start, end);
    }
}
```

**No changes needed to `Game`, `Board`, or `Move` classes!**

---

## 4. Class Responsibilities (Single Responsibility Principle)

| Class | Single Responsibility |
|-------|----------------------|
| `Game` | Orchestrates gameplay: turn management, win detection, move validation |
| `Board` | Manages 8x8 grid state: cell access, piece placement, attack detection |
| `Move` | Encapsulates a single move: execute, undo, track captured pieces |
| `Cell` | Container for position + optional piece reference |
| `Piece` | Defines movement rules for its type |
| `Player` | Holds player identity and color association |

**Interview Talking Point**:
> "Each class has exactly one reason to change. If we change how check detection works, only `Game` changes. If we change piece movement rules, only that `Piece` subclass changes. This isolation is critical for maintainability and testing."

---

## 5. Edge Cases & Failure Handling

### 5.1 Move Validation Chain

```java
public synchronized boolean makeMove(int startRow, int startCol, 
                                      int endRow, int endCol) {
    // 1. Game state check
    if (status != GameStatus.IN_PROGRESS) {
        System.out.println("Game is over!");
        return false;
    }
    
    // 2. Bounds check
    Cell start = board.getCell(startRow, startCol);
    Cell end = board.getCell(endRow, endCol);
    if (start == null || end == null) {
        System.out.println("Invalid coordinates.");
        return false;
    }
    
    // 3. Piece existence check
    Piece piece = start.getPiece();
    if (piece == null) {
        System.out.println("No piece at start position.");
        return false;
    }
    
    // 4. Turn validation
    if (piece.getColor() != currentPlayer.getColor()) {
        System.out.println("Not your turn.");
        return false;
    }
    
    // 5. Movement rule validation
    if (!piece.canMove(board, start, end)) {
        System.out.println("Invalid move for " + piece.getType());
        return false;
    }
    
    // 6. Self-capture prevention
    if (end.getPiece() != null && 
        end.getPiece().getColor() == piece.getColor()) {
        System.out.println("Cannot capture your own piece.");
        return false;
    }
    
    // 7. CRITICAL: Check exposure validation
    Move move = new Move(piece, start, end);
    move.execute(board);
    if (isInCheck(currentPlayer.getColor())) {
        move.undo(board);  // ROLLBACK - Command Pattern in action!
        System.out.println("Illegal move: would leave King in check!");
        return false;
    }
    
    // Move is legal - commit to history
    moveHistory.push(move);
    // ... continue with check/checkmate detection
}
```

### 5.2 Check Detection

```java
public boolean isInCheck(Color color) {
    Cell kingCell = board.findKing(color);
    return board.isSquareUnderAttack(
        kingCell.getRow(), 
        kingCell.getCol(), 
        color.opposite()
    );
}
```

**How `isSquareUnderAttack` Works**:

```java
public boolean isSquareUnderAttack(int row, int col, Color attackerColor) {
    for (int i = 0; i < SIZE; i++) {
        for (int j = 0; j < SIZE; j++) {
            Piece p = cells[i][j].getPiece();
            if (p != null && p.getColor() == attackerColor) {
                // Use polymorphic canMove to check if piece can reach target
                if (p.canMove(this, cells[i][j], cells[row][col])) {
                    return true;
                }
            }
        }
    }
    return false;
}
```

### 5.3 Checkmate vs Check Detection

**Critical Distinction**: Being in check ≠ checkmate. Checkmate requires:
1. King is in check, AND
2. **No legal move exists** that can escape the check

```java
public boolean isCheckmate(Color color) {
    if (!isInCheck(color)) {
        return false; // Can't be checkmate if not in check
    }
    return !hasAnyLegalMove(color);
}

private boolean hasAnyLegalMove(Color color) {
    // Try every piece of this color
    for (int row = 0; row < Board.SIZE; row++) {
        for (int col = 0; col < Board.SIZE; col++) {
            Piece piece = board.getCell(row, col).getPiece();
            if (piece == null || piece.getColor() != color) continue;
            
            // Try every possible destination
            for (int endRow = 0; endRow < Board.SIZE; endRow++) {
                for (int endCol = 0; endCol < Board.SIZE; endCol++) {
                    Cell endCell = board.getCell(endRow, endCol);
                    if (!piece.canMove(board, startCell, endCell)) continue;
                    if (endCell.getPiece() != null && 
                        endCell.getPiece().getColor() == color) continue;
                    
                    // Simulate the move
                    Move testMove = new Move(piece, startCell, endCell);
                    testMove.execute(board);
                    boolean stillInCheck = isInCheck(color);
                    testMove.undo(board);  // Command Pattern enables this!
                    
                    if (!stillInCheck) {
                        return true; // Found escape route!
                    }
                }
            }
        }
    }
    return false; // No escape - it's checkmate
}
```

**Stalemate Detection** (same pattern):
```java
public boolean isStalemate(Color color) {
    if (isInCheck(color)) return false; // Not stalemate if in check
    return !hasAnyLegalMove(color);
}
```

**Interview Talking Point**:
> "Check detection reuses the same `canMove()` logic we wrote for piece movement. This means if we fix a bug in Bishop movement, it automatically fixes Bishop attack detection. DRY principle in action.
>
> For checkmate, we try every possible move using the Command Pattern to simulate and undo. This is O(n⁴) worst case (64 squares × 64 destinations) but runs only after each move when opponent is in check - perfectly acceptable for human play."

### 5.4 Edge Cases Handled

| Edge Case | How Handled |
|-----------|-------------|
| Move into check | Execute → detect → undo → reject |
| Capture own piece | Pre-validated before execution |
| Move when game over | GameStatus check at start |
| Invalid coordinates | Null check from `getCell()` |
| Empty source cell | Null piece check |
| Wrong player's piece | Color comparison |
| Pawn double move | `hasMoved` flag tracking |
| Undo restores captured piece | `Move.capturedPiece` field |
| Undo restores first-move state | `Move.wasFirstMove` flag |
| Check vs Checkmate | `hasAnyLegalMove()` tries all escapes |
| Stalemate | Not in check + no legal moves |

---

## 6. Concurrency Considerations

```java
public class Game {
    // Synchronized to handle concurrent move attempts
    public synchronized boolean makeMove(...) { ... }
    public synchronized boolean undoLastMove() { ... }
}
```

**Why `synchronized`**:
- Online multiplayer: Both players could submit moves simultaneously
- Prevents race conditions in check detection
- Ensures atomic execute-validate-commit/rollback

**Interview Talking Point**:
> "While this implementation uses method-level synchronization, for a high-performance chess server I'd consider:
> 1. Fine-grained locking per game instance
> 2. Optimistic locking with version numbers for move submission
> 3. Event sourcing with CQRS for spectator views"

---

## 7. Pawn Special Rules

The Pawn implementation demonstrates handling complex, stateful rules:

```java
public class Pawn extends Piece {
    @Override
    public boolean canMove(Board board, Cell start, Cell end) {
        int direction = (color == Color.WHITE) ? 1 : -1;
        int rowDiff = end.getRow() - start.getRow();
        int colDiff = Math.abs(end.getCol() - start.getCol());
        
        // RULE 1: Forward move (non-capture)
        if (colDiff == 0 && end.isEmpty()) {
            if (rowDiff == direction) return true;  // Single step
            
            // RULE 2: Double step from starting position
            if (!hasMoved && rowDiff == 2 * direction) {
                Cell middle = board.getCell(start.getRow() + direction, 
                                            start.getCol());
                return middle.isEmpty();  // Path must be clear
            }
        }
        
        // RULE 3: Diagonal capture
        if (colDiff == 1 && rowDiff == direction && !end.isEmpty()) {
            return end.getPiece().getColor() != this.color;
        }
        
        return false;
    }
}
```

**Key Design Decisions**:
1. **Direction based on color**: White moves up (+1 row), Black moves down (-1 row)
2. **`hasMoved` flag**: Enables two-step initial move, correctly restored on undo
3. **Capture requires opponent piece**: Diagonal move only valid when capturing

---

## 8. Extensibility Analysis

### 8.1 Adding Castling

```java
// In King.canMove()
public boolean canMove(Board board, Cell start, Cell end) {
    // ... existing 1-square logic ...
    
    // Castling: King moves 2 squares toward rook
    if (!hasMoved && rowDiff == 0 && colDiff == 2) {
        int rookCol = (end.getCol() > start.getCol()) ? 7 : 0;
        Cell rookCell = board.getCell(start.getRow(), rookCol);
        Piece rook = rookCell.getPiece();
        
        if (rook instanceof Rook && !rook.hasMoved()) {
            // Check path clear and not through check
            return isPathClear(board, start, rookCell) &&
                   !board.isSquareUnderAttack(start.getRow(), 
                       (start.getCol() + end.getCol()) / 2, 
                       color.opposite());
        }
    }
}
```

**Required Changes**: Only `King.canMove()` - demonstrates Open/Closed adherence.

### 8.2 Adding En Passant

Would require:
1. Track last move in `Game` (already have `moveHistory`)
2. Pawn checks if adjacent pawn just made double move
3. Special capture handling in `Move.execute()`

### 8.3 Adding Pawn Promotion

```java
public class Move {
    private PieceType promotionType;  // New field
    
    public void execute(Board board) {
        // ... existing logic ...
        if (piece instanceof Pawn && isPromotionRank(end)) {
            endCell.setPiece(createPiece(promotionType, piece.getColor()));
        }
    }
}
```

---

## 9. Alternative Approaches Comparison

### 9.1 Movement Validation Strategies

| Approach | Our Design | Alternative: Move Generator |
|----------|------------|----------------------------|
| **Philosophy** | "Is this specific move valid?" | "Generate all valid moves first" |
| **Check Detection** | Try move, validate, rollback | Pre-filter moves that leave king in check |
| **Performance** | O(1) per move validation | O(n) upfront, O(1) lookup |
| **Memory** | Minimal | Stores all possible moves |
| **Use Case** | Human play, simple AI | Advanced AI (needs all moves anyway) |

**Interview Talking Point**:
> "Our approach is request-driven: validate what the player wants to do. For chess engines that need to explore millions of positions, you'd use move generators. But for human-playable games, our approach is simpler and equally efficient."

### 9.2 Board Representation

| Approach | Pros | Cons |
|----------|------|------|
| **2D Array (Chosen)** | Intuitive, O(1) access | Fixed size |
| Bitboard | Extremely fast, cache-friendly | Complex to implement, harder to debug |
| Map<Position, Piece> | Sparse, flexible | Slower access |

---

## 10. Interview-Ready Questions & Answers

### Q1: "Why not use inheritance for movement patterns?"

**Answer**:
> "You might think Queen = Rook + Bishop suggests multiple inheritance. But Java doesn't support it, and even if it did, diamond inheritance creates ambiguity. Instead, I used composition within Queen's `canMove()` - it checks 'is this a rook-style move OR a bishop-style move?' This is cleaner and more explicit."

### Q2: "How would you add draw detection (stalemate, 50-move rule, threefold repetition)?"

**Answer**:
> "Stalemate: In `isCheckmate()`, check if player has zero legal moves but is NOT in check.
> 
> 50-move rule: Add a counter in `Game` that resets on pawn moves or captures, increments otherwise. Check after each move.
> 
> Threefold repetition: Hash board positions (piece placements + castling rights + en passant square). Use a `Map<BoardHash, Integer>` to count occurrences."

### Q3: "This check detection is O(n²) per move. How would you optimize?"

**Answer**:
> "For human play, O(64) scans are negligible. For AI:
> 1. **Maintain attack tables**: When pieces move, incrementally update which squares they attack
> 2. **Piece lists**: Track piece positions in lists instead of scanning the board
> 3. **Bitboards**: Represent attacks as 64-bit integers, check with single AND operation
> 
> But I'd only add this complexity if profiling shows it's needed. Premature optimization is the root of all evil."

### Q4: "How would you make this distributed for online play?"

**Answer**:
> "The Command Pattern is key here. Moves are already serializable actions:
> 1. Client sends `MoveRequest(startRow, startCol, endRow, endCol)`
> 2. Server validates and executes (using our existing `makeMove()`)
> 3. Server broadcasts `MoveEvent` to both clients
> 4. Clients apply the move to their local boards
> 
> For consistency, server is authoritative. Clients show optimistic updates but rollback if server rejects."

### Q5: "What's the time complexity of your solution?"

**Answer**:
> | Operation | Complexity | Reason |
> |-----------|------------|--------|
> | `makeMove()` | O(n) | n=64 for check detection |
> | `canMove()` | O(n) | Path clearing for sliding pieces |
> | `undo()` | O(1) | Direct cell manipulation |
> | `findKing()` | O(n) | Could optimize with king position tracking |

---

## 11. Code Quality Highlights

### 11.1 Defensive Programming

```java
public Cell getCell(int row, int col) {
    if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) 
        return null;  // Bounds protection
    return cells[row][col];
}
```

### 11.2 Meaningful Abstractions

```java
// Color knows its opposite - encapsulated logic
public enum Color {
    WHITE, BLACK;
    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
```

### 11.3 Clear Piece Symbols

```java
// Uppercase = White, Lowercase = Black (Standard notation)
public String getSymbol() { 
    return color == Color.WHITE ? "K" : "k"; 
}
```

---

## 12. Testing Strategy

### Unit Tests to Write

```java
// Piece Movement Tests
@Test void testPawnSingleStep() { ... }
@Test void testPawnDoubleStepFromStart() { ... }
@Test void testPawnCannotDoubleStepAfterFirstMove() { ... }
@Test void testPawnDiagonalCapture() { ... }
@Test void testPawnCannotMoveBackward() { ... }

// Game Rule Tests
@Test void testCannotMoveIntoCheck() { ... }
@Test void testMustMoveOutOfCheck() { ... }
@Test void testCheckmateDetection() { ... }
@Test void testUndoRestoresCapturedPiece() { ... }
@Test void testUndoRestoresFirstMoveFlag() { ... }

// Edge Cases
@Test void testCannotCaptureOwnPiece() { ... }
@Test void testCannotMoveOpponentPiece() { ... }
@Test void testMoveAfterGameOver() { ... }
```

---

## 13. Potential Improvements

| Improvement | Effort | Impact |
|-------------|--------|--------|
| Complete checkmate detection | Medium | Required for real game |
| Add castling | Medium | Standard chess rule |
| Add en passant | Medium | Standard chess rule |
| Add pawn promotion | Low | Standard chess rule |
| Algebraic notation parsing | Low | Better UX |
| Move timer | Low | Tournament feature |
| Game persistence | Medium | Save/load games |
| AI opponent | High | Single-player mode |

---

## 14. Summary: Why This Design Wins

1. **Command Pattern for Moves**: Enables undo, check validation, game replay, and AI exploration
2. **Polymorphic Pieces**: Adding new piece types requires zero changes to core logic
3. **Template Method for Path Clearing**: DRY code, consistent behavior across sliding pieces
4. **Defensive Validation**: Comprehensive edge case handling with clear error messages
5. **Thread Safety**: Ready for multiplayer scenarios
6. **Separation of Concerns**: Each class has one job, easy to test and modify

**Closing Statement for Interview**:
> "This design balances simplicity with extensibility. It's not over-engineered with unnecessary patterns, but every pattern used serves a clear purpose. The Command Pattern isn't just for undo - it's the foundation for check validation, AI move exploration, and game replay. The polymorphic piece hierarchy isn't just OOP for OOP's sake - it directly enables variant chess games without touching core code. That's what I consider production-quality design."

