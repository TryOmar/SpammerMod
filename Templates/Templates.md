# Here I'll Share Some Templates I've Created

# Message Sending Script Templates

## Template 1: Greeting and Sequential Messaging

This template sends messages to a player using the command `/tell <User>`.

```json
{
  "id": "25",
  "userTag": "Mohaned_",
  "useCommand": true,
  "commandTemplate": "/tell <User>",
  "keywordTrigger": "",
  "minReceiveMessagesTrigger": 0,
  "maxReceiveMessagesTrigger": 0,
  "localMinDelaySeconds": 1,
  "localMaxDelaySeconds": 1,
  "globalMinDelayMillis": 100,
  "globalMaxDelayMillis": 500,
  "minMessagesToSend": 1,
  "maxMessagesToSend": 1,
  "messageTemplates": [
    "Hi <User>|3000 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10 | 11 | 12 | 14 | 15 | 16 | 17 | 18 | 19 | 20 | 21 | 22 | 23 | 24 | 25"
  ]
}
```

- **Script ID:** `25` - *Uniquely identifies the script.*
- **User Tag:** `Mohaned_` - *Specifies the target player.*
- **Command Usage:** `true` - *Indicates that the script will send messages.*
- **Global Delay:** `100 ms to 500 ms` - *Defines the delay between each message.*

### Message Flow:
1. **Greeting:** "Hi <User>" - *Sends a friendly introduction.*
2. **Sequential Messages:** 1 to 25 - *After a 3-second wait, sends numbered messages with the defined global delay.*

This template enables dynamic player interactions with a greeting followed by engaging messages.

---

# Message Sending Script Templates

## Template 2: Repeat Last Message

This template repeats the last message sent by a player in chat using the command `/tell <LastUser>`.

```json
{
  "id": "repeat",
  "userTag": "",
  "useCommand": true,
  "commandTemplate": "/tell <LastUser>",
  "keywordTrigger": "",
  "minReceiveMessagesTrigger": 1,
  "maxReceiveMessagesTrigger": 1,
  "localMinDelaySeconds": 3,
  "localMaxDelaySeconds": 6,
  "globalMinDelayMillis": 100,
  "globalMaxDelayMillis": 500,
  "minMessagesToSend": 99999999,
  "maxMessagesToSend": 99999999,
  "messageTemplates": [
    "<LastMessage>"
  ]
}
```

- **Script ID:** `repeat` - *Identifies the script.*
- **User Tag:** (empty) - *No specific target; responds to the last user who spoke.*
- **Command Usage:** `true` - *Enables message sending via command.*
- **Command Template:** `/tell <LastUser>` - *Sends the last message to the most recent player.*
- **Keyword Trigger:** (empty) - *Responds to any message in chat.*
- **Receive Message Trigger:** `1` - *Waits for just one message to repeat it.*
- **Local Delay:** `3 to 6 seconds` - *Waits this duration before sending the repeated message.*
- **Global Delay:** `100 ms to 500 ms` - *Prevents spam by adding a random delay between messages.*
- **Messages to Send:** `99999999` - *Runs indefinitely, repeating messages as long as conditions are met.*
- **Message Template:** `<LastMessage>` - *Sends the last message received from the user.*

### Process Overview:
The script captures the last message spoken in chat and repeats it back to the last user, enhancing interaction without requiring specific triggers. It employs random delays to avoid spam detection while ensuring continuous operation.

1. **Player:** "Hi there!"  
   **Script:** /tell <LastUser> "Hi there!"

2. **Player:** "How's it going?"  
   **Script:** /tell <LastUser> "How's it going?"

3. **Player:** "Let’s play games tonight!"  
   **Script:** /tell <LastUser> "Let’s play games tonight!"

This template enables dynamic interaction by allowing players to hear their last messages echoed back to them.


---


## Template 3: Repeat Last Shuffled Words

This template sends a shuffled version of the last message sent by a player in chat using the command `/tell <LastUser>`.

~~~
{
  "id": "shuffleRepeat",
  "userTag": "",
  "useCommand": true,
  "commandTemplate": "/tell <LastUser>",
  "keywordTrigger": "",
  "minReceiveMessagesTrigger": 1,
  "maxReceiveMessagesTrigger": 1,
  "localMinDelaySeconds": 3,
  "localMaxDelaySeconds": 6,
  "globalMinDelayMillis": 100,
  "globalMaxDelayMillis": 500,
  "minMessagesToSend": 99999999,
  "maxMessagesToSend": 99999999,
  "messageTemplates": [
    "<LastShuffledWords>"
  ]
}
~~~

