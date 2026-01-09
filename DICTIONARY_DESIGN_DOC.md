# Dictionary App - LLD Design Document

## Table of Contents
1. [Problem Statement](#problem-statement)
2. [Requirements](#requirements)
3. [Why Trie?](#why-trie)
4. [Architecture](#architecture)
5. [Class Design](#class-design)
6. [Core Operations](#core-operations)
7. [Search Types Explained](#search-types-explained)
8. [Time & Space Complexity](#time--space-complexity)
9. [Edge Cases](#edge-cases)
10. [Interview Tips](#interview-tips)

---

## Problem Statement

Design an in-memory dictionary that:
- Stores words and their meanings
- Supports multiple search types (exact, prefix, pattern)
- Uses **Trie** data structure for efficient lookups

---

## Requirements

### Functional Requirements

| Feature | Description | Priority |
|---------|-------------|----------|
| `addWord(word, meaning)` | Add/update word with meaning | Must |
| `getMeaning(word)` | Exact match lookup | Must |
| `searchByPrefix(prefix)` | Autocomplete feature | Must |
| `searchByPattern(pattern)` | Wildcard search (`.` = any char) | Should |
| `deleteWord(word)` | Remove word | Should |
| `containsWord(word)` | Check existence | Should |
| `startsWith(prefix)` | Check if any word starts with prefix | Should |

### Non-Functional

| Requirement | Solution |
|-------------|----------|
| Fast prefix search | Trie structure |
| Memory efficient | Shared prefixes |
| Case insensitive | Lowercase conversion |

---

## Why Trie?

### Comparison with Alternatives

| Data Structure | Exact Search | Prefix Search | Space |
|----------------|--------------|---------------|-------|
| HashMap | O(1) | O(n) ❌ | O(n*m) |
| TreeMap | O(log n) | O(log n + k) | O(n*m) |
| **Trie** | **O(m)** | **O(p + k)** ✅ | **O(shared)** |

Where: n = number of words, m = word length, p = prefix length, k = matching words

### Trie Advantages

1. **Prefix Search**: Native support - just traverse to prefix node
2. **Shared Prefixes**: "app", "apple", "apply" share "app" nodes
3. **Predictable**: O(m) regardless of dictionary size
4. **Pattern Matching**: Easy to implement with backtracking

### Visual: Trie Structure

```
                    ROOT
                   / | \
                  a  b  c
                 /   |   \
                p    a    a
               /|\   |\   |\
              p l l  t l  r t
             /  |     |    |
            l   y     l    t
           /
          i  e
         /
        c
        a
        t
        i
        o
        n

Words: app, apple, application, apply, bat, ball, car, cart, cat
```

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    DICTIONARY LLD                        │
├─────────────────────────────────────────────────────────┤
│                                                          │
│   ┌───────────────────────────────────────────────────┐ │
│   │                  Dictionary                        │ │
│   │                 (Main Service)                     │ │
│   │                                                    │ │
│   │  + addWord(word, meaning)                         │ │
│   │  + getMeaning(word) → String                      │ │
│   │  + searchByPrefix(prefix) → List<SearchResult>    │ │
│   │  + searchByPattern(pattern) → List<SearchResult>  │ │
│   │  + deleteWord(word) → boolean                     │ │
│   │  + containsWord(word) → boolean                   │ │
│   │  + startsWith(prefix) → boolean                   │ │
│   └───────────────────────┬───────────────────────────┘ │
│                           │                              │
│                           │ uses                         │
│                           ▼                              │
│   ┌───────────────────────────────────────────────────┐ │
│   │                   TrieNode                         │ │
│   │                                                    │ │
│   │  - children: Map<Character, TrieNode>             │ │
│   │  - meaning: String                                │ │
│   │  - isEndOfWord: boolean                           │ │
│   └───────────────────────────────────────────────────┘ │
│                                                          │
│   ┌───────────────────────────────────────────────────┐ │
│   │                 SearchResult                       │ │
│   │                                                    │ │
│   │  - word: String                                   │ │
│   │  - meaning: String                                │ │
│   └───────────────────────────────────────────────────┘ │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

---

## Class Design

### 1. TrieNode

```java
public class TrieNode {
    private Map<Character, TrieNode> children;  // HashMap for O(1) lookup
    private String meaning;                      // null if not end of word
    private boolean isEndOfWord;                 // Marks complete words
    
    public TrieNode() {
        this.children = new HashMap<>();
        this.meaning = null;
        this.isEndOfWord = false;
    }
    
    // Getters and setters...
}
```

**Design Decisions**:

| Choice | Why |
|--------|-----|
| `HashMap` for children | O(1) lookup, supports any character |
| `meaning` stored in node | Avoids separate storage |
| `isEndOfWord` flag | Distinguishes "app" (word) from "app" in "apple" (prefix only) |

### 2. Dictionary (Main Service)

```java
public class Dictionary {
    private TrieNode root;
    private int wordCount;
    
    public Dictionary() {
        this.root = new TrieNode();
        this.wordCount = 0;
    }
    
    // Core operations...
}
```

### 3. SearchResult (DTO)

```java
public class SearchResult {
    private String word;
    private String meaning;
    
    // Constructor, getters, toString...
}
```

---

## Core Operations

### 1. addWord(word, meaning) - O(m)

```java
public void addWord(String word, String meaning) {
    word = word.toLowerCase();
    TrieNode current = root;
    
    for (char c : word.toCharArray()) {
        if (!current.hasChild(c)) {
            current.addChild(c, new TrieNode());
        }
        current = current.getChild(c);
    }
    
    if (!current.isEndOfWord()) {
        wordCount++;
    }
    current.setMeaning(meaning);  // Also sets isEndOfWord = true
}
```

**Visual**:
```
Adding "cat" with meaning "A feline":

     root                root
      |          →        |
     (empty)              c
                          |
                          a
                          |
                          t ← isEndOfWord=true, meaning="A feline"
```

### 2. getMeaning(word) - O(m)

```java
public String getMeaning(String word) {
    TrieNode node = findNode(word.toLowerCase());
    
    if (node != null && node.isEndOfWord()) {
        return node.getMeaning();
    }
    return null;
}

private TrieNode findNode(String str) {
    TrieNode current = root;
    
    for (char c : str.toCharArray()) {
        if (!current.hasChild(c)) {
            return null;  // Path doesn't exist
        }
        current = current.getChild(c);
    }
    return current;
}
```

### 3. searchByPrefix(prefix) - O(p + k)

```java
public List<SearchResult> searchByPrefix(String prefix) {
    List<SearchResult> results = new ArrayList<>();
    
    TrieNode prefixNode = findNode(prefix.toLowerCase());
    
    if (prefixNode != null) {
        collectWords(prefixNode, new StringBuilder(prefix), results);
    }
    return results;
}

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
```

**Visual**:
```
searchByPrefix("app"):

1. Find node at "app"
2. Collect all words from that subtree

        root
         |
         a
         |
         p
         |
         p  ← Start DFS from here
        /|\
       l  y  (end: "app")
       |
       e  (end: "apply")
       |
       (end: "apple")

Results: ["app", "apple", "apply", "application"]
```

### 4. searchByPattern(pattern) - Wildcard Search

```java
public List<SearchResult> searchByPattern(String pattern) {
    List<SearchResult> results = new ArrayList<>();
    searchPatternHelper(root, pattern, 0, new StringBuilder(), results);
    return results;
}

private void searchPatternHelper(TrieNode node, String pattern, int index, 
                                 StringBuilder current, List<SearchResult> results) {
    if (index == pattern.length()) {
        if (node.isEndOfWord()) {
            results.add(new SearchResult(current.toString(), node.getMeaning()));
        }
        return;
    }
    
    char c = pattern.charAt(index);
    
    if (c == '.') {
        // Wildcard: try ALL children
        for (char child : node.getChildren().keySet()) {
            current.append(child);
            searchPatternHelper(node.getChild(child), pattern, index + 1, current, results);
            current.deleteCharAt(current.length() - 1);  // Backtrack
        }
    } else {
        // Exact match
        if (node.hasChild(c)) {
            current.append(c);
            searchPatternHelper(node.getChild(c), pattern, index + 1, current, results);
            current.deleteCharAt(current.length() - 1);  // Backtrack
        }
    }
}
```

**Visual**:
```
searchByPattern("c.t"):  (. matches any char)

       c
      /|\
     a o u
     |  |  |
     t  t  t  ← All match!
     
Results: ["cat", "cot", "cut"]
```

### 5. deleteWord(word)

```java
public boolean deleteWord(String word) {
    if (!containsWord(word)) {
        return false;
    }
    
    deleteHelper(root, word.toLowerCase(), 0);
    return true;
}

private boolean deleteHelper(TrieNode node, String word, int index) {
    if (index == word.length()) {
        node.setEndOfWord(false);
        node.setMeaning(null);
        wordCount--;
        return node.getChildren().isEmpty();  // Can delete if no children
    }
    
    char c = word.charAt(index);
    TrieNode child = node.getChild(c);
    
    boolean shouldDeleteChild = deleteHelper(child, word, index + 1);
    
    if (shouldDeleteChild) {
        node.getChildren().remove(c);
        return node.getChildren().isEmpty() && !node.isEndOfWord();
    }
    
    return false;
}
```

**Visual**:
```
Delete "apply" when we have ["app", "apple", "apply"]:

Before:          After:
    p               p
   /|\             / \
  p  l  y         p   l
     |                |
     e                e
     
"apply" removed, but "app" and "apple" preserved
```

---

## Search Types Explained

| Search Type | Method | Example | Use Case |
|-------------|--------|---------|----------|
| **Exact** | `getMeaning("cat")` | Returns "A feline" | Look up definition |
| **Prefix** | `searchByPrefix("app")` | Returns app, apple, apply | Autocomplete |
| **Pattern** | `searchByPattern("c.t")` | Returns cat, cot, cut | Crossword/games |
| **Exists** | `containsWord("cat")` | Returns true/false | Spell check |
| **Has Prefix** | `startsWith("app")` | Returns true/false | Quick validation |

---

## Time & Space Complexity

### Time Complexity

| Operation | Complexity | Explanation |
|-----------|------------|-------------|
| `addWord` | O(m) | Traverse/create m nodes |
| `getMeaning` | O(m) | Traverse m nodes |
| `searchByPrefix` | O(p + k) | p = prefix length, k = matching words total chars |
| `searchByPattern` | O(26^w) worst | w = wildcards count (branching factor) |
| `deleteWord` | O(m) | Traverse + cleanup |
| `containsWord` | O(m) | Traverse m nodes |

Where m = word length

### Space Complexity

| Metric | Complexity | Notes |
|--------|------------|-------|
| Trie storage | O(N * M) worst | N words, M avg length |
| With shared prefixes | O(Total unique chars) | Much better in practice |
| Per operation | O(m) stack | Recursion depth |

**Example**: 1000 words averaging 10 chars
- HashMap: ~10,000 character storage
- Trie: Much less due to shared prefixes (common in real words)

---

## Edge Cases

### 1. Empty/Null Input
```java
if (word == null || word.isEmpty()) {
    return null;  // or throw exception
}
```

### 2. Case Insensitivity
```java
word = word.toLowerCase();  // Normalize all input
```

### 3. Prefix is a Complete Word
```
"app" and "apple" both exist
getMeaning("app") → returns meaning (not null)
searchByPrefix("app") → includes "app" itself
```

### 4. Delete Word that is Prefix of Another
```
Words: ["app", "apple"]
deleteWord("app") → removes "app" but keeps structure for "apple"
```

### 5. Pattern with All Wildcards
```
searchByPattern("...") → returns all 3-letter words
searchByPattern("....") → returns all 4-letter words
```

---

## Interview Tips

### What to Code First

| Priority | Component | Time |
|----------|-----------|------|
| 1 | TrieNode class | 3 min |
| 2 | addWord() | 3 min |
| 3 | getMeaning() / findNode() | 3 min |
| 4 | searchByPrefix() | 5 min |
| 5 | searchByPattern() (if time) | 5 min |

### Key Points to Mention

1. **Why HashMap in TrieNode**: "O(1) child lookup, supports any character set"

2. **Why isEndOfWord flag**: "Distinguishes complete words from prefixes. 'app' is a word, but also prefix of 'apple'"

3. **Backtracking in collect**: "We reuse StringBuilder, appending and removing to avoid creating new strings"

4. **Space efficiency**: "Common prefixes are shared. Dictionary words share lots of prefixes"

### Alternative: Array Instead of HashMap

```java
// For lowercase letters only (a-z)
private TrieNode[] children = new TrieNode[26];

// Access
children[c - 'a']
```

| Approach | Pros | Cons |
|----------|------|------|
| HashMap | Flexible charset, sparse | Slightly slower |
| Array[26] | Faster, fixed | Only a-z, wastes space if sparse |

---

## Quick Reference

### Files Structure
```
com.dictionary/
├── model/
│   ├── TrieNode.java      (~55 lines)
│   └── SearchResult.java  (~25 lines)
├── service/
│   └── Dictionary.java    (~180 lines)
└── DictionaryDemo.java    (~100 lines)
```

### Key Operations Summary

```java
// Create
Dictionary dict = new Dictionary();

// Add
dict.addWord("apple", "A fruit");

// Lookup
String meaning = dict.getMeaning("apple");

// Autocomplete
List<SearchResult> results = dict.searchByPrefix("app");

// Wildcard
List<SearchResult> matches = dict.searchByPattern("c.t");

// Delete
boolean deleted = dict.deleteWord("apple");
```

### The Trie Node

```java
class TrieNode {
    Map<Character, TrieNode> children;  // HashMap
    String meaning;                      // null if not end
    boolean isEndOfWord;                 // true if complete word
}
```

---

## Final Interview Quote

> "I use a Trie because it provides **O(m) exact lookup** independent of dictionary size, **native prefix search** by traversing to the prefix node and collecting descendants, and **space efficiency** through shared prefixes. Each TrieNode uses a HashMap for O(1) child access. The `isEndOfWord` flag distinguishes complete words from mere prefixes - 'app' can be both a word and a prefix of 'apple'."

