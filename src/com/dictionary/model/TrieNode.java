package com.dictionary.model;

import java.util.HashMap;
import java.util.Map;

/**
 * TrieNode - Each node in the Trie
 * 
 * Key Design Decisions:
 * 1. HashMap for children: O(1) lookup, supports any character
 * 2. meaning field: Stores meaning if this node is end of a word
 * 3. isEndOfWord: Marks complete words (even if meaning is null)
 */
public class TrieNode {
    private Map<Character, TrieNode> children;
    private String meaning;
    private boolean isEndOfWord;
    
    public TrieNode() {
        this.children = new HashMap<>();
        this.meaning = null;
        this.isEndOfWord = false;
    }
    
    public Map<Character, TrieNode> getChildren() {
        return children;
    }
    
    public TrieNode getChild(char c) {
        return children.get(c);
    }
    
    public void addChild(char c, TrieNode node) {
        children.put(c, node);
    }
    
    public boolean hasChild(char c) {
        return children.containsKey(c);
    }
    
    public String getMeaning() {
        return meaning;
    }
    
    public void setMeaning(String meaning) {
        this.meaning = meaning;
        this.isEndOfWord = true;
    }
    
    public boolean isEndOfWord() {
        return isEndOfWord;
    }
    
    public void setEndOfWord(boolean endOfWord) {
        this.isEndOfWord = endOfWord;
    }
}

