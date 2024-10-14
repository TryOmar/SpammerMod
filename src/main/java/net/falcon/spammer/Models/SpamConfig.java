package net.falcon.spammer.Models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpamConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().disableInnerClassSerialization().create();

    public final String id;
    private String targetUsername = "targetUser";
    public boolean isPrivateMessage = false;
    private String privateMessageCommand = "/tell @<User>";
    public String triggerKeyword = "hey";
    private long minInterval = 3000;
    private long maxInterval = 6000;
    private boolean isRandomized = true;
    public int postTriggerMessageCount = 20;
    private String[] messageTemplates = { "Hello man, how are you @<User>?", "What's up @<User>?", "How's everything going @<User>?", "Just checking in @<User>, hope all is good!" };

    private transient int currentMessageIndex = 0;

    public static String getUsername(){ return MinecraftClient.getInstance().getSession().getUsername(); }

    public SpamConfig(String id) {
        this.id = id;
        read();
    }

    private static File getFile(String id) {
        File spamDir = new File("Spam");
        if (!spamDir.exists()) spamDir.mkdir();
        File userDir = new File(spamDir, getUsername());
        if (!userDir.exists()) userDir.mkdir();
        String filename =  id + ".json";
        return new File(userDir, filename);
    }

    public void write() {
        try (FileWriter writer = new FileWriter(getFile(id))) {
            GSON.toJson(this, writer);
            System.out.println("Spam template saved at: " + getFile(id).getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void read() {
        File file = getFile(id);
        if (!file.exists()) { write(); return; }

        try (FileReader reader = new FileReader(file)) {
            SpamConfig loaded = GSON.fromJson(reader, SpamConfig.class);
            if (loaded != null) {
                this.targetUsername = loaded.targetUsername;
                this.isPrivateMessage = loaded.isPrivateMessage;
                this.privateMessageCommand = loaded.privateMessageCommand;
                this.triggerKeyword = loaded.triggerKeyword;
                this.minInterval = loaded.minInterval;
                this.maxInterval = loaded.maxInterval;
                this.isRandomized = loaded.isRandomized;
                this.postTriggerMessageCount = loaded.postTriggerMessageCount;
                this.messageTemplates = loaded.messageTemplates;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String toString() {
        return "{\n" +
                "    \"id\": \"" + id + "\",\n" +
                "    \"targetUsername\": \"" + targetUsername + "\",\n" +
                "    \"isPrivateMessage\": " + isPrivateMessage + ",\n" +
                "    \"privateMessageCommand\": \"" + privateMessageCommand + "\"\n" +
                "    \"triggerKeyword\": \"" + triggerKeyword + "\",\n" +
                "    \"minInterval\": " + minInterval + ",\n" +
                "    \"maxInterval\": " + maxInterval + ",\n" +
                "    \"isRandomized\": " + isRandomized + ",\n" +
                "    \"postTriggerMessageCount\": " + postTriggerMessageCount + ",\n" +
                "    \"messageTemplates\": " + messageTemplates + "\n" +
                "}";
    }


    public static List<String> getAllIds() {
        List<String> ids = new ArrayList<>();
        File userDir = new File("Spam", getUsername());
        if (!userDir.exists()) return ids;
        for (File file : userDir.listFiles())
            ids.add(file.getName().replace(".json", ""));
        return ids;
    }
    public static boolean exists(String id) { return getFile(id).exists(); }

    public static void delete(String id) {
        File file = getFile(id);
        if (file.exists()) file.delete();
    }


    //  ----------------------------- Getters and Setters -----------------------------
    public String getMessage() {
        List<String> messages = new ArrayList<>();

        String regex = "(?i)@<User>";
        for (String template : messageTemplates) messages.add(template.replaceAll(regex, targetUsername));
        if (messages.isEmpty()) return "";

        if (isRandomized) currentMessageIndex = (int) (Math.random() * messages.size());
        else currentMessageIndex = (currentMessageIndex + 1) % messages.size();

        return messages.get(currentMessageIndex);
    }

    public long getDelay() {
        return minInterval + (long) (Math.random() * (maxInterval - minInterval));
    }


    public String populateCommand() {
        String regex = "(?i)@<User>"; // Case-insensitive regex for <User>
        String modifiedCommand = privateMessageCommand.replaceAll(regex, targetUsername);
        // Remove the leading '/' if it exists
        if (modifiedCommand.startsWith("/")) modifiedCommand = modifiedCommand.substring(1);
        return modifiedCommand;
    }
}
