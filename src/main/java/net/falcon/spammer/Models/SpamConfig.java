package net.falcon.spammer.Models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.falcon.spammer.Utils.MessageParser;
import net.falcon.spammer.Utils.OnlinePlayers;
import net.falcon.spammer.Utils.ShuffledWords;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SpamConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().disableInnerClassSerialization().create();

    public String id;
    public String targetUsername = getUsername();

    public boolean isPrivateMessage = true;
    private String privateMessageCommand = "/tell <User>";

    //private long preMinInterval = 3000;
    //private long preMaxInterval = 6000;

    public String keywordTrigger = "hi|hey&!bye";
    private long minMessageCountTrigger = 1;
    private long maxMessageCountTrigger = 1;

    private long postTriggerMinIntervalInSeconds = 3; // In seconds
    private long postTriggerMaxIntervalInSeconds = 6; // In seconds

    private long minLoopIntervalInMilliseconds = 100;
    private long maxLoopIntervalInMilliseconds = 500;

    private long minTotalLoopMessagesCount = 5;
    public long maxTotalLoopMessagesCount = 10;

    private String[] messageTemplates = {
            "Message1: Target user: <User>, Last user: <LastUser>, Last message: <LastMessage>, Shuffled words: <LastShuffledWords>, Online player: <OnlinePlayer>",
            "Message2: Target user: <User>, Last sender: <LastUser>, Last message: <LastMessage>, Shuffled words: <LastShuffledWords>, Online player: <OnlinePlayer>",
            "Message3: Current user: <User>, Recent sender: <LastUser>, Last message: <LastMessage>, Shuffled words: <LastShuffledWords>, Online player: <OnlinePlayer>",
    };

    private transient long lastModifiedTime = System.currentTimeMillis();
    //private transient List<String> messages;
    private transient List<Integer> selectionCounts;
    private transient OnlinePlayers onlinePlayers = new OnlinePlayers();
    private transient Random random = new Random(); // Initialize random

    // -------------------- Constructor and Static Methods --------------------
    public SpamConfig(String id) {
        this.id = id;
        read();
    } // Constructor

    public static String getUsername() {
        return MinecraftClient.getInstance().getSession().getUsername();
    }

    public static void delete(String id) {
        File file = getFile(id);
        if (file.exists()) file.delete();
    }

    public static boolean rename(String oldId, String newId) {
        File oldFile = getFile(oldId);
        if(!oldFile.exists()) return false;
        SpamConfig config = new SpamConfig(oldId);
        SpamConfig.delete(oldId); // Delete the old file
        config.id = newId;
        config.read(); // Save the new file
        return true;
    }

    public static boolean exists(String id) {
        return getFile(id).exists();
    }

    public static List<SpamConfig> getAllIds() {
        List<SpamConfig> ids = new ArrayList<>();
        File userDir = new File("Spam", getUsername());
        if (!userDir.exists()) return ids;
        for (File file : userDir.listFiles()){
            if(!file.getName().endsWith(".json")) continue;
            String id = file.getName().replace(".json", "");
            ids.add(new SpamConfig(id));
        }
        return ids;
    }

    private static File getFile(String id) {
        File spamDir = new File("Spam");
        if (!spamDir.exists()) spamDir.mkdir();
        File userDir = new File(spamDir, getUsername());
        if (!userDir.exists()) userDir.mkdir();
        String filename = id + ".json";
        return new File(userDir, filename);
    }

    // -------------------- Read/Write Configuration --------------------
    public void read() {
        File file = getFile(id);
        if (!file.exists()) {
            // If the file doesn't exist, initialize values and create the file
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
                lastModifiedTime = file.lastModified();
                //this.messages = populateMessages(); // Store the populated messages
                this.selectionCounts = initializeSelectionCounts(messageTemplates.length); // Initialize selection counts
                System.out.println("Spam configuration created at: " + file.getPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        // Load existing configuration
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            SpamConfig loaded = GSON.fromJson(reader, SpamConfig.class);
            if (loaded != null) {
                this.id = loaded.id;
                this.targetUsername = loaded.targetUsername;
                this.isPrivateMessage = loaded.isPrivateMessage;
                this.privateMessageCommand = loaded.privateMessageCommand;
                //this.preMinInterval = loaded.preMinInterval;
                //this.preMaxInterval = loaded.preMaxInterval;

                this.keywordTrigger = loaded.keywordTrigger;
                this.minMessageCountTrigger = loaded.minMessageCountTrigger;
                this.maxMessageCountTrigger = loaded.maxMessageCountTrigger;

                this.postTriggerMinIntervalInSeconds = loaded.postTriggerMinIntervalInSeconds;
                this.postTriggerMaxIntervalInSeconds = loaded.postTriggerMaxIntervalInSeconds;

                this.minLoopIntervalInMilliseconds = loaded.minLoopIntervalInMilliseconds;
                this.maxLoopIntervalInMilliseconds = loaded.maxLoopIntervalInMilliseconds;

                this.minTotalLoopMessagesCount = loaded.minTotalLoopMessagesCount;
                this.maxTotalLoopMessagesCount = loaded.maxTotalLoopMessagesCount;

                this.messageTemplates = loaded.messageTemplates;
                this.lastModifiedTime = file.lastModified();
                if( selectionCounts == null || selectionCounts.isEmpty() || selectionCounts.size() != messageTemplates.length) {
                    this.selectionCounts = initializeSelectionCounts(messageTemplates.length); // Initialize selection counts
                }
                System.out.println("Spam configuration loaded from file: " + file.getPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateIfNeeded() {
        File file = getFile(id);
        if (file.exists() && file.lastModified() > lastModifiedTime) {
            read(); // Reload configuration if the file has been modified
        }
    }

    // -------------------- Message Generation --------------------
//    private List<String> populateMessages() {
//        List<String> populatedMessages = new ArrayList<>();
//        String regex = "(?i)<User>"; // Case-insensitive regex for <User>
//
//        for (String template : messageTemplates) {
//            populatedMessages.add(template.replaceAll(regex, targetUsername));
//        }
//        return populatedMessages;
//    }

    private List<Integer> initializeSelectionCounts(int size) {
        List<Integer> counts = new ArrayList<>();
        for (int i = 0; i < size; i++) counts.add(0); // Initialize selection counts
        return counts;
    }

    private String getRandomMessage() {
        if (messageTemplates.length == 0) return ""; // Fallback if no messages are available

        // Calculate total score and selection probabilities
        double totalScore = 0;
        double[] probabilities = new double[messageTemplates.length];

        for (int i = 0; i < messageTemplates.length; i++) {
            double score = 1.0 / (selectionCounts.get(i) + 1); // Inverse selection count
            probabilities[i] = score;
            totalScore += score; // Sum of scores
        }

        // Normalize probabilities
        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] /= totalScore;
        }

        // Select a message based on weighted probabilities
        double randomValue = random.nextDouble();
        String selectedMessage = messageTemplates[0]; // Fallback message
        for (int i = 0; i < probabilities.length; i++) {
            randomValue -= probabilities[i];
            if (randomValue <= 0) {
                selectionCounts.set(i, selectionCounts.get(i) + 1); // Increment count
                selectedMessage = messageTemplates[i];
                break;
            }
        }

        return selectedMessage;
    }

    public String getMessage(String lastFullMessage) {
        String selectedMessage = getRandomMessage();
        String[] parts = MessageParser.parseMessage(lastFullMessage);

        // Users
        String user = targetUsername;
        String lastUser = parts[0]; // Get the last word (username)
        String onlinePlayer = onlinePlayers.getOnlinePlayer();

        // Messages
        String lastMessage = parts[1]; // The second part contains the message or status
        String lastShuffledWords = ShuffledWords.getLastShuffledWords(lastMessage);


        // Users
        selectedMessage = selectedMessage.replaceAll("(?i)<User>", user);
        selectedMessage = selectedMessage.replaceAll("(?i)<LastUser>", lastUser);
        selectedMessage = selectedMessage.replaceAll("(?i)<OnlinePlayer>", onlinePlayer);

        // Messages
        selectedMessage = selectedMessage.replaceAll("(?i)<LastMessage>", lastMessage);
        selectedMessage = selectedMessage.replaceAll("(?i)<LastShuffledWords>", lastShuffledWords);
        return selectedMessage;
    }

    public String getPrivateMessageCommand(String lastFullMessage) {
        String selectedMessage = privateMessageCommand + " " + getRandomMessage();
        if (selectedMessage.startsWith("/")) selectedMessage = selectedMessage.substring(1);
        String[] parts = MessageParser.parseMessage(lastFullMessage);

        // Users
        String user = targetUsername;
        String lastUser = parts[0]; // Get the last word (username)
        String onlinePlayer = onlinePlayers.getOnlinePlayer();

        // Messages
        String lastMessage = parts[1]; // The second part contains the message or status
        String lastShuffledWords = ShuffledWords.getLastShuffledWords(lastMessage);


        // Users
        selectedMessage = selectedMessage.replaceAll("(?i)<User>", user);
        selectedMessage = selectedMessage.replaceAll("(?i)<LastUser>", lastUser);
        selectedMessage = selectedMessage.replaceAll("(?i)<OnlinePlayer>", onlinePlayer);

        // Messages
        selectedMessage = selectedMessage.replaceAll("(?i)<LastMessage>", lastMessage);
        selectedMessage = selectedMessage.replaceAll("(?i)<LastShuffledWords>", lastShuffledWords);
        return selectedMessage;
    }

//    public long getPreDelay() {
//        return preMinInterval + (long) (Math.random() * (preMaxInterval - preMinInterval));
//    }

    public long getPostTriggerDelay() {
        long minInterval = postTriggerMinIntervalInSeconds * 1000; // Convert to milliseconds
        long maxInterval = postTriggerMaxIntervalInSeconds * 1000; // Convert to milliseconds
        return minInterval + (long) (Math.random() * (maxInterval - minInterval));
    }

    public long getLoopDelay() {
        return minLoopIntervalInMilliseconds + (long) (Math.random() * (maxLoopIntervalInMilliseconds - minLoopIntervalInMilliseconds));
    }


    public long getMessageCountTrigger() {
        return minMessageCountTrigger + (long) (Math.random() * (maxMessageCountTrigger - minMessageCountTrigger));
    }

    public long getTotalLoopMessagesCount() {
        return minTotalLoopMessagesCount + (long) (Math.random() * (maxTotalLoopMessagesCount - minTotalLoopMessagesCount));
    }

    // -------------------- Utility Methods --------------------

    @Override
    public String toString() {
        return "{\n" +
                "    \"id\": \"" + id + "\",\n" +
                "    \"targetUsername\": \"" + targetUsername + "\",\n" +
                "    \"isPrivateMessage\": " + isPrivateMessage + ",\n" +
                "    \"command\": \"" + privateMessageCommand + "\",\n" +
                "    \"triggerKeyword\": \"" + keywordTrigger + "\",\n" +
                //"    \"preMinInterval\": " + preMinInterval + ",\n" +
                //"    \"preMaxInterval\": " + preMaxInterval + ",\n" +
                //"    \"postTriggerMessageCount\": " + messageCountTrigger + ",\n" +
                "    \"minMessageCountTrigger\": " + minMessageCountTrigger + ",\n" +
                "    \"maxMessageCountTrigger\": " + maxMessageCountTrigger + ",\n" +
                "    \"postTriggerMinIntervalInSeconds\": " + postTriggerMinIntervalInSeconds + ",\n" +
                "    \"postTriggerMaxIntervalInSeconds\": " + postTriggerMaxIntervalInSeconds + ",\n" +
                "    \"minLoopIntervalInMilliseconds\": " + minLoopIntervalInMilliseconds + ",\n" +
                "    \"maxLoopIntervalInMilliseconds\": " + maxLoopIntervalInMilliseconds + ",\n" +
                "    \"minTotalLoopMessagesCount\": " + minTotalLoopMessagesCount + ",\n" +
                "    \"maxTotalLoopMessagesCount\": " + maxTotalLoopMessagesCount + ",\n" +
                "    \"messages\": " + (messageTemplates.length != 0 ? Arrays.asList(messageTemplates).toString() : "null") + ",\n" +
                "    \"lastModifiedTime\": " + lastModifiedTime + "\n" +
                "}";
    }
}
