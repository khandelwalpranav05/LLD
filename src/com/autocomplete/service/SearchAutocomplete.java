package com.autocomplete.service;

import com.autocomplete.model.SentenceFrequency;
import com.autocomplete.model.TrieNode;

import java.util.*;

/**
 * SearchAutocomplete - Main autocomplete service using Trie data structure
 * 
 * ===== HOW IT WORKS =====
 * 
 * 1. TRIE STRUCTURE:
 *    - Each path from root to an end-node represents a complete sentence
 *    - Characters are stored in edges (child mappings)
 *    - Frequency is stored at end nodes
 * 
 * 2. OPTIMIZATION - currentNode pointer:
 *    - Instead of traversing from root for every character, we maintain
 *      a pointer to our current position in the Trie
 *    - This gives O(1) per character instead of O(prefix_length)
 * 
 * 3. SUGGESTION COLLECTION:
 *    - When user types a character, we move currentNode to the child
 *    - Then collect all sentences in the subtree using DFS
 *    - Sort by frequency (desc) then ASCII (asc)
 *    - Return top 3
 * 
 * ===== TIME COMPLEXITY =====
 * 
 * | Operation              | Time Complexity                    |
 * |------------------------|------------------------------------|
 * | Constructor            | O(n × L) - n phrases, L avg length |
 * | getSuggestions (char)  | O(S) - S = sentences in subtree    |
 * | getSuggestions ('#')   | O(L) - L = current input length    |
 * 
 * ===== SPACE COMPLEXITY =====
 * 
 * | Component              | Space                              |
 * |------------------------|------------------------------------|
 * | Trie nodes             | O(total unique prefixes)           |
 * | Current input buffer   | O(max sentence length)             |
 * | DFS collection         | O(S) - temporary list              |
 */
public class SearchAutocomplete {
    
    private static final int TOP_K = 3; // Return top 3 suggestions
    
    // Root of the Trie - all insertions and lookups start here
    private final TrieNode root;
    
    // ===== OPTIMIZATION: Track current position =====
    // Instead of traversing from root every time, we maintain:
    // 1. currentInput: StringBuilder with chars typed so far
    // 2. currentNode: pointer to our current position in Trie
    // This gives O(1) per character instead of O(prefix_length)
    private StringBuilder currentInput;
    private TrieNode currentNode;
    
    /**
     * Constructor - Initialize with historical data
     * 
     * @param phrases Array of historical sentences
     * @param counts  Array of frequencies for each sentence
     * 
     * TIME: O(n × L) where n = number of phrases, L = average length
     * 
     * EXAMPLE:
     * phrases = ["hello world", "hi there", "hello", "hi world"]
     * counts  = [4, 3, 2, 2]
     * 
     * This builds a Trie with:
     *        (root)
     *           |
     *          'h'
     *         /   \
     *       'e'   'i'
     *        |     |
     *       'l'   ' '
     *       ...   ...
     */
    public SearchAutocomplete(String[] phrases, int[] counts) {
        this.root = new TrieNode();
        this.currentInput = new StringBuilder();
        this.currentNode = root; // Start at root
        
        // Insert all historical phrases
        for (int i = 0; i < phrases.length; i++) {
            insert(phrases[i], counts[i]);
        }
        
        System.out.println("=== Autocomplete Initialized ===");
        System.out.println("Inserted " + phrases.length + " phrases");
        System.out.println("Ready for input!\n");
    }
    
    /**
     * Insert a sentence into the Trie with given frequency
     * 
     * TIME: O(L) where L = sentence length
     * 
     * PROCESS:
     * 1. Start at root
     * 2. For each character in sentence:
     *    - Get or create child node for that character
     *    - Move to child
     * 3. Mark final node as end of sentence with frequency
     */
    private void insert(String sentence, int frequency) {
        TrieNode node = root;
        
        // Traverse/create path for each character
        for (char ch : sentence.toCharArray()) {
            node = node.getOrCreateChild(ch);
        }
        
        // Mark end of sentence
        if (node.isEndOfSentence()) {
            // Sentence already exists, add to frequency
            // This handles duplicates in input
            node.markAsEndOfSentence(sentence, node.getFrequency() + frequency);
        } else {
            node.markAsEndOfSentence(sentence, frequency);
        }
    }
    
