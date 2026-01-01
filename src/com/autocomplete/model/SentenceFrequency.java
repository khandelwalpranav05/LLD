package com.autocomplete.model;

/**
 * SentenceFrequency - Helper class to hold sentence with its frequency
 * 
 * Used for:
 * 1. Collecting sentences from Trie subtree during DFS
 * 2. Sorting by frequency (descending) then ASCII (ascending)
 * 3. Used in PriorityQueue (Min-Heap) for Top-K optimization
 * 
 * Implements Comparable for natural ordering in sorting/heap operations.
 */
public class SentenceFrequency implements Comparable<SentenceFrequency> {
    
    private final String sentence;
    private final int frequency;
    
    public SentenceFrequency(String sentence, int frequency) {
        this.sentence = sentence;
        this.frequency = frequency;
    }
    
    public String getSentence() {
        return sentence;
    }
    
    public int getFrequency() {
        return frequency;
    }
    
    /**
     * Compare for DESCENDING frequency, then ASCENDING ASCII order
     * 
     * This is for max-heap / descending sort behavior:
     * - Higher frequency comes first (negative if this.freq > other.freq)
     * - For same frequency, lower ASCII comes first
     * 
     * EXAMPLE:
     * Sentences: [("hello", 4), ("hi", 4), ("hey", 3)]
     * Sorted: [("hello", 4), ("hi", 4), ("hey", 3)]
     *         ↑ "hello" before "hi" because 'e' < 'i' in ASCII
     */
    @Override
    public int compareTo(SentenceFrequency other) {
        // First: compare by frequency (DESCENDING - higher first)
        if (this.frequency != other.frequency) {
            return other.frequency - this.frequency; // Descending
        }
        // Second: compare by ASCII (ASCENDING - lower first)
        return this.sentence.compareTo(other.sentence); // Ascending
    }
    
    @Override
    public String toString() {
        return String.format("('%s', freq=%d)", sentence, frequency);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SentenceFrequency other = (SentenceFrequency) obj;
        return frequency == other.frequency && sentence.equals(other.sentence);
    }
    
    @Override
    public int hashCode() {
        return 31 * sentence.hashCode() + frequency;
    }
}
