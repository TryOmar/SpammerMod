package net.falcon.spammer.Utils;

public class NameMatcher {

    public static boolean containsSimilarName(String message, String name) {
        int maxAllowedDistance = calculateMaxAllowedDistance(name.length());

        // Normalize the message and the name (lowercase, no special characters)
        String normalizedMessage = message.toLowerCase().replaceAll("[^a-z0-9 ]", "");
        String[] words = normalizedMessage.split("\\s+");  // Split by spaces to get words
        String normalizedName = name.toLowerCase();

        // Check if the message contains a word similar to the name
        for (String word : words) {
            // Check if the name is a substring of the word
            if (word.contains(normalizedName)) {
                return true; // Return true if a direct substring match is found
            }

            // Calculate Levenshtein distance
            int distance = calculateLevenshteinDistance(word, normalizedName);
            if (distance <= maxAllowedDistance) {
                return true; // Return true if a close match is found
            }
        }
        return false; // Return false if no match is found
    }

    private static int calculateMaxAllowedDistance(int nameLength) {
        return Math.max(1, nameLength / 3); // Adjust the factor as needed
    }

    private static int calculateLevenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            for (int j = 0; j <= len2; j++) {
                if (i == 0) {
                    dp[i][j] = j;  // If first string is empty, insert all characters of second string
                } else if (j == 0) {
                    dp[i][j] = i;  // If second string is empty, remove all characters of first string
                } else if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];  // Characters match, no operation required
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j - 1],  // Replace
                            Math.min(dp[i - 1][j],  // Remove
                                    dp[i][j - 1]));  // Insert
                }
            }
        }
        return dp[len1][len2];
    }

    public static void main(String[] args) {
        // Test cases
        String name = "abc123abc";

        System.out.println("Testing messages with variations of the name:");
        System.out.println(containsSimilarName("abcabc is online!", name)); // true

        System.out.println(containsSimilarName("Omaar is online!", name)); // true
        System.out.println(containsSimilarName("Omar is playing now", name)); // true
        System.out.println(containsSimilarName("I think Omr is here", name)); // true
        System.out.println(containsSimilarName("Omar is the best!", name)); // true
        System.out.println(containsSimilarName("Omaru is here", name)); // false
        System.out.println(containsSimilarName("Mikey is playing", name)); // false
        System.out.println(containsSimilarName("Omarrr is absent", name)); // true
        System.out.println(containsSimilarName("Omar123 is the name", name)); // true
    }
}
