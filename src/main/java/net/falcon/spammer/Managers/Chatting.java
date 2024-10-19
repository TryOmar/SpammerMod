package net.falcon.spammer.Managers;

import net.falcon.spammer.Models.SpamConfig;
import net.falcon.spammer.Utils.NameMatcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.mojang.text2speech.Narrator.LOGGER;

public class Chatting {
    private static final boolean DISABLE = false;
    public static final long MAX_FILE_SIZE = 1024 * 1024; // 1 MB limit
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Public final file names
    public static final String SENT_GENERAL_MESSAGES = "SentGeneralMessages.txt";
    public static final String RECEIVED_GENERAL_MESSAGES = "ReceivedGeneralMessages.txt";
    public static final String ALL_GENERAL_MESSAGES = "AllGeneralMessages.txt";
    public static final String SENT_PRIVATE_MESSAGES = "SentPrivateMessages.txt";
    public static final String RECEIVED_PRIVATE_MESSAGES = "ReceivedPrivateMessages.txt";
    public static final String ALL_PRIVATE_MESSAGES = "AllPrivateMessages.txt";
    public static final String SYSTEM_MESSAGES = "SystemMessages.txt";
    public static final String MENTIONS_GENERAL_CHAT = "MentionsGeneralChat.txt";
    public static final String SCAN_RESULT = "ScanResult.txt";

    private static void writeToFile(String fileName, String message) {
        if (DISABLE) return; // Assuming DISABLE is a valid flag

        File spamDir = new File("Spam");
        if (!spamDir.exists()) spamDir.mkdir();
        File userDir = new File(spamDir, SpamConfig.getUsername());
        if (!userDir.exists()) userDir.mkdir();
        File chatDir = new File(userDir, "Chat");
        if (!chatDir.exists()) chatDir.mkdir();
        File file = new File(chatDir, fileName);

        try {
            // Get the current date and time
            String timestamp = LocalDateTime.now().format(formatter);
            String formattedMessage = String.format("[%s] %s", timestamp, message);

            // Check file size before writing
            if (file.exists() && file.length() > MAX_FILE_SIZE) {
                // Read all lines from the file
                List<String> lines = new ArrayList<>();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lines.add(line);
                    }
                }

                // Remove the oldest lines until the file is under the size limit
                int linesToRemove = Math.max(1, lines.size() / 10);  // Remove 10% of the lines or at least 1
                for (int i = 0; i < linesToRemove; i++) {
                    lines.remove(0);  // Remove from the front
                }

                // Write back the remaining lines and append the new message
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    for (String remainingLine : lines) {
                        writer.write(remainingLine);
                        writer.newLine();
                    }
                    writer.write(formattedMessage);
                    writer.newLine();
                }
            } else {
                // If file size is within limits, simply append the new message
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                    writer.write(formattedMessage);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to write to file: " + e.getMessage());
        }
    }

    public static void clearFile(String fileName) {
        File spamDir = new File("Spam");
        if (!spamDir.exists()) spamDir.mkdir();

        File userDir = new File(spamDir, SpamConfig.getUsername());
        if (!userDir.exists()) userDir.mkdir();

        File chatDir = new File(userDir, "Chat");
        if (!chatDir.exists()) chatDir.mkdir();

        File file = new File(chatDir, fileName);

        // Check if the file exists
        if (file.exists()) {
            // Attempt to delete the file
            if (file.delete()) {
                // File successfully deleted
                LOGGER.info("Deleted file: " + fileName);
            } else {
                LOGGER.warn("Failed to delete file: " + fileName);
            }
        }

        // Create a new empty file
        try {
            if (file.createNewFile()) {
                LOGGER.info("Created new file: " + fileName);
            } else {
                LOGGER.warn("File already exists or could not be created: " + fileName);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to create new file: " + e.getMessage());
        }
    }

    // Public messages
    public static void SentGeneralMessages(String message) { writeToFile(SENT_GENERAL_MESSAGES, message); }
    public static void ReceivedGeneralMessages(String message) { writeToFile(RECEIVED_GENERAL_MESSAGES, message); }
    public static void AllGeneralMessages(String message) { writeToFile(ALL_GENERAL_MESSAGES, message); }
    // Private messages
    public static void SentPrivateMessages(String message) { writeToFile(SENT_PRIVATE_MESSAGES, message); }
    public static void ReceivedPrivateMessages(String message) { writeToFile(RECEIVED_PRIVATE_MESSAGES, message); }
    public static void AllPrivateMessages(String message) { writeToFile(ALL_PRIVATE_MESSAGES, message); }
    // System messages
    public static void SystemMessages(String message) { writeToFile(SYSTEM_MESSAGES, message); }

    // Mentions
    public static void MentionsGeneralChat(String message) { writeToFile(MENTIONS_GENERAL_CHAT, message); }


    // New function to retrieve and filter messages
    public static void scanResult(List<String> fileNames, String substring) {
        StringBuilder resultBuilder = new StringBuilder();
        List<String> filteredMessages = new ArrayList<>();

        for (String fileName : fileNames) {
            File file = new File("Spam/" + SpamConfig.getUsername() + "/Chat/" + fileName);
            if (!file.exists()) continue;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                List<Message> messages = new ArrayList<>();

                while ((line = reader.readLine()) != null) {
                    // Split the line into date/time and message
                    String[] parts = line.split("]", 2);
                    if (parts.length < 2) continue;

                    String dateTimeStr = parts[0].substring(1).trim(); // Remove leading '['
                    String messageContent = parts[1].trim();

                    // Parse the date and time
                    LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, formatter);
                    messages.add(new Message(dateTime, messageContent));
                }

                // Sort messages by date/time
                Collections.sort(messages, Comparator.comparing(Message::getDateTime));

                // Filter messages containing the substring
                for (Message message : messages) {
                    if (NameMatcher.containsSimilarName(message.getContent(), substring)) {
                        filteredMessages.add(message.toString());
                    }
                }
            } catch (IOException e) {
                LOGGER.warn("Failed to read file: " + fileName + " - " + e.getMessage());
            }
        }

        // Prepare the output
        if (!filteredMessages.isEmpty()) {
            resultBuilder.append("Filtered messages for substring: ").append(substring).append(System.lineSeparator());
            for (String msg : filteredMessages) {
                resultBuilder.append(msg).append(System.lineSeparator());
            }
        } else {
            resultBuilder.append("No messages found containing substring: ").append(substring);
        }

        // Write all results to ScanResult.txt at once
        writeToFile(SCAN_RESULT, resultBuilder.toString());
    }

    // Inner class to hold message data
    private static class Message {
        private final LocalDateTime dateTime;
        private final String content;

        public Message(LocalDateTime dateTime, String content) {
            this.dateTime = dateTime;
            this.content = content;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public String getContent() {
            return content;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s", dateTime.format(formatter), content);
        }
    }
}
