# ♟️ Chess LLD Cheatsheet

## Quick Reference During Interview

---

## 📁 File Structure (16 files)

```
com.chess/
├── model/           ← Enums + Data classes
│   ├── Color.java       (WHITE, BLACK + opposite())
│   ├── PieceType.java   (KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN)
│   ├── GameStatus.java  (IN_PROGRESS, WHITE_WINS, BLACK_WINS, STALEMATE)
│   ├── Cell.java        (row, col, piece)
│   ├── Move.java        (Command Pattern: execute/undo)
│   └── Player.java      (name, color)
├── piece/           ← Abstract Piece + 6 implementations
│   ├── Piece.java       (abstract: canMove, getType, getSymbol)
│   ├── King.java        (1 step any direction)
│   ├── Queen.java       (Rook + Bishop)
│   ├── Rook.java        (straight lines)
│   ├── Bishop.java      (diagonals)
│   ├── Knight.java      (L-shape, can jump)
│   └── Pawn.java        (forward, 2-step first, diagonal capture)
├── service/
│   ├── Board.java       (8x8 grid, initialize, findKing, isUnderAttack)
│   └── Game.java        (makeMove, check, checkmate, stalemate)
└── ChessDemo.java
```

---

## 📊 UML Class Diagram

### High-Level Architecture
```
┌─────────────────────────────────────────────────────────────────────┐
│                              GAME                                    │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐      │
│  │  Game    │───▶│  Board   │───▶│  Cell    │───▶│  Piece   │      │
│  └──────────┘    └──────────┘    └──────────┘    └──────────┘      │
│       │              │                                ▲             │
│       │              │                                │             │
│       ▼              │           ┌────────────────────┼────────┐   │
│  ┌──────────┐        │           │    │    │    │     │    │   │   │
│  │ Player[] │        │          King Queen Rook Bishop Knight Pawn │
│  └──────────┘        │                                             │
│       │              │                                             │
│       ▼              ▼                                             │
│  ┌──────────┐   ┌──────────┐                                       │
│  │  Color   │   │  Move    │ ◀── Command Pattern                   │
│  └──────────┘   └──────────┘                                       │
└─────────────────────────────────────────────────────────────────────┘
```

### Detailed Class Diagram
```
┌─────────────────────────────────────┐
│             «enum» Color            │
├─────────────────────────────────────┤
│ WHITE                               │
│ BLACK                               │
├─────────────────────────────────────┤
│ + opposite(): Color                 │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│           «enum» PieceType          │
├─────────────────────────────────────┤
│ KING, QUEEN, ROOK                   │
│ BISHOP, KNIGHT, PAWN                │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│          «enum» GameStatus          │
├─────────────────────────────────────┤
│ IN_PROGRESS, WHITE_WINS             │
│ BLACK_WINS, STALEMATE               │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│              Player                 │
├─────────────────────────────────────┤
│ - name: String                      │
│ - color: Color                      │
├─────────────────────────────────────┤
│ + getName(): String                 │
│ + getColor(): Color                 │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│               Cell                  │
├─────────────────────────────────────┤
│ - row: int                          │
│ - col: int                          │
│ - piece: Piece                      │
├─────────────────────────────────────┤
│ + isEmpty(): boolean                │
│ + getPiece(): Piece                 │
│ + setPiece(Piece): void             │
└─────────────────────────────────────┘
           │
           │ has-a
           ▼
┌─────────────────────────────────────┐
│        «abstract» Piece             │
├─────────────────────────────────────┤
│ # color: Color                      │
│ # hasMoved: boolean                 │
├─────────────────────────────────────┤
│ + «abstract» canMove(Board,         │
│              Cell, Cell): boolean   │
│ + «abstract» getType(): PieceType   │
│ + «abstract» getSymbol(): String    │
│ # isPathClear(Board,Cell,Cell): bool│
└─────────────────────────────────────┘
           ▲
           │ extends
     ┌─────┴─────┬─────────┬─────────┬─────────┬─────────┐
     │           │         │         │         │         │
┌────┴────┐ ┌────┴────┐ ┌──┴──┐ ┌────┴────┐ ┌──┴───┐ ┌───┴──┐
│  King   │ │  Queen  │ │Rook │ │ Bishop  │ │Knight│ │ Pawn │
├─────────┤ ├─────────┤ ├─────┤ ├─────────┤ ├──────┤ ├──────┤
│canMove()│ │canMove()│ │ ... │ │   ...   │ │ ...  │ │ ...  │
│ 1 step  │ │Rook+Bish│ │     │ │         │ │      │ │      │
└─────────┘ └─────────┘ └─────┘ └─────────┘ └──────┘ └──────┘


┌─────────────────────────────────────┐
│        Move (Command Pattern)       │
├─────────────────────────────────────┤
│ - piece: Piece                      │
│ - startCell: Cell                   │
│ - endCell: Cell                     │
│ - capturedPiece: Piece              │
│ - wasFirstMove: boolean             │
├─────────────────────────────────────┤
│ + execute(Board): void              │
│ + undo(Board): void                 │
└─────────────────────────────────────┘


┌─────────────────────────────────────┐
│               Board                 │
├─────────────────────────────────────┤
│ - cells: Cell[8][8]                 │
│ + SIZE: int = 8                     │
├─────────────────────────────────────┤
│ + initialize(): void                │
│ + getCell(row, col): Cell           │
│ + findKing(Color): Cell             │
│ + isSquareUnderAttack(row, col,     │
│                  attackerColor): bool│
│ + printBoard(): void                │
└─────────────────────────────────────┘
           │
           │ has-a
           ▼
┌─────────────────────────────────────┐
│               Game                  │
├─────────────────────────────────────┤
│ - board: Board                      │
│ - players: Player[2]                │
│ - currentPlayerIndex: int           │
│ - status: GameStatus                │
│ - moveHistory: Stack<Move>          │
├─────────────────────────────────────┤
│ + start(): void                     │
│ + makeMove(sr,sc,er,ec): boolean    │
│ + undoLastMove(): boolean           │
│ + isInCheck(Color): boolean         │
│ + isCheckmate(Color): boolean       │
│ + isStalemate(Color): boolean       │
│ - hasAnyLegalMove(Color): boolean   │
└─────────────────────────────────────┘
```

