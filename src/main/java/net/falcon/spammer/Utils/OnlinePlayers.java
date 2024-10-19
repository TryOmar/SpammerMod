package net.falcon.spammer.Utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.util.*;

public class OnlinePlayers {
    private final List<String> chosenPlayers = new ArrayList<>();  // Keeps track of already chosen players

    // Method to get the list of all online players
    public static List<String> getOnlinePlayers() {
        List<String> onlinePlayers = new ArrayList<>();
        MinecraftClient client = MinecraftClient.getInstance();

        // Check if the client is in single-player mode (integrated server)
        if (client.getServer() != null) {
            var names = client.getServer().getPlayerManager().getPlayerNames();
            onlinePlayers.addAll(Arrays.asList(names));
        }
        // Check if the client is connected to a multiplayer server
        else if (client.getNetworkHandler() != null) {
            Collection<PlayerListEntry> players = client.getNetworkHandler().getPlayerList();
            for (PlayerListEntry player : players) {
                onlinePlayers.add(player.getProfile().getName());
            }
        } else {
            System.out.println("No server or network handler available.");
        }

        return onlinePlayers;
    }

    // Method to get a random online player, ensuring no duplicates
    public String getOnlinePlayer() {
        List<String> onlinePlayers = getOnlinePlayers();

        // Shuffle the list of online players
        Collections.shuffle(onlinePlayers);

        // Loop through the shuffled list to find an unchosen player
        for (String player : onlinePlayers) {
            if (!chosenPlayers.contains(player)) {
                chosenPlayers.add(player);  // Add the first unchosen player to the chosen list
                return player;  // Return the chosen player
            }
        }

        // If all players have been chosen, reset the chosenPlayers list
        System.out.println("All players have been chosen. Resetting the chosen list.");
        chosenPlayers.clear();

        // Add the first player from the shuffled list to the chosen list and return it
        String firstPlayer = onlinePlayers.get(0);
        chosenPlayers.add(firstPlayer);

        return firstPlayer;
    }
}
