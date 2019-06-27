/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.anagrams;

import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class AnagramDictionary {

    private static final int MIN_NUM_ANAGRAMS = 5;
    private static final int DEFAULT_WORD_LENGTH = 3;
    private static final int MAX_WORD_LENGTH = 7;
    private Random random = new Random();
    private HashSet<String> wordSet = new HashSet<>();
    private HashMap<String, ArrayList<String>> lettersToWord = new HashMap<>();
    private SparseArray<ArrayList<String>> sizeToStarterWords = new SparseArray<>();
    private int wordLength = DEFAULT_WORD_LENGTH;
    private HashSet<String> usedWords = new HashSet<>();

    AnagramDictionary(Reader reader) throws IOException {
        BufferedReader in = new BufferedReader(reader);
        String line;
        while ((line = in.readLine()) != null) {
            String word = line.trim();
            wordSet.add(word);

            String sorted = sortLetters(word);
            if (!lettersToWord.containsKey(sorted))
                lettersToWord.put(sorted, new ArrayList<>());
            lettersToWord.get(sorted).add(word);

            int len = word.length();
            if (getAnagramsWithOneMoreLetter(word).size() > MIN_NUM_ANAGRAMS) {
                if (sizeToStarterWords.get(len, null) == null)
                    sizeToStarterWords.put(len, new ArrayList<>());
                sizeToStarterWords.get(len).add(word);
            }
        }
    }

    boolean isGoodWord(String word, String base) {
        return !word.contains(base) && wordSet.contains(word);
    }

    private List<String> getAnagrams(String targetWord) {
        String sorted = sortLetters(targetWord);
        return lettersToWord.get(sorted);
    }

    private String sortLetters(String word) {
        StringBuilder sb = new StringBuilder(word);
        for (int i = 0; i < sb.length() - 1; i++) {
            char min = sb.charAt(i);
            int indexMin = i;
            for (int j = i + 1; j < sb.length(); j++) {
                char c = sb.charAt(j);
                if (c < min) {
                    min = c;
                    indexMin = j;
                }
            }
            char old = sb.charAt(i);
            sb.setCharAt(indexMin, old);
            sb.setCharAt(i, min);
        }
        return sb.toString();
    }

    List<String> getAnagramsWithOneMoreLetter(String word) {
        ArrayList<String> result = new ArrayList<>();

        for (char c = 'a'; c <= 'z'; c++) {
            List<String> anagrams = getAnagrams(word + c);
            if (anagrams != null)
                result.addAll(anagrams.stream()
                        .filter(w -> !w.contains(word))
                        .collect(Collectors.toList()));
        }

        return result;
    }

    void increaseDifficulty() {
        wordLength++;
    }

    void decreaseDifficulty() {
        wordLength--;
    }

    public void reset() {
        usedWords.clear();
        wordLength = DEFAULT_WORD_LENGTH;
    }

    HashSet<String> getUsedWords() {
        return usedWords;
    }

    String pickGoodStarterWord() throws Exception {
        ArrayList<String> words = sizeToStarterWords.get(wordLength);
        if (words == null)
            throw new Exception("No good starter word found, please check the dictionary and the min/max word length.");

        int rand = random.nextInt(words.size() - 1);

        for (int i = rand + 1; i < words.size(); i++) {
            String word = words.get(i);
            if (!usedWords.contains(word)) {
                usedWords.add(word);
                return word;
            }

            if (i == rand) {
                if (wordLength < MAX_WORD_LENGTH) {
                    wordLength++;
                    return pickGoodStarterWord();  // look for a longer word
                }
                throw new Exception("No good starter word found, please check the dictionary and the minimum number of anagrams set.");
            }

            // Wrap around
            if (i == words.size() - 1)
                i = -1;
        }

        throw new Exception("No good starter word found, please check the dictionary.");
    }
}