### Relationships Summary
```
┌──────────────────────────────────────────────────────────┐
│                    RELATIONSHIPS                          │
├──────────────────────────────────────────────────────────┤
│  Game ──────────▶ Board           (1:1 composition)      │
│  Game ──────────▶ Player[2]       (1:2 association)      │
│  Game ──────────▶ Stack<Move>     (1:* aggregation)      │
│  Board ─────────▶ Cell[8][8]      (1:64 composition)     │
│  Cell ──────────▶ Piece           (1:0..1 association)   │
│  Move ──────────▶ Piece           (reference)            │
│  Move ──────────▶ Cell (start/end)(reference)            │
│  Piece ◁─────────King, Queen...   (inheritance)          │
│  Player ────────▶ Color           (association)          │
└──────────────────────────────────────────────────────────┘
```

### Quick Draw Version (For Whiteboard)
```
        Game
         │
    ┌────┴────┐
    ▼         ▼
  Board    Player[]
    │
    ▼
  Cell[8][8]
    │
    ▼
  Piece (abstract)
    △
    │
  ┌─┴─┬───┬───┬───┬───┐
  K   Q   R   B   N   P

  Move ←── Command Pattern
  - execute()
  - undo()
```

---

## 🎯 Design Patterns Used

| Pattern | Where | Why |
|---------|-------|-----|
| **Command** | `Move.java` | execute() + undo() for move history |
| **Template Method** | `Piece.java` | Abstract canMove(), concrete in subclasses |
| **Composition** | `Cell` has `Piece` | Cell contains piece, not IS-A |

---

## 🧩 Core Classes Quick Reference

### 1. Cell (Simple)
```java
class Cell {
    int row, col;
    Piece piece;  // null if empty
    boolean isEmpty() { return piece == null; }
}
```

### 2. Piece (Abstract)
```java
abstract class Piece {
    Color color;
    boolean hasMoved;  // For Pawn 2-step, Castling
    
    abstract boolean canMove(Board, Cell start, Cell end);
    abstract PieceType getType();
    abstract String getSymbol();
    
    // Helper for Rook/Bishop/Queen
    protected boolean isPathClear(Board, start, end);
}
```

### 3. Move (Command Pattern ⭐)
```java
class Move {
    Piece piece;
    Cell startCell, endCell;
    Piece capturedPiece;  // For undo
    boolean wasFirstMove; // For undo
    
    void execute(Board) {
        capturedPiece = endCell.getPiece();
        endCell.setPiece(piece);
        startCell.setPiece(null);
        piece.setMoved(true);
    }
    
    void undo(Board) {
        startCell.setPiece(piece);
        endCell.setPiece(capturedPiece);
        if (wasFirstMove) piece.setMoved(false);
    }
}
```

### 4. Board
```java
class Board {
    Cell[][] cells = new Cell[8][8];
    
    void initialize();          // Setup pieces
    Cell findKing(Color);       // Find king position
    boolean isSquareUnderAttack(row, col, attackerColor);
}
```

### 5. Game (Main Logic ⭐)
```java
class Game {
    Board board;
    Player[] players;
    int currentPlayerIndex;
    Stack<Move> moveHistory;
    
    boolean makeMove(startRow, startCol, endRow, endCol);
    boolean undoLastMove();
    boolean isInCheck(Color);
    boolean isCheckmate(Color);
    boolean isStalemate(Color);
}
```

---

## ♟️ Piece Movement Rules

