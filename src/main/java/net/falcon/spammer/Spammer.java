package net.falcon.spammer;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.falcon.spammer.Handlers.ChatMessageHandler;
import net.falcon.spammer.Managers.Debugging;
import net.falcon.spammer.Managers.SpamManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Spammer implements ModInitializer {
	public static final String MOD_ID = "spammer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static boolean CHAT_EVENTS_LOADED = false;

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");


		ClientPlayConnectionEvents.INIT.register((ClientPlayNetworkHandler handler, MinecraftClient client) -> {
			Debugging.Spam("Client connected to server: " + handler.getConnection().getAddress());
			// Load chat events
			if(!CHAT_EVENTS_LOADED) {
				ChatMessageHandler.loadChatEvents();
				CHAT_EVENTS_LOADED = true;
			}
			// Load all custom commands
			ChatMessageHandler.loadAllCustomCommands();
			SpamManager.spamStatus.clear();
		});


		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			Debugging.Spam("Client disconnected from server: " + handler.getConnection().getAddress());
			SpamManager.spamStatus.clear();
		});
	}
}