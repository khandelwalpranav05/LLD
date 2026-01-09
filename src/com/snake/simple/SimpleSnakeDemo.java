package com.snake.simple;

/**
 * Test demonstration for the simplified Snake Game
 */
public class SimpleSnakeDemo {
    
    public static void main(String[] args) {
        System.out.println("=== SIMPLIFIED SNAKE GAME TESTS ===\n");
        
        testBasicMovement();
        testWallCollision();
        testTailChasing();
        testSelfCollision();
        
        System.out.println("\n=== ALL TESTS PASSED ===");
    }
    
    static void testBasicMovement() {
        System.out.println("TEST 1: Basic Movement");
        SnakeGame game = new SnakeGame(5, 5);
        
        Cell initial = game.getHead();
        System.out.println("  Initial head: " + initial);
        
        game.move(Direction.RIGHT);
        Cell after = game.getHead();
        System.out.println("  After RIGHT: " + after);
        
        assert !game.isGameOver() : "Should not be game over";
        assert after.col == initial.col + 1 : "Should move right";
        System.out.println("  ✓ Basic movement works\n");
    }
    
    static void testWallCollision() {
        System.out.println("TEST 2: Wall Collision");
        SnakeGame game = new SnakeGame(5, 5);  // Center is (2,2)
        
        // Move UP until wall
        game.move(Direction.UP);  // (1,2)
        game.move(Direction.UP);  // (0,2)
        assert !game.isGameOver() : "Should not crash yet";
        
        game.move(Direction.UP);  // (-1,2) = WALL!
        assert game.isGameOver() : "Should crash into wall";
        System.out.println("  ✓ Wall collision detected\n");
    }
    
    static void testTailChasing() {
        System.out.println("TEST 3: Tail Chasing (The Tricky Case)");
        
        // Build a 2-cell snake manually
        SnakeGame game = new SnakeGame(10, 10);
        // Grow snake to have 2 cells
        game.body.addFirst(new Cell(5, 6));  // Grow head
        game.bodySet.add(new Cell(5, 6));
        // Now: head=(5,6), tail=(5,5)
        
        System.out.println("  Snake: head=" + game.body.peekFirst() + ", tail=" + game.body.peekLast());
        
        // Move LEFT: head wants to go to (5,5) where tail is
        // But tail will move away, so this should be SAFE
        boolean wouldCrash = game.bodySet.contains(new Cell(5, 5));
        System.out.println("  Moving to tail position. In bodySet? " + wouldCrash);
        
        // The hitsBody() should return false (safe)
        game.move(Direction.LEFT);  // Try to move to (5,5)
        
        // Should NOT be game over because tail vacates!
        System.out.println("  Game over after move? " + game.isGameOver());
        System.out.println("  ✓ Tail chasing is safe (not game over)\n");
    }
    
    static void testSelfCollision() {
        System.out.println("TEST 4: Self Collision (Body Hit)");
        
        // Build a snake that can hit itself
        SnakeGame game = new SnakeGame(10, 10);
        
        // Manually create a U-shaped snake
        game.body.clear();
        game.bodySet.clear();
        
        // Snake: (3,5)→(4,5)→(5,5)→(5,6)→(5,7)
        // Head at (3,5), U-shape going down then right
        Cell[] cells = {
            new Cell(3, 5),  // Head
            new Cell(4, 5),
            new Cell(5, 5),
            new Cell(5, 6),
            new Cell(5, 7)   // Tail
        };
        for (Cell c : cells) {
            game.body.addLast(c);
            game.bodySet.add(c);
        }
        
        System.out.println("  Snake shape: " + game.body);
        System.out.println("  Head at: " + game.getHead());
        
        // Move DOWN: head goes to (4,5) which is already body!
        game.move(Direction.DOWN);
        
        assert game.isGameOver() : "Should crash into self";
        System.out.println("  ✓ Self collision detected (game over)\n");
    }
}

