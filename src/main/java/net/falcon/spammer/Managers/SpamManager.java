package net.falcon.spammer.Managers;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.falcon.spammer.Handlers.ChatMessageHandler;
import net.falcon.spammer.Models.SpamConfig;
import net.falcon.spammer.Utils.PatternMatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.mojang.text2speech.Narrator.LOGGER;

public class SpamManager {
    public static Map<String, Boolean> spamStatus = new HashMap<>();

    public static void help(String message) {
        String helpMessage = "Spam Commands:\n" +
                "!helpSpam - Show this help message\n" +
                "!showSpam <ID> - Show the spam config details\n" +
                "!createSpam <ID> - Create a new spam config file\n" +
                "!deleteSpam <ID> - Delete the spam config file\n" +
                "!renameSpam <ID> <newID> - Rename the spam config file\n" +
                "!runSpam <ID> - Run the spam for the given ID\n" +
                "!stopSpam <ID> - Stop the spam for the given ID\n" +
                "!stopSpam - Stop all spam\n" +
                "!folderSpam - Open the spam folder\n";

        ChatMessageHandler.sendSystemMessage(helpMessage);

    }

    public static void create(String id) {
        if (SpamConfig.exists(id)) {
            // Send message to chat that the file already exists
            String message = "File Spam config ID already exists: " + id + "\n";
            ChatMessageHandler.sendSystemMessage(message);
        } else {
            SpamConfig newConfig = new SpamConfig(id); // Create a new config
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
    // rename id newId
    public static void rename(String message){
        if(message.split(" ").length != 2){
            ChatMessageHandler.sendSystemMessage("Please provide the ID and the new ID\n");
            return;
        }

        String id = message.split(" ")[0];
        String newId = message.split(" ")[1];
        if(!SpamConfig.exists(id)){
            ChatMessageHandler.sendSystemMessage("Spam config ID does not exist: " + id + "\n");
            return;
        }

        if(SpamConfig.rename(id, newId))
            ChatMessageHandler.sendSystemMessage("Spam config ID renamed: " + id + " to " + newId + "\n");
        else
            ChatMessageHandler.sendSystemMessage("Error renaming spam config ID: " + id + " to " + newId + "\n");
    }

    public static void run(String id) {
        if (!SpamConfig.exists(id)) {
            ChatMessageHandler.sendSystemMessage("Spam config ID does not exist: " + id + "\n");
            return;
        }

        if (spamStatus.containsKey(id) && spamStatus.get(id)) {
            ChatMessageHandler.sendSystemMessage("Spam is already running for ID: " + id + "\n");
            return;
        }

        runSpam(id);
    }

    public static void stop(String id) {
        if (id.isEmpty()) {
            spamStatus.clear();
            ChatMessageHandler.sendSystemMessage("All spam stopped\n");
            return;
        }

        if (!SpamConfig.exists(id)) {
            ChatMessageHandler.sendSystemMessage("Spam config ID does not exist: " + id + "\n");
            return;
        }

        if (!spamStatus.containsKey(id) || !spamStatus.get(id)) {
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

    public static void test(String numberOfMessages) {
        ChatMessageHandler.sendSystemMessage("Test spam function:\n");
    }


    // ----------------------------- Spam Functions -----------------------------
    public static void runSpam(String id) {
        if(!SpamConfig.exists(id)){
            ChatMessageHandler.sendSystemMessage("Spam config ID does not exist: " + id + "\n");
            return;
        }

        if(spamStatus.containsKey(id) && spamStatus.get(id)){
            ChatMessageHandler.sendSystemMessage("Spam is already running for ID: " + id + "\n");
            return;
        }
        // say running then sya starts in 3 2 1 then start spamming
        ChatMessageHandler.sendSystemMessage("Running spam for ID: " + id + "\n");
        ChatMessageHandler.sendSystemMessage("Spam starts in 3\n");
        Sleep(1000);
        ChatMessageHandler.sendSystemMessage("Spam starts in 2\n");
        Sleep(1000);
        ChatMessageHandler.sendSystemMessage("Spam starts in 1\n");
        Sleep(1000);
        ChatMessageHandler.sendSystemMessage("Spam started\n");


        spamStatus.put(id, true);
        SpamConfig config = new SpamConfig(id);

        Thread thread = new Thread(() -> {
            try {
                while (spamStatus.getOrDefault(id, false)) {
                    // --- Update the config ---
                    long updateDelay = updateSpamConfig(config);

                    // --- Wait for the triggers ---
                    String triggerKeyword = config.keywordTrigger;
                    System.out.println("Trigger Keyword: " + triggerKeyword);
                    int postTriggerMessageCount = config.getMessageCountTrigger();
                    String lastFullMessage = waitForChatTriggers(config.id, postTriggerMessageCount, triggerKeyword);

                    // --- Check if the spam is stopped ---
                    if (!spamStatus.getOrDefault(id, false)) break;

                    // --- Wait for the post delay ---

                    // --- Send the message ---
                    String message = config.getMessage(lastFullMessage);
                    boolean isPrivateMessage = config.isPrivateMessage;
                    new Thread(() -> {
                        // send teh thread id
                        long postDelay = config.getPostDelay();
                        Sleep(postDelay);
                        if (isPrivateMessage) {
                            String command = config.getCommand(lastFullMessage);
                            ChatMessageHandler.sendCommand(command + " " + message);
                        } else {
                            ChatMessageHandler.sendChatMessage(message);
                        }
                    }).start();
                }
            } catch (Exception e) {
                spamStatus.put(id, false);
                LOGGER.error("Thread interrupted", e);
                Thread.currentThread().interrupt();
            }
        });
        thread.start();
    }

    public static void Sleep(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            LOGGER.error("Thread interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    public static long updateSpamConfig(SpamConfig config) {
        long startTime = System.currentTimeMillis();
        config.updateIfNeeded();
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    public static String waitForChatTriggers(String id, int messageThreshold, String substring) {
        if (!spamStatus.getOrDefault(id, false)) {
            return "";
        }
        CompletableFuture<String> future = new CompletableFuture<>();
        final int[] messageCount = {0};
        final boolean[] substringMatched = {substring.isEmpty()};
        long startTime = System.currentTimeMillis();

        String lowerCaseSubstring = substring.toLowerCase();

        ClientReceiveMessageEvents.CHAT.register((messageText, signedMessage, profile, parameters, timestamp) -> {
            if (!spamStatus.getOrDefault(id, false)) {
                future.complete("");
                return;
            }

            String fullMessage = messageText.getString().toLowerCase();
            String senderName = profile != null ? profile.getName().toLowerCase() : "unknown";
            String messageContent;
            if(fullMessage.contains("»"))
                messageContent = fullMessage.split("»")[1].trim();
            else
                messageContent = fullMessage.split(senderName)[1].trim().substring(1).trim();



            // Construct a new message with Sender: Message
            String newFormMessage = senderName + ": " + messageContent;


//            System.out.println("Message: " + fullMessage);
//            System.out.println("Message Content: " + messageContent);
//            System.out.println("Sender: " + senderName);
//            System.out.println("Our username: " + MinecraftClient.getInstance().getSession().getUsername().toLowerCase());
//            System.out.println("Is self: " + senderName.equals(MinecraftClient.getInstance().getSession().getUsername().toLowerCase()));

            // Only count messages not sent by the client itself
            if (!senderName.equals(MinecraftClient.getInstance().getSession().getUsername().toLowerCase())) {
                messageCount[0]++;
                //substringMatched[0] = substringMatched[0] || fullMessage.contains(lowerCaseSubstring) || senderName.contains(lowerCaseSubstring);
                substringMatched[0] = PatternMatcher.evaluatePattern(fullMessage, substring);
            }

            System.out.println("\nSubstring to match: " +  substring + "\nSubstring matched: " + substringMatched[0] + " \nMessage count: " + messageCount[0] + "\nLast Message: " + fullMessage);
            if (messageCount[0] >= messageThreshold && substringMatched[0]) {
                future.complete(newFormMessage); // Return the last message instead of true
            }
        });

        try {
            String result = future.get(); // Get the last message
            return result; // Return the last message
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error while waiting for chat triggers: " + e.getMessage());
            return ""; // Return an empty string on error
        }
    }
}