    /**
     * Process the next character typed by the user
     * 
     * @param ch The character typed (a-z, space, or #)
     * @return Top 3 matching sentences, or empty list
     * 
     * TIME:
     * - For regular char: O(S) where S = sentences matching prefix
     * - For '#': O(L) where L = current input length
     * 
     * BEHAVIOR:
     * - a-z or space: Add to current prefix, return suggestions
     * - #: Save current input as new sentence, reset, return empty
     */
    public List<String> getSuggestions(char ch) {
        System.out.println("----------------------------------------");
        System.out.println("INPUT: '" + ch + "'");
        
        // ===== CASE 1: End of sentence (#) =====
        if (ch == '#') {
            return handleEndOfSentence();
        }
        
        // ===== CASE 2: Regular character (a-z or space) =====
        return handleRegularCharacter(ch);
    }
    
    /**
     * Handle '#' - End of current sentence
     * 
     * 1. Save current input as new sentence (or increment if exists)
     * 2. Reset for next input
     * 3. Return empty list
     */
    private List<String> handleEndOfSentence() {
        String sentence = currentInput.toString();
        
        if (!sentence.isEmpty()) {
            System.out.println("Saving sentence: '" + sentence + "'");
            
            // Insert or increment frequency
            if (currentNode != null && currentNode.isEndOfSentence()) {
                // Sentence already exists, increment
                currentNode.incrementFrequency();
                System.out.println("Incremented frequency to: " + currentNode.getFrequency());
            } else {
                // New sentence
                insert(sentence, 1);
                System.out.println("Added as new sentence with frequency 1");
            }
        }
        
        // Reset for next input
        currentInput = new StringBuilder();
        currentNode = root;
        
        System.out.println("STATE: Reset. Ready for new input.");
        System.out.println("RESULT: [] (empty - end of sentence)");
        
        return Collections.emptyList();
    }
    
    /**
     * Handle regular character (a-z or space)
     * 
     * 1. Append to current input
     * 2. Move currentNode to child (or null if no match)
     * 3. Collect all sentences in subtree
     * 4. Sort and return top 3
     */
    private List<String> handleRegularCharacter(char ch) {
        // Append to current input
        currentInput.append(ch);
        String prefix = currentInput.toString();
        System.out.println("Current prefix: '" + prefix + "'");
        
        // ===== OPTIMIZATION: Use currentNode pointer =====
        // Instead of: traverseFromRoot(prefix)
        // We do: currentNode = currentNode.getChild(ch)
        // This is O(1) instead of O(prefix_length)!
        
        if (currentNode == null) {
            // Already in a dead-end (previous char had no match)
            System.out.println("STATE: Still in dead-end (no matches)");
            System.out.println("RESULT: []");
            return Collections.emptyList();
        }
        
        // Move to child node
        currentNode = currentNode.getChild(ch);
        
        if (currentNode == null) {
            // No match for this prefix
            System.out.println("STATE: No sentences start with '" + prefix + "'");
            System.out.println("RESULT: []");
            return Collections.emptyList();
        }
        
        // Collect all sentences in subtree
        List<SentenceFrequency> allMatches = new ArrayList<>();
        collectSentences(currentNode, allMatches);
        
        System.out.println("Found " + allMatches.size() + " matching sentences");
        
        // Sort: frequency DESC, then ASCII ASC
        Collections.sort(allMatches);
        
        // Take top K
        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(TOP_K, allMatches.size()); i++) {
            result.add(allMatches.get(i).getSentence());
        }
        
        System.out.println("RESULT: " + result);
        return result;
    }
    
    /**
     * Collect all sentences in the subtree of given node using DFS
     * 
     * TIME: O(S × L) where S = sentences in subtree, L = avg length
     * 
     * This is the "collection" phase where we gather all matching sentences.
     * We then sort them outside this method.
     */
    private void collectSentences(TrieNode node, List<SentenceFrequency> result) {
        if (node == null) return;
        
        // If this node marks end of a sentence, add it
        if (node.isEndOfSentence()) {
            result.add(new SentenceFrequency(node.getSentence(), node.getFrequency()));
        }
        
        // Recursively collect from all children
        for (TrieNode child : node.getChildren().values()) {
            collectSentences(child, result);
        }
    }
    
    /**
     * Get current input state (for debugging/testing)
     */
    public String getCurrentInput() {
        return currentInput.toString();
    }
    
    /**
     * Check if currently at a valid node (for debugging/testing)
     */
    public boolean isAtValidNode() {
        return currentNode != null;
    }
}
