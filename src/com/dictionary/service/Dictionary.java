package com.dictionary.service;

import com.dictionary.model.TrieNode;
import com.dictionary.model.SearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Dictionary - Main service using Trie data structure
 * 
 * Supports:
 * 1. addWord(word, meaning)     - Add/Update word
 * 2. getMeaning(word)           - Exact match lookup
 * 3. searchByPrefix(prefix)     - Autocomplete/prefix search
 * 4. searchByPattern(pattern)   - Wildcard search (. matches any char)
 * 5. deleteWord(word)           - Remove word from dictionary
 * 6. startsWith(prefix)         - Check if any word starts with prefix
 * 
 * Time Complexity:
 * - Insert: O(m) where m = word length
 * - Search: O(m) for exact match
 * - Prefix Search: O(p + n) where p = prefix length, n = matching words
 */
public class Dictionary {
    private TrieNode root;
    private int wordCount;
    
    public Dictionary() {
        this.root = new TrieNode();
        this.wordCount = 0;
    }
    
    // ==================== CORE OPERATIONS ====================
    
    /**
     * Add a word with its meaning to the dictionary
     * If word exists, updates the meaning
     */
    public void addWord(String word, String meaning) {
        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("Word cannot be null or empty");
        }
        
        word = word.toLowerCase();
        TrieNode current = root;
        
        for (char c : word.toCharArray()) {
            if (!current.hasChild(c)) {
                current.addChild(c, new TrieNode());
            }
            current = current.getChild(c);
        }
        
        // Check if it's a new word
        if (!current.isEndOfWord()) {
            wordCount++;
        }
        
        current.setMeaning(meaning);
    }
    
    /**
     * Get meaning of a word (exact match)
     * Returns null if word not found
     */
    public String getMeaning(String word) {
        if (word == null || word.isEmpty()) {
            return null;
        }
        
        TrieNode node = findNode(word.toLowerCase());
        
        if (node != null && node.isEndOfWord()) {
            return node.getMeaning();
        }
        return null;
    }
    
    /**
     * Check if word exists in dictionary
     */
    public boolean containsWord(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        
        TrieNode node = findNode(word.toLowerCase());
        return node != null && node.isEndOfWord();
    }
    
    /**
     * Check if any word starts with the given prefix
     */
    public boolean startsWith(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return false;
        }
        
        return findNode(prefix.toLowerCase()) != null;
    }
    
    // ==================== SEARCH OPERATIONS ====================
    
    /**
     * Get all words starting with the given prefix (Autocomplete)
     */
    public List<SearchResult> searchByPrefix(String prefix) {
        List<SearchResult> results = new ArrayList<>();
        
        if (prefix == null || prefix.isEmpty()) {
            return results;
        }
        
        prefix = prefix.toLowerCase();
        TrieNode prefixNode = findNode(prefix);
        
        if (prefixNode != null) {
            // Collect all words starting from this node
            collectWords(prefixNode, new StringBuilder(prefix), results);
        }
        
        return results;
    }
    
    /**
     * Search words matching a pattern (. matches any single character)
     * Example: "c.t" matches "cat", "cot", "cut"
     */
    public List<SearchResult> searchByPattern(String pattern) {
        List<SearchResult> results = new ArrayList<>();
        
        if (pattern == null || pattern.isEmpty()) {
            return results;
        }
        
        searchPatternHelper(root, pattern.toLowerCase(), 0, new StringBuilder(), results);
        return results;
    }
    
    /**
     * Delete a word from the dictionary
     * Returns true if word was deleted, false if not found
     */
    public boolean deleteWord(String word) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        
        word = word.toLowerCase();
        
        // First check if word exists
        if (!containsWord(word)) {
            return false;
        }
        
        // Word exists, delete it
        deleteHelper(root, word, 0);
        return true;
    }
    
    /**
     * Get all words in the dictionary
     */
    public List<SearchResult> getAllWords() {
        List<SearchResult> results = new ArrayList<>();
        collectWords(root, new StringBuilder(), results);
        return results;
    }
    
    /**
     * Get total number of words in dictionary
     */
    public int getWordCount() {
        return wordCount;
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Find the node at the end of the given string path
     */
    private TrieNode findNode(String str) {
        TrieNode current = root;
        
        for (char c : str.toCharArray()) {
            if (!current.hasChild(c)) {
                return null;
            }
            current = current.getChild(c);
        }
        
        return current;
    }
    
    /**
     * Recursively collect all words from a node
     */
    private void collectWords(TrieNode node, StringBuilder prefix, List<SearchResult> results) {
        if (node.isEndOfWord()) {
            results.add(new SearchResult(prefix.toString(), node.getMeaning()));
        }
        
        for (char c : node.getChildren().keySet()) {
            prefix.append(c);
            collectWords(node.getChild(c), prefix, results);
            prefix.deleteCharAt(prefix.length() - 1);  // Backtrack
        }
    }
    
    /**
     * Recursively search for pattern matches
     * '.' matches any single character
     */
    private void searchPatternHelper(TrieNode node, String pattern, int index, 
                                     StringBuilder current, List<SearchResult> results) {
        // Base case: reached end of pattern
        if (index == pattern.length()) {
            if (node.isEndOfWord()) {
                results.add(new SearchResult(current.toString(), node.getMeaning()));
            }
            return;
        }
        
        char c = pattern.charAt(index);
        
        if (c == '.') {
            // Wildcard: try all children
            for (char child : node.getChildren().keySet()) {
                current.append(child);
                searchPatternHelper(node.getChild(child), pattern, index + 1, current, results);
                current.deleteCharAt(current.length() - 1);  // Backtrack
            }
        } else {
            // Exact character match
            if (node.hasChild(c)) {
                current.append(c);
                searchPatternHelper(node.getChild(c), pattern, index + 1, current, results);
                current.deleteCharAt(current.length() - 1);  // Backtrack
            }
        }
    }
    
    /**
     * Recursively delete a word
     * Returns true if the current node should be deleted
     */
    private boolean deleteHelper(TrieNode node, String word, int index) {
        if (index == word.length()) {
            if (!node.isEndOfWord()) {
                return false;  // Word doesn't exist
            }
            
            node.setEndOfWord(false);
            node.setMeaning(null);
            wordCount--;
            
            // Return true if node has no children (can be deleted)
            return node.getChildren().isEmpty();
        }
        
        char c = word.charAt(index);
        TrieNode child = node.getChild(c);
        
        if (child == null) {
            return false;  // Word doesn't exist
        }
        
        boolean shouldDeleteChild = deleteHelper(child, word, index + 1);
        
        if (shouldDeleteChild) {
            node.getChildren().remove(c);
            // Return true if node has no children and is not end of another word
            return node.getChildren().isEmpty() && !node.isEndOfWord();
        }
        
        return false;
    }
}

