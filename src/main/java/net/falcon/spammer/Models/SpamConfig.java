package net.falcon.spammer.Models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpamConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().disableInnerClassSerialization().create();

    public String id;
    private String targetUsername = getUsername();

    public boolean isPrivateMessage = true;
    private String privateMessageCommand = "/tell <User>";

    //private long preMinInterval = 3000;
    //private long preMaxInterval = 6000;

    public String keywordTrigger = "hi";
    public int messageCountTrigger = 1;

    private long postMinInterval = 3000;
    private long postMaxInterval = 6000;

    private boolean isRandomized = true;
    private String[] messageTemplates = {
            "Message1: Target user: <User>, Last user: <LastUser>, Last message: <LastMessage>",
            "Message2: Target user: <User>, Last sender: <LastUser>, Last message: <LastMessage>",
            "Message3: Current user: <User>, Recent sender: <LastUser>, Last message: <LastMessage>"
    };

    private transient long lastModifiedTime = System.currentTimeMillis();
    private transient List<String> messages;
    private transient List<Integer> selectionCounts;
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

    public static List<String> getAllIds() {
        List<String> ids = new ArrayList<>();
        File userDir = new File("Spam", getUsername());
        if (!userDir.exists()) return ids;
        for (File file : userDir.listFiles())
            ids.add(file.getName().replace(".json", ""));
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
                this.messages = populateMessages(); // Store the populated messages
                this.selectionCounts = initializeSelectionCounts(this.messages.size()); // Initialize selection counts
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
                this.messageCountTrigger = loaded.messageCountTrigger;

                this.postMinInterval = loaded.postMinInterval;
                this.postMaxInterval = loaded.postMaxInterval;

                this.isRandomized = loaded.isRandomized;
                this.messageTemplates = loaded.messageTemplates;
                this.lastModifiedTime = file.lastModified();
                if(messages == null || messages.isEmpty() || selectionCounts == null || selectionCounts.isEmpty() || messages != populateMessages()){
                    this.messages = populateMessages(); // Store the populated messages
                    this.selectionCounts = initializeSelectionCounts(this.messages.size()); // Initialize selection counts
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
    private List<String> populateMessages() {
        List<String> populatedMessages = new ArrayList<>();
        String regex = "(?i)<User>"; // Case-insensitive regex for <User>

        for (String template : messageTemplates) {
            populatedMessages.add(template.replaceAll(regex, targetUsername));
        }
        return populatedMessages;
    }

    private List<Integer> initializeSelectionCounts(int size) {
        List<Integer> counts = new ArrayList<>();
        for (int i = 0; i < size; i++) counts.add(0); // Initialize selection counts
        return counts;
    }

    public String getMessage(String lastFullMessage) {
        String lastUser = lastFullMessage.split(":")[0];
        String lastMessage = lastFullMessage.substring(lastUser.length() + 1).trim();

        if (messages.isEmpty()) return ""; // Fallback if no messages are available

        // Calculate total score and selection probabilities
        double totalScore = 0;
        double[] probabilities = new double[messages.size()];

        for (int i = 0; i < messages.size(); i++) {
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
        String selectedMessage = messages.get(0); // Fallback message
        for (int i = 0; i < probabilities.length; i++) {
            randomValue -= probabilities[i];
            if (randomValue <= 0) {
                selectionCounts.set(i, selectionCounts.get(i) + 1); // Increment count
                selectedMessage = messages.get(i);
                break;
            }
        }

        selectedMessage = selectedMessage.replaceAll("(?i)<User>", targetUsername);
        selectedMessage = selectedMessage.replaceAll("(?i)<LastUser>", lastUser);
        selectedMessage = selectedMessage.replaceAll("(?i)<LastMessage>", lastMessage);
        return selectedMessage;
    }

    public String getCommand(String lastFullMessage) {
        String lastUser = lastFullMessage.split(":")[0];
        String modifiedCommand = privateMessageCommand.replaceAll("(?i)<User>", targetUsername);
        modifiedCommand = modifiedCommand.replaceAll("(?i)<LastUser>", lastUser);
        if (modifiedCommand.startsWith("/")) modifiedCommand = modifiedCommand.substring(1);
        return modifiedCommand;
    }

//    public long getPreDelay() {
//        return preMinInterval + (long) (Math.random() * (preMaxInterval - preMinInterval));
//    }

    public long getPostDelay() {
        return postMinInterval + (long) (Math.random() * (postMaxInterval - postMinInterval));
    }

    // -------------------- Utility Methods --------------------

    @Override
    public String toString() {
        return "{\n" +
                "    \"id\": \"" + id + "\",\n" +
                "    \"targetUsername\": \"" + targetUsername + "\",\n" +
                "    \"isPrivateMessage\": " + isPrivateMessage + ",\n" +
                "    \"command\": \"" + getCommand("") + "\",\n" +
                "    \"triggerKeyword\": \"" + keywordTrigger + "\",\n" +
                //"    \"preMinInterval\": " + preMinInterval + ",\n" +
                //"    \"preMaxInterval\": " + preMaxInterval + ",\n" +
                "    \"postTriggerMessageCount\": " + messageCountTrigger + ",\n" +
                "    \"postMinInterval\": " + postMinInterval + ",\n" +
                "    \"postMaxInterval\": " + postMaxInterval + ",\n" +
                "    \"isRandomized\": " + isRandomized + ",\n" +
                "    \"messages\": " + (messages != null ? messages.toString() : "null") + ",\n" +
                "    \"lastModifiedTime\": " + lastModifiedTime + "\n" +
                "}";
    }
}