- **Script ID:** `shuffleRepeat` - *Identifies the script.*
- **User Tag:** (empty) - *No specific target; responds to the last user who spoke.*
- **Command Usage:** `true` - *Enables message sending via command.*
- **Command Template:** `/tell <LastUser>` - *Sends the shuffled message to the most recent player.*
- **Keyword Trigger:** (empty) - *Responds to any message in chat.*
- **Receive Message Trigger:** `1` - *Waits for just one message to repeat it.*
- **Local Delay:** `3 to 6 seconds` - *Waits this duration before sending the shuffled message.*
- **Global Delay:** `100 ms to 500 ms` - *Prevents spam by adding a random delay between messages.*
- **Messages to Send:** `99999999` - *Runs indefinitely, repeating messages as long as conditions are met.*
- **Message Template:** `<LastShuffledWords>` - *Sends a shuffled selection of words from the last message received from the user.*

### Process Overview:
The script captures the last message spoken in chat, shuffles its words, and sends back a selection of those words to the last user. This method enhances interaction while making it less obvious that a script is being used.

1. **Player:** "Hi there!"  
   **Script:** /tell <LastUser> "there Hi!"

2. **Player:** "How's it going?"  
   **Script:** /tell <LastUser> "going it?"

3. **Player:** "Let’s play games tonight!"  
   **Script:** /tell <LastUser> "games tonight play!"

---

## Template 4: Message to Quiet a Player

This template sends a polite reminder to a specified player to stay quiet using the command `/tell <User>`.

~~~
{
  "id": "repeat",
  "userTag": "Mohaned_",
  "useCommand": true,
  "commandTemplate": "/tell <User>",
  "keywordTrigger": "",
  "minReceiveMessagesTrigger": 1,
  "maxReceiveMessagesTrigger": 1,
  "localMinDelaySeconds": 3,
  "localMaxDelaySeconds": 6,
  "globalMinDelayMillis": 100,
  "globalMaxDelayMillis": 500,
  "minMessagesToSend": 99999999,
  "maxMessagesToSend": 99999999,
  "messageTemplates": [
    "Stay quiet, <User>",
    "Please be quiet, <User>",
    "Don't exaggerate, <User>",
    "Let's calm down, <User>",
    "You talk too much, <User>",
    "Can you be quiet, <User>",
    "Stop talking, <User>",
    "A bit of quiet, <User>",
    "No need to talk, <User>",
    "Take a break, <User>"
  ]
}
~~~

- **Script ID:** `repeat` - *Identifies the script.*
- **User Tag:** `Mohaned_` - *Targets a specific user, in this case, Mohaned, for personalized responses.*
- **Command Usage:** `true` - *Enables message sending via command.*
- **Command Template:** `/tell <User>` - *Sends a message to the specified user.*
- **Keyword Trigger:** (empty) - *No specific keyword is required to activate the script; it responds to any received message.*
- **Receive Message Trigger:** `1` - *Waits for just one message to trigger the response.*
- **Local Delay:** `3 to 6 seconds` - *The script waits between 3 to 6 seconds before sending the response to create a more natural interaction.*
- **Global Delay:** `100 ms to 500 ms` - *Introduces a random delay between messages to prevent spam detection.*
- **Messages to Send:** `99999999` - *Allows for indefinite repetition of the script as long as the conditions are met.*
- **Message Templates:** A list of responses that remind the user to be quiet. Each message includes the user’s name for personalization, such as:
   - "Stay quiet, <User>"
   - "Please be quiet, <User>"
   - "Don't exaggerate, <User>"
   - "Let's calm down, <User>"
   - "You talk too much, <User>"
   - "Can you be quiet, <User>"
   - "Stop talking, <User>"
   - "A bit of quiet, <User>"
   - "No need to talk, <User>"
   - "Take a break, <User>"

### Process Overview:
This script targets a specific player and gently reminds them to be quiet by sending various pre-defined messages. It creates a personalized interaction without being overly intrusive, aiming to maintain a friendly chat environment.

1. **Player:** "I'm just trying to share my ideas!"  
   **Script:** /tell Mohaned_ "Please be quiet, Mohaned_"

2. **Player:** "I have so much to say!"  
   **Script:** /tell Mohaned_ "You talk too much, Mohaned_"

3. **Player:** "What about my suggestions?"  
   **Script:** /tell Mohaned_ "Can you be quiet, Mohaned_"

This template fosters a fun and interactive way to manage conversations by reminding specific players to reduce their chat frequency.


