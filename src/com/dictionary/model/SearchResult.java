package com.dictionary.model;

/**
 * SearchResult - Represents a word-meaning pair
 */
public class SearchResult {
    private String word;
    private String meaning;
    
    public SearchResult(String word, String meaning) {
        this.word = word;
        this.meaning = meaning;
    }
    
    public String getWord() {
        return word;
    }
    
    public String getMeaning() {
        return meaning;
    }
    
    @Override
    public String toString() {
        return word + ": " + meaning;
    }
}

