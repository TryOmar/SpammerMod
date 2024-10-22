package net.falcon.spammer.Utils;

import com.ibm.icu.impl.Pair;

import java.util.ArrayList;
import java.util.List;

public class MessageDelaySplitter {

    private static final int DEFAULT_DELAY = 200; // Default delay in milliseconds
    // Static method to split messages and return a list of pairs (delay, message)
    /**
     * Splits a message string into pairs of (delay, message) based on the '|' delimiter.
     * The delay is an integer representing the delay in milliseconds before sending the message.
     * If the message does not start with a delay, the default delay is used.
     * @param message the message string to split
     * @return a list of pairs (delay, message)
     */
    public static List<Pair<Integer, String>> splitMessages(String message) {
        List<Pair<Integer, String>> result = new ArrayList<>();

        if (message.charAt(0) != '|') message = "|0 " + message;
        String[] parts = message.split("\\|");

        for (String part : parts) {
            String twoParts[] = part.split(" ", 2);
            if (!part.isEmpty() &&  isNumber(twoParts[0])){

                int delay = Integer.parseInt(twoParts[0]);
                String messagePart = twoParts[1];

                if (!messagePart.trim().isEmpty()) {
                    result.add(Pair.of(delay, messagePart.trim()));
                }
            } else{
                // If it doesn't start with a number, treat it as a message with default delay
                if (!part.trim().isEmpty()) {
                    result.add(Pair.of(DEFAULT_DELAY, part.trim()));
                }
            }

        }

        return result;
    }


    /**
     * Checks if a string is a number.
     * @param str the string to check
     * @return true if the string is a number, false otherwise
     */
    private static boolean isNumber(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        // Sample messages for testing
        String[] messages = {
                "Hello friend Default is 200ms!|100100ms Delay How are you doing?|0 hi|10omar10ms",
                "|100 100ms Delay How are you doing?|0 hi|10omar10ms",
                "No delay message| 200ms Delay How are you doing?|0 hi|10omar10ms",
        };

        // Testing the splitMessages method
        List<Pair<Integer, String>> messagePairs = splitMessages(messages[0]);

        // Printing the results
        for (Pair<Integer, String> pair : messagePairs) {
            System.out.println("Delay: " + pair.first + " ms, Message: \"" + pair.second + "\"");
        }
    }
}
