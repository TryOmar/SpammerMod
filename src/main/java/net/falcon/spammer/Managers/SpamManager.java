package net.falcon.spammer.Managers;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.falcon.spammer.Handlers.ChatMessageHandler;
import net.falcon.spammer.Models.SpamConfig;
import net.falcon.spammer.Utils.MessageParser;
import net.falcon.spammer.Utils.OnlinePlayers;
import net.falcon.spammer.Utils.PatternMatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.io.File;
import java.io.IOException;
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
                "!folderSpam - Open the spam folder\n" +
                "!scanChat <name> - Scan the chat for the given name\n" +
                "!scanClear - Clear the scan result file\n";

        ChatMessageHandler.sendSystemMessage(helpMessage);

    }

    public static void create(String id) {
        if (SpamConfig.exists(id)) {
            // Send message to chat that the file already exists
            String message = "File Spam config ID already exists: " + id;
            ChatMessageHandler.sendSystemMessage(message);
        } else {
            // Create a new config
            SpamConfig newConfig = new SpamConfig(id);

            // Build the message indicating success
            String successMessage = "File created successfully with ID: " + id;
            ChatMessageHandler.sendSystemMessage(successMessage);

            // Create the directory structure if it doesn't exist
            File userDir = new File("Spam", SpamConfig.getUsername());
            if (!userDir.exists()) userDir.mkdirs();

            // Specify the config file path directly in the user's directory
            File configFile = new File(userDir, id + ".json"); // Save config files directly in the user's folder

            // Ensure the file is created (optional, based on your implementation of SpamConfig)
            try {
                if (configFile.createNewFile()) {
                    // Successfully created the file
                }
            } catch (IOException e) {
                LOGGER.warn("Failed to create the config file: " + e.getMessage());
            }

            // Build the message for the Spam chat folder
            String folderPath = userDir.getAbsolutePath();
            MutableText folderMessage = Text.literal("Click here to open the Spam chat folder: ")
                    .append(
                            Text.literal("[Spam Chat Folder]")
                                    .styled(style -> style.withColor(Formatting.AQUA)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, folderPath))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Open Spam chat folder"))))
                    );

            // Build the message for the new config file
            String configFilePath = configFile.getAbsolutePath();
            MutableText fileMessage = Text.literal("Click here to open the config file: ")
                    .append(
                            Text.literal("[" + configFile.getName() + "]")
                                    .styled(style -> style.withColor(Formatting.AQUA)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, configFilePath))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Open config file"))))
                    );

            // Send both messages to the player
            MinecraftClient.getInstance().player.sendMessage(folderMessage, false);
            MinecraftClient.getInstance().player.sendMessage(fileMessage, false);
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

            for (SpamConfig config : SpamConfig.getAllIds()) {
                if (spamStatus.getOrDefault(config.id, false)) {
                    runningConfigs.append(config.id+" - " + config.targetUsername).append("\n ");
                } else {
                    notRunningConfigs.append(config.id+" - "+config.targetUsername).append("\n ");
                }
            }

            String message = "File Spam config ID does not exist: " + id + "\n" +
                    "Available IDs:\n" +
                    "Running:\n " + runningConfigs.toString() + "\n" +
                    "Not Running:\n " + notRunningConfigs.toString() + "\n";

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


    public static void scanName(String name) {
        List<String> fileNames = new ArrayList<>();
        fileNames.add(Chatting.ALL_GENERAL_MESSAGES);
        fileNames.add(Chatting.ALL_PRIVATE_MESSAGES);
        Chatting.scanResult(fileNames, name);

        // Create the directory structure if it doesn't exist
        File userDir = new File("Spam", SpamConfig.getUsername());
        if (!userDir.exists()) userDir.mkdirs();

        File chatDir = new File(userDir, "Chat");
        if (!chatDir.exists()) chatDir.mkdirs();

        // Specify the file to open (ScanResult.txt)
        File resultFile = new File(chatDir, Chatting.SCAN_RESULT);

        // Build the message for the Spam chat folder
        String folderPath = chatDir.getAbsolutePath();
        MutableText folderMessage = Text.literal("Click here to open the Spam chat folder: ")
                .append(
                        Text.literal("[Spam Chat Folder]")
                                .styled(style -> style.withColor(Formatting.AQUA)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, folderPath))
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Open Spam chat folder"))))
                );

        // Build the message for the Scan Result file
        MutableText fileMessage;
        if (resultFile.exists()) {
            String filePath = resultFile.getAbsolutePath();
            fileMessage = Text.literal("Click here to open the Scan Result file: ")
                    .append(
                            Text.literal("[" + Chatting.SCAN_RESULT + "]")
                                    .styled(style -> style.withColor(Formatting.AQUA)
                                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, filePath))
                                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Open Scan Result file"))))
                    );
        } else {
            fileMessage = Text.literal("Scan Result file not found.");
        }

        // Send both messages to the player
        MinecraftClient.getInstance().player.sendMessage(folderMessage, false);
        MinecraftClient.getInstance().player.sendMessage(fileMessage, false);
    }

    public static void scanClear(String name) {
        Chatting.clearFile(Chatting.SCAN_RESULT);
        ChatMessageHandler.sendSystemMessage("Scan result file cleared\n");
    }

    public static void test(String numberOfMessages) {
        ChatMessageHandler.sendSystemMessage("Test spam function:\n + ");
        // number of messages to send to int
        OnlinePlayers onlinePlayers = new OnlinePlayers();
        ChatMessageHandler.sendSystemMessage(onlinePlayers.getOnlinePlayer());
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
        Sleep(200);
        ChatMessageHandler.sendSystemMessage("Spam started!");


        spamStatus.put(id, true);
        SpamConfig config = new SpamConfig(id);

        Thread thread = new Thread(() -> {
            try {

                long count = config.getTotalLoopMessagesCount();
                for(long i = 0; i < count && i < config.maxTotalLoopMessagesCount; i++) {
                    // --- Check if the spam is stopped ---
                    if(!spamStatus.getOrDefault(id, false)) break;
                    // --- Update the config ---
                    long updateDelay = updateSpamConfig(config);

                    // --- Wait for the triggers ---
                    String triggerKeyword = config.keywordTrigger;
                    //System.out.println("Trigger Keyword: " + triggerKeyword);
                    long postTriggerMessageCount = config.getMessageCountTrigger();
                    String lastFullMessage = waitForChatTriggers(config.id, postTriggerMessageCount, triggerKeyword);

                    // --- Check if the spam is stopped ---
                    if (!spamStatus.getOrDefault(id, false)) break;

                    // --- Wait for the loop delay ---
                    long loopDelay = config.getLoopDelay();
                    Sleep(loopDelay);


                    // --- Send the message ---
                    String message = config.getMessage(lastFullMessage);
                    boolean isPrivateMessage = config.isPrivateMessage;
                    final long finalI = i;
                    new Thread(() -> {
                        // send teh thread id
                        long postDelay = config.getPostTriggerDelay();
                        Sleep(postDelay);
                        if(!spamStatus.getOrDefault(id, false)) return;
                        if (isPrivateMessage) {
                            String privateMessageCommand = config.getPrivateMessageCommand(lastFullMessage);
                            ChatMessageHandler.sendCommand(privateMessageCommand);
                        } else {
                            ChatMessageHandler.sendChatMessage(message);
                        }

                        // --- If it was the last message, stop the spam ---
                        if(finalI == count - 1) {
                            spamStatus.put(id, false);
                            Sleep(200);
                            ChatMessageHandler.sendSystemMessage("Spam finished for ID: " + id + "\n");
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
        LOGGER.info("Sleeping for " + duration + "ms");
        if(duration <= 0) return;
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

    public static String waitForChatTriggers(String id, long messageThreshold, String substring) {
        if(messageThreshold == 0 && substring.isEmpty()) return ChatMessageHandler.LastFullMessage;
        if (!spamStatus.getOrDefault(id, false)) {
            return "";
        }
        CompletableFuture<String> future = new CompletableFuture<>();
        final long[] messageCount = {0};
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
            String messageContent = MessageParser.parseMessage(fullMessage)[1];



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

            //System.out.println("\nSubstring to match: " +  substring + "\nSubstring matched: " + substringMatched[0] + " \nMessage count: " + messageCount[0] + "\nLast Message: " + fullMessage);
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
