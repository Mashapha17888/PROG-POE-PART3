package chatapp;

import javax.swing.*;
import java.io.*;
import java.util.*;

public class ChatApp {

    private static Login login = new Login();
    private static List<Message> sentMessages = new ArrayList<>();
    private static List<Message> disregardedMessages = new ArrayList<>();
    private static List<Message> storedMessages = new ArrayList<>();
    private static List<String> messageHashes = new ArrayList<>();
    private static List<String> messageIds = new ArrayList<>();

    public static void main(String[] args) {
        String username = getInputWithValidation("Enter username (max 5 chars & must contain _):",
                login::checkUserName,
                "Invalid username! Must contain '_' and be max 5 chars.",
                4);
        if (username == null) return;

        String password = getInputWithValidation("Enter password (8+ chars, uppercase, number, special char):",
                login::checkPasswordComplexity,
                "Invalid password! Must be 8+ chars, uppercase, number, special char.",
                4);
        if (password == null) return;

        String phone = getInputWithValidation("Enter phone number (format: +27XXXXXXXXX):",
                login::checkCellPhoneNumber,
                "Invalid phone number! Must be +27 followed by 9 digits.",
                4);
        if (phone == null) return;

        String firstName = getNonEmptyInput("Enter your first name:");
        if (firstName == null) return;

        String lastName = getNonEmptyInput("Enter your last name:");
        if (lastName == null) return;

        if (!login.registerUser(username, password, phone, firstName, lastName).equals("OK")) {
            JOptionPane.showMessageDialog(null, "Registration failed.");
            return;
        }

        JOptionPane.showMessageDialog(null, "Registration successful!");

        if (!tryLoginUsername(username) || !tryLoginPassword(password)) return;

        JOptionPane.showMessageDialog(null, "Welcome to QuickChat, " + firstName + "!");

        loadStoredMessages("messages.txt");

        while (true) {
            String choice = JOptionPane.showInputDialog(null,
                    "Select an option:\n" +
                    "1) Send Messages\n" +
                    "2) Show Sent Messages\n" +
                    "3) Reports & Search\n" +
                    "4) Show Stored Messages\n" +    // Added option 4 here
                    "5) Quit",
                    "Main Menu", JOptionPane.QUESTION_MESSAGE);

            switch (choice) {
                case "1": {
                    String input = JOptionPane.showInputDialog(null, "How many messages would you like to send?");
                    if (input != null && input.matches("\\d+")) {
                        sendMessages(Integer.parseInt(input));
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid number.");
                    }
                    break;
                }
                case "2":
                    showSentMessages();
                    break;
                case "3":
                    reportAndSearch();
                    break;
                case "4":
                    showStoredMessages();   // Show stored messages option
                    break;
                case "5":
                case null:
                    JOptionPane.showMessageDialog(null, "Goodbye!");
                    return;
                default:
                    JOptionPane.showMessageDialog(null, "Invalid option.");
            }
        }
    }

    private static void sendMessages(int maxMessages) {
        int sentCount = 0;

        while (sentCount < maxMessages) {
            String recipient = JOptionPane.showInputDialog(null, "Enter recipient phone number (+27XXXXXXXXX):");
            if (recipient == null || !recipient.startsWith("+27") || recipient.length() != 12) {
                JOptionPane.showMessageDialog(null, "Invalid recipient. Must start with +27 and be exactly 12 characters.");
                continue;
            }

            String messageText = JOptionPane.showInputDialog(null, "Enter message (max 250 characters):");
            if (messageText == null || messageText.length() > 250) {
                JOptionPane.showMessageDialog(null, "Please enter a message of less than 250 characters.");
                continue;
            }

            String messageId = String.format("%010d", (int) (Math.random() * 1_000_000_000L));
            Message msg = new Message(messageId, recipient, messageText);
            String hash = msg.createMessageHash(sentCount);

            String[] options = {"Send", "Disregard", "Store"};
            int choice = JOptionPane.showOptionDialog(null, "Choose action:\nHash: " + hash,
                    "Message Options", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, options, options[0]);

            if (choice == 0) {
                sentMessages.add(msg);
                messageHashes.add(hash);
                messageIds.add(messageId);
                sentCount++;
                JOptionPane.showMessageDialog(null, "Message sent!\n" + msg.sentMessage());
            } else if (choice == 1) {
                disregardedMessages.add(msg);
                JOptionPane.showMessageDialog(null, "Message disregarded.");
            } else if (choice == 2) {
                msg.storeMessage("messages.txt");
                storedMessages.add(msg);
                JOptionPane.showMessageDialog(null, "Message stored.");
            } else {
                return;
            }
        }

        JOptionPane.showMessageDialog(null, "Finished sending " + sentCount + " message(s).");
    }

