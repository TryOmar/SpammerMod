package net.falcon.spammer.Handlers;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.falcon.spammer.Managers.Debugging;
import net.falcon.spammer.Managers.SpamManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
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

        registerCustomCommand("!runSpam", SpamManager::run);
        registerCustomCommand("!stopSpam", SpamManager::stop);

        registerCustomCommand("!folderSpam", SpamManager::folder);
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
        return true; // Allow the message to be processed normally if no command matches
    }

    public static void onChatMessageReceived(Text messageText, SignedMessage signedMessage, GameProfile profile, MessageType.Parameters parameters, Instant timestamp) {
        // Log the message to the console
        LOGGER.info("Received chat message: " + messageText.getString());

        // Optionally log more details about the sender or message metadata
        if (profile != null) {
            LOGGER.info("Message sent by: " + profile.getName());
        }
    }

    public static void loadChatEvents() {
        ClientSendMessageEvents.ALLOW_CHAT.register(ChatMessageHandler::onChatMessageSent);
        ClientReceiveMessageEvents.CHAT.register(ChatMessageHandler::onChatMessageReceived);
    }

    // ----------------------------- Helper Functions -----------------------------
    public static void sendCommand(String command) {
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
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        client.inGameHud.getChatHud().addMessage(Text.of(message));
    }
}
