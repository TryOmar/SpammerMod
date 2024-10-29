package net.falcon.spammer.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageParser {

    // Public static variables for message splitters
    public static final String[] MESSAGE_SPLITTERS = { "»", ":" };

    public static String[] parseMessage(String fullMessage) {
        if(fullMessage.isEmpty()) return new String[] { "", "" };
        String sender = "";
        String messageContent = "";
        boolean foundSeparator = false;

        // Check for each separator
        for (String splitter : MESSAGE_SPLITTERS) {
            if (fullMessage.contains(splitter)) {
                // Extract the sender name (part before the splitter) and get the last word
                sender = fullMessage.split(splitter)[0].trim();
                String[] senderParts = sender.split(" "); // Split on spaces
                sender = senderParts[senderParts.length - 1]; // Get the last element
                if(senderParts[senderParts.length - 1].contains("[")) sender = senderParts[senderParts.length - 2]; // Get the second last element
                messageContent = fullMessage.split(splitter)[1].trim();
                foundSeparator = true;
                break; // Exit loop if a separator is found
            }
        }

        // If no separators are found, use the first word as sender and the rest as content
        if (!foundSeparator) {
            sender = fullMessage.substring(0, fullMessage.indexOf(" ")).trim(); // First word as sender
            messageContent = fullMessage.substring(sender.length()).trim(); // Rest as content
        }

        return new String[] { sender, messageContent };
    }


    // Short and concise replace function
    public static String replaceAllLiteral(String input, String pattern, String replacement) {
        return input.replaceAll("(?i)" + Pattern.quote(pattern), Matcher.quoteReplacement(replacement));
    }

    public static void main(String[] args) {
        String fullMessage1 = "Brawler | MEMBER KillUAll91 » false";
        String fullMessage2 = "Brawler | MEMBER KillUAll91: false";
        String fullMessage3 = "KillUAll91 Hello everyone!";

        String[] parsedMessage1 = parseMessage(fullMessage1);
        System.out.println("Sender: " + parsedMessage1[0]);
        System.out.println("Content: " + parsedMessage1[1]);

        String[] parsedMessage2 = parseMessage(fullMessage2);
        System.out.println("Sender: " + parsedMessage2[0]);
        System.out.println("Content: " + parsedMessage2[1]);

        String[] parsedMessage3 = parseMessage(fullMessage3);
        System.out.println("Sender: " + parsedMessage3[0]);
        System.out.println("Content: " + parsedMessage3[1]);
    }
}
