package com.autocomplete.model;

import java.util.HashMap;
import java.util.Map;

/**
 * TrieNode - A single node in the Trie (Prefix Tree)
 * 
 * Each node represents a character in the path from root to a complete sentence.
 * 
 * STRUCTURE:
 * - children: Map of character -> child TrieNode (supports a-z and space)
 * - isEndOfSentence: true if this node marks the end of a complete sentence
 * - frequency: how many times this sentence has been typed (only valid if isEndOfSentence)
 * - sentence: the complete sentence string (stored only at end nodes for easy retrieval)
 * 
 * EXAMPLE:
 * For sentences ["hi", "hello"]:
 * 
 *        (root)
 *           |
 *          'h'
 *           |
 *    +------+------+
 *   'e'           'i' ← isEndOfSentence=true, sentence="hi", frequency=X
 *    |
 *   'l'
 *    |
 *   'l'
 *    |
 *   'o' ← isEndOfSentence=true, sentence="hello", frequency=Y
 */
public class TrieNode {
    
    // Map from character to child node
    // Using HashMap for O(1) lookup
    // Could use array[27] (a-z + space) for fixed size, but HashMap is cleaner
    private Map<Character, TrieNode> children;
    
    // Marks if a complete sentence ends at this node
    private boolean isEndOfSentence;
    
    // Number of times this sentence was typed (popularity)
    // Only meaningful when isEndOfSentence = true
    private int frequency;
    
    // The complete sentence string
    // Stored at end node to avoid reconstructing from path
    private String sentence;
    
    public TrieNode() {
        this.children = new HashMap<>();
        this.isEndOfSentence = false;
        this.frequency = 0;
        this.sentence = null;
    }
    
    // =========== Child Node Operations ===========
    
    /**
     * Check if this node has a child for the given character
     * Time: O(1)
     */
    public boolean hasChild(char ch) {
        return children.containsKey(ch);
    }
    
    /**
     * Get child node for the given character
     * Time: O(1)
     * @return child TrieNode or null if doesn't exist
     */
    public TrieNode getChild(char ch) {
        return children.get(ch);
    }
    
    /**
     * Add a new child node for the given character
     * Time: O(1)
     */
    public void addChild(char ch, TrieNode node) {
        children.put(ch, node);
    }
    
    /**
     * Get or create child node for the given character
     * This is useful during insertion
     * Time: O(1)
     */
    public TrieNode getOrCreateChild(char ch) {
        if (!children.containsKey(ch)) {
            children.put(ch, new TrieNode());
        }
        return children.get(ch);
    }
    
    /**
     * Get all children (for DFS traversal during suggestion collection)
     */
    public Map<Character, TrieNode> getChildren() {
        return children;
    }
    
    // =========== Sentence End Markers ===========
    
    public boolean isEndOfSentence() {
        return isEndOfSentence;
    }
    
    /**
     * Mark this node as end of a sentence with given frequency
     */
    public void markAsEndOfSentence(String sentence, int frequency) {
        this.isEndOfSentence = true;
        this.sentence = sentence;
        this.frequency = frequency;
    }
    
    /**
     * Increment frequency (when same sentence is typed again)
     */
    public void incrementFrequency() {
        this.frequency++;
    }
    
    // =========== Getters ===========
    
    public int getFrequency() {
        return frequency;
    }
    
    public String getSentence() {
        return sentence;
    }
    
    @Override
    public String toString() {
        return String.format("TrieNode[children=%d, isEnd=%b, freq=%d, sentence='%s']",
                children.size(), isEndOfSentence, frequency, sentence);
    }
}
