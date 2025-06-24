package chatapp;

import java.io.FileWriter;
import java.io.IOException;

public class Message {
    private String messageId;
    private String recipient;
    private String messageText;

    public Message(String messageId, String recipient, String messageText) {
        this.messageId = messageId;
        this.recipient = recipient;
        this.messageText = messageText;
    }

    public String sentMessage() {
        return "To: " + recipient + "\nMessage: " + messageText + "\nMessage ID: " + messageId;
    }

    public boolean checkMessageID() {
        return messageId != null && messageId.length() <= 10;
    }

    public int checkRecipientCell() {
        if (recipient != null && recipient.startsWith("+27") && recipient.length() == 12) {
            return 1;
        } else {
            return 0;
        }
    }

    public String createMessageHash(int count) {
        String firstTwoDigits = messageId.length() >= 2 ? messageId.substring(0, 2) : "00";
        String[] words = messageText.trim().split("\\s+");
        String firstWord = words.length > 0 ? words[0] : "";
        String lastWord = words.length > 1 ? words[words.length - 1] : firstWord;
        return (firstTwoDigits + ":" + count + ":" + firstWord + lastWord).toUpperCase();
    }

    public void storeMessage(String filePath) {
        try (FileWriter writer = new FileWriter(filePath, true)) {
            String jsonLine = String.format(
                "{\"id\":\"%s\",\"recipient\":\"%s\",\"message\":\"%s\"}\n",
                messageId,
                recipient,
                messageText.replace("\"", "\\\"") // escape quotes
            );
            writer.write(jsonLine);
        } catch (IOException e) {
            System.err.println("Error saving message: " + e.getMessage());
        }
    }

    public String getMessageId() {
        return messageId;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }
}
