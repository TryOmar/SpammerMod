package net.falcon.spammer.Managers;

import net.falcon.spammer.Handlers.ChatMessageHandler;
import net.falcon.spammer.Models.SpamConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;

public class SpamManager {
    public static Map<String, Boolean> spamStatus = new HashMap<>();

    public static void help(String message) {
        String helpMessage = "Spam Commands:\n" +
                "!createSpam <id> - Create a new spam config file\n" +
                "!showSpam <id> - Show the spam config file\n" +
                "!deleteSpam <id> - Delete the spam config file\n";
        ChatMessageHandler.sendSystemMessage(helpMessage);
    }

    public static void create(String id) {
        if (SpamConfig.exists(id)) {
            // Send message to chat that the file already exists
            String message = "File Spam config ID already exists: " + id + "\n";
            ChatMessageHandler.sendSystemMessage(message);
        } else {
            SpamConfig newConfig = new SpamConfig(id);
            newConfig.write(); // Save the new config file
            String message = "File created successfully with ID: " + id + "\n";
            ChatMessageHandler.sendSystemMessage(message);
        }
    }

    public static void show(String id) {
        if (SpamConfig.exists(id)) {
            SpamConfig config = new SpamConfig(id);
            boolean isRunning = spamStatus.getOrDefault(id, false); // Check if the ID is running
            String statusMessage = "\nStatus: " + (isRunning ? "Running" : "Not Running");
            ChatMessageHandler.sendSystemMessage(config.toString() + "\n" + statusMessage + "\n");
        } else {
            StringBuilder runningConfigs = new StringBuilder();
            StringBuilder notRunningConfigs = new StringBuilder();

            for (String configId : SpamConfig.getAllIds()) {
                if (spamStatus.getOrDefault(configId, false)) {
                    runningConfigs.append(configId).append(", ");
                } else {
                    notRunningConfigs.append(configId).append(", ");
                }
            }

            String message = "File Spam config ID does not exist: " + id + "\n" +
                    "Available IDs:\n" +
                    "Running: " + runningConfigs.toString() + "\n" +
                    "Not Running: " + notRunningConfigs.toString() + "\n";

            ChatMessageHandler.sendSystemMessage(message);
        }
    }

    public static void delete(String id) {
        if (SpamConfig.exists(id)) {
            SpamConfig.delete(id);
            String message = "File Spam config ID deleted: " + id + "\n";
            ChatMessageHandler.sendSystemMessage(message);
        } else {
            String message = "File Spam config ID does not exist: " + id + "\n";
            ChatMessageHandler.sendSystemMessage(message);
        }
    }

    public static void run(String id){
        if(!SpamConfig.exists(id)){
            ChatMessageHandler.sendSystemMessage("Spam config ID does not exist: " + id + "\n");
            return;
        }

        if(spamStatus.containsKey(id) && spamStatus.get(id)){
            ChatMessageHandler.sendSystemMessage("Spam is already running for ID: " + id + "\n");
            return;
        }

        runSpam(id);
    }

    public static void stop(String id){
        if(!SpamConfig.exists(id)){
            ChatMessageHandler.sendSystemMessage("Spam config ID does not exist: " + id + "\n");
            return;
        }

        if(!spamStatus.containsKey(id) || !spamStatus.get(id)){
            ChatMessageHandler.sendSystemMessage("Spam is not running for ID: " + id + "\n");
            return;
        }

        spamStatus.put(id, false);
        ChatMessageHandler.sendSystemMessage("Spam stopped for ID: " + id + "\n");
    }

    public static void folder(String unused) {
        File userDir = new File("Spam", SpamConfig.getUsername());
        if (!userDir.exists()) userDir.mkdirs();

        String path = userDir.getAbsolutePath();
        MutableText message = Text.literal("Click here to open the Spam folder: ")
                .append(
                        Text.literal("[Minecraft/Spam/" + SpamConfig.getUsername() + "]")
                                .styled(style -> style.withColor(Formatting.AQUA)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Open folder"))))
                );

        MinecraftClient.getInstance().player.sendMessage(message, false);
    }


    // ----------------------------- Spam Functions -----------------------------
    public static void runSpam(String id) {
        // say running then sya starts in 3 2 1 then start spamming
        ChatMessageHandler.sendSystemMessage("Running spam for ID: " + id + "\n");
        ChatMessageHandler.sendSystemMessage("Spam starts in 3\n");
        Sleep(1000);
        ChatMessageHandler.sendSystemMessage("Spam starts in 2\n");
        Sleep(1000);
        ChatMessageHandler.sendSystemMessage("Spam starts in 1\n");
        Sleep(1000);

        spamStatus.put(id, true);
        SpamConfig config = new SpamConfig(id);
        Thread thread = new Thread(() -> {
            try {
                while (spamStatus.get(id)) {
                    ChatMessageHandler.sendChatMessage(config.getMessage());
                    long updateDelay = updateSpamConfig(config);
                    Sleep(config.getDelay() - updateDelay);
                }
            } catch (Exception e) {
                spamStatus.put(id, false);
                LOGGER.error("Thread interrupted", e);
                Thread.currentThread().interrupt();
            }
        });
        thread.start();
    }

    public static void Sleep(long duration){
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            LOGGER.error("Thread interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    public static long updateSpamConfig(SpamConfig config) {
        long startTime = System.currentTimeMillis();
        config.read();
        long endTime = System.currentTimeMillis();
        Debugging.Spam("Spam config updated: " + config.id + ", delay: " + (endTime - startTime) + "\n");
        return endTime - startTime;
    }
}