    private static void showSentMessages() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No sent messages.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sentMessages.size(); i++) {
            Message msg = sentMessages.get(i);
            sb.append(msg.sentMessage())
              .append("\nHash: ").append(msg.createMessageHash(i))
              .append("\n---\n");
        }
        sb.append("Total: ").append(sentMessages.size());
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    private static void showStoredMessages() {
        if (storedMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No stored messages found.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Message msg : storedMessages) {
            sb.append(msg.sentMessage()).append("\n---\n");
        }
        sb.append("Total stored messages: ").append(storedMessages.size());
        JOptionPane.showMessageDialog(null, sb.toString());
    }

    private static void reportAndSearch() {
        String[] options = {
                "a) Show sender & recipient of all sent messages",
                "b) Show longest message",
                "c) Search by Message ID",
                "d) Search messages by recipient",
                "e) Delete message by hash",
                "f) Show full report of all sent messages"
        };
        String input = (String) JOptionPane.showInputDialog(null, "Choose an action:", "Reports & Search",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (input == null) return;

        switch (input.charAt(0)) {
            case 'a':
                StringBuilder senders = new StringBuilder();
                for (Message msg : sentMessages) {
                    senders.append("Recipient: ").append(msg.getRecipient())
                            .append(", Message: ").append(msg.getMessageText()).append("\n");
                }
                JOptionPane.showMessageDialog(null, senders.toString());
                break;
            case 'b':
                Message longest = sentMessages.stream()
                        .max(Comparator.comparingInt(m -> m.getMessageText().length())).orElse(null);
                JOptionPane.showMessageDialog(null, "Longest message:\n" + (longest != null ? longest.sentMessage() : "None"));
                break;
            case 'c':
                String idSearch = JOptionPane.showInputDialog("Enter Message ID:");
                sentMessages.stream()
                        .filter(m -> m.getMessageId().equals(idSearch))
                        .findFirst()
                        .ifPresentOrElse(
                                m -> JOptionPane.showMessageDialog(null, m.sentMessage()),
                                () -> JOptionPane.showMessageDialog(null, "Message ID not found.")
                        );
                break;
            case 'd':
                String recipientSearch = JOptionPane.showInputDialog("Enter recipient number:");
                List<Message> found = sentMessages.stream()
                        .filter(m -> m.getRecipient().equals(recipientSearch)).toList();
                if (found.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No messages found.");
                } else {
                    StringBuilder sb = new StringBuilder();
                    found.forEach(m -> sb.append(m.sentMessage()).append("\n---\n"));
                    JOptionPane.showMessageDialog(null, sb.toString());
                }
                break;
            case 'e':
                String hashToDelete = JOptionPane.showInputDialog("Enter hash to delete:");
                for (int i = 0; i < sentMessages.size(); i++) {
                    if (sentMessages.get(i).createMessageHash(i).equals(hashToDelete)) {
                        sentMessages.remove(i);
                        messageHashes.remove(i);
                        messageIds.remove(i);
                        JOptionPane.showMessageDialog(null, "Message deleted.");
                        return;
                    }
                }
                JOptionPane.showMessageDialog(null, "Hash not found.");
                break;
            case 'f':
                showSentMessages();
                break;
        }
    }

    private static void loadStoredMessages(String path) {
        File file = new File(path);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("{")) continue;
                String[] parts = line.replace("{", "").replace("}", "").split(",");
                String id = parts[0].split(":")[1].replace("\"", "").trim();
                String recipient = parts[1].split(":")[1].replace("\"", "").trim();
                String message = parts[2].split(":")[1].replace("\"", "").trim();
                storedMessages.add(new Message(id, recipient, message));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Failed to load stored messages.");
        }
    }

    private static boolean tryLoginUsername(String registeredUsername) {
        for (int i = 0; i < 4; i++) {
            String input = JOptionPane.showInputDialog("Login - Enter username:");
            if (input == null) return false;
            if (input.equals(registeredUsername)) return true;
            JOptionPane.showMessageDialog(null, "Incorrect username! Attempts left: " + (3 - i));
        }
        return false;
    }

    private static boolean tryLoginPassword(String registeredPassword) {
        for (int i = 0; i < 4; i++) {
            String input = JOptionPane.showInputDialog("Login - Enter password:");
            if (input == null) return false;
            if (input.equals(registeredPassword)) return true;
            JOptionPane.showMessageDialog(null, "Incorrect password! Attempts left: " + (3 - i));
        }
        return false;
    }

    private static String getInputWithValidation(String prompt, Validator validator, String errorMessage, int maxAttempts) {
        for (int i = 0; i < maxAttempts; i++) {
            String input = JOptionPane.showInputDialog(prompt);
            if (input == null) return null;
            if (validator.validate(input)) return input;
            JOptionPane.showMessageDialog(null, errorMessage + " Attempts left: " + (maxAttempts - i - 1));
        }
        return null;
    }

    private static String getNonEmptyInput(String prompt) {
        while (true) {
            String input = JOptionPane.showInputDialog(prompt);
            if (input == null) return null;
            if (!input.trim().isEmpty()) return input;
            JOptionPane.showMessageDialog(null, "Input cannot be empty.");
        }
    }

    interface Validator {
        boolean validate(String input);
    }
}
