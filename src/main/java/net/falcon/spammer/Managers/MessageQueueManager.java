package net.falcon.spammer.Managers;

import net.falcon.spammer.Handlers.ChatMessageHandler;
import net.falcon.spammer.Models.SpamConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.*;
import java.util.logging.Logger;

public class MessageQueueManager {
    private final BlockingQueue<Pair<String, Boolean>> messageQueue = new LinkedBlockingQueue<>();
    private final Logger logger = Logger.getLogger(MessageQueueManager.class.getName());
    private final SpamConfig config;

    // Constructor to accept the spam ID, minimum delay, and maximum delay
    public MessageQueueManager(SpamConfig config) {
        this.config = config;
        Executors.newSingleThreadExecutor().execute(this::processQueue); // Start processing the queue
    }

    // Adds a message and boolean flag to the queue
    public void enqueueMessage(String message, boolean flag) {
        messageQueue.offer(Pair.of(message, flag));
        //logger.info("Message enqueued: " + message + " with flag: " + flag + " and spamId: " + spamId);
    }

    // Continuously process the queue with a random delay
    private void processQueue() {
        while (SpamManager.spamStatus.getOrDefault(config.id, false)) {
            try {
                Pair<String, Boolean> data = messageQueue.take(); // Blocks until available

                // Skip processing if the spamId is not active
                if (!SpamManager.spamStatus.getOrDefault(config.id, false)) continue;

                sendMessage(data);
                long randomDelay = getRandomDelay(); // Get a random delay between min and max
                Thread.sleep(randomDelay); // Add delay between sending messages
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Handle interrupt
            } catch (Exception e) {
                logger.severe("Error while sending message: " + e.getMessage());
            }
        }
    }

    // Generate a random delay between minDelayMillis and maxDelayMillis
    private long getRandomDelay() {
        return config.getGlobalDelay();
    }

    // Handle message and boolean flag
    private void sendMessage(Pair<String, Boolean> data) {
        String message = data.getLeft();
        boolean flag = data.getRight();

        if (!SpamManager.spamStatus.getOrDefault(config.id, false)) return;

        if (flag) {
            ChatMessageHandler.sendCommand(message);
        } else {
            ChatMessageHandler.sendChatMessage(message);
        }
    }

    // Await until all messages are processed and the queue is empty
    public void awaitFinishing() {
        while (!messageQueue.isEmpty()) {
            try {
                // Sleep for a short time before checking again to avoid busy-waiting
                Thread.sleep(200); // Check every 100 milliseconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Handle interrupt
            }
        }
    }
}
