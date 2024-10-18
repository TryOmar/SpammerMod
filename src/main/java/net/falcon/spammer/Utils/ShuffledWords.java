package net.falcon.spammer.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ShuffledWords {

    // Configuration variables
    private static final int MAXIMUM_DUPLICATES = 2; // Maximum number of duplicates allowed
    private static final int MINIMUM_WORDS = 1;      // Minimum number of words to select

    public static String getLastShuffledWords(String inputString) {
        // Split the string into words, removing punctuation
        String[] words = inputString.split("\\W+");

        // Create a list to store the words
        List<String> wordList = new ArrayList<>();
        Collections.addAll(wordList, words);

        // Check if there are enough words
        if (wordList.size() < MINIMUM_WORDS) {
            return ""; // Return an empty string if not enough words
        }

        // Select a random number of words (between MINIMUM_WORDS and the total number of words)
        Random random = new Random();
        int numberOfWordsToSelect = Math.max(MINIMUM_WORDS, random.nextInt(wordList.size() - MINIMUM_WORDS + 1) + MINIMUM_WORDS);

        // Select random words
        List<String> selectedWords = new ArrayList<>();
        while (selectedWords.size() < numberOfWordsToSelect) {
            int randomIndex = random.nextInt(wordList.size());
            String selectedWord = wordList.get(randomIndex);

            // Define a new maximum duplicates that is between 1 and MAXIMUM_DUPLICATES
            int maxDuplicatesForWord = random.nextInt(MAXIMUM_DUPLICATES) + 1; // Randomly between 1 and MAXIMUM_DUPLICATES

            // Count current duplicates in the selected words
            int currentDuplicates = Collections.frequency(selectedWords, selectedWord);
            // Allow adding the word if it hasn't reached the maximum duplicates limit
            if (currentDuplicates < maxDuplicatesForWord) {
                selectedWords.add(selectedWord);
            }
        }

        // Shuffle the selected words
        Collections.shuffle(selectedWords);

        // Join the shuffled words into a single string
        return String.join(" ", selectedWords);
    }

    public static void main(String[] args) {
        String input = "Hello World, I'm here to do something good.";
        String lastShuffledWords = getLastShuffledWords(input);
        System.out.println(lastShuffledWords); // Example output: "good here good"
    }
}
