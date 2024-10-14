package net.falcon.spammer.Managers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.mojang.text2speech.Narrator.LOGGER;

public class Debugging {
    private static final boolean DISALBE = true;
    private static String currentScreen;
    public static final long MAX_FILE_SIZE = 1024 * 1024; // 1 MB limit

    private static void writeToFile(String fileName, String message) {
        if(DISALBE) return;
        File file = new File(fileName);

        try {
            if (file.length() > MAX_FILE_SIZE) {
                // Delete the file if it exceeds the limit
                if (!file.delete()) {
                    LOGGER.warn("Failed to delete the file: " + fileName);
                }
            }

            // Write the new message
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
                writer.write(message);
                writer.newLine();
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to write to file: " + e.getMessage());
        }
    }

    public static void Screens(String message) {
        writeToFile("Screens.txt", message);
    }

    public static void LogScreenChange(String newScreen) {
        if(DISALBE) return;
        if (!newScreen.equals(currentScreen)) {
            Debugging.Screens("Screen changed from [" + currentScreen + "] --> [" + newScreen + "]");
            currentScreen = newScreen;
        }
    }

    public static void Chat(String message) { writeToFile("Chat.txt", message); }
    public static void Spam(String message) { writeToFile("Spam.txt", message); }
    public static void Error(String message) { writeToFile("Error.txt", message); }
    public static void Command(String message) { writeToFile("Command.txt", message); }
}