| Piece | Rule | Code Essence |
|-------|------|--------------|
| **King** | 1 step any direction | `rowDiff <= 1 && colDiff <= 1` |
| **Queen** | Rook + Bishop | `(straight \|\| diagonal) && pathClear` |
| **Rook** | Straight lines | `(sameRow \|\| sameCol) && pathClear` |
| **Bishop** | Diagonals | `rowDiff == colDiff && pathClear` |
| **Knight** | L-shape, jumps | `(2,1) or (1,2)` - no path check! |
| **Pawn** | Forward, 2-step first, diagonal capture | Complex - see below |

### Pawn Logic
```java
int direction = (WHITE) ? 1 : -1;

// Forward (no capture): colDiff == 0 && end.isEmpty()
if (rowDiff == direction) return true;              // 1 step
if (!hasMoved && rowDiff == 2*direction) return middle.isEmpty(); // 2 steps

// Diagonal capture: colDiff == 1 && rowDiff == direction && !end.isEmpty()
return end.getPiece().getColor() != this.color;
```

---

## 🔍 Check/Checkmate/Stalemate Logic

### isInCheck(Color)
```java
Cell kingCell = board.findKing(color);
return board.isSquareUnderAttack(kingCell, opponent);
```

### isSquareUnderAttack(row, col, attackerColor)
```java
for each piece of attackerColor:
    if (piece.canMove(board, pieceCell, targetCell))
        return true;
return false;
```

### isCheckmate(Color)
```
IN CHECK + NO LEGAL MOVES = CHECKMATE
```

### isStalemate(Color)
```
NOT IN CHECK + NO LEGAL MOVES = STALEMATE
```

### hasAnyLegalMove(Color) ⭐ Key Algorithm
```java
for each piece of this color:
    for each possible destination:
        if piece.canMove(board, start, end):
            // Simulate move
            move.execute(board)
            stillInCheck = isInCheck(color)
            move.undo(board)
            
            if (!stillInCheck):
                return true  // Found escape!
return false  // No legal moves
```

---

## 🔐 Move Validation Flow

```
1. Check game not over
2. Validate coordinates (bounds)
3. Check piece exists at start
4. Check it's current player's piece
5. Check piece.canMove() returns true
6. Check not capturing own piece
7. ⭐ Simulate move → Check if OWN king in check → Undo if yes
8. Execute move
9. Check opponent: CHECK? CHECKMATE? STALEMATE?
10. Switch turn
```

---

## 💡 Key Interview Points

### Q: "Why Command Pattern for Move?"
> "Enables undo functionality. Move stores captured piece and wasFirstMove to fully restore state."

### Q: "How do you prevent moving into check?"
> "After each move, simulate it, check if king is attacked, undo if yes. Only accept moves that don't leave king in check."

### Q: "How is checkmate detected?"
> "King is in check AND no legal move exists. Try every piece's every possible move, simulate, check if still in check."

### Q: "Why abstract Piece class?"
> "Polymorphism. Game calls piece.canMove() without knowing if it's King, Queen, etc. Each piece implements its own movement rules."

### Q: "How does isPathClear work?"
> "Use direction vectors (+1, -1, 0). Step cell by cell from start to end. If any cell is occupied, path is blocked."

---

## ⏱️ Interview Time Allocation (45 min)

| Time | Task |
|------|------|
| 0-3 | Requirements: 2 players, turns, pieces, check/checkmate |
| 3-8 | Enums: Color, PieceType, GameStatus |
| 8-12 | Cell class |
| 12-20 | Piece abstract + 2-3 pieces (King, Queen, Pawn) |
| 20-28 | Board: initialize, findKing |
| 28-38 | Game: makeMove, isInCheck |
| 38-45 | Discussion: Checkmate, extensions |

---

## 🎓 Extensions to Mention (Don't Code)

| Feature | How |
|---------|-----|
| Castling | Check: King/Rook not moved, path clear, not through check |
| En Passant | Track last move, special pawn capture |
| Pawn Promotion | On reaching last rank, replace with Queen/etc |
| Move Timer | Add clock per player, Strategy pattern for time control |
| AI Opponent | Minimax with alpha-beta pruning |

---

## 📝 Code You Should Know By Heart

### 1. Piece.canMove signature
```java
public abstract boolean canMove(Board board, Cell start, Cell end);
```

### 2. Path clear helper
```java
int rowDir = Integer.signum(end.getRow() - start.getRow());
int colDir = Integer.signum(end.getCol() - start.getCol());
// Step until reach end, check each cell empty
```

### 3. Checkmate check
```java
return isInCheck(color) && !hasAnyLegalMove(color);
```

### 4. Color opposite
```java
public Color opposite() {
    return this == WHITE ? BLACK : WHITE;
}
```

---

## ✅ Checklist Before Interview

- [ ] Know all piece movements (especially Pawn!)
- [ ] Understand Command pattern for Move
- [ ] Know Check → Checkmate → Stalemate flow
- [ ] Can explain "simulate move, check, undo" pattern
- [ ] Know why abstract Piece class

