# Spam Manager

## Overview
The **Spam Manager** is a Minecraft mod for automating and managing chat spamming through a configuration system. It allows users to create, manage, and trigger spam messages within the game, offering full control over various spam-related tasks.

## Important Disclaimer
This mod is intended for legitimate purposes such as:
- Automating repetitive messages for testing purposes
- Creating simple auto-response systems
- Testing anti-spam mechanisms on servers
- Development and debugging of chat-related features

**Note:** We are not responsible for any misuse of this mod. Users must comply with server rules and guidelines. Using this mod to harass other players or break server rules is strictly discouraged and may result in consequences from server administrators.

## Features
- **Command-Based Management**: Supports multiple commands for handling spam configurations, including creating, deleting, renaming, and running spam sessions.
- **Configuration Files**: Spam behavior is controlled via configuration files, each identified by a unique ID.
- **Automatic Chat Scanning**: Includes a feature for scanning in-game chat for specific usernames or keywords.
- **Spam Control**: Allows users to run and stop spam sessions at any time.
- **File Management**: Directly links to configuration files and spam folders from the game chat for easy access.

## Commands

- `!helpSpam`: Show the available spam commands.
- `!showSpam <ID>`: Display the details of a specific spam configuration.
- `!createSpam <ID>`: Create a new spam configuration file with the specified ID.
- `!deleteSpam <ID>`: Delete an existing spam configuration.
- `!renameSpam <ID> <newID>`: Rename a spam configuration.
- `!runSpam <ID>`: Run the spam using the specified configuration.
- `!stopSpam <ID>`: Stop the spam for the given ID.
- `!folderSpam`: Open the folder containing all spam configurations.
- `!scanChat <name>`: Scan the chat for a specific name or keyword.
- `!scanClear`: Clear the chat scan results.

## Spam Configuration Example

```json
{
  "id": "rep",
  "userTag": "ProofPlease",
  "useCommand": true,
  "commandTemplate": "/tell <User>",
  "keywordTrigger": "hi|hey&!bye",
  "minReceiveMessagesTrigger": 1,
  "maxReceiveMessagesTrigger": 1,
  "localMinDelaySeconds": 3,
  "localMaxDelaySeconds": 6,
  "globalMinDelayMillis": 100,
  "globalMaxDelayMillis": 500,
  "minMessagesToSend": 5,
  "maxMessagesToSend": 10,
  "messageTemplates": [
    "Message1: Target user: <User>, Last user: <LastUser>, Last message: <LastMessage>, Shuffled words: <LastShuffledWords>, Online player: <OnlinePlayer>",
    "Message2: Target user: <User>, Last sender: <LastUser>, Last message: <LastMessage>, Shuffled words: <LastShuffledWords>, Online player: <OnlinePlayer>",
    "Message3: Current user: <User>, Recent sender: <LastUser>, Last message: <LastMessage>, Shuffled words: <LastShuffledWords>, Online player: <OnlinePlayer>",
    "Message4: Hi Bro|3000 New Message Hi After 3s!"
  ]
}
```

### Configuration Details
- **id**: The name of the spam configuration.
- **userTag**: The name that will be replaced with `<User>`.
- **useCommand**: Set to `true` if you want to send the message as a private message or command (e.g., `/tell <User>`).
- **keywordTrigger**: Defines keywords that need to appear in chat for spam to trigger.
    - `|` = OR
    - `&` = AND
    - `!` = NOT
    - Parentheses `()` can be used for grouping expressions.

- **minReceiveMessagesTrigger** and **maxReceiveMessagesTrigger**: Number of messages received before responding. A random value between min and max is chosen.
- **localMinDelaySeconds** and **localMaxDelaySeconds**: Delay for each individual message.
- **globalMinDelayMillis** and **globalMaxDelayMillis**: Global delay between all messages.
- **minMessagesToSend** and **maxMessagesToSend**: Defines how many messages to send. Use a large number (e.g., `9999999999`) for infinite spamming.
- **messageTemplates**: Templates for the spam messages. Supported tags include:
    - `<User>`: The current user tag.
    - `<LastUser>`: Last user who sent a message (excluding yourself).
    - `<LastMessage>`: Last message sent in chat.
    - `<LastShuffledWords>`: The last message shuffled, with random words selected.
    - `<OnlinePlayer>`: A random online player.

### Example Explained

This is an example configuration for the Spam Manager:

- **id**: The identifier for this spam configuration is `"rep"`.
- **userTag**: The user tag is `"ProofPlease"`, which will replace `<User>` in the messages.
- **useCommand**: Set to `true`, meaning messages will be sent using the command template (`/tell <User>`), making them private messages.
- **commandTemplate**: The command used is `"/tell <User>"`, which will send private messages to the user specified in `<User>`.
- **keywordTrigger**: The message will only trigger if someone sends either `"hi"` or `"hey"`, but it will not trigger if the word `"bye"` is present.
    - For example, the message will send if someone says "hi" or "hey," but not if they include "bye."

- **minReceiveMessagesTrigger** and **maxReceiveMessagesTrigger**: The mod will wait to receive 1 message (as both are set to 1) before sending a response.
- **localMinDelaySeconds** and **localMaxDelaySeconds**: Each individual message will be sent after a delay randomly chosen between 3 to 6 seconds.
- **globalMinDelayMillis** and **globalMaxDelayMillis**: The delay between all messages will be between 100 to 500 milliseconds.
- **minMessagesToSend** and **maxMessagesToSend**: The mod will send between 5 to 10 messages in total, as defined by the random choice between these numbers.

- **messageTemplates**: There are four message templates that can be sent. Each one includes variables that will be replaced dynamically:
    - **Message 1**: `"Message1: Target user: <User>, Last user: <LastUser>, Last message: <LastMessage>, Shuffled words: <LastShuffledWords>, Online player: <OnlinePlayer>"`
    - **Message 2**: `"Message2: Target user: <User>, Last sender: <LastUser>, Last message: <LastMessage>, Shuffled words: <LastShuffledWords>, Online player: <OnlinePlayer>"`
    - **Message 3**: `"Message3: Current user: <User>, Recent sender: <LastUser>, Last message: <LastMessage>, Shuffled words: <LastShuffledWords>, Online player: <OnlinePlayer>"`
    - **Message 4**: `"Message4: Hi Bro|3000 New Message Hi After 3s!"` (This message is static and unrelated to the dynamic variables).

These variables allow for more personalized and randomized messages to be sent during the spam session.

## Usage
To use this mod effectively:

1. **Create a spam configuration** with the `!createSpam <ID>` command.
2. **Customize the configuration file** by editing the parameters such as `userTag`, `messageTemplates`, `keywordTrigger`, and delays.
3. **Run the spam session** using the `!runSpam <ID>` command.
4. You can stop the spam session at any time with the `!stopSpam <ID>` command.
5. **Use chat scanning** to monitor in-game chat with the `!scanChat <name>` command, which looks for specific usernames or keywords.
6. **Access configuration files** through the game using `!folderSpam` for easy management.

## Todo
- [ ] Show chat error message if the spam configuration is not correctly set up.
- [ ] Add <OnlinePlayersNumber> tag to get the number of online players.
- [ ] Allow get triggers from System chat.
- [ ] Add GUI support for easier configuration management.
- [ ] Test the mod in different Minecraft versions for compatibility.
- [ ] Test the mod in different multiplayer servers to ensure proper functionality.
- [ ] Add more Template examples for different spamming scenarios.

## Conclusion
The **Spam Manager** provides a flexible and customizable way to automate chat interactions in Minecraft. Whether for fun or more practical uses, this mod offers a wide range of options for spamming and chat management.
