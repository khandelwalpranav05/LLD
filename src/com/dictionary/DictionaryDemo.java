package com.dictionary;

import com.dictionary.service.Dictionary;
import com.dictionary.model.SearchResult;

import java.util.List;

/**
 * DictionaryDemo - Demonstrates all Dictionary features
 */
public class DictionaryDemo {
    
    public static void main(String[] args) {
        System.out.println("=== DICTIONARY APP DEMO ===\n");
        
        Dictionary dictionary = new Dictionary();
        
        // 1. Add words
        System.out.println("--- Adding Words ---");
        dictionary.addWord("apple", "A fruit that is red or green");
        dictionary.addWord("application", "A software program");
        dictionary.addWord("apply", "To put to use");
        dictionary.addWord("app", "Short for application");
        dictionary.addWord("banana", "A yellow tropical fruit");
        dictionary.addWord("bat", "A flying mammal or sports equipment");
        dictionary.addWord("ball", "A round object used in games");
        dictionary.addWord("cat", "A small domesticated feline");
        dictionary.addWord("car", "A motor vehicle");
        dictionary.addWord("cart", "A wheeled vehicle pulled by hand");
        dictionary.addWord("cut", "To divide with a sharp instrument");
        dictionary.addWord("cot", "A small portable bed");
        System.out.println("Added " + dictionary.getWordCount() + " words\n");
        
        // 2. Exact Match Search
        System.out.println("--- Exact Match Search ---");
        testExactMatch(dictionary, "apple");
        testExactMatch(dictionary, "app");
        testExactMatch(dictionary, "xyz");  // Not found
        System.out.println();
        
        // 3. Prefix Search (Autocomplete)
        System.out.println("--- Prefix Search (Autocomplete) ---");
        testPrefixSearch(dictionary, "app");
        testPrefixSearch(dictionary, "ca");
        testPrefixSearch(dictionary, "b");
        testPrefixSearch(dictionary, "xyz");  // No matches
        System.out.println();
        
        // 4. Pattern Search (Wildcard)
        System.out.println("--- Pattern Search (. = any char) ---");
        testPatternSearch(dictionary, "c.t");   // cat, cut, cot
        testPatternSearch(dictionary, "ba.");   // bat, ball? No, ball has 4 chars
        testPatternSearch(dictionary, "ba..");  // ball
        testPatternSearch(dictionary, "....");  // All 4-letter words
        System.out.println();
        
        // 5. Check if word exists
        System.out.println("--- Contains Word ---");
        System.out.println("Contains 'apple': " + dictionary.containsWord("apple"));
        System.out.println("Contains 'appl': " + dictionary.containsWord("appl"));
        System.out.println("Contains 'xyz': " + dictionary.containsWord("xyz"));
        System.out.println();
        
        // 6. Check if prefix exists
        System.out.println("--- Starts With ---");
        System.out.println("Starts with 'app': " + dictionary.startsWith("app"));
        System.out.println("Starts with 'xyz': " + dictionary.startsWith("xyz"));
        System.out.println();
        
        // 7. Update word meaning
        System.out.println("--- Update Word ---");
        System.out.println("Before: " + dictionary.getMeaning("apple"));
        dictionary.addWord("apple", "A round fruit, often red, green, or yellow");
        System.out.println("After:  " + dictionary.getMeaning("apple"));
        System.out.println();
        
        // 8. Delete word
        System.out.println("--- Delete Word ---");
        System.out.println("Word count before delete: " + dictionary.getWordCount());
        System.out.println("Delete 'banana': " + dictionary.deleteWord("banana"));
        System.out.println("Delete 'banana' again: " + dictionary.deleteWord("banana"));
        System.out.println("Word count after delete: " + dictionary.getWordCount());
        System.out.println("Contains 'banana': " + dictionary.containsWord("banana"));
        System.out.println();
        
        // 9. Get all words
        System.out.println("--- All Words ---");
        List<SearchResult> allWords = dictionary.getAllWords();
        for (SearchResult result : allWords) {
            System.out.println("  " + result);
        }
        
        System.out.println("\n=== DEMO COMPLETE ===");
    }
    
    private static void testExactMatch(Dictionary dict, String word) {
        String meaning = dict.getMeaning(word);
        if (meaning != null) {
            System.out.println("'" + word + "' -> " + meaning);
        } else {
            System.out.println("'" + word + "' -> NOT FOUND");
        }
    }
    
    private static void testPrefixSearch(Dictionary dict, String prefix) {
        List<SearchResult> results = dict.searchByPrefix(prefix);
        System.out.println("Prefix '" + prefix + "' (" + results.size() + " matches):");
        for (SearchResult r : results) {
            System.out.println("  " + r);
        }
    }
    
    private static void testPatternSearch(Dictionary dict, String pattern) {
        List<SearchResult> results = dict.searchByPattern(pattern);
        System.out.println("Pattern '" + pattern + "' (" + results.size() + " matches):");
        for (SearchResult r : results) {
            System.out.println("  " + r);
        }
    }
}

