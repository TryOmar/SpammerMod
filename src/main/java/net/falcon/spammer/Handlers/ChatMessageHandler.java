package net.falcon.spammer.Handlers;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.falcon.spammer.Managers.Chatting;
import net.falcon.spammer.Managers.Debugging;
import net.falcon.spammer.Managers.SpamManager;
import net.falcon.spammer.Models.SpamConfig;
import net.falcon.spammer.Utils.MessageParser;
import net.falcon.spammer.Utils.NameMatcher;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static com.mojang.text2speech.Narrator.LOGGER;

public class ChatMessageHandler {
    public static final Map<String, Consumer<String>> customCommands = new HashMap<>();
    public static Thread thread;

    public static void loadAllCustomCommands() {
        registerCustomCommand("!helpSpam", SpamManager::help);
        registerCustomCommand("!showSpam", SpamManager::show);

        registerCustomCommand("!createSpam", SpamManager::create);
        registerCustomCommand("!deleteSpam", SpamManager::delete);
        registerCustomCommand("!renameSpam", SpamManager::rename);

        registerCustomCommand("!runSpam", SpamManager::run);
        registerCustomCommand("!stopSpam", SpamManager::stop);

        registerCustomCommand("!folderSpam", SpamManager::folder);
        registerCustomCommand("!testSpam", SpamManager::test);

        registerCustomCommand("!scanName", SpamManager::scanName);
        registerCustomCommand("!scanClear", SpamManager::scanClear);
    }

    public static void registerCustomCommand(String command, Consumer<String> action) {
        customCommands.put(command, action);
    }

    // ----------------------------- Chat Message Events -----------------------------
    public static boolean onChatMessageSent(String message) {
        for (Map.Entry<String, Consumer<String>> entry : customCommands.entrySet()) {
            if (message.startsWith(entry.getKey())) {
                if (thread != null && thread.isAlive()) return false;
                String commandId = message.substring(entry.getKey().length()).trim();
                thread = new Thread(() -> {
                    try {
                        entry.getValue().accept(commandId);
                    } catch (Exception e) {
                        Debugging.Error("Thread interrupted: " + e.getMessage());
                        LOGGER.error("Thread interrupted", e);
                        Thread.currentThread().interrupt();
                    }
                });
                thread.start();
                return false; // Prevent further processing of the message
            }

        }
        if(message.startsWith("!")) return false;
        return true; // Allow the message to be processed normally if no command matches
    }

    public static String LastFullMessage = "";
    public static String LastMessage = "";
    public static String LastMessageSender = "";
    public static void onChatMessageReceived(Text messageText, SignedMessage signedMessage, GameProfile profile, MessageType.Parameters parameters, Instant timestamp) {
        String messageContent = MessageParser.parseMessage(messageText.getString())[1];
        LastFullMessage = messageText.getString();
        LastMessage = messageContent;
        LastMessageSender = profile.getName();
        // Log the message to the console
        Chatting.AllGeneralMessages(messageText.getString());

        //if(messageText.getString().split("»")[1].trim())
        if (NameMatcher.containsSimilarName(messageContent, MinecraftClient.getInstance().getSession().getUsername())) {
            Chatting.MentionsGeneralChat(messageText.getString());

            if (profile.getName().equals(MinecraftClient.getInstance().getSession().getUsername())) {
                Chatting.SentGeneralMessages(messageText.getString());
            } else {
                Chatting.ReceivedGeneralMessages(messageText.getString());
            }
        }
    }

    public static void onSystemMessageReceived(Text messageText, boolean overlay) {
        if(messageText.getString().contains("[MESSAGE]")){
            if (messageText.getString().contains("me ➟"))
                Chatting.SentPrivateMessages(messageText.getString());
            else if (messageText.getString().contains("➟ me"))
                Chatting.ReceivedPrivateMessages(messageText.getString());
            Chatting.AllPrivateMessages(messageText.getString());
        }

        Chatting.SystemMessages(messageText.getString());
    }

    public static void loadChatEvents() {
        ClientSendMessageEvents.ALLOW_CHAT.register(ChatMessageHandler::onChatMessageSent);
        ClientReceiveMessageEvents.CHAT.register(ChatMessageHandler::onChatMessageReceived);
        ClientReceiveMessageEvents.GAME.register(ChatMessageHandler::onSystemMessageReceived);
    }

    // ----------------------------- Helper Functions -----------------------------
    public static void sendCommand(String command) {
        LOGGER.info("Sending command: " + command);
        //Debugging.Spam("Sending command: " + command);
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        client.player.networkHandler.sendChatCommand(command);
    }

    public static void sendChatMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        client.player.networkHandler.sendChatMessage(message);
    }

    public static void sendSystemMessage(String message) {
        LOGGER.info("Sending system message: " + message);
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        client.inGameHud.getChatHud().addMessage(Text.of(message));
    }

    // ----------------------------- Chat Message Events -----------------------------
    public static boolean waitForMessages(int messageThreshold) {
        // Create a CompletableFuture to wait for the message count to reach the threshold
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Counter for the received messages
        final int[] messageCount = {0};

        // Register the listener for chat messages
        ClientReceiveMessageEvents.CHAT.register((messageText, signedMessage, profile, parameters, timestamp) -> {
            // Log the received message to the console
            LOGGER.info("Received chat message: " + messageText.getString());

            // Optionally log more details about the sender or message metadata
            if (profile != null) {
                LOGGER.info("Message sent by: " + profile.getName());
            }

            // Increment the message count
            messageCount[0]++;

            // Complete the future if the message count reaches the threshold
            if (messageCount[0] >= messageThreshold) {
                future.complete(true);
            }
        });

        // Wait for the CompletableFuture to complete and handle exceptions internally
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error while waiting for messages: " + e.getMessage());
            return false;
        }
    }

}
